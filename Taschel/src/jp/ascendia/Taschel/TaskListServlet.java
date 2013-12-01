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

public class TaskListServlet extends BaseServlet {
	
	StringBuilder userTaskSql = new StringBuilder("SELECT t.*, ki.name as ki_name, kstt.name as kstt_name, kstr.name as kstr_name, ke.name as ke_name, u.last_name, u.first_name")
		.append(" FROM t_task t, m_kbn ki, m_kbn kstt, m_kbn kstr, m_kbn ke, m_user u")
		.append(" WHERE t.imp_kbn = ki.kbn_value AND ki.id = 1")
		.append(" AND t.stat_kbn = kstt.kbn_value AND kstt.id = 2")
		.append(" AND t.start_kbn = kstr.kbn_value AND kstr.id = 3")
		.append(" AND t.end_kbn = ke.kbn_value AND ke.id = 4")
		.append(" AND t.orgnizer_id = u.id")
		.append(" AND t.user_id = ?");
	
	StringBuilder orgnizeTaskSql = new StringBuilder("SELECT t.*, ki.name as ki_name, kstt.name as kstt_name, kstr.name as kstr_name, ke.name as ke_name, u.last_name, u.first_name")
		.append(" FROM t_task t, m_kbn ki, m_kbn kstt, m_kbn kstr, m_kbn ke, m_user u")
		.append(" WHERE t.imp_kbn = ki.kbn_value AND ki.id = 1")
		.append(" AND t.stat_kbn = kstt.kbn_value AND kstt.id = 2")
		.append(" AND t.start_kbn = kstr.kbn_value AND kstr.id = 3")
		.append(" AND t.end_kbn = ke.kbn_value AND ke.id = 4")
		.append(" AND t.orgnizer_id = u.id")
		.append(" AND t.orgnizer_id != t.user_id")		
		.append(" AND t.orgnizer_id = ?");
	
	/**
	 * ResultSetクラスを受け取り、一レコードを一つのMapに変換し、Listに詰めて返します。
	 * 
	 * @param rs
	 * @return
	 * @throws SQLException
	 */
	private List<Map<String, Object>> convertResultSet2List(ResultSet rs) throws SQLException{		
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

		if ( session.getAttribute("USER_LIST") == null ) {
			// ユーザ一覧
			try (	Statement stmt = this.getConection().createStatement(); 
					ResultSet rs = stmt.executeQuery("SELECT id, first_name, last_name FROM m_user ORDER BY id;")) {
				session.setAttribute("USER_LIST", this.convertResultSet2List(rs));
			}
		}
		
		// ログインユーザのタスク一覧
		try (PreparedStatement pstmt = this.getConection().prepareStatement(userTaskSql.toString())){
			pstmt.setInt(1, user_id);
			System.out.println(pstmt.toString());
			try (ResultSet rs = pstmt.executeQuery()){				
				session.setAttribute("USER_TASK_LIST", this.convertResultSet2List(rs));
			}				
		}
		
		// ログインユーザが依頼しているタスク一覧
		try (PreparedStatement pstmt = this.getConection().prepareStatement(orgnizeTaskSql.toString())){
			pstmt.setInt(1, user_id);
			System.out.println(pstmt.toString());
			try (ResultSet rs = pstmt.executeQuery()){				
				session.setAttribute("ORGNIZE_TASK_LIST", this.convertResultSet2List(rs));
			}				
		}
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
				session.setAttribute("USER_TASK_LIST", this.convertResultSet2List(rs));
			}				
		}
		
		// ログインユーザが依頼しているタスク一覧
		try (PreparedStatement pstmt = this.getConection().prepareStatement(orgnizeTaskSql.toString() + " AND CONCAT(t.name, IFNULL(t.detail, '')) like ?")){
			pstmt.setInt(1, user_id);
			pstmt.setString(2, "%" + safeKeyword + "%");
			System.out.println(pstmt.toString());
			try (ResultSet rs = pstmt.executeQuery()){				
				session.setAttribute("ORGNIZE_TASK_LIST", this.convertResultSet2List(rs));
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
		
		HttpSession session = request.getSession(false);
		Integer user_id = (Integer)session.getAttribute("USER_ID");
		
		if ( session == null || user_id == null) {
			/* エラー処理 */
			this.goBackLogin(request, response, ErrorMessage.LOGIN_ERROR);
		} else {
			
			boolean isChangeUser = "決定".equals(request.getParameter("changeUser"));
			boolean isKeywordSearch = "検索".equals(request.getParameter("keywordSearch"));
			
			try {
				if ( isChangeUser ) {
					/* 表示ユーザの切り替え */
					System.out.println("[TaskServlet] 表示ユーザの切り替え");
					int target_user_id = Integer.parseInt(request.getParameter("targetUserId"));
					this.doGetTaskList(request, response, target_user_id);
				} else if ( isKeywordSearch && request.getParameter("keyword") != null && !"".equals(request.getParameter("keyword")) ) {
					/* 絞込み検索 */
					System.out.println("[TaskServlet] 絞込み検索");
					int target_user_id = Integer.parseInt(request.getParameter("targetUserId"));
					String keyword = request.getParameter("keyword");
					this.doKeywordSearch(request, response, target_user_id, keyword);
				} else {
					/* ログイン後の通常処理 */
					System.out.println("[TaskServlet] ログイン後の通常処理");
					this.doGetTaskList(request, response, user_id);
				}
				this.getServletContext().getRequestDispatcher("/taskList.jsp").forward(request, response);
				
			} catch (SQLException e) {
				this.goBackLogin(request, response, ErrorMessage.SYSTEM_ERROR);
				e.printStackTrace();
			}			
		}
		System.out.println("[TaskListServlet][ END ] execute");
	}
}
