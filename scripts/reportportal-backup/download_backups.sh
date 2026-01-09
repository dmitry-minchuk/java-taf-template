#!/bin/bash

set -e  # Exit on error
set -u  # Exit on undefined variable

REMOTE_SERVER="10.23.172.185"
REMOTE_USER="dminchuk"
REMOTE_BACKUP_DIR="/home/dminchuk/report-portal-backups"

LOCAL_BACKUP_DIR="/Users/dmitryminchuk/Documents/ReportPortalBackups"

RECENT_DAYS="${RECENT_DAYS:-7}"

LOCAL_RETENTION_DAYS="${LOCAL_RETENTION_DAYS:-90}"

SSH_PORT="${SSH_PORT:-22}"

SSH_KEY="${SSH_KEY:-}"

VERIFY_INTEGRITY=true

SHOW_PROGRESS=true

if [[ -t 1 ]]; then
    RED='\033[0;31m'
    GREEN='\033[0;32m'
    YELLOW='\033[1;33m'
    BLUE='\033[0;34m'
    NC='\033[0m' # No Color
else
    RED=''
    GREEN=''
    YELLOW=''
    BLUE=''
    NC=''
fi


log_info() {
    echo -e "${BLUE}[$(date '+%Y-%m-%d %H:%M:%S')]${NC} INFO: $*"
}

log_error() {
    echo -e "${RED}[$(date '+%Y-%m-%d %H:%M:%S')]${NC} ERROR: $*" >&2
}

log_success() {
    echo -e "${GREEN}[$(date '+%Y-%m-%d %H:%M:%S')]${NC} SUCCESS: $*"
}

log_warning() {
    echo -e "${YELLOW}[$(date '+%Y-%m-%d %H:%M:%S')]${NC} WARNING: $*"
}

show_usage() {
    cat << EOF
Usage: $0 [mode]

Modes:
  latest    - Download only the latest backup (default)
  all       - Download all backups from server
  recent    - Download backups from last $RECENT_DAYS days
  sync      - Sync entire backup directory (requires rsync)

Configuration (via environment variables):
  REMOTE_SERVER         - Remote server hostname/IP (default: your-reportportal-server)
  REMOTE_USER           - SSH user (default: root)
  REMOTE_BACKUP_DIR     - Remote backup directory (default: /opt/reportportal-backups)
  LOCAL_BACKUP_DIR      - Local backup directory (default: ~/reportportal-backups)
  RECENT_DAYS           - Days to look back for 'recent' mode (default: 7)
  LOCAL_RETENTION_DAYS  - Delete local backups older than N days (default: 90, 0=keep all)

Examples:
  # Download latest backup
  $0 latest

  # Download backups from last 14 days
  RECENT_DAYS=14 $0 recent

  # Download to custom directory
  LOCAL_BACKUP_DIR=/mnt/backups $0 all

  # Use custom SSH settings
  REMOTE_SERVER=192.168.1.100 REMOTE_USER=admin SSH_PORT=2222 $0 latest

EOF
    exit 1
}

check_prerequisites() {
    log_info "Checking prerequisites..."

    # Check if ssh is available
    if ! command -v ssh &> /dev/null; then
        log_error "SSH is not installed"
        exit 1
    fi

    # Check if scp is available
    if ! command -v scp &> /dev/null; then
        log_error "SCP is not installed"
        exit 1
    fi

    # Check if server is configured
    if [ "$REMOTE_SERVER" = "your-reportportal-server" ]; then
        log_error "Please configure REMOTE_SERVER before running this script"
        log_info "Example: export REMOTE_SERVER=192.168.1.100"
        show_usage
    fi

    # Create local backup directory
    mkdir -p "$LOCAL_BACKUP_DIR"

    log_success "Prerequisites check passed"
}

build_ssh_command() {
    local ssh_cmd="ssh"

    if [ -n "$SSH_KEY" ]; then
        ssh_cmd="$ssh_cmd -i $SSH_KEY"
    fi

    if [ "$SSH_PORT" != "22" ]; then
        ssh_cmd="$ssh_cmd -p $SSH_PORT"
    fi

    echo "$ssh_cmd ${REMOTE_USER}@${REMOTE_SERVER}"
}

build_scp_command() {
    local scp_cmd="scp"

    if [ "$SHOW_PROGRESS" = true ]; then
        scp_cmd="$scp_cmd -v"
    else
        scp_cmd="$scp_cmd -q"
    fi

    if [ -n "$SSH_KEY" ]; then
        scp_cmd="$scp_cmd -i $SSH_KEY"
    fi

    if [ "$SSH_PORT" != "22" ]; then
        scp_cmd="$scp_cmd -P $SSH_PORT"
    fi

    echo "$scp_cmd"
}

