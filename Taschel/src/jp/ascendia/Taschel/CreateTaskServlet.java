package jp.ascendia.Taschel;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

public class CreateTaskServlet extends BaseTaskServlet {

	

	
	private void doChangeUser(HttpServletRequest request, HttpServletResponse response, int user_id) throws ServletException, IOException, SQLException {
		
		System.out.println(String.format("[CreateTaskServlet][START] doChangeUser(user_id: %d)", user_id));
		HttpSession session = request.getSession();
		
		// ログインユーザのタスク一覧
		session.setAttribute(USER_TASK_LIST, this.getUserTaskList(user_id));
		
		// 画面遷移
		this.getServletContext().getRequestDispatcher("/createTask.jsp").forward(request, response);
	}
	
	/**
	 * タスク新規作成画面から呼ばれます。
	 *  	【登録】ボタンが押された場合：　新しいタスクをデータベースにinsertし、タスク一覧画面を表示します
	 *  	【決定】ボタンが押された場合：　ユーザを切り替えます
	 * 
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	@Override
	protected void execute(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		System.out.println("[ＣｒｅａｔｅＴａｓｋＳｅｒｖｌｅｔ][START] execute");

		HttpSession session = request.getSession(true);
		Integer user_id = (Integer) session.getAttribute(USER_ID);

		if (session.isNew() || user_id == null) {
			/* エラー処理 */
			this.goBackLogin(request, response, LOGIN_ERROR);
		} else {

			boolean isCreateTask = "登録".equals(request.getParameter(SUBMIT_TASK));
			boolean isChangeUser = "決定".equals(request.getParameter(SUBMIT_USER));
			
			try {
				
				if ( isCreateTask ) {
					int target_user_id = Integer.parseInt(request.getParameter(TARGET_USER_ID));
					//this.doCreteTask(request, response, target_user_id);
					
				} else if ( isChangeUser ) {
					int target_user_id = Integer.parseInt(request.getParameter(TARGET_USER_ID));
					this.doChangeUser(request, response, target_user_id);
					
				} else {
					/* 想定外のアクセス */
					this.goBackLogin(request, response, SYSTEM_ERROR);
				}
				
			} catch (SQLException e) {
				this.goBackLogin(request, response, SYSTEM_ERROR);
				e.printStackTrace();
			}
		}
		
		System.out.println("[CreateTaskServlet][ END ] execute");
	}
}
