package interpreter

abstract class SemanticsError(protected val currentFunction: String): Exception()

class MissingDeclarationException(currentFunction: String, private val expectedDeclaration: String): SemanticsError(currentFunction) {
    override val message: String
        get() = "error occurred in $currentFunction: expected a declaration of $expectedDeclaration"
}

class TypeException(currentFunction: String, private val exceptedTypedValue: TypedValue, private val actualTypedValue: TypedValue): SemanticsError(currentFunction) {
    override val message: String
        get() = "error occurred in $currentFunction: expected a value of ${exceptedTypedValue.value!!::class}, got ${actualTypedValue.value!!::class} instead"
}

class ConflictingDeclarationException(currentFunction: String, private val conflictingName: String): SemanticsError(currentFunction) {
    override val message: String
        get() = "error occurred in $currentFunction: found a redeclaration of $conflictingName"
}

class IllegalOperationException(currentFunction: String, private val operation: String): SemanticsError(currentFunction) {
    override val message: String
        get() = "error occurred in $currentFunction: tried to execute an illegal operation: $operation"
}
