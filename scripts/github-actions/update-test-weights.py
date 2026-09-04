#!/usr/bin/env python3
import argparse
import json
from collections import defaultdict
from pathlib import Path

FIXED_SECONDS_PER_TEST = 60

def main() -> None:
    parser = argparse.ArgumentParser(
        description=(
            "Refresh test-weights.json from the summary.json of a merged test report: the weight of a "
            "class is the summed duration of its tests plus a fixed 60 s per test for the container start-up."
        )
    )
    parser.add_argument("--summary", required=True, type=Path, help="merged-report/summary.json of a full run")
    parser.add_argument("--weights-file", default=Path(__file__).with_name("test-weights.json"), type=Path)
    parser.add_argument(
        "--keep-missing",
        action="store_true",
        help="Keep the weights of classes absent from the summary instead of dropping them.",
    )
    args = parser.parse_args()

    summary = json.loads(args.summary.read_text(encoding="utf-8"))
    measured: dict[str, float] = defaultdict(float)
    for test in summary.get("tests", []):
        if test.get("status") == "SKIPPED":
            continue
        measured[test["class"]] += FIXED_SECONDS_PER_TEST + float(test.get("durationMs", 0)) / 1000
    if not measured:
        raise SystemExit(f"No test durations found in {args.summary}")

    weights = {}
    if args.keep_missing and args.weights_file.exists():
        weights.update(json.loads(args.weights_file.read_text(encoding="utf-8")))
    weights.update({name: round(seconds) for name, seconds in measured.items()})
    args.weights_file.write_text(json.dumps(dict(sorted(weights.items())), indent=2) + "\n", encoding="utf-8")
    print(f"{len(measured)} classes measured, {len(weights)} weights written to {args.weights_file}")


if __name__ == "__main__":
    main()
