package skunk.config

import cats.effect.{Blocker, ContextShift, Sync}
import pureconfig.ConfigSource
import pureconfig.generic.auto._
import pureconfig.module.catseffect.syntax.CatsEffectConfigSource

/**
  * Created by Bomen Derick.
  */
object Config {
  final case class PostgresConfig(
     host: String,
     port: Int,
     user: String,
     password: String,
     database: String,
     maxConnections: Int
  )

  final case class AppConfig(postgres: PostgresConfig)
  object AppConfig {
    def load[F[_] : Sync : ContextShift](blocker: Blocker): F[AppConfig] =
      ConfigSource.default.loadF[F, AppConfig](blocker)
  }

}
