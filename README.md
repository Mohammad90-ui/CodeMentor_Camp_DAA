# CodeMentor Campus

A coding-practice tracker for students: browse problems, record attempts and mistakes, see progress, and get a
ranked **practice queue with a reason for every suggestion**.

**Stack:** React 18 (Vite) · Java 21 · Spring Boot 3.3 · Spring Security (JWT) · Spring Data JPA/Hibernate · MySQL · JUnit 5 + Mockito

```
codementor-campus/
├── backend/    Spring Boot API  (http://localhost:8080)
├── frontend/   React app        (http://localhost:5173)
└── docker-compose.yml           optional MySQL
```

## Run it

You need **JDK 21**, **Maven 3.9+**, **Node 18+**, and MySQL 8 (or Docker).

### 1. Database (MySQL)
```bash
docker compose up -d        # MySQL on :3306, user root / password root, database "codementor"
```
No Docker? Install MySQL yourself, then either use user `root`/`root` or set `DB_USER` and `DB_PASSWORD`
(`DB_URL` too if needed). The `codementor` database is created automatically.

> **No MySQL at all?** Use the built-in in-memory database instead (data resets on restart):
> `mvn spring-boot:run -Dspring-boot.run.profiles=h2`

### 2. Backend
```bash
cd backend
mvn spring-boot:run
```
On first start it creates the tables and loads 8 topics, 40 sample problems and a demo student.

### 3. Frontend
```bash
cd frontend
npm install
npm run dev
```
Open **http://localhost:5173** and log in with **demo@campus.edu / demo1234**, or register a new account.

### Tests
```bash
cd backend && mvn test      # 6 algorithm tests (JUnit 5) + 1 service test (Mockito)
```

## Features (matches the project plan)

| Page | What it does |
| --- | --- |
| Login / Register | JWT accounts, BCrypt-hashed passwords |
| Dashboard | totals, 7-day activity chart, weekly goal, weak topics, top 3 recommendations, recent activity |
| Problem Catalogue | search by title/topic, filter by topic & difficulty, sort by title/difficulty/time, bookmark |
| Problem Details | description, tags, external-site link, **your** attempt history |
| Record Attempt | Solved / Partially Solved / Unsolved, time spent, mistake type, notes |
| Practice Queue | ranked recommendations with score and explanation |
| Topic Progress | attempts, success rate and solved count per topic, with chart |
| Bookmarks · Profile | saved problems; preferred language and weekly goal |

## REST API

All endpoints except `/api/auth/*` need `Authorization: Bearer <token>`. Every query is scoped to the logged-in
user, so one student can never read or change another student's attempts or bookmarks.

| Method & path | Purpose |
| --- | --- |
| `POST /api/auth/register`, `POST /api/auth/login` | returns `{ token, user }` |
| `GET /api/me`, `PUT /api/me` | profile |
| `GET /api/topics` | topic names |
| `GET /api/problems?q=&topic=&difficulty=&sort=` | search / filter / sort |
| `GET /api/problems/{id}` | details + your attempts |
| `POST /api/attempts`, `GET /api/attempts` | record / list attempts |
| `GET /api/dashboard`, `GET /api/topics/progress` | progress data |
| `GET /api/recommendations?limit=10` | ranked practice queue |
| `GET /api/bookmarks`, `POST/DELETE /api/bookmarks/{problemId}` | bookmarks |

## The recommendation algorithm (DAA demo)

Code: `backend/src/main/java/com/codementor/recommendation/RecommendationEngine.java` (pure Java, no Spring).

1. **Topic statistics** – per topic: success rate over the latest 10 attempts (Solved = 1, Partial = 0.5, Unsolved = 0) and days since last practice.
2. **Scoring** – `topicScore = 60 × weakness + 40 × staleness`, where weakness = 1 − success rate and staleness = min(days, 14) / 14. A topic never tried counts as weakness 0.5 and staleness 1.0.
   Problem score = score of its weakest topic **+ 15** if its difficulty matches the student's level (+5 if adjacent) **+ 10** if attempted before but not solved. Solved problems are not eligible.
   Level: Easy until 3 Easy problems are solved, then Medium until 3 Medium are solved, then Hard.
3. **Priority queue + greedy** – all candidates go into a max-heap (`java.util.PriorityQueue`); we repeatedly poll the best one, allowing at most 3 per topic so the queue stays varied.
4. **Reason text** – generated from the same numbers, e.g. *"Practise Arrays next: your recent results in this topic were low (0% success), and you have not reviewed it for 12 days."*

| Concept | Where |
| --- | --- |
| Searching | `ProblemService.search` – title/topic match + filters |
| Sorting | `ProblemService.search` comparators; heap order in the engine |
| Greedy prioritisation | poll best candidate first, per-topic cap |
| Priority queue | `PriorityQueue<Scored>` in `RecommendationEngine` |

**Running time** (P problems, A attempts, k results): topic statistics O(A log A) · scoring O(P) · heap insertion O(P log P) · k polls O(k log P) → **O(A log A + P log P)**. Ranking 100,000 problems takes about 0.2 s in testing, so it scales far beyond a campus problem set.

Note: this is a helpful heuristic, not a guarantee that the order is perfect.

## 5-minute demo script
1. Log in as the demo student → Dashboard shows weak topics (Arrays, Sorting).
2. Problems → search "array" → open **Rotate Array**.
3. Record an **Unsolved** attempt with mistake **Logic error**.
4. Dashboard → topic progress updated. Practice Queue → an Arrays problem is on top, with its reason.
5. Record a **Solved** attempt on another Arrays problem → the queue and scores change.
6. Explain: scoring → priority queue → greedy selection (see the section above).

## Configuration
Environment variables: `DB_URL`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET` (use a long random value outside development).
