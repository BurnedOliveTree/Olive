package lexer

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.io.FileReader

class LexerIntegrationTest: FunSpec({
    test("Should properly parse all tokens") {
        val tokens = listOf(
            LexerToken(TokenType.Identifier, "isPerfectNumber", 1, 1),
            LexerToken(TokenType.LeftParenthesesSign, null, 1, 16),
            LexerToken(TokenType.Identifier, "number", 1, 17),
            LexerToken(TokenType.TypeSign, null, 1, 23),
            LexerToken(TokenType.IntType, null, 1, 25),
            LexerToken(TokenType.RightParenthesesSign, null, 1, 28),
            LexerToken(TokenType.TypeSign, null, 1, 29),
            LexerToken(TokenType.BoolType, null, 1, 31),
            LexerToken(TokenType.LeftBraceSign, null, 1, 36),
            LexerToken(TokenType.Variable, null, 2, 5),
            LexerToken(TokenType.Identifier, "count", 2, 9),
            LexerToken(TokenType.TypeSign, null, 2, 14),
            LexerToken(TokenType.IntType, null, 2, 16),
            LexerToken(TokenType.NormalAssignOp, null, 2, 20),
            LexerToken(TokenType.IntConstant, 0, 2, 22),
            LexerToken(TokenType.EndSign, null, 2, 23),
            LexerToken(TokenType.Variable, null, 3, 5),
            LexerToken(TokenType.Identifier, "iterator", 3, 9),
            LexerToken(TokenType.TypeSign, null, 3, 17),
            LexerToken(TokenType.IntType, null, 3, 19),
            LexerToken(TokenType.NormalAssignOp, null, 3, 23),
            LexerToken(TokenType.IntConstant, 1, 3, 25),
            LexerToken(TokenType.EndSign, null, 3, 26),
            LexerToken(TokenType.While, null, 4, 5),
            LexerToken(TokenType.LeftParenthesesSign, null, 4, 11),
            LexerToken(TokenType.Identifier, "iterator", 4, 12),
            LexerToken(TokenType.LesserThanOp, null, 4, 21),
            LexerToken(TokenType.Identifier, "number", 4, 23),
            LexerToken(TokenType.RightParenthesesSign, null, 4, 29),
            LexerToken(TokenType.LeftBraceSign, null, 4, 31),
            LexerToken(TokenType.If, null, 5, 9),
            LexerToken(TokenType.LeftParenthesesSign, null, 5, 12),
            LexerToken(TokenType.Identifier, "number", 5, 13),
            LexerToken(TokenType.ModuloOp, null, 5, 20),
            LexerToken(TokenType.Identifier, "iterator", 5, 22),
            LexerToken(TokenType.NormalComparisonOp, null, 5, 32),
            LexerToken(TokenType.IntConstant, 0, 5, 34),
            LexerToken(TokenType.RightParenthesesSign, null, 5, 35),
            LexerToken(TokenType.LeftBraceSign, null, 5, 37),
            LexerToken(TokenType.Identifier, "count", 6, 13),
            LexerToken(TokenType.SumAssignOp, null, 6, 20),
            LexerToken(TokenType.Identifier, "iterator", 6, 22),
            LexerToken(TokenType.EndSign, null, 6, 30),
            LexerToken(TokenType.RightBraceSign, null, 7, 9),
            LexerToken(TokenType.Identifier, "iterator", 8, 9),
            LexerToken(TokenType.SumAssignOp, null, 8, 19),
            LexerToken(TokenType.IntConstant, 1, 8, 21),
            LexerToken(TokenType.EndSign, null, 8, 22),
            LexerToken(TokenType.RightBraceSign, null, 9, 5),
            LexerToken(TokenType.Return, null, 10, 5),
            LexerToken(TokenType.Identifier, "count", 10, 12),
            LexerToken(TokenType.NormalComparisonOp, null, 10, 19),
            LexerToken(TokenType.Identifier, "number", 10, 21),
            LexerToken(TokenType.EndSign, null, 10, 27),
            LexerToken(TokenType.RightBraceSign, null, 11, 1),
            LexerToken(TokenType.Identifier, "main", 13, 1),
            LexerToken(TokenType.LeftParenthesesSign, null, 13, 5),
            LexerToken(TokenType.RightParenthesesSign, null, 13, 6),
            LexerToken(TokenType.TypeSign, null, 13, 7),
            LexerToken(TokenType.UnitType, null, 13, 9),
            LexerToken(TokenType.LeftBraceSign, null, 13, 14),
            LexerToken(TokenType.Variable, null, 14, 5),
            LexerToken(TokenType.Identifier, "number", 14, 9),
            LexerToken(TokenType.TypeSign, null, 14, 15),
            LexerToken(TokenType.FloatType, null, 14, 17),
            LexerToken(TokenType.NormalAssignOp, null, 14, 23),
            LexerToken(TokenType.FloatConstant, 6.5, 14, 27),
            LexerToken(TokenType.EndSign, null, 14, 28),
            LexerToken(TokenType.Identifier, "isPerfectNumber", 15, 5),
            LexerToken(TokenType.LeftParenthesesSign, null, 15, 20),
            LexerToken(TokenType.Identifier, "number", 15, 21),
            LexerToken(TokenType.CastOp, null, 15, 28),
            LexerToken(TokenType.IntType, null, 15, 31),
            LexerToken(TokenType.RightParenthesesSign, null, 15, 34),
            LexerToken(TokenType.EndSign, null, 15, 35),
            LexerToken(TokenType.RightBraceSign, null, 16, 1)
        )
        FileReader("build/resources/test/sample.olv").use { file ->
            val lexer = Lexer(CodeIterator(file))

            tokens.forEach {
                lexer.isEmpty() shouldBe false
                lexer.next() shouldBe it
            }
        }
    }
})