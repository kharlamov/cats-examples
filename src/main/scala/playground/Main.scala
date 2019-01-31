package playground

import cats.data.Validated.{Invalid, Valid}
import cats.data.{Validated, ValidatedNec}
import cats.{Apply, Semigroup}

trait Read[A] {
  def read(s: String): Option[A]
}

object Read {
  def apply[A](implicit A: Read[A]): Read[A] = A

  implicit val stringRead: Read[String] =
    (s: String) => Some(s)

  implicit val intRead: Read[Int] =
    (s: String) => if (s.matches("-?[0-9]+")) Some(s.toInt)
    else None
}

sealed abstract class ConfigError
final case class MissingConfig(field: String) extends ConfigError
final case class ParseError(field: String) extends ConfigError

case class ConnectionParams(url: String, port: Int)
case class Address(houseNumber: Int, street: String)
case class Person(name: String, age: Int, address: Address)
case class Config(map: Map[String, String]) {
  def parse[A: Read](key: String): Validated[ConfigError, A] =
    map.get(key) match {
      case None => Invalid(MissingConfig(key))
      case Some(value) =>
        Read[A].read(value) match {
          case None => Invalid(ParseError(key))
          case Some(a) => Valid(a)
        }
    }
}

object Main {
  def main(args: Array[String]): Unit = {
    val config = Config(Map(("name", "cat"), ("age", "not a number"), ("houseNumber", "1234"), ("lane", "feline street")))

    val personFromConfig: ValidatedNec[ConfigError, Person] =
      Apply[ValidatedNec[ConfigError, ?]].map4(
        config.parse[String]("name").toValidatedNec,
        config.parse[Int]("age").toValidatedNec,
        config.parse[Int]("house_number").toValidatedNec,
        config.parse[String]("street").toValidatedNec) {
        case (name, age, houseNumber, street) => Person(name, age, Address(houseNumber, street))
      }
    println(personFromConfig)

    val portConfig = Config(Map(("url", "127.0.0.1"), ("port", "1337")))
    val urlFromConfig = parallelValidate(
      portConfig.parse[String]("url").toValidatedNel,
      portConfig.parse[Int]("port").toValidatedNel
    )(ConnectionParams)
    println(urlFromConfig)
  }

  def parallelValidate[E: Semigroup, A, B, C](v1: Validated[E, A], v2: Validated[E, B])(f: (A, B) => C): Validated[E, C] =
    (v1, v2) match {
      case (Valid(a), Valid(b)) => Valid(f(a, b))
      case (Valid(_), i@Invalid(_)) => i
      case (i@Invalid(_), Valid(_)) => i
      case (Invalid(e1), Invalid(e2)) => Invalid(Semigroup[E].combine(e1, e2))
    }
}

