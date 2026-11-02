# CarrerX - Intelligent Job Matching, Personalization & Multi-Job Application System v5.0

An AI-driven Job Matching and Recommendation platform built using **Java**, **HTML5**, **CSS3**, and **JavaScript** in a modern **Light Frame** design. The system evaluates candidate resumes against a curated catalog of verified tech jobs, provides transparent multi-attribute match scoring (**60% Skills Weight**), enforces **Strict Experience Guards** (protecting Freshers from seeing Senior Manager roles), provides deep **Company & Location Intelligence** (climate, weather, annual holidays, joining timelines), supports **Advanced Personalization** with dedicated **3-Option Segmented Selectors** (Location, Work Mode, Working Hours, Bookmarks), and features an **End-to-End Multi-Job Application Engine** with **5 Defined Stages**.

---

## 🌟 Key Features

### 1. Multi-Job Application System (Apply to More Than One Job at Once)
Candidates can apply to multiple jobs simultaneously without retyping information:
- **Batch Multi-Apply**: Checkboxes on every job card (`Select to Apply`) allow selecting multiple roles across companies.
- **Floating/Toolbar Action Banner**: Real-time counter of selected jobs with 1-click **"🚀 Apply to Selected (X)"**.
- **1-Click Apply to All Bookmarked Jobs**: Immediately batches all bookmarked positions into a single multi-job application.
- **⚡ Select Top 3 Matches**: 1-click shortcut to select the candidate's top 3 algorithmically matched positions.
- **Single Modal Multi-Apply**: While applying to a single job, candidates can check additional recommended roles directly inside the modal to dispatch applications to all of them together.
- **AI-Tailored Multi-Application Statement**: Generates a unified statement of qualifications highlighting candidate competencies across all target organizations.
- **Batch Tracking Dispatch**: Backend `POST /api/apply/batch` creates unique tracking references (`APP-2026-XXXX`) for each company, initializes them to stage `Test/Assessment`, and surfaces them immediately on the user dashboard.

### 2. The Three Options System (Prominently Integrated)
The system incorporates clear 3-option interfaces throughout the user experience:
- **Work Mode (3 Options)**:
  1. 💻 **Online/Remote** — 100% remote / distributed
  2. 🏢 **Offline/On-site** — Physical campus office
  3. 🔄 **Hybrid** — Flexible 2-3 days office
  *(Available as a top segmented control on the sidebar, filter toolbar pills, and radio option cards in application forms)*
- **Location Selection (3 Options)**:
  1. 🏙️ **Preferred City** — Filter by tech hub cities (*San Francisco*, *Seattle*, *New York*, *Austin*, *Boston*, *Bengaluru*)
  2. 🗺️ **Preferred State / Region** — Filter by state (*California*, *Washington*, *New York*, *Texas*, *Massachusetts*, *Karnataka*)
  3. 💻 **100% Remote** — Worldwide distributed roles
- **Application Workflow (3 Options)**:
  1. 🎯 **Option 1: Single Job Application** (with tailored cover letter and optional additional role checkboxes)
  2. 🚀 **Option 2: Batch Multi-Apply** (select multiple job cards via checkboxes and submit at once)
  3. 🔖 **Option 3: 1-Click Apply to All Bookmarked Jobs** (apply to all saved positions in one stroke)

### 3. Tailored Cover Letter & Immediate Application Status Tracking
After the user submits a tailored application, the system immediately provides an **Application Status view** categorized into **5 clearly defined stages**:
1. 🟢 **Completely Selected** – The candidate has been selected for the position; official offer dispatched.
2. 🟠 **Waiting List** – The application is under active consideration and placed on the waiting list.
3. 🔴 **Rejected** – The application has not been selected.
4. 🔵 **Interview** – The candidate has been shortlisted for an interview round.
5. 🟣 **Test/Assessment** – The candidate has been shortlisted for a technical test or assessment round.

### 4. Live User Dashboard Application Tracker
- **Real-Time Pipeline Stepper**: Visual 5-stage progress indicator showing the candidate's exact stage.
- **Hiring Team Notes**: Displays real-time updates and notes from the recruiter/hiring team.
- **Interactive Stage Progression Simulator**: Allows users and evaluators to simulate moving applications forward/backward between any of the 5 stages with immediate server-sync.
- **Audit Timeline & Cover Letter Review**: Full historical log of every status transition with timestamps.
- **Job Card Indicators**: Jobs already applied display their current stage pill (e.g. `[🟣 Test/Assessment]`) with a direct **"Track Status 📊"** button.

### 5. Years of Experience & Strict Senior Role Guard
- Segmented career tiers: 🎓 **Fresher (0–1y)**, 💼 **Mid-Level (2–4y)**, 🚀 **Senior (5–7y)**, 👔 **Lead / Manager (8+y)**.
- **Strict Experience Guard (Active by default)**: When active, candidates in the **Fresher tier (0–1y)** will **never** see Senior, Lead, Executive Director, or Engineering Manager positions.

