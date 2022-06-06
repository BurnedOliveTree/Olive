package interpreter

import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import parser.*
import parser.Function

class InterpreterUnitTest: FunSpec({
    context("expressions tests") {
        withData(
            nameFn = { "Object \"$it\"" },
            StringConstant("sample") to "sample",
            BoolConstant(true) to true,
            IntConstant(1) to 1,
            FloatConstant(1.1) to 1.1,
            CastExpression(IntConstant(1), Double::class) to 1.0,
            RootExpression(IntConstant(125), IntConstant(3)) to 4.999999999999999,
            RootExpression(FloatConstant(125.0), FloatConstant(3.0)) to 4.999999999999999,
            ExponentExpression(IntConstant(5), IntConstant(3)) to 125,
            ExponentExpression(FloatConstant(5.0), FloatConstant(3.0)) to 125.0,
            InverseExpression(IntConstant(5)) to -5,
            InverseExpression(FloatConstant(5.0)) to -5.0,
            ModuloExpression(IntConstant(8), IntConstant(3)) to 2,
            ModuloExpression(FloatConstant(8.0), FloatConstant(3.0)) to 2.0,
            DivideExpression(IntConstant(8), IntConstant(3)) to 2,
            DivideExpression(FloatConstant(8.0), FloatConstant(3.0)) to 2.6666666666666665,
            MultiplyExpression(IntConstant(8), IntConstant(3)) to 24,
            MultiplyExpression(FloatConstant(7.0), FloatConstant(3.5)) to 24.5,
            SubtractExpression(IntConstant(8), IntConstant(3)) to 5,
            SubtractExpression(FloatConstant(1.0), FloatConstant(3.5)) to -2.5,
            AddExpression(IntConstant(8), IntConstant(3)) to 11,
            AddExpression(FloatConstant(1.0), FloatConstant(3.5)) to 4.5,
            GreaterOrEqualExpression(IntConstant(8), IntConstant(3)) to true,
            GreaterOrEqualExpression(FloatConstant(1.0), FloatConstant(3.5)) to false,
            GreaterThanExpression(IntConstant(8), IntConstant(3)) to true,
            GreaterThanExpression(FloatConstant(1.0), FloatConstant(3.5)) to false,
            LesserOrEqualExpression(IntConstant(8), IntConstant(3)) to false,
            LesserOrEqualExpression(FloatConstant(1.0), FloatConstant(3.5)) to true,
            LesserThanExpression(IntConstant(8), IntConstant(3)) to false,
            LesserThanExpression(FloatConstant(1.0), FloatConstant(3.5)) to true,
            TypeCheckExpression(IntConstant(1), Int::class) to true,
            TypeCheckExpression(IntConstant(1), Double::class) to false,
            NotExpression(BoolConstant(true)) to false,
            NormalComparisonExpression(IntConstant(1), IntConstant(1)) to true,
            NormalComparisonExpression(FloatConstant(1.0), FloatConstant(5.0)) to false,
            AndExpression(BoolConstant(true), BoolConstant(true)) to true,
            AndExpression(BoolConstant(true), BoolConstant(false)) to false,
            OrExpression(BoolConstant(true), BoolConstant(false)) to true,
            OrExpression(BoolConstant(false), BoolConstant(false)) to false,
        ) { expression ->
            val interpreter = Interpreter()
            interpreter.visit(expression.first)
            interpreter.environment.pop().value shouldBe expression.second
        }
    }

    context("statements tests") {
        withData(
            nameFn = { "Object \"$it\"" },
            listOf(VarDeclarationStatement("sample", String::class, StringConstant("sample"))) to "sample",
            listOf(VarDeclarationStatement("sample", Int::class, IntConstant(1))) to 1,
            listOf(
                VarDeclarationStatement("sample", Int::class, null),
                IfStatement(
                    BoolConstant(true),
                    listOf(NormalAssignmentStatement(VariableReference("sample"), IntConstant(2))),
                    listOf(NormalAssignmentStatement(VariableReference("sample"), IntConstant(3)))
                )
            ) to 2,
            listOf(
                VarDeclarationStatement("sample", Int::class, null),
                IfStatement(
                    BoolConstant(false),
                    listOf(NormalAssignmentStatement(VariableReference("sample"), IntConstant(2))),
                    listOf(NormalAssignmentStatement(VariableReference("sample"), IntConstant(3)))
                )
            ) to 3,
            listOf(
                VarDeclarationStatement("sample", Int::class, null),
                IfStatement(
                    BoolConstant(false),
                    listOf(NormalAssignmentStatement(VariableReference("sample"), IntConstant(2))),
                    listOf(NormalAssignmentStatement(VariableReference("sample"), IntConstant(3)))
                )
            ) to 3,
            listOf(
                VarDeclarationStatement("sample", Int::class, IntConstant(0)),
                WhileStatement(
                    LesserThanExpression(VariableReference("sample"), IntConstant(5)),
                    listOf(SumAssignmentStatement(VariableReference("sample"), IntConstant(2)))
                )
            ) to 6,
            listOf(
                VarDeclarationStatement("sample", Int::class, IntConstant(2)),
                NormalAssignmentStatement(VariableReference("sample"), IntConstant(6))
            ) to 6,
            listOf(
                VarDeclarationStatement("a", Int::class, IntConstant(2)),
                VarDeclarationStatement("b", Int::class, null),
                NormalAssignmentStatement(VariableReference("b"), VariableReference("a")),
                VarDeclarationStatement("sample", Boolean::class, ReferenceComparisonExpression(VariableReference("a"), (VariableReference("b"))))
            ) to false,
            listOf(
                VarDeclarationStatement("a", Int::class, IntConstant(2)),
                VarDeclarationStatement("b", Int::class, null),
                ReferenceAssignmentStatement(VariableReference("b"), VariableReference("a")),
                VarDeclarationStatement("sample", Boolean::class, ReferenceComparisonExpression(VariableReference("a"), (VariableReference("b"))))
            ) to true,
            listOf(
                VarDeclarationStatement("sample", Int::class, IntConstant(2)),
                SumAssignmentStatement(VariableReference("sample"), IntConstant(6))
            ) to 8,
            listOf(
                VarDeclarationStatement("sample", Int::class, IntConstant(2)),
                DifferenceAssignmentStatement(VariableReference("sample"), IntConstant(6))
            ) to -4,
            listOf(
                VarDeclarationStatement("sample", Int::class, IntConstant(2)),
                MultiplicationAssignmentStatement(VariableReference("sample"), IntConstant(6))
            ) to 12,
            listOf(
                VarDeclarationStatement("sample", Int::class, IntConstant(2)),
                DivisionAssignmentStatement(VariableReference("sample"), IntConstant(6))
            ) to 0,
            listOf(
                VarDeclarationStatement("sample", Int::class, IntConstant(2)),
                ModuloAssignmentStatement(VariableReference("sample"), IntConstant(6))
            ) to 2,
            listOf(
                VarDeclarationStatement("sample", Int::class, IntConstant(2)),
                ExponentAssignmentStatement(VariableReference("sample"), IntConstant(6))
            ) to 64,
            listOf(
                VarDeclarationStatement("sample", Int::class, IntConstant(2)),
                RootAssignmentStatement(VariableReference("sample"), IntConstant(6))
            ) to 1,
        ) { statement ->
            val interpreter = Interpreter()
            interpreter.environment.functionCall("main")
            statement.first.forEach { interpreter.visit(it) }
            interpreter.environment.variableValue("sample").value shouldBe statement.second
        }
    }

    context("unhappy path statement tests") {
        withData(
            nameFn = { "Object \"$it\"" },
            listOf(
                NormalAssignmentStatement(VariableReference("sample"), IntConstant(0))
            ) to MissingDeclarationException::class,
            listOf(
                VarDeclarationStatement("sample", String::class, null),
                VarDeclarationStatement("sample2", String::class, null),
                NormalAssignmentStatement(VariableReference("sample2"), VariableReference("sample"))
            ) to MissingDeclarationException::class,
            listOf(
                VarDeclarationStatement("sample", String::class, StringConstant("sample")),
                VarDeclarationStatement("sample2", Int::class, IntConstant(0)),
                NormalAssignmentStatement(VariableReference("sample2"), VariableReference("sample"))
            ) to TypeException::class,
            listOf(
                VarDeclarationStatement("sample", String::class, IntConstant(0))
            ) to TypeException::class,
            listOf(
                VarDeclarationStatement("sample", Int::class, IntConstant(2)),
                SumAssignmentStatement(VariableReference("sample"), FloatConstant(10.0))
            ) to TypeException::class,
            listOf(
                FunctionCallStatement(FunctionCallExpression("sample", listOf()))
            ) to MissingDeclarationException::class,
            listOf(
                VarDeclarationStatement("sample", Int::class, IntConstant(2)),
                VarDeclarationStatement("sample", Int::class, IntConstant(4))
            ) to ConflictingDeclarationException::class,
            listOf(
                VarDeclarationStatement("sample", Double::class, DivideExpression(FloatConstant(1.0), FloatConstant(0.0)))
            ) to IllegalMathematicalOperationException::class,
            listOf(
                VarDeclarationStatement("sample", Double::class, ExponentExpression(FloatConstant(0.0), FloatConstant(0.0)))
            ) to IllegalMathematicalOperationException::class,
            listOf(
                VarDeclarationStatement("sample", Double::class, RootExpression(FloatConstant(1.0), FloatConstant(0.0)))
            ) to IllegalMathematicalOperationException::class,
        ) { statement ->
            val interpreter = Interpreter()
            interpreter.setFunction(listOf(Function("main", Unit::class, listOf(), listOf())))
            interpreter.environment.functionCall("main")
            try {
                statement.first.forEach { interpreter.visit(it) }
            } catch (e: SemanticsError) {
                e::class shouldBe statement.second
            }
        }
    }

    context("unhappy path function definition tests") {
        withData(
            nameFn = { "Object \"$it\"" },
            listOf(
                Function("main", Int::class, listOf(), listOf()),
                Function("sample", Int::class, listOf(), listOf()),
                Function("sample", Int::class, listOf(), listOf())
            ) to ConflictingDeclarationException::class,
            listOf(
                Function("sample", Int::class, listOf(), listOf())
            ) to MissingDeclarationException::class,
        ) { statement ->
            val interpreter = Interpreter()
            try {
                interpreter.setFunction(statement.first)
            } catch (e: SemanticsError) {
                e::class shouldBe statement.second
            }
        }
    }
})