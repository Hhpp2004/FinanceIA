const initializePasswordToggles = (root = document) => {
    root.querySelectorAll('input[type="password"]:not([data-password-toggle])').forEach((input) => {
        input.dataset.passwordToggle = "true";

        const wrapper = document.createElement("div");
        wrapper.className = "password-input-wrapper";
        input.parentNode.insertBefore(wrapper, input);
        wrapper.appendChild(input);

        const button = document.createElement("button");
        button.className = "password-toggle";
        button.type = "button";
        button.setAttribute("aria-label", "Mostrar senha");
        button.setAttribute("aria-pressed", "false");
        button.setAttribute("title", "Mostrar senha");
        button.innerHTML = `
            <svg class="password-toggle-icon" viewBox="0 0 24 24" aria-hidden="true">
                <path d="M2.2 12s3.5-6 9.8-6 9.8 6 9.8 6-3.5 6-9.8 6-9.8-6-9.8-6Z"></path>
                <circle cx="12" cy="12" r="2.8"></circle>
            </svg>`;

        button.addEventListener("click", () => {
            const showPassword = input.type === "password";
            input.type = showPassword ? "text" : "password";
            const label = showPassword ? "Ocultar senha" : "Mostrar senha";
            button.setAttribute("aria-label", label);
            button.setAttribute("aria-pressed", String(showPassword));
            button.setAttribute("title", label);
            button.classList.toggle("is-visible", showPassword);
        });

        wrapper.appendChild(button);
    });
};

document.addEventListener("DOMContentLoaded", () => initializePasswordToggles());
document.addEventListener("htmx:load", (event) => initializePasswordToggles(event.detail.elt));
