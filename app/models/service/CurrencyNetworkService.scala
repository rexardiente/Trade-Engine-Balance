package models.service

import java.util.UUID
import java.time.Instant
import javax.inject.{ Inject, Singleton }
import cats._
import cats.data.{ EitherT, OptionT }
import cats.instances._
import cats.implicits._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import play.api.db.slick.{ DatabaseConfigProvider, HasDatabaseConfigProvider }
import play.api.libs.ws._
import play.api.libs.json._
import errors._
import mrtradelibrary.models.domain._
import mrtradelibrary.models.repo._

@Singleton
class CurrencyNetworkService @Inject()(
    currencyNetworkRepo: CurrencyNetworkRepo,
    ws: WSClient,
    protected val dbConfigProvider: DatabaseConfigProvider
  ) extends HasDatabaseConfigProvider[mrtradelibrary.utils.db.PostgresDriver] {
  import profile.api._

  def addCurrencyNetwork[CN <: CurrencyNetwork](cn: CN): EitherT[Future, MrError, Unit] = {
    for {
      - <- EitherT[Future, MrError, Unit] {
        currencyNetworkRepo
          .exists(cn.id)
          .map(exists => if (exists) Left(ObjectConflicts) else Right(()))
      }

      - <- EitherT[Future, MrError, Unit] {
        currencyNetworkRepo
          .add(cn)
          .map(count => if (count > 0) Right(()) else Left(UnknownError))
      }
    } yield()
  }

  def getCurrencies[J >: JsValue]: Future[Seq[J]] = {
    (for {
      currencies <- currencyNetworkRepo.all

      codes <- Future.successful {
          currencies.map(r => Json.obj("code" -> r.id, "name" -> r.name))
        }
    } yield(codes))
  }
}
