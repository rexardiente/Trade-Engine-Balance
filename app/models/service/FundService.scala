package models.service

import java.util.UUID
import javax.inject.{ Inject, Singleton }
import scala.concurrent.Future
import play.api.libs.json._
import play.api.db.slick.{ DatabaseConfigProvider, HasDatabaseConfigProvider }
import slick.jdbc.GetResult

@Singleton
class FundService @Inject()(
    protected val dbConfigProvider: DatabaseConfigProvider
  ) extends HasDatabaseConfigProvider[mrtradelibrary.utils.db.PostgresDriver] {
  import profile.api._

  def withdrawOrDepositFunds(
    idAccountRef: UUID,
    idCurrency: String,
    amount: BigDecimal,
    isDeposit: Boolean): Future[Boolean] = {
    isDeposit match {
    case true =>
      db.run(
        sql"""select "CFN_DEPOSIT"(
          $idAccountRef,
          $idCurrency,
          $amount);"""
        .as[Boolean].head)
    case false =>
      db.run(
        sql"""select "CFN_WITHDRAW"(
          $idAccountRef,
          $idCurrency,
          $amount);"""
        .as[Boolean].head)
    }
  }
}
