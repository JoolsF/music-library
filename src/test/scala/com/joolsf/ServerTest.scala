package com.joolsf

import org.http4s.Uri

class ServerTest extends munit.FunSuite {

  val authUri =
    "https://accounts.spotify.com/authorize?response_type=code&client_id=f76ab42bbf37451d9de6fe41f1ebf5d7&scope=user-read-private&redirect_uri=http://example.com/callback/&state=9876&show_dialog=false"

  test("example-test") {
    val uri = Uri.fromString(authUri).toTry.toEither
    uri match {
      case Left(e) => println(s"!! ERROR: $e")
      case _       => println("OK")
    }
    assertEquals(uri.isRight, true)

  }
}
