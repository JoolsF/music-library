package com.joolsf

import cats.effect.IO
import cats.implicits.toSemigroupKOps
import com.joolsf.model.Foo
import io.circe.generic.codec.DerivedAsObjectCodec.deriveCodec
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe.jsonOf
import org.http4s.dsl.io._
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.middleware.Logger
import org.http4s.{EntityDecoder, HttpRoutes}
import org.typelevel.log4cats.SelfAwareStructuredLogger

case class Server(database: Database, logger: SelfAwareStructuredLogger[IO]) {

  def endpoints: HttpRoutes[IO] = healthRoutes <+> testRoutes

  implicit val WithdrawalResponseEncoder: EntityDecoder[IO, Foo] =
    jsonOf[IO, Foo]

  def run(apiConfig: ApiConfig): IO[Nothing] = {
    val app =
      Logger.httpApp(logHeaders = true, logBody = true)(endpoints.orNotFound)

    EmberServerBuilder
      .default[IO]
      .withHost(apiConfig.host)
      .withPort(apiConfig.port)
      .withHttpApp(app)
      .build
      .useForever
  }

  val healthRoutes: HttpRoutes[IO] =
    HttpRoutes.of[IO] { case _ @GET -> Root / "ping" =>
      Ok("pong")
    }

  val testRoutes: HttpRoutes[IO] =
    HttpRoutes.of[IO] {
      case message @ POST -> Root / "test" / "foo" =>
        message.decodeStrict[Foo] { foo: Foo =>
          logger.info(s"Saving foo[${foo.id}]") >>
            database.saveFoo(foo) >> NoContent()
        }
      case _ @GET -> Root / "test" / "foo" / id =>
        logger.info(s"Attempting to get foo[$id]") >>
          database.getFoo(id).flatMap {
            case Some(foo) => Ok(foo)
            case None      => NotFound(s"Cannot find foo[$id]")
          }

    }

}
