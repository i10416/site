



object `script.worksheet` {
/*<script>*/// scala version 2.13.7
import fs2.io.file.Path
import fs2.io.Watcher.Event.Deleted
import fs2.io.Watcher.Event.Modified
import fs2.io.Watcher.Event.Created
import $ivy.A                                
import $ivy.A                                   
import $ivy.A                              
import $ivy.A                                 
import $ivy.A                       
import $ivy.A                     
import $ivy.A                                   

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

def watcher = fs2.io.Watcher.default[IO]
def res = for {
  w <- watcher
  t <- createTransformer[IO]
} yield (w,t)

res.use {case(w,t) =>
  for {
    // todo: how to watch recursively?
    _ <- w.register(Path.apply("docs").toNioPath)
    _ <- w.events().filter{case Created(path, count) => println("created"); true
  case Modified(path, count) => println("modified");true
case Deleted(path, count) =>  println("deleted");true
case _ => println("nope");false}.evalMap(_ => t.fromDirectory("docs").toDirectory("dist").transform).compile.drain
  } yield ()
}
/*</script>*/ /*<generated>*/
def args = `script.worksheet_sc`.args$
  /*</generated>*/
}
object `script.worksheet_sc` {
  private var args$opt0 = Option.empty[Array[String]]
  def args$set(args: Array[String]): Unit = {
    args$opt0 = Some(args)
  }
  def args$opt: Option[Array[String]] = args$opt0
  def args$: Array[String] = args$opt.getOrElse {
    sys.error("No arguments passed to this script")
  }
  def main(args: Array[String]): Unit = {
    args$set(args)
    `script.worksheet`.hashCode() // hasCode to clear scalac warning about pure expression in statement position
  }
}

