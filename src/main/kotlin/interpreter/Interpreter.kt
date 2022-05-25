package interpreter

import parser.*
import parser.Function
import kotlin.math.pow

class Interpreter: Visitor() {
    private lateinit var functions: Map<String, Function>
    private val environment = Environment()

    // TO-CHECK if (false and average()) -> average should not be called
    // TO-CHECK optimization?
    // TO-CHECK fun(n, inc(n), n) -> how to do it, will the first and third n have the same value, or incremented?
    // TO-CHECK arguments should be evaluated upon function call, not declaration

    internal fun value(): TypedValue {
        return environment.pop()
    }

    internal fun setFunction(functions: List<Function>) {
        this.functions = functions.associateBy { it.name }
    }

    override fun visit(visitable: Program) {
        functions = visitable.funDeclarations.associateBy { it.name }
        visit(functions["main"] ?: throw MissingDeclarationException("given program", "main"))
    }

    override fun visit(visitable: Function) {
        val parameters = visitable.parameters.map { Triple(it.name, it.type, environment.pop()) }
        environment.functionCall(visitable.name)
        parameters.forEach { environment.variableDeclare(it.first, it.second, it.third) }
        for (statement in visitable.block) {
            visit(statement)
            if (environment.isReturn())
                break
        }
        val returnValue = if (visitable.type != Unit::class) environment.pop() else null
        environment.functionLeave()
        environment.push(returnValue ?: Unit::class.toTypedNull())
        println(returnValue?.value)
    }

    override fun visit(visitable: VarDeclarationStatement) {
        val value = visitable.value?.let {
            visit(it)
            environment.pop()
        }
        environment.variableDeclare(visitable.name, visitable.type, value)
    }

    override fun visit(visitable: IfStatement) {
        visit(visitable.condition)
        if ((environment.pop() as TypedValue.Bool).value!!) {
            environment.blockEnter()
            for (statement in visitable.ifBlock) {
                visit(statement)
                if (environment.isReturn())
                    break
            }
            environment.blockLeave()
        } else {
            if (visitable.elseBlock != null) {
                environment.blockEnter()
                for (statement in visitable.elseBlock) {
                    visit(statement)
                    if (environment.isReturn())
                        break
                }
                environment.blockLeave()
            }
        }
    }

    override fun visit(visitable: WhileStatement) {
        environment.blockEnter()
        visit(visitable.condition)
        while ((environment.pop() as TypedValue.Bool).value!! && !environment.isReturn()) {
            for (statement in visitable.whileBlock) {
                visit(statement)
                if (environment.isReturn())
                    break
            }
            visit(visitable.condition)
        }
        environment.blockLeave()
    }

    override fun visit(visitable: NormalAssignmentStatement) {
        visit(visitable.expression)
        environment.variableAssign(visitable.variable.name, environment.pop())
    }

    override fun visit(visitable: ReferenceAssignmentStatement) {
        visit(visitable.expression)
        environment.variableAssign(visitable.variable.name, environment.pop()) // TODO
    }

    override fun visit(visitable: SumAssignmentStatement) {
        visit(visitable.expression)
        val right = environment.pop()
        val left = environment.variableValue(visitable.variable.name)
        if (left is TypedValue.Int && right is TypedValue.Int)
            environment.variableAssign(visitable.variable.name, TypedValue.Int(left.value!! + right.value!!))
        else if (left is TypedValue.Float && right is TypedValue.Float)
            environment.variableAssign(visitable.variable.name, TypedValue.Float(left.value!! + right.value!!))
        else if (left is TypedValue.Int || left is TypedValue.Float)
            throw TypeException(environment.functionName(), left, right)
        else
            throw TypeException(environment.functionName(), Int::class.toTypedNull(), left)
    }

    override fun visit(visitable: DifferenceAssignmentStatement) {
        visit(visitable.expression)
        val right = environment.pop()
        val left = environment.variableValue(visitable.variable.name)
        if (left is TypedValue.Int && right is TypedValue.Int)
            environment.variableAssign(visitable.variable.name, TypedValue.Int(left.value!! - right.value!!))
        else if (left is TypedValue.Float && right is TypedValue.Float)
            environment.variableAssign(visitable.variable.name, TypedValue.Float(left.value!! - right.value!!))
        else if (left is TypedValue.Int || left is TypedValue.Float)
            throw TypeException(environment.functionName(), left, right)
        else
            throw TypeException(environment.functionName(), Int::class.toTypedNull(), left)
    }

