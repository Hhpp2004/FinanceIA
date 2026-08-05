(() => {
  const reduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
  const revealItems = document.querySelectorAll('[data-reveal]');

  if (!reduceMotion) {
    document.documentElement.classList.add('motion-enabled');
    const revealObserver = new IntersectionObserver(entries => {
      entries.forEach(entry => {
        if (!entry.isIntersecting) return;
        entry.target.classList.add('is-visible');
        revealObserver.unobserve(entry.target);
      });
    }, { threshold: 0.12 });
    revealItems.forEach(item => revealObserver.observe(item));
  } else {
    revealItems.forEach(item => item.classList.add('is-visible'));
  }

  const scrollItems = [...document.querySelectorAll('[data-scroll]')];
  const progressBar = document.querySelector('.scroll-progress span');

  if (!reduceMotion && (scrollItems.length || progressBar)) {
    let ticking = false;

    const renderScrollMotion = () => {
      const viewportHeight = window.innerHeight;
      const maxScroll = Math.max(document.documentElement.scrollHeight - viewportHeight, 1);
      const progress = Math.min(Math.max(window.scrollY / maxScroll, 0), 1);

      if (progressBar) {
        progressBar.style.setProperty('--scroll-progress', progress.toFixed(4));
      }

      scrollItems.forEach(item => {
        const rect = item.getBoundingClientRect();
        const distanceFromCenter = rect.top + rect.height / 2 - viewportHeight / 2;
        const speed = Number(item.dataset.scrollSpeed || 0);
        const shift = Math.max(-70, Math.min(70, distanceFromCenter * speed));
        item.style.setProperty('--scroll-shift', `${shift.toFixed(2)}px`);
      });

      ticking = false;
    };

    const requestScrollRender = () => {
      if (ticking) return;
      ticking = true;
      window.requestAnimationFrame(renderScrollMotion);
    };

    window.addEventListener('scroll', requestScrollRender, { passive: true });
    window.addEventListener('resize', requestScrollRender);
    renderScrollMotion();
  }

  const stocksPreview = document.querySelector('[data-stocks-preview]');
  const stockRows = [...document.querySelectorAll('[data-preview-stock]')];
  const stockStatus = stocksPreview?.querySelector('[data-stocks-preview-status]');
  const stockMoney = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' });

  function updateStocksPreview() {
    stockRows.forEach(row => {
      const direction = Number(row.dataset.direction);
      const currentPrice = Number(row.dataset.price);
      const nextPrice = Math.max(.01, currentPrice * (1 + (Math.random() - .5) * .024));
      const change = direction * (.4 + Math.random() * 3.1);
      const value = row.querySelector('.stock-value');

      row.dataset.price = nextPrice.toFixed(2);
      row.querySelector('[data-preview-stock-price]').textContent = stockMoney.format(nextPrice);
      row.querySelector('[data-preview-stock-change]').textContent =
        `${direction > 0 ? '+' : '−'}${change.toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 }).replace('-', '')}%`;

      if (!reduceMotion) {
        value.classList.remove('is-updating');
        window.requestAnimationFrame(() => value.classList.add('is-updating'));
      }
    });

    if (stockStatus) {
      stockStatus.textContent = 'Atualizado agora';
      window.setTimeout(() => { stockStatus.textContent = 'Demonstração · 5s'; }, 2500);
    }
  }

  if (stocksPreview && stockRows.length) {
    window.setInterval(updateStocksPreview, 5000);
  }

  const preview = document.querySelector('[data-dashboard-preview]');
  if (!preview) return;

  const months = [
    { name: 'julho', balance: 3240, income: 5700, expense: 2460, bars: [42, 74, 56, 88, 65, 95], donut: [42, 26, 16, 16] },
    { name: 'agosto', balance: 4180, income: 6300, expense: 2120, bars: [58, 49, 82, 64, 91, 76], donut: [36, 31, 18, 15] },
    { name: 'setembro', balance: 3970, income: 5900, expense: 1930, bars: [37, 83, 68, 52, 78, 89], donut: [45, 21, 20, 14] }
  ];
  const money = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL', maximumFractionDigits: 0 });
  const month = preview.querySelector('[data-preview-month]');
  const balance = preview.querySelector('[data-preview-balance]');
  const income = preview.querySelector('[data-preview-income]');
  const expense = preview.querySelector('[data-preview-expense]');
  const bars = [...preview.querySelectorAll('[data-preview-bars] i')];
  const donut = preview.querySelector('[data-preview-donut]');
  let active = 0;

  function updatePreview() {
    const data = months[active];
    month.textContent = `Seu resumo financeiro de ${data.name}`;
    balance.textContent = money.format(data.balance);
    income.textContent = money.format(data.income);
    expense.textContent = money.format(data.expense);
    bars.forEach((bar, index) => { bar.style.setProperty('--bar-height', `${data.bars[index]}%`); });
    const [blue, green, yellow, red] = data.donut;
    donut.style.background = `conic-gradient(#1a73e8 0 ${blue}%, #34a853 ${blue}% ${blue + green}%, #fbbc04 ${blue + green}% ${blue + green + yellow}%, #ea4335 ${blue + green + yellow}% 100%)`;
    active = (active + 1) % months.length;
  }

  window.setInterval(updatePreview, 3200);

})();
