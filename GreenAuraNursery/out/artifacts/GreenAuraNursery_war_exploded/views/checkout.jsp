<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%--
  checkout.jsp — Checkout Page
  Gets data from: CheckoutServlet.doGet()
    - "loggedInUser" → User object (pre-filled details)
    - "cartItems"    → List<CartItem>
    - "total"        → double
--%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Checkout — Green Aura</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: 'Segoe UI', sans-serif; background: #f9fdf5; }

        nav {
            background: #2e7d32; padding: 15px 30px;
            display: flex; justify-content: space-between; align-items: center;
        }
        nav .logo { color: white; font-size: 22px; font-weight: bold; }
        nav a { color: white; text-decoration: none; margin-left: 20px;
                padding: 8px 16px; border-radius: 20px; }
        nav a:hover { background: rgba(255,255,255,0.2); }

        .page-header {
            background: linear-gradient(135deg, #388e3c, #66bb6a);
            color: white; text-align: center; padding: 35px 20px;
        }
        .page-header h1 { font-size: 32px; }

        .checkout-container { max-width: 650px; margin: 40px auto; padding: 0 20px; }

        /* Error box */
        .error-box {
            background: #ffebee; color: #c62828;
            border-left: 4px solid #ef5350;
            padding: 12px 16px; border-radius: 6px; margin-bottom: 20px;
        }

        /* Section cards */
        .card {
            background: white; border-radius: 14px;
            box-shadow: 0 3px 12px rgba(0,0,0,0.08);
            padding: 28px; margin-bottom: 24px;
        }
        .card h2 { color: #2e7d32; font-size: 18px; margin-bottom: 20px;
                   border-bottom: 2px solid #e8f5e9; padding-bottom: 10px; }

        /* Detail rows */
        .detail-row {
            display: flex; justify-content: space-between;
            padding: 10px 0; border-bottom: 1px solid #f0f0f0;
            font-size: 15px;
        }
        .detail-row:last-child { border-bottom: none; }
        .detail-row .label { color: #777; }
        .detail-row .value { font-weight: 600; color: #333; }

        /* Order items */
        .order-item {
            display: flex; justify-content: space-between;
            padding: 10px 0; border-bottom: 1px solid #f0f0f0;
        }
        .order-item:last-child { border-bottom: none; }
        .order-item .item-name { color: #2e7d32; font-weight: 600; }
        .order-item .item-detail { color: #777; font-size: 13px; }
        .order-item .item-price { font-weight: bold; color: #333; }

        /* Total */
        .total-row {
            display: flex; justify-content: space-between; align-items: center;
            background: #e8f5e9; border-radius: 10px;
            padding: 15px 20px; margin-top: 15px;
        }
        .total-row .total-label { font-size: 18px; font-weight: bold; }
        .total-row .total-amount { font-size: 26px; font-weight: bold; color: #2e7d32; }

        /* Place Order button — submits the checkout form */
        .btn-order {
            width: 100%; padding: 16px;
            background: #2e7d32; color: white;
            border: none; border-radius: 30px;
            font-size: 17px; font-weight: bold;
            cursor: pointer; margin-top: 10px;
            transition: background 0.2s;
        }
        .btn-order:hover { background: #1b5e20; }

        .back-link { text-align: center; margin-top: 14px; }
        .back-link a { color: #2e7d32; text-decoration: none; font-size: 14px; }

        footer { background: #1b5e20; color: #ccc; text-align: center; padding: 20px; font-size:14px; margin-top:40px; }
    </style>
</head>
<body>

<!-- NAVIGATION -->
<nav>
    <div class="logo">🌿 Green Aura Nursery</div>
    <div>
        <a href="${pageContext.request.contextPath}/cart">🛒 My Cart</a>
        <a href="${pageContext.request.contextPath}/logout">Logout</a>
    </div>
</nav>

<!-- PAGE HEADER -->
<div class="page-header">
    <h1>📋 Checkout</h1>
    <p style="opacity:0.9; margin-top:8px;">Review your order before confirming</p>
</div>

<div class="checkout-container">

    <%-- Show error if order failed --%>
    <c:if test="${not empty errorMessage}">
        <div class="error-box">⚠️ ${errorMessage}</div>
    </c:if>

    <%--
      DELIVERY DETAILS CARD
      These are pulled from the User object stored in the session.
      The user does NOT need to type them again — they were saved at signup.
      loggedInUser.fullName  → loggedInUser.getFullName()
      loggedInUser.phone     → loggedInUser.getPhone()
      loggedInUser.address   → loggedInUser.getAddress()
      This is Encapsulation + Abstraction working together!
    --%>
    <div class="card">
        <h2>📦 Delivery Details</h2>

        <div class="detail-row">
            <span class="label">Name:</span>
            <span class="value">${loggedInUser.fullName}</span>
        </div>
        <div class="detail-row">
            <span class="label">Phone:</span>
            <span class="value">${loggedInUser.phone}</span>
        </div>
        <div class="detail-row">
            <span class="label">Address:</span>
            <span class="value">${loggedInUser.address}</span>
        </div>
        <div class="detail-row">
            <span class="label">Email:</span>
            <span class="value">${loggedInUser.email}</span>
        </div>
    </div>

    <%-- ORDER SUMMARY CARD --%>
    <div class="card">
        <h2>🌿 Order Summary</h2>

        <c:forEach var="item" items="${cartItems}">
            <div class="order-item">
                <div>
                    <div class="item-name">${item.plant.name}</div>
                    <div class="item-detail">GH₵ ${item.plant.price} &times; ${item.quantity}</div>
                </div>
                <div class="item-price">GH₵ ${item.subtotal}</div>
            </div>
        </c:forEach>

        <div class="total-row">
            <span class="total-label">Total to Pay:</span>
            <span class="total-amount">GH₵ ${total}</span>
        </div>
    </div>

    <%--
      PLACE ORDER FORM
      Submits POST to /checkout → CheckoutServlet.doPost()
      No extra fields needed — all data is already on the server (session + DB)
    --%>
    <form action="${pageContext.request.contextPath}/checkout" method="post">
        <button type="submit" class="btn-order">✅ PLACE ORDER</button>
    </form>

    <div class="back-link">
        <a href="${pageContext.request.contextPath}/cart">← Back to Cart</a>
    </div>

</div>

<footer>&copy; 2025 Green Aura Nursery</footer>
</body>
</html>
