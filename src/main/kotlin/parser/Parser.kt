package parser

import lexer.Lexer
import lexer.TokenType
import java.util.*

class LexerIterator(private val lexer: Lexer) {
    private var current = lexer.next()
    
    fun current() = current
    fun next() = lexer.next()
    fun isEmpty() = lexer.isEmpty() // TODO use
}

class Parser(private val iterator: LexerIterator) {
    // Parser RD
    // Parser tworzy drzewo wyprowadzenia dla zdania lub informuje o błędzie
    // Parser buduje strukture obiektów wykorzystywaną w interpretacji
    // typy parserów: zstępujący (w4, 3h), wstępujący (w5, 3h)
    // na wyjściu parsera nie ma być nawiasów
    // na wyjściu nie ma być tokenów, tym bardziej tych z Lexera - spytać czym to ma być w takim razie
    // skonsultować szkielet
    // testy jednostkowe wykorzystujące sekwencje tokenów (czyli konstruktor z sekwencją tokenów)
    // testy integracyjne dopiero ma działać z Lexerem

    private val functions: Vector<Function> = Vector()
    private val typeMap = mapOf(
        TokenType.UnitType to Unit,
        TokenType.IntType to Int,
        TokenType.FloatType to Float,
        TokenType.NumberType to Number,
        TokenType.StringType to String,
        TokenType.BoolType to Boolean,
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
    private fun ifTokenTypeThenConsumeElseThrow(tokenType: TokenType, functionName: String) {
        if (iterator.current().type != tokenType)
            throw ExpectedOtherTokenException(iterator.current(), functionName, tokenType.toString())
        iterator.next()
    }

    // funDeclaration+;
    fun parse(): Program {
        while (parseFunDeclaration()) { }
        if (functions.isEmpty())
            throw ExpectedOtherTokenException(iterator.current(), this::parse.name, "funDeclaration")
        // throw if there are tokens left
        return Program(functions.toTypedArray())
    }

    // Identifier '(' parameters ')' TypeSign Type block;
    private fun parseFunDeclaration(): Boolean {
        if (!isTokenType(TokenType.Identifier))
            return false
        val name = iterator.current().value as String
        iterator.next()

        ifTokenTypeThenConsumeElseThrow(TokenType.LeftParenthesesSign, this::parseFunDeclaration.name)
        val parameters = parseParameters()
        ifTokenTypeThenConsumeElseThrow(TokenType.RightParenthesesSign, this::parseFunDeclaration.name) // TODO this error, as many others, does not need to end in panic; parser might just implicitly assume that it exists, inform the user about it and continue parsing, or even interpreting
        ifTokenTypeThenConsumeElseThrow(TokenType.TypeSign, this::parseFunDeclaration.name)

        val type = parseType().let {
            it ?: throw ExpectedOtherTokenException(iterator.current(), this::parseCastExpression.name, "type")
        }

        val block = parseBlock().let {
            it ?: throw ExpectedOtherTokenException(iterator.current(), this::parseFunDeclaration.name, "block")
        }

        functions.add(Function(name, type, parameters, block))
        return true
    }

    // (typedIdentifier (EnumerationSign typedIdentifier)*)?;
    private fun parseParameters(): Array<TypedIdentifier> {
        val parameters = Vector<TypedIdentifier>()

        parameters.add(parseTypedIdentifier() ?: return parameters.toTypedArray())
        while (isTokenTypeThenConsume(TokenType.EnumerationSign)) {
            parseTypedIdentifier().let {
                parameters.add(it ?: throw ExpectedOtherTokenException(iterator.current(), this::parseParameters.name, "typedIdentifier"))
            }
        }

        return parameters.toTypedArray()
    }

    // Identifier TypeSign Type;
    private fun parseTypedIdentifier(): TypedIdentifier? {
        if (!isTokenType(TokenType.Identifier))
            return null
        val name = iterator.current().value as String
        iterator.next()

        ifTokenTypeThenConsumeElseThrow(TokenType.TypeSign, this::parseTypedIdentifier.name)
        val type = parseType().let {
            it ?: throw ExpectedOtherTokenException(iterator.current(), this::parseCastExpression.name, "type")
        }

        return TypedIdentifier(name, type)
    }

    // '{' (statement | varDeclaration | ifStatement | whileStatement | assignStatement | returnStatement)* '}';
    private fun parseBlock(): Array<Statement>? {
        if (!isTokenTypeThenConsume(TokenType.LeftBraceSign))
            return null

        val block = Vector<Statement>()
        var statement: Statement?
        do {
            statement = parseIfStatement() ?:
                parseWhileStatement() ?:
                parseVarDeclaration() ?:
                parseIdentifierStartedStatement() ?:
                parseReturnStatement()
            statement?.let { block.add(statement) }
        } while (statement != null)
        ifTokenTypeThenConsumeElseThrow(TokenType.RightBraceSign, this::parseBlock.name)
        return block.toTypedArray()
    }

    // If '(' expression ')' block elseStatement?;
    private fun parseIfStatement(): IfStatement? {
        if (!isTokenTypeThenConsume(TokenType.If))
            return null
        ifTokenTypeThenConsumeElseThrow(TokenType.LeftParenthesesSign, this::parseIfStatement.name)
        val condition = parseExpression().let {
            it ?: throw ExpectedOtherTokenException(iterator.current(), this::parseIfStatement.name, "expression")
        }
        ifTokenTypeThenConsumeElseThrow(TokenType.RightParenthesesSign, this::parseIfStatement.name)
        val ifBlock = parseBlock().let {
            it ?: throw ExpectedOtherTokenException(iterator.current(), this::parseIfStatement.name, "block")
        }
        val elseBlock = parseElseStatement()
        return IfStatement(condition, ifBlock, elseBlock)
    }

    // Else block;
    private fun parseElseStatement(): Array<Statement>? {
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
        ifTokenTypeThenConsumeElseThrow(TokenType.LeftParenthesesSign, this::parseWhileStatement.name)
        val condition = parseExpression().let {
            it ?: throw ExpectedOtherTokenException(iterator.current(), this::parseWhileStatement.name, "expression")
        }
        ifTokenTypeThenConsumeElseThrow(TokenType.RightParenthesesSign, this::parseWhileStatement.name)
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
            ifTokenTypeThenConsumeElseThrow(TokenType.EndSign, this::parseIdentifierStartedStatement.name)
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
            ifTokenTypeThenConsumeElseThrow(TokenType.AssignOp, this::parseIdentifierStartedStatement.name)
            val expression = parseExpression().let {
                it ?: throw ExpectedOtherTokenException(iterator.current(), this::parseIdentifierStartedStatement.name, "expression")
            }
            AssignmentStatement(name, expression)
        } else {
            FunctionCallStatement(functionCallExpression)
        }
        ifTokenTypeThenConsumeElseThrow(TokenType.EndSign, this::parseIdentifierStartedStatement.name)
        return statement
    }

