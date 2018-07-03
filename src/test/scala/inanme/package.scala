import java.net.URL
import java.util.concurrent.atomic.AtomicInteger
import io.gatling.http.request.ExtraInfo
import org.slf4j.LoggerFactory
import scala.concurrent.duration._

package object inanme {
  val logger = LoggerFactory.getLogger("app")
  implicit class PercentageOps(val duration: Duration) extends AnyVal {
    def percentage(percentage: Double): Duration = {
      (duration.toSeconds * percentage / 100D) seconds
    }
  }
  def record(counter: AtomicInteger)(extraInfo: ExtraInfo): List[Any] = {
    val host = new URL(extraInfo.request.getUrl).getHost
    val status = extraInfo.status.name
    val responseTime = extraInfo.response.timings.responseTime
    logger.warn(s">>>>>>>>>${counter.getAndIncrement()}:$host:$status:$responseTime")
    List()
  }
}
