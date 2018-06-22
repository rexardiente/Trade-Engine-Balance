# --- !Ups
create or replace function "CFN_DEPOSIT"(
  "_ID_ACCOUNT_REF" uuid,
  "_ID_CURRENCY" text,
  "_AMOUNT" decimal(21,2)
) returns boolean as
$BODY$
begin
  if exists (select 1 from "TRADE_WALLETS"
    where "ID_ACCOUNT_REF" = "_ID_ACCOUNT_REF")
  then
    if exists (select 1 from "BALANCE"
      where "ID_ACCOUNT_REF" = "_ID_ACCOUNT_REF"
      and "ID_CURRENCY" = "_ID_CURRENCY"::currency)
    then
      update "BALANCE" set "BALANCE" = "BALANCE" + "_AMOUNT"
      where "ID_ACCOUNT_REF" = "_ID_ACCOUNT_REF"
      and "ID_CURRENCY" = "_ID_CURRENCY"::currency;;
    else
      insert into "BALANCE" (
        "ID",
        "ID_ACCOUNT_REF",
        "ID_CURRENCY",
        "BALANCE",
        "CREATE_AT")
      values (
        default,
        "_ID_ACCOUNT_REF",
        "_ID_CURRENCY"::currency,
        "_AMOUNT",
        now());;
    end if;;

    insert into "FUNDS_HISTORY" (
      "ID",
      "ID_ACCOUNT_REF",
      "ID_CURRENCY",
      "PAYMENT_TYPE",
      "AMOUNT",
      "CREATE_AT")
    values (
      default,
      "_ID_ACCOUNT_REF",
      "_ID_CURRENCY"::currency,
      'DEPOSIT'::payment,
      "_AMOUNT",
      now());;

    return true;;
  else
    return false;;
  end if;;
end;;
$BODY$
LANGUAGE plpgsql;

# --- !Downs
drop function "CFN_DEPOSIT"(uuid,text,decimal);
