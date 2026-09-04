#!/usr/bin/env python3
import argparse
import json
import os
import re
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from testgroups import TEST_SOURCES, ci_test_classes, discover_test_classes  # noqa: E402

CLASS_NAME = re.compile(r"^[A-Za-z_][A-Za-z0-9_.]*$")


def parse_selected_classes(raw: str) -> list[str]:
    names = [token for token in re.split(r"[,\s]+", raw.strip()) if token]
    for name in names:
        if not CLASS_NAME.match(name):
            raise SystemExit(f"Invalid test class name '{name}': only letters, digits, dots and underscores are allowed")
    return names


def resolve_selected(names: list[str], known: dict[str, bool]) -> list[str]:
    resolved: list[str] = []
    unknown: list[str] = []
    for name in names:
        matches = [cls for cls in known if cls == name or cls.endswith("." + name)]
        if not matches:
            unknown.append(name)
            continue
        for match in matches:
            if match not in resolved:
                resolved.append(match)
    if unknown:
        raise SystemExit(f"Test classes not found under {TEST_SOURCES}: {', '.join(unknown)}")
    return resolved


def write_github_outputs(discovery: dict) -> None:
    output_file = os.environ.get("GITHUB_OUTPUT")
    if not output_file:
        return
    with open(output_file, "a", encoding="utf-8") as handle:
        handle.write(f"classes={json.dumps(discovery['classes'])}\n")
        handle.write(f"matrix={json.dumps(discovery['matrix'])}\n")
        handle.write(f"selective={str(discovery['selective']).lower()}\n")


def main() -> None:
    parser = argparse.ArgumentParser(
        description="List the test classes a GitHub Actions run executes: every non-abstract class under src/test/java with "
                    "@Test methods (own or inherited) that is not annotated @LocalOnly, or the explicitly selected classes. "
                    "Writes classes, matrix and selective to GITHUB_OUTPUT when that variable is set."
    )
    parser.add_argument("--classes", default="", help="Comma or space separated class names for a selective run.")
    parser.add_argument("--shards", type=int, default=20, help="Upper bound of parallel shard jobs.")
    parser.add_argument("--sources", type=Path, default=TEST_SOURCES)
    args = parser.parse_args()

    known = discover_test_classes(args.sources)
    selected = parse_selected_classes(args.classes)
    classes = resolve_selected(selected, known) if selected else ci_test_classes(args.sources)
    if not classes:
        raise SystemExit("No test classes to run")
    if args.shards < 1:
        raise SystemExit("--shards must be at least 1")

    shard_count = min(args.shards, len(classes))
    discovery = {
        "classes": classes,
        "localOnly": sorted(name for name, local_only in known.items() if local_only),
        "selective": bool(selected),
        "matrix": {"include": [{"shard": index, "display": f"shard-{index:02d}"} for index in range(1, shard_count + 1)]},
    }
    write_github_outputs(discovery)

    print(f"{len(classes)} test class(es) queued for {shard_count} shard(s)" + (" (selective run)" if selected else ""))
    for name in classes:
        print(f"  {name}")
    if discovery["localOnly"]:
        print("Skipped as @LocalOnly: " + ", ".join(discovery["localOnly"]))


if __name__ == "__main__":
    main()
