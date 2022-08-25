ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.11.12"

lazy val root = (project in file("."))
  .settings(
    name := "WeatherStreamHandler",
    idePackagePrefix := Some("sn.rachitech.weather")
  )

val sparkVersion = "2.4.8"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % sparkVersion,
  "org.apache.spark" %% "spark-sql" % sparkVersion,
  "org.apache.spark" %% "spark-sql-kafka-0-10" % sparkVersion,
  "com.datastax.spark" %% "spark-cassandra-connector" % "2.5.2",
  "com.datastax.cassandra" % "cassandra-driver-core" % "4.0.0",
  "com.typesafe" % "config" % "1.4.2"
)