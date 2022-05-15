package tmp;

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
        System.out.println(SQLtmp.SQL);
        TestSelectStatVisitor loader=new TestSelectStatVisitor();
        String stat=loader.visitQuery_block(ctx);
        System.out.println("Select SQL:\n"+stat);
        SQLtmp.SQL2 += stat + "\n\n";
        return null;
    }

    @Override
    public Void visitStandard_function(PlSqlParser.Standard_functionContext ctx) {
        TestStandardFuncVisitor loader = new TestStandardFuncVisitor();
        String function = loader.visitStandard_function(ctx);
        System.out.println("Function SQL:\n"+function);
        SQLtmp.SQL2 += function + "\n\n";
        return null;
    }

    @Override
    public Void visitCreate_table(PlSqlParser.Create_tableContext ctx){
        TestCreateTableVisitor loader = new TestCreateTableVisitor();
        String function = loader.visitCreate_table(ctx);
        System.out.println("Create SQL:\n"+function);
        SQLtmp.SQL2 += function + "\n\n";
        return null;
    }

    @Override
    public Void visitAlter_table(PlSqlParser.Alter_tableContext ctx){
        TestAlterTableVisitor loader = new TestAlterTableVisitor();
        String function = loader.visitAlter_table(ctx);
        System.out.println("Alter SQL:\n"+function);
        SQLtmp.SQL2 += function + "\n\n";
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
