package skunk.db

import cats.effect.{Resource, Sync}
import cats.implicits._
import skunk.{Command, Query, Session, SqlState}
import skunk.errors.Errors.UniqueViolation

/**
  * Created by Bomen Derick.
  */
// While F is the effect type, E represents the domain/model used to perform operations
trait Repository[F[_], E] {
  // resource that represents our database session
  protected def resource: Resource[F, Session[F]]
  protected def run[A](session: Session[F] => F[A])(implicit F: Sync[F]): F[A] =
    resource.use(session).handleErrorWith {
      case SqlState.UniqueViolation(ex) =>
        UniqueViolation(ex.detail.fold(ex.message)(m => m)).raiseError[F, A]
    }

  protected def findOneBy[A](query: Query[A, E], argument: A)(implicit F: Sync[F]): F[Option[E]] =
    run { session =>
      session.prepare(query).use { preparedQuery =>
        preparedQuery.option(argument)
      }
    }

  protected def update[A](command: Command[A], argument: A)(implicit F: Sync[F]): F[Unit] =
    run { session =>
      session.prepare(command).use { preparedCommand =>
        preparedCommand.execute(argument).void
      }
    }
}
