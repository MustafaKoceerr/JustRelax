// What each Play screenshot says and shows, in store order. Every picture of the app is a real
// capture from ../captures, and every claim is something the app does. Around it, each slide
// lifts the part that tells the story out of the screen and dresses it with drawn extras.

import type { Art, IconName } from './decor.ts';

/** A part of a capture, in the capture's pixels, lifted out of the device and enlarged. */
export interface Popout {
  readonly src: string;
  readonly x: number;
  readonly y: number;
  readonly width: number;
  readonly height: number;
  /** Width on the slide, in slide pixels. */
  readonly shownWidth: number;
  /** Where its top-left corner sits on the slide. */
  readonly left: number;
  readonly top: number;
  /** Degrees. */
  readonly rotate?: number;
  /** A small label hanging off its bottom edge, or its top edge when `above`. */
  readonly label?: { readonly icon: IconName; readonly text: string; readonly above?: boolean };
}

export interface Slide {
  readonly name: string;
  readonly eyebrow: { readonly icon: IconName; readonly text: string };
  /** The part in *stars* is drawn in the sunset gradient. */
  readonly title: string;
  readonly body: string;
  readonly capture: string;
  /** Phone only: the device hangs from the top and the words sit below it. */
  readonly hanging?: boolean;
  /** Parts of the capture kept bright while the rest of the screen dims, in capture pixels. */
  readonly spotlights?: ReadonlyArray<readonly [number, number, number, number]>;
  readonly popouts?: ReadonlyArray<Popout>;
  readonly art?: ReadonlyArray<Art>;
}

interface Story {
  readonly name: string;
  readonly eyebrow: { readonly icon: IconName; readonly text: string };
  readonly title: string;
  readonly body: string;
}

const stories: ReadonlyArray<Story> = [
  {
    name: '1_layers',
    eyebrow: { icon: 'mixer', text: 'Sound mixer' },
    title: 'Your calm, *layer by layer*',
    body: 'Mix rain, waves, fire and birds into one soundscape.',
  },
  {
    name: '2_now_and_then',
    eyebrow: { icon: 'thunder', text: 'Now and then' },
    title: 'Rain all night. Thunder *now and then.*',
    body: 'Steady sounds keep going. Choose how often the rest drift in.',
  },
  {
    name: '3_ai',
    eyebrow: { icon: 'sparkle', text: 'AI scenes' },
    title: 'Describe a moment. *Hear it.*',
    body: 'Tell the AI a place or a feeling and it builds a scene from your sounds.',
  },
  {
    name: '4_sleep',
    eyebrow: { icon: 'moon', text: 'Sleep timer' },
    title: 'Drift off. It fades out *gently.*',
    body: 'Set a sleep timer. The sounds keep playing with the screen off.',
  },
  {
    name: '5_discover',
    eyebrow: { icon: 'search', text: 'Freesound.org' },
    title: 'Discover *free sounds*',
    body: 'Search Freesound.org and add what you find to your library.',
  },
  {
    name: '6_shuffle',
    eyebrow: { icon: 'dice', text: 'Shuffle' },
    title: 'Can’t decide? *Shuffle.*',
    body: 'Lock the sounds you love and let chance pick the rest.',
  },
  {
    name: '7_your_sounds',
    eyebrow: { icon: 'file', text: 'Your sounds' },
    title: 'Bring your *own sounds*',
    body: 'Import a file or a Freesound find, trim it and loop it.',
  },
  {
    name: '8_saved',
    eyebrow: { icon: 'heart', text: 'Saved mixes' },
    title: 'Keep the mixes *you love*',
    body: 'No ads and no subscription. Your sounds play offline.',
  },
];

const P: string = 'phone/';
const T: string = 'tablet/';
const TRUST: ReadonlyArray<string> = ['No ads', 'No subscription', 'Plays offline'];

