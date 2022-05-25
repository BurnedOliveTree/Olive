package parser

import kotlin.reflect.KClass

abstract class Visitor {
    // all `visit` should remain Unit type
    abstract fun visit(visitable: TypedIdentifier)
    abstract fun visit(visitable: OrExpression)
    abstract fun visit(visitable: AndExpression)
    abstract fun visit(visitable: NormalComparisonExpression)
    abstract fun visit(visitable: ReferenceComparisonExpression)
    abstract fun visit(visitable: NotExpression)
    abstract fun visit(visitable: TypeCheckExpression)
    abstract fun visit(visitable: LesserThanExpression)
    abstract fun visit(visitable: LesserOrEqualExpression)
    abstract fun visit(visitable: GreaterThanExpression)
    abstract fun visit(visitable: GreaterOrEqualExpression)
    abstract fun visit(visitable: AddExpression)
    abstract fun visit(visitable: SubtractExpression)
    abstract fun visit(visitable: MultiplyExpression)
    abstract fun visit(visitable: DivideExpression)
    abstract fun visit(visitable: ModuloExpression)
    abstract fun visit(visitable: InverseExpression)
    abstract fun visit(visitable: ExponentExpression)
    abstract fun visit(visitable: RootExpression)
    abstract fun visit(visitable: CastExpression)
    abstract fun visit(visitable: Variable)
    abstract fun visit(visitable: BoolConstant)
    abstract fun visit(visitable: FloatConstant)
    abstract fun visit(visitable: IntConstant)
    abstract fun visit(visitable: StringConstant)
    abstract fun visit(visitable: FunctionCallExpression)
    abstract fun visit(visitable: VarDeclarationStatement)
    abstract fun visit(visitable: IfStatement)
    abstract fun visit(visitable: WhileStatement)
    abstract fun visit(visitable: NormalAssignmentStatement)
    abstract fun visit(visitable: ReferenceAssignmentStatement)
    abstract fun visit(visitable: SumAssignmentStatement)
    abstract fun visit(visitable: DifferenceAssignmentStatement)
    abstract fun visit(visitable: MultiplicationAssignmentStatement)
    abstract fun visit(visitable: DivisionAssignmentStatement)
    abstract fun visit(visitable: ModuloAssignmentStatement)
    abstract fun visit(visitable: ExponentAssignmentStatement)
    abstract fun visit(visitable: RootAssignmentStatement)
    abstract fun visit(visitable: FunctionCallStatement)
    abstract fun visit(visitable: ReturnStatement)
    abstract fun visit(visitable: Function)
    abstract fun visit(visitable: Program)
}

class ProgramTreeFormatter: Visitor() {
    override fun visit(visitable: TypedIdentifier) {
        TODO("Not yet implemented")
    }

    override fun visit(visitable: OrExpression) {
        TODO("Not yet implemented")
    }

    override fun visit(visitable: AndExpression) {
        TODO("Not yet implemented")
    }

    override fun visit(visitable: NormalComparisonExpression) {
        TODO("Not yet implemented")
    }

    override fun visit(visitable: ReferenceComparisonExpression) {
        TODO("Not yet implemented")
    }

    override fun visit(visitable: NotExpression) {
        TODO("Not yet implemented")
    }

    override fun visit(visitable: TypeCheckExpression) {
        TODO("Not yet implemented")
    }

    override fun visit(visitable: LesserThanExpression) {
        TODO("Not yet implemented")
    }

    override fun visit(visitable: LesserOrEqualExpression) {
        TODO("Not yet implemented")
    }

    override fun visit(visitable: GreaterThanExpression) {
        TODO("Not yet implemented")
    }

    override fun visit(visitable: GreaterOrEqualExpression) {
        TODO("Not yet implemented")
    }

    override fun visit(visitable: AddExpression) {
        TODO("Not yet implemented")
    }

    override fun visit(visitable: SubtractExpression) {
        TODO("Not yet implemented")
    }

    override fun visit(visitable: MultiplyExpression) {
        TODO("Not yet implemented")
    }

    override fun visit(visitable: DivideExpression) {
        TODO("Not yet implemented")
    }

    override fun visit(visitable: ModuloExpression) {
        TODO("Not yet implemented")
    }

    override fun visit(visitable: InverseExpression) {
        TODO("Not yet implemented")
    }

    override fun visit(visitable: ExponentExpression) {
        TODO("Not yet implemented")
    }

    override fun visit(visitable: RootExpression) {
        TODO("Not yet implemented")
    }

