import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.IOException;

public class Translator {
    public static void main(String[] args) throws IOException {
        //selectTest
        String sqlSelect1= "SELECT LPAD('abc',7),'def' FROM DUAL;";
        String sqlSelect2 = "SELECT\n" +
                "    \"dname\",\n" +
                "    \"loc\"\n" +
                "FROM\n" +
                "    \"emp\",\n" +
                "    \"dept\"\n" +
                "WHERE \"salary\" > 50000\n" +
                "    AND \"emp\".\"deptno\" = \"dept\".\"deptno\"(+) ";
        String sqlSelect3 = "SELECT '='||TRIM(' HELLO ')||'=' FROM DUAL;";
        //createTest
        String sqlCreate1 = "CREATE TABLE TB;";
        ANTLRInputStream input = new ANTLRInputStream(sqlSelect3);   //将输入转成antlr的input流
        //词法分析
        PlSqlLexer lexer = new PlSqlLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);  //转成token流
        tokens.fill();
        System.out.println("token打印：" + tokens.getTokens());
        //语法分析
        PlSqlParser parser = new PlSqlParser(tokens);
        PlSqlParser.Sql_scriptContext tree = parser.sql_script();
//        PlSqlParser.Query_blockContext tree = parser.query_block();
//        PlSqlParser.Standard_functionContext tree=parser.standard_function();
        TestTreeVisitor loader = new TestTreeVisitor();
        loader.visit(tree);
    }
}
