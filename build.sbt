lazy val root = project
  .in(file("."))
  .settings(
      name    := "unbiased-bbc",
      version := "0.0.0",
      organization := "ru.ifmo",
      libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % "test",
      autoScalaLibrary := false,
      crossPaths := false,
      fork := true
  )
