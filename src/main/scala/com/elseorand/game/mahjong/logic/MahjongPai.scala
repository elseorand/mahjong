package com.elseorand.game.mahjong.logic

import spray.json._

sealed trait Pai {
  def defaultSorting(): Int = 0
}
case class MahjongPai(
  val paiType: PaiType,
  val number: Int,
  val index: Int,
  val id: Int,
  val unicode: Int,
  val yaochu: Boolean,
  val chuchan: Boolean,
  val routou: Boolean,
  val kaze: Boolean,
  val sangen: Boolean
) extends Pai {
  override def defaultSorting(): Int = this.id
}

case class HiddenPai() extends Pai

sealed trait PaiType {
  def name: String
}
case object Manzu extends PaiType {
  def name: String = "Manzu"
}
case object Souzu extends PaiType {
  def name: String = "Souzu"
}
case object Pinzu extends PaiType {
  def name: String = "Pinzu"
}
case object Jihai extends PaiType {
  def name: String = "Jihai"
}

object PaiTypeFormat extends JsonFormat[PaiType] {
  // deserialization code
  override def read(json: JsValue): PaiType = json match {
    case JsString("Manzu") => Manzu
    case JsString("Souzu") => Souzu
    case JsString("Pinzu") => Pinzu
    case JsString("Jihai") => Jihai
    case _ => deserializationError("PaiType expected")
  }

  // serialization code
  override def write(paiType: PaiType): JsValue = JsObject(
    "name" -> JsString(paiType.name)
  )
}

sealed trait Kaze {
  def name: String
}
case object Ton extends Kaze {
  def name: String = "Ton"
}
case object Nan extends Kaze {
  def name: String = "Nan"
}
case object Sya extends Kaze {
  def name: String = "Sya"
}
case object Bei extends Kaze {
  def name: String = "Bei"
}

object KazeFormat extends JsonFormat[Kaze] {
  // deserialization code
  override def read(json: JsValue): Kaze = json match {
    case JsString("Ton") => Ton
    case JsString("Nan") => Nan
    case JsString("Sya") => Sya
    case JsString("Bei") => Bei
    case _ => deserializationError("Kaze expected")
  }

  // serialization code
  override def write(kaze: Kaze): JsValue = JsObject(
    "name" -> JsString(kaze.name)
  )
}

object MahjongPaiJsonProtocol extends DefaultJsonProtocol {
  implicit val paiTypeFormat = PaiTypeFormat
  implicit val mahjongPaiFormat = jsonFormat10(MahjongPai.apply)
}
