package com.experiments.calvin

import java.util.UUID

import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration
import com.amazonaws.regions.Regions
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsyncClientBuilder
import com.experiments.calvin.repositories.algebras.OrderRepository
import com.experiments.calvin.repositories.interpreters.dynamo.DynamoFutureOrderRepository
import com.experiments.calvin.repositories.{Order, _}
import com.experiments.calvin.services.OrderService
import cats.instances.future._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._
import scala.util.Random

object DynamoApp extends App {
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global
  val dynamoClient = AmazonDynamoDBAsyncClientBuilder
    .standard()
    .withEndpointConfiguration(new EndpointConfiguration("http://localhost:8000", Regions.US_EAST_1.getName))
    .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials("dev", "dev")))
    .build()

  // commit to the DynamoDB implementation at the end of the world
  val dynamoOrderRepository: OrderRepository[Future] = DynamoFutureOrderRepository("orders_by_id", dynamoClient)
  val orderService: OrderService[Future] = OrderService(dynamoOrderRepository)

  // Usage
  val orderId = UUID.randomUUID()
  val result = for {
    _             <- orderService.create(Order(orderId, Placed, s"random-order-${Random.nextInt(1000)}"))
    _             <- orderService.updateStatus(orderId, Shipped)
    updatedOrder  <- orderService.get(orderId)
  } yield updatedOrder

  println {
    Await.result(result, 30.seconds)
  }
  sys.exit(0)
}
