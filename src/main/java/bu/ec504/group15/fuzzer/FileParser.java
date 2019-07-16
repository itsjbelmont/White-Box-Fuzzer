package bu.ec504.group15.fuzzer;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;

import java.io.File;
import java.util.*;

public class FileParser {

    /* Constructor makes the AST with root of type CompilationUnit */
    public FileParser(String file) {
        /* Initialize to null so the try{}catch{} blocks done worry about it being uninitialized */
        CompilationUnit compU = null;

        /* Set the AST up for solving symbol types */
        TypeSolver typeSolver = new CombinedTypeSolver();
        JavaSymbolSolver symbolsolver = new JavaSymbolSolver(typeSolver);
        JavaParser.getStaticConfiguration().setSymbolResolver(symbolsolver);

        /* Create the AST */
        try {
            compU = JavaParser.parse(new File(file));
        } catch (Exception e) {e.printStackTrace();}

        this.cu = compU;
        this.file = file;
        this.expectedTypes = new ArrayList<>();
        this.expressions = new ArrayList<>();
        this.inputDependantVars = new ArrayList<>();
        this.dependancies = new ArrayList<>();
        this.inputDependantVars.add("args[0]");
    }

    /* Finds all conditional expressions */
    public ArrayList<Expression> FindExpressions() {

        VoidVisitor<List<Expression>> expressionCollector = new BinaryExpressionCollector();

        /* Collect the expressions */
        expressionCollector.visit(cu, expressions);

        return expressions;
    }

    /* Finds some dependancies */
    public ArrayList<DependantArg> FindDependancies(String varName, ArrayList<DependantArg> da) {
        try {
            cu.findAll(AssignExpr.class).forEach(ae -> {
                if (ae.getTokenRange().get().toString().contains(varName)) {

                    ResolvedType rt = ae.calculateResolvedType();
                    String name = ae.getTarget().toString();
                    String type = rt.describe();
                    String assignment = ae.getTokenRange().get().toString();

                    ConditionOnVar c = new ConditionOnVar(name);
                    ArrayList<Expression> list = new ArrayList<>();
                    c.visit(cu, list);

                    DependantArg d = new DependantArg(name, type, assignment, list);
                    if (d != null) {
                        boolean contains = false;
                        for (int i = 0; i < da.size(); i++) {
                            if (da.get(i).getName() == d.getName()) {
                                contains = true;
                                break;
                            }
                        }

                        if (contains == false) {
                            da.add(d);
                        }
                    }
                }
            });

            cu.findAll(VariableDeclarator.class).forEach(vd -> {
                if (vd.getTokenRange().get().toString().contains(varName)) {

                    String name = vd.getNameAsString();
                    String type = vd.getType().toString();
                    String assignment = vd.getTokenRange().get().toString();

                    ConditionOnVar c = new ConditionOnVar(name);
                    ArrayList<Expression> list = new ArrayList<>();
                    c.visit(cu, list);

                    DependantArg d = new DependantArg(name, type, assignment, list);
                    if (d != null) {
                        boolean contains = false;
                        for (int i = 0; i < da.size(); i++) {
                            if (da.get(i).getName() == d.getName()) {
                                contains = true;
                                break;
                            }
                        }

                        if (contains == false) {
                            da.add(d);
                        }
                    }


                }
            });
        } catch(Exception e) {}
        return da;
    }

    /* Uses JavaSymbolSolver to get expected type of args[0] */
    public ArrayList<String> FindArgType() {
        try {
            cu.findAll(AssignExpr.class).forEach(ae -> {
                if (ae.getTokenRange().get().toString().contains("args[0]")) {
                    ResolvedType rt = ae.calculateResolvedType();
                    expectedTypes.add(rt.describe());
                }
            });

            cu.findAll(VariableDeclarator.class).forEach(vd -> {
                if (vd.getTokenRange().get().toString().contains("args[0]")) {
                    expectedTypes.add(vd.getType().toString());
                }
            });

            return expectedTypes;
        } catch (Exception e) {
            return expectedTypes;
        }
    }

    /* Finds all variables dependant on args[0] */
    public ArrayList<String> FindInputDependentVariables() {

        VoidVisitor<List<String>> dependencyCollector = new ArgdependencyCollector();
        dependencyCollector.visit(cu, inputDependantVars);

        return inputDependantVars;
    }

    /* Finds all of the integers in the source code */
    public ArrayList<Integer> findInts() {
        ArrayList<Integer> ints = new ArrayList<>();
        TreeSet<Integer> intMap = new TreeSet<>();

        IntegerCollector collectInt = new IntegerCollector();
        LongCollector collectLong = new LongCollector();
        collectInt.visit(cu, intMap);
        collectLong.visit(cu, intMap);


        ints.addAll(intMap);

        return ints;
    }

