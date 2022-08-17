import akka.NotUsed
import akka.actor.typed.scaladsl.{Behaviors, LoggerOps}
import akka.actor.typed.{ActorRef, ActorSystem, Behavior, Terminated}

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object ChatRoom {
  sealed trait RoomCommand
  final case class GetSession(screenName: String, replyTo: ActorRef[SessionEvent]) extends RoomCommand
  private final case class PublishSessionMessage(screenName: String, message: String) extends RoomCommand

  sealed trait SessionEvent
  final case class SessionGranted(handle: ActorRef[PostMessage]) extends SessionEvent
  final case class SessionDenied(reason: String) extends SessionEvent
  final case class MessagePosted(screenName: String, message: String) extends SessionEvent

  sealed trait SessionCommand
  final case class PostMessage(message: String) extends SessionCommand
  private final case class NotifyClient(message: MessagePosted) extends SessionCommand

  def apply(): Behavior[RoomCommand] =
    chatRoom(List.empty)

  private def chatRoom(sessions: List[ActorRef[SessionCommand]]): Behavior[RoomCommand] =
    Behaviors.receive { (ctx, msg) =>
      msg match {
        case GetSession(screenName, client) =>
          val ses = ctx.spawn(
            session(ctx.self, screenName, client),
            name = URLEncoder.encode(screenName, StandardCharsets.UTF_8.name)
          )

          client ! SessionGranted(ses)
          chatRoom(ses :: sessions)
        case PublishSessionMessage(screenName, message) =>
          val notification = NotifyClient(MessagePosted(screenName, message))
          sessions.foreach(_ ! notification)
          Behaviors.same
      }
    }

  private def session(room: ActorRef[PublishSessionMessage], screenName: String, client: ActorRef[SessionEvent]): Behavior[SessionCommand] =
    Behaviors.receiveMessage {
      case PostMessage(message) =>
        room ! PublishSessionMessage(screenName, message)
        Behaviors.same
      case NotifyClient(message) =>
        client ! message
        Behaviors.same
    }
}

object Gabbler {
  import ChatRoom._

  def apply(): Behavior[SessionEvent] =
    Behaviors.setup { ctx =>
      Behaviors.receiveMessage {
        case SessionDenied(reason) =>
          ctx.log.info("cannot start chaat room session: {}", reason)
          Behaviors.stopped
        case SessionGranted(handle) =>
          handle ! PostMessage("Hello World")
          Behaviors.same
        case MessagePosted(screenName, message) =>
          ctx.log.info2("message has beed posted by '{}': {}", screenName, message)
          Behaviors.stopped
      }
    }
}

object ChatRoomExample {
  def apply(): Behavior[NotUsed] =
    Behaviors.setup { ctx =>
      val chatRoom = ctx.spawn(ChatRoom(), "chat-room")
      val gabblerRef = ctx.spawn(Gabbler(), "gabbler")
      ctx.watch(gabblerRef)

      chatRoom ! ChatRoom.GetSession("Piotr R.", gabblerRef)

      Behaviors.receiveSignal {
        case (_, Terminated(_)) =>
          Behaviors.stopped
      }
    }

  def main(args: Array[String]): Unit =
    ActorSystem(ChatRoomExample(), "chat-room-demo")
}
