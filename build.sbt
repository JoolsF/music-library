import org.scalafmt.sbt.ScalafmtPlugin.scalafmtConfigSettings

val Http4sVersion          = "0.23.17"
val Http4sMunitVersion     = "0.12.0"
val CirceVersion           = "0.14.3"
val MunitVersion           = "0.7.29"
val LogbackVersion         = "1.2.11"
val MunitCatsEffectVersion = "1.0.7"
val CirisVersion           = "2.4.0"
val Enumeratum             = "1.7.0"
val FlywayVersion          = "9.10.1"
val DoobieVersion          = "1.0.0-RC2"

lazy val dependencies = Seq(
  "org.http4s"          %% "http4s-ember-server" % Http4sVersion,
  "org.http4s"          %% "http4s-ember-client" % Http4sVersion,
  "org.http4s"          %% "http4s-circe"        % Http4sVersion,
  "org.http4s"          %% "http4s-dsl"          % Http4sVersion,
  "io.circe"            %% "circe-generic"       % CirceVersion,
  "ch.qos.logback"       % "logback-classic"     % LogbackVersion         % Runtime,
  "org.tpolecat"        %% "doobie-core"         % DoobieVersion,
  "org.tpolecat"        %% "doobie-hikari"       % DoobieVersion,
  "org.tpolecat"        %% "doobie-postgres"     % DoobieVersion,
  "org.flywaydb"         % "flyway-core"         % FlywayVersion,
  "is.cir"              %% "ciris"               % CirisVersion,
  "org.scalameta"       %% "munit"               % MunitVersion           % Test,
  "org.typelevel"       %% "munit-cats-effect-3" % MunitCatsEffectVersion % Test,
  "org.tpolecat"        %% "doobie-munit"        % DoobieVersion          % Test,
  "com.alejandrohdezma" %% "http4s-munit"        % Http4sMunitVersion     % Test,
)

val unitTestSettings = List(
  Test / fork               := true,
  Test / testForkedParallel := true,
)

// make test sources available to integration tests
val IntegrationTest = Configurations.IntegrationTest.extend(Test)

val integrationTestSettings = inConfig(IntegrationTest)(
  Defaults.itSettings ++ List(
    IntegrationTest / fork               := true, // ensure SBT releases resources from IOApps on termination,
    IntegrationTest / testForkedParallel := true,
  ) ++ scalafmtConfigSettings,
)

val compilerOptions = List(
  "-Ywarn-value-discard", // Warn if value discarded
  "-Wvalue-discard", //  Warn when an expression e with non-Unit type is adapted by embedding it into a block { e; () } because the expected type is Unit.
  "-Xfatal-warnings", // Compiler warnings cause compilation to fail
)

lazy val root = (project in file("."))
  .settings(
    organization      := "com.joolsf.ec",
    name              := "music-library",
    version           := "0.0.1-SNAPSHOT",
    scalaVersion      := "2.13.10",
    scalafmtOnCompile := true,
    scalacOptions ++= compilerOptions,
    libraryDependencies ++= dependencies,
    addCompilerPlugin("org.typelevel" %% "kind-projector"     % "0.13.2" cross CrossVersion.full),
    addCompilerPlugin("com.olegpy"    %% "better-monadic-for" % "0.3.1"),
    testFrameworks += new TestFramework("munit.Framework"),
  )
  .configs(IntegrationTest)
  .settings(unitTestSettings, integrationTestSettings)
