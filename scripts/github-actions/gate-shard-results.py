#!/usr/bin/env python3
import argparse
import json
import re
from pathlib import Path


def sanitize(name: str) -> str:
    return re.sub(r"[^a-zA-Z0-9._-]", "_", name)


def main() -> None:
    parser = argparse.ArgumentParser(
        description="Decide the outcome of a shard from its rp-export and its queue record: fail on any failed test "
                    "without a known issue, on any skipped test (a skip means a configuration method failed, usually the "
                    "container start), on a Maven run that exited with an error and on a claimed class that exported no result."
    )
    parser.add_argument("--export-dir", default="target/rp-export", type=Path)
    parser.add_argument("--queue-file", required=True, type=Path, help="target/queue/<shard>.json written by run-test-queue.py")
    args = parser.parse_args()

    ran = json.loads(args.queue_file.read_text(encoding="utf-8")) if args.queue_file.exists() else []
    if not ran:
        print("The shard claimed no class: the queue was already empty")
        return

    blocking: list[str] = []
    known_issues: list[str] = []
    fixed_candidates: list[str] = []
    passed = 0
    for item in ran:
        class_dir = args.export_dir / "tests" / sanitize(str(item["class"]))
        exported = any(class_dir.rglob("result.json"))
        if item.get("exitCode", 0) != 0 or not exported:
            blocking.append(f'{item["class"]}: Maven exit code {item.get("exitCode")}, exported results: {exported}')

    for result_file in sorted(args.export_dir.rglob("result.json")):
        result = json.loads(result_file.read_text(encoding="utf-8"))
        test = f"{result_file.parent.parent.name}.{result_file.parent.name}"
        status = str(result.get("status") or "UNKNOWN")
        ticket = str((result.get("knownIssue") or {}).get("ticket") or "")
        if status == "PASSED":
            passed += 1
            if ticket:
                fixed_candidates.append(f"{test} ({ticket})")
        elif status == "FAILED" and ticket:
            known_issues.append(f"{test} ({ticket})")
        else:
            blocking.append(f"{test}: {status}")

    print(f"Classes run: {len(ran)}, passed: {passed}, failed with a known issue: {len(known_issues)}, blocking: {len(blocking)}")
    for item in known_issues:
        print(f"  known issue  {item}")
    for item in fixed_candidates:
        print(f"  passed although a known issue is declared  {item}")
    for item in blocking:
        print(f"  BLOCKING  {item}")
    if blocking:
        raise SystemExit("The shard has failed or skipped tests without a known issue, or a class run that produced no results")


if __name__ == "__main__":
    main()
