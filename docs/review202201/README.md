# 2022/01 月のふりかえり


## サイト移行

TypeScript&Gatsby から Scala&Laika に移行した. さようなら JS (＾ω＾) 

これでテーマや拡張機能も全部 Scala で作れる. うれしいですね(＾ω＾)


ソースコードは https://github.com/i10416/site においてある. netlify や vercel にホストするのは芸がないので
github actions と cloud run でGithub に Push -> 自動デプロイのワークフローを組んだ. インフラは terraform で GCP のインフラを管理している.

デプロイには scala-cli も使っている. チョットしたスクリプトを書くだけならセットアップしなくてもすぐに使えていい.


ちなみに Scala 3 の次のリリースには Scaladoc の改善が含まれていて、それには SSG 機能も付いているらしい(´･ω･｀) こっちでやるべきだったか？


## 統計

現代数理統計学の基礎 をﾁﾏﾁﾏすすめていた.

## 社会と疎結合になった

💪強い意志💪で Twitter をやめた. Twitter は前頭葉を刺激するタイプの情報(というかノイズ)が多すぎて疲弊する.
今年は宣伝用途と障害発生時の情報収集にしか使わないと思う. 


## OSS
Scala の OSS や JWM という ウィンドウマネージャにﾁﾏﾁﾏ PR を送った.

- https://github.com/sherpal/url-dsl
- https://github.com/planet42/laika
- https://github.com/HumbleUI/JWM


Munkres(Hangarian) Algorithm を Scala.js に対応させた.

https://github.com/i10416/munkres

気がついたら Scala.js も Scala Native も Scala 3 に対応しているのでうれしい. Scala Native は一次はオワコン化したか、
と思ったら奇跡の復活を遂げたのでコントリビュータの方々には足を向けて寝られませんね.


あと Dart の文法が嫌いなので Scala のコレクションAPIみたいな機能をまとめたライブラリをリリースした. alt Dart の誕生が待たれる.

https://github.com/i10416/dart-modules/tree/master/flechette

```yaml
dependencies:
  flechette: ^0.0.1
```

Dart の拡張メソッドは実質ただの関数なので null や nullable な値に対して使っても安全なのでいろいろと捗る.

こんなふうに `t` が nullable でも安心してメソッドチェーンを書ける.

(Laika のシンタックスハイライト、`Alloy` や `Dhall` には対応してるのに `Dart` には対応してなくて草.)

```dart
T? t = ???;

t
    .map((a)=>  b)
    .filter((b)=> cond(b) )
    .getOrElse(()=>els);

```


google 先生が [tuple](https://pub.dev/packages/tuple) というパッケージを公開しているが、
`Tuple(a,b)` ではタイプ数が多くてつらいので `$(a,b)` で書けるように書き直したものも入っている. tuple、というか HList があると zip みたいに複数の型をまとめて型安全にあつかいたいとき嬉しい.

```dart
final tpl = $(1,'a');
final l = [1,2,3,4,5];
l.zipWithIndex // => [(1,0),(2,1),(3,2),(4,3),(5,4)]
l.slide // => [(1,2),(2,3),(3,4),(4,5)]
[1,2,3,4,5,6,7].sliding(4) // =>  [[1,2,3,4],[2,3,4,5],[3,4,5,6],[4,5,6,7]];
l.zip(['a','b','c','d','e']); // => [(1,'a'),(2,'b'),(3,'c'),(4,'d'),(5,'e')]
null.fold(() => 'none!')((e) => (e * 100).toString()); // => 'none!'
[1,2,3,4,5,6,7,8,9,10].flatMap((e) => e %2 == 0 ? e : null); // [2,4,6,8,10];
```

余談だが JVM 言語と違って `dart pub publish` で雑にパッケージをリリースできるのはまあ悪くない.

## その他

たいていの Scala の OSS には xuwei-k さんのアイコンがあってｺﾜｲ(respect)

今月は Flutter のバグに時間を溶かされた. beta が一番安定してるとか何事(´･ω･｀)

 あと、しれっと dev チャンネルが消えて master, beta, stable だけになったりしていておやおやという顔になった.

https://github.com/flutter/flutter/wiki/Flutter-build-release-channels/_history

精神の平穏と scala のクロスプラットフォーム宣言的UIライブラリがほしい.
