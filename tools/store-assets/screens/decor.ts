// The drawn extras around the device: sound bubbles, badges and small illustrations. Everything
// here is decoration in the app's own look (peach chips with brown glyphs on the dusk sky); the
// app itself only ever appears through real captures.

export type IconName =
  | 'rain'
  | 'thunder'
  | 'waves'
  | 'fire'
  | 'bird'
  | 'leaf'
  | 'moon'
  | 'sparkle'
  | 'dice'
  | 'lock'
  | 'search'
  | 'plus'
  | 'heart'
  | 'scissors'
  | 'loop'
  | 'file'
  | 'check'
  | 'mixer';

// 24×24 glyphs drawn with currentColor.
const glyphs: Record<IconName, string> = {
  rain: '<path d="M12 3c3.2 4.2 6 7.6 6 11a6 6 0 0 1-12 0c0-3.4 2.8-6.8 6-11z"/>',
  thunder:
    '<path d="M7 16.5a4.5 4.5 0 0 1-.6-8.96A5.5 5.5 0 0 1 17 6.5a4 4 0 0 1 .5 7.98"/><path d="M12.5 12l-2.5 4.5h3.5l-2.5 4.5"/>',
  waves:
    '<path d="M3 7.5c1.5-1.6 3-1.6 4.5 0s3 1.6 4.5 0 3-1.6 4.5 0 3 1.6 4.5 0"/><path d="M3 12c1.5-1.6 3-1.6 4.5 0s3 1.6 4.5 0 3-1.6 4.5 0 3 1.6 4.5 0"/><path d="M3 16.5c1.5-1.6 3-1.6 4.5 0s3 1.6 4.5 0 3-1.6 4.5 0 3 1.6 4.5 0"/>',
  fire: '<path d="M12 2.5c.8 3.4 5.5 5.6 5.5 11a5.5 5.5 0 0 1-11 0c0-2.4 1.1-4 2.3-5 0 2 .9 3.3 2.2 3.3 0-3.2-1-5.6 1-9.3z"/>',
  bird: '<path d="M2.5 13.5c3-1.5 5.2-.6 6.5 1.5.8-4.6 4-7.5 9.5-7.5l3 1.2-3 .8c-.5 5.8-3.6 9-8.5 9-3.5 0-6-1.8-7.5-5z"/><path d="M16 9.6h.01"/>',
  leaf: '<path d="M5 19C5 10.5 11 5 19.5 4.5 19.5 13 14 19 5 19z"/><path d="M5 19l8.5-8.5"/>',
  moon: '<path d="M20 14.6A8.5 8.5 0 1 1 9.4 4a6.8 6.8 0 0 0 10.6 10.6z"/>',
  sparkle: '<path d="M12 2.5c.7 5.2 4.3 8.8 9.5 9.5-5.2.7-8.8 4.3-9.5 9.5-.7-5.2-4.3-8.8-9.5-9.5 5.2-.7 8.8-4.3 9.5-9.5z" fill="currentColor"/>',
  dice: '<rect x="4" y="4" width="16" height="16" rx="3.5"/><circle cx="9" cy="9" r="1.2" fill="currentColor"/><circle cx="15" cy="15" r="1.2" fill="currentColor"/><circle cx="12" cy="12" r="1.2" fill="currentColor"/>',
  lock: '<rect x="5" y="10.5" width="14" height="10" rx="2.5"/><path d="M8.5 10.5V7.5a3.5 3.5 0 0 1 7 0v3"/><path d="M12 14.5v2"/>',
  search: '<circle cx="10.5" cy="10.5" r="6"/><path d="M15 15l5 5"/>',
  plus: '<path d="M12 5v14M5 12h14"/>',
  heart: '<path d="M12 20s-7.5-4.6-7.5-10.2A4.3 4.3 0 0 1 12 7.2a4.3 4.3 0 0 1 7.5 2.6C19.5 15.4 12 20 12 20z"/>',
  scissors:
    '<circle cx="6.5" cy="6.5" r="2.8"/><circle cx="6.5" cy="17.5" r="2.8"/><path d="M8.6 8.3L20 18M8.6 15.7L20 6"/>',
  loop: '<path d="M7.5 8.5a3.5 3.5 0 1 0 0 7c3.5 0 5.5-7 9-7a3.5 3.5 0 1 1 0 7c-3.5 0-5.5-7-9-7z"/>',
  file: '<path d="M14 3H7.5A2.5 2.5 0 0 0 5 5.5v13A2.5 2.5 0 0 0 7.5 21h9a2.5 2.5 0 0 0 2.5-2.5V8z"/><path d="M14 3v5h5"/><path d="M12 17.5v-6M9.2 14.2L12 11.4l2.8 2.8"/>',
  check: '<path d="M5 12.5l4.5 4.5L19 7.5"/>',
  mixer: '<path d="M6 4v16M12 4v16M18 4v16"/><circle cx="6" cy="14" r="2" fill="currentColor"/><circle cx="12" cy="8" r="2" fill="currentColor"/><circle cx="18" cy="16" r="2" fill="currentColor"/>',
};

