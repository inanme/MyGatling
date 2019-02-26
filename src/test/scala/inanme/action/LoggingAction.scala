package inanme.action

import io.gatling.commons.stats.{KO, OK}
import io.gatling.commons.util.{Clock, DefaultClock}
import io.gatling.core.session._
import io.gatling.core.action._
import io.gatling.core.Predef._
import io.gatling.core.action.builder.ActionBuilder
import io.gatling.core.structure.ScenarioContext
import io.gatling.core.util.NameGen
import io.gatling.core.stats.StatsEngine

object LoggingActionBuilder extends ActionBuilder {
  override def build(ctx: ScenarioContext, next: Action): Action =
    new LoggingAction(ctx.coreComponents.statsEngine, next)
}

class LoggingAction(override val statsEngine: StatsEngine,
                    override val next: Action,
                    override val clock: Clock = new DefaultClock
                   ) extends ExitableAction with NameGen {

  override val name: String = genName("LoggingRequest")

  override def execute(session: Session): Unit = recover(session) {
    val log = session("log").as[String]
    println(s">>>>>>>>>>>>$log")
    statsEngine.logResponse(
      session,
      "log",
      startTimestamp = System.currentTimeMillis(),
      endTimestamp = System.currentTimeMillis(),
      OK,
      None,
      None
    )
  }

}

