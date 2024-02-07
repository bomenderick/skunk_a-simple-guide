package skunk.modules

import cats.effect.{ConcurrentEffect, ContextShift, Resource}
import natchez.Trace.Implicits.noop
import skunk.Session
import skunk.config.Config.AppConfig

import scala.concurrent.ExecutionContext

/**
  * Created by Bomen Derick.
  */
final class Resources[F[_]] (
    val postgres: Resource[F, Session[F]]
)

object Resources {
  def make[F[_] : ConcurrentEffect : ContextShift](
      config: AppConfig
  ): Resource[F, Resources[F]] = makePostgres(config).map(p => new Resources[F](p))

  private def makePostgres[F[_] : ConcurrentEffect : ContextShift](
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