    // Return expression EndSign;
    private fun parseReturnStatement(): ReturnStatement? {
        if (!isTokenTypeThenConsume(TokenType.Return)) { return null }
        val expression = parseExpression().let {
            it ?: throw ExpectedOtherTokenException(iterator.current(), this::parseReturnStatement.name, "statement")
        }
        ifTokenTypeThenConsumeElseThrow(TokenType.EndSign, this::parseIdentifierStartedStatement.name)
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
            return NotExpression(parseTypeCheckExpression().let { it ?: return null })
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

    // inverseExpression ((MultiplicationOp | DifferenceOp | ModuloOp) inverseExpression)*
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
            return InverseExpression(parseExponentExpression().let { it ?: return null })
        }
        return parseExponentExpression()
    }

    // castExpression ((ExponentOp | RootOp) castExpression)*;
    private fun parseExponentExpression(): Expression? {
        // TODO remember that this is right-handed first
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
            return parseRestOfFunctionCall(name) ?: Variable(name)
        } else if (isTokenType(listOf(TokenType.StringConstant, TokenType.NumConstant, TokenType.BoolConstant))) {
            // TODO Constant
            return
        } else if (isTokenTypeThenConsume(TokenType.LeftParenthesesSign)) {
            val expression = parseExpression().let {
                it ?: throw ExpectedOtherTokenException(iterator.current(), this::parseExpressionPiece.name, "expression")
            }
            ifTokenTypeThenConsumeElseThrow(TokenType.RightParenthesesSign, this::parseExpressionPiece.name)
            return expression
        } else
            return null
    }

    // '(' arguments ')' (MemberOfSign Identifier '(' arguments ')')*;
    private fun parseRestOfFunctionCall(name: String): FunctionCallExpression? {
        if (!isTokenTypeThenConsume(TokenType.LeftParenthesesSign)) { return null }
        var arguments = parseArguments()
        ifTokenTypeThenConsumeElseThrow(TokenType.RightParenthesesSign, this::parseRestOfFunctionCall.name)
        var functionCallExpression = FunctionCallExpression(name, arguments)
        while (isTokenTypeThenConsume(TokenType.MemberReferenceSign)) {
            if (!isTokenType(TokenType.Identifier))
                throw ExpectedOtherTokenException(iterator.current(), this::parseRestOfFunctionCall.name, "identifier")
            val newName = iterator.current().value as String
            iterator.next()
            ifTokenTypeThenConsumeElseThrow(TokenType.LeftParenthesesSign, this::parseRestOfFunctionCall.name)
            arguments = parseArguments()
            ifTokenTypeThenConsumeElseThrow(TokenType.RightParenthesesSign, this::parseRestOfFunctionCall.name)
            functionCallExpression = FunctionCallExpression(newName, (arguments.toList() + functionCallExpression).toTypedArray())
        }
        return functionCallExpression
    }

    // (expression (EnumerationSign expression)*)?;
    private fun parseArguments(): Array<Expression> {
        val arguments = Vector<Expression>()
        arguments.add(parseExpression().let {
            it ?: return arguments.toTypedArray()
        })
        while (isTokenTypeThenConsume(TokenType.EnumerationSign)) {
            arguments.add(parseExpression().let {
                it ?: throw ExpectedOtherTokenException(iterator.current(), this::parseArguments.name, "expression")
            })
        }
        return arguments.toTypedArray()
    }

    private fun parseType(): Any? {
        if (!iterator.current().type.isType())
            return null
        val type = typeMap[iterator.current().type]!!
        iterator.next()
        return type
    }
}