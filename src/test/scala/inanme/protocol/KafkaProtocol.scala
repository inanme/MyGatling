package inanme.protocol

import io.gatling.core.CoreComponents
import io.gatling.core.config.GatlingConfiguration
import io.gatling.core.protocol.{Protocol, ProtocolKey}

import io.gatling.core.protocol.ProtocolComponents
import io.gatling.core.session.Session

object KafkaProtocol {

  def apply(configuration: GatlingConfiguration): KafkaProtocol = KafkaProtocol(
    topic = "",
    properties = Map()
  )

  val KafkaProtocolKey = new ProtocolKey[KafkaProtocol, KafkaComponents] {

    type Protocol = KafkaProtocol
    type Components = KafkaComponents

    override def protocolClass: Class[io.gatling.core.protocol.Protocol] = classOf[KafkaProtocol].asInstanceOf[Class[io.gatling.core.protocol.Protocol]]

    override def defaultProtocolValue(configuration: GatlingConfiguration): KafkaProtocol = apply(configuration)

    override def newComponents(coreComponents: CoreComponents): KafkaProtocol => KafkaComponents = KafkaComponents

  }
}

case class KafkaProtocol(topic: String, properties: Map[String, Object]) extends Protocol {
  def topic(topic: String): KafkaProtocol = copy(topic = topic)

  def properties(properties: Map[String, Object] = Map[String, Object]()): KafkaProtocol = copy(properties = properties)
}


case class KafkaComponents(kafkaProtocol: KafkaProtocol) extends ProtocolComponents {
  override def onStart: Session => Session = ProtocolComponents.NoopOnStart

  override def onExit: Session => Unit = ProtocolComponents.NoopOnExit
}

object KafkaProtocolBuilder {
  implicit def toKafkaProtocol(builder: KafkaProtocolBuilder): KafkaProtocol = builder.build

  def apply(implicit configuration: GatlingConfiguration): KafkaProtocolBuilder =
    KafkaProtocolBuilder(KafkaProtocol(configuration))
}

case class KafkaProtocolBuilder(kafkaProtocol: KafkaProtocol) {
  def build: KafkaProtocol = kafkaProtocol
}
