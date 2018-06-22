# --- !Ups
create or replace function "CFN_INSERT_CANCEL"(
  "_ID_ACCOUNT_REF" uuid,
  "_CODE_CURRENCY_BASE" text,
  "_CODE_CURRENCY_COUNTER" text,
  "_SIDE" text,
  "_PRICE" decimal(21,2),
  "_SIZE" decimal(21,2),
  "_ID_ORDER" uuid
) returns boolean as
$BODY$
begin
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
    'CANCEL'::trade,
    "_SIDE"::side,
    "_PRICE",
    "_SIZE",
    "_CODE_CURRENCY_BASE"::currency,
    "_CODE_CURRENCY_COUNTER"::currency,
    now());;

    return true;;
end;;
$BODY$
LANGUAGE plpgsql;

# --- !Downs
drop function "CFN_INSERT_CANCEL"(uuid,text,text,text,decimal,decimal,uuid);
