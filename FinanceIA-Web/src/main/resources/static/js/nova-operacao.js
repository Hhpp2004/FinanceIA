document.addEventListener("DOMContentLoaded", () => {
    const types = document.querySelectorAll('input[name="tipo"]');
    const categories = document.querySelector("[data-categories]");
    const categoryInputs = categories.querySelectorAll('input[name="categoria"]');
    const date = document.querySelector('input[name="data"]');

    const now = new Date();
    const today = [now.getFullYear(), String(now.getMonth() + 1).padStart(2, "0"), String(now.getDate()).padStart(2, "0")].join("-");
    date.value = today;
    date.max = today;

    const updateCategories = (type) => {
        const isExpense = type === "SAIDA";
        categories.hidden = !isExpense;
        categoryInputs.forEach((input) => {
            input.disabled = !isExpense;
            input.required = isExpense;
            if (!isExpense) input.checked = false;
        });
    };

    types.forEach((input) => input.addEventListener("change", () => updateCategories(input.value)));
});
