\timing on

SELECT *
FROM (
         SELECT *,
                ROW_NUMBER() OVER (ORDER BY id) AS row_num
         FROM test_v7
     ) subquery
WHERE row_num = 50_000;
--- 1 000 000
insert into test_v7(id)
select uuidv7() from generate_series(1,1_000_000);

insert into test_v4(id)
select uuidv4() from generate_series(1,1_000_000);

SELECT pg_size_pretty(pg_relation_size('test_v7_pkey'));
SELECT pg_size_pretty(pg_relation_size('test_v4_pkey'));
---
truncate table test_v7;
truncate table test_v4;

vacuum (index_cleanup on, full) test_v7;
VACUUM (index_cleanup on, full) test_v4;
---10 000 000
insert into test_v7(id)
select uuidv7() from generate_series(1,10_000_000);

insert into test_v4(id)
select uuidv4() from generate_series(1,10_000_000);

SELECT pg_size_pretty(pg_relation_size('test_v7_pkey'));
SELECT pg_size_pretty(pg_relation_size('test_v4_pkey'));
---100 000 000
insert into test_v7(id)
select uuidv7() from generate_series(1,100_000_000);

insert into test_v4(id)
select uuidv4() from generate_series(1,100_000_000);

SELECT pg_size_pretty(pg_relation_size('test_v7_pkey'));
SELECT pg_size_pretty(pg_relation_size('test_v4_pkey'));
---1 000 000 000
insert into test_v7(id)
select uuidv7() from generate_series(1,1_000_000_000);

insert into test_v4(id)
select uuidv4() from generate_series(1,1_000_000_000);

SELECT pg_size_pretty(pg_relation_size('test_v7_pkey'));
SELECT pg_size_pretty(pg_relation_size('test_v4_pkey'));