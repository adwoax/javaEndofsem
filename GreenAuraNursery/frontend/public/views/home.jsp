<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%--
  home.jsp — The Landing Page
  Displays: welcome message, best sellers, navigation
  Gets data from: HomeServlet (sets "bestSellers" and "loggedInUser")
--%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Green Aura Nursery</title>
    <style>
        /* ===== GLOBAL STYLES ===== */
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: 'Segoe UI', sans-serif; background: #f9fdf5; color: #2d3d2e; }

        /* ===== NAVIGATION BAR ===== */
        nav {
            background: #2e7d32;
            padding: 15px 30px;
            display: flex;
            justify-content: space-between;
            align-items: center;
            box-shadow: 0 2px 8px rgba(0,0,0,0.15);
        }
        nav .logo { color: white; font-size: 22px; font-weight: bold; letter-spacing: 1px; }
        nav .nav-links a {
            color: white;
            text-decoration: none;
            margin-left: 20px;
            font-size: 15px;
            padding: 8px 16px;
            border-radius: 20px;
            transition: background 0.2s;
        }
        nav .nav-links a:hover { background: rgba(255,255,255,0.2); }
        nav .nav-links a.btn-outline {
            border: 2px solid white;
        }

        /* ===== HERO SECTION ===== */
        .hero {
            background: linear-gradient(135deg, #1b5e20, #4caf50);
            color: white;
            text-align: center;
            padding: 80px 20px;
        }
        .hero h1 { font-size: 48px; margin-bottom: 15px; }
        .hero p  { font-size: 20px; opacity: 0.9; margin-bottom: 30px; }
        .hero a  {
            background: white;
            color: #2e7d32;
            padding: 14px 36px;
            border-radius: 30px;
            text-decoration: none;
            font-size: 17px;
            font-weight: bold;
            transition: transform 0.2s;
            display: inline-block;
        }
        .hero a:hover { transform: scale(1.05); }

        /* ===== BEST SELLERS SECTION ===== */
        .section { padding: 50px 30px; }
        .section h2 { text-align: center; font-size: 28px; color: #2e7d32; margin-bottom: 30px; }

        .plant-grid {
            display: flex;
            justify-content: center;
            gap: 25px;
            flex-wrap: wrap;
        }
        .plant-card {
            background: white;
            border-radius: 12px;
            box-shadow: 0 3px 12px rgba(0,0,0,0.1);
            padding: 25px 20px;
            text-align: center;
            width: 220px;
            transition: transform 0.2s;
        }
        .plant-card:hover { transform: translateY(-5px); }
        .plant-card .emoji { font-size: 50px; margin-bottom: 12px; }
        .plant-card h3 { color: #2e7d32; margin-bottom: 8px; }
        .plant-card .price { color: #555; font-size: 15px; margin-bottom: 15px; }
        .plant-card a {
            background: #2e7d32;
            color: white;
            padding: 8px 20px;
            border-radius: 20px;
            text-decoration: none;
            font-size: 14px;
        }

        /* ===== FOOTER ===== */
        footer {
            background: #1b5e20;
            color: #ccc;
            text-align: center;
            padding: 20px;
            font-size: 14px;
        }
    </style>
</head>
<body>

<!-- ===== NAVIGATION ===== -->
<nav>
    <div class="logo">🌿 Green Aura Nursery</div>
    <div class="nav-links">
        <a href="${pageContext.request.contextPath}/cart">🛒 My Cart</a>
        <c:choose>
            <c:when test="${loggedInUser != null}">
                <a href="${pageContext.request.contextPath}/logout">Logout</a>
            </c:when>
            <c:otherwise>
                <a href="${pageContext.request.contextPath}/views/register.html" class="btn-outline">Sign Up</a>
            </c:otherwise>
        </c:choose>
        <a href="#contact">Contact</a>
    </div>
</nav>

<!-- ===== HERO SECTION ===== -->
<div class="hero">
    <h1>🌿 Nature's Oasis</h1>
    <p>Bring life and beauty into your home with our handpicked plants.</p>
    <a href="${pageContext.request.contextPath}/catalogue">View Catalogue</a>
</div>

<!-- ===== BEST SELLERS ===== -->
<div class="section">
    <h2>🌸 Best Sellers</h2>
    <div class="plant-grid">
        <%--
          JSTL forEach loop — iterates over the List<Plant> passed by HomeServlet
          Each "plant" here is a Plant OBJECT — we access fields using getters:
              ${plant.name}  calls plant.getName()
              ${plant.price} calls plant.getPrice()
        --%>
        <c:forEach var="plant" items="${bestSellers}">
            <div class="plant-card">
                <div class="emoji">🌱</div>
                <h3>${plant.name}</h3>
                <p class="price">GH₵ ${plant.price}</p>
                <a href="${pageContext.request.contextPath}/catalogue">Shop Now</a>
            </div>
        </c:forEach>
    </div>
</div>

<!-- ===== CONTACT SECTION ===== -->
<div class="section" id="contact" style="background:#e8f5e9; border-radius:12px; margin: 0 30px 40px; text-align:center;">
    <h2>📞 Contact Us</h2>
    <p style="font-size:16px; color:#555; margin-top:10px;">
        Call us: <strong>024 000 0000</strong> &nbsp;|&nbsp;
        Email: <strong>hello@greenaura.com</strong> &nbsp;|&nbsp;
        Location: <strong>Accra, Ghana</strong>
    </p>
</div>

<footer>
    &copy; 2025 Green Aura Nursery. All rights reserved.
</footer>

</body>
</html>
