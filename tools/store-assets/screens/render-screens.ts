// Builds the Play screenshots and the feature graphic from the real captures, then renders them
// with headless Chrome into fastlane/metadata/android/en-US/images/.
//
//   node tools/store-assets/screens/render-screens.ts [path to Chrome]
//
// One horizon runs through all eight slides of a device, so the set reads as a panorama in the
// store: the icon's dusk sun, sliced into layers by the same soft waves.

import { execFileSync } from 'node:child_process';
import { copyFileSync, mkdirSync, rmSync, statSync, writeFileSync } from 'node:fs';
import { dirname, join } from 'node:path';
import { fileURLToPath, pathToFileURL } from 'node:url';
import { type Point, band, circle, line, palette, wave } from '../shared/geometry.ts';
import { type Callout, type Slide, phoneSlides, tabletSlides } from './slides.ts';

const here: string = dirname(fileURLToPath(import.meta.url));
const repo: string = join(here, '../../..');
const captures: string = join(here, '../captures');
const out: string = join(here, 'out');
const images: string = join(repo, 'fastlane/metadata/android/en-US/images');
const chrome: string = process.argv[2] ?? '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome';
const fonts: string = join(repo, 'core/ui/src/main/res/font');

interface Device {
  readonly id: 'phone' | 'tablet';
  readonly width: number;
  readonly height: number;
  readonly captureWidth: number;
  readonly captureHeight: number;
  readonly slides: ReadonlyArray<Slide>;
  readonly horizon: Horizon;
  readonly folders: ReadonlyArray<string>;
}

interface Sun {
  readonly cx: number;
  readonly cy: number;
  readonly r: number;
}

/** [centre y, thickness, amplitude, phase] of a wavy gap, in slide pixels. */
type HorizonGap = readonly [number, number, number, number];

interface Horizon {
  readonly gaps: ReadonlyArray<HorizonGap>;
  readonly wavelength: number;
  readonly suns: ReadonlyArray<Sun>;
}

const SEA: ReadonlyArray<string> = ['#2A1C12', '#321F13', '#3B2415', '#452917'];

