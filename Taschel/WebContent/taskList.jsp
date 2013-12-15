<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List"%>
<%@ page import="java.util.ArrayList"%>
<%@ page import="java.util.Map"%>
<%@ page import="java.util.HashMap"%>
<%@ page import="java.util.Date"%>
<%@ page import="jp.ascendia.Taschel.TaschelConstant" %>
<%@ page import="jp.ascendia.Taschel.jspUtil.DisplayUtils" %>

<%-- メンバ変数としての変数の宣言、メソッドの定義は　<%! ... %>内で行います --%>
<%!
    // ユーザIDとユーザ名のマップ用
    Map<Integer, String> userIdNameMap = new HashMap<Integer, String>();
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
        user_id = (Integer)session.getAttribute(TaschelConstant.USER_ID);
        first_name = (String)session.getAttribute(TaschelConstant.FIRST_NAME);
        last_name = (String)session.getAttribute(TaschelConstant.LAST_NAME);
        group_name = (String)session.getAttribute(TaschelConstant.GROUP_NAME);     
        
        if ( request.getParameter(TaschelConstant.TARGET_USER_ID) != null ) {
            target_user_id = Integer.parseInt(request.getParameter(TaschelConstant.TARGET_USER_ID));
        }
    }
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/ html4/loose.dtd">
<html>
	<head>
	   <title>タスク一覧</title>
	   <link rel="stylesheet" href="css/common.css">
	</head>
	<body>		
        <div class="control_left">
            <p>
	            <% out.println(group_name + "の" + last_name + " " + first_name + "さんでログインしています"); %>
	        </p>
	        <form action="TaskList" method="POST">
	            <input type="text" name="<%=TaschelConstant.KEYWORD %>" maxlength="50" size="40" />
	            <input type="submit" name="<%=TaschelConstant.KEYWORD %>" value="検索" />
	            <br />
	                            担当者
	            <select name="<%=TaschelConstant.TARGET_USER_ID %>">
	            <%
	                List<Map<String, Object>> userList = (List<Map<String, Object>>) session.getAttribute(TaschelConstant.USER_LIST);    
	            
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
	            <input type="submit" name="<%=TaschelConstant.CHANGE_USER %>" value="決定" />
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
		<form action="PrepareCreateTask" method="POST">
			<%=label %>のタスク
			<%-- 誰宛にタスクを作るのか隠しパラメータで送る --%>
			<% if(target_user_id != null) { %>
			<input type="hidden" name="<%=TaschelConstant.TARGET_USER_ID %>" value="<%=target_user_id %>" />
			<% } else { %>
			<input type="hidden" name="<%=TaschelConstant.TARGET_USER_ID %>" value="<%=user_id %>" />
			<% } %>
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
					List<Map<String, Object>> userTaskList = (List<Map<String, Object>>) session.getAttribute(TaschelConstant.USER_TASK_LIST);
					for (Map<String, Object> m : userTaskList) { 
				%>
				<tr>
				    <td><input type="submit" name="<%=TaschelConstant.TASK_ID %>" value="<%=m.get("id") %>" /></td>
				    <td><%=DisplayUtils.diplayDeadline((Date)m.get("deadline")) %></td>
				    <td><%=DisplayUtils.display((Long)m.get("parent_id")) %></td>
				    <td><%=m.get("name") %></td>
				    <td><%=m.get("last_name") %> <%=m.get("first_name") %></td>
				    <td><%=m.get("ki_name") %></td>
				    <td><%=m.get("kstt_name") %></td>
				</tr>
				<% } %>
			</table>
		</form>
		<br/>
		<form action="PrepareCreateTask" method="POST">
		     依頼しているタスク 
		  <input type="button" name="<%=TaschelConstant.NEW_ORGNIZE_TASK %>" value="新規" />
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
                    <td><input type="submit" name="<%=TaschelConstant.TASK_ID %>" value="<%=m.get("id") %>" /></td>
                    <td><%=DisplayUtils.diplayDeadline((Date)m.get("deadline")) %></td>
                    <td><%=DisplayUtils.display((Long)m.get("parent_id")) %></td>
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