package com.joolsf

import cats.effect.implicits.effectResourceOps
import cats.effect.{IO, IOApp, Resource}
import cats.implicits.catsSyntaxTuple3Parallel
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger

object Main extends IOApp.Simple {

  val run: IO[Unit] =
    resources.use { case (config, logger, db) =>
      val server = Server(db, config, logger)

      server.run(config.apiConfig).void

    }

  def resources: Resource[
    IO,
    (Config, SelfAwareStructuredLogger[IO], Database),
  ] =
    Config.load.toResource.flatMap { config =>
      (
        Resource.pure[IO, Config](config),
        Resource.make(Slf4jLogger.create[IO])(_ => IO.unit), // TODO update release method
        Database.resource(config = config.databaseConfig, Database.create),
      ).parTupled
    }

}
