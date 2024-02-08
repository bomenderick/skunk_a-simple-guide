ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.12"

lazy val root = (project in file("."))
  .settings(
    name := "skunk_a-simple-guide",
    libraryDependencies ++= Seq(
      "org.tpolecat" %% "skunk-core" % "0.0.26",
      "io.estatico" %% "newtype" % "0.4.4",
      "com.github.pureconfig" %% "pureconfig" % "0.14.1",
      "com.github.pureconfig" %% "pureconfig-cats-effect" % "0.14.1"
    ),
    scalacOptions ++= Seq(
      "-Ymacro-annotations",
    )
  )
