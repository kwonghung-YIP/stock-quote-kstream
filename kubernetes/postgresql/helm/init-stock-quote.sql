CREATE SCHEMA stock;

CREATE TABLE stock.price_feed (
    ticker varchar(20) not null primary key,
    price numeric(10,2) not null,
    ver int not null default 1,
    created timestamp not null default current_timestamp,
    lastupd timestamp not null default current_timestamp
);

CREATE TABLE stock.volume_feed (
    ticker varchar(20) not null primary key,
    volume numeric(15,2) not null,
    ver int not null default 1,
    created timestamp not null default current_timestamp,
    lastupd timestamp not null default current_timestamp
);
