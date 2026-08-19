const DATA = window.DECK_DATA;
const slides = DATA.content.slides;
const metrics = DATA.metrics || {};

let current = 0;
let wheelLock = false;
let touchStartX = 0;
let touchStartY = 0;

const deck = document.getElementById("deck");
const progressBar = document.getElementById("progressBar");
const slideCounter = document.getElementById("slideCounter");
const notes = document.getElementById("speakerNotes");
const DECK_WIDTH = 1600;
const DECK_HEIGHT = 900;

const moneyBn = value => `Rp ${(value / 1_000_000_000).toFixed(2)}B`;
const pct = value => value.toFixed(3);

function escapeHtml(value) {
  return String(value).replace(/[&<>"]/g, char => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;" }[char]));
}

function featurePills(features) {
  return `<div class="feature-cloud stagger">${features.map(feature => `<span class="feature">${escapeHtml(feature)}</span>`).join("")}</div>`;
}

function metricCards(cards) {
  return `<div class="card-grid stagger">${cards.map(card => `
    <article class="card metric-card">
      <div class="label">${escapeHtml(card.label)}</div>
      <span class="value count-up" data-value="${escapeHtml(card.value)}">0</span>
      <div class="caption">${escapeHtml(card.caption)}</div>
    </article>`).join("")}</div>`;
}

function tablePreview(rows) {
  const cols = ["price", "district", "city", "bed_rooms", "bath_rooms", "carport", "land_area", "building_area"];
  return `<div class="table-wrap"><table><thead><tr>${cols.map(col => `<th>${col}</th>`).join("")}</tr></thead><tbody>
    ${rows.map(row => `<tr>${cols.map(col => `<td>${escapeHtml(row[col] ?? "")}</td>`).join("")}</tr>`).join("")}
  </tbody></table></div>`;
}

function comparisonTable(rows) {
  return `<div class="table-wrap"><table><thead><tr><th>Model</th><th>R2 3 var</th><th>R2 5 var</th><th>RMSE 3 var</th><th>RMSE 5 var</th></tr></thead><tbody>
    ${rows.map(row => `<tr><td>${row.model}</td><td>${pct(row.r2_3)}</td><td>${pct(row.r2_5)}</td><td>${moneyBn(row.rmse_3)}</td><td>${moneyBn(row.rmse_5)}</td></tr>`).join("")}
  </tbody></table></div>`;
}

function highlightCode(source) {
  const code = String(source || "");
  const pattern = /(\/\/.*$)|("(?:\\.|[^"\\])*")|\b(package|import|public|private|protected|class|abstract|interface|implements|extends|return|new|if|else|switch|case|default|while|for|break|continue|static|final|void|int|double|boolean|true|false|null)\b|\b([A-Z][A-Za-z0-9_]*)\b|\b([a-z][A-Za-z0-9_]*)\s*(?=\()|\b(\d+)\b/gm;
  let html = "";
  let lastIndex = 0;

  for (const match of code.matchAll(pattern)) {
    html += escapeHtml(code.slice(lastIndex, match.index));
    if (match[1]) html += `<span class="tok-comment">${escapeHtml(match[1])}</span>`;
    else if (match[2]) html += `<span class="tok-string">${escapeHtml(match[2])}</span>`;
    else if (match[3]) html += `<span class="tok-key">${escapeHtml(match[3])}</span>`;
    else if (match[4]) html += `<span class="tok-type">${escapeHtml(match[4])}</span>`;
    else if (match[5]) html += `<span class="tok-fn">${escapeHtml(match[5])}</span>`;
    else if (match[6]) html += `<span class="tok-num">${escapeHtml(match[6])}</span>`;
    lastIndex = match.index + match[0].length;
  }

  html += escapeHtml(code.slice(lastIndex));
  return html;
}

