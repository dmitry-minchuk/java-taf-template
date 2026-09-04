#!/usr/bin/env python3
import argparse
import json
import re
import statistics
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from suites import DEFAULT_SUITE_DIR, REGRESSION_SUITES, read_regression_classes  # noqa: E402

DEFAULT_EXCLUDED_CLASSES: set[str] = set()
DEFAULT_SHARDS = 20
DEFAULT_WEIGHTS_FILE = Path(__file__).with_name("test-weights.json")
STUDIO_IMAGE_KIND = "webstudio"
WS_IMAGE_KIND = "ws"


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


def load_weights(weights_file: Path) -> dict[str, float]:
    if not weights_file.exists():
        return {}
    return {name: float(seconds) for name, seconds in json.loads(weights_file.read_text(encoding="utf-8")).items()}


def fallback_weight(classes: list[str], weights: dict[str, float]) -> float:
    known = [weights[name] for name in classes if name in weights]
    return statistics.median(known) if known else 60.0


def balance(classes: list[str], weights: dict[str, float], shard_count: int) -> list[list[str]]:
    if not classes:
        return []
    fallback = fallback_weight(classes, weights)
    shard_count = max(1, min(shard_count, len(classes)))
    shards: list[list[str]] = [[] for _ in range(shard_count)]
    loads = [0.0] * shard_count
    ordered = sorted(classes, key=lambda name: (-weights.get(name, fallback), name))
    for name in ordered:
        target = min(range(shard_count), key=lambda index: (loads[index], len(shards[index]), index))
        shards[target].append(name)
        loads[target] += weights.get(name, fallback)
    return [shard for shard in shards if shard]


def main() -> None:
    parser = argparse.ArgumentParser(
        description=(
            "Build a GitHub Actions matrix: the test classes of every regression suite are pooled "
            "and spread over a fixed number of shards by their measured duration."
        )
    )
    parser.add_argument("--suite-dir", default=DEFAULT_SUITE_DIR, type=Path)
    parser.add_argument("--suites", nargs="*", default=REGRESSION_SUITES)
    parser.add_argument("--exclude-classes", nargs="*", default=sorted(DEFAULT_EXCLUDED_CLASSES))
    parser.add_argument(
        "--shards",
        default=DEFAULT_SHARDS,
        type=int,
        help="Number of shards the pooled classes are balanced over (never more than there are classes).",
    )
    parser.add_argument(
        "--weights-file",
        default=DEFAULT_WEIGHTS_FILE,
        type=Path,
        help="JSON map of fully qualified test class -> measured duration in seconds; unknown classes get the median.",
    )
    parser.add_argument(
        "--classes",
        default="",
        help=(
            "Optional selective run: test class names separated by commas, spaces or "
            "semicolons, simple (TestMethodTable) or fully qualified. A name found in no suite is an error."
        ),
    )
    args = parser.parse_args()

    if args.shards < 1:
        raise SystemExit("--shards must be at least 1")
    suite_of = read_regression_classes(args.suite_dir, args.suites, set(args.exclude_classes))
    classes = list(suite_of)
    selected = parse_selected_classes(args.classes)
    if selected:
        matched: set[str] = set()
        filtered = []
        for name in classes:
            hits = selected_names_for(name, selected)
            if hits:
                filtered.append(name)
                matched.update(hits)
        missing = sorted(set(selected) - matched)
        if missing:
            raise SystemExit(
                "Selected test classes not found in any suite of this workflow "
                f"(check the spelling): {', '.join(missing)}"
            )
        classes = filtered

    weights = load_weights(args.weights_file)
    fallback = fallback_weight(classes, weights)
    shards = balance(classes, weights, args.shards)
    if not shards:
        raise SystemExit("No test classes to run")
    width = max(2, len(str(len(shards))))
    include = []
    for index, shard in enumerate(shards, start=1):
        include.append(
            {
                "shard": index,
                "display": f"shard-{index:0{width}d}",
                "classes": ",".join(shard),
                "suites": ",".join(sorted({suite_of[name] for name in shard})),
                "weight_seconds": round(sum(weights.get(name, fallback) for name in shard)),
                "studio_image_kind": STUDIO_IMAGE_KIND,
                "ws_image_kind": WS_IMAGE_KIND,
            }
        )
    print(json.dumps({"include": include}, separators=(",", ":")))


if __name__ == "__main__":
    main()
