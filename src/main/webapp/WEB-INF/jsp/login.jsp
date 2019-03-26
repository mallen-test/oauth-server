<%--
  Created by IntelliJ IDEA.
  User: mallen
  Date: 3/16/19
  Time: 12:14 AM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <meta charset="UTF-8">
    <title>欢迎来到登录页面</title>
</head>
<body>
<h2>你将为<label style="color: orange">${appName}</label>授权，确认授权请登录</h2>
<form action="login" method="post">
    <p>帐号: <input type="text" name="username"/></p>
    <p>密码: <input type="password" name="password"/></p>
    <input type="submit" value="登录"/>
    <input type="hidden" value="${state}" name="state"/>
    <input type="hidden" value="${redirectUri}" name="redirectUri"/>
</form>
</body>
</html>
