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

class SqlFunctionSpec extends PlaySpec
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

  "CFN_DEDUCT_BALANCE.sql" should {
    "return false if the balance is not enough" in {
      whenReady(sqlExecutor.initDb) { r =>
        val idAccount = UUID.fromString("00000000-0000-0000-0000-000000000001")
        //buy BTC using JPY
        whenReady(sqlExecutor.deductBalance(
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
    "deduct the balance when buying" in {
      whenReady(sqlExecutor.initDb) { r =>
        val idAccount = UUID.fromString("00000000-0000-0000-0000-000000000001")
        //buy BTC using JPY
        whenReady(sqlExecutor.deductBalance(
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

  it should {
    "deduct the balance when selling" in {
      whenReady(sqlExecutor.initDb) { r =>
        val idAccount = UUID.fromString("00000000-0000-0000-0000-000000000001")
        //sell BTC for JPY
        whenReady(sqlExecutor.deductBalance(
          idAccount,
          "BTC",
          "JPY",
          "SELL",
          BigDecimal("10"),
          BigDecimal("1"),
          idOrders(0))) { res =>
          assert(res == true)

          whenReady(sqlExecutor.getBalance(idAccount, "JPY")) { balance =>
            assert(balance(0) == BigDecimal("100"))
          }

          whenReady(sqlExecutor.getBalance(idAccount, "BTC")) { balance =>
            assert(balance(0) == BigDecimal("9"))
          }

          whenReady(sqlExecutor.getTransactionHistory(idOrders(0))) { history =>
            assert(UUID.fromString(history(0)._1) == idAccount)
            assert(history(0)._2 == "CREATE")
            assert(history(0)._3 == "SELL")
            assert(history(0)._4 == BigDecimal("10"))
            assert(history(0)._5 == BigDecimal("1"))
            assert(history(0)._6 == "BTC")
            assert(history(0)._7 == "JPY")
          }
        }
      }
    }
  }

  "CFN_DEPOSIT.sql" should {
    "return false if the account is not existed in database" in {
      whenReady(sqlExecutor.initDb) { r =>
        val idAccount = UUID.fromString("00000000-0000-0000-0000-000000000ABC")
        whenReady(sqlExecutor.deposit(
          idAccount,
          "BTC",
          BigDecimal("100"))) { res =>
          assert(res == false)
        }
      }
    }
  }

  it should {
    "insert a record when currency entry is not existed in BALANCE table" in {
      whenReady(sqlExecutor.initDb) { r =>
        val idAccount = UUID.fromString("00000000-0000-0000-0000-000000000001")
        whenReady(sqlExecutor.deposit(
          idAccount,
          "LTC",
          BigDecimal("100"))) { res =>
          assert(res == true)

          whenReady(sqlExecutor.getBalance(idAccount, "LTC")) { balance =>
            assert(balance(0) == BigDecimal("100"))
          }

          whenReady(sqlExecutor.getFundHistory) { history =>
            assert(UUID.fromString(history(0)._1) == idAccount)
            assert(history(0)._2 == "LTC")
            assert(history(0)._3 == "DEPOSIT")
            assert(history(0)._4 == BigDecimal("100"))
          }
        }
      }
    }
  }

  it should {
    "deposit when currency entry is existed in BALANCE table" in {
      whenReady(sqlExecutor.initDb) { r =>
        val idAccount = UUID.fromString("00000000-0000-0000-0000-000000000001")
        whenReady(sqlExecutor.deposit(
          idAccount,
          "BTC",
          BigDecimal("100"))) { res =>
          assert(res == true)

          whenReady(sqlExecutor.getBalance(idAccount, "BTC")) { balance =>
            assert(balance(0) == BigDecimal("110"))
          }

          whenReady(sqlExecutor.getFundHistory) { history =>
            assert(UUID.fromString(history(0)._1) == idAccount)
            assert(history(0)._2 == "BTC")
            assert(history(0)._3 == "DEPOSIT")
            assert(history(0)._4 == BigDecimal("100"))
          }
        }
      }
    }
  }

  "CFN_WITHDRAW.sql" should {
    "return false if the account is not existed in database" in {
      whenReady(sqlExecutor.initDb) { r =>
        val idAccount = UUID.fromString("00000000-0000-0000-0000-000000000ABC")
        whenReady(sqlExecutor.withdraw(
          idAccount,
          "BTC",
          BigDecimal("100"))) { res =>
          assert(res == false)
        }
      }
    }
  }

  it should {
    "return false if the balance is not enough" in {
      whenReady(sqlExecutor.initDb) { r =>
        val idAccount = UUID.fromString("00000000-0000-0000-0000-000000000001")
        whenReady(sqlExecutor.withdraw(
          idAccount,
          "BTC",
          BigDecimal("100"))) { res =>
          assert(res == false)
        }
      }
    }
  }

  it should {
    "return false when currency entry is not existed in BALANCE table" in {
      whenReady(sqlExecutor.initDb) { r =>
        val idAccount = UUID.fromString("00000000-0000-0000-0000-000000000001")
        whenReady(sqlExecutor.withdraw(
          idAccount,
          "LTC",
          BigDecimal("100"))) { res =>
          assert(res == false)
        }
      }
    }
  }

  it should {
    "withdraw when currency entry is existed in BALANCE table" in {
      whenReady(sqlExecutor.initDb) { r =>
        val idAccount = UUID.fromString("00000000-0000-0000-0000-000000000001")
        whenReady(sqlExecutor.withdraw(
          idAccount,
          "BTC",
          BigDecimal("6"))) { res =>
          assert(res == true)

          whenReady(sqlExecutor.getBalance(idAccount, "BTC")) { balance =>
            assert(balance(0) == BigDecimal("4"))
          }

          whenReady(sqlExecutor.getFundHistory) { history =>
            assert(UUID.fromString(history(0)._1) == idAccount)
            assert(history(0)._2 == "BTC")
            assert(history(0)._3 == "WITHDRAW")
            assert(history(0)._4 == BigDecimal("6"))
          }
        }
      }
    }
  }

  "CFN_RESTORE_BALANCE.sql" should {
    "return false if the account is not existed in database" in {
      whenReady(sqlExecutor.initDb) { r =>
        val idAccount = UUID.fromString("00000000-0000-0000-0000-000000000ABC")

        whenReady(sqlExecutor.restoreBalance(
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
    "return false when currency entry is not existed in BALANCE table (buy)" in {
      whenReady(sqlExecutor.initDb) { r =>
        val idAccount = UUID.fromString("00000000-0000-0000-0000-000000000001")

        whenReady(sqlExecutor.restoreBalance(
          idAccount,
          "BTC",
          "LTC",
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
    "return false when currency entry is not existed in BALANCE table (sell)" in {
      whenReady(sqlExecutor.initDb) { r =>
        val idAccount = UUID.fromString("00000000-0000-0000-0000-000000000001")

        whenReady(sqlExecutor.restoreBalance(
          idAccount,
          "LTC",
          "JPY",
          "sell",
          BigDecimal("100"),
          BigDecimal("20"),
          idOrders(0))) { res =>
          assert(res == false)
        }
      }
    }
  }

  it should {
    "restore the balance when buying" in {
      whenReady(sqlExecutor.initDb) { r =>
        val idAccount = UUID.fromString("00000000-0000-0000-0000-000000000001")
        //buy BTC using JPY
        whenReady(sqlExecutor.restoreBalance(
          idAccount,
          "BTC",
          "JPY",
          "BUY",
          BigDecimal("10"),
          BigDecimal("2"),
          idOrders(0))) { res =>
          assert(res == true)

          whenReady(sqlExecutor.getBalance(idAccount, "JPY")) { balance =>
            assert(balance(0) == BigDecimal("120"))
          }

          whenReady(sqlExecutor.getBalance(idAccount, "BTC")) { balance =>
            assert(balance(0) == BigDecimal("10"))
          }

          whenReady(sqlExecutor.getTransactionHistory(idOrders(0))) { history =>
            assert(UUID.fromString(history(0)._1) == idAccount)
            assert(history(0)._2 == "CANCEL")
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

  it should {
    "restore the balance when selling" in {
      whenReady(sqlExecutor.initDb) { r =>
        val idAccount = UUID.fromString("00000000-0000-0000-0000-000000000001")
        //sell BTC for JPY
        whenReady(sqlExecutor.restoreBalance(
          idAccount,
          "BTC",
          "JPY",
          "SELL",
          BigDecimal("10"),
          BigDecimal("1"),
          idOrders(0))) { res =>
          assert(res == true)

          whenReady(sqlExecutor.getBalance(idAccount, "JPY")) { balance =>
            assert(balance(0) == BigDecimal("100"))
          }

          whenReady(sqlExecutor.getBalance(idAccount, "BTC")) { balance =>
            assert(balance(0) == BigDecimal("11"))
          }

          whenReady(sqlExecutor.getTransactionHistory(idOrders(0))) { history =>
            assert(UUID.fromString(history(0)._1) == idAccount)
            assert(history(0)._2 == "CANCEL")
            assert(history(0)._3 == "SELL")
            assert(history(0)._4 == BigDecimal("10"))
            assert(history(0)._5 == BigDecimal("1"))
            assert(history(0)._6 == "BTC")
            assert(history(0)._7 == "JPY")
          }
        }
      }
    }
  }

  "CFN_GET_PAIR_LIST.sql" should {
    "get pair list (1 pair)" in {
      whenReady(sqlExecutor.initTransDbOnePair) { r =>
        val pair = """[
          {"code_currency_base":"BTC", "code_currency_counter":"JPY"}
        ]"""

        whenReady(sqlExecutor.getPairList(pair)) { res =>
          assert(res(0)._1 == "BTC")
          assert(res(0)._2 == "JPY")
          assert(res(0)._3 == BigDecimal("1.0"))
          assert(res(0)._4 == BigDecimal("1.0"))
          assert(res(0)._5 == BigDecimal("1.0"))//?
        }
      }
    }
  }

  it should {
    "get pair list (1 pair) with 24 hour changes" in {
      whenReady(sqlExecutor.initTransDbOnePair24Hour) { r =>
        val pair = """[
          {"code_currency_base":"BTC", "code_currency_counter":"JPY"}
        ]"""

        whenReady(sqlExecutor.getPairList(pair)) { res =>
          assert(res(0)._1 == "BTC")
          assert(res(0)._2 == "JPY")
          assert(res(0)._3 == BigDecimal("1.0"))
          assert(res(0)._4 == BigDecimal("1.0"))
          assert(res(0)._5 == BigDecimal("-0.5"))//?
        }
      }
    }
  }
}
