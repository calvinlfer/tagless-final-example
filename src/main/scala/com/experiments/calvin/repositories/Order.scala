package com.experiments.calvin.repositories

import java.util.UUID

sealed trait OrderStatus
case object Approved extends OrderStatus
case object Placed extends OrderStatus
case object Shipped extends OrderStatus
case object Delivered extends OrderStatus

case object InvalidStatus extends RuntimeException("invalid status")

object OrderStatus {
  implicit class OrderStatusOps(s: OrderStatus) {
    def unsafeStatus: String = s match {
      case Approved => "A"
      case Placed => "P"
      case Shipped => "S"
      case Delivered => "D"
    }
  }

  def safeStatus(statusString: String): OrderStatus = statusString match {
    case "A" => Approved
    case "P" => Placed
    case "S" => Shipped
    case "D" => Delivered
  }
}

case class Order(id: UUID, status: OrderStatus, details: String)

