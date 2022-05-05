package lexer

import java.io.*
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

data class LexerToken(val type: TokenType, val value: Any?, val line: Int, val column: Int)

class CodeIterator private constructor (private val tabSize: Int) {
    private lateinit var iterator: Reader

    constructor (file: FileReader, tabSize: Int = 4) : this(tabSize) {
        iterator = file
    }
    constructor (sourceCode: String, tabSize: Int = 4) : this(tabSize) {
        iterator = StringReader(sourceCode)
    }

    var line = 1
    var column = 0
    private var currentChar: Char = '§'
    private var nextChar: Char? = null

    fun isEmpty(): Boolean {
        getNextIfNull()
        return nextChar == null
    }

    fun current() = currentChar

    fun peek(): Char? {
        getNextIfNull()
        return nextChar
    }

    fun next(): Char {
        getNextIfNull()
        currentChar = nextChar!!
        nextChar = null
        when (currentChar) {
            '\t' -> column += tabSize
            '\n' -> {
                line++
                column = 0
            }
            '\r' -> next() // 'r' is being omitted as I don't expect this to run on Commodore, ZX Spectrum, Mac Classic or any other system that has only /r
            else -> column++
        }
        return currentChar
    }

    private fun getNextIfNull() {
        if (nextChar == null) {
            iterator.read().let {
                nextChar = if (it >= 0) { it.toChar() } else { null }
            }
        }
    }
}

class Lexer (
    private val iterator: CodeIterator,
    private val maximalSize: Int = 2048
) {
    private val keywordsMap = mapOf(
        "Bool" to (TokenType.BoolType to null),
        "Float" to (TokenType.FloatType to null),
        "Int" to (TokenType.IntType to null),
        "Number" to (TokenType.NumberType to null),
        "String" to (TokenType.StringType to null),
        "Unit" to (TokenType.UnitType to null),
        "and" to (TokenType.AndOp to null),
        "as" to (TokenType.CastOp to null),
        "else" to (TokenType.Else to null),
        "false" to (TokenType.BoolConstant to false),
        "if" to (TokenType.If to null),
        "is" to (TokenType.IsOp to null),
        "not" to (TokenType.NotOp to null),
        "or" to (TokenType.OrOp to null),
        "return" to (TokenType.Return to null),
        "true" to (TokenType.BoolConstant to true),
        "var" to (TokenType.Variable to null),
        "while" to (TokenType.While to null),
    )
    private val operatorsMap = mapOf(
        "+" to (TokenType.SumOp),
        "+=" to (TokenType.SumAssignOp),
        "-" to (TokenType.DifferenceOp),
        "-=" to (TokenType.DifferenceAssignOp),
        "*" to (TokenType.MultiplicationOp),
        "*=" to (TokenType.MultiplicationAssignOp),
        "/" to (TokenType.DivisionOp),
        "/=" to (TokenType.DivisionAssignOp),
        "^" to (TokenType.ExponentOp),
        "^=" to (TokenType.ExponentAssignOp),
        "|" to (TokenType.RootOp),
        "|=" to (TokenType.RootAssignOp),
        "%" to (TokenType.ModuloOp),
        "%=" to (TokenType.ModuloAssignOp),
        "<" to (TokenType.LesserThanOp),
        "<=" to (TokenType.LesserOrEqualOp),
        ">" to (TokenType.GreaterThanOp),
        ">=" to (TokenType.GreaterOrEqualOp),
        "=" to (TokenType.NormalAssignOp),
        "==" to (TokenType.NormalComparisonOp),
        "&=" to (TokenType.ReferenceAssignOp),
        "&==" to (TokenType.ReferenceComparisonOp),
        ":" to (TokenType.TypeSign),
        ";" to (TokenType.EndSign),
        "," to (TokenType.EnumerationSign),
        "." to (TokenType.MemberReferenceSign),
        "(" to (TokenType.LeftParenthesesSign),
        ")" to (TokenType.RightParenthesesSign),
        "{" to (TokenType.LeftBraceSign),
        "}" to (TokenType.RightBraceSign),
    )
    private val specialMap = mapOf(
        "\\b" to '\b',
        "\\n" to '\n',
        "\\r" to '\r',
        "\\t" to '\t',
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
            throw UnrecognizedSignError(iterator.current(), iterator.line, iterator.column)
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
            return if (it != null)
                LexerToken(it.first, it.second, iterator.line, iterator.column - identifier.length + 1)
            else
                LexerToken(TokenType.Identifier, identifier.toString(), iterator.line, iterator.column - identifier.length + 1)
        }
    }

    private fun numericConstant(): LexerToken? {
        if (!iterator.current().isDigit())
            return null

        var number = iterator.current() - '0'
        if (iterator.current() != '0') {
            while (iterator.peek()?.isDigit() == true) {
                (number * 10 + (iterator.next() - '0')).let {
                    if (it < number)
                        throw TokenTooBigError(iterator.current(), iterator.line, iterator.column)
                    else
                        number = it
                }
            }
        }
        return if (iterator.peek() == '.') {
            var decimalPart = 0
            var digitCounter = 0
            iterator.next()
            if (iterator.peek()?.isDigit() != true)
                throw MissingSignError(iterator.current(), iterator.line, iterator.column, "number")
            while (iterator.peek()?.isDigit() == true) {
                (decimalPart * 10 + (iterator.next() - '0')).let {
                    if (it < decimalPart)
                        throw TokenTooBigError(iterator.current(), iterator.line, iterator.column)
                    else
                        decimalPart = it
                }
                digitCounter++
            }
            LexerToken(TokenType.NumConstant, number + decimalPart / 10.0.pow(digitCounter), iterator.line, iterator.column)
        } else
            LexerToken(TokenType.NumConstant, number, iterator.line, iterator.column)
    }

    private fun stringConstant(): LexerToken? {
        if (iterator.current() != '"')
            return null
        val stringConstant = StringBuilder()
        println(("\\"+"n").length)
        while ((iterator.peek() ?: '"') != '"') {
            if (stringConstant.length >= maximalSize)
                throw TokenTooBigError(iterator.current(), iterator.line, iterator.column)
            iterator.next()
            if (iterator.current() == '\\') {
                if (iterator.isEmpty())
                    throw MissingSignError(iterator.current(), iterator.line, iterator.column, "any sign")
                iterator.next()
                specialMap["\\" + iterator.current()].let {
                    stringConstant.append(it ?: iterator.current())
                }
            } else
                stringConstant.append(iterator.current())
        }
        if (iterator.isEmpty())
            throw MissingSignError(iterator.current(), iterator.line, iterator.column, "\"")
        iterator.next()
        return LexerToken(TokenType.StringConstant, stringConstant.toString(), iterator.line, iterator.column - stringConstant.length + 1)
    }

    private fun comment(): LexerToken? {
        if (iterator.current() != '#')
            return null
        val comment = StringBuilder()
        while (iterator.peek()?.equals('\n') == false) {
            if (comment.length >= maximalSize)
                throw TokenTooBigError(iterator.current(), iterator.line, iterator.column)
            comment.append(iterator.next())
        }
        return LexerToken(TokenType.Comment, comment.toString(), iterator.line, iterator.column)
    }

    private fun operator(): LexerToken? {
        val identifier = StringBuilder().append(iterator.current())
        while (!iterator.isEmpty() && operatorsMap[identifier.toString() + iterator.peek()!!] != null) {
            identifier.append(iterator.next())
        }
        return operatorsMap[identifier.toString()]?.let { LexerToken(it, null, iterator.line, iterator.column) }
    }

    fun next() = parseNextToken()!!

    fun isEmpty() = iterator.isEmpty()
}
