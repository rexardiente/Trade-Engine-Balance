package models.service

import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.inject.bind
import play.api.Application
import java.util.UUID
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play._
import org.scalatest._
import play.api.db.evolutions._

class OrderServiceSpec extends PlaySpec
  with OneAppPerSuite
  with ScalaFutures
  with BeforeAndAfterEach
  with BeforeAndAfterAll {

  val idOrders = List(UUID.randomUUID, UUID.randomUUID)

  implicit override lazy val app = new GuiceApplicationBuilder().
    configure(
      "slick.dbs.default.profile" -> "mrtradelibrary.utils.db.PostgresDriver$",
      "slick.dbs.default.db.driver" -> "org.postgresql.Driver",
      "slick.dbs.default.db.url" -> "jdbc:postgresql://127.0.0.1/mr-balance",
      "slick.dbs.default.db.user" -> "mr-balance",
      "slick.dbs.default.db.password" -> "",
      "slick.dbs.default.db.connectionTimeout" -> "30 seconds",
      "slick.dbs.default.db.keepAliveConnection" -> true).build

  def orderService(implicit app: Application): OrderService = Application.instanceCache[OrderService].apply(app)

  def sqlExecutor(implicit app: Application): models.SqlExecutor = Application.instanceCache[models.SqlExecutor].apply(app)

  override def beforeEach() {
    whenReady(sqlExecutor.truncate) { res =>
      println("cleanup")
    }
  }

  // override def afterEach() {
  //   whenReady(sqlExecutor.truncate) { res =>
  //     println("cleanup")
  //   }
  // }

  override def beforeAll() = {
    OfflineEvolutions.applyScript(
      new java.io.File("."),
      this.getClass.getClassLoader,
      app.injector.instanceOf[play.api.db.DBApi],
      "default",
      true
    )
  }

  "OrderService" should {
    "return false if the balance is not enough" in {
      whenReady(sqlExecutor.initDb) { r =>
        val idAccount = UUID.fromString("00000000-0000-0000-0000-000000000001")
        //buy BTC using JPY
        whenReady(orderService.deductBalance(
          idAccount,
          "BTC",
          "JPY",
          "buy",
          BigDecimal("100"),
          BigDecimal("20"),
          idOrders(0))) { res =>
          assert(res == false)
        }
      }
    }
  }

  it should {
    "deduct the balance" in {
      whenReady(sqlExecutor.initDb) { r =>
        val idAccount = UUID.fromString("00000000-0000-0000-0000-000000000001")
        //buy BTC using JPY
        whenReady(orderService.deductBalance(
          idAccount,
          "BTC",
          "JPY",
          "BUY",
          BigDecimal("10"),
          BigDecimal("2"),
          idOrders(0))) { res =>
          assert(res == true)

          whenReady(sqlExecutor.getBalance(idAccount, "JPY")) { balance =>
            assert(balance(0) == BigDecimal("80"))
          }

          whenReady(sqlExecutor.getBalance(idAccount, "BTC")) { balance =>
            assert(balance(0) == BigDecimal("10"))
          }

          whenReady(sqlExecutor.getTransactionHistory(idOrders(0))) { history =>
            assert(UUID.fromString(history(0)._1) == idAccount)
            assert(history(0)._2 == "CREATE")
            assert(history(0)._3 == "BUY")
            assert(history(0)._4 == BigDecimal("10"))
            assert(history(0)._5 == BigDecimal("2"))
            assert(history(0)._6 == "BTC")
            assert(history(0)._7 == "JPY")
          }
        }
      }
    }
  }
}
