create or replace function "CFN_DEDUCT_BALANCE"(
  "_ID_ACCOUNT_REF" uuid,
  "_CODE_CURRENCY_BASE" text,
  "_CODE_CURRENCY_COUNTER" text,
  "_SIDE" text,
  "_PRICE" decimal(21,2),
  "_SIZE" decimal(21,2),
  "_ID_ORDER" uuid
) returns boolean as
$BODY$
  declare "_ID_CURRENCY" text;
  declare "_TOTAL" decimal(21,2);
begin
  if (upper("_SIDE") = 'SELL')
  then
    select "_CODE_CURRENCY_BASE" into "_ID_CURRENCY";
    select "_SIZE" into "_TOTAL";
  else
    select "_CODE_CURRENCY_COUNTER" into "_ID_CURRENCY";
    select ("_SIZE" * "_PRICE") into "_TOTAL";
  end if;

  if exists (select 1 from "BALANCE"
    where "ID_ACCOUNT_REF" = "_ID_ACCOUNT_REF"
    and "ID_CURRENCY" = "_ID_CURRENCY"::currency
    and "BALANCE" >= "_TOTAL")
  then
    update "BALANCE" set "BALANCE" = "BALANCE" - "_TOTAL"
    where "ID_ACCOUNT_REF" = "_ID_ACCOUNT_REF"
    and "ID_CURRENCY" = "_ID_CURRENCY"::currency;

    insert into "TRANSACTIONS" (
      "ID",
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
      default,
      "_ID_ORDER",
      "_ID_ACCOUNT_REF",
      'CREATE'::trade,
      "_SIDE"::side,
      "_PRICE",
      "_SIZE",
      "_CODE_CURRENCY_BASE"::currency,
      "_CODE_CURRENCY_COUNTER"::currency,
      now());

    return true;
  else
    return false;
  end if;
end;
$BODY$
LANGUAGE plpgsql;

-- select "CFN_DEDUCT_BALANCE"(
--   '00000000-0000-0000-0000-000000000001',
--   'BTC',
--   'JPY',
--   'BUY',
--   '1.0',
--   '2.0',
--   '00000000-0000-0000-0000-00000000000A');
