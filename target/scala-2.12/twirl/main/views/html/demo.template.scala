
package views.html

import _root_.play.twirl.api.TwirlFeatureImports._
import _root_.play.twirl.api.TwirlHelperImports._
import _root_.play.twirl.api.Html
import _root_.play.twirl.api.JavaScript
import _root_.play.twirl.api.Txt
import _root_.play.twirl.api.Xml
import models._
import controllers._
import play.api.i18n._
import views.html._
import play.api.templates.PlayMagic._
import play.api.mvc._
import play.api.data._

object demo extends _root_.play.twirl.api.BaseScalaTemplate[play.twirl.api.HtmlFormat.Appendable,_root_.play.twirl.api.Format[play.twirl.api.HtmlFormat.Appendable]](play.twirl.api.HtmlFormat) with _root_.play.twirl.api.Template1[play.api.Configuration,play.twirl.api.HtmlFormat.Appendable] {

  /**/
  def apply/*1.2*/()(implicit conf: play.api.Configuration):play.twirl.api.HtmlFormat.Appendable = {
    _display_ {
      {


Seq[Any](format.raw/*1.43*/("""
"""),format.raw/*2.1*/("""<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>Mr. Balance Demo Page</title>
  <link href="https://fonts.googleapis.com/css?family=Lato" rel="stylesheet">
  <link rel="stylesheet" type="text/css" href="https://cdnjs.cloudflare.com/ajax/libs/foundation/6.4.3/css/foundation.min.css" media="screen" />
  <link rel="stylesheet" type="text/css" href=""""),_display_(/*9.49*/routes/*9.55*/.Assets.versioned("styles/style.css")),format.raw/*9.92*/("""" media="screen" />
</head>
<body class="grid-container">
  <div id="page-wrapper" class="grid-x align-center grid-margin-x">
    <div id="error-message" class="cell">
      <p id="success"></p>
      <p id="fail"></p>
      <p id="info"></p>
    </div>
    <div id="login-page" class="cell small-6">
      <section id="login" class="custom-box">
        <h1>Login</h1>
        <form id="login-form" name="loginForm">
          <fieldset>
            <div class="form-group grid-x">
              <label for="account_id" class="cell small-3">Account ID:</label>
              <input type="text" name="account_id" id="account_id" required class="cell small-9">
            </div>
            <div class="form-group grid-x align-right">
              <button type="submit" id="login-submit" class="button">Login</button>
            </div>
          </fieldset>
        </form>
      </section>
    </div>
    <div id="inner-page" class="small-12 cell">
      <header class="grid-x align-middle">
        <div class="small-10 cell">
          <ul class="grid-x">
            <li class="menu small-2 cell"><a href="#account" class="active">Account</a></li>
            <li class="menu small-2 cell"><a href="#trade">Trade</a></li>
            <li class="menu small-2 cell"><a href="#open-orders">Open Orders</a></li>
            <li class="menu small-2 cell"><a href="#transaction-history">History</a></li>
          </ul>
        </div>
        <div class="small-2 cell">
          <select class="align-right" name="currency-pair" id="currency-pair">
            """),_display_(/*46.14*/conf/*46.18*/.getStringList("currencyPair").get.map/*46.56*/ { pair =>_display_(Seq[Any](format.raw/*46.66*/("""
              """),format.raw/*47.15*/("""<option value=""""),_display_(/*47.31*/pair),format.raw/*47.35*/("""">"""),_display_(/*47.38*/pair),format.raw/*47.42*/("""</option>
            """)))}),format.raw/*48.14*/("""
          """),format.raw/*49.11*/("""</select>
        </div>
      </header>
      <main class="grid-x">
        <section id="account" class="small-12 cell">
          <div id="current-account-container" class="grid-x">
            <div class="small-12 cell">
              <div class="custom-box">
                <span id="current-account"></span>
                <a id="logout-account" href="javascript:;"><small>Click to Logout</small></a>
              </div>
            </div>
          </div>
          <div id="balance-container" class="grid-x">
            <div class="small-12 cell">
              <h1>Balance</h1>
              <table id="balance-table">
                <thead>
                    <tr>
                      <th>Currency</th><th>Total Balance</th>
                      <th>Available Balance</th><th>Reserved Balance</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr>
                      <td>...</td><td>...</td>
                      <td>...</td><td>...</td>
                    </tr>
                  </tbody>
              </table>
              <a href="javascript:;" id="refresh-balance"><small>Refresh Balance</small></a>
            </div>
          </div>
          <hr/>
          <div class="grid-x grid-margin-x">
            <div id="deposit-container" class="small-6 cell">
              <h1>Deposit</h1>
              <form id="deposit-form" class="custom-box">
                <fieldset>
                  <div class="form-group grid-x">
                    <label for="amount" class="small-3 cell no-padding">Amount:</label>
                    <input type="number" name="amount" id="deposit-amount" required class="small-9 cell">
                  </div>
                  <div class="form-group grid-x">
                    <label for="id_currency" class="small-3 cell no-padding">Currency:</label>
                    <select name="id_currency" id="id_currency" class="small-9 cell">
                      <option value="JPY">JPY</option>
                      <option value="USD">USD</option>
                      <option value="BTC">BTC</option>
                      <option value="XRP">XRP</option>
                      <option value="ETH">ETH</option>
                    </select>
                  </div>
                  <div class="grid-x align-right">
                    <input type="hidden" name="is_deposit" value="true">
                    <button type="submit" id="deposit-submit" class="button">Deposit</button>
                  </div>
                </fieldset>
              </form>
            </div>
            <div id="withdraw-container" class="small-6 cell">
              <h1>Withdraw</h1>
              <form id="withdraw-form" class="custom-box">
                <fieldset>
                  <div class="form-group grid-x">
                    <label for="amount" class="small-3 cell no-padding">Amount:</label>
                    <input type="number" name="amount" id="withdraw-amount" required class="small-9 cell">
                  </div>
                  <div class="form-group grid-x">
                    <label for="id_currency" class="small-3 cell no-padding">Currency:</label>
                    <select name="id_currency" id="id_currency" class="small-9 cell">
                      <option value="JPY">JPY</option>
                      <option value="USD">USD</option>
                      <option value="BTC">BTC</option>
                      <option value="XRP">XRP</option>
                      <option value="ETH">ETH</option>
                    </select>
                  </div>
                  <div class="grid-x align-right">
                    <input type="hidden" name="is_deposit" value="false">
                    <button type="submit" id="withdraw-submit" class="button">Withdraw</button>
                  </div>
                </fieldset>
              </form>
            </div>
          </div>
        </section>

        <section id="trade" class="small-12 cell">
          <div id="pairlist-display" class="grid-x">
            <div class="small-12 cell">
              <h1>Pairlist</h1>
              <table id="pairlist-table">
                <thead>
                  <tr>
                    <th>Currency</th>
                    <th>Price</th>
                    <th>24Hr Change</th>
                    <th>24Hr Volume</th>
                  </tr>
                </thead>
                <tbody>
                  <tr>
                    <td>...</td>
                    <td>...</td>
                    <td>...</td>
                    <td>...</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
          <hr/>
          <div id="orderbook-display-container" class="grid-x grid-margin-x">
            <h1 class="small-12 cell">
              Orderbook -
              <span class="base-currency"></span>/<span class="counter-currency"></span>
            </h1>
            <div class="small-6 cell">
              <h2>Bids</h2>
              <table id="bids-table">
                <thead>
                  <tr>
                    <th>Price (<span class="counter-currency"></span>)</th>
                    <th>Total (<span class="base-currency"></span>)</th>
                    <th>Total (<span class="counter-currency"></span>)</th>
                    <th>Sum (<span class="counter-currency"></span>)</th>
                  </tr>
                </thead>
                <tbody>
                  <tr>
                    <td>...</td><td>...</td><td>...</td><td>...</td>
                  </tr>
                </tbody>
              </table>
            </div>
            <div class="small-6 cell">
              <h2>Asks</h2>
              <table id="asks-table">
                <thead>
                  <tr>
                    <th>Price (<span class="counter-currency"></span>)</th>
                    <th>Total (<span class="base-currency"></span>)</th>
                    <th>Total (<span class="counter-currency"></span>)</th>
                    <th>Sum (<span class="counter-currency"></span>)</th>
                  </tr>
                </thead>
                <tbody>
                  <tr>
                    <td>...</td><td>...</td><td>...</td><td>...</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
          <hr/>
          <div id="place-order-container" class="grid-x grid-margin-x">
            <h1 class="small-12 cell">Place Order</h1>
            <div class="small-6 cell">
              <form id="buy-order-form" name="buy" class="custom-box" autocomplete="off">
                <fieldset>
                  <div class="grid-x">
                    <h2>Buy <span class="base-currency"></span></h2>
                    <input type="hidden" name="side" value="BUY">
                  </div>
                  <div class="form-group grid-x align-middle">
                    <label for="market-buy" class="small-3 cell no-padding">Market Order:</label>
                    <input type="checkbox" name="market" id="market-buy" class="market-order">
                  </div>
                  <div class="form-group grid-x align-middle">
                    <label for="buy-price" class="small-3 cell no-padding">Price:</label>
                    <input
                      type="number"
                      name="price"
                      id="buy-price"
                      step="0.00000001"
                      required
                      class="small-8 cell no-margin price">
                    <span class="small-1 cell counter-currency"></span>
                  </div>
                  <div class="form-group grid-x align-middle">
                    <label for="buy-amount" class="small-3 cell no-padding">Amount:</label>
                    <input
                      type="number"
                      name="amount"
                      id="buy-amount"
                      step="0.00000001"
                      required
                      class="small-8 cell no-margin amount">
                    <span class="small-1 cell base-currency"></span>
                  </div>
                  <div class="form-group grid-x align-middle">
                    <label for="buy-total" class="small-3 cell no-padding">Total:</label>
                    <input
                      type="number"
                      name="total"
                      id="buy-total"
                      step="0.00000001"
                      required
                      disabled
                      class="small-8 cell no-margin">
                    <span class="small-1 cell counter-currency"></span>
                  </div>
                  <div class="grid-x align-right">
                    <button type="submit" id="buy-order-submit" class="button">Submit</button>
                  </div>
                </fieldset>
              </form>
            </div>
            <div class="small-6 cell">
              <form id="sell-order-form" name="sell" class="custom-box" autocomplete="off">
                <fieldset>
                  <div class="grid-x">
                    <h2>Sell <span class="base-currency"></span></h2>
                    <input type="hidden" name="side" value="SELL">
                  </div>
                  <div class="form-group grid-x align-middle">
                    <label for="market-sell" class="small-3 cell no-padding">Market Order:</label>
                    <input type="checkbox" name="market" id="market-sell" class="market-order">
                  </div>
                  <div class="form-group grid-x align-middle">
                    <label for="sell-price" class="small-3 cell no-padding">Price:</label>
                    <input
                      type="number"
                      name="price"
                      id="sell-price"
                      step="0.00000001"
                      required class="small-8 cell no-margin price">
                    <span class="small-1 cell counter-currency"></span>
                  </div>
                  <div class="form-group grid-x align-middle">
                    <label for="sell-amount" class="small-3 cell no-padding">Amount:</label>
                    <input
                      type="number"
                      name="amount"
                      id="sell-amount"
                      step="0.00000001"
                      required
                      class="small-8 cell no-margin amount">
                    <span class="small-1 cell base-currency"></span>
                  </div>
                  <div class="form-group grid-x align-middle">
                    <label for="sell-total" class="small-3 cell no-padding">Total:</label>
                    <input
                      type="number"
                      name="total"
                      id="sell-total"
                      step="0.00000001"
                      required
                      disabled
                      class="small-8 cell no-margin">
                    <span class="small-1 cell counter-currency"></span>
                  </div>
                  <div class="grid-x align-right">
                    <button type="submit" id="sell-order-submit" class="button">Submit</button>
                  </div>
                </fieldset>
              </form>
            </div>
          </div>
        </section>

        <section id="transaction-history" class="small-12 cell">
          <h2>Funds History</h2>
          <table id="user-funds-history-table">
            <thead>
              <tr>
                <th>Date</th><th>Time</th>
                <th>Side</th><th>Amount</th>
                <th>Currency</th>
              </tr>
            </thead>
            <tbody id="funds-history">
              <tr>
                <td>...</td><td>...</td>
                <td>...</td><td>...</td>
                <td>...</td>
              </tr>
            </tbody>
          </table>
          <h2>Trade History</h2>
          <table id="user-trade-history-table">
            <thead>
              <tr>
                <th>Date</th><th>Time</th>
                <th>Side</th><th>Amount</th>
                <th>Currency</th><th>Price</th>
                <th>Type</th>
              </tr>
            </thead>
            <tbody id="trade-history">
              <tr>
                <td>...</td><td>...</td>
                <td>...</td><td>...</td>
                <td>...</td><td>...</td>
                <td>...</td>
              </tr>
            </tbody>
          </table>
          <a href="javascript:;" id="refresh-transaction-history">
            <small>Refresh Transaction History</small>
          </a>
        </section>

        <section id="open-orders" class="small-12 cell">
          <h1>
            Open Orders - <span class="base-currency"></span>/<span class="counter-currency"></span>
          </h1>
          <table id="open-orders-table">
            <thead>
              <tr>
                <th>Date</th><th>Time</th>
                <th>Side</th><th>Amount</th>
                <th>Price</th><th>Action</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td>...</td><td>...</td>
                <td>...</td><td>...</td>
                <td>...</td><td>...</td>
              </tr>
            </tbody>
          </table>
        </section>
      </main>
    </div>
    <script>
      window.mr = """),format.raw/*377.19*/("""{"""),format.raw/*377.20*/("""
        """),format.raw/*378.9*/("""balanceURL: '"""),_display_(/*378.23*/conf/*378.27*/.getString("url.mr-balance")),format.raw/*378.55*/("""',
        tradeEngineURL: '"""),_display_(/*379.27*/conf/*379.31*/.getString("url.mr-trade-engine")),format.raw/*379.64*/("""'
      """),format.raw/*380.7*/("""}"""),format.raw/*380.8*/("""
    """),format.raw/*381.5*/("""</script>
    <script src='"""),_display_(/*382.19*/routes/*382.25*/.Assets.versioned("scripts/dist/main.js")),format.raw/*382.66*/("""'></script>
  </div>
</body>
</html>
"""))
      }
    }
  }

