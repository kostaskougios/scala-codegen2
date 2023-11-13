package functions.sockets

import functions.model.{Coordinates4, ReceiverInput}

import java.io.InputStream
import java.net.{ServerSocket, Socket}
import java.util
import java.util.concurrent.atomic.{AtomicBoolean, AtomicInteger, AtomicLong}
import java.util.concurrent.{ExecutorService, Executors, Future}

// https://wiki.openjdk.org/display/loom/Getting+started
// https://openjdk.org/jeps/444

class FiberSocketServer private (serverSocket: ServerSocket, executor: ExecutorService):
  def shutdown(): Unit =
    interruptServerThread()
    try executor.shutdown()
    catch case t: Throwable => t.printStackTrace()
    try serverSocket.close()
    catch case t: Throwable => t.printStackTrace()

  @volatile private var serverThread: Thread = null
  private val stopServer                     = new AtomicBoolean(false)
  private val accepting                      = new AtomicBoolean(false)
  private val totalRequestCounter            = new AtomicLong(0)
  private val servingCounter                 = new AtomicInteger(0)
  def isAccepting: Boolean                   = accepting.get()
  def totalRequestCount: Long                = totalRequestCounter.get()
  def servingCount: Long                     = servingCounter.get()

  private def interruptServerThread(): Unit =
    stopServer.set(true)
    Thread.`yield`()
    if serverThread != null then serverThread.interrupt()
    serverThread = null

  private def acceptOneSocketConnection(invokerMap: Map[Coordinates4, ReceiverInput => Array[Byte]]): Unit =
    serverThread = Thread.currentThread()
    if !stopServer.get() then
      try
        accepting.set(true)
        val clientSocket = serverSocket.accept()
        executor.submit(runnable(clientSocket, invokerMap))
      finally accepting.set(false)

  private def start(invokerMap: Map[Coordinates4, ReceiverInput => Array[Byte]]): Future[_] =
    def listen: Runnable =
      () =>
        while (!stopServer.get())
          try acceptOneSocketConnection(invokerMap)
          catch case t: Throwable => logError(t)
    executor.submit(listen)

  protected def logError(throwable: Throwable): Unit =
    if !stopServer.get() then throwable.printStackTrace()

  private def runnable(s: Socket, invokerMap: Map[Coordinates4, ReceiverInput => Array[Byte]]): Runnable =
    () =>
      try
        val in  = s.getInputStream
        val out = s.getOutputStream

        def serveOne(): Unit =
          in.read() match
            case -1       => Thread.`yield`()
            case coordsSz =>
              try
                totalRequestCounter.incrementAndGet()
                servingCounter.incrementAndGet()
                val coordsRaw   = new String(in.readNBytes(coordsSz), "UTF-8")
                val coordinates = Coordinates4(coordsRaw)
                checkStopped()
                val inData      = inputStreamToByteArray(in)
                checkStopped()
                val outData     = invokerMap(coordinates)(ReceiverInput(inData))
                out.write(outData.length)
                out.write(outData)
                out.flush()
              finally servingCounter.decrementAndGet()

        while (s.isConnected)
          serveOne()
      catch case t: Throwable => logError(t)
      finally s.close()

  private def inputStreamToByteArray(inputStream: InputStream): Array[Byte] =
    val dataSz = inputStream.read()
    val data   = new Array[Byte](dataSz)
    inputStream.read(data)
    data

  private def checkStopped(): Unit = if stopServer.get() then throw new InterruptedException("Server is stopped")

object FiberSocketServer:
  def withServer[R](listenPort: Int, invokerMap: Map[Coordinates4, ReceiverInput => Array[Byte]])(f: FiberSocketServer => R) =
    val server   = new ServerSocket(listenPort)
    val executor = Executors.newVirtualThreadPerTaskExecutor
    val s        = new FiberSocketServer(server, executor)
    try
      s.start(invokerMap)
      var i = 8192
      while (!s.isAccepting && i > 0)
        i = i - 1
        Thread.`yield`()
      if i == 0 then throw new IllegalStateException("Could not start accepting requests.")
      f(s)
    finally s.shutdown()
