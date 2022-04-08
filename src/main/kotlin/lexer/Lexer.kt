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
    private var lineNumber = 0
    private var columnNumber = 0

    init {
        sourceCode.forEach { iterator.addLast(it) }
        var character: Char
        while (!iterator.isEmpty()) {
            character = iterator.removeFirst()
            when (character) {
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
                        character.isLetter() -> keywordOrIdentifier(character)
                        character.isDigit() -> numericConstant(character)
                        character == '"' -> stringConstant(character)
                        else -> operator(character)
                    }
                }
            }
        }
    }

    private fun keywordOrIdentifier(char: Char) {
        TODO()
    }

    private fun numericConstant(char: Char) {
        TODO()
    }

    private fun stringConstant(char: Char) {
        TODO()
    }

    private fun operator(char: Char) {
        fun assignOperator(normalTokenType: TokenType, assignTokenType: TokenType) {
            if (iterator.first() == '=') {
                iterator.removeFirst()
                columnNumber++
                tokens.addLast(LexerToken(assignTokenType, null))
            } else
                tokens.addLast(LexerToken(normalTokenType, null))
        }

        when (char) {
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
            else -> throw LexisError(char, lineNumber, columnNumber)
        }
    }

    fun next(): Boolean {
        return tokens.removeFirstOrNull() != null
    }

    fun peek(): LexerToken {
        return tokens.first()
    }
}
