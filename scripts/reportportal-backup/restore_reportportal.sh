#!/bin/bash

################################################################################
# Report Portal Restore Script
################################################################################

set -e  # Exit on error
set -u  # Exit on undefined variable

BACKUP_PATH="${1:-}"
REPORTPORTAL_DIR="${2:-/opt/reportportal}"
TEMP_RESTORE_DIR="/tmp/reportportal_restore_$$"

log_info() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] INFO: $*"
}

log_error() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] ERROR: $*" >&2
}

log_success() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] SUCCESS: $*"
}

log_warning() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] WARNING: $*"
}

show_usage() {
    cat << EOF
Usage: $0 /path/to/backup_directory [reportportal_install_dir]

Parameters:
  backup_directory        - Path to backup directory or tar.gz archive
  reportportal_install_dir - Directory to install Report Portal (default: /opt/reportportal)

Example:
  $0 /opt/reportportal-backups/backup_20260108_120000.tar.gz
  $0 /opt/reportportal-backups/backup_20260108_120000 /opt/reportportal

EOF
    exit 1
}

check_prerequisites() {
    log_info "Checking prerequisites..."

    # Check if backup path is provided
    if [ -z "$BACKUP_PATH" ]; then
        log_error "Backup path is required"
        show_usage
    fi

    # Check if backup exists
    if [ ! -e "$BACKUP_PATH" ]; then
        log_error "Backup does not exist: $BACKUP_PATH"
        exit 1
    fi

    # Check if docker is installed
    if ! command -v docker &> /dev/null; then
        log_error "Docker is not installed. Please install Docker first."
        exit 1
    fi

    # Check if docker-compose is installed
    if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
        log_error "Docker Compose is not installed. Please install Docker Compose first."
        exit 1
    fi

    log_success "Prerequisites check passed"
}

extract_backup() {
    log_info "Preparing backup for restore..."

    mkdir -p "$TEMP_RESTORE_DIR"

    # Check if backup is compressed
    if [[ "$BACKUP_PATH" == *.tar.gz ]]; then
        log_info "Extracting compressed backup: $BACKUP_PATH"
        tar -xzf "$BACKUP_PATH" -C "$TEMP_RESTORE_DIR"

        # Find the extracted directory
        BACKUP_DIR=$(find "$TEMP_RESTORE_DIR" -mindepth 1 -maxdepth 1 -type d | head -n 1)

        if [ -z "$BACKUP_DIR" ]; then
            log_error "Could not find extracted backup directory"
            exit 1
        fi
    else
        # Backup is a directory
        BACKUP_DIR="$BACKUP_PATH"
    fi

    log_info "Using backup directory: $BACKUP_DIR"

    # Verify backup structure
    if [ ! -f "$BACKUP_DIR/BACKUP_MANIFEST.txt" ]; then
        log_warning "Backup manifest not found. This might not be a valid backup."
    else
        log_info "Backup manifest found:"
        cat "$BACKUP_DIR/BACKUP_MANIFEST.txt"
    fi
}

install_reportportal() {
    log_info "Setting up Report Portal installation directory..."

    mkdir -p "$REPORTPORTAL_DIR"

    # Copy configuration files
    if [ -d "$BACKUP_DIR/config" ]; then
        log_info "Restoring configuration files..."
        cp -v "$BACKUP_DIR/config/docker-compose.yml" "$REPORTPORTAL_DIR/" 2>/dev/null || \
            log_warning "docker-compose.yml not found in backup"
        cp -v "$BACKUP_DIR/config/.env" "$REPORTPORTAL_DIR/" 2>/dev/null || \
            log_info ".env file not found in backup (this is optional)"
    fi

    # If docker-compose.yml is missing, download the default one
    if [ ! -f "$REPORTPORTAL_DIR/docker-compose.yml" ]; then
        log_warning "docker-compose.yml not found, downloading the default configuration..."
        curl -LO https://raw.githubusercontent.com/reportportal/reportportal/master/docker-compose.yml
        mv docker-compose.yml "$REPORTPORTAL_DIR/"
    fi

    cd "$REPORTPORTAL_DIR"
}

