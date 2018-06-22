package models

import play.api.db.slick.{ DatabaseConfigProvider, HasDatabaseConfigProvider }
import javax.inject.{ Inject, Singleton }
import java.util.UUID
import scala.concurrent._
import scala.concurrent.duration._
import ExecutionContext.Implicits.global

@Singleton
class SqlExecutor @Inject()(
    protected val dbConfigProvider: DatabaseConfigProvider,
    protected implicit val ec: ExecutionContext)
  extends HasDatabaseConfigProvider[mrtradelibrary.utils.db.PostgresDriver] {
  import profile.api._

  def truncate = {
    db.run(sql"""truncate table "TRADE_WALLETS" cascade;""".as[Int])
  }

  def initDb = {
    db.run(sql"""
      insert into "TRADE_WALLETS" ("ID","ID_ACCOUNT_REF","COLOR","CREATE_AT")
      values ('00000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000001', '#FF0000', now());
      insert into "TRADE_WALLETS" ("ID","ID_ACCOUNT_REF","COLOR","CREATE_AT")
      values('00000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000002', '#FF0000', now());
      insert into "TRADE_WALLETS" ("ID","ID_ACCOUNT_REF","COLOR","CREATE_AT")
      values('00000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-000000000003', '#FF0000', now());

      insert into "BALANCE" ("ID", "ID_ACCOUNT_REF","ID_CURRENCY", "BALANCE", "CREATE_AT")
      values (DEFAULT, '00000000-0000-0000-0000-000000000001', 'BTC', 10.0, now());
      insert into "BALANCE" ("ID", "ID_ACCOUNT_REF","ID_CURRENCY", "BALANCE", "CREATE_AT")
      values (DEFAULT, '00000000-0000-0000-0000-000000000001', 'JPY', 100.0, now());
      insert into "BALANCE" ("ID", "ID_ACCOUNT_REF","ID_CURRENCY", "BALANCE", "CREATE_AT")
      values (DEFAULT, '00000000-0000-0000-0000-000000000002', 'BTC', 5.0, now());
      insert into "BALANCE" ("ID", "ID_ACCOUNT_REF","ID_CURRENCY", "BALANCE", "CREATE_AT")
      values (DEFAULT, '00000000-0000-0000-0000-000000000002', 'JPY', 50.0, now());""".as[Int])
  }

  def initTransDbOnePair = {
    db.run(sql"""
      insert into "TRADE_WALLETS" ("ID","ID_ACCOUNT_REF","COLOR","CREATE_AT")
      values ('00000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000001', '#FF0000', now());
      insert into "TRANSACTIONS" ("ID", "ID_ORDER", "ID_ACCOUNT_REF", "TRADE_TYPE", "SIDE", "PRICE", "SIZE_IN_BASE", "ID_CURRENCY_BASE", "ID_CURRENCY_COUNTER", "CREATED_AT") values (default, '00000000-0000-0000-000A-000000000000', '00000000-0000-0000-0000-000000000001', 'FILL'::trade, 'BUY'::side, '1.0', '1.0', 'BTC'::currency, 'JPY'::currency, now());
      insert into "TRANSACTIONS" ("ID", "ID_ORDER", "ID_ACCOUNT_REF", "TRADE_TYPE", "SIDE", "PRICE", "SIZE_IN_BASE", "ID_CURRENCY_BASE", "ID_CURRENCY_COUNTER", "CREATED_AT") values (default, '00000000-0000-0000-000A-000000000000', '00000000-0000-0000-0000-000000000001', 'FILL'::trade, 'SELL'::side, '1.0', '1.0', 'BTC'::currency, 'JPY'::currency, now());""".as[Int])
  }

  def initTransDbOnePair24Hour = {
    db.run(sql"""
      insert into "TRADE_WALLETS" ("ID","ID_ACCOUNT_REF","COLOR","CREATE_AT")
      values ('00000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000001', '#FF0000', now());
      insert into "TRANSACTIONS" ("ID", "ID_ORDER", "ID_ACCOUNT_REF", "TRADE_TYPE", "SIDE", "PRICE", "SIZE_IN_BASE", "ID_CURRENCY_BASE", "ID_CURRENCY_COUNTER", "CREATED_AT") values (default, '00000000-0000-0000-000A-000000000000', '00000000-0000-0000-0000-000000000001', 'FILL'::trade, 'BUY'::side, '2.0', '1.0', 'BTC'::currency, 'JPY'::currency, now() - interval '25 hour');
      insert into "TRANSACTIONS" ("ID", "ID_ORDER", "ID_ACCOUNT_REF", "TRADE_TYPE", "SIDE", "PRICE", "SIZE_IN_BASE", "ID_CURRENCY_BASE", "ID_CURRENCY_COUNTER", "CREATED_AT") values (default, '00000000-0000-0000-000A-000000000000', '00000000-0000-0000-0000-000000000001', 'FILL'::trade, 'SELL'::side, '2.0', '1.0', 'BTC'::currency, 'JPY'::currency, now() - interval '25 hour');
      insert into "TRANSACTIONS" ("ID", "ID_ORDER", "ID_ACCOUNT_REF", "TRADE_TYPE", "SIDE", "PRICE", "SIZE_IN_BASE", "ID_CURRENCY_BASE", "ID_CURRENCY_COUNTER", "CREATED_AT") values (default, '00000000-0000-0000-000A-000000000000', '00000000-0000-0000-0000-000000000001', 'FILL'::trade, 'BUY'::side, '1.0', '1.0', 'BTC'::currency, 'JPY'::currency, now());
      insert into "TRANSACTIONS" ("ID", "ID_ORDER", "ID_ACCOUNT_REF", "TRADE_TYPE", "SIDE", "PRICE", "SIZE_IN_BASE", "ID_CURRENCY_BASE", "ID_CURRENCY_COUNTER", "CREATED_AT") values (default, '00000000-0000-0000-000A-000000000000', '00000000-0000-0000-0000-000000000001', 'FILL'::trade, 'SELL'::side, '1.0', '1.0', 'BTC'::currency, 'JPY'::currency, now());""".as[Int])
  }

  def getBalance(idAccount: UUID, code: String) = {
    implicit object SetUUID extends slick.jdbc.SetParameter[UUID] {
      def apply(v: UUID, pp: slick.jdbc.PositionedParameters): Unit = {
        pp.setObject(v, java.sql.JDBCType.BINARY.getVendorTypeNumber)
      }
    }

    db.run(sql"""
      select "BALANCE" from "BALANCE"
      where "ID_ACCOUNT_REF" = $idAccount
      and "ID_CURRENCY" = $code::currency;""".as[BigDecimal])
  }

  def getTransactionHistory(idOrder: UUID) = {
    implicit object SetUUID extends slick.jdbc.SetParameter[UUID] {
      def apply(v: UUID, pp: slick.jdbc.PositionedParameters): Unit = {
        pp.setObject(v, java.sql.JDBCType.BINARY.getVendorTypeNumber)
      }
    }

    db.run(sql"""
      select
        "ID_ACCOUNT_REF",
        "TRADE_TYPE",
        "SIDE",
        "PRICE",
        "SIZE_IN_BASE",
        "ID_CURRENCY_BASE",
        "ID_CURRENCY_COUNTER"
      from "TRANSACTIONS"
      where "ID_ORDER" = $idOrder;""".as[(String, String, String, BigDecimal, BigDecimal, String, String)])
  }

  def getFundHistory = {

    db.run(sql"""
      select
        "ID_ACCOUNT_REF",
        "ID_CURRENCY",
        "PAYMENT_TYPE",
        "AMOUNT"
      from "FUNDS_HISTORY"
      order by "ID" desc limit 1;""".as[(String, String, String, BigDecimal)])
  }

  def deductBalance(
    idAccount: UUID,
    currencyBase: String,
    currencyCounter: String,
    side: String,
    price: BigDecimal,
    size: BigDecimal,
    idOrder: UUID): Future[Boolean] = {
    db.run(
      sql"""select "CFN_DEDUCT_BALANCE"(
        $idAccount,
        $currencyBase,
        $currencyCounter,
        $side,
        $price,
        $size,
        $idOrder);""".as[Boolean].head)
  }

  def restoreBalance(
    idAccount: UUID,
    currencyBase: String,
    currencyCounter: String,
    side: String,
    price: BigDecimal,
    size: BigDecimal,
    idOrder: UUID): Future[Boolean] = {
    db.run(
      sql"""select "CFN_RESTORE_BALANCE"(
        $idAccount,
        $currencyBase,
        $currencyCounter,
        $side,
        $price,
        $size,
        $idOrder);""".as[Boolean].head)
  }

  def deposit(
    idAccount: UUID,
    idCurrency: String,
    amount: BigDecimal): Future[Boolean] = {
    db.run(
      sql"""select "CFN_DEPOSIT"(
        $idAccount,
        $idCurrency,
        $amount);""".as[Boolean].head)
  }

  def withdraw(
    idAccount: UUID,
    idCurrency: String,
    amount: BigDecimal): Future[Boolean] = {
    db.run(
      sql"""select "CFN_WITHDRAW"(
        $idAccount,
        $idCurrency,
        $amount);""".as[Boolean].head)
  }

  def getPairList(data: String) = {
    db.run(
      sql"""select
        "CURRENCY_BASE",
        "CURRENCY_COUNTER",
        "LATEST_PRICE",
        "VOLUME_24HR",
        "CHANGE_24HR" from "CFN_GET_PAIR_LIST"(
        $data);""".as[(String, String, BigDecimal, BigDecimal, BigDecimal)])
  }
}
