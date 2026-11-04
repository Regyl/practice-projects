CREATE TABLE account
(
    id     INTEGER PRIMARY KEY,
    amount BIGINT NOT NULL
);

INSERT INTO account VALUES (1, 1000), (2, 1000);

CREATE OR REPLACE PROCEDURE transfer_money(IN source_id integer, IN target_id integer, IN target_amount bigint)
    LANGUAGE PLPGSQL
as $$
DECLARE
    source_amount bigint;
    temp_entity bigint;
BEGIN
    if source_id = target_id then
        RAISE EXCEPTION 'source and target cannot be equal';
    end if;
    --check entity source exists
    temp_entity := -1;
    select id from account where id=source_id into temp_entity;
    if temp_entity is null then
        raise exception 'no entity with id %', source_id;
    end if;

    --check entity target exists
    temp_entity := -1;
    select id from account where id=target_id into temp_entity;
    if temp_entity is null then
        raise exception 'no entity with id %', source_id;
    end if;

    if source_id > target_id then
        update account set amount = amount - target_amount where id=source_id;
        select amount from account where id = source_id into source_amount;
        --check for amount only after locking
        if source_amount < 0 then
            raise exception 'not enough money';
        end if;
        update account set amount = amount + target_amount where id=target_id;
    ELSE
        update account set amount = amount + target_amount where id=target_id;
        update account set amount = amount - target_amount where id=source_id;
        select amount from account where id=source_id into source_amount;
        if source_amount < 0 then
            raise exception 'not enough money';
        end if;
    end if;
END
$$;

call transfer_money(1,2,10);
-- call transfer_money(1,1,10); --should raise exception
-- call transfer_money(1,0,1000);

SELECT * FROM account;