start_reportportal_containers() {
    log_info "Starting Report Portal containers..."

    cd "$REPORTPORTAL_DIR"

    # Start only PostgreSQL first
    log_info "Starting PostgreSQL container..."
    docker-compose up -d postgres

    # Wait for PostgreSQL to be ready
    log_info "Waiting for PostgreSQL to be ready..."
    sleep 10

    local max_attempts=30
    local attempt=1

    while [ $attempt -le $max_attempts ]; do
        if docker-compose exec -T postgres pg_isready -U rpuser &>/dev/null; then
            log_success "PostgreSQL is ready"
            break
        fi

        log_info "Waiting for PostgreSQL (attempt $attempt/$max_attempts)..."
        sleep 2
        attempt=$((attempt + 1))
    done

    if [ $attempt -gt $max_attempts ]; then
        log_error "PostgreSQL did not start in time"
        exit 1
    fi
}

restore_postgres() {
    log_info "Restoring PostgreSQL database..."

    local dump_file="$BACKUP_DIR/postgres/reportportal_db.sql"

    if [ ! -f "$dump_file" ]; then
        log_error "PostgreSQL dump file not found: $dump_file"
        exit 1
    fi

    cd "$REPORTPORTAL_DIR"

    # Get PostgreSQL container name
    local postgres_container=$(docker-compose ps -q postgres)

    if [ -z "$postgres_container" ]; then
        log_error "PostgreSQL container is not running"
        exit 1
    fi

    # Drop existing database (if exists) and create new one
    log_info "Preparing database..."
    docker-compose exec -T postgres psql -U rpuser -d postgres -c "DROP DATABASE IF EXISTS reportportal;" || true
    docker-compose exec -T postgres psql -U rpuser -d postgres -c "CREATE DATABASE reportportal OWNER rpuser;"

    # Restore database
    log_info "Restoring database from dump (this may take a while)..."
    docker exec -i "$postgres_container" psql -U rpuser -d reportportal < "$dump_file"

    log_success "PostgreSQL database restored successfully"

    # Display statistics
    if [ -f "$BACKUP_DIR/postgres/db_stats.txt" ]; then
        log_info "Database statistics after restore:"
        cat "$BACKUP_DIR/postgres/db_stats.txt"
    fi
}

restore_storage() {
    log_info "Restoring storage volume (screenshots, logs, attachments)..."

    local storage_backup_dir="$BACKUP_DIR/storage/data"

    if [ ! -d "$storage_backup_dir" ]; then
        log_warning "Storage backup directory not found, skipping storage restore"
        return
    fi

    # Check if storage is empty (nothing to restore)
    if [ -z "$(ls -A "$storage_backup_dir" 2>/dev/null)" ]; then
        log_info "Storage backup is empty, skipping restore"
        return
    fi

    cd "$REPORTPORTAL_DIR"

    # Find or create storage volume
    local storage_volume_name=$(docker volume ls --format '{{.Name}}' | grep -E 'reportportal.*storage|^storage$' | head -1)

    if [ -z "$storage_volume_name" ]; then
        log_info "Storage volume not found, creating reportportal_storage..."
        docker volume create reportportal_storage
        storage_volume_name="reportportal_storage"
    fi

    log_info "Using storage volume: $storage_volume_name"

    # Restore storage data using Docker to avoid permission issues
    log_info "Copying storage data (this may take a while)..."
    docker run --rm \
        -v "$storage_backup_dir":/source:ro \
        -v "$storage_volume_name":/target \
        alpine \
        sh -c 'cd /source && cp -a . /target/'

    # Verify restore
    local restored_size=$(docker run --rm -v "$storage_volume_name":/data alpine du -sh /data 2>/dev/null | cut -f1)
    log_success "Storage restored successfully: $restored_size"

    # Display storage info if available
    if [ -f "$BACKUP_DIR/storage/storage_info.txt" ]; then
        log_info "Storage information:"
        cat "$BACKUP_DIR/storage/storage_info.txt"
    fi
}

