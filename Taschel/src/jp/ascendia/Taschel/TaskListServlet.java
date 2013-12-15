package jp.ascendia.Taschel;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class TaskListServlet extends BaseTaskServlet {
	
	/**
	 * 指定したユーザIDでタスク一覧と依頼しているタスク一覧を取得します。
	 * 
	 * @param request
	 * @param response
	 * @param user_id
	 * @throws ServletException
	 * @throws IOException
	 * @throws SQLException
	 */
	private void doGetTaskList(HttpServletRequest request, HttpServletResponse response, int user_id) throws ServletException, IOException, SQLException {
		
		System.out.println(String.format("[TaskListServlet][START] doGetTaskList(user_id: %d)", user_id));
		HttpSession session = request.getSession();

		if ( session.getAttribute(USER_LIST) == null ) {
			// ユーザ一覧
			try (	Statement stmt = this.getConection().createStatement(); 
					ResultSet rs = stmt.executeQuery("SELECT id, first_name, last_name FROM m_user ORDER BY id;")) {
				session.setAttribute(USER_LIST, this.convertResultSet2List(rs));
			}
		}
		
		// ログインユーザのタスク一覧
		session.setAttribute(USER_TASK_LIST, this.getUserTaskList(user_id));
				
		// ログインユーザが依頼しているタスク一覧		
		session.setAttribute(ORGNIZE_TASK_LIST, this.getOrgnizeTaskList(user_id));		
	}
	
	/**
	 * 
	 * 
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 * @throws SQLException
	 */
	private void doKeywordSearch(HttpServletRequest request, HttpServletResponse response, int user_id, String keyword) throws ServletException, IOException, SQLException {
		
		System.out.println(String.format("[TaskListServlet][START] doKeywordSearch(user_id: %d, keyword: %s)", user_id, keyword));
		HttpSession session = request.getSession();		
		
		// 検索文字列として "%" と "_" が入力された場合、エスケープする
		// "%" と "_" はlike句ではワイルドカード（任意の文字列を意味する特殊文字）として使われるため、
		// ただの文字列と知らせるために、\\\\を付けている
		// ワイルドカードではないよ、普通の文字列だよと知らせることをエスケープする、といい
		// 知らせるために使う文字（ここでは\\\\）をエスケープシーケンスという
		String safeKeyword = keyword.replaceAll("%", "\\\\%").replaceAll("_", "\\\\_");
		
		// ログインユーザのタスク一覧
		try (PreparedStatement pstmt = this.getConection().prepareStatement(userTaskSql.toString() + " AND CONCAT(t.name, IFNULL(t.detail, '')) like ?")){
			pstmt.setInt(1, user_id);
			pstmt.setString(2, "%" + safeKeyword + "%");
			System.out.println(pstmt.toString());
			try (ResultSet rs = pstmt.executeQuery()){				
				session.setAttribute(USER_TASK_LIST, this.convertResultSet2List(rs));
			}				
		}
		
		// ログインユーザが依頼しているタスク一覧
		try (PreparedStatement pstmt = this.getConection().prepareStatement(orgnizeTaskSql.toString() + " AND CONCAT(t.name, IFNULL(t.detail, '')) like ?")){
			pstmt.setInt(1, user_id);
			pstmt.setString(2, "%" + safeKeyword + "%");
			System.out.println(pstmt.toString());
			try (ResultSet rs = pstmt.executeQuery()){				
				session.setAttribute(ORGNIZE_TASK_LIST, this.convertResultSet2List(rs));
			}				
		}
	}
	
	/**
	 * LoginServletにてログイン処理が成功したとき、
	 * taskList.jspにて
	 * 	・表示ユーザを切り替えたとき、
	 * 	・絞込み検索を行ったとき、
	 * に呼ばれます。
	 * 
	 * ユーザがログインしていない場合はlogin.jspへ戻すようにします。
	 * 
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	@Override
	protected void execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		System.out.println("[TaskListServlet][START] execute");
		
		// 2013-12-13 saito getSession(false)では、結果がnullのとき、session.getAttributeで
		// NullPointerExceptionとなるので、getSession(true)に変更。これに伴い、下のif文も
		// session == null から　session.isNew() に変更
		HttpSession session = request.getSession(true);
		Integer user_id = (Integer)session.getAttribute(USER_ID);
		
		if ( session.isNew() || user_id == null) {
			/* エラー処理 */
			this.goBackLogin(request, response, LOGIN_ERROR);
		} else {
			
			boolean isChangeUser = "決定".equals(request.getParameter(CHANGE_USER));
			boolean isKeywordSearch = "検索".equals(request.getParameter(KEYWORD_SEARCH));
			
			try {
				if ( isChangeUser ) {
					/* 表示ユーザの切り替え */
					System.out.println("[TaskServlet] 表示ユーザの切り替え");
					int target_user_id = Integer.parseInt(request.getParameter(TARGET_USER_ID));
					this.doGetTaskList(request, response, target_user_id);
				} else if ( isKeywordSearch && request.getParameter(KEYWORD) != null && !"".equals(request.getParameter(KEYWORD)) ) {
					/* 絞込み検索 */
					System.out.println("[TaskServlet] 絞込み検索");
					int target_user_id = Integer.parseInt(request.getParameter(TARGET_USER_ID));
					String keyword = request.getParameter(KEYWORD);
					this.doKeywordSearch(request, response, target_user_id, keyword);
				} else {
					/* ログイン後の通常処理 */
					System.out.println("[TaskServlet] ログイン後の通常処理");
					this.doGetTaskList(request, response, user_id);
				}
				this.getServletContext().getRequestDispatcher("/taskList.jsp").forward(request, response);
				
			} catch (SQLException e) {
				this.goBackLogin(request, response, SYSTEM_ERROR);
				e.printStackTrace();
			}			
		}
		System.out.println("[TaskListServlet][ END ] execute");
	}
}
