package tmp; /**
 * @author gongyuandaye
 * @date 2022/4/26 15:15
 */
import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.parser.SQLParserUtils;
import org.antlr.v4.runtime.tree.ParseTree;
import sql.PlSqlParserBaseVisitor;
import sql.PlSqlParser;
import util.DataTypeTranslator;

import java.util.*;

public class TestCreateTableVisitor extends PlSqlParserBaseVisitor<String> {
    StringBuilder createStat;
    String create_elem, col_now;
    int type;
    List<Integer> TransStrategy;
    List<String> create_elems;
    int counter;

    public TestCreateTableVisitor(){
        TransStrategy = new ArrayList<>();
        create_elems = new ArrayList<>();
        createStat = new StringBuilder("CREATE TABLE ");
        counter = -1;
        create_elem = "\t\t";
        type = -1;
    }

    @Override
    public String visitCreate_table(PlSqlParser.Create_tableContext ctx){
        System.out.println("/*");
        visitChildren(ctx);
        System.out.println("*/");
        int siz = create_elems.size();
        for(int i = 0; i < siz; i++){  //忽略0
            createStat.append(create_elems.get(i));
            if(i < siz - 1) {
                createStat.append(",\n");
            }
        }
        return createStat.append("\n\t\t);").toString();
    }


    @Override
    public String visitDatatype(PlSqlParser.DatatypeContext ctx) {
        System.out.println("visitDatatype：" + ctx.getText());
        String DataType = DataTypeTranslator.DataTypeTranslator(SQLParserUtils.createExprParser(ctx.getText(), DbType.oracle).parseDataType()).getName();
        DataType = DataType + ctx.getText().substring(ctx.getText().indexOf('('));
        create_elem += DataType + " ";
        if(col_now.contains("DEFAULT")) {
            create_elem += "DEFAULT ";
        }
        return visitChildren(ctx);
    }

    @Override
    public String visitColumn_definition(PlSqlParser.Column_definitionContext ctx) {
        System.out.println("visitColumn_definition：" + ctx.getText());
        col_now = ctx.getText();
        if(type == 0) create_elems.add(create_elem);
        create_elem = "\t\t";
        type = 0; //col
        return visitChildren(ctx);
    }


    @Override
    public String visitColumn_name(PlSqlParser.Column_nameContext ctx) {
        System.out.println("visitColumn_name：" + ctx.getText());
        create_elem += ctx.getText() + " ";
        return visitChildren(ctx);
    }

    @Override
    public String visitTableview_name(PlSqlParser.Tableview_nameContext ctx) {
        System.out.println("visitTableview_name：" + ctx.getText());
        createStat.append(ctx.getText()).append(" (\n");
        return visitChildren(ctx);
    }

    //constraint PK_T_DICT primary key(ID)
    @Override
    public String visitOut_of_line_constraint(PlSqlParser.Out_of_line_constraintContext ctx) {
        System.out.println("visitOut_of_line_constraint：" + ctx.getText());
        if(!Objects.equals(create_elem, "\t\t")) create_elems.add(create_elem);

        StringBuilder constraint = new StringBuilder();
        constraint.append("\t\t");
        for(int i = 0; i < ctx.getChildCount(); i++){
            ParseTree node = ctx.getChild(i);
            if(Objects.equals(node.getText(), ")")) constraint.deleteCharAt(constraint.length() - 1);
            constraint.append(node.getText());
            if(!Objects.equals(node.getText(), "(")) constraint.append(" ");
        }
        String temp = constraint.toString();
        create_elems.add(temp);
        type = 1; //constraint
        return visitChildren(ctx);
    }

    //notnull、unique、PRIMARY KEY
    //TODO ref、check
    @Override
    public String visitInline_constraint(PlSqlParser.Inline_constraintContext ctx) {
        System.out.println("visitInline_constraint：" + ctx.getText());
        if(ctx.getText().equals("NOTNULL")){
            create_elem += "NOT NULL";
        }else{
            create_elem += ctx.getText();
        }
        return visitChildren(ctx);
    }



    //常数、内置函数
    @Override
    public String visitConstant(PlSqlParser.ConstantContext ctx){
        System.out.println("visitConstant：" + ctx.getText());
        switch (ctx.getText()) {
            case "SYSDATE", "USER" -> {
                create_elem += ctx.getText() + "() ";
            }
            case "SYSTIMESTAMP" -> {
                create_elem += "CURRENT_TIMESTAMP ";
            }
            default -> {
                create_elem += ctx.getText() + " ";
            }
        }

        return visitChildren(ctx);
    }

    @Override
    public String visitStandard_function(PlSqlParser.Standard_functionContext ctx) {
        System.out.println("visitStandard_function：" + ctx.getText());
        TestStandardFuncVisitor loader = new TestStandardFuncVisitor();
        String function = loader.visitStandard_function(ctx);
        create_elem += function + " ";
        return null;
    }

    @Override
    public String visitGeneral_element(PlSqlParser.General_elementContext ctx) {
        System.out.println("visitGeneral_element：" + ctx.getText());
        TestStandardFuncVisitor loader = new TestStandardFuncVisitor();
        String function = loader.visitGeneral_element(ctx);
        create_elem += function + " ";
        return null;
    }


//    //包含系统变量等
//    @Override
//    public String visitVariable_name(PlSqlParser.Variable_nameContext ctx) {
//        System.out.println("visitVariable_name：" + ctx.getText());
//        switch (ctx.getText()) {
//            case "SYSDATE", "USER" -> {
//                create_elems.set(counter, create_elems.get(counter).replace(ctx.getText(), ctx.getText() + "()"));
//            }
//            case "SYSTIMESTAMP" -> {
//                create_elems.set(counter, create_elems.get(counter).replace(ctx.getText(), "CURRENT_TIMESTAMP"));
//            }
//            default -> {
//                System.out.println("变量尚未录入");
//            }
//        }
//        return visitChildren(ctx);
//    }
}
