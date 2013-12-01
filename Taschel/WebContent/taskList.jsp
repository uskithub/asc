<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List"%>
<%@ page import="java.util.ArrayList"%>
<%@ page import="java.util.Map"%>
<%@ page import="java.util.HashMap"%>
<%@ page import="java.util.Calendar"%>
<%@ page import="java.util.Date"%>

<%-- メンバ変数としての変数の宣言、メソッドの定義は　<%! ... %>内で行います --%>
<%!
    // ユーザIDとユーザ名のマップ用
    Map<Integer, String> userIdNameMap = new HashMap<Integer, String>();

    /**
     * 文字列がnullのとき、ハイフンに置き換えます。
     */
    private String display(Long tmp) {
    	if ( tmp == null){
    		return "－"; 
    	} else {
    		return tmp.toString(); 
    	}
    }

    /**
     *  期限を表示用に整形して返します。
     */
    private String diplayDeadline(Date deadline) {

        // nullの場合、以降処理しない
        if (deadline == null) {
            return "－";
        }

        // 今日の日付を取得
        Calendar t = Calendar.getInstance();
        // 表示をテストするときに使う（※MONTHは0から数えるので注意）
        //t.set(2013,10,19,12,0);

        // 期限をCalendar型へ変換
        Calendar d = Calendar.getInstance();
        d.setTime(deadline);
        
        int dYear = d.get(Calendar.YEAR);
        int tYear = t.get(Calendar.YEAR);
        
        // 期限が過ぎていないか
        if ( t.getTimeInMillis() <= d.getTimeInMillis() ){               
        	if ( 1 < dYear-tYear) {
                return String.format("%d年後", dYear-tYear);
            } else {                   
                int dMonth = d.get(Calendar.MONTH);
                int tMonth = t.get(Calendar.MONTH);
                if ( 1 == dYear-tYear && tMonth <= dMonth) {                        
                    return "1年後";
                } else {
                    if ( 1 < dMonth+(dYear-tYear)*12-tMonth) {
                        return String.format("%dヶ月後", dMonth+12-tMonth);
                    } else {
                        int dDay = d.get(Calendar.DAY_OF_MONTH);
                        int tDay = t.get(Calendar.DAY_OF_MONTH);
                        if ( 1 == dMonth-tMonth && tDay <= dDay) {                        
                            return "1ヶ月後";
                        } else {
                            Calendar c = Calendar.getInstance();
                            c.set(tYear, tMonth, 1);                            
                            int days=c.getActualMaximum(Calendar.DAY_OF_MONTH);
                            
                            if ( 1 < dDay+(dMonth-tMonth)*days - tDay) {
                                return String.format("%d日後", dDay+(dMonth-tMonth)*days-tDay);
                            } else {                                                                
                                int dHour = d.get(Calendar.HOUR_OF_DAY);
                                int tHour = t.get(Calendar.HOUR_OF_DAY);
                                int dMinute = d.get(Calendar.MINUTE);
                                if ( 1 == dDay-tDay ){
                                	if ( tHour <= dHour) { 
                                	    return "1日後";
                                	} else {                                		
                                        return String.format("翌　%d:%02d", dHour, dMinute);
                                	}
                                } else {                                	
                                    return String.format("%d:%02d", dHour, dMinute);
                                }                            
                            }
                        }       
                    }
                }               
            }          	
        } else {
        	if ( 1 < tYear-dYear) {
                return String.format("<span class=\"warn\">%d年経過</span>", tYear-dYear);
            } else {                   
            	int dMonth = d.get(Calendar.MONTH);
                int tMonth = t.get(Calendar.MONTH);
                if ( 1 == tYear-dYear && dMonth <= tMonth) {                        
                    return "<span class=\"warn\">1年経過</span>";
                } else {                	
                	if ( 1 < tMonth+(tYear-dYear)*12-dMonth) {
                        return String.format("<span class=\"warn\">%dヶ月経過</span>", tMonth+12-dMonth);
                    } else {
                    	int dDay = d.get(Calendar.DAY_OF_MONTH);
                        int tDay = t.get(Calendar.DAY_OF_MONTH);
                    	if ( 1 == tMonth-dMonth && dDay <= tDay) {                        
                            return "<span class=\"warn\">1ヶ月経過</span>";
                        } else {
                        	Calendar c = Calendar.getInstance();
                        	c.set(dYear, dMonth, 1);
                        	int days=c.getActualMaximum(Calendar.DAY_OF_MONTH);
                        	
                            if ( 1 < tDay+(tMonth-dMonth)*days - dDay) {
                                return String.format("<span class=\"warn\">%d日経過</span>", tDay+(tMonth-dMonth)*days-dDay);
                            } else {                            	                            	
                            	int dHour = d.get(Calendar.HOUR_OF_DAY);
                                int tHour = t.get(Calendar.HOUR_OF_DAY);
                                int dMinute = d.get(Calendar.MINUTE);
                                if ( 1 == tDay-dDay ){
                                    if ( dHour <= tHour) { 
                                        return "<span class=\"warn\">1日経過</span>";
                                    } else {                                        
                                        return String.format("<span class=\"warn\">昨日　%d:%02d</span>", dHour, dMinute);
                                    }
                                } else {                                    
                                    return String.format("<span class=\"warn\">%d:%02d</span>", dHour, dMinute);
                                }                             
                            }
                        }   	
                    }
                }               
            } 
        }
    }
