CREATE TABLE TEST_REPORT_KVA (
                                 ACCOUNT VARCHAR2(15),
                                 OPEN_DATE DATE,
                                 CLOSE_DATE DATE,
                                 ACCOUNT_TYPE VARCHAR2(500),
                                 AVERAGE_BACNT_BALANCE NUMBER,
                                 DEAL VARCHAR2(15),
                                 BEGIN_DATE DATE,
                                 PLAN_END_DATE DATE,
                                 ACTUAL_END_DATE DATE,
                                 DEAL_TYPE VARCHAR2(15),
                                 DEAL_CNT_OVER_ACNT NUMBER,
                                 DEAL_CNT_OVER_TYPE NUMBER,
                                 AS_OF_DATE DATE,
                                 LOAD_DATE DATE
)




    INSERT INTO TEST_REPORT_KVA (
    ACCOUNT,
    OPEN_DATE,
    CLOSE_DATE,
    ACCOUNT_TYPE,
    AVERAGE_BACNT_BALANCE,
    DEAL,
    BEGIN_DATE,
    PLAN_END_DATE,
    ACTUAL_END_DATE,
    DEAL_TYPE,
    DEAL_CNT_OVER_ACNT,
    DEAL_CNT_OVER_TYPE,
    AS_OF_DATE,
    LOAD_DATE
)
SELECT
    ra.ACNT_NUMBER,
    ra.ACNT_OPEN_DATE,
    ra.ACNT_CLOSE_DATE,
    dbt.BAL_NAME,
    AVG(aa.ACCD_IN_BALANCE + aa.ACCD_OUT_BALANCE),
    CASE WHEN LOWER(ddt.DEAL_TYPE_NAME) LIKE '%кредитная сделка%' THEN  rdc.CRDT_NUMBER
         WHEN LOWER(ddt.DEAL_TYPE_NAME) LIKE '%депозитная сделка%' THEN  rdd.DPST_NUMBER
         ELSE NULL
        END,
    rd.DEAL_BEGIN_DATE,
    rd.DEAL_PLAN_END_DATE,
    CASE WHEN rd.DEAL_ACTUAL_END_DATE IS NULL THEN rd.DEAL_ACTUAL_END_DATE
        END,
    ddt.DEAL_TYPE_NAME,
    COUNT(*) OVER(PARTITION BY rd.DEAL_DEAL_TYPE_CD),
        COUNT(*) OVER(PARTITION BY ra.ACNT_NUMBER),
        TO_DATE('01.05.2023', 'dd,mm,yyyy'),
    SYSDATE
FROM REF_ACCOUNT ra
         LEFT JOIN REF_ACNT_DEAL_LINK radl ON ra.ACNT_GID = radl.ACDL_ACNT_GID
         LEFT JOIN REF_DEAL rd ON radl.ACDL_DEAL_GID = rd.DEAL_GID
         LEFT JOIN DICT_DEAL_TYPE ddt ON rd.DEAL_DEAL_TYPE_CD = ddt.DEAL_TYPE_CD
         LEFT JOIN DICT_BALANCE_TYPE dbt ON ra.ACNT_BALANCE_TYPE_CD = dbt.BAL_KEY
         LEFT JOIN REF_DEAL_CREDIT rdc ON rd.DEAL_GID = rdc.CRDT_GID
         LEFT JOIN REF_DEAL_DEPOSIT rdd ON rd.DEAL_GID = rdd.DPST_GID
         LEFT JOIN ACC_ACCOUNT aa ON radl.ACDL_ACNT_GID = aa.ACCD_ACNT_GID
WHERE TRUNC(ra.ACNT_OPEN_DATE, 'MM') = TO_DATE('01.04.2023', 'dd,mm,yyyy')
  AND NOT REGEXP_LIKE(dbt.BAL_NAME, 'Резерв')
GROUP BY
    ra.ACNT_NUMBER,
    ra.ACNT_OPEN_DATE,
    ra.ACNT_CLOSE_DATE,
    dbt.BAL_NAME,
    rd.DEAL_BEGIN_DATE,
    rd.DEAL_PLAN_END_DATE,
    rd.DEAL_ACTUAL_END_DATE,
    ddt.DEAL_TYPE_NAME,
    CASE WHEN LOWER(ddt.DEAL_TYPE_NAME) LIKE '%кредитная сделка%' THEN  rdc.CRDT_NUMBER
         WHEN LOWER(ddt.DEAL_TYPE_NAME) LIKE '%депозитная сделка%' THEN  rdd.DPST_NUMBER
         ELSE NULL
        END;


select
    ra.ACNT_NUMBER,
    (select count(1)
     from REF_ACCOUNT ra1
     where ra.ACNT_NUMBER = ra1.ACNT_NUMBER
                         ) as DEAL_CNT_OVER_TYPE
FROM REF_ACCOUNT ra
inner JOIN REF_ACNT_DEAL_LINK radl ON ra.ACNT_GID = radl.ACDL_ACNT_GID
inner JOIN REF_DEAL rd ON radl.ACDL_DEAL_GID = rd.DEAL_GID
    limit 5;
