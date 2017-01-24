package com.elseorand.game.mahjong.logic

import scala.util.Random

import akka.actor._
import akka.stream.OverflowStrategy
import akka.stream.scaladsl._

import com.elseorand.game.mahjong.entity.User

trait MahjongLogic {
  val random = new Random
  val Manzu = PaiType("Manzu")
  val Souzu = PaiType("Souzu")
  val Pinzu = PaiType("Pinzu")
  val Jihai = PaiType("Jihai")

  val Ton = Kaze("Ton")
  val Nan = Kaze("Nan")
  val Sya = Kaze("Sya")
  val Bei = Kaze("Bei")

  val PAI_TYPES9NUMBER = List(Manzu, Souzu, Pinzu)
  val ALL_PAI_LIST = PAI_TYPES9NUMBER.flatMap(payType =>
    for(num <- 1 to 9; id <- 0 to 3) yield {
      val is1or9 = num == 1 || num == 9;
      MahjongPai(payType, num, id, num + 9 * id, is1or9, !is1or9, is1or9, false, false)
    }) ++ (for(num <- 1 to 7; id <- 0 to 3) yield {
      MahjongPai(Jihai, num, id, num + 7 * id + 108, true, false, false, num <= 4, num > 4)
    })

  def getAllPaiList(): List[MahjongPai] = ALL_PAI_LIST
  def getAllRandomedPaiList(): List[MahjongPai] = {
    val arrayed = ALL_PAI_LIST.toArray
    for(i <- 0 to ALL_PAI_LIST.length - 1) {
      val toIndex = random.nextInt(ALL_PAI_LIST.length)
      val extractedPai = arrayed(toIndex)
      arrayed(toIndex) = arrayed(i)
      arrayed(i) = extractedPai
    }
    // cut
    val cutIndex = random.nextInt(ALL_PAI_LIST.length)
    val (left, right) = arrayed splitAt(cutIndex)
    right.toList ++ left.toList
  }
  def gameFlow(senderId: String): Flow[String, Protocol.GameMessage, Any]
}

object MahjongLogic {
  // TODO 一次受付 palyers pool
  // TODO taku pool
  var taku: Taku = null;

  def apply(system: ActorSystem): MahjongLogic = {
    val gameActor = system.actorOf(Props(new Actor {
      var subscribers = Set.empty[(String, ActorRef)] // TODO concurrent

      def receive: Receive = {
        // TODO case NewTaku
        case NewParticipant(id, subscriber) => {
          context.watch(subscriber) // TODO what does this mean
          subscribers += (id -> subscriber)
          if (subscribers.size < 1){// TODO 1
            dispatch(Protocol.ChatMessage(id, s"user: $id joins." ))
          }else{
            // TODO create taku ID
            taku = Taku4(subscribers.toList.map(s => User(s._1, "hoge", s._2)))
            dispatch(Protocol.ChatMessage(id, s"user: $id joins. Game Start!!" ))
          }
        }
        case msg: ReceivedMessage => {
          Console println s"ReceivedMessage : ${msg.message}"
          dispatch(msg.toChatMessage)
        }
        case Tsumohai(senderId, paiList) => dispatch(Protocol.ResponseTsumohai(senderId, paiList))
        case Sutehai(senderId, paiList) => dispatch(Protocol.ResponseSutehai(senderId, paiList))
        case Pon(senderId, pai) => dispatch(Protocol.ResponsePon(senderId, pai))
        case Kan(senderId, pai) => dispatch(Protocol.ResponseKan(senderId, pai))
        case Chi(senderId, pai) => dispatch(Protocol.ResponseChi(senderId, pai))
        case Reach(senderId, pai) => dispatch(Protocol.ResponseReach(senderId, pai))
        case Ron(senderId, pai) => dispatch(Protocol.ResponseRon(senderId, pai))
        case Tsumo(senderId, pai) => dispatch(Protocol.ResponseTsumo(senderId, pai))
        case Syn(senderId, eventSeq) => dispatch(Protocol.Syn(senderId, eventSeq)) // TODO taku
        case Ack(senderId, eventSeq) => dispatch(Protocol.Ack(senderId, eventSeq)) // TODO taku

      }

      def dispatch(msg: Protocol.GameMessage): Unit = subscribers.foreach(_._2 ! msg)
      def members = subscribers.map(_._1).toSeq
    }))

    def gameInSink(senderId: String) = Sink.actorRef[GameEvent](gameActor, LeaveParticipant(senderId))

    new MahjongLogic {
      import spray.json._

      def gameFlow(senderId: String): Flow[String, Protocol.GameMessage, Any] = {

        val in = Flow[String]
          .map((msg: String) => {
            import com.elseorand.game.mahjong.logic.Protocol.DefaultTypeProtocol._
            // TODO observing how to dispatch
            val jsoned = msg.parseJson

            jsoned.convertTo[Protocol.DefaultType].$type match {
              case "com.elseorand.game.mahjong.logic.GameMessage.RequestTsumohai" => {
                import com.elseorand.game.mahjong.logic.Protocol.RequestTsumohaiProtocol._
                val reqTsumohai = jsoned.convertTo[Protocol.RequestTsumohai]
                Tsumohai(senderId, getAllRandomedPaiList().slice(0, reqTsumohai.number))// TODO
              }
              case _ => ReceivedMessage(senderId, msg) // => to receive method
            }
          })
          .to(gameInSink(senderId))

        val out = Source.actorRef[Protocol.GameMessage](1, OverflowStrategy.fail)
          .mapMaterializedValue(gameActor ! NewParticipant(senderId, _))

        Flow.fromSinkAndSource(in, out)
      }
    }
  }

  private sealed trait GameEvent
  private case class NewParticipant(id: String, subscriber: ActorRef) extends GameEvent
  private case class LeaveParticipant(id: String) extends GameEvent
  private case class ReceivedMessage(senderId: String, message: String) extends GameEvent {
    def toChatMessage: Protocol.ChatMessage = Protocol.ChatMessage(senderId, message)
  }
  private case class Tsumohai(senderId: String, paiList: Seq[MahjongPai]) extends GameEvent
  private case class Sutehai(senderId: String, paiList: Seq[MahjongPai]) extends GameEvent
  private case class Pon(senderId: String, pai: MahjongPai) extends GameEvent
  private case class Kan(senderId: String, pai: MahjongPai) extends GameEvent
  private case class Chi(senderId: String, pai: MahjongPai) extends GameEvent
  private case class Reach(senderId: String, pai: MahjongPai) extends GameEvent
  private case class Ron(senderId: String, pai: MahjongPai) extends GameEvent
  private case class Tsumo(senderId: String, pai: MahjongPai) extends GameEvent
  private case class Syn(senderId: String, eventSeq: Long) extends GameEvent
  private case class Ack(senderId: String, eventSeq: Long) extends GameEvent
}
