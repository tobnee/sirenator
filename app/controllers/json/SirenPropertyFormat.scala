package controllers.json

import com.yetu.siren.model._
import play.api.libs.json._

trait SirenPropertyFormat {
  import com.yetu.siren.json.playjson.PlayJsonSirenFormat._

  implicit val propertiesW: Format[Properties] = new Format[Properties] {
    override def writes(properties: Properties): JsValue = {
      val fields = properties.map(p ⇒ p.name -> Json.toJson(p.value))
      JsObject(fields)
    }

    override def reads(json: JsValue): JsResult[Properties] = json match {
      case JsObject(fields) ⇒
        fields.foldLeft[JsResult[Properties]](JsSuccess(Vector.empty)) {
          case (acc, (name, jsValue)) ⇒ (acc, jsValue.validate[Property.Value]) match {
            case (JsSuccess(props, _), JsSuccess(value, _)) ⇒
              JsSuccess(props :+ Property(name, value))
            case (JsSuccess(_, _), JsError(errors)) ⇒
              JsError(Seq(JsPath \ name -> errors.flatMap(_._2)))
            case (e: JsError, s: JsSuccess[_]) ⇒
              e
            case (e: JsError, JsError(errors)) ⇒
              e ++ JsError(Seq(JsPath \ name -> errors.flatMap(_._2)))
          }
        }
      case _ ⇒ JsError("error.expected.jsobject")
    }


  }
}
