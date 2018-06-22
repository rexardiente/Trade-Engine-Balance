package errors

import play.api.mvc._

object ServiceErrorHandler {

  def json[E >: MrError](error: E): Result = error match {
    case UnknownError => Results.InternalServerError
    case SubjectConflicts => Results.Conflict
    case SubjectNotExists => Results.NotFound
    case ObjectConflicts => Results.Conflict
    case ObjectNotExists => Results.NotFound
    case InvalidToken => Results.BadRequest
    case AuthenticationError => Results.Unauthorized
    case NoContent => Results.NoContent
    case _ => json(UnknownError)
  }
}
