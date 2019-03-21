<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
  Created by IntelliJ IDEA.
  User: mallen
  Date: 3/20/19
  Time: 11:17 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <%@include file="head.jsp" %>
    <title>查询客户端信息</title>
</head>
<body>
<div class="panel-body" style="padding-bottom:0px;">
    <div class="panel panel-default">
        <div class="panel-heading">查询条件</div>
        <div class="panel-body">
            <form id="formSearch" class="form-horizontal">
                <div class="form-group" style="margin-top:15px">
                    <label class="control-label col-sm-1" for="txt_search_departmentname">部门名称</label>
                    <div class="col-sm-3">
                        <input type="text" class="form-control" id="txt_search_departmentname">
                    </div>
                    <label class="control-label col-sm-1" for="txt_search_statu">状态</label>
                    <div class="col-sm-3">
                        <input type="text" class="form-control" id="txt_search_statu">
                    </div>
                    <div class="col-sm-4" style="text-align:left;">
                        <button type="button" style="margin-left:50px" id="btn_query" class="btn btn-primary">查询
                        </button>
                    </div>
                </div>
            </form>
        </div>
    </div>

    <div id="toolbar" class="btn-group">
        <button id="btn_add" type="button" class="btn btn-default">
            <span class="glyphicon glyphicon-plus" aria-hidden="true"></span>新增
        </button>
        <button id="btn_edit" type="button" class="btn btn-default">
            <span class="glyphicon glyphicon-pencil" aria-hidden="true"></span>修改
        </button>
        <button id="btn_delete" type="button" class="btn btn-default">
            <span class="glyphicon glyphicon-remove" aria-hidden="true"></span>删除
        </button>
    </div>
    <table data-toggle="table">
        <thead>
        <tr>
            <th>Client ID</th>
            <th>Client Security</th>
            <th>创建时间</th>
            <th>备注</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach items="${data}" var="client">
            <tr>
                <td>${client.clientId}</td>
                <td>${client.clientSecurity}</td>
                <td>${client.createdTime}</td>
                <td>${client.remark}</td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</div>

<%@include file="commonjs.jsp" %>
</body>
</html>
