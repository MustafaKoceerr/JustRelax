# Performance measurements

The numbers come from the `:benchmark` module on a real phone, measured before and after the UI work (PRs #44–#46).
Emulator numbers are not comparable, so they are not recorded here.

## How to run
1. Connect a phone with USB debugging on. The Baseline Profile needs Android 13+.
2. Open the app once with internet, so the built-in pack downloads. `LibraryBenchmark.dragVolume` needs a playable card.
3. Run:
   ```
   ./gradlew :benchmark:connectedBenchmarkReleaseAndroidTest
   ./gradlew :androidApp:generateReleaseBaselineProfile   # writes androidApp/src/release/generated/baselineProfiles/
   ```
   Results land in `benchmark/build/outputs/connected_android_test_additional_output/`. Each test writes JSON and Perfetto traces.

Compose compiler stability reports land in each module's `build/compose-reports/`:
```
./gradlew compileAndroidMain :androidApp:compileReleaseKotlin -Pjustrelax.composeReports=true --rerun-tasks
```
`--rerun-tasks` is needed because a cached compile does not write the reports.

## Device
Samsung Galaxy A52 (SM-A525F), Android 14, 90 Hz (11.1 ms per frame). "Stay awake" on: with the screen locked, benchmarks record no frames.

## Results
Frame times are `frameDurationCpuMs` P50 / P90 / P99. Startup is `timeToInitialDisplayMs` median.

| Benchmark | Before (#44) | After (#46) |
|---|---|---|
| Cold start, no profile | 731 ms | 603 ms |
| Cold start, Baseline Profile | 764 ms (library profiles only, no app profile yet) | 571 ms (app profile) |
| Timer: fling the minute wheel | 6.6 / 9.2 / 10.2 | 7.0 / 9.6 / 11.6 (3D drum) |
| Timer: running countdown, 5 s | 7.5 / 8.0 / 11.5 (338 frames) | 7.0 / 7.6 / 9.2 (514 frames: the ring glides) |
| Home: switch categories | 7.7 / 18.8 / 37.4 | 7.6 / 13.8 / 32.1 |
| Home: drag volume | 10.4 / 15.5 / 24.6 | 12.0 / 17.7 / 23.8 |

Notes:
- Before and after were measured the same day, five iterations each (startup: ten). Differences of about 1 ms are within the run-to-run noise.
- Dragging a volume slider did not get faster. The shell no longer recomposes for it, but Home and its grid still do; that needs a trace to look into.
- The new wheel costs a little more per frame (each row is scaled, faded and tilted), still under the 11.1 ms budget at P95.

## Compose stability (composable parameters)
After `compose-stability.conf` (PR #46), which marks the read-only collections, `Instant`, `core:model` and two `core:media` value classes as stable.

| | Before | After |
|---|---|---|
| stable | 392 | 441 |
| unstable | 4 | 0 |
| runtime-checked (UiState, List, …) | 56 | 50 |
| unstable classes | 26 (mostly ViewModels and UiStates) | 21 (ViewModels, the app classes, two small helpers) |