test_ssh_connection() {
    log_info "Testing SSH connection to ${REMOTE_USER}@${REMOTE_SERVER}..."

    local ssh_cmd=$(build_ssh_command)

    if ! $ssh_cmd "echo 'Connection successful'" &>/dev/null; then
        log_error "Cannot connect to remote server: ${REMOTE_USER}@${REMOTE_SERVER}"
        log_info "Please check:"
        log_info "  1. Server is reachable: ping $REMOTE_SERVER"
        log_info "  2. SSH service is running on server"
        log_info "  3. Credentials are correct"
        log_info "  4. SSH key is configured (recommended): ssh-copy-id ${REMOTE_USER}@${REMOTE_SERVER}"
        exit 1
    fi

    log_success "SSH connection successful"
}

list_remote_backups() {
    local mode="$1"
    local ssh_cmd=$(build_ssh_command)

    log_info "Listing remote backups (mode: $mode)..."

    case "$mode" in
        latest)
            # Get only the latest backup
            $ssh_cmd "ls -t ${REMOTE_BACKUP_DIR}/backup_*.tar.gz 2>/dev/null | head -1" || echo ""
            ;;
        all)
            # Get all backups
            $ssh_cmd "ls -t ${REMOTE_BACKUP_DIR}/backup_*.tar.gz 2>/dev/null" || echo ""
            ;;
        recent)
            # Get backups from last N days
            $ssh_cmd "find ${REMOTE_BACKUP_DIR} -name 'backup_*.tar.gz' -mtime -${RECENT_DAYS} -type f | sort -r" || echo ""
            ;;
        *)
            log_error "Unknown mode: $mode"
            show_usage
            ;;
    esac
}

download_backup() {
    local remote_file="$1"
    local filename=$(basename "$remote_file")
    local local_file="$LOCAL_BACKUP_DIR/$filename"

    # Check if file already exists locally
    if [ -f "$local_file" ]; then
        log_warning "File already exists locally: $filename"

        # Compare sizes
        local ssh_cmd=$(build_ssh_command)
        local remote_size=$($ssh_cmd "stat -f%z '$remote_file' 2>/dev/null || stat -c%s '$remote_file' 2>/dev/null")
        local local_size=$(stat -f%z "$local_file" 2>/dev/null || stat -c%s "$local_file" 2>/dev/null)

        if [ "$remote_size" = "$local_size" ]; then
            log_info "Local file size matches remote, skipping download"
            return 0
        else
            log_warning "Local file size differs from remote, re-downloading..."
        fi
    fi

    log_info "Downloading: $filename"

    local scp_cmd=$(build_scp_command)

    # Download file
    if $scp_cmd "${REMOTE_USER}@${REMOTE_SERVER}:${remote_file}" "$local_file"; then
        log_success "Downloaded: $filename"

        # Verify integrity if enabled
        if [ "$VERIFY_INTEGRITY" = true ]; then
            verify_download "$remote_file" "$local_file"
        fi

        # Show file size
        local size=$(du -h "$local_file" | cut -f1)
        log_info "File size: $size"

        return 0
    else
        log_error "Failed to download: $filename"
        return 1
    fi
}

verify_download() {
    local remote_file="$1"
    local local_file="$2"
    local ssh_cmd=$(build_ssh_command)

    log_info "Verifying download integrity..."

    # Compare file sizes
    local remote_size=$($ssh_cmd "stat -f%z '$remote_file' 2>/dev/null || stat -c%s '$remote_file' 2>/dev/null")
    local local_size=$(stat -f%z "$local_file" 2>/dev/null || stat -c%s "$local_file" 2>/dev/null)

    if [ "$remote_size" != "$local_size" ]; then
        log_error "Size mismatch! Remote: $remote_size bytes, Local: $local_size bytes"
        return 1
    fi

    # Calculate checksums (MD5)
    log_info "Calculating checksums (this may take a while for large files)..."

    local remote_md5=$($ssh_cmd "md5sum '$remote_file' 2>/dev/null | cut -d' ' -f1 || md5 -q '$remote_file' 2>/dev/null")
    local local_md5=$(md5sum "$local_file" 2>/dev/null | cut -d' ' -f1 || md5 -q "$local_file" 2>/dev/null)

    if [ "$remote_md5" = "$local_md5" ]; then
        log_success "Integrity check passed (MD5: $local_md5)"
        return 0
    else
        log_error "MD5 mismatch! Remote: $remote_md5, Local: $local_md5"
        return 1
    fi
}

