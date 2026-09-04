#!/usr/bin/env python3
import argparse
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from testngsuite import write_suite  # noqa: E402


def main() -> None:
    parser = argparse.ArgumentParser(description="Generate a TestNG suite for a comma-separated class list.")
    parser.add_argument("--classes", required=True, help="Comma-separated fully qualified test class names.")
    parser.add_argument("--output", required=True, type=Path)
    parser.add_argument("--suite-name", default="GitHub Actions Dynamic Shard")
    args = parser.parse_args()

    class_names = [value.strip() for value in args.classes.split(",") if value.strip()]
    write_suite(class_names, args.output, args.suite_name)


if __name__ == "__main__":
    main()
