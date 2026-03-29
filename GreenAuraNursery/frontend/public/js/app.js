(function () {
    function resolveApiBase() {
        const override = window.localStorage ? window.localStorage.getItem("GREENAURA_API_BASE") : "";
        if (override) {
            return String(override).replace(/\/+$/, "");
        }

        const protocol = window.location.protocol;
        const hostname = window.location.hostname;
        if ((protocol === "http:" || protocol === "https:") && hostname) {
            return protocol + "//" + hostname + ":8080/GreenAuraNursery/api";
        }

        // Fallback helps when pages are opened from non-http contexts.
        return "http://localhost:8080/GreenAuraNursery/api";
    }

    const API_BASE = resolveApiBase();

    function request(path, options) {
        const config = options || {};
        const headers = Object.assign({ "Content-Type": "application/json" }, config.headers || {});
        return fetch(API_BASE + path, {
            method: config.method || "GET",
            credentials: "include",
            headers,
            body: config.body ? JSON.stringify(config.body) : undefined
        }).then(function (res) {
            return res.json().catch(function () {
                return { status: "error", message: "Invalid server response" };
            });
        });
    }

    function formatMoney(value) {
        const num = Number(value || 0);
        return "GHc " + num.toFixed(2);
    }

    function resolvePlantImageUrl(imageUrl, plantName) {
        const raw = String(imageUrl || "").trim();
        const imageBase = "/assets/images/";

        if (/^https?:\/\//i.test(raw) || raw.indexOf("data:") === 0) {
            return raw;
        }

        const byLegacyFile = {
            "aloe.jpg": "greenaura.webp",
            "rose.jpg": "candalebra.jpg",
            "mint.jpg": "parsley.png",
            "lily.jpg": "candalebra.jpg",
            "snake.jpg": "snake plant.jpg",
            "bamboo.jpg": "coconutTREE.png"
        };

        const byPlantName = {
            "aloe vera": "greenaura.webp",
            "aloe": "greenaura.webp",
            "rose": "candalebra.jpg",
            "mint": "parsley.png",
            "peace lily": "candalebra.jpg",
            "lily": "candalebra.jpg",
            "snake plant": "snake plant.jpg",
            "bamboo palm": "coconutTREE.png",
            "bamboo": "coconutTREE.png",
            "hass avocado": "hassavocadoTREE.png",
            "avocado": "hassavocadoTREE.png",
            "orange": "orangeTREE.png",
            "lemon": "lemonPLANT.png",
            "mango": "mangoTREE.png",
            "fig": "figTREE.png",
            "guava": "guavaTREE.png",
            "miracle berry": "miracleberryTREE.png",
            "miracle fruit": "miracleberryTREE.png",
            "cactus": "candelabra cactus.jpg"
        };

        if (raw.indexOf("images/") === 0) {
            const legacyName = raw.split("/").pop() || "";
            const mappedLegacy = byLegacyFile[legacyName];
            if (mappedLegacy) {
                return imageBase + mappedLegacy;
            }
        }

        if (plantName) {
            const normalizedName = plantName.toLowerCase().trim();
            if (byPlantName[normalizedName]) {
                return imageBase + byPlantName[normalizedName];
            }
            for (const key in byPlantName) {
                if (normalizedName.includes(key) || key.includes(normalizedName)) {
                    return imageBase + byPlantName[key];
                }
            }
        }

        return imageBase + "greenaura.webp";
    }

    function toast(message, isError) {
        const el = document.createElement("div");
        el.className = "app-toast" + (isError ? " error" : "");
        el.textContent = message;
        document.body.appendChild(el);
        window.setTimeout(function () { el.classList.add("show"); }, 10);
        window.setTimeout(function () {
            el.classList.remove("show");
            window.setTimeout(function () { el.remove(); }, 250);
        }, 2200);
    }

    function renderLoadingState(container, message) {
        if (!container) {
            return;
        }
        const text = String(message || "Loading...");
        container.innerHTML = "" +
            "<div class=\"loading-state\" role=\"status\" aria-live=\"polite\">" +
            "<span class=\"loading-spinner\" aria-hidden=\"true\"></span>" +
            "<p>" + text + "</p>" +
            "</div>";
    }

    function updateCartCount() {
        const cartButton = document.getElementById("cartButton");
        if (!cartButton) {
            return;
        }
        request("/cart").then(function (res) {
            const items = (res && res.data && res.data.cartItems) || [];
            const count = items.reduce(function (sum, item) {
                return sum + Number(item.quantity || 0);
            }, 0);
            cartButton.textContent = "My Cart (" + count + ")";
        }).catch(function () {
            cartButton.textContent = "My Cart (0)";
        });
    }

    let cartPlantIdSet = new Set();

    function setCartButtonVisual(btn, isInCart) {
        if (!btn) {
            return;
        }
        btn.classList.toggle("in-cart", isInCart);
        btn.setAttribute("aria-pressed", isInCart ? "true" : "false");
        btn.innerHTML = isInCart ? "Added &#10003;" : "Add";
    }

    function syncCartButtonVisuals() {
        document.querySelectorAll(".add-to-cart-btn, .cat-add-btn").forEach(function (btn) {
            const plantId = Number(btn.getAttribute("data-plant-id"));
            setCartButtonVisual(btn, cartPlantIdSet.has(plantId));
        });
    }

    function refreshCartState() {
        return request("/cart")
            .then(function (res) {
                const ok = res && String(res.status || "").toLowerCase() === "success";
                if (!ok) {
                    cartPlantIdSet = new Set();
                    syncCartButtonVisuals();
                    return;
                }
                const items = (res.data && res.data.cartItems) || [];
                cartPlantIdSet = new Set(items
                    .map(function (item) { return item && item.plant ? Number(item.plant.id) : 0; })
                    .filter(function (id) { return id > 0; }));
                syncCartButtonVisuals();
            })
            .catch(function () {
                cartPlantIdSet = new Set();
                syncCartButtonVisuals();
            });
    }

    function bindCartToggleButton(btn, loginPath) {
        const plantId = Number(btn.getAttribute("data-plant-id"));
        if (!plantId) {
            return;
        }

        setCartButtonVisual(btn, cartPlantIdSet.has(plantId));

        btn.addEventListener("click", function () {
            const isInCart = cartPlantIdSet.has(plantId);
            const endpoint = isInCart ? "/removeFromCart" : "/addToCart";

            btn.disabled = true;
            request(endpoint, { method: "POST", body: { plantId: plantId } })
                .then(function (res) {
                    if (res && String(res.status || "").toLowerCase() === "success") {
                        toast(isInCart ? "Removed from cart" : "Added to cart");
                        return refreshCartState().then(function () {
                            updateCartCount();
                        });
                    }

                    const msg = (res && res.message) || "Please login to manage your cart.";
                    toast(msg, true);
                    if (String(msg).toLowerCase().indexOf("not authenticated") !== -1) {
                        window.setTimeout(function () { window.location.href = loginPath; }, 600);
                    }
                    return null;
                })
                .catch(function () {
                    toast("Could not reach the backend server.", true);
                })
                .finally(function () {
                    btn.disabled = false;
                });
        });
    }

    function bindPlantSelectButton(btn, cartPath) {
        const plantId = Number(btn.getAttribute("data-plant-id"));
        if (!plantId) {
            return;
        }

        btn.addEventListener("click", function () {
            const target = String(cartPath || "views/cart.html") + "?plantId=" + encodeURIComponent(String(plantId));
            window.location.href = target;
        });
    }

    function bindAuthIndicator() {
        const accountLink = document.querySelector(".account-link");
        if (!accountLink) {
            return;
        }
        const onViewsPage = window.location.pathname.toLowerCase().indexOf("/views/") !== -1;
        const cartsHref = onViewsPage ? "carts.html" : "views/carts.html";
        const myAccountHref = onViewsPage ? "my-account.html" : "views/my-account.html";
        const myOrdersHref = onViewsPage ? "my-orders.html" : "views/my-orders.html";

        const host = document.querySelector(".account-actions") || accountLink.parentElement;
        if (!host) {
            return;
        }

        const separator = document.querySelector(".account-inline-separator");
        const registerInline = document.querySelector(".account-register-link");

        const closeMenu = function () {
            const menu = document.getElementById("accountMenu");
            if (menu) {
                menu.classList.remove("show");
            }
        };

        const toggleMenu = function () {
            const menu = document.getElementById("accountMenu");
            if (menu) {
                menu.classList.toggle("show");
            }
        };

        document.addEventListener("click", function (e) {
            if (!host.contains(e.target)) {
                closeMenu();
            }
        });

        const showLoggedOutState = function () {
            accountLink.classList.remove("logged-in");
            accountLink.classList.add("logged-out-link");
            accountLink.setAttribute("href", "views/login.html");
            accountLink.setAttribute("aria-label", "Sign in or register");
            accountLink.textContent = "Log in";

            const existingMenu = document.getElementById("accountMenu");
            if (existingMenu) {
                existingMenu.remove();
            }

            if (separator) {
                separator.style.display = "inline";
            }
            if (registerInline) {
                registerInline.style.display = "inline";
            }

            accountLink.onclick = null;
        };

        const showLoggedInState = function () {
            accountLink.classList.add("logged-in");
            accountLink.classList.remove("logged-out-link");
            accountLink.setAttribute("href", myAccountHref);
            accountLink.setAttribute("aria-label", "My account");
            accountLink.innerHTML = "<span class=\"account-avatar\" aria-hidden=\"true\">\u25cf</span>";

            if (separator) {
                separator.style.display = "none";
            }
            if (registerInline) {
                registerInline.style.display = "none";
            }

            let menu = document.getElementById("accountMenu");
            if (!menu) {
                menu = document.createElement("div");
                menu.id = "accountMenu";
                menu.className = "account-menu";
                host.appendChild(menu);
            }

            menu.innerHTML = "" +
                "<a href=\"" + myOrdersHref + "\"><i class=\"fas fa-clipboard-list\" aria-hidden=\"true\"></i> My Orders</a>" +
                "<a href=\"" + myAccountHref + "\"><i class=\"fas fa-user\" aria-hidden=\"true\"></i> My Account</a>" +
                "<button id=\"accountLogoutBtn\" type=\"button\"><i class=\"fas fa-sign-out-alt\" aria-hidden=\"true\"></i> Logout</button>";

            accountLink.onclick = function (e) {
                e.preventDefault();
                toggleMenu();
            };

            const logoutBtn = document.getElementById("accountLogoutBtn");
            if (logoutBtn) {
                logoutBtn.addEventListener("click", function () {
                    request("/logout")
                        .then(function () {
                            window.location.href = "index.html";
                        })
                        .catch(function () {
                            window.location.href = "index.html";
                        });
                });
            }
        };

        request("/cart")
            .then(function (res) {
                if (isAuthenticatedResponse(res)) {
                    showLoggedInState();
                    return;
                }
                showLoggedOutState();
            })
            .catch(function () {
                showLoggedOutState();
            });
    }

    function normalizeRegisterLinks() {
        const onViewsPage = window.location.pathname.toLowerCase().indexOf("/views/") !== -1;
        const registerHref = onViewsPage ? "register.html" : "views/register.html";

        const anchors = document.querySelectorAll("a[href]");
        anchors.forEach(function (anchor) {
            const text = String(anchor.textContent || "").trim().toLowerCase();
            if (!text) {
                return;
            }
            if (text.indexOf("register") === -1 && text.indexOf("sign up") === -1) {
                return;
            }
            const href = String(anchor.getAttribute("href") || "").toLowerCase();
            if (href.indexOf("login.html") !== -1 || href.indexOf("signup.html") !== -1 || href === "#") {
                anchor.setAttribute("href", registerHref);
            }
        });
    }

    function bindHomepageRegisterGuards() {
        const isHome = /\/index\.html$|\/$/i.test(window.location.pathname);
        if (!isHome) {
            return;
        }

        document.addEventListener("click", function (e) {
            const link = e.target && e.target.closest ? e.target.closest("a[href]") : null;
            if (!link) {
                return;
            }

            if (link.classList.contains("account-link")) {
                return;
            }

            const text = String(link.textContent || "").trim().toLowerCase();
            if (text.indexOf("register") === -1 && text.indexOf("sign up") === -1) {
                return;
            }

            e.preventDefault();
            window.location.href = "views/register.html";
        }, true);
    }

    function bindHomepageActions() {
        const onViewsPage = window.location.pathname.toLowerCase().indexOf("/views/") !== -1;
        const cartTarget = onViewsPage ? "carts.html" : "views/carts.html";
        const catalogueTarget = onViewsPage ? "catalogue.html" : "views/catalogue.html";

        const cartButton = document.getElementById("cartButton");
        if (cartButton) {
            cartButton.addEventListener("click", function () {
                window.location.href = cartTarget;
            });
        }

        const searchInput = document.getElementById("searchInput");
        const searchButton = document.getElementById("searchButton");
        if (searchInput && searchButton) {
            const runSearch = function () {
                const q = (searchInput.value || "").trim();
                const target = catalogueTarget + (q ? ("?q=" + encodeURIComponent(q)) : "");
                window.location.href = target;
            };
            searchButton.addEventListener("click", runSearch);
            searchInput.addEventListener("keydown", function (e) {
                if (e.key === "Enter") {
                    e.preventDefault();
                    runSearch();
                }
            });
        }

        const productsWrap = document.querySelector(".featured-grid .products");
        if (productsWrap) {
            renderLoadingState(productsWrap, "Loading featured plants...");
            request("/catalogue")
                .then(function (res) {
                    const plants = (res && res.data && res.data.plants) || [];
                    if (!plants.length) {
                        productsWrap.innerHTML = "<p class=\"empty-state\">No featured plants available right now.</p>";
                        return;
                    }
                    const featured = plants.slice(0, 4);
                    productsWrap.innerHTML = featured.map(function (plant) {
                        const image = resolvePlantImageUrl(plant.imageUrl, plant.name);
                        return "" +
                            "<article class=\"product-card\">" +
                                "<img src=\"" + image + "\" alt=\"" + (plant.name || "Plant") + "\" onerror=\"this.onerror=null;this.src='/assets/images/greenaura.webp';\">" +
                            "<h4>" + (plant.name || "Plant") + "</h4>" +
                            "<p>" + (plant.description || "Fresh and healthy nursery plant.") + "</p>" +
                            "<div class=\"price-row\"><span>" + formatMoney(plant.price) + "</span><button class=\"add-to-cart-btn\" data-plant-id=\"" + Number(plant.id || 0) + "\" type=\"button\">Add</button></div>" +
                            "</article>";
                    }).join("");

                    productsWrap.querySelectorAll(".add-to-cart-btn").forEach(function (btn) {
                        bindPlantSelectButton(btn, "views/cart.html");
                    });
                })
                .catch(function () {
                    productsWrap.innerHTML = "<p class=\"empty-state\">Could not load featured plants right now.</p>";
                });
        }

        const infoCards = document.querySelectorAll(".info-card p");
        infoCards.forEach(function (p) {
            p.style.cursor = "pointer";
            p.addEventListener("click", function () {
                window.location.href = "views/info.html?topic=service-info";
            });
        });

        const newsletterBtn = document.querySelector(".newsletter-row button");
        const newsletterInput = document.querySelector(".newsletter-row input");
        if (newsletterBtn && newsletterInput) {
            newsletterBtn.addEventListener("click", function () {
                const email = String(newsletterInput.value || "").trim();
                if (!email || email.indexOf("@") === -1) {
                    toast("Enter a valid email address.", true);
                    return;
                }
                toast("Thanks. Newsletter signup captured.");
                newsletterInput.value = "";
            });
        }

        updateCartCount();
        refreshCartState();
    }

    function bindLoginPage() {
        const form = document.getElementById("loginForm");
        if (!form) {
            return;
        }
        form.addEventListener("submit", function (e) {
            e.preventDefault();
            const email = document.getElementById("loginEmail").value.trim();
            const password = document.getElementById("loginPassword").value.trim();
            request("/login", { method: "POST", body: { email: email, password: password } })
                .then(function (res) {
                    if (res && String(res.status || "").toLowerCase() === "success") {
                        toast("Login successful");
                        window.setTimeout(function () { window.location.href = "../index.html"; }, 500);
                        return;
                    }
                    toast((res && res.message) || "Login failed", true);
                })
                .catch(function () {
                    toast("Could not connect to backend.", true);
                });
        });
    }

    function bindRegisterPage() {
        const form = document.getElementById("registerForm");
        if (!form) {
            return;
        }
        form.addEventListener("submit", function (e) {
            e.preventDefault();
            const payload = {
                fullName: document.getElementById("registerName").value.trim(),
                email: document.getElementById("registerEmail").value.trim(),
                password: document.getElementById("registerPassword").value.trim(),
                phone: document.getElementById("registerPhone").value.trim(),
                address: document.getElementById("registerAddress").value.trim()
            };
            request("/register", { method: "POST", body: payload })
                .then(function (res) {
                    if (res && String(res.status || "").toLowerCase() === "success") {
                        toast("Registration successful. You can now login.");
                        window.setTimeout(function () { window.location.href = "login.html"; }, 700);
                        return;
                    }
                    toast((res && res.message) || "Registration failed", true);
                })
                .catch(function () {
                    toast("Could not connect to backend.", true);
                });
        });
    }

    function renderPlants(plants) {
        const wrap = document.getElementById("catalogueProducts");
        if (!wrap) {
            return;
        }
        if (!plants.length) {
            wrap.innerHTML = "<p class=\"empty-state\">No plants found for this filter.</p>";
            return;
        }
        wrap.innerHTML = plants.map(function (plant) {
            const image = resolvePlantImageUrl(plant.imageUrl, plant.name);
            return "" +
                "<article class=\"product-card\">" +
                    "<img src=\"" + image + "\" alt=\"" + plant.name + "\" onerror=\"this.onerror=null;this.src='/assets/images/greenaura.webp';\">" +
                "<h4>" + plant.name + "</h4>" +
                "<p>" + (plant.description || "Fresh and healthy nursery plant.") + "</p>" +
                "<div class=\"price-row\"><span>" + formatMoney(plant.price) + "</span>" +
                "<button class=\"cat-add-btn\" data-plant-id=\"" + plant.id + "\" type=\"button\">Add</button></div>" +
                "</article>";
        }).join("");

        wrap.querySelectorAll(".cat-add-btn").forEach(function (btn) {
            bindPlantSelectButton(btn, "cart.html");
        });

        syncCartButtonVisuals();
    }

    function bindCataloguePage() {
        const container = document.getElementById("catalogueProducts");
        if (!container) {
            return;
        }

        renderLoadingState(container, "Loading catalogue...");
        
        let allPlants = [];
        const filterCategory = document.getElementById("filterCategory");
        const filterPrice = document.getElementById("filterPrice");
        const sortBy = document.getElementById("sortBy");

        const resolvePlantCategory = function (plant) {
            const direct = String(plant && plant.category ? plant.category : "").toLowerCase().trim();
            if (direct) {
                return direct;
            }

            const blob = (
                String(plant && plant.name ? plant.name : "") + " " +
                String(plant && plant.description ? plant.description : "")
            ).toLowerCase();

            if (/rose|lily|flower|floral|petal/.test(blob)) {
                return "flowers";
            }
            if (/mint|basil|herb|aromatic|tea|cooking/.test(blob)) {
                return "herbs";
            }
            if (/aloe|succulent|cactus|snake plant/.test(blob)) {
                return "succulents";
            }
            if (/tree|palm|bamboo|oak|maple|fruit/.test(blob)) {
                return "trees";
            }
            return "";
        };
        
        const applyFiltersAndSort = function() {
            let filtered = allPlants.slice();
            
            // Apply category filter
            if (filterCategory && filterCategory.value && filterCategory.value !== "") {
                const category = String(filterCategory.value).toLowerCase();
                filtered = filtered.filter(function(p) {
                    return resolvePlantCategory(p) === category;
                });
            }
            
            // Apply price filter
            if (filterPrice && filterPrice.value && filterPrice.value !== "") {
                const priceRange = filterPrice.value;
                filtered = filtered.filter(function(p) {
                    const price = parseFloat(p.price || 0);
                    if (priceRange === "0-50") return price <= 50;
                    if (priceRange === "50-100") return price > 50 && price <= 100;
                    if (priceRange === "100-200") return price > 100 && price <= 200;
                    if (priceRange === "200+") return price > 200;
                    return true;
                });
            }
            
            // Apply search filter from URL
            const params = new URLSearchParams(window.location.search);
            const q = String(params.get("q") || "").toLowerCase();
            if (q) {
                filtered = filtered.filter(function(p) {
                    return String(p.name || "").toLowerCase().indexOf(q) !== -1;
                });
            }
            
            // Apply sorting
            if (sortBy && sortBy.value && sortBy.value !== "featured") {
                const sortValue = sortBy.value;
                if (sortValue === "name-asc") {
                    filtered.sort(function(a, b) {
                        return String(a.name || "").localeCompare(String(b.name || ""));
                    });
                } else if (sortValue === "name-desc") {
                    filtered.sort(function(a, b) {
                        return String(b.name || "").localeCompare(String(a.name || ""));
                    });
                } else if (sortValue === "price-asc") {
                    filtered.sort(function(a, b) {
                        return parseFloat(a.price || 0) - parseFloat(b.price || 0);
                    });
                } else if (sortValue === "price-desc") {
                    filtered.sort(function(a, b) {
                        return parseFloat(b.price || 0) - parseFloat(a.price || 0);
                    });
                }
            }
            
            renderPlants(filtered);
        };
        
        request("/catalogue")
            .then(function (res) {
                allPlants = (res && res.data && res.data.plants) || [];
                applyFiltersAndSort();
                
                // Bind filter and sort change events
                if (filterCategory) {
                    filterCategory.addEventListener("change", applyFiltersAndSort);
                }
                if (filterPrice) {
                    filterPrice.addEventListener("change", applyFiltersAndSort);
                }
                if (sortBy) {
                    sortBy.addEventListener("change", applyFiltersAndSort);
                }
            })
            .catch(function () {
                container.innerHTML = "<p class=\"empty-state\"><i class=\"fas fa-leaf\"></i> Could not load catalogue from backend.</p>";
            });
    }

    function bindCartPage() {
        const list = document.getElementById("cartItems");
        if (!list) {
            return;
        }
        const isCartsPage = document.body.classList.contains("carts-page");
        const cartLiveRegion = document.getElementById("cartLiveRegion");
        const viewCartLink = document.getElementById("openCartView");
        const addSelectedBtn = document.getElementById("addSelectedToCart");
        const qtyInput = document.querySelector(".detail-qty-input");
        const applyCouponBtn = document.getElementById("applyCoupon");
        const couponInput = document.getElementById("couponCode");
        const updateCartBtn = document.getElementById("updateCartBtn");
        const shippingZoneInputs = document.querySelectorAll("input[name='shippingZone']");
        const qtyDown = document.querySelector(".detail-qty-btn[aria-label='Decrease quantity']");
        const qtyUp = document.querySelector(".detail-qty-btn[aria-label='Increase quantity']");
        const bouquetSizeSelect = document.getElementById("bouquetSize");
        const titleEl = document.querySelector(".detail-title");
        const descEl = document.querySelector(".detail-description");
        const priceEl = document.querySelector(".detail-price");
        const imagePlaceholder = document.querySelector(".detail-image-label");
        const detailImageEl = document.getElementById("detailPlantImage");
        const detailBloomMark = document.querySelector(".detail-bloom-mark");
        let cachedSubtotal = 0;
        let cartPlantIdsOnPage = new Set();
        const shouldRenderCartPanel = isCartsPage;
        const sizeOptionDefs = [
            { key: "classic", label: "Classic Charm", multiplier: 1 },
            { key: "premium", label: "Premium Bloom", multiplier: 1.2 },
            { key: "grand", label: "Grand Signature", multiplier: 1.4 }
        ];

        const getStoredSizePrefs = function () {
            try {
                const raw = window.localStorage ? window.localStorage.getItem("GREENAURA_SIZE_PREFS") : "";
                const parsed = raw ? JSON.parse(raw) : {};
                return parsed && typeof parsed === "object" ? parsed : {};
            } catch (e) {
                return {};
            }
        };

        const setStoredSizePrefs = function (prefs) {
            if (!window.localStorage) {
                return;
            }
            try {
                window.localStorage.setItem("GREENAURA_SIZE_PREFS", JSON.stringify(prefs || {}));
            } catch (e) {
                // Ignore storage failures and continue with default pricing.
            }
        };

        const getSizePrefForPlant = function (plantId) {
            const prefs = getStoredSizePrefs();
            const pref = prefs[String(plantId)] || {};
            const fallback = sizeOptionDefs[0];
            const matched = sizeOptionDefs.find(function (opt) {
                return opt.key === pref.key;
            }) || fallback;
            return {
                key: matched.key,
                label: matched.label,
                multiplier: Number(pref.multiplier || matched.multiplier) || matched.multiplier
            };
        };

        const saveSizePrefForPlant = function (plantId, option) {
            const id = Number(plantId || 0);
            if (!id || !option) {
                return;
            }
            const prefs = getStoredSizePrefs();
            prefs[String(id)] = {
                key: String(option.key || "classic"),
                label: String(option.label || "Classic Charm"),
                multiplier: Number(option.multiplier || 1) || 1
            };
            setStoredSizePrefs(prefs);
        };

        const getAdjustedPrice = function (basePrice, plantId) {
            const pref = getSizePrefForPlant(plantId);
            return Number(basePrice || 0) * Number(pref.multiplier || 1);
        };

        const refreshBouquetSizeOptions = function (basePrice, selectedPlantId) {
            if (!bouquetSizeSelect) {
                return;
            }
            const activePrice = Number(basePrice || 0);
            const plantId = Number(selectedPlantId || bouquetSizeSelect.getAttribute("data-plant-id") || 0);
            const saved = getSizePrefForPlant(plantId);
            bouquetSizeSelect.innerHTML = sizeOptionDefs.map(function (opt) {
                const computed = activePrice * opt.multiplier;
                const selectedAttr = opt.key === saved.key ? " selected" : "";
                return "<option value=\"" + opt.key + "\" data-multiplier=\"" + opt.multiplier + "\"" + selectedAttr + ">" +
                    opt.label + " + " + formatMoney(computed) + "</option>";
            }).join("");
            if (plantId) {
                bouquetSizeSelect.setAttribute("data-plant-id", String(plantId));
                saveSizePrefForPlant(plantId, saved);
            }
        };

        const updateDetailPriceFromSize = function (basePrice, plantId) {
            if (!priceEl) {
                return;
            }
            const base = Number(basePrice || 0);
            const id = Number(plantId || 0);
            const currentOption = bouquetSizeSelect && bouquetSizeSelect.selectedOptions && bouquetSizeSelect.selectedOptions[0]
                ? bouquetSizeSelect.selectedOptions[0]
                : null;
            const multiplier = currentOption ? Number(currentOption.getAttribute("data-multiplier") || 1) : getSizePrefForPlant(id).multiplier;
            priceEl.textContent = formatMoney(base * (Number(multiplier || 1) || 1));
        };

        const getSelectedShippingCost = function () {
            const selected = document.querySelector("input[name='shippingZone']:checked");
            return Number(selected ? selected.value : 0) || 0;
        };

        const persistSelectedShipping = function () {
            if (!window.localStorage) {
                return;
            }
            if (!shippingZoneInputs.length) {
                window.localStorage.removeItem("GREENAURA_SELECTED_SHIPPING");
                return;
            }
            const shipping = getSelectedShippingCost();
            if (shipping > 0) {
                window.localStorage.setItem("GREENAURA_SELECTED_SHIPPING", String(shipping));
                return;
            }
            window.localStorage.removeItem("GREENAURA_SELECTED_SHIPPING");
        };

        const updateTotals = function (subtotalValue, backendTotal) {
            const subtotalEl = document.getElementById("cartSubtotal");
            const shippingEl = document.getElementById("cartShipping");
            const totalEl = document.getElementById("cartTotal");

            if (isCartsPage) {
                const shipping = getSelectedShippingCost();
                const total = Math.max(0, Number(subtotalValue || 0) + shipping);
                if (subtotalEl) {
                    subtotalEl.textContent = formatMoney(subtotalValue);
                }
                if (shippingEl) {
                    shippingEl.textContent = formatMoney(shipping);
                }
                if (totalEl) {
                    totalEl.textContent = formatMoney(total);
                }
                return;
            }

            const shipping = 15;
            const subtotal = Math.max(0, Number(backendTotal || 0) - shipping);
            if (subtotalEl) {
                subtotalEl.textContent = formatMoney(subtotal);
            }
            if (shippingEl) {
                shippingEl.textContent = formatMoney(shipping);
            }
            if (totalEl) {
                totalEl.textContent = formatMoney(backendTotal || 0);
            }
        };

        if (shippingZoneInputs.length) {
            const persistedShipping = window.localStorage
                ? Number(window.localStorage.getItem("GREENAURA_SELECTED_SHIPPING") || 0)
                : 0;
            if (persistedShipping > 0) {
                shippingZoneInputs.forEach(function (input) {
                    input.checked = Number(input.value || 0) === persistedShipping;
                });
            }
            shippingZoneInputs.forEach(function (input) {
                input.addEventListener("change", function () {
                    persistSelectedShipping();
                    updateTotals(cachedSubtotal, cachedSubtotal + getSelectedShippingCost());
                });
            });
        }

        const expandCartView = function () {
            if (!cartLiveRegion) {
                return;
            }
            if (!shouldRenderCartPanel) {
                return;
            }
            cartLiveRegion.classList.remove("is-collapsed");
            cartLiveRegion.scrollIntoView({ behavior: "smooth", block: "start" });
        };

        if (viewCartLink) {
            viewCartLink.addEventListener("click", function (e) {
                e.preventDefault();
                expandCartView();
                loadCartItems();
            });
        }

        if (qtyDown && qtyInput) {
            qtyDown.addEventListener("click", function () {
                const current = Number(qtyInput.value || 1);
                qtyInput.value = String(Math.max(1, current - 1));
            });
        }

        if (qtyUp && qtyInput) {
            qtyUp.addEventListener("click", function () {
                const current = Number(qtyInput.value || 1);
                qtyInput.value = String(Math.max(1, current + 1));
            });
        }

        const loadSelectedPlant = function () {
            if (!addSelectedBtn) {
                return;
            }

            const params = new URLSearchParams(window.location.search);
            const selectedPlantId = Number(params.get("plantId") || 0);

            request("/catalogue")
                .then(function (res) {
                    const plants = (res && res.data && res.data.plants) || [];
                    if (!plants.length) {
                        return;
                    }

                    const selected = plants.find(function (p) {
                        return Number(p && p.id) === selectedPlantId;
                    }) || plants[0];

                    if (!selected) {
                        return;
                    }

                    addSelectedBtn.setAttribute("data-plant-id", String(Number(selected.id || 0)));
                    addSelectedBtn.setAttribute("data-base-price", String(Number(selected.price || 0)));

                    if (titleEl) {
                        titleEl.textContent = String(selected.name || "flower name").toLowerCase();
                    }
                    if (descEl) {
                        descEl.textContent = String(selected.description || "A beautiful arrangement for meaningful moments.");
                    }
                    refreshBouquetSizeOptions(Number(selected.price || 0), Number(selected.id || 0));
                    updateDetailPriceFromSize(Number(selected.price || 0), Number(selected.id || 0));
                    if (detailImageEl) {
                        detailImageEl.src = resolvePlantImageUrl(selected.imageUrl, selected.name);
                        detailImageEl.alt = String(selected.name || "Selected plant");
                        detailImageEl.onerror = function () {
                            detailImageEl.onerror = null;
                            detailImageEl.src = "/assets/images/greenaura.webp";
                        };
                        detailImageEl.hidden = false;
                    }
                    if (imagePlaceholder) {
                        imagePlaceholder.textContent = String(selected.name || "Flower image");
                        imagePlaceholder.hidden = !!detailImageEl;
                    }
                    if (detailBloomMark) {
                        detailBloomMark.hidden = !!detailImageEl;
                    }
                })
                .catch(function () {
                    // Keep default placeholder content when catalogue cannot be loaded.
                });
        };

        if (bouquetSizeSelect) {
            bouquetSizeSelect.addEventListener("change", function () {
                const plantId = Number(addSelectedBtn && addSelectedBtn.getAttribute("data-plant-id") || bouquetSizeSelect.getAttribute("data-plant-id") || 0);
                const basePrice = Number(addSelectedBtn && addSelectedBtn.getAttribute("data-base-price") || 0);
                const selectedOption = bouquetSizeSelect.selectedOptions && bouquetSizeSelect.selectedOptions[0]
                    ? bouquetSizeSelect.selectedOptions[0]
                    : null;
                const selectedDef = selectedOption
                    ? {
                        key: String(selectedOption.value || "classic"),
                        label: String(selectedOption.textContent || "Classic Charm").split("+")[0].trim(),
                        multiplier: Number(selectedOption.getAttribute("data-multiplier") || 1)
                    }
                    : sizeOptionDefs[0];
                saveSizePrefForPlant(plantId, selectedDef);
                updateDetailPriceFromSize(basePrice, plantId);
            });
        }

        const addSelectedQuantity = function (plantId, count) {
            let chain = Promise.resolve({ status: "success" });
            for (let i = 0; i < count; i += 1) {
                chain = chain.then(function (res) {
                    if (!res || String(res.status || "").toLowerCase() !== "success") {
                        return res;
                    }
                    return request("/addToCart", { method: "POST", body: { plantId: plantId } });
                });
            }
            return chain;
        };

        if (addSelectedBtn) {
            addSelectedBtn.addEventListener("click", function () {
                const plantId = Number(addSelectedBtn.getAttribute("data-plant-id") || 0);
                const quantity = Math.max(1, Number(qtyInput && qtyInput.value ? qtyInput.value : 1));

                if (!plantId) {
                    toast("Select a plant first.", true);
                    return;
                }

                if (cartPlantIdsOnPage.has(plantId)) {
                    toast("Added to cart");
                    return;
                }

                if (bouquetSizeSelect) {
                    const selectedOption = bouquetSizeSelect.selectedOptions && bouquetSizeSelect.selectedOptions[0]
                        ? bouquetSizeSelect.selectedOptions[0]
                        : null;
                    const selectedDef = selectedOption
                        ? {
                            key: String(selectedOption.value || "classic"),
                            label: String(selectedOption.textContent || "Classic Charm").split("+")[0].trim(),
                            multiplier: Number(selectedOption.getAttribute("data-multiplier") || 1)
                        }
                        : sizeOptionDefs[0];
                    saveSizePrefForPlant(plantId, selectedDef);
                }

                addSelectedBtn.disabled = true;
                addSelectedQuantity(plantId, quantity)
                    .then(function (res) {
                        if (res && String(res.status || "").toLowerCase() === "success") {
                            cartPlantIdsOnPage.add(plantId);
                            toast("Added to cart");
                            if (shouldRenderCartPanel) {
                                loadCartItems();
                            }
                            updateCartCount();
                            refreshCartState();
                            return;
                        }
                        toast((res && res.message) || "Please login to add items.", true);
                    })
                    .catch(function () {
                        toast("Could not connect to backend.", true);
                    })
                    .finally(function () {
                        addSelectedBtn.disabled = false;
                    });
            });
        }

        if (applyCouponBtn && couponInput) {
            applyCouponBtn.addEventListener("click", function () {
                const code = String(couponInput.value || "").trim();
                if (!code) {
                    toast("Please enter a coupon code.", true);
                    return;
                }
                toast("Coupon feature will be connected soon.");
            });
        }

        if (updateCartBtn) {
            updateCartBtn.addEventListener("click", function () {
                loadCartItems();
                toast("Cart updated");
            });
        }

        function loadCartItems() {
            if (shouldRenderCartPanel) {
                renderLoadingState(list, "Loading cart items...");
            }

            request("/cart")
                .then(function (res) {
                    if (!res || String(res.status || "").toLowerCase() !== "success") {
                        if (shouldRenderCartPanel) {
                            list.classList.add("is-empty");
                            list.innerHTML = "<div class=\"empty-state\"><i class=\"fas fa-lock\"></i><p>Please login to view cart.</p></div>";
                        }
                        cartPlantIdsOnPage = new Set();
                        return;
                    }
                    const items = (res.data && res.data.cartItems) || [];
                    cartPlantIdsOnPage = new Set(items
                        .map(function (item) { return item && item.plant ? Number(item.plant.id) : 0; })
                        .filter(function (id) { return id > 0; }));
                    const total = Number((res.data && res.data.total) || 0);
                    const subtotalFromItems = items.reduce(function (sum, item) {
                        const plant = item.plant || {};
                        const plantId = Number(plant.id || 0);
                        const price = Number(plant.price || 0);
                        const adjustedPrice = getAdjustedPrice(price, plantId);
                        const qty = Number(item.quantity || 0);
                        const rowSubtotal = Number(adjustedPrice * qty);
                        return sum + rowSubtotal;
                    }, 0);
                    cachedSubtotal = subtotalFromItems;

                    if (!shouldRenderCartPanel) {
                        updateTotals(cachedSubtotal, total);
                        return;
                    }

                    if (!items.length) {
                        list.classList.add("is-empty");
                        list.innerHTML = "<div class=\"empty-state\"><i class=\"fas fa-shopping-bag\"></i><p>Your cart is empty.</p></div>";
                    } else {
                        list.classList.remove("is-empty");
                        list.innerHTML = items.map(function (item) {
                            const plant = item.plant || {};
                            const name = plant.name || "Plant";
                            const price = Number(plant.price || 0);
                            const adjustedPrice = getAdjustedPrice(price, plant.id);
                            const sizePref = getSizePrefForPlant(plant.id);
                            const description = (plant.description || "A beautiful, hand-selected arrangement crafted for meaningful moments.").trim();
                            const qty = Number(item.quantity || 0);
                            const subtotal = adjustedPrice * qty;
                            const plantId = Number(plant.id || 0);
                            const image = resolvePlantImageUrl(plant.imageUrl, plant.name);
                            if (isCartsPage) {
                                return "<div class=\"cart-table-row\" data-plant-id=\"" + plantId + "\">" +
                                    "<div class=\"cart-col product\">" +
                                    "<button class=\"cart-item-remove\" type=\"button\" data-plant-id=\"" + plantId + "\" title=\"Remove from cart\"><i class=\"fas fa-trash-alt\"></i></button>" +
                                    "<img src=\"" + image + "\" alt=\"" + name + "\" onerror=\"this.onerror=null;this.src='/assets/images/greenaura.webp';\">" +
                                    "<div class=\"cart-product-meta\">" +
                                    "<h4>" + name + "</h4>" +
                                    "<p>Select your bouquet size: " + sizePref.label + "</p>" +
                                    "<small>" + description + "</small>" +
                                    "</div>" +
                                    "</div>" +
                                    "<div class=\"cart-col price\">" + formatMoney(adjustedPrice) + "</div>" +
                                    "<div class=\"cart-col qty\">" +
                                    "<div class=\"cart-qty-control\">" +
                                    "<button class=\"qty-btn decrement-qty\" type=\"button\" data-plant-id=\"" + plantId + "\">-</button>" +
                                    "<span class=\"cart-item-qty\">" + qty + "</span>" +
                                    "<button class=\"qty-btn increment-qty\" type=\"button\" data-plant-id=\"" + plantId + "\">+</button>" +
                                    "</div>" +
                                    "</div>" +
                                    "<div class=\"cart-col subtotal\">" + formatMoney(subtotal) + "</div>" +
                                    "</div>";
                            }
                            return "<div class=\"cart-item\" data-plant-id=\"" + plantId + "\">" +
                                "<div class=\"cart-item-details\">" +
                                "<div class=\"cart-item-name\">" + name + "</div>" +
                                "<div class=\"cart-item-description\">" + description + "</div>" +
                                "<div class=\"cart-item-price\">" + formatMoney(adjustedPrice) + " each (" + sizePref.label + ")</div>" +
                                "<div class=\"cart-item-controls\">" +
                                "<button class=\"qty-btn decrement-qty\" type=\"button\" data-plant-id=\"" + plantId + "\"><i class=\"fas fa-minus\"></i></button>" +
                                "<span class=\"cart-item-qty\">" + qty + "</span>" +
                                "<button class=\"qty-btn increment-qty\" type=\"button\" data-plant-id=\"" + plantId + "\"><i class=\"fas fa-plus\"></i></button>" +
                                "</div>" +
                                "<div class=\"cart-item-subtotal\">" + formatMoney(subtotal) + "</div>" +
                                "<button class=\"cart-item-remove\" type=\"button\" data-plant-id=\"" + plantId + "\" title=\"Remove from cart\"><i class=\"fas fa-trash-alt\"></i></button>" +
                                "</div>" +
                                "</div>";
                        }).join("");

                        document.querySelectorAll(".cart-item-remove").forEach(function (btn) {
                            btn.addEventListener("click", function () {
                                const plantId = Number(btn.getAttribute("data-plant-id"));
                                if (confirm("Remove this item from your cart?")) {
                                    request("/removeFromCart", { method: "POST", body: { plantId: plantId } })
                                        .then(function (response) {
                                            if (response && String(response.status || "").toLowerCase() === "success") {
                                                toast("Item removed from cart");
                                                loadCartItems();
                                                updateCartCount();
                                                refreshCartState();
                                            } else {
                                                toast("Failed to remove item", true);
                                            }
                                        })
                                        .catch(function () {
                                            toast("Error removing item", true);
                                        });
                                }
                            });
                        });

                        document.querySelectorAll(".increment-qty").forEach(function (btn) {
                            btn.addEventListener("click", function () {
                                const plantId = Number(btn.getAttribute("data-plant-id"));
                                request("/addToCart", { method: "POST", body: { plantId: plantId } })
                                    .then(function (response) {
                                        if (response && String(response.status || "").toLowerCase() === "success") {
                                            loadCartItems();
                                            updateCartCount();
                                        } else {
                                            toast("Failed to update cart", true);
                                        }
                                    })
                                    .catch(function () {
                                        toast("Error updating cart", true);
                                    });
                            });
                        });

                        document.querySelectorAll(".decrement-qty").forEach(function (btn) {
                            btn.addEventListener("click", function () {
                                const plantId = Number(btn.getAttribute("data-plant-id"));
                                request("/removeFromCart", { method: "POST", body: { plantId: plantId } })
                                    .then(function (response) {
                                        if (response && String(response.status || "").toLowerCase() === "success") {
                                            loadCartItems();
                                            updateCartCount();
                                            refreshCartState();
                                        } else {
                                            toast("Failed to update cart", true);
                                        }
                                    })
                                    .catch(function () {
                                        toast("Error updating cart", true);
                                    });
                            });
                        });
                    }
                    updateTotals(cachedSubtotal, total);
                })
                .catch(function () {
                    if (shouldRenderCartPanel) {
                        list.classList.add("is-empty");
                        list.innerHTML = "<div class=\"empty-state\"><i class=\"fas fa-exclamation-circle\"></i><p>Could not load cart from backend.</p></div>";
                    }
                    cachedSubtotal = 0;
                    cartPlantIdsOnPage = new Set();
                    updateTotals(0, 0);
                });
        }

        loadSelectedPlant();
        loadCartItems();

        const checkoutBtn = document.getElementById("goCheckout");
        if (checkoutBtn) {
            checkoutBtn.addEventListener("click", function () {
                persistSelectedShipping();
                window.location.href = "checkout.html";
            });
        }
    }

    function bindCheckoutPage() {
        const list = document.getElementById("checkoutItems");
        if (!list) {
            return;
        }
        const subtotalEl = document.getElementById("checkoutSubtotal");
        const shippingEl = document.getElementById("checkoutShipping");
        const totalEl = document.getElementById("checkoutTotal");
        const shippingInputs = document.querySelectorAll("input[name='checkoutShippingMethod']");
        const deliveryForm = document.getElementById("deliveryForm");
        let cachedSubtotal = 0;

        const getStoredSizePrefs = function () {
            try {
                const raw = window.localStorage ? window.localStorage.getItem("GREENAURA_SIZE_PREFS") : "";
                const parsed = raw ? JSON.parse(raw) : {};
                return parsed && typeof parsed === "object" ? parsed : {};
            } catch (e) {
                return {};
            }
        };

        const getAdjustedPriceForPlant = function (basePrice, plantId) {
            const prefs = getStoredSizePrefs();
            const pref = prefs[String(Number(plantId || 0))] || {};
            const multiplier = Number(pref.multiplier || 1) || 1;
            return Number(basePrice || 0) * multiplier;
        };

        const getSelectedShipping = function () {
            const selected = document.querySelector("input[name='checkoutShippingMethod']:checked");
            return Number(selected ? selected.value : 15) || 15;
        };

        const renderTotals = function () {
            const shipping = getSelectedShipping();
            const total = Math.max(0, Number(cachedSubtotal || 0) + shipping);
            if (subtotalEl) {
                subtotalEl.textContent = formatMoney(cachedSubtotal);
            }
            if (shippingEl) {
                shippingEl.textContent = formatMoney(shipping);
            }
            if (totalEl) {
                totalEl.textContent = formatMoney(total);
            }
        };

        if (shippingInputs.length) {
            const persistedShipping = window.localStorage
                ? Number(window.localStorage.getItem("GREENAURA_SELECTED_SHIPPING") || 0)
                : 0;
            if (persistedShipping > 0) {
                const matched = Array.prototype.find.call(shippingInputs, function (input) {
                    return Number(input.value || 0) === persistedShipping;
                });
                if (matched) {
                    matched.checked = true;
                }
            }
            shippingInputs.forEach(function (input) {
                input.addEventListener("change", function () {
                    if (window.localStorage) {
                        window.localStorage.setItem("GREENAURA_SELECTED_SHIPPING", String(Number(input.value || 0) || 0));
                    }
                    renderTotals();
                });
            });
        }

        renderLoadingState(list, "Loading checkout summary...");
        request("/checkout")
            .then(function (res) {
                if (!res || String(res.status || "").toLowerCase() !== "success") {
                    list.innerHTML = "<div class=\"empty-state\"><i class=\"fas fa-exclamation-triangle\"></i><p>Checkout is unavailable. Please login and add items first.</p></div>";
                    cachedSubtotal = 0;
                    renderTotals();
                    return;
                }
                const items = (res.data && res.data.cartItems) || [];
                if (!items.length) {
                    list.innerHTML = "<div class=\"empty-state\"><i class=\"fas fa-shopping-bag\"></i><p>Your cart is empty. Add items to proceed with checkout.</p></div>";
                    cachedSubtotal = 0;
                } else {
                    cachedSubtotal = items.reduce(function (sum, item) {
                        const plant = item.plant || {};
                        const plantId = Number(plant.id || 0);
                        const price = getAdjustedPriceForPlant(Number(plant.price || 0), plantId);
                        const qty = Number(item.quantity || 0);
                        const subtotal = Number(price * qty);
                        return sum + subtotal;
                    }, 0);
                    list.innerHTML = items.map(function (item) {
                        const plant = item.plant || {};
                        const name = plant.name || "Plant";
                        const plantId = Number(plant.id || 0);
                        const price = getAdjustedPriceForPlant(Number(plant.price || 0), plantId);
                        const qty = Number(item.quantity || 0);
                        const subtotal = price * qty;
                        return "<div class=\"checkout-order-item\">" +
                            "<div class=\"checkout-order-meta\">" +
                            "<h4>" + name + "</h4>" +
                            "<p>" + formatMoney(price) + " each</p>" +
                            "</div>" +
                            "<div class=\"checkout-order-qty\">x" + qty + "</div>" +
                            "<div class=\"checkout-order-subtotal\">" + formatMoney(subtotal) + "</div>" +
                            "</div>";
                    }).join("");
                }
                renderTotals();
            })
            .catch(function () {
                list.innerHTML = "<div class=\"empty-state\"><i class=\"fas fa-exclamation-circle\"></i><p>Could not load checkout from backend.</p></div>";
                cachedSubtotal = 0;
                renderTotals();
            });

        const placeOrderBtn = document.getElementById("placeOrder");
        if (placeOrderBtn) {
            placeOrderBtn.addEventListener("click", function () {
                if (deliveryForm && !deliveryForm.checkValidity()) {
                    deliveryForm.reportValidity();
                    return;
                }
                request("/checkout", { method: "POST" })
                    .then(function (res) {
                        if (res && String(res.status || "").toLowerCase() === "success") {
                            window.location.href = "confirmation.html?msg=" + encodeURIComponent((res.data && res.data.message) || "Order placed successfully");
                            return;
                        }
                        toast((res && res.message) || "Order placement failed", true);
                    })
                    .catch(function () {
                        toast("Could not place order right now.", true);
                    });
            });
        }
    }

    function bindConfirmationPage() {
        const msgEl = document.getElementById("confirmationMessage");
        const ordersTotalCountEl = document.getElementById("ordersTotalCount");
        const ordersReceivedCountEl = document.getElementById("ordersReceivedCount");
        const ordersPendingCountEl = document.getElementById("ordersPendingCount");
        const ordersCancelledCountEl = document.getElementById("ordersCancelledCount");
        const orderHistoryListEl = document.getElementById("orderHistoryList");

        if (!msgEl && !orderHistoryListEl) {
            return;
        }

        const formatOrderDate = function (value) {
            const d = value ? new Date(value) : null;
            if (!d || Number.isNaN(d.getTime())) {
                return "Date unavailable";
            }
            return d.toLocaleString();
        };

        const statusClass = function (status) {
            const s = String(status || "PENDING").toLowerCase();
            if (s === "cancelled") {
                return "cancelled";
            }
            if (s === "received") {
                return "received";
            }
            return "pending";
        };

        const renderOrders = function (data) {
            const orders = (data && data.orders) || [];

            if (ordersTotalCountEl) {
                ordersTotalCountEl.textContent = String(Number(data && data.totalOrders || 0));
            }
            if (ordersReceivedCountEl) {
                ordersReceivedCountEl.textContent = String(Number(data && data.receivedOrders || 0));
            }
            if (ordersPendingCountEl) {
                ordersPendingCountEl.textContent = String(Number(data && data.pendingOrders || 0));
            }
            if (ordersCancelledCountEl) {
                ordersCancelledCountEl.textContent = String(Number(data && data.cancelledOrders || 0));
            }

            if (!orderHistoryListEl) {
                return;
            }

            if (!orders.length) {
                orderHistoryListEl.innerHTML = "<p class=\"order-history-empty\">No orders yet.</p>";
                return;
            }

            orderHistoryListEl.innerHTML = orders.map(function (order) {
                const id = Number(order && order.id || 0);
                const status = String(order && order.status || "PENDING").toUpperCase();
                const cancellable = !!(order && order.cancellable);
                const itemCount = Number(order && order.itemCount || 0);
                return "<article class=\"order-row\" data-order-id=\"" + id + "\">" +
                    "<div class=\"order-row-meta\">" +
                    "<strong>Order #" + id + "</strong>" +
                    "<small>" + itemCount + " item(s) • " + formatOrderDate(order.orderDate) + "</small>" +
                    "</div>" +
                    "<span class=\"order-status-badge " + statusClass(status) + "\">" + status + "</span>" +
                    "<span class=\"order-total\">" + formatMoney(order.totalPrice) + "</span>" +
                    "<button type=\"button\" class=\"cancel-order-btn\" data-order-id=\"" + id + "\" " + (cancellable ? "" : "disabled") + ">Cancel</button>" +
                    "</article>";
            }).join("");

            orderHistoryListEl.querySelectorAll(".cancel-order-btn[data-order-id]").forEach(function (btn) {
                if (btn.hasAttribute("disabled")) {
                    return;
                }
                btn.addEventListener("click", function () {
                    const orderId = Number(btn.getAttribute("data-order-id") || 0);
                    if (!orderId) {
                        return;
                    }
                    if (!confirm("Cancel this order? This is only allowed within 30 minutes while pending.")) {
                        return;
                    }
                    btn.disabled = true;
                    request("/orders/" + orderId + "/cancel", { method: "POST" })
                        .then(function (res) {
                            if (!res || String(res.status || "").toLowerCase() !== "success") {
                                toast((res && res.message) || "Order could not be cancelled.", true);
                                btn.disabled = false;
                                return;
                            }
                            toast("Order cancelled successfully.");
                            renderOrders(res.data || {});
                        })
                        .catch(function () {
                            toast("Could not cancel order right now.", true);
                            btn.disabled = false;
                        });
                });
            });
        };

        const params = new URLSearchParams(window.location.search);
        const msg = params.get("msg");
        if (msg) {
            msgEl.textContent = msg;
        } else {
            msgEl.textContent = "Loading confirmation details...";
            request("/confirmation").then(function (res) {
                const serverMsg = res && res.data && res.data.message;
                msgEl.textContent = serverMsg || "Your order has been confirmed.";
            }).catch(function () {
                msgEl.textContent = "Your order has been confirmed.";
            });
        }

        if (orderHistoryListEl) {
            renderLoadingState(orderHistoryListEl, "Loading your orders...");
        }

        request("/orders").then(function (res) {
            if (!res || String(res.status || "").toLowerCase() !== "success") {
                renderOrders({});
                return;
            }
            renderOrders(res.data || {});
        }).catch(function () {
            renderOrders({});
        });
    }

    function bindInfoPage() {
        const title = document.getElementById("infoTitle");
        const lead = document.getElementById("infoLead");
        const pill = document.getElementById("infoTopicPill");
        const details = document.getElementById("infoDetails");
        if (!title || !lead || !pill || !details) {
            return;
        }

        const topic = String(new URLSearchParams(window.location.search).get("topic") || "information").toLowerCase();
        const normalized = topic.replace(/\s+/g, "-");

        const pages = {
            "contact": {
                pill: "Contact",
                title: "Contact Green Aura Nursery",
                lead: "Our team is ready to help with orders, delivery updates, corporate requests, and plant-care questions.",
                html: "" +
                    "<article class=\"info-panel\">" +
                    "<h2>Customer Support</h2>" +
                    "<ul class=\"info-list\">" +
                    "<li><i class=\"fas fa-phone\" aria-hidden=\"true\"></i><span><strong>Phone:</strong> +233 (0) 24 000 0000</span></li>" +
                    "<li><i class=\"fas fa-envelope\" aria-hidden=\"true\"></i><span><strong>Email:</strong> hello@greenaura.com</span></li>" +
                    "<li><i class=\"fas fa-clock\" aria-hidden=\"true\"></i><span><strong>Hours:</strong> Mon - Sat, 8:00 AM - 6:00 PM</span></li>" +
                    "</ul>" +
                    "</article>" +
                    "<article class=\"info-panel\">" +
                    "<h2>Visit Our Nursery</h2>" +
                    "<ul class=\"info-list\">" +
                    "<li><i class=\"fas fa-location-dot\" aria-hidden=\"true\"></i><span><strong>Address:</strong> 18 Garden Avenue, East Legon, Accra</span></li>" +
                    "<li><i class=\"fas fa-truck\" aria-hidden=\"true\"></i><span><strong>Delivery:</strong> Same-day dispatch available in selected zones</span></li>" +
                    "<li><i class=\"fas fa-seedling\" aria-hidden=\"true\"></i><span><strong>Care Desk:</strong> Get plant-care guidance before and after purchase</span></li>" +
                    "</ul>" +
                    "</article>" +
                    "<article class=\"info-panel wide\">" +
                    "<h2>How Can We Help?</h2>" +
                    "<p>Need help with a current order, special request, or event arrangement? Reach out by phone or email and our team will respond quickly with the next best option.</p>" +
                    "</article>"
            },
            "about": {
                pill: "About Us",
                title: "About Green Aura Nursery",
                lead: "We combine thoughtful design, healthy plants, and dependable delivery to make every gift and every space feel alive.",
                html: "" +
                    "<article class=\"info-panel wide\">" +
                    "<h2>Who We Are</h2>" +
                    "<p>Green Aura Nursery is a Ghana-based floral and plant brand focused on premium quality, meaningful gifting, and practical plant solutions for homes and businesses.</p>" +
                    "</article>" +
                    "<article class=\"info-panel\">" +
                    "<h2>Our Mission</h2>" +
                    "<ul class=\"info-list\">" +
                    "<li><i class=\"fas fa-heart\" aria-hidden=\"true\"></i><span>Create joyful moments through intentional plant gifting</span></li>" +
                    "<li><i class=\"fas fa-leaf\" aria-hidden=\"true\"></i><span>Promote healthy green spaces for homes and offices</span></li>" +
                    "<li><i class=\"fas fa-handshake\" aria-hidden=\"true\"></i><span>Deliver reliable service with a personal touch</span></li>" +
                    "</ul>" +
                    "</article>" +
                    "<article class=\"info-panel\">" +
                    "<h2>Why Customers Choose Us</h2>" +
                    "<ul class=\"info-list\">" +
                    "<li><i class=\"fas fa-check-circle\" aria-hidden=\"true\"></i><span>Fresh, hand-selected stock</span></li>" +
                    "<li><i class=\"fas fa-check-circle\" aria-hidden=\"true\"></i><span>Carefully packed products and fast dispatch</span></li>" +
                    "<li><i class=\"fas fa-check-circle\" aria-hidden=\"true\"></i><span>Support before and after delivery</span></li>" +
                    "</ul>" +
                    "</article>"
            }
        };

        const data = pages[normalized] || {
            pill: "Information",
            title: "Information",
            lead: "Browse key updates and useful details about Green Aura Nursery.",
            html: "<article class=\"info-panel wide\"><h2>Information</h2><p>Select Contact or About Us from the top menu to view dedicated page details.</p></article>"
        };

        document.title = data.title + " - Green Aura Nursery";
        pill.textContent = data.pill;
        title.textContent = data.title;
        lead.textContent = data.lead;
        details.innerHTML = data.html;
    }

    function bindMyAccountPage() {
        const nameEl = document.getElementById("accountName");
        const emailEl = document.getElementById("accountEmail");
        const phoneEl = document.getElementById("accountPhone");
        const addressEl = document.getElementById("accountAddress");
        const statusEl = document.getElementById("accountStatus");
        const joinedEl = document.getElementById("accountJoined");
        const totalOrdersEl = document.getElementById("accountTotalOrders");
        const pendingOrdersEl = document.getElementById("accountPendingOrders");
        const receivedOrdersEl = document.getElementById("accountReceivedOrders");
        const cancelledOrdersEl = document.getElementById("accountCancelledOrders");
        const summaryEl = document.getElementById("accountSummary");

        if (!nameEl || !emailEl || !summaryEl) {
            return;
        }

        renderLoadingState(summaryEl, "Loading account summary...");

        const setProfile = function (user) {
            const fullName = String(user && user.fullName ? user.fullName : "Account User");
            const email = String(user && user.email ? user.email : "Not available");
            const phone = String(user && user.phone ? user.phone : "Not provided");
            const address = String(user && user.address ? user.address : "Not provided");
            const createdAt = user && user.createdAt ? new Date(user.createdAt) : null;

            nameEl.textContent = fullName;
            emailEl.textContent = email;
            phoneEl.textContent = phone;
            addressEl.textContent = address;

            if (statusEl) {
                statusEl.textContent = "Active";
            }

            if (joinedEl) {
                joinedEl.textContent = createdAt && !Number.isNaN(createdAt.getTime())
                    ? createdAt.toLocaleDateString()
                    : "Member";
            }
        };

        const showLoginFallback = function () {
            nameEl.textContent = "Guest";
            emailEl.textContent = "Please log in";
            if (phoneEl) {
                phoneEl.textContent = "-";
            }
            if (addressEl) {
                addressEl.textContent = "-";
            }
            if (statusEl) {
                statusEl.textContent = "Not Logged In";
            }
            if (joinedEl) {
                joinedEl.textContent = "-";
            }
            if (totalOrdersEl) {
                totalOrdersEl.textContent = "0";
            }
            if (pendingOrdersEl) {
                pendingOrdersEl.textContent = "0";
            }
            if (receivedOrdersEl) {
                receivedOrdersEl.textContent = "0";
            }
            if (cancelledOrdersEl) {
                cancelledOrdersEl.textContent = "0";
            }
            summaryEl.innerHTML = "<p class=\"order-history-empty\">You are currently not logged in. <a href=\"login.html\">Login to view your account details.</a></p>";
        };

        const renderOrderSummary = function (data) {
            const total = Number(data && data.totalOrders || 0);
            const pending = Number(data && data.pendingOrders || 0);
            const received = Number(data && data.receivedOrders || 0);
            const cancelled = Number(data && data.cancelledOrders || 0);

            if (totalOrdersEl) {
                totalOrdersEl.textContent = String(total);
            }
            if (pendingOrdersEl) {
                pendingOrdersEl.textContent = String(pending);
            }
            if (receivedOrdersEl) {
                receivedOrdersEl.textContent = String(received);
            }
            if (cancelledOrdersEl) {
                cancelledOrdersEl.textContent = String(cancelled);
            }

            summaryEl.innerHTML = "" +
                "<p><strong>Account Overview:</strong> You have <strong>" + total + "</strong> order(s) in your history.</p>" +
                "<p>Pending: <strong>" + pending + "</strong> | Received: <strong>" + received + "</strong> | Cancelled: <strong>" + cancelled + "</strong></p>";
        };

        request("/cart")
            .then(function (res) {
                if (!res || String(res.status || "").toLowerCase() !== "success") {
                    showLoginFallback();
                    return;
                }

                const user = (res.data && (res.data.loggedInUser || res.data.user)) || null;
                if (user) {
                    setProfile(user);
                } else {
                    nameEl.textContent = "Account User";
                    emailEl.textContent = "Logged in";
                    if (phoneEl) {
                        phoneEl.textContent = "Not provided";
                    }
                    if (addressEl) {
                        addressEl.textContent = "Not provided";
                    }
                }

                return request("/orders")
                    .then(function (ordersRes) {
                        if (!ordersRes || String(ordersRes.status || "").toLowerCase() !== "success") {
                            renderOrderSummary({});
                            return;
                        }
                        renderOrderSummary(ordersRes.data || {});
                    })
                    .catch(function () {
                        renderOrderSummary({});
                    });
            })
            .catch(function () {
                showLoginFallback();
            });
    }

    function bindStaffOrdersPage() {
        const listEl = document.getElementById("staffOrderHistoryList");
        if (!listEl) {
            return;
        }

        renderLoadingState(listEl, "Loading staff orders...");

        const totalEl = document.getElementById("staffOrdersTotalCount");
        const receivedEl = document.getElementById("staffOrdersReceivedCount");
        const pendingEl = document.getElementById("staffOrdersPendingCount");
        const cancelledEl = document.getElementById("staffOrdersCancelledCount");

        const formatOrderDate = function (value) {
            const d = value ? new Date(value) : null;
            if (!d || Number.isNaN(d.getTime())) {
                return "Date unavailable";
            }
            return d.toLocaleString();
        };

        const statusClass = function (status) {
            const s = String(status || "PENDING").toLowerCase();
            if (s === "cancelled") {
                return "cancelled";
            }
            if (s === "received") {
                return "received";
            }
            return "pending";
        };

        const renderOrders = function (data) {
            const orders = (data && data.orders) || [];
            if (totalEl) {
                totalEl.textContent = String(Number(data && data.totalOrders || 0));
            }
            if (receivedEl) {
                receivedEl.textContent = String(Number(data && data.receivedOrders || 0));
            }
            if (pendingEl) {
                pendingEl.textContent = String(Number(data && data.pendingOrders || 0));
            }
            if (cancelledEl) {
                cancelledEl.textContent = String(Number(data && data.cancelledOrders || 0));
            }

            if (!orders.length) {
                listEl.innerHTML = "<p class=\"order-history-empty\">No orders available.</p>";
                return;
            }

            listEl.innerHTML = orders.map(function (order) {
                const id = Number(order && order.id || 0);
                const status = String(order && order.status || "PENDING").toUpperCase();
                const canMarkReceived = status === "PENDING";
                const itemCount = Number(order && order.itemCount || 0);
                return "<article class=\"order-row\">" +
                    "<div class=\"order-row-meta\">" +
                    "<strong>Order #" + id + "</strong>" +
                    "<small>" + itemCount + " item(s) • " + formatOrderDate(order.orderDate) + "</small>" +
                    "</div>" +
                    "<span class=\"order-status-badge " + statusClass(status) + "\">" + status + "</span>" +
                    "<span class=\"order-total\">" + formatMoney(order.totalPrice) + "</span>" +
                    "<button type=\"button\" class=\"staff-mark-received-btn\" data-order-id=\"" + id + "\" " + (canMarkReceived ? "" : "disabled") + ">Mark Received</button>" +
                    "</article>";
            }).join("");

            listEl.querySelectorAll(".staff-mark-received-btn[data-order-id]").forEach(function (btn) {
                if (btn.hasAttribute("disabled")) {
                    return;
                }
                btn.addEventListener("click", function () {
                    const orderId = Number(btn.getAttribute("data-order-id") || 0);
                    if (!orderId) {
                        return;
                    }
                    btn.disabled = true;
                    request("/staff/orders/" + orderId + "/received", { method: "POST" })
                        .then(function (res) {
                            if (!res || String(res.status || "").toLowerCase() !== "success") {
                                toast((res && res.message) || "Could not mark order as received.", true);
                                btn.disabled = false;
                                return;
                            }
                            toast("Order marked as received.");
                            renderOrders(res.data || {});
                        })
                        .catch(function () {
                            toast("Could not connect to staff endpoint.", true);
                            btn.disabled = false;
                        });
                });
            });
        };

        request("/staff/orders")
            .then(function (res) {
                if (!res || String(res.status || "").toLowerCase() !== "success") {
                    listEl.innerHTML = "<p class=\"order-history-empty\">Staff access required. Login with a staff account.</p>";
                    toast((res && res.message) || "Staff access required", true);
                    return;
                }
                renderOrders(res.data || {});
            })
            .catch(function () {
                listEl.innerHTML = "<p class=\"order-history-empty\">Could not load staff orders right now.</p>";
            });
    }

    function isAuthenticatedResponse(res) {
        return !!(res && String(res.status || "").toLowerCase() === "success");
    }

    function bindRegisterPromptModal() {
        const modal = document.getElementById("registerPromptModal");
        if (!modal) {
            return;
        }

        const storageKey = "ga_register_prompt_seen_v1";
        if (window.localStorage.getItem(storageKey) === "1") {
            return;
        }

        const closeBtn = document.getElementById("registerPromptClose");
        const laterBtn = document.getElementById("registerPromptLater");
        const registerAction = document.getElementById("registerPromptAction");

        const closeModal = function (remember) {
            modal.classList.remove("show");
            modal.setAttribute("aria-hidden", "true");
            if (remember) {
                window.localStorage.setItem(storageKey, "1");
            }
        };

        if (closeBtn) {
            closeBtn.addEventListener("click", function () {
                closeModal(true);
            });
        }

        if (laterBtn) {
            laterBtn.addEventListener("click", function () {
                closeModal(true);
            });
        }

        if (registerAction) {
            registerAction.addEventListener("click", function () {
                window.localStorage.setItem(storageKey, "1");
            });
        }

        modal.addEventListener("click", function (e) {
            if (e.target === modal) {
                closeModal(true);
            }
        });

        document.addEventListener("keydown", function (e) {
            if (e.key === "Escape" && modal.classList.contains("show")) {
                closeModal(true);
            }
        });

        request("/cart")
            .then(function (res) {
                if (isAuthenticatedResponse(res)) {
                    window.localStorage.setItem(storageKey, "1");
                    return;
                }
                window.setTimeout(function () {
                    modal.classList.add("show");
                    modal.setAttribute("aria-hidden", "false");
                }, 550);
            })
            .catch(function () {
                window.setTimeout(function () {
                    modal.classList.add("show");
                    modal.setAttribute("aria-hidden", "false");
                }, 550);
            });
    }

    document.addEventListener("DOMContentLoaded", function () {
        normalizeRegisterLinks();
        bindAuthIndicator();
        bindHomepageRegisterGuards();
        bindHomepageActions();
        bindRegisterPromptModal();
        bindLoginPage();
        bindRegisterPage();
        bindCataloguePage();
        bindCartPage();
        bindCheckoutPage();
        bindConfirmationPage();
        bindStaffOrdersPage();
        bindInfoPage();
        bindMyAccountPage();
    });
})();
