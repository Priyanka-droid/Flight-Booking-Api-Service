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
