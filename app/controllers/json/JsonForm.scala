package controllers.json

import play.api.libs.json._

import scala.util.Try

object JsonForm {

  def formToJson(stringToStrings: Map[String, Seq[String]]) = {
    val nameValues = stringToStrings.mapValues {
      case e +: Nil =>
        toJsonValue(e)
      case list =>
        JsArray(list.map(toJsonValue))
    }.toSeq
    JsObject(nameValues)
  }

  def toJsonValue(v: String): JsValue = {
    Try(v.toInt).toOption
      .map(i => JsNumber(i))
      .getOrElse(JsString(v))
  }

}
