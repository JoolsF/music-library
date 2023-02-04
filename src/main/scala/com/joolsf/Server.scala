package com.joolsf

import cats.effect.IO
import cats.implicits.toSemigroupKOps
import com.joolsf.model.Foo
import io.circe.generic.codec.DerivedAsObjectCodec.deriveCodec
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe.jsonOf
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.middleware.Logger
import org.http4s.{EntityDecoder, HttpRoutes}
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.headers.Location
import fs2.io.file.Path

case class Server(database: Database, config: Config, logger: SelfAwareStructuredLogger[IO]) {

  def endpoints: HttpRoutes[IO] = healthRoutes <+> testRoutes <+> spotifyRoutes

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

  // val authUri =
  // "https://accounts.spotify.com/authorize?response_type=code&client_id=???&scope=user-read-private&redirect_uri=http://localhost:8888/callback&state=9876&show_dialog=false"

  val spotifyRoutes: HttpRoutes[IO] = {

    val spotifyAuthUriString: String =
      "https://accounts.spotify.com/authorize?" +
        Map(
          "response_type" -> "code",
          "client_id"     -> config.spotifyConfig.clientId,
          "scope" -> "user-read-private", // "user-read-private user-read-email" // TODO pass in multiple scopes - uri parse issue
          "redirect_uri" -> s"localhost:${config.apiConfig.port}/callback",
          "state"        -> scala.util.Random.nextString(16),
        ).map { case (k, v) => s"$k=$v" }.mkString("&")

    val spotifyAuthUri: Uri =
      Uri
        .fromString(
          spotifyAuthUriString,
        )
        .toOption
        .get // todo fix.get

    HttpRoutes.of[IO] {
      // https://github.com/spotify/web-api-examples/blob/master/authentication/authorization_code/public/index.html
      case request @ GET -> Root / "index" =>
        StaticFile
          .fromPath(Path("public/index.html"), Some(request))
          .getOrElseF(InternalServerError()) // In case the file doesn't exist
      // https://github.com/spotify/web-api-examples/blob/master/authentication/authorization_code/app.js
      case request @ GET -> Root / "login" =>
        logger.info(request.toString()) >>
          TemporaryRedirect(
            Location(
              spotifyAuthUri,
            ),
          )
      case request @ GET -> Root / "callback" =>
        logger.info(s"Callback: ${request.toString()}") >>
          Ok("callback worked")
    }
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
