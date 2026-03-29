(function () {
    const API_BASE = window.location.protocol + "//" + window.location.hostname + ":8080/GreenAuraNursery/api";

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

    function bindAuthIndicator() {
        const accountLink = document.querySelector(".account-link");
        if (!accountLink) {
            return;
        }

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
            accountLink.setAttribute("href", "views/cart.html");
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
                "<a href=\"views/cart.html\">My account</a>" +
                "<button id=\"accountLogoutBtn\" type=\"button\">Logout</button>";

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
        const cartButton = document.getElementById("cartButton");
        if (cartButton) {
            cartButton.addEventListener("click", function () {
                window.location.href = "views/cart.html";
            });
        }

        const searchInput = document.getElementById("searchInput");
        const searchButton = document.getElementById("searchButton");
        if (searchInput && searchButton) {
            const runSearch = function () {
                const q = (searchInput.value || "").trim();
                const target = "views/catalogue.html" + (q ? ("?q=" + encodeURIComponent(q)) : "");
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

        const addButtons = document.querySelectorAll(".add-to-cart-btn");
        addButtons.forEach(function (btn) {
            btn.addEventListener("click", function () {
                const plantId = Number(btn.getAttribute("data-plant-id"));
                if (!plantId) {
                    toast("Unable to add this plant right now.", true);
                    return;
                }
                btn.disabled = true;
                request("/addToCart", { method: "POST", body: { plantId: plantId } })
                    .then(function (res) {
                        if (res && String(res.status || "").toLowerCase() === "success") {
                            toast((res.data && res.data.message) || "Added to cart");
                            updateCartCount();
                            return;
                        }
                        const msg = (res && res.message) || "Please login to add to cart.";
                        toast(msg, true);
                        if (String(msg).toLowerCase().indexOf("not authenticated") !== -1) {
                            window.setTimeout(function () { window.location.href = "views/login.html"; }, 600);
                        }
                    })
                    .catch(function () {
                        toast("Could not reach the backend server.", true);
                    })
                    .finally(function () {
                        btn.disabled = false;
                    });
            });
        });

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
            const image = plant.imageUrl || "../assets/images/greenaura.webp";
            return "" +
                "<article class=\"product-card\">" +
                "<img src=\"" + image + "\" alt=\"" + plant.name + "\">" +
                "<h4>" + plant.name + "</h4>" +
                "<p>" + (plant.description || "Fresh and healthy nursery plant.") + "</p>" +
                "<div class=\"price-row\"><span>" + formatMoney(plant.price) + "</span>" +
                "<button class=\"cat-add-btn\" data-plant-id=\"" + plant.id + "\" type=\"button\">Add</button></div>" +
                "</article>";
        }).join("");

        wrap.querySelectorAll(".cat-add-btn").forEach(function (btn) {
            btn.addEventListener("click", function () {
                const plantId = Number(btn.getAttribute("data-plant-id"));
                request("/addToCart", { method: "POST", body: { plantId: plantId } })
                    .then(function (res) {
                        if (res && String(res.status || "").toLowerCase() === "success") {
                            toast("Added to cart");
                            return;
                        }
                        toast((res && res.message) || "Please login first.", true);
                    })
                    .catch(function () {
                        toast("Could not connect to backend.", true);
                    });
            });
        });
    }

    function bindCataloguePage() {
        const container = document.getElementById("catalogueProducts");
        if (!container) {
            return;
        }
        request("/catalogue")
            .then(function (res) {
                const allPlants = (res && res.data && res.data.plants) || [];
                const params = new URLSearchParams(window.location.search);
                const q = String(params.get("q") || "").toLowerCase();
                const filtered = q ? allPlants.filter(function (p) {
                    return String(p.name || "").toLowerCase().indexOf(q) !== -1;
                }) : allPlants;
                renderPlants(filtered);
            })
            .catch(function () {
                container.innerHTML = "<p class=\"empty-state\">Could not load catalogue from backend.</p>";
            });
    }

    function bindCartPage() {
        const list = document.getElementById("cartItems");
        if (!list) {
            return;
        }
        request("/cart")
            .then(function (res) {
                if (!res || String(res.status || "").toLowerCase() !== "success") {
                    list.innerHTML = "<p class=\"empty-state\">Please login to view cart.</p>";
                    return;
                }
                const items = (res.data && res.data.cartItems) || [];
                const total = (res.data && res.data.total) || 0;
                if (!items.length) {
                    list.innerHTML = "<p class=\"empty-state\">Your cart is empty.</p>";
                } else {
                    list.innerHTML = items.map(function (item) {
                        const name = item.plant ? item.plant.name : "Plant";
                        const qty = Number(item.quantity || 0);
                        const subtotal = item.subtotal || ((item.plant ? Number(item.plant.price || 0) : 0) * qty);
                        return "<div class=\"line-item\"><span>" + name + " x " + qty + "</span><strong>" + formatMoney(subtotal) + "</strong></div>";
                    }).join("");
                }
                const totalEl = document.getElementById("cartTotal");
                if (totalEl) {
                    totalEl.textContent = formatMoney(total);
                }
            })
            .catch(function () {
                list.innerHTML = "<p class=\"empty-state\">Could not load cart from backend.</p>";
            });

        const checkoutBtn = document.getElementById("goCheckout");
        if (checkoutBtn) {
            checkoutBtn.addEventListener("click", function () {
                window.location.href = "checkout.html";
            });
        }
    }

    function bindCheckoutPage() {
        const list = document.getElementById("checkoutItems");
        if (!list) {
            return;
        }
        request("/checkout")
            .then(function (res) {
                if (!res || String(res.status || "").toLowerCase() !== "success") {
                    list.innerHTML = "<p class=\"empty-state\">Checkout is unavailable. Please login and add items first.</p>";
                    return;
                }
                const items = (res.data && res.data.cartItems) || [];
                const total = (res.data && res.data.total) || 0;
                list.innerHTML = items.map(function (item) {
                    const plant = item.plant || {};
                    return "<div class=\"line-item\"><span>" + (plant.name || "Plant") + " x " + item.quantity + "</span><strong>" + formatMoney((item.subtotal || 0)) + "</strong></div>";
                }).join("");
                const totalEl = document.getElementById("checkoutTotal");
                if (totalEl) {
                    totalEl.textContent = formatMoney(total);
                }
            })
            .catch(function () {
                list.innerHTML = "<p class=\"empty-state\">Could not load checkout from backend.</p>";
            });

        const placeOrderBtn = document.getElementById("placeOrder");
        if (placeOrderBtn) {
            placeOrderBtn.addEventListener("click", function () {
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
        if (!msgEl) {
            return;
        }
        const params = new URLSearchParams(window.location.search);
        const msg = params.get("msg");
        if (msg) {
            msgEl.textContent = msg;
            return;
        }
        request("/confirmation").then(function (res) {
            const serverMsg = res && res.data && res.data.message;
            msgEl.textContent = serverMsg || "Your order has been confirmed.";
        }).catch(function () {
            msgEl.textContent = "Your order has been confirmed.";
        });
    }

    function bindInfoPage() {
        const title = document.getElementById("infoTitle");
        const body = document.getElementById("infoBody");
        if (!title || !body) {
            return;
        }
        const topic = (new URLSearchParams(window.location.search).get("topic") || "information").replace(/-/g, " ");
        title.textContent = topic.charAt(0).toUpperCase() + topic.slice(1);
        body.textContent = "This section is active and clickable. You can connect this page to a dedicated backend endpoint when ready.";
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
        bindInfoPage();
    });
})();
