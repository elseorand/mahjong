package com.elseorand.game.mahjong.logic

import spray.json._

object Protocol {
  sealed trait GameMessage {

  }
  case class DefaultType(val $type: String) extends GameMessage
  case class ChatMessage(senderId: String, message: String) extends GameMessage
  case class Subscribe(member: String, allMembers: Seq[String]) extends GameMessage
  case class UnSubscribe(member: String) extends GameMessage

  case class RequestTsumohai(senderId: String, number: Int) extends GameMessage
  case class ResponseTsumohai(senderId: String, paiList: Seq[MahjongPai], val $type: String = "ResponseTsumohai") extends GameMessage

  case class RequestSutehai(senderId: String, paiList: Seq[MahjongPai]) extends GameMessage
  case class ResponseSutehai(senderId: String, paiList: Seq[MahjongPai]) extends GameMessage

  case class RequestPon(senderId: String, pai: MahjongPai) extends GameMessage
  case class ResponsePon(senderId: String, pai: MahjongPai) extends GameMessage

  case class RequestKan(senderId: String, pai: MahjongPai) extends GameMessage
  case class ResponseKan(senderId: String, pai: MahjongPai) extends GameMessage

  case class RequestChi(senderId: String, pai: MahjongPai) extends GameMessage
  case class ResponseChi(senderId: String, pai: MahjongPai) extends GameMessage

  case class RequestReach(senderId: String, pai: MahjongPai) extends GameMessage
  case class ResponseReach(senderId: String, pai: MahjongPai) extends GameMessage

  case class RequestRon(senderId: String, pai: MahjongPai) extends GameMessage
  case class ResponseRon(senderId: String, pai: MahjongPai) extends GameMessage

  case class RequestTsumo(senderId: String, pai: MahjongPai) extends GameMessage
  case class ResponseTsumo(senderId: String, pai: MahjongPai) extends GameMessage

  case class Syn(senderId: String, eventSeq: Long) extends GameMessage
  case class Ack(senderId: String, eventSeq: Long) extends GameMessage

  object DefaultTypeProtocol extends DefaultJsonProtocol {
    implicit val format = jsonFormat1(DefaultType.apply)
  }

  object RequestTsumohaiProtocol extends DefaultJsonProtocol {
    implicit val format = jsonFormat2(RequestTsumohai.apply)
  }

  object ResponseTsumohaiProtocol extends DefaultJsonProtocol {
    implicit val paiTypeFormat = jsonFormat1(PaiType.apply)
    implicit val mahjongPaiFormat = jsonFormat10(MahjongPai.apply)
    implicit val format = jsonFormat3(ResponseTsumohai.apply)
  }

  object RequestSutehaiProtocol extends DefaultJsonProtocol {
    implicit val paiTypeFormat = jsonFormat1(PaiType.apply)
    implicit val mahjongPaiFormat = jsonFormat10(MahjongPai.apply)
    implicit val format = jsonFormat2(ResponseSutehai.apply)
  }

  object ResponseSutehaiProtocol extends DefaultJsonProtocol {
    implicit val paiTypeFormat = jsonFormat1(PaiType.apply)
    implicit val mahjongPaiFormat = jsonFormat10(MahjongPai.apply)
    implicit val format = jsonFormat2(ResponseSutehai.apply)
  }

  object RequestPonProtocol extends DefaultJsonProtocol {
    implicit val paiTypeFormat = jsonFormat1(PaiType.apply)
    implicit val mahjongPaiFormat = jsonFormat10(MahjongPai.apply)
    implicit val format = jsonFormat2(ResponsePon.apply)
  }

  object ResponsePonProtocol extends DefaultJsonProtocol {
    implicit val paiTypeFormat = jsonFormat1(PaiType.apply)
    implicit val mahjongPaiFormat = jsonFormat10(MahjongPai.apply)
    implicit val format = jsonFormat2(ResponsePon.apply)
  }

  object RequestKanProtocol extends DefaultJsonProtocol {
    implicit val paiTypeFormat = jsonFormat1(PaiType.apply)
    implicit val mahjongPaiFormat = jsonFormat10(MahjongPai.apply)
    implicit val format = jsonFormat2(ResponseKan.apply)
  }

  object ResponseKanProtocol extends DefaultJsonProtocol {
    implicit val paiTypeFormat = jsonFormat1(PaiType.apply)
    implicit val mahjongPaiFormat = jsonFormat10(MahjongPai.apply)
    implicit val format = jsonFormat2(ResponseKan.apply)
  }

  object RequestChiProtocol extends DefaultJsonProtocol {
    implicit val paiTypeFormat = jsonFormat1(PaiType.apply)
    implicit val mahjongPaiFormat = jsonFormat10(MahjongPai.apply)
    implicit val format = jsonFormat2(ResponseChi.apply)
  }

  object ResponseChiProtocol extends DefaultJsonProtocol {
    implicit val paiTypeFormat = jsonFormat1(PaiType.apply)
    implicit val mahjongPaiFormat = jsonFormat10(MahjongPai.apply)
    implicit val format = jsonFormat2(ResponseChi.apply)
  }

  object RequestReachProtocol extends DefaultJsonProtocol {
    implicit val paiTypeFormat = jsonFormat1(PaiType.apply)
    implicit val mahjongPaiFormat = jsonFormat10(MahjongPai.apply)
    implicit val format = jsonFormat2(ResponseReach.apply)
  }

  object ResponseReachProtocol extends DefaultJsonProtocol {
    implicit val paiTypeFormat = jsonFormat1(PaiType.apply)
    implicit val mahjongPaiFormat = jsonFormat10(MahjongPai.apply)
    implicit val format = jsonFormat2(ResponseReach.apply)
  }

  object RequestRonProtocol extends DefaultJsonProtocol {
    implicit val paiTypeFormat = jsonFormat1(PaiType.apply)
    implicit val mahjongPaiFormat = jsonFormat10(MahjongPai.apply)
    implicit val format = jsonFormat2(ResponseRon.apply)
  }

  object ResponseRonProtocol extends DefaultJsonProtocol {
    implicit val paiTypeFormat = jsonFormat1(PaiType.apply)
    implicit val mahjongPaiFormat = jsonFormat10(MahjongPai.apply)
    implicit val format = jsonFormat2(ResponseRon.apply)
  }

  object RequestTsumoProtocol extends DefaultJsonProtocol {
    implicit val paiTypeFormat = jsonFormat1(PaiType.apply)
    implicit val mahjongPaiFormat = jsonFormat10(MahjongPai.apply)
    implicit val format = jsonFormat2(ResponseTsumo.apply)
  }

  object ResponseTsumoProtocol extends DefaultJsonProtocol {
    implicit val paiTypeFormat = jsonFormat1(PaiType.apply)
    implicit val mahjongPaiFormat = jsonFormat10(MahjongPai.apply)
    implicit val format = jsonFormat2(ResponseTsumo.apply)
  }

  object SynProtocol extends DefaultJsonProtocol {
    implicit val format = jsonFormat2(Syn.apply)
  }

  object AckProtocol extends DefaultJsonProtocol {
    implicit val format = jsonFormat2(Ack.apply)
  }

}