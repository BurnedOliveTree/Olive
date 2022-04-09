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
    Comment;

    fun isConstant(): Boolean =
        this in listOf(BoolConstant, NumConstant, StringConstant)

    fun isType(): Boolean =
        this in listOf(UnitType, IntType, FloatType, NumberType, StringType, BoolType)

    fun isAssignOp(): Boolean =
        this in listOf(NormalAssignOp, ReferenceAssignOp, SumAssignOp, DifferenceAssignOp, MultiplicationAssignOp, ExponentAssignOp, DivisionAssignOp, RootAssignOp, ModuloAssignOp)
}

data class LexerToken(val type: TokenType, val value: Any? = null)

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
                        currentChar.isLetter() || currentChar == '_' -> keywordOrIdentifier()
                        currentChar.isDigit() -> numericConstant()
                        currentChar == '"' -> stringConstant()
                        else -> operator()
                    }
                }
            }
        }
    }

    private fun keywordOrIdentifier() {
        fun matchKeyword(tokenType: TokenType, identifier: String, keyword: String, value: Any? = null): String? {
            val keywordIterator = keyword.asSequence().iterator()
            while (keywordIterator.hasNext() && iterator.first() == keywordIterator.next()) {
                currentChar = iterator.removeFirst()
                columnNumber++
            }
            return if (keywordIterator.hasNext()) {
                identifier + keywordIterator
            } else {
                tokens.addLast(LexerToken(tokenType, value))
                null
            }
        }

        var identifier: String? = currentChar.toString()
        when (currentChar) {
            'B' -> identifier = matchKeyword(TokenType.BoolType,"B", "ool")
            'F' -> identifier = matchKeyword(TokenType.FloatType,"F", "loat")
            'I' -> identifier = matchKeyword(TokenType.IntType,"I", "nt")
            'N' -> identifier = matchKeyword(TokenType.NumberType,"N", "umber")
            'S' -> identifier = matchKeyword(TokenType.StringType,"S", "tring")
            'U' -> identifier = matchKeyword(TokenType.UnitType,"U", "nit")
            'a' -> {
                if (iterator.first().isLetter()) {
                    currentChar = iterator.removeFirst()
                    identifier += currentChar
                    columnNumber++
                    when (currentChar) {
                        'n' -> identifier = matchKeyword(TokenType.AndOp, "an", "d")
                        's' -> {
                            tokens.addLast(LexerToken(TokenType.CastOp))
                            identifier = null
                        }
                    }
                }
            }
            'e' -> identifier = matchKeyword(TokenType.Else,"e", "lse")
            'f' -> identifier = matchKeyword(TokenType.BoolConstant,"f", "alse", false)
            'i' -> {
                if (iterator.first().isLetter()) {
                    currentChar = iterator.removeFirst()
                    identifier += currentChar
                    columnNumber++
                    when (currentChar) {
                        'f' -> {
                            tokens.addLast(LexerToken(TokenType.If))
                            identifier = null
                        }
                        's' -> {
                            tokens.addLast(LexerToken(TokenType.IsOp))
                            identifier = null
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
            // TODO fix this being too greedy, will probably need to change the logic with whitespaces
            while (iterator.first().isLetter() || iterator.first() == '_') {
                currentChar = iterator.removeFirst()
                identifier += currentChar
                columnNumber++
            }
            tokens.addLast(LexerToken(TokenType.Identifier, identifier))
        }
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
                tokens.addLast(LexerToken(assignTokenType))
            } else
                tokens.addLast(LexerToken(normalTokenType))
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
            '&' -> {
                if (iterator.first() == '=') {
                    currentChar = iterator.removeFirst()
                    columnNumber++
                    assignOperator(TokenType.ReferenceAssignOp, TokenType.ReferenceComparisonOp)
                } else {
                    throw LexisError(currentChar, lineNumber, columnNumber)
                }
            }
            ':' -> tokens.addLast(LexerToken(TokenType.TypeSign))
            ';' -> tokens.addLast(LexerToken(TokenType.EndSign))
            ',' -> tokens.addLast(LexerToken(TokenType.EnumerationSign))
            '.' -> tokens.addLast(LexerToken(TokenType.MemberReferenceSign))
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