export function icon(name: IconName, size: number, color: string, stroke: number = 2): string {
  return `<svg width="${size}" height="${size}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="${stroke}" stroke-linecap="round" stroke-linejoin="round" style="color:${color};display:block">${glyphs[name]}</svg>`;
}

interface Placed {
  readonly left: number;
  readonly top: number;
  /** Degrees. */
  readonly rotate?: number;
  /** Drawn behind the device instead of over it. */
  readonly behind?: boolean;
}

export type Art = Placed &
  (
    | { readonly kind: 'bubble'; readonly icon: IconName; readonly size: number; readonly label?: string }
    | { readonly kind: 'badges'; readonly items: ReadonlyArray<string>; readonly size: number; readonly width: number }
    | { readonly kind: 'lanes'; readonly width: number }
    | { readonly kind: 'rain'; readonly width: number; readonly height: number }
    | { readonly kind: 'bolt'; readonly size: number }
    | { readonly kind: 'sparkles'; readonly size: number }
    | { readonly kind: 'moon'; readonly size: number }
    | { readonly kind: 'zzz'; readonly size: number }
    | { readonly kind: 'dice'; readonly size: number; readonly face: 1 | 2 | 3 | 4 | 5 | 6 }
    | { readonly kind: 'notes'; readonly size: number }
    | { readonly kind: 'halo'; readonly size: number }
    /** A dotted arrow; `d` is drawn in a box `width`×`height` placed at left/top. */
    | { readonly kind: 'arrow'; readonly width: number; readonly height: number; readonly d: string }
  );

const INK: string = '#3A2414';
const CREAM: string = '#FFE9D2';

/** Fixed pseudo-random numbers, the same on every run. */
function randoms(seed: number): () => number {
  let s: number = seed;
  return (): number => {
    s = (s * 16807) % 2147483647;
    return s / 2147483647;
  };
}

function place(a: Placed, body: string, extra: string = ''): string {
  const turn: string = a.rotate ? `transform:rotate(${a.rotate}deg);` : '';
  return `<div class="art" style="left:${a.left}px;top:${a.top}px;${turn}${extra}">${body}</div>`;
}

function bubble(iconName: IconName, size: number, label?: string): string {
  const tag: string = label ? `<span class="tag" style="font-size:${Math.round(size * 0.22)}px">${label}</span>` : '';
  return `<div class="bubble"><div class="disc" style="width:${size}px;height:${size}px">${icon(iconName, Math.round(size * 0.5), INK, 2.1)}</div>${tag}</div>`;
}

function badges(items: ReadonlyArray<string>, size: number, width: number): string {
  const pills: string = items
    .map((t: string): string => `<span class="badge" style="font-size:${size}px"><i>${icon('check', Math.round(size * 0.8), INK, 3)}</i>${t}</span>`)
    .join('');
  return `<div class="badges" style="width:${width}px;gap:${Math.round(size * 0.5)}px">${pills}</div>`;
}

/** Steady sounds against the ones that drift in: a strip of level bars for each. */
function lanes(width: number): string {
  const r: () => number = randoms(11);
  const count: number = 30;
  const barsWidth: number = width - 340;
  function bar(heights: ReadonlyArray<number>, bright: ReadonlyArray<boolean>): string {
    const w: number = barsWidth / count;
    return `<svg width="${barsWidth}" height="90" viewBox="0 0 ${barsWidth} 90">${heights
      .map((h: number, i: number): string => {
        const bh: number = Math.max(8, h * 84);
        return `<rect x="${(i * w + w * 0.22).toFixed(1)}" y="${(45 - bh / 2).toFixed(1)}" width="${(w * 0.56).toFixed(1)}" height="${bh.toFixed(1)}" rx="${(w * 0.28).toFixed(1)}" fill="${bright[i] ? '#FFC28A' : '#6B4B33'}"/>`;
      })
      .join('')}</svg>`;
  }
  const steady: number[] = Array.from({ length: count }, (): number => 0.5 + r() * 0.38);
  const bursts: number[] = Array.from({ length: count }, (_: unknown, i: number): number => {
    const d: number = Math.min(Math.abs(i - 7), Math.abs(i - 22));
    return d <= 2 ? [1, 0.75, 0.4][d] * (0.8 + r() * 0.2) : 0;
  });
  function row(iconName: IconName, name: string, sub: string, bars: string): string {
    return `<div class="lane">${bubble(iconName, 76)}<div class="lane-words"><b>${name}</b><span>${sub}</span></div>${bars}</div>`;
  }
  return `<div class="card lanes" style="width:${width}px">
${row('rain', 'Rain', 'all night', bar(steady, steady.map((): boolean => true)))}
${row('thunder', 'Thunder', 'now and then', bar(bursts, bursts.map((h: number): boolean => h > 0)))}
</div>`;
}

