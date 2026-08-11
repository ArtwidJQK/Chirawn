# Chirawn Agent Instructions

## Purpose

Chirawn is a personal Android entertainment hub for Ngọc Anh. Art is the product owner and final decision maker. Protect the product from feature bloat, generic UI, unnecessary dependencies, and architecture that is more complex than the product needs.

## Start Every Task

1. Read `CONTEXT.md`.
2. Read relevant ADRs in `docs/adr/`, if any.
3. Inspect the current repository state before making claims.
4. Identify the current phase, active task, affected behavior, and verification needed.

Do not rely on a previous model's chat history.

## Workflow

Choose the lightest process that fits the work:

- Tiny UI adjustment: inspect → implement → verify.
- Small bug: reproduce → diagnose → fix → verify.
- New game rule or deterministic logic: model the rules → define the public test seam with Art → TDD → implement → review.
- Major feature: clarify → domain model → specification → tickets → implement → test → review.
- Major architecture decision: research when facts may change → evaluate trade-offs → ask Art → record an ADR when the decision is hard to reverse.

Do not run a heavyweight process only because a skill exists.

## Decisions and Scope

Ask Art before a decision that materially changes product identity, major UX, architecture, data model, privacy, security, scope, or long-term maintenance. For ordinary implementation details, use sound engineering judgment.

Use GitHub Issues for approved specs and multi-step work. Do not create issues or labels unless the task requires them.

## Shared Handoff Protocol

`CONTEXT.md` is the living handoff record for Codex, Gemini, Claude, and any future agent.

Before ending a meaningful work session, update its **Current Work / Handoff** section with:

- exact task and phase;
- completed work and files changed;
- decisions made and unresolved questions;
- verification run and its result;
- known risks, regressions, or blockers;
- the single recommended next action.

Never mark work complete without stating what was verified. If no code was changed, say so.

## Code Expectations

- Keep core game logic independent from Compose UI and testable.
- Preserve the pragmatic layered Android architecture: UI → state/ViewModel → game/domain logic → persistence.
- Keep Chirawn local-first and offline-first.
- Prefer Android/Kotlin platform capabilities before adding dependencies.
- Make small coherent changes; do not rewrite working areas without a documented reason.
