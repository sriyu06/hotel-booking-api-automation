# hotel-booking-api-automation

API test automation framework for the public practice API
[Restful-Booker](https://restful-booker.herokuapp.com/apidoc/index.html), built with
**Java 17, REST Assured, JUnit 5 and Maven**, with a matching **Postman collection** and a
**GitHub Actions** pipeline.

## Project status (read this first)

The code and Postman collection were written against Restful-Booker's documented behaviour,
but **have not been executed by the author of this scaffold**. Run `mvn clean test` yourself
and fix anything that differs, then update this section with your real results.
No pass rate, coverage or timing figures are claimed here.

## Learning objectives
- Write readable given / when / then API tests
- Reuse request specifications, models and client classes
- Handle token authentication and negative scenarios
- Keep tests independent and clean up the data they create
- Run the same checks in Postman and in CI

## Tech stack
| Tool | Version | Why |
|---|---|---|
| Java | 17 | LTS; required baseline for modern REST Assured |
| REST Assured | 5.5.7 | Stable Jackson-2-based line; given/when/then DSL |
| Jackson Databind | 2.18.2 | POJO <-> JSON; pinned so REST Assured uses Jackson 2 |
| JUnit 5 (Jupiter) | 5.11.4 | Test runner, assertions, parameterized tests, tags |
| Maven Surefire | 3.5.2 | Runs JUnit 5 and writes reports |
| Postman | any recent | Exploratory testing and a shareable collection |
| GitHub Actions | - | CI |

## Structure
```
hotel-booking-api-automation/
├── pom.xml
├── src/main/java/com/hotelbooking/
│   ├── clients/    ApiSpecs, AuthClient, BookingClient, HealthClient
│   ├── config/     ConfigManager (env vars -> system props -> config.properties)
│   ├── models/     Booking, BookingDates, BookingResponse, AuthRequest (records)
│   └── utils/      TestDataFactory (unique, controlled test data)
├── src/main/resources/config.properties
├── src/test/java/com/hotelbooking/
│   ├── base/       BaseTest (token, clean-up, timeout)
│   └── tests/      HealthCheck, Authentication, CreateBooking, GetBooking,
│                   UpdateBooking, DeleteBooking, BookingWorkflow
├── postman/        collection + example environment
├── .github/workflows/api-tests.yml
└── docs/INTERVIEW_PREP.md
```
Deviation from a typical layout: the API clients live in `src/main` (as requested) so they are
reusable; this is why REST Assured is a compile-scope dependency. Models are Java records to
avoid boilerplate and get `equals()` for free.

## Endpoints covered
| Endpoint | Used for |
|---|---|
| `GET /ping` | health check (documented to return 201 Created) |
| `POST /auth` | get a token; wrong credentials return 200 with `{"reason":"Bad credentials"}` |
| `POST /booking` | create |
| `GET /booking`, `GET /booking/{id}` | list (with firstname/lastname/checkin/checkout filters), read |
| `PUT /booking/{id}`, `PATCH /booking/{id}` | full / partial update (token cookie required) |
| `DELETE /booking/{id}` | delete (documented 201 Created; token cookie required) |

## Prerequisites
- JDK 17+ (`java -version`)
- Maven 3.9+ (`mvn -version`)
- Git, and optionally Postman

## Run the tests
```
mvn clean test                        # default suite
mvn test -DexcludedGroups=none        # also run @Tag("exploratory") tests
mvn test -Dtest=DeleteBookingTests    # one class
```

## Configuration
| Variable | Default | Purpose |
|---|---|---|
| `BASE_URL` | `https://restful-booker.herokuapp.com` | API under test |
| `BOOKER_USERNAME` | public demo user from the API docs | auth user |
| `BOOKER_PASSWORD` | public demo password from the API docs | auth password |
| `CONNECT_TIMEOUT_MS` / `READ_TIMEOUT_MS` | 10000 / 20000 | HTTP timeouts |

Windows PowerShell: `$env:BOOKER_PASSWORD="..."; mvn clean test`
macOS/Linux: `BOOKER_PASSWORD="..." mvn clean test`

Never commit real secrets. `.gitignore` excludes `.env` files and local overrides.

## Failure diagnostics
When an assertion fails, REST Assured prints the full request and response. `Cookie`, `Set-Cookie`
and `Authorization` headers are redacted, and `POST /auth` is never logged, so passwords and tokens
stay out of the output.

## Reports
- Raw: `target/surefire-reports/` (XML and text)
- HTML (optional): `mvn org.apache.maven.plugins:maven-surefire-report-plugin:3.5.2:report-only`
  then open `target/reports/surefire.html` (or `target/site/surefire-report.html` on older plugin versions)
- In CI: download the `test-reports` artifact from the workflow run.

## Postman
1. Import `postman/collections/hotel-booking-api.postman_collection.json`.
2. Import `postman/environments/restful-booker.example.postman_environment.json`, select it,
   and set `username` and `password` (the public demo credentials are in the API docs).
3. Run folders in order 1 to 5 (or use the Collection Runner). `token`, `bookingId` and
   `guestFirstName` are filled in automatically by scripts.
4. Do not export an environment containing real secrets back into the repo.

## CI/CD
`.github/workflows/api-tests.yml` runs on push to `main`, on pull requests and manually. It checks
out the code, sets up Java 17 with Maven caching, runs `mvn clean test`, generates an HTML report and
uploads reports as an artifact. A failing test fails the workflow. Optional secrets
`BOOKER_USERNAME` / `BOOKER_PASSWORD` can be added under Settings > Secrets and variables > Actions;
without them the public demo credentials are used, so forks work out of the box.
Concurrency is limited to one run per branch because the API is shared.
To debug a failure: open the failed run, read the "Run API tests" log (request/response is printed for
the failing assertion), then download the `test-reports` artifact.

## Known limitations of Restful-Booker
- Public, shared and reset periodically; other users' data can appear in list results, so tests only
  rely on data they create themselves with unique names.
- Not strict about validation: invalid input often returns 500 instead of 400, and deleting or
  updating a nonexistent id may return 405 instead of 404. Tests accept the plausible set of codes.
- `GET /ping` returns 201 rather than 200.
- Free hosting may have slow cold starts, so timeouts are generous.
- Boundary rules (price, dates) are undocumented; those tests are tagged `exploratory`.

## Future improvements
JSON schema validation, Allure reporting, parallel execution, a mock server (e.g. WireMock) for
offline runs, and a retry strategy for cold starts.
