package com.experiments.calvin.repositories.interpreters.sql

import java.util.UUID

import com.experiments.calvin.repositories.{Order, OrderStatus}
import slick.jdbc.PostgresProfile.api._

class PostgresOrderTable(tableName: String, tableSchema: String, tableTag: Tag)
  extends Table[Order](tableTag, Some(tableSchema), tableName) {
  val orderId = column[UUID]("order_id", O.Length(35, varying = false), O.PrimaryKey)
  val status = column[String]("status", O.Length(1, varying = false))
  val details = column[String]("details")

  override def * = (orderId, status, details) <> (
    orderTuple3 => Order(orderTuple3._1, OrderStatus.safeStatus(orderTuple3._2), orderTuple3._3),
    (o: Order) => Some((o.id, o.status.unsafeStatus, o.details))
  )
}

object PostgresOrderTable {
  def apply(tableName: String, tableSchema: String)(tableTag: Tag): PostgresOrderTable =
    new PostgresOrderTable(tableName, tableSchema, tableTag)
}
