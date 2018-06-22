create or replace function "CFN_GET_PAIR_LIST"(
  "_DATA" text
) returns table(
  "CURRENCY_BASE" currency,
  "CURRENCY_COUNTER" currency,
  "LATEST_PRICE" decimal(21,2),
  "VOLUME_24HR" decimal(21,2),
  "CHANGE_24HR" decimal(21,2)
) as
$BODY$
BEGIN
  create temp table if not exists "PAIR_LIST" (
    "ID_CURRENCY_BASE" currency,
    "ID_CURRENCY_COUNTER" currency
  );
  truncate table "PAIR_LIST";

  insert into "PAIR_LIST"
  select
    "code_currency_base"::currency,
    "code_currency_counter"::currency
  from json_to_recordset("_DATA"::json) as x(
    "code_currency_base" varchar,
    "code_currency_counter" varchar);

  return query
  select
    tmp.base,
    tmp.counter,
    tmp.latest,
    tmp.volume,
    case tmp.latest_in_24hr is null when true then 0.0
    else
      case coalesce(tmp.latest_before_24hr, 0.0) when 0.0
        then (tmp.latest_in_24hr - coalesce(tmp.latest_before_24hr, 0.0))
      else
        (tmp.latest_in_24hr - tmp.latest_before_24hr) / tmp.latest_before_24hr
      end
    end
  from (
    select
      "ID_CURRENCY_BASE" as base,
      "ID_CURRENCY_COUNTER" as counter,
      (select trans."PRICE" from "TRANSACTIONS" as trans
        where trans."ID_CURRENCY_BASE" = pair."ID_CURRENCY_BASE"
        and trans."ID_CURRENCY_COUNTER" = pair."ID_CURRENCY_COUNTER"
        and (trans."TRADE_TYPE" = 'FILL'::trade or trans."TRADE_TYPE" = 'PARTIALLY_FILL'::trade)
        and trans."SIDE" = 'BUY'::side
        order by trans."ID" desc limit 1) as latest,
      coalesce((select sum(trans."SIZE_IN_BASE") from "TRANSACTIONS" as trans
        where trans."ID_CURRENCY_BASE" = pair."ID_CURRENCY_BASE"
        and trans."ID_CURRENCY_COUNTER" = pair."ID_CURRENCY_COUNTER"
        and (trans."TRADE_TYPE" = 'FILL'::trade or trans."TRADE_TYPE" = 'PARTIALLY_FILL'::trade)
        and trans."SIDE" = 'BUY'::side
        and "CREATED_AT" > now() - interval '24 hour'), 0.0) as volume,
      (select trans."PRICE" from "TRANSACTIONS" as trans
        where trans."ID_CURRENCY_BASE" = pair."ID_CURRENCY_BASE"
        and trans."ID_CURRENCY_COUNTER" = pair."ID_CURRENCY_COUNTER"
        and (trans."TRADE_TYPE" = 'FILL'::trade or trans."TRADE_TYPE" = 'PARTIALLY_FILL'::trade)
        and trans."SIDE" = 'BUY'::side
        and "CREATED_AT" > now() - interval '24 hour'
        order by trans."ID" desc limit 1) as latest_in_24hr,
      (select trans."PRICE" from "TRANSACTIONS" as trans
        where trans."ID_CURRENCY_BASE" = pair."ID_CURRENCY_BASE"
        and trans."ID_CURRENCY_COUNTER" = pair."ID_CURRENCY_COUNTER"
        and (trans."TRADE_TYPE" = 'FILL'::trade or trans."TRADE_TYPE" = 'PARTIALLY_FILL'::trade)
        and trans."SIDE" = 'BUY'::side
        and "CREATED_AT" <= now() - interval '24 hour'
        order by trans."ID" desc limit 1) as latest_before_24hr
    from "PAIR_LIST" as pair
  ) as tmp;
END;
$BODY$
LANGUAGE plpgsql;


--select "CFN_GET_PAIR_LIST"('[{"code_currency_base":"XRP", "code_currency_counter":"BTC"},{ "code_currency_base":"XRP", "code_currency_counter":"LTC"}]');
