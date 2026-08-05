document.addEventListener("DOMContentLoaded", () => {
    const form = document.querySelector(".login-form");
    if (!form) return;

    form.addEventListener("submit", () => {
        const button = form.querySelector(".submit-button");
        const label = form.querySelector("[data-submit-label]");
        const spinner = form.querySelector("[data-spinner]");
        button.disabled = true;
        button.setAttribute("aria-busy", "true");
        label.textContent = form.dataset.loadingLabel || "Aguarde...";
        spinner.hidden = false;
    });

    document.querySelectorAll("[data-unavailable]").forEach((link) => {
        link.addEventListener("click", (event) => event.preventDefault());
    });
});

document.addEventListener("htmx:responseError", () => window.location.reload());
