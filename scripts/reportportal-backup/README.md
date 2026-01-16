# Report Portal Backup & Restore Scripts

Simple backup solution for Report Portal that saves:
- **PostgreSQL database** (test history, defects, defect reasons)
- **Storage volume** (screenshots, logs, attachments)
- **Configuration** (docker-compose.yml)

Compatible with **ReportPortal 5.15.0+** (OpenSearch, Docker Compose profiles)

---

## Quick Start

### 1. Copy Script to Server

```bash
# From your local machine
scp backup_reportportal.sh user@your-server:/home/user/
```

### 2. Edit Configuration on Server

```bash
ssh user@your-server
nano ~/backup_reportportal.sh
```

**Find and change these lines (around line 14-20):**

```bash
POSTGRES_CONTAINER="postgres"           # Your PostgreSQL container name
POSTGRES_USER="rpuser"                  # Database user
POSTGRES_PASSWORD="rppass"              # Database password
```

**How to find your password:**
```bash
cat /path/to/docker-compose.yml | grep POSTGRES_PASSWORD
# or
docker exec postgres env | grep POSTGRES_PASSWORD
```

### 3. Run Backup

```bash
chmod +x ~/backup_reportportal.sh
~/backup_reportportal.sh
```

**Done!** Backup will be created in `/home/user/report-portal-backups/backup_YYYYMMDD_HHMMSS.tar.gz`

---

## Download Backup to Local Machine

### Option 1: Download Script (Recommended)

```bash
# On your local machine
cd /path/to/scripts/reportportal-backup

REMOTE_SERVER="10.23.172.185" \
REMOTE_USER="dminchuk" \
REMOTE_BACKUP_DIR="/home/dminchuk/report-portal-backups" \
./download_backups.sh latest
```

Backup will be saved to `~/reportportal-backups/`

### Option 2: Manual SCP

```bash
scp user@server:/home/user/report-portal-backups/backup_*.tar.gz ~/reportportal-backups/
```

---

## Restore Backup

### Restore to Existing Installation (Recommended)

If you already have Report Portal running with docker-compose:

```bash
# 1. Copy restore script to your Report Portal directory
cp restore_reportportal.sh /path/to/reportportal/

# 2. Run restore (script will automatically stop containers before restore)
./restore_reportportal.sh /path/to/backup.tar.gz /path/to/reportportal
```

**Example:**
```bash
./restore_reportportal.sh \
  ~/reportportal-backups/backup_20260109_041422.tar.gz \
  ~/Projects/report-portal/reportportal
```

**What happens:**
1. Extracts backup archive
2. **Keeps your existing docker-compose.yml** (does NOT overwrite)
3. Stops all containers (`docker-compose down`)
4. Starts PostgreSQL
5. Restores database
6. Restores storage volume (screenshots, logs, attachments)
7. Starts all services
8. Verifies API health

### Restore to New Server

```bash
# 1. Install Docker
curl -fsSL https://get.docker.com | sh

# 2. Install Docker Compose
curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" \
    -o /usr/local/bin/docker-compose
chmod +x /usr/local/bin/docker-compose

# 3. Copy backup and restore script
scp backup_20260109_041422.tar.gz root@new-server:/opt/
scp restore_reportportal.sh root@new-server:/opt/

# 4. Restore (will download docker-compose.yml if not present)
ssh root@new-server
chmod +x /opt/restore_reportportal.sh
/opt/restore_reportportal.sh /opt/backup_20260109_041422.tar.gz /opt/reportportal

# 5. Access UI
# URL: http://new-server-ip:8080
# superadmin: superadmin / erebus
# default user: default / 1q2w3e
```

### Restore Script Behavior

| Scenario | Behavior |
|----------|----------|
| docker-compose.yml exists | **Keeps existing file** (does NOT overwrite) |
| docker-compose.yml missing, in backup | Uses file from backup |
| docker-compose.yml missing everywhere | Downloads latest from GitHub |

**Why keep existing docker-compose.yml?**
- Backup may contain old image versions that no longer exist
- Your current configuration may have custom settings
- Prevents "image not found" errors during restore

