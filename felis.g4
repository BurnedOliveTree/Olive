grammar felis;

/// constants

StringConstant:     '"' ('\\' | ~[\\"]) '"';
IntConstant:        (([1-9] [0-9]*) | [0]);
FloatConstant:      (([1-9] [0-9]*) | [0]) '.' [0-9]+;
BoolConstant:       'true' | 'false';

/// keywords and operators

UnitType:           'Unit';
IntType:            'Int';
FloatType:          'Float';
NumberType:         'Number';
StringType:         'String';
BoolType:           'Bool';

SumOp:              '+';
DifferenceOp:       '-';
MultiplicationOp:   '*';
ExponentOp:         '^';
DivisionOp:         '/';
RootOp:             '|';
ModuloOp:           '%';

NotOp:              'not';
AndOp:              'and';
OrOp:               'or';
IsOp:               'is';
CastOp:             'as';

NormalAssignOp:     '=';
ReferenceAssignOp:  '&=';
SumAssignOp:        '+=';
DifferenceAssingOp: '-=';
MultiplicationAssingOp: '*=';
ExponentAssignOp:   '^=';
DivisionAssignOp:   '/=';
RootAssignOp:       '|=';
ModuloAssignOp:     '%=';

NormalComparOp:     '==';
ReferenceComparOp:  '&==';
LesserThanOp:       '<';
LesserOrEqualOp:    '<=';
GreaterThanOp:      '>';
GreaterOrEqualOp:   '>=';

TypeSign:           ':';
CommentSign:        '#';
EndSign:            ';';
EnumerationSign:    ',';
MemberOfSign:       '.';

Variable:           'var';
If:                 'if';
Else:               'else';
While:              'while';
Return:             'return';

/// token groups

Identifier:         [A-Za-z_]+; // to be changed to a library function isLetter()
Constant:           StringConstant | IntConstant | FloatConstant | BoolConstant;
Type:               UnitType | IntType | FloatType | NumberType | StringType | BoolType;
AssignOp:           NormalAssignOp | ReferenceAssignOp | SumAssignOp | DifferenceAssingOp | MultiplicationAssingOp | ExponentAssignOp | DivisionAssignOp | RootAssignOp | ModuloAssignOp;
Comment:            CommentSign ~('\n')* '\n'; // line comment
WhiteSpace:         [ \t\r\n]+ -> skip; // skip whitespaces

/// rules

arguments:          (expression (EnumerationSign expression)*)?;
restOfFunCall:      '(' arguments ')' (MemberOfSign Identifier '(' arguments ')')*;

expressionPiece:    (Identifier restOfFunCall?) | Constant | ('(' expression ')');
castExpression:     expressionPiece (CastOp Type)?;
exponentExpression: castExpression ((ExponentOp | RootOp) castExpression)*;
inverseExpression:  DifferenceOp? exponentExpression;
multiplyExpression: inverseExpression ((MultiplicationOp | DifferenceOp | ModuloOp) inverseExpression)*;
addExpression:      multiplyExpression ((SumOp | DifferenceOp) multiplyExpression)*;
compareExpression:  addExpression ((LesserThanOp | LesserOrEqualOp | GreaterThanOp | GreaterOrEqualOp) addExpression)?;
typeCheckExpression:compareExpression (IsOp Type)?;
notExpression:      NotOp? typeCheckExpression;
equalExpression:    notExpression ((NormalComparOp | ReferenceComparOp) notExpression)?;
andExpression:      equalExpression (AndOp equalExpression)*;
orExpression:       andExpression (OrOp andExpression)*;
expression:         orExpression;

block:              '{' (idStartedStatement | varDeclaration | ifStatement | whileStatement | returnStatement)* '}';
parameters:         (typedIdentifier (EnumerationSign typedIdentifier)*)?;

typedIdentifier:    Identifier TypeSign Type;
idStartedStatement: Identifier (restOfFunCall | (AssignOp expression)) EndSign;
returnStatement:    Return expression EndSign;
varDeclaration:     Variable typedIdentifier (NormalAssignOp expression)? EndSign;
elseStatement:      Else block;
ifStatement:        If '(' expression ')' block elseStatement?;
whileStatement:     While '(' expression ')' block;
funDeclaration:     Identifier '(' parameters ')' TypeSign Type block;

program:            funDeclaration+;
