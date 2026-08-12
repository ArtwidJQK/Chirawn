# ADR 0001: Pragmatic Layered Architecture for Chirawn

## Status

Accepted

## Context

Chirawn needs a maintainable and testable architecture that remains simple enough for a personal hobby project. We want to avoid over-engineering while ensuring that the core product (games and personal data) is protected and can evolve.

## Decision

We adopt a **Pragmatic Layered Architecture** on Android:

1.  **UI Layer**: Jetpack Compose with Material 3. Responsible only for rendering state and capturing user intent.
2.  **State/ViewModel Layer**: Android Lifecycle ViewModels. Manages UI state, handles user actions, and coordinates between UI and Domain/Data layers.
3.  **Domain/Game Logic Layer**: Pure Kotlin classes/functions. **Must be independent of Android frameworks and Compose.** (Note: Currently being migrated from ViewModels).
4.  **Data Layer**: 
    *   **Repository**: `HubRepository` as the single point of entry for data.
    *   **Persistence**: Room Database for relational data.
    *   **Policy**: Local-first and offline-first by default.

## Consequences

*   **Testability**: Game logic can be unit-tested without emulators or Compose stubs.
*   **Separation of Concerns**: UI changes won't leak into game rules.
*   **Simplicity**: We use standard Android Jetpack components without introducing complex DI frameworks (like Hilt) or Redux-like patterns unless the complexity justifies it.
*   **Migration Task**: Existing game logic currently residing in ViewModels (Sudoku, 2048, Sliding) needs to be extracted into dedicated logic classes.
