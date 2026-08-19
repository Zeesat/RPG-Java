const path = require('path');
const puppeteer = require('puppeteer');

(async () => {
    const browser = await puppeteer.launch({
        executablePath:
            'C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe',
        headless: true
    });

    const page = await browser.newPage();

    const filePath = 'file://' +
        path.resolve(__dirname, 'index.html')
            .replace(/\\/g, '/');

    await page.goto(
        filePath + '?print-pdf',
        {
            waitUntil: 'networkidle0'
        }
    );

    // Render every slide at least once so charts/counters are painted.
    await page.evaluate(async () => {
        const totalSlides = window.DECK_DATA?.content?.slides?.length || 0;
        const showSlide = typeof window.show === 'function' ? window.show : null;
        if (showSlide) {
            for (let i = 0; i < totalSlides; i += 1) {
                showSlide(i, true);
                await new Promise(resolve => requestAnimationFrame(resolve));
            }
        }
    });

    // Force "all slides" print layout: one slide per page.
    await page.addStyleTag({
        content: `
          @page { size: A4 landscape; margin: 0; }
          html, body { width: auto !important; height: auto !important; overflow: visible !important; }
          body::before, body::after { display: none !important; }
          .hud, .fullscreen-btn, .speaker-notes { display: none !important; }
          .deck {
            position: static !important;
            width: 100% !important;
            height: auto !important;
            transform: none !important;
            perspective: none !important;
          }
          .slide {
            position: relative !important;
            inset: auto !important;
            width: 100% !important;
            height: 100vh !important;
            min-height: 100vh !important;
            opacity: 1 !important;
            transform: none !important;
            filter: none !important;
            page-break-after: always !important;
            break-after: page !important;
            pointer-events: auto !important;
          }
          .slide:last-child {
            page-break-after: auto !important;
            break-after: auto !important;
          }
          .slide-content {
            width: min(1456px, calc(100% - 80px)) !important;
          }
          .stagger > *,
          .animate-in {
            opacity: 1 !important;
            animation: none !important;
            transform: none !important;
          }
        `
    });

    await page.pdf({
        path: 'output.pdf',
        format: 'A4',
        landscape: true,
        printBackground: true
    });

    await browser.close();
})();
