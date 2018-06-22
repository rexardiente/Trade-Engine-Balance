# Trade Engine Balance

```
createuser mr-balance
createdb --encoding=UTF8 --owner=mr-balance mr-balance
```
<!-- ## Mr Trade Engine Url
```javascript
tradeEngineURL {
  url = "akka.tcp://application@127.0.0.1:8001/user/TradeEngineAccessor"
}
``` -->

## Response Format

```javascript
{
  "message": "the command sent",
  "response": "the result"
}
```
## Validating Order Json Example

```javascript
{
  "command": "order",
  "id_account_ref": "dc59a3e6-c2e8-49d6-9ee3-5e39ad5264d5",
  "side": "BUY",
  "amount": 1,
  "price": 1,
  "id_currency_base": "JPY",
  "id_currency_counter": "BTC"
}
```
## Validating User or All Open Order Json Example

```javascript
{
  "command": "open_orders",
  "id_account_ref": "dc59a3e6-c2e8-49d6-9ee3-5e39ad5264d5",
  "currency_base": "BTC",
  "currency_counter": "JPY"
}
```
## Order Cancel Json Example

```javascript
{
  "command": "cancel_order",
  "id_account_ref": "dc59a3e6-c2e8-49d6-9ee3-5e39ad5264d5",
  "side": "BUY",
  "amount": 8,
  "price": 2,
  "id_currency_base": "JPY",
  "id_currency_counter": "BTC",
  "id_order": "d903314d-478d-4210-8afc-d761a83484e4"
}
```

## Currency Deposit
## `true` if deposit then `false` if withdraw...
```javascript
{
  "command" : "funds",
  "id_account_ref": "dc59a3e6-c2e8-49d6-9ee3-5e39ad5264d5",
  "amount": "1000",
  "id_currency": "XRP",
  "is_deposit": true
}
```
## Get Balances
```javascript
{
  "command" : "get_balance",
  "id_account_ref": "dc59a3e6-c2e8-49d6-9ee3-5e39ad5264d5"
}
```
## Validating Order DB Example

```sql
insert into "public"."ACCOUNTS" ( "ID", "ID_MR_EXCHANGE_ACCOUNT", "CREATE_AT") values ( '2da5e1b4-0a93-430e-875a-e0fece213273', 'dc59a3e6-c2e8-49d6-9ee3-5e39ad5264d5', '2017-12-11 11:14:18');

insert into "public"."BALANCE" ( "ID_MRE_ACCOUNT", "ID_CURRENCY", "BALANCE", "AVAILABLE", "CREATE_AT") values ( 'dc59a3e6-c2e8-49d6-9ee3-5e39ad5264d5', '1', '100.00', '50.00', '2017-12-11 11:15:52');
```

## Get Transaction History
```javascript
{
  "command" : "transaction_history",
  "id_account_ref": "dc59a3e6-c2e8-49d6-9ee3-5e39ad5264d5"
}
```

## Get Funds History
```javascript
{
  "command" : "funds_history",
  "id_account_ref": "dc59a3e6-c2e8-49d6-9ee3-5e39ad5264d5"
}
```
