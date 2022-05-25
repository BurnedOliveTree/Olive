package interpreter

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import lexer.CodeIterator
import lexer.Lexer
import parser.*
import java.io.FileReader

class InterpreterIntegrationTest: FunSpec({
    test("Should properly visit all objects") {
        FileReader("build/resources/test/extended_sample.cat").use { file ->
            val program = Parser(LexerIterator(Lexer(CodeIterator(file)))).parse()
            val interpreter = Interpreter()
            interpreter.setFunction(program.funDeclarations)
            interpreter.visit(FunctionCallExpression("isPrime", listOf(IntConstant(7))))
            interpreter.value().value shouldBe true
            interpreter.visit(FunctionCallExpression("isPrime", listOf(IntConstant(6))))
            interpreter.value().value shouldBe false
            interpreter.visit(FunctionCallExpression("isPerfectNumber", listOf(IntConstant(7))))
            interpreter.value().value shouldBe false
            interpreter.visit(FunctionCallExpression("isPerfectNumber", listOf(IntConstant(6))))
            interpreter.value().value shouldBe true
        }
    }
})