    override fun visit(visitable: CastExpression) {
        TODO("Not yet implemented")
    }

    override fun visit(visitable: Variable) {
        TODO("Not yet implemented")
    }

    override fun visit(visitable: BoolConstant) {
        TODO("Not yet implemented")
    }

    override fun visit(visitable: FloatConstant) {
        TODO("Not yet implemented")
    }

    override fun visit(visitable: IntConstant) {
        TODO("Not yet implemented")
    }

    override fun visit(visitable: StringConstant) {
        TODO("Not yet implemented")
    }

    override fun visit(visitable: FunctionCallExpression) {
        TODO("Not yet implemented")
    }

    override fun visit(visitable: VarDeclarationStatement) {
        TODO("Not yet implemented")
    }

    override fun visit(visitable: IfStatement) {
        TODO("Not yet implemented")
    }

    override fun visit(visitable: WhileStatement) {
        TODO("Not yet implemented")
    }

    override fun visit(visitable: NormalAssignmentStatement) {
        TODO("Not yet implemented")
    }

    override fun visit(visitable: ReferenceAssignmentStatement) {
        TODO("Not yet implemented")
    }

    override fun visit(visitable: SumAssignmentStatement) {
        TODO("Not yet implemented")
    }

    override fun visit(visitable: DifferenceAssignmentStatement) {
        TODO("Not yet implemented")
    }

    override fun visit(visitable: MultiplicationAssignmentStatement) {
        TODO("Not yet implemented")
    }

    override fun visit(visitable: DivisionAssignmentStatement) {
        TODO("Not yet implemented")
    }

    override fun visit(visitable: ModuloAssignmentStatement) {
        TODO("Not yet implemented")
    }

    override fun visit(visitable: ExponentAssignmentStatement) {
        TODO("Not yet implemented")
    }

    override fun visit(visitable: RootAssignmentStatement) {
        TODO("Not yet implemented")
    }

    override fun visit(visitable: FunctionCallStatement) {
        TODO("Not yet implemented")
    }

    override fun visit(visitable: ReturnStatement) {
        TODO("Not yet implemented")
    }

    override fun visit(visitable: Function) {
        TODO("Not yet implemented")
    }

    override fun visit(visitable: Program) {
        TODO("Not yet implemented")
    }

}

abstract class Visitable {
    abstract fun accept(visitor: Visitor)
}

data class TypedIdentifier(val name: String, val type: KClass<out Any>): Visitable() {
    override fun accept(visitor: Visitor) { visitor.visit(this) }
}

abstract class Expression: Visitable()

data class OrExpression(val left: Expression, val right: Expression): Expression() {
    override fun accept(visitor: Visitor) { visitor.visit(this) }
}

data class AndExpression(val left: Expression, val right: Expression): Expression() {
    override fun accept(visitor: Visitor) { visitor.visit(this) }
}

data class NormalComparisonExpression(val left: Expression, val right: Expression): Expression() {
    override fun accept(visitor: Visitor) { visitor.visit(this) }
}
data class ReferenceComparisonExpression(val left: Expression, val right: Expression): Expression() {
    override fun accept(visitor: Visitor) { visitor.visit(this) }
}

data class NotExpression(val expression: Expression): Expression() {
    override fun accept(visitor: Visitor) { visitor.visit(this) }
}

data class TypeCheckExpression(val expression: Expression, val type: KClass<out Any>): Expression() {
    override fun accept(visitor: Visitor) { visitor.visit(this) }
}

data class LesserThanExpression(val left: Expression, val right: Expression): Expression() {
    override fun accept(visitor: Visitor) { visitor.visit(this) }
}
data class LesserOrEqualExpression(val left: Expression, val right: Expression): Expression() {
    override fun accept(visitor: Visitor) { visitor.visit(this) }
}
data class GreaterThanExpression(val left: Expression, val right: Expression): Expression() {
    override fun accept(visitor: Visitor) { visitor.visit(this) }
}
data class GreaterOrEqualExpression(val left: Expression, val right: Expression): Expression() {
    override fun accept(visitor: Visitor) { visitor.visit(this) }
}

data class AddExpression(val left: Expression, val right: Expression): Expression() {
    override fun accept(visitor: Visitor) { visitor.visit(this) }
}
data class SubtractExpression(val left: Expression, val right: Expression): Expression() {
    override fun accept(visitor: Visitor) { visitor.visit(this) }
}

