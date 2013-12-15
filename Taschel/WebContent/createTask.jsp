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

    /**
     * Requestスコープから値を取得します。値が　null　の場合、空文字を返します。
     */
    private String getSafeParameter(HttpServletRequest request, String key) {
    	String tmp = request.getParameter(key); 
        if( tmp != null ) {
            return tmp;
        } else {
            return "";
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
        <title>タスク新規作成</title>
        <link rel="stylesheet" href="css/common.css">
    </head>
    <body>
        <h1><%=String.format("%s %s",last_name, first_name) %>さんのタスク新規作成</h1>
        <form action="CreateTask" method="post">
            <div class="grid">
                <div class="row">
                    <div class="columnHeader">
                        <label for="purpose" class="required">目的</label>
                    </div>                    
                    <div class="columnData">
                        <input type="text" id="purpose" name="<%=TaschelConstant.PURPOSE %>" maxlength="100" size="40" value="<%=getSafeParameter(request,TaschelConstant.PURPOSE) %>" tabindex="1" autofocus/>
                    </div>
                </div>
                <div class="row">
                    <div class="columnHeader">
                        <label for="title" class="required">タイトル</label>
                    </div>                    
                    <div class="columnData">
                        <input type="text" id="title" name="<%=TaschelConstant.TASK_NAME %>" maxlength="30" size="40" value="<%=getSafeParameter(request,TaschelConstant.TASK_NAME) %>" tabindex="2"　required/>
                    </div>
                    <div class="columnHeader">
                        <label for="startDate" class="required">開始日</label>
                    </div>
                    <div class="columnData">
                        <input type="date" id="startDate" name="<%=TaschelConstant.START_DATE %>" maxlength="10" size="40" value="<%=getSafeParameter(request,TaschelConstant.START_DATE) %>" tabindex="5"　required/>
                    </div>
                </div>
                
                <div class="row">
                    <div class="columnHeader">
                        <label for="summary">要約</label>
                    </div>                    
                    <div class="columnData">
                        <input type="text" id="summary" name="<%=TaschelConstant.SUMMARY %>" maxlength="30" size="40" value="<%=getSafeParameter(request,TaschelConstant.SUMMARY) %>" tabindex="3"/>
                    </div>
                    <div class="columnHeader">
                        <label for="deadline" class="required">〆切</label>
                    </div>
                    <div class="columnData">
                        <input type="date" id="deadline" name="<%=TaschelConstant.DEADLINE %>" maxlength="10" size="40" value="<%=getSafeParameter(request,TaschelConstant.DEADLINE) %>" tabindex="6"　required/>
                    </div>
                </div>
                
                <div class="row">
                    <div class="columnHeader">
                        <label for="status">状態</label>
                    </div>                    
                    <div class="columnData">
                        <select id="status" name="<%=TaschelConstant.STATUS %>" size="1" tabindex="4"　required>
                        <%
                        
                            List<Map<String, Object>> statusList = (List<Map<String, Object>>) session.getAttribute(TaschelConstant.STATUS_LIST);
                        	
                            // 初期値の設定
                        	int defaultStat = 1;
                        	String tmpStat = request.getParameter(TaschelConstant.STATUS);
                        	if ( tmpStat != null) { defaultStat = Integer.parseInt(tmpStat); }
                        	
                            for(Map<String, Object> m : statusList) {
                            	Integer v = (Integer) m.get("kbn_value");
                            	if ( v == defaultStat ) {
                        %>                  
                            <option value="<%=v %>" selected><%=m.get("name") %></option>
                        <%      } else { %>
                            <option value="<%=v %>"><%=m.get("name") %></option>
                        <%      } 
                            } %>
                        </select>
                    </div>
                    <div class="columnHeader">
                        <label for="importance" class="required">重要度</label>
                    </div>
                    <div class="columnData">
                        <select id="importance" name="<%=TaschelConstant.IMPORTANCE %>" size="1" tabindex="7">
                        <%
                            List<Map<String, Object>> importanceList = (List<Map<String, Object>>) session.getAttribute(TaschelConstant.IMPORTANCE_LIST);                                                    
                            
                       		// 初期値の設定
                            int defaultImp = 3;
                            String tmpImp = request.getParameter(TaschelConstant.IMPORTANCE);
                            if ( tmpImp != null) { defaultImp = Integer.parseInt(tmpImp); }
                            
                        	for(Map<String, Object> m : importanceList) {
                            	Integer v = (Integer) m.get("kbn_value");
                                if ( v == defaultImp) {
                        %>                  
                            <option value="<%=v %>" selected><%=m.get("name") %></option>
                        <%      } else { %>
                            <option value="<%=v %>"><%=m.get("name") %></option>
                        <%      } 
                            } %>
                        </select>
                    </div>
                </div>
                                
                <div class="row">
                    <div class="columnHeader">
                        <label for="startTriggerTask">開始条件</label>
                    </div>                    
                    <div class="columnData">
                        <select id="startTriggerTask" name="<%=TaschelConstant.START_TRIGGER_TASK %>" size="1" tabindex="8">
                        <%
		                    List<Map<String, Object>> allTaskList = (List<Map<String, Object>>) session.getAttribute(TaschelConstant.ALL_TASK_LIST);                                                    
		                    
                       		// 初期値の設定
                            long defaultStartTask = (Long) allTaskList.get(0).get("id");
                            String tmpStartTask = request.getParameter(TaschelConstant.START_TRIGGER_TASK);
                            if ( tmpStartTask != null) { defaultStartTask = Long.parseLong(tmpStartTask); }
                            
                        	for(Map<String, Object> m : allTaskList) {
                        		long v = (Long) m.get("id");
                                if ( v == defaultStartTask ) {
		                %>     
		                    <option value="<%=v %>" selected>id:<%=v %> <%=m.get("name") %></option>
                        <%      } else { %>             
		                    <option value="<%=v %>">id:<%=v %> <%=m.get("name") %></option>
		                <%      } 
                            } %>
                        </select>
                        <select name="<%=TaschelConstant.START_KBN %>" size="1" tabindex="9">
                        <%
                            List<Map<String, Object>> startKbnList = (List<Map<String, Object>>) session.getAttribute(TaschelConstant.START_KBN);                                                    
                            
                            // 初期値の設定
	                        int defaultStartKbn = 0;
	                        String tmpStartKbn = request.getParameter(TaschelConstant.START_KBN);
	                        if ( tmpStartKbn != null ) { defaultStartKbn = Integer.parseInt(tmpStartKbn); }
                        
                            for(Map<String, Object> m : startKbnList) {
                            	Integer v = (Integer) m.get("kbn_value");
                                if ( v == defaultStartKbn ) {
                        %>                  
                            <option value="<%=v %>" selected><%=m.get("name") %></option>
                        <%      } else { %>             
                            <option value="<%=v %>"><%=m.get("name") %></option>
                        <%      } 
                            } %>
                        </select>
                    </div>                   
                </div>
                
                <div class="row">
                    <div class="columnHeader">
                        <label for="endTriggerTask">終了条件</label>
                    </div>                    
                    <div class="columnData">
                        <select id="endTriggerTask" name="<%=TaschelConstant.END_TRIGGER_TASK %>" size="1" tabindex="10">
                        <%                           
	                        // 初期値の設定
	                        long defaultEndTask = (Long) allTaskList.get(0).get("id");
	                        String tmpEndTask = request.getParameter(TaschelConstant.END_TRIGGER_TASK);
	                        if ( tmpEndTask != null) { defaultEndTask = Long.parseLong(tmpEndTask); }
	                            
                            for(Map<String, Object> m : allTaskList) {
                                long v = (Long) m.get("id");
                                if ( v == defaultEndTask ) {
                        %>     
                            <option value="<%=v %>" selected>id:<%=v %> <%=m.get("name") %></option>
                        <%      } else { %>             
                            <option value="<%=v %>">id:<%=v %> <%=m.get("name") %></option>
                        <%      } 
                            } %>
                        </select>
                        <select name="<%=TaschelConstant.END_KBN %>" size="1" tabindex="11">
                        <%
                            List<Map<String, Object>> endKbnList = (List<Map<String, Object>>) session.getAttribute(TaschelConstant.START_KBN);                                                    
                            
	                        // 初期値の設定
	                        int defaultEndKbn = 0;
	                        String tmpEndKbn = request.getParameter(TaschelConstant.END_KBN);
	                        if ( tmpEndKbn != null ) { defaultEndKbn = Integer.parseInt(tmpEndKbn); }
                        
                            for(Map<String, Object> m : endKbnList) {
                                Integer v = (Integer) m.get("kbn_value");
                                if ( v == defaultEndKbn ) {
                        %>                  
                            <option value="<%=v %>" selected><%=m.get("name") %></option>
                        <%      } else { %>             
                            <option value="<%=v %>"><%=m.get("name") %></option>
                        <%      } 
                            } %>
                        </select>
                    </div>                    
                </div>
                <div class="row">
                    <div class="columnHeader">
                        <label for="content">内容</label>
                    </div>                    
                    <div class="columnData">
                        <textarea id="content" name="<%=TaschelConstant.CONTENT %>" cols="30" rows="10" tabindex="12"><%=getSafeParameter(request,TaschelConstant.CONTENT) %></textarea>
                    </div>
                    <div class="columnData">
                        <input type="submit" name="<%=TaschelConstant.SUBMIT_TASK %>" value="登録" tabindex="14"/>
                        <input type="submit" name="<%=TaschelConstant.CANCEL %>" value="戻る" tabindex="15"/>
                    </div>
                </div>
                <div class="row">
                    <div class="columnHeader">
                        <label for="user" class="required">担当者</label>
                    </div>                    
                    <div class="columnData">
                        <select name="<%=TaschelConstant.TARGET_USER_ID %>" size="1" tabindex="13">
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
                        <input type="submit" name="<%=TaschelConstant.SUBMIT_USER %>" value="決定" tabindex="-1"/>
                    </div>
                </div>
            </div>
        </form>
        <hr class="clear" />           
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
                <td><%=m.get("id") %></td>
                <td><%=DisplayUtils.diplayDeadline((Date)m.get("deadline")) %></td>
                <td><%=DisplayUtils.display((Long)m.get("parent_id")) %></td>
                <td><%=m.get("name") %></td>
                <td><%=m.get("last_name") %> <%=m.get("first_name") %></td>
                <td><%=m.get("ki_name") %></td>
                <td><%=m.get("kstt_name") %></td>
            </tr>
            <% } %>
        </table>
    </body>
</html>