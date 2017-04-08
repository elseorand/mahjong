package com.elseorand.game.mahjong.logic

import com.elseorand.game.mahjong.entity.User
import com.elseorand.game.mahjong.entity.UserId
import com.elseorand.game.mahjong.entity.GameBaEntity
import com.elseorand.game.mahjong.entity.GameBa4Entity
import com.elseorand.game.mahjong.entity.GameId

import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.ConcurrentHashMap
import scala.collection.concurrent.Map
import scala.collection.convert.decorateAsScala._

sealed trait Taku {

  def has(userId: UserId): Boolean
  def newGame(newPaiList: () => Seq[MahjongPai]): GameBaEntity
  def gameOf(gameId: GameId): GameBaEntity
}

case class Taku4(
  val id: Long,
  val gameCounter: AtomicLong,
  val memberList: Seq[(UserId, User)],
  val kazeList: Seq[Kaze],
  val registerNewGame: (GameId, GameBaEntity) => Option[GameBaEntity]) extends Taku {
  import scala.collection.mutable.ListBuffer

  val memberIdList = memberList map( _._1)
  val gameList: ListBuffer[(GameId, GameBaEntity)] = new ListBuffer

  def has(userId: UserId) = memberIdList contains userId

  def newGame(newPaiList: () => Seq[MahjongPai]): GameBa4Entity = {
    val newGameId = GameId(gameCounter.incrementAndGet());
    val newGameBa = GameBa4Entity(newGameId, newPaiList(), memberList.map(_._2).zip(kazeList))
    gameList += ((newGameId, newGameBa))
    newGameBa
  }

  def gameOf(gameId: GameId): GameBaEntity = ???

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
