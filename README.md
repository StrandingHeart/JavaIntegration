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

docker pull mysql:5.7

docker run -d \
-p 3306:3306 \
--name mysql \
-v /Users/admin/Documents/self/docker/mysql/conf:/etc/mysql \
-v /Users/admin/Documents/self/docker/mysql/data:/var/lib/mysql/ \
-e MYSQL_ROOT_PASSWORD=123 \
--restart always  \
mysql:5.7

mysql.cnf文件要写这些，开启binlog

[mysqld]
log-bin=mysql-bin # 开启 binlog
binlog-format=ROW # 选择 ROW 模式
server_id=1 # 配置 MySQL replaction 需要定义，不要和 canal 的 slaveId 重复

#给canal用户授权
GRANT SELECT, REPLICATION SLAVE, REPLICATION CLIENT ON *.* TO 'canal'@'%' IDENTIFIED BY 'canal' ;



docker pull canal/canal-server:v1.1.5

创建instance.properties文件下述内容

## mysql serverId
# 目前最新的服务端版本已不需要配置serverId参数
canal.instance.mysql.slaveId = 1234  
#position info，需要改成自己的数据库信息
canal.instance.master.address = 172.16.27.143:3306  ---------------------写本机的ip
canal.instance.master.journal.name = 
canal.instance.master.position = 
canal.instance.master.timestamp = 
#canal.instance.standby.address = 
#canal.instance.standby.journal.name =
#canal.instance.standby.position = 
#canal.instance.standby.timestamp = 
#username/password，需要改成自己的数据库信息
canal.instance.dbUsername = canal  
canal.instance.dbPassword = canal
canal.instance.defaultDatabaseName =
canal.instance.connectionCharset = UTF-8
# table regex
# binlog解析的过滤规则，采用正则表达式
canal.instance.filter.regex = .*\\..*

docker run --name=canal -p 11111:11111 -d -v /Users/admin/Documents/self/docker/canal/conf/instance.properties:/home/admin/canal-server/conf/example/instance.properties canal/canal-server:v1.1.5




