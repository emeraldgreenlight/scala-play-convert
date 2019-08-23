package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.ws._

import scala.concurrent.ExecutionContext


/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(ec: ExecutionContext, ws: WSClient, cc: ControllerComponents) extends AbstractController(cc) {

  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.index())
  }

  def explore() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.explore())
  }

  def tutorial() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.tutorial())
  }

  def ping() = Action { implicit request => Ok("String works!") }

  def serveJSON() = Action {
    _ => Ok(Json.obj("yes" -> true))
  }

  def serveName(name: String) = Action { implicit request =>
    Ok(Json.obj("name" -> name))
  }

  def transparent() = Action.async {
    ws.url("https://api.ratesapi.io/api/latest").get().map { result =>
      val Jsr = Json.parse(result.body)
      val rts = (Jsr \ "rates").get.as[JsObject]
      val baseStr = (Jsr \ "base").as[String]
      val rates = rts ++ Json.obj(baseStr -> 1.0)

      Ok(rates)
    }(ec)
  }

  def convert(from: String, to: String, number: String) = Action.async {
    implicit request => {
      ws.url("https://api.ratesapi.io/api/latest").get().map { result => {
        val Jsr = Json.parse(result.body)
        val rts = (Jsr \ "rates").get.as[JsObject]
        val rates = rts ++ Json.obj((Jsr \ "base").as[String] -> 1.0) // patch rates JsObject with "BASE" : 1.0
        Ok(Json.obj("result" -> number.toFloat * rates(to).as[Float] / rates(from).as[Float]  ))
      }
      }(ec).recover { case e =>
        Status(400)(Json.obj("error" -> e.toString))
      }(ec)
    }
  }
}
