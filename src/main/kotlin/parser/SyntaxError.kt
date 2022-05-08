package parser

import lexer.LexerToken

abstract class SyntaxError(
    protected val foundToken: LexerToken,
    protected val currentFunction: String
): Exception()

class ExpectedOtherTokenException(foundToken: LexerToken, currentFunction: String, private val expectedToken: String): SyntaxError(foundToken, currentFunction) {
    override val message: String
        get() = "error occurred in $currentFunction at ${foundToken.line}:${foundToken.column}: expected a $expectedToken, got a $foundToken instead"
}