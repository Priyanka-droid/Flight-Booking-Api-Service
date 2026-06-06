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