%>
<%
    Integer user_id = null;
    String first_name = null;
    String last_name = null;
    String group_name = null;
    
    // 選択したユーザのID
    Integer target_user_id = null;
    
    if (session.isNew()) {
        response.sendRedirect("login.jsp");
        return;
    } else {
        /* ログインしているユーザのデータを取得 */

        // intはプリミティブ型なので、クラス型であるObjectから直接キャスト（型変換）できません
        // Objectからクラス型のintのラッパーIntegerにキャストします
        user_id = (Integer)session.getAttribute("USER_ID");
        first_name = (String)session.getAttribute("FIRST_NAME");
        last_name = (String)session.getAttribute("LAST_NAME");
        group_name = (String)session.getAttribute("GROUP_NAME");     
        
        if ( request.getParameter("targetUserId") != null ) {
            target_user_id = Integer.parseInt(request.getParameter("targetUserId"));
        }
    }
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/ html4/loose.dtd">
<html>
	<head>
	   <title>タスク一覧</title>
	   <style>
            table { width: 100%;border-collapse: collapse; }
            th { padding:5px;text-align: center;vertical-align:middle;border: 1px solid #000; }
            td{ padding:5px;border: 1px solid #000; }
            
            div.control_left { float:left; }            
            div.control_right { float:right; }
            
            hr.clear { clear:both;margin:10px 0; }
            
            span.warn { color:#ff0000;font-weight:bold; }
	   </style>
	</head>
	<body>		
        <div class="control_left">
            <p>
	            <% out.println(group_name + "の" + last_name + " " + first_name + "さんでログインしています"); %>
	        </p>
	        <form action="TaskList" method="POST">
	            <input type="text" name="keyword" maxlength="50" size="40" />
	            <input type="submit" name="keywordSearch" value="検索" />
	            <br />
	                            担当者
	            <select name="targetUserId">
	            <%
	                List<Map<String, Object>> userList = (List<Map<String, Object>>) session.getAttribute("USER_LIST");    
	            
	                Integer selectedId = user_id;
	                if ( target_user_id != null ) {
	                    selectedId = target_user_id;
	                }
	                
	                for(Map<String, Object> m : userList) {
	                    int id = ((Long) m.get("id")).intValue();
	                    String name = String.format("%s %s", m.get("last_name"), m.get("first_name"));
	                    userIdNameMap.put(id, name);
	                    if ( selectedId == id ) {
	            %>                  
	                <option value="<%=id %>" selected><%=name %></option>
	                <% } else { %>
	                <option value="<%=id %>"><%=name %></option>
	            <%     }
	                }
	            %>
	            </select> 
	            <input type="submit" name="changeUser" value="決定" />
	        </form>
        </div>
        <div class="control_right">
            <ul>
                <li><a href="">管理者画面へ</a></li>
                <li><a href="Logout">ログアウト</a></li>
            </ul>
        </div>
		<hr class="clear" />
		<%
			// 表示する氏名を編集
			String label = "";
			if (target_user_id == null || user_id == target_user_id) {
				label = "あなた";
			} else {
				label = userIdNameMap.get(target_user_id);
			}
		%>
		<form action="" method="POST">
			<%=label %>のタスク 
			<input type="submit" name="newTask" value="新規" />
		</form>
        <form action="" method="POST">
			<table>
				<tr>
					<th>ID</th>
					<th>期限</th>
					<th>親タスク</th>
					<th>タイトル</th>
					<th>依頼元</th>
					<th>重要度</th>
					<th>進捗率</th>
				</tr>
				<%
					List<Map<String, Object>> userTaskList = (List<Map<String, Object>>) session.getAttribute("USER_TASK_LIST");
					for (Map<String, Object> m : userTaskList) { 
				%>
				<tr>
				    <td><input type="submit" name="taskId" value="<%=m.get("id") %>" /></td>
				    <td><%=diplayDeadline((Date)m.get("deadline")) %></td>
				    <td><%=display((Long)m.get("parent_id")) %></td>
				    <td><%=m.get("name") %></td>
				    <td><%=m.get("last_name") %> <%=m.get("first_name") %></td>
				    <td><%=m.get("ki_name") %></td>
				    <td><%=m.get("kstt_name") %></td>
				</tr>
				<% } %>
			</table>
		</form>
		<br/>
		<form action="" method="POST">
		     依頼しているタスク 
		  <input type="button" name="newOrgnizeTask" value="新規" />
		</form>
		<form action="" method="POST">
			<table>
				<tr>
					<th>ID</th>
					<th>期限</th>
					<th>親タスク</th>
					<th>タイトル</th>
					<th>依頼元</th>
					<th>重要度</th>
					<th>進捗率</th>
				</tr>
				<%
                    List<Map<String, Object>> orgnizedTaskList = (List<Map<String, Object>>) session.getAttribute("ORGNIZE_TASK_LIST");
                    for (Map<String, Object> m : orgnizedTaskList) { 
                %>
                <tr>
                    <td><input type="submit" name="taskId" value="<%=m.get("id") %>" /></td>
                    <td><%=diplayDeadline((Date)m.get("deadline")) %></td>
                    <td><%=display((Long)m.get("parent_id")) %></td>
                    <td><%=m.get("name") %></td>
                    <td><%=m.get("last_name") %> <%=m.get("first_name") %></td>
                    <td><%=m.get("ki_name") %></td>
                    <td><%=m.get("kstt_name") %></td>
                </tr>
                <% } %>
			</table>
		</form>
	</body>
</html>