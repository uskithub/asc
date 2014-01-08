package jp.ascendia.Taschel;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * login.jspからPOSTメソッドで呼ばれ、ユーザからログインID（user_name）とパスワード（password）を受け取り
 * DBからユーザを検索します。結果があった場合には TaskListServletへ、ない場合にはlogin.jspへ遷移します。
 * 
 * @author 斉藤 祐輔
 *
 */
public class LoginServlet extends BaseServlet {
	
	private final String SELECT_USER ="SELECT id, first_name, last_name FROM m_user WHERE user_name = ? AND password = ?;";
	
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

		System.out.println(String.format("■ loginID: %s, password: %s", loginID, password));
		
		// ログイン認証
		// SQLインジェクションの可能性がある箇所はPreparedStatementクラスを使います
		try (PreparedStatement pstmt = this.getConection().prepareStatement(SELECT_USER)){
			pstmt.setString(1, loginID);
			pstmt.setString(2, password);
			
			List<Map<String, Object>> result = this.executeSelect(pstmt);
				
			if ( result.size() == 0 ) {							
				this.goBackLogin(request, response, LOGIN_ERROR);				
			} else {
				Long user_id = (Long) result.get(0).get("id");
				String first_name = (String) result.get(0).get("first_name");
				String last_name = (String) result.get(0).get("last_name");
				System.out.println(String.format("user_id:%d, name: %s %s", user_id, last_name, first_name));
				// ユーザ情報をセッションに保存
				HttpSession session = request.getSession(true);
				session.setAttribute(USER_ID, user_id);						
				session.setAttribute(FIRST_NAME, first_name);
				session.setAttribute(LAST_NAME, last_name);
				
				String sql = "SELECT g.name FROM m_group g, k_syozoku s WHERE g.id = s.group_id AND s.user_id = ?;";
				
				try (PreparedStatement pstmt2 = this.getConection().prepareStatement(sql)){
					pstmt2.setLong(1, user_id);
					
					List<Map<String, Object>> result2 = this.executeSelect(pstmt2);
					
					if ( result2.size() != 0) {
						String group_name = (String) result.get(0).get("group_name");
						// グループ情報をセッションに保存
						session.setAttribute(GROUP_NAME, group_name);
					}
					
					// 次の処理へ
					//this.getServletContext().getRequestDispatcher("/dummy.jsp").forward(request, response);
					// _TODO: 遷移先を修正する
					this.getServletContext().getRequestDispatcher("/TaskList").forward(request, response);
					
				}			
			}			
		} catch (SQLException e) {
			this.goBackLogin(request, response, SYSTEM_ERROR);
			e.printStackTrace();
		}
	}
	
	/**
	 * login.jspからきた場合は通常処理し、直接URLを叩いてアクセスしてきた場合はlogin.jspへ戻すようにします。
	 * 
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	@Override
	protected void execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println("[LoginServlet][START] execute");
		
		HttpSession session = request.getSession(true);
		Long user_id = (Long)session.getAttribute(USER_ID);
		
		/* セッション情報確認 */		
		if ( session.isNew() || user_id == null) {
			System.out.println("セッションはありません");			
		} else {
			System.out.println(String.format("セッションがあります（user_id: %d）。セッションを破棄します...", user_id));
			session.invalidate();
		}

		// login.jspから送られてきたデータを取得
		String loginID = request.getParameter("loginID");
		String password = request.getParameter("password");
		
		// getParameterは値が存在しない場合はnullを返すので、nullの場合はlogin.jsp以外から来たと判断する
		// （login.jspで未入力で送信した場合は、loginIDとpasswordは""になる）
		if( loginID == null || password == null ){
			/* エラー処理 */
			this.goBackLogin(request, response, LOGIN_ERROR);
			System.out.println("[LoginServlet][ END ] execute2");
		} else {
			/* 正常処理 */
			this.doLogin(request, response, loginID, password);
			System.out.println("[LoginServlet][ END ] execute3");
		}		
	}
}
