#!/bin/bash

# 设置变量
DOMAIN="se"
REPOSITORY="magic-bag"
REGION="ap-southeast-1"
DOMAIN_OWNER="935194211492"

# 获取授权令牌
export CODEARTIFACT_AUTH_TOKEN=`aws codeartifact get-authorization-token \
    --domain $DOMAIN \
    --domain-owner $DOMAIN_OWNER \
    --region $REGION \
    --query authorizationToken --output text`

# 获取仓库端点
REPO_URL=`aws codeartifact get-repository-endpoint \
    --domain $DOMAIN \
    --repository $REPOSITORY \
    --format maven \
    --region $REGION \
    --query repositoryEndpoint --output text`

echo "授权令牌: $CODEARTIFACT_AUTH_TOKEN"
echo "仓库URL: $REPO_URL"

# 创建settings.xml
mkdir -p ~/.m2
cat > ~/.m2/settings.xml << EOF
<settings>
    <servers>
        <server>
            <id>codeartifact</id>
            <username>aws</username>
            <password>\${env.CODEARTIFACT_AUTH_TOKEN}</password>
        </server>
    </servers>
    <profiles>
        <profile>
            <id>default</id>
            <repositories>
                <repository>
                    <id>codeartifact</id>
                    <url>$REPO_URL</url>
                </repository>
            </repositories>
        </profile>
    </profiles>
    <activeProfiles>
        <activeProfile>default</activeProfile>
    </activeProfiles>
</settings>
EOF

echo "Maven配置完成！"

# 回到项目根目录（假设脚本在 script/ 子目录）
cd "$(dirname "$0")/.."

# 确保 pom.xml 存在
if [ ! -f "pom.xml" ]; then
  echo "错误：当前目录没有 pom.xml！请确保脚本放在项目子目录中。"
  exit 1
fi

echo "正在部署到 CodeArtifact..."
mvn deploy
