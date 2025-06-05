package visitor.typeAnalyzer;

import ast.node.Program;
import ast.node.declaration.ArgDeclaration;
import ast.node.declaration.Declaration;
import ast.node.declaration.FuncDeclaration;
import ast.node.declaration.MainDeclaration;
import ast.node.expression.ArrayAccess;
import ast.node.expression.Expression;
import ast.node.expression.FunctionCall;
import ast.node.expression.Identifier;
import ast.node.expression.operators.BinaryOperator;
import ast.node.expression.operators.UnaryOperator;
import ast.node.statement.*;
import ast.type.NoType;
import ast.type.Type;
import ast.type.primitiveType.BooleanType;
import ast.type.primitiveType.FloatType;
import ast.type.primitiveType.IntType;
import compileError.CompileError;
import compileError.Name.VariableRedefinition;
import compileError.Type.FunctionNotDeclared;
import compileError.Type.LeftSideNotLValue;
import compileError.Type.UnsupportedOperandType;
import compileError.Type.ConditionTypeNotBool;
import symbolTable.SymbolTable;
import symbolTable.itemException.ItemAlreadyExistsException;
import symbolTable.itemException.ItemNotFoundException;
import symbolTable.symbolTableItems.ForLoopItem;
import symbolTable.symbolTableItems.FunctionItem;
import symbolTable.symbolTableItems.MainItem;
import symbolTable.symbolTableItems.VariableItem;
import visitor.Visitor;

import java.util.ArrayList;

public class TypeAnalyzer extends Visitor<Void> {
    public ArrayList<CompileError> typeErrors = new ArrayList<>();
    ExpressionTypeChecker expressionTypeChecker = new ExpressionTypeChecker(typeErrors);

    @Override
    public Void visit(Program program) {

        for(var functionDec : program.getFuncs()) {
            functionDec.accept(this);
        }

        program.getMain().accept(this);

        return null;
    }

    @Override
    public Void visit(FuncDeclaration funcDeclaration) {
        var functionItem = new FunctionItem(funcDeclaration);
        var functionSymbolTable = new SymbolTable(SymbolTable.top, funcDeclaration.getName().getName());
        functionItem.setFunctionSymbolTable(functionSymbolTable);
        SymbolTable.push(functionSymbolTable);

        for(var arg : funcDeclaration.getArgs()) {
            arg.accept(this);
        }
        for(var stmt : funcDeclaration.getStatements()) {
            stmt.accept(this);
        }

        SymbolTable.pop();

        return null;
    }

    @Override
    public Void visit(MainDeclaration mainDeclaration) {
        var mainItem = new MainItem(mainDeclaration);
        var mainSymbolTable = new SymbolTable(SymbolTable.top, "main");
        mainItem.setMainItemSymbolTable(mainSymbolTable);

        SymbolTable.push(mainItem.getMainItemSymbolTable());

        for (var stmt : mainDeclaration.getMainStatements()) {
            stmt.accept(this);
        }

        return null;
    }
    @Override
    public Void visit(ForloopStmt forloopStmt) {
        try {
            ForLoopItem forLoopItem = (ForLoopItem) SymbolTable.top
                    .get(ForLoopItem.STARTKEY + forloopStmt.toString() ); //forloopStmt.getForLoopId()
            SymbolTable.push(forLoopItem.getForLoopSymbolTable());
        } catch (ItemNotFoundException e) {
            // unreachable
        }

        forloopStmt.getArrayName().accept(expressionTypeChecker);

        for (var stmt : forloopStmt.getStatements()) {
            stmt.accept(this);
        }

        SymbolTable.pop();

        return null;
    }

    @Override
    public Void visit(AssignStmt assignStmt) {
        Type tl = assignStmt.getLValue().accept(expressionTypeChecker);
        Type tr = assignStmt.getRValue().accept(expressionTypeChecker);

        if (!(tr instanceof NoType) && !(tl instanceof NoType) && !expressionTypeChecker.sameType(tl, tr)) {
            typeErrors.add(new UnsupportedOperandType(assignStmt.getLine(), BinaryOperator.assign.name()));
        }

        return null;
    }

