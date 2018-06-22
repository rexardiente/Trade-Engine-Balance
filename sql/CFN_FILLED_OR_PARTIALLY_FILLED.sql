create or replace function "CFN_FILLED_OR_PARTIALLY_FILLED"(
  "_ID_ORDER" uuid,
  "_ID_ACCOUNT_REF" uuid,
  "_TRADE_TYPE" trade,
  "_SIZE" decimal(21, 2),
  "_PRICE" decimal(21, 2),
  "_SIDE" side,
  "_ID_CURRENCY_BASE" currency,
  "_ID_CURRENCY_COUNTER" currency
) returns boolean as
$BODY$
  declare "_HAS_CURRENCY" boolean;
  declare "_CONTAINS_TX" boolean;
begin
  -- necessary conditions to update balance for trade type fill/partial-fill
  select 1 from "BALANCE" as bal
  where bal."ID_ACCOUNT_REF" = "_ID_ACCOUNT_REF"
  and bal."ID_CURRENCY" = case when("_SIDE" = 'SELL'::side) then ("_ID_CURRENCY_COUNTER") else ("_ID_CURRENCY_BASE") end
  into "_HAS_CURRENCY";

  select 1 from "TRANSACTIONS" as tx
  where tx."ID_ORDER" = "_ID_ORDER"
  and tx."ID_ACCOUNT_REF" = "_ID_ACCOUNT_REF"
  and tx."SIZE_IN_BASE" = "_SIZE"
  and tx."PRICE" = "_PRICE"
  and tx."SIDE" = "_SIDE"
  and tx."ID_CURRENCY_BASE" = "_ID_CURRENCY_BASE"
  and tx."ID_CURRENCY_COUNTER" = "_ID_CURRENCY_COUNTER"
  and tx."TRADE_TYPE" = "_TRADE_TYPE"
  into "_CONTAINS_TX";

  --  update balance if trade type fill/partial-fill
  if ("_HAS_CURRENCY" = true and "_CONTAINS_TX" is null) then
    update "BALANCE" as bal
    set "BALANCE" = case when("_SIDE" = 'SELL'::side) then (("_SIZE" * "_PRICE") + bal."BALANCE") else (bal."BALANCE" + "_SIZE") end
    where bal."ID_ACCOUNT_REF" = "_ID_ACCOUNT_REF"
    and bal."ID_CURRENCY" = case when("_SIDE" = 'SELL'::side) then ("_ID_CURRENCY_COUNTER") else ("_ID_CURRENCY_BASE") end;

    -- insert transaction
    insert into "TRANSACTIONS" (
    "ID_ORDER",
    "ID_ACCOUNT_REF",
    "TRADE_TYPE",
    "SIDE",
    "PRICE",
    "SIZE_IN_BASE",
    "ID_CURRENCY_BASE",
    "ID_CURRENCY_COUNTER",
    "CREATED_AT")
    values (
    "_ID_ORDER",
    "_ID_ACCOUNT_REF",
    "_TRADE_TYPE",
    "_SIDE",
    "_PRICE",
    "_SIZE",
    "_ID_CURRENCY_BASE",
    "_ID_CURRENCY_COUNTER",
    now());

    return true;

  elseif ("_HAS_CURRENCY" is null and "_CONTAINS_TX" is null) then
    -- insert row in balance if currency does not exist
    insert into "BALANCE" ("ID_ACCOUNT_REF", "ID_CURRENCY", "BALANCE", "CREATE_AT")
    select
    "_ID_ACCOUNT_REF",
    case when("_SIDE" = 'SELL'::side) then ("_ID_CURRENCY_COUNTER") else ("_ID_CURRENCY_BASE") end,
    case when("_SIDE" = 'SELL'::side) then ("_SIZE" * "_PRICE") else ("_SIZE") end,
    now()
    from "BALANCE" as bal
    where not exists(
    select 1 from "BALANCE" as bal where bal."ID_ACCOUNT_REF" = "_ID_ACCOUNT_REF"
    and bal."ID_CURRENCY" = case when("_SIDE" = 'SELL'::side) then ("_ID_CURRENCY_COUNTER") else ("_ID_CURRENCY_BASE") end);

    -- insert transaction
    insert into "TRANSACTIONS" (
    "ID_ORDER",
    "ID_ACCOUNT_REF",
    "TRADE_TYPE",
    "SIDE",
    "PRICE",
    "SIZE_IN_BASE",
    "ID_CURRENCY_BASE",
    "ID_CURRENCY_COUNTER",
    "CREATED_AT")
    values (
    "_ID_ORDER",
    "_ID_ACCOUNT_REF",
    "_TRADE_TYPE",
    "_SIDE",
    "_PRICE",
    "_SIZE",
    "_ID_CURRENCY_BASE",
    "_ID_CURRENCY_COUNTER",
    now());

    return true;
  else
    return false;
  end if;
  exception
    when others then
      raise notice 'EXCEPTION ON CFN_FILLED_OR_PARTIALLY_FILLED FUNCTION!';
      return false;
end;
$BODY$
LANGUAGE plpgsql;
