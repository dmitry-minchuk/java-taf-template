#!/usr/bin/env bash
set -euo pipefail

usage() {
  cat <<'USAGE'
Usage:
  import-rp-export.sh --export-dir DIR [--export-dir DIR ...] --rp-endpoint URL --rp-project PROJECT --rp-api-key KEY [--rp-launch NAME] [--dry-run]

The script compiles the ReportPortal importer, builds a runtime classpath from Maven
dependencies, then replays one or more previously generated target/rp-export directories into
ReportPortal. It is intended to run from Jenkins inside the VPN.
USAGE
}

EXPORT_DIRS=()
RP_ENDPOINT=""
RP_PROJECT=""
RP_API_KEY=""
RP_LAUNCH=""
DRY_RUN=false

while [[ $# -gt 0 ]]; do
  case "$1" in
    --export-dir)
      EXPORT_DIRS+=("$2")
      shift 2
      ;;
    --rp-endpoint)
      RP_ENDPOINT="$2"
      shift 2
      ;;
    --rp-project)
      RP_PROJECT="$2"
      shift 2
      ;;
    --rp-api-key)
      RP_API_KEY="$2"
      shift 2
      ;;
    --rp-launch)
      RP_LAUNCH="$2"
      shift 2
      ;;
    --dry-run)
      DRY_RUN=true
      shift
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      echo "Unknown option: $1" >&2
      usage >&2
      exit 2
      ;;
  esac
done

if [[ "${#EXPORT_DIRS[@]}" -eq 0 ]]; then
  usage >&2
  exit 2
fi

if [[ "$DRY_RUN" != true && ( -z "$RP_ENDPOINT" || -z "$RP_PROJECT" || -z "$RP_API_KEY" ) ]]; then
  usage >&2
  exit 2
fi

for export_dir in "${EXPORT_DIRS[@]}"; do
  if [[ ! -f "$export_dir/manifest.json" ]]; then
    echo "ReportPortal export manifest not found: $export_dir/manifest.json" >&2
    exit 1
  fi
done

mvn -q -DskipTests compile dependency:build-classpath -Dmdep.outputFile=target/reportportal-importer.classpath

CLASSPATH="target/classes:$(cat target/reportportal-importer.classpath)"
ARGS=(
  --rp-endpoint "$RP_ENDPOINT"
  --rp-project "$RP_PROJECT"
  --rp-api-key "$RP_API_KEY"
)

for export_dir in "${EXPORT_DIRS[@]}"; do
  ARGS+=(--export-dir "$export_dir")
done

if [[ -n "$RP_LAUNCH" ]]; then
  ARGS+=(--rp-launch "$RP_LAUNCH")
fi

if [[ "$DRY_RUN" == true ]]; then
  ARGS+=(--dry-run)
fi

java -cp "$CLASSPATH" helpers.reportportal.ReportPortalExportImporter "${ARGS[@]}"
