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
_To fill in: model, Android version, refresh rate._

## Results
Frame times are `frameDurationCpuMs` P50 / P90 / P99. Startup is `timeToInitialDisplayMs` median.

| Benchmark | Before | After |
|---|---|---|
| Cold start, no profile | | |
| Cold start, Baseline Profile | | |
| Timer: fling the minute wheel | | |
| Timer: running countdown, 5 s | | |
| Home: scroll sounds | | |
| Home: drag volume | | |

## Compose stability (composable parameters)
| | Before | After |
|---|---|---|
| stable | 392 | |
| unstable | 4 | |
| runtime-checked (UiState, List, …) | 56 | |
| unstable classes | 26 (mostly ViewModels and UiStates) | |
