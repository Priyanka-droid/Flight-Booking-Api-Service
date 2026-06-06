# Design Decisions

- **Models as immutable records** — `Flight(id, capacity, remainingSeats)` and
  `Booking(id, flightId, seats, passenger)`. Immutability fits the atomic
  check-and-decrement pattern (replace the value via `ConcurrentHashMap.compute`).
- **In-memory storage** — `FlightRepository` backed by a `ConcurrentHashMap`,
  keyed by flight id. No database, per project constraints.
- **Startup seeding** — `FlightDataLoader` (`CommandLineRunner`) loads a few
  flights at startup so the service is usable without a search feature; remaining
  seats start equal to capacity. Kept separate from the repository so storage stays
  pure and the seed is easy to test.
- **Booking endpoint** — `POST /bookings`, returning `201 Created`. POST to a
  collection that creates a resource is the conventional choice; 201 signals a new
  booking was created. No `Location` header is set, since there is no booking
  retrieval endpoint (a constraint) and pointing at a non-existent GET would mislead.
- **Response shape** — `{ bookingId, flightId, seatsBooked, seatsRemaining }`, so the
  client can identify the booking and see the flight's remaining seats in one call.
- **Atomic reserve** — `FlightRepository.reserveSeats` does the check-and-decrement
  inside `ConcurrentHashMap.compute`, so "book all N or none" holds and the same flight
  can never oversell. Unknown flight / not-enough-seats throw for now (full HTTP error
  mapping comes with the rejection iteration).
- **Request validation** — Jakarta Bean Validation on the request DTO
  (`@NotBlank` flightId/passenger, `@NotNull @Positive` seats) via `@Valid`, so invalid
  input is rejected with `400 Bad Request` before any booking work.
- **Failure statuses (one per cause)** — bad input → `400 Bad Request`; unknown flight →
  `404 Not Found`; not enough seats → `409 Conflict` (the request conflicts with the
  flight's current seat state). Three distinct causes, three distinct statuses.
- **Consistent error body** — every error returns the same `ErrorResponse {code, message}`
  via a single `@RestControllerAdvice`. Codes: `INVALID_REQUEST`, `FLIGHT_NOT_FOUND`,
  `INSUFFICIENT_SEATS`. `FlightNotFoundException` / `InsufficientSeatsException` are thrown
  from the atomic reserve and mapped centrally, so no failure returns bare/inconsistent text.
- **Concurrency** — the check-and-decrement runs inside `ConcurrentHashMap.compute`, which
  holds the per-key bin lock for the duration of the remapping. So same-flight bookings
  serialize (no oversell past capacity) while different flights, sitting under different
  keys, proceed without waiting on each other. No global lock, no `synchronized`. Proven by
  `BookingConcurrencyTest`: 100 threads race for 5 seats → exactly 5 booked, 95 rejected,
  0 remaining; and many flights booked concurrently each reach a correct count.
- **Idempotency** — clients pass an optional `Idempotency-Key` header. The token is claimed
  with `ConcurrentHashMap.computeIfAbsent`: the booking work runs inside the mapping function,
  so it executes exactly once per token and concurrent retries block then replay the stored
  result instead of booking again. The stored `BookingResult` is returned verbatim on replay,
  so a retry sees the original booking id and seat count. No token → every call is independent.
- **Token reuse with a different body** — each token stores a fingerprint of its payload
  (`flightId|seats|passenger`). A replay whose fingerprint differs is rejected as a client
  error with `422 Unprocessable Entity`, code `IDEMPOTENCY_KEY_CONFLICT` — a distinct status
  from the `409` used for insufficient seats, keeping one status per cause. Proven by
  `BookingIdempotencyTest`, including 50 simultaneous retries collapsing to a single booking.

- **Count based booking design choice**
Count-based booking, not a seat map — a flight tracks a single remainingSeats count and a booking takes N seats from it, rather than modelling individual seats (1A, 1B, …) and assigning specific ones. The task is "book N seats if enough remain," which a count answers directly; a seat map adds per-seat state, allocation rules, and a selection/availability API the requirements don't call for. The count also keeps the core invariant trivial to enforce atomically — the whole check-and-decrement is one ConcurrentHashMap.compute over a single integer, whereas reserving specific seats would mean coordinating updates across many seat entries to stay correct under concurrency. Seat selection is noted in the README as a separate feature worth adding later; modelling it now would be over-engineering for the stated scope.