  def render(conf:play.api.Configuration): play.twirl.api.HtmlFormat.Appendable = apply()(conf)

  def f:(() => (play.api.Configuration) => play.twirl.api.HtmlFormat.Appendable) = () => (conf) => apply()(conf)

  def ref: this.type = this

}


              /*
                  -- GENERATED --
                  DATE: Thu Apr 26 16:46:05 PHT 2018
                  SOURCE: /Volumes/Development/Projects/mr-balance/app/views/demo.scala.html
                  HASH: f57d735c0be088ebc85422d2d2beef7bf3b48bdd
                  MATRIX: 744->1|880->42|907->43|1308->418|1322->424|1379->461|2968->2023|2981->2027|3028->2065|3076->2075|3119->2090|3162->2106|3187->2110|3217->2113|3242->2117|3296->2140|3335->2151|16926->15713|16956->15714|16993->15723|17035->15737|17049->15741|17099->15769|17156->15798|17170->15802|17225->15835|17261->15843|17290->15844|17323->15849|17379->15877|17395->15883|17458->15924
                  LINES: 21->1|26->1|27->2|34->9|34->9|34->9|71->46|71->46|71->46|71->46|72->47|72->47|72->47|72->47|72->47|73->48|74->49|402->377|402->377|403->378|403->378|403->378|403->378|404->379|404->379|404->379|405->380|405->380|406->381|407->382|407->382|407->382
                  -- GENERATED --
              */
          