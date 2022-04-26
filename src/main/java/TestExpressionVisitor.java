import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;

public class TestExpressionVisitor extends PlSqlParserBaseVisitor<String>{
    @Override
    public String visitExpressions(PlSqlParser.ExpressionsContext ctx) {
        StringBuilder expressions=new StringBuilder();
        for(int i=0;i<ctx.getChildCount();i++){
            ParseTree node=ctx.getChild(i);
            if(node instanceof RuleNode)    //expression表达式
                expressions.append(visit(node));
            else                            //标点符号","
                expressions.append(node.getText());
        }
        return expressions.toString();
    }

    @Override
    public String visitExpression(PlSqlParser.ExpressionContext ctx) {
        return ctx.getText();
    }
}