    override fun visit(visitable: MultiplicationAssignmentStatement) {
        visit(visitable.expression)
        val right = environment.pop()
        val left = environment.variableValue(visitable.variable.name)
        if (left is TypedValue.Int && right is TypedValue.Int)
            environment.variableAssign(visitable.variable.name, TypedValue.Int(left.value!! * right.value!!))
        else if (left is TypedValue.Float && right is TypedValue.Float)
            environment.variableAssign(visitable.variable.name, TypedValue.Float(left.value!! * right.value!!))
        else if (left is TypedValue.Int || left is TypedValue.Float)
            throw TypeException(environment.functionName(), left, right)
        else
            throw TypeException(environment.functionName(), Int::class.toTypedNull(), left)
    }

    override fun visit(visitable: DivisionAssignmentStatement) {
        visit(visitable.expression)
        val right = environment.pop()
        val left = environment.variableValue(visitable.variable.name)
        if (left is TypedValue.Int && right is TypedValue.Int)
            environment.variableAssign(visitable.variable.name, TypedValue.Int(left.value!! / right.value!!))
        else if (left is TypedValue.Float && right is TypedValue.Float)
            environment.variableAssign(visitable.variable.name, TypedValue.Float(left.value!! / right.value!!))
        else if (left is TypedValue.Int || left is TypedValue.Float)
            throw TypeException(environment.functionName(), left, right)
        else
            throw TypeException(environment.functionName(), Int::class.toTypedNull(), left)
    }

    override fun visit(visitable: ModuloAssignmentStatement) {
        visit(visitable.expression)
        val right = environment.pop()
        val left = environment.variableValue(visitable.variable.name)
        if (left is TypedValue.Int && right is TypedValue.Int)
            environment.variableAssign(visitable.variable.name, TypedValue.Int(left.value!! % right.value!!))
        else if (left is TypedValue.Float && right is TypedValue.Float)
            environment.variableAssign(visitable.variable.name, TypedValue.Float(left.value!! % right.value!!))
        else if (left is TypedValue.Int || left is TypedValue.Float)
            throw TypeException(environment.functionName(), left, right)
        else
            throw TypeException(environment.functionName(), Int::class.toTypedNull(), left)
    }

    override fun visit(visitable: ExponentAssignmentStatement) {
        visit(visitable.expression)
        val right = environment.pop()
        val left = environment.variableValue(visitable.variable.name)
        if (left is TypedValue.Int && right is TypedValue.Int)
            environment.variableAssign(visitable.variable.name, TypedValue.Int(left.value!!.toDouble().pow(right.value!!).toInt()))
        else if (left is TypedValue.Float && right is TypedValue.Float)
            environment.variableAssign(visitable.variable.name, TypedValue.Float(left.value!!.pow(right.value!!)))
        else if (left is TypedValue.Int || left is TypedValue.Float)
            throw TypeException(environment.functionName(), left, right)
        else
            throw TypeException(environment.functionName(), Int::class.toTypedNull(), left)
    }

    override fun visit(visitable: RootAssignmentStatement) {
        visit(visitable.expression)
        val right = environment.pop()
        val left = environment.variableValue(visitable.variable.name)
        if (left is TypedValue.Int && right is TypedValue.Int)
            environment.variableAssign(visitable.variable.name, TypedValue.Int(left.value!!.toDouble().pow(1 / right.value!!).toInt()))
        else if (left is TypedValue.Float && right is TypedValue.Float)
            environment.variableAssign(visitable.variable.name, TypedValue.Float(left.value!!.pow(1 / right.value!!)))
        else if (left is TypedValue.Int || left is TypedValue.Float)
            throw TypeException(environment.functionName(), left, right)
        else
            throw TypeException(environment.functionName(), Int::class.toTypedNull(), left)
    }

    override fun visit(visitable: FunctionCallStatement) {
        visit(visitable.expression)
        environment.pop()
    }

    override fun visit(visitable: ReturnStatement) {
        visit(visitable.expression)
        environment.setReturn()
    }

    override fun visit(visitable: FunctionCallExpression) {
        visitable.arguments.forEach { visit(it) }
        visit(functions[visitable.name] ?: throw MissingDeclarationException(environment.functionName(), visitable.name))
    }

