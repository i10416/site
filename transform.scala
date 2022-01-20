// using scala 2.13
// using lib org.planet42::laika-core:0.18.1
// using lib org.planet42::laika-io:0.18.1
// using lib org.typelevel::cats-effect:3.3.4

// import $ivy.`org.planet42::laika-core:0.18.1`
// import $ivy.`org.planet42::laika-preview:0.18.1`
// import $ivy.`org.planet42::laika-io:0.18.1`
// import $ivy.`org.typelevel:::cats-effect:3.3.1`
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
    createTransformer[IO]
      .use { transformer =>
        transformer.fromDirectory(from).toDirectory(to).transform
      }
      .unsafeRunSync()
  }

}
// memo https://highlightjs.org/static/demo/
