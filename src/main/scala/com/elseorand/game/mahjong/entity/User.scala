package com.elseorand.game.mahjong.entity

import akka.actor._
import com.elseorand.game.mahjong.logic.Protocol

case class User(val userId: UserId, val name: String, actor: ActorRef) extends Receiver {
  def ! (msg: Protocol.GameMessage): Unit = actor ! msg
  def id(): UserId = userId
}
