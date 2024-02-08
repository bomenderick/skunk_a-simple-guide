package skunk.domain.user

import cats.effect.{Resource, Sync}
import cats.implicits._
import skunk.db.Repository
import fs2.Stream
import skunk._
import skunk.codec.all._
import skunk.implicits._
import fs2.Stream

import java.util.UUID

/**
  * Created by Bomen Derick.
  */
trait UserRepository[F[_]] extends Repository[F, User]{
  def findAll: Stream[F, User]
  def findById(id: User.Id): F[Option[User]]
  def create(name: User.Name, email: User.Email): F[User.Id]
  def update(user: User): F[Unit]
  def delete(id: User.Id): F[Unit]
}

object UserRepository {
  def make[F[_] : Sync](
      resource: Resource[F, Session[F]]
  ): F[UserRepository[F]] =
    Sync[F].delay(new UserPrivateRepository[F](resource))

  final private class UserPrivateRepository[F[_] : Sync](
    val resource: Resource[F, Session[F]]
  ) extends UserRepository[F] {
    override def findAll: Stream[F, User] =
      Stream.evalSeq(run(_.execute(selectAll)))

    override def findById(id: User.Id): F[Option[User]] =
      findOneBy(selectById, id.value)

    override def create(name: User.Name, email: User.Email): F[User.Id] =
      run { session =>
        session.prepare(insert).use {cmd =>
          val userId = User.Id(UUID.randomUUID())
          cmd.execute(User(userId, name, email)).map(_ => userId)
        }
      }

    override def update(user: User): F[Unit] =
      update(_update, user)

    override def delete(id: User.Id): F[Unit] =
      update(_delete, id)
  }

  private val codec: Codec[User] =
    (uuid ~ varchar ~ varchar).imap {
      case id ~ name ~ email => User(
        User.Id(id),
        User.Name(name),
        User.Email(email)
      )
    }(user => user.id.value ~ user.name.value ~ user.email.value)

  private val selectAll: Query[Void, User] =
    sql"""
         SELECT * FROM users
       """.query(codec)

  private val selectById: Query[UUID, User] =
    sql"""
         SELECT * FROM users
         WHERE id = $uuid
       """.query(codec)

  private val insert: Command[User] =
    sql"""
         INSERT INTO users
         VALUES ($codec)
       """.command

  private val _update: Command[User] =
    sql"""
         UPDATE users
         SET name = $varchar, email = $varchar
         WHERE id = $uuid
       """.command.contramap { user =>
      user.name.value ~ user.email.value ~ user.id.value
    }

  private val _delete: Command[User.Id] =
    sql"""
         DELETE FROM users
         WHERE id = $uuid
       """.command.contramap(_.value)
}