---

## ReportPortal 5.15.0+ Specifics

### Docker Compose Profiles

ReportPortal 5.15.0+ uses profiles. The restore script automatically uses:

```bash
docker-compose --profile core --profile infra <command>
```

### OpenSearch vs Elasticsearch

ReportPortal 5.15.0+ uses **OpenSearch** instead of Elasticsearch. The restore script supports both:

- Looks for `opensearch/data` directory first
- Falls back to `elasticsearch/data` (legacy backups)
- Auto-detects which service is configured in docker-compose.yml

### Default Credentials

| User | Password | Notes |
|------|----------|-------|
| superadmin | erebus | Full admin access |
| default | 1q2w3e | Default project user |

---

## Automatic Daily Backups

Set up automatic backups using cron:

```bash
ssh user@your-server

# Edit crontab
crontab -e

# Add this line (backup every day at 2:00 AM)
0 2 * * * /home/user/backup_reportportal.sh >> /var/log/reportportal-backup.log 2>&1
```

**Check backup logs:**
```bash
tail -f /var/log/reportportal-backup.log
```

---

## Configuration

### Backup Script Variables

Edit these variables in `backup_reportportal.sh`:

| Variable | Description | Default |
|----------|-------------|---------|
| `REPORTPORTAL_DIR` | Directory with docker-compose.yml | `/home/user/reportportal` |
| `BACKUP_DIR` | Backup storage directory | `/home/user/report-portal-backups` |
| `POSTGRES_CONTAINER` | PostgreSQL container name | `postgres` |
| `POSTGRES_USER` | Database user | `rpuser` |
| `POSTGRES_PASSWORD` | Database password | `rppass` |
| `POSTGRES_DB` | Database name | `reportportal` |
| `BACKUP_RETENTION_DAYS` | Delete backups older than N days | `30` |
| `COMPRESS_BACKUP` | Compress to tar.gz | `true` |

### Restore Script Environment Variables

You can override PostgreSQL credentials via environment:

```bash
POSTGRES_USER=myuser \
POSTGRES_PASSWORD=mypassword \
POSTGRES_DB=mydb \
./restore_reportportal.sh /path/to/backup.tar.gz /path/to/reportportal
```

---

## Download Script Options

The `download_backups.sh` script supports multiple modes:

```bash
# Download latest backup only (default)
./download_backups.sh latest

# Download all backups
./download_backups.sh all

# Download backups from last 7 days
./download_backups.sh recent

# Sync entire directory (requires rsync)
./download_backups.sh sync
```

**Configuration via environment variables:**

```bash
REMOTE_SERVER="10.23.172.185" \
REMOTE_USER="dminchuk" \
REMOTE_BACKUP_DIR="/home/dminchuk/report-portal-backups" \
LOCAL_BACKUP_DIR="$HOME/my-backups" \
./download_backups.sh latest
```

---

## What Gets Backed Up

### PostgreSQL Database
- Test launches and history
- **Defect types and defect reasons** (most important!)
- Test items and results
- Users and permissions
- Projects and settings
- Filters and dashboards

### Storage Volume
- Screenshots
- Logs
- Videos (if enabled)
- Other attachments

### Configuration Files
- `docker-compose.yml`
- `.env` (if exists)

### Typical Sizes

| Component | Size |
|-----------|------|
| Database dump | 50-200 MB |
| Storage | 100-500 MB |
| **Compressed total** | **100-300 MB** |

---

## Troubleshooting

### "PostgreSQL container is not running"

```bash
# Check container name
docker ps | grep postgres

# If container has different name, update in script:
POSTGRES_CONTAINER="your-postgres-container-name"
```

### "Database is being accessed by other users"

The restore script automatically runs `docker-compose down` before restore. If you still see this error:

```bash
# Manually stop all containers
cd /path/to/reportportal
docker-compose --profile core --profile infra down

# Then run restore
./restore_reportportal.sh /path/to/backup.tar.gz /path/to/reportportal
```

### "Image not found" during restore

This happens when backup contains docker-compose.yml with old image versions:

