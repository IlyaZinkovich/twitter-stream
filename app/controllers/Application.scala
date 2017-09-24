package controllers

import play.api._
import play.api.mvc._
import play.api.libs.oauth.{ConsumerKey, OAuthCalculator, RequestToken}
import play.api.Play.current

import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.iteratee.Iteratee
import play.api.libs.ws.WS

class Application extends Controller {

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  private val loggingIteratee = Iteratee.foreach[Array[Byte]] { array =>
    Logger.info(array.map(_.toChar).mkString)
  }

  def tweets = Action.async {
    credentials.map { case (consumerKey, requestToken) =>
      WS
        .url("https://stream.twitter.com/1.1/statuses/filter.json")
        .sign(OAuthCalculator(consumerKey, requestToken))
        .withQueryString("track" -> "cat")
        .get { response =>
          Logger.info("Status: " + response.status)
          loggingIteratee
        }
        .map { _ =>
          Ok("Stream closed")
        }
    } getOrElse {
      Future.successful(InternalServerError("Twitter credentials missing"))
    }
  }

  private def credentials: Option[(ConsumerKey, RequestToken)] = for {
    apiKey <- Play.configuration.getString("twitter.apiKey")
    apiSecret <- Play.configuration.getString("twitter.apiSecret")
    token <- Play.configuration.getString("twitter.token")
    tokenSecret <- Play.configuration.getString("twitter.tokenSecret")
  } yield (
    ConsumerKey(apiKey, apiSecret),
    RequestToken(token, tokenSecret)
  )
}