package com.elseorand.game.mahjong.ai

import com.elseorand.game.mahjong.entity.{ Receiver, UserId }
import com.elseorand.game.mahjong.logic.Protocol

case class AutoSutehai(val userId: UserId, val name: String) extends Receiver {
  def ! (msg: Protocol.GameMessage): Unit = {
    msg match {
      case Protocol.YouHaveTsumohai(paiList, cmdType) => Console println s"cmdType : $cmdType"


      case _ => Console println "none2"
    }
  }

  def id(): UserId = userId
}
