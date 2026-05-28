#!/usr/bin/env python3
import argparse
import json
import xml.etree.ElementTree as ET
from pathlib import Path


DEFAULT_PARALLEL_SUITES = [
    "studio_issues",
    "studio_smoke",
    "studio_rules_editor",
    "studio_sso",
    "service_smoke",
    "studio_open_api",
]


def read_classes(suite_dir: Path, suites: list[str]) -> list[dict[str, str]]:
    tests = []
    seen = set()
    for suite in suites:
        suite_file = suite_dir / f"{suite}.xml"
        root = ET.parse(suite_file).getroot()
        for class_node in root.findall(".//class"):
            class_name = class_node.attrib["name"]
            if class_name in seen:
                continue
            seen.add(class_name)
            tests.append({
                "suite": suite,
                "class_name": class_name,
                "short_name": class_name.rsplit(".", 1)[-1],
            })
    return tests


def shard_tests(tests: list[dict[str, str]], shards: int) -> list[list[dict[str, str]]]:
    result = [[] for _ in range(shards)]
    for index, test in enumerate(tests):
        result[index % shards].append(test)
    return [shard for shard in result if shard]


def main() -> None:
    parser = argparse.ArgumentParser(description="Build a GitHub Actions matrix for TestNG class shards.")
    parser.add_argument("--suite-dir", default="src/test/resources/testng_suites", type=Path)
    parser.add_argument("--shards", default=20, type=int)
    parser.add_argument("--suites", nargs="*", default=DEFAULT_PARALLEL_SUITES)
    args = parser.parse_args()

    tests = read_classes(args.suite_dir, args.suites)
    shards = shard_tests(tests, args.shards)
    include = []
    for index, shard in enumerate(shards, start=1):
        include.append({
            "shard": index,
            "classes": ",".join(test["class_name"] for test in shard),
            "display": f"shard-{index:02d}",
        })

    print(json.dumps({"include": include}, separators=(",", ":")))


if __name__ == "__main__":
    main()
