package parser

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import lexer.CodeIterator
import lexer.Lexer
import java.io.FileReader

class ParserIntegrationTest: FunSpec({
    test("Should properly parse all tokens") {
        val program = Program(
            listOf(
                Function("isPerfectNumber", Boolean::class, listOf(TypedIdentifier("number", Int::class)),
                    listOf(
                        VarDeclarationStatement("count", Int::class, IntConstant(0)),
                        VarDeclarationStatement("iterator", Int::class, IntConstant(1)),
                        WhileStatement(
                            LesserThanExpression(
                                Variable("iterator"),
                                Variable("number")
                            ),
                            listOf(
                                IfStatement(
                                    NormalComparisonExpression(
                                        ModuloExpression(
                                            Variable("number"),
                                            Variable("iterator")
                                        ),
                                        IntConstant(0)
                                    ),
                                    listOf(
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
                Function("main", Unit::class, listOf(),
                    listOf(
                        VarDeclarationStatement("number", Double::class, FloatConstant(6.5)),
                        FunctionCallStatement(
                            FunctionCallExpression(
                                "isPerfectNumber",
                                listOf(
                                    CastExpression(Variable("number"), Int::class)
                                )
                            )
                        )
                    )
                )
            ),
            emptyList()
        )
        FileReader("build/resources/test/sample.olv").use { file ->
            val parser = Parser(LexerIterator(Lexer(CodeIterator(file))))
            program shouldBe parser.parse()
        }
    }
})
