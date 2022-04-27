import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.tree.TerminalNode;
import sql.PlSqlParser;
import sql.PlSqlParserBaseVisitor;

import java.util.ArrayList;
import java.util.List;

public class TestTreeVisitor extends PlSqlParserBaseVisitor<Void> {
    public ParseTreeProperty<String> Stat = new ParseTreeProperty<String>();

//    @Override
//    public Void visitConstant(PlSqlParser.ConstantContext ctx) {
//        System.out.println(ctx.getText());
//        return null;
//    }

    @Override
    public Void visitQuery_block(PlSqlParser.Query_blockContext ctx) {
        TestSelectStatVisitor loader=new TestSelectStatVisitor();
        String stat=loader.visitQuery_block(ctx);
        System.out.println("转换后的Select语句为:\n\t"+stat);
        //Stat.put(ctx,datatype);
        return null;
    }

    @Override
    public Void visitStandard_function(PlSqlParser.Standard_functionContext ctx) {
        TestStandardFuncVisitor loader = new TestStandardFuncVisitor();
        String function = loader.visitStandard_function(ctx);
        System.out.println("转换后的Function为:\n\t"+function);
        return null;//阻止向下访问，同时结束函数，释放调用TestTypeAndFuncVisitor占用的空间
    }

    @Override
    public Void visitCreate_table(PlSqlParser.Create_tableContext ctx){
        TestCreateTableVisitor loader = new TestCreateTableVisitor();
        String function = loader.visitCreate_table(ctx);
        System.out.println("转换后的Create语句为:\n\t"+function);
        return null;
    }



    //RuleContext.getText()代码
    public String getText(ParseTree ctx) {
        int cnt=ctx.getChildCount();
        if (cnt == 0) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < cnt; i++) {
            builder.append(ctx.getChild(i).getText()).append(" ");
        }

        return builder.toString();
    }

    //ParserRuleContext.getTokens()代码
    public List<TerminalNode> getTokens(ParserRuleContext ctx) {
        if ( ctx.children==null ) {
            return null;
        }

        List<TerminalNode> tokens = new ArrayList<TerminalNode>();
        for (ParseTree o : ctx.children) {
            if (o instanceof TerminalNode tnode) {
                tokens.add(tnode);
            }
        }

        return tokens;
    }
}
