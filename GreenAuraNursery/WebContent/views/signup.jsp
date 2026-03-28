<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%--
  signup.jsp — Customer Registration Page
  Submits to: RegisterServlet (POST /register)
  Shows error messages set by RegisterServlet via request.setAttribute()
--%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Sign Up — Green Aura</title>
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
            padding: 40px 36px; width: 420px;
        }

        .form-logo { text-align: center; margin-bottom: 6px; font-size: 32px; }
        h2 { text-align: center; color: #2e7d32; margin-bottom: 24px; font-size: 24px; }

        .error-box {
            background: #ffebee; color: #c62828;
            border-left: 4px solid #ef5350;
            padding: 12px 16px; border-radius: 6px;
            margin-bottom: 18px; font-size: 14px;
        }

        label { display: block; color: #555; font-size: 14px; margin-bottom: 5px; }
        input[type="text"], input[type="email"], input[type="password"],
        input[type="tel"], textarea {
            width: 100%; padding: 11px 14px;
            border: 1.5px solid #ddd; border-radius: 8px;
            font-size: 15px; margin-bottom: 16px;
            transition: border-color 0.2s;
        }
        input:focus, textarea:focus {
            outline: none; border-color: #4caf50;
        }
        textarea { height: 70px; resize: vertical; font-family: inherit; }

        .btn-submit {
            width: 100%; padding: 13px;
            background: #2e7d32; color: white;
            border: none; border-radius: 25px;
            font-size: 16px; font-weight: bold;
            cursor: pointer; transition: background 0.2s;
        }
        .btn-submit:hover { background: #1b5e20; }

        .login-link { text-align: center; margin-top: 18px; font-size: 14px; color: #555; }
        .login-link a { color: #2e7d32; font-weight: bold; text-decoration: none; }
    </style>
</head>
<body>

<div class="form-container">
    <div class="form-logo">🌿</div>
    <h2>Create an Account</h2>

    <%-- Show error message from RegisterServlet (if any) --%>
    <c:if test="${not empty errorMessage}">
        <div class="error-box">⚠️ ${errorMessage}</div>
    </c:if>

    <%--
      FORM: Posts to /register (handled by RegisterServlet.doPost())
      Each input name="" must match request.getParameter("fieldName") in the servlet
    --%>
    <form action="${pageContext.request.contextPath}/register" method="post">

        <label for="fullName">Full Name *</label>
        <input type="text" id="fullName" name="fullName"
               placeholder="e.g. Nana Ama" required />

        <label for="email">Email Address *</label>
        <input type="email" id="email" name="email"
               placeholder="e.g. nana@gmail.com" required />

        <label for="password">Password *</label>
        <input type="password" id="password" name="password"
               placeholder="Choose a strong password" required />

        <label for="phone">Phone Number</label>
        <input type="tel" id="phone" name="phone"
               placeholder="e.g. 0244000000" />

        <label for="address">Delivery Address</label>
        <textarea id="address" name="address"
                  placeholder="e.g. 12 Cantonments Road, Accra"></textarea>

        <button type="submit" class="btn-submit">CREATE ACCOUNT</button>
    </form>

    <div class="login-link">
        Already have an account? <a href="${pageContext.request.contextPath}/login">Login here</a>
    </div>
</div>

</body>
</html>
