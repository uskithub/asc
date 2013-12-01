package jp.ascendia.Taschel;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

public abstract class BaseServlet extends HttpServlet {

	// メンバ変数
	private Connection _conn = null;
	private Boolean _isDbAvailable = true;

	@Override
	public void init() {
		System.out.println("[START] init");
		try {
			// InitialContextは、SQL検索をするときの出発点
			InitialContext context = new InitialContext();
			// DataSource型のインスタンスを取得
			DataSource dataSource = (DataSource) context.lookup("java:comp/env/jdbc/mysql");
			// DataSource型のインスタンスからConnectionを取得
			_conn = dataSource.getConnection();
		} catch (NamingException e) {
			_isDbAvailable = false;
			e.printStackTrace();
		} catch (SQLException e) {
			_isDbAvailable = false;
			e.printStackTrace();
		} finally {
			System.out.println("[ END ] init");
		}
	}
	
	/* getter */
	
	/**
	 * MySQLへのコネクションを返します。
	 * @return _conn
	 */
	protected Connection getConection() {
		return _conn;
	}

	/**
	 * DBが利用できるかを返します。
	 * @return isDbAvailable
	 */
	protected Boolean isDbAvailable() {
		return _isDbAvailable;
	}
	
	
	/**
	 * エラーがある場合の処理
	 * 
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	protected void goBackLogin(HttpServletRequest request, HttpServletResponse response, String errText) throws ServletException, IOException {
		request.setAttribute("ERROR", errText);
		this.getServletContext().getRequestDispatcher("/login.jsp").forward(request, response);
	}
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println("[START] doGet");
		
		/* 事前処理 */
		// データベース接続エラーなどがある場合、即座にlogin.jspへ戻す
		if ( !this.isDbAvailable() ){
			this.goBackLogin(request, response, ErrorMessage.SYSTEM_ERROR);
		} else {
			this.execute(request, response);
		}
		System.out.println("[ END ] doGet");
	}	

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println("[START] doPost");
		/* 事前処理 */
		// データベース接続エラーなどがある場合、即座にlogin.jspへ戻す
		if ( !this.isDbAvailable() ){
			this.goBackLogin(request, response, ErrorMessage.SYSTEM_ERROR);
		} else {
			this.execute(request, response);
		}
		System.out.println("[ END ] doPost");
	}
	
	/**
	 * このメソッドを上書きして、GET/POSTメソッドの際に行う共通の処理を記述して下さい。
	 *  
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	protected abstract void execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;

}
