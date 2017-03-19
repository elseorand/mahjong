package com.elseorand.game.mahjong.logic

import com.elseorand.game.mahjong.entity.User
import com.elseorand.game.mahjong.entity.GameBaEntity

import java.util.concurrent.ConcurrentMap
import java.util.concurrent.ConcurrentHashMap
import scala.collection.concurrent.Map
import scala.collection.convert.decorateAsScala._

sealed trait Taku {
  def has(user: User): Boolean
  def add(user: User): Option[Taku]
}

case class Taku4(val id: Long, itKaze: Iterator[Kaze]) extends Taku {
  val concrrentMap: ConcurrentMap[User, Kaze] = new ConcurrentHashMap()
  val members: Map[User, Kaze] = concrrentMap.asScala

  def has(user: User) = members.contains(user)

  def add(user: User) = {
    if(itKaze.hasNext) {
      concrrentMap.computeIfAbsent(user, u => itKaze.next)
      Option(this)
    } else None
  }

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
