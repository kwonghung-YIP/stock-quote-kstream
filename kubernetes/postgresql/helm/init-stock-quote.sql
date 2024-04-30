CREATE SCHEMA stock;

CREATE TABLE stock.price_feed (
    id uuid not null default gen_random_uuid() primary key,
    market varchar(20) not null default 'NASDAQ',
    ticker varchar(20) not null,
    trade_date date not null default current_date,
    price numeric(10,2) not null,
    ver int not null default 1,
    created timestamp not null default current_timestamp,
    lastupd timestamp not null default current_timestamp,
    unique(market,ticker,trade_date)
);

CREATE TABLE stock.volume_feed (
    id uuid not null default gen_random_uuid() primary key,
    market varchar(20) not null default 'NASDAQ',
    ticker varchar(20) not null,
    trade_date date not null default current_date,
    volume numeric(15,2) not null,
    ver int not null default 1,
    created timestamp not null default current_timestamp,
    lastupd timestamp not null default current_timestamp,
    unique(market,ticker,trade_date)
);
