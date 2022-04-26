import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.*;

import java.util.List;
import java.util.Objects;

//记录一些通用的操作（流程），方便随时化用
public class CommonFunc {
    public void walk(ParseTree t) {
        if ( t instanceof ErrorNode) {
            return;
        }
        else if ( t instanceof TerminalNode) {
            return;
        }
        RuleNode r = (RuleNode)t;
        int n = r.getChildCount();
        for (int i = 0; i<n; i++) {
            walk(r.getChild(i));
        }
    }

    public String getText(ParserRuleContext ctx,int start){
        if(start>=ctx.getChildCount())
            return "";
        StringBuilder str=new StringBuilder("");
        for(int i=start;i<ctx.getChildCount();i++){
            str.append(ctx.getChild(i).getText());
            if(i<ctx.getChildCount()-1)
                str.append(" ");
        }
        return str.toString();
    }

    public void getTextInBrackets(ParserRuleContext ctx){
        StringBuilder builder=new StringBuilder("");
        boolean start=false;
        int i;
        for(i=0;i<ctx.getChildCount();i++){
            if(ctx.getChild(i) instanceof TerminalNode){
                if(Objects.equals(ctx.getChild(i).getText(), "("))
                    start=true;
            }else if(start){    //否则说明还有
                break;
            }
            if(start){
                builder.append(ctx.getText());
                if(i<ctx.getChildCount()-1)
                    builder.append(" ");
            }
        }
        System.out.println(builder.toString());
    }

    String TransFuncWithParas(PlSqlParser.Standard_functionContext ctx){
        StringBuilder str=new StringBuilder("");
        String paras_in_paren=new String("");
        List<String> paraList=null;
        String func_name=new String("");
        if(paras_in_paren.contains(","))        //有可能只有一个参数
            paraList= List.of(paras_in_paren.split(","));
        switch (func_name) {
            case "INSTR" -> {
                assert paraList != null;
                if (paraList.size() == 2)
                    return ctx.getText();
                else if (paraList.size() == 3)
                    return str.append("LOCATE").append("(").append(paraList.get(1)).append(paraList.get(0)).
                            append(paraList.get(2)).append(")").toString();
            }
            case "LPAD", "RPAD" -> {
                assert paraList != null;
                if (paraList.size() == 2)
                    return str.append(func_name).append("(").append(paras_in_paren).append(",' ')").toString();
            }
            case "REPLACE" -> {
                assert paraList != null;
                if (paraList.size() == 2)
                    return str.append(func_name).append("(").append(paras_in_paren).append(",'')").toString();
            }
            case "ADD_MONTHS" -> {
                assert paraList != null;
                return str.append("TIMESTAMPADD").append("(").append("MONTH").append(paraList.get(1)).
                        append(paraList.get(0)).append(")").toString();
            }
            case "BITAND"->{
                assert paraList != null;
                return str.append(paraList.get(0)).append(" & ").append(paraList.get(1)).toString();
            }
            case "CONVERT"->{
                return ctx.getText().replace(","," USING ");
            }
            case "COSH"->{
                return str.append("(EXP(").append(paras_in_paren).append(") + EXP(-").append(paras_in_paren).append(")) / 2").toString();
            }
            case "SINH"->{
                return str.append("(EXP(").append(paras_in_paren).append(") - EXP(-").append(paras_in_paren).append(")) / 2").toString();
            }
            case "EXTRACT"->{
                paraList=List.of(paras_in_paren.split(" "));
                return str.append(paraList.get(0)).append("(").append(paraList.get(2)).append(")").toString();
            }
            case "REMAINDER"->{
                assert paraList != null;
                return str.append("(").append(paraList.get(0)).append("-").append(paraList.get(1)).append("*ROUND(")
                        .append(paraList.get(0)).append("/").append(paraList.get(1)).append("))").toString();
            }
            case "TANH"->{
                return str.append("(EXP(2*").append(paras_in_paren).append(") - 1)/(EXP(2*").
                        append(paras_in_paren).append(") + 1)").toString();
            }
            case "TRUNC"->{

            }
        }
        return "尚未录入映射模型或需要自定义函数";
    }
}
