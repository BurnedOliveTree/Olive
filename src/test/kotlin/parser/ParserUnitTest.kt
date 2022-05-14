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
    // While '(' expression ')' block;
    context("while statement tests") {
        withData(
            nameFn = { "Sequence of $it" },
            listOf(
                TokenType.While.toToken(),
                TokenType.LeftParenthesesSign.toToken(),
                TokenType.BoolConstant.toToken(true),
                TokenType.RightParenthesesSign.toToken(),
                TokenType.LeftBraceSign.toToken(),
                TokenType.RightBraceSign.toToken()
            ).wrapInProgram() to listOf(
                WhileStatement(BoolConstant(true), listOf())
            ).wrapInProgram(),
            listOf(
                TokenType.While.toToken(),
                TokenType.LeftParenthesesSign.toToken(),
                TokenType.BoolConstant.toToken(true),
                TokenType.RightParenthesesSign.toToken(),
                TokenType.LeftBraceSign.toToken(),
                TokenType.Identifier.toToken("function"),
                TokenType.LeftParenthesesSign.toToken(),
                TokenType.RightParenthesesSign.toToken(),
                TokenType.EndSign.toToken(),
                TokenType.RightBraceSign.toToken()
            ).wrapInProgram() to listOf(
                WhileStatement(BoolConstant(true), listOf(FunctionCallStatement(FunctionCallExpression("function", listOf()))))
            ).wrapInProgram(),
        ) { iterable ->
            Parser(LexerTokenIterator(iterable.first)).parse() shouldBe iterable.second
        }
    }
    // Variable typedIdentifier (NormalAssignOp expression)? EndSign;
    context("variable definition statement tests") {
        withData(
            nameFn = { "Sequence of $it" },
            listOf(
                TokenType.Variable.toToken(),
                TokenType.Identifier.toToken("variable"),
                TokenType.TypeSign.toToken(),
                TokenType.IntType.toToken(),
                TokenType.EndSign.toToken()
            ).wrapInProgram() to listOf(
                VarDeclarationStatement("variable", Int, null)
            ).wrapInProgram(),
            listOf(
                TokenType.Variable.toToken(),
                TokenType.Identifier.toToken("variable"),
                TokenType.TypeSign.toToken(),
                TokenType.IntType.toToken(),
                TokenType.NormalAssignOp.toToken(),
                TokenType.IntConstant.toToken(1),
                TokenType.EndSign.toToken()
            ).wrapInProgram() to listOf(
                VarDeclarationStatement("variable", Int, IntConstant(1))
            ).wrapInProgram(),
        ) { iterable ->
            Parser(LexerTokenIterator(iterable.first)).parse() shouldBe iterable.second
        }
    }
    // Identifier (restOfFunCall | (AssignOp expression)) EndSign;
    context("identifier-started statement tests") {
        withData(
            nameFn = { "Sequence of $it" },
            listOf(
                TokenType.Identifier.toToken("function"),
                TokenType.LeftParenthesesSign.toToken(),
                TokenType.RightParenthesesSign.toToken(),
                TokenType.EndSign.toToken()
            ).wrapInProgram() to listOf(
                FunctionCallStatement(FunctionCallExpression("function", listOf()))
            ).wrapInProgram(),
            listOf(
                TokenType.Identifier.toToken("variable"),
                TokenType.SumAssignOp.toToken(),
                TokenType.IntConstant.toToken(0),
                TokenType.EndSign.toToken()
            ).wrapInProgram() to listOf(
                SumAssignmentStatement(Variable("variable"), IntConstant(0))
            ).wrapInProgram(),
            listOf(
                TokenType.Identifier.toToken("variable"),
                TokenType.DifferenceAssignOp.toToken(),
                TokenType.IntConstant.toToken(1),
                TokenType.EndSign.toToken()
            ).wrapInProgram() to listOf(
                DifferenceAssignmentStatement(Variable("variable"), IntConstant(1))
            ).wrapInProgram(),
            listOf(
                TokenType.Identifier.toToken("variable"),
                TokenType.MultiplicationAssignOp.toToken(),
                TokenType.IntConstant.toToken(2),
                TokenType.EndSign.toToken()
            ).wrapInProgram() to listOf(
                MultiplicationAssignmentStatement(Variable("variable"), IntConstant(2))
            ).wrapInProgram(),
            listOf(
                TokenType.Identifier.toToken("variable"),
                TokenType.DivisionAssignOp.toToken(),
                TokenType.IntConstant.toToken(3),
                TokenType.EndSign.toToken()
            ).wrapInProgram() to listOf(
                DivisionAssignmentStatement(Variable("variable"), IntConstant(3))
            ).wrapInProgram(),
            listOf(
                TokenType.Identifier.toToken("variable"),
                TokenType.ModuloAssignOp.toToken(),
                TokenType.IntConstant.toToken(4),
                TokenType.EndSign.toToken()
            ).wrapInProgram() to listOf(
                ModuloAssignmentStatement(Variable("variable"), IntConstant(4))
            ).wrapInProgram(),
            listOf(
                TokenType.Identifier.toToken("variable"),
                TokenType.ExponentAssignOp.toToken(),
                TokenType.IntConstant.toToken(5),
                TokenType.EndSign.toToken()
            ).wrapInProgram() to listOf(
                ExponentAssignmentStatement(Variable("variable"), IntConstant(5))
            ).wrapInProgram(),
            listOf(
                TokenType.Identifier.toToken("variable"),
                TokenType.RootAssignOp.toToken(),
                TokenType.IntConstant.toToken(6),
                TokenType.EndSign.toToken()
            ).wrapInProgram() to listOf(
                RootAssignmentStatement(Variable("variable"), IntConstant(6))
            ).wrapInProgram(),
            listOf(
                TokenType.Identifier.toToken("variable"),
                TokenType.NormalAssignOp.toToken(),
                TokenType.IntConstant.toToken(7),
                TokenType.EndSign.toToken()
            ).wrapInProgram() to listOf(
                NormalAssignmentStatement(Variable("variable"), IntConstant(7))
            ).wrapInProgram(),
            listOf(
                TokenType.Identifier.toToken("variable"),
                TokenType.ReferenceAssignOp.toToken(),
                TokenType.IntConstant.toToken(8),
                TokenType.EndSign.toToken()
            ).wrapInProgram() to listOf(
                ReferenceAssignmentStatement(Variable("variable"), IntConstant(8))
            ).wrapInProgram(),
        ) { iterable ->
            Parser(LexerTokenIterator(iterable.first)).parse() shouldBe iterable.second
        }
    }
    // Return expression EndSign;
    context("return statement tests") {
        withData(
            nameFn = { "Sequence of $it" },
            listOf(
                TokenType.Return.toToken(),
                TokenType.IntConstant.toToken(0),
                TokenType.EndSign.toToken()
            ).wrapInProgram() to listOf(
                ReturnStatement(IntConstant(0))
            ).wrapInProgram(),
            listOf(
                TokenType.Return.toToken(),
                TokenType.IntConstant.toToken(5),
                TokenType.SumOp.toToken(),
                TokenType.IntConstant.toToken(3),
                TokenType.EndSign.toToken()
            ).wrapInProgram() to listOf(
                ReturnStatement(AddExpression(IntConstant(5), IntConstant(3)))
            ).wrapInProgram()
        ) { iterable ->
            Parser(LexerTokenIterator(iterable.first)).parse() shouldBe iterable.second
        }
    }
    // TODO more tests
})
