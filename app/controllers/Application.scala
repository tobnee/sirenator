package controllers

import javax.inject.Inject

import com.yetu.siren.json.playjson.PlayJsonSirenFormat._
import com.yetu.siren.model.Entity.RootEntity
import com.yetu.siren.model._
import controllers.json.{JsonForm, SirenPropertyFormat}
import helpers.ConditionalLayout
import play.api._
import play.api.libs.ws.{WSClient, WSRequest}
import play.api.mvc.{Action, _}
import play.twirl.api.Html

import scala.concurrent.Future

object Application {

  val SIREN_ACTION_URL_FIELD = "sirenactionurl"
  val SIREN_ACTION_FORMAT_FIELD = "sirenactionformat"
  val SIREN_ACTION_METHOD_FIELD = "sirenactionmethod"

  private val DEFAULT_ACTION_ENCODING = Some(List("application/x-www-form-urlencoded"))
  private val DEFAULT_ACTION_METHOD = Some(List("POST"))
}

class Application @Inject()(ws: WSClient) extends Controller with ConditionalLayout with SirenPropertyFormat {
  import play.api.libs.concurrent.Execution.Implicits._

  def index = Action {
    Ok(views.html.index())
  }

  def query(uri: String) = ConditionalLayoutAction.async { implicit req =>
    getSiren(uri).map { entity =>
      renderSiren(entity)
    }
  }

  def renderSiren(entity: RootEntity)(implicit req: ConditionalLayoutRequest[_]): Result = {
    Ok(renderCond("foo", views.html.sirenresource.render(entity, propertiesW)))
  }

  def getSiren(url: String): Future[RootEntity] = {
    ws.url(url).withHeaders("Accept" -> "application/json")
      .get().map { r =>
      val re = r.json.as[Entity.RootEntity]
      re
    }
  }

  def submit() = ConditionalLayoutAction.async(parse.urlFormEncoded) { implicit req =>
    val body = req.body
    extractSirenActionMetadata(body) match {
      case Some((url, format, method)) =>
        val formWithoutControls =
          body - Application.SIREN_ACTION_URL_FIELD - Application.SIREN_ACTION_FORMAT_FIELD - Application.SIREN_ACTION_METHOD_FIELD
        Logger.info(s"try to post $formWithoutControls in format $format to $url with method $method")
        withRequest(ws.url(url), format, formWithoutControls).execute(method).map { response =>
          Logger.info("response from service " + response)
          val j = response.json
          val re = j.as[RootEntity]
          renderSiren(re)
        }
      case _ =>
        Future.successful(BadRequest("missing url parameter"))
    }
  }

  def withRequest[T](wsr: WSRequest, format: String, formWithoutControls: Map[String, Seq[String]]): WSRequest = {
    format match {
      case "application/x-www-form-urlencoded" =>
        wsr.withBody(formWithoutControls)
      case "application/json" =>
        val json = JsonForm.formToJson(formWithoutControls)
        wsr.withBody(json)
    }
  }

  def extractSirenActionMetadata(body: Map[String, Seq[String]]) = {
    for {
      url <- body.get(Application.SIREN_ACTION_URL_FIELD)
      method <- body.get(Application.SIREN_ACTION_METHOD_FIELD) orElse Application.DEFAULT_ACTION_METHOD
      format <- body.get(Application.SIREN_ACTION_FORMAT_FIELD) orElse Application.DEFAULT_ACTION_ENCODING
      m <- method.headOption
      u <- url.headOption
      f <- format.headOption
    } yield (u, f, m)
  }

  def renderCond(title: String, content: Html)(implicit req: ConditionalLayoutRequest[_]) = {
    if (req.requiresLayout) views.html.main(title)(content)
    else content
  }

}
