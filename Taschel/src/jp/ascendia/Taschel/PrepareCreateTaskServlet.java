package jp.ascendia.Taschel;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class PrepareCreateTaskServlet extends BaseTaskServlet {

	/**
	 * タスク一覧画面にて 【新規】ボタンが押下されたときに呼ばれます。
	 * タスク新規作成画面を表示します。
	 * 
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	@Override
	protected void execute(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		System.out.println("[PrepareCreateTaskServlet][START] execute");
		
		HttpSession session = request.getSession(true);
		Integer user_id = (Integer)session.getAttribute(USER_ID);
		
		if ( session.isNew() || user_id == null) {
			/* エラー処理 */
			this.goBackLogin(request, response, LOGIN_ERROR);
		
		} else {						
			try {
				// ログインユーザのタスク一覧
				session.setAttribute(USER_TASK_LIST, this.getUserTaskList(user_id));
				
				if ( session.getAttribute(IMPORTANCE_LIST) == null ) {
					// タスク重要度一覧
					try (	Statement stmt = this.getConection().createStatement(); 
							ResultSet rs = stmt.executeQuery("SELECT kbn_value, name FROM m_kbn WHERE id = 1 ORDER BY kbn_value asc;")) {
						session.setAttribute(IMPORTANCE_LIST, this.convertResultSet2List(rs));
					}
				}
								
				if ( session.getAttribute(STATUS_LIST) == null ) {
					// タスクステータス一覧
					try (	Statement stmt = this.getConection().createStatement(); 
							ResultSet rs = stmt.executeQuery("SELECT kbn_value, name FROM m_kbn WHERE id = 2 ORDER BY kbn_value asc;")) {
						session.setAttribute(STATUS_LIST, this.convertResultSet2List(rs));
					}
				}
				
				
				if ( session.getAttribute(ALL_TASK_LIST) == null ) {
					// タスクステータス一覧
					try (	Statement stmt = this.getConection().createStatement(); 
							ResultSet rs = stmt.executeQuery("SELECT id, name FROM t_task WHERE stat_kbn between 0 and 6 ORDER BY id asc;")) {
						session.setAttribute(ALL_TASK_LIST, this.convertResultSet2List(rs));
					}
				}
				
				
				if ( session.getAttribute(START_KBN_LIST) == null ) {
					// タスク開始条件一覧
					try (	Statement stmt = this.getConection().createStatement(); 
							ResultSet rs = stmt.executeQuery("SELECT kbn_value, name FROM m_kbn WHERE id = 3 ORDER BY kbn_value asc;")) {
						session.setAttribute(START_KBN_LIST, this.convertResultSet2List(rs));
					}
				}
				
				if ( session.getAttribute(END_KBN_LIST) == null ) {
					// タスク終了条件一覧
					try (	Statement stmt = this.getConection().createStatement(); 
							ResultSet rs = stmt.executeQuery("SELECT kbn_value, name FROM m_kbn WHERE id = 4 ORDER BY kbn_value asc;")) {
						session.setAttribute(END_KBN_LIST, this.convertResultSet2List(rs));
					}
				}
				
				// 画面遷移
				this.getServletContext().getRequestDispatcher("/createTask.jsp").forward(request, response);				
			} catch (SQLException e) {
				this.goBackLogin(request, response, SYSTEM_ERROR);
				e.printStackTrace();
			}			
		}
	}
}
