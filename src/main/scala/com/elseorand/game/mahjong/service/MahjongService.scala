package com.elseorand.game.mahjong.logic

import com.elseorand.game.mahjong.ai.AutoSutehai
import scala.util.Random

import akka.actor._
import akka.stream.OverflowStrategy
import akka.stream.scaladsl._

import com.elseorand.game.mahjong.entity.GameId
import com.elseorand.game.mahjong.entity.GameBaEntity
import com.elseorand.game.mahjong.entity.User
import com.elseorand.game.mahjong.entity.UserId

trait MahjongService {

  def gameFlow(senderId: String): Flow[String, Protocol.GameMessage, Any]

}

object MahjongService {
  import java.util.concurrent.atomic.AtomicLong

  import java.util.concurrent.ConcurrentHashMap
  import scala.collection.JavaConverters._
  import scala.collection.concurrent.Map

  val logic = MahjongLogic()

  val subscribers: Map[UserId, User] = new ConcurrentHashMap().asScala

  var system: ActorSystem = null
  val newTakuId: AtomicLong = new AtomicLong
  val gameCounter: AtomicLong = new AtomicLong
  val gameRepository: Map[GameId, GameBaEntity] = new ConcurrentHashMap().asScala

  // TODO 一次受付 palyers pool
  // TODO taku pool
  @volatile
  implicit var taku: Taku = null // 小規模のため１卓のみ
  // TODO taku manager
  var gameActor: ActorRef = null // TODO gameActorは卓毎

  def refreshGameActor(): Unit = gameActor = genGameActor()

  def genGameActor(): ActorRef = system.actorOf(Props(new Actor {
    @volatile
    var gameBa: Option[GameBaEntity] = Option.empty
      // TODO agari, ryukyoku, taku end,
      def receive: Receive = {
        // TODO case NewTaku
        case NewParticipant(id, subscriber) => {
          context.watch(subscriber) // TODO what does this mean
          subscribers += (UserId(id) -> User(UserId(id), "hoge", subscriber))  // TODO using user_master
          dispatchAll(Protocol.ChatMessage(id, s"user: $id joins." ))

          synchronized {
            if (subscribers.size == 1) {
              val pair = subscribers.toSeq.splitAt(1)
              val memberList = pair._1 ++ Seq((UserId("-1"), AutoSutehai(UserId("-1"), "dummy1")), (UserId("-2"), AutoSutehai(UserId("-2"), "dummy1")), (UserId("-3"), AutoSutehai(UserId("-3"), "dummy1")))
              subscribers --= pair._2.map(_._1)
              taku = Taku4(newTakuId.incrementAndGet(), gameCounter, memberList, MahjongLogic.KazeList, (id, entity) => gameRepository.putIfAbsent(id, entity))// TODO async
              gameBa = taku.newGame(() => logic.allRandomedPaiList())
            }
            gameBa.map(_.start());
          }
        }
        case msg: ReceivedMessage => { // TODO rm this
          dispatch(msg.toChatMessage)
        }
          // mahjong actions
        case Haipai(kaze, pai13List) =>
        case Tsumohai(senderId, iHaveTsumohai) => {
          val paiList = gameBa.map(_.tsumoHai(iHaveTsumohai.number)).getOrElse(Seq.empty)
          dispatch(Protocol.YouHaveTsumohai(paiList))
        }
        case Sutehai(senderId, paiList) => dispatch(Protocol.YouHaveSutehai(senderId, paiList))
        case Pon(senderId, pai) => dispatch(Protocol.ResponsePon(senderId, pai))
        case Kan(senderId, pai) => dispatch(Protocol.ResponseKan(senderId, pai))
        case Chi(senderId, pai) => dispatch(Protocol.ResponseChi(senderId, pai))
        case Reach(senderId, pai) => dispatch(Protocol.ResponseReach(senderId, pai))
        case Ron(senderId, pai) => dispatch(Protocol.ResponseRon(senderId, pai))
        case Tsumo(senderId, pai) => dispatch(Protocol.ResponseTsumo(senderId, pai))
        case Syn(senderId, eventSeq) => dispatch(Protocol.Syn(senderId, eventSeq)) // TODO taku
        case Ack(senderId, eventSeq) => dispatch(Protocol.Ack(senderId, eventSeq)) // TODO taku
      }

      def dispatch(msg: Protocol.GameMessage)(implicit taku: Taku): Unit = taku.members().foreach(_._2 ! msg)
      def dispatchAll(msg: Protocol.GameMessage): Unit = subscribers.foreach(_._2 ! msg)
      def members = subscribers.map(_._1).toSeq
    }))

  def takuOfUserPlaying(userId: UserId): Taku = ???

  def gameOfUserPlaying(userId: UserId): GameBaEntity = {
    val taku = takuOfUserPlaying(userId);
    ???
  }

  def apply(system: ActorSystem): MahjongService = {
    this.system = system
    this.gameActor = genGameActor()

    def gameInSink(senderId: String) = Sink.actorRef[GameEvent](gameActor, LeaveParticipant(senderId))

    new MahjongService {
      import spray.json._

      def gameFlow(senderId: String): Flow[String, Protocol.GameMessage, Any] = {
        Console println s"gameFlow senderId : $senderId"

        val in = Flow[String]
          .map((msg: String) => {
            import com.elseorand.game.mahjong.logic.Protocol.DefaultTypeProtocol._
            // TODO observing how to dispatch
            val jsoned = msg.parseJson
            val defaultType = jsoned.convertTo[Protocol.DefaultType];
            val protocolType = defaultType.$type;
            protocolType match {
              case "IhaveTsumohai" => {
                Console println "route : IhaveTsumohai"
                import com.elseorand.game.mahjong.logic.Protocol.IhaveTsumohaiProtocol._
                val reqTsumohai = jsoned.convertTo[Protocol.IhaveTsumohai]
                Tsumohai(senderId, reqTsumohai)// TODO
              }
              // TODO dev other actions
              case _ => {
                ReceivedMessage(senderId, msg) // => to receive method
              }
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
  private case class Haipai(kaze: Kaze, pai13List: Seq[MahjongPai]) extends GameEvent
  private case class Tsumohai(senderId: String, iHaveTsumohai: Protocol.IhaveTsumohai) extends GameEvent
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
