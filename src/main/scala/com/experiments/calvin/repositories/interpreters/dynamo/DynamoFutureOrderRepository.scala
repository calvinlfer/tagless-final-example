package com.experiments.calvin.repositories.interpreters.dynamo

import java.util.UUID

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsync
import com.experiments.calvin.repositories.Order
import com.experiments.calvin.repositories.algebras.OrderRepository
import com.gu.scanamo._
import com.gu.scanamo.syntax._
import com.gu.scanamo.error.DynamoReadError
import com.gu.scanamo.error.DynamoReadError._
import DynamoFutureOrderRepository._

import scala.concurrent.{ExecutionContext, Future}

class DynamoFutureOrderRepository(tableName: String, client: AmazonDynamoDBAsync)(implicit ec: ExecutionContext)
  extends OrderRepository[Future] {
  private val table: Table[Order] = Table[Order](tableName)
  private val pKey: Symbol = 'id

  override def put(order: Order): Future[Order] = {
    val putInstruction = table.put(order)
    ScanamoAsync
      .exec(client)(putInstruction)
      .map(_ => order)
  }

  override def get(orderId: UUID): Future[Option[Order]] = {
    val getInstruction = table.get(pKey -> orderId)
    ScanamoAsync
      .exec(client)(getInstruction)
      .subsumeFailure
  }

  override def remove(orderId: UUID): Future[Option[Order]] = {
    val instruction = for {
      optOrder <- table.get(pKey -> orderId)
      _        <- table.delete(pKey -> orderId)
    } yield optOrder

    ScanamoAsync
      .exec(client)(instruction)
      .subsumeFailure
  }
}

object DynamoFutureOrderRepository {
  def apply(tableName: String, client: AmazonDynamoDBAsync)(implicit ec: ExecutionContext): DynamoFutureOrderRepository =
    new DynamoFutureOrderRepository(tableName, client)

  implicit class ScanamoResultOps[A](fe: Future[Option[Either[DynamoReadError, A]]]) {
    def subsumeFailure(implicit ec: ExecutionContext): Future[Option[A]] =
      fe.flatMap {
        case None => Future.successful(None)
        case Some(Left(dynamoReadError)) => Future.failed(new Exception(describe(dynamoReadError)))
        case Some(Right(cardMapping)) => Future.successful(Some(cardMapping))
      }
  }
}