data class MultiplyExpression(val left: Expression, val right: Expression): Expression() {
    override fun accept(visitor: Visitor) { visitor.visit(this) }
}
data class DivideExpression(val left: Expression, val right: Expression): Expression() {
    override fun accept(visitor: Visitor) { visitor.visit(this) }
}
data class ModuloExpression(val left: Expression, val right: Expression): Expression() {
    override fun accept(visitor: Visitor) { visitor.visit(this) }
}

data class InverseExpression(val expression: Expression): Expression() {
    override fun accept(visitor: Visitor) { visitor.visit(this) }
}

data class ExponentExpression(val left: Expression, val right: Expression): Expression() {
    override fun accept(visitor: Visitor) { visitor.visit(this) }
}
data class RootExpression(val left: Expression, val right: Expression): Expression() {
    override fun accept(visitor: Visitor) { visitor.visit(this) }
}

data class CastExpression(val expression: Expression, val type: KClass<out Any>): Expression() {
    override fun accept(visitor: Visitor) { visitor.visit(this) }
}

data class Variable(val name: String): Expression() {
    override fun accept(visitor: Visitor) { visitor.visit(this) }
}
data class BoolConstant(val value: Boolean): Expression() {
    override fun accept(visitor: Visitor) { visitor.visit(this) }
}
data class FloatConstant(val value: Double): Expression() {
    override fun accept(visitor: Visitor) { visitor.visit(this) }
}
data class IntConstant(val value: Int): Expression() {
    override fun accept(visitor: Visitor) { visitor.visit(this) }
}
data class StringConstant(val value: String): Expression() {
    override fun accept(visitor: Visitor) { visitor.visit(this) }
}

data class FunctionCallExpression(val name: String, val arguments: List<Expression>): Expression() {
    override fun accept(visitor: Visitor) { visitor.visit(this) }
}

abstract class Statement: Visitable()

data class VarDeclarationStatement(val name: String, val type: KClass<out Any>, val value: Expression?): Statement() {
    override fun accept(visitor: Visitor) { visitor.visit(this) }
}

data class IfStatement(val condition: Expression, val ifBlock: List<Statement>, val elseBlock: List<Statement>?): Statement() {
    override fun accept(visitor: Visitor) { visitor.visit(this) }
}

data class WhileStatement(val condition: Expression, val whileBlock: List<Statement>): Statement() {
    override fun accept(visitor: Visitor) { visitor.visit(this) }
}

data class NormalAssignmentStatement(val variable: Variable, val expression: Expression): Statement() {
    override fun accept(visitor: Visitor) { visitor.visit(this) }
}
data class ReferenceAssignmentStatement(val variable: Variable, val expression: Expression): Statement() {
    override fun accept(visitor: Visitor) { visitor.visit(this) }
}
data class SumAssignmentStatement(val variable: Variable, val expression: Expression): Statement() {
    override fun accept(visitor: Visitor) { visitor.visit(this) }
}
data class DifferenceAssignmentStatement(val variable: Variable, val expression: Expression): Statement() {
    override fun accept(visitor: Visitor) { visitor.visit(this) }
}
data class MultiplicationAssignmentStatement(val variable: Variable, val expression: Expression): Statement() {
    override fun accept(visitor: Visitor) { visitor.visit(this) }
}
data class DivisionAssignmentStatement(val variable: Variable, val expression: Expression): Statement() {
    override fun accept(visitor: Visitor) { visitor.visit(this) }
}
data class ModuloAssignmentStatement(val variable: Variable, val expression: Expression): Statement() {
    override fun accept(visitor: Visitor) { visitor.visit(this) }
}
data class ExponentAssignmentStatement(val variable: Variable, val expression: Expression): Statement() {
    override fun accept(visitor: Visitor) { visitor.visit(this) }
}
data class RootAssignmentStatement(val variable: Variable, val expression: Expression): Statement() {
    override fun accept(visitor: Visitor) { visitor.visit(this) }
}

data class FunctionCallStatement(val expression: FunctionCallExpression): Statement() {
    override fun accept(visitor: Visitor) { visitor.visit(this) }
}

data class ReturnStatement(val expression: Expression): Statement() {
    override fun accept(visitor: Visitor) { visitor.visit(this) }
}

data class Function(val name: String, val type: KClass<out Any>, val parameters: List<TypedIdentifier>, val block: List<Statement>): Visitable() {
    override fun accept(visitor: Visitor) { visitor.visit(this) }
}

data class Program(val funDeclarations: List<Function>, val exceptions: List<SyntaxError>): Visitable() {
    override fun accept(visitor: Visitor) { visitor.visit(this) }
}
