#!/bin/bash
set -e  # 遇到错误立即退出

# 配置参数
COMPOSE_FILE="docker-compose.yml"
KAFKA_CONTAINER="kafka-kraft"
TEST_TOPIC="user-register-topic"  # 与你的业务主题匹配

# 1. 清理旧容器（若存在）
echo "=== 清理旧容器和无效资源 ==="
docker-compose -f $COMPOSE_FILE down --remove-orphans

# 2. 启动 Kafka 服务
echo -e "\n=== 启动 Kafka Kraft 模式 ==="
docker-compose -f $COMPOSE_FILE up -d

# 3. 等待 Kafka 启动完成（最多等待 60 秒）
echo -e "\n=== 等待 Kafka 启动（最多 60 秒） ==="
MAX_RETRIES=12
RETRY_DELAY=5
RETRY_COUNT=0

until docker exec $KAFKA_CONTAINER /opt/bitnami/kafka/bin/kafka-topics.sh --list --bootstrap-server localhost:9092 > /dev/null 2>&1; do
  if [ $RETRY_COUNT -ge $MAX_RETRIES ]; then
    echo "ERROR: Kafka 启动超时，请检查日志"
    docker logs $KAFKA_CONTAINER
    exit 1
  fi
  echo "Kafka 尚未启动，等待 $RETRY_DELAY 秒...（$((RETRY_COUNT+1))/$MAX_RETRIES）"
  sleep $RETRY_DELAY
  RETRY_COUNT=$((RETRY_COUNT + 1))
done

echo "=== Kafka 启动成功 ==="

# 4. 创建业务主题（如用户注册主题）
echo -e "\n=== 创建业务主题: $TEST_TOPIC ==="
docker exec $KAFKA_CONTAINER /opt/bitnami/kafka/bin/kafka-topics.sh \
  --create \
  --topic $TEST_TOPIC \
  --bootstrap-server localhost:9092 \
  --partitions 3 \
  --replication-factor 1 \
  --if-not-exists

# 5. 验证主题创建结果
echo -e "\n=== 验证主题列表 ==="
docker exec $KAFKA_CONTAINER /opt/bitnami/kafka/bin/kafka-topics.sh \
  --list \
  --bootstrap-server localhost:9092

echo -e "\n=== 操作完成 ==="
echo "Kafka 地址: localhost:9092"
echo "已创建主题: $TEST_TOPIC"
