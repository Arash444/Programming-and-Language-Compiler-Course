grammar LogicPL;

@header{
import ast.node.*;
import ast.node.expression.*;
import ast.node.statement.*;
import ast.node.declaration.*;
import ast.node.expression.values.*;
import ast.node.expression.operators.*;
import ast.type.primitiveType.*;
import ast.type.*;
}

program returns[Program p]:
    {$p = new Program(); $p.setLine(0);}
    (f = functionDec {$p.addFunc($f.functionDeclaration);})*
    main = mainBlock {$p.setMain($main.main) ;}
    ;

functionDec returns[FuncDeclaration functionDeclaration]:
    {ArrayList<ArgDeclaration> args = new ArrayList<>();
     ArrayList<Statement> statements = new ArrayList<>();}
    FUNCTION name = identifier
    LPAR (arg1 = functionVarDec {args.add($arg1.argDeclaration);}
    (COMMA arg = functionVarDec {args.add($arg.argDeclaration);})*)? RPAR COLON returnType = type
    LBRACE ((stmt = statement {statements.add($stmt.stm_ret);})+) RBRACE
    {$functionDeclaration = new FuncDeclaration($name.id_ret, $returnType.type_ret, args, statements);
    $functionDeclaration.setLine($name.id_ret.getLine());}
    ;

functionVarDec returns [ArgDeclaration argDeclaration]:
    t = type arg_iden = identifier {$argDeclaration = new ArgDeclaration($arg_iden.id_ret, $t.type_ret);
    $argDeclaration.setLine($arg_iden.id_ret.getLine());}
    ;

mainBlock returns [MainDeclaration main]:
    {ArrayList<Statement> mainStmts = new ArrayList<>();}
    m = MAIN LBRACE (s = statement {mainStmts.add($s.stm_ret);})+ RBRACE
    {$main = new MainDeclaration(mainStmts); $main.setLine($m.getLine());}
    ;

statement returns [Statement stm_ret]:
    assign = assignSmt {$stm_ret = $assign.assign_stm_ret;}
    | ( pred = predicate SEMICOLON ) {$stm_ret = $pred.pred_ret;}
    | impl = implication {$stm_ret = $impl.impl_ret;}
    | ret = returnSmt {$stm_ret = $ret.return_stm;}
    | print = printSmt {$stm_ret = $print.print_stmt_ret;}
    | fr = forLoop {$stm_ret = $fr.for_ret;}
    | lvd = localVarDeclaration {$stm_ret = $lvd.lvd_ret;}
    ;

assignSmt returns [AssignStmt assign_stm_ret]:
    var = variable ass = ASSIGN exp = expression SEMICOLON
    {$assign_stm_ret = new AssignStmt($var.var_return, $exp.exp_ret); $assign_stm_ret.setLine($ass.getLine());}
    ;

variable returns [Variable var_return]:
    id = identifier {$var_return = $id.id_ret;}
    | id = identifier LBRACKET exp = expression RBRACKET
    {$var_return = new ArrayAccess($id.id_ret.getName(), $exp.exp_ret); $var_return.setLine($id.id_ret.getLine());}
    ;

localVarDeclaration returns [Statement lvd_ret]:
     vard_dec = varDeclaration {$lvd_ret = $vard_dec.var_dec_ret;}
    | arr_dec = arrayDeclaration {$lvd_ret = $arr_dec.arr_dec_ret;}
    ;

varDeclaration returns [VarDecStmt var_dec_ret]:
    var_type = type id = identifier
    {$var_dec_ret = new VarDecStmt($id.id_ret, $var_type.type_ret); $var_dec_ret.setLine($id.id_ret.getLine());}
    (ASSIGN expr = expression {$var_dec_ret.setInitialExpression($expr.exp_ret);})? SEMICOLON
    ;

arrayDeclaration returns [ArrayDecStmt arr_dec_ret]:
    array_type = type LBRACKET array_size = INT_NUMBER RBRACKET id = identifier
    {$arr_dec_ret = new ArrayDecStmt($id.id_ret, $array_type.type_ret, $array_size.int);
    $arr_dec_ret.setLine($id.id_ret.getLine());}
    (init_value = arrayInitialValue {$arr_dec_ret.setInitialValues($init_value.arr_init_ret);})? SEMICOLON
    ;

arrayInitialValue returns [ArrayList<Expression> arr_init_ret]:
    ASSIGN arr = arrayList {$arr_init_ret = $arr.arr_list_ret;}
    ;

