package lexer

import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class LexerTest {
    private fun createValuedTestData(tokenType: TokenType, data: List<Pair<String, Any?>>): List<Pair<String, LexerToken>> {
        return data.map { it.first to LexerToken(tokenType, it.second) }
    }

    private fun createTestData(tokenType: TokenType, data: List<String>): List<Pair<String, LexerToken>> {
        return data.map { it to LexerToken(tokenType, null) }
    }

    @Test
    fun stringConstantTest() {
        // TODO add strings with escaping
        createValuedTestData(TokenType.StringConstant,
            listOf("\"test\"" to "test", "\"ąęćśóżź\"" to "ąęćśóżź", "\"龙,龍\"" to "龙,龍")
        ).map { (code, token) ->
            dynamicTest("\"$code\" should create this token: $token") {
                assertEquals(token, Lexer(code).peek())
            }
        }
    }

    @Test
    fun numericConstantTest() {
        createValuedTestData(TokenType.NumConstant,
            listOf("0" to 0, "0.365" to 0.365, "1462" to 1462, "45.23" to 45.23)
        ).map { (code, token) ->
            dynamicTest("\"$code\" should create this token: $token") {
                assertEquals(token, Lexer(code).peek())
            }
        }
    }

    @Test
    fun booleanConstantTest() {
        createValuedTestData(TokenType.BoolConstant,
            listOf("true" to true, "false" to false)
        ).map { (code, token) ->
            dynamicTest("\"$code\" should create this token: $token") {
                assertEquals(token, Lexer(code).peek())
            }
        }
    }

    @Test
    fun improperNumericConstantTest() {
        listOf("000", "0123.456", ".123", "123.").map { code ->
            dynamicTest("\"$code\" should throw LexisError") {
                assertThrows<LexisError> { Lexer(code).peek() }
            }
        }
    }
}