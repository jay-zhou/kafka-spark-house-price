import sbt._
import Keys._
import sbtassembly.Plugin._
import AssemblyKeys._

object KafkaandSparkBuild extends Build {
  lazy val buildSettings = Defaults.defaultSettings ++ Seq(
    version := "0.1-SNAPSHOT",
    organization := "com.jzhou",
    scalaVersion := "2.10.4"
  )

  lazy val app = Project(
    "KafkaProducerHouseData",
    file("."),
    settings = buildSettings ++ assemblySettings ++ Seq(
      parallelExecution in Test := false,
      libraryDependencies ++= Seq(
        "org.apache.spark" %% "spark-streaming-kafka" % "1.3.1"
        // spark will already be on classpath when using spark-submit.
        // marked as provided, so that it isn't included in assembly.
        //"org.apache.spark" %% "spark-catalyst" % "1.3.1" % "provided",
        //"org.scalatest" %% "scalatest" % "2.1.5" % "test",
        //exclude("com.codahale.metrics", "metrics-core").
      )
    )
  )

}
