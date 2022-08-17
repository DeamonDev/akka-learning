ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.8"
val akkaVersion = "2.6.19"

lazy val root = (project in file("."))
  .settings(
    name := "akka-learning",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
      "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test,
      "org.scalatest" %% "scalatest" % "3.2.12" % Test,
      "ch.qos.logback" % "logback-classic" % "1.2.11"
    )
  )
