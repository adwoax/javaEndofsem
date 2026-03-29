<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%--
  cart.jsp — My Cart Page
  Gets data from: CartServlet
    - "cartItems"    → List<CartItem> (each CartItem has a Plant inside it)
    - "total"        → double (grand total)
    - "loggedInUser" → User object
--%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>My Cart — Green Aura</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: 'Segoe UI', sans-serif; background: #f9fdf5; }

        nav {
            background: #2e7d32; padding: 15px 30px;
            display: flex; justify-content: space-between; align-items: center;
        }
        nav .logo {
            color: white;
            font-size: 22px;
            font-weight: bold;
            display: inline-flex;
            align-items: center;
            gap: 8px;
        }
        nav .logo img { width: 28px; height: 28px; }
        nav a { color: white; text-decoration: none; margin-left: 20px;
                padding: 8px 16px; border-radius: 20px; }
        nav a:hover { background: rgba(255,255,255,0.2); }

        .page-header {
            background: linear-gradient(135deg, #388e3c, #66bb6a);
            color: white; text-align: center; padding: 35px 20px;
        }
        .page-header h1 { font-size: 32px; }

        .cart-container { max-width: 700px; margin: 40px auto; padding: 0 20px; }

        /* Empty cart state */
        .empty-cart {
            text-align: center; padding: 60px 20px;
            color: #777; font-size: 18px;
        }
        .empty-cart a {
            display: inline-block; margin-top: 20px;
            background: #2e7d32; color: white;
            padding: 12px 28px; border-radius: 25px;
            text-decoration: none; font-weight: bold;
        }

        /* Individual cart row */
        .cart-item {
            background: white; border-radius: 12px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.08);
            padding: 20px 24px; margin-bottom: 15px;
            display: flex; justify-content: space-between; align-items: center;
        }
        .cart-item .item-info h3 { color: #2e7d32; font-size: 18px; margin-bottom: 4px; }
        .cart-item .item-info p  { color: #777; font-size: 14px; }
        .cart-item .item-subtotal { font-size: 20px; font-weight: bold; color: #2e7d32; }

        /* Total summary box */
        .total-box {
            background: #e8f5e9; border-radius: 12px;
            padding: 24px 28px; margin-top: 20px;
            display: flex; justify-content: space-between; align-items: center;
        }
        .total-box .total-label { font-size: 20px; color: #333; font-weight: bold; }
        .total-box .total-amount { font-size: 28px; color: #2e7d32; font-weight: bold; }

        .btn-checkout {
            display: block; width: 100%; margin-top: 20px;
            background: #2e7d32; color: white;
            text-align: center; padding: 15px;
            border-radius: 30px; text-decoration: none;
            font-size: 17px; font-weight: bold;
            transition: background 0.2s;
        }
        .btn-checkout:hover { background: #1b5e20; }

        .continue-link { text-align: center; margin-top: 14px; }
        .continue-link a { color: #2e7d32; text-decoration: none; font-size: 14px; }

        footer { background: #1b5e20; color: #ccc; text-align: center; padding: 20px; font-size:14px; margin-top:50px; }
    </style>
</head>
<body>

<!-- NAVIGATION -->
<nav>
    <div class="logo"><img src="http://localhost:5500/assets/images/brand-logo.svg" alt="Green Aura Nursery logo"> Green Aura Nursery</div>
    <div>
        <a href="${pageContext.request.contextPath}/catalogue">Catalogue</a>
        <a href="${pageContext.request.contextPath}/logout">Logout</a>
    </div>
</nav>

<!-- PAGE HEADER -->
<div class="page-header">
    <h1>🛒 My Cart</h1>
    <p style="opacity:0.9; margin-top:8px;">Hello, ${loggedInUser.fullName}!</p>
</div>

<!-- CART CONTENT -->
<div class="cart-container">

    <c:choose>
        <%-- CASE 1: Cart is empty --%>
        <c:when test="${empty cartItems}">
            <div class="empty-cart">
                <p>🌱 Your cart is empty!</p>
                <a href="${pageContext.request.contextPath}/catalogue">Browse Plants</a>
            </div>
        </c:when>

        <%-- CASE 2: Cart has items — show them --%>
        <c:otherwise>

            <%--
              Loop through each CartItem object.
              Remember: CartItem HAS-A Plant (Composition).
              So we access:  ${item.plant.name}   → item.getPlant().getName()
                             ${item.quantity}      → item.getQuantity()
                             ${item.subtotal}      → item.getSubtotal()
            --%>
            <c:forEach var="item" items="${cartItems}">
                <div class="cart-item">
                    <div class="item-info">
                        <h3>🌿 ${item.plant.name}</h3>
                        <p>GH₵ ${item.plant.price} &times; ${item.quantity}</p>
                    </div>
                    <div class="item-subtotal">GH₵ ${item.subtotal}</div>
                </div>
            </c:forEach>

            <%-- TOTAL BOX --%>
            <div class="total-box">
                <span class="total-label">Total:</span>
                <span class="total-amount">GH₵ ${total}</span>
            </div>

            <%-- CHECKOUT BUTTON --%>
            <a href="${pageContext.request.contextPath}/checkout" class="btn-checkout">
                Proceed to Checkout →
            </a>

            <div class="continue-link">
                <a href="${pageContext.request.contextPath}/catalogue">← Continue Shopping</a>
            </div>

        </c:otherwise>
    </c:choose>

</div>

<footer><img src="http://localhost:5500/assets/images/brand-logo.svg" alt="Green Aura Nursery logo" style="width:16px;height:16px;vertical-align:text-bottom;margin-right:6px;">&copy; 2025 Green Aura Nursery</footer>
</body>
</html>
