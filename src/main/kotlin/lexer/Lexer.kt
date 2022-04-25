package lexer

// TODO lazy
internal fun String.toQueue(): ArrayDeque<Char> {
    val queue = ArrayDeque<Char>()
    this.forEach { queue.addLast(it) }
    return queue
}

enum class TokenType {
    StringConstant,
    NumConstant,
    BoolConstant,

    UnitType,
    IntType,
    FloatType,
    NumberType,
    StringType,
    BoolType,

    SumOp,
    DifferenceOp,
    MultiplicationOp,
    ExponentOp,
    DivisionOp,
    RootOp,
    ModuloOp,

    NotOp,
    AndOp,
    OrOp,
    IsOp,
    CastOp,

    NormalAssignOp,
    ReferenceAssignOp,
    SumAssignOp,
    DifferenceAssignOp,
    MultiplicationAssignOp,
    ExponentAssignOp,
    DivisionAssignOp,
    RootAssignOp,
    ModuloAssignOp,

    NormalComparisonOp,
    ReferenceComparisonOp,
    LesserThanOp,
    LesserOrEqualOp,
    GreaterThanOp,
    GreaterOrEqualOp,

    TypeSign,
    CommentSign,
    EndSign,
    EnumerationSign,
    MemberReferenceSign,

    LeftParenthesesSign,
    RightParenthesesSign,
    LeftBraceSign,
    RightBraceSign,

    Variable,
    If,
    Else,
    While,
    Return,

    Identifier,
    Comment;

    fun isConstant(): Boolean =
        this in listOf(BoolConstant, NumConstant, StringConstant)

    fun isType(): Boolean =
        this in listOf(UnitType, IntType, FloatType, NumberType, StringType, BoolType)

    fun isAssignOp(): Boolean =
        this in listOf(NormalAssignOp, ReferenceAssignOp, SumAssignOp, DifferenceAssignOp, MultiplicationAssignOp, ExponentAssignOp, DivisionAssignOp, RootAssignOp, ModuloAssignOp)
}

data class LexerToken(val type: TokenType, val value: Any? = null)

internal class CodeIterator(sourceCode: String, private val tabSize: Int) {
    private val iterator = sourceCode.toQueue()
    var lineNumber = 0
    var columnNumber = 1
    private var currentChar: Char = '§'

    fun isEmpty() = iterator.isEmpty()

    fun current() = currentChar

    fun peek(): Char? = iterator.firstOrNull()

    fun next(): Char {
        currentChar = iterator.removeFirst()
        when (currentChar) {
            '\t' -> columnNumber += tabSize
            '\n', '\r' -> {
                lineNumber++
                columnNumber = 0
            }
            else -> columnNumber++
        }
        return currentChar
    }
}

class Lexer(sourceCode: String, tabSize: Int = 4) {
    private val tokens: ArrayDeque<LexerToken> = ArrayDeque()
    private val iterator = CodeIterator(sourceCode, tabSize)
    private val keywordsMap = mapOf(
        "Bool" to LexerToken(TokenType.BoolType),
        "Float" to LexerToken(TokenType.FloatType),
        "Int" to LexerToken(TokenType.IntType),
        "Number" to LexerToken(TokenType.NumberType),
        "String" to LexerToken(TokenType.StringType),
        "Unit" to LexerToken(TokenType.UnitType),
        "and" to LexerToken(TokenType.AndOp),
        "as" to LexerToken(TokenType.CastOp),
        "else" to LexerToken(TokenType.Else),
        "false" to LexerToken(TokenType.BoolConstant, false),
        "if" to LexerToken(TokenType.If),
        "is" to LexerToken(TokenType.IsOp),
        "not" to LexerToken(TokenType.NotOp),
        "or" to LexerToken(TokenType.OrOp),
        "return" to LexerToken(TokenType.Return),
        "true" to LexerToken(TokenType.BoolConstant, true),
        "var" to LexerToken(TokenType.Variable),
        "while" to LexerToken(TokenType.While),
    )

    private fun parseNextToken(): Boolean {
        if (!iterator.isEmpty()) {
            iterator.next()
            skipWhitespace()
            if (keywordOrIdentifier()) return true
            if (numericConstant()) return true
            if (stringConstant()) return true
            if (comment()) return true
            if (operator()) return true
            throw LexisError(iterator.current(), iterator.lineNumber, iterator.columnNumber)
        }
        return false
    }

    private fun skipWhitespace() {
        while (iterator.current().isWhitespace()) {
            iterator.next()
        }
    }

    private fun keywordOrIdentifier(): Boolean {
        fun Char.isIdentifierSign() = this.isLetter() || this == '_'

        if (!iterator.current().isIdentifierSign())
            return false

        val identifier = StringBuilder().append(iterator.current())
        while (iterator.peek()?.isIdentifierSign() == true) {
            identifier.append(iterator.next())
        }
        keywordsMap[identifier.toString()].let {
            tokens.addLast(it ?: LexerToken(TokenType.Identifier, identifier.toString()))
        }
        return true
    }

