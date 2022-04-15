package lexer

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import parseFile

class LexerIntegrationTest: FunSpec({
    test("Should properly parse all tokens") {
        val sourceCode = parseFile("build/resources/test/sample.cat")
        val tokens = listOf(
            LexerToken(TokenType.Identifier, "isPerfectNumber"),
            LexerToken(TokenType.LeftParenthesesSign),
            LexerToken(TokenType.Identifier, "number"),
            LexerToken(TokenType.TypeSign),
            LexerToken(TokenType.IntType),
            LexerToken(TokenType.RightParenthesesSign),
            LexerToken(TokenType.TypeSign),
            LexerToken(TokenType.BoolType),
            LexerToken(TokenType.LeftBraceSign),
            LexerToken(TokenType.Variable),
            LexerToken(TokenType.Identifier, "count"),
            LexerToken(TokenType.TypeSign),
            LexerToken(TokenType.IntType),
            LexerToken(TokenType.NormalAssignOp),
            LexerToken(TokenType.NumConstant, 0),
            LexerToken(TokenType.EndSign),
            LexerToken(TokenType.Variable),
            LexerToken(TokenType.Identifier, "iterator"),
            LexerToken(TokenType.TypeSign),
            LexerToken(TokenType.IntType),
            LexerToken(TokenType.NormalAssignOp),
            LexerToken(TokenType.NumConstant, 1),
            LexerToken(TokenType.EndSign),
            LexerToken(TokenType.While),
            LexerToken(TokenType.LeftParenthesesSign),
            LexerToken(TokenType.Identifier, "iterator"),
            LexerToken(TokenType.LesserThanOp),
            LexerToken(TokenType.Identifier, "number"),
            LexerToken(TokenType.RightParenthesesSign),
            LexerToken(TokenType.LeftBraceSign),
            LexerToken(TokenType.If),
            LexerToken(TokenType.LeftParenthesesSign),
            LexerToken(TokenType.Identifier, "number"),
            LexerToken(TokenType.ModuloOp),
            LexerToken(TokenType.Identifier, "iterator"),
            LexerToken(TokenType.NormalComparisonOp),
            LexerToken(TokenType.NumConstant, 0),
            LexerToken(TokenType.RightParenthesesSign),
            LexerToken(TokenType.LeftBraceSign),
            LexerToken(TokenType.Identifier, "count"),
            LexerToken(TokenType.SumAssignOp),
            LexerToken(TokenType.Identifier, "iterator"),
            LexerToken(TokenType.EndSign),
            LexerToken(TokenType.RightBraceSign),
            LexerToken(TokenType.Identifier, "iterator"),
            LexerToken(TokenType.SumAssignOp),
            LexerToken(TokenType.NumConstant, 1),
            LexerToken(TokenType.EndSign),
            LexerToken(TokenType.RightBraceSign),
            LexerToken(TokenType.Return),
            LexerToken(TokenType.Identifier, "count"),
            LexerToken(TokenType.NormalComparisonOp),
            LexerToken(TokenType.Identifier, "number"),
            LexerToken(TokenType.EndSign),
            LexerToken(TokenType.RightBraceSign),
            LexerToken(TokenType.Identifier, "main"),
            LexerToken(TokenType.LeftParenthesesSign),
            LexerToken(TokenType.RightParenthesesSign),
            LexerToken(TokenType.TypeSign),
            LexerToken(TokenType.UnitType),
            LexerToken(TokenType.LeftBraceSign),
            LexerToken(TokenType.Variable),
            LexerToken(TokenType.Identifier, "number"),
            LexerToken(TokenType.TypeSign),
            LexerToken(TokenType.FloatType),
            LexerToken(TokenType.NormalAssignOp),
            LexerToken(TokenType.NumConstant, 6.5),
            LexerToken(TokenType.EndSign),
            LexerToken(TokenType.Identifier, "isPerfectNumber"),
            LexerToken(TokenType.LeftParenthesesSign),
            LexerToken(TokenType.Identifier, "number"),
            LexerToken(TokenType.CastOp),
            LexerToken(TokenType.IntType),
            LexerToken(TokenType.RightParenthesesSign),
            LexerToken(TokenType.EndSign),
            LexerToken(TokenType.RightBraceSign)
        )
        val lexer = Lexer(sourceCode)
        tokens.forEach {
            lexer.peek() shouldBe it
            lexer.next()
        }
    }
})