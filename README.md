# SudokuSage

> 현자의 한 수 — *A wiser way to solve.*

SudokuSage is an Android Sudoku app built around a single idea: be a faithful "Sudoku.com clone + α." That means you get the staples players already expect — six difficulties, a daily challenge, streaks, stats, hints, multiple variants — plus Good-Sudoku-style learning differentiators (technique-based progressive hints, a technique trainer).

The app is currently free; monetization slots (rewarded ads, in-app purchase, Pro entitlement, cloud sync) are wired as no-op interfaces so they can be flipped on without touching feature code.

---

## Status

All core milestones (M0–M10) shipped. Multi-module split + DI migration done.

| Area | Status |
|---|---|
| Classic 9×9 + 5 difficulties + endless seeds | ✅ |
| Variants — Mini 6×6, Mini 4×4, X-Sudoku, Hyper | ✅ |
| Killer Sudoku (RuleSet, generator, codec, board renderer) | ⚠️ infra-complete, hidden in UI pending uniqueness-guaranteed cage layout generator |
| Daily challenge — deterministic per-date seed, 5×7 calendar, streaks | ✅ |
| Auto-save / Resume, undo/redo, peer-note auto-clear | ✅ |
| Hint engine — Naked Single, Hidden Single, Naked Pair, Pointing Pair, 2-step progressive UX | ✅ |
| Trainer — explanation cards + practice puzzles | ✅ |
| Achievements — 6 binary unlocks fed by GameEventBus | ✅ |
| Settings — 4-way theme, AMOLED, 5 palettes (Sage/Forest/Ocean/Sunset/Lavender), font scale, color-blind mode, mistake-limit toggle, cell-first / number-first input mode | ✅ |
| Replay / 검토 mode — slider scrubbing through move history | ✅ |
| Tablet & landscape responsive layouts | ✅ |
| Today's puzzle home-screen widget — status + streak + deep-link | ✅ |
| Stats — wins/losses, best time, win-rate, Compose Canvas bar chart | ✅ |
| Localization — `ko` / `en` | ✅ |
| Accessibility — font scale, color-blind palette | ✅ |

**Outstanding (gated on external setup):**
- Firebase cloud sync — needs Firebase Console + `google-services.json`
- Real IAP — needs Play Console product setup
- Real rewarded ads — needs AdMob ad units

**Outstanding (algorithmic):**
- Uniqueness-guaranteed cage-layout generator for Killer (current empty-board flood-fill produces multi-solution puzzles for cage size ≥ 3, so the variant is hidden until this lands)

---

## Tech stack

| Layer | Choice |
|---|---|
| Language | Kotlin 2.2 (JVM 11) |
| UI | Jetpack Compose + Material 3 |
| Navigation | Navigation Compose (incl. deep links) |
| Persistence | Room 2.7 + DataStore Preferences |
| DI | Koin 4.0 (single-file modules; pivoted from Hilt due to AGP 9 incompat) |
| Async | kotlinx.coroutines + Flow |
| Build | AGP 9.1 + Gradle 9.3, KSP for Room/Moshi |
| min/target SDK | 36 / 36 (Android 16) |
| Tests | JUnit 4 + 52 unit tests covering codecs, hint techniques, variants, daily streak math, Killer ruleset |

---

## Module structure

```
:app                   Application class, Koin setup, MainActivity, NavGraph,
                       all UI screens, audio orchestration, feedback, monetization stubs,
                       widget, GameViewModel/GameEngine
:core:data             Room (DB, DAOs, entities), DataStore-backed settings
                       (audio + gameplay), repositories, GameEventBus collectors
                       (stats + achievements). Android library; depends on :core:domain.
:core:domain           Pure-JVM Kotlin library — no Android deps. Board, RuleSet,
                       GameState, Move, Region, Difficulty, VariantId, GenericSolver,
                       PuzzleGenerator, hint Technique catalog, achievement catalog,
                       daily seed/strategy/status, trainer catalog, all variants
                       (Classic / Mini6 / Mini4 / X / Hyper / Killer), board codec.
```

The split is deliberate — `:core:domain` has zero Android dependency so it compiles
fast and stays unit-testable on the JVM.

---

## Build & run

```bash
# 1. Clone, open in Android Studio (Iguana+ recommended, AGP 9 ready), or:
./gradlew :app:assembleDebug

# 2. Run unit tests across all modules
./gradlew test

# 3. Install on a connected device / emulator running Android 16+
./gradlew :app:installDebug
```

