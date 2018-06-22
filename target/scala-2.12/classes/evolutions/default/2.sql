# --- !Ups

create table "TRADE_WALLETS" ("ID" UUID NOT NULL PRIMARY KEY,"ID_ACCOUNT_REF" UUID NOT NULL,"COLOR" VARCHAR NOT NULL,"CREATE_AT" timestamp NOT NULL);

create unique index "IDX_ID_TRADE_WALLETS_REF_TRADE_WALLETS" on "TRADE_WALLETS" ("ID_ACCOUNT_REF");

create table "BALANCE" ("ID" SERIAL NOT NULL PRIMARY KEY,"ID_ACCOUNT_REF" UUID NOT NULL,"ID_CURRENCY" currency NOT NULL,"BALANCE" DECIMAL(21,2) NOT NULL,"CREATE_AT" timestamp NOT NULL);

create table "CURRENCY_NETWORK" ("ID" currency NOT NULL PRIMARY KEY,"NAME" VARCHAR NOT NULL);

create table "FUNDS_HISTORY" ("ID" SERIAL NOT NULL PRIMARY KEY,"ID_ACCOUNT_REF" UUID NOT NULL,"ID_CURRENCY" currency NOT NULL,"PAYMENT_TYPE" payment NOT NULL,"AMOUNT" DECIMAL(21,2) NOT NULL,"CREATE_AT" timestamp NOT NULL);

create table "TRANSACTIONS" ("ID" SERIAL NOT NULL PRIMARY KEY,"ID_ORDER" UUID NOT NULL,"ID_ACCOUNT_REF" UUID NOT NULL,"TRADE_TYPE" trade NOT NULL,"SIDE" side NOT NULL,"PRICE" DECIMAL(21,2) NOT NULL,"SIZE_IN_BASE" DECIMAL(21,2) NOT NULL,"ID_CURRENCY_BASE" currency NOT NULL,"ID_CURRENCY_COUNTER" currency NOT NULL,"CREATED_AT" timestamp NOT NULL);

create table "ORDER_BOOK_SUMMARY" ("DELTA" INTEGER NOT NULL,"CREATED_AT" timestamp NOT NULL,"DATA" VARCHAR NOT NULL);

alter table "BALANCE" add constraint "FK_ID_ACCOUNT_BALANCE" foreign key("ID_ACCOUNT_REF") references "TRADE_WALLETS"("ID_ACCOUNT_REF") on update CASCADE on delete CASCADE;

alter table "FUNDS_HISTORY" add constraint "FK_ID_ACCOUNT_FUNDS_HISTORY" foreign key("ID_ACCOUNT_REF") references "TRADE_WALLETS"("ID_ACCOUNT_REF") on update CASCADE on delete CASCADE;

alter table "TRANSACTIONS" add constraint "FK_ID_ACCOUNT_TRANSACTIONS" foreign key("ID_ACCOUNT_REF") references "TRADE_WALLETS"("ID_ACCOUNT_REF") on update CASCADE on delete CASCADE;



# --- !Downs

alter table "TRANSACTIONS" drop constraint "FK_ID_ACCOUNT_TRANSACTIONS";
alter table "FUNDS_HISTORY" drop constraint "FK_ID_ACCOUNT_FUNDS_HISTORY";
alter table "BALANCE" drop constraint "FK_ID_ACCOUNT_BALANCE";
drop table "ORDER_BOOK_SUMMARY";
drop table "TRANSACTIONS";
drop table "FUNDS_HISTORY";
drop table "CURRENCY_NETWORK";
drop table "BALANCE";
drop table "TRADE_WALLETS";
