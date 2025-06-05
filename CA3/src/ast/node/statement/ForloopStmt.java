package ast.node.statement;

import ast.node.expression.Identifier;
import visitor.IVisitor;

import java.util.ArrayList;

//Line -> FOR
public class ForloopStmt extends Statement {
    private Identifier iterator;
    private Identifier arrayName;
    private ArrayList<Statement> bodyStmts = new ArrayList<>();
    //private int id;

    public ForloopStmt(Identifier iterator, Identifier arrayName, ArrayList<Statement> bodyStmts) {
        this.iterator = iterator;
        this.arrayName = arrayName;
        this.bodyStmts = bodyStmts;
    }

    public void setStatements(ArrayList<Statement> statements) {
        this.bodyStmts = statements;
    }
    public ArrayList<Statement> getStatements() {
        return bodyStmts;
    }
    public void addStatements(Statement statement) {
        bodyStmts.add(statement);
    }

    public Identifier getIterator() {
        return iterator;
    }

    public void setIterator(Identifier identifier) {
        this.iterator = identifier;
    }

    public Identifier getArrayName() {
        return arrayName;
    }

    public void setArrayName(Identifier identifier) {
        this.arrayName = identifier;
    }

    @Override
    public String toString() {
        return "ForloopStmt";
    }

    //public int getForLoopId() { return id;}

    //public void setForLoopId(int id) {this.id = id;}

    @Override
    public <T> T accept(IVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
