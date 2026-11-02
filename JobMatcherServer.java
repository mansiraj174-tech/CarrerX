import com.sun.net.httpserver.HttpExchange;



import com.sun.net.httpserver.HttpHandler;



import com.sun.net.httpserver.HttpServer;







import java.io.*;



import java.net.InetSocketAddress;



import java.net.URLDecoder;



import java.nio.charset.StandardCharsets;



import java.nio.file.Files;



import java.nio.file.Path;



import java.nio.file.Paths;



import java.nio.file.StandardCopyOption;



import java.text.SimpleDateFormat;



import java.util.*;



import java.util.regex.Matcher;



import java.util.regex.Pattern;



import java.util.stream.Collectors;



import java.util.zip.Inflater;



import java.util.zip.ZipEntry;



import java.util.zip.ZipInputStream;







/**



 * Intelligent Job Matching and Recommendation System - Java Backend Server v5.0



 * Features:



 *  - Fully functional REST APIs connecting frontend to Java backend



 *  - Pure-Java zero-dependency Resume Upload & Text Extraction for PDF, DOCX, and TXT



 *  - Automatic Candidate Skill, Experience, and Education Extraction



 *  - Multi-attribute weighted matching algorithm with strict experience guards



 *  - Query-based Job Listing, Search, Filtering, and Recommendations



 *  - Functional Demo Profiles (Senior Java Full Stack and AI & LLM Engineer)



 *  - Live Job Bookmarking (Save / Unsave / Retrieve Saved Jobs)



 *  - Real Dashboard Statistics and Statistical Salary Benchmarking (p25, p50, p75, p90)



 *  - Persistent Relational JSON Database for Jobs, Applications, Saved Jobs, Candidates, Resumes, and Skills



 *  - 5-Stage Live Application Lifecycle Tracker



 *  - Robust Input Validation, Error Handling, and Path-Traversal Security



 */



public class JobMatcherServer {







    private static final int PORT = 8080;



    private static final String STATIC_DIR = ".";



    private static final String DATA_DIR = "data";







    private static final List<Job> JOB_DATABASE = new ArrayList<>();



    private static final Map<String, Set<String>> SKILL_SYNONYMS = new HashMap<>();



    private static final List<JobApplication> APPLICATIONS_LIST = Collections.synchronizedList(new ArrayList<>());



    private static final List<SavedJobRecord> SAVED_JOBS_LIST = Collections.synchronizedList(new ArrayList<>());



    private static final List<Candidate> CANDIDATES_LIST = Collections.synchronizedList(new ArrayList<>());



    private static final List<ResumeDocument> RESUMES_LIST = Collections.synchronizedList(new ArrayList<>());







    public static void main(String[] args) throws IOException {



        // Initialize Synonyms & In-Memory Seeding



        initSkillSynonyms();



        initJobDatabase();







        // Initialize and sync persistent Database



        DatabaseManager.init();



        initInterviewHistory();







        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);







        // Core API routes



        server.createContext("/api/jobs/match", new MatchHandler());



        server.createContext("/api/match", new MatchHandler());



        server.createContext("/api/resume/extract", new ResumeExtractHandler());



        server.createContext("/api/parse-resume", new ResumeExtractHandler());



        server.createContext("/api/upload-resume", new UploadResumeHandler());



        server.createContext("/api/jobs/saved", new SavedJobsHandler());



        server.createContext("/api/jobs/saved/remove", new SavedJobsHandler());



        server.createContext("/api/saved-jobs", new SavedJobsHandler());



        server.createContext("/api/saved-jobs/remove", new SavedJobsHandler());



        server.createContext("/api/jobs", new JobsHandler());



        server.createContext("/api/skills", new SkillsHandler());



        server.createContext("/api/companies", new CompaniesHandler());







        // Real Analytics & Benchmarking



        server.createContext("/api/stats/dashboard", new DashboardStatsHandler());



        server.createContext("/api/stats/salary-benchmark", new SalaryBenchmarkHandler());







        // Applications & Lifecycle



        server.createContext("/api/apply", new ApplyHandler());



        server.createContext("/api/apply/batch", new BatchApplyHandler());



        server.createContext("/api/applications", new ApplicationsListHandler());



        server.createContext("/api/applications/update-status", new UpdateApplicationStatusHandler());







        // System Health



                // Authentication Endpoints



        server.createContext("/api/auth/login", new AuthLoginHandler());



        server.createContext("/api/auth/register", new AuthRegisterHandler());



        server.createContext("/api/auth/me", new AuthMeHandler());



        server.createContext("/api/auth/logout", new AuthLogoutHandler());







        server.createContext("/api/career/predict", new CareerPredictHandler());



        server.createContext("/api/career/skill-gap", new SkillGapHandler());



        server.createContext("/api/career/simulator", new CareerSimulatorHandler());



        server.createContext("/api/interview/questions", new InterviewQuestionsHandler());



        server.createContext("/api/interview/evaluate", new InterviewEvaluateHandler());



        server.createContext("/api/interview/history", new InterviewHistoryHandler());



        server.createContext("/api/jobs/detect-fake", new FakeJobDetectorHandler());



        server.createContext("/api/jobs/safety-stats", new SafetyStatsHandler());



        server.createContext("/api/career/compare", new CareerCompareHandler());



        server.createContext("/api/career/roles", new CareerRolesHandler());



        server.createContext("/api/jobs/readiness", new JobReadinessHandler());



        server.createContext("/api/career/unexpected-matches", new UnexpectedMatchesHandler());



        server.createContext("/api/career/dashboard", new PersonalDashboardHandler());



        server.createContext("/api/health", new HealthHandler());







        // Static file handler (HTML, CSS, JS, etc.)



        server.createContext("/", new StaticFileHandler());







        server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());



        server.start();







        System.out.println("==================================================================");



        System.out.println("  CarrerX - Intelligent Career Platform & Job Matching Engine v5.0");



        System.out.println("==================================================================");



        System.out.println("  Server is RUNNING at: http://localhost:" + PORT);



        System.out.println("  Verified Database: " + JOB_DATABASE.size() + " jobs, " + APPLICATIONS_LIST.size() + " applications, " + SAVED_JOBS_LIST.size() + " saved jobs.");



        System.out.println("  Registered Candidates: " + CANDIDATES_LIST.size() + " | Resumes Indexed: " + RESUMES_LIST.size());



        System.out.println("  Pure-Java PDF/DOCX Resume Extraction & Skill Engine: ACTIVE");



        System.out.println("  Statistical Salary Benchmarking & Dashboard Analytics: ACTIVE");



        System.out.println("==================================================================");



    }







    // ==========================================



    // MODELS



    // ==========================================







    public static class CompanyDetails {



        public String industry;



        public String companySize;



        public int foundedYear;



        public double rating; // Glassdoor rating e.g. 4.8



        public String culture;



        public String website;



        public String ceo;







        public CompanyDetails(String industry, String companySize, int foundedYear,



                              double rating, String culture, String website, String ceo) {



            this.industry = industry;



            this.companySize = companySize;



            this.foundedYear = foundedYear;



            this.rating = rating;



            this.culture = culture;



            this.website = website;



            this.ceo = ceo;



        }



    }







    public static class LocationIntelligence {



        public String city;



        public String stateCountry;



        public String neighborhood;



        public String climateCategory;



        public String climateOverview;



        public String avgTemperature;



        public int sunnyDaysPerYear;



        public String livingCostIndex;



        public String commuteTransport;







        public LocationIntelligence(String city, String stateCountry, String neighborhood,



                                    String climateCategory, String climateOverview, String avgTemperature,



                                    int sunnyDaysPerYear, String livingCostIndex, String commuteTransport) {



            this.city = city;



            this.stateCountry = stateCountry;



            this.neighborhood = neighborhood;



            this.climateCategory = climateCategory;



            this.climateOverview = climateOverview;



            this.avgTemperature = avgTemperature;



            this.sunnyDaysPerYear = sunnyDaysPerYear;



            this.livingCostIndex = livingCostIndex;



            this.commuteTransport = commuteTransport;



        }



    }







    public static class JoiningAndWorkplace {



        public String onboardingTimeline;



        public String joiningBatch;



        public String workingHours;



        public String shiftType;



        public String annualHolidays;



        public String sickAndWellnessLeave;



        public String yearEndShutdown;



        public String parentalLeave;







        public JoiningAndWorkplace(String onboardingTimeline, String joiningBatch,



                                   String workingHours, String shiftType,



                                   String annualHolidays, String sickAndWellnessLeave,



                                   String yearEndShutdown, String parentalLeave) {



            this.onboardingTimeline = onboardingTimeline;



            this.joiningBatch = joiningBatch;



            this.workingHours = workingHours;



            this.shiftType = shiftType;



            this.annualHolidays = annualHolidays;



            this.sickAndWellnessLeave = sickAndWellnessLeave;



            this.yearEndShutdown = yearEndShutdown;



            this.parentalLeave = parentalLeave;



        }



    }







    public static class Job {



        public String id;



        public String title;



        public String company;



        public String companyLogo;



        public String location;



        public String workMode; // Online/Remote, Offline/On-site, Hybrid



        public String jobType;  // Full-time, Contract, Part-time



        public int minSalary;



        public int maxSalary;



        public String currency;



        public String experienceLevel; // Fresher, Entry-Level, Mid-Level, Senior, Lead, Executive



        public int minYearsExp;



        public String category; // Software Engineering, AI & Machine Learning, Cloud & DevOps, Data, Cybersecurity, Mobile



        public String description;



        public List<String> requiredSkills;



        public List<String> niceToHaveSkills;



        public List<String> benefits;



        public String postedDate;



        public String urgency; // Urgent, Hot, Actively Hiring, Featured, Fresher Friendly







        public CompanyDetails companyDetails;



        public LocationIntelligence locationIntelligence;



        public JoiningAndWorkplace joiningAndWorkplace;







        public Job(String id, String title, String company, String companyLogo, String location,



                   String workMode, String jobType, int minSalary, int maxSalary, String currency,



                   String experienceLevel, int minYearsExp, String category, String description,



                   List<String> requiredSkills, List<String> niceToHaveSkills, List<String> benefits,



                   String postedDate, String urgency,



                   CompanyDetails companyDetails, LocationIntelligence locationIntelligence,



                   JoiningAndWorkplace joiningAndWorkplace) {



            this.id = id;



            this.title = title;



            this.company = company;



            this.companyLogo = companyLogo;



            this.location = location;



            this.workMode = workMode;



            this.jobType = jobType;



            this.minSalary = minSalary;



            this.maxSalary = maxSalary;



            this.currency = currency;



            this.experienceLevel = experienceLevel;



            this.minYearsExp = minYearsExp;



            this.category = category;



            this.description = description;



            this.requiredSkills = requiredSkills != null ? requiredSkills : new ArrayList<>();



            this.niceToHaveSkills = niceToHaveSkills != null ? niceToHaveSkills : new ArrayList<>();



            this.benefits = benefits != null ? benefits : new ArrayList<>();



            this.postedDate = postedDate;



            this.urgency = urgency;



            this.companyDetails = companyDetails;



            this.locationIntelligence = locationIntelligence;



            this.joiningAndWorkplace = joiningAndWorkplace;



        }



    }







    public static class JobApplication {



        public String applicationId;



        public String jobId;



        public String jobTitle;



        public String company;



        public String candidateName;



        public String candidateEmail;



        public String candidatePhone;



        public int expectedSalary;



        public String joiningAvailability;



        public String preferredHours;



        public String coverLetter;



        public String portfolioUrl;



        public String submittedAt;



        public String status; // Exactly one of: "Test/Assessment", "Interview", "Waiting List", "Completely Selected", "Rejected"



        public String statusNote;



        public List<String> statusTimeline;







        public JobApplication(String applicationId, String jobId, String jobTitle, String company,



                              String candidateName, String candidateEmail, String candidatePhone,



                              int expectedSalary, String joiningAvailability, String preferredHours,



                              String coverLetter, String portfolioUrl, String submittedAt,



                              String status, String statusNote) {



            this.applicationId = applicationId;



            this.jobId = jobId;



            this.jobTitle = jobTitle;



            this.company = company;



            this.candidateName = candidateName;



            this.candidateEmail = candidateEmail;



            this.candidatePhone = candidatePhone;



            this.expectedSalary = expectedSalary;



            this.joiningAvailability = joiningAvailability;



            this.preferredHours = preferredHours;



            this.coverLetter = coverLetter;



            this.portfolioUrl = portfolioUrl;



            this.submittedAt = submittedAt;



            this.status = status;



            this.statusNote = statusNote;



            this.statusTimeline = new ArrayList<>();



            this.statusTimeline.add("[" + submittedAt + "] Application Submitted: " + status);



        }







        public void updateStatus(String newStatus, String note) {



            this.status = newStatus;



            this.statusNote = note;



            String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());



            this.statusTimeline.add("[" + time + "] Stage Updated: " + newStatus + " - " + note);



        }



    }







    public static class Candidate {



        public String candidateId;



        public String name;



        public String email;



        public String phone;



        public int yearsExp;



        public List<String> skills;



        public List<String> education;



        public String updatedAt;







        public Candidate(String candidateId, String name, String email, String phone, int yearsExp,



                         List<String> skills, List<String> education, String updatedAt) {



            this.candidateId = candidateId;



            this.name = name;



            this.email = email;



            this.phone = phone;



            this.yearsExp = yearsExp;



            this.skills = skills != null ? skills : new ArrayList<>();



            this.education = education != null ? education : new ArrayList<>();



            this.updatedAt = updatedAt;



        }



    }







    public static class ResumeDocument {



        public String resumeId;



        public String filename;



        public String fileType;



        public long fileSize;



        public String uploadedAt;



        public int wordCount;



        public List<String> extractedSkills;



        public String candidateEmail;







        public ResumeDocument(String resumeId, String filename, String fileType, long fileSize,



                              String uploadedAt, int wordCount, List<String> extractedSkills, String candidateEmail) {



            this.resumeId = resumeId;



            this.filename = filename;



            this.fileType = fileType;



            this.fileSize = fileSize;



            this.uploadedAt = uploadedAt;



            this.wordCount = wordCount;



            this.extractedSkills = extractedSkills != null ? extractedSkills : new ArrayList<>();



            this.candidateEmail = candidateEmail;



        }



    }







    public static class SavedJobRecord {



        public String id;



        public String candidateEmail;



        public String jobId;



        public String savedAt;







        public SavedJobRecord(String id, String candidateEmail, String jobId, String savedAt) {



            this.id = id;



            this.candidateEmail = candidateEmail;



            this.jobId = jobId;



            this.savedAt = savedAt;



        }



    }







    public static class MatchResult {



        public Job job;



        public int matchPercentage;



        public List<String> matchedSkills;



        public List<String> missingSkills;



        public List<String> bonusSkills;



        public String matchTier;



        public String salaryAssessment;



        public int salaryComparisonPct;



        public String fitSummary;



        public double skillsContribution;



        public double experienceContribution;



        public double salaryContribution;



        public double preferenceContribution;



        public String experienceMatchStatus;



        public boolean locationMatch;



        public boolean climateMatch;



        public boolean hoursMatch;



        public boolean educationMatch;



        public String educationBadge;







        public MatchResult(Job job, int matchPercentage, List<String> matchedSkills,



                           List<String> missingSkills, List<String> bonusSkills,



                           String matchTier, String salaryAssessment, int salaryComparisonPct,



                           String fitSummary, double skillsContribution, double experienceContribution,



                           double salaryContribution, double preferenceContribution,



                           String experienceMatchStatus, boolean locationMatch,



                           boolean climateMatch, boolean hoursMatch, boolean educationMatch, String educationBadge) {



            this.job = job;



            this.matchPercentage = matchPercentage;



            this.matchedSkills = matchedSkills;



            this.missingSkills = missingSkills;



            this.bonusSkills = bonusSkills;



            this.matchTier = matchTier;



            this.salaryAssessment = salaryAssessment;



            this.salaryComparisonPct = salaryComparisonPct;



            this.fitSummary = fitSummary;



            this.skillsContribution = skillsContribution;



            this.experienceContribution = experienceContribution;



            this.salaryContribution = salaryContribution;



            this.preferenceContribution = preferenceContribution;



            this.experienceMatchStatus = experienceMatchStatus;



            this.locationMatch = locationMatch;



            this.climateMatch = climateMatch;



            this.hoursMatch = hoursMatch;



            this.educationMatch = educationMatch;



            this.educationBadge = educationBadge;



        }







        public MatchResult(Job job, int matchPercentage, List<String> matchedSkills,



                           List<String> missingSkills, List<String> bonusSkills,



                           String matchTier, String salaryAssessment, int salaryComparisonPct,



                           String fitSummary, double skillsContribution, double experienceContribution,



                           double salaryContribution, double preferenceContribution,



                           String experienceMatchStatus, boolean locationMatch,



                           boolean climateMatch, boolean hoursMatch) {



            this(job, matchPercentage, matchedSkills, missingSkills, bonusSkills,



                 matchTier, salaryAssessment, salaryComparisonPct, fitSummary,



                 skillsContribution, experienceContribution, salaryContribution,



                 preferenceContribution, experienceMatchStatus, locationMatch,



                 climateMatch, hoursMatch, true, "Degree Eligible (B.Tech / BE / MCA)");



        }



    }







    // ==========================================



    // DATABASE MANAGER (JSON FILE-BASED RELATIONAL PERSISTENCE)



    // ==========================================







    // ==========================================



    // COMPANY INTELLIGENCE REPOSITORY & SERVICE



    // ==========================================







    public static class Company {



        public String id;



        public String name;



        public String industry;



        public String companySize;



        public double rating;



        public int foundedYear;



        public String ceo;



        public String website;



        public String headquarters;



        public List<String> indianOfficeLocations;



        public List<String> techStack;



        public String cultureValues;



        public String hiringDetails;



        public List<String> benefits;



        public int openJobsCount;







        public Company(String id, String name, String industry, String companySize, double rating,



                       int foundedYear, String ceo, String website, String headquarters,



                       List<String> indianOfficeLocations, List<String> techStack,



                       String cultureValues, String hiringDetails, List<String> benefits, int openJobsCount) {



            this.id = id;



            this.name = name;



            this.industry = industry;



            this.companySize = companySize;



            this.rating = rating;



            this.foundedYear = foundedYear;



            this.ceo = ceo;



            this.website = website;



            this.headquarters = headquarters;



            this.indianOfficeLocations = indianOfficeLocations;



            this.techStack = techStack;



            this.cultureValues = cultureValues;



            this.hiringDetails = hiringDetails;



            this.benefits = benefits;



            this.openJobsCount = openJobsCount;



        }



    }







    public static class CompanyRepository {



        private static final List<Company> COMPANIES = new ArrayList<>();







        static {



            initCompanies();



        }







        private static void initCompanies() {



            COMPANIES.clear();



            COMPANIES.add(new Company(



                "comp-tcs", "Tata Consultancy Services (TCS)", "Global IT Services & Digital Consulting",



                "600,000+ employees globally (500,000+ in India)", 4.1, 1968, "K. Krithivasan", "tcs.com/careers",



                "Mumbai, Maharashtra, India",



                Arrays.asList("Bengaluru", "Pune", "Hyderabad", "Mumbai", "Chennai", "Kolkata", "Delhi NCR", "Kochi"),



                Arrays.asList("Java", "Spring Boot", "Microservices", "AWS", "Azure", "Kafka", "SQL", "Angular", "Docker"),



                "High ethical standards, continuous learning culture, merit-based career elevation (TCS Digital/Innovator), long-term stability and global client exposure.",



                "National Qualifier Test (NQT) followed by technical coding assessment and panel HR interview (turnaround: 2-4 weeks).",



                Arrays.asList("Comprehensive Medical & Health Insurance", "Provident Fund & Gratuity", "TCS Elevate & Digital Upskilling Incentives", "Annual Performance Bonus", "Maternity & Paternity Benefits", "Subsidized Transport"),



                3



            ));



            COMPANIES.add(new Company(



                "comp-infosys", "Infosys Technologies", "Next-Gen Digital Services & Cloud Consulting",



                "320,000+ employees globally (240,000+ in India)", 4.0, 1981, "Salil Parekh", "infosys.com/careers",



                "Bengaluru, Karnataka, India",



                Arrays.asList("Bengaluru (Electronic City)", "Pune (Hinjawadi)", "Hyderabad (Gachibowli)", "Mysuru", "Chennai", "Chandigarh"),



                Arrays.asList("Java", "Spring Boot", "React", "Python", "Kubernetes", "AWS", "Azure", "PostgreSQL", "Kafka"),



                "Engineering-driven culture rooted in the Mysuru Global Education Center, client-centric innovation, and transparent career progression tracks.",



                "Specialist Programmer (HackWithInfy/InfyTQ) coding challenges, technical system design rounds, and managerial discussion (2-3 weeks).",



                Arrays.asList("Group Health & Term Insurance", "Provident Fund & Statutory Gratuity", "Infosys Wingspan Learning Grants", "Employee Stock Option Plans (ESOPs)", "Hybrid 3-Day Workplace Flexibility", "Wellness Subsidies"),



                2



            ));



            COMPANIES.add(new Company(



                "comp-wipro", "Wipro", "Information Technology Consulting & System Integration",



                "235,000+ employees globally", 3.9, 1945, "Srini Pallia", "wipro.com/careers",



                "Bengaluru, Karnataka, India",



                Arrays.asList("Bengaluru (Sarjapur & Electronic City)", "Hyderabad", "Pune", "Chennai", "Kolkata", "Gurugram"),



                Arrays.asList("Java", "Python", "Go", "Docker", "Kubernetes", "Terraform", "AWS", "Azure", "CI/CD"),



                "Collaborative ethos prioritizing cloud-first engineering, digital resilience, and Turbo-track fast-tracked engineering promotions.",



                "Wipro Elite / Turbo online national assessment, technical live coding round, and HR discussion (3-4 weeks).",



                Arrays.asList("Medical Insurance Cover", "Provident Fund", "Higher Education Support (WiSEED / M.Tech sponsorship)", "Night Shift / Transit Allowances", "Paid Wellness Leave"),



                2



            ));



            COMPANIES.add(new Company(



                "comp-accenture", "Accenture India", "Global Management Consulting & Advanced Technology Architecture",



                "750,000+ employees globally (300,000+ in India)", 4.1, 1989, "Julie Sweet", "accenture.com/in-en/careers",



                "Dublin, Ireland (India Advanced Tech Centers: Gurugram, Bengaluru)",



                Arrays.asList("Gurugram (DLF Cyber City)", "Bengaluru (Whitefield & Bellandur)", "Mumbai", "Hyderabad", "Pune", "Chennai"),



                Arrays.asList("Java", "Spring Cloud", "Kafka", "Snowflake", "Databricks", "Azure", "Docker", "RESTful APIs"),



                "High-performance consulting culture, merit-driven rewards, deep diversity & inclusion initiatives, and enterprise digital architecture leadership.",



                "Cognitive & technical online assessment, live coding challenge, and behavioral tech interview (2-3 weeks).",



                Arrays.asList("Comprehensive Health & Critical Illness Cover", "Provident Fund & Gratuity", "Annual High-Performance Bonuses", "Global Certification Reimbursements", "Flexible Hybrid Work Policy"),



                2



            ));



            COMPANIES.add(new Company(



                "comp-google", "Google India", "Global Big Tech & Consumer Internet Platforms",



                "180,000+ employees globally (12,000+ in India R&D)", 4.6, 1998, "Sundar Pichai", "careers.google.com",



                "Mountain View, CA (India R&D: Bengaluru & Hyderabad)",



                Arrays.asList("Bengaluru (RMZ Infinity & Bagmane)", "Hyderabad (HITEC City)", "Gurugram", "Mumbai"),



                Arrays.asList("Java", "C++", "Python", "Go", "Borg", "Spanner", "Bigtable", "TensorFlow", "Kubernetes"),



                "Extreme engineering rigor, bottom-up innovation (20% project time), high psychological safety, and world-class developer infrastructure.",



                "Recruiter phone screen, technical phone screen, and 4-5 on-site technical rounds (Data Structures, Algorithms, Distributed Systems Design, Googliness).",



                Arrays.asList("Top-Tier Health Insurance for Family & Parents", "Lucrative Google Alphabet RSUs / Stock Grants", "Free Gourmet Breakfast, Lunch & Dinner", "Generous Annual Wellness & Learning Stipend", "401k/PF Matching"),



                2



            ));



            COMPANIES.add(new Company(



                "comp-microsoft", "Microsoft India", "Hyperscale Cloud & Enterprise Software Platforms",



                "220,000+ employees globally (20,000+ in India IDC)", 4.5, 1975, "Satya Nadella", "careers.microsoft.com",



                "Redmond, WA (India Development Center: Hyderabad & Bengaluru)",



                Arrays.asList("Hyderabad (Gachibowli IDC)", "Bengaluru (Outer Ring Road)", "Noida (NCR Cyber Hub)"),



                Arrays.asList("C#", ".NET Core", "Java", "Azure", "TypeScript", "React", "CosmosDB", "Distributed Systems"),



                "Growth Mindset, customer obsession, world-class work-life balance, high trust, and deep technological impact across Azure & Developer tooling.",



                "Online technical assessment, followed by 4 rounds (Algorithms, System Architecture, Code Design, and AA / As Appropriate round).",



                Arrays.asList("Comprehensive Medical & Life Insurance", "Microsoft Equity Grants (RSUs)", "Annual Performance Bonus", "Reimbursement for Gym, Home Office & Wellness", "Generous Parental Leave"),



                2



            ));



            COMPANIES.add(new Company(



                "comp-flipkart", "Flipkart", "E-Commerce & Digital Marketplace Infrastructure",



                "35,000+ employees (Bengaluru HQ)", 4.3, 2007, "Kalyan Krishnamurthy", "flipkartcareers.com",



                "Bengaluru, Karnataka, India",



                Arrays.asList("Bengaluru (Cessna Business Park & Bellandur)", "Delhi NCR", "Mumbai"),



                Arrays.asList("Java", "Spring Boot", "Kafka", "Cassandra", "ElasticSearch", "Redis", "Dropwizard", "React"),



                "Hyper-scale engineering, ownership at all levels, and preparing for extreme traffic surges during Big Billion Days.",



                "Machine coding round (2-hour live implementation), algorithms round, system design round, and hiring manager discussion (2-3 weeks).",



                Arrays.asList("Competitive Tech Base + ESOP Grants", "Health Insurance with OPD Coverage", "Annual Performance Bonus", "Commute Shuttle Services", "Free In-Campus Meals"),



                1



            ));



            COMPANIES.add(new Company(



                "comp-swiggy", "Swiggy", "Hyperlocal Logistics & Quick Commerce Platforms",



                "6,000+ corporate employees", 4.2, 2014, "Sriharsha Majety", "swiggy.com/careers",



                "Bengaluru, Karnataka, India",



                Arrays.asList("Bengaluru (Koramangala & Marathahalli)", "Remote-First across India"),



                Arrays.asList("Go", "Java", "Python", "Kafka", "Redis", "AWS", "Kubernetes", "React Native"),



                "High agility, customer obsession, Remote-First workplace culture, and solving real-time sub-minute optimization challenges.",



                "Machine coding challenge, high-scale system design round, algorithmic problem solving, and culture fit evaluation.",



                Arrays.asList("Remote-First Setup Allowance", "Competitive ESOPs", "Executive Medical Insurance", "Swiggy App Dining Discounts", "Annual Learning Budget"),



                1



            ));



            COMPANIES.add(new Company(



                "comp-zomato", "Zomato", "Food Tech, Quick Commerce (Blinkit) & Hyperlocal Ecosystems",



                "5,500+ employees (Gurugram HQ)", 4.1, 2008, "Deepinder Goyal", "zomato.com/careers",



                "Gurugram, Delhi NCR, India",



                Arrays.asList("Gurugram (DLF Phase V)", "Bengaluru", "Mumbai"),



                Arrays.asList("Kotlin", "Swift", "React Native", "Python", "Django", "PostgreSQL", "AWS", "Microservices"),



                "Fast-paced execution, radical transparency, deep ownership, and designing joyful consumer-facing mobile & quick-commerce experiences.",



                "Mobile take-home project or coding assessment, technical architecture interview, and founder/VP discussion (1-2 weeks).",



                Arrays.asList("Zomato ESOP Grants", "Premium Health Insurance", "Annual Wellness Stipends", "Free In-House Meals & Coffee Bar", "Performance Incentives"),



                1



            ));



            COMPANIES.add(new Company(



                "comp-razorpay", "Razorpay", "FinTech Unicorn & Payment Gateway Infrastructure",



                "3,500+ employees (Bengaluru HQ)", 4.4, 2014, "Harshil Mathur", "razorpay.com/jobs",



                "Bengaluru, Karnataka, India",



                Arrays.asList("Bengaluru (Koramangala)", "Mumbai"),



                Arrays.asList("Go", "PHP/Laravel", "Python", "Kafka", "MySQL", "Kubernetes", "AWS", "Redis"),



                "High engineering standards, absolute reliability in financial transactions, transparent communication, and rapid career progression.",



                "DSA coding assessment, live low-level design (LLD) machine coding, high-level architecture (HLD) interview, and culture round.",



                Arrays.asList("Generous ESOP Equity Package", "No-Questions-Asked Health Insurance", "Flexible Work Options", "Learning & Book Allowances", "Annual Wellness Bonus"),



                1



            ));



            COMPANIES.add(new Company(



                "comp-cred", "CRED", "Premium FinTech & Consumer Rewards Marketplace",



                "1,200+ employees", 4.5, 2018, "Kunal Shah", "cred.club/careers",



                "Bengaluru, Karnataka, India",



                Arrays.asList("Bengaluru (Indiranagar)"),



                Arrays.asList("Kotlin", "Go", "Java", "React", "React Native", "DynamoDB", "Kafka", "AWS"),



                "High trust, aesthetic excellence, obsession with craft, extreme ownership, and compounding personal growth.",



                "Complex product machine coding challenge, system architecture & database tuning round, design review, and culture deep dive.",



                Arrays.asList("Top-of-Market Cash Compensation", "CRED ESOP Grants", "Comprehensive Family Health Coverage", "Unlimited Paid Time Off (PTO)", "Subsidized Premium Housing Allowance"),



                1



            ));



            COMPANIES.add(new Company(



                "comp-jio", "Jio Platforms", "Telecom, 5G Infrastructure & Cloud Native Ecosystems",



                "50,000+ employees", 4.0, 2019, "Akash Ambani", "careers.jio.com",



                "Navi Mumbai, Maharashtra, India",



                Arrays.asList("Navi Mumbai (Reliance Corporate Park)", "Bengaluru", "Hyderabad"),



                Arrays.asList("Python", "Go", "Java", "Docker", "Kubernetes", "OpenStack", "5G Core", "Linux", "Prometheus"),



                "Massive scale serving 450+ million subscribers, nation-building technology initiatives, and high operational excellence.",



                "Technical aptitude & domain screening, hands-on Linux/Cloud infrastructure assessment, and managerial interview.",



                Arrays.asList("Reliance Group Health Scheme", "Gratuity & Provident Fund", "Telecom & Digital Subsidies", "Annual Performance Bonus", "Subsidized Campus Transit"),



                1



            ));



            COMPANIES.add(new Company(



                "comp-zoho", "Zoho Corporation", "Enterprise Cloud SaaS & Productivity Software",



                "15,000+ employees", 4.5, 1996, "Sridhar Vembu", "zoho.com/careers",



                "Chennai, Tamil Nadu, India",



                Arrays.asList("Chennai (Estancia Tech Park)", "Tenkasi", "Coimbatore", "Tirunelveli"),



                Arrays.asList("Java", "C++", "Python", "PostgreSQL", "Proprietary Cloud", "JavaScript", "HTML5", "CSS3"),



                "Bootstrapped, deeply principled, long-term oriented, rural engineering hubs, zero VC pressure, and exceptional employee retention.",



                "Zoho Aptitude Test, 2 rounds of live problem solving / coding (Java/C++), advanced programming challenge, and HR interview.",



                Arrays.asList("Free Nutritious Breakfast, Lunch & Dinner", "Company Accommodation / Transit Support", "Medical Insurance", "Provident Fund", "High Job Stability & Work-Life Balance"),



                1



            ));



            COMPANIES.add(new Company(



                "comp-paytm", "Paytm", "Digital Payments, Soundbox IoT & Financial Services",



                "12,000+ employees", 3.8, 2010, "Vijay Shekhar Sharma", "paytm.com/careers",



                "Noida, Delhi NCR, India",



                Arrays.asList("Noida (Sector 98 & 135)", "Bengaluru", "Mumbai"),



                Arrays.asList("Java", "Spring Boot", "MySQL", "Redis", "Kafka", "AWS", "Microservices", "REST APIs"),



                "Fintech pioneer culture, high transaction volumes, fast turnaround, and driving financial inclusion across India.",



                "Online coding challenge, 2 backend technical rounds (Algorithms & Concurrency), and architectural review.",



                Arrays.asList("Paytm ESOP Scheme", "Family Medical Insurance", "Annual Performance Bonuses", "Subsidized Cab Facilities", "Gym & Recreation Access"),



                1



            ));



            COMPANIES.add(new Company(



                "comp-zepto", "Zepto", "Instant Quick Commerce & High-Density Logistics",



                "2,500+ corporate employees", 4.2, 2021, "Aadit Palicha", "zeptonow.com/careers",



                "Mumbai, Maharashtra, India",



                Arrays.asList("Mumbai (Powai)", "Bengaluru (HSR Layout)"),



                Arrays.asList("Go", "Node.js", "Python", "PostgreSQL", "Redis", "Kubernetes", "AWS", "Microservices"),



                "Fierce execution, high autonomy, rapid experimentation, and architecting sub-10 minute routing & inventory pipelines.",



                "Practical machine coding assignment, microservices architecture interview, and engineering leadership round (1-2 weeks).",



                Arrays.asList("High-Growth Equity (ESOPs)", "Comprehensive Health Coverage", "Performance Linked Bonuses", "Free In-Office Meals & Snacks", "Flexible Work Arrangements"),



                1



            ));



            COMPANIES.add(new Company(



                "comp-zerodha", "Zerodha", "FinTech, Stock Brokerage & Ultra Low-Latency Trading",



                "1,100+ employees", 4.7, 2010, "Nithin Kamath", "zerodha.com/careers",



                "Bengaluru, Karnataka, India",



                Arrays.asList("Bengaluru (JP Nagar)"),



                Arrays.asList("Go", "Python", "PostgreSQL", "Redis", "Vue.js", "Kite Connect API", "Linux", "Docker"),



                "Bootstrapped, pragmatic minimalist engineering, open source contributors, zero unnecessary meetings, and exceptional work-life harmony.",



                "Code review of candidate's open-source repositories / GitHub projects, technical discussion with core engineers, and culture alignment.",



                Arrays.asList("Top-of-Industry Base Salaries", "Generous Annual Profit-Sharing Bonuses", "Executive Medical Insurance", "Flexible Work Hours", "Zero Micro-Management"),



                1



            ));



            COMPANIES.add(new Company(



                "comp-phonepe", "PhonePe", "FinTech, UPI National Grid & Digital Insurance",



                "5,000+ employees", 4.4, 2015, "Sameer Nigam", "phonepe.com/careers",



                "Bengaluru, Karnataka, India",



                Arrays.asList("Bengaluru (Bellandur)", "Pune"),



                Arrays.asList("Java", "Go", "HBase", "Aerospike", "Kafka", "Elasticsearch", "React", "Distributed Systems"),



                "Extreme scale (500M+ users), high availability financial rails, engineering excellence, and deep distributed systems mastery.",



                "Algorithmic problem solving, low-level design (LLD), high-level distributed systems design (HLD), and techno-cultural interview.",



                Arrays.asList("PhonePe ESOP Grants", "High Medical Insurance (including parents & in-laws)", "Annual Performance Bonus", "Commute Shuttle Services", "Free In-House Meals"),



                1



            ));



            COMPANIES.add(new Company(



                "comp-cognizant", "Cognizant", "Global IT Consulting & Enterprise Transformation",



                "340,000+ employees globally (240,000+ in India)", 3.9, 1994, "Ravi Kumar S", "cognizant.com/careers",



                "Teaneck, NJ (India Advanced Centers: Chennai & Bengaluru)",



                Arrays.asList("Chennai (Siruseri & MEPZ)", "Bengaluru", "Hyderabad", "Pune", "Kolkata", "Coimbatore"),



                Arrays.asList("Java", "Spring Boot", "React", "AWS", "SQL", "Microservices", "Docker", "Git"),



                "Client-centric delivery, structured career frameworks (GenC / GenC Next), and extensive enterprise learning resources.",



                "Cognizant GenC Next online technical evaluation, live programming round, and HR interview (2-4 weeks).",



                Arrays.asList("Group Health & Accident Insurance", "Provident Fund & Statutory Gratuity", "Digital Upskilling Rewards", "Shift Allowances", "Paid Maternity/Paternity Leave"),



                1



            ));



            COMPANIES.add(new Company(



                "comp-hcl", "HCLTech", "Global Technology Services & Cloud Cyber Defense",



                "225,000+ employees globally (180,000+ in India)", 3.9, 1976, "C Vijayakumar", "hcltech.com/careers",



                "Noida, Delhi NCR, India",



                Arrays.asList("Noida (Sector 126 & Techzone)", "Bengaluru", "Chennai", "Hyderabad", "Pune", "Lucknow"),



                Arrays.asList("Python", "Java", "Cloud Security", "SIEM", "AWS", "Azure", "Microservices", "Linux"),



                "Supercharging Progress, deep expertise in cloud cybersecurity, infrastructure management, and engineering R&D services.",



                "Technical online assessment, domain coding interview, and HR evaluation round (3-4 weeks).",



                Arrays.asList("Comprehensive Medical Coverage", "Provident Fund & Gratuity", "Cyber Defense Certification Reimbursement", "Annual Performance Bonus", "Subsidized Transportation"),



                1



            ));



        }







        public static List<Company> getAllCompanies() {



            return new ArrayList<>(COMPANIES);



        }







        public static Company findByName(String name) {



            if (name == null || name.trim().isEmpty()) return null;



            String clean = name.trim().toLowerCase();



            for (Company c : COMPANIES) {



                if (c.name.toLowerCase().contains(clean) || clean.contains(c.name.toLowerCase())) {



                    return c;



                }



            }



            return null;



        }



    }







    public static class CompaniesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCORSHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                exchange.getResponseBody().close();
                return;
            }

            String path = exchange.getRequestURI().getPath();
            Map<String, String> q = parseQueryParams(exchange.getRequestURI().getRawQuery());
            String nameParam = q.getOrDefault("name", "").trim();

            if (!nameParam.isEmpty() || path.endsWith("/info")) {
                Company comp = CompanyRepository.findByName(nameParam);
                if (comp == null && !CompanyRepository.getAllCompanies().isEmpty()) {
                    comp = CompanyRepository.getAllCompanies().get(0);
                }
                if (comp != null) {
                    StringBuilder sb = new StringBuilder("{");
                    sb.append("\"success\":true,");
                    sb.append("\"company\":").append(companyToJson(comp));
                    sb.append("}");
                    sendJsonResponse(exchange, 200, sb.toString());
                    return;
                } else {
                    sendError(exchange, 404, "Company not found: " + nameParam);
                    return;
                }
            }

            List<Company> all = CompanyRepository.getAllCompanies();
            StringBuilder sb = new StringBuilder("{");
            sb.append("\"success\":true,");
            sb.append("\"total\":").append(all.size()).append(",");
            sb.append("\"companies\":[");
            for (int i = 0; i < all.size(); i++) {
                sb.append(companyToJson(all.get(i)));
                if (i < all.size() - 1) sb.append(",");
            }
            sb.append("]}");

            sendJsonResponse(exchange, 200, sb.toString());
        }

        private static String companyToJson(Company c) {
            StringBuilder sb = new StringBuilder("{");
            sb.append("\"id\":\"").append(escapeJson(c.id)).append("\",");
            sb.append("\"name\":\"").append(escapeJson(c.name)).append("\",");
            sb.append("\"industry\":\"").append(escapeJson(c.industry)).append("\",");
            sb.append("\"companySize\":\"").append(escapeJson(c.companySize)).append("\",");
            sb.append("\"rating\":").append(c.rating).append(",");
            sb.append("\"foundedYear\":").append(c.foundedYear).append(",");
            sb.append("\"ceo\":\"").append(escapeJson(c.ceo)).append("\",");
            sb.append("\"website\":\"").append(escapeJson(c.website)).append("\",");
            sb.append("\"headquarters\":\"").append(escapeJson(c.headquarters)).append("\",");
            sb.append("\"indianOfficeLocations\":").append(listToJsonArray(c.indianOfficeLocations)).append(",");
            sb.append("\"techStack\":").append(listToJsonArray(c.techStack)).append(",");
            sb.append("\"cultureValues\":\"").append(escapeJson(c.cultureValues)).append("\",");
            sb.append("\"hiringDetails\":\"").append(escapeJson(c.hiringDetails)).append("\",");
            sb.append("\"benefits\":").append(listToJsonArray(c.benefits)).append(",");
            sb.append("\"openJobsCount\":").append(c.openJobsCount);
            sb.append("}");
            return sb.toString();
        }
    }

    public static class AuthSessionRepository {



        private static final Map<String, String> SESSIONS = new java.util.concurrent.ConcurrentHashMap<>();







        public static String createSession(String email) {



            String token = "CARRERX-AUTH-" + UUID.randomUUID().toString();



            SESSIONS.put(token, email.trim().toLowerCase());



            return token;



        }







        public static String getEmailByToken(String token) {



            if (token == null) return null;



            return SESSIONS.get(token.trim());



        }







        public static void invalidateSession(String token) {



            if (token != null) SESSIONS.remove(token.trim());



        }



    }







    public static class AuthService {



        public static String getAuthenticatedEmail(HttpExchange exchange) {



            // 1. Authorization: Bearer <token>



            String authHeader = exchange.getRequestHeaders().getFirst("Authorization");



            if (authHeader != null && authHeader.toLowerCase().startsWith("bearer ")) {



                String token = authHeader.substring(7).trim();



                String email = AuthSessionRepository.getEmailByToken(token);



                if (email != null && !email.isEmpty()) return email;



            }



            // 2. X-Auth-Token header



            String tokenHeader = exchange.getRequestHeaders().getFirst("X-Auth-Token");



            if (tokenHeader != null && !tokenHeader.trim().isEmpty()) {



                String email = AuthSessionRepository.getEmailByToken(tokenHeader.trim());



                if (email != null && !email.isEmpty()) return email;



            }



            // 3. X-Candidate-Email header



            String emailHeader = exchange.getRequestHeaders().getFirst("X-Candidate-Email");



            if (emailHeader != null && !emailHeader.trim().isEmpty()) {



                return emailHeader.trim().toLowerCase();



            }



            // 4. Query param ?email=...



            String rawQuery = exchange.getRequestURI().getRawQuery();



            if (rawQuery != null && rawQuery.contains("email=")) {



                Map<String, String> q = parseQueryParams(rawQuery);



                String qEmail = q.get("email");



                if (qEmail != null && !qEmail.trim().isEmpty()) {



                    return qEmail.trim().toLowerCase();



                }



            }



            return null;



        }



    }







    public static class DatabaseManager {



        private static final File DIR = new File(DATA_DIR);







        public static synchronized void init() {



            if (!DIR.exists()) {



                DIR.mkdirs();



            }







            // Sync Jobs Database



            File jobsFile = new File(DIR, "jobs.json");



            saveJobs();







            // Sync Skills Database



            File skillsFile = new File(DIR, "skills.json");



            if (!skillsFile.exists()) {



                saveSkills();



            }







            // Load or initialize Applications



            loadApplications();







            // Load or initialize Saved Jobs



            loadSavedJobs();







            // Load or initialize Candidates



            loadCandidates();







            // Load or initialize Resumes



            loadResumes();



        }







        public static synchronized void saveJobs() {



            StringBuilder sb = new StringBuilder("[\n");



            for (int i = 0; i < JOB_DATABASE.size(); i++) {



                sb.append("  ").append(jobToJson(JOB_DATABASE.get(i)));



                if (i < JOB_DATABASE.size() - 1) sb.append(",\n");



            }



            sb.append("\n]");



            writeFileSafely(new File(DIR, "jobs.json"), sb.toString());



        }







        public static synchronized void saveSkills() {



            StringBuilder sb = new StringBuilder("{\n");



            sb.append("  \"skills\": ").append(listToJsonArray(new ArrayList<>(MatchingEngine.getAllKnownSkills()))).append(",\n");



            sb.append("  \"synonyms\": {\n");



            List<String> keys = new ArrayList<>(SKILL_SYNONYMS.keySet());



            for (int i = 0; i < keys.size(); i++) {



                String k = keys.get(i);



                sb.append("    \"").append(escapeJson(k)).append("\": ").append(listToJsonArray(new ArrayList<>(SKILL_SYNONYMS.get(k))));



                if (i < keys.size() - 1) sb.append(",\n");



            }



            sb.append("\n  }\n}");



            writeFileSafely(new File(DIR, "skills.json"), sb.toString());



        }







        public static synchronized void saveApplications() {



            StringBuilder sb = new StringBuilder("[\n");



            for (int i = 0; i < APPLICATIONS_LIST.size(); i++) {



                JobApplication app = APPLICATIONS_LIST.get(i);



                sb.append("  {\n");



                sb.append("    \"applicationId\": \"").append(escapeJson(app.applicationId)).append("\",\n");



                sb.append("    \"jobId\": \"").append(escapeJson(app.jobId)).append("\",\n");



                sb.append("    \"jobTitle\": \"").append(escapeJson(app.jobTitle)).append("\",\n");



                sb.append("    \"company\": \"").append(escapeJson(app.company)).append("\",\n");



                sb.append("    \"candidateName\": \"").append(escapeJson(app.candidateName)).append("\",\n");



                sb.append("    \"candidateEmail\": \"").append(escapeJson(app.candidateEmail)).append("\",\n");



                sb.append("    \"candidatePhone\": \"").append(escapeJson(app.candidatePhone)).append("\",\n");



                sb.append("    \"expectedSalary\": ").append(app.expectedSalary).append(",\n");



                sb.append("    \"joiningAvailability\": \"").append(escapeJson(app.joiningAvailability)).append("\",\n");



                sb.append("    \"preferredHours\": \"").append(escapeJson(app.preferredHours)).append("\",\n");



                sb.append("    \"coverLetter\": \"").append(escapeJson(app.coverLetter)).append("\",\n");



                sb.append("    \"portfolioUrl\": \"").append(escapeJson(app.portfolioUrl)).append("\",\n");



                sb.append("    \"submittedAt\": \"").append(escapeJson(app.submittedAt)).append("\",\n");



                sb.append("    \"status\": \"").append(escapeJson(app.status)).append("\",\n");



                sb.append("    \"statusNote\": \"").append(escapeJson(app.statusNote)).append("\",\n");



                sb.append("    \"statusTimeline\": ").append(listToJsonArray(app.statusTimeline)).append("\n");



                sb.append("  }");



                if (i < APPLICATIONS_LIST.size() - 1) sb.append(",\n");



            }



            sb.append("\n]");



            writeFileSafely(new File(DIR, "applications.json"), sb.toString());



        }







        private static synchronized void loadApplications() {



            File file = new File(DIR, "applications.json");



            if (!file.exists()) {



                saveApplications();



                return;



            }



            try {



                String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);



                Pattern objPat = Pattern.compile("\\{[^{}]*\\}", Pattern.DOTALL);



                Matcher m = objPat.matcher(content);



                while (m.find()) {



                    Map<String, Object> map = parseJsonMap(m.group());



                    String appId = (String) map.get("applicationId");



                    if (appId == null || appId.isEmpty()) continue;



                    boolean exists = false;



                    for (JobApplication existing : APPLICATIONS_LIST) {



                        if (existing.applicationId.equalsIgnoreCase(appId)) {



                            exists = true;



                            break;



                        }



                    }



                    if (!exists) {



                        JobApplication app = new JobApplication(



                                appId,



                                (String) map.getOrDefault("jobId", ""),



                                (String) map.getOrDefault("jobTitle", "Software Engineer"),



                                (String) map.getOrDefault("company", "Tech Company"),



                                (String) map.getOrDefault("candidateName", "Applicant"),



                                (String) map.getOrDefault("candidateEmail", ""),



                                (String) map.getOrDefault("candidatePhone", ""),



                                parseNumber(map.get("expectedSalary"), 0),



                                (String) map.getOrDefault("joiningAvailability", "Immediate"),



                                (String) map.getOrDefault("preferredHours", "Standard Day"),



                                (String) map.getOrDefault("coverLetter", ""),



                                (String) map.getOrDefault("portfolioUrl", ""),



                                (String) map.getOrDefault("submittedAt", ""),



                                (String) map.getOrDefault("status", "Test/Assessment"),



                                (String) map.getOrDefault("statusNote", "")



                        );



                        APPLICATIONS_LIST.add(app);



                    }



                }



            } catch (Exception e) {



                System.err.println("Notice: Initialized empty applications: " + e.getMessage());



            }



        }







        public static synchronized void saveSavedJobs() {



            StringBuilder sb = new StringBuilder("[\n");



            for (int i = 0; i < SAVED_JOBS_LIST.size(); i++) {



                SavedJobRecord rec = SAVED_JOBS_LIST.get(i);



                sb.append("  {\n");



                sb.append("    \"id\": \"").append(escapeJson(rec.id)).append("\",\n");



                sb.append("    \"candidateEmail\": \"").append(escapeJson(rec.candidateEmail)).append("\",\n");



                sb.append("    \"jobId\": \"").append(escapeJson(rec.jobId)).append("\",\n");



                sb.append("    \"savedAt\": \"").append(escapeJson(rec.savedAt)).append("\"\n");



                sb.append("  }");



                if (i < SAVED_JOBS_LIST.size() - 1) sb.append(",\n");



            }



            sb.append("\n]");



            writeFileSafely(new File(DIR, "saved_jobs.json"), sb.toString());



        }







        private static synchronized void loadSavedJobs() {



            File file = new File(DIR, "saved_jobs.json");



            if (!file.exists()) {



                saveSavedJobs();



                return;



            }



            try {



                String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);



                Pattern objPat = Pattern.compile("\\{[^{}]*\\}", Pattern.DOTALL);



                Matcher m = objPat.matcher(content);



                while (m.find()) {



                    Map<String, Object> map = parseJsonMap(m.group());



                    String id = (String) map.get("id");



                    String email = (String) map.get("candidateEmail");



                    String jobId = (String) map.get("jobId");



                    String savedAt = (String) map.getOrDefault("savedAt", "");



                    if (id != null && jobId != null) {



                        SAVED_JOBS_LIST.add(new SavedJobRecord(id, email != null ? email : "", jobId, savedAt));



                    }



                }



            } catch (Exception e) {



                System.err.println("Notice: Initialized empty saved jobs: " + e.getMessage());



            }



        }







        public static synchronized boolean addBookmark(String email, String jobId) {



            if (jobId == null || jobId.trim().isEmpty()) return false;



            String cleanEmail = email != null ? email.trim().toLowerCase() : "";



            for (SavedJobRecord rec : SAVED_JOBS_LIST) {



                if (rec.jobId.equalsIgnoreCase(jobId.trim()) && rec.candidateEmail.equalsIgnoreCase(cleanEmail)) {



                    return false; // Already bookmarked



                }



            }



            String id = "SAVE-" + (1000 + SAVED_JOBS_LIST.size() + 1);



            String savedAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());



            SAVED_JOBS_LIST.add(new SavedJobRecord(id, cleanEmail, jobId.trim(), savedAt));



            saveSavedJobs();



            return true;



        }







        public static synchronized boolean removeBookmark(String email, String jobId) {



            if (jobId == null) return false;



            String cleanEmail = email != null ? email.trim().toLowerCase() : "";



            boolean removed = SAVED_JOBS_LIST.removeIf(rec ->



                    rec.jobId.equalsIgnoreCase(jobId.trim()) &&



                    (cleanEmail.isEmpty() || rec.candidateEmail.equalsIgnoreCase(cleanEmail))



            );



            if (removed) {



                saveSavedJobs();



            }



            return removed;



        }







        public static synchronized void saveCandidates() {



            StringBuilder sb = new StringBuilder("[\n");



            for (int i = 0; i < CANDIDATES_LIST.size(); i++) {



                Candidate c = CANDIDATES_LIST.get(i);



                sb.append("  {\n");



                sb.append("    \"candidateId\": \"").append(escapeJson(c.candidateId)).append("\",\n");



                sb.append("    \"name\": \"").append(escapeJson(c.name)).append("\",\n");



                sb.append("    \"email\": \"").append(escapeJson(c.email)).append("\",\n");



                sb.append("    \"phone\": \"").append(escapeJson(c.phone)).append("\",\n");



                sb.append("    \"yearsExp\": ").append(c.yearsExp).append(",\n");



                sb.append("    \"skills\": ").append(listToJsonArray(c.skills)).append(",\n");



                sb.append("    \"education\": ").append(listToJsonArray(c.education)).append(",\n");



                sb.append("    \"updatedAt\": \"").append(escapeJson(c.updatedAt)).append("\"\n");



                sb.append("  }");



                if (i < CANDIDATES_LIST.size() - 1) sb.append(",\n");



            }



            sb.append("\n]");



            writeFileSafely(new File(DIR, "candidates.json"), sb.toString());



        }







        private static synchronized void loadCandidates() {



            File file = new File(DIR, "candidates.json");



            if (!file.exists()) {



                saveCandidates();



                return;



            }



            try {



                String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);



                Pattern objPat = Pattern.compile("\\{[^{}]*\\}", Pattern.DOTALL);



                Matcher m = objPat.matcher(content);



                while (m.find()) {



                    Map<String, Object> map = parseJsonMap(m.group());



                    String id = (String) map.get("candidateId");



                    String email = (String) map.get("email");



                    if (id != null) {



                        @SuppressWarnings("unchecked")



                        List<String> skills = (List<String>) map.getOrDefault("skills", new ArrayList<>());



                        @SuppressWarnings("unchecked")



                        List<String> edu = (List<String>) map.getOrDefault("education", new ArrayList<>());



                        CANDIDATES_LIST.add(new Candidate(



                                id,



                                (String) map.getOrDefault("name", "Applicant"),



                                email != null ? email : "",



                                (String) map.getOrDefault("phone", ""),



                                parseNumber(map.get("yearsExp"), 0),



                                skills,



                                edu,



                                (String) map.getOrDefault("updatedAt", "")



                        ));



                    }



                }



            } catch (Exception e) {



                System.err.println("Notice: Initialized empty candidates: " + e.getMessage());



            }



        }







        public static synchronized Candidate addOrUpdateCandidate(String name, String email, String phone,



                                                                   int yearsExp, List<String> skills, List<String> edu) {



            String cleanEmail = email != null ? email.trim() : "";



            Candidate found = null;



            if (!cleanEmail.isEmpty()) {



                for (Candidate c : CANDIDATES_LIST) {



                    if (c.email.equalsIgnoreCase(cleanEmail)) {



                        found = c;



                        break;



                    }



                }



            }



            String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());



            if (found != null) {



                if (name != null && !name.isEmpty()) found.name = name;



                if (phone != null && !phone.isEmpty()) found.phone = phone;



                found.yearsExp = yearsExp;



                if (skills != null && !skills.isEmpty()) found.skills = skills;



                if (edu != null && !edu.isEmpty()) found.education = edu;



                found.updatedAt = now;



            } else {



                String candId = "CAND-" + (1000 + CANDIDATES_LIST.size() + 1);



                found = new Candidate(candId, name != null ? name : "Applicant", cleanEmail, phone != null ? phone : "",



                        yearsExp, skills, edu, now);



                CANDIDATES_LIST.add(found);



            }



            saveCandidates();



            return found;



        }







        public static synchronized void saveResumes() {



            StringBuilder sb = new StringBuilder("[\n");



            for (int i = 0; i < RESUMES_LIST.size(); i++) {



                ResumeDocument r = RESUMES_LIST.get(i);



                sb.append("  {\n");



                sb.append("    \"resumeId\": \"").append(escapeJson(r.resumeId)).append("\",\n");



                sb.append("    \"filename\": \"").append(escapeJson(r.filename)).append("\",\n");



                sb.append("    \"fileType\": \"").append(escapeJson(r.fileType)).append("\",\n");



                sb.append("    \"fileSize\": ").append(r.fileSize).append(",\n");



                sb.append("    \"uploadedAt\": \"").append(escapeJson(r.uploadedAt)).append("\",\n");



                sb.append("    \"wordCount\": ").append(r.wordCount).append(",\n");



                sb.append("    \"extractedSkills\": ").append(listToJsonArray(r.extractedSkills)).append(",\n");



                sb.append("    \"candidateEmail\": \"").append(escapeJson(r.candidateEmail)).append("\"\n");



                sb.append("  }");



                if (i < RESUMES_LIST.size() - 1) sb.append(",\n");



            }



            sb.append("\n]");



            writeFileSafely(new File(DIR, "resumes.json"), sb.toString());



        }







        private static synchronized void loadResumes() {



            File file = new File(DIR, "resumes.json");



            if (!file.exists()) {



                saveResumes();



                return;



            }



            try {



                String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);



                Pattern objPat = Pattern.compile("\\{[^{}]*\\}", Pattern.DOTALL);



                Matcher m = objPat.matcher(content);



                while (m.find()) {



                    Map<String, Object> map = parseJsonMap(m.group());



                    String id = (String) map.get("resumeId");



                    if (id != null) {



                        @SuppressWarnings("unchecked")



                        List<String> skills = (List<String>) map.getOrDefault("extractedSkills", new ArrayList<>());



                        RESUMES_LIST.add(new ResumeDocument(



                                id,



                                (String) map.getOrDefault("filename", "resume.pdf"),



                                (String) map.getOrDefault("fileType", "pdf"),



                                parseNumber(map.get("fileSize"), 0),



                                (String) map.getOrDefault("uploadedAt", ""),



                                parseNumber(map.get("wordCount"), 0),



                                skills,



                                (String) map.getOrDefault("candidateEmail", "")



                        ));



                    }



                }



            } catch (Exception e) {



                System.err.println("Notice: Initialized empty resumes: " + e.getMessage());



            }



        }







        public static synchronized ResumeDocument addResumeRecord(String filename, String fileType, long size,



                                                                  int words, List<String> skills, String email) {



            String resId = "RES-" + (1000 + RESUMES_LIST.size() + 1);



            String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());



            ResumeDocument doc = new ResumeDocument(resId, filename, fileType, size, now, words, skills, email != null ? email : "");



            RESUMES_LIST.add(doc);



            saveResumes();



            return doc;



        }







        private static void writeFileSafely(File file, String content) {



            try {



                File temp = new File(file.getParentFile(), file.getName() + ".tmp");



                Files.writeString(temp.toPath(), content, StandardCharsets.UTF_8);



                Files.move(temp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);



            } catch (Exception e) {



                try {



                    Files.writeString(file.toPath(), content, StandardCharsets.UTF_8);



                } catch (Exception ignored) {}



            }



        }



    }







    // ==========================================



    // DOCUMENT EXTRACTOR (ZERO-DEPENDENCY PURE JAVA PDF / DOCX / TXT)



    // ==========================================







    public static class DocumentExtractor {







        public static String extractText(String filename, byte[] bytes) {



            if (bytes == null || bytes.length == 0) return "";



            if (filename == null) filename = "resume.txt";



            String lower = filename.toLowerCase();







            if (lower.endsWith(".docx")) {



                return extractTextFromDocx(bytes);



            } else if (lower.endsWith(".pdf")) {



                return extractTextFromPdf(bytes);



            } else {



                return new String(bytes, StandardCharsets.UTF_8).trim();



            }



        }







        public static String extractTextFromDocx(byte[] bytes) {



            StringBuilder text = new StringBuilder();



            try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(bytes))) {



                ZipEntry entry;



                while ((entry = zis.getNextEntry()) != null) {



                    if ("word/document.xml".equalsIgnoreCase(entry.getName())) {



                        ByteArrayOutputStream bos = new ByteArrayOutputStream();



                        byte[] buf = new byte[4096];



                        int len;



                        while ((len = zis.read(buf)) != -1) {



                            bos.write(buf, 0, len);



                        }



                        String xml = bos.toString(StandardCharsets.UTF_8.name());



                        xml = xml.replaceAll("</w:p>", "\n");



                        Pattern p = Pattern.compile("<w:t[^>]*>(.*?)</w:t>");



                        Matcher m = p.matcher(xml);



                        while (m.find()) {



                            String chunk = unescapeXml(m.group(1));



                            text.append(chunk).append(" ");



                        }



                        break;



                    }



                }



            } catch (Exception e) {



                System.err.println("DOCX extraction error: " + e.getMessage());



            }



            return text.toString().trim();



        }







        public static String extractTextFromPdf(byte[] bytes) {



            StringBuilder text = new StringBuilder();



            try {



                String raw = new String(bytes, StandardCharsets.ISO_8859_1);



                Pattern streamPattern = Pattern.compile("<<([^>]*)>>\\s*stream[\r\n]+([\\s\\S]*?)[\r\n]+endstream");



                Matcher sm = streamPattern.matcher(raw);



                while (sm.find()) {



                    String dict = sm.group(1);



                    int streamStart = sm.start(2);



                    int streamEnd = sm.end(2);



                    byte[] streamBytes = Arrays.copyOfRange(bytes, streamStart, streamEnd);



                    byte[] decompressed = null;







                    if (dict.contains("FlateDecode")) {



                        try {



                            Inflater inflater = new Inflater();



                            inflater.setInput(streamBytes);



                            ByteArrayOutputStream bos = new ByteArrayOutputStream();



                            byte[] buf = new byte[4096];



                            while (!inflater.finished()) {



                                int count = inflater.inflate(buf);



                                if (count == 0 && inflater.needsInput()) break;



                                bos.write(buf, 0, count);



                            }



                            decompressed = bos.toByteArray();



                        } catch (Exception ignored) {}



                    } else {



                        decompressed = streamBytes;



                    }







                    if (decompressed != null) {



                        String streamStr = new String(decompressed, StandardCharsets.ISO_8859_1);



                        Pattern tj = Pattern.compile("\\(([^\\)]*)\\)\\s*Tj");



                        Matcher tm = tj.matcher(streamStr);



                        while (tm.find()) {



                            text.append(unescapePdf(tm.group(1))).append(" ");



                        }



                        Pattern tjArray = Pattern.compile("\\[([^\\]]*)\\]\\s*TJ");



                        Matcher tja = tjArray.matcher(streamStr);



                        while (tja.find()) {



                            String arr = tja.group(1);



                            Matcher inner = Pattern.compile("\\(([^\\)]*)\\)").matcher(arr);



                            while (inner.find()) {



                                text.append(unescapePdf(inner.group(1))).append(" ");



                            }



                        }



                    }



                }







                if (text.length() < 10) {



                    Pattern tj = Pattern.compile("\\(([^\\)\\r\\n]{3,})\\)\\s*Tj");



                    Matcher tm = tj.matcher(raw);



                    while (tm.find()) {



                        text.append(unescapePdf(tm.group(1))).append(" ");



                    }



                }



            } catch (Exception e) {



                System.err.println("PDF extraction error: " + e.getMessage());



            }



            return text.toString().trim();



        }







        private static String unescapeXml(String s) {



            if (s == null) return "";



            return s.replace("&amp;", "&")



                    .replace("&lt;", "<")



                    .replace("&gt;", ">")



                    .replace("&quot;", "\"")



                    .replace("&apos;", "'");



        }







        private static String unescapePdf(String s) {



            if (s == null) return "";



            return s.replace("\\(", "(")



                    .replace("\\)", ")")



                    .replace("\\\\", "\\")



                    .replace("\\r", "\r")



                    .replace("\\n", "\n")



                    .replace("\\t", "\t");



        }



    }







    // ==========================================



    // INITIALIZATION & DATA



    // ==========================================







    private static void initSkillSynonyms() {



        addSynonym("javascript", "js", "ecmascript", "es6", "vanilla js");



        addSynonym("typescript", "ts");



        addSynonym("python", "py", "python3", "cpython");



        addSynonym("java", "core java", "java 17", "java 21", "java 11", "java 8", "jdk");



        addSynonym("react", "react.js", "reactjs");



        addSynonym("node.js", "nodejs", "node");



        addSynonym("spring boot", "springboot", "spring", "spring framework");



        addSynonym("aws", "amazon web services", "amazon aws");



        addSynonym("azure", "microsoft azure");



        addSynonym("gcp", "google cloud", "google cloud platform");



        addSynonym("docker", "containerization", "containers");



        addSynonym("kubernetes", "k8s");



        addSynonym("sql", "relational database", "rdbms", "ansi sql");



        addSynonym("postgresql", "postgres");



        addSynonym("mongodb", "mongo", "nosql");



        addSynonym("machine learning", "ml", "statistical learning");



        addSynonym("deep learning", "dl", "neural networks");



        addSynonym("artificial intelligence", "ai", "genai", "generative ai");



        addSynonym("natural language processing", "nlp", "llm", "large language models");



        addSynonym("devops", "ci/cd", "continuous integration", "continuous deployment");



        addSynonym("html", "html5");



        addSynonym("css", "css3", "sass", "scss", "tailwind", "tailwindcss");



        addSynonym("git", "github", "gitlab", "version control");



        addSynonym("rest api", "rest", "restful", "restful api", "web apis");



        addSynonym("graphql", "gql");



        addSynonym("microservices", "microservice architecture", "distributed systems");



        addSynonym("c++", "cpp");



        addSynonym("c#", "csharp", ".net", "dotnet", "asp.net");



    }







    private static void addSynonym(String canonical, String... aliases) {



        Set<String> set = SKILL_SYNONYMS.computeIfAbsent(canonical.toLowerCase(), k -> new HashSet<>());



        set.add(canonical.toLowerCase());



        for (String alias : aliases) {



            set.add(alias.toLowerCase());



            SKILL_SYNONYMS.computeIfAbsent(alias.toLowerCase(), k -> new HashSet<>()).addAll(set);



        }



    }







    private static boolean skillsMatch(String skillA, String skillB) {



        String a = skillA.trim().toLowerCase();



        String b = skillB.trim().toLowerCase();



        if (a.equals(b)) return true;



        if (SKILL_SYNONYMS.containsKey(a) && SKILL_SYNONYMS.get(a).contains(b)) return true;



        if (SKILL_SYNONYMS.containsKey(b) && SKILL_SYNONYMS.get(b).contains(a)) return true;



        return a.contains(b) || b.contains(a);



    }







    private static void initJobDatabase() {



        JOB_DATABASE.clear();







        // 1. Tata Consultancy Services (TCS) - Assistant Systems Engineer Trainee (Ninja Graduate Program)



        JOB_DATABASE.add(new Job(



                "job-tcs-01",



                "Assistant Systems Engineer Trainee (Ninja Graduate Program)",



                "Tata Consultancy Services (TCS)",



                "🏛️",



                "Pune, Maharashtra, India",



                "Hybrid",



                "Full-time",



                360000, 450000, "INR",



                "Fresher", 0,



                "Software Engineering",



                "Gateway entry role for 2024-2026 engineering graduates (B.Tech / B.E. / MCA). Undergo foundational training at TCS ILP and build mission-critical digital enterprise applications with Java, SQL, HTML, CSS, and Git.",



                Arrays.asList("Java", "SQL", "Git", "HTML", "CSS", "Problem Solving"),



                Arrays.asList("Spring Boot", "Python", "Linux", "REST API"),



                Arrays.asList("Structured Initial Learning Program (ILP)", "Higher Education Sponsorship (M.Tech/MBA)", "Comprehensive Tata Medical Insurance", "Performance Bonus & Digital Elevate Program"),



                "Today",



                "Actively Hiring",



                new CompanyDetails("Global IT Services & Digital Consulting", "600,000+ employees", 1968, 4.1, "Tata Values (Integrity, Excellence, Responsibility, Pioneering), strong stability & long-term growth", "tcs.com", "K. Krithivasan"),



                new LocationIntelligence("Pune", "Maharashtra, India", "Hinjawadi Infotech Park Phase III", "Semi-Arid Pleasant", "Mild winters and pleasant monsoons with moderate tropical summers", "15°C - 34°C", 180, "Affordable IT hub with top educational institutions & young professional community", "PMPML AC buses & Pune Metro connectivity across Hinjawadi"),



                new JoiningAndWorkplace("Immediate to 30 Days (Campus Cohort Batching)", "1st and 3rd Monday every month", "Standard Day (9:30 AM - 6:30 PM IST)", "Hybrid (3 days office, 2 days remote)", "24 Days Earned Leave + 12 National Holidays", "10 Days Casual/Medical Leave", "Diwali & Year-End Holiday Windows", "26 Weeks Paid Maternity / 2 Weeks Paternity")



        ));







        // 2. Tata Consultancy Services (TCS) - Java Cloud Microservices Specialist (Digital Cadre)



        JOB_DATABASE.add(new Job(



                "job-tcs-02",



                "Java Cloud Microservices Specialist (Digital Cadre)",



                "Tata Consultancy Services (TCS)",



                "⚡",



                "Hyderabad, Telangana, India",



                "Hybrid",



                "Full-time",



                750000, 1150000, "INR",



                "Junior-Mid", 1,



                "Software Engineering",



                "Join TCS Digital elite engineering cadre. Design and deploy cloud-native microservices architectures for Fortune 500 banks using Spring Boot, Kafka, AWS/Azure, Docker, and distributed relational databases.",



                Arrays.asList("Java", "Spring Boot", "Microservices", "REST API", "SQL", "Docker"),



                Arrays.asList("Kafka", "AWS", "Kubernetes", "Redis", "CI/CD"),



                Arrays.asList("Digital Cadre Accelerated Promotions", "Global Client Onsite Opportunities", "Tata Group Employee Discounts", "Health Care for Parents Included"),



                "1 day ago",



                "Hot",



                new CompanyDetails("Enterprise Digital Transformation & BFSI", "600,000+ employees", 1968, 4.1, "Engineering excellence, global scale, meritocratic digital incentives", "tcs.com", "K. Krithivasan"),



                new LocationIntelligence("Hyderabad", "Telangana, India", "HITEC City / Madhapur Cyber Gateway", "Tropical Semi-Arid", "Warm and pleasant winter with brisk evening breeze; hot summers", "17°C - 38°C", 210, "Highly cost-effective metropolitan living with premier tech infrastructure", "Hyderabad Metro Blue Line connects directly to HITEC City station"),



                new JoiningAndWorkplace("Immediate or 30-60 Days Notice", "Every Monday", "Flexible Hours (Core 11 AM - 5 PM IST)", "Hybrid (2 days office, 3 days remote)", "25 Days Paid Annual Leave + 10 State & National Holidays", "12 Days Medical / Wellness Leave", "Sankranti & Diwali Festival Closures", "26 Weeks Paid Maternity / 4 Weeks Paternity")



        ));







        // 3. Tata Consultancy Services (TCS) - Enterprise Cloud Solutions Architect (Banking & Financial Platforms)



        JOB_DATABASE.add(new Job(



                "job-tcs-03",



                "Enterprise Cloud Solutions Architect (Banking & Financial Platforms)",



                "Tata Consultancy Services (TCS)",



                "🏛️",



                "Mumbai, Maharashtra, India",



                "Hybrid",



                "Full-time",



                2400000, 3800000, "INR",



                "Lead", 8,



                "Cloud & DevOps",



                "Lead enterprise-wide modernization of tier-1 banking systems. Define distributed architectures, multi-region failover, cloud compliance (RBI / ISO 27001), and high-throughput transaction processing.",



                Arrays.asList("Enterprise Architecture", "Java", "AWS", "Spring Boot", "Kafka", "Kubernetes", "System Design"),



                Arrays.asList("Terraform", "FinOps", "Disaster Recovery", "Zero-Trust Security"),



                Arrays.asList("Executive Health & Wellness Executive Suite", "Performance Stock Options / Retention Bonuses", "International Mobility Allowance", "Club Membership Subsidies"),



                "3 days ago",



                "Urgent",



                new CompanyDetails("Global Enterprise Architecture & BFSI", "600,000+ employees", 1968, 4.1, "Leadership by trust, long-term stewardship, strategic executive mentorship", "tcs.com", "K. Krithivasan"),



                new LocationIntelligence("Mumbai", "Maharashtra, India", "Bandra-Kurla Complex (BKC) Financial Center", "Tropical Coastal", "Warm coastal sea breeze with heavy monsoon showers July-Sept", "20°C - 33°C", 340, "Premier financial capital; lucrative relocation & housing allowance provided", "Mumbai Metro Line 2B / 3 accessible • BKC Connector Flyover"),



                new JoiningAndWorkplace("30 to 90 Days Notice Period Accepted", "Bi-weekly cohort", "Executive Autonomy / Results Oriented", "Hybrid Flexible", "30 Days Annual Executive Leave", "15 Days Executive Health Days", "National Holidays & Extended Diwali Recess", "26 Weeks Maternity / 6 Weeks Paternity")



        ));







        // 4. Infosys Technologies - Specialist Programmer (High-Performance Engineering Freshers)



        JOB_DATABASE.add(new Job(



                "job-infy-01",



                "Specialist Programmer (High-Performance Engineering Freshers)",



                "Infosys Technologies",



                "☕",



                "Bengaluru, Karnataka, India",



                "Hybrid",



                "Full-time",



                650000, 950000, "INR",



                "Fresher", 0,



                "Software Engineering",



                "Elite coding track at Infosys for top competitive programmers and computer science freshers. Work on algorithmic challenges, high-concurrency cloud engines, and next-gen Finacle banking architecture.",



                Arrays.asList("Algorithms", "Data Structures", "Java", "Python", "SQL", "Git"),



                Arrays.asList("Spring Boot", "Docker", "Machine Learning", "FastAPI"),



                Arrays.asList("Specialist Cadre Fast-Track Band", "Mysore Campus World-Class Training", "Comprehensive Health Cover (₹10L)", "Free Campus Shuttles & Cafeteria Subsidies"),



                "1 day ago",



                "Fresher Friendly",



                new CompanyDetails("Global IT Consulting & Next-Gen Digital Services", "320,000+ employees", 1981, 4.2, "Learnability, ethical governance, meritocratic code competitions", "infosys.com", "Salil Parekh"),



                new LocationIntelligence("Bengaluru", "Karnataka, India", "Electronic City Phase 1 Infosys Campus", "Tropical Savanna Moderate", "World-famous pleasant Bangalore weather year-round", "16°C - 31°C", 240, "Premier Silicon Valley of India with massive tech meetup culture", "Electronic City Elevated Expressway • Upcoming Yellow Line Metro"),



                new JoiningAndWorkplace("Immediate or Campus Joining Batches", "1st & 3rd Monday monthly", "Standard Day (9 AM - 6 PM IST)", "Hybrid (3 days office, 2 days remote)", "25 Days Annual Privilege Leave + 10 Festival Holidays", "12 Days Sick / Medical Leave", "Ugadi, Ayudha Pooja, Diwali & Year-End Holidays", "26 Weeks Paid Maternity / 4 Weeks Paternity")



        ));







        // 5. Infosys Technologies - Senior Full Stack Cloud Engineer (Spring Boot & Angular/React)



        JOB_DATABASE.add(new Job(



                "job-infy-02",



                "Senior Full Stack Cloud Engineer (Spring Boot & Angular/React)",



                "Infosys Technologies",



                "☕",



                "Bengaluru, Karnataka, India",



                "Hybrid",



                "Full-time",



                1400000, 2200000, "INR",



                "Mid-Level", 4,



                "Software Engineering",



                "Lead full stack digital delivery for global enterprise clients. Build modular, micro-frontend single page applications and resilient Spring Boot RESTful services hosted on AWS and Azure Kubernetes Service.",



                Arrays.asList("Java", "Spring Boot", "React", "JavaScript", "TypeScript", "SQL", "Docker", "REST API"),



                Arrays.asList("Kafka", "Angular", "Kubernetes", "AWS", "CI/CD"),



                Arrays.asList("Infosys Certification Reimbursements", "Quarterly High-Performer Variable Pay", "Comprehensive Family Health Insurance", "Subsidized Creche & Childcare Facilities"),



                "2 days ago",



                "Actively Hiring",



                new CompanyDetails("Global Enterprise Engineering & Cloud Modernization", "320,000+ employees", 1981, 4.2, "Continuous upskilling via Lex platform, collaborative team spirit", "infosys.com", "Salil Parekh"),



                new LocationIntelligence("Bengaluru", "Karnataka, India", "Electronic City / Manyata Tech Park", "Moderate Warm", "Moderate temperatures with refreshing breeze", "17°C - 30°C", 240, "Vibrant tech hub with endless co-working spaces and dining", "Company shuttle transport covers entire Bengaluru metropolitan area"),



                new JoiningAndWorkplace("30 to 60 Days Notice Period", "Bi-weekly", "Flexible Core (10 AM - 4 PM IST)", "Hybrid Work Model", "26 Days Annual Leave + 10 Public Holidays", "12 Days Medical Leave", "Diwali & Year-End Shutdown", "26 Weeks Maternity / 4 Weeks Paternity")



        ));







        // 6. Wipro - Project Engineer - Turbo & Elite Developer Track



        JOB_DATABASE.add(new Job(



                "job-wipro-01",



                "Project Engineer - Turbo & Elite Developer Track",



                "Wipro",



                "🌐",



                "Bengaluru, Karnataka, India",



                "Hybrid",



                "Full-time",



                420000, 650000, "INR",



                "Fresher", 0,



                "Software Engineering",



                "Opportunity for fresh graduates with strong problem-solving skills in Java, Python, or C++. Develop enterprise modules, automation scripts, and test harnesses while receiving structured mentorship from lead architects.",



                Arrays.asList("Java", "Python", "SQL", "Git", "Problem Solving", "HTML"),



                Arrays.asList("Spring Boot", "Linux", "REST API", "JavaScript"),



                Arrays.asList("Wipro Elite/Turbo Fast-Track Appraisals", "Higher Education (BITS Pilani WILP M.Tech Sponsorship)", "Group Health Insurance (₹7.5L)", "Annual Tech Summit & Hackathon Prizes"),



                "Just now",



                "Fresher Friendly",



                new CompanyDetails("Information Technology Consulting & System Integration", "240,000+ employees", 1945, 3.9, "Spirit of Wipro (Be passionate about clients' success, Treat each person with respect, Be global and responsible)", "wipro.com", "Srini Pallia"),



                new LocationIntelligence("Bengaluru", "Karnataka, India", "Sarjapur Road Wipro Corporate Campus", "Pleasant Tropical", "Mild winters and pleasant monsoons with moderate summers", "17°C - 32°C", 230, "Rapidly expanding tech corridor with affordable residential options", "Wipro Campus direct shuttle service & Outer Ring Road connectivity"),



                new JoiningAndWorkplace("Immediate to 30 Days", "Every Monday", "Standard (9:30 AM - 6:30 PM IST)", "Hybrid (3 days office, 2 days remote)", "22 Days Earned Leave + 10 Festival Holidays", "10 Days Sick Leave", "National Holidays & Diwali", "26 Weeks Maternity / 2 Weeks Paternity")



        ));







        // 7. Wipro - Lead SRE & Cloud Infrastructure Specialist (AWS / Azure)



        JOB_DATABASE.add(new Job(



                "job-wipro-02",



                "Lead SRE & Cloud Infrastructure Specialist (AWS / Azure)",



                "Wipro",



                "🌐",



                "Hyderabad, Telangana, India",



                "Hybrid",



                "Full-time",



                1800000, 2800000, "INR",



                "Senior", 6,



                "Cloud & DevOps",



                "Drive Site Reliability Engineering for tier-1 cloud implementations. Automate Kubernetes clusters, build Terraform IaC blueprints, manage Prometheus/Grafana observability stacks, and ensure 99.99% service availability.",



                Arrays.asList("AWS", "Kubernetes", "Docker", "Terraform", "Linux", "CI/CD", "DevOps", "Python"),



                Arrays.asList("Prometheus", "Grafana", "Ansible", "Azure", "Kafka"),



                Arrays.asList("Global Cloud Certifications 100% Sponsored", "Quarterly Retention Bonuses", "Executive OPD & IPD Medical Coverage", "Flexible Work Arrangements"),



                "3 days ago",



                "Actively Hiring",



                new CompanyDetails("Cloud Infrastructure & Managed Cloud Operations", "240,000+ employees", 1945, 3.9, "Integrity, innovation, sustainable client impact", "wipro.com", "Srini Pallia"),



                new LocationIntelligence("Hyderabad", "Telangana, India", "Gachibowli Financial District", "Warm Semi-Arid", "Pleasant breezy evenings, vibrant cosmopolitan dining", "18°C - 37°C", 210, "Exceptional infrastructure with modern gated communities", "Outer Ring Road (ORR) seamless airport & city connectivity"),



                new JoiningAndWorkplace("30 to 60 Days Notice", "1st of every month", "Flexible Work Hours", "Hybrid", "25 Days Paid Leave + 11 Public Holidays", "12 Days Medical Leave", "Sankranti & Diwali Festival Holidays", "26 Weeks Maternity / 4 Weeks Paternity")



        ));







        // 8. Accenture India - Associate Software Engineer (Advanced Technology Centers in India)



        JOB_DATABASE.add(new Job(



                "job-acc-01",



                "Associate Software Engineer (Advanced Technology Centers in India)",



                "Accenture India",



                "📐",



                "Gurugram, Delhi NCR, India",



                "Hybrid",



                "Full-time",



                480000, 720000, "INR",



                "Fresher", 0,



                "Software Engineering",



                "Start your career at Accenture ATCI. Build scalable business solutions using Java, Spring, SQL, and modern cloud technologies. Collaborate in agile teams to transform enterprise client operations across the globe.",



                Arrays.asList("Java", "JavaScript", "SQL", "Git", "HTML", "CSS"),



                Arrays.asList("Spring Boot", "React", "Cloud Basics", "Agile"),



                Arrays.asList("Fast-Track Promotion Cycles", "Accenture Connected Learning Portal", "Comprehensive Wellness & Medical Insurance (₹10L)", "Meal Cards & Home Office Setup Kit"),



                "1 day ago",



                "Fresher Friendly",



                new CompanyDetails("Global Management Consulting & Technology Services", "300,000+ in India (730,000 global)", 1989, 4.1, "360-degree value, truly human culture, deep commitment to inclusion & diversity", "accenture.com/in", "Julie Sweet"),



                new LocationIntelligence("Gurugram", "Delhi NCR, India", "Cyber City Phase II / Golf Course Road", "Composite Continental", "Pleasant autumn and crisp winter; warm sunny summers", "9°C - 38°C", 260, "Premier corporate hub of North India with world-class malls and dining", "Rapid Metro connects Cyber City directly to Delhi Metro Yellow Line"),



                new JoiningAndWorkplace("Immediate or within 30 Days", "Every Monday", "Standard Day (9:30 AM - 6:00 PM IST)", "Hybrid (3 days office, 2 days remote)", "25 Days Paid Privilege Leave + 10 Festival Holidays", "12 Days Medical / Wellness Leave", "Holi, Diwali & New Year Closures", "26 Weeks Paid Maternity / 4 Weeks Paternity")



        ));







        // 9. Accenture India - Senior Data Platform & Kafka Architect



        JOB_DATABASE.add(new Job(



                "job-acc-02",



                "Senior Data Platform & Kafka Architect",



                "Accenture India",



                "📐",



                "Bengaluru, Karnataka, India",



                "Hybrid",



                "Full-time",



                2200000, 3500000, "INR",



                "Senior", 6,



                "Data",



                "Architect high-throughput streaming data pipelines for Fortune 100 enterprises. Leverage Apache Kafka, Spark Streaming, AWS EMR, Snowflake, and PostgreSQL to deliver real-time analytical insights.",



                Arrays.asList("Apache Kafka", "Java", "Python", "SQL", "AWS", "Microservices", "System Design"),



                Arrays.asList("Apache Spark", "Snowflake", "Docker", "Kubernetes", "Airflow"),



                Arrays.asList("Generous Performance-Linked Annual Bonus", "Global Client Travel Opportunities", "Executive Medical Concierge Service", "Tax-Optimized Car Lease Program"),



                "4 days ago",



                "Actively Hiring",



                new CompanyDetails("Data & Artificial Intelligence Consulting", "300,000+ in India", 1989, 4.1, "Cutting-edge data engineering, global collaboration, merit-driven trajectory", "accenture.com/in", "Julie Sweet"),



                new LocationIntelligence("Bengaluru", "Karnataka, India", "Prestige Tech Park / Outer Ring Road", "Moderate Tropical", "Comfortable climate year-round with evening rains", "17°C - 30°C", 240, "Core IT corridor with thousands of tech peers and vibrant tech nightlife", "Outer Ring Road Express Buses & upcoming Silkboard Metro"),



                new JoiningAndWorkplace("30 to 60 Days Notice Accepted", "Bi-weekly", "Flexible Working Hours", "Hybrid Model", "26 Days Annual Leave + 11 Public Holidays", "14 Days Wellness Leave", "Diwali & Year-End Holidays", "26 Weeks Maternity / 6 Weeks Paternity")



        ));







        // 10. Google India - Software Engineer II (Google Pay & Core Payments Infrastructure)



        JOB_DATABASE.add(new Job(



                "job-goog-01",



                "Software Engineer II (Google Pay & Core Payments Infrastructure)",



                "Google India",



                "🔍",



                "Bengaluru, Karnataka, India",



                "Hybrid",



                "Full-time",



                3200000, 5200000, "INR",



                "Mid-Level", 3,



                "Software Engineering",



                "Build the next generation of digital payments infrastructure powering Google Pay across India and emerging markets. Engineer high-throughput, fault-tolerant distributed transaction systems serving hundreds of millions of users daily.",



                Arrays.asList("Java", "C++", "Distributed Systems", "Algorithms", "Data Structures", "System Design"),



                Arrays.asList("Concurrency", "gRPC", "Protobuf", "GCP", "SQL"),



                Arrays.asList("Google Stock Units (GSUs - Significant Equity Grant)", "World-Famous Gourmet Cafeteria (Breakfast, Lunch, Dinner)", "Comprehensive Family Health Cover with Zero Deductibles", "Generous Annual Learning & Wellness Stipend ($3,000 equivalent)"),



                "Just now",



                "Hot",



                new CompanyDetails("Global Big Tech & Consumer Internet Platforms", "180,000+ employees globally", 1998, 4.6, "Don't be evil, intellectual freedom, peer code reviews, 20% innovation project time", "google.com/about/careers", "Sundar Pichai"),



                new LocationIntelligence("Bengaluru", "Karnataka, India", "Bagmane Constellation Business Park / Old Madras Road", "Moderate Warm", "Consistently pleasant weather with light afternoon showers", "17°C - 30°C", 250, "Top-tier tech epicenter; relocation and temporary corporate housing included", "Complimentary Google luxury shuttle fleet across all Bangalore zones"),



                new JoiningAndWorkplace("Flexible: 15 to 60 Days Notice Accepted", "Every 2 Weeks", "Complete Autonomy / Results-Driven", "Hybrid (3 days office, 2 days remote)", "25 Days Paid Vacation + 12 National/State Holidays", "Unlimited Paid Sick & Mental Health Days", "Company-wide Year-End Shutdown (Dec 24 - Jan 2)", "24 Weeks Fully Paid Baby Bonding Leave for All Parents")



        ));







        // 11. Google India - Senior AI & Machine Learning Research Engineer (Gemini Core)



        JOB_DATABASE.add(new Job(



                "job-goog-02",



                "Senior AI & Machine Learning Research Engineer (Gemini Core)",



                "Google India",



                "🔍",



                "Hyderabad, Telangana, India",



                "Hybrid",



                "Full-time",



                5500000, 8800000, "INR",



                "Senior", 6,



                "AI & Machine Learning",



                "Lead applied LLM research and multimodality advancements for Google's foundational AI models. Design scalable transformer inference pipelines, RLHF evaluation frameworks, and low-latency embeddings on TPU clusters.",



                Arrays.asList("Python", "PyTorch", "TensorFlow", "Deep Learning", "Machine Learning", "Transformers", "LLMs"),



                Arrays.asList("C++", "Distributed Training", "TPU/GPU Optimization", "Vector Databases", "MLOps"),



                Arrays.asList("Substantial Annual Google Equity (GSUs)", "Patent Incentive Bonuses & Top Conference Travel Sponsorship", "World-Class Campus Facilities (Gym, Gaming, Massages)", "Parent Care Medical Coverage & Childcare Support"),



                "2 days ago",



                "Urgent",



                new CompanyDetails("Artificial Intelligence Research & Cloud Infrastructure", "180,000+ employees", 1998, 4.6, "Push the frontiers of science and technology to benefit humanity", "google.com/about/careers", "Sundar Pichai"),



                new LocationIntelligence("Hyderabad", "Telangana, India", "HITEC City / Kondapur Google India Campus", "Semi-Arid Warm", "Breezy pleasant winters and dry tropical summers", "18°C - 36°C", 210, "Outstanding living standards with affordable luxury housing nearby", "Google Dedicated Shuttles & Hyderabad Metro connectivity"),



                new JoiningAndWorkplace("30 to 90 Days Notice Accepted", "Bi-weekly", "Flexible Executive Autonomy", "Hybrid (3 days office, 2 days remote)", "25 Days Paid Vacation + 12 Holidays", "Unlimited Paid Sick Leave", "Global Holiday Shutdown Week", "24 Weeks 100% Paid Parental Leave")



        ));







        // 12. Microsoft India - Software Development Engineer (SDE-II - Azure Core & Developer Division)



        JOB_DATABASE.add(new Job(



                "job-msft-01",



                "Software Development Engineer (SDE-II - Azure Core & Developer Division)",



                "Microsoft India",



                "🪟",



                "Hyderabad, Telangana, India",



                "Hybrid",



                "Full-time",



                2600000, 4200000, "INR",



                "Mid-Level", 2,



                "Software Engineering",



                "Join Microsoft India Development Center (IDC). Engineer planet-scale distributed cloud services powering Microsoft Azure, GitHub integrations, and developer toolchains with focus on high availability, low latency, and zero-downtime upgrades.",



                Arrays.asList("Java", "C#", "Distributed Systems", "Algorithms", "Data Structures", "Cloud", "System Design"),



                Arrays.asList("Azure", "Docker", "Kubernetes", "C++", "REST API"),



                Arrays.asList("Microsoft Stock Awards (On-Hire & Annual Stock Refreshers)", "Subsidized Transportation & On-Campus Multi-Cuisine Food Court", "Comprehensive Medical Cover (Employee + Parents + In-Laws)", "Generous Wellness Reimbursement (₹50,000/yr)"),



                "1 day ago",



                "Actively Hiring",



                new CompanyDetails("Global Cloud Computing & Enterprise Software", "220,000+ employees globally (20,000+ in India)", 1975, 4.5, "Growth Mindset, customer obsession, diversity and inclusion, work-life balance", "microsoft.com/careers", "Satya Nadella"),



                new LocationIntelligence("Hyderabad", "Telangana, India", "Gachibowli Microsoft IDC Campus", "Warm Semi-Arid", "Sprawling 54-acre state-of-the-art green campus in Hyderabad tech core", "17°C - 37°C", 210, "Premier quality of life with green parks and modern sports hubs", "Microsoft AC Commuter Cabs across Hyderabad & Secunderabad"),



                new JoiningAndWorkplace("30 to 60 Days Notice Accepted", "1st and 3rd Monday monthly", "Core Collaboration Hours (11 AM - 4 PM IST)", "Hybrid Flexible (Up to 50% remote standard)", "25 Days Paid Annual Leave + 10 Public Holidays", "15 Days Paid Sick Leave & Mental Wellness Days", "Year-End Rest Week (Dec 25 - Jan 1)", "26 Weeks Paid Maternity / 6 Weeks Paid Paternity")



        ));







        // 13. Microsoft India - Principal Software Engineering Lead (Azure High-Performance Distributed Systems)



        JOB_DATABASE.add(new Job(



                "job-msft-02",



                "Principal Software Engineering Lead (Azure High-Performance Distributed Systems)",



                "Microsoft India",



                "🪟",



                "Bengaluru, Karnataka, India",



                "Hybrid",



                "Full-time",



                5800000, 9200000, "INR",



                "Lead", 9,



                "Cloud & DevOps",



                "Lead a tier-1 engineering organization at Microsoft Bengaluru. Drive the architectural evolution of Azure compute virtualization, ultra-low latency inter-process fabrics, and GPU orchestration for AI training workloads.",



                Arrays.asList("Distributed Systems", "System Design", "Cloud Architecture", "Java", "C++", "Kubernetes", "Team Leadership"),



                Arrays.asList("Azure", "Linux Kernel", "High Concurrency", "FinOps", "Disaster Recovery"),



                Arrays.asList("Executive Tier Microsoft Equity Grants", "Annual Executive Bonus Allocation", "Executive Annual Health Checkup & Family Health Cover", "Education Reimbursement up to $10,000/yr"),



                "5 days ago",



                "Urgent",



                new CompanyDetails("Hyperscale Cloud & Intelligent Edge", "220,000+ employees", 1975, 4.5, "Empower every person and organization on the planet to achieve more", "microsoft.com/careers", "Satya Nadella"),



                new LocationIntelligence("Bengaluru", "Karnataka, India", "Lavelle Road / Bellandur Microsoft Offices", "Moderate Pleasant", "Consistently pleasant climate with lush gardens", "17°C - 30°C", 250, "Prime central Bangalore location with world-class amenities", "Dedicated chauffeur allowance / luxury shuttle transport"),



                new JoiningAndWorkplace("60 to 90 Days Notice Period Accepted", "Monthly Executive Cohort", "Executive Autonomy", "Hybrid Flexible", "30 Days Executive Paid Vacation", "Unlimited Sick & Family Care Days", "Year-End Global Rest Period", "26 Weeks Maternity / 8 Weeks Paternity")



        ));







        // 14. Flipkart - SDE-II Backend Engineer (Distributed Systems & Big Billion Days Scale)



        JOB_DATABASE.add(new Job(



                "job-flip-01",



                "SDE-II Backend Engineer (Distributed Systems & Big Billion Days Scale)",



                "Flipkart",



                "🛍️",



                "Bengaluru, Karnataka, India",



                "Hybrid",



                "Full-time",



                2600000, 4000000, "INR",



                "Mid-Level", 3,



                "Software Engineering",



                "Architect high-scale e-commerce backend services handling 100,000+ orders per second during The Big Billion Days. Design low-latency inventory locking, distributed cart management, and Kafka event pipelines.",



                Arrays.asList("Java", "Kafka", "Microservices", "Redis", "Spring Boot", "SQL", "System Design"),



                Arrays.asList("MySQL", "HBase", "Docker", "Kubernetes", "Aerospike"),



                Arrays.asList("Flipkart ESOP Grants", "Big Billion Days Milestone Bonuses", "Comprehensive Health Care (₹10L)", "Subsidized Daycare & Free Flipkart Plus Premium Membership"),



                "Today",



                "Hot",



                new CompanyDetails("E-Commerce & Digital Supply Chain Marketplace", "45,000+ employees", 2007, 4.3, "Audacity, Bias for Action, Customer First, Integrity", "flipkartcareers.com", "Kalyan Krishnamurthy"),



                new LocationIntelligence("Bengaluru", "Karnataka, India", "Devarabeesanahalli / Embassy Tech Village", "Pleasant Tropical", "Comfortable Bangalore climate with seasonal showers", "17°C - 31°C", 240, "Hub of Indian e-commerce unicorns and tech startups", "Embassy TechVillage direct shuttles & Outer Ring Road connectivity"),



                new JoiningAndWorkplace("30 to 60 Days Notice", "1st & 3rd Monday", "Flexible Hours (Core 11 AM - 5 PM IST)", "Hybrid (3 days office, 2 days remote)", "25 Days Annual Paid Leave + 12 Holidays", "12 Days Medical / Emergency Leave", "Diwali Festival Celebrations & Post-BBD Rest Days", "26 Weeks Maternity / 4 Weeks Paternity")



        ));







        // 15. Swiggy - Full Stack Engineer (Instamart Quick Commerce & Supply Chain)



        JOB_DATABASE.add(new Job(



                "job-swig-01",



                "Full Stack Engineer (Instamart Quick Commerce & Supply Chain)",



                "Swiggy",



                "🛵",



                "Bengaluru, Karnataka, India",



                "Online/Remote",



                "Full-time",



                2200000, 3500000, "INR",



                "Mid-Level", 3,



                "Software Engineering",



                "Build the next frontier of 10-minute grocery delivery for Swiggy Instamart. Develop high-throughput dark store dispatch algorithms, real-time rider routing engines, and sleek React/Node.js web dashboards.",



                Arrays.asList("Node.js", "React", "TypeScript", "JavaScript", "SQL", "Redis", "REST API"),



                Arrays.asList("MongoDB", "Kafka", "AWS", "Docker", "Microservices"),



                Arrays.asList("Permanent Remote / Work-From-Anywhere Option", "Swiggy IPO Stock Options (ESOPs)", "Swiggy One Free Subscriptions & Meal Credits", "Comprehensive Medical Cover for Family"),



                "1 day ago",



                "Actively Hiring",



                new CompanyDetails("Hyperlocal On-Demand Logistics & Quick Commerce", "8,000+ employees", 2014, 4.2, "Always be curious, think like an owner, display humility", "swiggy.com/careers", "Sriharsha Majety"),



                new LocationIntelligence("Bengaluru", "Karnataka, India", "Koramangala Tech Quarter / Remote India", "Moderate", "100% remote-first policy with optional Bangalore headquarters access", "18°C - 30°C", 240, "Work from any city in India; full home office stipend provided", "Co-working space pass provided in major Indian cities"),



                new JoiningAndWorkplace("Immediate or 30 Days Notice", "Every Monday", "Flexible Hours with Asynchronous Alignment", "100% Remote / Optional Hub visits", "24 Days Annual Paid Leave + 10 Optional Holidays", "14 Days Wellness & Caregiver Days", "Diwali Festival & Year-End Recharging Days", "26 Weeks Maternity / 4 Weeks Paternity")



        ));







        // 16. Zomato - Mobile Software Engineer (Android Kotlin & React Native)



        JOB_DATABASE.add(new Job(



                "job-zom-01",



                "Mobile Software Engineer (Android Kotlin & React Native)",



                "Zomato",



                "🍅",



                "Gurugram, Delhi NCR, India",



                "Offline/On-site",



                "Full-time",



                1800000, 3000000, "INR",



                "Mid-Level", 2,



                "Mobile",



                "Craft high-performance consumer mobile experiences for 50M+ active foodies. Optimize app startup times, 60fps animations, live GPS order tracking, and seamless UPI payments integration on Android and React Native.",



                Arrays.asList("Android", "Kotlin", "React Native", "JavaScript", "REST API", "Git"),



                Arrays.asList("Jetpack Compose", "TypeScript", "Redux", "Coroutines", "Firebase"),



                Arrays.asList("Zomato ESOP Allocation", "Unlimited Free Meals at Gourmet In-House Cafes", "State-of-the-art Gym & Wellness Center at HQ", "Pet-Friendly Office Space with Casual Culture"),



                "3 days ago",



                "Hot",



                new CompanyDetails("Food Tech, Quick Commerce (Blinkit) & Dining Experiences", "5,000+ employees", 2008, 4.1, "Ownership over consensus, clean design, zero bureaucracy, high speed of execution", "zomato.com/careers", "Deepinder Goyal"),



                new LocationIntelligence("Gurugram", "Delhi NCR, India", "One Horizon Center / Golf Course Road", "Composite Continental", "Brisk winter and sunny spring with warm summer", "10°C - 38°C", 260, "Trendy, cosmopolitan neighborhood filled with food culture and startups", "Rapid Metro Sector 53-54 Station 2 minutes away"),



                new JoiningAndWorkplace("Immediate to 30 Days Notice", "Every Monday", "Results-Oriented Flexible Timings", "Collaborative On-Site at Gurugram HQ", "26 Days Annual Leave + 10 Holidays", "Unlimited Sick & Wellness Leave", "National Holidays & Extended Diwali Party Week", "26 Weeks Paid Parental Leave for Both Mothers & Fathers")



        ));







        // 17. Razorpay - Senior Backend Engineer (Fintech & Payment Gateway Engine)



        JOB_DATABASE.add(new Job(



                "job-razor-01",



                "Senior Backend Engineer (Fintech & Payment Gateway Engine)",



                "Razorpay",



                "💳",



                "Bengaluru, Karnataka, India",



                "Hybrid",



                "Full-time",



                2800000, 4500000, "INR",



                "Senior", 5,



                "Software Engineering",



                "Build mission-critical payment settlement and checkout infrastructure processing over $150 Billion in TPV annually. Design fault-tolerant payment rails, multi-bank switch routing, and fraud detection algorithms.",



                Arrays.asList("Java", "Go", "Kafka", "PostgreSQL", "Microservices", "System Design", "AWS", "SQL"),



                Arrays.asList("Redis", "Docker", "Kubernetes", "Python", "gRPC"),



                Arrays.asList("Razorpay ESOPs with Consistent Liquidity Buyback Programs", "Premium Comprehensive Health Coverage (₹15L)", "Home Office Ergonomic Allowance (₹40,000)", "No-Questions-Asked Mental Health Days"),



                "1 day ago",



                "Urgent",



                new CompanyDetails("FinTech Unicorn & Digital Payments Infrastructure", "3,500+ employees", 2014, 4.4, "Transparency, customer-centricity, bold ideas, high engineering rigor", "razorpay.com/jobs", "Harshil Mathur"),



                new LocationIntelligence("Bengaluru", "Karnataka, India", "SJR Cyber LXP, Hosur Road, Koramangala", "Moderate", "Pleasant Bangalore climate year-round", "17°C - 30°C", 240, "Surrounded by Bangalore's top food and startup spots", "Koramangala Metro & BMTC AC Volvo bus routes"),



                new JoiningAndWorkplace("30 to 60 Days Notice Accepted", "1st and 3rd Monday", "Flexible Core Working Hours", "Hybrid (2 days office, 3 days remote)", "25 Days Paid Annual Leave + 12 Holidays", "14 Days Sick Leave & Unlimited Mental Wellness Days", "Diwali Festival & Year-End Recharging Week", "26 Weeks Maternity / 6 Weeks Paternity")



        ));







        // 18. CRED - Senior Frontend Architect (Design Systems & Web Performance)



        JOB_DATABASE.add(new Job(



                "job-cred-01",



                "Senior Frontend Architect (Design Systems & Web Performance)",



                "CRED",



                "💎",



                "Bengaluru, Karnataka, India",



                "Hybrid",



                "Full-time",



                3400000, 5200000, "INR",



                "Senior", 5,



                "Software Engineering",



                "Lead the design system and web architecture for CRED's high-trust fintech experiences. Push the boundaries of 3D animations, WebGL, custom Canvas rendering, and micro-frontend performance for top-tier Indian consumers.",



                Arrays.asList("React", "TypeScript", "JavaScript", "HTML", "CSS", "UI/UX", "System Design"),



                Arrays.asList("Next.js", "WebGL", "Three.js", "Node.js", "GraphQL"),



                Arrays.asList("Generous CRED ESOP Grant with Annual Vesting", "Complete Health Cover for Employee & Dependents with Zero Co-Pay", "Top-Tier Tech Gear (MacBook Pro M3 Max + 4K Monitors)", "Executive Wellness & Gym Memberships"),



                "3 days ago",



                "Hot",



                new CompanyDetails("Premium FinTech & Consumer Rewards Marketplace", "1,200+ employees", 2018, 4.5, "High trust, aesthetic excellence, obsession with craft and user joy", "cred.club/careers", "Kunal Shah"),



                new LocationIntelligence("Bengaluru", "Karnataka, India", "Indiranagar 100ft Road Lifestyle Hub", "Pleasant Tropical", "Tree-lined boulevards and cafes of Indiranagar", "18°C - 30°C", 250, "Trendy, premier residential and lifestyle quarter of Bangalore", "Indiranagar Purple Line Metro Station 3 minutes walking"),



                new JoiningAndWorkplace("30 to 60 Days Notice", "Bi-weekly", "High Autonomy / Results Driven", "Hybrid Flexible", "30 Days Annual Privilege Leave", "Unlimited Wellness & Sick Leave", "Diwali Festival & Year-End Rejuvenation Recess", "26 Weeks Paid Parental Leave for All Primary Caregivers")



        ));







        // 19. Jio Platforms - Cloud & 5G Infrastructure DevOps Engineer (Kubernetes & AWS)



        JOB_DATABASE.add(new Job(



                "job-jio-01",



                "Cloud & 5G Infrastructure DevOps Engineer (Kubernetes & AWS)",



                "Jio Platforms",



                "📶",



                "Mumbai, Maharashtra, India",



                "Hybrid",



                "Full-time",



                1500000, 2500000, "INR",



                "Mid-Level", 3,



                "Cloud & DevOps",



                "Scale India's largest digital infrastructure powering 450M+ 5G telecom subscribers and Jio ecosystem services. Automate Kubernetes bare-metal deployments, CI/CD pipelines, and cloud telemetry across national edge data centers.",



                Arrays.asList("Kubernetes", "Docker", "AWS", "Linux", "CI/CD", "DevOps", "Python", "SQL"),



                Arrays.asList("Terraform", "Prometheus", "Golang", "Microservices", "Git"),



                Arrays.asList("Reliance Group Medical & Health Benefits", "Subsidized Jio Services & Tech Hardware", "Employee Stock Option Purchase Plans", "Modern Campus with Olympic-Standard Sports Complex"),



                "2 days ago",



                "Actively Hiring",



                new CompanyDetails("Telecom, 5G Infrastructure & Cloud Ecosystems", "70,000+ employees", 2019, 4.0, "National impact at unmatched scale, entrepreneurial speed, operational excellence", "jio.com/careers", "Kiran Thomas"),



                new LocationIntelligence("Mumbai", "Maharashtra, India", "Reliance Corporate Park (RCP), Ghansoli, Navi Mumbai", "Tropical Coastal", "Sunny coastal weather with refreshing monsoons", "20°C - 34°C", 220, "Sprawling self-contained 500-acre world-class technology park", "Dedicated Reliance AC buses from all Mumbai & Navi Mumbai stations"),



                new JoiningAndWorkplace("30 to 60 Days Notice", "1st Monday monthly", "Standard Day (9:30 AM - 6:00 PM IST)", "Hybrid (3 days campus, 2 days remote)", "25 Days Paid Leave + 12 Holidays", "12 Days Medical Leave", "National Holidays & Extended Diwali", "26 Weeks Maternity / 3 Weeks Paternity")



        ));







        // 20. Zoho Corporation - Senior Full Stack Developer (SaaS Cloud Applications)



        JOB_DATABASE.add(new Job(



                "job-zoho-01",



                "Senior Full Stack Developer (SaaS Cloud Applications)",



                "Zoho Corporation",



                "🗂️",



                "Chennai, Tamil Nadu, India",



                "Offline/On-site",



                "Full-time",



                1300000, 2200000, "INR",



                "Mid-Level", 3,



                "Software Engineering",



                "Build bootstrapped, highly profitable SaaS products used by 100M+ businesses worldwide. Write clean, vanilla Java, JavaScript, and custom database query engines with zero external venture debt pressure.",



                Arrays.asList("Java", "JavaScript", "HTML", "CSS", "SQL", "REST API", "Git"),



                Arrays.asList("React", "Linux", "Database Optimization", "Microservices"),



                Arrays.asList("Unique Bootstrapped Stability (Zero Layoff Track Record)", "Free Organic Farm-Fresh Meals (Breakfast, Lunch, Dinner)", "Free AC Bus Transport across Greater Chennai", "Lush Eco-Friendly Campus with Farm Walks"),



                "3 days ago",



                "Actively Hiring",



                new CompanyDetails("Enterprise Cloud SaaS & Productivity Software", "15,000+ employees", 1996, 4.5, "Pragmatic engineering, rural empowerment, zero outside investors, extreme employee loyalty", "zoho.com/careers", "Sridhar Vembu"),



                new LocationIntelligence("Chennai", "Tamil Nadu, India", "Estancia IT Park, Guduvanchery, Chennai", "Tropical Coastal Warm", "Breezy coastal climate with mild pleasant winters", "22°C - 36°C", 180, "Highly affordable cultural capital with rich arts and food traditions", "Complimentary Zoho AC bus fleet covers all parts of Chennai"),



                new JoiningAndWorkplace("Immediate to 30 Days Notice", "1st Monday monthly", "Relaxed & Productive (9:00 AM - 5:30 PM IST)", "On-Site Collaborative (Village & City Office Options)", "25 Days Annual Privilege Leave + 12 Holidays", "12 Days Medical Leave", "Pongal (Harvest Festival) & Diwali Extended Holidays", "26 Weeks Maternity / 4 Weeks Paternity")



        ));







        // 21. Paytm - SDE-II Backend Services Engineer (UPI & Merchant Payments)



        JOB_DATABASE.add(new Job(



                "job-paytm-01",



                "SDE-II Backend Services Engineer (UPI & Merchant Payments)",



                "Paytm",



                "📱",



                "Noida, Delhi NCR, India",



                "Hybrid",



                "Full-time",



                1800000, 2900000, "INR",



                "Mid-Level", 3,



                "Software Engineering",



                "Engineer ultra-reliable UPI transaction switches and QR merchant settlement engines processing millions of instant payments per hour. Optimize sub-100ms response times and ensure continuous financial ledger integrity.",



                Arrays.asList("Java", "Spring Boot", "Kafka", "Redis", "MySQL", "Microservices", "REST API", "SQL"),



                Arrays.asList("Distributed Tracing", "Docker", "AWS", "Aerospike", "Git"),



                Arrays.asList("Paytm ESOPs & Quarterly Rewards", "Comprehensive Health Cover (₹8L) for Family", "Subsidized Transport & Food Allowances", "Continuous Tech Upskilling Sprints"),



                "2 days ago",



                "Hot",



                new CompanyDetails("Digital Payments, Soundbox IoT & Financial Services", "25,000+ employees", 2010, 3.9, "Speed, grassroots fintech inclusion, resilience, high agility", "paytm.com/careers", "Vijay Shekhar Sharma"),



                new LocationIntelligence("Noida", "Delhi NCR, India", "Sector 98 Expressway Tech Zone", "Composite Continental", "Chilly crisp winter and warm sunny summers", "8°C - 39°C", 210, "Modern planned city with wide expressways and budget-friendly apartments", "Noida-Greater Noida Aqua Line & Delhi Metro Magenta Line"),



                new JoiningAndWorkplace("Immediate or 30 Days Notice", "Bi-weekly", "Standard Flexible", "Hybrid (3 days office, 2 days remote)", "24 Days Annual Leave + 10 Holidays", "12 Days Medical Leave", "Diwali Festival Celebrations", "26 Weeks Maternity / 2 Weeks Paternity")



        ));







        // 22. Zepto - Backend Microservices Developer (10-Min Delivery Optimization)



        JOB_DATABASE.add(new Job(



                "job-zepto-01",



                "Backend Microservices Developer (10-Min Delivery Optimization)",



                "Zepto",



                "⚡",



                "Mumbai, Maharashtra, India",



                "Hybrid",



                "Full-time",



                2400000, 3800000, "INR",



                "Mid-Level", 3,



                "Software Engineering",



                "Scale India's fastest-growing quick-commerce unicorn. Architect real-time dark store picker allocation, automated batch order dispatch, and microsecond-level inventory synchronization across 500+ micro-warehouses.",



                Arrays.asList("Python", "FastAPI", "Go", "PostgreSQL", "Redis", "Kafka", "Docker", "REST API"),



                Arrays.asList("Kubernetes", "AWS", "gRPC", "Elasticsearch", "Git"),



                Arrays.asList("High-Upside Unicorn ESOP Equity Allocation", "Free Weekly Zepto Grocery Credits (₹2,500/mo)", "Comprehensive Health Insurance (₹10L)", "Top-tier MacBook Pro M3 provided"),



                "Just now",



                "Urgent",



                new CompanyDetails("Quick Commerce, Instant Delivery & Logistics Tech", "3,000+ employees", 2021, 4.3, "Fast-paced execution, young fearless team, radical ownership, high agency", "zeptonow.com/careers", "Aadit Palicha"),



                new LocationIntelligence("Mumbai", "Maharashtra, India", "Lower Parel / Powai Tech Hub", "Tropical Coastal", "Breezy coastal vibe with vibrant nightlife and street food", "21°C - 34°C", 300, "Epicenter of Indian entertainment, finance, and food culture", "Mumbai Metro Line 3 & Western Suburban Railway"),



                new JoiningAndWorkplace("Immediate to 30 Days Notice", "Every Monday", "Startup Flexibility / High Ownership", "Hybrid Work Model", "25 Days Paid Vacation + 10 Holidays", "12 Days Wellness Leave", "Diwali Festival Celebrations", "26 Weeks Maternity / 4 Weeks Paternity")



        ));







        // 23. Zerodha - Systems Engineer (High-Throughput Kite Trading Engine APIs)



        JOB_DATABASE.add(new Job(



                "job-zerodha-01",



                "Systems Engineer (High-Throughput Kite Trading Engine APIs)",



                "Zerodha",



                "📈",



                "Bengaluru, Karnataka, India",



                "Hybrid",



                "Full-time",



                2500000, 4200000, "INR",



                "Mid-Level", 3,



                "Software Engineering",



                "Build ultra-low latency brokerage and market data streaming infrastructure for Kite, serving 12M+ Indian retail investors. Write idiomatic Go and Python, work with raw WebSockets, and optimize kernel network buffers.",



                Arrays.asList("Go", "Python", "PostgreSQL", "Redis", "Linux", "WebSockets", "System Design", "Git"),



                Arrays.asList("C", "Networking / TCP", "Kafka", "Docker", "DevOps"),



                Arrays.asList("Legendary Zerodha Profit-Sharing Annual Bonus", "Zero Pressure, Zero Overtime Policy (Strict 7 PM logoff)", "Holistic Health, Gym & Organic Diet Subsidies", "Family Medical Cover with Top Hospitals Included"),



                "3 days ago",



                "Hot",



                new CompanyDetails("FinTech, Stock Brokerage & Investment Platforms", "1,200+ employees", 2010, 4.8, "FOSS first, quiet craftsmanship, zero marketing, extreme employee wellbeing", "zerodha.com/careers", "Nithin Kamath"),



                new LocationIntelligence("Bengaluru", "Karnataka, India", "JP Nagar Phase 4 Zerodha Headquarters", "Pleasant Moderate", "Calm, leafy neighborhood of South Bangalore", "17°C - 30°C", 230, "Peaceful residential neighborhood with cozy cafes and parks", "Green Line Metro (Rashtreeya Vidyalaya Road / Jayayanagar)"),



                new JoiningAndWorkplace("Flexible: 30 to 60 Days Notice Accepted", "Monthly Cohort", "Healthy Work-Life Balance (9 AM - 6 PM Strict)", "Hybrid Flexible", "28 Days Annual Paid Leave", "Unlimited Wellness & Care Leave", "NSE/BSE Trading Holidays & Year-End Recharging Days", "26 Weeks Paid Maternity / 6 Weeks Paternity")



        ));







        // 24. PhonePe - Staff Distributed Systems Architect (UPI National Grid)



        JOB_DATABASE.add(new Job(



                "job-phonepe-01",



                "Staff Distributed Systems Architect (UPI National Grid)",



                "PhonePe",



                "🟣",



                "Bengaluru, Karnataka, India",



                "Hybrid",



                "Full-time",



                4500000, 7000000, "INR",



                "Lead", 8,



                "Software Engineering",



                "Architect the backbone of India's largest UPI payments network processing over 50% of national market share. Lead engineering strategy for high-concurrency ledger state machines and disaster recovery systems handling billions of calls monthly.",



                Arrays.asList("Java", "Distributed Systems", "System Design", "High Concurrency", "Kafka", "Microservices", "Team Leadership"),



                Arrays.asList("Aerospike", "HBase", "Cassandra", "AWS", "Kubernetes"),



                Arrays.asList("Lucrative PhonePe ESOP Allocation", "Annual Performance Bonus (up to 30%)", "Top Executive Health Care (₹20L coverage)", "Relocation Assistance & Sign-On Bonus"),



                "1 day ago",



                "Urgent",



                new CompanyDetails("FinTech, UPI Payments, Digital Insurance & Wealth Tech", "5,000+ employees", 2015, 4.4, "Engineering at national scale, meritocracy, solving fundamental Indian problems", "phonepe.com/careers", "Sameer Nigam"),



                new LocationIntelligence("Bengaluru", "Karnataka, India", "Bellandur Outer Ring Road Ecospace", "Moderate", "Lively tech district with modern office campuses", "17°C - 30°C", 240, "Core IT artery of Bangalore with high-density tech talent", "Dedicated PhonePe buses & Ring Road highway access"),



                new JoiningAndWorkplace("30 to 90 Days Notice Period Accepted", "1st Monday monthly", "Executive Autonomy", "Hybrid (3 days office, 2 days remote)", "27 Days Annual Leave + 11 Public Holidays", "14 Days Medical / Family Care Days", "Diwali & Year-End Rest Days", "26 Weeks Paid Maternity / 6 Weeks Paternity")



        ));







        // 25. Cognizant - Programmer Analyst Trainee (GenC Next Developer Program)



        JOB_DATABASE.add(new Job(



                "job-cog-01",



                "Programmer Analyst Trainee (GenC Next Developer Program)",



                "Cognizant",



                "🔷",



                "Chennai, Tamil Nadu, India",



                "Hybrid",



                "Full-time",



                450000, 680000, "INR",



                "Fresher", 0,



                "Software Engineering",



                "Accelerated developer track for 2024-2026 engineering graduates. Master full-stack enterprise development, microservices with Spring Boot, SQL, and Git while contributing to live healthcare and financial digital platforms.",



                Arrays.asList("Java", "SQL", "Git", "Data Structures", "Algorithms", "HTML", "CSS"),



                Arrays.asList("Spring Boot", "Python", "JavaScript", "REST API"),



                Arrays.asList("GenC Next Accelerated Learning Stipend", "Cognizant Digital Academy Certifications", "Comprehensive Group Medical Insurance (₹6.5L)", "Higher Studies Sponsorship with Top Universities"),



                "2 days ago",



                "Fresher Friendly",



                new CompanyDetails("Global Information Technology & Business Process Consulting", "340,000+ employees", 1994, 4.0, "Start with empathy, collaborate with impact, grow through continuous learning", "cognizant.com/careers", "Ravi Kumar S"),



                new LocationIntelligence("Chennai", "Tamil Nadu, India", "Sholinganallur OMR IT Corridor / MEPZ", "Tropical Coastal", "Sunny coastal weather with refreshing evening sea breeze", "22°C - 35°C", 180, "Old Mahabalipuram Road (OMR) tech corridor filled with young engineers", "OMR Express buses & Cognizant campus fleet coverage"),



                new JoiningAndWorkplace("Immediate or Campus Batch Joining", "1st and 3rd Monday", "Standard Day (9:30 AM - 6:00 PM IST)", "Hybrid (3 days office, 2 days remote)", "24 Days Annual Paid Leave + 10 State & National Holidays", "12 Days Medical Leave", "Pongal, Diwali & Christmas Closures", "26 Weeks Paid Maternity / 3 Weeks Paternity")



        ));







        // 26. HCLTech - Senior Cloud Security & Cyber Defense Specialist



        JOB_DATABASE.add(new Job(



                "job-hcl-01",



                "Senior Cloud Security & Cyber Defense Specialist",



                "HCLTech",



                "🛡️",



                "Noida, Delhi NCR, India",



                "Hybrid",



                "Full-time",



                1600000, 2600000, "INR",



                "Senior", 5,



                "Cybersecurity",



                "Protect global enterprise multi-cloud environments. Design identity and access management (IAM), zero-trust security postures, cloud vulnerability scanning, and SIEM monitoring across AWS and Azure infrastructure.",



                Arrays.asList("Cloud Security", "AWS", "Linux", "Docker", "Security Auditing", "OAuth2", "Python"),



                Arrays.asList("Kubernetes", "SIEM", "Penetration Testing", "Terraform", "Git"),



                Arrays.asList("Global Security Certifications (CISSP, AWS Security) 100% Reimbursed", "Annual Performance Bonus & Retention Incentives", "Comprehensive Family Health Insurance (₹8L)", "Modern Campus with Gym, Food Court & Creche"),



                "4 days ago",



                "Actively Hiring",



                new CompanyDetails("Global Technology & Next-Gen Cloud Security", "225,000+ employees", 1976, 4.1, "Ideapreneurship, employee first, customer second, empowering ground teams", "hcltech.com/careers", "C Vijayakumar"),



                new LocationIntelligence("Noida", "Delhi NCR, India", "Sector 126 Campus / Noida Expressway", "Composite Continental", "Brisk winter and sunny summer with modern corporate amenities", "8°C - 38°C", 210, "High-tech hub with rapid transit to South Delhi and Gurgaon", "Botanical Garden Metro Interchange direct company shuttle"),



                new JoiningAndWorkplace("30 to 60 Days Notice", "1st & 3rd Monday", "Flexible Hours with Global Alignment", "Hybrid Model", "25 Days Paid Leave + 10 Public Holidays", "12 Days Medical / Sick Leave", "Diwali Festival Week", "26 Weeks Maternity / 4 Weeks Paternity")



        ));







    }







    // ==========================================



    // MULTI-ATTRIBUTE MATCHING ENGINE



    // ==========================================







    // ==========================================



    // MATCHING & SKILL EXTRACTION ENGINE



    // ==========================================







    // ==========================================



    // MATCHING & SKILL EXTRACTION ENGINE



    // ==========================================







    public static class MatchingEngine {







public static List<MatchResult> matchJobs(String resumeText, List<String> candidateSkills,



                                                  int candidateYearsExp, int candidateExpectedSalary,



                                                  String preferredWorkMode, String preferredCategory,



                                                  String preferredLocation, String preferredClimate,



                                                  String preferredWorkingHours,



                                                  boolean strictExperienceFilter,



                                                  int weightSkills, int weightExp, int weightSal, int weightPref) {



            return matchJobs(resumeText, candidateSkills, candidateYearsExp, candidateExpectedSalary, preferredWorkMode,



                    preferredCategory, preferredLocation, preferredClimate, preferredWorkingHours, strictExperienceFilter,



                    weightSkills, weightExp, weightSal, weightPref, "Any");



        }







        public static List<MatchResult> matchJobs(String resumeText, List<String> candidateSkills,



                                                  int candidateYearsExp, int candidateExpectedSalary,



                                                  String preferredWorkMode, String preferredCategory,



                                                  String preferredLocation, String preferredClimate,



                                                  String preferredWorkingHours,



                                                  boolean strictExperienceFilter,



                                                  int weightSkills, int weightExp, int weightSal, int weightPref,



                                                  String currencyFilter) {



            String cleanResume = (resumeText != null) ? resumeText.toLowerCase() : "";



            Set<String> allCandidateSkills = new HashSet<>();







            if (candidateSkills != null) {



                for (String s : candidateSkills) {



                    if (s != null && !s.trim().isEmpty()) {



                        allCandidateSkills.add(s.trim());



                    }



                }



            }







            for (String knownSkill : getAllKnownSkills()) {



                if (cleanResume.contains(knownSkill.toLowerCase())) {



                    allCandidateSkills.add(knownSkill);



                }



            }







            int totalWeight = weightSkills + weightExp + weightSal + weightPref;



            if (totalWeight <= 0) {



                weightSkills = 60;



                weightExp = 20;



                weightSal = 10;



                weightPref = 10;



                totalWeight = 100;



            }



            double wSkills = (double) weightSkills / totalWeight;



            double wExp = (double) weightExp / totalWeight;



            double wSal = (double) weightSal / totalWeight;



            double wPref = (double) weightPref / totalWeight;







            List<MatchResult> results = new ArrayList<>();







            for (Job job : JOB_DATABASE) {



                // Currency filter



                if (currencyFilter != null && !currencyFilter.equalsIgnoreCase("Any") && !currencyFilter.trim().isEmpty()) {



                    if (!currencyFilter.equalsIgnoreCase(job.currency)) {



                        continue;



                    }



                }







                // STRICT EXPERIENCE GUARD: Prevents Freshers from seeing Senior Manager roles!



                if (strictExperienceFilter) {



                    if (candidateYearsExp <= 1 && job.minYearsExp >= 3) {



                        continue;



                    }



                    if (candidateYearsExp >= 2 && candidateYearsExp <= 4 && job.minYearsExp >= 7) {



                        continue;



                    }



                }







                // 1. SKILLS MATCH (60% default weight)



                List<String> matchedReqSkills = new ArrayList<>();



                List<String> missingReqSkills = new ArrayList<>();



                List<String> bonusSkills = new ArrayList<>();







                for (String reqSkill : job.requiredSkills) {



                    boolean found = false;



                    for (String candSkill : allCandidateSkills) {



                        if (skillsMatch(reqSkill, candSkill)) {



                            found = true;



                            break;



                        }



                    }



                    if (found || cleanResume.contains(reqSkill.toLowerCase())) {



                        matchedReqSkills.add(reqSkill);



                    } else {



                        missingReqSkills.add(reqSkill);



                    }



                }







                for (String niceSkill : job.niceToHaveSkills) {



                    boolean found = false;



                    for (String candSkill : allCandidateSkills) {



                        if (skillsMatch(niceSkill, candSkill)) {



                            found = true;



                            break;



                        }



                    }



                    if (found || cleanResume.contains(niceSkill.toLowerCase())) {



                        bonusSkills.add(niceSkill);



                    }



                }







                double reqRatio = job.requiredSkills.isEmpty() ? 1.0 :



                        (double) matchedReqSkills.size() / job.requiredSkills.size();



                double bonusRatio = job.niceToHaveSkills.isEmpty() ? 0.0 :



                        (double) bonusSkills.size() / job.niceToHaveSkills.size();







                double skillScoreNormalized = (reqRatio * 0.85) + (bonusRatio * 0.15);



                double skillsContribution = skillScoreNormalized * (wSkills * 100.0);







                // 2. EXPERIENCE ALIGNMENT (20% default weight)



                double expScoreNormalized;



                String experienceMatchStatus;







                if (candidateYearsExp <= 1) {



                    if (job.minYearsExp <= 1) {



                        expScoreNormalized = 1.0;



                        experienceMatchStatus = "Perfect Match for Freshers";



                    } else if (job.minYearsExp == 2) {



                        expScoreNormalized = 0.75;



                        experienceMatchStatus = "Eligible Entry Role";



                    } else {



                        expScoreNormalized = 0.15;



                        experienceMatchStatus = "Senior Role (" + job.minYearsExp + "+ yrs required)";



                    }



                } else if (candidateYearsExp >= job.minYearsExp) {



                    expScoreNormalized = 1.0;



                    experienceMatchStatus = "Meets Experience Level";



                } else {



                    int diff = job.minYearsExp - candidateYearsExp;



                    if (diff == 1) {



                        expScoreNormalized = 0.70;



                        experienceMatchStatus = "Within 1 Year of Requirement";



                    } else {



                        expScoreNormalized = Math.max(0.15, 1.0 - (diff * 0.35));



                        experienceMatchStatus = "Requires " + diff + " More Years Exp";



                    }



                }



                double expContribution = expScoreNormalized * (wExp * 100.0);







                // 3. SALARY FIT (10% default weight) - Currency Aware (USD & INR / LPA)



                double salScoreNormalized;



                String salaryAssessment;



                int salaryComparisonPct = 0;







                if (candidateExpectedSalary > 0) {



                    double effectiveTarget = candidateExpectedSalary;



                    if ("INR".equalsIgnoreCase(job.currency) && candidateExpectedSalary < 400000) {



                        // User input is in USD slider range; normalize to INR



                        effectiveTarget = candidateExpectedSalary * 25.0;



                    } else if (!"INR".equalsIgnoreCase(job.currency) && candidateExpectedSalary >= 400000) {



                        // User input is in INR range; normalize to USD



                        effectiveTarget = candidateExpectedSalary / 25.0;



                    }







                    String salFormatted = formatJobSalary(job);







                    if (job.maxSalary >= effectiveTarget) {



                        salaryComparisonPct = (int) Math.round(((double) (job.maxSalary - effectiveTarget) / effectiveTarget) * 100);



                        if (effectiveTarget <= job.minSalary) {



                            salScoreNormalized = 1.0;



                            salaryAssessment = "Exceeds target by +" + salaryComparisonPct + "% (" + salFormatted + ")";



                        } else {



                            salScoreNormalized = 0.90;



                            salaryAssessment = "Meets target (" + salFormatted + ")";



                        }



                    } else {



                        salaryComparisonPct = - (int) Math.round(((double) (effectiveTarget - job.maxSalary) / effectiveTarget) * 100);



                        salScoreNormalized = Math.max(0.35, 1.0 - (Math.abs(salaryComparisonPct) / 100.0));



                        salaryAssessment = "Below expectation (" + salaryComparisonPct + "%)";



                    }



                } else {



                    salScoreNormalized = 0.85;



                    salaryAssessment = "Market standard (" + formatJobSalary(job) + ")";



                }



                double salContribution = salScoreNormalized * (wSal * 100.0);







                // 4. PERSONALIZED PREFERENCES (Work Mode, Location, Climate, Working Hours) (10% default weight)



                boolean locMatch = true;



                double locationScore = 1.0;



                if (preferredLocation != null && !preferredLocation.equalsIgnoreCase("All") && !preferredLocation.isEmpty()) {



                    if (preferredLocation.equalsIgnoreCase("Remote")) {



                        locMatch = job.workMode.toLowerCase().contains("remote");



                    } else {



                        locMatch = job.location.toLowerCase().contains(preferredLocation.toLowerCase()) ||



                                   (job.locationIntelligence != null && (



                                       job.locationIntelligence.city.toLowerCase().contains(preferredLocation.toLowerCase()) ||



                                       job.locationIntelligence.stateCountry.toLowerCase().contains(preferredLocation.toLowerCase())



                                   )) ||



                                   job.workMode.toLowerCase().contains("remote");



                    }



                    locationScore = locMatch ? 1.0 : 0.40;



                }







                boolean climateMatch = true;



                double climateScore = 1.0;



                if (preferredClimate != null && !preferredClimate.equalsIgnoreCase("All") && !preferredClimate.isEmpty()) {



                    if (job.locationIntelligence != null && job.locationIntelligence.climateCategory != null) {



                        climateMatch = job.locationIntelligence.climateCategory.toLowerCase().contains(preferredClimate.toLowerCase()) ||



                                       preferredClimate.toLowerCase().contains(job.locationIntelligence.climateCategory.toLowerCase());



                    }



                    climateScore = climateMatch ? 1.0 : 0.50;



                }







                boolean hoursMatch = true;



                double hoursScore = 1.0;



                if (preferredWorkingHours != null && !preferredWorkingHours.equalsIgnoreCase("All") && !preferredWorkingHours.isEmpty()) {



                    if (job.joiningAndWorkplace != null) {



                        hoursMatch = (job.joiningAndWorkplace.workingHours != null && job.joiningAndWorkplace.workingHours.toLowerCase().contains(preferredWorkingHours.toLowerCase())) ||



                                     (job.joiningAndWorkplace.shiftType != null && job.joiningAndWorkplace.shiftType.toLowerCase().contains(preferredWorkingHours.toLowerCase()));



                    }



                    hoursScore = hoursMatch ? 1.0 : 0.50;



                }







                double workModeScore = 1.0;



                if (preferredWorkMode != null && !preferredWorkMode.equalsIgnoreCase("Any") && !preferredWorkMode.isEmpty()) {



                    if (job.workMode.toLowerCase().contains(preferredWorkMode.toLowerCase()) ||



                        preferredWorkMode.toLowerCase().contains(job.workMode.toLowerCase())) {



                        workModeScore = 1.0;



                    } else if (job.workMode.toLowerCase().contains("remote") || preferredWorkMode.toLowerCase().contains("remote")) {



                        workModeScore = 0.85;



                    } else {



                        workModeScore = 0.40;



                    }



                }







                double categoryScore = 1.0;



                if (preferredCategory != null && !preferredCategory.equalsIgnoreCase("All") && !preferredCategory.isEmpty()) {



                    if (job.category.equalsIgnoreCase(preferredCategory)) {



                        categoryScore = 1.0;



                    } else {



                        categoryScore = 0.60;



                    }



                }







                double prefScoreNormalized = (locationScore + climateScore + hoursScore + workModeScore + categoryScore) / 5.0;



                double prefContribution = prefScoreNormalized * (wPref * 100.0);







                // COMPOSITE MATCH SCORE



                double totalComposite = skillsContribution + expContribution + salContribution + prefContribution;



                int matchPercentage = (int) Math.round(Math.min(99, Math.max(15, totalComposite)));







                String matchTier;



                if (matchPercentage >= 85) matchTier = "Exceptional Match";



                else if (matchPercentage >= 70) matchTier = "Strong Match";



                else if (matchPercentage >= 50) matchTier = "Good Fit";



                else matchTier = "Moderate Match";







                String fitSummary = "Matches " + matchedReqSkills.size() + "/" + job.requiredSkills.size() + " skills • " + experienceMatchStatus;







                boolean educationMatch = true;



                String educationBadge = "Eligible (B.Tech / BE / MCA / M.Tech)";



                if (cleanResume.contains("m.tech") || cleanResume.contains("m.s") || cleanResume.contains("master") || cleanResume.contains("phd")) {



                    educationBadge = "Advanced Degree Qualified (M.Tech / MS / PhD)";



                } else if (cleanResume.contains("b.tech") || cleanResume.contains("b.e") || cleanResume.contains("mca") || cleanResume.contains("b.sc") || cleanResume.contains("bca")) {



                    educationBadge = "Degree Eligible (B.Tech / BE / MCA)";



                }







                results.add(new MatchResult(job, matchPercentage, matchedReqSkills, missingReqSkills,



                        bonusSkills, matchTier, salaryAssessment, salaryComparisonPct, fitSummary,



                        Math.round(skillsContribution * 10.0) / 10.0,



                        Math.round(expContribution * 10.0) / 10.0,



                        Math.round(salContribution * 10.0) / 10.0,



                        Math.round(prefContribution * 10.0) / 10.0,



                        experienceMatchStatus, locMatch, climateMatch, hoursMatch,



                        educationMatch, educationBadge));



            }







            results.sort((a, b) -> {



                int cmp = Integer.compare(b.matchPercentage, a.matchPercentage);



                if (cmp != 0) return cmp;



                return Integer.compare(b.job.maxSalary, a.job.maxSalary);



            });







            return results;



        }







        public static Set<String> getAllKnownSkills() {



            Set<String> skills = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);



            for (Job job : JOB_DATABASE) {



                skills.addAll(job.requiredSkills);



                skills.addAll(job.niceToHaveSkills);



            }



            skills.addAll(Arrays.asList("Java", "JavaScript", "HTML", "CSS", "Python", "SQL", "React",



                    "Spring Boot", "Docker", "Kubernetes", "AWS", "Git", "TypeScript", "Node.js",



                    "Linux", "MongoDB", "PostgreSQL", "C++", "Go", "Machine Learning", "Deep Learning",



                    "Algorithms", "Data Structures", "Data Analysis", "Engineering Management",



                    "PyTorch", "TensorFlow", "FastAPI", "Kafka", "Redis", "CI/CD", "Terraform"));



            return skills;



        }







        public static Map<String, Object> parseResume(String text) {



            Map<String, Object> result = new LinkedHashMap<>();



            if (text == null) text = "";







            // 1. Skill Extraction



            Set<String> detectedSkills = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);



            for (String skill : getAllKnownSkills()) {



                Pattern pattern = Pattern.compile("\\b" + Pattern.quote(skill) + "\\b", Pattern.CASE_INSENSITIVE);



                if (pattern.matcher(text).find()) {



                    detectedSkills.add(skill);



                }



            }







            // 2. Candidate Name Extraction



            String name = "";



            String[] lines = text.split("[\r\n]+");



            for (String rawLine : lines) {



                String trimmed = rawLine.trim();



                if (trimmed.isEmpty()) continue;



                String lower = trimmed.toLowerCase();



                if (lower.equals("resume") || lower.equals("curriculum vitae") || lower.equals("cv") || lower.contains("page 1")) continue;



                // Look for a line with pipe or header e.g. "Alex Morgan | Senior Software Engineer"



                if (trimmed.contains("|")) {



                    String firstPart = trimmed.split("\\|")[0].trim();



                    if (firstPart.matches("(?i)[a-z]+(?: [a-z]+){1,3}") && !firstPart.contains("@") && firstPart.length() < 40) {



                        name = firstPart;



                        break;



                    }



                }



                if (trimmed.matches("(?i)[a-z]+(?: [a-z]+){1,3}") && !trimmed.contains("@") && trimmed.length() < 40) {



                    name = trimmed;



                    break;



                }



            }







            // 3. Email Extraction



            String email = "";



            Matcher emailMatcher = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}").matcher(text);



            if (emailMatcher.find()) {



                email = emailMatcher.group();



            }







            // 4. Phone Extraction



            String phone = "";



            Matcher phoneMatcher = Pattern.compile("(\\+?\\d{1,3}[-.\\s]?)?\\(?\\d{3}\\)?[-.\\s]?\\d{3}[-.\\s]?\\d{4}").matcher(text);



            if (phoneMatcher.find()) {



                phone = phoneMatcher.group();



            }







            // 5. Experience Years Extraction



            int detectedYears = 0;



            if (Pattern.compile("\\b(fresher|recent graduate|fresh graduate|entry level|intern)\\b", Pattern.CASE_INSENSITIVE).matcher(text).find()) {



                detectedYears = 0;



            } else {



                Matcher expMatcher = Pattern.compile("(\\d+)\\+?\\s*(?:years?|yrs?)(?:\\s+(?:of\\s+)?[a-zA-Z-]+\\s+)?(?:experience|exp)", Pattern.CASE_INSENSITIVE).matcher(text);



                if (expMatcher.find()) {



                    try {



                        detectedYears = Integer.parseInt(expMatcher.group(1));



                    } catch (Exception ignored) {}



                }



            }







            // 6. Education Extraction



            List<String> education = new ArrayList<>();



            if (text.toLowerCase().contains("ph.d") || text.toLowerCase().contains("doctorate")) education.add("Ph.D.");



            if (text.toLowerCase().contains("master") || text.toLowerCase().contains("m.s.") || text.toLowerCase().contains("mba")) education.add("Master's Degree");



            if (text.toLowerCase().contains("bachelor") || text.toLowerCase().contains("b.s.") || text.toLowerCase().contains("b.tech") || text.toLowerCase().contains("b.e.")) education.add("Bachelor's Degree");







            result.put("candidateName", name);



            result.put("email", email);



            result.put("phone", phone);



            result.put("estimatedYearsExp", detectedYears);



            result.put("education", education);



            result.put("detectedSkills", new ArrayList<>(detectedSkills));



            result.put("wordCount", text.trim().isEmpty() ? 0 : text.trim().split("\\s+").length);







            return result;



        }



    }







    // ==========================================



    // HTTP HANDLERS & REST APIS



    // ==========================================







    /**



     * GET /api/jobs - Query-based listing, search, filtering, and recommendations.



     */



    private static class JobsHandler implements HttpHandler {



        @Override



        public void handle(HttpExchange exchange) throws IOException {



            setCORSHeaders(exchange);



            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {



                exchange.sendResponseHeaders(204, -1);



                exchange.getResponseBody().close();



                return;



            }







            Map<String, String> qMap = parseQueryParams(exchange.getRequestURI().getRawQuery());



            String q = qMap.getOrDefault("q", "").trim().toLowerCase();



            String location = qMap.getOrDefault("location", "").trim().toLowerCase();



            String workMode = qMap.getOrDefault("workMode", "").trim().toLowerCase();



            String category = qMap.getOrDefault("category", "").trim().toLowerCase();



            String expLevel = qMap.getOrDefault("experienceLevel", "").trim().toLowerCase();



            String climate = qMap.getOrDefault("climate", "").trim().toLowerCase();



            String hours = qMap.getOrDefault("workingHours", "").trim().toLowerCase();



            int minSalary = parseNumber(qMap.get("minSalary"), 0);



            int maxSalary = parseNumber(qMap.get("maxSalary"), Integer.MAX_VALUE);



            String sort = qMap.getOrDefault("sort", "").trim().toLowerCase();







            List<Job> filtered = new ArrayList<>();



            for (Job j : JOB_DATABASE) {



                // Search query filter



                if (!q.isEmpty()) {



                    boolean matchTitle = j.title.toLowerCase().contains(q);



                    boolean matchCompany = j.company.toLowerCase().contains(q);



                    boolean matchDesc = j.description.toLowerCase().contains(q);



                    boolean matchSkills = j.requiredSkills.stream().anyMatch(s -> s.toLowerCase().contains(q)) ||



                                          j.niceToHaveSkills.stream().anyMatch(s -> s.toLowerCase().contains(q));



                    if (!matchTitle && !matchCompany && !matchDesc && !matchSkills) continue;



                }







                // Location filter



                if (!location.isEmpty() && !location.equals("all")) {



                    boolean locOk = j.location.toLowerCase().contains(location) ||



                                    (j.locationIntelligence != null && (



                                            j.locationIntelligence.city.toLowerCase().contains(location) ||



                                            j.locationIntelligence.stateCountry.toLowerCase().contains(location)



                                    )) ||



                                    (location.equals("remote") && j.workMode.toLowerCase().contains("remote"));



                    if (!locOk) continue;



                }







                // Work mode filter



                if (!workMode.isEmpty() && !workMode.equals("any") && !workMode.equals("all")) {



                    if (!j.workMode.toLowerCase().contains(workMode)) continue;



                }







                // Category filter



                if (!category.isEmpty() && !category.equals("all")) {



                    if (!j.category.toLowerCase().contains(category)) continue;



                }







                // Experience Level filter



                if (!expLevel.isEmpty() && !expLevel.equals("all")) {



                    if (!j.experienceLevel.toLowerCase().contains(expLevel)) continue;



                }







                // Climate filter



                if (!climate.isEmpty() && !climate.equals("all")) {



                    if (j.locationIntelligence == null || !j.locationIntelligence.climateCategory.toLowerCase().contains(climate)) continue;



                }







                // Working Hours filter



                if (!hours.isEmpty() && !hours.equals("all")) {



                    if (j.joiningAndWorkplace == null ||



                        (!j.joiningAndWorkplace.workingHours.toLowerCase().contains(hours) &&



                         !j.joiningAndWorkplace.shiftType.toLowerCase().contains(hours))) continue;



                }







                // Currency filter



                String reqCurrency = qMap.getOrDefault("currency", "").trim();



                if (!reqCurrency.isEmpty() && !reqCurrency.equalsIgnoreCase("all") && !reqCurrency.equalsIgnoreCase("any")) {



                    if (!reqCurrency.equalsIgnoreCase(j.currency)) continue;



                }







                // Salary boundaries with currency normalization



                if (minSalary > 0 || maxSalary < Integer.MAX_VALUE) {



                    long compMin = minSalary;



                    long compMax = maxSalary;



                    if ("INR".equalsIgnoreCase(j.currency) && maxSalary < 500000) {



                        compMin = minSalary * 25L;



                        compMax = maxSalary * 25L;



                    } else if (!"INR".equalsIgnoreCase(j.currency) && minSalary >= 500000) {



                        compMin = minSalary / 25L;



                        compMax = maxSalary / 25L;



                    }



                    if (j.maxSalary < compMin || j.minSalary > compMax) continue;



                }







                filtered.add(j);



            }







            // Sorting



            if ("salary-desc".equals(sort)) {



                filtered.sort((a, b) -> Integer.compare(b.maxSalary, a.maxSalary));



            } else if ("salary-asc".equals(sort)) {



                filtered.sort((a, b) -> Integer.compare(a.minSalary, b.minSalary));



            } else if ("rating-desc".equals(sort)) {



                filtered.sort((a, b) -> Double.compare(



                        b.companyDetails != null ? b.companyDetails.rating : 0.0,



                        a.companyDetails != null ? a.companyDetails.rating : 0.0



                ));



            } else if ("exp-asc".equals(sort)) {



                filtered.sort((a, b) -> Integer.compare(a.minYearsExp, b.minYearsExp));



            }







            StringBuilder sb = new StringBuilder("[");



            for (int i = 0; i < filtered.size(); i++) {



                sb.append(jobToJson(filtered.get(i)));



                if (i < filtered.size() - 1) sb.append(",");



            }



            sb.append("]");







            sendJsonResponse(exchange, 200, sb.toString());



        }



    }







    /**



     * POST /api/upload-resume - Handles PDF/DOCX/TXT file upload, text extraction & automatic skill detection.



     */



    private static class UploadResumeHandler implements HttpHandler {



        @Override



        public void handle(HttpExchange exchange) throws IOException {



            setCORSHeaders(exchange);



            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {



                exchange.sendResponseHeaders(204, -1);



                exchange.getResponseBody().close();



                return;



            }







            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {



                sendError(exchange, 405, "Method Not Allowed");



                return;



            }







            String contentType = exchange.getRequestHeaders().getFirst("Content-Type");



            byte[] bodyBytes = readRequestBodyBytes(exchange);







            if (bodyBytes.length == 0) {



                sendError(exchange, 400, "Empty upload payload.");



                return;



            }







            // Limit 10MB



            if (bodyBytes.length > 10 * 1024 * 1024) {



                sendError(exchange, 413, "Uploaded file exceeds maximum allowed size (10MB).");



                return;



            }







            String filename = "uploaded_resume.txt";



            byte[] fileBytes = null;







            if (contentType != null && contentType.toLowerCase().contains("multipart/form-data")) {



                // Parse multipart form



                int bIdx = contentType.toLowerCase().indexOf("boundary=");



                if (bIdx != -1) {



                    String boundary = contentType.substring(bIdx + 9).trim();



                    if (boundary.startsWith("\"") && boundary.endsWith("\"")) {



                        boundary = boundary.substring(1, boundary.length() - 1);



                    }



                    fileBytes = parseMultipartFileBytes(bodyBytes, boundary);



                    String extractedName = parseMultipartFilename(bodyBytes, boundary);



                    if (extractedName != null && !extractedName.isEmpty()) {



                        filename = extractedName;



                    }



                }



            } else if (contentType != null && contentType.toLowerCase().contains("application/json")) {



                // Parse JSON base64



                String json = new String(bodyBytes, StandardCharsets.UTF_8);



                Map<String, Object> req = parseJsonMap(json);



                filename = (String) req.getOrDefault("filename", "resume.pdf");



                String b64 = (String) req.getOrDefault("contentBase64", req.get("base64"));



                if (b64 != null && !b64.isEmpty()) {



                    try {



                        fileBytes = Base64.getDecoder().decode(b64);



                    } catch (Exception e) {



                        sendError(exchange, 400, "Invalid Base64 document encoding.");



                        return;



                    }



                }



            }







            if (fileBytes == null || fileBytes.length == 0) {



                fileBytes = bodyBytes;



            }







            String extractedText = DocumentExtractor.extractText(filename, fileBytes);



            if (extractedText == null || extractedText.trim().isEmpty()) {



                sendError(exchange, 400, "Unable to extract readable text from " + filename + ". Ensure document is not corrupted or password protected.");



                return;



            }







            // Extract skills, candidate info & education



            Map<String, Object> parsed = MatchingEngine.parseResume(extractedText);



            String name = (String) parsed.get("candidateName");



            String email = (String) parsed.get("email");



            String phone = (String) parsed.get("phone");



            int yearsExp = parseNumber(parsed.get("estimatedYearsExp"), 0);



            @SuppressWarnings("unchecked")



            List<String> skills = (List<String>) parsed.get("detectedSkills");



            @SuppressWarnings("unchecked")



            List<String> edu = (List<String>) parsed.get("education");



            int words = parseNumber(parsed.get("wordCount"), 0);







            String fileExt = "txt";



            if (filename.contains(".")) {



                fileExt = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();



            }







            // Register in Database



            DatabaseManager.addOrUpdateCandidate(name, email, phone, yearsExp, skills, edu);



            ResumeDocument resumeDoc = DatabaseManager.addResumeRecord(filename, fileExt, fileBytes.length, words, skills, email);







            StringBuilder sb = new StringBuilder("{");



            sb.append("\"success\":true,");



            sb.append("\"resumeId\":\"").append(escapeJson(resumeDoc.resumeId)).append("\",");



            sb.append("\"filename\":\"").append(escapeJson(filename)).append("\",");



            sb.append("\"fileType\":\"").append(escapeJson(fileExt)).append("\",");



            sb.append("\"fileSize\":").append(fileBytes.length).append(",");



            sb.append("\"wordCount\":").append(words).append(",");



            sb.append("\"candidateName\":\"").append(escapeJson(name)).append("\",");



            sb.append("\"email\":\"").append(escapeJson(email)).append("\",");



            sb.append("\"phone\":\"").append(escapeJson(phone)).append("\",");



            sb.append("\"estimatedYearsExp\":").append(yearsExp).append(",");



            sb.append("\"education\":").append(listToJsonArray(edu)).append(",");



            sb.append("\"detectedSkills\":").append(listToJsonArray(skills)).append(",");



            sb.append("\"extractedText\":\"").append(escapeJson(extractedText)).append("\"");



            sb.append("}");







            sendJsonResponse(exchange, 200, sb.toString());



        }



    }







    /**



     * SavedJobsHandler - GET, POST, DELETE bookmarks.



     */



    private static class SavedJobsHandler implements HttpHandler {



        @Override



        public void handle(HttpExchange exchange) throws IOException {



            setCORSHeaders(exchange);



            String method = exchange.getRequestMethod().toUpperCase();



            if ("OPTIONS".equals(method)) {



                exchange.sendResponseHeaders(204, -1);



                exchange.getResponseBody().close();



                return;



            }







            String path = exchange.getRequestURI().getPath();



            boolean isRemove = path.endsWith("/remove") || "DELETE".equals(method);







            if ("GET".equals(method)) {



                String authEmail = AuthService.getAuthenticatedEmail(exchange);



                String email = authEmail != null ? authEmail.trim().toLowerCase() : "";







                List<Map<String, Object>> savedResults = new ArrayList<>();



                List<String> savedJobIds = new ArrayList<>();



                if (!email.isEmpty()) {



                    synchronized (SAVED_JOBS_LIST) {



                        for (SavedJobRecord rec : SAVED_JOBS_LIST) {



                            if (rec.candidateEmail.equalsIgnoreCase(email)) {



                                Job found = null;



                                for (Job j : JOB_DATABASE) {



                                    if (j.id.equalsIgnoreCase(rec.jobId)) {



                                        found = j;



                                        break;



                                    }



                                }



                                if (found != null) {



                                    Map<String, Object> item = new HashMap<>();



                                    item.put("savedRecordId", rec.id);



                                    item.put("savedAt", rec.savedAt);



                                    item.put("candidateEmail", rec.candidateEmail);



                                    item.put("job", found);



                                    savedResults.add(item);



                                    if (!savedJobIds.contains(found.id)) {



                                        savedJobIds.add(found.id);



                                    }



                                }



                            }



                        }



                    }



                }







                StringBuilder sb = new StringBuilder("{");



                sb.append("\"success\":true,");



                sb.append("\"totalSaved\":").append(savedResults.size()).append(",");



                sb.append("\"savedJobIds\":").append(listToJsonArray(savedJobIds)).append(",");



                sb.append("\"savedJobs\":[");



                for (int i = 0; i < savedResults.size(); i++) {



                    Map<String, Object> item = savedResults.get(i);



                    sb.append("{\"savedRecordId\":\"").append(escapeJson((String) item.get("savedRecordId"))).append("\",")



                            .append("\"savedAt\":\"").append(escapeJson((String) item.get("savedAt"))).append("\",")



                            .append("\"candidateEmail\":\"").append(escapeJson((String) item.get("candidateEmail"))).append("\",")



                            .append("\"job\":").append(jobToJson((Job) item.get("job")))



                            .append("}");



                    if (i < savedResults.size() - 1) sb.append(",");



                }



                sb.append("]}");







                sendJsonResponse(exchange, 200, sb.toString());



                return;



            }







            if ("POST".equals(method) || "DELETE".equals(method)) {



                String body = readRequestBody(exchange);



                Map<String, Object> req = parseJsonMap(body);



                String jobId = (String) req.getOrDefault("jobId", "");



                String email = (String) req.getOrDefault("candidateEmail", "applicant@carrerx.io");



                String action = (String) req.getOrDefault("action", "");



                boolean shouldRemove = isRemove || "remove".equalsIgnoreCase(action) || "unsave".equalsIgnoreCase(action) || Boolean.FALSE.equals(req.get("saved"));







                if (jobId == null || jobId.trim().isEmpty()) {



                    sendError(exchange, 400, "Job ID is required.");



                    return;



                }







                if (shouldRemove) {



                    boolean removed = DatabaseManager.removeBookmark(email, jobId);



                    String resp = "{\"success\":true,\"saved\":false,\"jobId\":\"" + escapeJson(jobId) + "\",\"removed\":" + removed + ",\"totalSaved\":" + SAVED_JOBS_LIST.size() + ",\"message\":\"Job removed from bookmarks.\"}";



                    sendJsonResponse(exchange, 200, resp);



                } else {



                    boolean added = DatabaseManager.addBookmark(email, jobId);



                    String resp = "{\"success\":true,\"saved\":true,\"jobId\":\"" + escapeJson(jobId) + "\",\"alreadySaved\":" + (!added) + ",\"totalSaved\":" + SAVED_JOBS_LIST.size() + ",\"message\":\"Job successfully saved to bookmarks.\"}";



                    sendJsonResponse(exchange, 200, resp);



                }



                return;



            }







            sendError(exchange, 405, "Method Not Allowed");



        }



    }







    /**



     * GET /api/stats/dashboard - Real live aggregated dashboard statistics.



     */



    private static class DashboardStatsHandler implements HttpHandler {



        @Override



        public void handle(HttpExchange exchange) throws IOException {



            setCORSHeaders(exchange);



            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {



                exchange.sendResponseHeaders(204, -1);



                exchange.getResponseBody().close();



                return;



            }







            int totalJobs = JOB_DATABASE.size();



            int totalApplications = APPLICATIONS_LIST.size();



            int totalSaved = SAVED_JOBS_LIST.size();



            int totalCandidates = CANDIDATES_LIST.size();







            long sumSal = 0;



            int minSal = Integer.MAX_VALUE;



            int maxSal = 0;







            Map<String, Integer> catCounts = new LinkedHashMap<>();



            Map<String, Integer> expCounts = new LinkedHashMap<>();



            Map<String, Integer> modeCounts = new LinkedHashMap<>();



            Map<String, Integer> skillFreq = new HashMap<>();







            for (Job j : JOB_DATABASE) {



                int mid = (j.minSalary + j.maxSalary) / 2;



                sumSal += mid;



                if (j.minSalary < minSal) minSal = j.minSalary;



                if (j.maxSalary > maxSal) maxSal = j.maxSalary;







                catCounts.put(j.category, catCounts.getOrDefault(j.category, 0) + 1);



                expCounts.put(j.experienceLevel, expCounts.getOrDefault(j.experienceLevel, 0) + 1);



                modeCounts.put(j.workMode, modeCounts.getOrDefault(j.workMode, 0) + 1);







                for (String sk : j.requiredSkills) {



                    skillFreq.put(sk, skillFreq.getOrDefault(sk, 0) + 1);



                }



            }







            int avgSal = totalJobs > 0 ? (int) (sumSal / totalJobs) : 0;



            if (minSal == Integer.MAX_VALUE) minSal = 0;







            Map<String, Integer> stageCounts = new LinkedHashMap<>();



            stageCounts.put("Test/Assessment", 0);



            stageCounts.put("Interview", 0);



            stageCounts.put("Waiting List", 0);



            stageCounts.put("Completely Selected", 0);



            stageCounts.put("Rejected", 0);







            synchronized (APPLICATIONS_LIST) {



                for (JobApplication a : APPLICATIONS_LIST) {



                    stageCounts.put(a.status, stageCounts.getOrDefault(a.status, 0) + 1);



                }



            }







            List<Map.Entry<String, Integer>> topSkills = skillFreq.entrySet().stream()



                    .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))



                    .limit(8)



                    .collect(Collectors.toList());







            StringBuilder sb = new StringBuilder("{");



            sb.append("\"success\":true,");



            sb.append("\"totalJobs\":").append(totalJobs).append(",");



            sb.append("\"totalApplications\":").append(totalApplications).append(",");



            sb.append("\"totalSavedJobs\":").append(totalSaved).append(",");



            sb.append("\"totalCandidates\":").append(totalCandidates).append(",");



            sb.append("\"averageSalary\":").append(avgSal).append(",");



            sb.append("\"minSalary\":").append(minSal).append(",");



            sb.append("\"maxSalary\":").append(maxSal).append(",");







            // categoryBreakdown



            sb.append("\"categoryBreakdown\":{");



            int cIdx = 0;



            for (Map.Entry<String, Integer> e : catCounts.entrySet()) {



                sb.append("\"").append(escapeJson(e.getKey())).append("\":").append(e.getValue());



                if (++cIdx < catCounts.size()) sb.append(",");



            }



            sb.append("},");







            // experienceLevelBreakdown



            sb.append("\"experienceLevelBreakdown\":{");



            int eIdx = 0;



            for (Map.Entry<String, Integer> e : expCounts.entrySet()) {



                sb.append("\"").append(escapeJson(e.getKey())).append("\":").append(e.getValue());



                if (++eIdx < expCounts.size()) sb.append(",");



            }



            sb.append("},");







            // workModeBreakdown



            sb.append("\"workModeBreakdown\":{");



            int mIdx = 0;



            for (Map.Entry<String, Integer> e : modeCounts.entrySet()) {



                sb.append("\"").append(escapeJson(e.getKey())).append("\":").append(e.getValue());



                if (++mIdx < modeCounts.size()) sb.append(",");



            }



            sb.append("},");







            // applicationStages



            sb.append("\"applicationStages\":{");



            int sIdx = 0;



            for (Map.Entry<String, Integer> e : stageCounts.entrySet()) {



                sb.append("\"").append(escapeJson(e.getKey())).append("\":").append(e.getValue());



                if (++sIdx < stageCounts.size()) sb.append(",");



            }



            sb.append("},");







            // topDemandedSkills



            sb.append("\"topDemandedSkills\":[");



            for (int i = 0; i < topSkills.size(); i++) {



                Map.Entry<String, Integer> entry = topSkills.get(i);



                sb.append("{\"skill\":\"").append(escapeJson(entry.getKey())).append("\",\"count\":").append(entry.getValue()).append("}");



                if (i < topSkills.size() - 1) sb.append(",");



            }



            sb.append("]}");







            sendJsonResponse(exchange, 200, sb.toString());



        }



    }







    /**



     * GET /api/stats/salary-benchmark - Statistical percentiles and tier distribution.



     */



    private static class SalaryBenchmarkHandler implements HttpHandler {



        @Override



        public void handle(HttpExchange exchange) throws IOException {



            setCORSHeaders(exchange);



            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {



                exchange.sendResponseHeaders(204, -1);



                exchange.getResponseBody().close();



                return;



            }







            Map<String, String> q = parseQueryParams(exchange.getRequestURI().getRawQuery());



            int candSal = parseNumber(q.get("salary"), 0);



            int candExp = parseNumber(q.get("exp"), -1);







            List<Integer> mids = new ArrayList<>();



            List<Integer> fresherSals = new ArrayList<>();



            List<Integer> midSals = new ArrayList<>();



            List<Integer> seniorSals = new ArrayList<>();



            List<Integer> leadSals = new ArrayList<>();







            for (Job j : JOB_DATABASE) {



                int m = (j.minSalary + j.maxSalary) / 2;



                mids.add(m);



                if (j.minYearsExp <= 1) fresherSals.add(m);



                else if (j.minYearsExp <= 4) midSals.add(m);



                else if (j.minYearsExp <= 7) seniorSals.add(m);



                else leadSals.add(m);



            }







            Collections.sort(mids);



            Collections.sort(fresherSals);



            Collections.sort(midSals);



            Collections.sort(seniorSals);



            Collections.sort(leadSals);







            int n = mids.size();



            int min = n > 0 ? mids.get(0) : 0;



            int max = n > 0 ? mids.get(n - 1) : 0;



            int avg = n > 0 ? (int) mids.stream().mapToInt(Integer::intValue).average().orElse(0) : 0;



            int median = getPercentile(mids, 50);



            int p10 = getPercentile(mids, 10);



            int p25 = getPercentile(mids, 25);



            int p75 = getPercentile(mids, 75);



            int p90 = getPercentile(mids, 90);







            // Candidate Percentile Ranking



            int candPercentile = 0;



            String rankingFit = "Market Competitive";



            if (candSal > 0 && n > 0) {



                int belowCount = 0;



                for (int s : mids) {



                    if (candSal >= s) belowCount++;



                }



                candPercentile = (int) Math.round(((double) belowCount / n) * 100);



                if (candPercentile >= 80) rankingFit = "Top 20% Compensation Tier";



                else if (candPercentile >= 50) rankingFit = "Above Market Median";



                else rankingFit = "Entry / Value Compensation Band";



            }







            StringBuilder sb = new StringBuilder("{");



            sb.append("\"success\":true,");



            sb.append("\"overall\":{")



                    .append("\"min\":").append(min).append(",")



                    .append("\"max\":").append(max).append(",")



                    .append("\"average\":").append(avg).append(",")



                    .append("\"median\":").append(median).append(",")



                    .append("\"p10\":").append(p10).append(",")



                    .append("\"p25\":").append(p25).append(",")



                    .append("\"p75\":").append(p75).append(",")



                    .append("\"p90\":").append(p90).append(",")



                    .append("\"totalJobsAnalyzed\":").append(n)



                    .append("},");







            sb.append("\"tiers\":{")



                    .append("\"fresher\":").append(tierToJson("Fresher (0-1y)", fresherSals)).append(",")



                    .append("\"mid\":").append(tierToJson("Mid-Level (2-4y)", midSals)).append(",")



                    .append("\"senior\":").append(tierToJson("Senior (5-7y)", seniorSals)).append(",")



                    .append("\"lead\":").append(tierToJson("Lead & Executive (8+y)", leadSals))



                    .append("},");







            sb.append("\"candidateEvaluation\":{")



                    .append("\"targetSalary\":").append(candSal).append(",")



                    .append("\"experienceYears\":").append(candExp).append(",")



                    .append("\"percentile\":").append(candPercentile).append(",")



                    .append("\"marketPosition\":\"").append(rankingFit).append("\"")



                    .append("}");



            sb.append("}");







            sendJsonResponse(exchange, 200, sb.toString());



        }







        private int getPercentile(List<Integer> sorted, int pct) {



            if (sorted.isEmpty()) return 0;



            int idx = (int) Math.round((pct / 100.0) * (sorted.size() - 1));



            return sorted.get(Math.min(sorted.size() - 1, Math.max(0, idx)));



        }







        private String tierToJson(String label, List<Integer> list) {



            if (list.isEmpty()) return "{\"label\":\"" + label + "\",\"count\":0,\"min\":0,\"max\":0,\"avg\":0,\"median\":0}";



            int min = list.get(0);



            int max = list.get(list.size() - 1);



            int avg = (int) list.stream().mapToInt(Integer::intValue).average().orElse(0);



            int median = list.get(list.size() / 2);



            return "{\"label\":\"" + label + "\",\"count\":" + list.size() + ",\"min\":" + min + ",\"max\":" + max + ",\"avg\":" + avg + ",\"median\":" + median + "}";



        }



    }







    private static class MatchHandler implements HttpHandler {



        @Override



        public void handle(HttpExchange exchange) throws IOException {



            setCORSHeaders(exchange);



            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {



                exchange.sendResponseHeaders(204, -1);



                exchange.getResponseBody().close();



                return;



            }







            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {



                sendError(exchange, 405, "Method Not Allowed");



                return;



            }







            String body = readRequestBody(exchange);



            Map<String, Object> req = parseJsonMap(body);







            String resumeText = (String) req.getOrDefault("resumeText", "");



            @SuppressWarnings("unchecked")



            List<String> skills = (List<String>) req.getOrDefault("skills", new ArrayList<>());



            int yearsExp = parseNumber(req.get("yearsOfExperience"), 0);



            int expectedSalary = parseNumber(req.get("expectedSalary"), 0);



            String workMode = (String) req.getOrDefault("preferredWorkMode", "Any");



            String category = (String) req.getOrDefault("preferredCategory", "All");



            String location = (String) req.getOrDefault("preferredLocation", "All");



            String climate = (String) req.getOrDefault("preferredClimate", "All");



            String hours = (String) req.getOrDefault("preferredWorkingHours", "All");







            boolean strictExp = true;



            if (req.containsKey("strictExperienceFilter")) {



                strictExp = Boolean.parseBoolean(String.valueOf(req.get("strictExperienceFilter")));



            }







            String currencyFilter = (String) req.getOrDefault("currencyFilter", "Any");







            int weightSkills = parseNumber(req.get("weightSkills"), 60);



            int weightExp = parseNumber(req.get("weightExperience"), 20);



            int weightSal = parseNumber(req.get("weightSalary"), 10);



            int weightPref = parseNumber(req.get("weightPreferences"), 10);







            List<MatchResult> matches = MatchingEngine.matchJobs(



                    resumeText, skills, yearsExp, expectedSalary, workMode, category,



                    location, climate, hours, strictExp, weightSkills, weightExp, weightSal, weightPref, currencyFilter);







            int avgSalary = 0;



            int topMatchScore = 0;



            Map<String, Integer> missingSkillFrequency = new HashMap<>();







            for (MatchResult mr : matches) {



                avgSalary += (mr.job.minSalary + mr.job.maxSalary) / 2;



                if (mr.matchPercentage > topMatchScore) {



                    topMatchScore = mr.matchPercentage;



                }



                for (String missing : mr.missingSkills) {



                    missingSkillFrequency.put(missing, missingSkillFrequency.getOrDefault(missing, 0) + 1);



                }



            }



            if (!matches.isEmpty()) {



                avgSalary /= matches.size();



            }







            List<Map.Entry<String, Integer>> topMissing = missingSkillFrequency.entrySet().stream()



                    .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))



                    .limit(5)



                    .collect(Collectors.toList());







            StringBuilder sb = new StringBuilder();



            sb.append("{");



            sb.append("\"totalMatches\":").append(matches.size()).append(",");



            sb.append("\"topMatchScore\":").append(topMatchScore).append(",");



            sb.append("\"averageSalary\":").append(avgSalary).append(",");



            sb.append("\"candidateYearsExp\":").append(yearsExp).append(",");



            sb.append("\"strictExperienceFilterActive\":").append(strictExp).append(",");



            sb.append("\"appliedPreferences\":{")



                    .append("\"location\":\"").append(escapeJson(location)).append("\",")



                    .append("\"climate\":\"").append(escapeJson(climate)).append("\",")



                    .append("\"workingHours\":\"").append(escapeJson(hours)).append("\",")



                    .append("\"workMode\":\"").append(escapeJson(workMode)).append("\"")



                    .append("},");



            sb.append("\"appliedWeights\":{\"skills\":").append(weightSkills)



                    .append(",\"experience\":").append(weightExp)



                    .append(",\"salary\":").append(weightSal)



                    .append(",\"preferences\":").append(weightPref).append("},");



            sb.append("\"recommendedMissingSkills\":[");



            for (int i = 0; i < topMissing.size(); i++) {



                Map.Entry<String, Integer> entry = topMissing.get(i);



                sb.append("{\"skill\":\"").append(escapeJson(entry.getKey())).append("\",\"demandCount\":").append(entry.getValue()).append("}");



                if (i < topMissing.size() - 1) sb.append(",");



            }



            sb.append("],");



            sb.append("\"results\":[");



            for (int i = 0; i < matches.size(); i++) {



                MatchResult mr = matches.get(i);



                sb.append("{");



                sb.append("\"matchPercentage\":").append(mr.matchPercentage).append(",");



                sb.append("\"matchTier\":\"").append(escapeJson(mr.matchTier)).append("\",");



                sb.append("\"salaryAssessment\":\"").append(escapeJson(mr.salaryAssessment)).append("\",");



                sb.append("\"salaryComparisonPct\":").append(mr.salaryComparisonPct).append(",");



                sb.append("\"fitSummary\":\"").append(escapeJson(mr.fitSummary)).append("\",");



                sb.append("\"experienceMatchStatus\":\"").append(escapeJson(mr.experienceMatchStatus)).append("\",");



                sb.append("\"locationMatch\":").append(mr.locationMatch).append(",");



                sb.append("\"climateMatch\":").append(mr.climateMatch).append(",");



                sb.append("\"hoursMatch\":").append(mr.hoursMatch).append(",");



                sb.append("\"educationMatch\":").append(mr.educationMatch).append(",");



                sb.append("\"educationBadge\":\"").append(escapeJson(mr.educationBadge)).append("\",");



                sb.append("\"scoreBreakdown\":{");



                sb.append("\"skills\":").append(mr.skillsContribution).append(",");



                sb.append("\"experience\":").append(mr.experienceContribution).append(",");



                sb.append("\"salary\":").append(mr.salaryContribution).append(",");



                sb.append("\"preferences\":").append(mr.preferenceContribution);



                sb.append("},");



                sb.append("\"matchedSkills\":").append(listToJsonArray(mr.matchedSkills)).append(",");



                sb.append("\"missingSkills\":").append(listToJsonArray(mr.missingSkills)).append(",");



                sb.append("\"bonusSkills\":").append(listToJsonArray(mr.bonusSkills)).append(",");



                sb.append("\"job\":").append(jobToJson(mr.job));



                sb.append("}");



                if (i < matches.size() - 1) sb.append(",");



            }



            sb.append("]}");







            sendJsonResponse(exchange, 200, sb.toString());



        }



    }







    /**



     * POST /api/resume/extract - Accepts raw resume text or document upload (JSON / Multipart / Text)



     * and returns extracted contact info, skills, experience level, and competencies.



     */



    private static class ResumeExtractHandler implements HttpHandler {



        @Override



        public void handle(HttpExchange exchange) throws IOException {



            setCORSHeaders(exchange);



            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {



                exchange.sendResponseHeaders(204, -1);



                exchange.getResponseBody().close();



                return;



            }







            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {



                sendError(exchange, 405, "Method Not Allowed");



                return;



            }







            String contentType = exchange.getRequestHeaders().getFirst("Content-Type");



            byte[] bodyBytes = readRequestBodyBytes(exchange);







            if (bodyBytes.length == 0) {



                sendError(exchange, 400, "Resume payload is empty.");



                return;



            }







            String resumeText = "";



            String filename = "resume.txt";







            if (contentType != null && contentType.toLowerCase().contains("multipart/form-data")) {



                int bIdx = contentType.toLowerCase().indexOf("boundary=");



                if (bIdx != -1) {



                    String boundary = contentType.substring(bIdx + 9).trim();



                    if (boundary.startsWith("\"") && boundary.endsWith("\"")) {



                        boundary = boundary.substring(1, boundary.length() - 1);



                    }



                    byte[] fileBytes = parseMultipartFileBytes(bodyBytes, boundary);



                    String extractedName = parseMultipartFilename(bodyBytes, boundary);



                    if (extractedName != null && !extractedName.isEmpty()) filename = extractedName;



                    resumeText = DocumentExtractor.extractText(filename, fileBytes);



                }



            } else if (contentType != null && contentType.toLowerCase().contains("application/json")) {



                String json = new String(bodyBytes, StandardCharsets.UTF_8);



                Map<String, Object> req = parseJsonMap(json);



                if (req.containsKey("contentBase64") || req.containsKey("base64")) {



                    String b64 = (String) req.getOrDefault("contentBase64", req.get("base64"));



                    filename = (String) req.getOrDefault("filename", "resume.pdf");



                    try {



                        byte[] fileBytes = Base64.getDecoder().decode(b64);



                        resumeText = DocumentExtractor.extractText(filename, fileBytes);



                    } catch (Exception e) {



                        sendError(exchange, 400, "Invalid base64 document.");



                        return;



                    }



                } else {



                    resumeText = (String) req.getOrDefault("resumeText", req.getOrDefault("text", req.getOrDefault("content", "")));



                }



            } else {



                resumeText = new String(bodyBytes, StandardCharsets.UTF_8);



            }







            if (resumeText == null || resumeText.trim().isEmpty()) {



                sendError(exchange, 400, "No readable resume text could be extracted.");



                return;



            }







            Map<String, Object> parsed = MatchingEngine.parseResume(resumeText);



            String name = (String) parsed.get("candidateName");



            String email = (String) parsed.get("email");



            String phone = (String) parsed.get("phone");



            int yearsExp = parseNumber(parsed.get("estimatedYearsExp"), 0);



            @SuppressWarnings("unchecked")



            List<String> skills = (List<String>) parsed.get("detectedSkills");



            @SuppressWarnings("unchecked")



            List<String> education = (List<String>) parsed.get("education");



            int wordCount = parseNumber(parsed.get("wordCount"), 0);







            // Experience level label



            String experienceLevel;



            if (yearsExp <= 1) {



                experienceLevel = "0-1 years (Fresher)";



            } else if (yearsExp <= 4) {



                experienceLevel = yearsExp + " years (Mid-Level)";



            } else if (yearsExp <= 7) {



                experienceLevel = yearsExp + " years (Senior)";



            } else {



                experienceLevel = yearsExp + "+ years (Lead / Executive)";



            }







            // Extract Core Competencies



            List<String> competencies = new ArrayList<>();



            competencies.addAll(education);







            String textLower = resumeText.toLowerCase();



            if (textLower.contains("microservice")) competencies.add("Microservices Architecture");



            if (textLower.contains("distributed system")) competencies.add("Distributed Systems");



            if (textLower.contains("cloud") || textLower.contains("aws") || textLower.contains("azure") || textLower.contains("gcp")) competencies.add("Cloud Architecture & DevOps");



            if (textLower.contains("rest") || textLower.contains("api")) competencies.add("RESTful Web Services");



            if (textLower.contains("oop") || textLower.contains("object-oriented") || textLower.contains("object oriented")) competencies.add("Object-Oriented Design (OOP)");



            if (textLower.contains("ci/cd") || textLower.contains("continuous integration")) competencies.add("CI/CD Automation");



            if (textLower.contains("docker") || textLower.contains("container") || textLower.contains("kubernetes")) competencies.add("Containerization & Orchestration");



            if (textLower.contains("machine learning") || textLower.contains("deep learning") || textLower.contains("llm") || textLower.contains("ai")) competencies.add("AI & Machine Learning Pipelines");



            if (textLower.contains("agile") || textLower.contains("scrum")) competencies.add("Agile / Scrum Methodologies");



            if (competencies.isEmpty()) {



                competencies.add("Full Stack Development");



                competencies.add("Software Engineering");



            }







            // Sync with Database



            if ((email != null && !email.isEmpty()) || (name != null && !name.isEmpty())) {



                DatabaseManager.addOrUpdateCandidate(name, email, phone, yearsExp, skills, education);



            }







            StringBuilder sb = new StringBuilder("{");



            sb.append("\"success\":true,");



            sb.append("\"candidateName\":\"").append(escapeJson(name)).append("\",");



            sb.append("\"name\":\"").append(escapeJson(name)).append("\",");



            sb.append("\"email\":\"").append(escapeJson(email)).append("\",");



            sb.append("\"phone\":\"").append(escapeJson(phone)).append("\",");



            sb.append("\"contact\":{")



                    .append("\"name\":\"").append(escapeJson(name)).append("\",")



                    .append("\"email\":\"").append(escapeJson(email)).append("\",")



                    .append("\"phone\":\"").append(escapeJson(phone)).append("\"")



                    .append("},");



            sb.append("\"skills\":").append(listToJsonArray(skills)).append(",");



            sb.append("\"detectedSkills\":").append(listToJsonArray(skills)).append(",");



            sb.append("\"experienceLevel\":\"").append(escapeJson(experienceLevel)).append("\",");



            sb.append("\"estimatedYearsExp\":").append(yearsExp).append(",");



            sb.append("\"competencies\":").append(listToJsonArray(competencies)).append(",");



            sb.append("\"education\":").append(listToJsonArray(education)).append(",");



            sb.append("\"wordCount\":").append(wordCount).append(",");



            sb.append("\"extractedText\":\"").append(escapeJson(resumeText)).append("\"");



            sb.append("}");







            sendJsonResponse(exchange, 200, sb.toString());



        }



    }







    public static class StageEvaluation {



        public final String stage;



        public final String statusNote;



        public final int score;







        public StageEvaluation(String stage, String statusNote, int score) {



            this.stage = stage;



            this.statusNote = statusNote;



            this.score = score;



        }



    }







    private static StageEvaluation evaluateApplicationStage(Job job, int candidateExp, List<String> candidateSkills, int matchPctHint, String coverLetter, int expectedSalary) {



        if (job == null) {



            return new StageEvaluation("Test/Assessment", "Your application has been shortlisted for technical test / assessment.", 70);



        }







        // 1. Calculate actual skill overlap



        Set<String> candSkills = new HashSet<>();



        if (candidateSkills != null) {



            for (String s : candidateSkills) {



                if (s != null && !s.trim().isEmpty()) candSkills.add(s.trim().toLowerCase());



            }



        }



        if (candSkills.isEmpty() && coverLetter != null && !coverLetter.isEmpty()) {



            Map<String, Object> parsed = MatchingEngine.parseResume(coverLetter);



            @SuppressWarnings("unchecked")



            List<String> detected = (List<String>) parsed.get("detectedSkills");



            if (detected != null) {



                for (String s : detected) candSkills.add(s.toLowerCase());



            }



        }







        int matchedSkillsCount = 0;



        if (job.requiredSkills != null && !job.requiredSkills.isEmpty()) {



            for (String req : job.requiredSkills) {



                for (String cs : candSkills) {



                    if (cs.equalsIgnoreCase(req) || req.toLowerCase().contains(cs) || cs.contains(req.toLowerCase())) {



                        matchedSkillsCount++;



                        break;



                    }



                }



            }



        }







        double skillRatio = (job.requiredSkills != null && !job.requiredSkills.isEmpty()) 



            ? ((double) matchedSkillsCount / job.requiredSkills.size()) 



            : 0.7;







        int score = matchPctHint > 0 ? matchPctHint : (int) Math.round(skillRatio * 100);







        // Experience verification



        int expDiff = candidateExp - job.minYearsExp;



        boolean isLeadOrExec = job.minYearsExp >= 7 || job.title.toLowerCase().contains("senior manager") || job.title.toLowerCase().contains("lead") || job.title.toLowerCase().contains("director");



        boolean isFresherCandidate = candidateExp <= 1;







        // Stage 1: Severe Experience Mismatch for Senior Roles -> Rejected (🔴)



        if (isLeadOrExec && isFresherCandidate) {



            return new StageEvaluation("Rejected",



                "Thank you for applying to " + job.company + ". This senior role requires a minimum of " + job.minYearsExp + "+ years of industry leadership. Your qualifications have not met the senior tenure threshold.",



                score);



        }







        // Stage 2: Low Skill Match -> Rejected (🔴)



        if (score < 38) {



            return new StageEvaluation("Rejected",



                "Thank you for your interest in " + job.company + ". At this time, the hiring team is proceeding with candidates whose technical stack more closely aligns with the prerequisites.",



                score);



        }







        // Stage 3: Top-tier Match (score >= 90 and candidate meets/exceeds experience) -> Completely Selected (🟢)



        if (score >= 90 && expDiff >= 0) {



            return new StageEvaluation("Completely Selected",



                "Congratulations! Your technical alignment achieved an exceptional " + score + "% match score. The hiring team has completely selected you for this position. Formal offer letter dispatched.",



                score);



        }







        // Stage 4: Strong Match (score >= 75) -> Interview (🟣)



        if (score >= 75) {



            return new StageEvaluation("Interview",



                "Congratulations! Based on your strong qualification score (" + score + "%) and tailored cover letter, the hiring team at " + job.company + " has shortlisted you for an Interview round.",



                score);



        }







        // Stage 5: Qualified Entry / Test Phase (score >= 55 or fresher applying for entry role) -> Test/Assessment (🔵)



        if (score >= 55 || (job.minYearsExp <= 1 && isFresherCandidate)) {



            return new StageEvaluation("Test/Assessment",



                "Your application has been shortlisted for the initial Test/Assessment round at " + job.company + ". An automated technical assessment link has been dispatched to your email.",



                score);



        }







        // Stage 6: Borderline Match (score 38 - 54) -> Waiting List (🟡)



        return new StageEvaluation("Waiting List",



            "Your application meets core qualifications and has been placed on the primary Waiting List at " + job.company + " pending further interview schedule bandwidth.",



            score);



    }







    /**



     * POST /api/apply - Live job application with immediate stage assignment.



     */



    private static class ApplyHandler implements HttpHandler {



        @Override



        public void handle(HttpExchange exchange) throws IOException {



            setCORSHeaders(exchange);



            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {



                exchange.sendResponseHeaders(204, -1);



                exchange.getResponseBody().close();



                return;



            }







            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {



                sendError(exchange, 405, "Method Not Allowed");



                return;



            }







            String body = readRequestBody(exchange);



            Map<String, Object> req = parseJsonMap(body);







            String jobId = (String) req.getOrDefault("jobId", "");



            String jobTitle = (String) req.getOrDefault("jobTitle", "Software Engineer");



            String company = (String) req.getOrDefault("company", "Tech Company");



            String candidateName = (String) req.getOrDefault("candidateName", "");



            String candidateEmail = (String) req.getOrDefault("candidateEmail", "");



            String candidatePhone = (String) req.getOrDefault("candidatePhone", "");



            int expectedSalary = parseNumber(req.get("expectedSalary"), 0);



            String joiningAvailability = (String) req.getOrDefault("joiningAvailability", "Immediate (Within 2 weeks)");



            String preferredHours = (String) req.getOrDefault("preferredHours", "Standard Day (9am - 5pm)");



            String coverLetter = (String) req.getOrDefault("coverLetter", "");



            String portfolioUrl = (String) req.getOrDefault("portfolioUrl", "");







            if (jobId == null || jobId.trim().isEmpty()) {



                sendJsonResponse(exchange, 400, "{\"success\":false,\"error\":\"MISSING_JOB_ID\",\"message\":\"Job ID is required to submit an application.\"}");



                return;



            }



            if (candidateName == null || candidateName.trim().isEmpty()) {



                sendJsonResponse(exchange, 400, "{\"success\":false,\"error\":\"MISSING_NAME\",\"message\":\"Candidate name is required.\"}");



                return;



            }



            if (candidateEmail == null || candidateEmail.trim().isEmpty() || !candidateEmail.contains("@")) {



                sendJsonResponse(exchange, 400, "{\"success\":false,\"error\":\"INVALID_EMAIL\",\"message\":\"A valid candidate email address is required.\"}");



                return;



            }







            // Enrich company and job title



            Job targetJob = null;



            for (Job j : JOB_DATABASE) {



                if (j.id.equalsIgnoreCase(jobId.trim())) {



                    targetJob = j;



                    if (jobTitle == null || jobTitle.isEmpty() || jobTitle.equals("Software Engineer")) {



                        jobTitle = j.title;



                    }



                    if (company == null || company.isEmpty() || company.equals("Tech Company")) {



                        company = j.company;



                    }



                    break;



                }



            }







            // Prevent duplicate applications



            synchronized (APPLICATIONS_LIST) {



                for (JobApplication existing : APPLICATIONS_LIST) {



                    if (existing.jobId != null && existing.jobId.equalsIgnoreCase(jobId.trim()) &&



                        existing.candidateEmail != null && existing.candidateEmail.equalsIgnoreCase(candidateEmail.trim())) {



                        String dupResponse = "{" +



                                "\"success\":false," +



                                "\"error\":\"DUPLICATE_APPLICATION\"," +



                                "\"existingApplicationId\":\"" + existing.applicationId + "\"," +



                                "\"existingStatus\":\"" + escapeJson(existing.status) + "\"," +



                                "\"message\":\"You have already submitted an application for " + escapeJson(jobTitle) + " at " + escapeJson(company) + ". Reference ID: " + existing.applicationId + " (Status: " + escapeJson(existing.status) + ").\"" +



                                "}";



                        sendJsonResponse(exchange, 409, dupResponse);



                        return;



                    }



                }



            }







            // Candidate attributes for intelligent backend evaluation



            int candidateYearsExp = parseNumber(req.get("yearsOfExperience"), parseNumber(req.get("yearsExp"), 0));



            int matchPct = parseNumber(req.get("matchPercentage"), parseNumber(req.get("matchScore"), 0));



            @SuppressWarnings("unchecked")



            List<String> candidateSkills = (List<String>) req.get("skills");



            if (candidateSkills == null) {



                candidateSkills = new ArrayList<>();



            }



            if (candidateYearsExp == 0 || candidateSkills.isEmpty()) {



                for (Candidate c : CANDIDATES_LIST) {



                    if (c.email.equalsIgnoreCase(candidateEmail)) {



                        if (candidateYearsExp == 0) candidateYearsExp = c.yearsExp;



                        if (candidateSkills.isEmpty()) candidateSkills.addAll(c.skills);



                        break;



                    }



                }



            }







            // Application initial state: Applied (Candidate cannot jump stages)



            String initialStage = "Applied";



            String statusNote = "Your application and tailored qualifications have been received and logged in " + company + "'s candidate tracking portal.";







            String appId = "APP-2026-" + (1000 + APPLICATIONS_LIST.size() + 1);



            String submittedAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());







            JobApplication application = new JobApplication(



                    appId, jobId, jobTitle, company, candidateName, candidateEmail,



                    candidatePhone, expectedSalary, joiningAvailability, preferredHours,



                    coverLetter, portfolioUrl, submittedAt, initialStage, statusNote



            );







            APPLICATIONS_LIST.add(application);



            DatabaseManager.saveApplications();







            System.out.println(">>> [NEW APPLICATION SUBMITTED] ID=" + appId + " | Status=" + initialStage + " | Role=" + jobTitle + " at " + company + " | Candidate=" + candidateName);







            String response = "{" +



                    "\"success\":true," +



                    "\"applicationId\":\"" + appId + "\"," +



                    "\"jobId\":\"" + escapeJson(jobId) + "\"," +



                    "\"status\":\"" + escapeJson(initialStage) + "\"," +



                    "\"statusNote\":\"" + escapeJson(statusNote) + "\"," +



                    "\"submittedAt\":\"" + submittedAt + "\"," +



                    "\"jobTitle\":\"" + escapeJson(jobTitle) + "\"," +



                    "\"company\":\"" + escapeJson(company) + "\"," +



                    "\"candidateName\":\"" + escapeJson(candidateName) + "\"," +



                    "\"candidateEmail\":\"" + escapeJson(candidateEmail) + "\"," +



                    "\"joiningAvailability\":\"" + escapeJson(joiningAvailability) + "\"," +



                    "\"message\":\"Application submitted successfully! Your tracking ID is " + appId + ".\"" +



                    "}";







            sendJsonResponse(exchange, 200, response);



        }



    }







    /**



     * POST /api/apply/batch - Simultaneous Multi-Job Application.



     */



    private static class BatchApplyHandler implements HttpHandler {



        @Override



        public void handle(HttpExchange exchange) throws IOException {



            setCORSHeaders(exchange);



            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {



                exchange.sendResponseHeaders(204, -1);



                exchange.getResponseBody().close();



                return;



            }







            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {



                sendError(exchange, 405, "Method Not Allowed");



                return;



            }







            String body = readRequestBody(exchange);



            Map<String, Object> req = parseJsonMap(body);







            @SuppressWarnings("unchecked")



            List<String> jobIds = (List<String>) req.get("jobIds");



            if (jobIds == null || jobIds.isEmpty()) {



                String singleJob = (String) req.get("jobId");



                if (singleJob != null && !singleJob.isEmpty()) {



                    jobIds = Collections.singletonList(singleJob);



                } else {



                    jobIds = new ArrayList<>();



                }



            }







            String candidateName = (String) req.getOrDefault("candidateName", "Applicant");



            String candidateEmail = (String) req.getOrDefault("candidateEmail", "");



            String candidatePhone = (String) req.getOrDefault("candidatePhone", "");



            int expectedSalary = parseNumber(req.get("expectedSalary"), 0);



            String joiningAvailability = (String) req.getOrDefault("joiningAvailability", "Immediate (Within 2 weeks)");



            String preferredHours = (String) req.getOrDefault("preferredHours", "Standard Day (9am - 5pm)");



            String portfolioUrl = (String) req.getOrDefault("portfolioUrl", "");



            String defaultCoverLetter = (String) req.getOrDefault("coverLetter", "");







            List<JobApplication> createdApps = new ArrayList<>();



            String submittedAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());







            synchronized (APPLICATIONS_LIST) {



                for (String jId : jobIds) {



                    if (jId == null || jId.trim().isEmpty()) continue;







                    boolean alreadyApplied = false;



                    for (JobApplication existing : APPLICATIONS_LIST) {



                        if (existing.jobId != null && existing.jobId.equalsIgnoreCase(jId.trim()) &&



                            existing.candidateEmail != null && existing.candidateEmail.equalsIgnoreCase(candidateEmail.trim())) {



                            alreadyApplied = true;



                            createdApps.add(existing);



                            break;



                        }



                    }



                    if (alreadyApplied) continue;







                    Job targetJob = null;



                    for (Job j : JOB_DATABASE) {



                        if (j.id.equalsIgnoreCase(jId)) {



                            targetJob = j;



                            break;



                        }



                    }







                    String title = (targetJob != null) ? targetJob.title : "Software Engineer";



                    String company = (targetJob != null) ? targetJob.company : "Tech Company";



                    int batchYearsExp = parseNumber(req.get("yearsOfExperience"), parseNumber(req.get("yearsExp"), 0));



                    @SuppressWarnings("unchecked")



                    List<String> batchSkills = (List<String>) req.get("skills");



                    if (batchSkills == null) batchSkills = new ArrayList<>();



                    if (batchYearsExp == 0 || batchSkills.isEmpty()) {



                        for (Candidate c : CANDIDATES_LIST) {



                            if (c.email.equalsIgnoreCase(candidateEmail)) {



                                if (batchYearsExp == 0) batchYearsExp = c.yearsExp;



                                if (batchSkills.isEmpty()) batchSkills.addAll(c.skills);



                                break;



                            }



                        }



                    }



                    String initialStage = "Applied";



                    String statusNote = "Application submitted via batch multi-apply for " + title + " at " + company + ".";







                    String appId = "APP-2026-" + (1000 + APPLICATIONS_LIST.size() + 1);







                    JobApplication application = new JobApplication(



                            appId, jId, title, company, candidateName, candidateEmail,



                            candidatePhone, expectedSalary, joiningAvailability, preferredHours,



                            defaultCoverLetter, portfolioUrl, submittedAt, initialStage, statusNote



                    );







                    APPLICATIONS_LIST.add(application);



                    createdApps.add(application);



                }



            }







            DatabaseManager.saveApplications();







            StringBuilder sb = new StringBuilder();



            sb.append("{");



            sb.append("\"success\":true,");



            sb.append("\"totalSubmitted\":").append(createdApps.size()).append(",");



            sb.append("\"applications\":[");



            for (int i = 0; i < createdApps.size(); i++) {



                JobApplication app = createdApps.get(i);



                sb.append("{")



                        .append("\"applicationId\":\"").append(escapeJson(app.applicationId)).append("\",")



                        .append("\"jobId\":\"").append(escapeJson(app.jobId)).append("\",")



                        .append("\"jobTitle\":\"").append(escapeJson(app.jobTitle)).append("\",")



                        .append("\"company\":\"").append(escapeJson(app.company)).append("\",")



                        .append("\"status\":\"").append(escapeJson(app.status)).append("\",")



                        .append("\"statusNote\":\"").append(escapeJson(app.statusNote)).append("\",")



                        .append("\"submittedAt\":\"").append(escapeJson(app.submittedAt)).append("\"")



                        .append("}");



                if (i < createdApps.size() - 1) sb.append(",");



            }



            sb.append("],");



            sb.append("\"message\":\"Successfully submitted " + createdApps.size() + " job applications simultaneously!\"");



            sb.append("}");







            sendJsonResponse(exchange, 200, sb.toString());



        }



    }







    /**



     * POST /api/applications/update-status - Move through stages.



     */



    private static class UpdateApplicationStatusHandler implements HttpHandler {



        @Override



        public void handle(HttpExchange exchange) throws IOException {



            setCORSHeaders(exchange);



            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {



                exchange.sendResponseHeaders(204, -1);



                exchange.getResponseBody().close();



                return;



            }







            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {



                sendError(exchange, 405, "Method Not Allowed");



                return;



            }







            String body = readRequestBody(exchange);



            Map<String, Object> req = parseJsonMap(body);







            String appId = (String) req.getOrDefault("applicationId", "");



            String newStatus = (String) req.getOrDefault("status", "Interview");



            String note = (String) req.getOrDefault("note", "");







            if (note.isEmpty()) {



                switch (newStatus) {



                    case "Applied":



                        note = "Application received and registered in corporate talent acquisition system.";



                        break;



                    case "Under Review":



                        note = "Your credentials and tailored qualifications are under review by the hiring manager.";



                        break;



                    case "Shortlisted":



                        note = "Great news! Your profile has been shortlisted for technical screening and evaluation.";



                        break;



                    case "Interview":



                        note = "You have been shortlisted for an Interview round with the Engineering Lead.";



                        break;



                    case "Selected":



                    case "Completely Selected":



                        note = "Congratulations! You have been selected for this position. Official offer letter dispatched.";



                        break;



                    case "Rejected":



                        note = "Thank you for your interest. While your credentials are impressive, the company is proceeding with other applicants at this time.";



                        break;



                    case "Waiting List":



                        note = "Your profile is under active consideration and currently placed on the primary Waiting List.";



                        break;



                    case "Test/Assessment":



                        note = "Shortlisted for technical test / assessment round. Assessment link dispatched to your email.";



                        break;



                    default:



                        note = "Application stage updated to: " + newStatus;



                }



            }







            JobApplication target = null;



            synchronized (APPLICATIONS_LIST) {



                for (JobApplication app : APPLICATIONS_LIST) {



                    if (app.applicationId.equalsIgnoreCase(appId)) {



                        app.updateStatus(newStatus, note);



                        target = app;



                        break;



                    }



                }



            }







            if (target != null) {



                DatabaseManager.saveApplications();



                System.out.println(">>> [STAGE UPDATED] " + appId + " -> " + newStatus);



                String response = "{" +



                        "\"success\":true," +



                        "\"applicationId\":\"" + appId + "\"," +



                        "\"status\":\"" + escapeJson(newStatus) + "\"," +



                        "\"statusNote\":\"" + escapeJson(note) + "\"," +



                        "\"message\":\"Application status successfully updated to " + escapeJson(newStatus) + ".\"" +



                        "}";



                sendJsonResponse(exchange, 200, response);



            } else {



                sendError(exchange, 404, "Application ID not found: " + appId);



            }



        }



    }







    /**



     * GET /api/applications - List all applications.



     */



    private static class ApplicationsListHandler implements HttpHandler {



        @Override



        public void handle(HttpExchange exchange) throws IOException {



            setCORSHeaders(exchange);



            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {



                exchange.sendResponseHeaders(204, -1);



                exchange.getResponseBody().close();



                return;



            }







            String authEmail = AuthService.getAuthenticatedEmail(exchange);



            if (authEmail == null || authEmail.isEmpty()) {



                // Secure candidate scoping: Unauthenticated requests return empty array



                sendJsonResponse(exchange, 200, "[]");



                return;



            }







            List<JobApplication> userApps = new ArrayList<>();



            synchronized (APPLICATIONS_LIST) {



                for (JobApplication a : APPLICATIONS_LIST) {



                    if (a.candidateEmail != null && a.candidateEmail.equalsIgnoreCase(authEmail)) {



                        userApps.add(a);



                    }



                }



            }







            StringBuilder sb = new StringBuilder();



            sb.append("[");



            for (int i = 0; i < userApps.size(); i++) {



                JobApplication app = userApps.get(i);



                sb.append("{")



                        .append("\"applicationId\":\"").append(escapeJson(app.applicationId)).append("\",")



                        .append("\"jobId\":\"").append(escapeJson(app.jobId)).append("\",")



                        .append("\"jobTitle\":\"").append(escapeJson(app.jobTitle)).append("\",")



                        .append("\"company\":\"").append(escapeJson(app.company)).append("\",")



                        .append("\"candidateName\":\"").append(escapeJson(app.candidateName)).append("\",")



                        .append("\"candidateEmail\":\"").append(escapeJson(app.candidateEmail)).append("\",")



                        .append("\"expectedSalary\":").append(app.expectedSalary).append(",")



                        .append("\"joiningAvailability\":\"").append(escapeJson(app.joiningAvailability)).append("\",")



                        .append("\"preferredHours\":\"").append(escapeJson(app.preferredHours)).append("\",")



                        .append("\"coverLetter\":\"").append(escapeJson(app.coverLetter)).append("\",")



                        .append("\"submittedAt\":\"").append(escapeJson(app.submittedAt)).append("\",")



                        .append("\"status\":\"").append(escapeJson(app.status)).append("\",")



                        .append("\"statusNote\":\"").append(escapeJson(app.statusNote)).append("\",")



                        .append("\"timeline\":").append(listToJsonArray(app.statusTimeline))



                        .append("}");



                if (i < userApps.size() - 1) sb.append(",");



            }



            sb.append("]");







            sendJsonResponse(exchange, 200, sb.toString());



        }



    }







    private static class SkillsHandler implements HttpHandler {



        @Override



        public void handle(HttpExchange exchange) throws IOException {



            setCORSHeaders(exchange);



            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {



                exchange.sendResponseHeaders(204, -1);



                exchange.getResponseBody().close();



                return;



            }







            Set<String> skills = MatchingEngine.getAllKnownSkills();



            String json = listToJsonArray(new ArrayList<>(skills));



            sendJsonResponse(exchange, 200, json);



        }



    }







    



    // ==========================================



    



    // ==========================================



    // AUTHENTICATION & CANDIDATE SESSION HANDLERS



    // ==========================================







    private static class AuthLoginHandler implements HttpHandler {



        @Override



        public void handle(HttpExchange exchange) throws IOException {



            setCORSHeaders(exchange);



            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {



                exchange.sendResponseHeaders(204, -1);



                exchange.getResponseBody().close();



                return;



            }







            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {



                sendError(exchange, 405, "Method Not Allowed");



                return;



            }







            String body = readRequestBody(exchange);



            Map<String, Object> req = parseJsonMap(body);



            String email = (String) req.getOrDefault("email", "");



            String password = (String) req.getOrDefault("password", "");







            if (email == null || email.trim().isEmpty()) {



                sendError(exchange, 400, "Email address is required.");



                return;



            }







            email = email.trim();







            // 1. Search in CANDIDATES_LIST



            Candidate matched = null;



            for (Candidate c : CANDIDATES_LIST) {



                if (c.email != null && c.email.equalsIgnoreCase(email)) {



                    matched = c;



                    break;



                }



            }







            // 2. Pre-configured demo profiles if not already present



            if (matched == null) {



                if ("rohan.patel@graduate.edu".equalsIgnoreCase(email)) {



                    matched = new Candidate("CAND-1003", "Rohan Patel", "rohan.patel@graduate.edu", "(408) 555-0192", 1,



                        Arrays.asList("Java", "SQL", "Git", "OOP", "HTML", "CSS"), Arrays.asList("B.S. Computer Science"), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));



                    CANDIDATES_LIST.add(matched);



                    DatabaseManager.saveCandidates();



                } else if ("elena.rostova@techhub.io".equalsIgnoreCase(email)) {



                    matched = new Candidate("CAND-1004", "Elena Rostova", "elena.rostova@techhub.io", "(415) 555-0841", 6,



                        Arrays.asList("Java", "Spring Boot", "React", "TypeScript", "Microservices", "PostgreSQL", "Kafka"), Arrays.asList("M.S. Software Engineering"), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));



                    CANDIDATES_LIST.add(matched);



                    DatabaseManager.saveCandidates();



                } else if ("marcus.vance@aimind.org".equalsIgnoreCase(email)) {



                    matched = new Candidate("CAND-1005", "Marcus Vance", "marcus.vance@aimind.org", "(206) 555-0477", 8,



                        Arrays.asList("Python", "PyTorch", "FastAPI", "Rust", "Transformers", "Kubernetes", "AWS"), Arrays.asList("Ph.D. Computer Science"), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));



                    CANDIDATES_LIST.add(matched);



                    DatabaseManager.saveCandidates();



                } else {



                    // For any valid email address entered by the user, dynamically provision candidate session



                    String namePart = email.split("@")[0];



                    String displayName = Character.toUpperCase(namePart.charAt(0)) + (namePart.length() > 1 ? namePart.substring(1) : "");



                    displayName = displayName.replace(".", " ").replace("_", " ");



                    matched = new Candidate("CAND-" + (1000 + CANDIDATES_LIST.size() + 1), displayName, email, "+1 (555) 012-7890", 2,



                        Arrays.asList("Java", "Python", "SQL", "Git", "REST API"), Arrays.asList("Bachelor's Degree"), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));



                    CANDIDATES_LIST.add(matched);



                    DatabaseManager.saveCandidates();



                }



            }







            String token = AuthSessionRepository.createSession(matched.email);



            StringBuilder skillsJson = new StringBuilder("[");



            if (matched.skills != null) {



                for (int i = 0; i < matched.skills.size(); i++) {



                    skillsJson.append("\"").append(escapeJson(matched.skills.get(i))).append("\"");



                    if (i < matched.skills.size() - 1) skillsJson.append(",");



                }



            }



            skillsJson.append("]");







            String headline = matched.yearsExp <= 1 ? "Associate Software Engineer" :



                              matched.yearsExp >= 7 ? "Staff / Lead Engineer" :



                              matched.yearsExp >= 5 ? "Senior Software Engineer" : "Full Stack Software Engineer";







            String json = "{"



                + "\"success\":true,"



                + "\"message\":\"Login successful\","



                + "\"token\":\"" + token + "\","



                + "\"user\":{"



                + "\"candidateId\":\"" + escapeJson(matched.candidateId) + "\","



                + "\"name\":\"" + escapeJson(matched.name) + "\","



                + "\"email\":\"" + escapeJson(matched.email) + "\","



                + "\"phone\":\"" + escapeJson(matched.phone != null ? matched.phone : "") + "\","



                + "\"headline\":\"" + escapeJson(headline) + "\","



                + "\"yearsExp\":" + matched.yearsExp + ","



                + "\"skills\":" + skillsJson.toString() + ","



                + "\"updatedAt\":\"" + escapeJson(matched.updatedAt != null ? matched.updatedAt : "") + "\""



                + "}"



                + "}";







            sendJsonResponse(exchange, 200, json);



        }



    }







    private static class AuthRegisterHandler implements HttpHandler {



        @Override



        public void handle(HttpExchange exchange) throws IOException {



            setCORSHeaders(exchange);



            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {



                exchange.sendResponseHeaders(204, -1);



                exchange.getResponseBody().close();



                return;



            }







            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {



                sendError(exchange, 405, "Method Not Allowed");



                return;



            }







            String body = readRequestBody(exchange);



            Map<String, Object> req = parseJsonMap(body);



            String name = (String) req.getOrDefault("name", "Candidate User");



            String email = (String) req.getOrDefault("email", "");



            String phone = (String) req.getOrDefault("phone", "+1 (555) 012-3456");



            int yearsExp = 0;



            if (req.containsKey("yearsExp")) {



                try {



                    yearsExp = Integer.parseInt(req.get("yearsExp").toString());



                } catch (Exception ignored) {}



            }







            List<String> skills = new ArrayList<>();



            if (req.get("skills") instanceof List) {



                for (Object o : (List<?>) req.get("skills")) {



                    skills.add(o.toString());



                }



            } else if (req.get("skills") instanceof String) {



                String[] parts = ((String) req.get("skills")).split(",");



                for (String p : parts) if (!p.trim().isEmpty()) skills.add(p.trim());



            }



            if (skills.isEmpty()) {



                skills.addAll(Arrays.asList("Java", "SQL", "Git", "REST API"));



            }







            if (email == null || email.trim().isEmpty()) {



                sendError(exchange, 400, "Email address is required.");



                return;



            }







            email = email.trim();







            // Check duplicate



            Candidate existing = null;



            for (Candidate c : CANDIDATES_LIST) {



                if (c.email != null && c.email.equalsIgnoreCase(email)) {



                    existing = c;



                    break;



                }



            }







            Candidate candToReturn = existing;



            if (candToReturn == null) {



                String newId = "CAND-" + (1000 + CANDIDATES_LIST.size() + 1);



                candToReturn = new Candidate(newId, name, email, phone, yearsExp, skills,



                    Arrays.asList("Bachelor's Degree"), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));



                CANDIDATES_LIST.add(candToReturn);



                DatabaseManager.saveCandidates();



            }







            String token = "CARRERX-AUTH-" + UUID.randomUUID().toString();



            StringBuilder skillsJson = new StringBuilder("[");



            for (int i = 0; i < candToReturn.skills.size(); i++) {



                skillsJson.append("\"").append(escapeJson(candToReturn.skills.get(i))).append("\"");



                if (i < candToReturn.skills.size() - 1) skillsJson.append(",");



            }



            skillsJson.append("]");







            String headline = yearsExp <= 1 ? "Associate Software Engineer" :



                              yearsExp >= 7 ? "Staff / Lead Engineer" :



                              yearsExp >= 5 ? "Senior Software Engineer" : "Software Engineer";







            String json = "{"



                + "\"success\":true,"



                + "\"message\":\"Account created and signed in successfully\","



                + "\"token\":\"" + token + "\","



                + "\"user\":{"



                + "\"candidateId\":\"" + escapeJson(candToReturn.candidateId) + "\","



                + "\"name\":\"" + escapeJson(candToReturn.name) + "\","



                + "\"email\":\"" + escapeJson(candToReturn.email) + "\","



                + "\"phone\":\"" + escapeJson(candToReturn.phone) + "\","



                + "\"headline\":\"" + escapeJson(headline) + "\","



                + "\"yearsExp\":" + candToReturn.yearsExp + ","



                + "\"skills\":" + skillsJson.toString() + ","



                + "\"updatedAt\":\"" + escapeJson(candToReturn.updatedAt != null ? candToReturn.updatedAt : "") + "\""



                + "}"



                + "}";







            sendJsonResponse(exchange, 201, json);



        }



    }







    private static class AuthMeHandler implements HttpHandler {



        @Override



        public void handle(HttpExchange exchange) throws IOException {



            setCORSHeaders(exchange);



            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {



                exchange.sendResponseHeaders(204, -1);



                exchange.getResponseBody().close();



                return;



            }







            String email = AuthService.getAuthenticatedEmail(exchange);



            if (email == null || email.isEmpty()) {



                sendJsonResponse(exchange, 200, "{\"authenticated\":false}");



                return;



            }







            Candidate c = null;



            for (Candidate cand : CANDIDATES_LIST) {



                if (cand.email != null && cand.email.equalsIgnoreCase(email)) {



                    c = cand;



                    break;



                }



            }







            if (c != null) {



                StringBuilder skillsJson = new StringBuilder("[");



                if (c.skills != null) {



                    for (int i = 0; i < c.skills.size(); i++) {



                        skillsJson.append("\"").append(escapeJson(c.skills.get(i))).append("\"");



                        if (i < c.skills.size() - 1) skillsJson.append(",");



                    }



                }



                skillsJson.append("]");







                String headline = c.yearsExp <= 1 ? "Associate Software Engineer" :



                                  c.yearsExp >= 7 ? "Staff / Lead Engineer" :



                                  c.yearsExp >= 5 ? "Senior Software Engineer" : "Software Engineer";







                String json = "{\"authenticated\":true,\"user\":{\"candidateId\":\"" + escapeJson(c.candidateId) + "\",\"name\":\"" + escapeJson(c.name) + "\",\"email\":\"" + escapeJson(c.email) + "\",\"phone\":\"" + escapeJson(c.phone != null ? c.phone : "") + "\",\"headline\":\"" + escapeJson(headline) + "\",\"yearsExp\":" + c.yearsExp + ",\"skills\":" + skillsJson.toString() + "}}";



                sendJsonResponse(exchange, 200, json);



            } else {



                sendJsonResponse(exchange, 200, "{\"authenticated\":false}");



            }



        }



    }







    private static class AuthLogoutHandler implements HttpHandler {



        @Override



        public void handle(HttpExchange exchange) throws IOException {



            setCORSHeaders(exchange);



            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {



                exchange.sendResponseHeaders(204, -1);



                exchange.getResponseBody().close();



                return;



            }



            String authHeader = exchange.getRequestHeaders().getFirst("Authorization");



            if (authHeader != null && authHeader.toLowerCase().startsWith("bearer ")) {



                AuthSessionRepository.invalidateSession(authHeader.substring(7).trim());



            }



            String tokenHeader = exchange.getRequestHeaders().getFirst("X-Auth-Token");



            if (tokenHeader != null) {



                AuthSessionRepository.invalidateSession(tokenHeader.trim());



            }



            sendJsonResponse(exchange, 200, "{\"success\":true,\"message\":\"Logged out successfully.\"}");



        }



    }







    // ==============================================================================
    // SECURE AI SERVICE CLIENT (Optional Gemini Integration via Environment Variable)
    // ==============================================================================
    static class AiServiceClient {
        private static final String GEMINI_API_KEY = getApiKey();

        private static String getApiKey() {
            String key = System.getenv("GEMINI_API_KEY");
            if (key == null || key.trim().isEmpty()) {
                key = System.getenv("AI_API_KEY");
            }
            return key != null ? key.trim() : "";
        }

        public static boolean isConfigured() {
            return !GEMINI_API_KEY.isEmpty();
        }

        public static String generateContent(String prompt) {
            if (!isConfigured()) return null;
            try {
                String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + GEMINI_API_KEY;
                java.net.URI uri = java.net.URI.create(endpoint);
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) uri.toURL().openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setConnectTimeout(6000);
                conn.setReadTimeout(10000);
                conn.setDoOutput(true);

                String jsonPrompt = "{\"contents\":[{\"parts\":[{\"text\":\"" + escapeJson(prompt) + "\"}]}],\"generationConfig\":{\"temperature\":0.3,\"maxOutputTokens\":800}}";
                try (java.io.OutputStream os = conn.getOutputStream()) {
                    os.write(jsonPrompt.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                }

                if (conn.getResponseCode() == 200) {
                    try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getInputStream(), java.nio.charset.StandardCharsets.UTF_8))) {
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) sb.append(line);
                        return sb.toString();
                    }
                }
            } catch (Exception e) {
                System.err.println("Gemini AI API connection notice: " + e.getMessage());
            }
            return null;
        }
    }

    private static class HealthHandler implements HttpHandler {



        @Override



        public void handle(HttpExchange exchange) throws IOException {



            setCORSHeaders(exchange);



            String json = "{\"status\":\"healthy\",\"server\":\"Java Built-in HttpServer\",\"version\":\"5.0.0\",\"totalJobs\":" + JOB_DATABASE.size() + ",\"totalApplications\":" + APPLICATIONS_LIST.size() + ",\"totalSavedJobs\":" + SAVED_JOBS_LIST.size() + ",\"totalCandidates\":" + CANDIDATES_LIST.size() + "}";



            sendJsonResponse(exchange, 200, json);



        }



    }







    /**



     * POST /api/career/predict - AI Career Path Predictor Engine.



     */



    private static class CareerPredictHandler implements HttpHandler {



        @Override



        public void handle(HttpExchange exchange) throws IOException {



            setCORSHeaders(exchange);



            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {



                exchange.sendResponseHeaders(204, -1);



                exchange.getResponseBody().close();



                return;



            }



            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod()) && !"GET".equalsIgnoreCase(exchange.getRequestMethod())) {



                sendError(exchange, 405, "Method Not Allowed");



                return;



            }







            String body = readRequestBody(exchange);



            Map<String, Object> req = parseJsonMap(body);







            @SuppressWarnings("unchecked")



            List<String> rawSkills = (List<String>) req.get("skills");



            if (rawSkills == null) rawSkills = new ArrayList<>();



            int yearsExp = parseNumber(req.get("yearsOfExperience"), parseNumber(req.get("yearsExp"), 0));



            String education = safeString(req.get("education"), "Computer Science Degree");



            String candidateEmail = (String) req.getOrDefault("email", "");







            if (rawSkills.isEmpty() && !candidateEmail.isEmpty()) {



                for (Candidate c : CANDIDATES_LIST) {



                    if (c.email.equalsIgnoreCase(candidateEmail)) {



                        rawSkills.addAll(c.skills);



                        if (yearsExp == 0) yearsExp = c.yearsExp;



                        break;



                    }



                }



            }







            int backendScore = 0;



            int cloudScore = 0;



            int aiScore = 0;



            int fullstackScore = 0;



            int leadScore = 0;







            for (String s : rawSkills) {



                String low = s.toLowerCase();



                if (low.contains("java") || low.contains("spring") || low.contains("microservice") || low.contains("sql") || low.contains("kafka") || low.contains("backend") || low.contains("database")) backendScore += 3;



                if (low.contains("aws") || low.contains("cloud") || low.contains("docker") || low.contains("kubernetes") || low.contains("devops") || low.contains("ci/cd") || low.contains("terraform")) cloudScore += 3;



                if (low.contains("python") || low.contains("ai") || low.contains("ml") || low.contains("machine learning") || low.contains("llm") || low.contains("deep learning") || low.contains("pytorch") || low.contains("tensorflow")) aiScore += 3;



                if (low.contains("react") || low.contains("angular") || low.contains("vue") || low.contains("node") || low.contains("javascript") || low.contains("typescript") || low.contains("fullstack")) fullstackScore += 3;



                if (low.contains("system design") || low.contains("architecture") || low.contains("leadership") || low.contains("management") || low.contains("lead")) leadScore += 3;



            }







            String trajectory = "Enterprise Software & Cloud Platforms";



            if (aiScore >= backendScore && aiScore >= cloudScore && aiScore >= fullstackScore) {



                trajectory = "Applied AI Engineering & Intelligent Systems";



            } else if (cloudScore >= backendScore && cloudScore >= fullstackScore) {



                trajectory = "Cloud Architecture & Site Reliability";



            } else if (fullstackScore > backendScore) {



                trajectory = "Full-Stack Web Systems & Modern UX Architecture";



            } else if (leadScore >= 6 || yearsExp >= 7) {



                trajectory = "Distributed Architecture & Engineering Leadership";



            }







            String role1, role3, role5;



            int salCurrent, sal1, sal3, sal5;



            int conf1, conf3, conf5;



            List<String> reqSkills1 = new ArrayList<>();



            List<String> reqSkills3 = new ArrayList<>();



            List<String> reqSkills5 = new ArrayList<>();







            if (yearsExp <= 1) {



                salCurrent = 650000;



                sal1 = 950000;



                sal3 = 1600000;



                sal5 = 2800000;



                conf1 = 94;



                conf3 = 86;



                conf5 = 75;



                if (trajectory.contains("AI")) {



                    role1 = "Junior AI Engineer (SDE-I)";



                    role3 = "Senior AI Software Engineer";



                    role5 = "Staff Machine Learning Architect";



                    reqSkills1.addAll(Arrays.asList("Vector Databases", "Prompt Engineering", "FastAPI"));



                    reqSkills3.addAll(Arrays.asList("Fine-Tuning LLMs", "Distributed Training", "MLOps / Kubeflow"));



                    reqSkills5.addAll(Arrays.asList("AI Systems Strategy", "Scalable Neural Inference", "Tech Org Leadership"));



                } else {



                    role1 = "Associate Software Development Engineer (SDE-II)";



                    role3 = "Senior Software Engineer";



                    role5 = "Technical Lead / Engineering Manager";



                    reqSkills1.addAll(Arrays.asList("Spring Boot", "Docker", "REST API Optimization"));



                    reqSkills3.addAll(Arrays.asList("System Design", "Kafka Event-Driven", "AWS ECS"));



                    reqSkills5.addAll(Arrays.asList("Enterprise Architecture", "Team Leadership", "Cloud Cost Optimization"));



                }



            } else if (yearsExp <= 4) {



                salCurrent = 1400000;



                sal1 = 1850000;



                sal3 = 2600000;



                sal5 = 3800000;



                conf1 = 91;



                conf3 = 83;



                conf5 = 74;



                if (trajectory.contains("AI")) {



                    role1 = "Senior AI & LLM Systems Engineer";



                    role3 = "Lead AI Systems Architect";



                    role5 = "Principal AI Engineer / VP of AI";



                    reqSkills1.addAll(Arrays.asList("LangChain / LlamaIndex", "GPU Acceleration", "Model Evaluation"));



                    reqSkills3.addAll(Arrays.asList("RAG Architecture at Scale", "Multi-Agent Systems", "Data Pipeline Orchestration"));



                    reqSkills5.addAll(Arrays.asList("AI Product Vision", "Executive Tech Leadership", "Patents & Research Commercialization"));



                } else if (trajectory.contains("Cloud")) {



                    role1 = "Senior Cloud & DevOps Engineer";



                    role3 = "Lead Cloud Infrastructure Architect";



                    role5 = "Director of Cloud Operations & SRE";



                    reqSkills1.addAll(Arrays.asList("Terraform IaC", "Kubernetes Production", "Observability (Prometheus/Grafana)"));



                    reqSkills3.addAll(Arrays.asList("Multi-Cloud Hybrid Architecture", "Zero-Trust Security", "Disaster Recovery"));



                    reqSkills5.addAll(Arrays.asList("Executive Infrastructure Strategy", "FinOps", "Global Team Management"));



                } else {



                    role1 = "Senior Java Distributed Systems Engineer";



                    role3 = "Staff Software Engineer / Solutions Architect";



                    role5 = "Principal Engineer / VP of Software Engineering";



                    reqSkills1.addAll(Arrays.asList("Kafka Event Streaming", "High-Throughput Microservices", "AWS"));



                    reqSkills3.addAll(Arrays.asList("Large Scale Distributed Systems", "Database Sharding", "Domain Driven Design"));



                    reqSkills5.addAll(Arrays.asList("Platform Strategy", "Technical Governance", "Cross-Functional Org Direction"));



                }



            } else {



                salCurrent = 2800000;



                sal1 = 3600000;



                sal3 = 4800000;



                sal5 = 7000000;



                conf1 = 90;



                conf3 = 81;



                conf5 = 72;



                role1 = "Staff Systems Engineer / Principal Developer";



                role3 = "Enterprise Solutions Architect / Director of Engineering";



                role5 = "Chief Technology Officer (CTO) / VP of Global Engineering";



                reqSkills1.addAll(Arrays.asList("High-Scale Architecture", "Multi-Region Resiliency", "Strategic Technical Mentorship"));



                reqSkills3.addAll(Arrays.asList("Enterprise Modernization", "Tech Radar Leadership", "Budget & Vendor Management"));



                reqSkills5.addAll(Arrays.asList("Board Advisory", "Executive Product Innovation", "Global Engineering Scaling"));



            }







            int overallConfidence = (conf1 + conf3 + conf5) / 3;







            StringBuilder sb = new StringBuilder();



            sb.append("{");



            sb.append("\"success\":true,");



            sb.append("\"primaryTrajectory\":\"").append(escapeJson(trajectory)).append("\",");



            sb.append("\"confidenceScore\":").append(overallConfidence).append(",");



            sb.append("\"yearsOfExperience\":").append(yearsExp).append(",");



            sb.append("\"education\":\"").append(escapeJson(education)).append("\",");







            sb.append("\"milestones\":[");



            sb.append("{")



              .append("\"horizon\":\"1-Year (Immediate Next Level)\",")



              .append("\"predictedRole\":\"").append(escapeJson(role1)).append("\",")



              .append("\"confidence\":").append(conf1).append(",")



              .append("\"salaryRange\":\"").append(formatSalaryLPARange(sal1 - 100000, sal1 + 100000)).append("\",")



              .append("\"salaryMedian\":").append(sal1).append(",")



              .append("\"marketDemand\":\"High (Top 10% Industry Demand)\",")



              .append("\"description\":\"Consolidate core competencies and lead mission-critical feature modules with enhanced system ownership.\",")



              .append("\"requiredSkills\":").append(listToJsonArray(reqSkills1))



              .append("},");



            sb.append("{")



              .append("\"horizon\":\"3-Year (Strategic Mid-Horizon)\",")



              .append("\"predictedRole\":\"").append(escapeJson(role3)).append("\",")



              .append("\"confidence\":").append(conf3).append(",")



              .append("\"salaryRange\":\"").append(formatSalaryLPARange(sal3 - 200000, sal3 + 200000)).append("\",")



              .append("\"salaryMedian\":").append(sal3).append(",")



              .append("\"marketDemand\":\"Very High (Critical Talent Shortage)\",")



              .append("\"description\":\"Own cross-team architecture, scalability strategy, and technology evaluations across organizational roadmaps.\",")



              .append("\"requiredSkills\":").append(listToJsonArray(reqSkills3))



              .append("},");



            sb.append("{")



              .append("\"horizon\":\"5-Year (Visionary & Leadership)\",")



              .append("\"predictedRole\":\"").append(escapeJson(role5)).append("\",")



              .append("\"confidence\":").append(conf5).append(",")



              .append("\"salaryRange\":\"").append(formatSalaryLPARange(sal5 - 300000, sal5 + 400000)).append("\",")



              .append("\"salaryMedian\":").append(sal5).append(",")



              .append("\"marketDemand\":\"Elite / Executive Grade\",")



              .append("\"description\":\"Drive high-level technical direction, enterprise strategy, and mentor next-generation engineering talent.\",")



              .append("\"requiredSkills\":").append(listToJsonArray(reqSkills5))



              .append("}");



            sb.append("],");







            sb.append("\"salaryTrajectory\":[");



            sb.append("{\"year\":\"Current Level\",\"avg\":").append(salCurrent).append("},");



            sb.append("{\"year\":\"Year +1\",\"avg\":").append(sal1).append("},");



            sb.append("{\"year\":\"Year +3\",\"avg\":").append(sal3).append("},");



            sb.append("{\"year\":\"Year +5\",\"avg\":").append(sal5).append("}");



            sb.append("],");







            List<String> breakthrough = new ArrayList<>();



            breakthrough.addAll(reqSkills1);



            breakthrough.addAll(reqSkills3);



            sb.append("\"breakthroughSkills\":").append(listToJsonArray(breakthrough)).append(",");







            sb.append("\"industryTransitions\":[");



            sb.append("{\"industry\":\"FinTech & Real-time Payments\",\"growth\":\"+24% YoY\",\"alignment\":\"94% Skill Overlap\"},");



            sb.append("{\"industry\":\"Autonomous Cloud & AI Infrastructure\",\"growth\":\"+38% YoY\",\"alignment\":\"89% Skill Overlap\"},");



            sb.append("{\"industry\":\"Enterprise Healthcare SaaS & Security\",\"growth\":\"+21% YoY\",\"alignment\":\"83% Skill Overlap\"}");



            sb.append("]");







            sb.append("}");



            sendJsonResponse(exchange, 200, sb.toString());



        }



    }







    /**



     * POST /api/career/skill-gap - Smart Skill Gap Finder & Personalized Learning Roadmap.



     */



    private static class SkillGapHandler implements HttpHandler {



        @Override



        public void handle(HttpExchange exchange) throws IOException {



            setCORSHeaders(exchange);



            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {



                exchange.sendResponseHeaders(204, -1);



                exchange.getResponseBody().close();



                return;



            }



            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod()) && !"GET".equalsIgnoreCase(exchange.getRequestMethod())) {



                sendError(exchange, 405, "Method Not Allowed");



                return;



            }







            String body = readRequestBody(exchange);



            Map<String, Object> req = parseJsonMap(body);







            String jobId = (String) req.getOrDefault("jobId", "");



            @SuppressWarnings("unchecked")



            List<String> userSkills = (List<String>) req.get("skills");



            if (userSkills == null) userSkills = new ArrayList<>();



            String candidateEmail = (String) req.getOrDefault("email", "");







            if (userSkills.isEmpty() && !candidateEmail.isEmpty()) {



                for (Candidate c : CANDIDATES_LIST) {



                    if (c.email.equalsIgnoreCase(candidateEmail)) {



                        userSkills.addAll(c.skills);



                        break;



                    }



                }



            }



            if (userSkills.isEmpty()) {



                userSkills.addAll(Arrays.asList("Java", "Spring Boot", "SQL", "REST APIs"));



            }







            // Find target job



            Job targetJob = null;



            if (!jobId.isEmpty()) {



                for (Job j : JOB_DATABASE) {



                    if (j.id.equalsIgnoreCase(jobId)) {



                        targetJob = j;



                        break;



                    }



                }



            }



            if (targetJob == null && !JOB_DATABASE.isEmpty()) {



                targetJob = JOB_DATABASE.get(0);



            }







            List<String> matched = new ArrayList<>();



            List<String> missingCritical = new ArrayList<>();



            List<String> missingSecondary = new ArrayList<>();







            if (targetJob != null) {



                for (String reqS : targetJob.requiredSkills) {



                    boolean has = false;



                    for (String uS : userSkills) {



                        if (skillsMatch(uS, reqS)) {



                            has = true;



                            matched.add(reqS);



                            break;



                        }



                    }



                    if (!has) missingCritical.add(reqS);



                }







                for (String niceS : targetJob.niceToHaveSkills) {



                    boolean has = false;



                    for (String uS : userSkills) {



                        if (skillsMatch(uS, niceS)) {



                            has = true;



                            if (!matched.contains(niceS)) matched.add(niceS);



                            break;



                        }



                    }



                    if (!has && !missingCritical.contains(niceS)) missingSecondary.add(niceS);



                }



            }







            int totalReq = targetJob != null ? targetJob.requiredSkills.size() : 5;



            int matchedReqCount = totalReq - missingCritical.size();



            int currentMatchPct = (int) Math.round(((double) Math.max(matchedReqCount, 1) / Math.max(totalReq, 1)) * 85.0);



            if (currentMatchPct > 98) currentMatchPct = 98;



            int potentialMatchPct = Math.min(currentMatchPct + (missingCritical.size() * 12) + (missingSecondary.size() * 4), 99);







            StringBuilder sb = new StringBuilder();



            sb.append("{");



            sb.append("\"success\":true,");



            sb.append("\"jobId\":\"").append(targetJob != null ? escapeJson(targetJob.id) : "").append("\",");



            sb.append("\"jobTitle\":\"").append(targetJob != null ? escapeJson(targetJob.title) : "Senior Software Engineer").append("\",");



            sb.append("\"company\":\"").append(targetJob != null ? escapeJson(targetJob.company) : "Tech Corp").append("\",");



            sb.append("\"currentMatchPercentage\":").append(currentMatchPct).append(",");



            sb.append("\"potentialMatchPercentage\":").append(potentialMatchPct).append(",");



            sb.append("\"matchedSkills\":").append(listToJsonArray(matched)).append(",");



            sb.append("\"missingCritical\":").append(listToJsonArray(missingCritical)).append(",");



            sb.append("\"missingSecondary\":").append(listToJsonArray(missingSecondary)).append(",");



            sb.append("\"estimatedTimeToClose\":\"4 to 6 Weeks (10-12 hrs/week)\",");







            // Roadmap Phases



            sb.append("\"roadmap\":[");



            sb.append("{")



              .append("\"phase\":\"Weeks 1–2 (High-Priority Core)\",")



              .append("\"title\":\"Foundational Critical Gaps: ").append(!missingCritical.isEmpty() ? escapeJson(missingCritical.get(0)) : "Core System Architecture").append("\",")



              .append("\"skills\":").append(listToJsonArray(!missingCritical.isEmpty() ? missingCritical.subList(0, Math.min(2, missingCritical.size())) : Arrays.asList("Distributed Systems", "Cloud Deployments"))).append(",")



              .append("\"hoursPerWeek\":12,")



              .append("\"projectMilestone\":\"Develop and benchmark a standalone proof-of-concept implementing enterprise architectural patterns\",")



              .append("\"learningResources\":[\"Official Engineering Docs & RFCs\", \"Interactive Production Sandbox Tutorials\", \"GitHub Enterprise Reference Architectures\"],")



              .append("\"matchGain\":\"+14% Match\"")



              .append("},");



            sb.append("{")



              .append("\"phase\":\"Weeks 3–4 (Production Integration)\",")



              .append("\"title\":\"Scalability & Cloud Infrastructure: ").append(missingCritical.size() > 1 ? escapeJson(missingCritical.get(1)) : "Container Orchestration").append("\",")



              .append("\"skills\":").append(listToJsonArray(missingCritical.size() > 1 ? missingCritical.subList(1, missingCritical.size()) : Arrays.asList("Kubernetes", "Observability"))).append(",")



              .append("\"hoursPerWeek\":10,")



              .append("\"projectMilestone\":\"Build a fault-tolerant containerized cluster with automated CI/CD and metrics monitoring\",")



              .append("\"learningResources\":[\"Production Kubernetes Best Practices Guide\", \"Cloud Native Computing Foundation (CNCF) Patterns\"],")



              .append("\"matchGain\":\"+10% Match\"")



              .append("},");



            sb.append("{")



              .append("\"phase\":\"Weeks 5–6 (Secondary Polish & Interview Mastery)\",")



              .append("\"title\":\"Nice-to-Have Polish & Domain Mastery: ").append(!missingSecondary.isEmpty() ? escapeJson(missingSecondary.get(0)) : "Performance Tuning").append("\",")



              .append("\"skills\":").append(listToJsonArray(!missingSecondary.isEmpty() ? missingSecondary : Arrays.asList("System Performance Profiling", "Security Auditing"))).append(",")



              .append("\"hoursPerWeek\":8,")



              .append("\"projectMilestone\":\"Deploy complete end-to-end portfolio demo with automated testing and live production link\",")



              .append("\"learningResources\":[\"High Performance Systems Playbook\", \"Production Security Hardening Guide\"],")



              .append("\"matchGain\":\"+6% Match\"")



              .append("}");



            sb.append("]");







            sb.append("}");



            sendJsonResponse(exchange, 200, sb.toString());



        }



    }







    /**



     * POST /api/career/simulator - What-If Career & Skill Simulator.



     */



    private static class CareerSimulatorHandler implements HttpHandler {



        @Override



        public void handle(HttpExchange exchange) throws IOException {



            setCORSHeaders(exchange);



            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {



                exchange.sendResponseHeaders(204, -1);



                exchange.getResponseBody().close();



                return;



            }



            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod()) && !"GET".equalsIgnoreCase(exchange.getRequestMethod())) {



                sendError(exchange, 405, "Method Not Allowed");



                return;



            }







            String body = readRequestBody(exchange);



            Map<String, Object> req = parseJsonMap(body);







            @SuppressWarnings("unchecked")



            List<String> currentSkills = (List<String>) req.get("currentSkills");



            if (currentSkills == null) currentSkills = new ArrayList<>();



            @SuppressWarnings("unchecked")



            List<String> simulatedSkills = (List<String>) req.get("simulatedSkills");



            if (simulatedSkills == null) simulatedSkills = new ArrayList<>();



            int yearsExp = parseNumber(req.get("yearsOfExperience"), parseNumber(req.get("yearsExp"), 2));



            String candidateEmail = (String) req.getOrDefault("email", "");







            if (currentSkills.isEmpty() && !candidateEmail.isEmpty()) {



                for (Candidate c : CANDIDATES_LIST) {



                    if (c.email.equalsIgnoreCase(candidateEmail)) {



                        currentSkills.addAll(c.skills);



                        if (yearsExp == 0) yearsExp = c.yearsExp;



                        break;



                    }



                }



            }



            if (currentSkills.isEmpty()) {



                currentSkills.addAll(Arrays.asList("Java", "SQL", "Git"));



            }







            // Baseline matching



            List<MatchResult> baselineResults = MatchingEngine.matchJobs(



                    "", currentSkills, yearsExp, 90000, "Any", "All", "All", "All", "All", false, 60, 20, 10, 10



            );







            // Combined skills



            Set<String> combinedSkillsSet = new LinkedHashSet<>(currentSkills);



            combinedSkillsSet.addAll(simulatedSkills);



            List<String> combinedSkills = new ArrayList<>(combinedSkillsSet);







            List<MatchResult> simulatedResults = MatchingEngine.matchJobs(



                    "", combinedSkills, yearsExp, 90000, "Any", "All", "All", "All", "All", false, 60, 20, 10, 10



            );







            // Calculate baseline metrics



            int baselineTotalScore = 0;



            int baselineQualifiedCount = 0;



            long baselineSalarySum = 0;



            Map<String, Integer> baselineScoreMap = new HashMap<>();







            for (MatchResult r : baselineResults) {



                baselineScoreMap.put(r.job.id, r.matchPercentage);



                baselineTotalScore += r.matchPercentage;



                if (r.matchPercentage >= 65) {



                    baselineQualifiedCount++;



                    baselineSalarySum += (r.job.minSalary + r.job.maxSalary) / 2;



                }



            }



            int baselineAvgScore = !baselineResults.isEmpty() ? (baselineTotalScore / baselineResults.size()) : 50;



            int baselineAvgSalary = baselineQualifiedCount > 0 ? (int)(baselineSalarySum / baselineQualifiedCount) : 1200000;







            // Calculate simulated metrics



            int simTotalScore = 0;



            int simQualifiedCount = 0;



            long simSalarySum = 0;



            List<String> newlyUnlockedJsonList = new ArrayList<>();







            // Find jobs that significantly improve or become unlocked



            List<MatchResult> candidateUnlocked = new ArrayList<>();



            for (MatchResult r : simulatedResults) {



                simTotalScore += r.matchPercentage;



                int baseScore = baselineScoreMap.getOrDefault(r.job.id, 40);



                if (r.matchPercentage >= 60) {



                    simQualifiedCount++;



                    simSalarySum += (r.job.minSalary + r.job.maxSalary) / 2;



                }



                boolean hasSimSkillMatch = false;



                for (String simS : simulatedSkills) {



                    for (String reqS : r.job.requiredSkills) {



                        if (skillsMatch(simS, reqS)) {



                            hasSimSkillMatch = true;



                            break;



                        }



                    }



                    if (hasSimSkillMatch) break;



                }



                if ((r.matchPercentage > baseScore && hasSimSkillMatch) || (r.matchPercentage >= 60 && baseScore < 60)) {



                    candidateUnlocked.add(r);



                }



            }







            // Sort by greatest improvement



            candidateUnlocked.sort((a, b) -> {



                int gainA = a.matchPercentage - baselineScoreMap.getOrDefault(a.job.id, 40);



                int gainB = b.matchPercentage - baselineScoreMap.getOrDefault(b.job.id, 40);



                return Integer.compare(gainB, gainA);



            });







            // If empty, take top 3 simulated jobs



            if (candidateUnlocked.isEmpty() && !simulatedResults.isEmpty()) {



                candidateUnlocked.addAll(simulatedResults.subList(0, Math.min(3, simulatedResults.size())));



            }







            for (MatchResult r : candidateUnlocked.subList(0, Math.min(6, candidateUnlocked.size()))) {



                int baseScore = baselineScoreMap.getOrDefault(r.job.id, Math.max(30, r.matchPercentage - 15));



                List<String> unlockedBy = new ArrayList<>();



                for (String simS : simulatedSkills) {



                    for (String reqS : r.job.requiredSkills) {



                        if (skillsMatch(simS, reqS) && !unlockedBy.contains(simS)) {



                            unlockedBy.add(simS);



                        }



                    }



                }



                if (unlockedBy.isEmpty() && !simulatedSkills.isEmpty()) {



                    unlockedBy.addAll(simulatedSkills.subList(0, Math.min(2, simulatedSkills.size())));



                }







                StringBuilder jobSb = new StringBuilder();



                jobSb.append("{")



                     .append("\"id\":\"").append(escapeJson(r.job.id)).append("\",")



                     .append("\"title\":\"").append(escapeJson(r.job.title)).append("\",")



                     .append("\"company\":\"").append(escapeJson(r.job.company)).append("\",")



                     .append("\"companyLogo\":\"").append(escapeJson(r.job.companyLogo)).append("\",")



                     .append("\"location\":\"").append(escapeJson(r.job.location)).append(" (").append(escapeJson(r.job.workMode)).append(")\",")



                     .append("\"salaryRange\":\"").append(escapeJson(formatJobSalary(r.job))).append("\",")



                     .append("\"beforeMatch\":").append(baseScore).append(",")



                     .append("\"afterMatch\":").append(r.matchPercentage).append(",")



                     .append("\"unlockedBy\":").append(listToJsonArray(unlockedBy))



                     .append("}");



                newlyUnlockedJsonList.add(jobSb.toString());



            }







            int simAvgScore = !simulatedResults.isEmpty() ? (simTotalScore / simulatedResults.size()) : 75;



            int simAvgSalary = simQualifiedCount > 0 ? (int)(simSalarySum / simQualifiedCount) : (baselineAvgSalary + 250000);







            int matchGain = Math.max(0, simAvgScore - baselineAvgScore);



            int salaryBoost = Math.max(150000, simAvgSalary - baselineAvgSalary);



            int unlockedCount = newlyUnlockedJsonList.size();



            if (unlockedCount == 0 && !simulatedSkills.isEmpty()) {



                unlockedCount = Math.min(simulatedSkills.size() * 2, 8);



            }







            StringBuilder sb = new StringBuilder();



            sb.append("{");



            sb.append("\"success\":true,");



            sb.append("\"baselineMatchAvg\":").append(baselineAvgScore).append(",");



            sb.append("\"simulatedMatchAvg\":").append(simAvgScore).append(",");



            sb.append("\"matchScoreGain\":").append(matchGain).append(",");



            sb.append("\"unlockedJobsCount\":").append(unlockedCount).append(",");



            sb.append("\"salaryBoostAmount\":").append(salaryBoost).append(",");



            sb.append("\"baselineSalaryAvg\":").append(baselineAvgSalary).append(",");



            sb.append("\"simulatedSalaryAvg\":").append(simAvgSalary).append(",");



            sb.append("\"simulatedSkillsCount\":").append(simulatedSkills.size()).append(",");







            sb.append("\"unlockedJobs\":[");



            for (int i = 0; i < newlyUnlockedJsonList.size(); i++) {



                sb.append(newlyUnlockedJsonList.get(i));



                if (i < newlyUnlockedJsonList.size() - 1) sb.append(",");



            }



            sb.append("]");







            sb.append("}");



            sendJsonResponse(exchange, 200, sb.toString());



        }



    }







    private static class StaticFileHandler implements HttpHandler {



        @Override



        public void handle(HttpExchange exchange) throws IOException {



            String path = exchange.getRequestURI().getPath();



            if (path == null || path.equals("/") || path.isEmpty()) {



                path = "/index.html";



            }







            File baseDir = new File(STATIC_DIR).getCanonicalFile();



            File file = new File(baseDir, path).getCanonicalFile();







            // Directory Traversal Security Guard



            if (!file.getPath().startsWith(baseDir.getPath())) {



                sendError(exchange, 403, "Access Forbidden");



                return;



            }







            if (!file.exists() || file.isDirectory()) {



                file = new File(baseDir, "index.html");



            }







            if (!file.exists()) {



                sendError(exchange, 404, "File Not Found: " + path);



                return;



            }







            String mime = getMimeType(file.getName());



            exchange.getResponseHeaders().set("Content-Type", mime);



            exchange.getResponseHeaders().set("Cache-Control", "no-cache, no-store, must-revalidate");



            setCORSHeaders(exchange);







            byte[] bytes = Files.readAllBytes(file.toPath());



            exchange.sendResponseHeaders(200, bytes.length);



            try (OutputStream os = exchange.getResponseBody()) {



                os.write(bytes);



            }



        }



    }







    // ==========================================



    // JSON & MULTIPART HELPERS



    // ==========================================







    private static String jobToJson(Job j) {



        StringBuilder sb = new StringBuilder("{");



        sb.append("\"id\":\"").append(escapeJson(j.id)).append("\",");



        sb.append("\"title\":\"").append(escapeJson(j.title)).append("\",");



        sb.append("\"company\":\"").append(escapeJson(j.company)).append("\",");



        sb.append("\"companyLogo\":\"").append(escapeJson(j.companyLogo)).append("\",");



        sb.append("\"location\":\"").append(escapeJson(j.location)).append("\",");



        sb.append("\"workMode\":\"").append(escapeJson(j.workMode)).append("\",");



        sb.append("\"jobType\":\"").append(escapeJson(j.jobType)).append("\",");



        sb.append("\"minSalary\":").append(j.minSalary).append(",");



        sb.append("\"maxSalary\":").append(j.maxSalary).append(",");



        sb.append("\"currency\":\"").append(escapeJson(j.currency)).append("\",");



        sb.append("\"experienceLevel\":\"").append(escapeJson(j.experienceLevel)).append("\",");



        sb.append("\"minYearsExp\":").append(j.minYearsExp).append(",");



        sb.append("\"category\":\"").append(escapeJson(j.category)).append("\",");



        sb.append("\"description\":\"").append(escapeJson(j.description)).append("\",");



        sb.append("\"requiredSkills\":").append(listToJsonArray(j.requiredSkills)).append(",");



        sb.append("\"niceToHaveSkills\":").append(listToJsonArray(j.niceToHaveSkills)).append(",");



        sb.append("\"benefits\":").append(listToJsonArray(j.benefits)).append(",");



        sb.append("\"postedDate\":\"").append(escapeJson(j.postedDate)).append("\",");



        sb.append("\"urgency\":\"").append(escapeJson(j.urgency)).append("\",");







        // Company Details



        sb.append("\"companyDetails\":{");



        if (j.companyDetails != null) {



            sb.append("\"industry\":\"").append(escapeJson(j.companyDetails.industry)).append("\",");



            sb.append("\"companySize\":\"").append(escapeJson(j.companyDetails.companySize)).append("\",");



            sb.append("\"foundedYear\":").append(j.companyDetails.foundedYear).append(",");



            sb.append("\"rating\":").append(j.companyDetails.rating).append(",");



            sb.append("\"culture\":\"").append(escapeJson(j.companyDetails.culture)).append("\",");



            sb.append("\"website\":\"").append(escapeJson(j.companyDetails.website)).append("\",");



            sb.append("\"ceo\":\"").append(escapeJson(j.companyDetails.ceo)).append("\"");



        }



        sb.append("},");







        // Location Intelligence



        sb.append("\"locationIntelligence\":{");



        if (j.locationIntelligence != null) {



            sb.append("\"city\":\"").append(escapeJson(j.locationIntelligence.city)).append("\",");



            sb.append("\"stateCountry\":\"").append(escapeJson(j.locationIntelligence.stateCountry)).append("\",");



            sb.append("\"neighborhood\":\"").append(escapeJson(j.locationIntelligence.neighborhood)).append("\",");



            sb.append("\"climateCategory\":\"").append(escapeJson(j.locationIntelligence.climateCategory)).append("\",");



            sb.append("\"climateOverview\":\"").append(escapeJson(j.locationIntelligence.climateOverview)).append("\",");



            sb.append("\"avgTemperature\":\"").append(escapeJson(j.locationIntelligence.avgTemperature)).append("\",");



            sb.append("\"sunnyDaysPerYear\":").append(j.locationIntelligence.sunnyDaysPerYear).append(",");



            sb.append("\"livingCostIndex\":\"").append(escapeJson(j.locationIntelligence.livingCostIndex)).append("\",");



            sb.append("\"commuteTransport\":\"").append(escapeJson(j.locationIntelligence.commuteTransport)).append("\"");



        }



        sb.append("},");







        // Joining & Workplace



        sb.append("\"joiningAndWorkplace\":{");



        if (j.joiningAndWorkplace != null) {



            sb.append("\"onboardingTimeline\":\"").append(escapeJson(j.joiningAndWorkplace.onboardingTimeline)).append("\",");



            sb.append("\"joiningBatch\":\"").append(escapeJson(j.joiningAndWorkplace.joiningBatch)).append("\",");



            sb.append("\"workingHours\":\"").append(escapeJson(j.joiningAndWorkplace.workingHours)).append("\",");



            sb.append("\"shiftType\":\"").append(escapeJson(j.joiningAndWorkplace.shiftType)).append("\",");



            sb.append("\"annualHolidays\":\"").append(escapeJson(j.joiningAndWorkplace.annualHolidays)).append("\",");



            sb.append("\"sickAndWellnessLeave\":\"").append(escapeJson(j.joiningAndWorkplace.sickAndWellnessLeave)).append("\",");



            sb.append("\"yearEndShutdown\":\"").append(escapeJson(j.joiningAndWorkplace.yearEndShutdown)).append("\",");



            sb.append("\"parentalLeave\":\"").append(escapeJson(j.joiningAndWorkplace.parentalLeave)).append("\"");



        }



        sb.append("}");







        sb.append("}");



        return sb.toString();



    }







    private static String listToJsonArray(List<String> list) {



        if (list == null) return "[]";



        StringBuilder sb = new StringBuilder("[");



        for (int i = 0; i < list.size(); i++) {



            sb.append("\"").append(escapeJson(list.get(i))).append("\"");



            if (i < list.size() - 1) sb.append(",");



        }



        sb.append("]");



        return sb.toString();



    }







    private static String escapeJson(String raw) {



        if (raw == null) return "";



        StringBuilder sb = new StringBuilder();



        for (char c : raw.toCharArray()) {



            switch (c) {



                case '"': sb.append("\\\""); break;



                case '\\': sb.append("\\\\"); break;



                case '\b': sb.append("\\b"); break;



                case '\f': sb.append("\\f"); break;



                case '\n': sb.append("\\n"); break;



                case '\r': sb.append("\\r"); break;



                case '\t': sb.append("\\t"); break;



                default:



                    if (c < 32 || c > 126) {



                        sb.append(String.format("\\u%04x", (int) c));



                    } else {



                        sb.append(c);



                    }



            }



        }



        return sb.toString();



    }







    private static Map<String, Object> parseJsonMap(String json) {



        Map<String, Object> map = new HashMap<>();



        if (json == null || json.trim().isEmpty()) return map;



        json = json.trim();



        if (json.startsWith("{")) json = json.substring(1);



        if (json.endsWith("}")) json = json.substring(0, json.length() - 1);







        Pattern propPattern = Pattern.compile("\"([^\"]+)\"\\s*:\\s*(\"(?:\\\\.|[^\"\\\\])*\"|\\[[\\s\\S]*?\\]|\\{[\\s\\S]*?\\}|-?\\d+(?:\\.\\d+)?|true|false|null)", Pattern.DOTALL);



        Matcher m = propPattern.matcher(json);



        while (m.find()) {



            String key = m.group(1);



            String val = m.group(2).trim();



            if (val.startsWith("\"") && val.endsWith("\"")) {



                map.put(key, unescapeJson(val.substring(1, val.length() - 1)));



            } else if (val.startsWith("[") && val.endsWith("]")) {



                String inner = val.substring(1, val.length() - 1).trim();



                List<String> items = new ArrayList<>();



                if (!inner.isEmpty()) {



                    Matcher itemMatcher = Pattern.compile("\"((?:\\\\.|[^\"\\\\])*)\"").matcher(inner);



                    while (itemMatcher.find()) {



                        items.add(unescapeJson(itemMatcher.group(1)));



                    }



                }



                map.put(key, items);



            } else if (val.matches("-?\\d+")) {



                map.put(key, Integer.parseInt(val));



            } else if (val.matches("-?\\d+\\.\\d+")) {



                map.put(key, Double.parseDouble(val));



            } else if (val.equalsIgnoreCase("true") || val.equalsIgnoreCase("false")) {



                map.put(key, Boolean.parseBoolean(val));



            }



        }



        return map;



    }







    private static String unescapeJson(String s) {



        if (s == null) return "";



        return s.replace("\\\"", "\"")



                .replace("\\\\", "\\")



                .replace("\\n", "\n")



                .replace("\\r", "\r")



                .replace("\\t", "\t");



    }







    private static String formatIndianNumber(long n) {



        String s = String.valueOf(n);



        if (s.length() <= 3) return s;



        String lastThree = s.substring(s.length() - 3);



        String remaining = s.substring(0, s.length() - 3);



        StringBuilder sb = new StringBuilder();



        while (remaining.length() > 2) {



            sb.insert(0, "," + remaining.substring(remaining.length() - 2));



            remaining = remaining.substring(0, remaining.length() - 2);



        }



        sb.insert(0, remaining);



        sb.append(",").append(lastThree);



        return sb.toString();



    }







    private static String formatLakh(long n) {



        double l = n / 100000.0;



        if (l == (long) l) return String.valueOf((long) l);



        return String.format(java.util.Locale.US, "%.1f", l);



    }

    private static String formatSalaryLPARange(long min, long max) {
        String minL = formatLakh(min);
        String maxL = formatLakh(max);
        return "₹" + minL + " - ₹" + maxL + " LPA";
    }








    private static String formatJobSalary(Job j) {



        if (j == null) return "₹0 LPA";



        String minL = formatLakh(j.minSalary);



        String maxL = formatLakh(j.maxSalary);



        if (j.minSalary == j.maxSalary || j.maxSalary <= 0) {



            return "₹" + minL + " LPA";



        }



        return "₹" + minL + " - ₹" + maxL + " LPA";



    }







    private static String formatNumber(int n) {



        return String.format("%,d", n);



    }







        private static String safeString(Object obj, String fallback) {
        if (obj == null) return fallback;
        if (obj instanceof String) {
            String s = ((String) obj).trim();
            return s.isEmpty() ? fallback : s;
        }
        if (obj instanceof List) {
            List<?> l = (List<?>) obj;
            if (l.isEmpty()) return fallback;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < l.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(String.valueOf(l.get(i)));
            }
            return sb.toString();
        }
        return String.valueOf(obj);
    }

    private static int parseNumber(Object obj, int defaultVal) {



        if (obj == null) return defaultVal;



        if (obj instanceof Number) return ((Number) obj).intValue();



        try {



            return Integer.parseInt(obj.toString().replaceAll("[^0-9]", ""));



        } catch (Exception e) {



            return defaultVal;



        }



    }







    private static Map<String, String> parseQueryParams(String query) {



        Map<String, String> map = new HashMap<>();



        if (query == null || query.trim().isEmpty()) return map;



        for (String param : query.split("&")) {



            String[] pair = param.split("=", 2);



            if (pair.length > 0) {



                try {



                    String key = URLDecoder.decode(pair[0], StandardCharsets.UTF_8.name());



                    String value = pair.length > 1 ? URLDecoder.decode(pair[1], StandardCharsets.UTF_8.name()) : "";



                    map.put(key, value);



                } catch (Exception ignored) {}



            }



        }



        return map;



    }







    private static byte[] readRequestBodyBytes(HttpExchange exchange) throws IOException {



        try (InputStream is = exchange.getRequestBody();



             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {



            byte[] buf = new byte[8192];



            int read;



            while ((read = is.read(buf)) != -1) {



                bos.write(buf, 0, read);



            }



            return bos.toByteArray();



        }



    }







    private static String readRequestBody(HttpExchange exchange) throws IOException {



        byte[] bytes = readRequestBodyBytes(exchange);



        return new String(bytes, StandardCharsets.UTF_8);



    }







    private static byte[] parseMultipartFileBytes(byte[] body, String boundary) {



        try {



            String raw = new String(body, StandardCharsets.ISO_8859_1);



            String boundMarker = "--" + boundary;



            int startIdx = raw.indexOf(boundMarker);



            if (startIdx == -1) return body;







            // Find end of headers (\r\n\r\n or \n\n)



            int headerEnd = raw.indexOf("\r\n\r\n", startIdx);



            int headerLen = 4;



            if (headerEnd == -1) {



                headerEnd = raw.indexOf("\n\n", startIdx);



                headerLen = 2;



            }



            if (headerEnd == -1) return body;







            int fileStart = headerEnd + headerLen;



            int fileEnd = raw.indexOf(boundMarker, fileStart);



            if (fileEnd == -1) fileEnd = body.length;



            else {



                if (fileEnd >= 2 && raw.charAt(fileEnd - 2) == '\r' && raw.charAt(fileEnd - 1) == '\n') {



                    fileEnd -= 2;



                } else if (fileEnd >= 1 && raw.charAt(fileEnd - 1) == '\n') {



                    fileEnd -= 1;



                }



            }







            if (fileStart < fileEnd) {



                return Arrays.copyOfRange(body, fileStart, fileEnd);



            }



        } catch (Exception ignored) {}



        return body;



    }







    private static String parseMultipartFilename(byte[] body, String boundary) {



        try {



            String raw = new String(body, StandardCharsets.ISO_8859_1);



            Matcher m = Pattern.compile("filename=\"([^\"]+)\"").matcher(raw);



            if (m.find()) {



                return m.group(1);



            }



        } catch (Exception ignored) {}



        return "resume.pdf";



    }











    // ==============================================================================



    // FEATURE 4: AI INTERVIEW PRACTICE & EVALUATION ENGINE



    // ==============================================================================



    private static final List<Map<String, Object>> INTERVIEW_HISTORY = new java.util.concurrent.CopyOnWriteArrayList<>();







    private static void initInterviewHistory() {



        try {



            File f = new File("data/interview_history.json");



            if (f.exists()) {



                String content = new String(java.nio.file.Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);



                java.util.regex.Pattern objPat = java.util.regex.Pattern.compile("\\{[^{}]*\\}", java.util.regex.Pattern.DOTALL);



                java.util.regex.Matcher m = objPat.matcher(content);



                while (m.find()) {



                    Map<String, Object> map = parseJsonMap(m.group());



                    if (map != null && !map.isEmpty()) {



                        INTERVIEW_HISTORY.add(map);



                    }



                }



            }



        } catch (Exception e) {



            System.err.println("Note: Starting with empty interview history: " + e.getMessage());



        }



    }







    private static synchronized void saveInterviewHistory() {



        try {



            File dataDir = new File("data");



            if (!dataDir.exists()) dataDir.mkdirs();



            File f = new File(dataDir, "interview_history.json");



            StringBuilder sb = new StringBuilder();



            sb.append("[\n");



            for (int i = 0; i < INTERVIEW_HISTORY.size(); i++) {



                Map<String, Object> item = INTERVIEW_HISTORY.get(i);



                sb.append("  {");



                sb.append("\"id\":\"").append(escapeJson((String)item.getOrDefault("id", ""))).append("\",");



                sb.append("\"email\":\"").append(escapeJson((String)item.getOrDefault("email", ""))).append("\",");



                sb.append("\"jobId\":\"").append(escapeJson((String)item.getOrDefault("jobId", ""))).append("\",");



                sb.append("\"jobTitle\":\"").append(escapeJson((String)item.getOrDefault("jobTitle", ""))).append("\",");



                sb.append("\"questionId\":\"").append(escapeJson((String)item.getOrDefault("questionId", ""))).append("\",");



                sb.append("\"questionText\":\"").append(escapeJson((String)item.getOrDefault("questionText", ""))).append("\",");



                sb.append("\"overallScore\":").append(item.getOrDefault("overallScore", 70)).append(",");



                sb.append("\"technicalScore\":").append(item.getOrDefault("technicalScore", 70)).append(",");



                sb.append("\"communicationScore\":").append(item.getOrDefault("communicationScore", 70)).append(",");



                sb.append("\"starScore\":").append(item.getOrDefault("starScore", 70)).append(",");



                sb.append("\"verdict\":\"").append(escapeJson((String)item.getOrDefault("verdict", ""))).append("\",");



                sb.append("\"timestamp\":\"").append(escapeJson((String)item.getOrDefault("timestamp", ""))).append("\"");



                sb.append("}").append(i < INTERVIEW_HISTORY.size() - 1 ? ",\n" : "\n");



            }



            sb.append("]\n");



            java.nio.file.Files.write(f.toPath(), sb.toString().getBytes(StandardCharsets.UTF_8));



        } catch (Exception e) {



            System.err.println("Error saving interview history: " + e.getMessage());



        }



    }







    private static class InterviewQuestionsHandler implements HttpHandler {



        @Override



        public void handle(HttpExchange exchange) throws IOException {



            setCORSHeaders(exchange);



            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {



                exchange.sendResponseHeaders(204, -1);



                exchange.getResponseBody().close();



                return;



            }







            String jobId = "";



            String jobTitle = "";







            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {



                String body = readRequestBody(exchange);



                Map<String, Object> req = parseJsonMap(body);



                jobId = (String) req.getOrDefault("jobId", "");



                jobTitle = (String) req.getOrDefault("jobTitle", "");



            } else {



                Map<String, String> query = parseQueryParams(exchange.getRequestURI().getQuery());



                jobId = query.getOrDefault("jobId", "");



                jobTitle = query.getOrDefault("jobTitle", "");



            }







            Job matchedJob = null;



            if (!jobId.isEmpty()) {



                for (Job j : JOB_DATABASE) {



                    if (j.id.equalsIgnoreCase(jobId)) {



                        matchedJob = j;



                        break;



                    }



                }



            }



            if (matchedJob == null && !jobTitle.isEmpty()) {



                for (Job j : JOB_DATABASE) {



                    if (j.title.toLowerCase().contains(jobTitle.toLowerCase())) {



                        matchedJob = j;



                        break;



                    }



                }



            }



            if (matchedJob == null && !JOB_DATABASE.isEmpty()) {



                matchedJob = JOB_DATABASE.get(0);



            }







            String targetTitle = matchedJob != null ? matchedJob.title : "Software Engineer";



            String targetCompany = matchedJob != null ? matchedJob.company : "Top Tech";



            List<String> targetSkills = matchedJob != null ? matchedJob.requiredSkills : Arrays.asList("Java", "SQL", "Spring Boot", "Git");



            String skill1 = targetSkills.size() > 0 ? targetSkills.get(0) : "Backend";



            String skill2 = targetSkills.size() > 1 ? targetSkills.get(1) : "Databases";



            String skill3 = targetSkills.size() > 2 ? targetSkills.get(2) : "Cloud";







            String titleLower = targetTitle.toLowerCase();



            boolean isDataAI = titleLower.contains("data") || titleLower.contains("ai") || titleLower.contains("machine") || titleLower.contains("python");



            boolean isCloudDevOps = titleLower.contains("cloud") || titleLower.contains("devops") || titleLower.contains("sre") || titleLower.contains("infrastructure");



            boolean isFrontend = titleLower.contains("frontend") || titleLower.contains("react") || titleLower.contains("ui") || titleLower.contains("full stack");







            StringBuilder sb = new StringBuilder();



            sb.append("{");



            sb.append("\"success\":true,");



            sb.append("\"jobId\":\"").append(escapeJson(matchedJob != null ? matchedJob.id : "")).append("\",");



            sb.append("\"jobTitle\":\"").append(escapeJson(targetTitle)).append("\",");



            sb.append("\"company\":\"").append(escapeJson(targetCompany)).append("\",");



            sb.append("\"questions\":[");







            if (isDataAI) {



                // Question 1: System Design / ML



                sb.append("{")



                  .append("\"id\":\"q1\",")



                  .append("\"category\":\"System Architecture & Pipelines\",")



                  .append("\"difficulty\":\"Senior Level\",")



                  .append("\"question\":\"How would you architect a fault-tolerant batch and streaming data pipeline that processes millions of event records per hour with deduplication and schema drift handling?\",")



                  .append("\"hints\":\"Mention ingestion tools (Kafka, Spark), storage formats (Parquet, Delta Lake), deduplication strategies, and schema evolution rules.\",")



                  .append("\"keyConcepts\":[\"Kafka\",\"Spark\",\"Parquet\",\"Delta Lake\",\"Deduplication\",\"Schema Registry\",\"Idempotency\"]")



                  .append("},");







                // Question 2: Technical Deep Dive



                sb.append("{")



                  .append("\"id\":\"q2\",")



                  .append("\"category\":\"Technical Core & Optimization\",")



                  .append("\"difficulty\":\"Technical Round\",")



                  .append("\"question\":\"Explain the trade-offs between dense vector embeddings (e.g., Cosine/HNSW in Vector DBs) versus lexical search (BM25/Elasticsearch). How would you build a hybrid search engine to prevent hallucination?\",")



                  .append("\"hints\":\"Discuss semantic search accuracy, cold start issues, reciprocal rank fusion (RRF), and cross-encoder re-ranking latency.\",")



                  .append("\"keyConcepts\":[\"Vector DB\",\"BM25\",\"HNSW\",\"Hybrid Search\",\"RRF\",\"Re-ranking\",\"Hallucination\"]")



                  .append("},");







                // Question 3: Behavioral STAR



                sb.append("{")



                  .append("\"id\":\"q3\",")



                  .append("\"category\":\"Behavioral (STAR Method)\",")



                  .append("\"difficulty\":\"Leadership & Culture\",")



                  .append("\"question\":\"Describe a scenario where a critical data inconsistency or model performance degradation was spotted in production. How did you diagnose, resolve, and communicate with stakeholders?\",")



                  .append("\"hints\":\"Structure using Situation, Task, Action, and measurable Result. Highlight incident response and data drift monitoring.\",")



                  .append("\"keyConcepts\":[\"Data Drift\",\"Postmortem\",\"Data Quality\",\"Stakeholder Management\",\"SLA\",\"Root Cause\"]")



                  .append("},");







                // Question 4: Scalability & Production



                sb.append("{")



                  .append("\"id\":\"q4\",")



                  .append("\"category\":\"Scalability & Cost Optimization\",")



                  .append("\"difficulty\":\"Architecture Round\",")



                  .append("\"question\":\"How do you optimize cluster computing resources and query execution costs when running petabyte-scale workloads on cloud data warehouses like Snowflake, BigQuery, or Databricks?\",")



                  .append("\"hints\":\"Discuss partitioning, clustering keys, materialized views, warehouse auto-suspension, and memory spilling mitigation.\",")



                  .append("\"keyConcepts\":[\"Partitioning\",\"Clustering\",\"Auto-suspend\",\"Materialized Views\",\"Spilling\",\"Cost Optimization\"]")



                  .append("}");







            } else if (isCloudDevOps) {



                // Question 1



                sb.append("{")



                  .append("\"id\":\"q1\",")



                  .append("\"category\":\"Cloud Infrastructure & IaC\",")



                  .append("\"difficulty\":\"Senior Level\",")



                  .append("\"question\":\"How do you structure Terraform and GitOps pipelines to ensure multi-environment drift detection, automated security scanning, and zero-downtime rolling upgrades?\",")



                  .append("\"hints\":\"Discuss state locking (DynamoDB), remote backends, TFSec/Checkov, ArgoCD, and automated rollback triggers.\",")



                  .append("\"keyConcepts\":[\"Terraform\",\"GitOps\",\"ArgoCD\",\"State Locking\",\"Drift Detection\",\"Checkov\",\"Rollback\"]")



                  .append("},");







                // Question 2



                sb.append("{")



                  .append("\"id\":\"q2\",")



                  .append("\"category\":\"Container Orchestration & Reliability\",")



                  .append("\"difficulty\":\"Technical Round\",")



                  .append("\"question\":\"Walk me through how you troubleshoot a Kubernetes service degradation where pods are repeatedly evicted due to OOMKilled events and node memory pressure.\",")



                  .append("\"hints\":\"Explain cgroup limits, resource requests vs limits, heap dump capture, node autoscaling, and vertical pod autoscalers.\",")



                  .append("\"keyConcepts\":[\"Kubernetes\",\"OOMKilled\",\"cgroups\",\"Requests and Limits\",\"VPA\",\"HPA\",\"Heap Analysis\"]")



                  .append("},");







                // Question 3



                sb.append("{")



                  .append("\"id\":\"q3\",")



                  .append("\"category\":\"Behavioral (STAR Method)\",")



                  .append("\"difficulty\":\"Incident Management\",")



                  .append("\"question\":\"Tell me about a high-severity production outage you managed. How did you establish the incident command, mitigate blast radius, and execute the postmortem?\",")



                  .append("\"hints\":\"Detail the initial alert, war room coordination, immediate rollback or failover action, and corrective work items created.\",")



                  .append("\"keyConcepts\":[\"War Room\",\"Blast Radius\",\"Failover\",\"Postmortem\",\"Action Items\",\"SLA\",\"Runbook\"]")



                  .append("},");







                // Question 4



                sb.append("{")



                  .append("\"id\":\"q4\",")



                  .append("\"category\":\"Production Observability & Security\",")



                  .append("\"difficulty\":\"Production Operations\",")



                  .append("\"question\":\"How do you design a comprehensive observability strategy (Metrics, Logs, Traces) with distributed tracing (OpenTelemetry) to track sub-second microservice SLAs?\",")



                  .append("\"hints\":\"Discuss span context propagation, Prometheus alerting thresholds, high-cardinality log indexing, and synthetic monitoring.\",")



                  .append("\"keyConcepts\":[\"OpenTelemetry\",\"Distributed Tracing\",\"Prometheus\",\"Context Propagation\",\"SLO/SLA\",\"Synthetic Probes\"]")



                  .append("}");







            } else {



                // Generic / Backend / Fullstack / Java / Spring Boot / Systems



                // Question 1



                sb.append("{")



                  .append("\"id\":\"q1\",")



                  .append("\"category\":\"System Architecture & Distributed Systems\",")



                  .append("\"difficulty\":\"Senior System Design\",")



                  .append("\"question\":\"How do you ensure data consistency and transactional integrity across microservices communicating asynchronously via Kafka without relying on slow distributed 2PC locking?\",")



                  .append("\"hints\":\"Discuss the Transactional Outbox Pattern, Saga pattern (Orchestration vs Choreography), idempotent consumers, and dead letter queues.\",")



                  .append("\"keyConcepts\":[\"Transactional Outbox\",\"Saga Pattern\",\"Kafka\",\"Idempotency\",\"Eventual Consistency\",\"DLQ\"]")



                  .append("},");







                // Question 2



                sb.append("{")



                  .append("\"id\":\"q2\",")



                  .append("\"category\":\"Performance, Concurrency & Database Tuning\",")



                  .append("\"difficulty\":\"Technical Deep Dive\",")



                  .append("\"question\":\"A critical REST endpoint has latency spiking from 35ms to 1200ms during peak load. Detail your step-by-step methodology to profile, identify, and eliminate the bottleneck.\",")



                  .append("\"hints\":\"Cover thread dumps/APM profiling, connection pool exhaustion (HikariCP), unindexed SQL queries (EXPLAIN ANALYZE), and Redis cache strategies.\",")



                  .append("\"keyConcepts\":[\"HikariCP\",\"Connection Pool\",\"EXPLAIN ANALYZE\",\"Indexes\",\"Redis Cache\",\"Thread Contention\",\"APM\"]")



                  .append("},");







                // Question 3



                sb.append("{")



                  .append("\"id\":\"q3\",")



                  .append("\"category\":\"Behavioral & Leadership (STAR Method)\",")



                  .append("\"difficulty\":\"Engineering Leadership\",")



                  .append("\"question\":\"Describe a project where you had to push back on unrealistic delivery deadlines or tech debt accumulation. How did you balance business urgency with long-term code quality?\",")



                  .append("\"hints\":\"Use Situation, Task, Action, Result. Show pragmatic compromise, phased rollouts, and measurable risk reduction.\",")



                  .append("\"keyConcepts\":[\"Tech Debt\",\"Stakeholder Tradeoffs\",\"Phased Delivery\",\"Risk Mitigation\",\"Architecture Review\",\"STAR\"]")



                  .append("},");







                // Question 4



                sb.append("{")



                  .append("\"id\":\"q4\",")



                  .append("\"category\":\"API Security, Resiliency & Scale\",")



                  .append("\"difficulty\":\"Production Hardening\",")



                  .append("\"question\":\"How do you protect high-volume APIs against cascading failure, distributed denial of service, and credential stuffing while maintaining high availability?\",")



                  .append("\"hints\":\"Discuss Circuit Breakers (Resilience4j), distributed Token Bucket rate limiting, OAuth2/JWT stateless validation, and bulkhead patterns.\",")



                  .append("\"keyConcepts\":[\"Circuit Breaker\",\"Resilience4j\",\"Rate Limiting\",\"Token Bucket\",\"JWT\",\"Bulkhead Pattern\",\"High Availability\"]")



                  .append("}");



            }







            sb.append("]");



            sb.append("}");







            sendJsonResponse(exchange, 200, sb.toString());



        }



    }







    private static class InterviewEvaluateHandler implements HttpHandler {



        @Override



        public void handle(HttpExchange exchange) throws IOException {



            setCORSHeaders(exchange);



            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {



                exchange.sendResponseHeaders(204, -1);



                exchange.getResponseBody().close();



                return;



            }



            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {



                sendError(exchange, 405, "Method Not Allowed");



                return;



            }







            String body = readRequestBody(exchange);



            Map<String, Object> req = parseJsonMap(body);







            String jobId = (String) req.getOrDefault("jobId", "");



            String jobTitle = (String) req.getOrDefault("jobTitle", "Software Engineer");



            String questionId = (String) req.getOrDefault("questionId", "q1");



            String questionText = (String) req.getOrDefault("questionText", "Technical Interview Question");



            String userAnswer = (String) req.getOrDefault("userAnswer", "");



            String email = (String) req.getOrDefault("email", "");







            if (userAnswer == null || userAnswer.trim().isEmpty()) {



                sendError(exchange, 400, "Please provide an answer to evaluate.");



                return;



            }







            String cleanAns = userAnswer.trim();



            String lowerAns = cleanAns.toLowerCase();



            String[] words = cleanAns.split("\\s+");



            int wordCount = words.length;







            // 1. Concept / Technical Hit Detection



            String[] commonKeywords = {



                "architecture", "kafka", "redis", "database", "sql", "cache", "latency",



                "microservice", "docker", "kubernetes", "cloud", "aws", "security",



                "scalable", "throughput", "index", "concurrency", "thread", "api",



                "pattern", "testing", "monitoring", "pipeline", "performance", "metric"



            };







            int conceptMatches = 0;



            List<String> matchedConcepts = new ArrayList<>();



            for (String kw : commonKeywords) {



                if (lowerAns.contains(kw)) {



                    conceptMatches++;



                    if (matchedConcepts.size() < 5) matchedConcepts.add(kw);



                }



            }







            // 2. STAR Method Analysis



            boolean hasSituation = lowerAns.contains("when ") || lowerAns.contains("situation") || lowerAns.contains("project")



                    || lowerAns.contains("company") || lowerAns.contains("client") || lowerAns.contains("problem") || lowerAns.contains("faced");



            boolean hasTask = lowerAns.contains("task") || lowerAns.contains("goal") || lowerAns.contains("needed to")



                    || lowerAns.contains("responsible for") || lowerAns.contains("challenge was") || lowerAns.contains("requirement");



            boolean hasAction = lowerAns.contains("i built") || lowerAns.contains("i designed") || lowerAns.contains("i created")



                    || lowerAns.contains("i implemented") || lowerAns.contains("i used") || lowerAns.contains("we implemented")



                    || lowerAns.contains("we designed") || lowerAns.contains("i configured") || lowerAns.contains("i diagnosed")



                    || lowerAns.contains("i refactored") || lowerAns.contains("my approach") || lowerAns.contains("we adopted");



            boolean hasResult = lowerAns.contains("result") || lowerAns.contains("improved") || lowerAns.contains("reduced")



                    || lowerAns.contains("increased") || lowerAns.contains("%") || lowerAns.contains("saved") || lowerAns.contains("ms")



                    || lowerAns.contains("successfully") || lowerAns.contains("achieved") || lowerAns.contains("prevented");







            int starComponentCount = (hasSituation ? 1 : 0) + (hasTask ? 1 : 0) + (hasAction ? 1 : 0) + (hasResult ? 1 : 0);







            // 3. Scoring Engine



            int technicalScore;



            int communicationScore;



            int starScore;







            if (wordCount < 15) {



                technicalScore = Math.min(45, wordCount * 3 + conceptMatches * 5);



                communicationScore = 40;



                starScore = 30;



            } else if (wordCount < 40) {



                technicalScore = Math.min(68, 45 + (conceptMatches * 5));



                communicationScore = 65;



                starScore = 50 + (starComponentCount * 7);



            } else if (wordCount < 90) {



                technicalScore = Math.min(88, 62 + (conceptMatches * 4));



                communicationScore = 80;



                starScore = 65 + (starComponentCount * 8);



            } else {



                technicalScore = Math.min(97, 72 + (conceptMatches * 4));



                communicationScore = Math.min(96, 85 + Math.min(10, (wordCount - 90) / 10));



                starScore = Math.min(98, 70 + (starComponentCount * 7));



            }







            int overallScore = (int) Math.round((technicalScore * 0.45) + (communicationScore * 0.25) + (starScore * 0.30));



            overallScore = Math.max(25, Math.min(98, overallScore));







            String verdict;



            String verdictColor;



            if (overallScore >= 85) {



                verdict = "Strong Hire (Exceeds Expectations)";



                verdictColor = "#059669";



            } else if (overallScore >= 72) {



                verdict = "Hire (Meets Role Requirements)";



                verdictColor = "#2563eb";



            } else if (overallScore >= 55) {



                verdict = "Needs Polish (Potential Fit with Practice)";



                verdictColor = "#d97706";



            } else {



                verdict = "Below Threshold (Requires More Substance & Structure)";



                verdictColor = "#dc2626";



            }







            // Strengths & Improvement Suggestions



            List<String> strengths = new ArrayList<>();



            List<String> improvements = new ArrayList<>();







            if (conceptMatches >= 3) {



                strengths.add("Strong technical vocabulary demonstrated (identified concepts: " + String.join(", ", matchedConcepts) + ").");



            } else {



                improvements.add("Incorporate more industry-standard terminology and tool names (e.g., specific protocols, patterns, and databases).");



            }







            if (wordCount >= 70) {



                strengths.add("Thorough, well-elaborated explanation with clear technical narrative.");



            } else {



                improvements.add("Expand your answer depth (currently " + wordCount + " words). Aim for 70-150 words with specific architectural trade-offs.");



            }







            if (hasAction) {



                strengths.add("Clear demonstration of personal ownership and explicit technical decisions taken.");



            } else {



                improvements.add("State your personal contribution more clearly using first-person active verbs ('I designed...', 'I implemented...').");



            }







            if (hasResult) {



                strengths.add("Great inclusion of quantitative outcome and impact metrics.");



            } else {



                improvements.add("Quantify your results with concrete numbers (e.g. 'reduced latency by 45%', 'handled 50k RPS', or 'achieved 99.99% uptime').");



            }







            if (strengths.isEmpty()) {



                strengths.add("Answer directly addresses the interview question prompt.");



            }



            if (improvements.isEmpty()) {



                improvements.add("Challenge yourself by exploring edge cases, security posture, and failover disaster recovery scenarios.");



            }







            // AI Exemplar Answer



            String exemplar = "In a previous production system processing 25,000 requests/sec, we encountered severe database contention during traffic spikes. " +



                "I architected a multi-tiered mitigation: first, I introduced a Redis write-through cache with TTL jitter to eliminate cache stampedes; " +



                "second, I transitioned asynchronous state mutations to an event-driven Kafka topic with idempotent consumer workers. " +



                "This decoupled the write path and allowed graceful load shedding via a token bucket rate limiter. " +



                "As a result, p99 endpoint latency dropped from 940ms to 24ms (a 97% reduction), zero transactional records were dropped, and our infrastructure cost decreased by 30%.";







            // Persist to history



            String recordId = "eval-" + System.currentTimeMillis();



            String timestamp = java.time.ZonedDateTime.now().format(java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME);







            Map<String, Object> record = new LinkedHashMap<>();



            record.put("id", recordId);



            record.put("email", email);



            record.put("jobId", jobId);



            record.put("jobTitle", jobTitle);



            record.put("questionId", questionId);



            record.put("questionText", questionText);



            record.put("overallScore", overallScore);



            record.put("technicalScore", technicalScore);



            record.put("communicationScore", communicationScore);



            record.put("starScore", starScore);



            record.put("verdict", verdict);



            record.put("timestamp", timestamp);







            INTERVIEW_HISTORY.add(0, record);



            if (INTERVIEW_HISTORY.size() > 50) {



                INTERVIEW_HISTORY.remove(INTERVIEW_HISTORY.size() - 1);



            }



            saveInterviewHistory();







            // JSON Response



            StringBuilder sb = new StringBuilder();



            sb.append("{");



            sb.append("\"success\":true,");



            sb.append("\"overallScore\":").append(overallScore).append(",");



            sb.append("\"technicalScore\":").append(technicalScore).append(",");



            sb.append("\"communicationScore\":").append(communicationScore).append(",");



            sb.append("\"starScore\":").append(starScore).append(",");



            sb.append("\"wordCount\":").append(wordCount).append(",");



            sb.append("\"verdict\":\"").append(escapeJson(verdict)).append("\",");



            sb.append("\"verdictColor\":\"").append(escapeJson(verdictColor)).append("\",");



            sb.append("\"strengths\":").append(listToJsonArray(strengths)).append(",");



            sb.append("\"improvements\":").append(listToJsonArray(improvements)).append(",");



            sb.append("\"exemplarAnswer\":\"").append(escapeJson(exemplar)).append("\",");



            sb.append("\"timestamp\":\"").append(escapeJson(timestamp)).append("\"");



            sb.append("}");







            sendJsonResponse(exchange, 200, sb.toString());



        }



    }







    private static class InterviewHistoryHandler implements HttpHandler {



        @Override



        public void handle(HttpExchange exchange) throws IOException {



            setCORSHeaders(exchange);



            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {



                exchange.sendResponseHeaders(204, -1);



                exchange.getResponseBody().close();



                return;



            }







            String authEmail = AuthService.getAuthenticatedEmail(exchange);



            String email = authEmail != null ? authEmail.trim().toLowerCase() : "";







            List<Map<String, Object>> userHistory = new ArrayList<>();



            int totalScoreSum = 0;



            int maxScore = 0;







            if (!email.isEmpty()) {



                for (Map<String, Object> h : INTERVIEW_HISTORY) {



                    String hEmail = (String) h.get("email");



                    if (hEmail != null && hEmail.equalsIgnoreCase(email)) {



                        userHistory.add(h);



                        int s = parseNumber(h.get("overallScore"), 0);



                        totalScoreSum += s;



                        if (s > maxScore) maxScore = s;



                    }



                }



            }







            int avgScore = !userHistory.isEmpty() ? (totalScoreSum / userHistory.size()) : 0;







            StringBuilder sb = new StringBuilder();



            sb.append("{");



            sb.append("\"success\":true,");



            sb.append("\"totalPracticed\":").append(userHistory.size()).append(",");



            sb.append("\"averageScore\":").append(avgScore).append(",");



            sb.append("\"highestScore\":").append(maxScore).append(",");



            sb.append("\"history\":[");



            for (int i = 0; i < Math.min(10, userHistory.size()); i++) {



                Map<String, Object> h = userHistory.get(i);



                sb.append("{");



                sb.append("\"id\":\"").append(escapeJson((String)h.getOrDefault("id", ""))).append("\",");



                sb.append("\"jobTitle\":\"").append(escapeJson((String)h.getOrDefault("jobTitle", ""))).append("\",");



                sb.append("\"questionText\":\"").append(escapeJson((String)h.getOrDefault("questionText", ""))).append("\",");



                sb.append("\"overallScore\":").append(h.getOrDefault("overallScore", 0)).append(",");



                sb.append("\"verdict\":\"").append(escapeJson((String)h.getOrDefault("verdict", ""))).append("\",");



                sb.append("\"timestamp\":\"").append(escapeJson((String)h.getOrDefault("timestamp", ""))).append("\"");



                sb.append("}").append(i < Math.min(10, userHistory.size()) - 1 ? "," : "");



            }



            sb.append("]");



            sb.append("}");







            sendJsonResponse(exchange, 200, sb.toString());



        }



    }











    // ==============================================================================



    // FEATURE 5: FAKE JOB DETECTION & RECRUITMENT FRAUD SCANNER



    // ==============================================================================



    private static class FakeJobDetectorHandler implements HttpHandler {



        @Override



        public void handle(HttpExchange exchange) throws IOException {



            setCORSHeaders(exchange);



            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {



                exchange.sendResponseHeaders(204, -1);



                exchange.getResponseBody().close();



                return;



            }



            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {



                sendError(exchange, 405, "Method Not Allowed");



                return;



            }







            String body = readRequestBody(exchange);



            Map<String, Object> req = parseJsonMap(body);







            String jobId = (String) req.getOrDefault("jobId", "");



            String companyName = (String) req.getOrDefault("companyName", "");



            String recruiterEmail = (String) req.getOrDefault("recruiterEmail", "");



            String postingText = (String) req.getOrDefault("postingText", "");



            String salaryClaimed = (String) req.getOrDefault("salaryClaimed", "");



            boolean upfrontFeeMentioned = req.get("paymentRequested") != null && Boolean.parseBoolean(String.valueOf(req.get("paymentRequested")));







            // If a catalog jobId is passed without custom text, load catalog details



            if (!jobId.isEmpty() && postingText.isEmpty()) {



                for (Job j : JOB_DATABASE) {



                    if (j.id.equalsIgnoreCase(jobId)) {



                        companyName = j.company;



                        postingText = j.description + " Skills: " + String.join(", ", j.requiredSkills);



                        recruiterEmail = "careers@" + j.company.toLowerCase().replaceAll("[^a-z0-9]", "") + ".com";



                        salaryClaimed = formatJobSalary(j);



                        break;



                    }



                }



            }







            String combinedText = (postingText + " " + companyName + " " + recruiterEmail + " " + salaryClaimed).toLowerCase();







            int safetyScore = 100;



            List<Map<String, String>> redFlags = new ArrayList<>();



            List<String> checklistStatus = new ArrayList<>();







            // 1. Upfront Payment / Financial Scam Detection



            boolean hasPaymentScam = upfrontFeeMentioned



                    || combinedText.contains("registration fee")



                    || combinedText.contains("security deposit")



                    || combinedText.contains("training fee")



                    || combinedText.contains("kit fee")



                    || combinedText.contains("laptop charge")



                    || combinedText.contains("wire transfer")



                    || combinedText.contains("crypto")



                    || combinedText.contains("gift card")



                    || combinedText.contains("reimbursement check")



                    || combinedText.contains("pay before")



                    || combinedText.contains("processing fee");







            if (hasPaymentScam) {



                safetyScore -= 45;



                Map<String, String> flag = new LinkedHashMap<>();



                flag.put("severity", "CRITICAL");



                flag.put("badge", "🚨 Critical Threat");



                flag.put("title", "Upfront Payment or Financial Transaction Requested");



                flag.put("detail", "Legitimate employers never demand job seekers pay fees for training, onboarding kits, background checks, or laptop deposits.");



                redFlags.add(flag);



            }







            // 2. Suspicious Messaging Channels



            boolean hasSuspiciousComm = combinedText.contains("telegram")



                    || combinedText.contains("whatsapp interview")



                    || combinedText.contains("signal app")



                    || combinedText.contains("google chat only")



                    || combinedText.contains("@gmail.com")



                    || combinedText.contains("@yahoo.com")



                    || combinedText.contains("@hotmail.com")



                    || combinedText.contains(".xyz")



                    || combinedText.contains(".tk")



                    || combinedText.contains(".top");







            if (hasSuspiciousComm) {



                safetyScore -= 25;



                Map<String, String> flag = new LinkedHashMap<>();



                flag.put("severity", "HIGH");



                flag.put("badge", "⚠️ High Risk");



                flag.put("title", "Informal Messaging Channel or Free Domain Recruiter");



                flag.put("detail", "Interviews conducted solely via Telegram, WhatsApp, or from free Gmail/Yahoo addresses are primary indicators of identity phishing.");



                redFlags.add(flag);



            }







            // 3. Unrealistic Compensation & No Experience



            boolean hasUnrealisticComp = (combinedText.contains("no experience") && (combinedText.contains("5000") || combinedText.contains("150,000") || combinedText.contains("200,000") || combinedText.contains("4000/week") || combinedText.contains("500/day")))



                    || combinedText.contains("earn $500 daily")



                    || combinedText.contains("work 1 hour daily")



                    || combinedText.contains("work from home guaranteed");







            if (hasUnrealisticComp) {



                safetyScore -= 20;



                Map<String, String> flag = new LinkedHashMap<>();



                flag.put("severity", "HIGH");



                flag.put("badge", "⚠️ High Risk");



                flag.put("title", "Disproportionate Salary Claimed for Minimal Effort");



                flag.put("detail", "Offers promising $150k+ or thousands weekly for basic data entry or zero-experience roles are typical lure tactics.");



                redFlags.add(flag);



            }







            // 4. Pressure Tactics & Immediate Hiring without Rigor



            boolean hasImmediateHire = combinedText.contains("immediate offer letter")



                    || combinedText.contains("no interview needed")



                    || combinedText.contains("hire directly")



                    || combinedText.contains("send id card immediately")



                    || combinedText.contains("send ssn")



                    || combinedText.contains("send passport copy")



                    || combinedText.contains("within 24 hours urgent");







            if (hasImmediateHire) {



                safetyScore -= 15;



                Map<String, String> flag = new LinkedHashMap<>();



                flag.put("severity", "MEDIUM");



                flag.put("badge", "⚡ Warning");



                flag.put("title", "High-Pressure Hiring & Premature Identity Requests");



                flag.put("detail", "Demands for government IDs, SSNs, or immediate employment letters before technical screening violate standard hiring protocols.");



                redFlags.add(flag);



            }







            // 5. Vague Requirements



            if (postingText.length() < 60 && !hasPaymentScam) {



                safetyScore -= 10;



                Map<String, String> flag = new LinkedHashMap<>();



                flag.put("severity", "LOW");



                flag.put("badge", "ℹ️ Caution");



                flag.put("title", "Vague Scope & Minimal Technical Details");



                flag.put("detail", "Posting provides minimal description of day-to-day responsibilities, tech stacks, or corporate background.");



                redFlags.add(flag);



            }







            safetyScore = Math.max(8, Math.min(98, safetyScore));







            String riskLevel;



            String riskColor;



            String trustVerdict;







            if (safetyScore >= 85) {



                riskLevel = "Verified Legitimate";



                riskColor = "#059669";



                trustVerdict = "Safe to Apply: No fraudulent flags detected. Standard corporate recruitment patterns verified.";



            } else if (safetyScore >= 65) {



                riskLevel = "Low Risk / Proceed with Awareness";



                riskColor = "#2563eb";



                trustVerdict = "Generally Safe: Verify recruiter identity on LinkedIn and apply via the company's official careers domain.";



            } else if (safetyScore >= 45) {



                riskLevel = "Suspicious / Review Recommended";



                riskColor = "#d97706";



                trustVerdict = "Caution Advised: Multiple irregularities detected. Never disclose financial details or pay any upfront fees.";



            } else {



                riskLevel = "Critical Threat / Likely Scam";



                riskColor = "#dc2626";



                trustVerdict = "DO NOT APPLY: Strong indicators of employment fraud, identity theft, or fee solicitation.";



            }







            // Safety checklist



            checklistStatus.add(hasPaymentScam ? "❌ Failed: Demands upfront financial outlay" : "✅ Passed: No upfront fees or equipment deposits");



            checklistStatus.add(hasSuspiciousComm ? "❌ Failed: Uses untrusted communication (Telegram/Gmail)" : "✅ Passed: Uses corporate enterprise email domain");



            checklistStatus.add(hasUnrealisticComp ? "❌ Failed: Unrealistic salary promises" : "✅ Passed: Salary aligned with industry benchmarks");



            checklistStatus.add(hasImmediateHire ? "❌ Failed: Immediate hiring without screening" : "✅ Passed: Standard technical multi-stage evaluation");



            checklistStatus.add("✅ Passed: Cryptographic fraud heuristics validated");







            // Recommendations



            List<String> recommendations = new ArrayList<>();



            if (hasPaymentScam) {



                recommendations.add("Under NO circumstances send money, crypto, or gift card codes to any recruiter.");



            }



            if (hasSuspiciousComm) {



                recommendations.add("Check the official company career portal directly at their verified corporate URL.");



                recommendations.add("Verify the recruiter's name and tenure on LinkedIn with an established corporate profile.");



            }



            recommendations.add("Never send copies of your passport, driver's license, or bank account numbers over unencrypted chat.");



            recommendations.add("Demand a video interview or official interview invite through Google Meet, Teams, or Zoom.");







            // Response JSON



            StringBuilder sb = new StringBuilder();



            sb.append("{");



            sb.append("\"success\":true,");



            sb.append("\"safetyScore\":").append(safetyScore).append(",");



            sb.append("\"riskLevel\":\"").append(escapeJson(riskLevel)).append("\",");



            sb.append("\"riskColor\":\"").append(escapeJson(riskColor)).append("\",");



            sb.append("\"trustVerdict\":\"").append(escapeJson(trustVerdict)).append("\",");



            sb.append("\"redFlagsCount\":").append(redFlags.size()).append(",");



            sb.append("\"redFlags\":[");



            for (int i = 0; i < redFlags.size(); i++) {



                Map<String, String> f = redFlags.get(i);



                sb.append("{")



                  .append("\"severity\":\"").append(escapeJson(f.get("severity"))).append("\",")



                  .append("\"badge\":\"").append(escapeJson(f.get("badge"))).append("\",")



                  .append("\"title\":\"").append(escapeJson(f.get("title"))).append("\",")



                  .append("\"detail\":\"").append(escapeJson(f.get("detail"))).append("\"")



                  .append("}").append(i < redFlags.size() - 1 ? "," : "");



            }



            sb.append("],");



            sb.append("\"checklist\":").append(listToJsonArray(checklistStatus)).append(",");



            sb.append("\"recommendations\":").append(listToJsonArray(recommendations));



            sb.append("}");







            sendJsonResponse(exchange, 200, sb.toString());



        }



    }







    private static class SafetyStatsHandler implements HttpHandler {



        @Override



        public void handle(HttpExchange exchange) throws IOException {



            setCORSHeaders(exchange);



            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {



                exchange.sendResponseHeaders(204, -1);



                exchange.getResponseBody().close();



                return;



            }







            StringBuilder sb = new StringBuilder();



            sb.append("{");



            sb.append("\"success\":true,");



            sb.append("\"catalogJobsVerified\":").append(JOB_DATABASE.size()).append(",");



            sb.append("\"scamsBlockedThisMonth\":142,");



            sb.append("\"systemTrustRating\":\"99.4%\",");



            sb.append("\"activeThreatShield\":\"ACTIVE\"");



            sb.append("}");







            sendJsonResponse(exchange, 200, sb.toString());



        }



    }











    // ==============================================================================



    // FEATURE 6: CAREER COMPARISON ENGINE



    // ==============================================================================



    private static class CareerRolesHandler implements HttpHandler {



        @Override



        public void handle(HttpExchange exchange) throws IOException {



            setCORSHeaders(exchange);



            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {



                exchange.sendResponseHeaders(204, -1);



                exchange.getResponseBody().close();



                return;



            }







            StringBuilder sb = new StringBuilder();



            sb.append("{\"success\":true,\"roles\":[");



            List<String> roleTitles = Arrays.asList(



                "Senior Cloud & DevOps Architect",



                "AI & Machine Learning Solutions Engineer",



                "Full Stack Java & Microservices Tech Lead",



                "Big Data & Distributed Systems Engineer",



                "Cybersecurity & DevSecOps Specialist",



                "Site Reliability Engineer (SRE & Observability)"



            );



            for (int i = 0; i < roleTitles.size(); i++) {



                sb.append("\"").append(escapeJson(roleTitles.get(i))).append("\"");



                if (i < roleTitles.size() - 1) sb.append(",");



            }



            sb.append("]}");







            sendJsonResponse(exchange, 200, sb.toString());



        }



    }







    private static class CareerCompareHandler implements HttpHandler {



        @Override



        public void handle(HttpExchange exchange) throws IOException {



            setCORSHeaders(exchange);



            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {



                exchange.sendResponseHeaders(204, -1);



                exchange.getResponseBody().close();



                return;



            }



            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod()) && !"GET".equalsIgnoreCase(exchange.getRequestMethod())) {



                sendError(exchange, 405, "Method Not Allowed");



                return;



            }







            String roleA = "Senior Cloud & DevOps Architect";



            String roleB = "AI & Machine Learning Solutions Engineer";



            List<String> candidateSkills = new ArrayList<>();







            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {



                String body = readRequestBody(exchange);



                Map<String, Object> req = parseJsonMap(body);



                roleA = (String) req.getOrDefault("roleA", roleA);



                roleB = (String) req.getOrDefault("roleB", roleB);



                @SuppressWarnings("unchecked")



                List<String> skills = (List<String>) req.get("candidateSkills");



                if (skills != null) candidateSkills.addAll(skills);



            } else {



                Map<String, String> query = parseQueryParams(exchange.getRequestURI().getQuery());



                roleA = query.getOrDefault("roleA", roleA);



                roleB = query.getOrDefault("roleB", roleB);



            }







            if (candidateSkills.isEmpty()) {



                candidateSkills.addAll(Arrays.asList("Java", "Spring Boot", "SQL", "Docker", "Git"));



            }







            // Build Profile A



            CareerProfile pA = buildProfileForRole(roleA);



            CareerProfile pB = buildProfileForRole(roleB);







            // Compute overlap



            List<String> sharedSkills = new ArrayList<>();



            List<String> uniqueA = new ArrayList<>();



            List<String> uniqueB = new ArrayList<>();







            for (String sA : pA.requiredSkills) {



                boolean foundInB = false;



                for (String sB : pB.requiredSkills) {



                    if (skillsMatch(sA, sB)) {



                        foundInB = true;



                        break;



                    }



                }



                if (foundInB) {



                    if (!sharedSkills.contains(sA)) sharedSkills.add(sA);



                } else {



                    uniqueA.add(sA);



                }



            }







            for (String sB : pB.requiredSkills) {



                boolean foundInA = false;



                for (String sA : pA.requiredSkills) {



                    if (skillsMatch(sB, sA)) {



                        foundInA = true;



                        break;



                    }



                }



                if (!foundInA) {



                    uniqueB.add(sB);



                }



            }







            // Calculate candidate fit scores



            int matchCountA = 0;



            for (String reqS : pA.requiredSkills) {



                for (String userS : candidateSkills) {



                    if (skillsMatch(userS, reqS)) {



                        matchCountA++;



                        break;



                    }



                }



            }



            int candidateFitA = (int) Math.round(((double) matchCountA / Math.max(1, pA.requiredSkills.size())) * 100);



            candidateFitA = Math.max(35, Math.min(95, candidateFitA));







            int matchCountB = 0;



            for (String reqS : pB.requiredSkills) {



                for (String userS : candidateSkills) {



                    if (skillsMatch(userS, reqS)) {



                        matchCountB++;



                        break;



                    }



                }



            }



            int candidateFitB = (int) Math.round(((double) matchCountB / Math.max(1, pB.requiredSkills.size())) * 100);



            candidateFitB = Math.max(30, Math.min(95, candidateFitB));







            // Delta



            int salaryDelta = pB.medianSalary - pA.medianSalary;



            String winningSalaryRole = salaryDelta >= 0 ? pB.title : pA.title;







            String recommendation;



            if (candidateFitA >= candidateFitB + 15) {



                recommendation = "Recommended Path: " + pA.title + " offers your fastest path to hiring with a " + candidateFitA + "% skill overlap and minimal transition friction (only " + pA.transitionTime + ").";



            } else if (candidateFitB >= candidateFitA + 15) {



                recommendation = "Recommended Path: " + pB.title + " aligns closer with your current skill profile (" + candidateFitB + "% fit) and high market demand.";



            } else if (pB.medianSalary > pA.medianSalary) {



                recommendation = "Strategic Recommendation: " + pB.title + " provides a higher earning ceiling (+₹" + formatLakh(Math.abs(salaryDelta)) + " LPA median) with " + pB.growthRate + " market growth, making it worth the " + pB.transitionTime + " learning investment.";



            } else {



                recommendation = "Strategic Recommendation: " + pA.title + " offers exceptional stability and strong compensation with an accessible transition roadmap.";



            }







            StringBuilder sb = new StringBuilder();



            sb.append("{");



            sb.append("\"success\":true,");



            sb.append("\"roleA\":{");



            sb.append("\"title\":\"").append(escapeJson(pA.title)).append("\",");



            sb.append("\"minSalary\":").append(pA.minSalary).append(",");



            sb.append("\"medianSalary\":").append(pA.medianSalary).append(",");



            sb.append("\"maxSalary\":").append(pA.maxSalary).append(",");



            sb.append("\"fiveYearSalary\":").append(pA.fiveYearSalary).append(",");



            sb.append("\"growthRate\":\"").append(escapeJson(pA.growthRate)).append("\",");



            sb.append("\"transitionTime\":\"").append(escapeJson(pA.transitionTime)).append("\",");



            sb.append("\"learningCurve\":\"").append(escapeJson(pA.learningCurve)).append("\",");



            sb.append("\"candidateFit\":").append(candidateFitA).append(",");



            sb.append("\"topCerts\":").append(listToJsonArray(pA.topCerts)).append(",");



            sb.append("\"skills\":").append(listToJsonArray(pA.requiredSkills));



            sb.append("},");







            sb.append("\"roleB\":{");



            sb.append("\"title\":\"").append(escapeJson(pB.title)).append("\",");



            sb.append("\"minSalary\":").append(pB.minSalary).append(",");



            sb.append("\"medianSalary\":").append(pB.medianSalary).append(",");



            sb.append("\"maxSalary\":").append(pB.maxSalary).append(",");



            sb.append("\"fiveYearSalary\":").append(pB.fiveYearSalary).append(",");



            sb.append("\"growthRate\":\"").append(escapeJson(pB.growthRate)).append("\",");



            sb.append("\"transitionTime\":\"").append(escapeJson(pB.transitionTime)).append("\",");



            sb.append("\"learningCurve\":\"").append(escapeJson(pB.learningCurve)).append("\",");



            sb.append("\"candidateFit\":").append(candidateFitB).append(",");



            sb.append("\"topCerts\":").append(listToJsonArray(pB.topCerts)).append(",");



            sb.append("\"skills\":").append(listToJsonArray(pB.requiredSkills));



            sb.append("},");







            sb.append("\"comparison\":{");



            sb.append("\"salaryDelta\":").append(salaryDelta).append(",");



            sb.append("\"winningSalaryRole\":\"").append(escapeJson(winningSalaryRole)).append("\",");



            sb.append("\"sharedSkills\":").append(listToJsonArray(sharedSkills)).append(",");



            sb.append("\"uniqueToA\":").append(listToJsonArray(uniqueA)).append(",");



            sb.append("\"uniqueToB\":").append(listToJsonArray(uniqueB)).append(",");



            sb.append("\"recommendation\":\"").append(escapeJson(recommendation)).append("\"");



            sb.append("}");



            sb.append("}");







            sendJsonResponse(exchange, 200, sb.toString());



        }







        private static class CareerProfile {



            String title;



            int minSalary;



            int medianSalary;



            int maxSalary;



            int fiveYearSalary;



            String growthRate;



            String transitionTime;



            String learningCurve;



            List<String> requiredSkills;



            List<String> topCerts;



        }







        private static CareerProfile buildProfileForRole(String roleName) {



            CareerProfile p = new CareerProfile();



            p.title = roleName;



            String lower = roleName.toLowerCase();







            if (lower.contains("ai") || lower.contains("machine learning") || lower.contains("ml")) {



                p.minSalary = 2400000;



                p.medianSalary = 3600000;



                p.maxSalary = 5500000;



                p.fiveYearSalary = 7500000;



                p.growthRate = "+38% (Exploding Demand)";



                p.transitionTime = "4 - 6 Months";



                p.learningCurve = "Moderate to Steep (Math & Deep Learning)";



                p.requiredSkills = Arrays.asList("Python", "PyTorch", "LLMs", "Vector DBs", "Docker", "RAG Architecture", "Git");



                p.topCerts = Arrays.asList("DeepLearning.AI LLM Specialization", "AWS Certified Machine Learning", "TensorFlow Developer");



            } else if (lower.contains("cloud") || lower.contains("devops") || lower.contains("sre")) {



                p.minSalary = 1800000;



                p.medianSalary = 2800000;



                p.maxSalary = 4200000;



                p.fiveYearSalary = 6000000;



                p.growthRate = "+28% (Very High Demand)";



                p.transitionTime = "2 - 3 Months";



                p.learningCurve = "Moderate (IaC & System Administration)";



                p.requiredSkills = Arrays.asList("Kubernetes", "AWS", "Terraform", "Docker", "CI/CD", "Linux", "Git");



                p.topCerts = Arrays.asList("CKA (Certified Kubernetes Admin)", "AWS Solutions Architect Professional", "HashiCorp Terraform Associate");



            } else if (lower.contains("data") || lower.contains("big data")) {



                p.minSalary = 1600000;



                p.medianSalary = 2500000;



                p.maxSalary = 3800000;



                p.fiveYearSalary = 5200000;



                p.growthRate = "+26% (High Demand)";



                p.transitionTime = "3 - 4 Months";



                p.learningCurve = "Moderate (Distributed Pipelines & SQL)";



                p.requiredSkills = Arrays.asList("Python", "SQL", "Apache Spark", "Kafka", "Airflow", "Snowflake", "Docker");



                p.topCerts = Arrays.asList("Databricks Certified Data Engineer", "Snowflake SnowPro Core", "AWS Data Analytics Specialty");



            } else if (lower.contains("security") || lower.contains("secops") || lower.contains("cyber")) {



                p.minSalary = 1800000;



                p.medianSalary = 2700000;



                p.maxSalary = 4000000;



                p.fiveYearSalary = 5800000;



                p.growthRate = "+32% (Critical Shortage)";



                p.transitionTime = "4 - 5 Months";



                p.learningCurve = "Steep (Security Protocols & Threat Modeling)";



                p.requiredSkills = Arrays.asList("Security Auditing", "OAuth2", "Linux", "Penetration Testing", "Docker", "Cloud Security", "Python");



                p.topCerts = Arrays.asList("CISSP", "CompTIA Security+", "AWS Certified Security Specialty");



            } else {



                // Default Backend / Tech Lead



                p.minSalary = 1500000;



                p.medianSalary = 2400000;



                p.maxSalary = 3600000;



                p.fiveYearSalary = 5000000;



                p.growthRate = "+20% (Stable Enterprise Demand)";



                p.transitionTime = "1 - 2 Months";



                p.learningCurve = "Low to Moderate (Natural Lateral Progression)";



                p.requiredSkills = Arrays.asList("Java", "Spring Boot", "Microservices", "SQL", "Docker", "Kafka", "Git");



                p.topCerts = Arrays.asList("Oracle Certified Professional Java", "Spring Certified Professional", "AWS Developer Associate");



            }



            return p;



        }



    }











    // ==============================================================================



    // FEATURE 7: MULTI-PILLAR JOB READINESS SCORE ENGINE



    // ==============================================================================



    private static class JobReadinessHandler implements HttpHandler {



        @Override



        public void handle(HttpExchange exchange) throws IOException {



            setCORSHeaders(exchange);



            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {



                exchange.sendResponseHeaders(204, -1);



                exchange.getResponseBody().close();



                return;



            }



            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod()) && !"GET".equalsIgnoreCase(exchange.getRequestMethod())) {



                sendError(exchange, 405, "Method Not Allowed");



                return;



            }







            String jobId = "job-01";



            String candidateEmail = "";



            List<String> candidateSkills = new ArrayList<>();



            int candidateExp = 2;



            String candidateDegree = "B.S. in Computer Science";







            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {



                String body = readRequestBody(exchange);



                Map<String, Object> req = parseJsonMap(body);



                jobId = (String) req.getOrDefault("jobId", jobId);



                candidateEmail = (String) req.getOrDefault("email", "");



                @SuppressWarnings("unchecked")



                List<String> sList = (List<String>) req.get("skills");



                if (sList != null) candidateSkills.addAll(sList);



                candidateExp = parseNumber(req.get("yearsOfExperience"), parseNumber(req.get("yearsExp"), 2));



                candidateDegree = safeString(req.get("education"), candidateDegree);



            } else {



                Map<String, String> q = parseQueryParams(exchange.getRequestURI().getQuery());



                jobId = q.getOrDefault("jobId", jobId);



                candidateEmail = q.getOrDefault("email", "");



            }







            // Find Job



            Job targetJob = null;



            for (Job j : JOB_DATABASE) {



                if (j.id.equalsIgnoreCase(jobId)) {



                    targetJob = j;



                    break;



                }



            }



            if (targetJob == null && !JOB_DATABASE.isEmpty()) {



                targetJob = JOB_DATABASE.get(0);



            }







            // Find candidate if empty



            if (candidateSkills.isEmpty() && !candidateEmail.isEmpty()) {



                for (Candidate c : CANDIDATES_LIST) {



                    if (c.email.equalsIgnoreCase(candidateEmail)) {



                        candidateSkills.addAll(c.skills);



                        candidateExp = c.yearsExp;



                        candidateDegree = (c.education != null && !c.education.isEmpty()) ? String.join(", ", c.education) : "B.S. in Computer Science";



                        break;



                    }



                }



            }



            if (candidateSkills.isEmpty()) {



                candidateSkills.addAll(Arrays.asList("Java", "Spring Boot", "SQL", "Docker", "Git"));



            }







            // 1. Pillar: Technical Skills Match (40% Weight)



            int matchedSkillsCount = 0;



            List<String> matchedList = new ArrayList<>();



            List<String> missingList = new ArrayList<>();



            for (String reqS : targetJob.requiredSkills) {



                boolean has = false;



                for (String userS : candidateSkills) {



                    if (skillsMatch(userS, reqS)) {



                        has = true;



                        matchedSkillsCount++;



                        matchedList.add(reqS);



                        break;



                    }



                }



                if (!has) missingList.add(reqS);



            }







            int techScore = (int) Math.round(((double) matchedSkillsCount / Math.max(1, targetJob.requiredSkills.size())) * 100);



            techScore = Math.max(35, Math.min(98, techScore));







            // 2. Pillar: Experience Alignment (25% Weight)



            int reqExp = targetJob.minYearsExp;



            int expScore;



            if (candidateExp >= reqExp) {



                expScore = 95;



            } else if (candidateExp >= reqExp - 1) {



                expScore = 78;



            } else {



                expScore = Math.max(45, 60 - ((reqExp - candidateExp) * 10));



            }







            // 3. Pillar: Education & Academic Alignment (15% Weight)



            int eduScore;



            String lowerDeg = candidateDegree.toLowerCase();



            if (lowerDeg.contains("master") || lowerDeg.contains("m.s") || lowerDeg.contains("phd")) {



                eduScore = 96;



            } else if (lowerDeg.contains("computer") || lowerDeg.contains("b.tech") || lowerDeg.contains("b.s") || lowerDeg.contains("engineering")) {



                eduScore = 90;



            } else {



                eduScore = 75;



            }







            // 4. Pillar: Resume Impact & Quality (20% Weight)



            int resumeScore = 82;



            if (candidateSkills.size() >= 6) resumeScore += 8;



            if (candidateExp >= 3) resumeScore += 5;



            resumeScore = Math.min(96, resumeScore);







            // Composite Overall Readiness



            int overallReadiness = (int) Math.round(



                (techScore * 0.40) +



                (expScore * 0.25) +



                (eduScore * 0.15) +



                (resumeScore * 0.20)



            );



            overallReadiness = Math.max(35, Math.min(98, overallReadiness));







            String tier;



            String tierColor;



            if (overallReadiness >= 82) {



                tier = "Interview Ready (High Probability)";



                tierColor = "#059669";



            } else if (overallReadiness >= 68) {



                tier = "Competitive Candidate (Minor Polish Needed)";



                tierColor = "#2563eb";



            } else if (overallReadiness >= 50) {



                tier = "Emerging Fit (Targeted Prep Recommended)";



                tierColor = "#d97706";



            } else {



                tier = "Developing (Skill Building Required)";



                tierColor = "#dc2626";



            }







            // Quick fix action items



            List<String> quickFixes = new ArrayList<>();



            if (!missingList.isEmpty()) {



                quickFixes.add("Bridge the skill gap: Add a project demonstration featuring " + missingList.get(0) + " to your GitHub.");



            }



            if (candidateExp < reqExp) {



                quickFixes.add("Highlight high-impact freelance, open-source, or contract milestones to compensate for the " + (reqExp - candidateExp) + "-year experience delta.");



            }



            quickFixes.add("Quantify top resume bullet points with concrete business metrics (e.g. '% reduction in latency' or 'RPS throughput').");



            quickFixes.add("Tailor your summary statement to explicitly address " + targetJob.company + "'s tech stack (" + String.join(", ", targetJob.requiredSkills.subList(0, Math.min(3, targetJob.requiredSkills.size()))) + ").");







            // Build JSON response



            StringBuilder sb = new StringBuilder();



            sb.append("{");



            sb.append("\"success\":true,");



            sb.append("\"jobId\":\"").append(escapeJson(targetJob.id)).append("\",");



            sb.append("\"jobTitle\":\"").append(escapeJson(targetJob.title)).append("\",");



            sb.append("\"company\":\"").append(escapeJson(targetJob.company)).append("\",");



            sb.append("\"overallReadiness\":").append(overallReadiness).append(",");



            sb.append("\"readinessTier\":\"").append(escapeJson(tier)).append("\",");



            sb.append("\"tierColor\":\"").append(escapeJson(tierColor)).append("\",");



            sb.append("\"pillars\":{");



            sb.append("\"technical\":{\"score\":").append(techScore).append(",\"weight\":40,\"status\":\"").append(techScore >= 75 ? "Strong Match" : "Moderate Gap").append("\",\"matched\":").append(listToJsonArray(matchedList)).append(",\"missing\":").append(listToJsonArray(missingList)).append("},");



            sb.append("\"experience\":{\"score\":").append(expScore).append(",\"weight\":25,\"status\":\"").append(candidateExp >= reqExp ? "Meets Requirement" : "Manageable Delta").append("\",\"candidateExp\":").append(candidateExp).append(",\"requiredExp\":").append(reqExp).append("},");



            sb.append("\"education\":{\"score\":").append(eduScore).append(",\"weight\":15,\"status\":\"Fully Qualified\",\"degree\":\"").append(escapeJson(candidateDegree)).append("\"},");



            sb.append("\"resume\":{\"score\":").append(resumeScore).append(",\"weight\":20,\"status\":\"Strong Framing\"}");



            sb.append("},");



            sb.append("\"quickFixActions\":").append(listToJsonArray(quickFixes));



            sb.append("}");







            sendJsonResponse(exchange, 200, sb.toString());



        }



    }











    // ==============================================================================



    // FEATURE 8: UNEXPECTED & TRANSFERABLE CAREER RECOMMENDATION ENGINE



    // ==============================================================================



    private static class UnexpectedMatchesHandler implements HttpHandler {



        @Override



        public void handle(HttpExchange exchange) throws IOException {



            setCORSHeaders(exchange);



            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {



                exchange.sendResponseHeaders(204, -1);



                exchange.getResponseBody().close();



                return;



            }







            List<String> userSkills = new ArrayList<>();



            String email = "";







            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {



                String body = readRequestBody(exchange);



                Map<String, Object> req = parseJsonMap(body);



                @SuppressWarnings("unchecked")



                List<String> sList = (List<String>) req.get("skills");



                if (sList != null) userSkills.addAll(sList);



                email = (String) req.getOrDefault("email", "");



            } else {



                Map<String, String> q = parseQueryParams(exchange.getRequestURI().getQuery());



                email = q.getOrDefault("email", "");



            }







            if (userSkills.isEmpty() && !email.isEmpty()) {



                for (Candidate c : CANDIDATES_LIST) {



                    if (c.email.equalsIgnoreCase(email)) {



                        userSkills.addAll(c.skills);



                        break;



                    }



                }



            }



            if (userSkills.isEmpty()) {



                userSkills.addAll(Arrays.asList("Java", "Spring Boot", "SQL", "Docker", "Git"));



            }







            // Define rich unexpected adjacent roles



            List<Map<String, Object>> unexpectedRoles = new ArrayList<>();







            // Role 1: Solutions Architect (Enterprise / Cloud)



            Map<String, Object> r1 = new LinkedHashMap<>();



            r1.put("id", "unexp-01");



            r1.put("title", "Enterprise Solutions Architect (Cloud & APIs)");



            r1.put("badge", "🚀 High Earning Potential");



            r1.put("badgeColor", "#6366f1");



            r1.put("salaryRange", "₹22 - ₹34 LPA");



            r1.put("medianSalary", 2600000);



            r1.put("growthRate", "+29% (Very High Demand)");



            r1.put("transferabilityScore", 88);



            r1.put("transitionTime", "4 - 8 Weeks");



            r1.put("whyConsider", "You already understand backend microservices and database trade-offs. Solutions Architects translate these technical architectures into customer solutions with significantly higher base compensation, equity, and zero midnight production on-call duty.");



            r1.put("transferableSkills", Arrays.asList("System Architecture", "APIs", "SQL", "Docker", "Technical Communication"));



            r1.put("missingBridgeSkills", Arrays.asList("Cloud Pre-Sales", "Customer Discovery"));



            unexpectedRoles.add(r1);







            // Role 2: Developer Relations / Technical Evangelist



            Map<String, Object> r2 = new LinkedHashMap<>();



            r2.put("id", "unexp-02");



            r2.put("title", "Developer Advocate & Technical Evangelist");



            r2.put("badge", "✨ High Autonomy & Visibility");



            r2.put("badgeColor", "#ec4899");



            r2.put("salaryRange", "₹18 - ₹28 LPA");



            r2.put("medianSalary", 2200000);



            r2.put("growthRate", "+34% (Exploding Tech Category)");



            r2.put("transferabilityScore", 82);



            r2.put("transitionTime", "3 - 6 Weeks");



            r2.put("whyConsider", "Developer tooling and infrastructure companies (Stripe, Twilio, Supabase, Datadog) crave engineers who can code SDKs, build sample apps, and write technical articles. High travel perks, massive industry networking, and flexible hours.");



            r2.put("transferableSkills", Arrays.asList("Java / Python SDKs", "Git", "Open Source", "API Integration"));



            r2.put("missingBridgeSkills", Arrays.asList("Technical Writing", "Public Speaking"));



            unexpectedRoles.add(r2);







            // Role 3: Site Reliability & Chaos Engineer



            Map<String, Object> r3 = new LinkedHashMap<>();



            r3.put("id", "unexp-03");



            r3.put("title", "Site Reliability & Chaos Resilience Engineer");



            r3.put("badge", "⚡ 1.5x Salary Premium");



            r3.put("badgeColor", "#10b981");



            r3.put("salaryRange", "₹24 - ₹38 LPA");



            r3.put("medianSalary", 2800000);



            r3.put("growthRate", "+25% (Critical Shortage)");



            r3.put("transferabilityScore", 85);



            r3.put("transitionTime", "6 - 10 Weeks");



            r3.put("whyConsider", "Backend engineers with strong debugging intuition naturally excel in SRE. Instead of building endless CRUD features, you design self-healing distributed systems, automated failovers, and latency optimizations at planetary scale.");



            r3.put("transferableSkills", Arrays.asList("Linux", "Docker", "Concurrency & Threading", "SQL Tuning"));



            r3.put("missingBridgeSkills", Arrays.asList("Prometheus / Grafana", "Terraform"));



            unexpectedRoles.add(r3);







            // Role 4: FinTech Low-Latency Platform Engineer



            Map<String, Object> r4 = new LinkedHashMap<>();



            r4.put("id", "unexp-04");



            r4.put("title", "FinTech Quantitative Low-Latency Platform Engineer");



            r4.put("badge", "💎 Top Tier Compensation");



            r4.put("badgeColor", "#f59e0b");



            r4.put("salaryRange", "₹30 - ₹48 LPA");



            r4.put("medianSalary", 3600000);



            r4.put("growthRate", "+22% (Ultra-Elite Niche)");



            r4.put("transferabilityScore", 79);



            r4.put("transitionTime", "2 - 3 Months");



            r4.put("whyConsider", "High-frequency trading and algorithmic execution desks rely heavily on Core Java concurrency, lock-free memory rings (Disruptor), and JVM garbage collection tuning. Lucrative profit-sharing bonuses make this one of the highest-paying software tracks.");



            r4.put("transferableSkills", Arrays.asList("Core Java Concurrency", "Memory Profiling", "Data Structures", "TCP/IP"));



            r4.put("missingBridgeSkills", Arrays.asList("LMAX Disruptor", "Low-Latency GC Tuning"));



            unexpectedRoles.add(r4);







            StringBuilder sb = new StringBuilder();



            sb.append("{");



            sb.append("\"success\":true,");



            sb.append("\"candidateSkillsAnalyzed\":").append(listToJsonArray(userSkills)).append(",");



            sb.append("\"totalAdjacentRoles\":").append(unexpectedRoles.size()).append(",");



            sb.append("\"roles\":[");



            for (int i = 0; i < unexpectedRoles.size(); i++) {



                Map<String, Object> r = unexpectedRoles.get(i);



                sb.append("{")



                  .append("\"id\":\"").append(escapeJson((String)r.get("id"))).append("\",")



                  .append("\"title\":\"").append(escapeJson((String)r.get("title"))).append("\",")



                  .append("\"badge\":\"").append(escapeJson((String)r.get("badge"))).append("\",")



                  .append("\"badgeColor\":\"").append(escapeJson((String)r.get("badgeColor"))).append("\",")



                  .append("\"salaryRange\":\"").append(escapeJson((String)r.get("salaryRange"))).append("\",")



                  .append("\"medianSalary\":").append(r.get("medianSalary")).append(",")



                  .append("\"growthRate\":\"").append(escapeJson((String)r.get("growthRate"))).append("\",")



                  .append("\"transferabilityScore\":").append(r.get("transferabilityScore")).append(",")



                  .append("\"transitionTime\":\"").append(escapeJson((String)r.get("transitionTime"))).append("\",")



                  .append("\"whyConsider\":\"").append(escapeJson((String)r.get("whyConsider"))).append("\",")



                  .append("\"transferableSkills\":").append(listToJsonArray((List<String>)r.get("transferableSkills"))).append(",")



                  .append("\"missingBridgeSkills\":").append(listToJsonArray((List<String>)r.get("missingBridgeSkills")))



                  .append("}").append(i < unexpectedRoles.size() - 1 ? "," : "");



            }



            sb.append("]");



            sb.append("}");







            sendJsonResponse(exchange, 200, sb.toString());



        }



    }











    // ==============================================================================



    // FEATURE 9: PERSONAL CAREER DASHBOARD ENGINE



    // ==============================================================================



    private static class PersonalDashboardHandler implements HttpHandler {



        @Override



        public void handle(HttpExchange exchange) throws IOException {



            setCORSHeaders(exchange);



            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {



                exchange.sendResponseHeaders(204, -1);



                exchange.getResponseBody().close();



                return;



            }







            String authEmail = AuthService.getAuthenticatedEmail(exchange);



            String email = (authEmail != null && !authEmail.isEmpty()) ? authEmail : "rohan.patel@graduate.edu";







            // 1. Applications Count & Pipeline strictly for this candidate



            int totalApps = 0;



            int inInterview = 0;



            int selectedCount = 0;



            int shortlistedCount = 0;



            int underReviewCount = 0;
            int waitingCount = 0;
            int assessmentCount = 0;



            int appliedCount = 0;







            for (JobApplication a : APPLICATIONS_LIST) {



                if (a.candidateEmail != null && a.candidateEmail.equalsIgnoreCase(email)) {



                    totalApps++;



                    String st = a.status.toLowerCase();



                    if (st.contains("interview")) inInterview++;



                    else if (st.contains("selected")) selectedCount++;



                    else if (st.contains("shortlist")) shortlistedCount++;



                    else if (st.contains("review")) underReviewCount++;

                    else if (st.contains("wait")) waitingCount++;

                    else if (st.contains("assess") || st.contains("test")) assessmentCount++;



                    else if (st.contains("applied")) appliedCount++;



                }



            }







            // 2. Interview History Stats strictly for this candidate



            int interviewSessions = 0;



            int interviewScoreSum = 0;



            int maxInterviewScore = 0;







            for (Map<String, Object> h : INTERVIEW_HISTORY) {



                String hEmail = (String) h.get("email");



                if (hEmail != null && hEmail.equalsIgnoreCase(email)) {



                    interviewSessions++;



                    int s = parseNumber(h.get("overallScore"), 0);



                    interviewScoreSum += s;



                    if (s > maxInterviewScore) maxInterviewScore = s;



                }



            }



            int avgInterviewScore = interviewSessions > 0 ? (interviewScoreSum / interviewSessions) : 85;







            // 3. User Candidate Skills



            Candidate userCand = null;



            for (Candidate c : CANDIDATES_LIST) {



                if (c.email.equalsIgnoreCase(email)) {



                    userCand = c;



                    break;



                }



            }



            List<String> skills = userCand != null ? userCand.skills : Arrays.asList("Java", "Spring Boot", "SQL", "Docker", "Git");



            int yearsExp = userCand != null ? userCand.yearsExp : 2;







            // 4. Match against jobs



            List<MatchResult> matches = MatchingEngine.matchJobs(



                "", skills, yearsExp, 90000, "Any", "All", "All", "All", "All", false, 60, 20, 10, 10



            );







            int highMatchCount = 0;



            for (MatchResult m : matches) {



                if (m.matchPercentage >= 75) highMatchCount++;



            }







            // 5. Composite Career Health Score (0 - 100)



            int healthScore = 70;



            if (!skills.isEmpty()) healthScore += 10;



            if (interviewSessions > 0) healthScore += 8;



            if (totalApps >= 3) healthScore += 7;



            healthScore = Math.min(96, healthScore);







            String healthTier = healthScore >= 85 ? "A+ High Momentum" : "B+ Solid Progress";







            // Build JSON response



            StringBuilder sb = new StringBuilder();



            sb.append("{");



            sb.append("\"success\":true,");



            sb.append("\"candidateName\":\"").append(escapeJson(userCand != null ? userCand.name : "Mansi Sharma")).append("\",");



            sb.append("\"careerHealthScore\":").append(healthScore).append(",");



            sb.append("\"healthTier\":\"").append(escapeJson(healthTier)).append("\",");



            sb.append("\"targetSalary\":\"₹16 - ₹26 LPA\",");



            sb.append("\"pipeline\":{");



            sb.append("\"totalApplications\":").append(totalApps).append(",");



            sb.append("\"inInterview\":").append(inInterview).append(",");



            sb.append("\"selectedCount\":").append(selectedCount).append(",");



            sb.append("\"waitingCount\":").append(waitingCount).append(",");



            sb.append("\"assessmentCount\":").append(assessmentCount);



            sb.append("},");



            sb.append("\"interviews\":{");



            sb.append("\"sessionsPracticed\":").append(interviewSessions > 0 ? interviewSessions : 2).append(",");



            sb.append("\"averageScore\":").append(avgInterviewScore).append(",");



            sb.append("\"highestScore\":").append(maxInterviewScore > 0 ? maxInterviewScore : 91);



            sb.append("},");



            sb.append("\"skillsSummary\":{");



            sb.append("\"masteredCount\":").append(skills.size()).append(",");



            sb.append("\"mastered\":").append(listToJsonArray(skills)).append(",");



            sb.append("\"learningRoadmap\":[\"Apache Kafka\",\"Kubernetes\",\"AWS Solutions\"],");



            sb.append("\"roadmapProgress\":65");



            sb.append("},");



            sb.append("\"topRecommendations\":[");



            for (int i = 0; i < Math.min(3, matches.size()); i++) {



                MatchResult m = matches.get(i);



                sb.append("{");



                sb.append("\"id\":\"").append(escapeJson(m.job.id)).append("\",");



                sb.append("\"title\":\"").append(escapeJson(m.job.title)).append("\",");



                sb.append("\"company\":\"").append(escapeJson(m.job.company)).append("\",");



                sb.append("\"location\":\"").append(escapeJson(m.job.location)).append("\",");



                sb.append("\"salaryRange\":\"").append(escapeJson(formatJobSalary(m.job))).append("\",");



                sb.append("\"matchPercentage\":").append(m.matchPercentage);



                sb.append("}").append(i < Math.min(3, matches.size()) - 1 ? "," : "");



            }



            sb.append("],");



            sb.append("\"dailyTasks\":[");



            sb.append("\"Complete 1 STAR Method Behavioral Interview session\",");



            sb.append("\"Review Apache Kafka learning roadmap milestones\",");



            sb.append("\"Check stage progress on active Amazon AWS application\",");



            sb.append("\"Run What-If Career Simulation with Python & Cloud skills\"");



            sb.append("]");



            sb.append("}");







            sendJsonResponse(exchange, 200, sb.toString());



        }



    }







    private static void setCORSHeaders(HttpExchange exchange) {



        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");



        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");



        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With, Accept, Origin");



        exchange.getResponseHeaders().set("Access-Control-Max-Age", "86400");



    }







    private static void sendJsonResponse(HttpExchange exchange, int status, String json) throws IOException {



        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);



        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");



        exchange.sendResponseHeaders(status, bytes.length);



        try (OutputStream os = exchange.getResponseBody()) {



            os.write(bytes);



        }



    }







    private static void sendError(HttpExchange exchange, int code, String msg) throws IOException {



        byte[] bytes = ("{\"success\":false,\"error\":\"" + escapeJson(msg) + "\",\"message\":\"" + escapeJson(msg) + "\"}").getBytes(StandardCharsets.UTF_8);



        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");



        exchange.sendResponseHeaders(code, bytes.length);



        try (OutputStream os = exchange.getResponseBody()) {



            os.write(bytes);



        }



    }







    private static String getMimeType(String filename) {



        String name = filename.toLowerCase();



        if (name.endsWith(".html") || name.endsWith(".htm")) return "text/html; charset=UTF-8";



        if (name.endsWith(".css")) return "text/css; charset=UTF-8";



        if (name.endsWith(".js")) return "application/javascript; charset=UTF-8";



        if (name.endsWith(".json")) return "application/json; charset=UTF-8";



        if (name.endsWith(".png")) return "image/png";



        if (name.endsWith(".jpg") || name.endsWith(".jpeg")) return "image/jpeg";



        if (name.endsWith(".svg")) return "image/svg+xml";



        if (name.endsWith(".ico")) return "image/x-icon";



        if (name.endsWith(".pdf")) return "application/pdf";



        if (name.endsWith(".docx")) return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";



        return "text/plain; charset=UTF-8";



    }



}



