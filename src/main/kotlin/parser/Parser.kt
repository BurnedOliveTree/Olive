package parser

import lexer.Lexer
import lexer.LexerToken
import lexer.TokenType
import kotlin.reflect.KClass

abstract class BaseLexerIterator {
    abstract fun current(): LexerToken
    abstract fun next(): LexerToken
}

class LexerIterator(private val lexer: Lexer): BaseLexerIterator() {
    private var current = lexer.next()
    
    override fun current() = current
    override fun next(): LexerToken {
        current = lexer.next()
        while (current.type == TokenType.Comment)
            current = lexer.next()
        return current
    }
}

class LexerTokenIterator(iterable: Iterable<LexerToken>): BaseLexerIterator() {
    private val iterator = iterable.iterator()
    private var current = iterator.next()

    override fun current() = current
    override fun next(): LexerToken {
        current = if (iterator.hasNext())
            iterator.next()
        else
            LexerToken(TokenType.End, null, -1, -1)
        return current
    }
}

class Parser(private val iterator: BaseLexerIterator) {
    // Parser RD

    private val exceptions = mutableListOf<SyntaxError>()
    private val typeMap = mapOf(
        TokenType.UnitType to Unit::class,
        TokenType.IntType to Int::class,
        TokenType.FloatType to Double::class,
        TokenType.NumberType to Number::class,
        TokenType.StringType to String::class,
        TokenType.BoolType to Boolean::class,
    )

    private fun isTokenType(tokenType: TokenType) = iterator.current().type == tokenType
    private fun isTokenType(tokenTypes: List<TokenType>) = iterator.current().type in tokenTypes
    private fun isTokenTypeThenConsume(tokenType: TokenType): Boolean {
        return if (!isTokenType(tokenType)) {
            false
        } else {
            iterator.next()
            true
        }
    }
    private fun isTokenTypeThenConsume(tokenTypes: List<TokenType>): Boolean {
        return if (!isTokenType(tokenTypes)) {
            false
        } else {
            iterator.next()
            true
        }
    }
    private fun ifTokenTypeThenConsumeElseException(tokenType: TokenType, functionName: String) {
        if (iterator.current().type != tokenType)
            exceptions.add(ExpectedOtherTokenException(iterator.current(), functionName, tokenType.toString()))
        iterator.next()
    }
    fun getException() = exceptions

    // funDeclaration+;
    fun parse(): Program {
        val functions = mutableListOf<Function>()
        var function: Function? = parseFunDeclaration()
            ?: throw ExpectedOtherTokenException(iterator.current(), this::parse.name, "funDeclaration")
        while (function != null) {
            functions.add(function)
            function = parseFunDeclaration()
        }
        if (iterator.next().type != TokenType.End)
            throw ExpectedOtherTokenException(iterator.current(), this::parse.name, "endOfText")
        return Program(functions.toList(), exceptions.toList())
    }

    // Identifier '(' parameters ')' TypeSign Type block;
    private fun parseFunDeclaration(): Function? {
        if (!isTokenType(TokenType.Identifier))
            return null
        val name = iterator.current().value as String
        iterator.next()

        ifTokenTypeThenConsumeElseException(TokenType.LeftParenthesesSign, this::parseFunDeclaration.name)
        val parameters = parseParameters()
        ifTokenTypeThenConsumeElseException(TokenType.RightParenthesesSign, this::parseFunDeclaration.name)
        ifTokenTypeThenConsumeElseException(TokenType.TypeSign, this::parseFunDeclaration.name)

        val type = parseType().let {
            it ?: throw ExpectedOtherTokenException(iterator.current(), this::parseCastExpression.name, "type")
        }

        val block = parseBlock().let {
            it ?: throw ExpectedOtherTokenException(iterator.current(), this::parseFunDeclaration.name, "block")
        }

        return Function(name, type, parameters, block)
    }

    // (typedIdentifier (EnumerationSign typedIdentifier)*)?;
    private fun parseParameters(): List<TypedIdentifier> {
        val parameters = mutableListOf<TypedIdentifier>()

        parameters.add(parseTypedIdentifier() ?: return parameters.toList())
        while (isTokenTypeThenConsume(TokenType.EnumerationSign)) {
            parseTypedIdentifier().let {
                parameters.add(it ?: throw ExpectedOtherTokenException(iterator.current(), this::parseParameters.name, "typedIdentifier"))
            }
        }

        return parameters.toList()
    }

