package inanme

import java.time.LocalDateTime

import inanme.action.LoggingActionBuilder
import io.gatling.core.Predef._
import io.gatling.core.protocol.Protocol
import io.gatling.core.structure._

import scala.concurrent.duration._

class LoggingSimulation extends Simulation {

  val logFeed: Iterator[Map[String, String]] =
    Iterator.continually(Map("log" -> LocalDateTime.now().toString))

  val p1: PopulationBuilder =
    scenario("Logging Simulation")
      .during(1 minutes) {
        pace(400 millis, 600 millis)
          .feed(logFeed)
          .exec {
            LoggingActionBuilder
          }
      }.inject(atOnceUsers(1))

  val p2: PopulationBuilder =
    scenario("Logging Simulation")
      .feed(logFeed)
      .exec {
        LoggingActionBuilder
      }.inject(atOnceUsers(1))

  val p3: PopulationBuilder =
    scenario("Logging Simulation")
      .feed(logFeed)
      .repeat(10) {
        exec {
          LoggingActionBuilder
        }
      }.inject(atOnceUsers(1))

  setUp(p1).protocols(Seq.empty[Protocol])
}