    override fun visit(visitable: OrExpression) {
        visit(visitable.left)
        visit(visitable.right)
        val right = environment.pop()
        val left = environment.pop()
        if (left !is TypedValue.Bool)
            throw TypeException(environment.functionName(), TypedValue.Bool(null), left)
        if (right !is TypedValue.Bool)
            throw TypeException(environment.functionName(), TypedValue.Bool(null), right)
        environment.push(TypedValue.Bool(left.value!! || right.value!!))
    }

    override fun visit(visitable: AndExpression) {
        visit(visitable.left)
        visit(visitable.right)
        val right = environment.pop()
        val left = environment.pop()
        if (left !is TypedValue.Bool)
            throw TypeException(environment.functionName(), TypedValue.Bool(null), left)
        if (right !is TypedValue.Bool)
            throw TypeException(environment.functionName(), TypedValue.Bool(null), right)
        environment.push(TypedValue.Bool(left.value!! && right.value!!))
    }

    override fun visit(visitable: NormalComparisonExpression) {
        visit(visitable.left)
        visit(visitable.right)
        val right = environment.pop()
        val left = environment.pop()
        if (left::class != right::class)
            throw TypeException(environment.functionName(), right, left)
        environment.push(TypedValue.Bool(left.value!! == right.value!!))
    }

    override fun visit(visitable: ReferenceComparisonExpression) {
        visit(visitable.left)
        visit(visitable.right)
        val right = environment.pop()
        val left = environment.pop()
        if (left::class != right::class)
            throw TypeException(environment.functionName(), right, left)
        environment.push(TypedValue.Bool(left.value!! === right.value!!)) // TODO
    }

    override fun visit(visitable: NotExpression) {
        visit(visitable.expression)
        val value = environment.pop()
        if (value !is TypedValue.Bool)
            throw TypeException(environment.functionName(), TypedValue.Bool(null), value)
        environment.push(TypedValue.Bool(!value.value!!))
    }

    override fun visit(visitable: TypeCheckExpression) {
        visit(visitable.expression)
        environment.push(TypedValue.Bool(environment.pop().value!!::class == visitable.type))
    }

    override fun visit(visitable: LesserThanExpression) {
        visit(visitable.left)
        visit(visitable.right)
        val right = environment.pop()
        val left = environment.pop()
        if (left is TypedValue.Int && right is TypedValue.Int)
            environment.push(TypedValue.Bool(left.value!! < right.value!!))
        else if (left is TypedValue.Float && right is TypedValue.Float)
            environment.push(TypedValue.Bool(left.value!! < right.value!!))
        else if (left is TypedValue.Int || left is TypedValue.Float)
            throw TypeException(environment.functionName(), left, right)
        else
            throw TypeException(environment.functionName(), Int::class.toTypedNull(), left)
    }

    override fun visit(visitable: LesserOrEqualExpression) {
        visit(visitable.left)
        visit(visitable.right)
        val right = environment.pop()
        val left = environment.pop()
        if (left is TypedValue.Int && right is TypedValue.Int)
            environment.push(TypedValue.Bool(left.value!! <= right.value!!))
        else if (left is TypedValue.Float && right is TypedValue.Float)
            environment.push(TypedValue.Bool(left.value!! <= right.value!!))
        else if (left is TypedValue.Int || left is TypedValue.Float)
            throw TypeException(environment.functionName(), left, right)
        else
            throw TypeException(environment.functionName(), Int::class.toTypedNull(), left)
    }

    override fun visit(visitable: GreaterThanExpression) {
        visit(visitable.left)
        visit(visitable.right)
        val right = environment.pop()
        val left = environment.pop()
        if (left is TypedValue.Int && right is TypedValue.Int)
            environment.push(TypedValue.Bool(left.value!! > right.value!!))
        else if (left is TypedValue.Float && right is TypedValue.Float)
            environment.push(TypedValue.Bool(left.value!! > right.value!!))
        else if (left is TypedValue.Int || left is TypedValue.Float)
            throw TypeException(environment.functionName(), left, right)
        else
            throw TypeException(environment.functionName(), Int::class.toTypedNull(), left)
    }

    override fun visit(visitable: GreaterOrEqualExpression) {
        visit(visitable.left)
        visit(visitable.right)
        val right = environment.pop()
        val left = environment.pop()
        if (left is TypedValue.Int && right is TypedValue.Int)
            environment.push(TypedValue.Bool(left.value!! >= right.value!!))
        else if (left is TypedValue.Float && right is TypedValue.Float)
            environment.push(TypedValue.Bool(left.value!! >= right.value!!))
        else if (left is TypedValue.Int || left is TypedValue.Float)
            throw TypeException(environment.functionName(), left, right)
        else
            throw TypeException(environment.functionName(), Int::class.toTypedNull(), left)
    }