    //@Overridev
    //public Void visit(FunctionCall functionCall) {
    //    try {
    //        SymbolTable.root.get(FunctionItem.STARTKEY + functionCall.getUFuncName().getName());
    //    }
    //    catch (ItemNotFoundException e) {
    //        typeErrors.add(new FunctionNotDeclared(functionCall.getLine(), functionCall.getUFuncName().getName()));
    //    }
    //    for (Expression arg : functionCall.getArgs()) {
    //        arg.accept(expressionTypeChecker);
    //    }
    //    return null;
    //}
    @Override
    public Void visit(ImplicationStmt implicationStmt) {
        var implicationSymbolTable = new SymbolTable(SymbolTable.top, implicationStmt.toString());
        SymbolTable.push(implicationSymbolTable);

        Expression imp = implicationStmt.getCondition();
        Type impType = imp.accept(expressionTypeChecker);
        if(impType instanceof IntType || impType instanceof FloatType)
            typeErrors.add(new ConditionTypeNotBool(implicationStmt.getLine()));
        for (var stmt : implicationStmt.getStatements()) {
            stmt.accept(this);
        }

        SymbolTable.pop();

        return null;
    }
    @Override
    public Void visit(VarDecStmt varDecStmt) {
        var variableItem = new VariableItem(varDecStmt.getIdentifier().getName(), varDecStmt.getType());
        try {
            SymbolTable.top.put(variableItem);
        } catch (ItemAlreadyExistsException e) {
            //
        }
        Type idType = varDecStmt.getIdentifier().accept(expressionTypeChecker);

        Expression initExpr = varDecStmt.getInitialExpression();
        if(initExpr != null) {
            Type initExprType = initExpr.accept(expressionTypeChecker);
            if (!(idType instanceof NoType) && !(initExprType instanceof NoType) &&
                    !expressionTypeChecker.sameType(idType, initExprType))
                typeErrors.add(new UnsupportedOperandType(varDecStmt.getLine(), BinaryOperator.assign.name()));
        }
        return null;
    }
    @Override
    public Void visit(ArrayDecStmt arrDecStmt) {
        var initialValues = arrDecStmt.getInitialValues();
        if (!initialValues.isEmpty()) {
            boolean doesItHaveError = false;
            Type arrayType = arrDecStmt.getType();
            for (var item : arrDecStmt.getInitialValues()) {
                Type valueType = item.accept(expressionTypeChecker);
                if (!expressionTypeChecker.sameType(arrayType, valueType) &&
                        !(arrayType instanceof NoType) && !(valueType instanceof NoType)) {
                    doesItHaveError = true;
                }
            }
            if (doesItHaveError) {
                typeErrors.add(new UnsupportedOperandType(arrDecStmt.getLine(), BinaryOperator.assign.name()));
            }
        }
        return null;
    }

    @Override
    public Void visit(ArgDeclaration argDecStmt) {
        var variableItem = new VariableItem(argDecStmt.getIdentifier().getName(), argDecStmt.getType());
        try {
            SymbolTable.top.put(variableItem);
        } catch (ItemAlreadyExistsException e) {
            //
        }
        return null;
    }

    @Override
    public Void visit(PrintStmt printStmt) {
        printStmt.getArg().accept(expressionTypeChecker);
        return null;
    }
    @Override
    public Void visit(ReturnStmt returnStmt) {
        Expression ret = returnStmt.getExpression();
        if(ret != null)
            returnStmt.getExpression().accept(expressionTypeChecker);
        return null;
    }

    //@Override
    //public Void visit(ArrayAccess arrayAccess) {
    //    arrayAccess.accept(expressionTypeChecker);
    //    return null;
    //}

    //@Override
    //public Void visit(PredicateStmt predicateStmt) {
    //    var variableItem = new VariableItem(predicateStmt.getIdentifier().getName(), predicateStmt.getIdentifier().getType());
    //    try {
    //        SymbolTable.top.put(variableItem);
    //    } catch (ItemAlreadyExistsException e) {
    //        //
    //    }
    //    return null;
    //}
}
