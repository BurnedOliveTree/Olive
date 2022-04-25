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
    var columnNumber = 0
    private var currentChar: Char = '\n'

    fun isEmpty() = iterator.isEmpty()

    fun current() = currentChar

    fun peek(): Char? = iterator.firstOrNull()

    fun next(): Char {
        val character = iterator.removeFirst()
        when (character) {
            '\t' -> columnNumber += tabSize
            '\n', '\r' -> {
                lineNumber++
                columnNumber = 0
            }
            else -> columnNumber++
        }
        return character
    }
}

class Lexer(sourceCode: String, tabSize: Int = 4) {
    private val tokens: ArrayDeque<LexerToken> = ArrayDeque()
    private val iterator = CodeIterator(sourceCode, tabSize)

    private fun parseNextToken(): Boolean {
        if (!iterator.isEmpty()) {
            iterator.next()
            skipWhitespace()
            when { // TODO do not check first sign here
                iterator.current().isLetter() || iterator.current() == '_' -> keywordOrIdentifier()
                iterator.current().isDigit() -> numericConstant()
                iterator.current() == '"' -> stringConstant()
                iterator.current() == '#' -> comment()
                else -> operator()
            }
            return true
        }
        return false
    }

    private fun skipWhitespace() {
        while (iterator.current().isWhitespace()) {
            iterator.next()
        }
    }

    private fun keywordOrIdentifier() {
        fun matchKeyword(tokenType: TokenType, identifier: String, keyword: String, value: Any? = null): String? {
            var newIdentifier = identifier
            val keywordIterator = keyword.toQueue()
            while (!keywordIterator.isEmpty() && iterator.peek() == keywordIterator.first()) {
                keywordIterator.removeFirst()
                newIdentifier += iterator.next()
            }
            return if (!keywordIterator.isEmpty() || (iterator.peek()?.isLetter() == true)) {
                newIdentifier
            } else {
                tokens.addLast(LexerToken(tokenType, value))
                null
            }
        }

        var identifier: String? = iterator.current().toString()
        when (iterator.current()) { // TODO dictionary instead of switch
            'B' -> identifier = matchKeyword(TokenType.BoolType,"B", "ool")
            'F' -> identifier = matchKeyword(TokenType.FloatType,"F", "loat")
            'I' -> identifier = matchKeyword(TokenType.IntType,"I", "nt")
            'N' -> identifier = matchKeyword(TokenType.NumberType,"N", "umber")
            'S' -> identifier = matchKeyword(TokenType.StringType,"S", "tring")
            'U' -> identifier = matchKeyword(TokenType.UnitType,"U", "nit")
            'a' -> {
                if (iterator.peek()!!.isLetter()) {
                    identifier += iterator.next()
                    when (iterator.current()) {
                        'n' -> identifier = matchKeyword(TokenType.AndOp, "an", "d")
                        's' -> {
                            if (iterator.peek()?.isLetter() != true) {
                                tokens.addLast(LexerToken(TokenType.CastOp))
                                identifier = null
                            }
                        }
                    }
                }
            }
            'e' -> identifier = matchKeyword(TokenType.Else,"e", "lse")
            'f' -> identifier = matchKeyword(TokenType.BoolConstant,"f", "alse", false)
            'i' -> {
                if (iterator.peek()!!.isLetter()) {
                    identifier += iterator.next()
                    when (iterator.current()) {
                        'f' -> {
                            if (iterator.peek()?.isLetter() != true) {
                                tokens.addLast(LexerToken(TokenType.If))
                                identifier = null
                            }
                        }
                        's' -> {
                            if (iterator.peek()?.isLetter() != true) {
                                tokens.addLast(LexerToken(TokenType.IsOp))
                                identifier = null
                            }
                        }
                    }
                }
            }
            'n' -> identifier = matchKeyword(TokenType.NotOp,"n", "ot")
            'o' -> identifier = matchKeyword(TokenType.OrOp,"o", "r")
            'r' -> identifier = matchKeyword(TokenType.Return,"r", "eturn")
            't' -> identifier = matchKeyword(TokenType.BoolConstant,"t", "rue", true)
            'v' -> identifier = matchKeyword(TokenType.Variable,"v", "ar")
            'w' -> identifier = matchKeyword(TokenType.While,"w", "hile")
        }
        if (identifier != null) {
            while (iterator.peek()?.isLetter() == true || iterator.peek() == '_') {
                identifier += iterator.next()
            }
            tokens.addLast(LexerToken(TokenType.Identifier, identifier))
        }
    }

    private fun numericConstant() {
        var number: String = iterator.current().toString()
        if (iterator.current() != '0') {
            while (iterator.peek()?.isDigit() == true) {
                number += iterator.next()
            }
        }
        if (iterator.peek() == '.') {
            number += iterator.next()
            if (iterator.peek()?.isDigit() != true)
                throw LexisError(iterator.current(), iterator.lineNumber, iterator.columnNumber)
            while (iterator.peek()?.isDigit() == true) {
                number += iterator.next()
            }
            tokens.addLast(LexerToken(TokenType.NumConstant, number.toDouble()))
        } else
            tokens.addLast(LexerToken(TokenType.NumConstant, number.toInt()))
    }

    private fun stringConstant() {
        var stringConstant = ""
        while ((iterator.peek() ?: '"') != '"') {
            iterator.next()
            if (iterator.current() == '\\') {
                if (iterator.isEmpty())
                    throw LexisError(iterator.current(), iterator.lineNumber, iterator.columnNumber) // TODO unknown cause of error
                iterator.next()
            }
            stringConstant += iterator.current()
        }
        if (iterator.isEmpty())
            throw LexisError(iterator.current(), iterator.lineNumber, iterator.columnNumber)
        iterator.next()
        tokens.addLast(LexerToken(TokenType.StringConstant, stringConstant))
    }

    private fun comment() {
        tokens.addLast(LexerToken(TokenType.CommentSign))
        var comment = ""
        while (iterator.peek()?.equals('\n') == false) {
            comment += iterator.next()
        }
        tokens.addLast(LexerToken(TokenType.Comment, comment))
    }

    private fun operator() {
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
            else -> throw LexisError(iterator.current(), iterator.lineNumber, iterator.columnNumber)
        }
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
