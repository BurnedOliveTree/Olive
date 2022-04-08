package lexer;

class LexisError(
    private val foundToken: Char,
    private val line: Int,
    private val column: Int
): Exception() {
    override val message: String
        get() = "unrecognized token: $foundToken found at $line:$column"
}
