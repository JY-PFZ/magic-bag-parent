#!/bin/bash

set -e  # 遇错即停

echo "🚀 开始安装 Docker..."

# 1. 卸载旧版本（如果存在）
echo "🧹 正在卸载旧版本 Docker（如有）..."
sudo apt remove docker docker-engine docker.io containerd runc -y || true

# 2. 安装必要依赖
echo "📦 安装依赖工具..."
sudo apt update
sudo apt install -y ca-certificates curl gnupg lsb-release

# 3. 添加 Docker 官方 GPG 密钥
echo "🔑 添加 Docker 官方 GPG 密钥..."
sudo mkdir -p /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg

# 4. 设置 stable 版本仓库
echo "📡 添加 Docker APT 仓库..."
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
  $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

# 5. 安装 Docker 引擎
echo "📥 安装 Docker Engine..."
sudo apt update
sudo apt install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

# 6. 启动 Docker 服务并设置开机自启
echo "⚙️ 启动 Docker 服务..."
sudo systemctl enable docker
sudo systemctl start docker

# 7. 将当前用户加入 docker 组（避免每次用 sudo）
echo "👥 将当前用户 $USER 加入 docker 用户组..."
sudo usermod -aG docker $USER

# 8. 验证安装
echo "✅ 验证 Docker 安装..."
docker --version
sudo docker run --rm hello-world

echo ""
echo "🎉 Docker 已成功安装！"
echo "💡 注意：为使用户组生效，请重新登录终端或执行：newgrp docker"