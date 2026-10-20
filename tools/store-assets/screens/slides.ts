// What each Play screenshot says and shows, in store order. Every picture is a real capture from
// ../captures (see ../README.md), and every claim is something the app does.

/** A part of a capture, in the capture's pixels, shown enlarged over the device. */
export interface Callout {
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
}

export interface Slide {
  readonly name: string;
  readonly title: string;
  readonly body: string;
  readonly capture: string;
  /** Phone only: the device hangs from the top and the words sit below it. */
  readonly hanging?: boolean;
  readonly callout?: Callout;
}

const phone = (file: string): string => `phone/${file}`;
const tablet = (file: string): string => `tablet/${file}`;

interface Story {
  readonly name: string;
  readonly title: string;
  readonly body: string;
}

const stories: ReadonlyArray<Story> = [
  {
    name: '1_layers',
    title: 'Your calm, layer by layer',
    body: 'Mix rain, waves, fire and birds into one soundscape.',
  },
  {
    name: '2_now_and_then',
    title: 'Rain all night. Thunder now and then.',
    body: 'Steady sounds keep going. Choose how often the rest drift in.',
  },
  {
    name: '3_ai',
    title: 'Describe a moment. Hear it.',
    body: 'Tell the AI a place or a feeling and it builds a scene from your sounds.',
  },
  {
    name: '4_sleep',
    title: 'Drift off. It fades out gently.',
    body: 'Set a sleep timer. The sounds keep playing with the screen off.',
  },
  {
    name: '5_discover',
    title: 'Discover free sounds',
    body: 'Search Freesound.org and add what you find to your library.',
  },
  {
    name: '6_shuffle',
    title: 'Can’t decide? Shuffle.',
    body: 'Lock the sounds you love and let chance pick the rest.',
  },
  {
    name: '7_your_sounds',
    title: 'Bring your own sounds',
    body: 'Import a file or a Freesound find, trim it and loop it.',
  },
  {
    name: '8_saved',
    title: 'Keep the mixes you love',
    body: 'No ads and no subscription. Your sounds play offline.',
  },
];

// Phone slides are 1080×1920; phone captures are 1080×2400.
export const phoneSlides: ReadonlyArray<Slide> = [
  { ...stories[0], capture: phone('scene.png') },
  {
    ...stories[1],
    capture: phone('scene-how-often.png'),
    hanging: true,
    // The How often menu under Thunder.
    callout: { src: phone('scene-how-often.png'), x: 222, y: 1236, width: 292, height: 424, shownWidth: 380, left: 620, top: 560 },
  },
  { ...stories[2], capture: phone('ai.png') },
  {
    ...stories[3],
    capture: phone('timer.png'),
    // The media controls in the notification shade, over the empty space above the ring.
    callout: { src: phone('notification.png'), x: 43, y: 485, width: 994, height: 480, shownWidth: 820, left: 130, top: 790 },
  },
  { ...stories[4], capture: phone('discover.png') },
  { ...stories[5], capture: phone('mixer.png') },
  {
    ...stories[6],
    capture: phone('editor.png'),
    // The waveform with its trim handles.
    callout: { src: phone('editor.png'), x: 0, y: 330, width: 1080, height: 400, shownWidth: 960, left: 60, top: 1000 },
  },
  { ...stories[7], capture: phone('saved.png') },
];

// Tablet slides are 2560×1440; tablet captures are 2560×1600.
export const tabletSlides: ReadonlyArray<Slide> = [
  { ...stories[0], capture: tablet('home.png') },
  {
    ...stories[1],
    capture: tablet('scene-how-often.png'),
    callout: { src: tablet('scene-how-often.png'), x: 1960, y: 840, width: 270, height: 370, shownWidth: 330, left: 2150, top: 760 },
  },
  { ...stories[2], capture: tablet('ai.png') },
  {
    ...stories[3],
    capture: tablet('timer.png'),
    callout: { src: tablet('notification.png'), x: 81, y: 896, width: 1118, height: 367, shownWidth: 760, left: 150, top: 1010 },
  },
  { ...stories[4], capture: tablet('discover.png') },
  { ...stories[5], capture: tablet('mixer.png') },
  {
    ...stories[6],
    capture: tablet('editor.png'),
    callout: { src: tablet('editor.png'), x: 560, y: 270, width: 1440, height: 320, shownWidth: 1200, left: 1180, top: 1040 },
  },
  { ...stories[7], capture: tablet('saved.png') },
];
