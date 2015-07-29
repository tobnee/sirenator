name := """sirenator"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  json,
  cache,
  ws,
  specs2 % Test,
  "com.yetu" %% "siren-scala" % "0.5.1",
  "org.webjars" %% "webjars-play" % "2.4.0-1",
  "org.webjars" % "bootstrap" % "3.3.5",
  "org.webjars" % "jquery-pjax" % "1.9.5",
  "org.webjars" % "normalize.css" % "3.0.2",
  "org.webjars.bower" % "google-code-prettify" % "1.0.4"
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

resolvers += "yetu Bintray Repo" at "http://dl.bintray.com/yetu/maven/"

// Play provides two styles of routers, one expects its actions to be injected, the
routesGenerator := InjectedRoutesGenerator
