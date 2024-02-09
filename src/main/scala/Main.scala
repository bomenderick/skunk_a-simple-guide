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

  override def run(args: List[String]): IO[ExitCode] = {
    // using a single database session
    Blocker[IO].use(AppConfig.load[IO]).flatMap { config =>
      for {
        userRepo <- UserRepository.make[IO](Resources.makeSingleSession[IO](config).postgres)
        _ <- IO(println("Creating users" + "_" * 50))
        johnId <- userRepo.create(User.Name("John"), User.Email("email@john.com")).debugIO
        jacobId <- userRepo.create(User.Name("Jacob"), User.Email("email@jacob.com")).debugIO
        _ <- userRepo.create(User.Name("Kendrick"), User.Email("email@kendrick.com")).debugIO
        _ <- IO(println("Fetching all users" + "_" * 50))
        _ <- userRepo.findAll.compile.toList.debugIO
        _ <- IO(println("Fetching John by Id" + "_" * 50))
        _ <- userRepo.findById(johnId).debugIO
        _ <- IO(println("Update John's email" + "_" * 50))
        _ <- userRepo.update(User(User.Id(johnId.value), User.Name("John"), User.Email("email@email.com")))
        _ <- userRepo.findAll.compile.toList.debugIO
        _ <- IO(println("Delete Jacob" + "_" * 50))
        _ <- userRepo.delete(jacobId)
        _ <- userRepo.findAll.compile.toList.debugIO
      } yield ExitCode.Success
    }

    // using a pooled database session
//    Blocker[IO].use(AppConfig.load[IO]).flatMap { config =>
//      Resources.make[IO](
//        config
//      ).use { resource =>
//        for {
//          userRepo <- UserRepository.make[IO](resource.postgres)
//          _        <- IO(println("Creating users" + "_"*50))
//          johnId   <- userRepo.create(User.Name("John"), User.Email("email@john.com")).debugIO
//          jacobId  <- userRepo.create(User.Name("Jacob"), User.Email("email@jacob.com")).debugIO
//          _        <- userRepo.create(User.Name("Kendrick"), User.Email("email@kendrick.com")).debugIO
//          _        <- IO(println("Fetching all users" + "_"*50))
//          _        <- userRepo.findAll.compile.toList.debugIO
//          _        <- IO(println("Fetching John by Id" + "_"*50))
//          _        <- userRepo.findById(johnId).debugIO
//          _        <- IO(println("Update John's email" + "_"*50))
//          _        <- userRepo.update(User(User.Id(johnId.value), User.Name("John"), User.Email("email@email.com")))
//          _        <- userRepo.findAll.compile.toList.debugIO
//          _        <- IO(println("Delete Jacob" + "_"*50))
//          _        <- userRepo.delete(jacobId)
//          _        <- userRepo.findAll.compile.toList.debugIO
//        } yield ExitCode.Success
//      }
//    }
  }
}