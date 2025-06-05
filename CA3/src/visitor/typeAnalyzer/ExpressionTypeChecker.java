package visitor.typeAnalyzer;

import ast.node.declaration.ArgDeclaration;
import ast.node.declaration.FuncDeclaration;
import ast.node.expression.*;
import ast.node.expression.operators.BinaryOperator;
import ast.node.expression.operators.UnaryOperator;
import ast.node.expression.values.BooleanValue;
import ast.node.expression.values.FloatValue;
import ast.node.expression.values.IntValue;
import ast.node.statement.ArrayDecStmt;
import ast.node.statement.VarDecStmt;
import ast.type.NoType;
import ast.type.Type;
import ast.type.primitiveType.BooleanType;
import ast.type.primitiveType.FloatType;
import ast.type.primitiveType.IntType;
import compileError.CompileError;
import compileError.Name.FunctionRedefinition;
import compileError.Type.FunctionNotDeclared;
import compileError.Type.UnsupportedOperandType;
import compileError.Type.VarNotDeclared;
import symbolTable.SymbolTable;
import symbolTable.itemException.ItemAlreadyExistsException;
import symbolTable.itemException.ItemNotFoundException;
import symbolTable.symbolTableItems.ArrayItem;
import symbolTable.symbolTableItems.FunctionItem;
import symbolTable.symbolTableItems.VariableItem;
import visitor.Visitor;

import java.awt.geom.Arc2D;
import java.util.ArrayList;
import java.util.Objects;

public class ExpressionTypeChecker extends Visitor<Type> {
    public ArrayList<CompileError> typeErrors;
    public ExpressionTypeChecker(ArrayList<CompileError> typeErrors){
        this.typeErrors = typeErrors;
    }

    public boolean sameType(Type el1, Type el2){
        if(el1 instanceof IntType && el2 instanceof IntType) {
            return true;
        }
        else if(el1 instanceof FloatType && el2 instanceof FloatType) {
            return true;
        }
        else if (el1 instanceof BooleanType && el2 instanceof BooleanType) {
            return true;
        }
        else {
            return false;
        }
    }

    private boolean isOperatorIntFloat(BinaryOperator operator)
    {
        return operator == BinaryOperator.add || operator == BinaryOperator.mult || operator == BinaryOperator.sub
                || operator == BinaryOperator.mod || operator == BinaryOperator.div;
    }
    private boolean isOperatorComp(BinaryOperator operator)
    {
        return operator == BinaryOperator.neq || operator == BinaryOperator.eq || operator == BinaryOperator.lte
                || operator == BinaryOperator.lt || operator == BinaryOperator.gt || operator == BinaryOperator.gte;
    }

    @Override
    public Type visit(UnaryExpression unaryExpression) {

        Expression uExpr = unaryExpression.getOperand();
        Type expType = uExpr.accept(this);
        UnaryOperator operator = unaryExpression.getUnaryOperator();

        if(expType instanceof IntType && (operator == UnaryOperator.minus || operator == UnaryOperator.plus)) {
            return new IntType();
        }
        else if (expType instanceof BooleanType && operator == UnaryOperator.not){
            return new BooleanType();
        }
        else if (expType instanceof NoType){
            return new NoType();  //
        }
        else {
            typeErrors.add(new UnsupportedOperandType(unaryExpression.getLine(), operator.name()));
            return new NoType();
        }
    }

    @Override
    public Type visit(BinaryExpression binaryExpression) {
        Type tl = binaryExpression.getLeft().accept(this);
        Type tr = binaryExpression.getRight().accept(this);
        BinaryOperator operator =  binaryExpression.getBinaryOperator();
        if(isOperatorIntFloat(operator)) {
            if(tl instanceof IntType && tr instanceof IntType)
                return new IntType();
            if(tl instanceof FloatType && tr instanceof FloatType)
                return new FloatType();
            else if((tl instanceof NoType && !(tr instanceof BooleanType)) ||
                    (!(tl instanceof BooleanType) && tr instanceof NoType))
                return new NoType();
            else {
                typeErrors.add(new UnsupportedOperandType(binaryExpression.getLine(), operator.name()));
                return new NoType();
            }
        }
        else if(isOperatorComp(operator)) {
            if(sameType(tl, tr))
                return new BooleanType();
            else if(tl instanceof NoType || tr instanceof NoType)
                return new NoType();
            else {
                typeErrors.add(new UnsupportedOperandType(binaryExpression.getLine(), operator.name()));
                return new NoType();
            }
        }
        else {
            if(tl instanceof BooleanType && tr instanceof BooleanType)
                return new BooleanType();
            else if ((tl instanceof NoType && tr instanceof BooleanType) ||
                    (tl instanceof BooleanType && tr instanceof NoType) ||
                    (tl instanceof NoType && tr instanceof NoType))
                return new NoType();
            else {
                typeErrors.add(new UnsupportedOperandType(binaryExpression.getLine(), operator.name()));
                return new NoType();
            }
        }
    }

