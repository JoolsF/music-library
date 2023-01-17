package com.joolsf

import cats.effect.IO
import org.typelevel.log4cats.SelfAwareStructuredLogger

object StubbedLogger {

  def create(): SelfAwareStructuredLogger[IO] = new SelfAwareStructuredLogger[IO] {
    override def isTraceEnabled: IO[Boolean] = IO.pure(true)

    override def isDebugEnabled: IO[Boolean] = IO.pure(true)

    override def isInfoEnabled: IO[Boolean] = IO.pure(true)

    override def isWarnEnabled: IO[Boolean] = IO.pure(true)

    override def isErrorEnabled: IO[Boolean] = IO.pure(true)

    override def trace(ctx: Map[String, String])(msg: => String): IO[Unit] = IO.unit

    override def trace(ctx: Map[String, String], t: Throwable)(msg: => String): IO[Unit] = IO.unit

    override def debug(ctx: Map[String, String])(msg: => String): IO[Unit] = IO.unit

    override def debug(ctx: Map[String, String], t: Throwable)(msg: => String): IO[Unit] = IO.unit

    override def info(ctx: Map[String, String])(msg: => String): IO[Unit] = IO.unit

    override def info(ctx: Map[String, String], t: Throwable)(msg: => String): IO[Unit] = IO.unit

    override def warn(ctx: Map[String, String])(msg: => String): IO[Unit] = IO.unit

    override def warn(ctx: Map[String, String], t: Throwable)(msg: => String): IO[Unit] = IO.unit

    override def error(ctx: Map[String, String])(msg: => String): IO[Unit] = IO.unit

    override def error(ctx: Map[String, String], t: Throwable)(msg: => String): IO[Unit] = IO.unit

    override def error(t: Throwable)(message: => String): IO[Unit] = IO.unit

    override def warn(t: Throwable)(message: => String): IO[Unit] = IO.unit

    override def info(t: Throwable)(message: => String): IO[Unit] = IO.unit

    override def debug(t: Throwable)(message: => String): IO[Unit] = IO.unit

    override def trace(t: Throwable)(message: => String): IO[Unit] = IO.unit

    override def error(message: => String): IO[Unit] = IO.unit

    override def warn(message: => String): IO[Unit] = IO.unit

    override def info(message: => String): IO[Unit] = IO.unit

    override def debug(message: => String): IO[Unit] = IO.unit

    override def trace(message: => String): IO[Unit] = IO.unit
  }

}
