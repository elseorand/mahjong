package com.elseorand.game.mahjong.logic

import scala.util.Random

import akka.actor._
import akka.stream.OverflowStrategy
import akka.stream.scaladsl._

import com.elseorand.game.mahjong.entity.GameId
import com.elseorand.game.mahjong.entity.GameBaEntity
import com.elseorand.game.mahjong.entity.User
import com.elseorand.game.mahjong.entity.UserId

trait MahjongLogic {

  val random = new Random

  val PAI_TYPES9NUMBER = List(Manzu, Souzu, Pinzu)
  val ALL_PAI_LIST = PAI_TYPES9NUMBER.zipWithIndex.flatMap(paiType_paiIndex => {
    val (paiType, paiIndex)= paiType_paiIndex
    for(index <- 0 to 3; num <- 1 to 9) yield {
      val is1or9 = num == 1 || num == 9;
      MahjongPai(paiType, num, index
        , index + 4 * num + 36 * paiIndex // id
        , 126982 + num + 9 * paiIndex// 126982 is unicode
        , is1or9, !is1or9, is1or9, false, false)
    }
  }) ++ (for(num <- 1 to 7; index <- 0 to 3) yield {
      MahjongPai(Jihai, num, index, index + 4 * num + 108
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
}

object MahjongLogic {
  import java.util.concurrent.atomic.AtomicLong

  import java.util.concurrent.ConcurrentHashMap
  import scala.collection.JavaConverters._
  import scala.collection.concurrent.Map

  val KazeList: Seq[Kaze] = Seq(Ton, Nan, Sya, Bei)

  def apply(): MahjongLogic = {
    new MahjongLogic {}
  }
}
