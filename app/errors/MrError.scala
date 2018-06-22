package errors

class MrError private (val code: Int, val reason: Option[String] = None) {
  def toThrowable: MrThrowableError = new MrThrowableError(this)

  override def toString: String =
    reason.map(r => s"MrError($code): $r").getOrElse(s"MrError($code)")
}

object MrError {
  private[errors] def apply[E >: MrError, C <: Int, R <: String](code: C, reason: Option[R] = None)
    : E = new MrError(code, reason)

  def unapply[E <: MrError](err: E): Option[(Int, Option[String])] =
    Some((err.code, err.reason))

  def unapply(err: MrThrowableError): Option[(Int, Option[String])] =
    Some((err.code, err.reason))
}
