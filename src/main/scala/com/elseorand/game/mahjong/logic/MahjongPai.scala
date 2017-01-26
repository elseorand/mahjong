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

sealed trait PaiTypeTrait
case class PaiType(name: String) extends PaiTypeTrait

sealed trait KazeTrait
case class Kaze(name: String) extends KazeTrait

object MahjongPaiJsonProtocol extends DefaultJsonProtocol {
  implicit val paiTypeFormat = jsonFormat1(PaiType.apply)
  implicit val mahjongPaiFormat = jsonFormat10(MahjongPai.apply)
}