function codePanel(slide) {
  const code = slide.codeHtml || highlightCode(slide.code || "");
  const bullets = slide.bullets?.length ? `<ul class="bullet-list stagger">${slide.bullets.map(item => `<li>${item}</li>`).join("")}</ul>` : "";
  const label = slide.language ? `<span class="pill">${escapeHtml(slide.language)}</span>` : "";
  return `<div class="slide-content grid-2">
    <section><div class="eyebrow">${slide.eyebrow}</div><h2>${slide.title}</h2><p class="lead">${slide.lead}</p>${bullets}</section>
    <aside class="card code-card animate-in">
      <div class="code-meta">${label}${slide.caption ? `<span class="code-caption">${slide.caption}</span>` : ""}</div>
      <pre class="code-window"><code>${code}</code></pre>
    </aside>
  </div>`;
}

function diagramPanel(slide) {
  return `<div class="slide-content grid-2">
    <section><div class="eyebrow">${slide.eyebrow}</div><h2>${slide.title}</h2><p class="lead">${slide.lead}</p><ul class="bullet-list stagger">${slide.bullets.map(item => `<li>${item}</li>`).join("")}</ul></section>
    <aside class="card diagram-card animate-in">
      <img class="diagram-image" src="${slide.image}" alt="${escapeHtml(slide.alt || slide.title)}" />
      ${slide.caption ? `<p class="diagram-caption">${slide.caption}</p>` : ""}
    </aside>
  </div>`;
}

function showcasePanel(slide) {
  return `<div class="slide-content">
    <div class="eyebrow">${slide.eyebrow}</div>
    <h2>${slide.title}</h2>
    <p class="lead">${slide.lead}</p>
    <div class="showcase-grid stagger">
      ${slide.items.map(item => `
        <article class="card media-card">
          <img src="${item.src}" alt="${escapeHtml(item.title)}" />
          <h3>${item.title}</h3>
          <p>${item.caption}</p>
        </article>`).join("")}
    </div>
  </div>`;
}

