



object serve {
/*<script>*/import $ivy.A                                  
import $ivy.A                                        
import $ivy.A                                          
import $ivy.A                                 
import org.http4s.ember.server
import org.http4s.HttpApp
import org.http4s.server.staticcontent._
import org.http4s.server.Server
import com.comcast.ip4s.Port
import com.comcast.ip4s.Host
import cats.effect.IO
import cats.effect.ExitCode
import cats.effect.unsafe.implicits.global

@main
def run()= server.EmberServerBuilder
  .default[IO]
  .withHost(Host.fromString("0.0.0.0").get)
  .withPort(Port.fromInt(8989).get)
  .withHttpApp(fileService[IO](FileService.Config("./dist")).orNotFound)
  .build.use(_ => IO.never).as(ExitCode.Success).unsafeRunSync()

/*</script>*/ /*<generated>*/
def args = serve_sc.args$
  /*</generated>*/
}
object serve_sc {
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
    serve.hashCode() // hasCode to clear scalac warning about pure expression in statement position
  }
}

