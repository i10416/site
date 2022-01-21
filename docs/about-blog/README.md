## サイトの構成

このサイトは GCP の cloud run に nginx コンテナをデプロイして静的ファイルを配信している.

インフラ構成は https://github.com/i10416/infra.git と https://github.com/i10416/site.git の infra ディレクトリにある `*.tf` で
terraform を使って管理している. `terraform apply` と `terraform destroy` でインフラリソースをまとめて作成・破棄できるので楽しい(＾ω＾)

## GCP で 静的サイトをホスティングするときのユースケース

選択肢は

1: cloud storage + load balancer
2: app engine
3: cloud run

しかし、1 は小規模なサイトだとロードバランサや固定 IP のコストがバカにならないので個人の静的ウェブサイトをホストするのには向いていない. ちなみにスケールが大きくなれば cloud storage + load balancer の構成のコスパがいいようだ.


2 は 無料枠、https 対応があり、app.yaml と gcloud コマンドさえあればほぼすぐにウェブサイトを公開できるくらいに簡単だが、terraform などの外部ツールとの食い合わせがやや悪い. 小規模なシステムなので cloud build による自動ビルドと app engine へのデプロイはオーバーキル.


3 はコンテナ以外の要素については自由に選択できるのでうれしい. 例えばビルドとデプロイは github actions で、ホスティングは cloud run で、といった使い分けがしやすい. お陰で terraform による構成管理も複雑にならない. ということでこのサイトは github actions と cloud run を使って管理することにしている.

## サイトの機能

よくある静的サイトの機能は一通り備えている.

### テキスト

orem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. 

### シンタックスハイライト

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


@:callout(info)
This is a info.
@:@

@:callout(warning)
This is a warning.
@:@

@:callout(error)
This is Error.
@:@


### table

|this | is | a | table|
|---|---|---|---|
|and | these| are|cells|

