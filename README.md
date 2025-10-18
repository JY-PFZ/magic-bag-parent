# Magic Bag Microservice

## 配置nacos和kafka

服务启动需要nacos和kafka，我已经配置好远程连接

也可以本地启动，见docker-compose.yml文件。本地启动记得改IP



## 使用usercontext上下文

直接引用usercontextholder

所有经过gateway转发过来的请求，框架会自动拦截并保存用户上下文。

对于异步场景和定时任务需要通过userclient调用user微服务获取用户信息

## 微服务介绍
magic-bag-gateway 网关服务
magic-bag-auth 认证服务
magic-bag-user user服务
magic-bag-product 商品服务
magic-bag-order 订单服务
magic-bag-payment 支付服务
magic-bag-cart 购物车服务
magic-bag-common 通用组件库 
magic-bag-kafka-starter kafka轮子

测试某个微服务时，网关服务，认证服务，user服务需要启动。如果需要其他服务，也许启动该服务。
请求接口需要token，所以先调用登陆接口后再测试其他接口。或者在nacos中变更gateway网关配置，添加接口白名单