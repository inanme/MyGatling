name := "MyGatling"
version := "0.1"
scalaVersion := "2.12.6"

scalacOptions := Seq(
  "-encoding", "UTF-8", "-target:jvm-1.8", "-deprecation",
  "-feature", "-unchecked", "-language:implicitConversions", "-language:postfixOps")

enablePlugins(GatlingPlugin)

libraryDependencies ++= Seq(
  "io.gatling.highcharts" % "gatling-charts-highcharts" % "2.3.1" % "test,it",
  "io.gatling" % "gatling-test-framework" % "2.3.1" % "test,it"
)
