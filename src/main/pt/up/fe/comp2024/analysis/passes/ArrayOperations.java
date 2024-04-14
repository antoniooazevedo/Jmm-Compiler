package pt.up.fe.comp2024.analysis.passes;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2024.analysis.AnalysisVisitor;
import pt.up.fe.comp2024.analysis.Utils;
import pt.up.fe.comp2024.ast.Kind;
import pt.up.fe.comp2024.ast.NodeUtils;


/**
 * Checks all the array operations
 *
 */
public class ArrayOperations extends AnalysisVisitor {

    private String currentMethod;

    @Override
    public void buildVisitor() {
        addVisit(Kind.ARRAY_REF_EXPR, this::visitArrayRefExpr);
        addVisit(Kind.ARRAY_INIT_EXPR, this::visitArrayInitExpr);
        addVisit(Kind.METHOD_DECL, this::visitMethodDecl);

    }

    private Void visitMethodDecl(JmmNode methodDecl, SymbolTable table) {
        currentMethod = methodDecl.get("name");
        return null;
    }

    private Void visitArrayRefExpr(JmmNode arrayRefExpr, SymbolTable table) {
        var array = arrayRefExpr.getChildren().get(0);
        var arrayType = Utils.getOperandType(array, table, currentMethod);
        if (arrayType != null && !arrayType.contains("[]") && !arrayType.equals("vararg")) {
            var message = String.format("Cannot perform array access on a non-array variable '%s'", array.get("name"));
            addReport(Report.newError(
                    Stage.SEMANTIC,
                    NodeUtils.getLine(arrayRefExpr),
                    NodeUtils.getColumn(arrayRefExpr),
                    message,
                    null
            ));
        }

        var index = arrayRefExpr.getChildren().get(1);
        var indexType = Utils.getOperandType(index, table, currentMethod);

        if (indexType != null && !indexType.equals("int")) {
            var message = String.format("Array index must be an integer, but found '%s'", indexType);
            addReport(Report.newError(
                    Stage.SEMANTIC,
                    NodeUtils.getLine(arrayRefExpr),
                    NodeUtils.getColumn(arrayRefExpr),
                    message,
                    null
            ));
        }

        return null;

    }

    private Void visitArrayInitExpr(JmmNode arrayInitExpr, SymbolTable table) {
        for (JmmNode child : arrayInitExpr.getChildren()) {
            String childType = Utils.getOperandType(child, table, currentMethod);
            System.out.println(childType);
            if (childType == null || !childType.equals("int")) {
                var message = String.format("Array initialization expects integers, but found '%s'", childType);
                addReport(Report.newError(
                        Stage.SEMANTIC,
                        NodeUtils.getLine(arrayInitExpr),
                        NodeUtils.getColumn(arrayInitExpr),
                        message,
                        null
                ));
            }
        }
        return null;
    }
}
