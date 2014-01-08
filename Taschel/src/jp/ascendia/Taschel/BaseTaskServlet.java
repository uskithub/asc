package jp.ascendia.Taschel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;


public abstract class BaseTaskServlet extends BaseServlet {

	protected StringBuilder userTaskSql = new StringBuilder("SELECT t.*, ki.name as ki_name, kstt.name as kstt_name, kstr.name as kstr_name, ke.name as ke_name, u.last_name, u.first_name")
	.append(" FROM t_task t, m_kbn ki, m_kbn kstt, m_kbn kstr, m_kbn ke, m_user u")
	.append(" WHERE t.imp_kbn = ki.kbn_value AND ki.id = 1")
	.append(" AND t.stat_kbn = kstt.kbn_value AND kstt.id = 2")
	.append(" AND t.start_kbn = kstr.kbn_value AND kstr.id = 3")
	.append(" AND t.end_kbn = ke.kbn_value AND ke.id = 4")
	.append(" AND t.orgnizer_id = u.id")
	.append(" AND t.user_id = ?");

	protected StringBuilder orgnizeTaskSql = new StringBuilder("SELECT t.*, ki.name as ki_name, kstt.name as kstt_name, kstr.name as kstr_name, ke.name as ke_name, u.last_name, u.first_name")
	.append(" FROM t_task t, m_kbn ki, m_kbn kstt, m_kbn kstr, m_kbn ke, m_user u")
	.append(" WHERE t.imp_kbn = ki.kbn_value AND ki.id = 1")
	.append(" AND t.stat_kbn = kstt.kbn_value AND kstt.id = 2")
	.append(" AND t.start_kbn = kstr.kbn_value AND kstr.id = 3")
	.append(" AND t.end_kbn = ke.kbn_value AND ke.id = 4")
	.append(" AND t.orgnizer_id = u.id")
	.append(" AND t.orgnizer_id != t.user_id")		
	.append(" AND t.orgnizer_id = ?");
	
	private StringBuilder createTaskSql = new StringBuilder("INSERT INTO t_task")
	.append(" (name, summary, purpose, imp_kbn, orgnized_id, user_id, start_date, deadline, start_kbn, start_trigger_id, end_kbn, end_trigger_id, stat_kbn, detail, last_upd_date, created_date, last_upd_user, created_user)")
	.append(" VALUES")
	.append(" (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");
	
	/**
	 * 引数で指定したＩＤのユーザのタスク一覧を取得します。
	 * 
	 * @param user_id
	 * @return
	 * @throws SQLException
	 */
	protected List<Map<String, Object>> getUserTaskList(long user_id) throws SQLException {
		try (PreparedStatement pstmt = this.getConection().prepareStatement(userTaskSql.toString())){
			pstmt.setLong(1, user_id);
			System.out.println(pstmt.toString());
			try (ResultSet rs = pstmt.executeQuery()){				
				return this.convertResultSet2List(rs);
			}				
		}
	}
	
	
	/**
	 * 引数で指定したＩＤのユーザが依頼しているタスク一覧を取得します。
	 * 
	 * @param user_id
	 * @return
	 * @throws SQLException
	 */
	protected List<Map<String, Object>> getOrgnizeTaskList(long user_id) throws SQLException {
		try (PreparedStatement pstmt = this.getConection().prepareStatement(orgnizeTaskSql.toString())){
			pstmt.setLong(1, user_id);
			System.out.println(pstmt.toString());
			try (ResultSet rs = pstmt.executeQuery()){				
				return this.convertResultSet2List(rs);
			}				
		}
	}
	
	/**
	 * 
	 * @param name
	 * @param summary
	 * @param purpose
	 * @param imp_kbn
	 * @param orgnized_id
	 * @param user_id
	 * @param start_date
	 * @param deadline
	 * @param start_kbn
	 * @param start_trigger_id
	 * @param end_kbn
	 * @param end_trigger_id
	 * @param stat_kbn
	 * @param detail
	 * @param created_user
	 * @throws SQLException
	 */
	protected void createTask(String name, String summary, String purpose, int imp_kbn, long orgnized_id, long user_id,
			Date start_date, Date deadline, int start_kbn, long start_trigger_id, int end_kbn, long end_trigger_id,
			int stat_kbn, String detail, long created_user) throws SQLException {
		
		Date now = new Date();
		Connection conn = this.getConection();
		
		try (PreparedStatement pstmt = conn.prepareStatement(createTaskSql.toString())){			
			pstmt.setString(1, name);
			pstmt.setString(2, summary);
			pstmt.setString(3, purpose);
			pstmt.setInt(4, imp_kbn);
			pstmt.setLong(5, orgnized_id);
			pstmt.setLong(6, user_id);
			pstmt.setTimestamp(7, new Timestamp(start_date.getTime()));
			pstmt.setTimestamp(8, new Timestamp(deadline.getTime()));	
			pstmt.setInt(9, start_kbn);
			pstmt.setLong(10, start_trigger_id);
			pstmt.setInt(11, end_kbn);
			pstmt.setLong(12, end_trigger_id);
			pstmt.setInt(13, stat_kbn);
			pstmt.setString(14, detail);
			pstmt.setTimestamp(15, new Timestamp(now.getTime()));
			pstmt.setTimestamp(16, new Timestamp(now.getTime()));
			pstmt.setLong(17, created_user);			
			pstmt.setLong(18, created_user);
			
			// トランザクションを開始
			conn.setAutoCommit(false);
			
			if ( 0 < pstmt.executeUpdate()) {
			    conn.commit();
			}
		} catch (SQLException e) {			
			conn.rollback();			
		}		
	}
}
