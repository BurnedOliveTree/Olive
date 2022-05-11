package parser

abstract class Visitable

sealed class Number {
    class Int(val value: Int) : Number()
    class Float(val value: Float) : Number()

    companion object
}

data class TypedIdentifier(val name: String, val type: Any): Visitable()

abstract class Expression: Visitable()

data class OrExpression(val left: Expression, val right: Expression): Expression()

data class AndExpression(val left: Expression, val right: Expression): Expression()

data class NormalComparisonExpression(val left: Expression, val right: Expression): Expression()
data class ReferenceComparisonExpression(val left: Expression, val right: Expression): Expression()

data class NotExpression(val expression: Expression): Expression()

data class TypeCheckExpression(val expression: Expression, val type: Any): Expression()

data class LesserThanExpression(val left: Expression, val right: Expression): Expression()
data class LesserOrEqualExpression(val left: Expression, val right: Expression): Expression()
data class GreaterThanExpression(val left: Expression, val right: Expression): Expression()
data class GreaterOrEqualExpression(val left: Expression, val right: Expression): Expression()

data class AddExpression(val left: Expression, val right: Expression): Expression()
data class SubtractExpression(val left: Expression, val right: Expression): Expression()

data class MultiplyExpression(val left: Expression, val right: Expression): Expression()
data class DivideExpression(val left: Expression, val right: Expression): Expression()
data class ModuloExpression(val left: Expression, val right: Expression): Expression()

data class InverseExpression(val expression: Expression): Expression()

data class ExponentExpression(val left: Expression, val right: Expression): Expression()
data class RootExpression(val left: Expression, val right: Expression): Expression()

data class CastExpression(val expression: Expression, val type: Any): Expression()

data class Variable(val name: String): Expression()
data class BoolConstant(val value: Boolean): Expression()
data class FloatConstant(val value: Float): Expression()
data class IntConstant(val value: Int): Expression()
data class StringConstant(val value: String): Expression()

data class FunctionCallExpression(val name: String, val arguments: Array<Expression>): Expression()

abstract class Statement: Visitable()

data class VarDeclarationStatement(val name: String, val type: Any, val value: Expression?): Statement()

data class IfStatement(val condition: Expression, val ifBlock: Array<Statement>, val elseBlock: Array<Statement>?): Statement()

data class WhileStatement(val condition: Expression, val ifBlock: Array<Statement>): Statement()

data class AssignmentStatement(val variable: String, val expression: Expression): Statement()

data class FunctionCallStatement(val expression: FunctionCallExpression): Statement()

data class ReturnStatement(val expression: Expression): Statement()

data class Function(val name: String, val type: Any, val parameters: Array<TypedIdentifier>, val block: Array<Statement>): Visitable()

data class Program(val funDeclarations: Array<Function>): Visitable()
