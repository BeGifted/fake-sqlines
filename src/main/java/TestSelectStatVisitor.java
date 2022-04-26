import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.*;

public class TestSelectStatVisitor extends PlSqlParserBaseVisitor<String> {
//    public ParseTreeProperty<String> Stat = new ParseTreeProperty<String>();
    StringBuilder selectStat;
    String from_clause,where_clause;
    List<String> clauses;
    //包括into_clause? from_clause where_clause? hierarchical_query_clause? group_by_clause? model_clause?
    //order_by_clause? fetch_clause?
    //若转换不影响整体结构，通过clauseVisitor获取
    List<Integer> TransStrategy;

    //判断目前是在读取select属性、from表、还是where变量。
    //可能需要访问同一语法规则中，可以通过switch(readType)区别操作
    private static final int non = -1;
    private static final int select_elem = 0;
    private static final int from_tab  = 1;
    private static final int where_var = 2;
    int readType,counter;
    List<String> select_elems,from_tabs,where_vars;
    //Queue<String> where_exprs,expr_oprs;
    List<String> where_exprs, expr_oprs;

    public TestSelectStatVisitor(){
        readType=non;
        TransStrategy=new ArrayList<>();
        selectStat=new StringBuilder("SELECT ");
        from_clause=new String("");
        where_clause=new String("");
    }


    @Override
    public String visitQuery_block(PlSqlParser.Query_blockContext ctx) {
        //visitChildren(ctx)
        System.out.println("/*");
        visitChildren(ctx);
        System.out.println("*/");
        /*
         @FROM
         @WHERE
         @Outer_join_sign   size+1
         null
         */

        loadStrategy();
        System.out.println(TransStrategy.size());
        if(TransStrategy.size()==0) {
            System.out.println("无需转换");
            return ctx.getText();
        }
        if(!from_clause.isEmpty()){
            selectStat.append(" ").append(from_clause);
            if(!where_clause.isEmpty()){
                selectStat.append(" ").append(where_clause);
            }
            return selectStat.toString();
        }
        if(clauses!=null && !clauses.isEmpty())
            for (String clause : clauses) selectStat.append(clause);
        return selectStat.append(";").toString();
    }

    void loadStrategy(){
        for(int ST:TransStrategy)
            switch (ST){
                case TestStrategies.ReMoveDual -> {
                    from_clause="";
                }
                case TestStrategies.LeftOuterJoin -> {
                    if(from_tabs.size() < 2){     //语义检查
                        System.out.println("异常策略"+ST);
                    }
                    int siz = where_exprs.size();
                    for (int i = 0; i < siz; i++) {
                        String where_expr = where_exprs.get(i);
                        if (where_expr.endsWith("(+)")) {
                            if(i == 0){
                                expr_oprs.remove(i);
                            }else{
                                expr_oprs.remove(i - 1);
                            }
                            from_clause=String.format("FROM %s LEFT OUTER JOIN %s ON %s",
                                    from_tabs.get(0),from_tabs.get(1),
                                    where_expr.replace("(+)",""));
                        }
                    }
                    if(where_exprs.isEmpty())
                        break;
                    StringBuilder whereBulider=new StringBuilder("WHERE ");
                    for(int i = 0; i < siz - 1; i++){
                        String where_expr = where_exprs.get(i);
                        whereBulider.append(where_expr);
                        if(i != siz - 2) whereBulider.append(" ").append(expr_oprs.get(i)).append(" ");
                    }
                    where_clause=whereBulider.toString();
                }
                default -> {
                    System.out.println("异常策略"+ST);
                }
            }
    }

    //select短句
    @Override
    public String visitSelected_list(PlSqlParser.Selected_listContext ctx) {
        counter=-1;
        select_elems=new ArrayList<>();
        readType=select_elem;
        System.out.println("---select开始---");
        visitChildren(ctx);     //可能会调用函数
        System.out.println("---select结束---");
        readType=non;
        for(int i=0;i<select_elems.size();i++){
            selectStat.append(select_elems.get(i));
            System.out.println(select_elems.get(i));
            if(i<select_elems.size()-1)
                selectStat.append(",");
        }
        return null;
    }

    @Override
    public String visitSelect_list_elements(PlSqlParser.Select_list_elementsContext ctx) {
        select_elems.add(ctx.getText());
        System.out.println(ctx.getText());
        counter++;
        return visitChildren(ctx);
    }

    //获取函数
    @Override
    public String visitStandard_function(PlSqlParser.Standard_functionContext ctx) {
        System.out.println("visitStandard_function：" + ctx.getText());
        TestStandardFuncVisiotr loader=new TestStandardFuncVisiotr();
        String function=loader.visitStandard_function(ctx);
        select_elems.set(counter,select_elems.get(counter).replace(ctx.getText(),function));
        return null;
    }

    //LPAD等
    @Override
    public String visitGeneral_element(PlSqlParser.General_elementContext ctx) {
        System.out.println("visitGeneral_element");
        TestStandardFuncVisiotr loader=new TestStandardFuncVisiotr();
        String function=loader.visitGeneral_element(ctx);
        select_elems.set(counter,function);
        return null;
    }

    //    @Override
//    public String visitVariable_name(PlSqlParser.Variable_nameContext ctx) {
//        if (readType == where_var) {
//            where_vars.add(ctx.getText());
//        }
//        return visitChildren(ctx);
//    }

    //from子句
    @Override
    public String visitFrom_clause(PlSqlParser.From_clauseContext ctx) {
        System.out.println("@FROM");
        if(ctx.getText().contains("DUAL")){     //也可以在tableview_name或table_ref识别
            TransStrategy.add(TestStrategies.ReMoveDual);
            return null;
        }
        from_tabs=new ArrayList<>();
        readType=from_tab;
        visitChildren(ctx);
        readType=non;
        return null;
    }

    @Override
    public String visitTable_ref(PlSqlParser.Table_refContext ctx) {
        from_tabs.add(ctx.getText());
        return null;
    }

    //where子句
    @Override
    public String visitWhere_clause(PlSqlParser.Where_clauseContext ctx) {
        System.out.println("@WHERE");
        where_vars=new ArrayList<>();
        where_exprs=new ArrayList<>();
        expr_oprs=new ArrayList<>();
        readType=where_var;
        visitChildren(ctx);     //这里最有可能有方言
        readType=non;
        return null;
    }

    //expression最终一定能划分成一个个Unary_logical_expression
    @Override
    public String visitUnary_logical_expression(PlSqlParser.Unary_logical_expressionContext ctx) {
        visitChildren(ctx);     //where方言通常在Unary_logical_expression子树中
        if(readType==where_var){
            where_exprs.add(ctx.getText());
            System.out.println("where内容：" + ctx.getText());
        }
        return null;
    }

    @Override
    public String visitTerminal(TerminalNode node) {
        String str=node.getText();
        if(str.equals("AND")||str.equals("OR"))
            expr_oprs.add(str);
        return null;
    }

    //下面获取where条件的变量
    @Override
    public String visitTable_element(PlSqlParser.Table_elementContext ctx) {
        if(readType==where_var)
            where_vars.add(ctx.getText());
        return null;
    }

    //下面是where子句的方言识别和转换
    @Override
    public String visitOuter_join_sign(PlSqlParser.Outer_join_signContext ctx) {
        System.out.println("@Outer_join_sign");
        TransStrategy.add(TestStrategies.LeftOuterJoin);
        return null;
    }
}
