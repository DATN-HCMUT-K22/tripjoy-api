#!/bin/bash
# ================================================================
# aws-setup.sh — EC2 Initial Setup Script
#
# CHẠY SCRIPT NÀY MỘT LẦN sau khi tạo EC2 instance mới
#
# CÁCH DÙNG:
#   # 1. SSH vào EC2
#   ssh -i your-key.pem ec2-user@<EC2-PUBLIC-IP>
#
#   # 2. Upload script
#   scp -i your-key.pem scripts/aws-setup.sh ec2-user@<EC2-IP>:~/
#
#   # 3. Chạy script
#   chmod +x ~/aws-setup.sh && ~/aws-setup.sh
#
# HOẶC chạy inline qua SSH:
#   ssh -i key.pem ec2-user@<IP> "bash -s" < scripts/aws-setup.sh
#
# OS: Amazon Linux 2023 (amzn2023)
# ================================================================

set -e  # Dừng ngay nếu có lỗi

# Color output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

log_info()    { echo -e "${BLUE}[INFO]${NC} $1"; }
log_success() { echo -e "${GREEN}[✓]${NC} $1"; }
log_warning() { echo -e "${YELLOW}[!]${NC} $1"; }
log_error()   { echo -e "${RED}[✗]${NC} $1"; exit 1; }

echo ""
echo "================================================================"
echo "  TripJoy API — EC2 Initial Setup"
echo "  OS: Amazon Linux 2023"
echo "================================================================"
echo ""

# ----------------------------------------------------------------
# Step 1: Update system packages
# ----------------------------------------------------------------
log_info "Updating system packages..."
sudo dnf update -y -q
log_success "System updated"

# ----------------------------------------------------------------
# Step 2: Install Docker
#
# Amazon Linux 2023 dùng 'dnf' thay vì 'yum'
# Docker được install từ Amazon Linux extras
# ----------------------------------------------------------------
log_info "Installing Docker..."
if command -v docker &> /dev/null; then
    log_warning "Docker already installed: $(docker --version)"
else
    sudo dnf install -y docker
    sudo systemctl start docker
    sudo systemctl enable docker
    # Thêm ec2-user vào docker group để không cần sudo mỗi lần
    sudo usermod -aG docker ec2-user
    log_success "Docker installed: $(docker --version)"
    log_warning "You need to LOGOUT and LOGIN again for docker group to take effect"
fi

# ----------------------------------------------------------------
# Step 3: Install Docker Compose (v2 — standalone plugin)
#
# Docker Compose v2 được install như Docker CLI plugin
# Dùng: docker compose (không có dấu gạch ngang)
# ----------------------------------------------------------------
log_info "Installing Docker Compose v2..."
if docker compose version &> /dev/null; then
    log_warning "Docker Compose already installed: $(docker compose version)"
else
    DOCKER_COMPOSE_VERSION="v2.27.0"
    ARCH=$(uname -m)
    
    sudo mkdir -p /usr/local/lib/docker/cli-plugins
    sudo curl -SL "https://github.com/docker/compose/releases/download/${DOCKER_COMPOSE_VERSION}/docker-compose-linux-${ARCH}" \
        -o /usr/local/lib/docker/cli-plugins/docker-compose
    sudo chmod +x /usr/local/lib/docker/cli-plugins/docker-compose
    
    log_success "Docker Compose installed: $(docker compose version)"
fi

# ----------------------------------------------------------------
# Step 4: Install useful tools
# ----------------------------------------------------------------
log_info "Installing utilities..."
sudo dnf install -y git curl wget htop nano jq python3
log_success "Utilities installed"

# ----------------------------------------------------------------
# Step 5: Configure firewall (nếu dùng firewalld)
# Trên Amazon Linux 2023, security groups của AWS đã xử lý firewall
# Nhưng nếu cần mở thêm local ports:
# ----------------------------------------------------------------
log_info "Checking firewall..."
if systemctl is-active --quiet firewalld; then
    sudo firewall-cmd --permanent --add-port=8080/tcp  # Spring Boot API
    sudo firewall-cmd --permanent --add-port=3000/tcp  # Grafana
    sudo firewall-cmd --permanent --add-port=9090/tcp  # Prometheus
    sudo firewall-cmd --reload
    log_success "Firewall configured"
else
    log_info "firewalld not active — using AWS Security Groups"
fi

# ----------------------------------------------------------------
# Step 6: Setup deploy directories
# ----------------------------------------------------------------
log_info "Setting up directories..."
mkdir -p ~/tripjoy-prod
mkdir -p ~/tripjoy-staging
mkdir -p ~/backups
log_success "Directories created"

# ----------------------------------------------------------------
# Step 7: Setup log rotation cho Docker
# Ngăn Docker logs chiếm hết disk space
# ----------------------------------------------------------------
log_info "Configuring Docker log rotation..."
sudo mkdir -p /etc/docker
cat <<EOF | sudo tee /etc/docker/daemon.json
{
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "10m",
    "max-file": "3"
  }
}
EOF
sudo systemctl restart docker
log_success "Docker log rotation configured (max 10MB × 3 files per container)"

# ----------------------------------------------------------------
# Step 8: Verify installation
# ----------------------------------------------------------------
echo ""
echo "================================================================"
echo "  Setup Complete! Verification:"
echo "================================================================"
docker --version
docker compose version
git --version
python3 --version
echo ""
echo "  Next Steps:"
echo "  1. LOGOUT and LOGIN again: exit && ssh -i key.pem ec2-user@<IP>"
echo "  2. Add GitHub Secrets (see docs/DEVOPS_SETUP_GUIDE.md)"
echo "  3. Push to staging branch to trigger first deployment"
echo ""
echo "  Ports to open in AWS Security Group:"
echo "  - 22    (SSH)"
echo "  - 8080  (Spring Boot API)"
echo "  - 3000  (Grafana)"
echo "  - 9090  (Prometheus) — optional, consider restricting to your IP"
echo "================================================================"
