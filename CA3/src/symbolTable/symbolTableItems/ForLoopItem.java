package symbolTable.symbolTableItems;

import ast.node.declaration.ArgDeclaration;
import ast.node.declaration.FuncDeclaration;
import ast.node.statement.ForloopStmt;
import ast.type.Type;
import symbolTable.SymbolTable;

import java.util.ArrayList;

public class ForLoopItem extends SymbolTableItem{
    protected SymbolTable ForLoopSymbolTable;
    protected ForloopStmt forloopStmt;
    public static final String STARTKEY = "ForLoop_";
    //private static int counter = 0;
    public ForLoopItem(String name) {
        this.name = name;
    }
    //private int id;
    public ForLoopItem(ForloopStmt forloopStmt) {
        //this.id = counter++;
        //forloopStmt.setForLoopId(id);
        this.name = forloopStmt.toString();
        this.forloopStmt = forloopStmt;
    }
    public SymbolTable getForLoopSymbolTable()
    {
        return this.ForLoopSymbolTable;
    }
    public void setForLoopSymbolTable(SymbolTable symbolTable) {this.ForLoopSymbolTable = symbolTable; }


    @Override
    public String getKey() {
        return ForLoopItem.STARTKEY + this.name;
    }

}
