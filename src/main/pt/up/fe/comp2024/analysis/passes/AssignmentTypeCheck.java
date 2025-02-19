package pt.up.fe.comp2024.analysis.passes;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2024.analysis.AnalysisVisitor;
import pt.up.fe.comp2024.analysis.Utils;
import pt.up.fe.comp2024.ast.Kind;
import pt.up.fe.comp2024.ast.NodeUtils;

import java.util.HashSet;
import java.util.Objects;

public class AssignmentTypeCheck extends AnalysisVisitor {


    @Override
    public void buildVisitor() {
        addVisit(Kind.ASSIGN_STMT, this::visitAssignStmt);
    }


    private Void visitAssignStmt(JmmNode assignStmt, SymbolTable table) {
        var assignee = assignStmt.getChildren().get(0);
        var value = assignStmt.getChildren().get(1);

        var assigneeType = assignee.get("type");
        var valueType = value.get("type");

        var className = table.getClassName();
        var superClass = table.getSuper();

        if (valueType.equals("self")) {
            valueType = className;
        }

        if (assigneeType.equals("self")) {
            var message = "Cannot assign to 'this'";
            addReport(Report.newError(
                    Stage.SEMANTIC,
                    NodeUtils.getLine(assignStmt),
                    NodeUtils.getColumn(assignStmt),
                    message,
                    null
            ));

            return null;
        }

        //System.out.println(value.getChildren());

        if (!(assignee.getKind().equals(Kind.VAR_REF_EXPR.toString()) ||
                (assignee.getKind().equals(Kind.ARRAY_REF_EXPR.toString()) && assignee.getChildren().get(0).getKind().equals(Kind.VAR_REF_EXPR.toString()))
        )) {
            var message = "Cannot assign to a non variable";
            addReport(Report.newError(
                    Stage.SEMANTIC,
                    NodeUtils.getLine(assignStmt),
                    NodeUtils.getColumn(assignStmt),
                    message,
                    null
            ));

            return null;
        }

        var importSet = Utils.getImports(table);


        if (Objects.equals(assigneeType, valueType)
                || importSet.contains(valueType) && importSet.contains(assigneeType)
                || (!superClass.isEmpty() && superClass.equals(valueType) && importSet.contains(valueType))
                || (importSet.contains(valueType))
                || (!superClass.isEmpty() && superClass.equals(assigneeType) && importSet.contains(assigneeType))
        ) {
            assignStmt.put("type", assigneeType);
        } else {
            var message = "Type mismatch in assignment statement";
            addReport(Report.newError(
                    Stage.SEMANTIC,
                    NodeUtils.getLine(assignStmt),
                    NodeUtils.getColumn(assignStmt),
                    message,
                    null
            ));
        }


        return null;
    }

}
