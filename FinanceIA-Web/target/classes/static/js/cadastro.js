document.addEventListener("DOMContentLoaded", () => {
    const password = document.querySelector("#senha");
    const confirmation = document.querySelector("#confirmarSenha");
    const matchMessage = document.querySelector("#password-match");
    if (!password || !confirmation || !matchMessage) return;

    const rules = {
        lower: (value) => /[a-z]/.test(value),
        upper: (value) => /[A-Z]/.test(value),
        number: (value) => /\d/.test(value),
        special: (value) => /[^A-Za-z0-9\s]/.test(value),
        length: (value) => value.length >= 8,
    };

    const validatePassword = () => {
        const value = password.value;
        let valid = true;

        Object.entries(rules).forEach(([name, test]) => {
            const item = document.querySelector(`[data-requirement="${name}"]`);
            const passed = test(value);
            item.classList.toggle("requirement-valid", passed);
            item.classList.toggle("requirement-invalid", value.length > 0 && !passed);
            valid = valid && passed;
        });

        password.setCustomValidity(valid || value.length === 0 ? "" : "A senha não atende a todos os requisitos.");
        validateConfirmation();
    };

    const validateConfirmation = () => {
        const hasValue = confirmation.value.length > 0;
        const matches = hasValue && confirmation.value === password.value;
        confirmation.setCustomValidity(!hasValue || matches ? "" : "As senhas não coincidem.");
        matchMessage.textContent = hasValue ? (matches ? "As senhas coincidem." : "As senhas não coincidem.") : "";
        matchMessage.classList.toggle("match-valid", matches);
        matchMessage.classList.toggle("match-invalid", hasValue && !matches);
    };

    password.addEventListener("input", validatePassword);
    confirmation.addEventListener("input", validateConfirmation);
});
