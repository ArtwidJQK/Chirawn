# ADR 0002: Deep Game Engines

## Status

Proposed

## Context

Current game logic (Sudoku, 2048, Sliding Puzzle) is embedded within ViewModels. This makes the logic hard to test in isolation, difficult to reuse, and couples game rules to Android's ViewModel lifecycle and Compose's State.

## Decision

We will extract game logic into **Deep Modules** called "Engines":

1.  **Pure Kotlin**: Engines must be pure Kotlin classes with no dependencies on Android or Compose.
2.  **Small Interface**: They will expose a minimal set of functions (e.g., `makeMove`, `initialize`).
3.  **Deep Implementation**: Complex rules like Sudoku validation, 2048 tile merging, and board generation will be hidden inside the implementation.
4.  **State Management**: Engines will return new state objects or immutable data structures, allowing ViewModels to manage the "living" state.

## Consequences

*   **TDD Ready**: We can write comprehensive unit tests for all game rules.
*   **Locality**: Bug fixes in game rules will be concentrated in the Engine classes.
*   **Leverage**: The same Engine could theoretically be used in different UI implementations or for AI "hint" systems in the future.
*   **Complexity**: Introduces a new layer, but simplifies the ViewModels.
