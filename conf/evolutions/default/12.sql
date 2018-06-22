# --- !Ups
create or replace function "CFN_CANCELLED"(
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
  declare "_HAS_CURRENCY" boolean;;
  declare "_CANCEL_CONTAINS_TX" boolean;;
begin
  -- necessary conditions to update balance for trade type cancel to cancelled
  select 1 from "BALANCE" as bal
  where bal."ID_ACCOUNT_REF" = "_ID_ACCOUNT_REF"
  and bal."ID_CURRENCY" = case when("_SIDE" = 'SELL'::side) then ("_ID_CURRENCY_BASE") else ("_ID_CURRENCY_COUNTER") end
  into "_HAS_CURRENCY";;

  select 1 from "TRANSACTIONS" as tx
  where tx."ID_ORDER" = "_ID_ORDER"
  and tx."ID_ACCOUNT_REF" = "_ID_ACCOUNT_REF"
  and tx."TRADE_TYPE" = 'CANCEL'::trade
  and "_TRADE_TYPE" = 'CANCELLED'::trade
  and tx."SIDE" = "_SIDE"
  and tx."PRICE" = "_PRICE"
  and tx."SIZE_IN_BASE" = "_SIZE"
  and tx."ID_CURRENCY_BASE" = "_ID_CURRENCY_BASE"
  and tx."ID_CURRENCY_COUNTER" = "_ID_CURRENCY_COUNTER"
  into "_CANCEL_CONTAINS_TX";;

  -- update from cancel to cancelled in transactions table
  -- update balance if trade type is cancelled
  if ("_HAS_CURRENCY" = true and "_CANCEL_CONTAINS_TX" = true) then
    update "BALANCE" as bal
    set "BALANCE" = case when("_SIDE" ='SELL'::side) then (bal."BALANCE" + "_SIZE")  else (("_SIZE" * "_PRICE") + bal."BALANCE") end
    where bal."ID_ACCOUNT_REF" = "_ID_ACCOUNT_REF"
    and bal."ID_CURRENCY" = case when("_SIDE" = 'SELL'::side) then ("_ID_CURRENCY_BASE") else ("_ID_CURRENCY_COUNTER") end;;

    update "TRANSACTIONS" as tx
    set "TRADE_TYPE" = 'CANCELLED'::trade
    where tx."ID_ORDER" = "_ID_ORDER"
    and tx."ID_ACCOUNT_REF" = "_ID_ACCOUNT_REF"
    and tx."TRADE_TYPE" = 'CANCEL'::trade
    and "_TRADE_TYPE" = 'CANCELLED'::trade
    and tx."SIDE" = "_SIDE"
    and tx."PRICE" = "_PRICE"
    and tx."SIZE_IN_BASE" = "_SIZE"
    and tx."ID_CURRENCY_BASE" = "_ID_CURRENCY_BASE"
    and tx."ID_CURRENCY_COUNTER" = "_ID_CURRENCY_COUNTER";;

    return true;;
  else
    return false;;
  end if;;
  exception
    when others then
      raise notice 'EXCEPTION ON CFN_CANCELLED FUNCTION!';;
      return false;;
end;;
$BODY$
LANGUAGE plpgsql;

# --- !Downs
drop function "CFN_CANCELLED"(uuid,uuid,trade,decimal,decimal,side,currency,currency);
