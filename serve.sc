import $ivy.`org.http4s::http4s-core:1.0.0-M30`
import $ivy.`org.http4s::http4s-ember-core:1.0.0-M30`
import $ivy.`org.http4s::http4s-ember-server:1.0.0-M30`
import $ivy.`org.typelevel::cats-effect:3.3.1`
import org.http4s.ember.server
import org.http4s.HttpApp
import org.http4s.server.staticcontent._
import org.http4s.server.Server
import com.comcast.ip4s.Port
import com.comcast.ip4s.Host
import cats.effect.IO
import cats.effect.ExitCode
import cats.effect.unsafe.implicits.global

@main
def run() = server.EmberServerBuilder
  .default[IO]
  .withHost(Host.fromString("0.0.0.0").get)
  .withPort(Port.fromInt(8989).get)
  .withHttpApp(fileService[IO](FileService.Config("./dist")).orNotFound)
  .build
  .use(_ => IO.never)
  .as(ExitCode.Success)
  .unsafeRunSync()
