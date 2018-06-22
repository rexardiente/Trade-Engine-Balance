package errors

class MrThrowableError(error: MrError) extends Throwable(error.toString) {
  def code: Int = error.code
  def reason: Option[String] = error.reason
}
