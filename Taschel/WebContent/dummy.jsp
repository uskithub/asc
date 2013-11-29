<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%         
    Integer user_id = null;
    String first_name = null;
    String last_name = null;
    String group_name = null;
        
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
    }
   
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>確認用ダミー画面</title>
    </head>    
    <body>
        <ul>                        
            <li>SessionId: <%=session.getId() %></li>
            <li>user_id: <%=String.format("%d", user_id) %></li>
            <li>first_name: <%=first_name %></li>
            <li>last_name: <%=last_name %> </li>
            <li>group_name: <%=group_name %></li>
        </ul>
    </body>
</html>