function slideTemplate(slide) {
  switch (slide.type) {
    case "cover":
      return `${slide.background ? `<img class="slide-bg" src="${slide.background}" alt="" />` : ""}
        <div class="slide-content animate-in">
          <div class="eyebrow">${slide.eyebrow}</div>
          <h1>${slide.title}</h1>
          <p class="subtitle">${slide.subtitle}</p>
          <div class="meta-row">${slide.meta.map(item => `<span class="pill">${item}</span>`).join("")}</div>
        </div>`;
    case "split":
      return `<div class="slide-content grid-2">
        <section class="animate-in"><div class="eyebrow">${slide.eyebrow}</div><h2>${slide.title}</h2><p class="lead">${slide.lead}</p><ul class="bullet-list stagger">${slide.bullets.map(item => `<li>${item}</li>`).join("")}</ul></section>
        <aside class="card big-stat animate-in"><strong>${slide.stat.value}</strong><span>${slide.stat.label}</span><img class="orbit" src="assets/images/data-orbit.svg" alt="" /></aside>
      </div>`;
    case "dataset":
      return `<div class="slide-content"><div class="eyebrow">${slide.eyebrow}</div><h2>${slide.title}</h2><p class="lead">${slide.lead}</p>${metricCards(slide.cards)}${tablePreview(slide.preview)}</div>`;
    case "methodology":
      return `<div class="slide-content"><div class="eyebrow">${slide.eyebrow}</div><h2>${slide.title}</h2><div class="step-row stagger">${slide.steps.map((step, index) => `<article class="card step"><div class="step-num">0${index + 1}</div><h3>${step.title}</h3><p>${step.text}</p></article>`).join("")}</div><div class="formula">${slide.formula}</div></div>`;
    case "section":
      return `<div class="slide-content section-title animate-in"><div class="kicker">${slide.kicker}</div><h1>${slide.title}</h1><p class="subtitle">${slide.subtitle}</p></div>`;
    case "cards":
      return `<div class="slide-content"><div class="eyebrow">${slide.eyebrow}</div><h2>${slide.title}</h2><p class="lead">${slide.lead}</p>${metricCards(slide.cards)}${slide.footer ? `<p class="cards-footer">${slide.footer}</p>` : ""}</div>`;
    case "diagram":
      return diagramPanel(slide);
    case "code":
      return codePanel(slide);
    case "showcase":
      return showcasePanel(slide);
    case "experimentDeep":
      return `<div class="slide-content grid-2"><section><div class="eyebrow">${slide.eyebrow}</div><h2>${slide.title}</h2>${featurePills(slide.features)}<div class="insight-list stagger">${slide.insights.map(item => `<div class="insight">${item}</div>`).join("")}</div></section><aside class="card chart-card"><canvas class="chart" data-chart="${slide.chart}"></canvas>${slide.notebookImage ? `<img class="notebook-shot" src="${slide.notebookImage}" alt="Notebook output chart" />` : ""}</aside></div>`;
    case "metrics":
      return `<div class="slide-content"><div class="eyebrow">${slide.eyebrow}</div><h2>${slide.title}</h2>${metricCards(slide.cards)}<div class="comparison-grid"><div class="card chart-card"><canvas class="chart" data-chart="${slide.chart}"></canvas></div><div class="card chart-card"><canvas class="chart" data-chart="${slide.predictionChart}"></canvas></div></div><p class="lead">${slide.takeaway}</p></div>`;
    case "comparison":
      return `<div class="slide-content"><div class="eyebrow">${slide.eyebrow}</div><h2>${slide.title}</h2><p class="lead">${slide.lead}</p><div class="comparison-grid">${slide.charts.map(chart => `<div class="card chart-card"><canvas class="chart" data-chart="${chart}"></canvas></div>`).join("")}</div>${comparisonTable(slide.table)}</div>`;
    case "interpretation":
      return `<div class="slide-content grid-2"><section><div class="eyebrow">${slide.eyebrow}</div><h2>${slide.title}</h2><div class="insight-list stagger">${slide.points.map(point => `<article class="insight"><h3>${point.title}</h3><p>${point.text}</p></article>`).join("")}</div></section><aside class="card chart-card"><canvas class="chart" data-chart="${slide.chart}"></canvas></aside></div>`;
    case "findings":
      return `<div class="slide-content"><div class="eyebrow">${slide.eyebrow}</div><h2>${slide.title}</h2><div class="finding-grid stagger">${slide.findings.map((finding, index) => `<article class="card finding"><div class="num">${String(index + 1).padStart(2, "0")}</div><p>${finding}</p></article>`).join("")}</div></div>`;
    case "conclusion":
      return `<div class="slide-content grid-2"><section><div class="eyebrow">${slide.eyebrow}</div><h2>${slide.title}</h2><p class="verdict">${slide.verdict}</p><ul class="bullet-list stagger">${slide.tradeoffs.map(item => `<li>${item}</li>`).join("")}</ul></section><aside class="card winner-card"><span>${slide.winner.label || "Recommended focus"}</span><strong>${slide.winner.model}</strong><span>${slide.winner.score}</span><span>${slide.winner.rmse}</span></aside></div>`;
    case "thanks":
      return `<div class="slide-content section-title animate-in"><div class="eyebrow">${slide.meta.join(" / ")}</div><h1>${slide.title}</h1><p class="subtitle">${slide.subtitle}</p></div>`;
    default:
      return `<div class="slide-content"><h2>${slide.title}</h2></div>`;
  }
}

function render() {
  deck.innerHTML = slides.map((slide, index) => `<section class="slide ${slide.type === "section" || slide.type === "thanks" ? "section-slide" : ""}" data-index="${index}" id="${slide.id}">${slideTemplate(slide)}</section>`).join("");
  fitDeckToViewport();
  show(0, true);
}

function show(index, immediate = false) {
  current = Math.max(0, Math.min(slides.length - 1, index));
  document.querySelectorAll(".slide").forEach((element, slideIndex) => {
    element.classList.toggle("active", slideIndex === current);
    element.classList.toggle("previous", slideIndex < current);
  });
  progressBar.style.width = `${((current + 1) / slides.length) * 100}%`;
  slideCounter.textContent = `${current + 1} / ${slides.length}`;
  notes.textContent = slides[current].notes || "";
  animateCounters();
  requestAnimationFrame(drawVisibleCharts);
  if (!immediate) history.replaceState(null, "", `#${slides[current].id}`);
}

function next() { show(current + 1); }
function prev() { show(current - 1); }

