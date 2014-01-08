package jp.ascendia.Taschel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.eclipse.jetty.testing.HttpTester;
import org.eclipse.jetty.testing.ServletTester;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

public class LoginServletMockWithServletTester {
	
	private ServletTester tester;
	private HttpTester request;
	private HttpTester response;
	private InitialContext context;

	@Before
	public void setUp() throws Exception {
		
        // Create initial context
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.eclipse.jetty.jndi.InitialContextFactory");
        System.setProperty(Context.URL_PKG_PREFIXES, "org.eclipse.jetty.jndi");
        
        context = new InitialContext();                
        context.createSubcontext("java:comp/env");
        context.createSubcontext("java:comp/env/jdbc");
        
        // Construct DataSource
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setUrl("jdbc:mysql://localhost/taschel");
        dataSource.setUser("taschel_admin");
        dataSource.setPassword("sendai2013");
        context.bind("java:comp/env/jdbc/mysql", dataSource);        
        
		this.tester = new ServletTester();
		// 以下の様にしていすると、http://localhost:8080/Taschel　となる
		this.tester.setContextPath("/Taschel");

		// 本来、web.xml で指定するサーブレットの呼び出しURLを指定
		this.tester.addServlet(LoginServlet.class, "/Login");
		this.tester.start();

		this.request = new HttpTester();
		this.response = new HttpTester();
		this.request.setMethod("GET");
		this.request.setHeader("Host", "tester");
		this.request.setVersion("HTTP/1.1");
	}
	
	@After
	public void tearDown() throws Exception {
		context.unbind("java:comp/env");		
	}

	@Test
	public void test() throws Exception {
		this.request.setURI("/Taschel/Login");
		this.response.parse(tester.getResponses(request.generate()));
		
		assertTrue(this.response.getMethod() == null);
		assertEquals(200, this.response.getStatus());	
	}
	
	@Test
	public void セッション情報破棄() throws Exception {
		this.request.setURI("/Taschel/Login");
		this.response.parse(tester.getResponses(request.generate()));
		assertTrue(this.response.getMethod() == null);
		assertEquals(200, this.response.getStatus());
		
	}

}
