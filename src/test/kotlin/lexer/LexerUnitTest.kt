package lexer

import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe

class LexerUnitTest: FunSpec({
    // TODO whitespace and positions tests
    context("StringConstant tests") {
        withData(
            nameFn = { it.first },
            "\"test\"" to LexerToken(TokenType.StringConstant, "test"),
            "\"ąęćśóżź\"" to LexerToken(TokenType.StringConstant, "ąęćśóżź"),
            "\"龙,龍\"" to LexerToken(TokenType.StringConstant, "龙,龍"),
            "\"\\\"\"" to LexerToken(TokenType.StringConstant, "\""),
            "\"\\\\\"" to LexerToken(TokenType.StringConstant, "\\")
        ) { (code, token) ->
            Lexer(code).next() shouldBe token
        }
    }
    context("invalid StringConstant tests") {
        withData(
            nameFn = { it },
            "\"",
            "\"invalid"
        ) { code ->
            shouldThrowExactly<MissingSignError> { Lexer(code).next() }
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
            Lexer(code).next() shouldBe token
        }
    }
    context("BooleanConstant tests") {
        withData(
            nameFn = { it.first },
            "true" to LexerToken(TokenType.BoolConstant, true),
            "false" to LexerToken(TokenType.BoolConstant, false)
        ) { (code, token) ->
            Lexer(code).next() shouldBe token
        }
    }
    context("invalid NumericConstant tests") {
        withData(
            nameFn = { it },
            "123.",
            "0."
        ) { code ->
            shouldThrowExactly<MissingSignError> { Lexer(code).next() }
        }
    }
    test("UnitType test") {
        Lexer("Unit").next() shouldBe LexerToken(TokenType.UnitType)
    }
    test("IntegerType test") {
        Lexer("Int").next() shouldBe LexerToken(TokenType.IntType)
    }
    test("FloatType test") {
        Lexer("Float").next() shouldBe LexerToken(TokenType.FloatType)
    }
    test("NumberType test") {
        Lexer("Number").next() shouldBe LexerToken(TokenType.NumberType)
    }
    test("StringType test") {
        Lexer("String").next() shouldBe LexerToken(TokenType.StringType)
    }
    test("BooleanType test") {
        Lexer("Bool").next() shouldBe LexerToken(TokenType.BoolType)
    }
    test("SumOperator test") {
        Lexer("+").next() shouldBe LexerToken(TokenType.SumOp)
    }
    test("DifferenceOperator test") {
        Lexer("-").next() shouldBe LexerToken(TokenType.DifferenceOp)
    }
    test("MultiplicationOperator test") {
        Lexer("*").next() shouldBe LexerToken(TokenType.MultiplicationOp)
    }
    test("ExponentOperator test") {
        Lexer("^").next() shouldBe LexerToken(TokenType.ExponentOp)
    }
    test("DivisionOperator test") {
        Lexer("/").next() shouldBe LexerToken(TokenType.DivisionOp)
    }
    test("RootOperator test") {
        Lexer("|").next() shouldBe LexerToken(TokenType.RootOp)
    }
    test("ModuloOperator test") {
        Lexer("%").next() shouldBe LexerToken(TokenType.ModuloOp)
    }
    test("NotOperator test") {
        Lexer("not").next() shouldBe LexerToken(TokenType.NotOp)
    }
    test("AndOperator test") {
        Lexer("and").next() shouldBe LexerToken(TokenType.AndOp)
    }
    test("OrOperator test") {
        Lexer("or").next() shouldBe LexerToken(TokenType.OrOp)
    }
    test("IsOperator test") {
        Lexer("is").next() shouldBe LexerToken(TokenType.IsOp)
    }
    test("CastOperator test") {
        Lexer("as").next() shouldBe LexerToken(TokenType.CastOp)
    }
    test("NormalAssignmentOperator test") {
        Lexer("=").next() shouldBe LexerToken(TokenType.NormalAssignOp)
    }
    test("ReferenceAssignmentOperator test") {
        Lexer("&=").next() shouldBe LexerToken(TokenType.ReferenceAssignOp)
    }
    test("SumAssignmentOperator test") {
        Lexer("+=").next() shouldBe LexerToken(TokenType.SumAssignOp)
    }
    test("DifferenceAssignmentOperator test") {
        Lexer("-=").next() shouldBe LexerToken(TokenType.DifferenceAssignOp)
    }
    test("MultiplicationAssignmentOperator test") {
        Lexer("*=").next() shouldBe LexerToken(TokenType.MultiplicationAssignOp)
    }
    test("ExponentAssignmentOperator test") {
        Lexer("^=").next() shouldBe LexerToken(TokenType.ExponentAssignOp)
    }
    test("DivisionAssignmentOperator test") {
        Lexer("/=").next() shouldBe LexerToken(TokenType.DivisionAssignOp)
    }
    test("RootAssignmentOperator test") {
        Lexer("|=").next() shouldBe LexerToken(TokenType.RootAssignOp)
    }
    test("ModuloAssignmentOperator test") {
        Lexer("%=").next() shouldBe LexerToken(TokenType.ModuloAssignOp)
    }
    test("NormalComparisonOperator test") {
        Lexer("==").next() shouldBe LexerToken(TokenType.NormalComparisonOp)
    }
    test("ReferenceComparisonOperator test") {
        Lexer("&==").next() shouldBe LexerToken(TokenType.ReferenceComparisonOp)
    }
    test("LesserThanOperator test") {
        Lexer("<").next() shouldBe LexerToken(TokenType.LesserThanOp)
    }
    test("LesserOrEqualThanOperator test") {
        Lexer("<=").next() shouldBe LexerToken(TokenType.LesserOrEqualOp)
    }
    test("GreaterThanOperator test") {
        Lexer(">").next() shouldBe LexerToken(TokenType.GreaterThanOp)
    }
    test("TypeSign test") {
        Lexer(":").next() shouldBe LexerToken(TokenType.TypeSign)
    }
    test("EndOfLineSign test") {
        Lexer(";").next() shouldBe LexerToken(TokenType.EndSign)
    }
    test("ArgumentEnumerationSign test") {
        Lexer(",").next() shouldBe LexerToken(TokenType.EnumerationSign)
    }
    test("MemberReferenceSign test") {
        Lexer(".").next() shouldBe LexerToken(TokenType.MemberReferenceSign)
    }
    test("VariableKeyword test") {
        Lexer("var").next() shouldBe LexerToken(TokenType.Variable)
    }
    test("IfKeyword test") {
        Lexer("if").next() shouldBe LexerToken(TokenType.If)
    }
    test("ElseKeyword test") {
        Lexer("else").next() shouldBe LexerToken(TokenType.Else)
    }
    test("WhileKeyword test") {
        Lexer("while").next() shouldBe LexerToken(TokenType.While)
    }
    test("ReturnKeyword test") {
        Lexer("return").next() shouldBe LexerToken(TokenType.Return)
    }
    context("Identifier tests") {
        withData(
            nameFn = { it.first },
            "identifier" to LexerToken(TokenType.Identifier, "identifier"),
            "f" to LexerToken(TokenType.Identifier, "f"),
            "fals" to LexerToken(TokenType.Identifier, "fals"),
            "isEmpty" to LexerToken(TokenType.Identifier, "isEmpty"),
            "andorvar" to LexerToken(TokenType.Identifier, "andorvar"),
            "ąęćśóżž" to LexerToken(TokenType.Identifier, "ąęćśóżž"),
            "_龙龍" to LexerToken(TokenType.Identifier, "_龙龍"),
        ) { (code, token) ->
            Lexer(code).next() shouldBe token
        }
    }
    context("Comment tests") {
        withData(
            nameFn = { it.first },
            "#" to LexerToken(TokenType.Comment, ""),
            "# " to LexerToken(TokenType.Comment, " "),
            "# \"" to LexerToken(TokenType.Comment, " \""),
            "# some comment \n" to LexerToken(TokenType.Comment, " some comment "),
            "# var x: Int\nvar x: Int" to LexerToken(TokenType.Comment, " var x: Int")
        ) { (code, token) ->
            Lexer(code).let { it.next() } shouldBe token
        }
    }
    context("invalid signs tests") {
        withData(
            nameFn = { it },
            "@",
            "$",
            "'"
        ) { code ->
            shouldThrowExactly<UnrecognizedSignError> { Lexer(code).next() }
        }
    }
})
