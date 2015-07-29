package controllers

import controllers.SirenDemoApi.Item
import play.api.libs.json.{Reads, Json}
import play.api.mvc.{RequestHeader, Action, Controller}

object SirenDemoApi {
  case class Item(productCode: String, quantity: Int, produced: String)

  implicit val itemReads: Reads[Item] = Json.reads[Item]
}

class SirenDemoApi extends Controller {

  def home = Action { implicit req =>
    Ok(sirenHome)
  }
  
  def order = Action { implicit request =>
    Ok(sirenOrderJson)
  }

  def addItem() = Action { r =>
    r.body.asJson match {
      case Some(j) =>
        Created(sirenItemJson(j.as[Item]))
      case _ => BadRequest
    }
  }


  def sirenHome(implicit request: RequestHeader) = Json.parse(s"""{
                                    |  "class": [ "home" ],
                                    |  "links": [
                                    |    { "rel": [ "first" ], "href": "${routes.SirenDemoApi.order().absoluteURL()}" }
                                    |  ]
                                    |}""".stripMargin)

  def sirenOrderJson(implicit request: RequestHeader) = Json.parse(s"""{
                               |  "class": [ "order" ],
                               |  "properties": {
                               |      "orderNumber": 42,
                               |      "itemCount": 3,
                               |      "status": "pending"
                               |  },
                               |  "entities": [
                               |    {
                               |      "class": [ "items", "collection" ],
                               |      "rel": [ "http://x.io/rels/order-items" ],
                               |      "href": "http://api.x.io/orders/42/items"
                               |    },
                               |    {
                               |      "class": [ "info", "customer" ],
                               |      "rel": [ "http://x.io/rels/customer" ],
                               |      "properties": {
                               |        "customerId": "pj123",
                               |        "name": "Peter Joseph"
                               |      },
                               |      "links": [
                               |        { "rel": [ "self" ], "href": "http://api.x.io/customers/pj123" }
                               |      ]
                               |    }
                               |  ],
                               |  "actions": [
                               |    {
                               |      "name": "add-item",
                               |      "title": "Add Item",
                               |      "method": "POST",
                               |      "href": "${routes.SirenDemoApi.addItem().absoluteURL()}",
                               |      "type": "application/json",
                               |      "fields": [
                               |        { "name": "orderNumber", "type": "hidden", "value": "42" },
                               |        { "name": "productCode", "type": "text" },
                               |        { "name": "quantity", "type": "number" },
                               |        { "name": "produced", "type": "date" }
                               |      ]
                               |    }
                               |  ],
                               |  "links": [
                               |    { "rel": [ "self" ], "href": "http://api.x.io/orders/42" },
                               |    { "rel": [ "previous" ], "href": "http://api.x.io/orders/41" },
                               |    { "rel": [ "next" ], "href": "http://api.x.io/orders/43" }
                               |  ]
                               |}""".stripMargin)


  def sirenItemJson(item: Item) = Json.parse(s"""{
                                    |  "class": [ "item" ],
                                    |  "properties": {
                                    |      "productCode": "${item.productCode}",
                                    |      "quantity": ${item.quantity},
                                    |      "produced": "${item.produced}"
                                    |  },
                                    |  "links": [
                                    |    { "rel": [ "parent" ], "href": "http://api.x.io/orders/42" }
                                    |  ]
                                    |}""".stripMargin)

}
