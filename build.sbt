name := """mr-balance"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.4"

scalaSource in Compile := baseDirectory.value / "mr-trade-library"

libraryDependencies ++= Seq(
  // cache,
  ws,
  guice,
  "org.typelevel" %% "cats" % "0.9.0",
  "com.typesafe.slick" %% "slick" % "3.2.1",
  "com.typesafe.play" %% "play-slick" % "3.0.3",
  "com.typesafe.play" %% "play-slick-evolutions" % "3.0.3",
  "com.github.tminglei" %% "slick-pg" % "0.15.2",
  "com.typesafe.akka" %% "akka-remote" % "2.5.9",
  "com.typesafe.akka" %% "akka-actor" % "2.5.9",
  "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.1" % Test
)

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
// resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"
