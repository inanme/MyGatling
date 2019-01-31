import org.slf4j.LoggerFactory
import scala.concurrent.duration._

package object inanme {
  val logger = LoggerFactory.getLogger("app")

  implicit class PercentageOps(val duration: Duration) extends AnyVal {
    def percentage(percentage: Double): Duration = {
      (duration.toSeconds * percentage / 100D) seconds
    }
  }

}
