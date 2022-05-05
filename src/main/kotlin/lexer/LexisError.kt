package lexer

abstract class LexisError(
    protected val foundToken: Char,
    protected val line: Int,
    protected val column: Int
): Exception()

class UnrecognizedSignError(foundToken: Char, line: Int, column: Int): LexisError(foundToken, line, column) {
    override val message: String
        get() = "unrecognized token: $foundToken found at $line:$column"
}

class MissingSignError(foundToken: Char, line: Int, column: Int, private val expectedSignGroup: String): LexisError(foundToken, line, column) {
    override val message: String
        get() = "expected a $expectedSignGroup after token: $foundToken found at $line:$column"
}

class TokenTooBigError(foundToken: Char, line: Int, column: Int): LexisError(foundToken, line, column) {
    override val message: String
        get() = "token value is too big: $foundToken found at $line:$column"
}
