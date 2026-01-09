#!/bin/bash

set -e  # Exit on error
set -u  # Exit on undefined variable

REPORTPORTAL_DIR="${REPORTPORTAL_DIR:-/home/dminchuk/reportportal}"

BACKUP_DIR="${1:-/home/dminchuk/report-portal-backups}"

BACKUP_RETENTION_DAYS="${BACKUP_RETENTION_DAYS:-30}"

COMPOSE_PROJECT_NAME="${COMPOSE_PROJECT_NAME:-reportportal}"

POSTGRES_CONTAINER="postgres"
MINIO_CONTAINER="${COMPOSE_PROJECT_NAME}-minio-1"
ELASTICSEARCH_CONTAINER="${COMPOSE_PROJECT_NAME}-elasticsearch-1"

POSTGRES_USER="${POSTGRES_USER:-rpuser}"
POSTGRES_DB="${POSTGRES_DB:-reportportal}"
POSTGRES_PASSWORD="${POSTGRES_PASSWORD:-rppass}"

MINIO_ROOT_USER="${MINIO_ROOT_USER:-minio}"
MINIO_ROOT_PASSWORD="${MINIO_ROOT_PASSWORD:-minio123}"
MINIO_BUCKET="${MINIO_BUCKET:-rp-bucket}"

COMPRESS_BACKUP=true
BACKUP_ELASTICSEARCH=false

log_info() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] INFO: $*"
}

log_error() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] ERROR: $*" >&2
}

log_success() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] SUCCESS: $*"
}

check_prerequisites() {
    log_info "Checking prerequisites..."

    if ! command -v docker &> /dev/null; then
        log_error "Docker is not installed"
        exit 1
    fi

    if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
        log_error "Docker Compose is not installed"
        exit 1
    fi

    if [ ! -d "$REPORTPORTAL_DIR" ]; then
        log_error "Report Portal directory does not exist: $REPORTPORTAL_DIR"
        log_error "Please set REPORTPORTAL_DIR environment variable"
        exit 1
    fi

    if ! docker ps --format '{{.Names}}' | grep -q "$POSTGRES_CONTAINER"; then
        log_error "PostgreSQL container is not running: $POSTGRES_CONTAINER"
        exit 1
    fi

    log_success "Prerequisites check passed"
}

create_backup_directory() {
    local timestamp=$(date '+%Y%m%d_%H%M%S')
    CURRENT_BACKUP_DIR="$BACKUP_DIR/backup_$timestamp"

    log_info "Creating backup directory: $CURRENT_BACKUP_DIR"
    mkdir -p "$CURRENT_BACKUP_DIR"/{postgres,storage,minio,config,elasticsearch}
}

backup_postgres() {
    log_info "Starting PostgreSQL backup..."

    local dump_file="$CURRENT_BACKUP_DIR/postgres/reportportal_db.sql"

    docker exec -e PGPASSWORD="$POSTGRES_PASSWORD" "$POSTGRES_CONTAINER" pg_dump \
        -U "$POSTGRES_USER" \
        -d "$POSTGRES_DB" \
        --verbose \
        --no-owner \
        --no-acl \
        > "$dump_file"

    local dump_size=$(du -h "$dump_file" | cut -f1)
    log_success "PostgreSQL backup completed: $dump_file ($dump_size)"

    log_info "Collecting database statistics..."
    docker exec -e PGPASSWORD="$POSTGRES_PASSWORD" "$POSTGRES_CONTAINER" psql \
        -U "$POSTGRES_USER" \
        -d "$POSTGRES_DB" \
        -c "SELECT
                (SELECT COUNT(*) FROM launch) as launches,
                (SELECT COUNT(*) FROM test_item) as test_items,
                (SELECT COUNT(*) FROM attachment) as attachments,
                (SELECT COUNT(*) FROM issue) as issues;" \
        > "$CURRENT_BACKUP_DIR/postgres/db_stats.txt"

    log_success "Database statistics saved"
}

