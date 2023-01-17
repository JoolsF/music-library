package com.joolsf

import cats.effect.IO
import cats.implicits._
import ciris._
import com.comcast.ip4s.{Host, IpLiteralSyntax, Port}

case class Config(
  databaseConfig: DatabaseConfig,
  apiConfig: ApiConfig,
)

object Config {

  def load: IO[Config] =
    (DatabaseConfig.load, ApiConfig.load)
      .parMapN { case (databaseConfig, apiConfig) =>
        Config(
          databaseConfig = databaseConfig,
          apiConfig = apiConfig,
        )
      }
      .load[IO]

}

case class ApiConfig(host: Host, port: Port)

object ApiConfig {

  def load: ConfigValue[IO, ApiConfig] =
    env("PORT").map(port => ApiConfig(ipv4"0.0.0.0", port = Port.fromString(port).get))
}

case class DatabaseConfig(
  url: String,
  user: String,
  password: Secret[String],
  schema: String,
  maxConnections: Int,
)

object DatabaseConfig {

  def load: ConfigValue[IO, DatabaseConfig] =
    (
      env("DATABASE_URL").as[String],
      env("DATABASE_USER")
        .as[String],
      env("DATABASE_PASSWORD")
        .as[String]
        .secret,
      env("DATABASE_SCHEMA")
        .as[String],
      env("DATABASE_MAX_CONNECTIONS")
        .as[Int]
        .default(40),
    ).parMapN(DatabaseConfig.apply)
}
