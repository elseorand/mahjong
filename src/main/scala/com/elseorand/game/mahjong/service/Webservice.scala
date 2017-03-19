package com.elseorand.game.mahjong.service

import akka.actor._
import akka.stream._
import akka.stream.scaladsl._
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.model.ws.{Message, TextMessage}

import com.elseorand.game.mahjong.logic._
import com.elseorand.game.mahjong.logic.Protocol
import com.elseorand.game.mahjong.logic.Protocol._
import com.elseorand.game.mahjong.logic.Protocol.ResponseTsumohaiProtocol._

import spray.json._

class WebService(implicit fm: Materializer, system: ActorSystem) extends Directives {

  val mahjongLogic = MahjongLogic(system)
  // import system.dispatcher

  def route = get {
    Console println s"pass : get"

    pathPrefix("mahjong") {
      pathSingleSlash {
        getFromResource("html/main.html")
      } ~
      pathPrefix("wysiwyg"){
        getFromResourceDirectory("wysiwyg")
      } ~
      pathPrefix("jscss"){
        getFromResourceDirectory("jscss") // TODO search recursive
      } ~
      pathPrefix("jscss" / "jquery-ui-1.11.4") {
        getFromResourceDirectory("jscss/jquery-ui-1.11.4")
      }
    }
  } ~
  pathPrefix("mahjong" / "ws") {
    Console println s"pass : ws"

    // val uuid = UUID.randomUUID().toString
    // TODO 誰が捨てたかが、必須のため、受領を意味するHttpレスポンスも返す
    parameter('userId) { userId =>
      Console println s"userId : $userId" // TODO rm test code
      // TODO wsFlowの生成にHttpRequestの情報を用いる
      handleWebSocketMessages(wsFlow(senderId = userId))
    }
  }
  // 下記 `handleWebSocketMessages` で使用される Flow (Strict 版)
  // TODO back pressure やるのでその内修正
  def wsFlow(senderId: String): Flow[Message, Message, _] =
    Flow[Message]
      .map { msg => // for logging
        Console println s"log msg : $msg"
        msg
      }
      .collect {
        case TextMessage.Strict(msg) => msg
      }
      .via(mahjongLogic.gameFlow(senderId))
      .map {
      case msg: Protocol.ResponseTsumohai => {
        val json: JsValue = msg.toJson
        TextMessage(Source.single(json.toString()))
      }
      case msg: Protocol.GameMessage =>
        TextMessage(Source.single("OK: " + msg))
  }
  // .via(reportErrorsFlow)

  // def reportErrorsFlow[T]: Flow[T, T, Any] = Flow[T]
  //   .transform(() ⇒ new PushStage[T, T] {
  //     def onPush(elem: T, ctx: Context[T]): SyncDirective = ctx.push(elem)

  //     override def onUpstreamFailure(cause: Throwable, ctx: Context[T]): TerminationDirective = {
  //       println(s"WS stream failed with $cause")
  //       super.onUpstreamFailure(cause, ctx)
  //     }
  //   })

}
