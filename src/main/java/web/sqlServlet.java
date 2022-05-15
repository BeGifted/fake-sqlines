package web;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import sql.PlSqlLexer;
import sql.PlSqlParser;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import tmp.SQLtmp;
import tmp.TestTreeVisitor;

/**
 * @author gongyuandaye
 * @date 2022/5/12 1:02
 */
@WebServlet("/sqlServlet")
public class sqlServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//        ByteArrayOutputStream baoStream = new ByteArrayOutputStream(1024);
//        PrintStream cacheStream = new PrintStream(baoStream);// 临时输出
//        PrintStream oldStream = System.out;// 缓存系统输出
//        System.setOut(cacheStream);

        //接收oracle
        String oracle = request.getParameter("oracle");
        SQLtmp.SQL = oracle;
        SQLtmp.SQL2 = "";
        System.out.println(oracle);

        CharStream input = CharStreams.fromString(oracle);   //将输入转成antlr的input流
        //词法分析
        PlSqlLexer lexer = new PlSqlLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);  //转成token流
        tokens.fill();
        System.out.println("Print Token：" + tokens.getTokens());
        //语法分析
        PlSqlParser parser = new PlSqlParser(tokens);
        PlSqlParser.Sql_scriptContext tree = parser.sql_script();
        TestTreeVisitor loader = new TestTreeVisitor();
        loader.visit(tree);

        String mysql = SQLtmp.SQL2;

//        File f = new File("C:\\Users\\mabeg\\Desktop\\fake-sqlines\\sql.txt");
//        FileOutputStream fop = new FileOutputStream(f);
//        OutputStreamWriter writer = new OutputStreamWriter(fop, "UTF-8");
//        writer.write(mysql);
//        writer.close();
//        fop.close();

        response.getWriter().write(mysql);

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request, response);
    }
}