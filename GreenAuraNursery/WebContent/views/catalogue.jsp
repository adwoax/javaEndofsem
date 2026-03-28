<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%--
  catalogue.jsp — Shows all plants from the database
  Gets data from: CatalogueServlet (sets "plants" list and "loggedInUser")
--%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Plant Catalogue — Green Aura</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: 'Segoe UI', sans-serif; background: #f9fdf5; color: #2d3d2e; }

        nav {
            background: #2e7d32; padding: 15px 30px;
            display: flex; justify-content: space-between; align-items: center;
        }
        nav .logo { color: white; font-size: 22px; font-weight: bold; }
        nav a { color: white; text-decoration: none; margin-left: 20px;
                padding: 8px 16px; border-radius: 20px; transition: background 0.2s; }
        nav a:hover { background: rgba(255,255,255,0.2); }

        .page-header {
            background: linear-gradient(135deg, #388e3c, #66bb6a);
            color: white; text-align: center; padding: 40px 20px;
        }
        .page-header h1 { font-size: 36px; }

        /* Success banner shown after adding to cart */
        .success-banner {
            background: #c8e6c9; color: #1b5e20;
            text-align: center; padding: 12px;
            font-weight: bold; border-bottom: 2px solid #4caf50;
        }

        .catalogue-grid {
            display: flex; flex-wrap: wrap;
            justify-content: center; gap: 25px;
            padding: 40px 30px;
        }

        .plant-card {
            background: white; border-radius: 14px;
            box-shadow: 0 3px 12px rgba(0,0,0,0.1);
            padding: 25px 20px; text-align: center; width: 230px;
            transition: transform 0.2s;
        }
        .plant-card:hover { transform: translateY(-5px); }
        .plant-card .plant-icon { font-size: 55px; margin-bottom: 12px; }
        .plant-card h3 { color: #2e7d32; font-size: 18px; margin-bottom: 6px; }
        .plant-card .desc { color: #777; font-size: 13px; margin-bottom: 10px; min-height: 36px; }
        .plant-card .price { font-size: 20px; font-weight: bold; color: #2e7d32; margin-bottom: 15px; }

        /* "Add to Cart" form button */
        .plant-card form button {
            background: #2e7d32; color: white;
            border: none; padding: 10px 24px;
            border-radius: 25px; cursor: pointer;
            font-size: 14px; font-weight: bold;
            transition: background 0.2s;
        }
        .plant-card form button:hover { background: #1b5e20; }

        footer { background: #1b5e20; color: #ccc; text-align: center; padding: 20px; font-size:14px; }
    </style>
</head>
<body>

<!-- NAVIGATION -->
<nav>
    <div class="logo">🌿 Green Aura Nursery</div>
    <div>
        <a href="${pageContext.request.contextPath}/cart">🛒 My Cart</a>
        <c:choose>
            <c:when test="${loggedInUser != null}">
                <a href="${pageContext.request.contextPath}/logout">Logout</a>
            </c:when>
            <c:otherwise>
                <a href="${pageContext.request.contextPath}/register">Sign Up</a>
                <a href="${pageContext.request.contextPath}/login">Login</a>
            </c:otherwise>
        </c:choose>
        <a href="${pageContext.request.contextPath}/home">Home</a>
    </div>
</nav>

<!-- SUCCESS BANNER (shown after adding to cart) -->
<c:if test="${not empty successMessage}">
    <div class="success-banner">✅ ${successMessage}</div>
</c:if>

<!-- PAGE HEADER -->
<div class="page-header">
    <h1>🌱 Our Plant Catalogue</h1>
    <p style="margin-top:10px; opacity:0.9;">Fresh plants delivered to your door across Ghana</p>
</div>

<!-- PLANT GRID -->
<div class="catalogue-grid">
    <%--
      Loop through every Plant object in the "plants" list.
      CatalogueServlet fetched these from the database via PlantDAO.
      Each ${plant.name} calls plant.getName() — Encapsulation in action!
    --%>
    <c:forEach var="plant" items="${plants}">
        <div class="plant-card">
            <div class="plant-icon">🌿</div>
            <h3>${plant.name}</h3>
            <p class="desc">${plant.description}</p>
            <p class="price">GH₵ ${plant.price}</p>

            <%--
              "Add to Cart" FORM
              Submits a POST to /addToCart with the plant's ID as a hidden field.
              AddToCartServlet reads this plantId to know which plant to add.
            --%>
            <form action="${pageContext.request.contextPath}/addToCart" method="post">
                <input type="hidden" name="plantId" value="${plant.id}" />
                <button type="submit">🛒 Add to Cart</button>
            </form>
        </div>
    </c:forEach>
</div>

<footer>&copy; 2025 Green Aura Nursery</footer>
</body>
</html>