    private fun numericConstant(): Boolean {
        if (!iterator.current().isDigit())
            return false

        val number = StringBuilder().append(iterator.current())
        if (iterator.current() != '0') {
            while (iterator.peek()?.isDigit() == true) {
                number.append(iterator.next())
            }
        }
        if (iterator.peek() == '.') {
            number.append(iterator.next())
            if (iterator.peek()?.isDigit() != true)
                throw LexisError(iterator.current(), iterator.lineNumber, iterator.columnNumber)
            while (iterator.peek()?.isDigit() == true) {
                number.append(iterator.next())
            }
            tokens.addLast(LexerToken(TokenType.NumConstant, number.toString().toDouble()))
        } else
            tokens.addLast(LexerToken(TokenType.NumConstant, number.toString().toInt()))
        return true
    }

    private fun stringConstant(): Boolean {
        if (iterator.current() != '"')
            return false
        val stringConstant = StringBuilder()
        while ((iterator.peek() ?: '"') != '"') {
            iterator.next()
            if (iterator.current() == '\\') {
                if (iterator.isEmpty())
                    throw LexisError(iterator.current(), iterator.lineNumber, iterator.columnNumber) // TODO unknown cause of error
                iterator.next()
            }
            stringConstant.append(iterator.current())
        }
        if (iterator.isEmpty())
            throw LexisError(iterator.current(), iterator.lineNumber, iterator.columnNumber)
        iterator.next()
        tokens.addLast(LexerToken(TokenType.StringConstant, stringConstant.toString()))
        return true
    }

    private fun comment(): Boolean {
        if (iterator.current() != '#')
            return false
        tokens.addLast(LexerToken(TokenType.CommentSign))
        val comment = StringBuilder()
        while (iterator.peek()?.equals('\n') == false) {
            comment.append(iterator.next())
        }
        tokens.addLast(LexerToken(TokenType.Comment, comment.toString()))
        return true
    }

    private fun operator(): Boolean {
        fun assignOperator(normalTokenType: TokenType, assignTokenType: TokenType) {
            if (iterator.peek() == '=') {
                iterator.next()
                tokens.addLast(LexerToken(assignTokenType))
            } else
                tokens.addLast(LexerToken(normalTokenType))
        }

        when (iterator.current()) {
            '+' -> assignOperator(TokenType.SumOp, TokenType.SumAssignOp)
            '-' -> assignOperator(TokenType.DifferenceOp, TokenType.DifferenceAssignOp)
            '*' -> assignOperator(TokenType.MultiplicationOp, TokenType.MultiplicationAssignOp)
            '/' -> assignOperator(TokenType.DivisionOp, TokenType.DivisionAssignOp)
            '^' -> assignOperator(TokenType.ExponentOp, TokenType.ExponentAssignOp)
            '|' -> assignOperator(TokenType.RootOp, TokenType.RootAssignOp)
            '%' -> assignOperator(TokenType.ModuloOp, TokenType.ModuloAssignOp)
            '<' -> assignOperator(TokenType.LesserThanOp, TokenType.LesserOrEqualOp)
            '>' -> assignOperator(TokenType.GreaterThanOp, TokenType.GreaterOrEqualOp)
            '=' -> assignOperator(TokenType.NormalAssignOp, TokenType.NormalComparisonOp)
            '&' -> {
                if (iterator.peek()!! == '=') {
                    iterator.next()
                    assignOperator(TokenType.ReferenceAssignOp, TokenType.ReferenceComparisonOp)
                } else {
                    throw LexisError(iterator.current(), iterator.lineNumber, iterator.columnNumber)
                }
            }
            ':' -> tokens.addLast(LexerToken(TokenType.TypeSign))
            ';' -> tokens.addLast(LexerToken(TokenType.EndSign))
            ',' -> tokens.addLast(LexerToken(TokenType.EnumerationSign))
            '.' -> tokens.addLast(LexerToken(TokenType.MemberReferenceSign))
            '(' -> tokens.addLast(LexerToken(TokenType.LeftParenthesesSign))
            ')' -> tokens.addLast(LexerToken(TokenType.RightParenthesesSign))
            '{' -> tokens.addLast(LexerToken(TokenType.LeftBraceSign))
            '}' -> tokens.addLast(LexerToken(TokenType.RightBraceSign))
            else -> return false
        }
        return true
    }

    fun next(): Boolean {
        if (tokens.isEmpty())
            parseNextToken()
        return tokens.removeFirstOrNull() != null
    }

    fun peek(): LexerToken {
        if (tokens.isEmpty())
            parseNextToken()
        return tokens.first()
    }

    fun hasNext(): Boolean {
        if (tokens.isEmpty())
            parseNextToken()
        return tokens.firstOrNull() != null
    }
}
