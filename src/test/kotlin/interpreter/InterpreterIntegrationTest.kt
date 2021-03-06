package interpreter

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import lexer.CodeIterator
import lexer.Lexer
import parser.*
import java.io.FileReader

class InterpreterIntegrationTest: FunSpec({
    test("Should properly visit all objects in extended_sample.olv") {
        FileReader("build/resources/test/extended_sample.olv").use { file ->
            val program = Parser(LexerIterator(Lexer(CodeIterator(file)))).parse()
            val interpreter = Interpreter()
            interpreter.setFunction(program.funDeclarations)
            interpreter.visit(FunctionCallExpression("isPrime", listOf(IntConstant(7))))
            interpreter.environment.pop().value shouldBe true
            interpreter.visit(FunctionCallExpression("isPrime", listOf(IntConstant(6))))
            interpreter.environment.pop().value shouldBe false
            interpreter.visit(FunctionCallExpression("isPerfectNumber", listOf(IntConstant(7))))
            interpreter.environment.pop().value shouldBe false
            interpreter.visit(FunctionCallExpression("isPerfectNumber", listOf(IntConstant(6))))
            interpreter.environment.pop().value shouldBe true
        }
    }
    test("Should properly visit all objects in chain_call.olv") {
        FileReader("build/resources/test/chain_call.olv").use { file ->
            val program = Parser(LexerIterator(Lexer(CodeIterator(file)))).parse()
            val interpreter = Interpreter()
            interpreter.setFunction(program.funDeclarations)
            interpreter.visit(FunctionCallExpression("chainCall", listOf(IntConstant(6))))
            interpreter.environment.pop().value shouldBe 100
        }
    }
})