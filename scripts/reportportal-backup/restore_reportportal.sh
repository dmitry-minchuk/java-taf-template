#!/bin/bash

################################################################################
# Report Portal Restore Script
# Compatible with ReportPortal 5.15.0+ (OpenSearch, profiles support)
################################################################################

set -e  # Exit on error
set -u  # Exit on undefined variable

BACKUP_PATH="${1:-}"
REPORTPORTAL_DIR="${2:-/opt/reportportal}"
TEMP_RESTORE_DIR="/tmp/reportportal_restore_$$"

# Docker Compose profiles for ReportPortal 5.15.0+
COMPOSE_PROFILES="--profile core --profile infra"

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

# Wrapper for docker-compose commands with profile support
dc() {
    docker-compose $COMPOSE_PROFILES "$@"
}

# Wrapper for docker-compose commands without profiles (for queries)
dc_no_profile() {
    docker-compose "$@"
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

    # Handle docker-compose.yml
    if [ -f "$REPORTPORTAL_DIR/docker-compose.yml" ]; then
        log_info "Existing docker-compose.yml found, keeping it (not overwriting)"
    elif [ -d "$BACKUP_DIR/config" ] && [ -f "$BACKUP_DIR/config/docker-compose.yml" ]; then
        log_info "Restoring docker-compose.yml from backup..."
        cp -v "$BACKUP_DIR/config/docker-compose.yml" "$REPORTPORTAL_DIR/"
    else
        log_warning "docker-compose.yml not found, downloading the default configuration..."
        curl -LO https://raw.githubusercontent.com/reportportal/reportportal/master/docker-compose.yml
        mv docker-compose.yml "$REPORTPORTAL_DIR/"
    fi

    # Copy .env if exists in backup
    if [ -d "$BACKUP_DIR/config" ]; then
        cp -v "$BACKUP_DIR/config/.env" "$REPORTPORTAL_DIR/" 2>/dev/null || \
            log_info ".env file not found in backup (this is optional)"
    fi

    cd "$REPORTPORTAL_DIR"
}

start_reportportal_containers() {
    log_info "Preparing Report Portal containers..."

    cd "$REPORTPORTAL_DIR"

    # Stop all containers first to free database connections
    log_info "Stopping all containers..."
    dc down 2>/dev/null || true
    sleep 3

    # Start only PostgreSQL first (with profiles)
    log_info "Starting PostgreSQL container..."
    dc up -d postgres

    # Wait for PostgreSQL to be ready
    log_info "Waiting for PostgreSQL to be ready..."
    sleep 10

    local max_attempts=30
    local attempt=1

    local postgres_container=$(dc ps -q postgres)

    while [ $attempt -le $max_attempts ]; do
        if docker exec "$postgres_container" pg_isready -U rpuser &>/dev/null; then
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
    local postgres_container=$(dc ps -q postgres)

    if [ -z "$postgres_container" ]; then
        log_error "PostgreSQL container is not running"
        exit 1
    fi

    # PostgreSQL credentials (from docker-compose defaults)
    local pg_user="${POSTGRES_USER:-rpuser}"
    local pg_pass="${POSTGRES_PASSWORD:-rppass}"
    local pg_db="${POSTGRES_DB:-reportportal}"

    # Drop existing database (if exists) and create new one
    log_info "Preparing database..."
    docker exec -e PGPASSWORD="$pg_pass" "$postgres_container" \
        psql -U "$pg_user" -d postgres -c "DROP DATABASE IF EXISTS $pg_db;" || true
    docker exec -e PGPASSWORD="$pg_pass" "$postgres_container" \
        psql -U "$pg_user" -d postgres -c "CREATE DATABASE $pg_db OWNER $pg_user;"

    # Restore database
    log_info "Restoring database from dump (this may take a while)..."
    docker exec -i -e PGPASSWORD="$pg_pass" "$postgres_container" \
        psql -U "$pg_user" -d "$pg_db" < "$dump_file"

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
    log_info "Checking MinIO storage..."

    local minio_backup_dir="$BACKUP_DIR/minio/data"

    if [ ! -d "$minio_backup_dir" ]; then
        log_info "MinIO backup directory not found, skipping MinIO restore"
        return
    fi

    cd "$REPORTPORTAL_DIR"

    # Check if MinIO is configured
    if ! dc_no_profile config --services 2>/dev/null | grep -q minio; then
        log_info "MinIO is not configured in docker-compose, skipping..."
        return
    fi

    # Stop MinIO container
    log_info "Stopping MinIO container..."
    dc stop minio || true

    # Get MinIO data volume path
    local minio_container=$(dc ps -aq minio)
    if [ -z "$minio_container" ]; then
        log_warning "MinIO container not found"
        return
    fi

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
    dc start minio

    sleep 5

    log_success "MinIO storage restored successfully"
}