/** The whole panorama for one device: sky, stars, suns and the sea of layers, `count` slides wide. */
function horizonSvg(width: number, height: number, count: number, h: Horizon, id: string): string {
  const total: number = width * count;
  const edges: Array<{ upper: Point[]; lower: Point[] }> = h.gaps.map(([y, t, amp, phase]: HorizonGap): { upper: Point[]; lower: Point[] } => ({
    upper: wave(y - t / 2, amp, h.wavelength, phase, -50, total + 50),
    lower: wave(y + t / 2, amp, h.wavelength, phase, -50, total + 50),
  }));
  // Layer 0 is the sky above the first gap; the rest are the bands between the gaps.
  const layers: string[] = [];
  for (let i: number = 0; i <= edges.length; i++) {
    const upper: Point[] = i === 0 ? line(-10, -50, total + 50) : edges[i - 1].lower;
    const lower: Point[] = i === edges.length ? line(height + 10, -50, total + 50) : edges[i].upper;
    layers.push(band(upper, lower));
  }
  const seaTop: number = h.gaps[0][0];

  // Stars: fixed pseudo-random dots in the upper sky, the same on every run.
  let seed: number = 7;
  const random = (): number => {
    seed = (seed * 16807) % 2147483647;
    return seed / 2147483647;
  };
  const stars: string[] = [];
  for (let i: number = 0; i < count * 26; i++) {
    const x: number = random() * total;
    const y: number = random() * seaTop * 0.8;
    const r: number = 1.2 + random() * 2.6;
    const o: number = 0.12 + random() * 0.35;
    stars.push(`<circle cx="${x.toFixed(1)}" cy="${y.toFixed(1)}" r="${r.toFixed(2)}" fill="${palette.star}" fill-opacity="${o.toFixed(2)}"/>`);
  }

  const suns: string = h.suns
    .map((s: Sun, k: number): string => {
      const glow: string = `<radialGradient id="${id}g${k}" cx="${s.cx}" cy="${s.cy}" r="${s.r * 2.2}" gradientUnits="userSpaceOnUse"><stop offset="0" stop-color="${palette.glow}" stop-opacity="0.30"/><stop offset="0.5" stop-color="${palette.glow}" stop-opacity="0.07"/><stop offset="1" stop-color="${palette.glow}" stop-opacity="0"/></radialGradient>`;
      const slices: string = layers
        .map((d: string, i: number): string => {
          const [a, z] = palette.layers[Math.min(i, palette.layers.length - 1)];
          return `<linearGradient id="${id}s${k}l${i}" x1="0" y1="${s.cy - s.r}" x2="0" y2="${s.cy + s.r}" gradientUnits="userSpaceOnUse"><stop offset="0" stop-color="${a}"/><stop offset="1" stop-color="${z}"/></linearGradient><path d="${d}" fill="url(#${id}s${k}l${i})" clip-path="url(#${id}c${k})"/>`;
        })
        .join('');
      return `<defs>${glow}<clipPath id="${id}c${k}"><path d="${circle(s.cx, s.cy, s.r)}"/></clipPath></defs><rect x="${s.cx - s.r * 2.2}" y="0" width="${s.r * 4.4}" height="${height}" fill="url(#${id}g${k})"/>${slices}`;
    })
    .join('');

  const sea: string = layers
    .slice(1)
    .map((d: string, i: number): string => `<path d="${d}" fill="${SEA[Math.min(i, SEA.length - 1)]}"/>`)
    .join('');

  return `<svg xmlns="http://www.w3.org/2000/svg" width="${total}" height="${height}" viewBox="0 0 ${total} ${height}">
<defs><linearGradient id="${id}sky" x1="0" y1="0" x2="0" y2="${height}" gradientUnits="userSpaceOnUse">
<stop offset="0" stop-color="${palette.skyTop}"/><stop offset="${(seaTop / height).toFixed(3)}" stop-color="#18120E"/><stop offset="1" stop-color="${palette.skyBottom}"/>
</linearGradient></defs>
<rect width="${total}" height="${height}" fill="url(#${id}sky)"/>
${stars.join('')}
<rect x="-50" y="${seaTop}" width="${total + 100}" height="${height - seaTop}" fill="#100C09"/>
${sea}
${suns}
</svg>`;
}

const weights: ReadonlyArray<readonly [string, number]> = [
  ['regular', 400],
  ['medium', 500],
  ['semibold', 600],
  ['bold', 700],
];

const fontFaces: string = weights
  .map(([file, weight]: readonly [string, number]): string => `@font-face{font-family:Nunito;font-weight:${weight};src:url("${pathToFileURL(join(fonts, `nunito_${file}.ttf`)).href}")}`)
  .join('\n');

const captureUrl = (src: string): string => pathToFileURL(join(captures, src)).href;

function calloutHtml(c: Callout, captureWidth: number): string {
  const scale: number = c.shownWidth / c.width;
  return `<div class="callout" style="left:${c.left}px;top:${c.top}px;width:${c.shownWidth}px;height:${Math.round(c.height * scale)}px">
<img src="${captureUrl(c.src)}" style="width:${captureWidth * scale}px;left:${-c.x * scale}px;top:${-c.y * scale}px"></div>`;
}

function slideHtml(device: Device, slide: Slide, index: number, horizon: string): string {
  const calloutCaptureWidth: number = slide.callout?.src.startsWith('phone/') ? 1080 : device.captureWidth;
  return `<!doctype html><html><head><meta charset="utf-8"><title>${slide.name}</title><style>
${fontFaces}
${device.id === 'phone' ? phoneCss : tabletCss}
.horizon{position:absolute;top:0;left:${-index * device.width}px}
</style></head><body>
<div class="slide${slide.hanging ? ' hanging' : ''}">
<div class="horizon">${horizon}</div>
<div class="words"><h1>${slide.title}</h1><p>${slide.body}</p></div>
<div class="device"><img src="${captureUrl(slide.capture)}"></div>
${slide.callout ? calloutHtml(slide.callout, calloutCaptureWidth) : ''}
</div></body></html>`;
}

