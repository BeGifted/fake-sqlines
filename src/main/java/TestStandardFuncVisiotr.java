import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;
import sql.PlSqlParser;
import sql.PlSqlParserBaseVisitor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

//有部分内置函数的参数部分转换起来比较复杂，没有普适规律
public class TestStandardFuncVisiotr  extends PlSqlParserBaseVisitor<String> {
    List<String> function_non_trans;    //无需转换的内置函数
    List<String> function_cannot_trans; //无法转换的内置函数
    Map<String, String> function_non_paras;     //不涉及参数、可以直接换函数名
//    List<String> function_with_paras;    //涉及参数的内置函数映射
    String paras_in_paren, func_name;

    public TestStandardFuncVisiotr(){
        paras_in_paren="";
        func_name ="";
        //在下面写死内置函数的映射关系
        function_non_trans=List.of("ABS","ACOS","ASCII","ASIN","ATAN","ATAN2","CEIL","COALESCE","COS","EXP","FLOOR","GREATEST",
                "LEAST","LN","LOCALTIMESTAMP","LOG","LOWER","MOD","NULLIF","POWER","ROUND","SIGN","SIN","SOUNDEX","SQRT","SUBSTR",
                "TAN","TRIM","UPPER");
        function_cannot_trans=List.of("ASCIISTR","BIN_TO_NUM","DECODE","INITCAP","MONTHS_BETWEEN","NEXT_DAY","TRANSLATE",
                "TO_LOB","TO_NCHAR","TO_NCLOB","TO_NUMBER","TO_SINGLE_BYTE","TO_TIMESTAMP","USERENV","VSIZE");
        function_non_paras=new HashMap<>();
        function_non_paras.put("LENGTH","CHAR_LENGTH");
        function_non_paras.put("CHR","CHAR");
        function_non_paras.put("NVL","IFNULL");
        function_non_paras.put("LENGTHB","LENGTH");
        //function_with_paras=List.of("INSTR","LAPD","LTRIM","REPLACE","RPAD","RTRIM","ADD_MONTHS","BITAND","EXTRACT");
    }

    //转换内置函数
    @Override
    public String visitStandard_function(PlSqlParser.Standard_functionContext ctx) {
        visitChildren(ctx);
        System.out.println("func_name：" + func_name);
        System.out.println("ctx：" + ctx.getText());
        if(function_cannot_trans.contains(func_name)||func_name.contains("xml"))    //不能转换
            return "函数无法转换或需要写自定义函数";
        else if(function_non_trans.contains(func_name))                 //兼容
            return ctx.getText();
        else if(function_non_paras.containsKey(func_name))              //函数名不同，用法相同
            return ctx.getText().replace(func_name,function_non_paras.get(func_name));
        else return TransFuncWithParas(ctx);
    }