// Phone slides are 1080×1920; phone captures are 1080×2400. The device's screen starts at
// (166, 666) and shows the capture at 0.6926×, so a capture point (x, y) lands at
// (166 + 0.6926x, 666 + 0.6926y).
export const phoneSlides: ReadonlyArray<Slide> = [
  {
    ...stories[0],
    capture: `${P}scene.png`,
    // Three sound rows fan out of the list like layers.
    popouts: [
      { src: `${P}scene.png`, x: 43, y: 771, width: 994, height: 270, shownWidth: 740, left: 60, top: 1185, rotate: -4 },
      { src: `${P}scene.png`, x: 43, y: 1071, width: 994, height: 270, shownWidth: 740, left: 190, top: 1395, rotate: -1 },
      { src: `${P}scene.png`, x: 43, y: 1380, width: 994, height: 270, shownWidth: 740, left: 320, top: 1610, rotate: 2.5 },
    ],
    art: [
      { kind: 'bubble', icon: 'rain', label: 'Rain', size: 132, left: 30, top: 690 },
      { kind: 'bubble', icon: 'fire', label: 'Fire', size: 120, left: 910, top: 760, rotate: 6 },
      { kind: 'bubble', icon: 'bird', label: 'Birds', size: 104, left: 930, top: 1040 },
      { kind: 'bubble', icon: 'waves', label: 'Waves', size: 104, left: 40, top: 1640, rotate: -6 },
    ],
  },
  {
    ...stories[1],
    capture: `${P}scene-how-often.png`,
    hanging: true,
    spotlights: [[222, 1236, 292, 424]],
    // The How often menu under Thunder.
    popouts: [{ src: `${P}scene-how-often.png`, x: 222, y: 1236, width: 292, height: 424, shownWidth: 330, left: 256, top: 325, rotate: 3 }],
    art: [
      { kind: 'rain', width: 1080, height: 1200, left: 0, top: 0, behind: true },
      { kind: 'bolt', size: 220, left: 720, top: 420, rotate: 14 },
      { kind: 'lanes', width: 800, left: 140, top: 880, rotate: -2 },
    ],
  },
  {
    ...stories[2],
    capture: `${P}ai.png`,
    spotlights: [[43, 249, 994, 380], [43, 1641, 994, 146]],
    popouts: [
      { src: `${P}ai.png`, x: 43, y: 249, width: 994, height: 380, shownWidth: 840, left: 120, top: 800, rotate: -2 },
      { src: `${P}ai.png`, x: 43, y: 1641, width: 994, height: 146, shownWidth: 900, left: 90, top: 1772, rotate: 1.5 },
    ],
    art: [
      { kind: 'arrow', width: 160, height: 640, left: 880, top: 1130, d: 'M80 620 C 190 460, 190 200, 60 20' },
      { kind: 'sparkles', size: 170, left: 880, top: 690 },
      { kind: 'sparkles', size: 110, left: 30, top: 1490, rotate: 40 },
    ],
  },
  {
    ...stories[3],
    capture: `${P}timer.png`,
    popouts: [
      {
        src: `${P}notification.png`, x: 43, y: 485, width: 994, height: 480, shownWidth: 740, left: 170, top: 830, rotate: -2,
        label: { icon: 'lock', text: 'Plays with the screen off', above: true },
      },
    ],
    art: [
      { kind: 'moon', size: 250, left: 820, top: 520, rotate: -10, behind: true },
      { kind: 'zzz', size: 110, left: 900, top: 1240 },
      { kind: 'halo', size: 590, left: 245, top: 1180 },
    ],
  },
  {
    ...stories[4],
    capture: `${P}discover.png`,
    // The search, and the result that is playing as a preview.
    spotlights: [[43, 257, 994, 137], [43, 858, 994, 140]],
    popouts: [
      { src: `${P}discover.png`, x: 43, y: 257, width: 994, height: 137, shownWidth: 960, left: 60, top: 830, rotate: -2 },
      { src: `${P}discover.png`, x: 43, y: 858, width: 994, height: 140, shownWidth: 900, left: 90, top: 1245, rotate: 2, label: { icon: 'plus', text: 'Add to your library' } },
    ],
    art: [
      { kind: 'notes', size: 190, left: 860, top: 1460 },
      { kind: 'notes', size: 120, left: 30, top: 1040, rotate: -12 },
    ],
  },
  {
    ...stories[5],
    capture: `${P}mixer.png`,
    spotlights: [[43, 548, 994, 146], [43, 973, 994, 270]],
    popouts: [
      { src: `${P}mixer.png`, x: 43, y: 548, width: 994, height: 146, shownWidth: 900, left: 90, top: 1031, rotate: -2 },
      { src: `${P}mixer.png`, x: 43, y: 973, width: 994, height: 270, shownWidth: 800, left: 140, top: 1325, rotate: 1.5, label: { icon: 'lock', text: 'Locked sounds stay' } },
    ],
    art: [
      { kind: 'dice', size: 140, face: 5, left: 40, top: 800, rotate: -20 },
      { kind: 'dice', size: 112, face: 3, left: 910, top: 1180, rotate: 22 },
    ],
  },
  {
    ...stories[6],
    capture: `${P}editor.png`,
    spotlights: [[0, 330, 1080, 400]],
    // The waveform with its trim handles.
    popouts: [{ src: `${P}editor.png`, x: 0, y: 330, width: 1080, height: 400, shownWidth: 980, left: 50, top: 852, rotate: -1.5 }],
    art: [
      { kind: 'bubble', icon: 'file', label: 'Import', size: 120, left: 30, top: 660, rotate: -6 },
      { kind: 'bubble', icon: 'scissors', label: 'Trim', size: 112, left: 900, top: 1250, rotate: 8 },
      { kind: 'bubble', icon: 'loop', label: 'Loop', size: 112, left: 30, top: 1330, rotate: -4 },
    ],
  },
  {
    ...stories[7],
    capture: `${P}saved.png`,
    spotlights: [[43, 1050, 994, 225]],
    popouts: [{ src: `${P}saved.png`, x: 43, y: 1050, width: 994, height: 225, shownWidth: 900, left: 70, top: 1369, rotate: -1.5 }],
    art: [
      { kind: 'bubble', icon: 'heart', size: 116, left: 900, top: 1270, rotate: 10 },
      { kind: 'badges', items: TRUST, size: 34, width: 1000, left: 70, top: 1680 },
    ],
  },
];

