package com.elseorand.game.mahjong.entity

import com.elseorand.game.mahjong.logic._

sealed trait GameBaEntity {
  def omoteDora(): Seq[MahjongPai]
  def uraDora(): Seq[MahjongPai]
  /**
   * 風毎の牌を返す
   */
  def haiList(kaze: Kaze): Seq[MahjongPai]
  def tsumoHai(number: Int, sort: Boolean = false): Seq[MahjongPai]
  def start(): Unit
}

case class GameBa4Entity(id: GameId, allPaiList: Seq[MahjongPai], val players: Seq[(Receiver, Kaze)]) extends GameBaEntity {
  import java.util.concurrent.CopyOnWriteArrayList
  import scala.collection.JavaConverters._

  val OmoteKanDora: Seq[MahjongPai] = allPaiList.slice(0, 4)
  val UraKanDora: Seq[MahjongPai] = allPaiList.slice(4, 8)
  val RinsyanHai: Seq[MahjongPai] = allPaiList.slice(8, 12)
  val Doras: Seq[MahjongPai] = allPaiList.slice(12, 14)

  val ranges= Seq((14, 27), (27, 40), (40, 53), (53, 66))
  val paiListByKaze: Map[Kaze, (Receiver, CopyOnWriteArrayList[MahjongPai])] = players.zip(ranges)
    .map((player_range: ((Receiver, Kaze), (Int, Int))) => {
      val player = player_range._1
      val range = player_range._2
      val paiList = new CopyOnWriteArrayList[MahjongPai]()
      paiList.addAll(allPaiList.slice(range._1, range._2).sortBy(_.defaultSorting).asJava)
      (player._2, (player._1, paiList))
    }).toMap;
  @volatile
  var pipaiList = allPaiList.drop(67)
  def omoteDora(): Seq[MahjongPai] = Seq(Doras(0))
  def uraDora(): Seq[MahjongPai] = Seq(Doras(1))
  def haiList(kaze: Kaze): Seq[MahjongPai] = Seq.empty

  def tsumoHai(number: Int, sort: Boolean = false): Seq[MahjongPai] = {
    synchronized {
      if (pipaiList.size >= number){
        val (subList, restList) = pipaiList.splitAt(number)
        pipaiList = restList
        if(sort) {
          subList.sortBy(_.defaultSorting)
        } else {
          subList
        }
      } else {
        Seq.empty
      }
    }
//    if (sort) tsumoHai.sortBy(_.defaultSorting)
//    else tsumoHai
  }

  def start(): Unit = {
    players.foreach(playerKaze => {
      playerKaze._2 match {
        case Ton => playerKaze._1 ! Protocol.YouHaveTsumohai(tsumoHai(14, true))
        case _ => playerKaze._1 ! Protocol.YouHaveTsumohai(tsumoHai(13, true))
      }

    })
  }

}