    override fun visit(visitable: AddExpression) {
        visit(visitable.left)
        visit(visitable.right)
        val right = environment.pop()
        val left = environment.pop()
        if (left is TypedValue.Int && right is TypedValue.Int)
            environment.push(TypedValue.Int(left.value!! + right.value!!))
        else if (left is TypedValue.Float && right is TypedValue.Float)
            environment.push(TypedValue.Float(left.value!! + right.value!!))
        else if (left is TypedValue.Int || left is TypedValue.Float)
            throw TypeException(environment.functionName(), left, right)
        else
            throw TypeException(environment.functionName(), Int::class.toTypedNull(), left)
    }

    override fun visit(visitable: SubtractExpression) {
        visit(visitable.left)
        visit(visitable.right)
        val right = environment.pop()
        val left = environment.pop()
        if (left is TypedValue.Int && right is TypedValue.Int)
            environment.push(TypedValue.Int(left.value!! - right.value!!))
        else if (left is TypedValue.Float && right is TypedValue.Float)
            environment.push(TypedValue.Float(left.value!! - right.value!!))
        else if (left is TypedValue.Int || left is TypedValue.Float)
            throw TypeException(environment.functionName(), left, right)
        else
            throw TypeException(environment.functionName(), Int::class.toTypedNull(), left)
    }

    override fun visit(visitable: MultiplyExpression) {
        visit(visitable.left)
        visit(visitable.right)
        val right = environment.pop()
        val left = environment.pop()
        if (left is TypedValue.Int && right is TypedValue.Int)
            environment.push(TypedValue.Int(left.value!! * right.value!!))
        else if (left is TypedValue.Float && right is TypedValue.Float)
            environment.push(TypedValue.Float(left.value!! * right.value!!))
        else if (left is TypedValue.Int || left is TypedValue.Float)
            throw TypeException(environment.functionName(), left, right)
        else
            throw TypeException(environment.functionName(), Int::class.toTypedNull(), left)
    }

    override fun visit(visitable: DivideExpression) {
        visit(visitable.left)
        visit(visitable.right)
        val right = environment.pop()
        val left = environment.pop()
        if (left is TypedValue.Int && right is TypedValue.Int) {
            environment.push(TypedValue.Int(left.value!! / right.value!!))
        } else if (left is TypedValue.Float && right is TypedValue.Float) {
            environment.push(TypedValue.Float(left.value!! / right.value!!))
        } else if (left is TypedValue.Int || left is TypedValue.Float) {
            throw TypeException(environment.functionName(), left, right)
        } else
            throw TypeException(environment.functionName(), Int::class.toTypedNull(), left)
    }

    override fun visit(visitable: ModuloExpression) {
        visit(visitable.left)
        visit(visitable.right)
        val right = environment.pop()
        val left = environment.pop()
        if (left is TypedValue.Int && right is TypedValue.Int) {
            environment.push(TypedValue.Int(left.value!! % right.value!!))
        } else if (left is TypedValue.Float && right is TypedValue.Float) {
            environment.push(TypedValue.Float(left.value!! % right.value!!))
        } else if (left is TypedValue.Int || left is TypedValue.Float) {
            throw TypeException(environment.functionName(), left, right)
        } else
            throw TypeException(environment.functionName(), Int::class.toTypedNull(), left)
    }

    override fun visit(visitable: InverseExpression) {
        visit(visitable.expression)
        val value = environment.pop()
        when (value) {
            is TypedValue.Int -> environment.push(TypedValue.Int(-value.value!!))
            is TypedValue.Float -> environment.push(TypedValue.Float(-value.value!!))
            else -> throw TypeException(environment.functionName(), Int::class.toTypedNull(), value)
        }
    }

    override fun visit(visitable: ExponentExpression) {
        visit(visitable.left)
        visit(visitable.right)
        val right = environment.pop()
        val left = environment.pop()
        if (left is TypedValue.Int && right is TypedValue.Int)
            environment.push(TypedValue.Int(left.value!!.toDouble().pow(right.value!!).toInt()))
        else if (left is TypedValue.Float && right is TypedValue.Float)
            environment.push(TypedValue.Float(left.value!!.pow(right.value!!)))
        else if (left is TypedValue.Int || left is TypedValue.Float)
            throw TypeException(environment.functionName(), left, right)
        else
            throw TypeException(environment.functionName(), Int::class.toTypedNull(), left)
    }