    // Identifier TypeSign Type;
    private fun parseTypedIdentifier(): TypedIdentifier? {
        if (!isTokenType(TokenType.Identifier))
            return null
        val name = iterator.current().value as String
        iterator.next()

        ifTokenTypeThenConsumeElseException(TokenType.TypeSign, this::parseTypedIdentifier.name)
        val type = parseType().let {
            it ?: throw ExpectedOtherTokenException(iterator.current(), this::parseCastExpression.name, "type")
        }

        return TypedIdentifier(name, type)
    }

    // '{' (idStartedStatement | varDeclaration | ifStatement | whileStatement | returnStatement)* '}';
    private fun parseBlock(): List<Statement>? {
        if (!isTokenTypeThenConsume(TokenType.LeftBraceSign))
            return null

        val block = mutableListOf<Statement>()
        var statement: Statement?
        do {
            statement = parseIfStatement() ?:
                parseWhileStatement() ?:
                parseVarDeclaration() ?:
                parseIdentifierStartedStatement() ?:
                parseReturnStatement()
            statement?.let { block.add(statement) }
        } while (statement != null)
        ifTokenTypeThenConsumeElseException(TokenType.RightBraceSign, this::parseBlock.name)
        return block.toList()
    }

    // If '(' expression ')' block elseStatement?;
    private fun parseIfStatement(): IfStatement? {
        if (!isTokenTypeThenConsume(TokenType.If))
            return null
        ifTokenTypeThenConsumeElseException(TokenType.LeftParenthesesSign, this::parseIfStatement.name)
        val condition = parseExpression().let {
            it ?: throw ExpectedOtherTokenException(iterator.current(), this::parseIfStatement.name, "expression")
        }
        ifTokenTypeThenConsumeElseException(TokenType.RightParenthesesSign, this::parseIfStatement.name)
        val ifBlock = parseBlock().let {
            it ?: throw ExpectedOtherTokenException(iterator.current(), this::parseIfStatement.name, "block")
        }
        val elseBlock = parseElseStatement()
        return IfStatement(condition, ifBlock, elseBlock)
    }

    // Else block;
    private fun parseElseStatement(): List<Statement>? {
        if (!isTokenTypeThenConsume(TokenType.Else))
            return null
        val block = parseBlock().let {
            it ?: throw ExpectedOtherTokenException(iterator.current(), this::parseElseStatement.name, "block")
        }
        return block
    }

    // While '(' expression ')' block;
    private fun parseWhileStatement(): WhileStatement? {
        if (!isTokenTypeThenConsume(TokenType.While))
            return null
        ifTokenTypeThenConsumeElseException(TokenType.LeftParenthesesSign, this::parseWhileStatement.name)
        val condition = parseExpression().let {
            it ?: throw ExpectedOtherTokenException(iterator.current(), this::parseWhileStatement.name, "expression")
        }
        ifTokenTypeThenConsumeElseException(TokenType.RightParenthesesSign, this::parseWhileStatement.name)
        val block = parseBlock().let {
            it ?: throw ExpectedOtherTokenException(iterator.current(), this::parseWhileStatement.name, "block")
        }
        return WhileStatement(condition, block)
    }

    // Variable typedIdentifier (NormalAssignOp expression)? EndSign;
    private fun parseVarDeclaration(): VarDeclarationStatement? {
        if (!isTokenTypeThenConsume(TokenType.Variable))
            return null
        val typedIdentifier = parseTypedIdentifier().let {
            it ?: throw ExpectedOtherTokenException(iterator.current(), this::parseVarDeclaration.name, "typedIdentifier")
        }
        if (isTokenTypeThenConsume(TokenType.NormalAssignOp)) {
            val expression = parseExpression().let {
                it ?: throw ExpectedOtherTokenException(iterator.current(), this::parseVarDeclaration.name, "statement")
            }
            ifTokenTypeThenConsumeElseException(TokenType.EndSign, this::parseIdentifierStartedStatement.name)
            return VarDeclarationStatement(typedIdentifier.name, typedIdentifier.type, expression)
        } else if (isTokenTypeThenConsume(TokenType.EndSign)) {
            return VarDeclarationStatement(typedIdentifier.name, typedIdentifier.type, null)
        }
        throw ExpectedOtherTokenException(iterator.current(), this::parseVarDeclaration.name, "NormalAssignOperator or StatementEndSign")
    }

