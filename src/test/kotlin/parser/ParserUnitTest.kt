package parser

import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import lexer.LexerToken
import lexer.TokenType

fun TokenType.toToken(value: Any? = null) = LexerToken(this, value, 0, 0)

fun List<LexerToken>.wrapInProgram(): List<LexerToken> =
    listOf(TokenType.Identifier.toToken("main"), TokenType.LeftParenthesesSign.toToken(),
        TokenType.RightParenthesesSign.toToken(), TokenType.TypeSign.toToken(), TokenType.UnitType.toToken(),
        TokenType.LeftBraceSign.toToken()) + this + listOf(TokenType.RightBraceSign.toToken())

fun List<Statement>.wrapInProgram(): Program =
    Program(listOf(Function("main", Unit, listOf(), this)), listOf())

class ParserUnitTest: FunSpec({
    // Identifier '(' parameters ')' TypeSign Type block;
    context("function and parameters tests") {
        withData(
            nameFn = { "Sequence of $it" },
            listOf(
                TokenType.Identifier.toToken("main"),
                TokenType.LeftParenthesesSign.toToken(),
                TokenType.RightParenthesesSign.toToken(),
                TokenType.TypeSign.toToken(),
                TokenType.UnitType.toToken(),
                TokenType.LeftBraceSign.toToken(),
                TokenType.RightBraceSign.toToken(),
            ) to Program(listOf(
                Function(
                    "main",
                    Unit,
                    listOf(),
                    listOf()
                )
            ), listOf()),
            listOf(
                TokenType.Identifier.toToken("main"),
                TokenType.LeftParenthesesSign.toToken(),
                TokenType.Identifier.toToken("number"),
                TokenType.TypeSign.toToken(),
                TokenType.NumberType.toToken(),
                TokenType.RightParenthesesSign.toToken(),
                TokenType.TypeSign.toToken(),
                TokenType.UnitType.toToken(),
                TokenType.LeftBraceSign.toToken(),
                TokenType.RightBraceSign.toToken(),
            ) to Program(listOf(
                Function(
                    "main",
                    Unit,
                    listOf(
                        TypedIdentifier("number", Number)
                    ),
                    listOf()
                )
            ), listOf()),
            listOf(
                TokenType.Identifier.toToken("main"),
                TokenType.LeftParenthesesSign.toToken(),
                TokenType.Identifier.toToken("number"),
                TokenType.TypeSign.toToken(),
                TokenType.NumberType.toToken(),
                TokenType.EnumerationSign.toToken(),
                TokenType.Identifier.toToken("string"),
                TokenType.TypeSign.toToken(),
                TokenType.StringType.toToken(),
                TokenType.RightParenthesesSign.toToken(),
                TokenType.TypeSign.toToken(),
                TokenType.UnitType.toToken(),
                TokenType.LeftBraceSign.toToken(),
                TokenType.RightBraceSign.toToken(),
            ) to Program(listOf(
                Function(
                    "main",
                    Unit,
                    listOf(
                        TypedIdentifier("number", Number),
                        TypedIdentifier("string", String)
                    ),
                    listOf()
                )
            ), listOf()),
        ) { iterable ->
            Parser(LexerTokenIterator(iterable.first)).parse() shouldBe iterable.second
        }
    }
    // If '(' expression ')' block elseStatement?;
    context("if statement tests") {
        withData(
            nameFn = { "Sequence of $it" },
            listOf(
                TokenType.If.toToken(),
                TokenType.LeftParenthesesSign.toToken(),
                TokenType.BoolConstant.toToken(true),
                TokenType.RightParenthesesSign.toToken(),
                TokenType.LeftBraceSign.toToken(),
                TokenType.RightBraceSign.toToken()
            ).wrapInProgram() to listOf(
                IfStatement(BoolConstant(true), listOf(), null)
            ).wrapInProgram(),
            listOf(
                TokenType.If.toToken(),
                TokenType.LeftParenthesesSign.toToken(),
                TokenType.BoolConstant.toToken(true),
                TokenType.RightParenthesesSign.toToken(),
                TokenType.LeftBraceSign.toToken(),
                TokenType.RightBraceSign.toToken(),
                TokenType.Else.toToken(),
                TokenType.LeftBraceSign.toToken(),
                TokenType.Identifier.toToken("function"),
                TokenType.LeftParenthesesSign.toToken(),
                TokenType.RightParenthesesSign.toToken(),
                TokenType.EndSign.toToken(),
                TokenType.RightBraceSign.toToken()
            ).wrapInProgram() to listOf(
                IfStatement(BoolConstant(true), listOf(), listOf(FunctionCallStatement(FunctionCallExpression("function", listOf()))))
            ).wrapInProgram(),
        ) { iterable ->
            Parser(LexerTokenIterator(iterable.first)).parse() shouldBe iterable.second
        }
    }
    // TODO more tests
})