arrayList returns [ArrayList<Expression> arr_list_ret]:
    {ArrayList<Expression> array_init_list = new ArrayList<>();}
    LBRACKET ( val = value {array_init_list.add($val.value_return);}
    | id = identifier {array_init_list.add($id.id_ret);} )
    (COMMA ( val = value {array_init_list.add($val.value_return);}
    | id = identifier {array_init_list.add($id.id_ret);} ))* RBRACKET
    {$arr_list_ret = array_init_list;}
    ;

printSmt returns [PrintStmt print_stmt_ret]:
    print = PRINT LPAR exp = printExpr RPAR SEMICOLON
    {$print_stmt_ret = new PrintStmt($exp.print_expr_ret); $print_stmt_ret.setLine($print.getLine());}
    ;

printExpr returns [Expression print_expr_ret]:
    var = variable {$print_expr_ret = $var.var_return;}
    | q = query {$print_expr_ret = $q.query_ret;}
    ;

query returns [QueryExpression query_ret]:
      q1 = queryType1 {$query_ret = $q1.query1_ret;}
     | q2 = queryType2 {$query_ret = $q2.query2_ret;}
    ;

queryType1 returns [QueryExpression query1_ret]:
    LBRACKET mark = QUARYMARK pred = predicateIdentifier LPAR var = variable RPAR RBRACKET
    {$query1_ret = new QueryExpression($pred.id_ret); $query1_ret.setVar($var.var_return);
    $query1_ret.setLine($mark.getLine());}
    ;

queryType2 returns [QueryExpression query2_ret]:
    LBRACKET pred = predicateIdentifier LPAR mark = QUARYMARK RPAR RBRACKET
    {$query2_ret = new QueryExpression($pred.id_ret); $query2_ret.setLine($mark.getLine());}
    ;

returnSmt returns [ReturnStmt return_stm]:
    {$return_stm = new ReturnStmt(null);}
    ret = RETURN (val = value {$return_stm.setExpression($val.value_return);}
    | id = identifier {$return_stm.setExpression($id.id_ret);} )?
    SEMICOLON { $return_stm.setLine($ret.getLine());}
    ;

forLoop returns [ForloopStmt for_ret]:
    {ArrayList<Statement> body = new ArrayList<>();}
    for = FOR LPAR itr = identifier COLON array = identifier
    RPAR LBRACE ((stm = statement {body.add($stm.stm_ret);})*) RBRACE
    {$for_ret = new ForloopStmt($itr.id_ret, $array.id_ret, body); $for_ret.setLine($for.getLine());}
    ;

predicate returns [PredicateStmt pred_ret]:
    id = predicateIdentifier LPAR var = variable RPAR
    {$pred_ret= new PredicateStmt($id.id_ret, $var.var_return); $pred_ret.setLine($id.id_ret.getLine());}
    ;

implication returns [ImplicationStmt impl_ret]:
    {ArrayList<Statement> result = new ArrayList<>();}
    LPAR exp = expression RPAR arrow = ARROW
    LPAR ((stm = statement {result.add($stm.stm_ret);})+) RPAR
    {$impl_ret= new ImplicationStmt($exp.exp_ret, result); $impl_ret.setLine($arrow.getLine());}
    ;

expression returns [Expression exp_ret]:
    left = andExpr right = expression2
    {if($right.exp2_ret != null)
    {$exp_ret = new BinaryExpression($left.and_ret, $right.exp2_ret.getRight(), $right.exp2_ret.getBinaryOperator());
    $exp_ret.setLine($right.exp2_ret.getLine());} else {$exp_ret = $left.and_ret;}}
    ;

expression2 returns [BinaryExpression exp2_ret]:
    or = OR left = andExpr right = expression2
    {if($right.exp2_ret != null)
    {BinaryExpression temp = new
    BinaryExpression($left.and_ret, $right.exp2_ret.getRight(), $right.exp2_ret.getBinaryOperator());
    temp.setLine($right.exp2_ret.getLine());
    $exp2_ret = new BinaryExpression(null, temp, BinaryOperator.or);}
    else{$exp2_ret = new BinaryExpression(null, $left.and_ret, BinaryOperator.or);}}
    {$exp2_ret.setLine($or.getLine());}
    | {$exp2_ret = null;}
    ;

andExpr returns [Expression and_ret]:
    left = eqExpr right = andExpr2
    {if($right.and2_ret != null)
    {$and_ret = new BinaryExpression($left.eq_ret, $right.and2_ret.getRight(), $right.and2_ret.getBinaryOperator());
    $and_ret.setLine($right.and2_ret.getLine());} else {$and_ret = $left.eq_ret;}}
    ;

