package endtoend.tests.loomsockets

import endtoend.tests.{SimpleFunctionsCallerFactory, SimpleFunctionsImpl, SimpleFunctionsReceiverFactory}
import functions.sockets.{FiberSocketServer, SocketTransport}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.*

class LoomSocketsSuite extends AnyFunSuite:
  val transport    = new SocketTransport("localhost", 7200)
  val invokerMap   = SimpleFunctionsReceiverFactory.invokerMap(new SimpleFunctionsImpl)
  def createServer = FiberSocketServer.withServer[Unit](7200, invokerMap) _

  val caller = SimpleFunctionsCallerFactory.newAvroSimpleFunctions(transport.transportFunction)

  test("client/server") {
    createServer: server =>
      caller.add(5, 6) should be(11)
  }

  test("server can be shut down") {
    createServer: server =>
      Thread.sleep(200)
  }
