package client

import scala.concurrent.duration._
import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpEntity, HttpMethods, HttpRequest, HttpResponse, MediaTypes}
import akka.stream.scaladsl.{Sink, Source}

import scala.concurrent.Future

object TransactionGeneratorLoader extends App {
  implicit val system = ActorSystem()
  import system.dispatcher

  val runId = System.currentTimeMillis()

  def generateTransactions(n: Int): String = {
    s"""
       |{
       |  "transaction_count":$n,
       |  "transaction_amount":100.0,
       |  "merchant_count":1
       |}
       |""".stripMargin
  }

  val startNanos = System.nanoTime()

  val batchSize = 100

  def req(i: Int): Future[Done] = {
    val entity =
      HttpEntity(MediaTypes.`application/json`.toContentType, generateTransactions(batchSize))

    val req =
      HttpRequest(HttpMethods.POST, uri = "https://mute-meadow-0538.us-east1.kalixapps.com/generate-transactions", entity = entity)

    Http().singleRequest(req)
      .flatMap { res =>
        if (i % 50 == 0) {
          val lastedNanos = (System.nanoTime() - startNanos)
          println(f"[${i*batchSize}%6d] ${res.status.intValue()} ${(i * batchSize).toFloat * 1000000000 / lastedNanos}%5.2f req/s")
        }
        res.discardEntityBytes().future.map(_ => Done)
      }
  }

  Source(1 to 10000)
    .throttle(15, 1.second)
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