andExpr2 returns [BinaryExpression and2_ret]:
    and = AND left = eqExpr right = andExpr2
    {if($right.and2_ret != null)
    { BinaryExpression temp = new BinaryExpression
    ($left.eq_ret, $right.and2_ret.getRight(), $right.and2_ret.getBinaryOperator());
    temp.setLine($right.and2_ret.getLine());
    $and2_ret = new BinaryExpression(null, temp, BinaryOperator.and);}
    else{$and2_ret = new BinaryExpression(null, $left.eq_ret, BinaryOperator.and);}
    $and2_ret.setLine($and.getLine());}
    | {$and2_ret = null;}
    ;

eqExpr returns [Expression eq_ret]:
    left = compExpr right = eqExpr2
    {if($right.eq2_ret != null)
    {$eq_ret = new BinaryExpression($left.cmp_ret, $right.eq2_ret.getRight(), $right.eq2_ret.getBinaryOperator());
    $eq_ret.setLine($right.eq2_ret.getLine());} else {$eq_ret = $left.cmp_ret;}}
    ;

eqExpr2 returns [BinaryExpression eq2_ret] locals [BinaryOperator opr]:
    ( op = EQ {$opr = BinaryOperator.eq;} | op = NEQ {$opr = BinaryOperator.neq;} )
    left = compExpr right = eqExpr2
    {if($right.eq2_ret != null)
    {BinaryExpression temp = new BinaryExpression
    ($left.cmp_ret, $right.eq2_ret.getRight(), $right.eq2_ret.getBinaryOperator());
    temp.setLine($right.eq2_ret.getLine());
    $eq2_ret = new BinaryExpression(null, temp, $opr);}
    else{$eq2_ret = new BinaryExpression(null, $left.cmp_ret, $opr);}
    $eq2_ret.setLine($op.getLine());}
    | {$eq2_ret = null;}
    ;

compExpr returns [Expression cmp_ret]:
    left = additive right = compExpr2
    {if($right.cmp2_ret != null)
    {$cmp_ret = new BinaryExpression($left.add_ret, $right.cmp2_ret.getRight(), $right.cmp2_ret.getBinaryOperator());
    $cmp_ret.setLine($right.cmp2_ret.getLine());} else {$cmp_ret = $left.add_ret;}}
    ;

compExpr2 returns [BinaryExpression cmp2_ret] locals [BinaryOperator opr]:
    ( op = LT {$opr = BinaryOperator.lt;} | op = LTE {$opr = BinaryOperator.lte;}
    | op = GT {$opr = BinaryOperator.gt;} | op = GTE {$opr = BinaryOperator.gte;} )
    left = additive right = compExpr2
    {if($right.cmp2_ret != null)
    {BinaryExpression temp = new BinaryExpression
    ($left.add_ret, $right.cmp2_ret.getRight(), $right.cmp2_ret.getBinaryOperator());
    temp.setLine($right.cmp2_ret.getLine());
    $cmp2_ret = new BinaryExpression(null, temp, $opr);}
    else{$cmp2_ret = new BinaryExpression(null, $left.add_ret, $opr);}
    $cmp2_ret.setLine($op.getLine());}
    | {$cmp2_ret = null;}
    ;

additive returns [Expression add_ret]:
    left = multicative right = additive2
    {if($right.add2_ret != null)
    {$add_ret = new BinaryExpression($left.multi_ret, $right.add2_ret.getRight(), $right.add2_ret.getBinaryOperator());
    $add_ret.setLine($right.add2_ret.getLine());} else {$add_ret = $left.multi_ret;}}
    ;

additive2 returns [BinaryExpression add2_ret] locals [BinaryOperator opr]:
    (op = PLUS {$opr = BinaryOperator.add;} | op = MINUS {$opr = BinaryOperator.sub;})
    left = multicative right = additive2
    {if($right.add2_ret != null)
    {BinaryExpression temp = new BinaryExpression
    ($left.multi_ret, $right.add2_ret.getRight(), $right.add2_ret.getBinaryOperator());
    temp.setLine($right.add2_ret.getLine());
    $add2_ret = new BinaryExpression(null, temp, $opr);}
    else{$add2_ret = new BinaryExpression(null, $left.multi_ret, $opr);}
    $add2_ret.setLine($op.getLine());}
    | {$add2_ret = null;}
    ;

multicative returns [Expression multi_ret]:
    left = unary right = multicative2
    {if($right.multi2_ret != null)
    {$multi_ret = new BinaryExpression($left.unary_ret, $right.multi2_ret.getRight(), $right.multi2_ret.getBinaryOperator());
    $multi_ret.setLine($right.multi2_ret.getLine());} else {$multi_ret = $left.unary_ret;}}
    ;