const baseCss: string = `
*{box-sizing:border-box}
html,body{margin:0;background:${palette.night}}
.slide{position:relative;overflow:hidden;background:${palette.night};font-family:Nunito,sans-serif;-webkit-font-smoothing:antialiased}
.words{position:absolute}
h1{margin:0;font-weight:700;color:#FFE9D2;letter-spacing:-0.018em;text-wrap:balance}
p{margin:0;font-weight:500;color:#D9C7B6;text-wrap:pretty}
.device{position:absolute;background:#0B0907;box-shadow:0 0 0 2px #3B2F26,0 0 0 3px #0B0907,0 60px 140px rgba(0,0,0,.65),0 0 220px rgba(255,183,123,.10)}
.device img{display:block;width:100%}
.callout{position:absolute;overflow:hidden;box-shadow:0 0 0 3px rgba(255,204,153,.75),0 40px 90px rgba(0,0,0,.7)}
.callout img{position:absolute;max-width:none}
`;

// Phone: 1080×1920. The device is 780 wide; its screen keeps the capture's 9:20.
const phoneCss: string = `${baseCss}
.slide{width:1080px;height:1920px}
.words{left:88px;right:88px;top:148px}
.hanging .words{top:auto;bottom:120px}
h1{font-size:100px;line-height:1.04}
p{font-size:44px;line-height:1.34;margin-top:30px;max-width:880px}
.device{left:150px;width:780px;top:650px;padding:16px;border-radius:72px}
.device img{border-radius:56px}
.hanging .device{top:${1300 - (32 + Math.round((748 * 2400) / 1080))}px}
.callout{border-radius:40px}
`;

// Tablet: 2560×1440, words on the left and the device on the right.
const tabletCss: string = `${baseCss}
.slide{width:2560px;height:1440px}
.words{left:150px;width:760px;top:330px}
h1{font-size:112px;line-height:1.04}
p{font-size:44px;line-height:1.36;margin-top:36px}
.device{left:980px;top:236px;width:1480px;padding:18px;border-radius:46px}
.device img{border-radius:30px}
.callout{border-radius:36px}
`;

const phone: Device = {
  id: 'phone',
  width: 1080,
  height: 1920,
  captureWidth: 1080,
  captureHeight: 2400,
  slides: phoneSlides,
  folders: ['phoneScreenshots'],
  horizon: {
    gaps: [
      [1250, 12, 22, 0.1],
      [1390, 14, 26, 0.45],
      [1560, 16, 28, 0.8],
      [1750, 18, 30, 0.25],
    ],
    wavelength: 1150,
    suns: [
      // Rising behind the first phone; then one sun on every second seam.
      { cx: 540, cy: 1040, r: 460 },
      { cx: 1080 * 3, cy: 1200, r: 330 },
      { cx: 1080 * 5, cy: 1200, r: 330 },
      { cx: 1080 * 7, cy: 1200, r: 330 },
    ],
  },
};

const tablet: Device = {
  id: 'tablet',
  width: 2560,
  height: 1440,
  captureWidth: 2560,
  captureHeight: 1600,
  slides: tabletSlides,
  folders: ['sevenInchScreenshots', 'tenInchScreenshots'],
  horizon: {
    gaps: [
      [1110, 12, 20, 0.1],
      [1210, 14, 24, 0.45],
      [1325, 16, 26, 0.8],
    ],
    wavelength: 1700,
    suns: [
      { cx: 2560 * 1, cy: 1190, r: 360 },
      { cx: 2560 * 3, cy: 1190, r: 360 },
      { cx: 2560 * 5, cy: 1190, r: 360 },
      { cx: 2560 * 7, cy: 1190, r: 360 },
    ],
  },
};

