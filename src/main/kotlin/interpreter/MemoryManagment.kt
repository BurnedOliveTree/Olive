package interpreter

import java.util.*
import kotlin.reflect.KClass

sealed class TypedValue {
    abstract val value: Any?
    abstract fun copy(): TypedValue

    class Int(override var value: kotlin.Int?): TypedValue() {
        override fun copy(): TypedValue {
            return Int(this.value)
        }
    }
    class Float(override var value: Double?): TypedValue() {
        override fun copy(): TypedValue {
            return Float(this.value)
        }
    }
    class String(override var value: kotlin.String?) : TypedValue() {
        override fun copy(): TypedValue {
            return String(this.value)
        }
    }
    class Bool(override var value: Boolean?) : TypedValue() {
        override fun copy(): TypedValue {
            return Bool(this.value)
        }
    }
    class Unit(override val value: kotlin.Unit? = null): TypedValue() {
        override fun copy(): TypedValue {
            return Unit(this.value)
        }
    }


    companion object {
        fun Number(value: Number?): TypedValue {
            return if (value is kotlin.Int?) {
                Int(value)
            } else {
                Float(value?.toDouble())
            }
        }
    }
}

fun KClass<out Any>.toTypedNull(): TypedValue {
    return when (this) {
        Int::class -> TypedValue.Int(null)
        Double::class -> TypedValue.Float(null)
        Boolean::class -> TypedValue.Bool(null)
        String::class -> TypedValue.String(null)
        Unit::class -> TypedValue.Unit(null)
        else -> throw IllegalStateException()
    }
}

class Scope {
    private val variables = mutableMapOf<String, TypedValue>()

    fun declare(name: String, type: KClass<out Any>, value: TypedValue?, functionName: String) {
        if (value != null)
            if (value.value!!::class != type)
                throw TypeException(functionName, variables[name]!!, value)
        variables[name] = value ?: type.toTypedNull()
        println(variables.forEach { print(it.key to it.value.value) })
    }

    fun assign(name: String, value: TypedValue, functionName: String) {
        if (value::class != variables[name]!!::class)
            throw TypeException(functionName, variables[name]!!, value)
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

    fun declare(name: String, type: KClass<out Any>, value: TypedValue?, functionName: String) {
        scopes.last().declare(name, type, value, functionName)
    }

    fun assign(name: String, value: TypedValue, functionName: String) {
        val scope = scopes.reversed().firstOrNull { it.isPresent(name) }
        scope?.assign(name, value, functionName) ?: throw MissingDeclarationException(functionName, name)
    }

    fun value(name: String, functionName: String): TypedValue {
        val scope = scopes.reversed().firstOrNull { it.isPresent(name) }
        return scope?.value(name) ?: throw MissingDeclarationException(functionName, name)
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
        functionCalls.last().declare(name, type, value, functionName())
    }
    fun variableAssign(name: String, value: TypedValue) {
        functionCalls.last().assign(name, value, functionName())
    }
    fun variableValue(name: String): TypedValue {
        return functionCalls.last().value(name, functionName())
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
