<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%--
  login.jsp — Login Page
  Submits to: LoginServlet (POST /login)
--%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Login — Green Aura</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body {
            font-family: 'Segoe UI', sans-serif;
            background: linear-gradient(135deg, #e8f5e9, #f1f8e9);
            min-height: 100vh;
            display: flex; align-items: center; justify-content: center;
        }
        .form-container {
            background: white; border-radius: 16px;
            box-shadow: 0 8px 30px rgba(0,0,0,0.12);
            padding: 40px 36px; width: 400px;
        }
        .form-logo { text-align: center; font-size: 36px; margin-bottom: 6px; }
        h2 { text-align: center; color: #2e7d32; margin-bottom: 24px; }

        .error-box {
            background: #ffebee; color: #c62828;
            border-left: 4px solid #ef5350;
            padding: 12px 16px; border-radius: 6px;
            margin-bottom: 18px; font-size: 14px;
        }
        .success-box {
            background: #e8f5e9; color: #1b5e20;
            border-left: 4px solid #4caf50;
            padding: 12px 16px; border-radius: 6px;
            margin-bottom: 18px; font-size: 14px;
        }

        label { display: block; color: #555; font-size: 14px; margin-bottom: 5px; }
        input[type="email"], input[type="password"] {
            width: 100%; padding: 11px 14px;
            border: 1.5px solid #ddd; border-radius: 8px;
            font-size: 15px; margin-bottom: 18px;
            transition: border-color 0.2s;
        }
        input:focus { outline: none; border-color: #4caf50; }

        .btn-login {
            width: 100%; padding: 13px;
            background: #2e7d32; color: white;
            border: none; border-radius: 25px;
            font-size: 16px; font-weight: bold;
            cursor: pointer; transition: background 0.2s;
        }
        .btn-login:hover { background: #1b5e20; }

        .signup-link { text-align: center; margin-top: 18px; font-size: 14px; color: #555; }
        .signup-link a { color: #2e7d32; font-weight: bold; text-decoration: none; }
    </style>
</head>
<body>

<div class="form-container">
    <div class="form-logo">🔑</div>
    <h2>Welcome Back</h2>

    <%-- Success message after registration --%>
    <c:if test="${not empty successMessage}">
        <div class="success-box">✅ ${successMessage}</div>
    </c:if>

    <%-- Error message from LoginServlet --%>
    <c:if test="${not empty errorMessage}">
        <div class="error-box">⚠️ ${errorMessage}</div>
    </c:if>

    <form action="${pageContext.request.contextPath}/login" method="post">

        <label for="email">Email Address</label>
        <input type="email" id="email" name="email"
               placeholder="Enter your email" required />

        <label for="password">Password</label>
        <input type="password" id="password" name="password"
               placeholder="Enter your password" required />

        <button type="submit" class="btn-login">LOGIN</button>
    </form>

    <div class="signup-link">
        Don't have an account? <a href="${pageContext.request.contextPath}/register">Sign Up</a>
    </div>
</div>

</body>
</html>
