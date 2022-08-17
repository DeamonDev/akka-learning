//import akka.Done
//import akka.actor.testkit.typed.javadsl.ActorTestKit
//import akka.actor.testkit.typed.scaladsl.{LogCapturing, ScalaTestWithActorTestKit}
//import akka.actor.typed.ActorSystem
//import org.scalatest.wordspec.AnyWordSpecLike
//
//class MainSpec
//    extends ScalaTestWithActorTestKit
//    with AnyWordSpecLike
//    with LogCapturing {
//
//  "Main sample" must {
//    "say hello" in {
//      val system: ActorSystem[HelloWorldMain.SayHello] = {
//        ActorSystem(HelloWorldMain(), "hello")
//
//        system ! HelloWorldMain.SayHello("World")
//        system ! HelloWorldMain.SayHello("Akka")
//
//        Thread.sleep(500)
//
//        ActorTestKit.shutdown(system)
//      }
//    }
//  }
//}