sync_backups_rsync() {
    log_info "Synchronizing backups using rsync..."

    # Check if rsync is available
    if ! command -v rsync &> /dev/null; then
        log_error "rsync is not installed. Please install it or use another download mode."
        log_info "Install on Mac: brew install rsync"
        log_info "Install on Linux: apt-get install rsync  or  yum install rsync"
        exit 1
    fi

    local rsync_opts="-avz --progress"

    if [ -n "$SSH_KEY" ]; then
        rsync_opts="$rsync_opts -e 'ssh -i $SSH_KEY -p $SSH_PORT'"
    elif [ "$SSH_PORT" != "22" ]; then
        rsync_opts="$rsync_opts -e 'ssh -p $SSH_PORT'"
    fi

    # Sync backups
    log_info "Running: rsync $rsync_opts ${REMOTE_USER}@${REMOTE_SERVER}:${REMOTE_BACKUP_DIR}/ ${LOCAL_BACKUP_DIR}/"

    if rsync $rsync_opts "${REMOTE_USER}@${REMOTE_SERVER}:${REMOTE_BACKUP_DIR}/" "${LOCAL_BACKUP_DIR}/"; then
        log_success "Synchronization completed"
    else
        log_error "Synchronization failed"
        exit 1
    fi
}

cleanup_old_local_backups() {
    if [ "$LOCAL_RETENTION_DAYS" -eq 0 ]; then
        log_info "Local backup retention is disabled, keeping all backups"
        return
    fi

    log_info "Cleaning up local backups older than $LOCAL_RETENTION_DAYS days..."

    local count=$(find "$LOCAL_BACKUP_DIR" -name "backup_*.tar.gz" -mtime +$LOCAL_RETENTION_DAYS -type f | wc -l)

    if [ "$count" -gt 0 ]; then
        find "$LOCAL_BACKUP_DIR" -name "backup_*.tar.gz" -mtime +$LOCAL_RETENTION_DAYS -type f -delete
        log_success "Deleted $count old backup(s)"
    else
        log_info "No old backups to clean up"
    fi
}

print_summary() {
    echo ""
    echo "================================================================================"
    log_success "DOWNLOAD COMPLETED SUCCESSFULLY!"
    echo "================================================================================"
    echo ""
    echo "Local backup directory: $LOCAL_BACKUP_DIR"
    echo ""
    echo "Downloaded backups:"
    ls -lh "$LOCAL_BACKUP_DIR"/backup_*.tar.gz 2>/dev/null | tail -5 || echo "  (no backups found)"
    echo ""
    echo "Total backups: $(find "$LOCAL_BACKUP_DIR" -name 'backup_*.tar.gz' -type f | wc -l)"
    echo "Total size: $(du -sh "$LOCAL_BACKUP_DIR" 2>/dev/null | cut -f1)"
    echo ""
    echo "To restore a backup:"
    echo "  1. Copy to server: scp backup_*.tar.gz ${REMOTE_USER}@new-server:/opt/"
    echo "  2. Run restore script on server"
    echo ""
    echo "================================================================================"
}

# ============================================================================
# MAIN EXECUTION
# ============================================================================

main() {
    local mode="${1:-latest}"

    log_info "Starting Report Portal backup download..."
    log_info "Mode: $mode"
    log_info "Remote: ${REMOTE_USER}@${REMOTE_SERVER}:${REMOTE_BACKUP_DIR}"
    log_info "Local: $LOCAL_BACKUP_DIR"

    check_prerequisites
    test_ssh_connection

    case "$mode" in
        sync)
            sync_backups_rsync
            ;;
        latest|all|recent)
            # Get list of backups to download
            local backups=$(list_remote_backups "$mode")

            if [ -z "$backups" ]; then
                log_warning "No backups found on remote server"
                exit 0
            fi

            local total=$(echo "$backups" | wc -l)
            local current=0

            log_info "Found $total backup(s) to download"

            # Download each backup
            while IFS= read -r backup; do
                current=$((current + 1))
                log_info "Downloading backup $current of $total"
                download_backup "$backup" || log_warning "Skipping failed download"
            done <<< "$backups"
            ;;
        help|--help|-h)
            show_usage
            ;;
        *)
            log_error "Unknown mode: $mode"
            show_usage
            ;;
    esac

    cleanup_old_local_backups
    print_summary
}

# Run main function
main "$@"
