document.addEventListener("DOMContentLoaded", () => {
    const canvas = document.querySelector("#statement-category-chart");
    if (!canvas) return;
    const chartCard = document.querySelector(".statement-chart-card");
    const transactionsCard = document.querySelector(".transactions-card");
    const reportForm = document.querySelector(".statement-main > form");
    const scrollIndicator = document.querySelector("[data-statement-scroll-indicator]");
    if (reportForm && chartCard && transactionsCard && scrollIndicator) {
        scrollIndicator.hidden = false;
        let chartMode = false;
        const setMode = (showChart) => {
            chartMode = showChart;
            document.body.classList.toggle("statement-chart-mode", chartMode);
            document.body.classList.toggle("statement-list-mode", !chartMode);
            (chartMode ? chartCard : transactionsCard).append(reportForm);
            scrollIndicator.innerHTML = chartMode
                ? '<span aria-hidden="true">↑</span> Ver transações'
                : '<span aria-hidden="true">↓</span> Ver gráfico';
            if (chartMode) window.requestAnimationFrame(() => render());
        };
        setMode(false);
        scrollIndicator.addEventListener("click", () => {
            setMode(!chartMode);
            window.scrollTo({ top: 0, behavior: "smooth" });
        });

        let lastScrollY = window.scrollY;
        let switchingByScroll = false;
        const switchAtEdge = (direction) => {
            if (switchingByScroll) return;
            const atTop = window.scrollY <= 2;
            const atBottom = window.scrollY + window.innerHeight >= document.documentElement.scrollHeight - 4;
            if ((!chartMode && direction > 0 && atBottom) || (chartMode && direction < 0 && atTop)) {
                switchingByScroll = true;
                setMode(direction > 0);
                window.scrollTo({ top: 0, behavior: "smooth" });
                window.setTimeout(() => { switchingByScroll = false; }, 500);
            }
        };
        window.addEventListener("wheel", event => switchAtEdge(event.deltaY), { passive: true });
        window.addEventListener("scroll", () => {
            const currentY = window.scrollY;
            const direction = currentY - lastScrollY;
            lastScrollY = currentY;
            if (direction > 0 && currentY + window.innerHeight >= document.documentElement.scrollHeight - 4) {
                switchAtEdge(direction);
            }
        }, { passive: true });
        let touchStartY = 0;
        window.addEventListener("touchstart", event => { touchStartY = event.touches[0]?.clientY || 0; }, { passive: true });
        window.addEventListener("touchend", event => {
            const touchEndY = event.changedTouches[0]?.clientY || touchStartY;
            const gesture = touchStartY - touchEndY;
            if (Math.abs(gesture) > 35) switchAtEdge(gesture);
        }, { passive: true });
    }
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

    const chartProgress = 1;
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
    window.addEventListener("resize", () => render());
    render();
});
