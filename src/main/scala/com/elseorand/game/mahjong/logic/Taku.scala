package com.elseorand.game.mahjong.logic

import com.elseorand.game.mahjong.entity.User
import com.elseorand.game.mahjong.entity.GameBaEntity

sealed trait Taku

case class Taku4(members: Seq[User]) extends Taku {

}

sealed trait UserInterface {
  val otherHiddenPaiList: Seq[Pai] = for(i <- 1 to 13) yield HiddenPai()

  def myPaiList(): Seq[MahjongPai]
  def otherPaiList(kaze: Kaze): Seq[Pai] = otherHiddenPaiList
  def pretendPaiList(kaze: Kaze): Seq[Pai] = otherHiddenPaiList
}

case class UserInterfaceNoCheat(val user: User, val kaze: Kaze, val gameBa: GameBaEntity ) extends UserInterface {
  def myPaiList(): Seq[MahjongPai] = gameBa.haiList(kaze)

}
