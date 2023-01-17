package com.joolsf

import cats.effect.syntax.all._
import cats.effect.{IO, Resource}
import cats.syntax.all._
import com.joolsf.model.Foo
import doobie.hikari.{HikariTransactor, Config => HikariConfig}
import doobie.implicits._
import doobie.{ExecutionContexts, Transactor, Update0}
import org.flywaydb.core.Flyway

trait Database {
  def saveFoo(foo: Foo): IO[Unit]
  def getFoo(id: String): IO[Option[Foo]]
}

object Database {

  object Foo {

    def upsert(foo: Foo): Update0 =
      sql"""INSERT INTO foo (
           |id, bar
           |)
           |VALUES (
           |${foo.id},
           |${foo.bar}
           |)
           |ON CONFLICT (id)
           |DO NOTHING""".stripMargin.update

    def get(id: String): doobie.Query0[Foo] =
      sql"""SELECT id, bar
           |FROM foo
           |WHERE id = $id""".stripMargin.query[Foo]

  }

  def create(xa: Transactor[IO]): Database = new Database {
    override def saveFoo(foo: Foo): IO[Unit] = Foo.upsert(foo).run.transact(xa).void

    override def getFoo(id: String) = Foo.get(id).option.transact(xa)
  }

  def resource[A](config: DatabaseConfig, db: Transactor[IO] => A): Resource[IO, A] =
    IO.blocking {
      Flyway.configure
        .dataSource(
          config.url,
          config.user,
          config.password.value,
        )
        .defaultSchema(config.schema)
        .load
        .migrate
    }.toResource >>
      ExecutionContexts
        .fixedThreadPool[IO](config.maxConnections)
        .flatMap { ec =>
          HikariTransactor.fromConfig(
            HikariConfig(
              jdbcUrl = config.url.some,
              username = config.user.some,
              password = config.password.value.some,
              maximumPoolSize = config.maxConnections.some,
              driverClassName = "org.postgresql.Driver".some,
              schema = config.schema.some,
            ),
            ec,
          )
        }
        .map(db)
}