    /* Finds all of the doubles in the source code */
    public ArrayList<Double> findDoubles() {
        ArrayList<Double> doubles = new ArrayList<>();
        TreeSet<Double> doubleMap = new TreeSet<>();
        DoubleCollector collectDouble = new DoubleCollector();

        collectDouble.visit(cu, doubleMap);
        doubles.addAll(doubleMap);

        return doubles;
    }

    /* Find all of the string literals in the source code */
    public ArrayList<String> findStrings() {
        StringCollector collector = new StringCollector();
        HashSet<String> hashList = new HashSet<>();
        ArrayList<String> list = new ArrayList<>();
        collector.visit(cu, hashList);
        list.addAll(hashList);
        return list;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /* Fields */
    private CompilationUnit cu;
    private String file;
    private ArrayList<Expression> expressions;
    private ArrayList<String> expectedTypes;
    private ArrayList<String> inputDependantVars;

    private ArrayList<DependantArg> dependancies;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /* Private Classes */

    /* Collect the Binary Expressions (conditional statements) that depend on args[0] */
    private class BinaryExpressionCollector extends VoidVisitorAdapter<List<Expression>> {

        @Override
        public void visit (BinaryExpr b, List<Expression> list) {
            super.visit(b, list);
            if (b.getParentNode().get() instanceof IfStmt ||
                    b.getParentNode().get() instanceof WhileStmt ||
                    b.getParentNode().get() instanceof SwitchStmt ||
                    b.getParentNode().get() instanceof ForStmt) {

                for (int i=0; i<inputDependantVars.size(); i++){
                    if (b.getTokenRange().get().toString().contains(inputDependantVars.get(i))) {
                        list.add(b);
                    }
                }
            }
        }
    }

    /* Get conditional statements of a specific variable */
    private class ConditionOnVar extends VoidVisitorAdapter<List<Expression>> {

        public ConditionOnVar(String name) {
            super();
            this.name = name;
        }


        @Override
        public void visit (BinaryExpr b, List<Expression> list) {
            super.visit(b, list);
            if (b.getParentNode().get() instanceof IfStmt ||
                    b.getParentNode().get() instanceof WhileStmt ||
                    b.getParentNode().get() instanceof SwitchStmt ||
                    b.getParentNode().get() instanceof ForStmt) {

                if (b.getTokenRange().get().toString().contains(name)) {
                    list.add(b);
                }
            }
        }

        private String name;
    }

    /* Collect the names of variables that depend on args[0] */
    private class ArgdependencyCollector extends VoidVisitorAdapter<List<String>> {

        @Override
        public void visit (ExpressionStmt es, List<String> list) {
            super.visit(es, list);

            int i;
            int size;
            if (es.getExpression() instanceof VariableDeclarationExpr || es.getExpression() instanceof AssignExpr) {
                size = list.size();
                for (i=0; i<size; i++) {
                    if (es.getTokenRange().get().toString().contains(inputDependantVars.get(i))) {
                        if (es.getExpression() instanceof  AssignExpr) {
                            if (!list.contains(((AssignExpr) es.getExpression()).getTarget().toString()))
                                list.add(((AssignExpr) es.getExpression()).getTarget().toString());
                        } else if (es.getExpression() instanceof VariableDeclarationExpr) {
                            if (!list.contains(((VariableDeclarationExpr) es.getExpression()).getVariable(0).getNameAsString()))
                                list.add(((VariableDeclarationExpr) es.getExpression()).getVariable(0).getNameAsString());
                        }
                    }
                }
            }
        }
    }

    /* Collect all integers */
    private class IntegerCollector extends VoidVisitorAdapter<TreeSet<Integer>> {
        @Override
        public void visit (IntegerLiteralExpr i, TreeSet<Integer> list) {
            if (Integer.toString(i.asInt()).length() < 4)
                list.add(i.asInt());
        }
    }

    /* Collect all doubles */
    private class DoubleCollector extends VoidVisitorAdapter<TreeSet<Double>> {
        @Override
        public void visit(DoubleLiteralExpr d, TreeSet<Double> list) {
            if (Double.toString(d.asDouble()).length() < 4)
                list.add(d.asDouble());
        }
    }

    /* Collect all Longs */
    private class LongCollector extends VoidVisitorAdapter<TreeSet<Integer>> {
        @Override
        public void visit(LongLiteralExpr l, TreeSet<Integer> list) {
            if (Long.toString(l.asLong()).length() <= 3)
                list.add((int)l.asLong());
        }
    }

    /* Collect all strings not inside print statements */
    private class StringCollector extends VoidVisitorAdapter<HashSet<String>> {
        @Override
        public void visit(StringLiteralExpr s, HashSet<String> list) {

            Node i = s;
            while ((i=i.getParentNode().get()) instanceof BinaryExpr) { }
            if (!i.getTokenRange().get().toString().contains("System.out")) {
                list.add(s.asString());
            }
        }
    }

}