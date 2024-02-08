package skunk.domain.user

import io.estatico.newtype.macros.newtype

import java.util.UUID

/**
  * Created by Bomen Derick.
  */
final case class User (
  id: User.Id,
  name: User.Name,
  email: User.Email
)

object User {
  @newtype case class Id(value: UUID)
  @newtype case class Name(value: String)
  @newtype case class Email(value: String)
}