```bash
# Solution 1: Use your existing docker-compose.yml (default behavior)
# The restore script keeps existing docker-compose.yml

# Solution 2: Update images in backup's docker-compose.yml before restore
tar -xzf backup.tar.gz
nano backup_*/config/docker-compose.yml  # Update image versions
./restore_reportportal.sh backup_*/ /path/to/reportportal
```

### "Permission denied" errors

The script uses Docker to avoid permission issues. If you still get errors:

```bash
# Clean up old backup directories
docker run --rm -v /home/user/report-portal-backups:/backups alpine rm -rf /backups/backup_*
```

### Verify backup content

```bash
# Extract and check
tar -xzf backup_20260109_040945.tar.gz
cd backup_20260109_040945

# Check manifest
cat BACKUP_MANIFEST.txt

# Check database size
ls -lh postgres/reportportal_db.sql

# Check storage size
du -sh storage/data/
```

### API not responding after restore

```bash
# Check container status
cd /path/to/reportportal
docker-compose --profile core --profile infra ps

# View logs
docker-compose --profile core --profile infra logs -f api

# Restart all services
docker-compose --profile core --profile infra restart
```

---

## Important Notes

### Safe Operations
- Backup script **ONLY READS** data, never modifies
- Uses `pg_dump` (same as SELECT query)
- Storage mounted as **read-only** (`:ro` flag)
- No containers are stopped during backup

### Restore Operations
- Restore script **STOPS all containers** before restore
- Database is dropped and recreated
- Storage volume is overwritten
- **Always verify data after restore**

### Performance Impact
- Disk I/O during backup/restore
- CPU usage for compression
- **Recommendation:** Run during off-peak hours

### Security
- Backup contains sensitive data (passwords, test data)
- Store backups in secure location
- Consider encrypting backups:
  ```bash
  gpg --symmetric --cipher-algo AES256 backup.tar.gz
  ```

---

## Example: Complete Workflow

```bash
# === ON SERVER ===

# 1. Initial setup
scp backup_reportportal.sh dminchuk@10.23.172.185:~/
ssh dminchuk@10.23.172.185
nano ~/backup_reportportal.sh  # Edit POSTGRES_PASSWORD
chmod +x ~/backup_reportportal.sh

# 2. Create backup
~/backup_reportportal.sh

# 3. Set up automatic backups
crontab -e
# Add: 0 2 * * * /home/dminchuk/backup_reportportal.sh >> /var/log/rp-backup.log 2>&1


# === ON LOCAL MACHINE ===

# 4. Download backup
cd ~/Projects/java-taf-template/scripts/reportportal-backup
REMOTE_SERVER="10.23.172.185" \
REMOTE_USER="dminchuk" \
REMOTE_BACKUP_DIR="/home/dminchuk/report-portal-backups" \
./download_backups.sh latest

# 5. Test restore locally
./restore_reportportal.sh \
  ~/reportportal-backups/backup_20260109_041422.tar.gz \
  ~/Projects/report-portal/reportportal

# 6. Verify at http://localhost:8080
#    Login: superadmin / erebus
```

---

## File Structure

```
scripts/reportportal-backup/
├── README.md                  # This file
├── backup_reportportal.sh     # Creates backups on server
├── restore_reportportal.sh    # Restores backups
└── download_backups.sh        # Downloads backups from server
```

### Backup Archive Structure

```
backup_20260109_041422/
├── BACKUP_MANIFEST.txt        # Backup metadata
├── config/
│   └── docker-compose.yml     # Configuration (may have old images)
├── postgres/
│   ├── reportportal_db.sql    # Database dump
│   └── db_stats.txt           # Statistics
└── storage/
    ├── data/                  # Screenshots, logs, attachments
    └── storage_info.txt       # Storage statistics
```

---

## Support

If you encounter issues:

1. Check logs: `/var/log/reportportal-backup.log`
2. Verify Docker is running: `docker ps`
3. Check disk space: `df -h`
4. Review backup manifest: `cat backup_*/BACKUP_MANIFEST.txt`
5. Check API health: `curl http://localhost:8080/api/health`

---

**Your Report Portal data is now protected!**
