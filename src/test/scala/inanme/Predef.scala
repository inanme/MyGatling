package inanme

import inanme.action.KafkaRequestBuilder
import inanme.protocol.KafkaProtocolBuilder
import io.gatling.core.config.GatlingConfiguration
import io.gatling.core.session.Expression


object Predef {

  def kafka(implicit configuration: GatlingConfiguration): KafkaProtocolBuilder = KafkaProtocolBuilder(configuration)

  def kafka(requestName: Expression[String]): KafkaRequestBuilder = KafkaRequestBuilder(requestName)

}