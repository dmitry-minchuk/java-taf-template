#!/usr/bin/env bash
set -euo pipefail

usage() {
  cat <<'USAGE'
Usage:
  import-rp-export.sh --export-dir DIR --rp-endpoint URL --rp-project PROJECT --rp-api-key KEY [--rp-launch NAME] [--dry-run]

The script compiles the ReportPortal importer, builds a runtime classpath from Maven
dependencies, then replays a previously generated target/rp-export directory into
ReportPortal. It is intended to run from Jenkins inside the VPN.
USAGE
}

EXPORT_DIR=""
RP_ENDPOINT=""
RP_PROJECT=""
RP_API_KEY=""
RP_LAUNCH=""
DRY_RUN=false

while [[ $# -gt 0 ]]; do
  case "$1" in
    --export-dir)
      EXPORT_DIR="$2"
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

if [[ -z "$EXPORT_DIR" ]]; then
  usage >&2
  exit 2
fi

if [[ "$DRY_RUN" != true && ( -z "$RP_ENDPOINT" || -z "$RP_PROJECT" || -z "$RP_API_KEY" ) ]]; then
  usage >&2
  exit 2
fi

if [[ ! -f "$EXPORT_DIR/manifest.json" ]]; then
  echo "ReportPortal export manifest not found: $EXPORT_DIR/manifest.json" >&2
  exit 1
fi

mvn -q -DskipTests compile dependency:build-classpath -Dmdep.outputFile=target/reportportal-importer.classpath

CLASSPATH="target/classes:$(cat target/reportportal-importer.classpath)"
ARGS=(
  --export-dir "$EXPORT_DIR"
  --rp-endpoint "$RP_ENDPOINT"
  --rp-project "$RP_PROJECT"
  --rp-api-key "$RP_API_KEY"
)

if [[ -n "$RP_LAUNCH" ]]; then
  ARGS+=(--rp-launch "$RP_LAUNCH")
fi

if [[ "$DRY_RUN" == true ]]; then
  ARGS+=(--dry-run)
fi

java -cp "$CLASSPATH" helpers.reportportal.ReportPortalExportImporter "${ARGS[@]}"