function animateCounters() {
  const active = document.querySelector(".slide.active");
  active?.querySelectorAll(".count-up").forEach(element => {
    const target = parseFloat(element.dataset.value.replace(/,/g, ""));
    if (Number.isNaN(target)) {
      element.textContent = element.dataset.value;
      return;
    }
    const decimals = (element.dataset.value.split(".")[1] || "").length;
    const start = performance.now();
    function tick(now) {
      const progress = Math.min(1, (now - start) / 650);
      const eased = 1 - Math.pow(1 - progress, 3);
      element.textContent = (target * eased).toLocaleString(undefined, { minimumFractionDigits: decimals, maximumFractionDigits: decimals });
      if (progress < 1) requestAnimationFrame(tick);
    }
    requestAnimationFrame(tick);
  });
}

function resizeCanvas(canvas) {
  const ratio = window.devicePixelRatio || 1;
  const rect = canvas.getBoundingClientRect();
  canvas.width = Math.max(1, rect.width * ratio);
  canvas.height = Math.max(1, rect.height * ratio);
  const ctx = canvas.getContext("2d");
  ctx.setTransform(ratio, 0, 0, ratio, 0, 0);
  return { ctx, w: rect.width, h: rect.height };
}

function drawVisibleCharts() {
  document.querySelectorAll(".slide.active canvas.chart").forEach(canvas => drawChart(canvas));
}

function fitDeckToViewport() {
  const horizontalPadding = 40;
  const verticalPadding = 120;
  const availableWidth = Math.max(320, window.innerWidth - horizontalPadding);
  const availableHeight = Math.max(240, window.innerHeight - verticalPadding);
  const scale = Math.min(availableWidth / DECK_WIDTH, availableHeight / DECK_HEIGHT);
  deck.style.setProperty("--deck-scale", Math.min(scale, 1));
}

function drawChart(canvas) {
  const type = canvas.dataset.chart;
  const { ctx, w, h } = resizeCanvas(canvas);
  ctx.clearRect(0, 0, w, h);
  if (type === "correlation3") return drawHeatmap(ctx, w, h, metrics.correlations.three_variables);
  if (type === "correlation5") return drawHeatmap(ctx, w, h, metrics.correlations.five_variables);
  if (type === "metrics3") return drawModelBars(ctx, w, h, metrics.experiments.three_variables.models, "Experiment 1: Test R2");
  if (type === "metrics5") return drawModelBars(ctx, w, h, metrics.experiments.five_variables.models, "Experiment 2: Test R2");
  if (type === "r2Comparison") return drawGrouped(ctx, w, h, "Testing R2", "r2");
  if (type === "rmseComparison") return drawGrouped(ctx, w, h, "RMSE in billion Rupiah", "rmse");
  if (type === "prediction3Linear") return drawPrediction(ctx, w, h, metrics.experiments.three_variables.models.linear_regression, "Linear: actual vs predicted");
  if (type === "prediction5RandomForest") return drawPrediction(ctx, w, h, metrics.experiments.five_variables.models.random_forest, "Random Forest: actual vs predicted");
  if (type === "generalizationGap") return drawGap(ctx, w, h);
}

function title(ctx, text, x, y) {
  ctx.fillStyle = "#eff9ff";
  ctx.font = "700 18px Segoe UI, sans-serif";
  ctx.fillText(text, x, y);
}

function axisLabel(ctx, text, x, y) {
  ctx.fillStyle = "#9fb6c8";
  ctx.font = "12px Segoe UI, sans-serif";
  ctx.fillText(text, x, y);
}

function roundedRect(ctx, x, y, width, height, radius) {
  const r = Math.min(radius, Math.abs(width) / 2, Math.abs(height) / 2);
  ctx.beginPath();
  ctx.moveTo(x + r, y);
  ctx.arcTo(x + width, y, x + width, y + height, r);
  ctx.arcTo(x + width, y + height, x, y + height, r);
  ctx.arcTo(x, y + height, x, y, r);
  ctx.arcTo(x, y, x + width, y, r);
  ctx.closePath();
}

