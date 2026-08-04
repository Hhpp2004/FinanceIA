document.addEventListener("DOMContentLoaded", () => {
    const reduceMotion = window.matchMedia("(prefers-reduced-motion: reduce)").matches;
    const progressBar = document.querySelector(".dashboard-scroll-progress span");
    const chartStage = document.querySelector("[data-chart-stage]");
    const balanceCard = document.querySelector(".summary-card.balance");
    const currentBalance = document.querySelector("[data-current-balance]");

    if (balanceCard && currentBalance) {
        const value = Number(currentBalance.dataset.balanceValue);
        balanceCard.classList.remove("balance-positive", "balance-negative", "balance-zero");
        balanceCard.classList.add(value > 0 ? "balance-positive" : value < 0 ? "balance-negative" : "balance-zero");
    }

    const marketTemplate = document.querySelector("[data-market-row-template]");
    const marketStatus = document.querySelector("[data-market-status]");
    const marketMoney = new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" });

    const fillMarketList = (selector, items = []) => {
        const list = document.querySelector(selector);
        if (!list || !marketTemplate) return;
        list.replaceChildren();

        items.slice(0, 5).forEach((item) => {
            const row = marketTemplate.content.firstElementChild.cloneNode(true);
            const logo = row.querySelector("[data-market-logo]");
            const ticker = String(item.ticker || item.stock || "");
            row.href = `/acoes/${encodeURIComponent(ticker)}`;
            row.setAttribute("aria-label", `Ver todos os dados de ${ticker}`);
            row.querySelector("[data-market-ticker]").textContent = ticker || "—";
            row.querySelector("[data-market-name]").textContent = item.name || "Ativo da B3";
            row.querySelector("[data-market-price]").textContent = marketMoney.format(Number(item.price ?? item.close ?? 0));
            row.querySelector("[data-market-change]").textContent = `${Number(item.change ?? 0).toLocaleString("pt-BR", { minimumFractionDigits: 2, maximumFractionDigits: 2 })}%`;

            if (item.logo) {
                const image = document.createElement("img");
                image.src = item.logo;
                image.alt = "";
                image.loading = "lazy";
                logo.append(image);
            } else {
                logo.textContent = String(item.ticker || item.stock || "A").slice(0, 1);
            }
            list.append(row);
        });

        if (!items.length) {
            const empty = document.createElement("p");
            empty.className = "market-empty";
            empty.textContent = "Nenhum ativo disponível neste momento.";
            list.append(empty);
        }
    };

    window.renderMarketMovers = ({ gainers = [], losers = [], updatedAt = "Atualizado agora" } = {}) => {
        fillMarketList("[data-market-gainers-list]", gainers);
        fillMarketList("[data-market-losers-list]", losers);
        if (marketStatus) marketStatus.textContent = updatedAt;
    };

    const searchForm = document.querySelector("[data-market-search-form]");
    const searchInput = searchForm?.querySelector("input");
    const searchPanel = document.querySelector("[data-market-search-panel]");
    const searchResults = document.querySelector("[data-market-search-results]");
    const searchStatus = document.querySelector("[data-market-search-status]");
    let searchTimer;
    let searchRequest;

    const renderSearchResults = (items) => {
        searchResults.replaceChildren();
        items.forEach((item) => {
            const link = document.createElement("a");
            const symbol = document.createElement("span");
            const copy = document.createElement("span");
            const ticker = document.createElement("strong");
            const name = document.createElement("small");
            const type = document.createElement("span");

            link.className = "market-search-result";
            link.href = `/acoes/${encodeURIComponent(item.stock)}`;
            symbol.className = "market-search-symbol";
            symbol.textContent = item.stock?.slice(0, 1) || "A";
            if (item.logo) {
                const image = document.createElement("img");
                image.src = item.logo;
                image.alt = "";
                symbol.replaceChildren(image);
            }
            copy.className = "market-search-copy";
            ticker.textContent = item.stock;
            name.textContent = item.name || "Ativo da B3";
            copy.append(ticker, name);
            type.className = "market-search-type";
            type.textContent = item.type === "fund" ? "FII" : "Ação";
            link.append(symbol, copy, type);
            searchResults.append(link);
        });
    };

    const searchAssets = async () => {
        const term = searchInput.value.trim();
        searchRequest?.abort();
        if (term.length < 2) {
            searchPanel.hidden = true;
            searchResults.replaceChildren();
            return;
        }

        searchPanel.hidden = false;
        searchStatus.textContent = "Pesquisando…";
        searchRequest = new AbortController();
        try {
            const response = await fetch(`/api/acoes/pesquisa?termo=${encodeURIComponent(term)}`, {
                signal: searchRequest.signal,
                headers: { Accept: "application/json" }
            });
            if (!response.ok) throw new Error(`HTTP ${response.status}`);
            const items = await response.json();
            renderSearchResults(items);
            searchStatus.textContent = items.length
                ? `${items.length} ${items.length === 1 ? "ativo encontrado" : "ativos encontrados"}`
                : "Nenhum ativo encontrado";
        } catch (error) {
            if (error.name === "AbortError") return;
            searchResults.replaceChildren();
            searchStatus.textContent = "Não foi possível pesquisar agora";
        }
    };

    searchForm?.addEventListener("submit", (event) => {
        event.preventDefault();
        searchAssets();
    });
    searchInput?.addEventListener("input", () => {
        window.clearTimeout(searchTimer);
        searchTimer = window.setTimeout(searchAssets, 350);
    });

    if (!reduceMotion && chartStage) {
        document.documentElement.classList.add("dashboard-motion");
    }

    const drawer = document.querySelector("[data-drawer-toggle]");
    const panel = document.querySelector(".app-drawer");
    const backdrop = document.querySelector("[data-drawer-backdrop]");

    const setDrawer = (open) => {
        panel.classList.toggle("open", open);
        backdrop.hidden = !open;
        drawer.setAttribute("aria-expanded", String(open));
        panel.setAttribute("aria-hidden", String(!open));
        document.body.classList.toggle("drawer-open", open);
    };
    drawer.addEventListener("click", () => setDrawer(!panel.classList.contains("open")));
    backdrop.addEventListener("click", () => setDrawer(false));
    document.addEventListener("keydown", (event) => event.key === "Escape" && setDrawer(false));

    const raw = window.categoryData || {};
    const meta = {
        MORADIA: ["Moradia", "#f59e0b"], TRANSPORTE: ["Transporte", "#14b8a6"],
        INVESTIMENTOS: ["Investimentos", "#6366f1"], LAZER: ["Lazer", "#8b5cf6"],
        SAUDE: ["Saúde", "#ec4899"], OUTROS: ["Outros", "#64748b"], IMPOSTOS: ["Impostos", "#ef4444"],
        ALIMENTACAO: ["Alimentação", "#3b82f6"]
    };
    const items = Object.entries(meta).map(([key, [label, color]]) => ({ key, label, color, value: Number(raw[key] || 0) }));
    const total = items.reduce((sum, item) => sum + item.value, 0);
    const money = new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" });
    document.querySelector("[data-chart-total]").textContent = money.format(total);

    if (!total) document.querySelector("[data-empty-chart]").hidden = false;

    const canvas = document.querySelector("#category-chart");
    const ctx = canvas.getContext("2d");
    const tooltip = document.querySelector("[data-chart-tooltip]");
    let arcs = [];
    let chartProgress = reduceMotion ? 1 : 0;
    const render = (reveal = chartProgress) => {
        const size = Math.min(canvas.parentElement.clientWidth, 460);
        const ratio = window.devicePixelRatio || 1;
        canvas.style.width = `${size}px`; canvas.style.height = `${size}px`;
        canvas.width = size * ratio; canvas.height = size * ratio; ctx.setTransform(ratio, 0, 0, ratio, 0, 0);
        ctx.clearRect(0, 0, size, size); arcs = [];
        const radius = size * .39, inner = radius * .59, center = size / 2;
        let start = -Math.PI / 2;
        (total ? items : [{ value: 1, color: "#e8edf5", label: "Sem gastos" }]).forEach((item) => {
            const angle = (item.value / (total || 1)) * Math.PI * 2 * reveal;
            ctx.beginPath(); ctx.arc(center, center, radius, start, start + angle); ctx.arc(center, center, inner, start + angle, start, true); ctx.closePath();
            ctx.fillStyle = item.color; ctx.fill();
            if (total && item.value) arcs.push({ ...item, start, end: start + angle, center, radius, inner });
            start += angle;
        });
    };
    const clamp = (value, min = 0, max = 1) => Math.min(Math.max(value, min), max);
    let ticking = false;

    const renderScrollScene = () => {
        const viewportHeight = window.innerHeight;
        const maxScroll = Math.max(document.documentElement.scrollHeight - viewportHeight, 1);
        const chartTop = chartStage.getBoundingClientRect().top;
        const scrollProgress = clamp(window.scrollY / maxScroll);
        const reachedScrollEnd = window.scrollY >= maxScroll - 1;
        chartProgress = reduceMotion || reachedScrollEnd
            ? 1
            : clamp((viewportHeight * .94 - chartTop) / (viewportHeight * .62));
        const smoothProgress = chartProgress * chartProgress * (3 - 2 * chartProgress);

        if (progressBar) {
            progressBar.style.setProperty("--dashboard-scroll-progress", scrollProgress.toFixed(4));
        }
        if (!reduceMotion) {
            chartStage.style.setProperty("--chart-scroll-shift", `${((1 - smoothProgress) * 64).toFixed(2)}px`);
            chartStage.style.setProperty("--chart-scroll-opacity", smoothProgress.toFixed(3));
            chartStage.style.setProperty("--chart-scroll-scale", (.96 + smoothProgress * .04).toFixed(4));
            chartStage.style.setProperty("--legend-scroll-shift", `${((1 - smoothProgress) * 32).toFixed(2)}px`);
        }

        render(smoothProgress);
        ticking = false;
    };

    const requestScrollRender = () => {
        if (ticking) return;
        ticking = true;
        window.requestAnimationFrame(renderScrollScene);
    };

    window.addEventListener("scroll", requestScrollRender, { passive: true });
    window.addEventListener("resize", requestScrollRender);
    renderScrollScene();
    canvas.addEventListener("mousemove", (event) => {
        const rect = canvas.getBoundingClientRect(), x = event.clientX - rect.left, y = event.clientY - rect.top;
        const hit = arcs.find((arc) => { const dx=x-arc.center, dy=y-arc.center, distance=Math.hypot(dx,dy); let angle=Math.atan2(dy,dx); if(angle < -Math.PI/2) angle += Math.PI*2; return distance >= arc.inner && distance <= arc.radius && angle >= arc.start && angle <= arc.end; });
        if (!hit) { tooltip.hidden = true; return; }
        tooltip.innerHTML = `<strong>${hit.label}</strong><span>${money.format(hit.value)} · ${(hit.value / total * 100).toLocaleString("pt-BR", { maximumFractionDigits: 1 })}%</span>`;
        tooltip.style.left = `${x}px`; tooltip.style.top = `${y}px`; tooltip.hidden = false;
    });
    canvas.addEventListener("mouseleave", () => tooltip.hidden = true);
});