    override fun visit(visitable: RootExpression) {
        visit(visitable.left)
        visit(visitable.right)
        val right = environment.pop()
        val left = environment.pop()
        if (left is TypedValue.Int && right is TypedValue.Int)
            environment.push(TypedValue.Float(left.value!!.toDouble().pow(1.0 / right.value!!)))
        else if (left is TypedValue.Float && right is TypedValue.Float)
            environment.push(TypedValue.Float(left.value!!.pow(1.0 / right.value!!)))
        else if (left is TypedValue.Int || left is TypedValue.Float)
            throw TypeException(environment.functionName(), left, right)
        else
            throw TypeException(environment.functionName(), Int::class.toTypedNull(), left)
    }

    override fun visit(visitable: CastExpression) {
        visit(visitable.expression)
        val value = environment.pop()
        if (visitable.type == Double::class && value is TypedValue.Int) {
            environment.push(TypedValue.Float(value.value!!.toDouble()))
        } else if (visitable.type == Int::class && value is TypedValue.Float) {
            environment.push(TypedValue.Int(value.value!!.toInt()))
        } else {
            when (visitable.type) {
                Int::class -> environment.push(TypedValue.Int(value.value!! as Int))
                Double::class -> environment.push(TypedValue.Float(value.value!! as Double))
                Boolean::class -> environment.push(TypedValue.Bool(value.value!! as Boolean))
                String::class -> environment.push(TypedValue.String(value.value!! as String))
                else -> throw TypeException(environment.functionName(), TypedValue.Int(null), value)
            }
        }
    }

    override fun visit(visitable: TypedIdentifier) {
        throw IllegalStateException()
    }

    override fun visit(visitable: Variable) {
        println(visitable)
        environment.push(environment.variableValue(visitable.name))
    }

    override fun visit(visitable: BoolConstant) {
        environment.push(TypedValue.Bool(visitable.value))
    }

    override fun visit(visitable: FloatConstant) {
        environment.push(TypedValue.Float(visitable.value))
    }

    override fun visit(visitable: IntConstant) {
        environment.push(TypedValue.Int(visitable.value))
    }

    override fun visit(visitable: StringConstant) {
        environment.push(TypedValue.String(visitable.value))
    }

    internal fun visit(visitable: Statement) {
        // TODO fix this
        when (visitable) {
            is VarDeclarationStatement -> visit(visitable)
            is IfStatement -> visit(visitable)
            is WhileStatement -> visit(visitable)
            is NormalAssignmentStatement -> visit(visitable)
            is ReferenceAssignmentStatement -> visit(visitable)
            is SumAssignmentStatement -> visit(visitable)
            is DifferenceAssignmentStatement -> visit(visitable)
            is MultiplicationAssignmentStatement -> visit(visitable)
            is DivisionAssignmentStatement -> visit(visitable)
            is ModuloAssignmentStatement -> visit(visitable)
            is ExponentAssignmentStatement -> visit(visitable)
            is RootAssignmentStatement -> visit(visitable)
            is FunctionCallStatement -> visit(visitable)
            is ReturnStatement -> visit(visitable)
        }
    }

    internal fun visit(visitable: Expression) {
        // TODO fix this
        when (visitable) {
            is OrExpression -> visit(visitable)
            is AndExpression -> visit(visitable)
            is NormalComparisonExpression -> visit(visitable)
            is ReferenceComparisonExpression -> visit(visitable)
            is NotExpression -> visit(visitable)
            is TypeCheckExpression -> visit(visitable)
            is LesserThanExpression -> visit(visitable)
            is LesserOrEqualExpression -> visit(visitable)
            is GreaterThanExpression -> visit(visitable)
            is GreaterOrEqualExpression -> visit(visitable)
            is AddExpression -> visit(visitable)
            is SubtractExpression -> visit(visitable)
            is MultiplyExpression -> visit(visitable)
            is DivideExpression -> visit(visitable)
            is ModuloExpression -> visit(visitable)
            is InverseExpression -> visit(visitable)
            is ExponentExpression -> visit(visitable)
            is RootExpression -> visit(visitable)
            is CastExpression -> visit(visitable)
            is Variable -> visit(visitable)
            is BoolConstant -> visit(visitable)
            is FloatConstant -> visit(visitable)
            is IntConstant -> visit(visitable)
            is StringConstant -> visit(visitable)
            is FunctionCallExpression -> visit(visitable)
        }
    }
}