//> using repository "https://s01.oss.sonatype.org/content/repositories/snapshots"
//> using scala "3.2.2"
//> using lib "org.typelevel::cats-core:2.9.0"
//> using lib "co.fs2::fs2-core:3.5.0"
//> using lib "co.fs2::fs2-io:3.5.0"
//> using lib "org.typelevel::cats-effect:3.4.6"
//> using lib "org.planet42::laika-core:0.18.2"
//> using lib "org.planet42::laika-io:0.18.2"
//> using lib "org.planet42::laika-preview:0.18.2"
//> using lib "dev.i10416::petit:0.0.0+95-7957ec18-SNAPSHOT"
//> using lib "org.http4s::http4s-core:1.0.0-M38"
//> using lib "org.http4s::http4s-ember-core:1.0.0-M38"
//> using lib "org.http4s::http4s-ember-server:1.0.0-M38"

import cats.*
import cats.effect.*
import cats.effect.unsafe.implicits.global
import cats.syntax.all.*
import com.comcast.ip4s.Host
import com.comcast.ip4s.Port
import dev.i10416.petit.Petit
import fs2.io.Watcher.Event.Created
import fs2.io.Watcher.Event.Deleted
import fs2.io.Watcher.Event.Modified
import fs2.io.Watcher.EventType
import fs2.io.file.Path
import laika.api.*
import laika.ast.TemplateElement
import laika.directive.std.StandardDirectives
import laika.format
import laika.io.api.TreeTransformer
import laika.io.implicits.*
import org.http4s.ember.server
import org.http4s.server.staticcontent._

import scala.concurrent.duration.*

def createTransformer[F[_]: Async]: Resource[F, TreeTransformer[F]] =
  Transformer
    .from(format.Markdown)
    .to(format.HTML)
    .parallel[F]
    .withTheme(Petit)
    .build

def program =
  val port = 8989
  val host = "0.0.0.0"
  val inst = for
    transformer <- fs2.Stream.resource(createTransformer[IO])
    transformOnChange = fs2.io.file
      .Files[IO]
      .watch(
        Path("docs").toNioPath,
        Seq(EventType.Created, EventType.Modified, EventType.Deleted)
      )
      .evalTap(e =>
        for
          _ <- IO.println(s"receive event: $e")
          _ <- IO.println("transforming...")
          _ <- transformer
            .fromDirectory("docs")
            .toDirectory("dist")
            .transform
          _ <- IO.println("finish transforming")
        yield ()
      )
    srv = fs2.Stream.eval(
      server.EmberServerBuilder
        .default[IO]
        .withHost(Host.fromString(host).get)
        .withPort(Port.fromInt(port).get)
        .withHttpApp(fileService[IO](FileService.Config("./dist")).orNotFound)
        .build
        .useForever
    )
    _ <- srv concurrently transformOnChange
  yield ()
  inst.compile.drain

@main def run() = program.unsafeRunSync()
