package models.service

import javax.inject.{ Inject, Singleton }
import java.util.UUID
import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import cats.data.{ EitherT, OptionT }
import cats.implicits._
import play.api.db.slick.{ DatabaseConfigProvider, HasDatabaseConfigProvider }
import mrtradelibrary.models.domain._
import mrtradelibrary.models.repo._
import errors._

@Singleton
class TradeWalletService @Inject()(
    tradeWalletRepo: TradeWalletRepo,
    protected val dbConfigProvider: DatabaseConfigProvider
  ) extends HasDatabaseConfigProvider[mrtradelibrary.utils.db.PostgresDriver] {
  import profile.api._

  def addTradeWallet[A <: TradeWallet](wallet: A): EitherT[Future, MrError, Unit] = {
    for {
      - <- EitherT[Future, MrError, Unit] {
        tradeWalletRepo
          .exists(wallet.idAccountRef)
          .map(exists => if (exists) Left(NoContent) else Right(()))
      }

      - <- EitherT[Future, MrError, Unit] {
        tradeWalletRepo
          .add(wallet)
          .map(count => if (count > 0) Right(()) else Left(UnknownError))
      }
    } yield()
  }

  def updateAccount[U <: TradeWallet](update: U): EitherT[Future, MrError, Unit] = {
    for {
      - <- EitherT[Future, MrError, Unit] {
        tradeWalletRepo
          .exists(update.idAccountRef)
          .map(exists => if (exists) Right(()) else Left(ObjectConflicts))
      }

      - <- EitherT[Future, MrError, Unit] {
        tradeWalletRepo
          .update(update)
          .map(count => if (count > 0) Right(()) else Left(UnknownError))
      }
    } yield ()
  }

  def deleteAccount[U <: UUID](id: U): EitherT[Future, MrError, Int] = {
    for {
      - <- EitherT[Future, MrError, Unit] {
        tradeWalletRepo
          .exists(id)
          .map(exists => if (exists) Right(()) else Left(ObjectConflicts))
      }

      count <- EitherT[Future, MrError, Int] {
        tradeWalletRepo
          .delete(id)
          .map(count => if (count > 0) Right(count) else Left(UnknownError))
      }
    } yield(count)
  }
}

