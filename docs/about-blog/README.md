## ブログの機能

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

