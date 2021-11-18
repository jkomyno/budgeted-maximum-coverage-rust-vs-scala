ThisBuild / useCoursier := false

lazy val root = project
  .in(file("."))
  .settings(
    name := "budgeted-maximum-coverage",
    version := "0.1.0",
    scalaVersion := "3.0.2",

    libraryDependencies += ("com.typesafe.akka" %% "akka-actor-typed" % "2.6.17")
      .cross(CrossVersion.for3Use2_13),
    libraryDependencies += ("com.typesafe.akka" %% "akka-slf4j" % "2.6.17")
      .cross(CrossVersion.for3Use2_13),
    libraryDependencies += ("org.rogach" %% "scallop" % "4.1.0"),
    libraryDependencies += ("ch.qos.logback" % "logback-classic" % "1.2.3" % Runtime),
    libraryDependencies += ("com.typesafe.scala-logging" %% "scala-logging" % "3.9.4")
      .cross(CrossVersion.for3Use2_13)
  )

scalacOptions ++= {
  Seq(
    "-encoding",
    "UTF-8",
    "-feature",
    "-language:implicitConversions",
    // disabled during the migration
    // "-Xfatal-warnings"
  ) ++ 
    (CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((3, _)) => Seq(
        "-unchecked",
        "-source:3.0-migration"
      )
      case _ => Seq(
        "-deprecation",
        "-Xfatal-warnings",
        "-Wunused:imports,privates,locals",
        "-Wvalue-discard"
      )
    })
}
