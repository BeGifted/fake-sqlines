import junit.framework.TestCase;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import sql.PlSqlLexer;
import sql.PlSqlParser;

public class TranslatorTest extends TestCase{

    public static String SQL;

    public void testSqlSelect1() {
        //selectTest
        String sqlSelect1= "SELECT LPAD('abc',7),'def' FROM DUAL;" +
                "SELECT TRUNC(SYSDATE) FROM DUAL;";
        SQLtmp.SQL = sqlSelect1;
        CharStream input = CharStreams.fromString(sqlSelect1);   //将输入转成antlr的input流
        //词法分析
        PlSqlLexer lexer = new PlSqlLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);  //转成token流
        tokens.fill();
        System.out.println("token打印：" + tokens.getTokens());
        //语法分析
        PlSqlParser parser = new PlSqlParser(tokens);
        PlSqlParser.Sql_scriptContext tree = parser.sql_script();
        TestTreeVisitor loader = new TestTreeVisitor();
        loader.visit(tree);
    }
    public void testSqlSelect2() {
        //selectTest
        String sqlSelect2 = "SELECT\n" +
                "    \"dname\",\n" +
                "    \"loc\"\n" +
                "FROM\n" +
                "    \"emp\",\n" +
                "    \"dept\"\n" +
                "WHERE \"salary\" > 50000\n" +
                "    AND \"emp\".\"deptno\" = \"dept\".\"deptno\"(+) ";
        CharStream input = CharStreams.fromString(sqlSelect2);   //将输入转成antlr的input流
        //词法分析
        PlSqlLexer lexer = new PlSqlLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);  //转成token流
        tokens.fill();
        System.out.println("token打印：" + tokens.getTokens());
        //语法分析
        PlSqlParser parser = new PlSqlParser(tokens);
        PlSqlParser.Sql_scriptContext tree = parser.sql_script();
        TestTreeVisitor loader = new TestTreeVisitor();
        loader.visit(tree);
    }
    public void testSqlSelect3() {
        //selectTest
        String sqlSelect3 = "SELECT '='||TRIM(' HELLO ')||'=' FROM DUAL;";
        CharStream input = CharStreams.fromString(sqlSelect3);   //将输入转成antlr的input流
        //词法分析
        PlSqlLexer lexer = new PlSqlLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);  //转成token流
        tokens.fill();
        System.out.println("token打印：" + tokens.getTokens());
        //语法分析
        PlSqlParser parser = new PlSqlParser(tokens);
        PlSqlParser.Sql_scriptContext tree = parser.sql_script();
        TestTreeVisitor loader = new TestTreeVisitor();
        loader.visit(tree);
    }
    public void testSqlSelect4(){
        //selectTest
        String sqlSelect4 = "SELECT SYS_GUID(),SYSTIMESTAMP,'oracle' FROM DUAL;";;
        CharStream input = CharStreams.fromString(sqlSelect4);   //将输入转成antlr的input流
        //词法分析
        PlSqlLexer lexer = new PlSqlLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);  //转成token流
        tokens.fill();
        System.out.println("token打印：" + tokens.getTokens());
        //语法分析
        PlSqlParser parser = new PlSqlParser(tokens);
        PlSqlParser.Sql_scriptContext tree = parser.sql_script();
        TestTreeVisitor loader = new TestTreeVisitor();
        loader.visit(tree);
    }

    public void testSqlSelect5() {
        //selectTest
        String sqlSelect5 = "SELECT LISTAGG('t2.id', ',') WITHIN GROUP (ORDER BY 't2.id')\n" +
                "FROM T_BASE_USERDEPT 't1' LEFT JOIN T_BASE_DEPT 't2' ON 't1.fdept_id' = 't2.id'\n" +
                "WHERE 't1.fuser_id' = 1;";
        SQLtmp.SQL = sqlSelect5;
        CharStream input = CharStreams.fromString(sqlSelect5);   //将输入转成antlr的input流
        //词法分析
        PlSqlLexer lexer = new PlSqlLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);  //转成token流
        tokens.fill();
        System.out.println("token打印：" + tokens.getTokens());
        //语法分析
        PlSqlParser parser = new PlSqlParser(tokens);
        PlSqlParser.Sql_scriptContext tree = parser.sql_script();
        TestTreeVisitor loader = new TestTreeVisitor();
        loader.visit(tree);
    }


    public void testSqlCreate() {
        //createTest
        String sqlCreate = "CREATE TABLE T_DICT (\n" +
                "  ID VARCHAR2(32) DEFAULT SYS_GUID() NOT NULL ,\n" +
                "  CLASS_ID VARCHAR2(20) ,\n" +
                "  CLASS_NAME VARCHAR2(100) ,\n" +
                "  CLASS_ENAME VARCHAR2(2000) ,\n" +
                "  KEY_ID VARCHAR2(20) ,\n" +
                "  KEY_NAME VARCHAR2(500) ,\n" +
                "  KEY_ENAME VARCHAR2(2000) ,\n" +
                "  OR_VALID_D VARCHAR2(1) DEFAULT 0 ,\n" +
                "  MEMO VARCHAR2(2000) ,\n" +
                "  TIME_MARK TIMESTAMP(6) ,\n" +
                "  STA VARCHAR2(10) DEFAULT 1 ,\n" +
                "  KEY_SEQ NUMBER(20) ,\n" +
                "  CREATE_PRSN VARCHAR2(64) ,\n" +
                "  CREATE_TIME TIMESTAMP(6) DEFAULT SYSTIMESTAMP ,\n" +
                "  UPDATE_PRSN VARCHAR2(64) ,\n" +
                "  UPDATE_TIME TIMESTAMP(6) DEFAULT SYSTIMESTAMP ,\n" +
                "  FDELETE_ID VARCHAR2(1) DEFAULT 0  NOT NULL ,\n" +
                "  CONSTRAINT PK_T_DICT PRIMARY KEY(ID)\n" +
                ");";

        CharStream input = CharStreams.fromString(sqlCreate);   //将输入转成antlr的input流
        //词法分析
        PlSqlLexer lexer = new PlSqlLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);  //转成token流
        tokens.fill();
        System.out.println("token打印：" + tokens.getTokens());
        //语法分析
        PlSqlParser parser = new PlSqlParser(tokens);
        PlSqlParser.Sql_scriptContext tree = parser.sql_script();
        TestTreeVisitor loader = new TestTreeVisitor();
        loader.visit(tree);
    }
}
