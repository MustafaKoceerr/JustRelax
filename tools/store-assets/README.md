# Store assets

The app icon family and the Google Play listing graphics, built from code so they stay in step.

- **Icon:** a dusk sun sliced into layers by soft waves, standing for the layers of a scene.
  `icon/build-icon.ts` draws it once. The same geometry becomes:
  - the Android adaptive icon (vector foreground, background and monochrome layers);
  - the notification icon;
  - the Play icon (512 px);
  - the iOS icons (1024 px: light, dark and tinted);
  - the in-app mark and the media artwork.
- **Play graphics:**
  - 8 phone screenshots (1080×1920);
  - 8 tablet screenshots (2560×1440), the same set for the 7- and 10-inch sections;
  - the feature graphic (1024×500).

  They are written in the fastlane layout under `fastlane/metadata/android/en-US/images/`.

## Rebuild

```sh
cd tools/store-assets
npm install                      # TypeScript and Node types, for `npm run typecheck` only
./render.sh icons                # icon family -> androidApp, core/ui, iosApp, fastlane icon.png
./render.sh screens              # screenshots + feature graphic -> fastlane/
npm run typecheck
```

`render.sh` uses headless Google Chrome and nothing else. Node 26 runs the `.ts` files directly.

Review pages are written to `screens/out/` (git-ignored):
- `phone-sheet.png` and `tablet-sheet.png` show each set side by side, the way the store lines them up.
- Each slide's `.html` file opens in a browser.

## What goes on a slide

`screens/slides.ts` holds the copy and picks the capture for each slide. No slide is a bare
screenshot in a frame. Each one is dressed to tell its story:

- an eyebrow chip and a headline whose key words (`*like this*`) take the sunset gradient;
- **popouts**: the part of the screen that tells the story is lifted out of the device, enlarged
  and tilted, sitting over the place it came from;
- **spotlights**: the rest of the screen dims, and the lifted parts get a glowing rim;
- drawn extras from `screens/decor.ts`:
  - sound bubbles;
  - the steady-versus-now-and-then strip;
  - rain, lightning, the moon and "zzz";
  - dice, sparkles, music notes and arrows;
  - the trust badges.

Two rules hold throughout:

- **Real UI only.** Every screen and every popout is cropped from a real capture in `captures/`.
  The drawn extras are plainly illustrations; none of them imitates a screen of the app.
- **True claims only.** Each sentence and label describes something the app does today. There are
  no rankings ("#1", "best"), no sound counts (the catalog changes) and no prices.

Popout and spotlight rectangles are in capture pixels. The comments at the top of each device's
list in `slides.ts` give the formula for where a capture point lands on the slide.

## Retaking the captures

The captures come from the Pixel_8a (1080×2400) and Pixel_Tablet (2560×1600, landscape) emulators.
Run one emulator at a time and use a debug build pointed at the CDN catalog, so all built-in sounds
appear. To do that, comment out the `AssetSoundCatalog` line in
`androidApp/src/debug/.../di/BuildTypeModule.kt` for the session, and don't commit the change. The
device setup:

- dark theme: `adb shell cmd uimode night yes`;
- English;
- SystemUI demo mode, with the clock at 9:41 and full battery and signal.

On the timer slide, the demo clock is set to the device's real time so that it matches the timer's
end time.

AI and Discover need the local API proxy: `npm run dev` in `server/api-proxy`, then
`adb reverse tcp:8787 tcp:8787`.
