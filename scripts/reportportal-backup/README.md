# Report Portal Backup & Restore Scripts

Simple backup solution for Report Portal that saves:
- ✅ **PostgreSQL database** (test history, defects, defect reasons)
- ✅ **Storage volume** (screenshots, logs, attachments)
- ✅ **Configuration** (docker-compose.yml)

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

## Restore Backup on New Server

### 1. Prepare New Server

```bash
# Install Docker
curl -fsSL https://get.docker.com | sh

# Install Docker Compose
curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" \
    -o /usr/local/bin/docker-compose
chmod +x /usr/local/bin/docker-compose
```

### 2. Copy Backup and Restore Script

```bash
# From local machine
scp backup_20260109_040945.tar.gz root@new-server:/opt/
scp restore_reportportal.sh root@new-server:/opt/
```

### 3. Restore

```bash
ssh root@new-server

chmod +x /opt/restore_reportportal.sh
/opt/restore_reportportal.sh /opt/backup_20260109_040945.tar.gz /opt/reportportal
```

### 4. Verify

```bash
cd /opt/reportportal
docker-compose ps

# Find UI port
docker-compose port ui 8080

# Open in browser: http://new-server-ip:8080
# Default login: default / 1q2w3e
```

---

## Configuration

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

---

## Troubleshooting

### "PostgreSQL container is not running"

```bash
# Check container name
docker ps | grep postgres

# If container has different name, update in script:
POSTGRES_CONTAINER="your-postgres-container-name"
```

### "Permission denied" errors

The script uses Docker to avoid permission issues. If you still get errors:

```bash
# Clean up old backup directories
docker run --rm -v /home/user/report-portal-backups:/backups alpine rm -rf /backups/backup_*
```

### Check backup size

```bash
ls -lh /home/user/report-portal-backups/

# Typical sizes:
# - Small project: 50-100 MB
# - Medium project: 100-300 MB
# - Large project: 300+ MB
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

### PostgreSQL Database (166 MB uncompressed)
- Test launches and history
- **Defect types and defect reasons** ⭐ (most important!)
- Test items and results
- Users and permissions
- Projects and settings
- Filters and dashboards

### Storage Volume (286 MB uncompressed)
- Screenshots
- Logs
- Videos (if enabled)
- Other attachments

### Configuration Files
- `docker-compose.yml`
- Docker volumes info
- Container information

### Compressed Archive Size
**~150-200 MB** (compression ratio ~3:1)

---

## Important Notes

### ✅ Safe Operations
- Backup script **ONLY READS** data, never modifies
- Uses `pg_dump` (same as SELECT query)
- Storage mounted as **read-only** (`:ro` flag)
- No containers are stopped or restarted

### ⚠️ Performance Impact
- Disk I/O during backup (copying files)
- CPU usage for compression
- **Recommendation:** Run backups during off-peak hours (night/weekends)

### 🔒 Security
- Backup contains sensitive data (passwords, test data)
- Store backups in secure location
- Consider encrypting backups:
  ```bash
  gpg --symmetric --cipher-algo AES256 backup.tar.gz
  ```

---

## Example: Complete Workflow

```bash
# 1. Initial setup on server
scp backup_reportportal.sh dminchuk@10.23.172.185:~/
ssh dminchuk@10.23.172.185
nano ~/backup_reportportal.sh  # Edit POSTGRES_PASSWORD
chmod +x ~/backup_reportportal.sh

# 2. Create first backup
~/backup_reportportal.sh

# 3. Download to local machine
cd ~/Projects/java-taf-template/scripts/reportportal-backup
REMOTE_SERVER="10.23.172.185" \
REMOTE_USER="dminchuk" \
REMOTE_BACKUP_DIR="/home/dminchuk/report-portal-backups" \
./download_backups.sh latest

# 4. Set up automatic backups
ssh dminchuk@10.23.172.185
crontab -e
# Add: 0 2 * * * /home/dminchuk/backup_reportportal.sh >> /var/log/rp-backup.log 2>&1

# 5. Set up automatic downloads (optional)
# On local machine crontab:
# 0 3 * * * REMOTE_SERVER="10.23.172.185" REMOTE_USER="dminchuk" REMOTE_BACKUP_DIR="/home/dminchuk/report-portal-backups" /path/to/download_backups.sh latest >> ~/rp-download.log 2>&1
```

---

## Support

If you encounter issues:

1. Check logs: `/var/log/reportportal-backup.log`
2. Verify Docker is running: `docker ps`
3. Check disk space: `df -h`
4. Review backup manifest: `cat backup_*/BACKUP_MANIFEST.txt`

---

**🎉 Your Report Portal data is now protected!**
