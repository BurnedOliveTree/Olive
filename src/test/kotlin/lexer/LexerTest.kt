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
    test("UnitType test") {
        Lexer("Unit").peek() shouldBe LexerToken(TokenType.UnitType)
    }
    test("IntegerType test") {
        Lexer("Int").peek() shouldBe LexerToken(TokenType.IntType)
    }
    test("FloatType test") {
        Lexer("Float").peek() shouldBe LexerToken(TokenType.FloatType)
    }
    test("NumberType test") {
        Lexer("Number").peek() shouldBe LexerToken(TokenType.NumberType)
    }
    test("StringType test") {
        Lexer("String").peek() shouldBe LexerToken(TokenType.StringType)
    }
    test("BooleanType test") {
        Lexer("Bool").peek() shouldBe LexerToken(TokenType.BoolType)
    }
    test("SumOperator test") {
        Lexer("+").peek() shouldBe LexerToken(TokenType.SumOp)
    }
    test("DifferenceOperator test") {
        Lexer("-").peek() shouldBe LexerToken(TokenType.DifferenceOp)
    }
    test("MultiplicationOperator test") {
        Lexer("*").peek() shouldBe LexerToken(TokenType.MultiplicationOp)
    }
    test("ExponentOperator test") {
        Lexer("^").peek() shouldBe LexerToken(TokenType.ExponentOp)
    }
    test("DivisionOperator test") {
        Lexer("/").peek() shouldBe LexerToken(TokenType.DivisionOp)
    }
    test("RootOperator test") {
        Lexer("|").peek() shouldBe LexerToken(TokenType.RootOp)
    }
    test("ModuloOperator test") {
        Lexer("%").peek() shouldBe LexerToken(TokenType.ModuloOp)
    }
})
