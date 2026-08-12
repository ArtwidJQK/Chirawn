# Chirawn Context

Chirawn is a personal Android entertainment hub for Ngọc Anh. It should feel like a calm, modern, playful, slightly premium personal digital space — not a generic mini-game collection or a utility app.

## Product Direction

**Product owner**: Art  
**Primary user**: Ngọc Anh  
**Current product shape**: Home, Game Center, Profile, Sudoku, 2048, Sliding Puzzle, and local game/profile persistence.  
**Future ideas**: progression, achievements, daily challenges, themes, journal, companion, easter eggs, and narrowly useful AI are not approved scope until Art activates them.

## Language

**Chirawn**: The whole personal digital space, not merely the game collection.  
_Avoid_: generic game hub, mini-game demo

**Game Center**: The entry point for the currently approved games.  
_Avoid_: arcade, game library

**Game Session**: One recorded play attempt, including game type, score, elapsed duration, completion state, and timestamp.  
_Avoid_: game record, match

**Best Stats**: The best persisted score and/or completion time for a game.  
_Avoid_: leaderboard

**Profile**: The locally stored personal identity surface, currently containing the nickname.  
_Avoid_: account, user account

**Streak**: The number of consecutive days Ngọc Anh completes at least one game session.

**Streak Recovery**: A 48-hour window after a streak is broken where it can be restored. If not restored within 2 days, the streak is lost permanently.

**Artwid**: The digital companion and supporter within Chirawn, representing Art (the creator). Artwid provides greetings, encouragement, and manages the Streak Recovery offers.

## Architecture Snapshot

- Android, Kotlin, Jetpack Compose, Material 3 foundation.
- Single app module, package `com.chirawn.app`.
- Room local database: `UserProfile`, `GameSession`, `GameBestStats`.
- Current UI routing is a local route enum/back stack inside the Compose app; Navigation Compose is declared as a dependency but is not the current routing mechanism.
- The current codebase is an early foundation. UI is substantially concentrated in `ChirawnApp.kt`; persistence is concentrated in `AppDatabase.kt`.
- Product direction requires game rules to stay separate from Compose UI and independently testable.

## Working Agreements

- Local-first and offline-first by default.
- No remote services, analytics, accounts, cloud sync, or AI APIs without Art's explicit approval.
- Prefer the smallest coherent change.
- Use GitHub Issues for approved specifications and multi-step work; see `docs/agents/issue-tracker.md`.
- Read relevant ADRs from `docs/adr/` before challenging an established decision. Create ADRs only for durable, non-obvious, hard-to-reverse trade-offs.

## Current Work / Handoff

**Current phase**: Foundation build stabilization.  
**Active task**: Foundation baseline verification & Architecture reinforcement.  
**Completed**:
- Synced canonical `AGENTS.md` and `CONTEXT.md` from `origin/main`.
- Added Gradle wrapper to enable standalone builds.
- Updated `app/build.gradle.kts` to align Java and Kotlin compilation on JVM 17 and added unit test support.
- Reformatted `ChirawnApp.kt` for standard Kotlin/Compose style and fixed build errors.
- Extracted game logic for Sudoku, 2048, and Sliding Puzzle into dedicated "Deep Engine" classes (`SudokuEngine`, `Game2048Engine`, `SlidingEngine`).
- Implemented full unit test suite for game engines using TDD.
- Recorded architecture decisions in `docs/adr/0001` and `0002`.

**Verification**:
- `assembleDebug` finished successfully.
- `app:testDebugUnitTest` passed with 17 tests across all 3 game engines.
- `ChirawnApp.kt` and `GameViewModels.kt` analyzed with no errors.

**Known gaps**:
- Home screen `Chuỗi hiện tại` and Profile screen time/achievement stats currently show placeholders (`"—"`).
- UI remains concentrated in `ChirawnApp.kt`.

**Recommended next action**: Run the app on an emulator/device to verify runtime behavior and UI after the reformatting and stabilization.
