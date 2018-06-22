package actors

import javax.inject.{ Inject, Singleton, Named }
import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import com.typesafe.config.ConfigFactory
import akka.actor._
import play.api.Logger
import play.api.libs.json._
import models.service._
import models.service.OrderService
import mrtradelibrary.models.domain._
import mrtradelibrary.models.repo._
import mrtradelibrary.models.util._
import mrtradelibrary.models.util.Clients._
import mrtradelibrary.utils.Transaction._
import mrtradelibrary.utils._
import AccountServiceActor._
import AccountManagerActor._

object AccountServiceActor {
  def props = Props[AccountServiceActor]
}

@Singleton
class AccountServiceActor @Inject()(
  orderService: OrderService,
  fundService: FundService,
  balanceRepo: BalanceRepo,
  transactionsRepo: TransactionsRepo,
  fundsHistoryRepo: FundsHistoryRepo,
  @Named("TradeEngineAccessor") tradeEngineAccessor: ActorRef) extends Actor {
  override def preStart(): Unit = {
    super.preStart()
  }

  def sellToggle[S <: Sides.Value](ss: S): Boolean = {
    ss match {
      case Sides.Buy => false
      case Sides.Sell => true
    }
  }

  def discerningFundType[D <: Boolean](dd: D): Transaction.Value = {
    dd match {
      case true => deposit
      case false => withdraw
    }
  }

  def receive = {
    case json: JsValue =>
      Logger.info("RECEIVED JSON")

    case order: Order => {
      val sell: Boolean = sellToggle(order.side)
      val idOrder: UUID = UUID.randomUUID()

      orderService
        .deductBalance(
          order.idAccountRef,
          order.currencyBase.toString,
          order.currencyCounter.toString,
          order.side.toString,
          order.price,
          order.amount,
          idOrder)
          .map { r =>
            orders
              .find(_._1 == order.idAccountRef)
              .map { u =>
                  val orderMessage = Transaction.order.toString
                  val status = TransactionMessage.failOrSuccess(r)
                  val message =
                    Json.toJson(Message(orderMessage,
                    Response(status, TransactionMessage.description(orderMessage, status))))

                tradeEngineAccessor ! Notification(order.command.toString, order.idAccountRef, u._2, Some(r))
                u._2 ! message
                orders -= order.idAccountRef
              }
          }
    }

    case order: CancelOrder => {
      val sell: Boolean = sellToggle(order.side)

      orderService
        .insertCancel(
          order.idAccountRef,
          order.currencyBase.toString,
          order.currencyCounter.toString,
          order.side.toString,
          order.price,
          order.amount,
          order.idOrder)
          .map { r =>
            cancel_orders
              .find(_._1 == order.idAccountRef)
              .map { u =>
                tradeEngineAccessor ! Notification(order.command.toString, order.idAccountRef, u._2, Some(r))
                cancel_orders -= order.idAccountRef
              }
          }
    }

    case order: OpenOrders =>
      orderService
        .openOrders(order)
        .map { r => // Use Simple responder to user and change it in the future...
          open_orders
            .find(_._1 == order.idAccountRef)
            .map { u =>
              u._2 ! MessageResponse(Transaction.openOrders, r).toJson
              open_orders -= order.idAccountRef
            }
        }

    case fund: Funds =>
      val fundType = discerningFundType(fund.isDeposit).toString

      fundService
        .withdrawOrDepositFunds(
          fund.idAccountRef,
          fund.codeCurrency.toString,
          fund.amount,
          fund.isDeposit).map { r =>
          fundsList
            .find(_._1 == fund.idAccountRef)
            .map { u =>
              val status = TransactionMessage.failOrSuccess(r)
              val message =
                Json.toJson(Message(fundType,
                Response(status, TransactionMessage.description(fundType, status))))

              u._2 ! message
              fundsList -= fund.idAccountRef
            }
      }

    case getBalance: GetBalance =>
      balanceRepo
        .getAccountBalances(getBalance.idAccountRef)
        .map { r =>
          get_balance
            .find(_._1 == getBalance.idAccountRef)
            .map { u =>
              u._2 ! MessageResponse(Transaction.getBalance, r).toJson
              get_balance -= getBalance.idAccountRef
            }
        }

    case history: TransactionHistory =>
      transactionsRepo
        .getTransactionHistory(history)
        .map { r =>
          transaction_history
            .find(_._1 == history.idAccountRef)
            .map { u =>
              u._2 ! MessageResponse(Transaction.transactionHistory, r).toJson
              transaction_history -= history.idAccountRef
            }
        }

    case history: RequestFundsHistory =>
      fundsHistoryRepo
        .getFundsHistory(history)
        .map { r =>
          funds_history
            .find(_._1 == history.idAccountRef)
            .map { u =>
              u._2 ! MessageResponse(Transaction.fundsHistory, r).toJson
              funds_history -= history.idAccountRef
            }
        }

    case other =>
      Logger.info(s"AccountServiceActor: $other")
  }
}
