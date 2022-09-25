//> using scala "2.13.8"
//> using lib "org.planet42::laika-core:0.18.2"
//> using lib "org.planet42::laika-io:0.18.2"
//> using lib "org.typelevel::cats-effect:3.3.11"

import java.nio.file.Path
import java.nio.file.Files
import cats.effect.{Async, Resource}
import laika.format
import dev.i10416.petit.Petit
import laika.io.api.TreeTransformer
import cats.effect.IO
import laika.api._
import cats.effect.unsafe.implicits.global
import laika.io.implicits._
import laika.io.model.TreeOutput
import laika.io.model.StringTreeOutput
import laika.theme.ThemeProvider
import laika.theme.Theme
import laika.theme.ThemeBuilder
import laika.config.ConfigBuilder
import cats.effect.kernel.Sync
import java.nio.charset.Charset

object Transform {
  private def createTransformer[F[_]: Async]: Resource[F, TreeTransformer[F]] =
    Transformer
      .from(format.Markdown)
      .to(format.HTML)
      .parallel[F]
      .withTheme(Petit)
      .build

  def main(arg: Array[String]): Unit = {
    val (from, to) = arg.toList match {
      case from :: to :: Nil
          if Files.exists(Path.of(from)) && Files.exists(Path.of(to)) =>
        (from, to)
      case _ => ("docs", "deploy/dist")
    }
    Files.write(
      Path.of("docs/directory.conf"),
      s"""|
          |petit.site.host  = "${sys.env
           .get("SITE_HOST")
           .getOrElse("localhost")}"
          |laika.site.metadata.title = "${sys.env
           .get("SITE_TITLE")
           .getOrElse("")}"
          |petit.site.twitter = "${sys.env.get("SITE_TWITTER").getOrElse("")}"
          |petit.site.github = "${sys.env.get("SITE_GITHUB").getOrElse("")}"
          |""".stripMargin.getBytes()
    )
    createTransformer[IO]
      .use { transformer =>
        transformer.fromDirectory(from).toDirectory(to).transform
      }
      .unsafeRunSync()
  }

}
// memo https://highlightjs.org/static/demo/
