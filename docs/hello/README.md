# Good Bye, Gatsby. Hello, Laika!

[Laika](http://planet42.github.io/Laika/) でブログを作ったった(＾ω＾)

[Laika](http://planet42.github.io/Laika/) は Scala で書かれたマークアップ変換ツールです.

入力したディレクトリやファイルの操作は仮想ツリー上で行います. この仕組みのおかげで Gatsby で graphql を使って処理していた機能を、
仮想的なディレクトリ構造(木構造)の変換として処理することができます.
例えば以下のコードは、手元の `src` ディレクトリにある `theme.css` を仮想ディレクトリツリーの `root/css/theme.css` にマウントします.
このツリーを使うコードは仮想ツリーから `theme.css` にアクセスすることができます.
```scala
InputTree
  .apply[F].addFile("./src/theme.css",Path.Root / "css" / "theme.css")
```



```scala
import laika._
import laika.directive.std.StandardDirectives
import laika.markdown.github.GitHubFlavor
import laika.parse.code.SyntaxHighlighting
```
Directives は markup(例えば、`markdown`) や template(例えば、`default.template.html`)
に埋め込んで文書を拡張するための概念です.

以下のように theme に Directives を渡すことで テンプレートやマークアップから Directive を使うことができます.

```scala
def style[F[_]:Sync] = InputTree
  .apply[F].addFile("./src/theme.css",Path.Root / "css" / "theme.css")

def myThemeBuilder[F[_]: Sync] = ThemeBuilder
  .apply[F]("<theme name>")
  .addExtensions(StandardDirectives, GitHubFlavor, SyntaxHighlighting)
  .addInputs(style)
  .addBaseConfig(conf.build)
  .processTree(addListPage,format.HTML)
  .build
```

直前のページ、直後のページへのリンクを動的に生成する Directive は次のように定義できます.

```scala
val prevDoc = Templates.create("prevDoc") {
  cursor.map { cursor =>
    cursor.previousDocument.fold[TemplateSpan](TemplateElement(Deleted(Nil))) {
      d => TemplateElement(SpanLink(d.target.title.getOrElse(SpanSequence(Text(d.path.toString)+:Nil)) +:Nil,InternalTarget(laika.ast.PathBase.parse(d.path.toString))))
    }
  }
}

val nextDoc = Templates.create("nextDoc") {
  cursor.map { cursor =>
    cursor.nextDocument.fold[TemplateSpan](TemplateElement(Deleted(Nil))) {
      d => TemplateElement(SpanLink(d.target.title.getOrElse(SpanSequence(Text(d.path.toString)+:Nil)) +:Nil,InternalTarget(laika.ast.PathBase.parse(d.path.toString))))
    }
  }
}

object MyDirectives extends DirectiveRegistry {
  val spanDirectives = Seq()
  val blockDirectives = Seq()
  val templateDirectives = Seq(prevDoc,nextDoc)
  val linkDirectives = Seq()
}

```

この theme の `addExtensions(MyDirectives)` で Directives を登録すれば、テンプレートから `@:prevDoc`,`@:nextDoc` を使って前後の記事へのリンクを生成することができます.


theme は、directives, 静的アセット、文書の仮想的なディレクトリ構造を保持するツリーの変換器(TreeProcessor)などを持ちます.


```scala
def createTransformer[F[_]: Async]: Resource[F, TreeTransformer[F]] =
  Transformer
    .from(format.Markdown)
    .to(format.HTML)
    .parallel[F]
    .withTheme(new theme.ThemeProvider {
      def build[F[_]: Sync]: Resource[F, Theme[F]] = myThemeBuilder
    })
    .build
```

入力フォーマット、出力フォーマット、テーマを指定してマークアップ変換処理を定義します.

```scala
createTransformer[IO]
  .use { transformer =>
    transformer.fromDirectory("src").toDirectory("dist").transform
  }
  .unsafeRunSync()
```

`src`ディレクトリにある `*.md` ファイルを `dist` ディレクトリに HTML 形式に変換して出力します.

文書のレイアウトは `src` ディレクトリに置かれた `default.template.html` に従います.

```laika-html
<!DOCTYPE html>
<html>
  <head>
    <title>${cursor.currentDocument.title}</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
      @:linkCSS { paths = ${simple.site.includeCSS} }
  </head>
  <body>
    <nav id="drawer">
      
      @:navigationTree {
        entries = [ 
          { target = "/", excludeRoot = true, excludeSections = true, depth = 2 } 
        ]
      }
  </nav>
    <div id="content" class="content">
      <div id="article">
      ${cursor.currentDocument.content}
      </div>
    </div>
  </body>
</html>


```
