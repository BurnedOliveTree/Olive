package lexer

import kotlin.math.pow

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
    private val iterator = sourceCode.iterator()
    var lineNumber = 1
    var columnNumber = 0
    private var currentChar: Char = '§'
    private var nextChar: Char? = null

    fun isEmpty() = !iterator.hasNext() && nextChar == null

    fun current() = currentChar

    fun peek(): Char? {
        if (nextChar == null && iterator.hasNext())
            nextChar = iterator.nextChar()
        return nextChar
    }

    fun next(): Char {
        if (nextChar == null)
            nextChar = iterator.nextChar()
        currentChar = nextChar!!
        nextChar = null
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
    private val operatorsMap = mapOf(
        "+" to LexerToken(TokenType.SumOp),
        "+=" to LexerToken(TokenType.SumAssignOp),
        "-" to LexerToken(TokenType.DifferenceOp),
        "-=" to LexerToken(TokenType.DifferenceAssignOp),
        "*" to LexerToken(TokenType.MultiplicationOp),
        "*=" to LexerToken(TokenType.MultiplicationAssignOp),
        "/" to LexerToken(TokenType.DivisionOp),
        "/=" to LexerToken(TokenType.DivisionAssignOp),
        "^" to LexerToken(TokenType.ExponentOp),
        "^=" to LexerToken(TokenType.ExponentAssignOp),
        "|" to LexerToken(TokenType.RootOp),
        "|=" to LexerToken(TokenType.RootAssignOp),
        "%" to LexerToken(TokenType.ModuloOp),
        "%=" to LexerToken(TokenType.ModuloAssignOp),
        "<" to LexerToken(TokenType.LesserThanOp),
        "<=" to LexerToken(TokenType.LesserOrEqualOp),
        ">" to LexerToken(TokenType.GreaterThanOp),
        ">=" to LexerToken(TokenType.GreaterOrEqualOp),
        "=" to LexerToken(TokenType.NormalAssignOp),
        "==" to LexerToken(TokenType.NormalComparisonOp),
        "&=" to LexerToken(TokenType.ReferenceAssignOp),
        "&==" to LexerToken(TokenType.ReferenceComparisonOp),
        ":" to LexerToken(TokenType.TypeSign),
        ";" to LexerToken(TokenType.EndSign),
        "," to LexerToken(TokenType.EnumerationSign),
        "." to LexerToken(TokenType.MemberReferenceSign),
        "(" to LexerToken(TokenType.LeftParenthesesSign),
        ")" to LexerToken(TokenType.RightParenthesesSign),
        "{" to LexerToken(TokenType.LeftBraceSign),
        "}" to LexerToken(TokenType.RightBraceSign),
    )

    private fun parseNextToken(): LexerToken? {
        if (!iterator.isEmpty()) {
            iterator.next()
            skipWhitespace()
            keywordOrIdentifier()?.let { return it }
            numericConstant()?.let { return it }
            stringConstant()?.let { return it }
            comment()?.let { return it }
            operator()?.let { return it }
            throw UnrecognizedSignError(iterator.current(), iterator.lineNumber, iterator.columnNumber)
        }
        return null
    }

    private fun skipWhitespace() {
        while (iterator.current().isWhitespace()) {
            iterator.next()
        }
    }

    private fun keywordOrIdentifier(): LexerToken? {
        fun Char.isIdentifierSign() = this.isLetter() || this == '_'

        if (!iterator.current().isIdentifierSign())
            return null

        val identifier = StringBuilder().append(iterator.current())
        while (iterator.peek()?.isIdentifierSign() == true) {
            identifier.append(iterator.next())
        }
        keywordsMap[identifier.toString()].let {
            return it ?: LexerToken(TokenType.Identifier, identifier.toString())
        }
    }

    private fun numericConstant(): LexerToken? {
        if (!iterator.current().isDigit())
            return null

        var number = iterator.current() - '0'
        if (iterator.current() != '0') {
            while (iterator.peek()?.isDigit() == true) {
                number = number * 10 + (iterator.next() - '0')
            }
        }
        return if (iterator.peek() == '.') {
            var decimalPart = 0
            var digitCounter = 0
            iterator.next()
            if (iterator.peek()?.isDigit() != true)
                throw MissingSignError(iterator.current(), iterator.lineNumber, iterator.columnNumber, "number")
            while (iterator.peek()?.isDigit() == true) {
                decimalPart = decimalPart * 10 + (iterator.next() - '0')
                digitCounter++
            }
            LexerToken(TokenType.NumConstant, number + decimalPart / 10.0.pow(digitCounter))
        } else
            LexerToken(TokenType.NumConstant, number)
    }

    private fun stringConstant(): LexerToken? {
        if (iterator.current() != '"')
            return null
        val stringConstant = StringBuilder()
        while ((iterator.peek() ?: '"') != '"') {
            iterator.next()
            if (iterator.current() == '\\') {
                if (iterator.isEmpty())
                    throw MissingSignError(iterator.current(), iterator.lineNumber, iterator.columnNumber, "any sign")
                iterator.next()
            }
            stringConstant.append(iterator.current())
        }
        if (iterator.isEmpty())
            throw MissingSignError(iterator.current(), iterator.lineNumber, iterator.columnNumber, "\"")
        iterator.next()
        return LexerToken(TokenType.StringConstant, stringConstant.toString())
    }

    private fun comment(): LexerToken? {
        if (iterator.current() != '#')
            return null
        val comment = StringBuilder()
        while (iterator.peek()?.equals('\n') == false) {
            comment.append(iterator.next())
        }
        return LexerToken(TokenType.Comment, comment.toString())
    }

    private fun operator(): LexerToken? {
        val identifier = StringBuilder().append(iterator.current())
        while (!iterator.isEmpty() && operatorsMap[identifier.toString() + iterator.peek()!!] != null) {
            identifier.append(iterator.next())
        }
        return operatorsMap[identifier.toString()]
    }

    fun next() = parseNextToken()!!

    fun isEmpty() = iterator.isEmpty()
}
