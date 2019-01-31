name := "MyGatling"
version := "0.1"
scalaVersion := "2.12.8"

scalacOptions := Seq(
  "-encoding", "UTF-8", "-target:jvm-1.8", "-deprecation",
  "-feature", "-unchecked", "-language:implicitConversions", "-language:postfixOps")

enablePlugins(GatlingPlugin)

libraryDependencies ++= Seq(
  "io.gatling.highcharts" % "gatling-charts-highcharts" % "3.0.3" % "test,it",
  "io.gatling" % "gatling-test-framework" % "3.0.3" % "test,it",
  "org.scalacheck" %% "scalacheck" % "1.14.0" % "test"
)
