#!/usr/bin/env python3
import argparse
import json
import math
import re
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

DEFAULT_EXCLUDED_CLASSES: set[str] = set()

DEFAULT_CLASS_WEIGHTS = {
    # This test is much longer than a regular class on GHA. Treat it as a full
    # shard so the remaining rules_editor tests can finish in parallel.
    "tests.ui.webstudio.rules_editor.TestSwitchModuleViaBreadcrumbsNavigation": 8,
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


def parse_selected_classes(raw: str) -> list[str]:
    names = [name for name in re.split(r"[\s,;]+", raw or "") if name]
    invalid = [name for name in names if not re.fullmatch(r"[A-Za-z0-9_.]+", name)]
    if invalid:
        raise SystemExit(
            "Test class names may contain only letters, digits, dots and underscores: "
            + ", ".join(invalid)
        )
    return names


def selected_names_for(class_name: str, selected: list[str]) -> list[str]:
    return [
        name
        for name in selected
        if class_name == name or ("." not in name and class_name.endswith("." + name))
    ]


def shard_within_suite(classes: list[str], max_shard_size: int) -> list[list[str]]:
    if not classes:
        return []

    weights = {name: DEFAULT_CLASS_WEIGHTS.get(name, 1) for name in classes}
    if all(weight == 1 for weight in weights.values()):
        shard_count = max(1, math.ceil(len(classes) / max_shard_size))
        unweighted_shards: list[list[str]] = [[] for _ in range(shard_count)]
        for index, name in enumerate(classes):
            unweighted_shards[index % shard_count].append(name)
        return [shard for shard in unweighted_shards if shard]

    shard_count = max(
        1,
        math.ceil(len(classes) / max_shard_size),
        math.ceil(sum(weights.values()) / max_shard_size),
    )
    weighted_shards: list[list[str]] = [[] for _ in range(shard_count)]
    shard_weights = [0] * shard_count
    weighted_classes = sorted(
        enumerate(classes), key=lambda item: (-weights[item[1]], item[0])
    )
    for _, name in weighted_classes:
        candidates = [
            index
            for index, shard in enumerate(weighted_shards)
            if len(shard) < max_shard_size
        ]
        target_index = min(
            candidates,
            key=lambda index: (
                shard_weights[index],
                len(weighted_shards[index]),
                index,
            ),
        )
        weighted_shards[target_index].append(name)
        shard_weights[target_index] += weights[name]
    return [shard for shard in weighted_shards if shard]


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
        help=(
            "Upper bound on the number of test classes per shard within a single "
            "suite; weighted classes can also increase the shard count."
        ),
    )
    parser.add_argument("--suites", nargs="*", default=DEFAULT_PARALLEL_SUITES)
    parser.add_argument(
        "--exclude-classes", nargs="*", default=sorted(DEFAULT_EXCLUDED_CLASSES)
    )
    parser.add_argument(
        "--classes",
        default="",
        help=(
            "Optional selective run: test class names separated by commas, spaces or "
            "semicolons, simple (TestMethodTable) or fully qualified. Only suites holding "
            "at least one of them produce shards; a name found in no suite is an error."
        ),
    )
    args = parser.parse_args()

    excluded = set(args.exclude_classes)
    selected = parse_selected_classes(args.classes)
    matched: set[str] = set()
    include: list[dict[str, object]] = []
    for suite in args.suites:
        studio_kind, ws_kind = SUITE_IMAGE_KINDS[suite]
        classes = read_suite_classes(args.suite_dir, suite, excluded)
        if selected:
            classes = [name for name in classes if selected_names_for(name, selected)]
            for name in classes:
                matched.update(selected_names_for(name, selected))
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

    if selected:
        missing = sorted(set(selected) - matched)
        if missing:
            raise SystemExit(
                "Selected test classes not found in any suite of this workflow "
                f"(check the spelling): {', '.join(missing)}"
            )
        if not include:
            raise SystemExit("No shards left after applying the selected test classes")

    print(json.dumps({"include": include}, separators=(",", ":")))


if __name__ == "__main__":
    main()
