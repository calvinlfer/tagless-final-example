package com.experiments.calvin

import java.util.UUID

import com.experiments.calvin.repositories.algebras.OrderRepository
import com.experiments.calvin.repositories.interpreters.sql.{PostgresMonixOrderRepository, PostgresOrderTable}
import com.experiments.calvin.repositories.{Order, _}
import com.experiments.calvin.services.OrderService
import com.typesafe.config.ConfigFactory
import monix.cats._
import monix.eval.Task
import monix.execution.Scheduler
import slick.jdbc.PostgresProfile.backend.DatabaseDef
import slick.jdbc.PostgresProfile.api._
import slick.jdbc.meta.MTable

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.Random

object PostgresApp extends App {
  implicit val scheduler: Scheduler = monix.execution.Scheduler.Implicits.global

  val config = ConfigFactory.load()
  val database: DatabaseDef = Database.forConfig("db")
  val query: TableQuery[PostgresOrderTable] =
    TableQuery(PostgresOrderTable(config.getString("database.table-name"), config.getString("database.schema-name")))

  // table creation
  val tableCreationInstruction: DBIO[Unit] = for {
    createTable <- MTable.getTables(query.baseTableRow.tableName).map(_.isEmpty)
    _           <- if (createTable) query.schema.create
                   else DBIO.successful(s"Table already exists")
  } yield ()

  Await.result(database.run(tableCreationInstruction), 30.seconds)

  // commit to the Postgres Monix Task based implementation at the end of the world
  val monixOrderRepository: OrderRepository[Task] = PostgresMonixOrderRepository(query, database)
  val orderService: OrderService[Task] = OrderService(monixOrderRepository)

  // Usage
  val orderId = UUID.randomUUID()
  val result: Task[Option[Order]] = for {
    _             <- orderService.create(Order(orderId, Placed, s"random-order-${Random.nextInt(1000)}"))
    _             <- orderService.updateStatus(orderId, Shipped)
    updatedOrder  <- orderService.get(orderId)
  } yield updatedOrder

  println {
    Await.result(result.runAsync: Future[Option[Order]], 30.seconds)
  }
}