    //日期类TODO
    String TransFuncWithParas(ParserRuleContext ctx){
        List<String> paraList=null;
        if(paras_in_paren.contains(","))        //有可能只有一个参数
            paraList= List.of(paras_in_paren.split(","));
        System.out.println("func_name：" + func_name);
        switch (func_name) {
            case "INSTR" -> {
                assert paraList != null;
                if (paraList.size() == 2)
                    return ctx.getText();
                else if (paraList.size() == 3)
                    return String.format("LOCATE(%s,%s)",paraList.get(1),paraList.get(0));
            }
            case "LPAD", "RPAD" -> {
                assert paraList != null;
                if (paraList.size() == 2)
                    return String.format("%s(%s,' ')",func_name,paras_in_paren);
                else return ctx.getText();
            }
            case "REPLACE" -> {
                assert paraList != null;
                if (paraList.size() == 2)
                    return String.format("%s(%s,'')",func_name,paras_in_paren);
                else return ctx.getText();
            }
            case "ADD_MONTHS" -> {
                assert paraList != null;
                return String.format("TIMESTAMPADD(MONTH,%s,%s)",paraList.get(1),paraList.get(0));
            }
            case "BITAND"->{
                assert paraList != null;
                return String.format("(%s & %s)",paraList.get(0),paraList.get(1));
            }
            case "CONVERT"->{
                return ctx.getText().replace(","," USING ");
            }
            case "COSH"->{
                return String.format("(EXP(%s) + EXP(-%s)) / 2",paras_in_paren,paras_in_paren);
            }
            case "SINH"->{
                return String.format("(EXP(%s) - EXP(-%s)) / 2",paras_in_paren,paras_in_paren);
            }
            case "EXTRACT"->{
                paraList=List.of(paras_in_paren.split(" "));
                return String.format("%s(%s)",paraList.get(0),paraList.get(2));
            }
            case "REMAINDER"->{
                assert paraList != null;
                return String.format("(%s - %s*ROUND(%s/%s))",paraList.get(0),paraList.get(1),paraList.get(0),paraList.get(1));
            }
            case "TANH"->{
                return String.format("(EXP(2*%s) - 1)/(EXP(2*%s) + 1)",paras_in_paren,paras_in_paren);
            }
            case "TRUNC"->{

            }
            case "SYS_GUID"->{
                return "replace(uuid(), '-', '')";
            }
        }
        return "尚未录入映射模型或需要自定义函数";
    }

    //一些函数的日期相关参数需要变更格式
    String TransDateFormate(String date){
        return date;
    }

    @Override
    public String visitNumeric_function(PlSqlParser.Numeric_functionContext ctx) {
        func_name=ctx.getChild(0).getText();
        paras_in_paren=getTextInBrackets(ctx);
        return null;
    }

    @Override
    public String visitString_function(PlSqlParser.String_functionContext ctx) {
        func_name=ctx.getChild(0).getText();
        paras_in_paren=getTextInBrackets(ctx);
        return null;
    }

    @Override
    public String visitOther_function(PlSqlParser.Other_functionContext ctx) {
        func_name=ctx.getChild(0).getText();
        paras_in_paren=getTextInBrackets(ctx);
        return null;
    }

    //获得括号内的内容，即参数
    public String getTextInBrackets(ParserRuleContext ctx){
        StringBuilder str=new StringBuilder("");
        boolean start=false;
        int i;
        for(i=0;i<ctx.getChildCount();i++){
            if(ctx.getChild(i) instanceof TerminalNode){
                if(Objects.equals(ctx.getChild(i).getText(), "("))
                    start=true;
                else if(Objects.equals(ctx.getChild(i).getText(), ")"))
                    break;
            }else if(start){    //否则说明还有
                break;
            }
            if(start){
                str.append(ctx.getText());
                if(i<ctx.getChildCount()-1)
                    str.append(" ");
            }
        }
//        while (i<ctx.getChildCount()) {
//            str.append(ctx.getChild(i).getText());
//        }
        return str.toString();
    }

    //有些函数没能被概括，函数名写在general_element中
    //还有待研究
    @Override
    public String visitGeneral_element(PlSqlParser.General_elementContext ctx) {
        visitChildren(ctx);
        if(function_cannot_trans.contains(func_name)||func_name.contains("xml"))    //不能转换
            return "函数无法转换或需要写自定义函数";
        else if(function_non_trans.contains(func_name))                 //兼容
            return ctx.getText();
        else if(function_non_paras.containsKey(func_name))              //函数名不同，用法相同
            return ctx.getText().replace(func_name,function_non_paras.get(func_name));
        else return TransFuncWithParas(ctx);
    }

    @Override
    public String visitGeneral_element_part(PlSqlParser.General_element_partContext ctx) {
        func_name=ctx.getChild(0).getText();
        return visitChildren(ctx);
    }

    @Override
    public String visitFunction_argument(PlSqlParser.Function_argumentContext ctx) {
        paras_in_paren=getTextInBrackets(ctx);
        paras_in_paren=paras_in_paren.substring(1,paras_in_paren.length()-2);
        return null;
    }
}
