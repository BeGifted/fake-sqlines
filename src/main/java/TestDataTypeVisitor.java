import sql.PlSqlParser;
import sql.PlSqlParserBaseVisitor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestDataTypeVisitor extends PlSqlParserBaseVisitor<String> {
    List<String> datatype_non_trans;    //无需转换的数据类型
//    List<String> datatype_cannot_trans; //无法转换的数据类型
    Map<String, String> datatype_non_paras;     //不涉及参数的数据类型映射
    Map<String, List<String>> datatype_with_paras;    //涉及参数的数据类型映射（存储结构待定）
    List<Integer> datatype_paras;       //参数（可能有两个参数，所以用List）

    String paras_in_paren, type_name;

    public TestDataTypeVisitor(){
        paras_in_paren="";
        type_name ="";
        //在下面写死数据类型的映射关系
        //of方法返回的是一个不能改变的集合，不能再用add()和put(K,V)方法添加元素
        datatype_non_trans=List.of("DEC","DECIMAL","INT","NUMERIC","VARCHAR");

        datatype_non_paras=new HashMap<>();
        datatype_non_paras.put("BINARY_FLOAT","FLOAT");
        datatype_non_paras.put("BINARY_DOUBLE","DOUBLE");
        datatype_non_paras.put("BLOB","LONGBLOB");
        datatype_non_paras.put("CLOB","LONGTEXT");
        datatype_non_paras.put("DATE","DATETIME");
        datatype_non_paras.put("INTEGER","INT");
        datatype_non_paras.put("LONG","LONGTEXT");
        datatype_non_paras.put("RAW","VARBINARY");
        datatype_non_paras.put("REAL","DOUBLE");
        datatype_non_paras.put("TIMESTAMP","DATETIME");
        datatype_non_paras.put("UROWID","VARCHAR");
        datatype_non_paras.put("VARCHAR2","VARCHAR");

        datatype_with_paras=Map.of(
                "CHAR",List.of("CHAR","VARCHAR"),
                "NCHAR",List.of("NCHAR","NVARCHAR"),
                "NUMBER",List.of("TINYINT","SMALLINT","INT","BIGINT","DECIMAL")
        );
    }

    //转换数据类型
    @Override
    public String visitDatatype(PlSqlParser.DatatypeContext ctx) {
        visitChildren(ctx);
        StringBuilder dtype = new StringBuilder("");
        if(type_name.isEmpty()){    //没访问到native_datatype_element，即Oracle自带的INTERVAL类型
            dtype.append("VARCHAR(30)");
            return dtype.toString();
        }
        else{
            if(datatype_non_trans.contains(type_name))            //同名且使用方法相同的数据类型
                dtype.append(ctx.getText());
            else if(datatype_non_paras.containsKey(type_name)) {    //不同名但不用关心参数的数据类型
                dtype.append(datatype_non_paras.get(type_name)).append(paras_in_paren);
            }
            else{                                                //更为复杂的是数据类型映射
                List<String> tgt_type=datatype_with_paras.get(type_name);
                if(tgt_type.isEmpty())
                    return dtype.append(type_name).append("尚未录入或无映射类型！").toString();
                switch (type_name) {
                    case "CHAR", "NCHAR" -> {
                        if (datatype_paras.size() > 1) { System.out.println("参数出错"); }
                        dtype.append(datatype_paras.get(0) <= 255 ? tgt_type.get(0) : tgt_type.get(1)).append(paras_in_paren);
                    }
                    case "NUMBER" -> {
                        if (datatype_paras.size() > 2){ System.out.println("参数出错"); }
                        if (datatype_paras.size() == 2 || datatype_paras.get(0) >= 19)
                            dtype.append(tgt_type.get(4)).append(paras_in_paren);
                        else if(datatype_paras.get(0) >= 9)
                            dtype.append(tgt_type.get(3));
                        else if(datatype_paras.get(0) >= 5)
                            dtype.append(tgt_type.get(2));
                        else if(datatype_paras.get(0) >= 3)
                            dtype.append(tgt_type.get(1));
                        else
                            dtype.append(tgt_type.get(0));
                    }
                    default -> dtype.append("VARCHAR").append(paras_in_paren);
                }
            }
        }
        //如果还有(WITH LOCAL? TIME ZONE | CHARACTER SET char_set_name)，添加上去
        for(int i=(paras_in_paren.isEmpty()?1:2);i<ctx.getChildCount();i++){
            dtype.append(ctx.getChild(i).getText());
            if(i<ctx.getChildCount()-1)
                dtype.append(" ");
        }
        return dtype.toString();
    }

    //原数据类型
    @Override
    public String visitNative_datatype_element(PlSqlParser.Native_datatype_elementContext ctx) {
        type_name =ctx.getText();
        //System.out.println("数据类别是"+src_type);
        return null;
    }

    //数据类型的括号及里面的参数部分
    @Override
    public String visitPrecision_part(PlSqlParser.Precision_partContext ctx) {
        paras_in_paren=ctx.getText();
        if(paras_in_paren.isEmpty())
            return null;
        if(paras_in_paren.contains(","))
            //获取参数（数据类型的参数都是整数）
            //List<String> raw_list = List.of(paras_in_paren.substring(1, ctx.getText().length() - 1).split(","));
            //把List<String>转换为List<Integer>
            //List<Integer> datatype_paras=raw_list.stream().map(Integer::parseInt).collect(Collectors.toList());
            //合并为一句
            datatype_paras = Stream.of(paras_in_paren.substring(1, paras_in_paren.length() - 1).split(",")).
                    map(Integer::parseInt).collect(Collectors.toList());
        else{
            int para=Integer.parseInt(paras_in_paren.substring(1, paras_in_paren.length() - 1));
            datatype_paras=new ArrayList<>();
            datatype_paras.add(para);
        }
        return null;
    }
}
