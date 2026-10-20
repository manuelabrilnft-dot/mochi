# Mochi

A habit tracking web app with a virtual companion. You set goals and plan your
day, and a character called Mochi reacts to how well you keep to it. The aim was
to make habit tracking feel less like a checklist and more like looking after
something.

Built by a student team at the University of Twente as a multidisciplinary
project. Vue 3 on the front, a Jakarta EE REST API on the back, PostgreSQL
underneath.

## Features

- **Goals** — personal goals and daily goals, built from reusable templates
- **Ideal day** — plan a day as a set of time slots, then track what you did
- **Mochi companion** — a character you customise with faces, colours, costumes
  and scenes, and whose mood follows your progress
- **Challenges** — join a challenge and track participation
- **Friends** — add friends and see what they are doing
- **Achievements** — unlocked from activity over time
- **Calendar** — your activity across the month
- **Accounts** — signup, login and session handling, with guarded routes

## Tech stack

**Frontend**

- Vue 3 with the Composition API, built with Vite
- Tailwind CSS v4 with shadcn-vue and reka-ui components
- vee-validate with zod for form validation
- A small hand written router in `src/router.js` rather than vue-router, with
  auth guards and lazy loaded views
- One composable per domain in `src/composables`, so views stay thin

**Backend**

- Java 17 on Jakarta EE, packaged as a WAR
- JAX-RS via Jersey 3.1.5 for the REST layer, mounted at `/api`
- CDI for injection and JPA for persistence
- PostgreSQL
- jBCrypt for password hashing
- Token auth with `SecureRandom` tokens and a 30 minute expiry
- JUnit 5 tests covering the resource classes

## Architecture

The frontend is a single page app that talks to the API over REST. Each feature
has a composable that owns its API calls and state, and views read from those.

The API follows a resource per feature layout. Each resource class handles the
HTTP side and the repository handles database access. An auth filter checks the
token on every guarded request before the resource runs.

## Data model

Thirteen tables, including:

| Table | Holds |
| --- | --- |
| `Person` | accounts and profile data |
| `MochiEntity` | the companion and its current state |
| `PersonalGoal`, `DailyGoal`, `GoalTemplate` | the goal system |
| `IdealDay`, `Timeslot`, `Activity` | day planning and what was done |
| `Challenge`, `ChallengeParticipation` | challenges and who joined |
| `Achievement`, `AchievementAssociation` | achievements and who earned them |
| `Friendship` | the friend graph |

The full schema is in `design/db_schema.sql`, with sample data in
`design/population.sql`.

## Project structure

```
frontend/          Vue 3 single page app
  src/views/       one view per feature
  src/components/  shared and UI components
  src/composables/ API calls and state, one per domain
  src/models/      the Mochi character and its parts
mochi-api/         Jakarta EE REST API
  models/          JPA entities
  services/        JAX-RS resource classes
  repositories/    database access
  security/        token management and the auth filter
design/            class diagrams, use case diagrams, schema, mockups
assignments/       deliverables for each project phase
presentation/      final presentation
```

## Running it

**Frontend**

```
cd frontend
npm install
npm run dev
```

**Backend**

Needs Java 17, Maven and a servlet container such as Tomcat 10 or later.

```
cd mochi-api
mvn package
```

Deploy the WAR to the container. Database credentials are read from a `.env`
file at the API root:

```
DB_USER=your_user
DB_PASSWORD=your_password
```

Note that the connection points at the university database server
`bronto.ewi.utwente.nl`. Running this outside the university network means
pointing it at your own PostgreSQL instance and loading the schema from
`design/db_schema.sql`.

## Design documentation

The project ran in phases, and each one produced documents that are kept in
`assignments/`:

- Use case diagrams and class diagrams, each with a design decisions document
- Story maps for both releases
- Mockups and the reasoning behind them
- A user test report
- A security analysis report
- Acceptance criteria and a cost estimate

## Notes

Session tokens are held in memory, so they are lost when the API restarts and
the API cannot run as more than one instance. Moving them to the database or a
cache would be the first thing to change for real use.
