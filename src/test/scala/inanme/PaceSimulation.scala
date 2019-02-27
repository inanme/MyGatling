package inanme

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.core.structure._
import io.gatling.http.protocol._

import scala.concurrent.duration._

class PaceSimulation extends Simulation {

  val everySecond: PopulationBuilder =
    scenario("Get page")
      .during(10 seconds) {
        pace(400 millis, 600 millis)
          .exec {
            http("get computers")
              .get("/computers")
              .check(status.in(200))
          }
      }
      .inject(atOnceUsers(1))

  val _1Message_1User: PopulationBuilder =
    scenario("Get page")
      .exec {
        http("get computers")
          .get("/computers")
          .check(status.in(200))
      }
      .inject(atOnceUsers(1))

  val _1Message_10Users: PopulationBuilder =
    scenario("Get page")
      .exec {
        http("get computers")
          .get("/computers")
          .check(status.in(200))
      }
      .inject(atOnceUsers(10))

  val protocol: HttpProtocol = http
    .baseUrl("http://computer-database.gatling.io").disableCaching.shareConnections.build

  setUp(_1Message_10Users).protocols(protocol)
}
