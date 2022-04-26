import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import sql.PlSqlLexer;
import sql.PlSqlParser;

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
        String sqlSelect4 = "SELECT SYS_GUID(),SYSDATE,'oracle' FROM DUAL;";
        //createTest
        String sqlCreate1 = "\"CREATE TABLE T_DICT (\\n\" +\n" +
                "                \"  ID VARCHAR2(32) DEFAULT sys_guid()  NOT NULL ,\\n\" +\n" +
                "                \"  CLASS_ID VARCHAR2(20) ,\\n\" +\n" +
                "                \"  CLASS_NAME VARCHAR2(100) ,\\n\" +\n" +
                "                \"  CLASS_ENAME VARCHAR2(2000) ,\\n\" +\n" +
                "                \"  KEY_ID VARCHAR2(20) ,\\n\" +\n" +
                "                \"  KEY_NAME VARCHAR2(500) ,\\n\" +\n" +
                "                \"  KEY_ENAME VARCHAR2(2000) ,\\n\" +\n" +
                "                \"  OR_VALID_D VARCHAR2(1) DEFAULT 0 ,\\n\" +\n" +
                "                \"  MEMO VARCHAR2(2000) ,\\n\" +\n" +
                "                \"  TIME_MARK TIMESTAMP(6) ,\\n\" +\n" +
                "                \"  STA VARCHAR2(10) DEFAULT 1 ,\\n\" +\n" +
                "                \"  KEY_SEQ NUMBER(20) ,\\n\" +\n" +
                "                \"  CREATE_PRSN VARCHAR2(64) ,\\n\" +\n" +
                "                \"  CREATE_TIME TIMESTAMP(6) DEFAULT systimestamp ,\\n\" +\n" +
                "                \"  UPDATE_PRSN VARCHAR2(64) ,\\n\" +\n" +
                "                \"  UPDATE_TIME TIMESTAMP(6) DEFAULT systimestamp ,\\n\" +\n" +
                "                \"  FDELETE_ID VARCHAR2(1) DEFAULT 0  NOT NULL ,\\n\" +\n" +
                "                \"  constraint PK_T_DICT primary key(ID)\\n\" +\n" +
                "                \");\"";
        ANTLRInputStream input = new ANTLRInputStream(sqlSelect1);   //将输入转成antlr的input流
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
