package visitor.codeGeneration;

import ast.node.Program;
import ast.node.declaration.*;
import ast.node.expression.operators.BinaryOperator;
import ast.node.expression.operators.UnaryOperator;
import ast.node.expression.values.IntValue;
import ast.node.statement.*;
import ast.node.expression.*;
import compileError.*;
import compileError.Name.*;
import org.stringtemplate.v4.ST;
import symbolTable.SymbolTable;
import symbolTable.symbolTableItems.*;
import symbolTable.itemException.ItemAlreadyExistsException;
import symbolTable.symbolTableItems.VariableItem;
import visitor.Visitor;
import visitor.typeAnalyzer.ExpressionTypeChecker;
import java.io.FileWriter;

import java.util.ArrayList;
public class CodeGeneration extends Visitor<String>{
    private final ArrayList<String> slots =new ArrayList<>();
    String indent = "    ";

    private int slotOf(String identifier) {
        if (identifier.equals("")) {
            return slots.size()+1;
        }
        for (int i = 0; i < slots.size(); i++)
            if (slots.get(i).equals(identifier))
                return i+1;
        slots.add(identifier);
        return slots.size();
    }
    private String startCodeGeneration(String program){
        StringBuilder code = new StringBuilder();
        code.append(".class public ").append("CodeGeneration").append('\n');
        code.append(".super java/lang/Object\n\n");
        code.append(".method public <init>()V\n");
        code.append(indent).append("aload_0\n");
        code.append(indent).append("invokenonvirtual java/lang/Object/<init>()V\n");
        code.append(indent).append("return\n");
        code.append(".end method\n");
        return code.toString();
    }

    @Override
    public String visit(Program program) {
        StringBuilder code = new StringBuilder();
        code.append(startCodeGeneration(program.toString())).append('\n');
        for (var func : program.getFuncs()) {
            code.append(func.accept(this)).append('\n');
            slots.clear();
        }
        code.append(program.getMain().accept(this)).append('\n');
        return code.toString();
    }

    @Override
    public String visit(FuncDeclaration funcDeclaration) {
        StringBuilder code = new StringBuilder();
        code.append(".method public ").append(funcDeclaration.getIdentifier().getName()).append(" ()I\n");
        code.append(indent).append(".limit stack 256\n");
        code.append(indent).append(".limit locals 256\n");

        for (var arg: funcDeclaration.getArgs()) {
            int slot = slotOf(arg.getIdentifier().getName());
            code.append(indent).append("iconst_0").append('\n');
            code.append(indent).append("istore ").append(slot).append('\n');
        }
        for (var stmt : funcDeclaration.getStatements()) {
            if(stmt instanceof VarDecStmt) {
                code.append(stmt.accept(this));
            }
            else if(stmt instanceof AssignStmt) {
                code.append(stmt.accept(this));
            }
            else if(stmt instanceof ReturnStmt) {
                code.append(stmt.accept(this));
            }
        }

        //code.append(indent + "return\n");
        code.append(".end method\n");

        return code.toString();
    }

    @Override
    public String visit(MainDeclaration mainDeclaration) {
        StringBuilder code = new StringBuilder();
        code.append(".method public static main([Ljava/lang/String;)V\n");
        code.append(indent).append(".limit stack 256\n");
        code.append(indent).append(".limit locals 256\n");

        slotOf("main_arg");

        for (var stmt : mainDeclaration.getMainStatements()) {
            if(stmt instanceof VarDecStmt) {
                code.append(stmt.accept(this));
            }
            else if(stmt instanceof AssignStmt) {
                code.append(stmt.accept(this));
            }
            else if(stmt instanceof ReturnStmt) {
                code.append(stmt.accept(this));
            }
            //else if(stmt instanceof FunctionCall) {
            //    code.append(indent).append("invokevirtual java/lang/Object/<init>()V\n");
            //}
        }

        //code.append(indent + "return\n");
        code.append(".end method\n");

        return code.toString();
    }

    @Override
    public String visit(AssignStmt assignStmt) {
        String code = assignStmt.getRValue().accept(this);
        int slot = slotOf(((Variable) assignStmt.getLValue()).getName());
        return code + indent + "istore " + slot + '\n';
    }

    @Override
    public String visit(VarDecStmt varDecStmt) {
        int slot = slotOf((varDecStmt.getIdentifier()).getName());
        String code;
        if(varDecStmt.getInitialExpression() != null){
            code = varDecStmt.getInitialExpression().accept(this);
            return code + indent + "istore " + slot + '\n';
        }
        return  indent + "iconst_0" + '\n' + indent + "istore " + slot + '\n';
    }

    @Override
    public String visit(ReturnStmt returnStmt) {
        String code = returnStmt.getExpression().accept(this);
        return code + indent + "ireturn " + '\n';
    }

    @Override
    public String visit(BinaryExpression binaryExpression) {
        String code = binaryExpression.getLeft().accept(this) + binaryExpression.getRight().accept(this);

        var operator = binaryExpression.getBinaryOperator();
        if(operator.equals(BinaryOperator.add))
            code = code + indent + "iadd " + '\n';
        else if(operator.equals(BinaryOperator.sub))
            code = code + indent + "isub " + '\n';
        else if(operator.equals(BinaryOperator.mult))
            code = code + indent + "imul " + '\n';
        else if(operator.equals(BinaryOperator.div))
            code = code + indent + "idiv " + '\n';
        else if(operator.equals(BinaryOperator.mod))
            code = code + indent + "irem " + '\n';

        return code;
    }

    @Override
    public String visit(UnaryExpression unaryExpression) {
        String code = unaryExpression.getOperand().accept(this);

        var operator = unaryExpression.getUnaryOperator();
        if(operator.equals(UnaryOperator.minus))
            code = code + indent + "ineg " + '\n';

        return code;
    }

    @Override
    public String visit(Identifier identifier) {
        int slot = slotOf(identifier.getName());
        return indent + "iload " + slot + '\n';
    }

    @Override
    public String visit(IntValue intValue) {
        return indent + "bipush " + intValue.getConstant() + '\n';
    }
}
