# AWS环境变量配置说明

## 环境变量设置

在部署Magic Bag应用时，需要设置以下环境变量：

```bash
# AWS访问密钥
export AWS_ACCESS_KEY_ID=your-actual-access-key-id
export AWS_SECRET_ACCESS_KEY=your-actual-secret-access-key

# 其他可能需要的环境变量
export AWS_REGION=ap-southeast-1
export AWS_S3_BUCKET_NAME=magic-bag-oss
```

## Docker部署示例

```bash
# 使用Docker运行时的环境变量设置
docker run -e AWS_ACCESS_KEY_ID=your-access-key \
           -e AWS_SECRET_ACCESS_KEY=your-secret-key \
           -e AWS_REGION=ap-southeast-1 \
           -e AWS_S3_BUCKET_NAME=magic-bag-oss \
           your-image-name
```

## Docker Compose示例

```yaml
version: '3.8'
services:
  magic-bag-order:
    image: your-image-name
    environment:
      - AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID}
      - AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY}
      - AWS_REGION=ap-southeast-1
      - AWS_S3_BUCKET_NAME=magic-bag-oss
```

## 安全注意事项

1. **永远不要在代码中硬编码敏感信息**
2. **使用环境变量或密钥管理服务**
3. **定期轮换AWS密钥**
4. **限制AWS密钥的权限范围**
5. **使用IAM角色而不是访问密钥（如果可能）**

## 本地开发环境

在本地开发时，可以创建 `.env` 文件（不要提交到Git）：

```bash
# .env 文件（不要提交到Git）
AWS_ACCESS_KEY_ID=your-dev-access-key
AWS_SECRET_ACCESS_KEY=your-dev-secret-key
```

然后使用工具如 `dotenv` 来加载环境变量。
