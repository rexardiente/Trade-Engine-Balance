package actors

import javax.inject._
import java.{util, time}
import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.duration._
import akka.actor._
import akka.pattern.ask
import play.api.Logger
import play.api.libs.json._
import mrtradelibrary.models.util._
import mrtradelibrary.models.domain._
import AccountServiceActor._
import AccountManagerActor._
import TradeEngineAccessor._
import mrtradelibrary.utils.Transaction
import mrtradelibrary.models.util.Clients._
import mrtradelibrary.models.util.CacheUsers

object AccountManagerActor {
  def props[T <: ActorRef](out: T, accServiceActor: T) =
   Props(classOf[AccountManagerActor], out, accServiceActor)
}

@Singleton
class AccountManagerActor(out: ActorRef, accServiceActor: ActorRef)
  extends Actor with CacheUsers {
  override def preStart() = {
    super.preStart
    accServiceActor ! "Starting Mr Balance"
  }

  def requestValidation(request: Option[RequestValidation]) =
    request match {
      case Some(order: Order) =>
        accServiceActor ! order

      case Some(cancelOrder: CancelOrder) =>
        accServiceActor ! cancelOrder

      case Some(balance: Balance) =>
        accServiceActor ! balance

      case Some(getBalance: GetBalance) =>
        accServiceActor ! getBalance

      case Some(funds: Funds) =>
        accServiceActor ! funds

      case Some(history: RequestFundsHistory) =>
        accServiceActor ! history

      case Some(openOrders: OpenOrders) =>
        accServiceActor ! openOrders

      case Some(history: TransactionHistory) =>
        accServiceActor ! history

      case other =>
        println("Invalid request.")
    }

  override def receive: Receive = {
    case json: JsValue =>
      try {
        saveUser(json, out)
      } finally {
        requestValidation(json)
      }

    case other =>
      out ! Json.obj("other" -> other.toString)
  }
}

