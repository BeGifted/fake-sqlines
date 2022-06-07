package tmp;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import sql.PlSqlParser;
import sql.PlSqlParserBaseVisitor;

import java.util.*;
import java.util.regex.Pattern;

//有部分内置函数的参数部分转换起来比较复杂，没有普适规律
public class TestStandardFuncVisitor extends PlSqlParserBaseVisitor<String> {
    List<String> function_non_trans;    //无需转换的内置函数
    List<String> function_cannot_trans; //无法转换的内置函数
    Map<String, String> function_non_paras;     //不涉及参数、可以直接换函数名
//    List<String> function_with_paras;    //涉及参数的内置函数映射
    String paras_in_paren, func_name;

    public TestStandardFuncVisitor(){
        paras_in_paren="";
        func_name ="";
        //在下面写死内置函数的映射关系
        function_non_trans=List.of("ABS","ACOS","ASCII","ASIN","ATAN","ATAN2","CEIL",
                "COALESCE","COS","EXP","FLOOR","GREATEST",
                "LEAST","LN","LOCALTIMESTAMP","LOG","LOWER",
                "MOD","NULLIF","POWER","ROUND","SIGN","SIN",
                "SOUNDEX","SQRT","SUBSTR","TAN","TRIM","UPPER");
        function_cannot_trans=List.of("ASCIISTR","BIN_TO_NUM","DECODE","INITCAP",
                "MONTHS_BETWEEN","NEXT_DAY","TRANSLATE",
                "TO_LOB","TO_NCHAR","TO_NCLOB","TO_NUMBER",
                "TO_SINGLE_BYTE","TO_TIMESTAMP","USERENV","VSIZE");
        function_non_paras=new HashMap<>();
        function_non_paras.put("LENGTH","CHAR_LENGTH");
        function_non_paras.put("CHR","CHAR");
        function_non_paras.put("NVL","IFNULL");
        function_non_paras.put("NVL2","IF");
        function_non_paras.put("HEXTORAW","UNHEX");
        function_non_paras.put("LENGTHB","LENGTH");
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

    String TransFuncWithParas(ParserRuleContext ctx){
        List<String> paraList = new ArrayList<>();
        if(paras_in_paren.contains(","))        //有可能只有一个参数
            paraList = List.of(paras_in_paren.split(","));
        else
            paraList.add(paras_in_paren);
        System.out.println("paraList：" + paraList);
        switch (func_name) {
            case "INSTR" -> {
                if (paraList.size() == 2)
                    return ctx.getText();
                else if (paraList.size() == 3)
                    return String.format("LOCATE(%s,%s,%s)",paraList.get(1),paraList.get(0),paraList.get(2));
            }
            case "LPAD", "RPAD" -> {
                if (paraList.size() == 2)
                    return String.format("%s(%s,' ')",func_name,paras_in_paren);
                else return ctx.getText();
            }
            case "REPLACE" -> {
                if (paraList.size() == 2)
                    return String.format("%s(%s,'')",func_name,paras_in_paren);
                else return ctx.getText();
            }
            case "ADD_MONTHS" -> {
                return String.format("TIMESTAMPADD(MONTH,%s,%s)",paraList.get(1),SpecialTrans(paraList.get(0)));
            }
            case "BITAND"->{
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
                return String.format("%s(%s)",paraList.get(0),SpecialTrans(paraList.get(2)));
            }
            case "REMAINDER"->{
                return String.format("(%s - %s*ROUND(%s/%s))",paraList.get(0),paraList.get(1),paraList.get(0),paraList.get(1));
            }
            case "TANH"->{
                return String.format("(EXP(2*%s) - 1)/(EXP(2*%s) + 1)",paras_in_paren,paras_in_paren);
            }
            case "TRUNC"->{
                if(Pattern.compile("^[-\\+]?[\\d]*$").matcher(paraList.get(0)).matches()){
                    if(paraList.size()==1)
                        return String.format("TRUNCATE(%s,0)",paras_in_paren);
                    else return String.format("TRUNCATE(%s)",paras_in_paren);
                }else{
                    if(paraList.size() == 1){
                        return String.format("TRUNCATE(%s)",SpecialTrans(paraList.get(0)));
                    }else if(paraList.get(1).equals("'DD'")){
                        return String.format("TRUNCATE(%s)",SpecialTrans(paraList.get(0)));
                    }else {
                        return String.format("DATE_FORMAT(%s, '%Y-%m-01')",SpecialTrans(paraList.get(0)));
                    }
                }
            }
            case "SYS_GUID"->{
                return "replace(uuid(), '-', '')";
            }
            case "LISTAGG"->{
                StringBuilder sb = new StringBuilder();
                sb.append("GROUP_CONCAT(");
                if(paraList.get(1).equals("'") && paraList.get(2).equals("'")) {
                    sb.append(paraList.get(0)).append(" ");
                    sb.append("ORDER BY ").append(paraList.get(3)).append(" SEPARATOR ").append("',' ").append(")");
                }else {
                    sb.append(paraList.get(0)).append(" ");
                    sb.append("ORDER BY ").append(paraList.get(2)).append(" SEPARATOR ").append(paraList.get(1)).append(")");
                }
                return sb.toString();
            }
            case "TO_CHAR"->{
                if(Pattern.compile("^[-\\+]?[\\d]*$").matcher(paraList.get(0)).matches()){
                    //TODO TO_CHAR(number, format)
                    //规则非常复杂 http://oracle.chinaitlab.com/exploiture/798048.html
                }else{
                    return String.format("DATE_FORMAT(%s,%s)",SpecialTrans(paraList.get(0)),SpecialTrans(paraList.get(1)));
                }
            }
            case "TO_DATE"->{
                return String.format("STR_TO_DATE(%s, %s)",paraList.get(0),SpecialTrans(paraList.get(1)));
            }
            case "TO_TIMESTAMP"->{
                return String.format("TO_TIMESTAMP(%s)",SpecialTrans(paraList.get(0)));
            }
            case "TO_NUMBER"->{
                //TODO TO_NUMBER

            }

        }
        return "尚未录入映射模型或需要自定义函数";
    }

    /**
     * 获取小数点多少位
     */
    Integer getDecimalPlace(String str) {
        int round = 0;
        if (str.indexOf(".") > 0) {
            round = str.split("\\.")[1].length();
        }
        return round;
    }

    //Oracle保留字或日期函数参数格式
    String SpecialTrans(String para){
        switch (para){
            case "CURRENT_DATE","CURRENT_TIMESTAMP"->{  return "NOW()"; }
            case "SYSDATE","SYSDATE "->{   return "SYSDATE()"; }
            case "SYSTIMESTAMP"->{  return "CURRENT_TIMESTAMP"; }
            case "USER"->{  return "USER()";    }
        }
        List<List<String>> FS=List.of(
                List.of("YYYY","%Y"),List.of("YY","%y"),List.of("RRRR","%Y"),List.of("RR","%y"),
                List.of("MON","%b"),List.of("MONTH","%M"),List.of("MM","%m"),List.of("DY","%a"),
                List.of("DD","%d"),List.of("HH24","%H"),List.of("HH","%h"),List.of("HH12","%h"),
                List.of("MI","%i"),List.of("SS","%s")
        );
        for(List<String> map:FS)
            if(para.contains(map.get(0))) {
                para = para.replace(map.get(0), map.get(1));
            }
        return para;
    }

    @Override
    public String visitNumeric_function(PlSqlParser.Numeric_functionContext ctx) {
        func_name = ctx.getChild(0).getText();
        paras_in_paren = getTextInBrackets(ctx);
        return visitChildren(ctx);
    }

    @Override
    public String visitString_function(PlSqlParser.String_functionContext ctx) {
        func_name=ctx.getChild(0).getText();
        paras_in_paren=getTextInBrackets(ctx);
        return visitChildren(ctx);
    }

    @Override
    public String visitOther_function(PlSqlParser.Other_functionContext ctx) {
        func_name=ctx.getChild(0).getText();
        paras_in_paren=getTextInBrackets(ctx);
        return visitChildren(ctx);
    }

    //获得参数
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

    @Override
    public String visitOrder_by_clause(PlSqlParser.Order_by_clauseContext ctx) {
        int siz = ctx.getChildCount();
        paras_in_paren += "," + ctx.getChild(siz - 1).getText();
        return null;
    }

}
