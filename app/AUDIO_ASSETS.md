# Audio Assets — SudokuSage

Drop audio files into `app/src/main/res/raw/` using these exact filenames.
Missing files are tolerated at runtime (silent no-op), so deliver in any order.

## Format
- **SFX**: `.ogg` preferred (compressed, low-latency via `SoundPool`). `.wav` also works. Keep < 200 KB each, mono, 44.1 kHz.
- **BGM**: `.ogg` preferred (better compression than `.mp3`). Loop-friendly authoring (no fade-in / fade-out at boundaries). 1–3 min loops are typical.

## File slots

### SFX (one-shot)

| Filename | When it fires | Mood |
|---|---|---|
| `sfx_ui_tap.ogg` | Generic button / menu row | Soft, neutral click |
| `sfx_ui_toggle.ogg` | Switch / segmented control | Slightly higher pitch |
| `sfx_ui_back.ogg` | Back navigation | Descending |
| `sfx_ui_open.ogg` | Dialog / sheet opens | Soft swoosh |
| `sfx_ui_close.ogg` | Dialog / sheet closes | Reverse of open |
| `sfx_cell_select.ogg` | Sudoku cell focused | Very soft tick |
| `sfx_number_place.ogg` | Digit placed | Crisp confirm |
| `sfx_number_erase.ogg` | Cell erased | Soft swipe |
| `sfx_note_toggle.ogg` | Pencil mark added/removed | Pencil tick |
| `sfx_mistake.ogg` | Wrong digit / conflict | Low buzz |
| `sfx_hint_reveal.ogg` | Hint shown | Bright chime |
| `sfx_line_complete.ogg` | Row/column/box complete | Sparkle |
| `sfx_puzzle_complete.ogg` | Puzzle solved | Short fanfare |
| `sfx_streak_up.ogg` | Daily streak +1 | Coin / pop |
| `sfx_achievement.ogg` | Achievement unlocked | Triumphant chime |

### BGM (looping)

| Filename | Where it plays | Mood |
|---|---|---|
| `bgm_menu.ogg` | Home, settings, stats, daily | Calm, ambient, sparse |
| `bgm_puzzle.ogg` | During gameplay | Focused, low energy, looping |
| `bgm_win.ogg` | Win screen | Brief celebratory loop |

## Recommended free / CC0 sources

All free to use commercially (verify license per track):

- **Pixabay Music** (https://pixabay.com/music/) — large catalog, "ambient" / "calm" filters give good `bgm_menu` candidates. License: Pixabay Content License (free commercial, no attribution required).
- **Kenney UI Audio** (https://kenney.nl/assets/ui-audio) — CC0 UI clicks / toggles / confirmations. Excellent fit for `sfx_ui_*` and `sfx_number_*`. Just rename to slot names.
- **Kenney Casino Audio** / **Interface Sounds** — additional UI variants.
- **Kenney Sci-Fi Sounds** — for `sfx_achievement`, `sfx_hint_reveal`.
- **OpenGameArt** (https://opengameart.org/) — filter by CC0. Good for puzzle-completion fanfares.
- **Free Music Archive** (https://freemusicarchive.org/) — filter by CC0 / CC-BY for BGM.

### Concrete picks (search terms)

- `bgm_menu`: search Pixabay Music — "ambient calm" / "lofi piano calm" / "meditation soft".
- `bgm_puzzle`: Pixabay — "lofi study" / "minimal piano" / "ambient focus".
- `bgm_win`: Pixabay — "victory short" / "celebration soft".
- `sfx_ui_tap`: Kenney UI Audio → `click1.ogg` (rename).
- `sfx_number_place`: Kenney UI Audio → `confirmation_001.ogg` (rename).
- `sfx_mistake`: Kenney UI Audio → `error_005.ogg` (rename).
- `sfx_puzzle_complete`: Kenney Interface → `success.ogg` or OpenGameArt fanfare.

## After dropping files
No code changes needed. `SfxPlayer.preload()` re-resolves on next app start.
The catalog of slots lives in `audio/SoundEvent.kt`.
