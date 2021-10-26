lazy val root = project
  .in(file("."))
  .settings(
    name    := "unbiased-bbc",
    version := "0.0.0",
    organization := "ru.ifmo",
    libraryDependencies ++= Seq(
      "com.github.sbt" % "junit-interface" % "0.13.2" % "test",
      "org.apache.commons" % "commons-math3" % "3.6.1"
    ),
    autoScalaLibrary := false,
    crossPaths := false,
    fork := true
  )