function featureHtml(): string {
  const width: number = 1024;
  const height: number = 500;
  const horizon: string = horizonSvg(width, height, 1, {
    gaps: [
      [318, 7, 9, 0.15],
      [372, 8, 10, 0.5],
      [434, 9, 11, 0.85],
    ],
    wavelength: 560,
    suns: [{ cx: 770, cy: 300, r: 168 }],
  }, 'f');
  return `<!doctype html><html><head><meta charset="utf-8"><title>Feature graphic</title><style>
${fontFaces}
*{box-sizing:border-box}html,body{margin:0}
.slide{position:relative;width:${width}px;height:${height}px;overflow:hidden;font-family:Nunito,sans-serif;-webkit-font-smoothing:antialiased}
.horizon{position:absolute;inset:0}
.words{position:absolute;left:84px;top:150px;width:470px}
h1{margin:0;font-weight:700;font-size:76px;line-height:1;letter-spacing:-0.02em;color:#FFE9D2}
p{margin:22px 0 0;font-weight:500;font-size:30px;line-height:1.3;color:#D9C7B6}
</style></head><body><div class="slide"><div class="horizon">${horizon}</div>
<div class="words"><h1>JustRelax</h1><p>Layer sounds into your own calm.</p></div></div></body></html>`;
}

/** A review page with every slide side by side, the way the store lines them up. */
function sheetHtml(device: Device, files: ReadonlyArray<string>): string {
  const imgs: string = files.map((f: string): string => `<img src="${pathToFileURL(f).href}">`).join('');
  const h: number = device.id === 'phone' ? 640 : 360;
  return `<!doctype html><body style="margin:0;padding:24px;background:#fff;display:flex;gap:16px">
<style>img{height:${h}px;border-radius:12px}</style>${imgs}</body>`;
}

function render(url: string, width: number, height: number, file: string): void {
  mkdirSync(dirname(file), { recursive: true });
  execFileSync(chrome, [
    '--headless',
    '--disable-gpu',
    '--hide-scrollbars',
    '--allow-file-access-from-files',
    '--force-device-scale-factor=1',
    '--virtual-time-budget=3000',
    `--window-size=${width},${height}`,
    `--screenshot=${file}`,
    url,
  ], { stdio: 'ignore' });
  const size: number = statSync(file).size;
  if (size > 8 * 1024 * 1024) throw new Error(`${file} is ${size} bytes; Play allows 8 MB`);
}

function page(name: string, html: string): string {
  const file: string = join(out, `${name}.html`);
  writeFileSync(file, html);
  return pathToFileURL(file).href;
}

rmSync(out, { recursive: true, force: true });
mkdirSync(out, { recursive: true });

const devices: ReadonlyArray<Device> = [phone, tablet];
for (const device of devices) {
  const horizon: string = horizonSvg(device.width, device.height, device.slides.length, device.horizon, device.id[0]);
  const rendered: string[] = [];
  device.slides.forEach((slide: Slide, i: number): void => {
    const url: string = page(`${device.id}-${slide.name}`, slideHtml(device, slide, i, horizon));
    const first: string = join(images, device.folders[0], `${slide.name}.png`);
    render(url, device.width, device.height, first);
    // The 7- and 10-inch tablet sections take the same pictures.
    for (const folder of device.folders.slice(1)) {
      const copy: string = join(images, folder, `${slide.name}.png`);
      mkdirSync(dirname(copy), { recursive: true });
      copyFileSync(first, copy);
    }
    rendered.push(first);
  });
  const sheet: string = page(`${device.id}-sheet`, sheetHtml(device, rendered));
  const sheetWidth: number = device.id === 'phone' ? 8 * 376 + 48 : 8 * 656 + 48;
  render(sheet, sheetWidth, device.id === 'phone' ? 690 : 410, join(out, `${device.id}-sheet.png`));
  console.log(`${device.id}: ${rendered.length} slides`);
}

render(page('feature', featureHtml()), 1024, 500, join(images, 'featureGraphic.png'));
console.log('feature graphic done');
