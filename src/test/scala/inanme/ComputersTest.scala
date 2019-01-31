package inanme

import java.util.concurrent.atomic.AtomicLong

import io.gatling.commons.validation.Validation
import io.gatling.core.Predef._
import io.gatling.core.session.{Session, _}
import io.gatling.http.Predef._
import io.gatling.core.structure._
import io.gatling.http.protocol.HttpProtocolBuilder
import io.gatling.http.response.Response

import scala.concurrent.duration._

trait Scenario {
  def populationBuilder: PopulationBuilder

  def protocolBuilder: Option[HttpProtocolBuilder]
}

case class GetComputers(testDuration: FiniteDuration,
                        userRampDuration: FiniteDuration,
                        userCount: Int)
  extends Scenario {
  val populationBuilder: PopulationBuilder =
    scenario("Get page")
      .during(testDuration) {
        pace(500 milliseconds, 1000 milliseconds)
          .exec {
            http("get computers")
              .get("/computers")
              .check(status.in(200))
          }
      }
      .inject(rampUsers(userCount) during userRampDuration)
  val scnProtocolBuilder: HttpProtocolBuilder = http
    .baseUrl("http://computer-database.gatling.io").disableCaching.shareConnections
  val protocolBuilder = Some(scnProtocolBuilder)
}

case class PrintEveryQuarter(duration: FiniteDuration) extends Scenario {
  val populationBuilder: PopulationBuilder =
    scenario("Print every quarter")
      .exec(log("1st"))
      .pause(duration.percentage(25))
      .exec(log("2nd"))
      .pause(duration.percentage(25))
      .exec(log("3rd"))
      .pause(duration.percentage(25))
      .exec(log("4th"))
      .pause(duration.percentage(25))
      .exec(log("finished"))
      .inject(atOnceUsers(1))
  val protocolBuilder: Option[HttpProtocolBuilder] = None

  private def log(message: String): Expression[Session] = session => {
    logger.warn(s"<<<<<<<<<$message")
    session
  }
}

class ComputersTest extends Simulation {
  val testDuration = (11 * 60) seconds
  val userRampDuration = (3 * 60) seconds
  val nrUsers = 10

  def scenarios: List[Scenario] = List[Scenario](
    GetComputers(testDuration, userRampDuration, nrUsers),
    PrintEveryQuarter(testDuration)
  )


  def log(counter: AtomicLong)(session: Session, response: Response): Validation[Response] = {
    val host = response.request.getUri.getHost
    val status = response.status
    val responseTime = response.endTimestamp - response.startTimestamp
    logger.warn(s">>>>>>>>>${counter.getAndIncrement()}:$host:$status:$responseTime")
    response
  }

  setUp(scenarios.map(_.populationBuilder))
    .protocols(scenarios.flatMap(_.protocolBuilder).map(_.transformResponse(log(new AtomicLong())).protocol)
    )

}
