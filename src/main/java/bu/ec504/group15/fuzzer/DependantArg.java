package bu.ec504.group15.fuzzer;

import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.type.Type;

import java.util.ArrayList;

public class DependantArg {

    public DependantArg(String name, String type, String assignment, ArrayList<Expression> conditions) {
        this.name = name;
        this.type = type;
        this.assignment = assignment;
        this.conditions = conditions;
    }


    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append(name + " of type " + type + " assigned via '" + assignment + "'\n");
        str.append("    -> Conditions:\n");
        for (int i=0; i<conditions.size(); i++) {
            str.append("        ->" + conditions.get(i).getTokenRange().get().toString() + "\n");
        }
        return str.toString();
    }


    public String getType() { return type; }
    public String getName() { return name; }
    public String getAssignment() { return assignment; }


    private String type;
    private String name;
    private String assignment;

    private ArrayList<Expression> conditions;

}