    // Identifier (restOfFunCall | (AssignOp expression)) EndSign;
    private fun parseIdentifierStartedStatement(): Statement? {
        if (!isTokenType(TokenType.Identifier))
            return null
        val name = iterator.current().value as String
        iterator.next()

        val functionCallExpression = parseRestOfFunctionCall(name)
        val statement = if (functionCallExpression == null) {
            parseRestOfAssignmentStatement(name).let {
                it ?: throw ExpectedOtherTokenException(iterator.current(), this::parseIdentifierStartedStatement.name, "assignOperator")
            }
        } else {
            FunctionCallStatement(functionCallExpression)
        }
        ifTokenTypeThenConsumeElseException(TokenType.EndSign, this::parseIdentifierStartedStatement.name)
        return statement
    }

    private fun parseRestOfAssignmentStatement(name: String): Statement? {
        if (!iterator.current().type.isAssignOp())
            return null
        val operator = iterator.current().type
        iterator.next()
        val expression = parseExpression().let {
            it ?: throw ExpectedOtherTokenException(iterator.current(), this::parseIdentifierStartedStatement.name, "expression")
        }
        return when (operator) {
            TokenType.NormalAssignOp -> NormalAssignmentStatement(VariableReference(name), expression)
            TokenType.ReferenceAssignOp -> ReferenceAssignmentStatement(VariableReference(name), expression)
            TokenType.SumAssignOp -> SumAssignmentStatement(VariableReference(name), expression)
            TokenType.DifferenceAssignOp -> DifferenceAssignmentStatement(VariableReference(name), expression)
            TokenType.MultiplicationAssignOp -> MultiplicationAssignmentStatement(VariableReference(name), expression)
            TokenType.DivisionAssignOp -> DivisionAssignmentStatement(VariableReference(name), expression)
            TokenType.ModuloAssignOp -> ModuloAssignmentStatement(VariableReference(name), expression)
            TokenType.ExponentAssignOp -> ExponentAssignmentStatement(VariableReference(name), expression)
            TokenType.RootAssignOp -> RootAssignmentStatement(VariableReference(name), expression)
            else -> throw IllegalStateException()
        }
    }

    // Return expression EndSign;
    private fun parseReturnStatement(): ReturnStatement? {
        if (!isTokenTypeThenConsume(TokenType.Return)) { return null }
        val expression = parseExpression().let {
            it ?: throw ExpectedOtherTokenException(iterator.current(), this::parseReturnStatement.name, "statement")
        }
        ifTokenTypeThenConsumeElseException(TokenType.EndSign, this::parseIdentifierStartedStatement.name)
        return ReturnStatement(expression)
    }

    // orExpression;
    private fun parseExpression(): Expression? {
        return parseOrExpression()
    }

    // andExpression (OrOp andExpression)*;
    private fun parseOrExpression(): Expression? {
        var left = parseAndExpression().let {
            it ?: return null
        }
        while (isTokenTypeThenConsume(TokenType.OrOp)) {
            val right = parseAndExpression().let {
                it ?: throw ExpectedOtherTokenException(iterator.current(), this::parseOrExpression.name, "expression")
            }
            left = OrExpression(left, right)
        }
        return left
    }

    // equalExpression (AndOp equalExpression)*;
    private fun parseAndExpression(): Expression? {
        var left = parseEqualExpression().let {
            it ?: return null
        }
        while (isTokenTypeThenConsume(TokenType.AndOp)) {
            val right = parseEqualExpression().let {
                it ?: throw ExpectedOtherTokenException(iterator.current(), this::parseEqualExpression.name, "expression")
            }
            left = AndExpression(left, right)
        }
        return left
    }

    // notExpression ((NormalComparOp | ReferenceComparOp) notExpression)?;
    private fun parseEqualExpression(): Expression? {
        var left = parseNotExpression().let {
            it ?: return null
        }
        if (isTokenType(listOf(TokenType.NormalComparisonOp, TokenType.ReferenceComparisonOp))) {
            val operation = iterator.current().type
            iterator.next()
            val right = parseNotExpression().let {
                it ?: throw ExpectedOtherTokenException(iterator.current(), this::parseEqualExpression.name, "expression")
            }
            left = when (operation) {
                TokenType.NormalComparisonOp -> NormalComparisonExpression(left, right)
                TokenType.ReferenceComparisonOp -> ReferenceComparisonExpression(left, right)
                else -> throw IllegalStateException()
            }
        }
        return left
    }

