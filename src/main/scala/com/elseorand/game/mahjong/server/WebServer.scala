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
        pathEndOrSingleSlash {
          getFromResource("html" / "main.html")
        } ~
        pathPrefix("jscss"){
          getFromResourceDirectory("jscss") // TODO search recursive
        } ~
        pathPrefix("jscss" / "jquery-ui-1.11.4") {
          getFromResourceDirectory("jscss/jquery-ui-1.11.4")
        }
      } ~
      path("sutehai") {
        // TODO 誰が捨てたかが、必須のため、受領を意味するHttpレスポンスも返す

        handleWebSocketMessages(sutehaiFlow)// TODO sutehaiFlowの生成にHttpRequestの情報を用いる
      }
    }

  val broadCaster = system.actorOf(Props[BroadcastActor])

  def sutehaiFlow() = Flow.fromGraph(GraphDSL.create(Source.actorRef[Sutehai](???)) {
    implicit builder =>
    subscribeActor =>
    import GraphDSL.Implicits._

    val websocketSource = builder.add(Flow[Message].collect (???))

  })

  val bindingFuture = Http().bindAndHandle(route, "localhost", serverPort)

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
