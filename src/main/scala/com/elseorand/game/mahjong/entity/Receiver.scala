package com.elseorand.game.mahjong.entity

import com.elseorand.game.mahjong.logic.Protocol

trait Receiver {
  def ! (msg: Protocol.GameMessage): Unit
  def id(): UserId
}
