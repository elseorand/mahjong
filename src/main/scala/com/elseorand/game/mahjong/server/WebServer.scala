import scala.concurrent._
import scala.io.StdIn

import collection.mutable
import collection.JavaConversions._

import akka._
import akka.actor._
import akka.stream._
import akka.stream.scaladsl._
import akka.http._
import akka.http.scaladsl._
import akka.http.scaladsl.server._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.ws._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.util._

import spray.json.DefaultJsonProtocol._

object WebServer extends App {
  implicit val system = ActorSystem("mahjong")
  implicit val materializer = ActorMaterializer()
  // needed for the future flatMap/onComplete in the end
  implicit val executionContext = system.dispatcher

  implicit val serverPort = 18080

  val route =
    get {
      pathPrefix("mahjong") {
        pathSingleSlash {
          getFromResource("html/main.html")
        } ~
        pathPrefix("jscss"){
          getFromResourceDirectory("jscss") // TODO search recursive
        } ~
        pathPrefix("jscss" / "jquery-ui-1.11.4") {
          getFromResourceDirectory("jscss/jquery-ui-1.11.4")
        } ~
        pathPrefix("ws") {
          // TODO 誰が捨てたかが、必須のため、受領を意味するHttpレスポンスも返す
          handleWebSocketMessages(wsFlow)// TODO wsFlowの生成にHttpRequestの情報を用いる
        }
      }
    }

  val broadCaster = system.actorOf(Props[BroadcastActor])

  // 下記 `handleWebSocketMessages` で使用される Flow (Strict 版) // TODO back pressure やるのでその内修正
  val wsFlow: Flow[Message, Message, _] =
      Flow[Message].map {
        case TextMessage.Strict(txt) => {
          // 完全なメッセージを受信するまで待ちます。
          // Strict (完全なメッセージ) を受信してから、完全なメッセージで応答します。
          // つまり、TextMessage(textStream: Source[String, Any]) ではなく TextMessage(text: String) で応答します。
          TextMessage("Hello " + txt)
        }
        case _ => {
          // バイナリメッセージを受信
          TextMessage("Message type unsupported")
        }
      }


  val bindingFuture = Http().bindAndHandle(route, "localhost", serverPort)

  // EnterでSeverを終了
  println(s"Server online at http://localhost:$serverPort\nPress RETURN to stop...")
  StdIn.readLine() // let it run until user presses return
  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ => system.terminate()) // and shutdown when done

}

class BroadcastActor extends Actor {
  val subscribers = mutable.HashMap.empty[String, ActorRef]

  def receive: Receive = {
    case Subscribe(id, actorRef) => subscribers += ((id, actorRef))
    case UnSubscribe(id) =>subscribers -= id
    case Sutehai => subscribers.values.foreach(_ ! ??? )
  }

}

sealed trait OperationMessage
case class Subscribe(id: String, actorRef: ActorRef) extends OperationMessage
case class UnSubscribe(id: String) extends OperationMessage
case class Sutehai(idPai: String, done: Boolean) extends OperationMessage
