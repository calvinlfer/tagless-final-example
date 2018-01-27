package com.experiments.calvin.repositories.interpreters.sql

import java.util.UUID

import com.experiments.calvin.repositories.Order
import com.experiments.calvin.repositories.algebras.OrderRepository
import monix.eval.Task
import slick.jdbc.PostgresProfile.api._
import slick.jdbc.PostgresProfile.backend.DatabaseDef

class PostgresMonixOrderRepository(query: TableQuery[PostgresOrderTable], db: DatabaseDef) extends OrderRepository[Task] {
  override def put(order: Order): Task[Order] = Task.deferFutureAction { implicit ec =>
    val insertInstruction = query.insertOrUpdate(order)
    db.run(insertInstruction)
      .map(_ => order)
  }

  override def get(orderId: UUID): Task[Option[Order]] = Task.deferFutureAction { implicit ec =>
    val findInstruction = query.filter(_.orderId === orderId).take(1).result
    db.run(findInstruction)
      .map(_.headOption)
  }

  override def remove(orderId: UUID): Task[Option[Order]] = Task.deferFutureAction { implicit ec =>
    val orderQuery = query.filter(_.orderId === orderId)
    val findOrder = orderQuery.take(1).result.map(_.headOption)
    val deleteOrder = orderQuery.delete

    val result: DBIO[Option[Order]] = for {
      optOrder <- findOrder
      _        <- if (optOrder.nonEmpty) deleteOrder
                  else DBIO.successful(())
    } yield optOrder

    db.run(result)
  }
}

object PostgresMonixOrderRepository {
  def apply(query: TableQuery[PostgresOrderTable], db: DatabaseDef): PostgresMonixOrderRepository =
    new PostgresMonixOrderRepository(query, db)
}