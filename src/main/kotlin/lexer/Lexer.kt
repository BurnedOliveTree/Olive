package lexer

import kotlin.collections.ArrayDeque

internal fun List<TokenType>.toRegExAlteration() =
    this.joinToString(separator = " | ") { "${it.regex}" }

enum class TokenType(val regex: Regex) {
    StringConstant(Regex("\" ('\\\\' | ~[\\\\\"]) \"")),
    NumConstant(Regex("(([1-9][0-9]*) | [0]) ('.' [0-9]+)?")),
    BoolConstant(Regex("true | false")),

    UnitType(Regex("Unit")),
    IntType(Regex("Int")),
    FloatType(Regex("Float")),
    NumberType(Regex("Number")),
    StringType(Regex("String")),
    BoolType(Regex("Bool")),

    SumOp(Regex("\\+")),
    DifferenceOp(Regex("-")),
    MultiplicationOp(Regex("\\*")),
    ExponentOp(Regex("\\^")),
    DivisionOp(Regex("/")),
    RootOp(Regex("\\|")),
    ModuloOp(Regex("%")),

    NotOp(Regex("not")),
    AndOp(Regex("and")),
    OrOp(Regex("or")),
    IsOp(Regex("is")),
    CastOp(Regex("as")),

    NormalAssignOp(Regex("=")),
    ReferenceAssignOp(Regex("&=")),
    SumAssignOp(Regex("\\+=")),
    DifferenceAssignOp(Regex("-=")),
    MultiplicationAssignOp(Regex("\\*=")),
    ExponentAssignOp(Regex("\\^=")),
    DivisionAssignOp(Regex("/=")),
    RootAssignOp(Regex("\\|=")),
    ModuloAssignOp(Regex("%=")),

    NormalComparisonOp(Regex("==")),
    ReferenceComparisonOp(Regex("&==")),
    LesserThanOp(Regex("<")),
    LesserOrEqualOp(Regex("<=")),
    GreaterThanOp(Regex(">")),
    GreaterOrEqualOp(Regex(">=")),

    TypeSign(Regex(":")),
    CommentSign(Regex("#")),
    EndSign(Regex(".")),
    EnumerationSign(Regex(",")),
    MemberReferenceSign(Regex(".")),

    Variable(Regex("var")),
    If(Regex("if")),
    Else(Regex("else")),
    While(Regex("while")),
    Return(Regex("return")),

    Identifier(Regex("[A-Za-z_]+")), // TODO change to isLetter or smth
    Constant(Regex(listOf(StringConstant, NumConstant, BoolConstant).toRegExAlteration())),
    Type(Regex(listOf(UnitType, IntType, FloatType, NumberType, StringType, BoolType).toRegExAlteration())),
    AssignOp(Regex(listOf(NormalAssignOp, ReferenceAssignOp, SumAssignOp, DifferenceAssignOp, MultiplicationAssignOp, ExponentAssignOp, DivisionAssignOp, RootAssignOp, ModuloAssignOp).toRegExAlteration())),
    Comment(Regex("${CommentSign.regex} ~(\n)* \n")),
    WhiteSpace(Regex("[ \t\r\n]+"));
}

data class LexerToken(val type: TokenType, val value: Any) {
}

class Lexer(sourceCode: String) {
    private val tokens: ArrayDeque<LexerToken> = ArrayDeque()

    init {
        for (character in sourceCode) {
            println(character)
        }
    }

    fun next(): Boolean {
        return tokens.removeFirstOrNull() != null
    }

    fun peek(): LexerToken {
        return tokens.first()
    }
}