multicative2 returns [BinaryExpression multi2_ret] locals [BinaryOperator opr]:
    ( op = MULT { $opr = BinaryOperator.mult; } | op = MOD { $opr = BinaryOperator.mod; }
    | op = DIV { $opr = BinaryOperator.div; }) left = unary right = multicative2
    {if($right.multi2_ret != null)
    {BinaryExpression temp = new BinaryExpression
    ($left.unary_ret, $right.multi2_ret.getRight(), $right.multi2_ret.getBinaryOperator());
    temp.setLine($right.multi2_ret.getLine());
    $multi2_ret = new BinaryExpression(null, temp, $opr);}
    else{$multi2_ret = new BinaryExpression(null, $left.unary_ret, $opr);}
    $multi2_ret.setLine($op.getLine());}
    |  {$multi2_ret = null;}
    ;

unary returns [Expression unary_ret] locals [UnaryOperator opr]:
    oth = other {$unary_ret = $oth.other_ret;}
    | ( op = PLUS { $opr = UnaryOperator.plus; }
    | op = MINUS { $opr = UnaryOperator.minus; }
    | op = NOT { $opr = UnaryOperator.not; } )
     oth = other { $unary_ret = new UnaryExpression($opr, $oth.other_ret); $unary_ret.setLine($op.getLine()); }
    ;

other returns [Expression other_ret]:
    LPAR exp = expression RPAR {$other_ret = $exp.exp_ret;}
    | var = variable {$other_ret = $var.var_return;}
    | val = value {$other_ret = $val.value_return;}
    | query1 = queryType1 {$other_ret = $query1.query1_ret;}
    | funcCall = functionCall {$other_ret = $funcCall.func_call_ret;}
    ;

functionCall returns [FunctionCall func_call_ret]:
    { ArrayList<Expression> expressions = new ArrayList<>(); }
    name = identifier LPAR
    (exp1 = expression { expressions.add($exp1.exp_ret); }
    (COMMA exp2 = expression { expressions.add($exp2.exp_ret); })*)?RPAR
    { $func_call_ret = new FunctionCall(expressions, $name.id_ret);
    $func_call_ret.setLine($name.id_ret.getLine()); }
    ;

value returns [Value value_return]:
    num = numericValue { $value_return = $num.num_val_ret; }
    | val = TRUE { $value_return = new BooleanValue(true); $value_return.setLine($val.getLine());}
    | val = FALSE { $value_return = new BooleanValue(false); $value_return.setLine($val.getLine());}
    | MINUS num = numericValue {$value_return = $num.num_val_ret ; $value_return.negateConstant(); }
    ;

numericValue returns [Value num_val_ret]:
    num = INT_NUMBER { $num_val_ret = new IntValue($num.int); $num_val_ret.setLine($num.getLine());}
    | num = FLOAT_NUMBER { $num_val_ret = new FloatValue(Float.parseFloat($num.text));
    $num_val_ret.setLine($num.getLine());}
    ;

identifier returns [Identifier id_ret]:
    id = IDENTIFIER { $id_ret = new Identifier($id.text); $id_ret.setLine($id.getLine());}
    ;

predicateIdentifier returns [Identifier id_ret]:
    id = PREDICATE_IDENTIFIER { $id_ret = new Identifier($id.text); $id_ret.setLine($id.getLine());}
    ;

type returns [Type type_ret]:
    BOOLEAN { $type_ret = new BooleanType(); }
    | INT { $type_ret = new IntType(); }
    | FLOAT { $type_ret = new FloatType(); }
    ;




FUNCTION : 'function';
BOOLEAN : 'boolean';
INT : 'int';
FLOAT: 'float';
MAIN: 'main';
PRINT: 'print';
RETURN: 'return';
FOR: 'for';
TRUE: 'true';
FALSE: 'false';

LPAR: '(';
RPAR: ')';
COLON: ':';
COMMA: ',';
LBRACE: '{';
RBRACE: '}';
SEMICOLON: ';';
ASSIGN: '=';
LBRACKET: '[';
RBRACKET: ']';
QUARYMARK: '?';
ARROW: '=>';
OR: '||';
AND: '&&';
EQ: '==';
GT: '>';
LT: '<';
GTE: '>=';
LTE: '<=';
PLUS: '+';
MINUS: '-';
MULT: '*';
DIV: '/';
MOD: '%';
NEQ: '!=';
NOT: '!';


WS : [ \t\r\n]+ -> skip ;
COMMENT : '#' ~[\r\n]* -> skip ;

IDENTIFIER : [a-z][a-zA-Z0-9_]* ;
PREDICATE_IDENTIFIER : [A-Z][a-zA-Z0-9]* ;
INT_NUMBER : [0-9]+;
FLOAT_NUMBER: ([0-9]*[.])?[0-9]+;