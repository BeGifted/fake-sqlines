/**
 * @author gongyuandaye
 * @date 2022/4/26 15:15
 */
import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.parser.SQLParserUtils;
import sql.PlSqlParserBaseVisitor;
import sql.PlSqlParser;
import util.DataTypeTranslator;

import java.util.*;

public class TestCreateTableVisitor extends PlSqlParserBaseVisitor<String> {
    StringBuilder createStat;
    List<Integer> TransStrategy;
    List<String> createColName_elems, createDataType_elems;
    int counter;

    public TestCreateTableVisitor(){
        TransStrategy = new ArrayList<>();
        createColName_elems = new ArrayList<>();
        createDataType_elems = new ArrayList<>();
        createStat = new StringBuilder("CREATE TABLE ");
        counter = -1;
    }

    @Override
    public String visitCreate_table(PlSqlParser.Create_tableContext ctx){
        System.out.println("/*");
        visitChildren(ctx);
        System.out.println("*/");

        //loadStrategy();
        System.out.println(TransStrategy.size());
        if(TransStrategy.size()==0) {
            System.out.println("无需转换");
            return ctx.getText();
        }

        return createStat.append(");").toString();
    }


    @Override
    public String visitDatatype(PlSqlParser.DatatypeContext ctx) {
        System.out.println("visitDatatype：" + ctx.getText());
        String DataType = DataTypeTranslator.DataTypeTranslator(SQLParserUtils.createExprParser(ctx.getText(), DbType.oracle).parseDataType()).getName();
        return visitChildren(ctx);
    }

    @Override
    public String visitColumn_definition(PlSqlParser.Column_definitionContext ctx) {
        System.out.println("visitColumn_definition：" + ctx.getText());
        return visitChildren(ctx);
    }


    @Override
    public String visitColumn_name(PlSqlParser.Column_nameContext ctx) {
        System.out.println("visitColumn_name：" + ctx.getText());
        createColName_elems.add(ctx.getText());
        return visitChildren(ctx);
    }

    @Override
    public String visitTableview_name(PlSqlParser.Tableview_nameContext ctx) {
        System.out.println("visitTableview_name：" + ctx.getText());
        createStat.append(ctx.getText() + " (\n");
        return visitChildren(ctx);
    }


    //常数
    @Override
    public String visitConstant(PlSqlParser.ConstantContext ctx){
        System.out.println("visitConstant：" + ctx.getText());
        return visitChildren(ctx);
    }

    @Override
    public String visitStandard_function(PlSqlParser.Standard_functionContext ctx) {
        System.out.println("visitStandard_function：" + ctx.getText());
        TestStandardFuncVisitor loader = new TestStandardFuncVisitor();
        String function = loader.visitStandard_function(ctx);
        //create_elems.set(counter,create_elems.get(counter).replace(ctx.getText(),function));
        return null;
    }

    @Override
    public String visitGeneral_element(PlSqlParser.General_elementContext ctx) {
        System.out.println("visitGeneral_element：" + ctx.getText());
        TestStandardFuncVisitor loader = new TestStandardFuncVisitor();
        String function = loader.visitGeneral_element(ctx);
        //create_elems.set(counter,create_elems.get(counter).replace(ctx.getText(),function));
        return null;
    }


    //包含系统变量等
    @Override
    public String visitVariable_name(PlSqlParser.Variable_nameContext ctx) {
        System.out.println("visitVariable_name：" + ctx.getText());
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
        return visitChildren(ctx);
    }
}