function rain(width: number, height: number): string {
  const r: () => number = randoms(5);
  const drops: string[] = [];
  for (let i: number = 0; i < (width * height) / 9000; i++) {
    const x: number = r() * width;
    const y: number = r() * height;
    const len: number = 40 + r() * 70;
    drops.push(
      `<line x1="${x.toFixed(1)}" y1="${y.toFixed(1)}" x2="${(x - len * 0.27).toFixed(1)}" y2="${(y + len).toFixed(1)}" stroke="#FFE3C4" stroke-opacity="${(0.12 + r() * 0.3).toFixed(2)}" stroke-width="${(2 + r() * 1.5).toFixed(1)}" stroke-linecap="round"/>`,
    );
  }
  return `<svg width="${width}" height="${height}" viewBox="0 0 ${width} ${height}">${drops.join('')}</svg>`;
}

function bolt(size: number): string {
  return `<svg width="${size}" height="${size}" viewBox="0 0 24 24" class="glow"><defs><linearGradient id="boltg" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="#FFF1E2"/><stop offset="1" stop-color="#FFB77B"/></linearGradient></defs><path d="M13.5 1.5L4 13.5h6.5L8.5 22.5 19.5 9.5H13z" fill="url(#boltg)"/></svg>`;
}

function sparkles(size: number): string {
  function star(x: number, y: number, s: number, o: number): string {
    return `<path transform="translate(${x} ${y}) scale(${s})" d="M12 0c.9 6.5 5.5 11.1 12 12-6.5.9-11.1 5.5-12 12-.9-6.5-5.5-11.1-12-12 6.5-.9 11.1-5.5 12-12z" fill="#FFE9D2" fill-opacity="${o}"/>`;
  }
  return `<svg width="${size}" height="${size}" viewBox="0 0 100 100" class="glow">${star(30, 22, 2.4, 1)}${star(4, 6, 1.1, 0.85)}${star(10, 66, 0.8, 0.7)}</svg>`;
}

function moon(size: number): string {
  return `<svg width="${size}" height="${size}" viewBox="0 0 100 100" class="moon"><defs>
<linearGradient id="moong" x1="0" y1="0" x2="1" y2="1"><stop offset="0" stop-color="#FFF1E2"/><stop offset="1" stop-color="#FFC28A"/></linearGradient>
<mask id="moonm"><rect width="100" height="100" fill="#fff"/><circle cx="66" cy="34" r="36" fill="#000"/></mask></defs>
<circle cx="50" cy="50" r="44" fill="url(#moong)" mask="url(#moonm)"/></svg>`;
}

function zzz(size: number): string {
  return `<div class="zzz" style="font-size:${size}px"><span>z</span><span>z</span><span>Z</span></div>`;
}

const PIPS: Record<number, ReadonlyArray<readonly [number, number]>> = {
  1: [[1, 1]],
  2: [[0, 0], [2, 2]],
  3: [[0, 0], [1, 1], [2, 2]],
  4: [[0, 0], [2, 0], [0, 2], [2, 2]],
  5: [[0, 0], [2, 0], [1, 1], [0, 2], [2, 2]],
  6: [[0, 0], [2, 0], [0, 1], [2, 1], [0, 2], [2, 2]],
};

function dice(size: number, face: number): string {
  const pips: string = PIPS[face]
    .map(([c, r]: readonly [number, number]): string => `<i style="left:${22 + c * 28}%;top:${22 + r * 28}%"></i>`)
    .join('');
  return `<div class="die" style="width:${size}px;height:${size}px">${pips}</div>`;
}

function notes(size: number): string {
  function note(x: number, y: number, s: number, o: number): string {
    return `<g transform="translate(${x} ${y}) scale(${s})" fill="#FFE3C4" fill-opacity="${o}"><ellipse cx="7" cy="40" rx="8" ry="6" transform="rotate(-20 7 40)"/><rect x="13" y="4" width="3.4" height="37" rx="1.7"/><path d="M13 4c8 2 14 6 14 15-3-4-8-6-14-7z"/></g>`;
  }
  return `<svg width="${size}" height="${size}" viewBox="0 0 100 100" class="glow">${note(8, 40, 1, 0.9)}${note(58, 6, 0.75, 0.6)}${note(66, 62, 0.55, 0.45)}</svg>`;
}

