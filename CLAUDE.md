# CLAUDE.md — Flight Ticket Booking API

Spring Boot 3.x.x + Java 17, single instance, in-memory. Favour correctness
and clarity; do not over-engineer. Read this before every change.

## Non-negotiable constraints

- In-memory only. No database, no persistence.
- No overbooking
- Booking is the only write. No retrieval endpoints.
- No authentication, no authorization, no rate limiting, no search, no destination logic, single instance.

## Features

## Features

1. **Booking** — create a booking for a known flight and N seats. Each flight has a
   fixed capacity and a remaining-seat count; a booking succeeds only if enough seats
   remain, then decrements the count. All-or-nothing: either all N seats are booked or
   the request is rejected.

2. **Concurrency** — the check-and-decrement must be one atomic operation per flight
   (e.g. `ConcurrentHashMap.compute`): under any interleaving of concurrent bookings on
   the same flight, total seats booked never exceeds capacity. A plain read-then-write
   will oversell — don't write it. Same-flight requests serialize; different flights
   don't.

3. **Idempotency** — a token-carrying retry returns the original booking, not a new one.
   This matters because a count-only retry would otherwise silently book N more seats.
   Claim the token before doing the booking work so concurrent retries can't double-book;
   a replay returns the original booking. Fingerprint the request by its payload (flight +
   count + passenger); the same token with a different body is a client error.
4. **Comments** — comment complex methods only; keep them short and plain, just stating what the method does.

You design the API (the task says "model endpoints as you see fit"). Don't overload one status code across unrelated failures.

## Design decisions log

Keep design/implementation decisions in `DECISIONS.md`, not the README. When you make a
non-trivial choice (endpoint shape, status codes, a tradeoff), append a short bullet to
`DECISIONS.md` — append, don't rewrite, and don't re-explain past decisions each
iteration. The README stays a clean run/usage doc.

## TDD (required)

Test first, for business behaviour only (not DTOs, controllers, wiring):
failing test → minimum code → green → refactor.

## Do NOT add (these cause drift)

database, authorization, authentication, search, booking retrieval, cancellation, hold/TTL,
multi-instance concerns, rate-limit.

## Commit rule (CRITICAL)

After each green iteration, commit yourself: `git add -A`, then commit with my exact
prompt as the message verbatim, via `git commit -F <tempfile>` (never `-m`, so quotes
and newlines survive). No "Generated with Claude Code" footer, no trailers. One prompt =
one commit (test + code together). Then show me `git log --oneline -1`.