package skunk.db

import cats.effect.{Resource, Sync}
import cats.implicits._
import skunk.{Command, Query, Session, SqlState}
import skunk.errors.Errors.UniqueViolation

/**
  * Created by Bomen Derick.
  */
trait Repository[F[_], E] {
  protected def resource: Resource[F, Session[F]]
  protected def run[A](command: Session[F] => F[A])(implicit F: Sync[F]): F[A] =
    resource.use(command).handleErrorWith {
      case SqlState.UniqueViolation(ex) =>
        UniqueViolation(ex.detail.fold(ex.message)(m => m)).raiseError[F, A]
//        UniqueViolation(ex.message).raiseError[F, A]
    }

  protected def findOneBy[A](command: Query[A, E], argument: A)(implicit F: Sync[F]): F[Option[E]] =
    run {session =>
      session.prepare(command).use { pq =>
        pq.option(argument)
      }
    }

  protected def update[A](command: Command[A], argument: A)(implicit F: Sync[F]): F[Unit] =
    run { session =>
      session.prepare(command).use { cmd =>
        cmd.execute(argument).void
      }
    }

}