backup_storage() {
    log_info "Starting storage backup (screenshots, logs, attachments)..."

    # Find storage volume
    local storage_volume=$(docker volume inspect storage --format '{{ .Mountpoint }}' 2>/dev/null)

    if [ -z "$storage_volume" ]; then
        log_warning "Storage volume not found, trying alternative method..."

        # Try to find volume by listing all volumes
        storage_volume=$(docker volume ls --format '{{.Name}}' | grep -E '^storage$|reportportal.*storage' | head -1)

        if [ -n "$storage_volume" ]; then
            storage_volume=$(docker volume inspect "$storage_volume" --format '{{ .Mountpoint }}')
        fi
    fi

    if [ -z "$storage_volume" ]; then
        log_warning "Could not find storage volume, skipping storage backup"
        log_info "Available volumes:"
        docker volume ls
        return
    fi

    local storage_data_dir="$CURRENT_BACKUP_DIR/storage/data"
    mkdir -p "$storage_data_dir"

    log_info "Copying storage data from: $storage_volume"
    log_info "This may take a while depending on the size of attachments..."

    # Copy storage data using docker to avoid permission issues
    # This method works without sudo by using a temporary container
    docker run --rm \
        -v storage:/source:ro \
        -v "$storage_data_dir":/backup \
        alpine \
        sh -c 'cd /source && tar cf - . | (cd /backup && tar xf -)'

    local storage_size=$(du -sh "$storage_data_dir" | cut -f1)
    log_success "Storage backup completed: $storage_data_dir ($storage_size)"

    # Save storage info
    echo "Storage volume path: $storage_volume" > "$CURRENT_BACKUP_DIR/storage/storage_info.txt"
    echo "Backup size: $storage_size" >> "$CURRENT_BACKUP_DIR/storage/storage_info.txt"
    echo "File count: $(find "$storage_data_dir" -type f | wc -l)" >> "$CURRENT_BACKUP_DIR/storage/storage_info.txt"
}

backup_minio() {
    log_info "Checking for MinIO backup..."

    if ! docker ps --format '{{.Names}}' | grep -q "$MINIO_CONTAINER"; then
        log_info "MinIO container is not running, skipping MinIO backup"
        return
    fi

    local minio_data_dir="$CURRENT_BACKUP_DIR/minio/data"
    mkdir -p "$minio_data_dir"

    # Get MinIO data volume
    local minio_volume=$(docker inspect "$MINIO_CONTAINER" \
        | grep -A 5 '"Mounts"' \
        | grep '"Source"' \
        | head -n 1 \
        | cut -d'"' -f4)

    if [ -z "$minio_volume" ]; then
        log_error "Could not find MinIO data volume"
        return
    fi

    log_info "Copying MinIO data from: $minio_volume"

    # Copy MinIO data (preserving permissions)
    rsync -av --progress "$minio_volume/" "$minio_data_dir/"

    local minio_size=$(du -sh "$minio_data_dir" | cut -f1)
    log_success "MinIO backup completed: $minio_data_dir ($minio_size)"

    # Save MinIO bucket list
    docker exec "$MINIO_CONTAINER" mc alias set local http://localhost:9000 "$MINIO_ROOT_USER" "$MINIO_ROOT_PASSWORD" 2>/dev/null || true
    docker exec "$MINIO_CONTAINER" mc ls local > "$CURRENT_BACKUP_DIR/minio/buckets.txt" 2>/dev/null || true
}

backup_elasticsearch() {
    if [ "$BACKUP_ELASTICSEARCH" = false ]; then
        log_info "Elasticsearch backup is disabled, skipping..."
        return
    fi

    log_info "Starting Elasticsearch backup..."

    # Check if Elasticsearch container is running
    if ! docker ps --format '{{.Names}}' | grep -q "$ELASTICSEARCH_CONTAINER"; then
        log_info "Elasticsearch container is not running, skipping..."
        return
    fi

    local es_data_dir="$CURRENT_BACKUP_DIR/elasticsearch/data"
    mkdir -p "$es_data_dir"

    # Get Elasticsearch data volume
    local es_volume=$(docker inspect "$ELASTICSEARCH_CONTAINER" \
        | grep -A 5 '"Mounts"' \
        | grep '"Source"' \
        | head -n 1 \
        | cut -d'"' -f4)

    if [ -z "$es_volume" ]; then
        log_error "Could not find Elasticsearch data volume"
        return
    fi

    log_info "Copying Elasticsearch data from: $es_volume"
    rsync -av --progress "$es_volume/" "$es_data_dir/"

    local es_size=$(du -sh "$es_data_dir" | cut -f1)
    log_success "Elasticsearch backup completed: $es_data_dir ($es_size)"
}

backup_config() {
    log_info "Backing up configuration files..."

    # Copy docker-compose.yml
    if [ -f "$REPORTPORTAL_DIR/docker-compose.yml" ]; then
        cp "$REPORTPORTAL_DIR/docker-compose.yml" "$CURRENT_BACKUP_DIR/config/"
        log_success "Copied docker-compose.yml"
    fi

    # Copy .env file if exists
    if [ -f "$REPORTPORTAL_DIR/.env" ]; then
        cp "$REPORTPORTAL_DIR/.env" "$CURRENT_BACKUP_DIR/config/"
        log_success "Copied .env file"
    fi

    # Save Docker volumes info
    docker volume ls > "$CURRENT_BACKUP_DIR/config/docker_volumes.txt"

    # Save container info
    docker ps -a --filter "name=$COMPOSE_PROJECT_NAME" > "$CURRENT_BACKUP_DIR/config/containers.txt"
}