// Tablet slides are 2560×1440; tablet captures are 2560×1600. The device's screen starts at
// (998, 254) and shows the capture at 0.5641×.
export const tabletSlides: ReadonlyArray<Slide> = [
  {
    ...stories[0],
    capture: `${T}home.png`,
    spotlights: [[1847, 846, 663, 214]],
    popouts: [{ src: `${T}home.png`, x: 1847, y: 846, width: 663, height: 214, shownWidth: 560, left: 1947, top: 700, rotate: -3 }],
    art: [
      { kind: 'bubble', icon: 'rain', label: 'Rain', size: 124, left: 150, top: 930 },
      { kind: 'bubble', icon: 'thunder', label: 'Thunder', size: 110, left: 340, top: 1010, rotate: 5 },
      { kind: 'bubble', icon: 'waves', label: 'Waves', size: 116, left: 530, top: 920, rotate: -5 },
      { kind: 'bubble', icon: 'fire', label: 'Fire', size: 108, left: 720, top: 1010 },
    ],
  },
  {
    ...stories[1],
    capture: `${T}scene-how-often.png`,
    spotlights: [[1983, 866, 226, 324]],
    popouts: [{ src: `${T}scene-how-often.png`, x: 1983, y: 866, width: 226, height: 324, shownWidth: 300, left: 2031, top: 619, rotate: 3 }],
    art: [
      { kind: 'rain', width: 2560, height: 400, left: 0, top: 0, behind: true },
      { kind: 'bolt', size: 170, left: 2350, top: 500, rotate: 14 },
      { kind: 'lanes', width: 780, left: 130, top: 960, rotate: -2 },
    ],
  },
  {
    ...stories[2],
    capture: `${T}ai.png`,
    spotlights: [[229, 1083, 731, 165], [1028, 215, 1509, 435]],
    popouts: [
      { src: `${T}ai.png`, x: 229, y: 1083, width: 731, height: 165, shownWidth: 640, left: 1013, top: 839, rotate: -2 },
      { src: `${T}ai.png`, x: 1028, y: 215, width: 1509, height: 435, shownWidth: 1000, left: 1503, top: 354, rotate: 1.5 },
    ],
    art: [
      // The sounds the AI picked for this scene.
      { kind: 'bubble', icon: 'fire', label: 'Campfire', size: 112, left: 160, top: 920 },
      { kind: 'bubble', icon: 'thunder', label: 'Thunder', size: 112, left: 390, top: 980, rotate: 5 },
      { kind: 'bubble', icon: 'rain', label: 'Light Rain', size: 112, left: 620, top: 920, rotate: -4 },
      { kind: 'arrow', width: 200, height: 200, left: 1560, top: 640, d: 'M60 190 C 110 140, 120 90, 100 20' },
      { kind: 'sparkles', size: 150, left: 1700, top: 690 },
    ],
  },
  {
    ...stories[3],
    capture: `${T}timer.png`,
    popouts: [
      {
        src: `${T}notification.png`, x: 81, y: 896, width: 1118, height: 367, shownWidth: 760, left: 150, top: 1030, rotate: -2,
        label: { icon: 'lock', text: 'Plays with the screen off', above: true },
      },
    ],
    art: [
      { kind: 'moon', size: 240, left: 2290, top: 40, rotate: -10, behind: true },
      { kind: 'zzz', size: 110, left: 2200, top: 300 },
      { kind: 'halo', size: 520, left: 1460, top: 432 },
    ],
  },
  {
    ...stories[4],
    capture: `${T}discover.png`,
    spotlights: [[686, 206, 1440, 137], [238, 380, 745, 114]],
    popouts: [
      { src: `${T}discover.png`, x: 686, y: 206, width: 1440, height: 137, shownWidth: 980, left: 110, top: 900, rotate: -2 },
      { src: `${T}discover.png`, x: 238, y: 380, width: 745, height: 114, shownWidth: 720, left: 190, top: 1110, rotate: 2, label: { icon: 'plus', text: 'Add to your library' } },
    ],
    art: [{ kind: 'notes', size: 170, left: 800, top: 1230 }],
  },
  {
    ...stories[5],
    capture: `${T}mixer.png`,
    spotlights: [[242, 471, 622, 114], [946, 384, 782, 197]],
    popouts: [
      { src: `${T}mixer.png`, x: 242, y: 471, width: 622, height: 114, shownWidth: 660, left: 160, top: 930, rotate: -2 },
      { src: `${T}mixer.png`, x: 946, y: 384, width: 782, height: 197, shownWidth: 700, left: 150, top: 1140, rotate: 1.5, label: { icon: 'lock', text: 'Locked sounds stay' } },
    ],
    art: [
      { kind: 'dice', size: 120, face: 5, left: 80, top: 830, rotate: -20 },
      { kind: 'dice', size: 100, face: 3, left: 840, top: 1050, rotate: 22 },
    ],
  },
  {
    ...stories[6],
    capture: `${T}editor.png`,
    spotlights: [[560, 270, 1440, 320]],
    popouts: [{ src: `${T}editor.png`, x: 560, y: 270, width: 1440, height: 320, shownWidth: 1200, left: 1180, top: 1060, rotate: -1.5 }],
    art: [
      { kind: 'bubble', icon: 'file', label: 'Import', size: 120, left: 160, top: 960, rotate: -6 },
      { kind: 'bubble', icon: 'scissors', label: 'Trim', size: 112, left: 400, top: 1040, rotate: 8 },
      { kind: 'bubble', icon: 'loop', label: 'Loop', size: 112, left: 640, top: 960 },
    ],
  },
  {
    ...stories[7],
    capture: `${T}saved.png`,
    spotlights: [[229, 626, 731, 160]],
    popouts: [{ src: `${T}saved.png`, x: 229, y: 626, width: 731, height: 160, shownWidth: 760, left: 150, top: 950, rotate: -1.5 }],
    art: [
      { kind: 'bubble', icon: 'heart', size: 104, left: 830, top: 880, rotate: 10 },
      { kind: 'badges', items: TRUST, size: 30, width: 920, left: 150, top: 1200 },
    ],
  },
];
