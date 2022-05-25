package interpreter

import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import lexer.CodeIterator
import lexer.Lexer
import parser.*

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
            // TODO ReferenceComparison
            NormalComparisonExpression(IntConstant(1), IntConstant(1)) to true,
            NormalComparisonExpression(FloatConstant(1.0), FloatConstant(5.0)) to false,
            AndExpression(BoolConstant(true), BoolConstant(true)) to true,
            AndExpression(BoolConstant(true), BoolConstant(false)) to false,
            OrExpression(BoolConstant(true), BoolConstant(false)) to true,
            OrExpression(BoolConstant(false), BoolConstant(false)) to false,
        ) { expression ->
            val interpreter = Interpreter()
            interpreter.visit(expression.first)
            interpreter.value().value shouldBe expression.second
        }
    }
})