function drawModelBars(ctx, w, h, modelMap, label) {
  title(ctx, label, 18, 28);
  const entries = Object.values(modelMap);
  const base = h - 44;
  const plotH = h - 86;
  const barW = Math.min(82, (w - 80) / entries.length - 24);
  entries.forEach((model, index) => {
    const x = 42 + index * ((w - 80) / entries.length) + 10;
    const barHeight = (model.test_r2 / 0.6) * plotH;
    const gradient = ctx.createLinearGradient(0, base - barHeight, 0, base);
    gradient.addColorStop(0, index === 0 ? "#2afadf" : index === 1 ? "#ffd166" : "#4f8cff");
    gradient.addColorStop(1, "rgba(255,255,255,.12)");
    ctx.fillStyle = gradient;
    roundedRect(ctx, x, base - barHeight, barW, barHeight, 12);
    ctx.fill();
    ctx.fillStyle = "#eef8ff";
    ctx.font = "800 18px Segoe UI, sans-serif";
    ctx.fillText(model.test_r2.toFixed(3), x, base - barHeight - 10);
    axisLabel(ctx, model.name.replace(" Regression", ""), x - 6, base + 24);
  });
}

function drawGrouped(ctx, w, h, label, mode) {
  title(ctx, label, 18, 28);
  const exp3 = metrics.experiments.three_variables.models;
  const exp5 = metrics.experiments.five_variables.models;
  const keys = ["linear_regression", "decision_tree", "random_forest"];
  const max = mode === "r2" ? 0.6 : 19;
  const base = h - 48;
  const plotH = h - 92;
  const groupW = (w - 80) / keys.length;
  keys.forEach((key, index) => {
    const v3 = mode === "r2" ? exp3[key].test_r2 : exp3[key].rmse / 1e9;
    const v5 = mode === "r2" ? exp5[key].test_r2 : exp5[key].rmse / 1e9;
    const x = 42 + index * groupW;
    [["3 var", v3, "#2afadf"], ["5 var", v5, "#4f8cff"]].forEach((bar, barIndex) => {
      const barHeight = (bar[1] / max) * plotH;
      ctx.fillStyle = bar[2];
      roundedRect(ctx, x + barIndex * 34, base - barHeight, 24, barHeight, 8);
      ctx.fill();
      axisLabel(ctx, mode === "r2" ? bar[1].toFixed(3) : bar[1].toFixed(1), x + barIndex * 30 - 2, base - barHeight - 8);
    });
    axisLabel(ctx, exp3[key].name.replace(" Regression", "").replace("Random ", "RF "), x - 6, base + 24);
  });
  axisLabel(ctx, "cyan = 3 variables, blue = 5 variables", 18, h - 10);
}

function drawPrediction(ctx, w, h, model, label) {
  title(ctx, label, 18, 28);
  const actual = model.actual_first_10 || [];
  const predicted = model.predictions_first_10 || [];
  const max = Math.max(...actual, ...predicted) / 1e9;
  const base = h - 48;
  const plotH = h - 92;
  const step = (w - 56) / actual.length;
  actual.forEach((value, index) => {
    const x = 28 + index * step;
    const actualHeight = (value / 1e9 / max) * plotH;
    const predictedHeight = (predicted[index] / 1e9 / max) * plotH;
    ctx.fillStyle = "#2afadf";
    roundedRect(ctx, x, base - actualHeight, 10, actualHeight, 4);
    ctx.fill();
    ctx.fillStyle = "#ffd166";
    roundedRect(ctx, x + 12, base - predictedHeight, 10, predictedHeight, 4);
    ctx.fill();
  });
  axisLabel(ctx, "cyan = actual, amber = predicted, first 10 test samples", 18, h - 12);
}

function drawGap(ctx, w, h) {
  title(ctx, "Train vs Test R2 gap", 18, 28);
  const rows = [
    ["LR 3", metrics.experiments.three_variables.models.linear_regression],
    ["DT 3", metrics.experiments.three_variables.models.decision_tree],
    ["RF 3", metrics.experiments.three_variables.models.random_forest],
    ["LR 5", metrics.experiments.five_variables.models.linear_regression],
    ["DT 5", metrics.experiments.five_variables.models.decision_tree],
    ["RF 5", metrics.experiments.five_variables.models.random_forest],
  ];
  const base = h - 48;
  const plotH = h - 92;
  const step = (w - 60) / rows.length;
  rows.forEach(([label, model], index) => {
    const x = 34 + index * step;
    const trainHeight = model.train_r2 * plotH;
    const testHeight = model.test_r2 * plotH;
    ctx.fillStyle = "rgba(79,140,255,.38)";
    roundedRect(ctx, x, base - trainHeight, 20, trainHeight, 7);
    ctx.fill();
    ctx.fillStyle = "#2afadf";
    roundedRect(ctx, x + 23, base - testHeight, 20, testHeight, 7);
    ctx.fill();
    axisLabel(ctx, label, x - 2, base + 22);
  });
  axisLabel(ctx, "blue = training, cyan = testing", 18, h - 10);
}