### 6. Multi-Attribute Weighted Scoring Engine
- 🧠 **Technical Skills Match — 60%** (Core required skills: 85% of skill score, bonus skills: 15%)
- ⏳ **Experience Level Alignment — 20%**
- 💰 **Salary Expectation Fit — 10%**
- 🌐 **Personalized Preferences (Work Mode, Location, Climate, Hours) — 10%**
- Interactive sliders allow users to customize weights at any time.

### 7. Comprehensive Company & Workplace Intelligence
Every job includes verified insights into:
- 🏢 **Company Profile**: Industry, company size, Glassdoor employee rating (⭐), founded year, CEO name, official website, and culture values.
- 📍 **Location Intelligence**: City, neighborhood, tech corridor details, cost of living index, and transit subsidies.
- ☀️ **Climate Guide**: Climate overview, average annual temperature, and sunny days per year.
- 📅 **When to Join**: Onboarding timeline, joining batch schedule, and workplace equipment policies.
- 🏖️ **Annual Holidays**: Total annual paid vacation days, paid sick & wellness days, year-end holiday shutdown, and parental leave.

---

## 🚀 How to Run

### Option 1: 1-Click Launch (Windows)
Double-click:
```bat
start_server.bat
```
Automatically compiles the Java backend with the bundled `ecj.jar` compiler, launches the server on port `8080`, and opens `http://localhost:8080` in your browser.

### Option 2: Command Line
```powershell
# Compile Java server (zero external dependencies)
java -jar ecj.jar -source 17 -target 17 JobMatcherServer.java

# Run Java HTTP server
java JobMatcherServer
```
Open **[http://localhost:8080](http://localhost:8080)** in any modern browser.

---

## 🔌 REST API Endpoints

| Endpoint | Method | Description |
|---|---|---|
| `GET /api/jobs` | `GET` | Retrieve jobs with query search & filtering (`?q=`, `?location=`, `?workMode=`, `?category=`, `?experienceLevel=`, `?climate=`, `?workingHours=`, `?minSalary=`, `?maxSalary=`, `?sort=`). |
| `POST /api/match` | `POST` | Calculates multi-attribute job matches (60% skills, 20% experience, 10% salary, 10% preferences), enforces fresher experience guard. |
| `POST /api/parse-resume` | `POST` | Extracts candidate name, email, phone, estimated years of experience, education degrees, and technical skills from raw resume text. |
| `POST /api/upload-resume` | `POST` | Pure-Java zero-dependency resume upload (PDF, DOCX, TXT) via multipart or JSON base64; extracts text and registers candidate profile in database. |
| `GET /api/saved-jobs` | `GET` | Retrieves saved/bookmarked jobs for candidate (`?email=...`) with full opportunity and company intelligence. |
| `POST /api/saved-jobs` | `POST` | Saves/bookmarks a job for candidate with automatic JSON database persistence (`data/saved_jobs.json`). |
| `POST /api/saved-jobs/remove` | `POST` | Removes a job bookmark for candidate with automatic database update. |
| `GET /api/stats/dashboard` | `GET` | Returns live calculated statistics: verified jobs, applications, bookmarks, registered candidates, actual average salary, and top skills. |
| `GET /api/stats/salary-benchmark` | `GET` | Calculates statistical market compensation percentiles (p10, p25, median/p50, p75, p90), experience tier distributions, and candidate market ranking. |
| `POST /api/apply` | `POST` | Submits a single job application with tailored cover letter, assigns tracking ID (`APP-2026-XXXX`), and activates initial stage (`Test/Assessment`). |
| `POST /api/apply/batch` | `POST` | Submits multi-job applications simultaneously for multiple `jobIds`, creates individual tracking records, and initializes all to `Test/Assessment`. |
| `POST /api/applications/update-status` | `POST` | Updates an application to any of the 5 defined stages (`Completely Selected`, `Waiting List`, `Rejected`, `Interview`, `Test/Assessment`) with audit timeline logging. |
| `GET /api/applications` | `GET` | Retrieves all submitted job applications with their 5-stage statuses, audit timelines, and cover letters. |
| `GET /api/skills` | `GET` | Returns full dictionary of known skills and alias synonyms. |
| `GET /api/health` | `GET` | System health check and server status. |

---

## 🗄️ Relational JSON Database Architecture (`data/`)

The backend maintains persistent, zero-dependency JSON tables in the `data/` directory:
- `data/jobs.json`: Catalog of 15 verified technology positions with company culture, climate guides, and compensation.
- `data/applications.json`: Submitted candidate applications with 5-stage progression histories and audit timestamps.
- `data/saved_jobs.json`: Bookmarked jobs per candidate.
- `data/candidates.json`: Registered candidate profiles, extracted skills, contact info, and education.
- `data/resumes.json`: Document upload records, filenames, byte sizes, and parsed skills.
- `data/skills.json`: Skills dictionary and canonical alias synonyms.
| `POST /api/parse-resume` | `POST` | Extracts skills, years of experience, contact info, and education from resume text. |
| `GET /api/jobs` | `GET` | Returns full catalog of tech jobs with full company profiles and location climate guides. |
| `GET /api/skills` | `GET` | Returns full set of known technical skills for autocomplete. |
| `GET /api/health` | `GET` | Returns server health, active job count, and total application count. |
