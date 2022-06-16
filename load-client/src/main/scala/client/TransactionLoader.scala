package client

import scala.concurrent.duration._
import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpEntity, HttpMethods, HttpRequest, HttpResponse, MediaTypes}
import akka.stream.scaladsl.{Sink, Source}

import scala.concurrent.Future

object TransactionLoader extends App {
  implicit val system = ActorSystem()
  import system.dispatcher

  val runId = System.currentTimeMillis()

  def transaction(i: Int): String = {
    s"""
       |{
       |  "transaction_key": {
       |  "transaction_id":"transaction-$runId-$i",
       |  "service_code":"service-code-1",
       |  "account_from":"account-from-1",
       |  "account_to":"account-to-1"
       |  },
       |  "transaction_amount":100.00,
       |  "merchant_id":"merchant-id-1",
       |  "shop_id":"shop-id-1"
       | }
       | """.stripMargin
  }

  val startNanos = System.nanoTime()

  def req(i: Int): Future[Done] = {
    val entity =
      HttpEntity(MediaTypes.`application/json`.toContentType, transaction(i))

    val req =
      HttpRequest(HttpMethods.POST, uri = "https://quiet-snow-9094.us-east1.kalixapps.com/transaction-topic/create", entity = entity)
//    HttpRequest(HttpMethods.POST, uri = "http://localhost:9000/transaction-topic/create", entity = entity)

    Http().singleRequest(req)
      .flatMap { res =>
        if (i % 5000 == 0) {
          val lastedNanos = (System.nanoTime() - startNanos)
          println(f"[$i%6d] ${res.status.intValue()} ${i.toFloat * 1000000000 / lastedNanos}%5.2f req/s")
        }
        res.discardEntityBytes().future.map(_ => Done)
      }
  }

  Source(1 to 500000)
    .throttle(1000, 1.second)
    .mapAsyncUnordered(150)(i => req(i).recover { exc =>
      println(s"Req $i failed: $exc")
      Done
    })
    .runWith(Sink.ignore)
    .onComplete { res =>
      println(s"Done: $res")
      system.terminate()
    }
}
