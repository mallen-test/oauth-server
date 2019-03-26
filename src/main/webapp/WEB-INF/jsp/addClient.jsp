<%--
  Created by IntelliJ IDEA.
  User: mallen
  Date: 3/21/19
  Time: 4:34 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <%@include file="head.jsp" %>
    <title>新增客户端</title>
</head>
<body>

<div class="panel panel-default">
    <div class="panel-heading">
        <h3 class="panel-title">新增客户端</h3>
    </div>
    <div class="panel-body">
        <form action="add" method="post">
            <ul class="list-group">
                <li class="list-group-item">
                    Client ID：<input type="text" value="${clientId}" readonly="true" name="clientId">
                </li>
                <li class="list-group-item">
                    Client Secutiry：<input type="text" value="${clientSecurity}" readonly="true" name="clientSecurity">
                </li>
                <li class="list-group-item">
                    应用名称：<input type="text" class="form-control" name="appName">
                </li>

                <li class="list-group-item">
                    redirectUris（如果有多个，请使用英文分号分隔）：
                    <input type="text" class="form-control" name="redirectUris">
                </li>
                <li class="list-group-item">
                    说明：<input type="text" class="form-control" name="remark">
                </li>
                <li class="list-group-item">
                    <button type="submit" style="margin-left:50px" id="btn_" class="btn btn-primary">新增</button>
                </li>
            </ul>
        </form>
    </div>
</div>

<%@include file="commonjs.jsp" %>
</body>
</html>
