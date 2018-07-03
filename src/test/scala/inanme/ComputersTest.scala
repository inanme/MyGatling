package inanme

import java.util.concurrent.atomic.AtomicInteger
import io.gatling.core.Predef._
import io.gatling.core.session._
import io.gatling.http.Predef._
import io.gatling.core.structure.PopulationBuilder
import io.gatling.http.protocol.HttpProtocolBuilder
import scala.concurrent.duration.{Duration, FiniteDuration, _}

trait Scenario {
  def populationBuilder: PopulationBuilder
  def protocolBuilder: Option[HttpProtocolBuilder]
}
case class GetComputers(testDuration: FiniteDuration, userRampDuration: FiniteDuration, userCount: Int) extends Scenario {
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
      .inject(rampUsers(userCount) over userRampDuration)
  val protocolBuilder = Some(http
    .baseURL("http://computer-database.gatling.io")
    .disableCaching
    .shareConnections)
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
class AppResilienceSimulation extends Simulation {
  val testDuration = 10 seconds
  val userRampDuration = 2 seconds

  //
  def scenarios: List[Scenario] = List[Scenario](GetComputers(testDuration, userRampDuration, 6), PrintEveryQuarter(testDuration))

  setUp(scenarios.map(_.populationBuilder))
    .protocols(scenarios.flatMap(_.protocolBuilder).map(_.extraInfoExtractor(record(new AtomicInteger())).protocol))

}
