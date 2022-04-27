import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import sql.PlSqlParser;
import sql.PlSqlParserBaseVisitor;

public class TestExpressionVisitor extends PlSqlParserBaseVisitor<String> {
    @Override
    public String visitExpressions(PlSqlParser.ExpressionsContext ctx) {
        StringBuilder expressions = new StringBuilder();
        for(int i = 0; i < ctx.getChildCount(); i++){
            ParseTree node = ctx.getChild(i);
            if(node instanceof RuleNode)    //expression表达式
                expressions.append(visit(node));
            else
                expressions.append(node.getText());
        }
        return expressions.toString();
    }

    @Override
    public String visitExpression(PlSqlParser.ExpressionContext ctx) {
        return ctx.getText();
    }
}
