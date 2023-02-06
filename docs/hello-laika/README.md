# Good Bye, Gatsby. Hello, Laika!

## Motivation

I used to use Gatsby + Typescript for my websites. However, do I really need clumsy Webpack build pipeline, hacky MDX template and even GraphQL for simple personal website? No. That's why I use [Laika](http://planet42.github.io/Laika/), which is an extensible markup converter with some useful tools written in **Scala**, because I'm tired of JavaScript and love elegance and type safety of Scala. Good bye dirty JS solutions :)


## About Laika

Laika is a library for libraries. It provides essential features (e.g. abstract file tree, pluggable interfaces) for building markup conversion libraries.

It is written in purely functional style using cats-effect with famous Tagless-Final pattern, so it is theoretically possible to switch effect libraries from one to another.

You can programatically manupulate contents in input directories using abstract file tree. You can regard this feature as functionality Gatsby provides via GraphQL. 


For example, the following code snippet shows how to mount local file at `./src/theme.css` to `/css/theme.css` in virtual tree.

```scala
InputTree
  .apply[F].addFile("./src/theme.css",Path.Root / "css" / "theme.css")
```

```
└── src
    └── theme.css
```

Now that `InputTree` has theme.css, developer can use that file as if it is located at `/css/theme.css`.

you can read and edit contents from input directories and even dynamically generate contents via virtual tree. Laika provides us with builtin toolkits for tree conversion (`ExtensionBundle`, `Directive`).


```scala

import laika._
import laika.directive.std.StandardDirectives
import laika.markdown.github.GitHubFlavor
import laika.parse.code.SyntaxHighlighting
```

Directives can be embedded in markup(e.g. `markdown`) or template(e.g.`default.template.html`) to provide rich features like custom snippet in markup or dynamically change template from config.


Laika exposes APIs useful for theme authors.

Theme can be built from extensions, static assets(template, css, js and images), configuration and tree processors as shown below.

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

Tree processor is just a Klisli, alias for `A => F[B]`, which uses value of type `A` and returns value of type `B` in the context of `F`.

For example, fetching OGP for a link in a document from web is `URL => IO[OGP]`. `IO` indicates that it perform `IO` effect and thus it is not deterministic and may fail due to errors like network connection error.

Considering `addListPage`, it is effect-less, justs collects and lists contents in tree in a deterministic way, it uses `Sync[F].pure` to lift value to the context of `F`.

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
      // do some conversion...
      implicitly[Sync[F]].pure(newTree)
```

Extension may contain custom directives.

For example, you can define `Directive` that dynamically generates link to previous/next post for a post.

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

`Templates.create(...)` defines `pervDoc` Directive for template file. When Laika finds `prevDoc` While conversion, Laika looks up a post previous to the post with `prevDoc` directive and replace `prevDoc` with link to the previous document.

`cursor` allows access to documents and configurations. It is natural that cursor API is similar to json libraries like Circe or Argonaut because all of them provide abstraction for manipulating tree structure.

```scala

object MyDirectives extends DirectiveRegistry {
  val spanDirectives = Seq()
  val blockDirectives = Seq()
  val templateDirectives = Seq(prevDoc,nextDoc)
  val linkDirectives = Seq()
}

```

`DirectiveRegistry` consists of a set of Directives, each of which has dedicated purpose. `prevDoc` and `nextDoc` is supposed to be used in templates, I added them to `templateDirectives`.

You can register `MyDirective` to theme via `addExtensions` method. With that theme, you can generate link to previous/next post in template using `@:prevDoc` or `@nextDoc`.

Now, the question is, how to debug/use this theme?


The simplest way is to use `Transformer`. I recommend you `pulishLocal` your theme and debug it with Scala CLI or ammonite script as you usually want to check look-and-feels of your theme in fast-feedback-loop.

The following snippet shows the simple way to create and use your(my) theme`Transformer`.

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
It is self explanatory. It reads `*.md` from `src` directory, converts them to `html` and emits in `dist` directory.

Layout is determined by theme's default template or `default.template.html` in `src` directory.

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

You can access document information in template using cursor.

For instance, the following template render markup content after conversion using `cursor.currentDocument.content`.


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