    @Override
    public Type visit(Identifier identifier) {
        try {
            VariableItem varItem = (VariableItem) SymbolTable.top.get(VariableItem.STARTKEY + identifier.getName());
            return varItem.getType();
        } catch (ItemNotFoundException e) {
            typeErrors.add(new VarNotDeclared(identifier.getLine(), identifier.getName()));
            VarDecStmt errorVar = new VarDecStmt(identifier, new NoType());
            VariableItem errorItem = new VariableItem(errorVar);
            try {
                SymbolTable.top.put(errorItem);
            }
            catch (ItemAlreadyExistsException ee) {

            }
            return new NoType();
        }
    }

    @Override
    public Type visit(FunctionCall functionCall) {
        try {
            FunctionItem funcItem = (FunctionItem) SymbolTable.top.get(FunctionItem.STARTKEY + functionCall.getUFuncName().getName());
            for (Expression arg : functionCall.getArgs()) {
                arg.accept(this);
            }
            return funcItem.getHandlerDeclaration().getType();
        }
        catch (ItemNotFoundException e) {
            typeErrors.add(new FunctionNotDeclared(functionCall.getLine(), functionCall.getUFuncName().getName()));
            FuncDeclaration errorFunction = new FuncDeclaration(functionCall.getUFuncName(), new NoType(), null, null);
            FunctionItem errorItem = new FunctionItem(errorFunction);
            try {
                SymbolTable.top.put(errorItem);
            }
            catch (ItemAlreadyExistsException ee) {
                //
            }
            for (Expression arg : functionCall.getArgs()) {
                arg.accept(this);
            }
            return new NoType();
        }
    }

    @Override
    public Type visit(QueryExpression queryExpression) {
        var var = queryExpression.getVar();
        if(var != null) {
            var.accept(this);
            return new BooleanType();
        }
        else
            return new NoType();
    }

    @Override
    public Type visit(ArrayAccess arrayAccess) {
        try {
            VariableItem arrayItem = (VariableItem) SymbolTable.top.get(VariableItem.STARTKEY + arrayAccess.getName());
            arrayAccess.getIndex().accept(this);
            return arrayItem.getType();
        } catch (ItemNotFoundException e) {
            typeErrors.add(new VarNotDeclared(arrayAccess.getLine(), arrayAccess.getName()));
            VariableItem errorItem = new VariableItem(arrayAccess.getName(), new NoType());
            try {
                SymbolTable.top.put(errorItem);
            }
            catch (ItemAlreadyExistsException ee) {
                //
            }
            arrayAccess.getIndex().accept(this);
            return new NoType();
        }
    }

    @Override
    public Type visit(IntValue value) {
        return new IntType();
    }

    @Override
    public Type visit(FloatValue value) {
        return new FloatType();
    }

    @Override
    public Type visit(BooleanValue value) {
        return new BooleanType();
    }
}

//FunctionItem functionItem;
//try {
//    functionItem = (FunctionItem) SymbolTable.root.get(FunctionItem.STARTKEY +
//        (Identifier) functionCall.getUFuncName());
//}
//catch (ItemNotFoundException itemNotFoundException) {
//    typeErrors.add(new FunctionNotDeclared(functionCall.getLine(), functionCall.getUFuncName().toString()));
//    return new NoType();
//}
//var funcDecArgs =  functionItem.getHandlerDeclaration().getArgs();
//var funCallArgs = functionCall.getArgs();
//if (funCallArgs.size() != funcDecArgs.size()) {
//    typeErrors.add(new FunctionNotDeclared(functionCall.getLine(), functionCall.getUFuncName().toString()));
//    return new NoType();
//}
//for (int i = 0; i < funCallArgs.size(); i++) {
//    Type funcCallArgType = funCallArgs.get(i).accept(this);
//    Type funcDecArgType = funCallArgs.get(i).accept(this);
//}
//if(tr instanceof IntType && tl instanceof IntType) {
//    if(isOperatorIntFloat(operator))
//        return new IntType();
//    else if(isOperatorComp(operator))
//        return new BooleanType();
//}
//else if(tr instanceof FloatType && tl instanceof FloatType) {
//    if(isOperatorIntFloat(operator))
//        return new FloatType();
//    else if(isOperatorComp(operator))
//        return new BooleanType();
//}
//else if (tr instanceof BooleanType && tl instanceof BooleanType && !isOperatorIntFloat(operator)
//&& !(operator == BinaryOperator.assign)){
//    return new BooleanType();
//}
//else if(tr instanceof NoType || tl instanceof NoType){
//    return new NoType();
//}
//else {
//    typeErrors.add(new UnsupportedOperandType(binaryExpression.getLine(), operator.name()));
//    return new NoType();
//}