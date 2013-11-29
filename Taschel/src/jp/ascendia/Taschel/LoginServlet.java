package jp.ascendia.Taschel;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

/**
 * login.jspからPOSTメソッドで呼ばれ、ユーザからログインID（user_name）とパスワード（password）を受け取り
 * DBからユーザを検索します。結果があった場合には TaskListServletへ、ない場合にはlogin.jspへ遷移します。
 * 
 * @author 斉藤 祐輔
 *
 */
public class LoginServlet extends HttpServlet {
	
	// メンバ変数
	private Connection conn = null;
	private Boolean isDbAvailable = true;

	@Override
	public void init() {
		System.out.println("[START] init");
		try {
			// InitialContextは、SQL検索をするときの出発点
			InitialContext context = new InitialContext();
			// DataSource型のインスタンスを取得
			DataSource dataSource = (DataSource) context.lookup("java:comp/env/jdbc/mysql");
			// DataSource型のインスタンスからConnectionを取得
			conn = dataSource.getConnection();
		} catch (NamingException e) {
			isDbAvailable = false;
			e.printStackTrace();
		} catch (SQLException e) {
			isDbAvailable = false;
			e.printStackTrace();
		} finally {
			System.out.println("[ END ] init");
		}
	}
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println("[START] doGet");
		this.execute(request, response);
		System.out.println("[ END ] doGet");
	}	

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println("[START] doPost");
		this.execute(request, response);
		System.out.println("[ END ] doPost");
	}

	/**
	 * エラーがある場合の処理
	 * 
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	private void goBackLogin(HttpServletRequest request, HttpServletResponse response, String errText) throws ServletException, IOException {
		request.setAttribute("ERROR", errText);
		this.getServletContext().getRequestDispatcher("/ｌogin.jsp").forward(request, response);
	}

	/**
	 * 
	 * @param request
	 * @param response
	 * @param loginID
	 * @param password
	 * @throws ServletException
	 * @throws IOException
	 */
	private void doLogin(HttpServletRequest request, HttpServletResponse response, String loginID, String password) throws ServletException, IOException {
 
		// 連結を前提に文字列を作成するためのクラス（Stringで　+　を使って連結するよりも処理が速い）
		StringBuilder sb = new StringBuilder("SELECT count(*), id, first_name, last_name")
				.append(" FROM m_user")
				.append(" WHERE user_name = ? AND password = ?;");

		// ログイン認証
		// SQLインジェクションの可能性がある箇所はPreparedStatementクラスを使います
		try (PreparedStatement pstmt = conn.prepareStatement(sb.toString())){
			pstmt.setString(1, loginID);
			pstmt.setString(2, password);
			
			try (ResultSet rs = pstmt.executeQuery()){
				int count = 0;
				int user_id = 0;
				String first_name = null;
				String last_name = null;
				
				// nextメソッドはカーソルを一つ進め、次の行が有効かどうかを返します
				// 今回は、count()関数を使っているので、取得結果は必ず1件になります
				// （マッチする結果が0件の場合は、0件という結果が1件返ってくる）
				rs.next();
				count = rs.getInt(1);
				user_id = rs.getInt(2);
				first_name = rs.getString(3);
				last_name = rs.getString(4);
				
				if ( count == 0) {
					this.goBackLogin(request, response, ErrorMessage.LOGIN_ERROR);
					
				} else {					
					String sql = String.format("SELECT g.name FROM m_group g, k_syozoku s WHERE g.id = s.group_id AND s.user_id = %d;", user_id);
					
					// SQLインジェクションの可能性がないので、通常のStatementクラスを使います
					try (	Statement stmt = conn.createStatement(); 
							ResultSet rs2 = stmt.executeQuery(sql)) {
						
						String group_name = null;
						// 読み込み処理
						while (rs2.next()) {
							group_name = rs2.getString(1);
						}
						
						HttpSession session = request.getSession(true);
						session.setAttribute("USER_ID", user_id);						
						session.setAttribute("FIRST_NAME", first_name);
						session.setAttribute("LAST_NAME", last_name);
						session.setAttribute("GROUP_NAME", group_name);						
						// 次の処理へ
						this.getServletContext().getRequestDispatcher("/dummy.jsp").forward(request, response);
						// TODO: 遷移先を修正する
						//this.getServletContext().getRequestDispatcher("/taskList").forward(request, response);
					}
				}
			}			
		} catch (SQLException e) {
			this.goBackLogin(request, response, ErrorMessage.SYSTEM_ERROR);
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}
	
	/**
	 * login.jspからきた場合は通常処理し、直接URLを叩いてアクセスしてきた場合はlogin.jspへ戻すようにします
	 * 
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	private void execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println("[START] execute");
		
		/* 事前処理 */
		// データベース接続エラーなどがある場合、即座にlogin.jspへ戻す
		if ( !isDbAvailable ){
			this.goBackLogin(request, response, ErrorMessage.SYSTEM_ERROR);
			return;
		}

		// login.jspから送られてきたデータを取得
		String loginID = request.getParameter("loginID");
		String password = request.getParameter("password");
		
		// getParameterは値が存在しない場合はnullを返すので、nullの場合はlogin.jsp以外から来たと判断する
		// （login.jspで未入力で送信した場合は、loginIDとpasswordは""になる）
		if( loginID == null || password == null ){
			/* エラー処理 */
			this.goBackLogin(request, response, ErrorMessage.LOGIN_ERROR);
			
		} else {
			/* 正常処理 */
			this.doLogin(request, response, loginID, password);
		}		
	}


}
