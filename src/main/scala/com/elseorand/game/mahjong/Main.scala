package com.elseorand.game.mahjong

import scala.io.StdIn

import akka.actor._
import akka.http.scaladsl._
import akka.stream._

import scala.util.{Success, Failure}

import com.elseorand.game.mahjong.service.WebService

object Main extends App {
  // args serverName, serverPort
  implicit val system = ActorSystem("mahjong")// TODO observe param role
  import system.dispatcher
  implicit val materializer = ActorMaterializer()

  implicit val SERVER_NAME = if(args.length > 0) args(0) else "192.168.11.3" // TODO rm test code
  implicit val SERVER_PORT = if(args.length > 1) args(1).toInt else 18080 // TODO rm test code

  val service = new WebService

  val bindingFuture = Http().bindAndHandle(service.route, SERVER_NAME, SERVER_PORT)
  bindingFuture.onComplete {
    case Success(binding) =>
      val localAddress = binding.localAddress
      Console println s"Server is litening on : ${localAddress.getHostName}:${localAddress.getPort}"
    case Failure(e) =>
      Console println s"Binding failed with ${e.getMessage}"
      system.shutdown()
  }

  // EnterでSeverを終了
  println(s"Server online at http://${SERVER_NAME}:${SERVER_PORT}\nPress RETURN to stop...")
  StdIn.readLine() // let it run until user presses return
  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ => system.terminate()) // and shutdown when done
}