    // NotOp? typeCheckExpression;
    private fun parseNotExpression(): Expression? {
        if (isTokenTypeThenConsume(TokenType.NotOp)) {
            return NotExpression(parseTypeCheckExpression().let {
                it ?: throw ExpectedOtherTokenException(iterator.current(), this::parseNotExpression.name, "expression")
            })
        }
        return parseTypeCheckExpression()
    }

    // compareExpression (IsOp Type)?;
    private fun parseTypeCheckExpression(): Expression? {
        var expression = parseCompareExpression().let { it ?: return null }
        if (isTokenTypeThenConsume(TokenType.IsOp)) {
            val type = parseType().let {
                it ?: throw ExpectedOtherTokenException(iterator.current(), this::parseCastExpression.name, "type")
            }
            expression = TypeCheckExpression(expression, type)
        }
        return expression
    }

    // addExpression ((LesserThanOp | LesserOrEqualOp | GreaterThanOp | GreaterOrEqualOp) addExpression)?;
    private fun parseCompareExpression(): Expression? {
        var left = parseAddExpression().let {
            it ?: return null
        }
        if (isTokenType(listOf(TokenType.LesserThanOp, TokenType.LesserOrEqualOp, TokenType.GreaterThanOp, TokenType.GreaterOrEqualOp))) {
            val operation = iterator.current().type
            iterator.next()
            val right = parseAddExpression().let {
                it ?: throw ExpectedOtherTokenException(iterator.current(), this::parseCompareExpression.name, "expression")
            }
            left = when (operation) {
                TokenType.LesserThanOp -> LesserThanExpression(left, right)
                TokenType.LesserOrEqualOp -> LesserOrEqualExpression(left, right)
                TokenType.GreaterThanOp -> GreaterThanExpression(left, right)
                TokenType.GreaterOrEqualOp -> GreaterOrEqualExpression(left, right)
                else -> throw IllegalStateException()
            }
        }
        return left
    }

    // multiplyExpression ((SumOp | DifferenceOp) multiplyExpression)*;
    private fun parseAddExpression(): Expression? {
        var left = parseMultiplyExpression().let {
            it ?: return null
        }
        while (isTokenType(listOf(TokenType.SumOp, TokenType.DifferenceOp))) {
            val operation = iterator.current().type
            iterator.next()
            val right = parseMultiplyExpression().let {
                it ?: throw ExpectedOtherTokenException(iterator.current(), this::parseAddExpression.name, "expression")
            }
            left = when (operation) {
                TokenType.SumOp -> AddExpression(left, right)
                TokenType.DifferenceOp -> SubtractExpression(left, right)
                else -> throw IllegalStateException()
            }
        }
        return left
    }

    // inverseExpression ((MultiplicationOp | DivisionOp | ModuloOp) inverseExpression)*
    private fun parseMultiplyExpression(): Expression? {
        var left = parseInverseExpression().let {
            it ?: return null
        }
        while (isTokenType(listOf(TokenType.MultiplicationOp, TokenType.DivisionOp, TokenType.ModuloOp))) {
            val operation = iterator.current().type
            iterator.next()
            val right = parseInverseExpression().let {
                it ?: throw ExpectedOtherTokenException(iterator.current(), this::parseMultiplyExpression.name, "expression")
            }
            left = when (operation) {
                TokenType.MultiplicationOp -> MultiplyExpression(left, right)
                TokenType.DivisionOp -> DivideExpression(left, right)
                TokenType.ModuloOp -> ModuloExpression(left, right)
                else -> throw IllegalStateException()
            }
        }
        return left
    }

    // DifferenceOp? exponentExpression;
    private fun parseInverseExpression(): Expression? {
        if (isTokenTypeThenConsume(TokenType.DifferenceOp)) {
            return InverseExpression(parseExponentExpression().let {
                it ?: throw ExpectedOtherTokenException(iterator.current(), this::parseInverseExpression.name, "expression")
            })
        }
        return parseExponentExpression()
    }

    // castExpression ((ExponentOp | RootOp) exponentExpression)*;
    private fun parseExponentExpression(): Expression? {
        var left = parseCastExpression().let {
            it ?: return null
        }
        if (isTokenType(listOf(TokenType.ExponentOp, TokenType.RootOp))) {
            val operation = iterator.current().type
            iterator.next()
            val right = parseExponentExpression().let {
                it ?: throw ExpectedOtherTokenException(iterator.current(), this::parseExponentExpression.name, "expression")
            }
            left = when (operation) {
                TokenType.ExponentOp -> ExponentExpression(left, right)
                TokenType.RootOp -> RootExpression(left, right)
                else -> throw IllegalStateException()
            }
        }
        return left
    }

