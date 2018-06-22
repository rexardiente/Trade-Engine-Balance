package actors

import javax.inject._
import java.util.UUID
import akka.actor._
import play.api.Logger
import play.api.libs.json._
import com.typesafe.config.ConfigFactory
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import cats.implicits._
import mrtradelibrary.models.domain._
import mrtradelibrary.models.repo._
import models.service._
import mrtradelibrary.models.util._


object TradeEngineAccessor {
  def props = Props[TradeEngineAccessor]
}

@Singleton
class TradeEngineAccessor @Inject()(
    TransactionsRepo: TransactionsRepo,
    currencyNetworkRepo: CurrencyNetworkRepo,
    orderService: OrderService
  ) extends Actor {
  import TradeEngineAccessor._

  val conf = ConfigFactory.load()
  val tradeEngineURL = conf.getString("tradeEngine.url")

  override def preStart() = {
    super.preStart
    context.actorSelection(tradeEngineURL) ! "Connection from Balance"
  }

  override def receive: Receive = {
    case notify: Notification =>
      context.actorSelection(tradeEngineURL) ! notify

    case other =>
      println(other)
  }
}
