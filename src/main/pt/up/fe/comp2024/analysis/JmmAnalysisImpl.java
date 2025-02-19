package pt.up.fe.comp2024.analysis;

import pt.up.fe.comp.jmm.analysis.JmmAnalysis;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2024.analysis.optimization.ConstantFolding;
import pt.up.fe.comp2024.analysis.optimization.ConstantPropagation;
import pt.up.fe.comp2024.analysis.passes.*;
import pt.up.fe.comp2024.symboltable.JmmSymbolTableBuilder;

import java.util.ArrayList;
import java.util.List;

public class JmmAnalysisImpl implements JmmAnalysis {


    private final List<AnalysisPass> analysisPasses;

    public JmmAnalysisImpl() {

        this.analysisPasses = List.of(new NodesTypesCheck(),new DiffChecks(), new UndeclaredVariable(),
                new OperationTypesCheck(), new ArrayOperationsCheck(), new MethodsChecks(), new AssignmentTypeCheck(),
                new ExpressionConditionTypeCheck(), new ParamChecks(), new ThisOperationsCheck(), new ReturnCheck());

    }

    @Override
    public JmmSemanticsResult semanticAnalysis(JmmParserResult parserResult) {

        JmmNode rootNode = parserResult.getRootNode();

        SymbolTable table = JmmSymbolTableBuilder.build(rootNode);

        List<Report> reports = new ArrayList<>();

        // Visit all nodes in the AST
        for (var analysisPass : analysisPasses) {
            try {
                var passReports = analysisPass.analyze(rootNode, table);
                reports.addAll(passReports);
            } catch (Exception e) {
                reports.add(Report.newError(Stage.SEMANTIC,
                        -1,
                        -1,
                        "Problem while executing analysis pass '" + analysisPass.getClass() + "'",
                        e)
                );
            }

        }

        if(parserResult.getConfig().get("optimize") != null && parserResult.getConfig().get("optimize").equals("true")){
            ConstantPropagation constantPropagationVisitor = new ConstantPropagation();
            ConstantFolding constantFoldingVisitor = new ConstantFolding();
            boolean modified;
            do {
                constantPropagationVisitor.optimize(rootNode, table);
                constantFoldingVisitor.optimize(rootNode, table);
                modified = constantPropagationVisitor.modified || constantFoldingVisitor.modified;
            } while (modified);

        }
        return new JmmSemanticsResult(parserResult, table, reports);
    }
}
