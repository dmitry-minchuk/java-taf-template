#!/usr/bin/env python3
import argparse
import json
import math
import xml.etree.ElementTree as ET
from pathlib import Path


DEFAULT_PARALLEL_SUITES = [
    "studio_issues",
    "studio_smoke",
    "studio_rules_editor",
    "studio_git",
    "studio_sso",
    "service_smoke",
    "studio_open_api",
]

DEFAULT_EXCLUDED_CLASSES = {
    "tests.ui.webstudio.git.TestGitSwitchToDeletedBranch",
    "tests.ui.webstudio.git.TestGitSwitchDeletedBranchPreset",
}

# Mirrors the (studioImageName, wsImageName) pairs from Jenkinsfile's
# functionalJobList. The "kind" picks which docker image template the
# workflow resolves at run time: "webstudio" -> webstudio:VERSION,
# "ws" -> ws:VERSION-all.
SUITE_IMAGE_KINDS: dict[str, tuple[str, str]] = {
    "studio_issues": ("webstudio", "webstudio"),
    "studio_smoke": ("webstudio", "ws"),
    "studio_rules_editor": ("webstudio", "webstudio"),
    "studio_git": ("webstudio", "webstudio"),
    "studio_sso": ("webstudio", "webstudio"),
    "service_smoke": ("ws", "ws"),
    "studio_open_api": ("webstudio", "webstudio"),
}


def read_suite_classes(suite_dir: Path, suite: str, excluded: set[str]) -> list[str]:
    root = ET.parse(suite_dir / f"{suite}.xml").getroot()
    classes: list[str] = []
    seen: set[str] = set()
    for class_node in root.findall(".//class"):
        name = class_node.attrib["name"]
        if name in excluded or name in seen:
            continue
        seen.add(name)
        classes.append(name)
    return classes


def shard_within_suite(classes: list[str], max_shard_size: int) -> list[list[str]]:
    if not classes:
        return []
    shard_count = max(1, math.ceil(len(classes) / max_shard_size))
    shards: list[list[str]] = [[] for _ in range(shard_count)]
    for index, name in enumerate(classes):
        shards[index % shard_count].append(name)
    return [shard for shard in shards if shard]


def main() -> None:
    parser = argparse.ArgumentParser(
        description="Build a GitHub Actions matrix sharded within each TestNG suite."
    )
    parser.add_argument(
        "--suite-dir", default="src/test/resources/testng_suites", type=Path
    )
    parser.add_argument(
        "--max-shard-size",
        default=8,
        type=int,
        help="Upper bound on the number of test classes per shard within a single suite.",
    )
    parser.add_argument("--suites", nargs="*", default=DEFAULT_PARALLEL_SUITES)
    parser.add_argument(
        "--exclude-classes", nargs="*", default=sorted(DEFAULT_EXCLUDED_CLASSES)
    )
    args = parser.parse_args()

    excluded = set(args.exclude_classes)
    include: list[dict[str, object]] = []
    for suite in args.suites:
        studio_kind, ws_kind = SUITE_IMAGE_KINDS[suite]
        classes = read_suite_classes(args.suite_dir, suite, excluded)
        shards = shard_within_suite(classes, args.max_shard_size)
        for shard_index, shard in enumerate(shards, start=1):
            display = f"{suite}-{shard_index:02d}" if len(shards) > 1 else suite
            include.append(
                {
                    "suite": suite,
                    "shard": shard_index,
                    "classes": ",".join(shard),
                    "display": display,
                    "studio_image_kind": studio_kind,
                    "ws_image_kind": ws_kind,
                }
            )

    print(json.dumps({"include": include}, separators=(",", ":")))


if __name__ == "__main__":
    main()
