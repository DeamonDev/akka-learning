import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.LoggerOps
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}

object HelloWorld {
  final case class Greet(whom: String, replyTo: ActorRef[Greeted])
  final case class Greeted(whom: String, from: ActorRef[Greet])

  def apply(): Behavior[Greet] = Behaviors.receive { (ctx, msg) =>
    println(s"Hello ${msg.whom}")
    msg.replyTo ! Greeted(msg.whom, ctx.self)
    Behaviors.same
  }
}

object HelloWorldBot {
  def apply(max: Int): Behavior[HelloWorld.Greeted] =
    bot(0, max)

  private def bot(
      greetingCounter: Int,
      max: Int
  ): Behavior[HelloWorld.Greeted] =
    Behaviors.receive { (ctx, msg) =>
      val n = greetingCounter + 1
      println(s"Greeting $n for ${msg.whom}")

      if (n == max)
        Behaviors.stopped
      else {
        msg.from ! HelloWorld.Greet(msg.whom, ctx.self)
        bot(n, max)
      }
    }
}

object HelloWorldMain {
  final case class SayHello(name: String)

  def apply(): Behavior[SayHello] =
    Behaviors.setup { ctx =>
      val greeter = ctx.spawn(HelloWorld(), "greeter")

      Behaviors.receiveMessage { msg =>
        val replyTo = ctx.spawn(HelloWorldBot(max = 3), msg.name)
        greeter ! HelloWorld.Greet(msg.name, replyTo)
        Behaviors.same
      }
    }

  def main(args: Array[String]): Unit = {
    val system: ActorSystem[HelloWorldMain.SayHello] =
      ActorSystem(HelloWorldMain(), "hello")

    system ! HelloWorldMain.SayHello("World")
    system ! HelloWorldMain.SayHello("Akka")
  }
}
