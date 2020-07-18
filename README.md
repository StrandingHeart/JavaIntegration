# JavaIntegration
This is a springboot integration middleware project

# Introduction

The git flow workflow is used as the development process of the project, but the master branch is only used as the base branch, with springboot, redis, mysql, mybatis, and tkmapper.

For the integration of middleware, the meaning of the branch name represents the integrated middleware. For example: feature/springboot-kafka represents springboot integrated kafka and wrote some demos.

Joining this project you will learn how to use various middleware in springboot. The middleware expected to be integrated are Redis, MySQL, Mybatis, MongoDB, Postgresql, Oauth, JWT, Kafka, Elasticsearch, Rabbit MQ, Websocket, Slf4j, Netty, Flink. Will expand in the future according to demand.


# Run&Environment

use  docker  

docker pull redis:6.0.3

docker run --name redis -d -e bind=0.0.0.0  -p 6379:6379 redis:6.0.3  redis-server --requirepass "12345678"

docker pull mysql:8.0

docker run --name mysql -d -e MYSQL_ROOT_PASSWORD=12345678 -p 3306:3306 mysql:8.0



