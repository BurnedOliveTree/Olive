package lexer

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

    Variable,
    If,
    Else,
    While,
    Return,

    Identifier,
    Constant,
    AssignOp,
    Comment,
    WhiteSpace;

    fun isConstant(): Boolean =
        this in listOf(BoolConstant, NumConstant, StringConstant)

    fun isType(): Boolean =
        this in listOf(UnitType, IntType, FloatType, NumberType, StringType, BoolType)

    fun isAssignOp(): Boolean =
        this in listOf(NormalAssignOp, ReferenceAssignOp, SumAssignOp, DifferenceAssignOp, MultiplicationAssignOp, ExponentAssignOp, DivisionAssignOp, RootAssignOp, ModuloAssignOp)
}

data class LexerToken(val type: TokenType, val value: Any?)

class Lexer(sourceCode: String) {
    private val tokens: ArrayDeque<LexerToken> = ArrayDeque()
    private val iterator: ArrayDeque<Char> = ArrayDeque()
    private var currentChar: Char = '\n'
    private var lineNumber = 0
    private var columnNumber = 0

    init {
        sourceCode.forEach { iterator.addLast(it) }
        while (!iterator.isEmpty()) {
            currentChar = iterator.removeFirst()
            when (currentChar) {
                ' ' -> columnNumber++
                '\t' -> columnNumber += 4
                '\n' -> {
                    lineNumber++
                    columnNumber = 0
                }
                // TODO: handle comment
                else -> {
                    columnNumber++
                    when {
                        currentChar.isLetter() -> keywordOrIdentifier()
                        currentChar.isDigit() -> numericConstant()
                        currentChar == '"' -> stringConstant()
                        else -> operator()
                    }
                }
            }
        }
    }

    private fun keywordOrIdentifier() {
        TODO()
    }

    private fun numericConstant() {
        var number: String = currentChar.toString()
        if (currentChar != '0') {
            while (iterator.first().isDigit()) {
                currentChar = iterator.removeFirst()
                number += currentChar
                columnNumber++
            }
        }
        if (iterator.first() == '.') {
            currentChar = iterator.removeFirst()
            number += currentChar
            columnNumber++
            if (!iterator.first().isDigit())
                throw LexisError(currentChar, lineNumber, columnNumber)
            while (iterator.first().isDigit()) {
                currentChar = iterator.removeFirst()
                number += currentChar
                columnNumber++
            }
            tokens.addLast(LexerToken(TokenType.NumConstant, number.toDouble()))
        } else
            tokens.addLast(LexerToken(TokenType.NumConstant, number.toInt()))
    }

    private fun stringConstant() {
        TODO()
    }

    private fun operator() {
        fun assignOperator(normalTokenType: TokenType, assignTokenType: TokenType) {
            if (iterator.first() == '=') {
                currentChar = iterator.removeFirst()
                columnNumber++
                tokens.addLast(LexerToken(assignTokenType, null))
            } else
                tokens.addLast(LexerToken(normalTokenType, null))
        }

        when (currentChar) {
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
            '&' -> TODO()
            ':' -> tokens.addLast(LexerToken(TokenType.TypeSign, null))
            ';' -> tokens.addLast(LexerToken(TokenType.EndSign, null))
            ',' -> tokens.addLast(LexerToken(TokenType.EnumerationSign, null))
            '.' -> tokens.addLast(LexerToken(TokenType.MemberReferenceSign, null))
            else -> throw LexisError(currentChar, lineNumber, columnNumber)
        }
    }

    fun next(): Boolean {
        return tokens.removeFirstOrNull() != null
    }

    fun peek(): LexerToken {
        return tokens.first()
    }
}
