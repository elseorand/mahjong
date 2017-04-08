package com.elseorand.game.mahjong.entity

import akka.actor._

case class User(val userId: UserId, val name: String, actor: ActorRef) {

}
