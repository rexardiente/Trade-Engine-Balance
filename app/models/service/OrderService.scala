package models.service

import java.util.UUID
import javax.inject.{ Inject, Singleton }
import scala.concurrent.Future
import play.api.libs.json._
import play.api.db.slick.{ DatabaseConfigProvider, HasDatabaseConfigProvider }
import slick.jdbc.GetResult
import cats.data.{ OptionT, EitherT }
import mrtradelibrary.models.domain._
import mrtradelibrary.models.repo._
import mrtradelibrary.models.util._

@Singleton
class OrderService @Inject()(
    transactionsRepo: TransactionsRepo,
    currencyNetworkRepo: CurrencyNetworkRepo,
    protected val dbConfigProvider: DatabaseConfigProvider
  ) extends HasDatabaseConfigProvider[mrtradelibrary.utils.db.PostgresDriver] {
  import profile.api._

  implicit val getTransactionsResult = {
        GetResult(r =>
          Transactions(
            r.nextInt,
            r.nextUUID,
            r.nextUUID,
            r.nextTrade,
            r.nextSide,
            r.nextBigDecimal,
            r.nextBigDecimal,
            r.nextCurrency,
            r.nextCurrency,
            r.nextInstant
        ))
  }

  def deductBalance(
    idAccount: UUID,
    currencyBase: String,
    currencyCounter: String,
    side: String,
    price: BigDecimal,
    size: BigDecimal,
    idOrder: UUID):
    Future[Boolean] = {
    db.run(
      sql"""select "CFN_DEDUCT_BALANCE"(
        $idAccount,
        $currencyBase,
        $currencyCounter,
        $side,
        $price,
        $size,
        $idOrder);"""
      .as[Boolean].head)
  }

  def insertCancel(
    idAccount: UUID,
    currencyBase: String,
    currencyCounter: String,
    side: String,
    price: BigDecimal,
    size: BigDecimal,
    idOrder: UUID):
    Future[Boolean] = {
    db.run(
      sql"""select "CFN_INSERT_CANCEL"(
        $idAccount,
        $currencyBase,
        $currencyCounter,
        $side,
        $price,
        $size,
        $idOrder);"""
      .as[Boolean].head)
  }

  def openOrders[O <: OpenOrders, T >: Transactions](order: O): Future[Seq[T]] = {
    transactionsRepo.openOrders(order)
  }

  def updateFunds(orderResults: JsValue): Future[Boolean] = {
    val res = orderResults.toString
    db.run(sql"""select "CFN_UPDATE_FUNDS"($res);""".as[Boolean].head)
  }
}
