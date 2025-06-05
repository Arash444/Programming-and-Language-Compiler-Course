package visitor.nameAnalyzer;

import ast.node.Program;
import ast.node.declaration.*;
import ast.node.statement.ArrayDecStmt;
import ast.node.statement.ForloopStmt;
import ast.node.statement.ImplicationStmt;
import ast.node.statement.VarDecStmt;
import compileError.*;
import compileError.Name.*;
import symbolTable.SymbolTable;
import symbolTable.symbolTableItems.*;
import symbolTable.itemException.ItemAlreadyExistsException;
import symbolTable.symbolTableItems.VariableItem;
import visitor.Visitor;

import java.util.ArrayList;

public class NameAnalyzer extends Visitor<Void> {

    public ArrayList<CompileError> nameErrors = new ArrayList<>();

    @Override
    public Void visit(Program program) {
        SymbolTable.root = new SymbolTable();
        SymbolTable.push(SymbolTable.root);

        for (FuncDeclaration functionDeclaration : program.getFuncs()) {
            functionDeclaration.accept(this);
        }

        for (var stmt : program.getMain().getMainStatements()) {
            if(stmt instanceof VarDecStmt || stmt instanceof ForloopStmt
                     || stmt instanceof ImplicationStmt || stmt instanceof ArrayDecStmt) {
                stmt.accept(this);
            }
        }

        return null;
    }


    @Override
    public Void visit(FuncDeclaration funcDeclaration) {
        var functionItem = new FunctionItem(funcDeclaration);
        var functionSymbolTable = new SymbolTable(SymbolTable.top, functionItem.getName());
        functionItem.setFunctionSymbolTable(functionSymbolTable);

        boolean done = false;
        while(!done)
        {
            try {
                SymbolTable.top.put(functionItem);
                done = true;
            } catch (ItemAlreadyExistsException iaee) {
                if(!functionItem.getName().endsWith("#")) {
                    FunctionRedefinition error = new FunctionRedefinition(funcDeclaration.getLine(), functionItem.getName());
                    this.nameErrors.add(error);
                }
                functionItem.setName(functionItem.getName() + "#");
            }
        }
        SymbolTable.push(functionSymbolTable);

        for (ArgDeclaration varDeclaration : funcDeclaration.getArgs()) {
            varDeclaration.accept(this);
        }

        for (var stmt : funcDeclaration.getStatements()) {
            if(stmt instanceof VarDecStmt || stmt instanceof ForloopStmt
                     || stmt instanceof ImplicationStmt || stmt instanceof ArrayDecStmt) {
                stmt.accept(this);
            }
        }
        SymbolTable.pop();
        return null;
    }

    @Override
    public Void visit(VarDecStmt varDeclaration) {
        var variableItem = new VariableItem(varDeclaration);
        boolean done = false;
        while(!done) {
            try {
                SymbolTable.top.put(variableItem);
                done = true;
            } catch (ItemAlreadyExistsException iaee) {
                if(!variableItem.getName().endsWith("#")){
                    VariableRedefinition error = new VariableRedefinition(varDeclaration.getLine(), variableItem.getName());
                    this.nameErrors.add(error);
                }
                variableItem.setName(variableItem.getName() + "#");
            }
        }
        return null;
    }

    public Void visit(ForloopStmt forloopStmt) {
        var forLoopItem = new VariableItem(forloopStmt.getIterator().getName(),forloopStmt.getIterator().getType());
        var forLoopSymbolTable = new SymbolTable(SymbolTable.top, forLoopItem.getName());
        forLoopItem.setVarSymbolTable(forLoopSymbolTable);
        SymbolTable.push(forLoopSymbolTable);
        boolean done = false;
        while(!done) {
            try {
                SymbolTable.top.put(forLoopItem);
                done = true;
            } catch (ItemAlreadyExistsException iaee) {
            }
        }
        for (var stmt : forloopStmt.getStatements()) {
            if(stmt instanceof VarDecStmt || stmt instanceof ForloopStmt
                    || stmt instanceof ImplicationStmt || stmt instanceof ArrayDecStmt) {
                stmt.accept(this);
            }
        }
        SymbolTable.pop();
        return null;
    }
    public Void visit(ImplicationStmt implicationStmt) {
        var implicationSymbolTable = new SymbolTable(SymbolTable.top, "ImplicationStm");
        SymbolTable.push(implicationSymbolTable);
        for (var stmt : implicationStmt.getStatements()) {
            if(stmt instanceof VarDecStmt || stmt instanceof ForloopStmt
                    || stmt instanceof ImplicationStmt || stmt instanceof ArrayDecStmt) {
                stmt.accept(this);
            }
        }
        SymbolTable.pop();
        return null;
    }
    public Void visit(ArrayDecStmt arrayDecStmt) {
        var arrayItem = new ArrayItem(arrayDecStmt);
        boolean done = false;
        while(!done) {
            try {
                SymbolTable.top.put(arrayItem);
                done = true;
            } catch (ItemAlreadyExistsException iaee) {
                if(!arrayItem.getName().endsWith("#")){
                    VariableRedefinition error = new VariableRedefinition(arrayDecStmt.getLine(), arrayItem.getName());
                    this.nameErrors.add(error);
                }
                arrayItem.setName(arrayItem.getName() + "#");
            }
        }
        return null;
    }
    public Void visit(ArgDeclaration argDeclaration) {
        var argItem = new VariableItem(argDeclaration.getIdentifier().getName(),argDeclaration.getIdentifier().getType());;

        boolean done = false;
        while (!done) {
            try {
                SymbolTable.top.put(argItem);
                done = true;
            } catch (ItemAlreadyExistsException e) {
                if (!argItem.getName().endsWith("#")) {
                    VariableRedefinition error = new VariableRedefinition(argDeclaration.getLine(), argItem.getName());
                    this.nameErrors.add(error);
                }
                argItem.setName(argItem.getName() + "#");
            }
        }

        return null;
    }
}


