package ls

import ls.model.{LsFile, LsOptions, LsResult}
import org.scalatest.funsuite.AnyFunSuiteLike
import org.scalatest.matchers.should.Matchers.*

class LsFunctionsCallerTest extends AnyFunSuiteLike {
  test("calls toByteArray"):
    val byteData                                = Array(1, 2).map(_.toByte)
    val expected                                = LsResult(Seq(LsFile("test")))
    def toByteArray(p: LsFunctionsMethodParams) = byteData
    def callFunction(a: Array[Byte])            =
      a should be(byteData)
      expected
    val caller                                  = new LsFunctionsCaller(toByteArray, callFunction)
    caller.ls("/home", LsOptions()) should be(expected)
}
