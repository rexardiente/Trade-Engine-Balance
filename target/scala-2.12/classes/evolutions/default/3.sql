# --- !Ups

insert into "CURRENCY_NETWORK" ("ID", "NAME") values ('BTC', 'Bitcoin');
insert into "CURRENCY_NETWORK" ("ID", "NAME") values ('LTC', 'Litecoin');
insert into "CURRENCY_NETWORK" ("ID", "NAME") values ('USD', 'US Dollar');
insert into "CURRENCY_NETWORK" ("ID", "NAME") values ('XRP', 'Ripple');
insert into "CURRENCY_NETWORK" ("ID", "NAME") values ('REP', 'Augur');
insert into "CURRENCY_NETWORK" ("ID", "NAME") values ('ETC', 'EthereumClassic');
insert into "CURRENCY_NETWORK" ("ID", "NAME") values ('XLM', 'Stellar');
insert into "CURRENCY_NETWORK" ("ID", "NAME") values ('DOG', 'Dogecoin');
insert into "CURRENCY_NETWORK" ("ID", "NAME") values ('ETH', 'Ethereum');
insert into "CURRENCY_NETWORK" ("ID", "NAME") values ('BCH', 'Bitcoin Cash');
insert into "CURRENCY_NETWORK" ("ID", "NAME") values ('DCT', 'Decent');
insert into "CURRENCY_NETWORK" ("ID", "NAME") values ('XMR', 'Monero');
insert into "CURRENCY_NETWORK" ("ID", "NAME") values ('ZEC', 'Zcash');
insert into "CURRENCY_NETWORK" ("ID", "NAME") values ('JPY', 'Japanese Yen');
insert into "CURRENCY_NETWORK" ("ID", "NAME") values ('VDX', 'Vauldexcoin');

# --- !Downs
truncate table "CURRENCY_NETWORK" cascade;
