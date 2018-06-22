truncate table "TRADE_WALLETS" cascade;

insert into "TRADE_WALLETS" ("ID", "ID_ACCOUNT_REF", "COLOR", "CREATE_AT") values
('00000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000001', '#FF0000', now());
insert into "TRADE_WALLETS" ("ID", "ID_ACCOUNT_REF", "COLOR", "CREATE_AT") values
('00000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000002', '#FF0000', now());
insert into "TRADE_WALLETS" ("ID", "ID_ACCOUNT_REF", "COLOR", "CREATE_AT") values
('00000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-000000000003', '#FF0000', now());

insert into "BALANCE" ("ID", "ID_ACCOUNT_REF", "ID_CURRENCY", "BALANCE", "CREATE_AT") values (DEFAULT, '00000000-0000-0000-0000-000000000001', 'BTC', 10.0, now());
insert into "BALANCE" ("ID", "ID_ACCOUNT_REF", "ID_CURRENCY", "BALANCE", "CREATE_AT") values (DEFAULT, '00000000-0000-0000-0000-000000000001', 'JPY', 20.0, now());
insert into "BALANCE" ("ID", "ID_ACCOUNT_REF", "ID_CURRENCY", "BALANCE", "CREATE_AT") values (DEFAULT, '00000000-0000-0000-0000-000000000002', 'BTC', 5.0, now());