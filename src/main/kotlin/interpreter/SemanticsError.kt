package interpreter

import utilities.TypedValue

abstract class SemanticsError(protected val currentFunction: String): Exception()

class MissingDeclarationException(currentFunction: String, private val expectedDeclaration: String): SemanticsError(currentFunction) {
    override val message: String
        get() = "error occurred in $currentFunction: expected a declaration of $expectedDeclaration"
}

class TypeException(currentFunction: String, private val exceptedTypedValue: TypedValue): SemanticsError(currentFunction) {
    override val message: String
        get() = "error occurred in $currentFunction: expected a value of ${exceptedTypedValue::class} type"
}