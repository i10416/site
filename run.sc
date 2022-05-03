import fs2.io.file.Path
import fs2.io.Watcher.Event.Deleted
import fs2.io.Watcher.Event.Modified
import fs2.io.Watcher.Event.Created
import $ivy.`org.planet42::laika-core:0.18.2`
import $ivy.`org.planet42::laika-preview:0.18.2`
import $ivy.`org.planet42::laika-io:0.18.2`
import $ivy.`org.typelevel::cats-effect:3.3.11`
import $ivy.`co.fs2::fs2-core:3.2.7`
import $ivy.`co.fs2::fs2-io:3.2.7`
import $ivy.`dev.i10416::petit:0.0.0+60-b1a4fe67+20220503-2103-SNAPSHOT`

import cats.effect.{IO, Async, Sync, Resource}
import laika.ast.TemplateElement
import laika.format
import dev.i10416.petit.Petit
import laika.io.api.TreeTransformer
import laika.directive.std.StandardDirectives
import laika.markdown.github.GitHubFlavor
import laika.parse.code.SyntaxHighlighting
// memo https://highlightjs.org/static/demo/
import laika.api._
import cats.effect.unsafe.implicits.global
import laika.io.implicits._
def createTransformer[F[_]: Async]: Resource[F, TreeTransformer[F]] =
  Transformer
    .from(format.Markdown)
    .to(format.HTML)
    .parallel[F]
    .withTheme(Petit)
    .build

createTransformer[IO]
  .use { t =>
    t.fromDirectory("docs").toDirectory("dist").transform
  }
  .unsafeRunSync()