function drawHeatmap(ctx, w, h, corr) {
  title(ctx, "Correlation matrix", 18, 28);
  const labels = corr.labels;
  const n = labels.length;
  const size = Math.min((w - 120) / n, (h - 96) / n);
  const startX = Math.max(92, (w - size * n) / 2 + 24);
  const startY = 58;
  for (let row = 0; row < n; row += 1) {
    for (let col = 0; col < n; col += 1) {
      const value = corr.matrix[row][col];
      ctx.fillStyle = heatColor(value);
      roundedRect(ctx, startX + col * size, startY + row * size, size - 4, size - 4, 8);
      ctx.fill();
      ctx.fillStyle = value > .55 ? "#061018" : "#eaf7ff";
      ctx.font = "700 11px Segoe UI, sans-serif";
      ctx.fillText(value.toFixed(2), startX + col * size + 8, startY + row * size + size / 2 + 4);
    }
  }
  labels.forEach((label, index) => {
    axisLabel(ctx, label.replace("_", " "), startX + index * size, startY + n * size + 18);
    ctx.save();
    ctx.translate(startX - 10, startY + index * size + size - 8);
    ctx.rotate(-Math.PI / 2);
    axisLabel(ctx, label.replace("_", " "), 0, 0);
    ctx.restore();
  });
}

function heatColor(value) {
  const amount = Math.max(0, Math.min(1, value));
  const red = Math.round(30 + amount * 50);
  const green = Math.round(75 + amount * 180);
  const blue = Math.round(120 + amount * 100);
  return `rgb(${red}, ${green}, ${blue})`;
}

window.addEventListener("keydown", event => {
  if (["ArrowRight", "ArrowDown", "PageDown", " "].includes(event.key)) {
    event.preventDefault();
    next();
  }
  if (["ArrowLeft", "ArrowUp", "PageUp"].includes(event.key)) {
    event.preventDefault();
    prev();
  }
  if (event.key === "Home") show(0);
  if (event.key === "End") show(slides.length - 1);
  if (event.key.toLowerCase() === "f") toggleFullscreen();
  if (event.key.toLowerCase() === "n") notes.classList.toggle("visible");
});

window.addEventListener("wheel", event => {
  if (wheelLock || Math.abs(event.deltaY) < 18) return;
  wheelLock = true;
  event.deltaY > 0 ? next() : prev();
  setTimeout(() => { wheelLock = false; }, 650);
}, { passive: true });

window.addEventListener("touchstart", event => {
  touchStartX = event.changedTouches[0].clientX;
  touchStartY = event.changedTouches[0].clientY;
}, { passive: true });

window.addEventListener("touchend", event => {
  const dx = event.changedTouches[0].clientX - touchStartX;
  const dy = event.changedTouches[0].clientY - touchStartY;
  if (Math.abs(dx) > 50 && Math.abs(dx) > Math.abs(dy)) dx < 0 ? next() : prev();
}, { passive: true });

document.getElementById("nextBtn").addEventListener("click", next);
document.getElementById("prevBtn").addEventListener("click", prev);
document.getElementById("fullscreenBtn").addEventListener("click", toggleFullscreen);
window.addEventListener("resize", () => {
  fitDeckToViewport();
  drawVisibleCharts();
});

function toggleFullscreen() {
  if (!document.fullscreenElement) document.documentElement.requestFullscreen?.();
  else document.exitFullscreen?.();
}

render();
const hashIndex = slides.findIndex(slide => `#${slide.id}` === window.location.hash);
if (hashIndex >= 0) show(hashIndex, true);
