name := "playground"

version := "0.1"

scalaVersion := "2.12.8"

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % "1.5.0",
  "org.scalactic" %% "scalactic" % "3.0.5",
  "org.scalatest" %% "scalatest" % "3.0.5" % "test"
)

resolvers += Resolver.sonatypeRepo("releases")

addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.8")

// if your project uses multiple Scala versions, use this for cross building
addCompilerPlugin("org.spire-math" % "kind-projector" % "0.9.8" cross CrossVersion.binary)

// if your project uses both 2.10 and polymorphic lambdas
libraryDependencies ++= (scalaBinaryVersion.value match {
  case "2.10" =>
    compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full) :: Nil
  case _ =>
    Nil
})

mainClass in (Compile, run) := Some("playground.Main")
