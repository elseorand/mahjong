package com.elseorand.game.mahjong.logic

import scala.util.Random

import akka.actor._
import akka.stream.OverflowStrategy
import akka.stream.scaladsl._

import com.elseorand.game.mahjong.entity.GameId
import com.elseorand.game.mahjong.entity.GameBaEntity
import com.elseorand.game.mahjong.entity.User
import com.elseorand.game.mahjong.entity.UserId

trait MahjongService {

  val random = new Random

  val PAI_TYPES9NUMBER = List(MahjongService.Manzu, MahjongService.Souzu, MahjongService.Pinzu)
  val ALL_PAI_LIST = PAI_TYPES9NUMBER.flatMap(payType =>
    for(num <- 1 to 9; index <- 0 to 3) yield {
      val is1or9 = num == 1 || num == 9;
      MahjongPai(payType, num, index, num + 9 * index
        , 126982 + num + 9 * PAI_TYPES9NUMBER.indexOf(payType)// 126982 is unicode
        , is1or9, !is1or9, is1or9, false, false)
    }) ++ (for(num <- 1 to 7; index <- 0 to 3) yield {
      MahjongPai(MahjongService.Jihai, num, index, num + 7 * index + 108
        , 126975 + num
        , true, false, false, num <= 4, num > 4)
    })

  def allPaiList(): List[MahjongPai] = ALL_PAI_LIST
  def allRandomedPaiList(): List[MahjongPai] = {
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

object MahjongService {
  import java.util.concurrent.atomic.AtomicLong

  import java.util.concurrent.ConcurrentHashMap
  import scala.collection.JavaConverters._
  import scala.collection.concurrent.Map

  val Manzu = PaiType("Manzu")
  val Souzu = PaiType("Souzu")
  val Pinzu = PaiType("Pinzu")
  val Jihai = PaiType("Jihai")

  val Ton = Kaze("Ton")
  val Nan = Kaze("Nan")
  val Sya = Kaze("Sya")
  val Bei = Kaze("Bei")

  val subscribers: Map[UserId, User] = new ConcurrentHashMap().asScala
  val kazeList: Seq[Kaze] = Seq(MahjongService.Ton, MahjongService.Nan, MahjongService.Sya, MahjongService.Bei)

  var system: ActorSystem = null
  val newTakuId: AtomicLong = new AtomicLong
  val gameCounter: AtomicLong = new AtomicLong
  val gameRepository: Map[GameId, GameBaEntity] = new ConcurrentHashMap().asScala

  // TODO 一次受付 palyers pool
  // TODO taku pool
  var taku: Taku = null // 小規模のため１卓のみ
  // TODO taku manager
  var gameActor: ActorRef = null // TODO gameActorは卓毎

  def refreshGameActor(): Unit = gameActor = genGameActor()

  def genGameActor(): ActorRef = system.actorOf(Props(new Actor {

      // TODO agari, ryukyoku, taku end,
      def receive: Receive = {
        // TODO case NewTaku
        case NewParticipant(id, subscriber) => {
          context.watch(subscriber) // TODO what does this mean
          subscribers += (UserId(id) -> User(UserId(id), "hoge", subscriber))  // TODO using user_master
          if (subscribers.size < 1){// TODO magic 1 => 4
            dispatch(Protocol.ChatMessage(id, s"user: $id joins." ))
          }else{
            var memberList: Seq[(UserId, User)] = Nil
            synchronized {
              val pair = subscribers.toSeq.splitAt(4)
              memberList = pair._1
              subscribers --= pair._2.map(_._1)
            }
            taku = Taku4(newTakuId.incrementAndGet(), gameCounter, memberList, kazeList, (id, entity) => gameRepository.putIfAbsent(id, entity))// TODO async
            dispatch(Protocol.ChatMessage(id, s"user: $id joins. Game Start!!" ))
          }
        }
        case msg: ReceivedMessage => { // TODO rm this
          Console println s"ReceivedMessage : ${msg.message}"
          dispatch(msg.toChatMessage)
        }
        // mahjong actions
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

      def dispatch(msg: Protocol.GameMessage): Unit = subscribers.foreach(_._2.actor ! msg)
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
            Console println s"in msg : $msg"
            val protocolType = jsoned.convertTo[Protocol.DefaultType].$type;
            protocolType match {
              case "RequestTsumohai" => {
                Console println "route : RequestTsumohai"
                import com.elseorand.game.mahjong.logic.Protocol.RequestTsumohaiProtocol._
                val reqTsumohai = jsoned.convertTo[Protocol.RequestTsumohai]
                Tsumohai(senderId, allRandomedPaiList().slice(0, reqTsumohai.number))// TODO
              }
              // TODO dev other actions
              case _ => {
                Console println s"route : $protocolType"
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
