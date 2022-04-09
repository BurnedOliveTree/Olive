package lexer

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class LexerTest {
    @Test
    fun booleanConstantTest() {
        assertEquals(
            LexerToken(TokenType.BoolConstant, true),
            Lexer("true").peek()
        )
        assertEquals(
            LexerToken(TokenType.BoolConstant, false),
            Lexer("false").peek()
        )
    }
}