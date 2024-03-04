grammar Javamm;

@header {
    package pt.up.fe.comp2024;
}

EQUALS : '=';
SEMI : ';' ;
LCURLY : '{' ;
RCURLY : '}' ;
LPAREN : '(' ;
RPAREN : ')' ;
RBRAC : ']' ;
LBRAC : '[' ;
MUL : '*' ;
DIV : '/' ;
ADD : '+' ;
SUB : '-' ;
AND : '&&' ;
LTHAN : '<';
GTHAN : '>';
COM : '//';
RCOM : '*/';
LCOM: '/*';

CLASS : 'class' ;
STATIC : 'static' ;
INT : 'int' ;
BOOL : 'boolean';
PUBLIC : 'public' ;
RETURN : 'return' ;

INTEGER : [0] | ([1-9][0-9]*);
ID : ([a-z]|[A-Z]|'_'|'$') ([a-z]|[A-Z]|'_'|'$'|[0-9])*;
BOOLEAN: [0] | [1] | [true] | [false];

COMMENT : LCOM .*? RCOM -> skip;
LINECOMMENT : COM .*? ('\r')?'\n' -> skip;

WS : [ \t\n\r\f]+ -> skip ;

program
    : (importDecl)* classDecl EOF
    | stmt + EOF
    ;

importDecl
    : 'import' name+=ID ( '.' name+=ID )* SEMI #ImportDeclRule
    ;

classDecl
    : CLASS name=ID
        ('extends' ID)?
        LCURLY
            varDecl* methodDecl*
        RCURLY #ClassDeclRule
    ;

varDecl
    : type name=ID SEMI #VarDeclRule
    | type name=ID EQUALS expr SEMI #VarDeclInitRule
    ;

type locals [boolean isArray=false, boolean isVararg=false]
    : name=INT LBRAC RBRAC {$isArray=true;} #ArrayType
    | name=INT '...' {$isVararg=true;}#VarargType
    | name=INT #IntType
    | name=BOOL #BoolType
    | name=ID #ObjectType
    | name='String' {$isArray=true;} #StringType
    ;


methodDecl locals [boolean isPublic=false, boolean isStatic = false]
    : (PUBLIC {$isPublic=true;})?
        (STATIC {$isStatic=true;})?
        type name=ID
        LPAREN
            ( type args+=ID ( ',' type args+=ID )* )?
        RPAREN
        LCURLY
            ( stmt )*
            RETURN expr
            SEMI
        RCURLY #ClassMethod
    | (PUBLIC {$isPublic=true;})?
        (STATIC {$isStatic=true;})?
        'void' 'main'
        LPAREN
            'String' LBRAC RBRAC
            args=ID
        RPAREN
        LCURLY
            ( stmt )*
        RCURLY #MainFunction
    ;


stmt
    : expr EQUALS expr SEMI #AssignStmt
    | varDecl #VarDeclStmt
    | 'if' LPAREN expr* RPAREN stmt ('else' stmt) #IfElseStmt
    | 'while' LPAREN expr* RPAREN stmt #WhileStmt
    | expr SEMI #ExprStmt
    | RETURN expr SEMI #ReturnStmt
    | LCURLY ( stmt )* RCURLY #BlockStmt
    ;

expr
    : LPAREN expr RPAREN #PrecendentExpr
    | '!' expr #NegExpr
    | expr op=( MUL | DIV ) expr #BinaryExpr 
    | expr op=( ADD | SUB ) expr #BinaryExpr 
    | expr op=( LTHAN | GTHAN | AND ) expr #BinaryExpr
    | expr '.' ID LPAREN ( expr ( ',' expr )* )? RPAREN #MemberCallExpr
    | expr LBRAC expr RBRAC #ArrayRefExpr
    | LBRAC ( expr ( ',' expr )* )? RBRAC #ArrayInitExpr
    | 'new' 'int' LBRAC expr RBRAC #NewArrayExpr
    | expr '.' 'length' #LengthExpr
    | 'this' ('.' varRef)? #SelfExpr
    | 'new' varRef LPAREN RPAREN #NewObjExpr
    | varRef LBRAC expr RBRAC #ArrayRefExpr
    | value=INTEGER #IntegerLiteral 
    | bool=BOOLEAN #BoolExpr
    | varRef #VarRefExpr
    ;

varRef
    : name=ID #VarRefRule
    | name='main' #VarRefRule
    | name='length' #VarRefRule
    ;