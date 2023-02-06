## About Site (Infrastructure and Theme)

This site is served from nginx container on GCP Cloud Run. Infrastructure is managed by terraform, which help me get rid of my toil(just `terraform apply` to create resources and `terraform destory` to delete resources). 
You can find a part of terraform definition at infra directory in https://github.com/i10416/site. 



All blog posts are generated from Markdown using elegant Scala library: [Laika](https://planet42.github.io/Laika). Taking advantage of Laika's flexitibity, this site uses my custom theme(that is also written in Scala!). My theme provides some essential features like custom snippets, syntax highlighting and so on. If you're interested, visit https://github.com/i10416/petit and give it a star⭐️.

You can fetch my theme from snapshot reopsitory.

Scala CLI:

```scala
//> using lib "dev.i10416::petit:0.0.0-xxxxx-SNAPSHOT"
```

ammonite script:

```scala
import $ivy.`dev.i10416::petit:0.0.0-xxxxx-SNAPSHOT`
```


`build.sbt`:

```scala
libraryDependencies ++= Seq(
  "dev.110416" %% "petit" % "0.0.0-xxxx-SNAPSHOT"
)
```

## Some Thought on Hosting Static Website on GCP
There are several options when you want to host a static website on GCP.

For example,

1. cloud storage + load balancer
2. app engine
3. cloud run
4. firebase hosting

Option 1 is costly when your site traffic is small because load balancer and permanent IP address are expensive compared to their benefit.
(Of course, if your site must handle large traffic, cost for cloud strage + load balancer will pay).

Option 2, Google App Engine, is not so bad as it has free quota, supports https and it is quite easy to deploy. However I prefer simple container deployment rather than app.yaml and Cloud Build, which is tied to GAE.

Option 4, Firebase Hosting, is free, fast, and easy, but I'm not a big fan of Firebase.

Considering these options above, I chose option 3, Cloud Run. Cloud Run is loosely coupled with other infrastructure stuffs, so it gives me flexibility for build and deploy workflow. For example, I can use GitHub Actions to build and deploy your website while serving website from Cloud Run. This helps me keep infrastructure as simple as possible. After a few months, I'm satisfied with my desicion but I think GCE with nix would be interesting.


## About My Site Theme

This site uses [my custom theme](https://github.com/i10416/petit) for Laika.
My theme has essential features for common static website.

### Rendering Text with Some Decoration

orem ipsum dolor sit amet, __consectetur adipiscing__ elit, sed do **eiusmod** tempor incididunt ~~ut labore~~ et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. 

### Syntax Highlighting

My theme has fancy syntax highlight as do other popular static site themes. I'm so accustomed to it that I couldn't do without them.

```scala
class Foo(val field :Int = 0):
  @annotation
  def main(args: Array[String]): Unit = ???

// this is comment
println("hello, my theme!")
lazy val hoge =
  println("this is evaluated lazily")
  1
```

### table

```

| this | is    | a   | table |
| ---- | ----- | --- | ----- |
| and  | these | are | cells |
```


| this | is    | a   | table |
| ---- | ----- | --- | ----- |
| and  | these | are | cells |


### Quote

> this is a quote
>
> foo bar


### Custom Snippets

@:callout(info)
`callout`
@:@



Laika has a concept of "Directive", which enables us to extend markup language syntax.
"Directive" is a kind of function injected in render pipeline.

Laika has a lot of nice builtin Directives, visit https://planet42.github.io/Laika/0.18/07-reference/01-standard-directives.html for more details.

```
@:callout(info)
Info.
@:@
```

@:callout(info)
Info.
@:@

@:callout(warning)
Warning.
@:@

@:callout(error)
Error.
@:@


### Image

You can embed image by `![alt](path)` as you do in GitHub flavor markdown or `@:image` directive.

```
@:image(scala_img.png) {
  intrinsicWidth = 300
  intrinsicHeight = 300
  alt = Scala Logo
  title = Scala Logo
}
```

@:image(scala_img.png) {
  intrinsicWidth = 300
  intrinsicHeight = 300
  alt = Scala Logo
  title = Scala Logo
}

