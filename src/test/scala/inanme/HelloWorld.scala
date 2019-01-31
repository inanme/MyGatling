package inanme

import java.util.concurrent.atomic.AtomicInteger

import inanme.Predef._
import inanme.action.KafkaRequestBuilder
import inanme.protocol.KafkaProtocol
import io.gatling.core.Predef._
import io.gatling.core.structure.PopulationBuilder

import scala.concurrent.duration._

class HelloWorld extends Simulation {
  val counter = new AtomicInteger(0)

  val messageFeed: Iterator[Map[String, String]] =
    Iterator.continually(Map("message" -> counter.getAndIncrement.toString))

  val scn: PopulationBuilder =
    scenario("Kafka Loader")
      .feed(messageFeed)
      .exec(
        kafka("kafka-load").send[String](session => session("message").as[String])
      ).inject(constantUsersPerSec(1) during (1 minute))

  val protocol: KafkaProtocol = kafka
    .topic("some topic")
    .properties()

  setUp(scn).protocols(protocol)

}
