<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ page import="jp.ascendia.Taschel.TaschelConstant" %>

<%
	//メッセージ表示処理
	String errText = (String) request.getAttribute(TaschelConstant.ERROR);
	if (errText == null) {
		errText = "ログインIDとパスワードを入力し、【ログイン】ボタンを押して下さい。";
	}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/ html4/loose.dtd">
<html>
    <head>
        <title>Login</title>
        <style type="text/css">
        </style>
    </head>
	<body>
		<h1>タスク管理システム</h1>
		<form action="Login" method="post">
			<%
				String loginID = (String) request.getAttribute(TaschelConstant.LOGIN_ID);
				String password = (String) request.getAttribute(TaschelConstant.PASSWORD);
	
				if (loginID == null) {
					loginID = "";
				}
				if (password == null) {
					password = "";
				}
			%>
			ログインID: <input type="text" name="<%=TaschelConstant.LOGIN_ID %>" maxlength="30" size="40" value="miura.emi" /><br /> 
			パスワード: <input type="password" name="<%=TaschelConstant.PASSWORD %>" maxlength="30" size="40" value="asc7038" /><br />
			<br />
			<input type="submit" value="ログイン" /> <input type="reset" value="入力クリア" />
		</form>
		<p><%=errText %></p>
	</body>
</html>