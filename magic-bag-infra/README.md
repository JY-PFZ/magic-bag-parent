# 启动kafka-kraft docker
### 1. 第一次启动KRaft 需要为每个 broker 分配一个唯一 ID

[//]: # (生成一个随机 UUID 作为 node.id)
NODE_ID=$(docker run --rm bitnami/kafka:latest kafka-storage.sh random-uuid)

echo "NODE_ID: $NODE_ID"
echo "NODE_ID: $mpUAVSfbSj6VuKvgOt0HqQ"

[//]: # (生成 cluster.id（16位十六进制）)
CLUSTER_ID=$(openssl rand -hex 16)
echo "CLUSTER_ID: $CLUSTER_ID"

### 2. KRaft 模式第一次启动前，必须格式化存储目录

[//]: # (执行格式化（替换 YOUR_CLUSTER_id）)
docker-compose run --no-deps kafka kafka-storage.sh format \
--config /opt/bitnami/kafka/config/kraft/server.properties \
--cluster-id ccb20a350bfc69e31333764c381a2bec \
--ignore-formatted

### 3. 启动Kafka
docker-compose up -d


docker run -d \
--name kafka-kraft \
-p 9092:9092 \
-e KAFKA_ENABLE_KRAFT=yes \
-e KAFKA_CFG_NODE_ID=1 \
-e KAFKA_CFG_PROCESS_ROLES=broker,controller \
-e KAFKA_CFG_CONTROLLER_QUORUM_VOTERS=1@localhost:9093 \  # 强制使用localhost
-e KAFKA_CFG_LISTENERS=PLAINTEXT://:9092,CONTROLLER://:9093 \
-e KAFKA_CFG_LISTENER_SECURITY_PROTOCOL_MAP=PLAINTEXT:PLAINTEXT,CONTROLLER:PLAINTEXT \
-e KAFKA_CFG_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 \
-e KAFKA_CFG_CONTROLLER_LISTENER_NAMES=CONTROLLER \
-e KAFKA_CLUSTER_ID=41pFNxwDT3a-zUAdkJU4cA \  # 复用之前生成的集群ID（避免重复生成）
-v kafka-kraft-data:/bitnami/kafka/data \
bitnami/kafka:latest


docker exec -it kafka-kraft /bin/bash
[//]: # (查看所有topics)
/opt/bitnami/kafka/bin/kafka-topics.sh \
--list \
--bootstrap-server localhost:9092

[//]: # (删除指定topic)
/opt/bitnami/kafka/bin/kafka-topics.sh \
--delete \
--topic user.registered \
--bootstrap-server localhost:9092