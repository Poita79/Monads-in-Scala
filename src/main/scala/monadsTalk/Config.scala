package talk

import monadTransformers.{Monad, ScalaMonad}
import ScalaMonad.typeclass2ScalaMonad

object Config {

  implicit val configIsAMonad = new Monad[Config] {
    def returnM[A](a: A): Config[A] = { (env: Map[String, String]) => Some(a) }
    def bindM[A, B](m: Config[A], f: A => Config[B]): Config[B] = { env: Map[String, String] =>
      val a = m(env)
      a match {
        case Some(x) => f(x)(env)
        case None => None
      }
    }
  }

  implicit val optionIsAMonad: Monad[Option] = new Monad[Option] {
    def returnM[A](a: A): Option[A] = Some(a)
    def bindM[A, B](m: Option[A], f: A => Option[B]): Option[B] = m flatMap f
  }









  case class ConnectionPool(host: String, user: String, password: String)
  val someConfiguration = Map[String, String](
    "host" -> "some guy",
    "user" -> "guy@some.com",
    "password" -> "HisPassw0rd"
  )




  // using the Option monad
  def buildConnectionPool(config: Map[String, String]): Option[ConnectionPool] = {
    for {
      host <- config.get("host")
      user <- config.get("user")
      password <- config.get("password")
    } yield ConnectionPool(host, user, password)
  }

  val connectionPool = buildConnectionPool(someConfiguration)





  def sequenceM3[T, M[_]: Monad, A, B, C](tuple: Tuple3[M[A], M[B], M[C]]): M[Tuple3[A, B, C]] = {
    for {
      u <- tuple._1
      v <- tuple._2
      w <- tuple._3
    } yield (u, v, w)
  }


  val configParameters = (someConfiguration.get("host"), someConfiguration.get("user"), someConfiguration.get("password"))
  val connectionPool2 = sequenceM3(configParameters) map {
    case (host, user, password) => ConnectionPool(host, user, password)
  }





  type Config[A] = Map[String, String] => Option[A]
  def config(param: String): Config[String] = {
    config: Map[String, String] => config.get(param)
  }

  def connectionPoolBuilder: Config[ConnectionPool] = {
    for {
      host <- config("host")
      user <- config("user")
      password <- config("password")
    } yield ConnectionPool(host, user, password)
  }





  val connectionPool3 = connectionPoolBuilder(someConfiguration)



  val builder = connectionPoolBuilder
  val connectionPool4 = builder.apply(someConfiguration)
}
