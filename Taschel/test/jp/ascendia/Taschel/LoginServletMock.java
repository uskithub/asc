package jp.ascendia.Taschel;

import static org.junit.Assert.*;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import mockit.Cascading;
import mockit.Deencapsulation;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;


public class LoginServletMock {

	@Mocked
	HttpServletRequest request;
	 	
	@Cascading
	HttpServletResponse response;	
	
	@Cascading
	ServletConfig servletConfig;
		
	@Cascading
	HttpSession session;
	
	InitialContext context;
	
	LoginServlet servlet;
	
	@Before
	public void setUp() throws Exception {
        // Jetty用のコンテキストを設定
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.eclipse.jetty.jndi.InitialContextFactory");
        System.setProperty(Context.URL_PKG_PREFIXES, "org.eclipse.jetty.jndi");
        
        context = new InitialContext();                
        context.createSubcontext("java:comp/env");
        context.createSubcontext("java:comp/env/jdbc");
        
        // MySQL用のデータソースを作成
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setUrl("jdbc:mysql://localhost/taschel");
        dataSource.setUser("taschel_admin");
        dataSource.setPassword("sendai2013");
        context.bind("java:comp/env/jdbc/mysql", dataSource);
        
        // サーブレットの初期化       
        servlet = new LoginServlet();
		servlet.init(servletConfig);		
	}
	
	@After
	public void tearDown() throws Exception {
		context.unbind("java:comp/env");		
	}
	

	
	@Test
	public void セッション情報確認() throws Exception {
		System.out.println("■■■　セッション情報確認　==================================================");
		
		/* 振る舞いを記述 */
		new NonStrictExpectations() {
			{
				// requestにgetSessionしたら、sessionを返してね
				request.getSession(true);result=session;
			}
		};
		
		/**
		 * 上記の記述は、以下を無名クラスとしてextendsしているのと等価
		 * 
		public class MyExpectations extends NonStrictExpectations {
			// コンストラクタ
			public MyExpectations(){
				// requestにgetSessionしたら、sessionを返してね
				request.getSession(true);result=session;
			}
		}*/
		
		/* テスト実行 */		
		// 振る舞いが記録（Record）され、実行時に再現（replay）されています。
		servlet.doPost(request, response);		
	}
	
	@Test
	public void セッション情報破棄() throws Exception {
		System.out.println("■■■　セッションセッション情報破棄　==================================================");
		
		/* 振る舞いを記述 */
		new NonStrictExpectations() {{
				// requestさん、getSessionされたら、sessionを返してね
				request.getSession(true);result=session;
				// sessionさん、getAttribute(USER_ID)されたら、1を返してね
				session.getAttribute(TaschelConstant.USER_ID);result=1L;
			}};
		
		/* テスト実行 */		
		servlet.doPost(request, response);
		
		/* 検証したいことを記述（実際の実行よりも後に記述しないとテスト失敗になります） */
		new Verifications(){{
				// sessionのinvalidateが 1 回呼ばれること
				session.invalidate();times=1;
			}};
		
	}

	/**
	 * ①このテストはテストケース「ログイン処理失敗１」を、doPostメソッドから始めた場合のテスト方法です。
	 * 
	 * @throws Exception
	 */
	@Test
	public void ログイン処理失敗１() throws Exception {
		System.out.println("■■■　ログイン処理失敗１　==================================================");
		
		/* 振る舞いを記述 */
		new NonStrictExpectations() {{
				// requestさん、getSessionされたら、sessionを返してね
				request.getSession(true);result=session;
				// requestさん、getParameter("loginID")されたら、"存在しないログインID"を返してね
				request.getParameter("loginID");result="存在しないログインID";				
				// requestさん、getParameter("password")されたら、"ASC9999"を返してね
				request.getParameter("password");result="ASC9999";
			}};
		
		/* テスト実行 */
		servlet.doPost(request, response);
		
		/* 検証したいことを記述（実際の実行よりも後に記述しないとテスト失敗になります） */
		new Verifications(){{
				// privateメソッドの呼び出しは
				//		Deencapsulation.invoke(対象のオブジェクト, "メソッド名", メソッドのパラメータ...);
				//　と書く
				// LoginServletのdoLoginが 1回呼ばれること
				Deencapsulation.invoke(servlet, "doLogin", request, response, "存在しないログインID", "ASC9999");times=1;
				// LoginServletのgoBackLoginが 1回呼ばれること
				servlet.goBackLogin(request, response, ErrorMessage.LOGIN_ERROR);times=1;
			}};
		
	}
	
	
	/**
	 * ②このテストはテストケース「ログイン処理失敗１」を、doLogin内の、executeSelectメソッドのみテストしています。
	 * テストしたいのはNGなログインIDが入力された異常系なので、このように局所的なテストで良いでしょう。
	 * 一方で、「ログイン処理成功」など正常系は、①のように、doPostメソッドから始めましょう。
	 * 
	 * @throws Exception
	 */
	@Test
	public void ログイン処理失敗１改() throws Exception {
		System.out.println("■■■　ログイン処理失敗１改　==================================================");
		
		/* テスト実行 */
		List<Map<String,Object>> actual = null;			
		List<Map<String,Object>> expected = new ArrayList<Map<String,Object>>();
	
		// Deencapsulation.getFieldは、privateなメンバ変数を取得するための方法
		try (PreparedStatement pstmt = servlet.getConection().prepareStatement((String)Deencapsulation.getField(servlet, "SELECT_USER"))){
			pstmt.setString(1, "存在しないログインID");
			pstmt.setString(2, "ASC9999");			
			actual = servlet.executeSelect(pstmt);
		}
		
		/* 判定 */
		// JUnitのアサーション（判定用メソッド）を使って判定します。詳しくは書籍「JUnit実践入門」を参照して下さい
		assertEquals(expected, actual);		
	}
}
