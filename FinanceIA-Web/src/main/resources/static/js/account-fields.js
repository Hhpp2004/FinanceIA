document.addEventListener("DOMContentLoaded", () => {
    const form = document.querySelector(".registration-form, .settings-form");
    if (!form) return;

    const country = form.querySelector("#paisTelefone");
    const phone = form.querySelector("[data-phone-local]");
    const completePhone = form.querySelector("#telefoneCompleto");
    const emojiPattern = /[\p{Extended_Pictographic}\uFE0F\u200D]/gu;

    // Ajusta o seletor ao código do país já salvo na conta.
    if (phone && country && phone.value) {
        const stored = phone.value.trim();
        const match = stored.match(/^\+(\d{1,3})\s*(.*)$/);
        if (match && [...country.options].some((option) => option.value === match[1])) {
            country.value = match[1];
            phone.value = match[2].replace(/\D/g, "");
        } else {
            country.value = "55";
            phone.value = stored.replace(/\D/g, "");
        }
    }

    form.querySelectorAll("[data-no-emoji]").forEach((field) => {
        const removeEmoji = () => {
            const start = field.selectionStart;
            const before = field.value;
            field.value = before.replace(emojiPattern, "");
            if (start !== null && before !== field.value) {
                const removed = before.slice(0, start).replace(/[\p{Extended_Pictographic}\uFE0F\u200D]/gu, "").length;
                field.setSelectionRange(removed, removed);
            }
        };
        field.addEventListener("input", removeEmoji);
        field.addEventListener("paste", (event) => {
            if (emojiPattern.test(event.clipboardData?.getData("text") || "")) event.preventDefault();
            emojiPattern.lastIndex = 0;
        });
    });

    const formatPhone = () => {
        if (!phone || !country) return;
        const digits = phone.value.replace(/\D/g, "").slice(0, 15);
        const isBrazil = country.value === "55";
        const nationalDigits = isBrazil ? digits.slice(0, 11) : digits;
        phone.value = isBrazil
            ? (nationalDigits.length > 10
                ? nationalDigits.replace(/^(\d{2})(\d{5})(\d{0,4}).*/, "($1) $2-$3").replace(/-$/, "")
                : nationalDigits.replace(/^(\d{2})(\d{0,4})(\d{0,4}).*/, "($1) $2-$3").replace(/[- ]$/, ""))
            : nationalDigits;
        const minLength = isBrazil ? 10 : 7;
        const maxLength = isBrazil ? 11 : 12;
        const validLength = nationalDigits.length >= minLength && nationalDigits.length <= maxLength;
        phone.setCustomValidity(validLength ? "" : "Informe o telefone com DDD e número completo.");
        if (completePhone) completePhone.value = country.value && validLength
            ? `+${country.value} ${phone.value}` : "";
    };

    phone?.addEventListener("input", formatPhone);
    country?.addEventListener("change", formatPhone);
    formatPhone();

    form.addEventListener("submit", (event) => {
        const email = form.querySelector('input[type="email"]');
        if (email && email.value && !/^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/.test(email.value)) {
            email.setCustomValidity("Informe um e-mail válido.");
            email.reportValidity();
            event.preventDefault();
            return;
        }
        email?.setCustomValidity("");
        formatPhone();
        if (phone?.validationMessage) {
            phone.reportValidity();
            event.preventDefault();
        }
    });
});