restore_minio() {
    log_info "Restoring MinIO storage..."

    local minio_backup_dir="$BACKUP_DIR/minio/data"

    if [ ! -d "$minio_backup_dir" ]; then
        log_warning "MinIO backup directory not found, skipping MinIO restore"
        return
    fi

    cd "$REPORTPORTAL_DIR"

    # Stop MinIO container
    log_info "Stopping MinIO container..."
    docker-compose stop minio

    # Get MinIO data volume path
    local minio_container=$(docker-compose ps -aq minio)
    local minio_volume=$(docker inspect "$minio_container" \
        | grep -A 5 '"Mounts"' \
        | grep '"Source"' \
        | head -n 1 \
        | cut -d'"' -f4)

    if [ -z "$minio_volume" ]; then
        log_error "Could not find MinIO data volume"
        return
    fi

    log_info "MinIO data volume: $minio_volume"

    # Clear existing MinIO data
    log_info "Clearing existing MinIO data..."
    rm -rf "${minio_volume:?}"/*

    # Restore MinIO data
    log_info "Restoring MinIO data (this may take a while)..."
    rsync -av --progress "$minio_backup_dir/" "$minio_volume/"

    # Start MinIO container
    log_info "Starting MinIO container..."
    docker-compose start minio

    sleep 5

    log_success "MinIO storage restored successfully"
}

restore_elasticsearch() {
    local es_backup_dir="$BACKUP_DIR/elasticsearch/data"

    if [ ! -d "$es_backup_dir" ]; then
        log_info "Elasticsearch backup not found, skipping..."
        return
    fi

    log_info "Restoring Elasticsearch..."

    cd "$REPORTPORTAL_DIR"

    # Check if Elasticsearch is in docker-compose
    if ! docker-compose config --services | grep -q elasticsearch; then
        log_info "Elasticsearch is not configured in docker-compose, skipping..."
        return
    fi

    # Stop Elasticsearch container
    docker-compose stop elasticsearch || true

    # Get Elasticsearch data volume
    local es_container=$(docker-compose ps -aq elasticsearch)
    if [ -z "$es_container" ]; then
        log_warning "Elasticsearch container not found"
        return
    fi

    local es_volume=$(docker inspect "$es_container" \
        | grep -A 5 '"Mounts"' \
        | grep '"Source"' \
        | head -n 1 \
        | cut -d'"' -f4)

    if [ -z "$es_volume" ]; then
        log_error "Could not find Elasticsearch data volume"
        return
    fi

    log_info "Elasticsearch data volume: $es_volume"

    # Restore Elasticsearch data
    log_info "Restoring Elasticsearch data..."
    rm -rf "${es_volume:?}"/*
    rsync -av --progress "$es_backup_dir/" "$es_volume/"

    # Start Elasticsearch
    docker-compose start elasticsearch

    log_success "Elasticsearch restored successfully"
}

start_all_services() {
    log_info "Starting all Report Portal services..."

    cd "$REPORTPORTAL_DIR"
    docker-compose up -d

    log_info "Waiting for all services to be healthy..."
    sleep 15

    log_success "All services started"
}

verify_restore() {
    log_info "Verifying restore..."

    cd "$REPORTPORTAL_DIR"

    # Check if all containers are running
    local running_containers=$(docker-compose ps --services --filter "status=running" | wc -l)
    local total_containers=$(docker-compose ps --services | wc -l)

    log_info "Running containers: $running_containers / $total_containers"

    # Get Report Portal UI URL
    local ui_port=$(docker-compose port ui 8080 2>/dev/null | cut -d':' -f2)

    if [ -n "$ui_port" ]; then
        log_success "Report Portal UI should be available at: http://localhost:$ui_port"
        log_info "Default credentials: default / 1q2w3e"
    fi
}

cleanup() {
    log_info "Cleaning up temporary files..."

    if [ -d "$TEMP_RESTORE_DIR" ]; then
        rm -rf "$TEMP_RESTORE_DIR"
    fi

    log_success "Cleanup completed"
}

print_summary() {
    echo ""
    echo "================================================================================"
    log_success "RESTORE COMPLETED SUCCESSFULLY!"
    echo "================================================================================"
    echo ""
    echo "Report Portal Installation Directory: $REPORTPORTAL_DIR"
    echo ""
    echo "Next steps:"
    echo "  1. Check if all containers are running:"
    echo "     cd $REPORTPORTAL_DIR && docker-compose ps"
    echo ""
    echo "  2. View logs if needed:"
    echo "     cd $REPORTPORTAL_DIR && docker-compose logs -f"
    echo ""
    echo "  3. Access Report Portal UI:"
    echo "     Check the port with: docker-compose port ui 8080"
    echo "     Default URL: http://localhost:8080"
    echo "     Default login: default / 1q2w3e"
    echo ""
    echo "  4. Verify your data:"
    echo "     - Login and check if your projects are visible"
    echo "     - Check if launch history is present"
    echo "     - Verify defect types and reasons are restored"
    echo ""
    echo "================================================================================"
}

# ============================================================================
# MAIN EXECUTION
# ============================================================================

main() {
    log_info "Starting Report Portal restore process..."
    log_info "Backup path: $BACKUP_PATH"
    log_info "Installation directory: $REPORTPORTAL_DIR"

    check_prerequisites
    extract_backup
    install_reportportal
    start_reportportal_containers

    restore_postgres
    restore_storage

    start_all_services
    verify_restore
    cleanup

    print_summary
}

# Trap to ensure cleanup on exit
trap cleanup EXIT

# Run main function
main