    // expressionPiece (CastOp Type)?;
    private fun parseCastExpression(): Expression? {
        var expression = parseExpressionPiece().let {
            it ?: return null
        }
        if (isTokenTypeThenConsume(TokenType.CastOp)) {
            val type = parseType().let {
                it ?: throw ExpectedOtherTokenException(iterator.current(), this::parseCastExpression.name, "type")
            }
            expression = CastExpression(expression, type)
        }
        return expression
    }

    // (Identifier restOfFunCall?) | Constant | ('(' expression ')');
    private fun parseExpressionPiece(): Expression? {
        if (isTokenType(TokenType.Identifier)) {
            val name = iterator.current().value as String
            iterator.next()
            return parseRestOfFunctionCall(name) ?: VariableReference(name)
        } else if (iterator.current().type.isConstant()) {
            val constant = when (iterator.current().type) {
                TokenType.BoolConstant -> BoolConstant(iterator.current().value as Boolean)
                TokenType.FloatConstant -> FloatConstant(iterator.current().value as Double)
                TokenType.IntConstant -> IntConstant(iterator.current().value as Int)
                TokenType.StringConstant -> StringConstant(iterator.current().value as String)
                else -> throw IllegalStateException()
            }
            iterator.next()
            return constant
        } else if (isTokenTypeThenConsume(TokenType.LeftParenthesesSign)) {
            val expression = parseExpression().let {
                it ?: throw ExpectedOtherTokenException(iterator.current(), this::parseExpressionPiece.name, "expression")
            }
            ifTokenTypeThenConsumeElseException(TokenType.RightParenthesesSign, this::parseExpressionPiece.name)
            return expression
        } else
            return null
    }

    // (('(' arguments ')') chainFunCall*) | chainFunCall+;
    private fun parseRestOfFunctionCall(name: String): FunctionCallExpression? {
        var functionCallExpression = if (isTokenTypeThenConsume(TokenType.LeftParenthesesSign)) {
            val arguments = parseArguments()
            ifTokenTypeThenConsumeElseException(TokenType.RightParenthesesSign, this::parseRestOfFunctionCall.name)
            FunctionCallExpression(name, arguments)
        } else {
            if (isTokenTypeThenConsume(TokenType.MemberReferenceSign))
                parseChainCall(VariableReference(name))
                    ?: throw ExpectedOtherTokenException(iterator.current(), this::parseRestOfFunctionCall.name, "identifier")
            else
                return null
        }
        while (isTokenTypeThenConsume(TokenType.MemberReferenceSign)) {
            functionCallExpression = parseChainCall(functionCallExpression)
                ?: throw ExpectedOtherTokenException(iterator.current(), this::parseRestOfFunctionCall.name, "identifier")
        }
        return functionCallExpression
    }

    // (MemberOfSign Identifier '(' arguments ')');
    private fun parseChainCall(expression: Expression): FunctionCallExpression? {
        if (!isTokenType(TokenType.Identifier))
            return null
        val newName = iterator.current().value as String
        iterator.next()
        ifTokenTypeThenConsumeElseException(TokenType.LeftParenthesesSign, this::parseChainCall.name)
        val arguments = parseArguments()
        ifTokenTypeThenConsumeElseException(TokenType.RightParenthesesSign, this::parseChainCall.name)
        return FunctionCallExpression(newName, (listOf(expression) + arguments.toList()).toList())
    }

    // (expression (EnumerationSign expression)*)?;
    private fun parseArguments(): List<Expression> {
        val arguments = mutableListOf<Expression>()
        arguments.add(parseExpression().let {
            it ?: return arguments.toList()
        })
        while (isTokenTypeThenConsume(TokenType.EnumerationSign)) {
            arguments.add(parseExpression().let {
                it ?: throw ExpectedOtherTokenException(iterator.current(), this::parseArguments.name, "expression")
            })
        }
        return arguments.toList()
    }

    private fun parseType(): KClass<out Any>? {
        if (!iterator.current().type.isType())
            return null
        val type = typeMap[iterator.current().type]!!
        iterator.next()
        return type
    }
}