grammar felis;

// TODO? Tokens

Identifier:         [A-Za-z_]+;         // to be changed to library function isLetter() after generation
Digit:              [0-9];
Type:               'Unit' | 'Int' | 'Float' | 'Number' | 'String' | 'Bool';
ArithmOp:           '+' | '-' | '*' | '/' | '^' | '|' | '%';
AssignOp:           '=' | '&=' | '+=' | '-=' | '*=' | '/=' | '^=' | '|=' | '%=';
ComparOp:           '==' | '&==' | '>' | '>=' | '<' | '<=';
BoolOp:             'not' | 'and' | 'or' | 'is';
Comment:            '#' ~('\n')* '\n';   // line comment
WhiteSpace:         [ \t\r\n]+ -> skip;  // skip whitespaces

stringConstant:     '"' . '"';
numConstant:        Digit+ ('.' Digit+)?;
BoolConstant:       'true' | 'false';
constant:           stringConstant | numConstant | BoolConstant;

conditionPiece:     Identifier | BoolConstant | (Identifier ComparOp Identifier);
condition:          conditionPiece (BoolOp conditionPiece)*;

// TODO operator order
// TODO operator ..
// TODO how to address range of cells?

typedIdentifier:    Identifier':' Type;
functionCall:       (Identifier '.')? Identifier '(' Identifier? (',' Identifier)* ')'; // TODO this production lets through a lonely , at the start
expressionPiece:    Identifier | functionCall | constant; // TODO functionCall starts with Identifier
arithmExpression:   expressionPiece (ArithmOp expressionPiece)*; // TODO there is no ()
expression:         ((Identifier AssignOp) | 'return')? arithmExpression | condition ';';
block:              '{' expression* '}';

// TODO this all be should available from with-in a block, except for funDecl
varDeclaration:     'var' typedIdentifier ('=' expression)? ';';
elseStatement:      'else' block;
ifStatement:        'if' '(' condition ')' block elseStatement?;
whileStatement:     'while' '(' condition ')' block;
funDeclaration:     'fun' Identifier '(' typedIdentifier* ')' ':' Type block;