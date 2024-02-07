import cats.effect.{Blocker, ExitCode, IO, IOApp}
import skunk.config.Config.AppConfig
import skunk.domain.user.{User, UserRepository}
import skunk.modules.Resources

/**
 * Created by Bomen Derick.
 */
object Main extends IOApp {

  implicit class IODebugger[A](io: IO[A]) {
    def debugIO: IO[A] = io.map { a =>
      println(s"[${Thread.currentThread().getName}] - $a")
      a
    }
  }

  override def run(args: List[String]): IO[ExitCode] =
    Blocker[IO].use(AppConfig.load[IO]).flatMap { config =>
      Resources.make[IO](
        config
      ).use { resource =>
        for {
          user    <- UserRepository.make[IO](resource.postgres)
          _       <- IO.pure(println("Creating users" + "_"*50))
          johnId  <- user.create(User.Name("John"), User.Email("email@john.com")).debugIO
          jacobId <- user.create(User.Name("Jacob"), User.Email("email@jacob.com")).debugIO
          _       <- user.create(User.Name("Kendrick"), User.Email("email@kendrick.com")).debugIO
          _       <- IO.pure(println("Fetching all users" + "_"*50))
          _       <- user.findAll.compile.toList.debugIO
          _       <- IO.pure(println("Fetching John by Id" + "_"*50))
          _       <- user.findById(johnId).debugIO
          _       <- IO.pure(println("Update John's email" + "_"*50))
          _       <- user.update(User(User.Id(johnId.value), User.Name("John"), User.Email("email@email.com")))
          _       <- user.findAll.compile.toList.debugIO
          _       <- IO.pure(println("Delete Jacob" + "_"*50))
          _       <- user.delete(jacobId)
          _       <- user.findAll.compile.toList.debugIO
        } yield ()
      }
    }.as(ExitCode.Success)
}