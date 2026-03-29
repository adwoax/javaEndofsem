<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%--
  confirmation.jsp — Order Success Page
  The final step in the checkout flow.
  Gets: "loggedInUser" from ConfirmationServlet
--%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Order Confirmed — Green Aura</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body {
            font-family: 'Segoe UI', sans-serif;
            background: linear-gradient(135deg, #e8f5e9, #f1f8e9);
            min-height: 100vh;
            display: flex; flex-direction: column; align-items: center; justify-content: center;
        }

        .confirmation-card {
            background: white; border-radius: 20px;
            box-shadow: 0 10px 40px rgba(0,0,0,0.12);
            padding: 50px 40px; text-align: center;
            max-width: 480px; width: 90%;
        }

        .checkmark { font-size: 72px; margin-bottom: 16px; }

        h1 { color: #2e7d32; font-size: 28px; margin-bottom: 12px; }

        .customer-name {
            font-size: 18px; color: #555;
            margin-bottom: 20px;
        }

        .message-box {
            background: #f1f8e9; border-radius: 12px;
            padding: 20px; margin: 20px 0;
            color: #388e3c; font-size: 16px; line-height: 1.6;
        }

        .order-info {
            color: #777; font-size: 14px; margin-bottom: 28px;
        }

        .btn-home {
            display: inline-block;
            background: #2e7d32; color: white;
            padding: 14px 36px; border-radius: 30px;
            text-decoration: none; font-size: 16px; font-weight: bold;
            transition: background 0.2s;
        }
        .btn-home:hover { background: #1b5e20; }

        .btn-catalogue {
            display: inline-block; margin-top: 12px;
            color: #2e7d32; text-decoration: none;
            font-size: 14px;
        }
    </style>
</head>
<body>

<div class="confirmation-card">

    <div class="checkmark">✅</div>

    <h1>Order Successful!</h1>

    <c:if test="${loggedInUser != null}">
        <p class="customer-name">
            Thank you, <strong>${loggedInUser.fullName}</strong>! 🌿
        </p>
    </c:if>

    <div class="message-box">
        🌱 Your order has been received.<br>
        You will receive a call from the nursery shortly<br>
        to confirm delivery to your address.
    </div>

    <p class="order-info">
        Our team will contact you on <strong>${loggedInUser.phone}</strong>.
    </p>

    <a href="${pageContext.request.contextPath}/home" class="btn-home">
        🏠 Back to Home
    </a>

    <br>

    <a href="${pageContext.request.contextPath}/catalogue" class="btn-catalogue">
        Continue Shopping →
    </a>

</div>

</body>
</html>
