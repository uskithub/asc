package jp.ascendia.Taschel;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

public abstract class BaseServlet extends HttpServlet implements TaschelConstant, ErrorMessage {

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
		request.setAttribute(ERROR, errText);
		this.getServletContext().getRequestDispatcher("/login.jsp").forward(request, response);
	}
	
	/**
	 * ResultSetクラスを受け取り、一レコードを一つのMapに変換し、Listに詰めて返します。
	 * 
	 * @param rs
	 * @return
	 * @throws SQLException
	 */
	protected List<Map<String, Object>> convertResultSet2List(ResultSet rs) throws SQLException{		
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

		ResultSetMetaData rsmd = rs.getMetaData();

		while (rs.next()) {
			Map<String, Object> m = new HashMap<String, Object>();
			for (int i = 1; i <= rsmd.getColumnCount(); i++) {
				m.put(rsmd.getColumnLabel(i), rs.getObject(i));
				
				// 参考
				/*switch(rsmd.getColumnType(i)) {
					case java.sql.Types.BIGINT:
						System.out.println(String.format("%s　は Long型です。", rsmd.getColumnLabel(i)));
						break;
					case java.sql.Types.INTEGER:
						System.out.println(String.format("%s　は Integer型です。", rsmd.getColumnLabel(i)));
						break;
					case java.sql.Types.VARCHAR:
					case java.sql.Types.LONGVARCHAR:
						System.out.println(String.format("%s　は String型です。", rsmd.getColumnLabel(i)));
						break;
					case java.sql.Types.BOOLEAN:
						System.out.println(String.format("%s　は Boolean型です。", rsmd.getColumnLabel(i)));
						break;
					case java.sql.Types.DATE:
					case java.sql.Types.TIMESTAMP:
						System.out.println(String.format("%s　は Date型です。", rsmd.getColumnLabel(i)));
						break;
					case java.sql.Types.NULL:						
						System.out.println(String.format("%s　は nullです。", rsmd.getColumnLabel(i)));
						break;
					default:
						throw new UnsupportedOperationException(String.format("%s　は実装されていません（型: %d）", rsmd.getColumnLabel(i), rsmd.getColumnType(i)));
				}*/
			}
			list.add(m);
		}
		return list;
	}
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println("[START] doGet");
		
		/* 事前処理 */
		// データベース接続エラーなどがある場合、即座にlogin.jspへ戻す
		if ( !this.isDbAvailable() ){
			this.goBackLogin(request, response, SYSTEM_ERROR);
		} else {
			
			Map<String, String[]> m = request.getParameterMap();
			for( String key: m.keySet() ) {
				for( String v : m.get(key)) {
					System.out.println(String.format("[%s](GET) %s=%s", this.getClass().getName(), key, v));
				}
			}
			
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
			this.goBackLogin(request, response, SYSTEM_ERROR);
		} else {
			
			Map<String, String[]> m = request.getParameterMap();
			for( String key: m.keySet() ) {
				for( String v : m.get(key)) {
					System.out.println(String.format("[%s](POST) %s=%s", this.getClass().getName(), key, v));
				}
			}
			
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
