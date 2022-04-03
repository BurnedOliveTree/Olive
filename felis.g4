grammar felis;

/// constants

StringConstant:     '"' . '"';
NumConstant:        [0-9]+ ('.' [0-9]+)?;
BoolConstant:       'true' | 'false';

/// keywords and operators

AnyType:            'Any';
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
Function:           'fun';
If:                 'if';
Else:               'else';
While:              'while';
Return:             'return';

/// token groups

Identifier:         [A-Za-z_]+; // to be changed to library function isLetter() after generation
Constant:           StringConstant | NumConstant | BoolConstant;
Type:               AnyType | UnitType | IntType | FloatType | NumberType | StringType | BoolType;
AssignOp:           NormalAssignOp | ReferenceAssignOp | SumAssignOp | DifferenceAssingOp | MultiplicationAssingOp | ExponentAssignOp | DivisionAssignOp | RootAssignOp | ModuloAssignOp;
Comment:            CommentSign ~('\n')* '\n'; // line comment
WhiteSpace:         [ \t\r\n]+ -> skip; // skip whitespaces

/// rules

functionCall:       (Identifier MemberOfSign)? Identifier '(' (Identifier (EnumerationSign Identifier)*)? ')';

expressionPiece:    Identifier | functionCall | Constant | ('(' arithmExpression ')'); // TODO analysis of recursion
exponentExpression: expressionPiece ((ExponentOp | RootOp) expressionPiece)*;
multiplyExpression: exponentExpression ((MultiplicationOp | DifferenceOp | ModuloOp) exponentExpression)*;
addExpression:      multiplyExpression ((SumOp | DifferenceOp) multiplyExpression)*;
arithmExpression:   addExpression;

conditionPiece:     Identifier | functionCall | BoolConstant | ('(' condition ')'); // TODO analysis of recursion
compareExpression:  conditionPiece ((LesserThanOp | LesserOrEqualOp | GreaterThanOp | GreaterOrEqualOp) conditionPiece)*;
notExpression:      NotOp* compareExpression;
equalExpression:    notExpression ((NormalComparOp | ReferenceComparOp) notExpression)*;
andExpression:      equalExpression (AndOp equalExpression)*;
orExpression:       andExpression (OrOp andExpression)*;
condition:          orExpression;

expression:         ((Identifier AssignOp) | Return)? arithmExpression | condition EndSign;
block:              '{' (expression | varDeclaration | ifStatement | whileStatement)* '}'; // TODO analysis of recursion

typedIdentifier:    Identifier TypeSign Type;
varDeclaration:     Variable typedIdentifier (NormalAssignOp expression) | EndSign;
elseStatement:      Else block;
ifStatement:        If '(' condition ')' block elseStatement?;
whileStatement:     While '(' condition ')' block;
funDeclaration:     Function Identifier '(' typedIdentifier* ')' TypeSign Type block;