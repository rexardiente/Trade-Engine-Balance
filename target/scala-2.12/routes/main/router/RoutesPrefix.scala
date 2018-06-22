
// @GENERATOR:play-routes-compiler
// @SOURCE:/Volumes/Development/Projects/mr-balance/conf/routes
// @DATE:Wed Apr 25 12:52:59 PHT 2018


package router {
  object RoutesPrefix {
    private var _prefix: String = "/"
    def setPrefix(p: String): Unit = {
      _prefix = p
    }
    def prefix: String = _prefix
    val byNamePrefix: Function0[String] = { () => prefix }
  }
}
