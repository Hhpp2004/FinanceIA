document.addEventListener("DOMContentLoaded", () => {
    const canvas = document.querySelector("#statement-category-chart");
    if (!canvas) return;
    const chartCard = document.querySelector(".statement-chart-card");
    const reduceMotion = window.matchMedia("(prefers-reduced-motion: reduce)").matches;
    const transactionCount = document.querySelectorAll(".transactions-list .transaction-row").length;
    if (transactionCount <= 3) document.body.classList.add("statement-compact-list");

    const raw = window.statementCategoryData || {};
    const categories = [
        ["MORADIA", "Moradia", "#f59e0b"],
        ["TRANSPORTE", "Transporte", "#14b8a6"],
        ["INVESTIMENTOS", "Investimentos", "#6366f1"],
        ["LAZER", "Lazer", "#8b5cf6"],
        ["SAUDE", "Saúde", "#ec4899"],
        ["OUTROS", "Outros", "#64748b"],
        ["ALIMENTACAO", "Alimentação", "#3b82f6"],
        ["IMPOSTOS", "Impostos", "#ef4444"],
    ].map(([key, label, color]) => ({ key, label, color, value: Number(raw[key] || 0) }));
    const total = categories.reduce((sum, item) => sum + item.value, 0);
    const money = new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" });
    const ctx = canvas.getContext("2d");
    const tooltip = document.querySelector("[data-statement-chart-tooltip]");
    const emptyMessage = document.querySelector("[data-statement-empty-chart]");
    let arcs = [];

    document.querySelector("[data-statement-chart-total]").textContent = money.format(total);
    if (total === 0 && emptyMessage) emptyMessage.hidden = false;

    let chartProgress = reduceMotion ? 1 : 0;
    const render = (reveal = chartProgress) => {
        const size = Math.min(canvas.parentElement.clientWidth, 460);
        const ratio = window.devicePixelRatio || 1;
        canvas.style.width = `${size}px`;
        canvas.style.height = `${size}px`;
        canvas.width = size * ratio;
        canvas.height = size * ratio;
        ctx.setTransform(ratio, 0, 0, ratio, 0, 0);
        ctx.clearRect(0, 0, size, size);
        arcs = [];

        const radius = size * .39;
        const inner = radius * .59;
        const center = size / 2;
        let start = -Math.PI / 2;
        const slices = total > 0 ? categories : [{ value: 1, color: "#e8edf5", label: "Sem gastos" }];
        slices.forEach((item) => {
            const angle = (item.value / (total || 1)) * Math.PI * 2 * reveal;
            ctx.beginPath();
            ctx.arc(center, center, radius, start, start + angle);
            ctx.arc(center, center, inner, start + angle, start, true);
            ctx.closePath();
            ctx.fillStyle = item.color;
            ctx.fill();
            if (total > 0 && item.value > 0 && reveal >= 1) arcs.push({ ...item, start, end: start + angle, center, radius, inner });
            start += angle;
        });
    };

    let focusLayout = false;
    let lastScrollY = window.scrollY;
    let ticking = false;
    const clamp = (value) => Math.min(Math.max(value, 0), 1);
    const renderScrollScene = () => {
        const currentScrollY = window.scrollY;
        const scrollDelta = currentScrollY - lastScrollY;
        const chartTop = chartCard.getBoundingClientRect().top;
        if (scrollDelta < -1 && focusLayout) {
            focusLayout = false;
            document.body.classList.remove("statement-focus-layout");
        } else if (scrollDelta > 1 && !focusLayout && chartTop < window.innerHeight * .92) {
            focusLayout = true;
            document.body.classList.add("statement-focus-layout");
        }
        lastScrollY = window.scrollY;

        const progress = reduceMotion ? 1 : clamp((window.innerHeight * .94 - chartTop) / (window.innerHeight * .62));
        chartProgress = progress * progress * (3 - 2 * progress);
        chartCard.style.setProperty("--statement-chart-opacity", chartProgress.toFixed(3));
        chartCard.style.setProperty("--statement-chart-shift", `${((1 - chartProgress) * 64).toFixed(2)}px`);
        chartCard.querySelector(".chart-legend")?.style.setProperty("--statement-legend-shift", `${((1 - chartProgress) * 28).toFixed(2)}px`);
        chartCard.querySelector(".chart-legend")?.style.setProperty("--statement-legend-opacity", chartProgress.toFixed(3));
        render(chartProgress);
        ticking = false;
    };
    const requestScrollRender = () => {
        if (ticking) return;
        ticking = true;
        window.requestAnimationFrame(renderScrollScene);
    };

    canvas.addEventListener("pointermove", (event) => {
        const rect = canvas.getBoundingClientRect();
        const x = event.clientX - rect.left;
        const y = event.clientY - rect.top;
        const hit = arcs.find((arc) => {
            const dx = x - arc.center;
            const dy = y - arc.center;
            const distance = Math.hypot(dx, dy);
            let angle = Math.atan2(dy, dx);
            if (angle < -Math.PI / 2) angle += Math.PI * 2;
            return distance >= arc.inner && distance <= arc.radius && angle >= arc.start && angle <= arc.end;
        });
        if (!hit) {
            tooltip.hidden = true;
            return;
        }
        const percentage = (hit.value / total * 100).toLocaleString("pt-BR", { maximumFractionDigits: 1 });
        tooltip.innerHTML = `<strong>${hit.label}</strong><span>${money.format(hit.value)} · ${percentage}%</span>`;
        tooltip.style.left = `${x}px`;
        tooltip.style.top = `${y}px`;
        tooltip.hidden = false;
    });
    canvas.addEventListener("pointerleave", () => tooltip.hidden = true);
    window.addEventListener("scroll", requestScrollRender, { passive: true });
    window.addEventListener("resize", requestScrollRender);
    renderScrollScene();
});
