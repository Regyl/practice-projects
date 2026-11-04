# Tasks
- Design a debit credit thread safe flow
  - https://leetcode.com/discuss/post/2296788/revolut-sde-3-bangalore-wfh-rejected-by-0d4v8/

# Remember
- Functional and non-functional requirements
- TDD
- At least 2 steps of implementation
- Strategy pattern
- Custom exceptions
- Concurrency
- 100% test coverage

# Useful tips
```bash
mvn clean install -Dmaven.test.skip -Dcheckstyle.skip -Dpmd.skip
```
```
JaCoCo report at target/site/jacoco/index.html
```

# Common questions
## Money
BigDecimal is the ideal data type
## Concurrency
Q: why synchronizing the method is a bad idea vs using a variable
A: Because it locks the entire class. So all the rest methods are locked too, not only current

### Volatile
- not atomic
- When some thread made a change to a volatile variable, JVM sends a signal to the rest 
threads that previously cached value is invalid. On the next usage of this variable
thread will recache it from RAM to its own CPU cache
- Use volatile instead of AtomicBoolean in cases when u have only one publisher and multiple subscribers. Otherwise AtomicBoolean if variable can be updated by different threads

### Locks
- Semaphore: only N threads can enter the lock at the same time
- CountDownLatch: locked until all threads will release it
- CyclicBarrier: ? FIXME
- Exchanger: exchange the value, then proceed
- Phaser: ? FIXME
- ReentrantReadWriteLock: can allow read when no writes are present
- ReentrantLock: just the simplest lock
- StampedLock: ? FIXME

## Database
### ACID
- A - atomic. All statements will be executed or no one
- C - consistency. Data will match all CHECKs and other constraints after saving
- I - isolation. Each transaction is isolated from other ones
- D - durability. Data will not be lost after a server crush

### Optimistic VS pessimistic
- Optimistic: check for version before saving
- Pessimistic: SELECT FOR UPDATE

### Isolation levels
Implemented using MVCC (Multiversion Concurrency Control)
- Read uncommited: not exists in PostgreSQL
- Read commited: can see changes right before lock acquirement
- Repeatable read: will raise an exception if different transaction will modify row during the current transaction
- Serializable: ensures that data will be the same as if all transactions were executed one-by-one

### Indexes
- Partial
- Covering
- CLUSTER

### Just questions
- What are transaction isolation levels and how are they implemented?
  - read commited, repeatable, serializable. MVCC
- What types of locks exist in databases? 
  - ACCESS SHARE: SELECT
  - ROW SHARE: SELECT FOR UPDATE
  - ROW EXCL.: UPDATE/DELETE
  - ACCESS EXCL: VACUUM FULL, TRUNCATE, 
- What are the different scalability options for databases, and what are their pros and cons? 
  - Vertical
    - Partitioning
    - CLUSTER
  - Horizontal
    - Sharding
    - Replicas
- How do you handle database migrations?
  - Flyway: Simple. No way to rollback on free tier
  - Liquibase: More flexible and therefore more complicated
- What are deployment strategies for software systems? 
  - Big-bang - deploy everything at once with downtime
  - Rolling - changes are exposed to an increasing percentage of users incrementally until fully released
  - Canary - like blue/rolling, but only 5% traffic affects new version
  - Blue/green - old and new environments are exists simultaneously. After releasing v2, switch traffic to the new one
  - Shadow - checks for response from v1 and v2 match
  - Feature-flag
  - A/B - part of traffic comes to the old, part comes to the new. Then comparing business metrics
  - [Docs](https://circleci.com/blog/deployment-strategies-types-trade-offs-and-how-to-choose/#c-consent-modal)
- How do microservices operate, and what stability patterns can be applied?
  - API Gateway
  - Retries
    - Circuit breaker
  - Load balancers
  - CQRS: separate reading & writing models
  - Saga: distributed transaction strategy (Camunda BPM)
  - Fallback pattern: response in case of error
- Deployment tools
  - Ansible
  - Jenkins
  - GitHub/GitLab CI/CD
- How w'd u deploy?
  - Ansible for playbooks, Jenkins for running playbooks. Deployment strategy depends on context, but common answer: big-bang for non-mission-critical, rolling for mission-critical
- What if deployment failed?
  - Depends on strategy. For mission-critiical the only way is to rollback. For non-mission-critical maybe hotfix
- Configuration management
  - The best and only acceptable for me is something **like Spring Cloud**
- Secret management
  - HashiCorp Vault
  - Kubernetes Secrets
- Observability
  - What to check
    - JVM metrics
    - Application tech metrics
    - Application business metrics
    - DB metrics
    - Stacktrace
  - Tools
    - Granafa/Prometheus
    - Sentry
  - Alerting basics
    - Just create alerts for all the tech & business metrics. E.g. heap consumption 
  - Common metrics
    - Available heap
    - CPU consumption
    - Size of metaspace
    - Quantity of idle/locked/live threads
    - I/O speed
    - Healthcheck
    - Latency
    - RPS
    - WAL for PG
    - pg_lock
    - pg_stat_acitivity
    - Time for ack for Apache Kafka
    - Network load
    - Quantity of exceptions grouped by exception type per minute
    - GC pauses
- CQRS
  - Separate models/DB for reading & writing
  - Allows to simply scale reading and writing 
  - Produces eventual consistency
  - [Doc](https://martinfowler.com/bliki/CQRS.html)
- Thread vs Virtual Thread
  - Thread
    - Limited by OS
    - More resource consumption
    - Use when task is high-CPU-consuming
  - Virtual thread
    - Less resource consumption
    - There can be much more than hardware threads
- Apache Kafka exactly-once
  - On producer side enable.idempotency=true. This sends sequenceVal so broker can detect that this producer has already sent this message
  - On consumer needs additional id to detect that this message already has been consumed




