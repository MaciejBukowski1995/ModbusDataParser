<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>
<%@page import="models.RowModel"%>

<html>
<head>
    <title>Title From JSP</title>
    <style><%@include file="/WEB-INF/css/parsedData.css"%></style>
</head>
<body>
    <table>
        <tr class="tableHeader">
            <td>Variable Name</td>
            <td>Value</td>
            <td>Unit</td>
        </tr>
        <% List<RowModel> results = (ArrayList<RowModel>)request.getAttribute("results");

            for(RowModel rowModel : results)
            {
                out.print("<tr>");
                out.print("<td>" + rowModel.getNameOfVariable() + "</td>");
                out.print("<td>" + rowModel.getValue() + "</td>");
                out.print("<td>" + rowModel.getUnit() + "</td>");
                out.print("</tr>");
            }
        %>
    </table>
</body>
</html>
