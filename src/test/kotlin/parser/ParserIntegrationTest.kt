package parser

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import lexer.CodeIterator
import lexer.Lexer
import java.io.FileReader

class ParserIntegrationTest: FunSpec({
    test("Should properly parse all tokens") {
        val program = Program(
            arrayOf(
                Function("isPerfectNumber", Boolean, arrayOf(TypedIdentifier("number", Int)),
                    arrayOf(
                        VarDeclarationStatement("count", Int, IntConstant(0)),
                        VarDeclarationStatement("iterator", Int, IntConstant(1)),
                        WhileStatement(
                            LesserThanExpression(
                                Variable("iterator"),
                                Variable("number")
                            ),
                            arrayOf(
                                IfStatement(
                                    NormalComparisonExpression(
                                        ModuloExpression(
                                            Variable("number"),
                                            Variable("iterator")
                                        ),
                                        IntConstant(0)
                                    ),
                                    arrayOf(
                                        SumAssignmentStatement(
                                            Variable("count"),
                                            Variable("iterator")
                                        )
                                    ),
                                    null
                                ),
                                SumAssignmentStatement(
                                    Variable("iterator"),
                                    IntConstant(1)
                                )
                            )
                        ),
                        ReturnStatement(
                            NormalComparisonExpression(
                                Variable("count"),
                                Variable("number")
                            )
                        )
                    )
                ),
                Function("main", Unit, arrayOf(),
                    arrayOf(
                        VarDeclarationStatement("number", Float, FloatConstant(6.5)),
                        FunctionCallStatement(
                            FunctionCallExpression(
                                "isPerfectNumber",
                                arrayOf(
                                    CastExpression(Variable("number"), Int)
                                )
                            )
                        )
                    )
                )
            ),
            emptyArray()
        )
        FileReader("build/resources/test/sample.cat").use { file ->
            val parser = Parser(LexerIterator(Lexer(CodeIterator(file))))
            program shouldBe parser.parse()
        }
    }
})
