package inanme

import java.util.concurrent.atomic.AtomicLong

import io.gatling.commons.validation.Validation
import io.gatling.core.Predef._
import io.gatling.core.structure._
import io.gatling.http.Predef._
import io.gatling.http.protocol._
import io.gatling.http.response.Response

class BugSimulation extends Simulation {

  val counter = new AtomicLong(0)

  def log(session: Session, response: Response): Validation[Response] = {
    println(s">>>>>>>>>${counter.getAndIncrement()}:${session.status}")
    response
  }

  val _10Messages_1User: PopulationBuilder =
    scenario("Get page")
      .repeat(10) {
        exec {
          http("get computers")
            .get("/computers")
            .check(status.in(200))
            .check(substring("surely does not exists").find(1))
            .transformResponse(log)
        }
      }
      .inject(atOnceUsers(1))

  val protocol: HttpProtocol = http
    .baseUrl("http://computer-database.gatling.io").disableCaching.shareConnections.build

  setUp(_10Messages_1User).protocols(protocol)
}
