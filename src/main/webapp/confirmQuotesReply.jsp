<%@page import="ds.gae.view.JSPSite"%>

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<% session.setAttribute("currentPage", JSPSite.CONFIRM_QUOTES_RESPONSE); %>
<% String renter = (String) session.getAttribute("renter"); %>

<%@include file="_header.jsp"%>

<div class="frameDiv" style="margin: 150px 150px;">
    <h2>Reply</h2>
    <div class="group">
        <p>
            An e-mail will be sent to <%=renter%> confirming if your reservation succeeded or not.
        </p>
    </div>
</div>

<%@include file="_footer.jsp"%>
