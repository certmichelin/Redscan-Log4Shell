## REDSCAN-LOG4SHELL

| Attribute     | Value                                        |
| ------------- | -------------------------------------------- |
| Subscribe to  | FANOUT_HTTPS_SERVICES_EXCHANGE_NAME          |
| Send to       |                                              |
| Tools used    |                                              |
| Configuration | /conf/log4j2.xml : Log4j2 configuration file |

### Objective

Fuzz HTTP parameter for log4shell vulnerability.

### Description


### How to develop

```
docker run -d -p 5672:5672 -p 15672:15672 --name redscan-rabbit-dev rabbitmq:3-management
docker run -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" --name redscan-elasticsearch-dev docker.elastic.co/elasticsearch/elasticsearch:7.10.1
```