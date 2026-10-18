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

| Benchmark | Before | After |
|---|---|---|
| Cold start, no profile | 731 ms | |
| Cold start, Baseline Profile | 764 ms (library profiles only, no app profile yet) | |
| Timer: fling the minute wheel | 6.6 / 9.2 / 10.2 | |
| Timer: running countdown, 5 s | 7.5 / 8.0 / 11.5 (338 frames) | |
| Home: switch categories | 7.7 / 18.8 / 37.4 | |
| Home: drag volume | 10.4 / 15.5 / 24.6 | |

## Compose stability (composable parameters)
| | Before | After |
|---|---|---|
| stable | 392 | |
| unstable | 4 | |
| runtime-checked (UiState, List, …) | 56 | |
| unstable classes | 26 (mostly ViewModels and UiStates) | |