function arrow(width: number, height: number, d: string): string {
  return `<svg width="${width}" height="${height}" viewBox="0 0 ${width} ${height}" style="overflow:visible"><defs><marker id="head" viewBox="0 0 10 10" refX="5" refY="5" markerWidth="5" markerHeight="5" orient="auto-start-reverse"><path d="M0 0L10 5 0 10z" fill="#FFC28A"/></marker></defs><path d="${d}" fill="none" stroke="#FFC28A" stroke-width="7" stroke-linecap="round" stroke-dasharray="0.1 20" marker-end="url(#head)"/></svg>`;
}

export function artHtml(a: Art): string {
  switch (a.kind) {
    case 'bubble':
      return place(a, bubble(a.icon, a.size, a.label));
    case 'badges':
      return place(a, badges(a.items, a.size, a.width));
    case 'lanes':
      return place(a, lanes(a.width));
    case 'rain':
      return place(a, rain(a.width, a.height));
    case 'bolt':
      return place(a, bolt(a.size));
    case 'sparkles':
      return place(a, sparkles(a.size));
    case 'moon':
      return place(a, moon(a.size));
    case 'zzz':
      return place(a, zzz(a.size));
    case 'dice':
      return place(a, dice(a.size, a.face));
    case 'notes':
      return place(a, notes(a.size));
    case 'halo':
      return place(a, '', `width:${a.size}px;height:${a.size}px;border-radius:50%;box-shadow:0 0 70px 18px rgba(255,183,123,.45),inset 0 0 70px 18px rgba(255,183,123,.35);mix-blend-mode:screen`);
    case 'arrow':
      return place(a, arrow(a.width, a.height, a.d));
  }
}

export const decorCss: string = `
.art{position:absolute;transform-origin:center}
.glow{filter:drop-shadow(0 0 24px rgba(255,183,123,.65))}
.bubble{display:flex;flex-direction:column;align-items:center;gap:14px}
.disc{border-radius:50%;display:grid;place-items:center;background:radial-gradient(circle at 34% 28%,#FFF1E2,#FFC28A 48%,#E3884A);
  box-shadow:0 0 0 9px rgba(255,183,123,.18),0 0 0 22px rgba(255,183,123,.07),0 28px 60px rgba(0,0,0,.55),0 0 90px rgba(255,183,123,.45)}
.tag{font-weight:700;color:${CREAM};padding:.25em .8em;border-radius:999px;background:rgba(21,15,11,.82);border:2px solid rgba(255,204,153,.35);white-space:nowrap}
.badges{display:flex;flex-wrap:wrap}
.badge{display:inline-flex;align-items:center;gap:.45em;padding:.42em .95em .42em .5em;border-radius:999px;font-weight:700;color:${CREAM};white-space:nowrap;
  background:linear-gradient(180deg,rgba(255,194,138,.2),rgba(255,194,138,.08));border:2px solid rgba(255,204,153,.45);box-shadow:0 18px 40px rgba(0,0,0,.4)}
.badge i{display:grid;place-items:center;width:1.25em;height:1.25em;border-radius:50%;background:#FFC28A}
.card{border-radius:40px;background:rgba(27,20,15,.97);border:2px solid rgba(255,204,153,.28);box-shadow:0 50px 110px rgba(0,0,0,.65),0 0 100px rgba(255,183,123,.14)}
.lanes{padding:34px 38px;display:flex;flex-direction:column;gap:30px}
.lane{display:flex;align-items:center;gap:24px}
.lane .disc{box-shadow:0 0 30px rgba(255,183,123,.35)}
.lane-words{width:210px;display:flex;flex-direction:column}
.lane-words b{font-size:34px;color:${CREAM}}
.lane-words span{font-size:25px;font-weight:500;color:#C9B19C}
.moon{filter:drop-shadow(0 0 40px rgba(255,183,123,.7)) drop-shadow(0 0 110px rgba(255,183,123,.35))}
.zzz{font-weight:700;color:#FFE3C4;line-height:1;text-shadow:0 0 24px rgba(255,183,123,.7)}
.zzz span{display:inline-block}
.zzz span:nth-child(1){font-size:.5em;opacity:.55;transform:translateY(.9em)}
.zzz span:nth-child(2){font-size:.72em;opacity:.75;transform:translateY(.4em)}
.die{position:relative;border-radius:24%;background:linear-gradient(145deg,#FFF4E8,#FFC896 58%,#E3884A);
  box-shadow:inset -6px -8px 0 rgba(150,80,30,.35),inset 4px 5px 0 rgba(255,255,255,.6),0 30px 60px rgba(0,0,0,.55),0 0 70px rgba(255,183,123,.4)}
.die i{position:absolute;width:16%;height:16%;margin:-8% 0 0 -8%;border-radius:50%;background:${INK};transform:translate(50%,50%)}
`;
