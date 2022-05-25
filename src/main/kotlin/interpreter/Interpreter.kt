package interpreter

import parser.*
import parser.Function
import utilities.TypedValue
import utilities.toTypedNull
import java.util.Stack
import kotlin.math.pow
import kotlin.reflect.KClass

class Scope {
    private val variables = mutableMapOf<String, TypedValue>()

    fun declare(name: String, type: KClass<out Any>, value: TypedValue?) {
        if (value != null)
            if (value.value!!::class != type)
                throw TypeException("", value) // TODO
        variables[name] = value ?: type.toTypedNull()
        println(variables.forEach { print(it.key to it.value.value) })
    }

    fun assign(name: String, value: TypedValue) {
        // TODO assert type
        variables[name] = value
        println(variables.forEach { print(it.key to it.value.value) })
    }

    fun value(name: String): TypedValue? {
        println(variables.forEach { print(it.key to it.value.value) })
        return variables[name]
    }

    fun isPresent(name: String): Boolean {
        return variables.containsKey(name)
    }
}

class CallContext(val name: String) {
    private val scopes = Stack<Scope>()
    var returnFlag = false

    fun declare(name: String, type: KClass<out Any>, value: TypedValue?) {
        scopes.last().declare(name, type, value)
    }

    fun assign(name: String, value: TypedValue) {
        val scope = scopes.reversed().firstOrNull { it.isPresent(name) }
        scope?.assign(name, value) ?: throw MissingDeclarationException("", name) // TODO unassigned access exception or smth
    }

    fun value(name: String): TypedValue {
        val scope = scopes.reversed().firstOrNull { it.isPresent(name) }
        return scope?.value(name) ?: throw MissingDeclarationException("", name) // TODO unassigned access exception or smth
    }

    fun enter() {
        scopes.push(Scope())
    }

    fun leave() {
        scopes.pop()
    }
}

class Environment {
    private val functionCalls = Stack<CallContext>()
    private val stack = Stack<TypedValue>()

    fun variableDeclare(name: String, type: KClass<out Any>, value: TypedValue?) {
        functionCalls.last().declare(name, type, value)
    }
    fun variableAssign(name: String, value: TypedValue) {
        functionCalls.last().assign(name, value)
    }
    fun variableValue(name: String): TypedValue {
        return functionCalls.last().value(name)
    }

    fun blockEnter() {
        functionCalls.last().enter()
    }
    fun blockLeave() {
        functionCalls.last().leave()
    }

    fun functionCall(name: String) {
        functionCalls.push(CallContext(name))
        functionCalls.last().enter()
    }
    fun functionLeave() {
        functionCalls.last().leave()
        functionCalls.pop()
    }

    fun functionName(): String {
        return functionCalls.last().name
    }

    fun push(value: TypedValue) {
        stack.push(value)
    }
    fun pop(): TypedValue {
        return stack.pop()
    }

    fun isReturn() = functionCalls.last().returnFlag
    fun setReturn() { functionCalls.last().returnFlag = true }
}

class Interpreter: Visitor() {
    private lateinit var functions: Map<String, Function>
    private val environment = Environment()

    // TO-CHECK if (false and average()) -> average should not be called
    // TO-CHECK optimization?
    // TO-CHECK fun(n, inc(n), n) -> how to do it, will the first and third n have the same value, or incremented?
    // TO-CHECK arguments should be evaluated upon function call, not declaration

