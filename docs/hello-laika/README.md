# Good Bye, Gatsby. Hello, Laika!

以前のブログは Gatsby+TypeScript というアリがちな構成だったが、チョットしたウェブサイトを作るのにホントに graphql が必要なのかと胸に手を当てて考えてみるとそんなことはないという結論に至ってしまった. 
というわけで Scala 使いの信仰を試す意味もあって[Laika](http://planet42.github.io/Laika/) でブログを作ったった(＾ω＾)


さて、[Laika](http://planet42.github.io/Laika/) は Scala で書かれたマークアップ変換ツールです.
(Gatsby よりもメタな、SSG を作るためのライブラリといってもいいかもしれない.) Tagless-Final パターンを使っているので
実行時に cats-effect の IO や Monix Task を差し替えることができます.


入力したディレクトリやファイルの操作は仮想ツリー上で行います. この仕組みのおかげで Gatsby で graphql を使って処理していた機能を、
仮想的なディレクトリ構造(木構造)の変換として処理することができます.
例えば以下のコードは、手元の `src` ディレクトリにある `theme.css` を仮想ディレクトリツリーの `root/css/theme.css` にマウントします.


```
└── src
    └── theme.css
```


開発者はこのInputTreeで`theme.css` をマウントした仮想ツリーから `theme.css` にアクセスすることができます.

```scala
InputTree
  .apply[F].addFile("./src/theme.css",Path.Root / "css" / "theme.css")
```

仮想ツリーの操作を介して OGP の生成やページの動的な生成もできます.

また、Laika には変換処理などをまとめた ExtensionBundle や Directive という概念があります. これは開発者がテーマを作りやすくするために提供されているAPIです.

```scala
import laika._
import laika.directive.std.StandardDirectives
import laika.markdown.github.GitHubFlavor
import laika.parse.code.SyntaxHighlighting
```

Directives は markup(例えば、`markdown`) や template(例えば、`default.template.html`)
に埋め込んで文書を拡張するための概念です.

Laikaでは以下のように自作のテーマを作ることができます.  
テーマに Directives を渡すことで テンプレートやマークアップから Directive を使えるようになります.

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

たとえば、ある記事の直前のページ・直後のページへのリンクを動的に生成する Directive は次のように定義できます.

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
```

`Templates.create(...)`で、テンプレートファイルから呼び出せる`prevDoc`というDirectivesを作成しています. Laika がマークアップを変換するタイミングで`prevDoc`を見つけると、そのマークアップファイルを基準にして直前のドキュメントの情報を取得してドキュメントの内容を書き換えます.

`cursor` はドキュメントやその周りのドキュメントの情報にアクセスできます. 木構造を操作するための API なので Scala の Json ライブラリの Circe や Argonaut に似た API 設計になっています.


```scala

object MyDirectives extends DirectiveRegistry {
  val spanDirectives = Seq()
  val blockDirectives = Seq()
  val templateDirectives = Seq(prevDoc,nextDoc)
  val linkDirectives = Seq()
}

```


この theme の `addExtensions(MyDirectives)` で Directives を登録すれば、テンプレートから `@:prevDoc`,`@:nextDoc` を使って前後の記事へのリンクを生成することができます.


theme は、directives, css, js やテンプレートなどの静的アセット、文書の仮想的なディレクトリ構造を保持するツリーの変換器(TreeProcessor)などを持っています.



入力フォーマット、出力フォーマット、テーマを指定してマークアップ変換処理を定義できます.

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


```scala
createTransformer[IO]
  .use { transformer =>
    transformer.fromDirectory("src").toDirectory("dist").transform
  }
  .unsafeRunSync()
```

これは `src`ディレクトリにある `*.md` ファイルを `dist` ディレクトリに HTML 形式に変換して出力します.

文書のレイアウトはテーマが定義するデフォルトのレイアウトか `src` ディレクトリに置かれた `default.template.html` に従います.

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

次のようなテンプレートファイル(`articles.template.html`)とScalaのコードで動的にページを生成することもできます.


```laika-html
<body>
  <div class="row">
    <div class="spacer"></div>
    <div class="entries">
      <h1>articles</h1>
      ${cursor.currentDocument.content}
    </div>
    <div class="spacer"></div>
  </div>
</body>
```

Klisli とは `A => F[B]` をあらわしています. つまり、型Aの値を受け取ってなんらかの副作用を伴って型Bの値を返す処理を意味します.
たとえば、コンテンツを受け取って記事にurlが含まれていたら HTTP リクエストを送って OGP 情報を取得する処理は Klisli で表現できます.

この場合は、副作用を含まないので型を合わせるために `Sync[F].pure` で `F` の文脈で返り値を包みます.

```scala
private def addListPage[F[_]: Sync]: Theme.TreeProcessor[F] = Kleisli {
    tree =>
      val entries = tree.root.allDocuments.map { doc =>
        laika.ast.BlockSequence(
          Seq(
            Header(
              3,
              SpanLink
                .internal(doc.path)
                .apply(doc.title.getOrElse(SpanSequence(doc.path.name)))
            ),
            Paragraph(".....")
          ),
          NoOpt
        )
      }
      val articleListDocument =
        TitleDocumentConfig.inputName(tree.root.config).map { inputName =>
          Document(
            path = Path.Root / inputName,
            content = RootElement(entries),
            fragments = Map.empty,
            config =
              tree.root.config.withValue(LaikaKeys.versioned, false).build
          )
        }
      val withTemplate = articleListDocument.map(doc =>
        doc.copy(config =
          doc.config
            .withValue(LaikaKeys.template, "articles.template.html")
            .build
        )
      )
```
