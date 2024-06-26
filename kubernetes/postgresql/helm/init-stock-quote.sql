CREATE SCHEMA stock;

CREATE TABLE stock.stock_statistic (
    code varchar(20) not null primary key,
    init_price numeric(10,2) not null,
    avg_volume numeric(15) not null,
    mu numeric(12,10) not null,
    sigma numeric(12,10) not null,
    price_t numeric(10,2),
    volume_t numeric(15)
);

CREATE TABLE stock.price_feed (
    id uuid not null default gen_random_uuid() primary key,
    code varchar(20) not null,
    price numeric(10,2) not null,
    created timestamp not null default current_timestamp
);

CREATE TABLE stock.volume_feed (
    id uuid not null default gen_random_uuid() primary key,
    code varchar(20) not null,
    volume numeric(15) not null,
    created timestamp not null default current_timestamp
);

COPY stock.stock_statistic (
    code, init_price, avg_volume, mu, sigma
)
FROM '/docker-entrypoint-initdb.d/stock-statistic.csv'
DELIMITER ','
CSV HEADER;

CREATE OR REPLACE PROCEDURE stock.nextPriceFeed(
    stat record,
    delta_t numeric
) LANGUAGE plpgsql
as $$
declare
    v_price_t numeric;
    delta_S numeric;
begin
    v_price_t = coalesce(stat.price_t,stat.init_price);
    delta_S = delta_t * v_price_t + stat.sigma * sqrt(delta_t) * v_price_t * random_normal(0.0,1.0);

    raise notice 'next stock price %:%', stat.code, v_price_t + delta_s;

    insert into stock.price_feed (
        code, price
    ) values (
        stat.code, v_price_t + delta_s
    );

    update stock.stock_statistic 
    set price_t = v_price_t + delta_s
    where code = stat.code;
    
end $$;


CREATE OR REPLACE PROCEDURE stock.nextVolumeFeed(
    stat record,
    p_interval integer
) LANGUAGE plpgsql
as $$
declare
    v_volume_t integer;
    delta_v integer;
begin
    v_volume_t = coalesce(stat.volume_t,0);
    delta_v = floor((stat.avg_volume * (0.95 + random() * 0.1))/p_interval);
 
    raise notice 'next stock volume %:%', stat.code, v_volume_t + delta_v;

    insert into stock.volume_feed (
        code, volume
    ) values (
        stat.code, v_volume_t + delta_v
    );

    update stock.stock_statistic 
    set volume_t = v_volume_t + delta_v
    where code = stat.code;
    
end $$;

CREATE OR REPLACE procedure stock.genRandomPriceFeed(
    p_cnt integer default 100,
    p_batch integer default 5,
    p_max_timeout integer default 5
) language plpgsql
as $$
declare
    stat record;
    delta_t numeric;
begin
    delta_t = 1.0/(252*8*60);

    for cnt in 1..p_cnt
    loop
        for stat in
            select * from stock.stock_statistic
            order by random() limit p_batch
        loop
            call stock.nextPriceFeed(stat,delta_t);
            commit;
        end loop;

        perform pg_sleep(round(random()*p_max_timeout));
    end loop;
end $$;

CREATE OR REPLACE procedure stock.genRandomVolumeFeed(
    p_cnt integer default 100,
    p_batch integer default 5,
    p_max_timeout integer default 5
) language plpgsql
as $$
declare
    stat record;
    interval integer;
begin
    interval = (8*60);

    for cnt in 1..p_cnt
    loop
        for stat in
            select * from stock.stock_statistic
            order by random() limit p_batch
        loop
            call stock.nextVolumeFeed(stat,interval);
            commit;
        end loop;

        perform pg_sleep(round(random()*p_max_timeout));
    end loop;
end $$;
