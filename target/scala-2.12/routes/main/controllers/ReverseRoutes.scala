
// @GENERATOR:play-routes-compiler
// @SOURCE:/Volumes/Development/Projects/mr-balance/conf/routes
// @DATE:Wed Apr 25 12:52:59 PHT 2018

import play.api.mvc.Call


import _root_.controllers.Assets.Asset

// @LINE:2
package controllers {

  // @LINE:2
  class ReverseHomeController(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:3
    def demo(): Call = {
      
      Call("GET", _prefix + { _defaultPrefix } + "demo")
    }
  
    // @LINE:2
    def socket(): Call = {
      
      Call("GET", _prefix)
    }
  
    // @LINE:5
    def getCurrencies(): Call = {
      
      Call("GET", _prefix + { _defaultPrefix } + "currencies")
    }
  
    // @LINE:4
    def login(): Call = {
      
      Call("POST", _prefix + { _defaultPrefix } + "login")
    }
  
  }

  // @LINE:8
  class ReverseAssets(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:8
    def versioned(file:Asset): Call = {
      implicit lazy val _rrc = new play.core.routing.ReverseRouteContext(Map(("path", "/public"))); _rrc
      Call("GET", _prefix + { _defaultPrefix } + "assets/" + implicitly[play.api.mvc.PathBindable[Asset]].unbind("file", file))
    }
  
  }


}