    fun value() {
        print(environment)
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
        // TODO do something with return value
        print(returnValue?.value)
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
        if ((environment.pop() as TypedValue.tBool).value!!) {
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
        while ((environment.pop() as TypedValue.tBool).value!! && !environment.isReturn()) {
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
        if (left is TypedValue.tInt && right is TypedValue.tInt)
            environment.variableAssign(visitable.variable.name, TypedValue.tInt(left.value!! + right.value!!))
        else if (left is TypedValue.tFloat && right is TypedValue.tFloat)
            environment.variableAssign(visitable.variable.name, TypedValue.tFloat(left.value!! + right.value!!))
        else if (left is TypedValue.tInt || left is TypedValue.tFloat)
            throw TypeException(environment.functionName(), right)
        else
            throw TypeException(environment.functionName(), left)
    }

    override fun visit(visitable: DifferenceAssignmentStatement) {
        visit(visitable.expression)
        val right = environment.pop()
        val left = environment.variableValue(visitable.variable.name)
        if (left is TypedValue.tInt && right is TypedValue.tInt)
            environment.variableAssign(visitable.variable.name, TypedValue.tInt(left.value!! - right.value!!))
        else if (left is TypedValue.tFloat && right is TypedValue.tFloat)
            environment.variableAssign(visitable.variable.name, TypedValue.tFloat(left.value!! - right.value!!))
        else if (left is TypedValue.tInt || left is TypedValue.tFloat)
            throw TypeException(environment.functionName(), right)
        else
            throw TypeException(environment.functionName(), left)
    }

    override fun visit(visitable: MultiplicationAssignmentStatement) {
        visit(visitable.expression)
        val right = environment.pop()
        val left = environment.variableValue(visitable.variable.name)
        if (left is TypedValue.tInt && right is TypedValue.tInt)
            environment.variableAssign(visitable.variable.name, TypedValue.tInt(left.value!! * right.value!!))
        else if (left is TypedValue.tFloat && right is TypedValue.tFloat)
            environment.variableAssign(visitable.variable.name, TypedValue.tFloat(left.value!! * right.value!!))
        else if (left is TypedValue.tInt || left is TypedValue.tFloat)
            throw TypeException(environment.functionName(), right)
        else
            throw TypeException(environment.functionName(), left)
    }

    override fun visit(visitable: DivisionAssignmentStatement) {
        visit(visitable.expression)
        val right = environment.pop()
        val left = environment.variableValue(visitable.variable.name)
        if (left is TypedValue.tInt && right is TypedValue.tInt)
            environment.variableAssign(visitable.variable.name, TypedValue.tInt(left.value!! / right.value!!))
        else if (left is TypedValue.tFloat && right is TypedValue.tFloat)
            environment.variableAssign(visitable.variable.name, TypedValue.tFloat(left.value!! / right.value!!))
        else if (left is TypedValue.tInt || left is TypedValue.tFloat)
            throw TypeException(environment.functionName(), right)
        else
            throw TypeException(environment.functionName(), left)
    }

    override fun visit(visitable: ModuloAssignmentStatement) {
        visit(visitable.expression)
        val right = environment.pop()
        val left = environment.variableValue(visitable.variable.name)
        if (left is TypedValue.tInt && right is TypedValue.tInt)
            environment.variableAssign(visitable.variable.name, TypedValue.tInt(left.value!! % right.value!!))
        else if (left is TypedValue.tFloat && right is TypedValue.tFloat)
            environment.variableAssign(visitable.variable.name, TypedValue.tFloat(left.value!! % right.value!!))
        else if (left is TypedValue.tInt || left is TypedValue.tFloat)
            throw TypeException(environment.functionName(), right)
        else
            throw TypeException(environment.functionName(), left)
    }

    override fun visit(visitable: ExponentAssignmentStatement) {
        visit(visitable.expression)
        val right = environment.pop()
        val left = environment.variableValue(visitable.variable.name)
        if (left is TypedValue.tInt && right is TypedValue.tInt)
            environment.variableAssign(visitable.variable.name, TypedValue.tInt(left.value!!.toDouble().pow(right.value!!).toInt()))
        else if (left is TypedValue.tFloat && right is TypedValue.tFloat)
            environment.variableAssign(visitable.variable.name, TypedValue.tFloat(left.value!!.pow(right.value!!)))
        else if (left is TypedValue.tInt || left is TypedValue.tFloat)
            throw TypeException(environment.functionName(), right)
        else
            throw TypeException(environment.functionName(), left)
    }

    override fun visit(visitable: RootAssignmentStatement) {
        visit(visitable.expression)
        val right = environment.pop()
        val left = environment.variableValue(visitable.variable.name)
        if (left is TypedValue.tInt && right is TypedValue.tInt)
            environment.variableAssign(visitable.variable.name, TypedValue.tInt(left.value!!.toDouble().pow(1 / right.value!!).toInt()))
        else if (left is TypedValue.tFloat && right is TypedValue.tFloat)
            environment.variableAssign(visitable.variable.name, TypedValue.tFloat(left.value!!.pow(1 / right.value!!)))
        else if (left is TypedValue.tInt || left is TypedValue.tFloat)
            throw TypeException(environment.functionName(), right)
        else
            throw TypeException(environment.functionName(), left)
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
        if (left !is TypedValue.tBool)
            throw TypeException(environment.functionName(), TypedValue.tBool(null))
        if (right !is TypedValue.tBool)
            throw TypeException(environment.functionName(), TypedValue.tBool(null))
        environment.push(TypedValue.tBool(left.value!! || right.value!!))
    }

    override fun visit(visitable: AndExpression) {
        visit(visitable.left)
        visit(visitable.right)
        val right = environment.pop()
        val left = environment.pop()
        if (left !is TypedValue.tBool)
            throw TypeException(environment.functionName(), TypedValue.tBool(null))
        if (right !is TypedValue.tBool)
            throw TypeException(environment.functionName(), TypedValue.tBool(null))
        environment.push(TypedValue.tBool(left.value!! && right.value!!))
    }

    override fun visit(visitable: NormalComparisonExpression) {
        visit(visitable.left)
        visit(visitable.right)
        val right = environment.pop()
        val left = environment.pop()
        if (left::class != right::class)
            throw TypeException(environment.functionName(), right)
        environment.push(TypedValue.tBool(left.value!! == right.value!!))
    }

    override fun visit(visitable: ReferenceComparisonExpression) {
        visit(visitable.left)
        visit(visitable.right)
        val right = environment.pop()
        val left = environment.pop()
        if (left::class != right::class)
            throw TypeException(environment.functionName(), right)
        environment.push(TypedValue.tBool(left.value!! === right.value!!)) // TODO
    }

    override fun visit(visitable: NotExpression) {
        visit(visitable.expression)
        val value = environment.pop()
        if (value !is TypedValue.tBool)
            throw TypeException(environment.functionName(), TypedValue.tBool(null))
        environment.push(TypedValue.tBool(!value.value!!))
    }

    override fun visit(visitable: TypeCheckExpression) {
        visit(visitable.expression)
        environment.push(TypedValue.tBool(environment.pop()::class == visitable.type))
    }

    override fun visit(visitable: LesserThanExpression) {
        visit(visitable.left)
        visit(visitable.right)
        val right = environment.pop()
        val left = environment.pop()
        if (left is TypedValue.tInt && right is TypedValue.tInt)
            environment.push(TypedValue.tBool(left.value!! < right.value!!))
        else if (left is TypedValue.tFloat && right is TypedValue.tFloat)
            environment.push(TypedValue.tBool(left.value!! < right.value!!))
        else if (left is TypedValue.tInt || left is TypedValue.tFloat)
            throw TypeException(environment.functionName(), right)
        else
            throw TypeException(environment.functionName(), left)
    }

    override fun visit(visitable: LesserOrEqualExpression) {
        visit(visitable.left)
        visit(visitable.right)
        val right = environment.pop()
        val left = environment.pop()
        if (left is TypedValue.tInt && right is TypedValue.tInt)
            environment.push(TypedValue.tBool(left.value!! <= right.value!!))
        else if (left is TypedValue.tFloat && right is TypedValue.tFloat)
            environment.push(TypedValue.tBool(left.value!! <= right.value!!))
        else if (left is TypedValue.tInt || left is TypedValue.tFloat)
            throw TypeException(environment.functionName(), right)
        else
            throw TypeException(environment.functionName(), left)
    }

    override fun visit(visitable: GreaterThanExpression) {
        visit(visitable.left)
        visit(visitable.right)
        val right = environment.pop()
        val left = environment.pop()
        if (left is TypedValue.tInt && right is TypedValue.tInt)
            environment.push(TypedValue.tBool(left.value!! > right.value!!))
        else if (left is TypedValue.tFloat && right is TypedValue.tFloat)
            environment.push(TypedValue.tBool(left.value!! > right.value!!))
        else if (left is TypedValue.tInt || left is TypedValue.tFloat)
            throw TypeException(environment.functionName(), right)
        else
            throw TypeException(environment.functionName(), left)
    }

    override fun visit(visitable: GreaterOrEqualExpression) {
        visit(visitable.left)
        visit(visitable.right)
        val right = environment.pop()
        val left = environment.pop()
        if (left is TypedValue.tInt && right is TypedValue.tInt)
            environment.push(TypedValue.tBool(left.value!! >= right.value!!))
        else if (left is TypedValue.tFloat && right is TypedValue.tFloat)
            environment.push(TypedValue.tBool(left.value!! >= right.value!!))
        else if (left is TypedValue.tInt || left is TypedValue.tFloat)
            throw TypeException(environment.functionName(), right)
        else
            throw TypeException(environment.functionName(), left)
    }

    override fun visit(visitable: AddExpression) {
        visit(visitable.left)
        visit(visitable.right)
        val right = environment.pop()
        val left = environment.pop()
        if (left is TypedValue.tInt && right is TypedValue.tInt)
            environment.push(TypedValue.tInt(left.value!! + right.value!!))
        else if (left is TypedValue.tFloat && right is TypedValue.tFloat)
            environment.push(TypedValue.tFloat(left.value!! + right.value!!))
        else if (left is TypedValue.tInt || left is TypedValue.tFloat)
            throw TypeException(environment.functionName(), right)
        else
            throw TypeException(environment.functionName(), left)
    }

    override fun visit(visitable: SubtractExpression) {
        visit(visitable.left)
        visit(visitable.right)
        val right = environment.pop()
        val left = environment.pop()
        if (left is TypedValue.tInt && right is TypedValue.tInt)
            environment.push(TypedValue.tInt(left.value!! - right.value!!))
        else if (left is TypedValue.tFloat && right is TypedValue.tFloat)
            environment.push(TypedValue.tFloat(left.value!! - right.value!!))
        else if (left is TypedValue.tInt || left is TypedValue.tFloat)
            throw TypeException(environment.functionName(), right)
        else
            throw TypeException(environment.functionName(), left)
    }

    override fun visit(visitable: MultiplyExpression) {
        visit(visitable.left)
        visit(visitable.right)
        val right = environment.pop()
        val left = environment.pop()
        if (left is TypedValue.tInt && right is TypedValue.tInt)
            environment.push(TypedValue.tInt(left.value!! * right.value!!))
        else if (left is TypedValue.tFloat && right is TypedValue.tFloat)
            environment.push(TypedValue.tFloat(left.value!! * right.value!!))
        else if (left is TypedValue.tInt || left is TypedValue.tFloat)
            throw TypeException(environment.functionName(), right)
        else
            throw TypeException(environment.functionName(), left)
    }

    override fun visit(visitable: DivideExpression) {
        visit(visitable.left)
        visit(visitable.right)
        val right = environment.pop()
        val left = environment.pop()
        if (left is TypedValue.tInt && right is TypedValue.tInt) {
            environment.push(TypedValue.tInt(left.value!! / right.value!!))
        } else if (left is TypedValue.tFloat && right is TypedValue.tFloat) {
            environment.push(TypedValue.tFloat(left.value!! / right.value!!))
        } else if (left is TypedValue.tInt || left is TypedValue.tFloat) {
            throw TypeException(environment.functionName(), right)
        } else
            throw TypeException(environment.functionName(), left)
    }

    override fun visit(visitable: ModuloExpression) {
        visit(visitable.left)
        visit(visitable.right)
        val right = environment.pop()
        val left = environment.pop()
        if (left is TypedValue.tInt && right is TypedValue.tInt) {
            environment.push(TypedValue.tInt(left.value!! % right.value!!))
        } else if (left is TypedValue.tFloat && right is TypedValue.tFloat) {
            environment.push(TypedValue.tFloat(left.value!! % right.value!!))
        } else if (left is TypedValue.tInt || left is TypedValue.tFloat) {
            throw TypeException(environment.functionName(), right)
        } else
            throw TypeException(environment.functionName(), left)
    }

    override fun visit(visitable: InverseExpression) {
        visit(visitable.expression)
        val value = environment.pop()
        when (value) {
            is TypedValue.tInt -> environment.push(TypedValue.tInt(-value.value!!))
            is TypedValue.tFloat -> environment.push(TypedValue.tFloat(-value.value!!))
            else -> throw TypeException(environment.functionName(), TypedValue.tInt(null))
        }
        environment.push(value)
    }

    override fun visit(visitable: ExponentExpression) {
        visit(visitable.left)
        visit(visitable.right)
        val right = environment.pop()
        val left = environment.pop()
        if (left is TypedValue.tInt && right is TypedValue.tInt)
            environment.push(TypedValue.tInt(left.value!!.toDouble().pow(right.value!!).toInt()))
        else if (left is TypedValue.tFloat && right is TypedValue.tFloat)
            environment.push(TypedValue.tFloat(left.value!!.pow(right.value!!)))
        else if (left is TypedValue.tInt || left is TypedValue.tFloat)
            throw TypeException(environment.functionName(), right)
        else
            throw TypeException(environment.functionName(), left)
    }

    override fun visit(visitable: RootExpression) {
        visit(visitable.left)
        visit(visitable.right)
        val right = environment.pop()
        val left = environment.pop()
        if (left is TypedValue.tInt && right is TypedValue.tInt)
            environment.push(TypedValue.tFloat(left.value!!.toDouble().pow(1.0 / right.value!!)))
        else if (left is TypedValue.tFloat && right is TypedValue.tFloat)
            environment.push(TypedValue.tFloat(left.value!!.pow(1.0 / right.value!!)))
        else if (left is TypedValue.tInt || left is TypedValue.tFloat)
            throw TypeException(environment.functionName(), right)
        else
            throw TypeException(environment.functionName(), left)
    }

    override fun visit(visitable: CastExpression) {
        visit(visitable.expression)
        val value = environment.pop()
        if (visitable.type == Double::class && value is TypedValue.tInt) {
            environment.push(TypedValue.tFloat(value.value!!.toDouble()))
        } else if (visitable.type == Int::class && value is TypedValue.tFloat) {
            environment.push(TypedValue.tInt(value.value!!.toInt()))
        } else {
            when (visitable.type) {
                Int::class -> environment.push(TypedValue.tInt(value.value!! as Int))
                Double::class -> environment.push(TypedValue.tFloat(value.value!! as Double))
                Boolean::class -> environment.push(TypedValue.tBool(value.value!! as Boolean))
                String::class -> environment.push(TypedValue.tString(value.value!! as String))
                else -> throw TypeException(environment.functionName(), TypedValue.tInt(null))
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
        environment.push(TypedValue.tBool(visitable.value))
    }

    override fun visit(visitable: FloatConstant) {
        environment.push(TypedValue.tFloat(visitable.value))
    }

    override fun visit(visitable: IntConstant) {
        environment.push(TypedValue.tInt(visitable.value))
    }

    override fun visit(visitable: StringConstant) {
        environment.push(TypedValue.tString(visitable.value))
    }

    private fun visit(visitable: Statement) {
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

    private fun visit(visitable: Expression) {
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