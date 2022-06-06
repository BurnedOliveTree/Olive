package interpreter

import kotlin.reflect.KClass

abstract class SemanticsError(protected val currentFunction: String): Exception()

class MissingDeclarationException(currentFunction: String, private val expectedDeclaration: String): SemanticsError(currentFunction) {
    override val message: String
        get() = "error occurred in $currentFunction: expected a declaration of $expectedDeclaration"
}

class TypeException(currentFunction: String, private val exceptedTypedValue: KClass<out Any>, private val actualTypedValue: KClass<out Any>): SemanticsError(currentFunction) {
    override val message: String
        get() = "error occurred in $currentFunction: expected a value of $exceptedTypedValue, got $actualTypedValue instead"
}

class ConflictingDeclarationException(currentFunction: String, private val conflictingName: String): SemanticsError(currentFunction) {
    override val message: String
        get() = "error occurred in $currentFunction: found a redeclaration of $conflictingName"
}

class IllegalMathematicalOperationException(currentFunction: String, private val operation: String): SemanticsError(currentFunction) {
    override val message: String
        get() = "error occurred in $currentFunction: tried to execute an illegal mathematical operation: $operation"
}

class InvalidArgumentAmountException(currentFunction: String, private val functionName: String, private val actualAmount: Int): SemanticsError(currentFunction) {
    override val message: String
        get() = "error occurred in $currentFunction: tried to call function $functionName with $actualAmount arguments"
}