`local.properties` (auto-generated, gitignored) must contain a valid `sdk.dir` line.

---

## Optional setup checklists

These are documented separately so the app ships with all flows wired:

- **Audio assets** — `app/AUDIO_ASSETS.md` lists 30 SFX/BGM slots in `app/src/main/res/raw/`. Drop the matching `.ogg` filenames; missing files are silently no-op'd at runtime. CC0 / royalty-free libraries (Freesound, Mixkit, Pixabay) are good sources.
- **Monetization** — `app/MONETIZATION.md` documents the Play Console + AdMob + Firebase setup needed to swap the no-op `EntitlementGate` / `AdProvider` / `IapProvider` / `CloudSyncProvider` impls for real ones. Each swap is a one-line change in `app/src/main/java/com/mingeek/sudokusage/di/KoinModules.kt`.

---

## Architecture highlights

- **Variant pluggability** — every variant is a `RuleSet` + `PuzzleGenerator` registered with a `VariantRegistry`. New variants land by adding a `register()` call in `VariantsBootstrap`; no other file changes.
- **Hint pluggability** — every technique is a `Technique` implementation registered with `HintEngine` via `HintBootstrap`. New techniques (Hidden Pair, X-Wing, Box-Line Reduction, …) drop in without touching engine internals.
- **GameEvent bus** — `StatsCollector` and `AchievementCollector` subscribe to a process-wide `GameEventBus` so feature code (the `GameViewModel`) just emits events and never imports persistence.
- **Per-puzzle Killer ruleset** — Killer cages are puzzle-specific data, not a singleton. `VariantRegistry` registers a default `KillerRuleSet(emptyList())` for variant lookup; `GameViewModel.rulesFor(state)` constructs the real `KillerRuleSet(state.cages)` per-game.

---

## Project layout (selected files)

```
app/src/main/java/com/mingeek/sudokusage/
├── SudokuSageApp.kt              Application — startKoin + collectors
├── MainActivity.kt               Compose host + intent deep-link
├── di/KoinModules.kt             Single-file 7-module DI graph
├── game/                         GameEngine, GameLaunch, GameViewModel, GameUiState, HintState
├── ui/
│   ├── navigation/SudokuNavGraph.kt   Routes + deep-link wiring
│   ├── screens/{home,daily,game,stats,settings,trainer,achievements,pro,replay}
│   └── theme/                    5 palettes + 4-way theme + AMOLED + tokens
├── widget/TodaysPuzzleWidgetProvider.kt   Home-screen widget
├── audio/, feedback/             AudioController + BgmPlayer + SfxPlayer + HapticController
├── monetization/                 EntitlementGate / AdProvider / IapProvider (NoOp impls)
├── platform/sync/                CloudSyncProvider (NoOp; Firebase swap-in)
├── analytics/, featureflags/     interface-only; no-op impls

core/data/src/main/java/com/mingeek/sudokusage/
├── data/db/                      SudokuDatabase + DAOs + entities
├── data/repo/                    GameSave / Stats / Daily / Achievement repos
├── data/preferences/             GameplaySettings (DataStore)
├── data/stats/, data/achievement/  Event-bus collectors
└── audio/AudioSettings.kt        DataStore-backed audio prefs

core/domain/src/main/java/com/mingeek/sudokusage/
├── domain/board/                 Board, Cell, RuleSet, BoxedRuleSet, Region, GenericSolver, GameState, Move, Difficulty, VariantId, VariantRegistry
├── domain/hint/                  HintEngine, Technique + 4 implementations
├── domain/generator/             PuzzleGenerator, GenericGenerator
├── domain/event/                 GameEvent, GameEventBus
├── domain/daily/                 DailyConfig, DailySeed, DailyStrategy, DailyStatus
├── domain/achievement/, domain/trainer/  Catalogs
├── data/codec/                   BoardCodec, MoveCodec, NotesCodec, CageCodec
├── game/hint/HintBootstrap.kt    Default Technique registry
└── variant/{classic,mini,x,hyper,killer}/  Per-variant RuleSet + Generator
```

---

## License

To be added. Until then, all rights reserved by the author.

---

## Acknowledgements

Hint-technique design draws from the Good Sudoku app's progressive-reveal philosophy. Daily-seed mixing uses the standard Knuth multiplicative-hash pattern. Compose UI patterns follow Google's now-in-android sample.
