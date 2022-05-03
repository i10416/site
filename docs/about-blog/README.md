## サイトの構成

このサイトは GCP の cloud run に nginx コンテナをデプロイして静的ファイルを配信しています. 記事のコンテンツは Scala の Laika というライブラリで markdown ファイルを html に変換しています.

インフラ構成は https://github.com/i10416/infra.git と https://github.com/i10416/site.git の infra ディレクトリにある `*.tf` で
terraform を使って管理しています. `terraform apply` と `terraform destroy` でインフラリソースをまとめて作成・破棄できるので楽しいですね(＾ω＾)

また、ウェブサイトのスタイルはテーマにしてライブラリとして公開しているのでディレクトリ構成を揃えれば誰でもｼｭｯと使えるはずです.

ソースは以下のレポジトリに置いてあります. 気が向いたらスターしてほしいですね❤

https://github.com/i10416/petit


Scala-CLI なら以下のように

```scala
// using lib dev.i10416::petit:0.0.0-xxxxx-SNAPSHOT`
```


ammonite script なら以下のように,

```scala
import $ivy.`dev.i10416::petit:0.0.0-xxxxx-SNAPSHOT`
```


`build.sbt` なら以下のように

```scala
libraryDependencies ++= Seq(
  "dev.110416" %% "petit" % "0.0.0-xxxx-SNAPSHOT"
)
```

と書いてダウンロードできます.

ディレクトリ構成などは https://github.com/i10416/site を参考に.

## GCP で 静的サイトをホスティングするときのユースケース

さて、GCPで静的サイトをホスティングしようと思ったら次のようなユースケースが考えられます.
選択肢は

1: cloud storage + load balancer

2: app engine

3: cloud run

4: firebase hosting

1 は小規模なサイトだとロードバランサや固定 IP のコストがバカにならないので個人の静的ウェブサイトをホストするのには向いていないです. ちなみにスケールが大きくなれば cloud storage + load balancer の構成のコスパがいいはずです.

2 は 無料枠、https 対応があり、app.yaml と gcloud コマンドさえあればほぼすぐにウェブサイトを公開できるくらいに簡単だが、terraform などの外部ツールとの食い合わせがやや悪いです. 小規模なシステムなので cloud build による自動ビルドと app engine へのデプロイはオーバーキルですね.

4 は安い・速い・楽の牛丼屋さんの三点セットがそろっているが面白みに欠けます.

3 はコンテナ以外の要素については自由に選択できるのでうれしいですね. 例えばビルドとデプロイは github actions で、ホスティングは cloud run で、といった使い分けがしやすいです. お陰で terraform による構成管理も複雑にならないというおまけつき. ということでこのサイトは github actions と cloud run を使って管理することにしています.

## サイトの機能

よくある静的サイトの機能は一通り備えています.

### テキスト

orem ipsum dolor sit amet, __consectetur adipiscing__ elit, sed do **eiusmod** tempor incididunt ~~ut labore~~ et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. 

### シンタックスハイライト

しばしばコードスニペットを書くのでシンタックスハイライトは Must です.

```scala
class Hoge(val a :Int = 0) {
  @main
  def main(args:Array[String]) :Unit = ???
}
// this is comment
println("hello, my theme!")
lazy val hoge = {
  println("this is evaluated lazily")
  1
}
```

### 引用

> this is a quote
>
> foo bar


### Info,Warning など

Directive という Laika の機能を使うと以下の `info`, `warning`, `error`  のような拡張シンタックスが使えます.

他にもいろいろな機能があるので詳細については https://planet42.github.io/Laika/0.18/07-reference/01-standard-directives.html を見てほしい.

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


### table

| this | is    | a   | table |
| ---- | ----- | --- | ----- |
| and  | these | are | cells |

