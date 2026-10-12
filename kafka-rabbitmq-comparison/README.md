# Introduction


## Limits
- Apache Kafka without idempotency
- Kafka n Rabbit without data replication
- Concurrency for all consumers is the same
- Redis Pub/Sub is at-most-once, with no persistence and no consumer group; one subscriber, dispatched with the same concurrency as the other brokers
- No tuning for auto-acks, GC and such things. Results can differ a lot by tuning the application and/or message queues configuration.