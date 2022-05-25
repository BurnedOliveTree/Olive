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
    Program(listOf(Function("main", Unit::class, listOf(), this)), listOf())

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
                    Unit::class,
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
                    Unit::class,
                    listOf(
                        TypedIdentifier("number", Number::class)
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
                    Unit::class,
                    listOf(
                        TypedIdentifier("number", Number::class),
                        TypedIdentifier("string", String::class)
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
                VarDeclarationStatement("variable", Int::class, null)
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
                VarDeclarationStatement("variable", Int::class, IntConstant(1))
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
    // andExpression (OrOp andExpression)*;
    context("or expression tests") {
        withData(
            nameFn = { "Sequence of $it" },
            listOf(
                TokenType.Return.toToken(),
                TokenType.BoolConstant.toToken(true),
                TokenType.EndSign.toToken()
            ).wrapInProgram() to listOf(
                ReturnStatement(BoolConstant(true))
            ).wrapInProgram(),
            listOf(
                TokenType.Return.toToken(),
                TokenType.BoolConstant.toToken(true),
                TokenType.OrOp.toToken(),
                TokenType.BoolConstant.toToken(false),
                TokenType.EndSign.toToken()
            ).wrapInProgram() to listOf(
                ReturnStatement(OrExpression(BoolConstant(true), BoolConstant(false)))
            ).wrapInProgram(),
            listOf(
                TokenType.Return.toToken(),
                TokenType.BoolConstant.toToken(true),
                TokenType.OrOp.toToken(),
                TokenType.BoolConstant.toToken(false),
                TokenType.OrOp.toToken(),
                TokenType.BoolConstant.toToken(false),
                TokenType.EndSign.toToken()
            ).wrapInProgram() to listOf(
                ReturnStatement(OrExpression(OrExpression(BoolConstant(true), BoolConstant(false)), BoolConstant(false)))
            ).wrapInProgram()
        ) { iterable ->
            Parser(LexerTokenIterator(iterable.first)).parse() shouldBe iterable.second
        }
    }
    // equalExpression (AndOp equalExpression)*;
    context("and expression tests") {
        withData(
            nameFn = { "Sequence of $it" },
            listOf(
                TokenType.Return.toToken(),
                TokenType.BoolConstant.toToken(true),
                TokenType.EndSign.toToken()
            ).wrapInProgram() to listOf(
                ReturnStatement(BoolConstant(true))
            ).wrapInProgram(),
            listOf(
                TokenType.Return.toToken(),
                TokenType.BoolConstant.toToken(true),
                TokenType.AndOp.toToken(),
                TokenType.BoolConstant.toToken(false),
                TokenType.EndSign.toToken()
            ).wrapInProgram() to listOf(
                ReturnStatement(AndExpression(BoolConstant(true), BoolConstant(false)))
            ).wrapInProgram(),
            listOf(
                TokenType.Return.toToken(),
                TokenType.BoolConstant.toToken(true),
                TokenType.AndOp.toToken(),
                TokenType.BoolConstant.toToken(false),
                TokenType.AndOp.toToken(),
                TokenType.BoolConstant.toToken(false),
                TokenType.EndSign.toToken()
            ).wrapInProgram() to listOf(
                ReturnStatement(AndExpression(AndExpression(BoolConstant(true), BoolConstant(false)), BoolConstant(false)))
            ).wrapInProgram()
        ) { iterable ->
            Parser(LexerTokenIterator(iterable.first)).parse() shouldBe iterable.second
        }
    }
    // notExpression ((NormalComparOp | ReferenceComparOp) notExpression)?;
    context("equal expression tests") {
        withData(
            nameFn = { "Sequence of $it" },
            listOf(
                TokenType.Return.toToken(),
                TokenType.BoolConstant.toToken(true),
                TokenType.EndSign.toToken()
            ).wrapInProgram() to listOf(
                ReturnStatement(BoolConstant(true))
            ).wrapInProgram(),
            listOf(
                TokenType.Return.toToken(),
                TokenType.BoolConstant.toToken(true),
                TokenType.NormalComparisonOp.toToken(),
                TokenType.BoolConstant.toToken(false),
                TokenType.EndSign.toToken()
            ).wrapInProgram() to listOf(
                ReturnStatement(NormalComparisonExpression(BoolConstant(true), BoolConstant(false)))
            ).wrapInProgram(),
            listOf(
                TokenType.Return.toToken(),
                TokenType.BoolConstant.toToken(true),
                TokenType.ReferenceComparisonOp.toToken(),
                TokenType.BoolConstant.toToken(false),
                TokenType.EndSign.toToken()
            ).wrapInProgram() to listOf(
                ReturnStatement(ReferenceComparisonExpression(BoolConstant(true), BoolConstant(false)))
            ).wrapInProgram(),
        ) { iterable ->
            Parser(LexerTokenIterator(iterable.first)).parse() shouldBe iterable.second
        }
    }
    // NotOp? typeCheckExpression;
    context("not expression tests") {
        withData(
            nameFn = { "Sequence of $it" },
            listOf(
                TokenType.Return.toToken(),
                TokenType.BoolConstant.toToken(true),
                TokenType.EndSign.toToken()
            ).wrapInProgram() to listOf(
                ReturnStatement(BoolConstant(true))
            ).wrapInProgram(),
            listOf(
                TokenType.Return.toToken(),
                TokenType.NotOp.toToken(),
                TokenType.BoolConstant.toToken(true),
                TokenType.EndSign.toToken()
            ).wrapInProgram() to listOf(
                ReturnStatement(NotExpression(BoolConstant(true)))
            ).wrapInProgram()
        ) { iterable ->
            Parser(LexerTokenIterator(iterable.first)).parse() shouldBe iterable.second
        }
    }
    // compareExpression (IsOp Type)?;
    context("type check expression tests") {
        withData(
            nameFn = { "Sequence of $it" },
            listOf(
                TokenType.Return.toToken(),
                TokenType.BoolConstant.toToken(true),
                TokenType.EndSign.toToken()
            ).wrapInProgram() to listOf(
                ReturnStatement(BoolConstant(true))
            ).wrapInProgram(),
            listOf(
                TokenType.Return.toToken(),
                TokenType.BoolConstant.toToken(true),
                TokenType.IsOp.toToken(),
                TokenType.BoolType.toToken(),
                TokenType.EndSign.toToken()
            ).wrapInProgram() to listOf(
                ReturnStatement(TypeCheckExpression(BoolConstant(true), Boolean::class))
            ).wrapInProgram(),
        ) { iterable ->
            Parser(LexerTokenIterator(iterable.first)).parse() shouldBe iterable.second
        }
    }
    // addExpression ((LesserThanOp | LesserOrEqualOp | GreaterThanOp | GreaterOrEqualOp) addExpression)?;
    context("comparison expression tests") {
        withData(
            nameFn = { "Sequence of $it" },
            listOf(
                TokenType.Return.toToken(),
                TokenType.IntConstant.toToken(1),
                TokenType.EndSign.toToken()
            ).wrapInProgram() to listOf(
                ReturnStatement(IntConstant(1))
            ).wrapInProgram(),
            listOf(
                TokenType.Return.toToken(),
                TokenType.IntConstant.toToken(1),
                TokenType.LesserThanOp.toToken(),
                TokenType.IntConstant.toToken(2),
                TokenType.EndSign.toToken()
            ).wrapInProgram() to listOf(
                ReturnStatement(LesserThanExpression(IntConstant(1), IntConstant(2)))
            ).wrapInProgram(),
            listOf(
                TokenType.Return.toToken(),
                TokenType.IntConstant.toToken(1),
                TokenType.LesserOrEqualOp.toToken(),
                TokenType.IntConstant.toToken(2),
                TokenType.EndSign.toToken()
            ).wrapInProgram() to listOf(
                ReturnStatement(LesserOrEqualExpression(IntConstant(1), IntConstant(2)))
            ).wrapInProgram(),
            listOf(
                TokenType.Return.toToken(),
                TokenType.IntConstant.toToken(1),
                TokenType.GreaterThanOp.toToken(),
                TokenType.IntConstant.toToken(2),
                TokenType.EndSign.toToken()
            ).wrapInProgram() to listOf(
                ReturnStatement(GreaterThanExpression(IntConstant(1), IntConstant(2)))
            ).wrapInProgram(),
            listOf(
                TokenType.Return.toToken(),
                TokenType.IntConstant.toToken(1),
                TokenType.GreaterOrEqualOp.toToken(),
                TokenType.IntConstant.toToken(2),
                TokenType.EndSign.toToken()
            ).wrapInProgram() to listOf(
                ReturnStatement(GreaterOrEqualExpression(IntConstant(1), IntConstant(2)))
            ).wrapInProgram(),
        ) { iterable ->
            Parser(LexerTokenIterator(iterable.first)).parse() shouldBe iterable.second
        }
    }
    // multiplyExpression ((SumOp | DifferenceOp) multiplyExpression)*;
    context("add expression tests") {
        withData(
            nameFn = { "Sequence of $it" },
            listOf(
                TokenType.Return.toToken(),
                TokenType.IntConstant.toToken(1),
                TokenType.EndSign.toToken()
            ).wrapInProgram() to listOf(
                ReturnStatement(IntConstant(1))
            ).wrapInProgram(),
            listOf(
                TokenType.Return.toToken(),
                TokenType.IntConstant.toToken(1),
                TokenType.SumOp.toToken(),
                TokenType.IntConstant.toToken(2),
                TokenType.EndSign.toToken()
            ).wrapInProgram() to listOf(
                ReturnStatement(AddExpression(IntConstant(1), IntConstant(2)))
            ).wrapInProgram(),
            listOf(
                TokenType.Return.toToken(),
                TokenType.IntConstant.toToken(1),
                TokenType.DifferenceOp.toToken(),
                TokenType.IntConstant.toToken(2),
                TokenType.SumOp.toToken(),
                TokenType.IntConstant.toToken(3),
                TokenType.EndSign.toToken()
            ).wrapInProgram() to listOf(
                ReturnStatement(AddExpression(SubtractExpression(IntConstant(1), IntConstant(2)), IntConstant(3)))
            ).wrapInProgram(),
        ) { iterable ->
            Parser(LexerTokenIterator(iterable.first)).parse() shouldBe iterable.second
        }
    }
    // inverseExpression ((MultiplicationOp | DivisionOp | ModuloOp) inverseExpression)*
    context("multiply expression tests") {
        withData(
            nameFn = { "Sequence of $it" },
            listOf(
                TokenType.Return.toToken(),
                TokenType.IntConstant.toToken(1),
                TokenType.EndSign.toToken()
            ).wrapInProgram() to listOf(
                ReturnStatement(IntConstant(1))
            ).wrapInProgram(),
            listOf(
                TokenType.Return.toToken(),
                TokenType.IntConstant.toToken(1),
                TokenType.MultiplicationOp.toToken(),
                TokenType.IntConstant.toToken(2),
                TokenType.EndSign.toToken()
            ).wrapInProgram() to listOf(
                ReturnStatement(MultiplyExpression(IntConstant(1), IntConstant(2)))
            ).wrapInProgram(),
            listOf(
                TokenType.Return.toToken(),
                TokenType.IntConstant.toToken(1),
                TokenType.DivisionOp.toToken(),
                TokenType.IntConstant.toToken(2),
                TokenType.MultiplicationOp.toToken(),
                TokenType.IntConstant.toToken(3),
                TokenType.EndSign.toToken()
            ).wrapInProgram() to listOf(
                ReturnStatement(MultiplyExpression(DivideExpression(IntConstant(1), IntConstant(2)), IntConstant(3)))
            ).wrapInProgram(),
            listOf(
                TokenType.Return.toToken(),
                TokenType.IntConstant.toToken(1),
                TokenType.DivisionOp.toToken(),
                TokenType.IntConstant.toToken(2),
                TokenType.ModuloOp.toToken(),
                TokenType.IntConstant.toToken(3),
                TokenType.MultiplicationOp.toToken(),
                TokenType.IntConstant.toToken(4),
                TokenType.EndSign.toToken()
            ).wrapInProgram() to listOf(
                ReturnStatement(MultiplyExpression(ModuloExpression(DivideExpression(IntConstant(1), IntConstant(2)), IntConstant(3)), IntConstant(4)))
            ).wrapInProgram(),
        ) { iterable ->
            Parser(LexerTokenIterator(iterable.first)).parse() shouldBe iterable.second
        }
    }
    // DifferenceOp? exponentExpression;
    context("inversion expression tests") {
        withData(
            nameFn = { "Sequence of $it" },
            listOf(
                TokenType.Return.toToken(),
                TokenType.IntConstant.toToken(1),
                TokenType.EndSign.toToken()
            ).wrapInProgram() to listOf(
                ReturnStatement(IntConstant(1))
            ).wrapInProgram(),
            listOf(
                TokenType.Return.toToken(),
                TokenType.DifferenceOp.toToken(),
                TokenType.IntConstant.toToken(1),
                TokenType.EndSign.toToken()
            ).wrapInProgram() to listOf(
                ReturnStatement(InverseExpression(IntConstant(1)))
            ).wrapInProgram(),
        ) { iterable ->
            Parser(LexerTokenIterator(iterable.first)).parse() shouldBe iterable.second
        }
    }
    // castExpression ((ExponentOp | RootOp) exponentExpression)*;
    context("exponent expression tests") {
        withData(
            nameFn = { "Sequence of $it" },
            listOf(
                TokenType.Return.toToken(),
                TokenType.IntConstant.toToken(1),
                TokenType.EndSign.toToken()
            ).wrapInProgram() to listOf(
                ReturnStatement(IntConstant(1))
            ).wrapInProgram(),
            listOf(
                TokenType.Return.toToken(),
                TokenType.IntConstant.toToken(1),
                TokenType.ExponentOp.toToken(),
                TokenType.IntConstant.toToken(2),
                TokenType.EndSign.toToken()
            ).wrapInProgram() to listOf(
                ReturnStatement(ExponentExpression(IntConstant(1), IntConstant(2)))
            ).wrapInProgram(),
            listOf(
                TokenType.Return.toToken(),
                TokenType.IntConstant.toToken(1),
                TokenType.RootOp.toToken(),
                TokenType.IntConstant.toToken(2),
                TokenType.ExponentOp.toToken(),
                TokenType.IntConstant.toToken(3),
                TokenType.EndSign.toToken()
            ).wrapInProgram() to listOf(
                ReturnStatement(RootExpression(IntConstant(1), ExponentExpression(IntConstant(2), IntConstant(3))))
            ).wrapInProgram(),
        ) { iterable ->
            Parser(LexerTokenIterator(iterable.first)).parse() shouldBe iterable.second
        }
    }
    // expressionPiece (CastOp Type)?;
    context("cast expression tests") {
        withData(
            nameFn = { "Sequence of $it" },
            listOf(
                TokenType.Return.toToken(),
                TokenType.IntConstant.toToken(1),
                TokenType.EndSign.toToken()
            ).wrapInProgram() to listOf(
                ReturnStatement(IntConstant(1))
            ).wrapInProgram(),
            listOf(
                TokenType.Return.toToken(),
                TokenType.IntConstant.toToken(1),
                TokenType.CastOp.toToken(),
                TokenType.FloatType.toToken(),
                TokenType.EndSign.toToken()
            ).wrapInProgram() to listOf(
                ReturnStatement(CastExpression(IntConstant(1), Double::class))
            ).wrapInProgram(),
        ) { iterable ->
            Parser(LexerTokenIterator(iterable.first)).parse() shouldBe iterable.second
        }
    }
    // (Identifier restOfFunCall?) | Constant | ('(' expression ')');
    context("expression piece tests") {
        withData(
            nameFn = { "Sequence of $it" },
            listOf(
                TokenType.Return.toToken(),
                TokenType.BoolConstant.toToken(false),
                TokenType.EndSign.toToken()
            ).wrapInProgram() to listOf(
                ReturnStatement(BoolConstant(false))
            ).wrapInProgram(),
            listOf(
                TokenType.Return.toToken(),
                TokenType.IntConstant.toToken(1),
                TokenType.EndSign.toToken()
            ).wrapInProgram() to listOf(
                ReturnStatement(IntConstant(1))
            ).wrapInProgram(),
            listOf(
                TokenType.Return.toToken(),
                TokenType.FloatConstant.toToken(1.0),
                TokenType.EndSign.toToken()
            ).wrapInProgram() to listOf(
                ReturnStatement(FloatConstant(1.0))
            ).wrapInProgram(),
            listOf(
                TokenType.Return.toToken(),
                TokenType.StringConstant.toToken("const"),
                TokenType.EndSign.toToken()
            ).wrapInProgram() to listOf(
                ReturnStatement(StringConstant("const"))
            ).wrapInProgram(),
            listOf(
                TokenType.Return.toToken(),
                TokenType.LeftParenthesesSign.toToken(),
                TokenType.StringConstant.toToken("const"),
                TokenType.RightParenthesesSign.toToken(),
                TokenType.EndSign.toToken()
            ).wrapInProgram() to listOf(
                ReturnStatement(StringConstant("const"))
            ).wrapInProgram(),
            listOf(
                TokenType.Return.toToken(),
                TokenType.LeftParenthesesSign.toToken(),
                TokenType.IntConstant.toToken(1),
                TokenType.SumOp.toToken(),
                TokenType.IntConstant.toToken(2),
                TokenType.RightParenthesesSign.toToken(),
                TokenType.MultiplicationOp.toToken(),
                TokenType.IntConstant.toToken(3),
                TokenType.EndSign.toToken()
            ).wrapInProgram() to listOf(
                ReturnStatement(MultiplyExpression(AddExpression(IntConstant(1), IntConstant(2)), IntConstant(3)))
            ).wrapInProgram(),
        ) { iterable ->
            Parser(LexerTokenIterator(iterable.first)).parse() shouldBe iterable.second
        }
    }
})
