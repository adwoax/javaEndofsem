<%-- index.jsp: Immediately redirects to /home (handled by HomeServlet) --%>
<%@ page contentType="text/html;charset=UTF-8" %>
<% response.sendRedirect(request.getContextPath() + "/home"); %>
