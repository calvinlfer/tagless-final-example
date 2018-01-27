package com.experiments.calvin.repositories.algebras

import java.util.UUID

import com.experiments.calvin.repositories.Order

import scala.language.higherKinds

trait OrderRepository[F[_]] {
  def put(order: Order): F[Order]
  def get(orderId: UUID): F[Option[Order]]
  def remove(orderId: UUID): F[Option[Order]]
}
