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
**Active task**: Resolve the failed Android debug build before adding new product features.  
**Completed**:
- Synced canonical `AGENTS.md` and `CONTEXT.md` from `origin/main`.
- Added Gradle wrapper (`gradlew`, `gradlew.bat`, `gradle/wrapper/*`) to enable standalone builds.
- Inspected all Kotlin source code files and current Gradle configuration.
- Diagnosed `:app:kaptGenerateStubsDebugKotlin`: Java tasks targeted JVM 11 while Kotlin, under JDK 25, selected JVM target 24.
- Updated `app/build.gradle.kts` to align Java and Kotlin compilation on JVM 17.

**Verification**:
- Root cause confirmed from Android Studio's complete Gradle error.
- Configuration fix committed as `946272cff1d5f686281686e27ac907b98fea5014`.
- `assembleDebug` has not yet been rerun after the fix; build status is therefore still unverified.

**Known gaps**:
- Local Gradle build/test execution baseline not yet verified after the JVM-target fix.
- `ChirawnApp.kt` contains all UI views in a single compressed file.
- Home screen `Chuỗi hiện tại` and Profile screen time/achievement stats currently show placeholders (`"—"`).

**Recommended next action**: In Android Studio, sync Gradle and run `assembleDebug` or Run on the Pixel 6 emulator. Send the full result; only after a successful baseline should we plan the next feature.