restore_opensearch() {
    # Support both opensearch and elasticsearch backup directories
    local os_backup_dir="$BACKUP_DIR/opensearch/data"
    local es_backup_dir="$BACKUP_DIR/elasticsearch/data"
    local backup_dir=""

    if [ -d "$os_backup_dir" ]; then
        backup_dir="$os_backup_dir"
        log_info "Found OpenSearch backup directory"
    elif [ -d "$es_backup_dir" ]; then
        backup_dir="$es_backup_dir"
        log_info "Found Elasticsearch backup directory (legacy)"
    else
        log_info "OpenSearch/Elasticsearch backup not found, skipping..."
        return
    fi

    cd "$REPORTPORTAL_DIR"

    # Check if OpenSearch or Elasticsearch is configured
    local search_service=""
    if dc_no_profile config --services 2>/dev/null | grep -q opensearch; then
        search_service="opensearch"
    elif dc_no_profile config --services 2>/dev/null | grep -q elasticsearch; then
        search_service="elasticsearch"
    else
        log_info "OpenSearch/Elasticsearch is not configured in docker-compose, skipping..."
        return
    fi

    log_info "Restoring $search_service..."

    # Stop search container
    dc stop "$search_service" || true

    # Get search data volume
    local search_container=$(dc ps -aq "$search_service")
    if [ -z "$search_container" ]; then
        log_warning "$search_service container not found"
        return
    fi

    local search_volume=$(docker inspect "$search_container" \
        | grep -A 5 '"Mounts"' \
        | grep '"Source"' \
        | head -n 1 \
        | cut -d'"' -f4)

    if [ -z "$search_volume" ]; then
        log_error "Could not find $search_service data volume"
        return
    fi

    log_info "$search_service data volume: $search_volume"

    # Restore search data
    log_info "Restoring $search_service data..."
    rm -rf "${search_volume:?}"/*
    rsync -av --progress "$backup_dir/" "$search_volume/"

    # Start search service
    dc start "$search_service"

    log_success "$search_service restored successfully"
}

start_all_services() {
    log_info "Starting all Report Portal services..."

    cd "$REPORTPORTAL_DIR"

    # Start all services with profiles
    dc up -d

    log_info "Waiting for all services to be healthy..."
    sleep 30

    log_success "All services started"
}

verify_restore() {
    log_info "Verifying restore..."

    cd "$REPORTPORTAL_DIR"

    # Check running containers (without profile filter for accurate count)
    log_info "Checking container status..."
    dc ps

    # Get Report Portal UI URL via gateway (port 8080)
    local gateway_port=$(dc_no_profile port gateway 8080 2>/dev/null | cut -d':' -f2 || echo "8080")

    if [ -n "$gateway_port" ]; then
        log_success "Report Portal UI should be available at: http://localhost:$gateway_port"
    else
        log_info "Report Portal UI default URL: http://localhost:8080"
    fi

    # Wait and check if API is responding
    log_info "Checking API health..."
    sleep 10

    local max_health_attempts=12
    local health_attempt=1

    while [ $health_attempt -le $max_health_attempts ]; do
        if curl -sf "http://localhost:${gateway_port:-8080}/api/health" &>/dev/null; then
            log_success "API is healthy and responding"
            break
        fi
        log_info "Waiting for API to be ready (attempt $health_attempt/$max_health_attempts)..."
        sleep 10
        health_attempt=$((health_attempt + 1))
    done

    if [ $health_attempt -gt $max_health_attempts ]; then
        log_warning "API health check timed out. Services may still be starting."
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
    echo "     cd $REPORTPORTAL_DIR && docker-compose $COMPOSE_PROFILES ps"
    echo ""
    echo "  2. View logs if needed:"
    echo "     cd $REPORTPORTAL_DIR && docker-compose $COMPOSE_PROFILES logs -f"
    echo ""
    echo "  3. Access Report Portal UI:"
    echo "     URL: http://localhost:8080"
    echo "     Default superadmin: superadmin / erebus"
    echo "     Default user: default / 1q2w3e"
    echo ""
    echo "  4. Verify your data:"
    echo "     - Login and check if your projects are visible"
    echo "     - Check if launch history is present"
    echo "     - Verify dashboards and widgets are restored"
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
    log_info "Using Docker Compose profiles: $COMPOSE_PROFILES"

    check_prerequisites
    extract_backup
    install_reportportal
    start_reportportal_containers

    restore_postgres
    restore_storage
    restore_opensearch

    start_all_services
    verify_restore
    cleanup

    print_summary
}

# Trap to ensure cleanup on exit
trap cleanup EXIT

# Run main function
main
