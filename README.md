# Magic Bag Microservice

## 配置nacos和kafka

服务启动需要nacos和kafka，我已经配置好远程连接

也可以本地启动，见docker-compose.yml文件。本地启动记得改IP



## 使用usercontext上下文

直接引用usercontextholder

所有经过gateway转发过来的请求，框架会自动拦截并保存用户上下文。

对于异步场景和定时任务需要通过userclient调用user微服务获取用户信息