create_backup_manifest() {
    log_info "Creating backup manifest..."

    cat > "$CURRENT_BACKUP_DIR/BACKUP_MANIFEST.txt" << EOF
Report Portal Backup Manifest
Generated: $(date '+%Y-%m-%d %H:%M:%S')
================================================================================

Backup Location: $CURRENT_BACKUP_DIR
Report Portal Directory: $REPORTPORTAL_DIR
Compose Project Name: $COMPOSE_PROJECT_NAME

Components Backed Up:
- PostgreSQL Database: $([ -f "$CURRENT_BACKUP_DIR/postgres/reportportal_db.sql" ] && echo "YES" || echo "NO")
- Storage (Screenshots/Logs): $([ -d "$CURRENT_BACKUP_DIR/storage/data" ] && echo "YES" || echo "NO")
- MinIO Storage: $([ -d "$CURRENT_BACKUP_DIR/minio/data" ] && echo "YES" || echo "NO")
- Elasticsearch: $([ -d "$CURRENT_BACKUP_DIR/elasticsearch/data" ] && echo "YES" || echo "NO")
- Configuration: $([ -f "$CURRENT_BACKUP_DIR/config/docker-compose.yml" ] && echo "YES" || echo "NO")

File Sizes:
$(du -sh "$CURRENT_BACKUP_DIR"/* 2>/dev/null || echo "N/A")

Database Statistics:
$(cat "$CURRENT_BACKUP_DIR/postgres/db_stats.txt" 2>/dev/null || echo "N/A")

================================================================================
To restore this backup, use the restore_reportportal.sh script with this backup directory.
EOF

    log_success "Backup manifest created"
}

compress_backup() {
    if [ "$COMPRESS_BACKUP" = false ]; then
        log_info "Compression is disabled, skipping..."
        return
    fi

    log_info "Compressing backup..."

    local backup_name=$(basename "$CURRENT_BACKUP_DIR")
    local archive_path="$BACKUP_DIR/${backup_name}.tar.gz"

    cd "$BACKUP_DIR"
    tar -czf "$archive_path" "$backup_name"

    local archive_size=$(du -h "$archive_path" | cut -f1)
    log_success "Backup compressed: $archive_path ($archive_size)"

    # Remove uncompressed directory
    log_info "Removing uncompressed backup directory..."
    rm -rf "$CURRENT_BACKUP_DIR"

    CURRENT_BACKUP_PATH="$archive_path"
}

cleanup_old_backups() {
    if [ "$BACKUP_RETENTION_DAYS" -eq 0 ]; then
        log_info "Backup retention is disabled, keeping all backups"
        return
    fi

    log_info "Cleaning up backups older than $BACKUP_RETENTION_DAYS days..."

    find "$BACKUP_DIR" -name "backup_*.tar.gz" -mtime +$BACKUP_RETENTION_DAYS -delete
    find "$BACKUP_DIR" -name "backup_*" -type d -mtime +$BACKUP_RETENTION_DAYS -exec rm -rf {} +

    log_success "Old backups cleaned up"
}

print_summary() {
    echo ""
    echo "================================================================================"
    log_success "BACKUP COMPLETED SUCCESSFULLY!"
    echo "================================================================================"
    echo ""
    echo "Backup Location:"
    echo "  ${CURRENT_BACKUP_PATH:-$CURRENT_BACKUP_DIR}"
    echo ""
    echo "Backup Size:"
    if [ -n "${CURRENT_BACKUP_PATH:-}" ]; then
        du -h "$CURRENT_BACKUP_PATH"
    else
        du -sh "$CURRENT_BACKUP_DIR"
    fi
    echo ""
    echo "To restore this backup on a new server:"
    echo "  1. Copy the backup to the new server"
    echo "  2. Extract if compressed: tar -xzf backup_*.tar.gz"
    echo "  3. Run: ./restore_reportportal.sh /path/to/backup_directory"
    echo ""
    echo "================================================================================"
}

# ============================================================================
# MAIN EXECUTION
# ============================================================================

main() {
    log_info "Starting Report Portal backup process..."
    log_info "Report Portal directory: $REPORTPORTAL_DIR"
    log_info "Backup directory: $BACKUP_DIR"

    check_prerequisites
    create_backup_directory

    backup_postgres
    backup_storage
    backup_minio
    backup_elasticsearch
    backup_config

    create_backup_manifest
    compress_backup
    cleanup_old_backups

    print_summary
}

# Run main function
main
