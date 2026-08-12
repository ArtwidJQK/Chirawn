# ADR 0003: Streak Recovery Mechanic

## Status

Proposed

## Context

Chirawn aims to be a calm and supportive space. A broken streak can be discouraging. We want to provide a "second chance" while maintaining the habit-building aspect of streaks.

## Decision

We implement a **48-hour recovery window**:

1.  **Detection**: If a full calendar day passes without a completed session, the `currentStreak` is set to 0.
2.  **Grace Period**: The date the streak was broken is recorded as `streakBrokenDate`.
3.  **Restoration**: For the next 48 hours, the UI will offer a "Restore Streak" option facilitated by Artwid.
4.  **Expiration**: If the user does not choose to restore within 48 hours of the `streakBrokenDate`, the previous streak value is permanently discarded.
5.  **Storage**: These values will be stored in the `user_profile` table to ensure persistence.

## Consequences

*   **User Experience**: Reduces the "all-or-nothing" pressure of streaks.
*   **Engagement**: Creates an emotional touchpoint with Artwid.
*   **Complexity**: Requires careful timestamp handling to ensure 48-hour accuracy across timezones.
