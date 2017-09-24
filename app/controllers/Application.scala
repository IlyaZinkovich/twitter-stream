package controllers

import actors.TwitterStreamer
import play.api.Play.current
import play.api.libs.json._
import play.api.mvc._

class Application extends Controller {

  def index = Action { implicit request =>
    Ok(views.html.index("Your new application is ready."))
  }


  def tweets = WebSocket.acceptWithActor[String, JsValue] {
    request => out => TwitterStreamer.props(out)
  }
}