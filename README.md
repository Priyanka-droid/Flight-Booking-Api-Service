# Flight Ticket Booking API

A small Spring Boot service for booking seats on a flight. In-memory, single instance,
no database. Booking is the only operation.

## Requirements

- Java 17
- Maven 3.x

## Run

```bash
mvn spring-boot:run
```

The service starts on `http://localhost:8080`.

A few flights are seeded at startup (edit `src/main/resources/application.yml` to change them):

| Flight | Seats |
|--------|-------|
| AA100  | 150   |
| BA200  | 200   |
| CA300  | 80    |
| DL400  | 50    |

## Run the tests

```bash
mvn test
```

## Usage

### Create a booking

`POST /bookings`

```bash
curl -i -X POST http://localhost:8080/bookings \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: order-123" \
  -d '{"flightId": "AA100", "seats": 2, "passenger": "Jane Doe"}'
```

`201 Created`:

```json
{
  "bookingId": "5f9c1e2a-...",
  "flightId": "AA100",
  "seatsBooked": 2,
  "seatsRemaining": 148
}
```

### Retry safely with an idempotency key

Send the same `Idempotency-Key` on a retry and you get the original booking back —
the seats are not booked twice. Reusing the same key with a different body is rejected.

```bash
curl -i -X POST http://localhost:8080/bookings \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: order-123" \
  -d '{"flightId": "AA100", "seats": 2, "passenger": "Jane Doe"}'
```
### Retry with same idempotency key but different body

Send the same `Idempotency-Key` but with different requets body, should fail
```bash
curl --location 'http://localhost:8080/bookings' \
--header 'Content-Type: application/json' \
--header 'Idempotency-Key: order-124' \
--data '{"flightId": "AA100", "seats": 3, "passenger": "Jane Doe"}'
```

```json
{
    "code": "IDEMPOTENCY_KEY_CONFLICT",
    "message": "Idempotency-Key order-124 was reused with a different request"
}
```

### Send request for a flight that does not exist

Should fail

```bash
curl --location 'http://localhost:8080/bookings' \
--header 'Content-Type: application/json' \
--header 'Idempotency-Key: order-125' \
--data '{"flightId": "1", "seats": 3, "passenger": "Jane Doe"}'
```

```json
{
    "code": "FLIGHT_NOT_FOUND",
    "message": "Unknown flight: 1"
}
```

### Request more than allowed seats

Should fail
```bash
curl --location 'http://localhost:8080/bookings' \
--header 'Content-Type: application/json' \
--header 'Idempotency-Key: order-125' \
--data '{"flightId": "DL400", "seats": 51, "passenger": "Jane Doe"}'
```


### Errors

Every error returns the same shape — `{ "code": ..., "message": ... }`:

| Situation                         | Status | Code                    |
|-----------------------------------|--------|-------------------------|
| Invalid / malformed request       | 400    | `INVALID_REQUEST`       |
| Unknown flight                    | 404    | `FLIGHT_NOT_FOUND`      |
| Not enough seats left             | 409    | `INSUFFICIENT_SEATS`    |
| Same key reused with a different body | 422 | `IDEMPOTENCY_KEY_CONFLICT` |

```json
{ "code": "INSUFFICIENT_SEATS", "message": "Flight AA100 has 1 seat(s) left, requested 2" }
```

## Design choices

The booking is **all-or-nothing** and never oversells: the check-and-decrement of a
flight's seats runs as one atomic step per flight, so concurrent bookings on the same
flight can't push it past capacity, while bookings on different flights don't block each
other. Idempotency keys let a client retry without booking twice.

The full reasoning — endpoint shape, status code choices, the concurrency and idempotency
mechanisms — is in [DECISIONS.md](DECISIONS.md).

## What I'd improve with more time

- **Expire old idempotency keys.** Retry tokens are kept forever; they'd need a TTL or
  periodic cleanup so the store doesn't grow unbounded.
- **Persistence.** Everything lives in memory, so all bookings are lost on restart. A real
  deployment would back this with a database.
- **Multi-instance correctness.** The no-overbooking guarantee relies on a single process's
  in-memory concurrent hashmap. Across multiple servers it would have to move to database-level locking (or
  another shared coordinator) to stay safe.
- **Seat selection.** Booking only takes a seat *count*; choosing specific seats would be a
  separate feature.
