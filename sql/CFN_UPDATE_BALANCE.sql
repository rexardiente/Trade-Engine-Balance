create or replace function "CFN_UPDATE_BALANCE"(
  "_DATA" text
) returns table(
  "ID_ORDER" uuid,
  "ID_ACCOUNT_REF" uuid,
  "TRADE_TYPE" trade,
  "SIZE" decimal(21, 2),
  "PRICE" decimal(21, 2),
  "SIDE" side,
  "ID_CURRENCY_BASE" currency,
  "ID_CURRENCY_COUNTER" currency,
  "IS_SUCCESSFUL" boolean
) as
$BODY$
begin
  create table if not exists "ORDER_RESULTS" (
    "ID_ORDER" uuid,
    "ID_ACCOUNT_REF" uuid,
    "TRADE_TYPE" trade,
    "SIZE" decimal(21, 2),
    "PRICE" decimal(21, 2),
    "SIDE" side,
    "ID_CURRENCY_BASE" currency,
    "ID_CURRENCY_COUNTER" currency
  );
  lock table "ORDER_RESULTS" in exclusive mode;
  lock table "TRANSACTIONS" in exclusive mode;
  lock table "BALANCE" in exclusive mode;

  truncate table "ORDER_RESULTS";

  insert into "ORDER_RESULTS"
  select
    "id_order",
    "id_account",
    "code"::trade,
    "size",
    "price",
    case when ("is_sell" = true) then 'SELL'::side else 'BUY'::side end,
    "code_currency_base"::currency,
    "code_currency_counter"::currency
  from json_to_recordset("_DATA"::json) as x(
    "id_order" uuid,
    "id_account" uuid,
    "code" varchar,
    "size" decimal(21,2),
    "price" decimal(21,2),
    "is_sell" boolean,
    "code_currency_base" varchar,
    "code_currency_counter" varchar)
  where "code" in ('FILL', 'PARTIALLY_FILL', 'CANCELLED');

  return query
  select
    res."ID_ORDER",
    res."ID_ACCOUNT_REF",
    res."TRADE_TYPE",
    res."SIZE",
    res."PRICE",
    res."SIDE",
    res."ID_CURRENCY_BASE",
    res."ID_CURRENCY_COUNTER",
    case
    when (res."TRADE_TYPE" = 'FILL'::trade or res."TRADE_TYPE" = 'PARTIALLY_FILL'::trade) then
    (select "CFN_FILLED_OR_PARTIALLY_FILLED"(
    res."ID_ORDER",
    res."ID_ACCOUNT_REF",
    res."TRADE_TYPE",
    res."SIZE",
    res."PRICE",
    res."SIDE",
    res."ID_CURRENCY_BASE",
    res."ID_CURRENCY_COUNTER"))
    when (res."TRADE_TYPE" = 'CANCELLED'::trade) then
    (select "CFN_CANCELLED"(
    res."ID_ORDER",
    res."ID_ACCOUNT_REF",
    res."TRADE_TYPE",
    res."SIZE",
    res."PRICE",
    res."SIDE",
    res."ID_CURRENCY_BASE",
    res."ID_CURRENCY_COUNTER"))
  else
    false
  end
  from "ORDER_RESULTS" as res;
  exception
    when others then
      raise notice 'EXCEPTION ON CFN_UPDATE_BALANCE FUNCTION!';
end;
$BODY$
LANGUAGE plpgsql;


--select "CFN_UPDATE_BALANCE"('[{"id_order":"00000000-0000-0000-000A-000000000000", "id_account":"00000000-0000-0000-0000-000000000001", "code":"FILL", "size":"3.1", "price":"2.3", "is_sell": false, "code_currency_base":"XRP", "code_currency_counter":"JPY"},{"id_order":"00000000-0000-0000-000B-000000000000", "id_account":"00000000-0000-0000-0000-000000000002", "code":"FILL", "size":"3.1", "price":"2.3", "is_sell": true, "code_currency_base":"XRP", "code_currency_counter":"JPY"}]');
