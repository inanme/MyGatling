package inanme
package action

import java.util.concurrent.TimeUnit

import inanme.protocol._
import io.gatling.commons.stats.{KO, OK}
import io.gatling.commons.util.DefaultClock
import io.gatling.commons.validation.Validation
import io.gatling.core.CoreComponents
import io.gatling.core.action.{Action, ExitableAction}
import io.gatling.core.action.builder.ActionBuilder
import io.gatling.core.structure.ScenarioContext
import io.gatling.core.util.NameGen
import io.gatling.core.session._
import io.gatling.core.stats.StatsEngine

case class ProducerRecord[K, V](topic: String, k: Option[K], v: V) {
  def this(topic: String, v: V) = this(topic, None, v)
}

trait Producer[K, V]

case class KafkaProducer[K, V](props: Map[String, Object]) extends Producer[K, V] {
  def close(): Unit = {
    println("producer.close")
  }
}

case class KafkaAttributes[K, V](requestName: Expression[String],
                                 key: Option[Expression[K]],
                                 payload: Expression[V])

case class KafkaRequestBuilder(requestName: Expression[String]) {

  def send[V](payload: Expression[V]): KafkaActionBuilder[_, V] =
    send(payload, None)

  def send[K, V](key: Expression[K], payload: Expression[V]): KafkaActionBuilder[K, V] =
    send(payload, Some(key))

  private def send[K, V](payload: Expression[V], key: Option[Expression[K]]): KafkaActionBuilder[K, V] =
    new KafkaActionBuilder(KafkaAttributes(requestName, key, payload))

}

class KafkaActionBuilder[K, V](kafkaAttributes: KafkaAttributes[K, V]) extends ActionBuilder {

  override def build(ctx: ScenarioContext, next: Action): Action = {
    import ctx._

    val kafkaComponents: KafkaComponents = protocolComponentsRegistry.components(KafkaProtocol.KafkaProtocolKey)
    val kafkaProtocol = kafkaComponents.kafkaProtocol
    val producer = new KafkaProducer[K, V](kafkaProtocol.properties)

    coreComponents.actorSystem.registerOnTermination(producer.close())

    new KafkaAction(
      producer,
      kafkaAttributes,
      coreComponents,
      kafkaProtocol,
      throttled,
      next
    )

  }

}

class KafkaAction[K, V](val producer: KafkaProducer[K, V],
                        val kafkaAttributes: KafkaAttributes[K, V],
                        val coreComponents: CoreComponents,
                        val kafkaProtocol: KafkaProtocol,
                        val throttled: Boolean,
                        val next: Action)
  extends ExitableAction with NameGen {

  val statsEngine: StatsEngine = coreComponents.statsEngine
  val clock = new DefaultClock
  override val name: String = genName("kafkaRequest")

  override def execute(session: Session): Unit = recover(session) {

    kafkaAttributes requestName session flatMap { requestName =>

      val outcome =
        sendRequest(
          requestName,
          producer,
          kafkaAttributes,
          throttled,
          session)

      outcome.onFailure(
        errorMessage => statsEngine.reportUnbuildableRequest(session, requestName, errorMessage)
      )
      outcome
    }
  }

  private def sendRequest(requestName: String,
                          producer: Producer[K, V],
                          kafkaAttributes: KafkaAttributes[K, V],
                          throttled: Boolean,
                          session: Session): Validation[Unit] = {

    kafkaAttributes payload session map { payload =>

      val record = kafkaAttributes.key match {
        case Some(k) =>
          new ProducerRecord[K, V](kafkaProtocol.topic, k(session).toOption, payload)
        case None =>
          new ProducerRecord[K, V](kafkaProtocol.topic, payload)
      }

      val requestStartDate = clock.nowMillis
      val e: Exception = null

      //      producer.send(record, new Callback() {
      TimeUnit.MILLISECONDS.sleep(scala.util.Random.nextInt(1300))
      val requestEndDate = clock.nowMillis
      statsEngine.logResponse(
        session,
        requestName,
        startTimestamp = requestStartDate,
        endTimestamp = requestEndDate,
        if (e == null) OK else KO,
        None,
        if (e == null) None else Some(e.getMessage)
      )

      if (throttled) {
        coreComponents.throttler.throttle(session.scenario, () => next ! session)
      } else {
        next ! session
      }

    }

  }

}

