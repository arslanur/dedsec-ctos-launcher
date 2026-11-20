import puppeteer from 'puppeteer';
import { resolve } from 'path';
import { fileURLToPath } from 'url';
import { dirname } from 'path';

const __dirname = dirname(fileURLToPath(import.meta.url));
const url = 'file:///' + resolve(__dirname, 'mockup.html').replace(/\\/g, '/');

const browser = await puppeteer.launch({
  headless: true,
  args: ['--no-sandbox', '--disable-setuid-sandbox', '--font-render-hinting=none']
});

const page = await browser.newPage();

// Full preview — all 3 phones side by side
await page.setViewport({ width: 1600, height: 900, deviceScaleFactor: 2 });
await page.goto(url, { waitUntil: 'networkidle0' });
await new Promise(r => setTimeout(r, 2500)); // let animations settle

await page.screenshot({ path: resolve(__dirname, 'all-screens.png'), fullPage: true });
console.log('✓ all-screens.png');

// Individual phone crops — locate each phone frame
const phones = await page.$$('.phone');

const labels = ['01-boot-sequence', '02-home-screen', '03-app-drawer', '04-wallpaper-picker'];
for (let i = 0; i < phones.length; i++) {
  const box = await phones[i].boundingBox();
  if (!box) continue;
  const pad = 20;
  await page.screenshot({
    path: resolve(__dirname, `${labels[i]}.png`),
    clip: {
      x: Math.max(0, (box.x - pad) * 2) / 2,
      y: Math.max(0, (box.y - pad) * 2) / 2,
      width:  (box.width  + pad * 2),
      height: (box.height + pad * 2)
    }
  });
  console.log(`✓ ${labels[i]}.png`);
}

await browser.close();
