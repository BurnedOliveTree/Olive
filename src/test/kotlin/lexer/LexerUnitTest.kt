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
            Lexer(code).peek() shouldBe token
        }
    }
    context("invalid StringConstant tests") {
        withData(
            nameFn = { it },
            "\"",
            "\"invalid"
        ) { code ->
            shouldThrowExactly<LexisError> { Lexer(code).peek() }
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
    test("NotOperator test") {
        Lexer("not").peek() shouldBe LexerToken(TokenType.NotOp)
    }
    test("AndOperator test") {
        Lexer("and").peek() shouldBe LexerToken(TokenType.AndOp)
    }
    test("OrOperator test") {
        Lexer("or").peek() shouldBe LexerToken(TokenType.OrOp)
    }
    test("IsOperator test") {
        Lexer("is").peek() shouldBe LexerToken(TokenType.IsOp)
    }
    test("CastOperator test") {
        Lexer("as").peek() shouldBe LexerToken(TokenType.CastOp)
    }
    test("NormalAssignmentOperator test") {
        Lexer("=").peek() shouldBe LexerToken(TokenType.NormalAssignOp)
    }
    test("ReferenceAssignmentOperator test") {
        Lexer("&=").peek() shouldBe LexerToken(TokenType.ReferenceAssignOp)
    }
    test("SumAssignmentOperator test") {
        Lexer("+=").peek() shouldBe LexerToken(TokenType.SumAssignOp)
    }
    test("DifferenceAssignmentOperator test") {
        Lexer("-=").peek() shouldBe LexerToken(TokenType.DifferenceAssignOp)
    }
    test("MultiplicationAssignmentOperator test") {
        Lexer("*=").peek() shouldBe LexerToken(TokenType.MultiplicationAssignOp)
    }
    test("ExponentAssignmentOperator test") {
        Lexer("^=").peek() shouldBe LexerToken(TokenType.ExponentAssignOp)
    }
    test("DivisionAssignmentOperator test") {
        Lexer("/=").peek() shouldBe LexerToken(TokenType.DivisionAssignOp)
    }
    test("RootAssignmentOperator test") {
        Lexer("|=").peek() shouldBe LexerToken(TokenType.RootAssignOp)
    }
    test("ModuloAssignmentOperator test") {
        Lexer("%=").peek() shouldBe LexerToken(TokenType.ModuloAssignOp)
    }
    test("NormalComparisonOperator test") {
        Lexer("==").peek() shouldBe LexerToken(TokenType.NormalComparisonOp)
    }
    test("ReferenceComparisonOperator test") {
        Lexer("&==").peek() shouldBe LexerToken(TokenType.ReferenceComparisonOp)
    }
    test("LesserThanOperator test") {
        Lexer("<").peek() shouldBe LexerToken(TokenType.LesserThanOp)
    }
    test("LesserOrEqualThanOperator test") {
        Lexer("<=").peek() shouldBe LexerToken(TokenType.LesserOrEqualOp)
    }
    test("GreaterThanOperator test") {
        Lexer(">").peek() shouldBe LexerToken(TokenType.GreaterThanOp)
    }
    test("TypeSign test") {
        Lexer(":").peek() shouldBe LexerToken(TokenType.TypeSign)
    }
    test("CommentSign test") {
        Lexer("#").peek() shouldBe LexerToken(TokenType.CommentSign)
    }
    test("EndOfLineSign test") {
        Lexer(";").peek() shouldBe LexerToken(TokenType.EndSign)
    }
    test("ArgumentEnumerationSign test") {
        Lexer(",").peek() shouldBe LexerToken(TokenType.EnumerationSign)
    }
    test("MemberReferenceSign test") {
        Lexer(".").peek() shouldBe LexerToken(TokenType.MemberReferenceSign)
    }
    test("VariableKeyword test") {
        Lexer("var").peek() shouldBe LexerToken(TokenType.Variable)
    }
    test("IfKeyword test") {
        Lexer("if").peek() shouldBe LexerToken(TokenType.If)
    }
    test("ElseKeyword test") {
        Lexer("else").peek() shouldBe LexerToken(TokenType.Else)
    }
    test("WhileKeyword test") {
        Lexer("while").peek() shouldBe LexerToken(TokenType.While)
    }
    test("ReturnKeyword test") {
        Lexer("return").peek() shouldBe LexerToken(TokenType.Return)
    }
    test("Whitespace test") {
        val lexer = Lexer("is\n\n\n0.")
        lexer.next()
        lexer.next()
        lexer.next()
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
            Lexer(code).peek() shouldBe token
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
            Lexer(code).let { it.next(); it.peek() } shouldBe token
        }
    }
    context("invalid signs tests") {
        withData(
            nameFn = { it },
            "@",
            "$",
            "'"
        ) { code ->
            shouldThrowExactly<LexisError> { Lexer(code).peek() }
        }
    }
})
