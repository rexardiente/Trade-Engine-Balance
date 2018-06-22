package controllers

import javax.inject._
import java.util.UUID
import java.time.Instant
import scala.concurrent.{ Future, ExecutionContext }
import scala.concurrent.ExecutionContext.Implicits.global._
import akka.actor._
import akka.stream._
import play.api.mvc._
import play.api.mvc.WebSocket._
import play.api.libs.json._
import play.api.libs.streams._
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.format.Formats._
import play.api.i18n._
import cats.implicits._
import errors._
import actors._
import mrtradelibrary.models.domain.TradeWallet

@Singleton
class HomeController @Inject()(
    implicit system: ActorSystem,
    tradeWalletRepo: mrtradelibrary.models.repo.TradeWalletRepo,
    tradeWalletService: models.service.TradeWalletService,
    currencyNetworkService: models.service.CurrencyNetworkService,
    val messagesApi: MessagesApi,
    implicit val materializer: Materializer,
    implicit val ec: ExecutionContext,
    implicit val configuration: play.api.Configuration,
    @Named("AccountServiceActor") accServiceActor: ActorRef)
  extends Controller with I18nSupport {
    implicit val messageFlowTransformer =
      MessageFlowTransformer.jsonMessageFlowTransformer[JsValue, JsValue]

  private val loginForm = Form(single(
    "account_id" -> nonEmptyText
  ))

  def socket = WebSocket.accept[JsValue, JsValue] { implicit request =>
    ActorFlow.actorRef(out =>
      AccountManagerActor.props(out, accServiceActor))
  }

  def demo = Action {
    Ok(views.html.demo())
  }

  def getCurrencies = Action.async { implicit request =>
    currencyNetworkService
      .getCurrencies[JsValue]
      .map(count => Ok(Json.obj("currencies" -> count)))
  }

  def login = Action.async { implicit request =>
    loginForm.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest(formWithErrors.errorsAsJson)),
      server =>
      try {
        val id = UUID.fromString(server) // Check if valid UUID...
        tradeWalletService
          .addTradeWallet(TradeWallet(UUID.randomUUID, id, "#FF0000", Instant.now))
          .fold(ServiceErrorHandler.json(_), _ => Created)
      } catch {
        case _: Exception => Future.successful(BadRequest)
      })
  }
}
