//#full-example
package com.example

import akka.actor.{ Actor, ActorLogging, ActorRef, ActorSystem, Props, PoisonPill, Terminated }

//#greeter-companion
//#greeter-messages
object Greeter {
  //#greeter-messages
  def props(message: String, printerActor: ActorRef): Props = Props(new Greeter(message, printerActor))
  //#greeter-messages
  final case class WhoToGreet(who: String)
  case object Greet
}
//#greeter-messages
//#greeter-companion

//#greeter-actor
class Greeter(message: String, printerActor: ActorRef) extends Actor {
  import Greeter._
  import Printer._

  var greeting = ""

  def receive = {
    case WhoToGreet(who) =>
      greeting = message + ", " + who
    case Greet           =>
      //#greeter-send-message
      printerActor ! Greeting(greeting)
      //#greeter-send-message
  }
}
//#greeter-actor

//#printer-companion
//#printer-messages
object Printer {
  //#printer-messages
  def props: Props = Props[Printer]
  //#printer-messages
  final case class Greeting(greeting: String)
}
//#printer-messages
//#printer-companion

//#printer-actor
class Printer extends Actor with ActorLogging {
  import Printer._

  def receive = {
    case Greeting(greeting) =>
      log.info("Greeting received (from " + sender() + "): " + greeting)
  }
}
//#printer-actor

//#main-class
object AkkaQuickstart extends App {
  import Greeter._

  // Create the 'helloAkka' actor system
  val system: ActorSystem = ActorSystem("helloAkka")

  //#create-actors
  // Create the printer actor
  val printer: ActorRef = system.actorOf(Printer.props, "printerActor")

  // Create the 'greeter' actors
  val howdyGreeter: ActorRef =
    system.actorOf(Greeter.props("Howdy", printer), "howdyGreeter")
  val helloGreeter: ActorRef =
    system.actorOf(Greeter.props("Hello", printer), "helloGreeter")
  val goodDayGreeter: ActorRef =
    system.actorOf(Greeter.props("Good day", printer), "goodDayGreeter")
  //#create-actors

  //#main-send-messages
  howdyGreeter ! WhoToGreet("Akka")
  howdyGreeter ! Greet

  howdyGreeter ! WhoToGreet("Lightbend")
  howdyGreeter ! Greet

  helloGreeter ! WhoToGreet("Scala")
  helloGreeter ! Greet

  goodDayGreeter ! WhoToGreet("Play")
  goodDayGreeter ! Greet
  //#main-send-messages

  // Hello World Simple
  val talker = system.actorOf(Props[SimpleTalker],"talker")
  // 发送三条消息
  talker ! SimpleGreet("Dante")
  talker ! SimplePraise("Winston")
  talker ! SimpleCelebrate("clare",18)
  
  // Better Greet
  system.actorOf(Props[BetterMaster], "master")
  // Print Ref
  val systemRef = ActorSystem("testSystem")
  val firstRef = systemRef.actorOf(Props[PrintMyActorRefActor],"first-actor")
  println(s"First: $firstRef")
  firstRef ! "printit"
}
//#main-class
//#full-example

// Hello World Simple
case class SimpleGreet(name: String)
case class SimplePraise(name: String)
case class SimpleCelebrate(name: String, age: Int)

class SimpleTalker extends Actor {
  def receive = {
    case SimpleGreet(name) => println(s"Hello $name")
    case SimplePraise(name) => println(s"$name,you're amazing")
    case SimpleCelebrate(name, age) => println(s"Here's to another $age years, $name")
  }
}

// Better World Simple
case class BetterGreet(name: String)
case class BetterPraise(name: String)
case class BetterCelebrate(name: String, age: Int)

class BetterTalker extends Actor {
  def receive() = {
    case BetterGreet(name) => println(s"Hello1 $name")
    case BetterPraise(name) => println(s"$name, you're amazing1")
    case BetterCelebrate(name: String, age: Int) => println(s"Here's to another $age years1, $name")
  }
}

class BetterMaster extends Actor {
  val talker = context.actorOf(Props[BetterTalker], "talker")
  
  override def preStart {
    context.watch(talker)
    
    talker ! BetterGreet("Dante")
    talker ! BetterPraise("Winston")
    talker ! BetterCelebrate("Clare" ,16)
    //发送一个毒丸，告诉actor已经结束了。因此后面发送的消息将不会被传递
    talker ! PoisonPill
    talker ! BetterGreet("Dante")
  }

  def receive = {
     case Terminated(`talker`) => context.system.terminate()
  }
}

// print ref
class PrintMyActorRefActor extends Actor {
    override def receive: Receive = {
      case "printit" =>
        val secondRef = context.actorOf(Props.empty, "second-actor")
        println(s"Second: $secondRef")
    }
}

