package lexer

import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe

class LexerUnitTest: FunSpec({
    context("Whitespace tests") {
        withData(
            nameFn = { "Sign \"$it\"" },
            " ",
            "\t",
            "\n",
            "\r"
        ) { code ->
            Lexer(CodeIterator(code)).isEmpty()
        }
    }
    context("StringConstant tests") {
        withData(
            nameFn = { it.first },
            "\"test\"" to listOf(TokenType.StringConstant, "test"),
            "\"ąęćśóżź\"" to listOf(TokenType.StringConstant, "ąęćśóżź"),
            "\"龙,龍\"" to listOf(TokenType.StringConstant, "龙,龍"),
            "\"\\\"\"" to listOf(TokenType.StringConstant, "\""),
            "\"\\\\\"" to listOf(TokenType.StringConstant, "\\")
        ) { (code, token) ->
            Lexer(CodeIterator(code)).next().let {
                it.type shouldBe token[0]
                it.value shouldBe token[1]
            }
        }
    }
    context("invalid StringConstant tests") {
        withData(
            nameFn = { it },
            "\"",
            "\"invalid"
        ) { code ->
            shouldThrowExactly<MissingSignError> { Lexer(CodeIterator(code)).next() }
        }
    }
    context("NumericConstant tests") {
        withData(
            nameFn = { it.first },
            "0" to listOf(TokenType.NumConstant, 0),
            "0.365" to listOf(TokenType.NumConstant, 0.365),
            "1462" to listOf(TokenType.NumConstant, 1462),
            "45.23" to listOf(TokenType.NumConstant, 45.23)
        ) { (code, token) ->
            Lexer(CodeIterator(code)).next().let {
                it.type shouldBe token[0]
                it.value shouldBe token[1]
            }
        }
    }
    context("invalid NumericConstant tests (TokenTooBigError)") {
        withData(
            nameFn = { it },
            "99999999999999999999999999999999",
            "0.99999999999999999999999999999999"
        ) { code ->
            shouldThrowExactly<TokenTooBigError> { Lexer(CodeIterator(code)).next() }
        }
    }
    context("invalid NumericConstant tests (MissingSignError)") {
        withData(
            nameFn = { it },
            "123.",
            "0."
        ) { code ->
            shouldThrowExactly<MissingSignError> { Lexer(CodeIterator(code)).next() }
        }
    }
    context("BooleanConstant tests") {
        withData(
            nameFn = { it.first },
            "true" to listOf(TokenType.BoolConstant, true),
            "false" to listOf(TokenType.BoolConstant, false)
        ) { (code, token) ->
            Lexer(CodeIterator(code)).next().let {
                it.type shouldBe token[0]
                it.value shouldBe token[1]
            }
        }
    }
    test("UnitType test") {
        Lexer(CodeIterator("Unit")).next().type shouldBe TokenType.UnitType
    }
    test("IntegerType test") {
        Lexer(CodeIterator("Int")).next().type shouldBe TokenType.IntType
    }
    test("FloatType test") {
        Lexer(CodeIterator("Float")).next().type shouldBe TokenType.FloatType
    }
    test("NumberType test") {
        Lexer(CodeIterator("Number")).next().type shouldBe TokenType.NumberType
    }
    test("StringType test") {
        Lexer(CodeIterator("String")).next().type shouldBe TokenType.StringType
    }
    test("BooleanType test") {
        Lexer(CodeIterator("Bool")).next().type shouldBe TokenType.BoolType
    }
    test("SumOperator test") {
        Lexer(CodeIterator("+")).next().type shouldBe TokenType.SumOp
    }
    test("DifferenceOperator test") {
        Lexer(CodeIterator("-")).next().type shouldBe TokenType.DifferenceOp
    }
    test("MultiplicationOperator test") {
        Lexer(CodeIterator("*")).next().type shouldBe TokenType.MultiplicationOp
    }
    test("ExponentOperator test") {
        Lexer(CodeIterator("^")).next().type shouldBe TokenType.ExponentOp
    }
    test("DivisionOperator test") {
        Lexer(CodeIterator("/")).next().type shouldBe TokenType.DivisionOp
    }
    test("RootOperator test") {
        Lexer(CodeIterator("|")).next().type shouldBe TokenType.RootOp
    }
    test("ModuloOperator test") {
        Lexer(CodeIterator("%")).next().type shouldBe TokenType.ModuloOp
    }
    test("NotOperator test") {
        Lexer(CodeIterator("not")).next().type shouldBe TokenType.NotOp
    }
    test("AndOperator test") {
        Lexer(CodeIterator("and")).next().type shouldBe TokenType.AndOp
    }
    test("OrOperator test") {
        Lexer(CodeIterator("or")).next().type shouldBe TokenType.OrOp
    }
    test("IsOperator test") {
        Lexer(CodeIterator("is")).next().type shouldBe TokenType.IsOp
    }
    test("CastOperator test") {
        Lexer(CodeIterator("as")).next().type shouldBe TokenType.CastOp
    }
    test("NormalAssignmentOperator test") {
        Lexer(CodeIterator("=")).next().type shouldBe TokenType.NormalAssignOp
    }
    test("ReferenceAssignmentOperator test") {
        Lexer(CodeIterator("&=")).next().type shouldBe TokenType.ReferenceAssignOp
    }
    test("SumAssignmentOperator test") {
        Lexer(CodeIterator("+=")).next().type shouldBe TokenType.SumAssignOp
    }
    test("DifferenceAssignmentOperator test") {
        Lexer(CodeIterator("-=")).next().type shouldBe TokenType.DifferenceAssignOp
    }
    test("MultiplicationAssignmentOperator test") {
        Lexer(CodeIterator("*=")).next().type shouldBe TokenType.MultiplicationAssignOp
    }
    test("ExponentAssignmentOperator test") {
        Lexer(CodeIterator("^=")).next().type shouldBe TokenType.ExponentAssignOp
    }
    test("DivisionAssignmentOperator test") {
        Lexer(CodeIterator("/=")).next().type shouldBe TokenType.DivisionAssignOp
    }
    test("RootAssignmentOperator test") {
        Lexer(CodeIterator("|=")).next().type shouldBe TokenType.RootAssignOp
    }
    test("ModuloAssignmentOperator test") {
        Lexer(CodeIterator("%=")).next().type shouldBe TokenType.ModuloAssignOp
    }
    test("NormalComparisonOperator test") {
        Lexer(CodeIterator("==")).next().type shouldBe TokenType.NormalComparisonOp
    }
    test("ReferenceComparisonOperator test") {
        Lexer(CodeIterator("&==")).next().type shouldBe TokenType.ReferenceComparisonOp
    }
    test("LesserThanOperator test") {
        Lexer(CodeIterator("<")).next().type shouldBe TokenType.LesserThanOp
    }
    test("LesserOrEqualThanOperator test") {
        Lexer(CodeIterator("<=")).next().type shouldBe TokenType.LesserOrEqualOp
    }
    test("GreaterThanOperator test") {
        Lexer(CodeIterator(">")).next().type shouldBe TokenType.GreaterThanOp
    }
    test("TypeSign test") {
        Lexer(CodeIterator(":")).next().type shouldBe TokenType.TypeSign
    }
    test("EndOfLineSign test") {
        Lexer(CodeIterator(";")).next().type shouldBe TokenType.EndSign
    }
    test("ArgumentEnumerationSign test") {
        Lexer(CodeIterator(",")).next().type shouldBe TokenType.EnumerationSign
    }
    test("MemberReferenceSign test") {
        Lexer(CodeIterator(".")).next().type shouldBe TokenType.MemberReferenceSign
    }
    test("VariableKeyword test") {
        Lexer(CodeIterator("var")).next().type shouldBe TokenType.Variable
    }
    test("IfKeyword test") {
        Lexer(CodeIterator("if")).next().type shouldBe TokenType.If
    }
    test("ElseKeyword test") {
        Lexer(CodeIterator("else")).next().type shouldBe TokenType.Else
    }
    test("WhileKeyword test") {
        Lexer(CodeIterator("while")).next().type shouldBe TokenType.While
    }
    test("ReturnKeyword test") {
        Lexer(CodeIterator("return")).next().type shouldBe TokenType.Return
    }
    context("Identifier tests") {
        withData(
            nameFn = { it.first },
            "identifier" to listOf(TokenType.Identifier, "identifier"),
            "f" to listOf(TokenType.Identifier, "f"),
            "fals" to listOf(TokenType.Identifier, "fals"),
            "isEmpty" to listOf(TokenType.Identifier, "isEmpty"),
            "andorvar" to listOf(TokenType.Identifier, "andorvar"),
            "ąęćśóżž" to listOf(TokenType.Identifier, "ąęćśóżž"),
            "_龙龍" to listOf(TokenType.Identifier, "_龙龍"),
        ) { (code, token) ->
            Lexer(CodeIterator(code)).next().let {
                it.type shouldBe token[0]
                it.value shouldBe token[1]
            }
        }
    }
    context("Comment tests") {
        withData(
            nameFn = { it.first },
            "#" to listOf(TokenType.Comment, ""),
            "# " to listOf(TokenType.Comment, " "),
            "# \"" to listOf(TokenType.Comment, " \""),
            "# some comment \n" to listOf(TokenType.Comment, " some comment "),
            "# var x: Int\nvar x: Int" to listOf(TokenType.Comment, " var x: Int")
        ) { (code, token) ->
            Lexer(CodeIterator(code)).next().let {
                it.type shouldBe token[0]
                it.value shouldBe token[1]
            }
        }
    }
    context("invalid signs tests") {
        withData(
            nameFn = { it },
            "@",
            "$",
            "'"
        ) { code ->
            shouldThrowExactly<UnrecognizedSignError> { Lexer(CodeIterator(code)).next() }
        }
    }
    context("sourceCode line and column tests") {
        withData(
            nameFn = { it.first },
            "\"" to (1 to 1),
            " \"" to (1 to 2),
            "\t\"" to (1 to 5),
            "\n\"" to (2 to 1),
            "\n  \"" to (2 to 3),
        ) { (code, place) ->
            try { Lexer(CodeIterator(code)).next() } catch (e: MissingSignError) { e.message shouldBe "expected a \" after token: \" found at ${place.first}:${place.second}" }
        }
    }
})
