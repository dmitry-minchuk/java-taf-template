#!/usr/bin/env python3
import argparse
import json
import shutil
from datetime import datetime, timezone
from pathlib import Path


def find_export_dirs(input_root: Path) -> list[Path]:
    return sorted(path.parent for path in input_root.rglob("manifest.json"))


def read_json(path: Path) -> dict[str, object]:
    return json.loads(path.read_text(encoding="utf-8"))


def merge_tests(export_dirs: list[Path], output_dir: Path) -> None:
    tests_output = output_dir / "tests"
    tests_output.mkdir(parents=True, exist_ok=True)

    for export_dir in export_dirs:
        tests_dir = export_dir / "tests"
        if not tests_dir.exists():
            continue

        for source in tests_dir.rglob("*"):
            if source.is_dir():
                continue
            relative = source.relative_to(tests_dir)
            target = tests_output / relative
            target.parent.mkdir(parents=True, exist_ok=True)
            shutil.copy2(source, target)


def write_manifest(
    export_dirs: list[Path], output_dir: Path, launch_name: str | None
) -> None:
    manifests = [read_json(export_dir / "manifest.json") for export_dir in export_dirs]
    primary = manifests[0]
    manifest = dict(primary)
    manifest["createdAt"] = (
        datetime.now(timezone.utc).isoformat().replace("+00:00", "Z")
    )
    manifest["mergedExport"] = True
    manifest["sourceExportCount"] = len(export_dirs)
    manifest["sourceRpLaunches"] = sorted(
        {str(value) for value in (item.get("rpLaunch") for item in manifests) if value}
    )
    manifest["sourceArtifacts"] = [str(export_dir) for export_dir in export_dirs]
    if launch_name:
        manifest["rpLaunch"] = launch_name

    output_dir.mkdir(parents=True, exist_ok=True)
    (output_dir / "manifest.json").write_text(
        json.dumps(manifest, indent=2, sort_keys=False) + "\n",
        encoding="utf-8",
    )


def write_summary(export_dirs: list[Path], output_dir: Path) -> None:
    summary = []
    for export_dir in export_dirs:
        tests_dir = export_dir / "tests"
        test_count = (
            len(list(tests_dir.rglob("result.json"))) if tests_dir.exists() else 0
        )
        summary.append({"exportDir": str(export_dir), "testCount": test_count})

    (output_dir / "merge-summary.json").write_text(
        json.dumps(summary, indent=2) + "\n",
        encoding="utf-8",
    )


def main() -> None:
    parser = argparse.ArgumentParser(
        description="Merge multiple rp-export directories."
    )
    parser.add_argument("--input-root", required=True, type=Path)
    parser.add_argument("--output-dir", required=True, type=Path)
    parser.add_argument("--launch-name")
    args = parser.parse_args()

    export_dirs = find_export_dirs(args.input_root)
    if not export_dirs:
        raise SystemExit(f"No rp-export manifest files found under {args.input_root}")

    if args.output_dir.exists():
        shutil.rmtree(args.output_dir)
    args.output_dir.mkdir(parents=True)

    write_manifest(export_dirs, args.output_dir, args.launch_name)
    merge_tests(export_dirs, args.output_dir)
    write_summary(export_dirs, args.output_dir)

    test_count = len(list((args.output_dir / "tests").rglob("result.json")))
    print(f"Merged {test_count} test result(s) from {len(export_dirs)} export dir(s)")


if __name__ == "__main__":
    main()
