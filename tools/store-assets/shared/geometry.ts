// The shapes and colours the icon and the store artwork share: a dusk sky, and soft waves that
// slice a sun into layers (the layers of a scene).

export interface Point {
  readonly x: number;
  readonly y: number;
}

export interface Palette {
  readonly night: string;
  readonly skyTop: string;
  readonly skyBottom: string;
  readonly glow: string;
  readonly star: string;
  /** Top layer to bottom layer, each a [top, bottom] gradient: moonlight peach down to the app's brown. */
  readonly layers: ReadonlyArray<readonly [string, string]>;
}

export const palette: Palette = {
  night: '#15120F',
  skyTop: '#2B1E15',
  skyBottom: '#120E0B',
  glow: '#FFB77B',
  star: '#FFE3C4',
  layers: [
    ['#FFE3C4', '#FFCC99'],
    ['#FFC28A', '#F6A86A'],
    ['#E98F4E', '#D57A3C'],
    ['#B8662C', '#9A5420'],
  ],
};

/** Points along a sine wave from `from` to `to`; `phase` is in turns. */
export function wave(y0: number, amp: number, length: number, phase: number, from: number, to: number): Point[] {
  const points: Point[] = [];
  const step: number = length / 16;
  for (let x: number = from; x <= to + 1e-6; x += step) {
    points.push({ x, y: y0 + amp * Math.sin((2 * Math.PI * (x - from)) / length + phase * 2 * Math.PI) });
  }
  return points;
}

export function line(y: number, from: number, to: number): Point[] {
  return [{ x: from, y }, { x: to, y }];
}

const f = (n: number): number => +n.toFixed(3);

/** A smooth path through the points (Catmull-Rom as cubic Béziers); `move` starts a new subpath. */
export function curve(points: ReadonlyArray<Point>, move: boolean): string {
  const start: Point = points[0];
  let d: string = `${move ? 'M' : 'L'}${f(start.x)},${f(start.y)}`;
  if (points.length === 2) return `${d}L${f(points[1].x)},${f(points[1].y)}`;
  for (let i: number = 0; i < points.length - 1; i++) {
    const p0: Point = points[Math.max(i - 1, 0)];
    const p1: Point = points[i];
    const p2: Point = points[i + 1];
    const p3: Point = points[Math.min(i + 2, points.length - 1)];
    const c1: Point = { x: p1.x + (p2.x - p0.x) / 6, y: p1.y + (p2.y - p0.y) / 6 };
    const c2: Point = { x: p2.x - (p3.x - p1.x) / 6, y: p2.y - (p3.y - p1.y) / 6 };
    d += `C${f(c1.x)},${f(c1.y)} ${f(c2.x)},${f(c2.y)} ${f(p2.x)},${f(p2.y)}`;
  }
  return d;
}

/** The closed shape between an upper and a lower edge, both running left to right. */
export function band(upper: ReadonlyArray<Point>, lower: ReadonlyArray<Point>): string {
  return `${curve(upper, true)}${curve([...lower].reverse(), false)}Z`;
}

export function circle(cx: number, cy: number, r: number): string {
  return `M${cx - r},${cy}A${r},${r} 0 1,1 ${cx + r},${cy}A${r},${r} 0 1,1 ${cx - r},${cy}Z`;
}

/** [centre y, thickness, amplitude, phase]: y, thickness and amplitude are fractions of the radius. */
export type Gap = readonly [number, number, number, number];

export interface MarkSpec {
  readonly cx: number;
  readonly cy: number;
  readonly r: number;
  /** Wave length as a fraction of the radius. */
  readonly wavelength: number;
  readonly gaps: ReadonlyArray<Gap>;
}

export interface Band {
  readonly path: string;
  readonly y0: number;
  readonly y1: number;
}

export interface Mark {
  readonly cx: number;
  readonly cy: number;
  readonly r: number;
  readonly bands: ReadonlyArray<Band>;
  readonly clip: string;
}

/** The mark: a disk cut by wavy gaps into stacked layers. */
export function markGeometry({ cx, cy, r, gaps, wavelength }: MarkSpec): Mark {
  const top: number = cy - r - 1;
  const bottom: number = cy + r + 1;
  const left: number = cx - r - 2;
  const right: number = cx + r + 2;
  const edges: Array<{ upper: Point[]; lower: Point[] }> = gaps.map(([y, thickness, amp, phase]: Gap) => ({
    upper: wave(cy + y * r - (thickness * r) / 2, amp * r, wavelength * r, phase, left, right),
    lower: wave(cy + y * r + (thickness * r) / 2, amp * r, wavelength * r, phase, left, right),
  }));
  const bands: Band[] = [];
  for (let i: number = 0; i <= edges.length; i++) {
    const upper: Point[] = i === 0 ? line(top, left, right) : edges[i - 1].lower;
    const lower: Point[] = i === edges.length ? line(bottom, left, right) : edges[i].upper;
    bands.push({ path: band(upper, lower), y0: upper[0].y, y1: lower[0].y });
  }
  return { cx, cy, r, bands, clip: circle(cx, cy, r) };
}

/** The mark as SVG; `id` keeps gradient ids unique when several marks share a document. */
export function svgMark(mark: Mark, id: string, mono?: string): string {
  const grads: string = mark.bands
    .map((b: Band, i: number): string => {
      const [a, z] = palette.layers[i];
      return `<linearGradient id="${id}l${i}" x1="0" y1="${b.y0}" x2="0" y2="${b.y1}" gradientUnits="userSpaceOnUse"><stop offset="0" stop-color="${a}"/><stop offset="1" stop-color="${z}"/></linearGradient>`;
    })
    .join('');
  const paths: string = mark.bands
    .map((b: Band, i: number): string => `<path d="${b.path}" fill="${mono ?? `url(#${id}l${i})`}"/>`)
    .join('');
  return `<defs>${mono ? '' : grads}<clipPath id="${id}disk"><path d="${mark.clip}"/></clipPath></defs><g clip-path="url(#${id}disk)">${paths}</g>`;
}
