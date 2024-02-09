package skunk.modules

import cats.effect.{ConcurrentEffect, ContextShift, Resource}
import natchez.Trace.Implicits.noop
import skunk.Session
import skunk.config.Config.AppConfig

/**
  * Created by Bomen Derick.
  */
final class Resources[F[_]] (
    val postgres: Resource[F, Session[F]]
)

object Resources {
  def makePooledSession[F[_] : ConcurrentEffect : ContextShift](
      config: AppConfig
  ): Resource[F, Resources[F]] = createPostgresPooledSession(config).map(p => new Resources[F](p))

  def makeSingleSession[F[_] : ConcurrentEffect : ContextShift](
     config: AppConfig
  ): Resources[F] = new Resources[F](createPostgresSingleSession(config))

  // Creating a single database session
  private def createPostgresSingleSession[F[_] : ConcurrentEffect : ContextShift](
     config: AppConfig
  ): Resource[F, Session[F]] =
    Session.single[F](
      host = config.postgres.host,
      port = config.postgres.port,
      user = config.postgres.user,
      password = Some(config.postgres.password),
      database = config.postgres.database
    )

  // Creating a pooled database session
  private def createPostgresPooledSession[F[_] : ConcurrentEffect : ContextShift](
    config: AppConfig
    ): Resource[F, Resource[F, Session[F]]] =
    Session.pooled[F](
      host = config.postgres.host,
      port = config.postgres.port,
      user = config.postgres.user,
      password = Some(config.postgres.password),
      database = config.postgres.database,
      max = config.postgres.maxConnections
    )
}
