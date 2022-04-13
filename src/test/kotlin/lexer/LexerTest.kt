package lexer

import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe

class LexerTest: FunSpec({
    context("StringConstant tests") {
        // TODO add strings with escaping
        withData(
            nameFn = { it.first },
            "\"test\"" to LexerToken(TokenType.StringConstant, "test"),
            "\"ąęćśóżź\"" to LexerToken(TokenType.StringConstant, "ąęćśóżź"),
            "\"龙,龍\"" to LexerToken(TokenType.StringConstant, "龙,龍")
        ) { (code, token) ->
            Lexer(code).peek() shouldBe token
        }
    }
    context("NumericConstant tests") {
        withData(
            nameFn = { it.first },
            "0" to LexerToken(TokenType.NumConstant, 0),
            "0.365" to LexerToken(TokenType.NumConstant, 0.365),
            "1462" to LexerToken(TokenType.NumConstant, 1462),
            "45.23" to LexerToken(TokenType.NumConstant, 45.23)
        ) { (code, token) ->
            Lexer(code).peek() shouldBe token
        }
    }
    context("BooleanConstant tests") {
        withData(
            nameFn = { it.first },
            "true" to LexerToken(TokenType.BoolConstant, true),
            "false" to LexerToken(TokenType.BoolConstant, false)
        ) { (code, token) ->
            Lexer(code).peek() shouldBe token
        }
    }
    context("invalid NumericConstant tests") {
        withData(
            nameFn = { it },
            "123.",
            "0."
        ) { code ->
            shouldThrowExactly<LexisError> { Lexer(code).peek() }
        }
    }
})
