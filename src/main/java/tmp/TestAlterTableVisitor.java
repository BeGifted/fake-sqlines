package tmp; /**
 * @author gongyuandaye
 * @date 2022/4/26 22:51
 */
import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.parser.SQLParserUtils;
import org.antlr.v4.runtime.tree.ParseTree;
import sql.PlSqlParserBaseVisitor;
import sql.PlSqlParser;
import util.DataTypeTranslator;

import java.util.*;

public class TestAlterTableVisitor extends PlSqlParserBaseVisitor<String> {

    StringBuilder alterStat;
    int type, flagParen;
    List<String> alter_elems;
    String col_now, alter_elem;
    int add, modify, drop;


    public TestAlterTableVisitor(){
        alter_elems = new ArrayList<>();
        alterStat = new StringBuilder("ALTER TABLE ");
        type = -1;
        flagParen = 0;
        alter_elem = "\t";
        add = modify = drop = 0;
    }

    @Override
    public String visitAlter_table(PlSqlParser.Alter_tableContext ctx){
        System.out.println("/*");
        visitChildren(ctx);
        System.out.println("*/");
        int siz = alter_elems.size();
        if(siz >= 1) alterStat.append("(");
        alter_elems.add(alter_elem);
        siz++;
        for(int i = 0; i < siz; i++){
            alterStat.append(alter_elems.get(i));
            if(i < siz - 1) {
                alterStat.append(", \n\t\t");
            }
        }
        if(siz > 1) alterStat.append("\n\t)");
        return alterStat.append(";").toString();
    }

    @Override
    public String visitTableview_name(PlSqlParser.Tableview_nameContext ctx) {
        System.out.println("visitTableview_name：" + ctx.getText());
        alterStat.append(ctx.getText());
        return visitChildren(ctx);
    }

    @Override
    public String visitAdd_column_clause(PlSqlParser.Add_column_clauseContext ctx) {
        System.out.println("visitAdd_column_clause");
        alterStat.append("\nADD ");
        add = 1;
        return visitChildren(ctx);
    }

    @Override
    public String visitColumn_definition(PlSqlParser.Column_definitionContext ctx) {
        System.out.println("visitColumn_definition：" + ctx.getText());
        col_now = ctx.getText();
        if(type == 0) alter_elems.add(alter_elem);
        alter_elem = "\t";
        type = 0; //col
        return visitChildren(ctx);
    }


    @Override
    public String visitColumn_name(PlSqlParser.Column_nameContext ctx) {
        System.out.println("visitColumn_name：" + ctx.getText());
        alter_elem += ctx.getText() + " ";
        return visitChildren(ctx);
    }

    @Override
    public String visitDatatype(PlSqlParser.DatatypeContext ctx) {
        System.out.println("visitDatatype：" + ctx.getText());
        String DataType = DataTypeTranslator.DataTypeTranslator(SQLParserUtils.createExprParser(ctx.getText(), DbType.oracle).parseDataType()).getName();
        DataType = DataType + ctx.getText().substring(ctx.getText().indexOf('('));
        alter_elem += DataType + " ";
        if(col_now.contains("DEFAULT")) {
            alter_elem += "DEFAULT ";
        }
        return visitChildren(ctx);
    }

    //constraint PK_T_DICT primary key(ID)
    @Override
    public String visitOut_of_line_constraint(PlSqlParser.Out_of_line_constraintContext ctx) {
        System.out.println("visitOut_of_line_constraint：" + ctx.getText());
        if(!Objects.equals(alter_elem, "\t")) alter_elems.add(alter_elem);

        StringBuilder constraint = new StringBuilder();
        constraint.append("\t");
        for(int i = 0; i < ctx.getChildCount(); i++){
            ParseTree node = ctx.getChild(i);
            if(Objects.equals(node.getText(), ")")) constraint.deleteCharAt(constraint.length() - 1);
            constraint.append(node.getText());
            if(!Objects.equals(node.getText(), "(")) constraint.append(" ");
        }
        String temp = constraint.toString();
        alter_elems.add(temp);
        type = 1; //constraint
        return visitChildren(ctx);
    }

    //notnull、unique、PRIMARY KEY
    //TODO ref、check
    @Override
    public String visitInline_constraint(PlSqlParser.Inline_constraintContext ctx) {
        System.out.println("visitInline_constraint：" + ctx.getText());
        if(ctx.getText().equals("NOTNULL")){
            alter_elem += "NOT NULL";
        }else{
            alter_elem += ctx.getText();
        }
        return visitChildren(ctx);
    }


    //常数、内置函数
    @Override
    public String visitConstant(PlSqlParser.ConstantContext ctx){
        System.out.println("visitConstant：" + ctx.getText());
        switch (ctx.getText()) {
            case "SYSDATE", "USER" -> {
                alter_elem += ctx.getText() + "() ";
            }
            case "SYSTIMESTAMP" -> {
                alter_elem += "CURRENT_TIMESTAMP ";
            }
            default -> {
                alter_elem += ctx.getText() + " ";
            }
        }

        return visitChildren(ctx);
    }

    @Override
    public String visitStandard_function(PlSqlParser.Standard_functionContext ctx) {
        System.out.println("visitStandard_function：" + ctx.getText());
        TestStandardFuncVisitor loader = new TestStandardFuncVisitor();
        String function = loader.visitStandard_function(ctx);
        alter_elem += function + " ";
        return null;
    }

    @Override
    public String visitGeneral_element(PlSqlParser.General_elementContext ctx) {
        System.out.println("visitGeneral_element：" + ctx.getText());
        TestStandardFuncVisitor loader = new TestStandardFuncVisitor();
        String function = loader.visitGeneral_element(ctx);
        alter_elem += function + " ";
        return null;
    }

    @Override
    public String visitModify_column_clauses(PlSqlParser.Modify_column_clausesContext ctx) {
        System.out.println("visitModify_column_clause");
        alterStat.append("\nMODIFY ");
        modify = 1;
        return visitChildren(ctx);
    }

    @Override
    public String visitModify_col_properties(PlSqlParser.Modify_col_propertiesContext ctx) {
        System.out.println("visitModify_col_properties");
        col_now = ctx.getText();
        if(type == 0) alter_elems.add(alter_elem);
        alter_elem = "\t";
        type = 0; //col
        return visitChildren(ctx);
    }

}
