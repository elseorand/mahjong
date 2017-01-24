package com.elseorand.game.mahjong.entity

import akka.actor._

case class User(val userId: String, val name: String, actor: ActorRef) {

}
