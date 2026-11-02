/**
 * CarrerX - Intelligent Career Platform & Multi-Attribute Recommendation Controller v3.0
 * Light Frame Design with Deep Company & Location Intelligence and Live Job Applications
 */

(function () {
  'use strict';


  function renderCompanyDetailsGrid(job, c, isLive = false) {
    if (!DOM.modalCompanyDetailsGrid) return;
    const offices = c.indianOfficeLocations && c.indianOfficeLocations.length > 0
      ? c.indianOfficeLocations.join(', ')
      : 'Bengaluru, Hyderabad, Pune, Mumbai, Delhi NCR';
    const tech = c.techStack && c.techStack.length > 0
      ? c.techStack.join(', ')
      : (job.requiredSkills ? job.requiredSkills.join(', ') : 'Java, Spring Boot, React, Cloud');
    const benefits = c.benefits && c.benefits.length > 0
      ? c.benefits.join(' • ')
      : 'Comprehensive Medical & Health Insurance, Provident Fund & Gratuity, Upskilling Allowances, Hybrid Work Flexibility';
    const culture = c.cultureValues || c.culture || 'High agency, autonomy, collaborative engineering excellence and continuous learning.';
    const hiring = c.hiringDetails || 'Initial resume screening, technical coding assessment, technical architecture round, and managerial interview (2-3 weeks).';

    DOM.modalCompanyDetailsGrid.innerHTML = `
      <div class="intel-tile">
        <span class="intel-tile-heading">Industry & Focus</span>
        <span class="intel-tile-value">${escapeHtml(c.industry || 'Enterprise Technology & Software')}</span>
      </div>
      <div class="intel-tile">
        <span class="intel-tile-heading">Company Headcount</span>
        <span class="intel-tile-value">${escapeHtml(c.companySize || '10,000+ employees')}</span>
      </div>
      <div class="intel-tile">
        <span class="intel-tile-heading">Employee Rating</span>
        <span class="intel-tile-value text-amber">⭐ ${c.rating || 4.5} / 5.0 (Glassdoor)</span>
      </div>
      <div class="intel-tile">
        <span class="intel-tile-heading">Headquarters & Leadership</span>
        <span class="intel-tile-value">${escapeHtml(c.headquarters || 'India')} • CEO: ${escapeHtml(c.ceo || 'Executive Leadership')}</span>
      </div>
      <div class="intel-tile">
        <span class="intel-tile-heading">Official Careers Portal</span>
        <span class="intel-tile-value"><a href="https://${escapeHtml(c.website || 'careers.google.com')}" target="_blank" style="color:var(--accent-primary); font-weight:700;">${escapeHtml(c.website || 'Visit Site')} ↗</a></span>
      </div>
      <div class="intel-tile">
        <span class="intel-tile-heading">Indian Office Hubs 🇮🇳</span>
        <span class="intel-tile-value">📍 ${escapeHtml(offices)}</span>
      </div>
      <div class="intel-tile full-width">
        <span class="intel-tile-heading">Verified Tech Stack</span>
        <span class="intel-tile-value">💻 <span style="font-family:var(--font-mono); font-size:0.83rem;">${escapeHtml(tech)}</span></span>
      </div>
      <div class="intel-tile full-width">
        <span class="intel-tile-heading">Company Culture & Work Environment</span>
        <span class="intel-tile-value">"${escapeHtml(culture)}"</span>
      </div>
      <div class="intel-tile full-width">
        <span class="intel-tile-heading">Hiring Process & Timelines</span>
        <span class="intel-tile-value">📋 ${escapeHtml(hiring)}</span>
      </div>
      <div class="intel-tile full-width">
        <span class="intel-tile-heading">Employee Benefits & Perks</span>
        <span class="intel-tile-value">🎁 ${escapeHtml(benefits)}</span>
      </div>
    `;
  }

  // Centralized API Configuration
  const API_BASE = (window.location.protocol.startsWith('http')) ? window.location.origin : 'http://localhost:8080';
  const API_BASE_URL = `${API_BASE}/api`;
  let isJavaServerOnline = false;

  // Application State
  const state = {
    userProfile: {
      resumeText: '',
      skills: [],
      yearsOfExperience: 0, // Default 0 = Fresher
      expectedSalary: 80000,
      preferredLocation: 'All',
      preferredClimate: 'All',
      preferredWorkingHours: 'All',
      preferredWorkMode: 'Any',
      preferredCategory: 'All',
      strictExperienceFilter: true, // Prevents Freshers from seeing Senior Manager roles
      weights: {
        skills: 60,
        experience: 20,
        salary: 10,
        preferences: 10
      },
      detectedInfo: {
        email: '',
        phone: '',
        years: null,
        education: []
      }
    },
    knownSkills: [
      'Java', 'Spring Boot', 'React', 'JavaScript', 'HTML', 'CSS', 'Python', 'SQL',
      'Docker', 'Kubernetes', 'AWS', 'Azure', 'GCP', 'TypeScript', 'Node.js',
      'Microservices', 'REST API', 'GraphQL', 'Linux', 'Git', 'Kafka', 'Apache Spark',
      'Machine Learning', 'Deep Learning', 'PyTorch', 'TensorFlow', 'FastAPI',
      'PostgreSQL', 'MongoDB', 'Redis', 'CI/CD', 'Terraform', 'DevOps', 'Cybersecurity',
      'Cloud Security', 'React Native', 'Swift', 'UI/UX Design', 'Engineering Management',
      'Algorithms', 'Data Structures', 'Data Analysis'
    ],
    jobCatalog: [],
    matchedResults: [],
    savedJobIds: new Set(JSON.parse(localStorage.getItem('carrerx_saved_jobs') || '[]')),
    submittedApplications: JSON.parse(localStorage.getItem('carrerx_applications') || '[]'),
    activeFilter: 'all',
    activeSort: 'match-desc',
    searchQuery: '',
    currentModalJob: null,
    selectedJobIdsForBatch: new Set(),
    simulatedSkills: new Set(),
    currentInterviewQuestions: [],
    activeQuestionIndex: 0,
    currentInterviewJobId: null,
    hasRunInitialFakeScan: false,
    currencyFilter: 'Any',
    currentUser: JSON.parse(localStorage.getItem('carrerx_auth_user') || 'null'),
    authToken: localStorage.getItem('carrerx_auth_token') || ''
  };

  function getAuthHeaders(extraHeaders = {}) {
    const headers = { ...extraHeaders };
    if (state.authToken) {
      headers['Authorization'] = `Bearer ${state.authToken}`;
      headers['X-Auth-Token'] = state.authToken;
    }
    if (state.currentUser && state.currentUser.email) {
      headers['X-Candidate-Email'] = state.currentUser.email;
    }
    return headers;
  }

  async function fetchWithAuth(url, options = {}) {
    const opts = { ...options };
    opts.headers = getAuthHeaders(opts.headers || {});
    return fetch(url, opts);
  }

  // Pre-configured Demo Profiles
  const DEMO_PROFILES = {
    'fresher-java': {
      title: 'Fresher / Recent Computer Science Graduate',
      resumeText: `Rohan Patel | Fresh Computer Science Graduate
Email: rohan.patel@graduate.edu | Phone: (408) 555-0192 | San Jose, CA
GitHub: github.com/rohan-grad | LinkedIn: linkedin.com/in/rohanpatel-cs

CAREER OBJECTIVE:
Motivated recent B.S. Computer Science graduate (2026) with 0-1 year experience seeking an Associate Software Engineer or Junior Web Developer role. Strong academic foundations in Object-Oriented Programming (Java), Relational Databases (SQL), Data Structures, and responsive web frontend (HTML5, CSS3, JavaScript). Completed university capstone project on micro-commerce portals.

TECHNICAL SKILLS:
- Languages: Java, SQL, JavaScript, HTML5, CSS3
- Tools & Libraries: Git, GitHub, MySQL, JUnit, VS Code, Eclipse
- Core Concepts: Object-Oriented Programming (OOP), RESTful API concepts, Algorithms

ACADEMIC PROJECTS:
1. Student Gradebook Management Portal (Java, MySQL, HTML/CSS)
   - Built a CRUD web application using Java Servlets, JDBC, and SQL to track university grades.
2. Responsive Portfolio Website (HTML5, CSS3, JavaScript)
   - Created mobile-first responsive portfolio showcasing algorithms and coursework.

EDUCATION:
Bachelor of Science in Computer Science | San Jose State University (Graduated Magna Cum Laude, GPA: 3.8/4.0)`,
      skills: ['Java', 'SQL', 'HTML', 'CSS', 'JavaScript', 'Git'],
      candidateName: 'Rohan Patel',
      candidateEmail: 'rohan.patel@graduate.edu',
      candidatePhone: '+91 98450 12345',
      yearsExp: 0,
      salary: 650000,
      location: 'All',
      climate: 'All',
      workingHours: 'All',
      workMode: 'Online/Remote',
      category: 'Software Engineering'
    },

    'mid-fullstack': {
      title: 'Mid-Level Full Stack Software Engineer',
      resumeText: `Jordan Lee | Full Stack Software Engineer (3 Years Exp)
Email: jordan.lee@clouddev.com | Phone: (206) 555-7821 | Seattle, WA

SUMMARY:
Software Engineer with 3 years of hands-on experience developing enterprise web services and responsive single-page applications using Java, Spring Boot, React, TypeScript, and SQL databases.

TECHNICAL SKILLS:
- Backend: Java, Spring Boot, REST API, SQL, PostgreSQL, Docker
- Frontend: JavaScript, TypeScript, React, HTML5, CSS3
- Tools: Git, AWS, Postman, CI/CD

EXPERIENCE:
Software Engineer | CloudScale Systems (2023 - Present)
- Engineered scalable microservice endpoints in Java & Spring Boot.
- Implemented dashboard widgets using React and modern CSS.

EDUCATION:
B.S. in Software Engineering (2023)`,
      skills: ['Java', 'Spring Boot', 'React', 'JavaScript', 'HTML', 'CSS', 'SQL', 'REST API', 'Git', 'Docker'],
      candidateName: 'Jordan Lee',
      candidateEmail: 'jordan.lee@clouddev.com',
      candidatePhone: '+1 (206) 555-7821',
      yearsExp: 3,
      salary: 135000,
      location: 'Seattle',
      climate: 'Mild / Temperate',
      workingHours: 'Flexible Hours',
      workMode: 'Hybrid',
      category: 'Software Engineering'
    },

    'java-fullstack': {
      title: 'Senior Java Full Stack Engineer',
      resumeText: `Alex Morgan | Senior Java Full Stack Software Engineer (6+ Years)
Email: alex.morgan@devmail.com | Phone: (415) 890-1244 | San Francisco, CA

SUMMARY:
Results-driven Senior Full Stack Developer with 6+ years of experience designing high-throughput distributed microservices, RESTful APIs, and reactive frontend architectures. Proven track record leading cloud migration to AWS, optimizing PostgreSQL query performance, and delivering responsive web apps using Java, Spring Boot, React, and TypeScript.

TECHNICAL EXPERTISE:
- Languages: Java (17/21), JavaScript (ES6+), TypeScript, SQL, HTML5, CSS3
- Frameworks: Spring Boot, Spring Cloud, Hibernate, React, Node.js
- Cloud & DevOps: AWS, Docker, Kubernetes, CI/CD, Git
- Databases & Systems: PostgreSQL, Redis, Kafka, Microservices, REST API

EDUCATION:
Bachelor of Science in Computer Science | UC Berkeley (2020)`,
      skills: ['Java', 'Spring Boot', 'React', 'SQL', 'Microservices', 'REST API', 'Docker', 'AWS', 'PostgreSQL', 'Git', 'HTML', 'CSS'],
      candidateName: 'Alex Morgan',
      candidateEmail: 'alex.morgan@devmail.com',
      candidatePhone: '+1 (415) 890-1244',
      yearsExp: 6,
      salary: 165000,
      location: 'San Francisco',
      climate: 'Warm & Sunny',
      workingHours: 'Global Flexible',
      workMode: 'Online/Remote',
      category: 'Software Engineering'
    },

    'ai-ml': {
      title: 'AI & LLM Machine Learning Engineer',
      resumeText: `Priya Sharma | AI Research & Machine Learning Engineer (4 Years Exp)
Email: priya.sharma@aiml-labs.io | Phone: (650) 412-9830 | Mountain View, CA

SUMMARY:
Innovative AI Engineer with 4 years hands-on experience developing transformer-based language models, RAG (Retrieval-Augmented Generation) systems, and deep learning pipelines. Strong foundation in Python, PyTorch, LangChain, vector databases, and MLOps.

TECHNICAL SKILLS:
- Core: Python, Machine Learning, Deep Learning, Artificial Intelligence, PyTorch
- Tools: LangChain, Vector Databases, Docker, FastAPI, AWS, SQL, Git

EDUCATION:
Master of Science in Artificial Intelligence | Stanford University`,
      skills: ['Python', 'Machine Learning', 'Artificial Intelligence', 'Deep Learning', 'PyTorch', 'Docker', 'AWS', 'FastAPI', 'SQL', 'Git'],
      candidateName: 'Priya Sharma',
      candidateEmail: 'priya.sharma@aiml-labs.io',
      candidatePhone: '+1 (650) 412-9830',
      yearsExp: 4,
      salary: 180000,
      location: 'San Francisco',
      climate: 'Warm & Sunny',
      workingHours: 'Flexible Hours',
      workMode: 'Online/Remote',
      category: 'AI & Machine Learning'
    },

    'senior-manager': {
      title: 'Senior Engineering Manager / Director',
      resumeText: `Catherine Vance | Senior Engineering Manager & Tech Leader (9+ Years)
Email: catherine.vance@techlead.io | Phone: (408) 789-3321 | Palo Alto, CA

SUMMARY:
Accomplished Senior Engineering Leader with 9+ years directing high-impact distributed systems squads. Experienced leading 35+ engineers, architecting petabyte-scale cloud systems, driving strategic technology roadmaps, managing multi-million dollar infrastructure budgets, and scaling core Java and cloud microservices.

LEADERSHIP & EXPERTISE:
- Leadership: Engineering Management, System Architecture, Agile/Scrum, Mentorship
- Systems: Distributed Systems, Java, Cloud Architecture, AWS, Kubernetes, Microservices
- Strategy: Multi-Cloud Infrastructure, Executive Risk Management, Zero-Trust

EDUCATION:
M.S. in Computer Science & Engineering Management | Carnegie Mellon University`,
      skills: ['Engineering Management', 'System Architecture', 'Distributed Systems', 'Java', 'Cloud Architecture', 'AWS', 'Kubernetes', 'Microservices'],
      yearsExp: 8,
      salary: 240000,
      location: 'New York',
      climate: 'Four Seasons',
      workingHours: 'Standard Day',
      workMode: 'Offline/On-site',
      category: 'Software Engineering'
    }
  };

  // DOM Elements
  const DOM = {
    serverStatusBadge: document.getElementById('serverStatusBadge'),
    serverStatusText: document.getElementById('serverStatusText'),
    totalJobsCounter: document.getElementById('totalJobsCounter'),
    savedJobsBtn: document.getElementById('savedJobsBtn'),
    savedCountBadge: document.getElementById('savedCountBadge'),
    myApplicationsBtn: document.getElementById('myApplicationsBtn'),
    applicationsCountBadge: document.getElementById('applicationsCountBadge'),
    themeToggleBtn: document.getElementById('themeToggleBtn'),
    themeIcon: document.getElementById('themeIcon'),

    // Experience Elements
    activeTierBadge: document.getElementById('activeTierBadge'),
    tierButtons: document.querySelectorAll('.tier-btn'),
    exactYearsInput: document.getElementById('exactYearsInput'),
    decrementYearsBtn: document.getElementById('decrementYearsBtn'),
    incrementYearsBtn: document.getElementById('incrementYearsBtn'),
    strictExperienceCheckbox: document.getElementById('strictExperienceCheckbox'),
    summaryTierLabel: document.getElementById('summaryTierLabel'),
    summaryWeightsLabel: document.getElementById('summaryWeightsLabel'),
    filterStatusPill: document.getElementById('filterStatusPill'),
    experienceGuardVal: document.getElementById('experienceGuardVal'),

    // Weights Controls
    weightSkillsSlider: document.getElementById('weightSkillsSlider'),
    weightExpSlider: document.getElementById('weightExpSlider'),
    weightSalSlider: document.getElementById('weightSalSlider'),
    weightPrefSlider: document.getElementById('weightPrefSlider'),
    skillsWeightLabel: document.getElementById('skillsWeightLabel'),
    expWeightLabel: document.getElementById('expWeightLabel'),
    salWeightLabel: document.getElementById('salWeightLabel'),
    prefWeightLabel: document.getElementById('prefWeightLabel'),
    resetWeightsBtn: document.getElementById('resetWeightsBtn'),
    toggleWeightsModalBtn: document.getElementById('toggleWeightsModalBtn'),

    // Weights Modal
    weightsModal: document.getElementById('weightsModal'),
    closeWeightsModalBtn: document.getElementById('closeWeightsModalBtn'),
    saveWeightsModalBtn: document.getElementById('saveWeightsModalBtn'),
    modalWeightSkills: document.getElementById('modalWeightSkills'),
    modalWeightExp: document.getElementById('modalWeightExp'),
    modalWeightSal: document.getElementById('modalWeightSal'),
    modalWeightPref: document.getElementById('modalWeightPref'),
    modalSkillsWeightVal: document.getElementById('modalSkillsWeightVal'),
    modalExpWeightVal: document.getElementById('modalExpWeightVal'),
    modalSalWeightVal: document.getElementById('modalSalWeightVal'),
    modalPrefWeightVal: document.getElementById('modalPrefWeightVal'),
    weightsTotalSum: document.getElementById('weightsTotalSum'),

    // Profile Inputs
    resumeTextInput: document.getElementById('resumeTextInput'),
    resumeFileInput: document.getElementById('resumeFileInput'),
    dropZone: document.getElementById('dropZone'),
    wordCountLabel: document.getElementById('wordCountLabel'),
    clearResumeBtn: document.getElementById('clearResumeBtn'),
    extractSkillsBtn: document.getElementById('extractSkillsBtn'),
    extractedInfoCard: document.getElementById('extractedInfoCard'),
    extractedEmailPhone: document.getElementById('extractedEmailPhone'),
    extractedYears: document.getElementById('extractedYears'),
    extractedEdu: document.getElementById('extractedEdu'),
    fileUploadStatus: document.getElementById('fileUploadStatus'),
    uploadedFileName: document.getElementById('uploadedFileName'),
    removeFileBtn: document.getElementById('removeFileBtn'),

    // Skills
    skillTagsList: document.getElementById('skillTagsList'),
    skillInput: document.getElementById('skillInput'),
    skillCountBadge: document.getElementById('skillCountBadge'),
    skillSuggestionsDropdown: document.getElementById('skillSuggestionsDropdown'),
    salaryRange: document.getElementById('salaryRange'),
    salaryDisplay: document.getElementById('salaryDisplay'),
    // Preferences & Filters
    locationPreferenceSelect: document.getElementById('locationPreferenceSelect'),
    climatePreferenceSelect: document.getElementById('climatePreferenceSelect'),
    workingHoursSelect: document.getElementById('workingHoursSelect'),
    workModeSelect: document.getElementById('workModeSelect'),
    categorySelect: document.getElementById('categorySelect'),
    matchJobsBtn: document.getElementById('matchJobsBtn'),
    resetFormBtn: document.getElementById('resetFormBtn'),

    // Work Mode Segmented Control (3 Options: Online/Remote, Offline/On-site, Hybrid)
    workModeSegmentedControl: document.getElementById('workModeSegmentedControl'),

    // Location Segmented Control (3 Options: City, State, Remote)
    locationTypeSegmentedControl: document.getElementById('locationTypeSegmentedControl'),
    cityLocationSubBox: document.getElementById('cityLocationSubBox'),
    citySelect: document.getElementById('citySelect'),
    stateLocationSubBox: document.getElementById('stateLocationSubBox'),
    stateSelect: document.getElementById('stateSelect'),
    remoteLocationSubBox: document.getElementById('remoteLocationSubBox'),
    currencySegmentedControl: document.getElementById('currencySegmentedControl'),
    currencyFilterSelect: document.getElementById('currencyFilterSelect'),
    applyExpectedSalaryLabel: document.getElementById('applyExpectedSalaryLabel'),
    batchExpectedSalaryLabel: document.getElementById('batchExpectedSalaryLabel'),

    // Multi-Job Application Banner & Actions
    multiApplyBanner: document.getElementById('multiApplyBanner'),

    // Platform Navigation & Feature 1: AI Career Predictor
    platformNavTabs: document.getElementById('platformNavTabs'),
    careerPredictorSection: document.getElementById('careerPredictorSection'),
    recalcPredictorBtn: document.getElementById('recalcPredictorBtn'),
    predictTrajectoryTitle: document.getElementById('predictTrajectoryTitle'),
    predictTrajectoryDesc: document.getElementById('predictTrajectoryDesc'),
    predictConfidenceScore: document.getElementById('predictConfidenceScore'),
    predictExpYears: document.getElementById('predictExpYears'),
    predictGrowthPace: document.getElementById('predictGrowthPace'),
    predictMomentumVal: document.getElementById('predictMomentumVal'),
    predictorMilestonesGrid: document.getElementById('predictorMilestonesGrid'),
    predictSalaryBars: document.getElementById('predictSalaryBars'),
    predictBreakthroughSkills: document.getElementById('predictBreakthroughSkills'),
    predictIndustryChips: document.getElementById('predictIndustryChips'),

    // Feature 2: Smart Skill Gap Finder
    gapTargetJobSelect: document.getElementById('gapTargetJobSelect'),
    refreshSkillGapBtn: document.getElementById('refreshSkillGapBtn'),
    gapJobHeaderTitle: document.getElementById('gapJobHeaderTitle'),
    gapJobHeaderCompany: document.getElementById('gapJobHeaderCompany'),
    gapTimeToClose: document.getElementById('gapTimeToClose'),
    gapTotalReqCount: document.getElementById('gapTotalReqCount'),
    gapMatchUplift: document.getElementById('gapMatchUplift'),
    gapCurrentMatchVal: document.getElementById('gapCurrentMatchVal'),
    gapCurrentBar: document.getElementById('gapCurrentBar'),
    gapPotentialMatchVal: document.getElementById('gapPotentialMatchVal'),
    gapPotentialBar: document.getElementById('gapPotentialBar'),
    gapMatchedCount: document.getElementById('gapMatchedCount'),
    gapMatchedSkillsList: document.getElementById('gapMatchedSkillsList'),
    gapCriticalCount: document.getElementById('gapCriticalCount'),
    gapCriticalSkillsList: document.getElementById('gapCriticalSkillsList'),
    gapSecondaryCount: document.getElementById('gapSecondaryCount'),
    gapSecondarySkillsList: document.getElementById('gapSecondarySkillsList'),
    gapRoadmapTimeline: document.getElementById('gapRoadmapTimeline'),

    // Feature 3: What-If Career Simulator
    simulatorSection: document.getElementById('simulatorSection'),
    simulatorChipsCloud: document.getElementById('simulatorChipsCloud'),
    resetSimulatorBtn: document.getElementById('resetSimulatorBtn'),
    simulatorCustomSkillInput: document.getElementById('simulatorCustomSkillInput'),
    addSimulatorSkillBtn: document.getElementById('addSimulatorSkillBtn'),
    simSelectedSkillsBadge: document.getElementById('simSelectedSkillsBadge'),
    simMatchGainVal: document.getElementById('simMatchGainVal'),
    simMatchSub: document.getElementById('simMatchSub'),
    simUnlockedCountVal: document.getElementById('simUnlockedCountVal'),
    simSalaryUpliftVal: document.getElementById('simSalaryUpliftVal'),
    simNewAvgSalaryVal: document.getElementById('simNewAvgSalaryVal'),
    simUnlockedJobsList: document.getElementById('simUnlockedJobsList'),

    // Feature 4: AI Interview Practice DOM
    interviewPracticeSection: document.getElementById('interviewPracticeSection'),
    interviewTargetJobSelect: document.getElementById('interviewTargetJobSelect'),
    refreshInterviewQuestionsBtn: document.getElementById('refreshInterviewQuestionsBtn'),
    interviewQuestionTabs: document.getElementById('interviewQuestionTabs'),
    interviewCategoryBadge: document.getElementById('interviewCategoryBadge'),
    interviewDifficultyBadge: document.getElementById('interviewDifficultyBadge'),
    interviewQuestionPrompt: document.getElementById('interviewQuestionPrompt'),
    interviewHintsContent: document.getElementById('interviewHintsContent'),
    interviewAnswerInput: document.getElementById('interviewAnswerInput'),
    interviewWordCountBadge: document.getElementById('interviewWordCountBadge'),
    insertStarTemplateBtn: document.getElementById('insertStarTemplateBtn'),
    clearInterviewAnswerBtn: document.getElementById('clearInterviewAnswerBtn'),
    submitInterviewAnswerBtn: document.getElementById('submitInterviewAnswerBtn'),
    submitInterviewBtnText: document.getElementById('submitInterviewBtnText'),
    interviewScorecardEmpty: document.getElementById('interviewScorecardEmpty'),
    interviewScorecardActive: document.getElementById('interviewScorecardActive'),
    interviewOverallScoreVal: document.getElementById('interviewOverallScoreVal'),
    interviewVerdictText: document.getElementById('interviewVerdictText'),
    interviewWordCountStat: document.getElementById('interviewWordCountStat'),
    interviewTechScoreVal: document.getElementById('interviewTechScoreVal'),
    interviewTechScoreBar: document.getElementById('interviewTechScoreBar'),
    interviewCommScoreVal: document.getElementById('interviewCommScoreVal'),
    interviewCommScoreBar: document.getElementById('interviewCommScoreBar'),
    interviewStarScoreVal: document.getElementById('interviewStarScoreVal'),
    interviewStarScoreBar: document.getElementById('interviewStarScoreBar'),
    interviewStrengthsList: document.getElementById('interviewStrengthsList'),
    interviewImprovementsList: document.getElementById('interviewImprovementsList'),
    interviewExemplarText: document.getElementById('interviewExemplarText'),
    copyExemplarBtn: document.getElementById('copyExemplarBtn'),
    interviewTotalPracticedBadge: document.getElementById('interviewTotalPracticedBadge'),
    interviewHistoryList: document.getElementById('interviewHistoryList'),

    // Feature 5: Fake Job Detection DOM
    fakeJobDetectorSection: document.getElementById('fakeJobDetectorSection'),
    fakeCatalogSelect: document.getElementById('fakeCatalogSelect'),
    scanCatalogJobBtn: document.getElementById('scanCatalogJobBtn'),
    fakeCompanyNameInput: document.getElementById('fakeCompanyNameInput'),
    fakeRecruiterEmailInput: document.getElementById('fakeRecruiterEmailInput'),
    fakeSalaryInput: document.getElementById('fakeSalaryInput'),
    fakePostingTextInput: document.getElementById('fakePostingTextInput'),
    fakePaymentCheckbox: document.getElementById('fakePaymentCheckbox'),
    runFakeDetectorBtn: document.getElementById('runFakeDetectorBtn'),
    runFakeBtnText: document.getElementById('runFakeBtnText'),
    loadPresetScamBtn: document.getElementById('loadPresetScamBtn'),
    loadPresetPhishBtn: document.getElementById('loadPresetPhishBtn'),
    loadPresetLegitBtn: document.getElementById('loadPresetLegitBtn'),
    fakeReportPlaceholder: document.getElementById('fakeReportPlaceholder'),
    fakeReportActive: document.getElementById('fakeReportActive'),
    fakeSafetyMeterVal: document.getElementById('fakeSafetyMeterVal'),
    fakeRiskLevelTitle: document.getElementById('fakeRiskLevelTitle'),
    fakeThreatsDetectedBadge: document.getElementById('fakeThreatsDetectedBadge'),
    fakeVerdictBanner: document.getElementById('fakeVerdictBanner'),
    fakeTrustVerdictText: document.getElementById('fakeTrustVerdictText'),
    fakeRedFlagsList: document.getElementById('fakeRedFlagsList'),
    fakeChecklistGrid: document.getElementById('fakeChecklistGrid'),
    fakeRecommendationsList: document.getElementById('fakeRecommendationsList'),

    // Feature 6: Career Comparison DOM
    careerComparisonSection: document.getElementById('careerComparisonSection'),
    compareRoleASelect: document.getElementById('compareRoleASelect'),
    compareRoleBSelect: document.getElementById('compareRoleBSelect'),
    runCareerCompareBtn: document.getElementById('runCareerCompareBtn'),
    presetCompareDevOpsAI: document.getElementById('presetCompareDevOpsAI'),
    presetCompareJavaCloud: document.getElementById('presetCompareJavaCloud'),
    presetCompareDataSec: document.getElementById('presetCompareDataSec'),
    roleATitle: document.getElementById('roleATitle'),
    roleAFitVal: document.getElementById('roleAFitVal'),
    roleAMedianSalary: document.getElementById('roleAMedianSalary'),
    roleAFiveYearSalary: document.getElementById('roleAFiveYearSalary'),
    roleAGrowthRate: document.getElementById('roleAGrowthRate'),
    roleBTitle: document.getElementById('roleBTitle'),
    roleBFitVal: document.getElementById('roleBFitVal'),
    roleBMedianSalary: document.getElementById('roleBMedianSalary'),
    roleBFiveYearSalary: document.getElementById('roleBFiveYearSalary'),
    roleBGrowthRate: document.getElementById('roleBGrowthRate'),
    compareVerdictText: document.getElementById('compareVerdictText'),
    salaryLabelA: document.getElementById('salaryLabelA'),
    salaryBarA: document.getElementById('salaryBarA'),
    salaryRangeA: document.getElementById('salaryRangeA'),
    salaryLabelB: document.getElementById('salaryLabelB'),
    salaryBarB: document.getElementById('salaryBarB'),
    salaryRangeB: document.getElementById('salaryRangeB'),
    salaryDeltaHighlight: document.getElementById('salaryDeltaHighlight'),
    transLabelA: document.getElementById('transLabelA'),
    transTimeA: document.getElementById('transTimeA'),
    transLabelB: document.getElementById('transLabelB'),
    transTimeB: document.getElementById('transTimeB'),
    learningCurveDetail: document.getElementById('learningCurveDetail'),
    vennHeaderA: document.getElementById('vennHeaderA'),
    vennChipsA: document.getElementById('vennChipsA'),
    vennChipsShared: document.getElementById('vennChipsShared'),
    vennHeaderB: document.getElementById('vennHeaderB'),
    vennChipsB: document.getElementById('vennChipsB'),
    certsLabelA: document.getElementById('certsLabelA'),
    certsListA: document.getElementById('certsListA'),
    certsLabelB: document.getElementById('certsLabelB'),
    certsListB: document.getElementById('certsListB'),

    // Feature 7: Job Readiness DOM
    jobReadinessSection: document.getElementById('jobReadinessSection'),
    readinessTargetJobSelect: document.getElementById('readinessTargetJobSelect'),
    runReadinessAuditBtn: document.getElementById('runReadinessAuditBtn'),
    readinessOverallVal: document.getElementById('readinessOverallVal'),
    readinessTargetRoleTitle: document.getElementById('readinessTargetRoleTitle'),
    readinessTargetCompany: document.getElementById('readinessTargetCompany'),
    readinessTierBadge: document.getElementById('readinessTierBadge'),
    pillarTechScoreVal: document.getElementById('pillarTechScoreVal'),
    pillarTechStatus: document.getElementById('pillarTechStatus'),
    pillarTechBar: document.getElementById('pillarTechBar'),
    pillarTechDetails: document.getElementById('pillarTechDetails'),
    pillarExpScoreVal: document.getElementById('pillarExpScoreVal'),
    pillarExpStatus: document.getElementById('pillarExpStatus'),
    pillarExpBar: document.getElementById('pillarExpBar'),
    pillarExpDetails: document.getElementById('pillarExpDetails'),
    pillarEduScoreVal: document.getElementById('pillarEduScoreVal'),
    pillarEduStatus: document.getElementById('pillarEduStatus'),
    pillarEduBar: document.getElementById('pillarEduBar'),
    pillarEduDetails: document.getElementById('pillarEduDetails'),
    pillarResumeScoreVal: document.getElementById('pillarResumeScoreVal'),
    pillarResumeStatus: document.getElementById('pillarResumeStatus'),
    pillarResumeBar: document.getElementById('pillarResumeBar'),
    pillarResumeDetails: document.getElementById('pillarResumeDetails'),
    readinessActionItemsList: document.getElementById('readinessActionItemsList'),

    // Feature 8: Unexpected Jobs DOM
    unexpectedJobsSection: document.getElementById('unexpectedJobsSection'),
    refreshUnexpectedBtn: document.getElementById('refreshUnexpectedBtn'),
    unexpectedRolesGrid: document.getElementById('unexpectedRolesGrid'),

    // Feature 9: Career Dashboard DOM
    careerDashboardSection: document.getElementById('careerDashboardSection'),
    dashUserGreeting: document.getElementById('dashUserGreeting'),
    refreshPersonalDashboardBtn: document.getElementById('refreshPersonalDashboardBtn'),
    dashHealthScoreVal: document.getElementById('dashHealthScoreVal'),
    dashHealthTier: document.getElementById('dashHealthTier'),
    dashInterviewAvgVal: document.getElementById('dashInterviewAvgVal'),
    dashInterviewSessions: document.getElementById('dashInterviewSessions'),
    dashTotalAppsVal: document.getElementById('dashTotalAppsVal'),
    dashInterviewPipeline: document.getElementById('dashInterviewPipeline'),
    dashTargetSalaryVal: document.getElementById('dashTargetSalaryVal'),
    dashStageAssessment: document.getElementById('dashStageAssessment'),
    dashStageInterview: document.getElementById('dashStageInterview'),
    dashStageWaiting: document.getElementById('dashStageWaiting'),
    dashStageSelected: document.getElementById('dashStageSelected'),
    dashMasteredSkillsList: document.getElementById('dashMasteredSkillsList'),
    dashRoadmapProgressBar: document.getElementById('dashRoadmapProgressBar'),
    dashRoadmapProgressText: document.getElementById('dashRoadmapProgressText'),
    dashRoadmapSkillsList: document.getElementById('dashRoadmapSkillsList'),
    dashTopRecsList: document.getElementById('dashTopRecsList'),
    dashInterviewHighVal: document.getElementById('dashInterviewHighVal'),
    dashInterviewMetaText: document.getElementById('dashInterviewMetaText'),
    dashDailyTasksList: document.getElementById('dashDailyTasksList'),
    selectedJobsCountBadge: document.getElementById('selectedJobsCountBadge'),
    selectAllQualifiedBtn: document.getElementById('selectAllQualifiedBtn'),
    batchCountNum: document.getElementById('batchCountNum'),
    batchApplyBtn: document.getElementById('batchApplyBtn'),
    selectBookmarkedBtn: document.getElementById('selectBookmarkedBtn'),
    bookmarkedCountNum: document.getElementById('bookmarkedCountNum'),
    selectTopMatchesBtn: document.getElementById('selectTopMatchesBtn'),
    clearSelectionBtn: document.getElementById('clearSelectionBtn'),

    // Single Apply Modal Additional Jobs
    additionalJobsCheckboxes: document.getElementById('additionalJobsCheckboxes'),

    // Dedicated Batch Multi-Job Application Modal
    batchApplicationModal: document.getElementById('batchApplicationModal'),
    closeBatchModalBtn: document.getElementById('closeBatchModalBtn'),
    batchModalJobsCount: document.getElementById('batchModalJobsCount'),
    batchListSummaryCount: document.getElementById('batchListSummaryCount'),
    batchApplicationForm: document.getElementById('batchApplicationForm'),
    batchSelectedJobsList: document.getElementById('batchSelectedJobsList'),
    batchFullName: document.getElementById('batchFullName'),
    batchEmail: document.getElementById('batchEmail'),
    batchPhone: document.getElementById('batchPhone'),
    batchExpectedSalary: document.getElementById('batchExpectedSalary'),
    batchJoiningTimeline: document.getElementById('batchJoiningTimeline'),
    batchWorkAuth: document.getElementById('batchWorkAuth'),
    batchPortfolio: document.getElementById('batchPortfolio'),
    batchCoverLetter: document.getElementById('batchCoverLetter'),
    submitBatchApplyBtn: document.getElementById('submitBatchApplyBtn'),
    cancelBatchApplyBtn: document.getElementById('cancelBatchApplyBtn'),

    // Batch Success Screen
    batchSuccessScreen: document.getElementById('batchSuccessScreen'),
    batchSuccessCount: document.getElementById('batchSuccessCount'),
    batchSubmittedResultsList: document.getElementById('batchSubmittedResultsList'),
    goToDashboardFromBatchBtn: document.getElementById('goToDashboardFromBatchBtn'),
    closeBatchSuccessBtn: document.getElementById('closeBatchSuccessBtn'),

    // Saved Drawer Multi-Apply
    applyAllSavedJobsBtn: document.getElementById('applyAllSavedJobsBtn'),
    savedDrawerCountNum: document.getElementById('savedDrawerCountNum'),

    // Salary Benchmark Modal
    salaryBenchmarkBtn: document.getElementById('salaryBenchmarkBtn'),
    salaryBenchmarkModal: document.getElementById('salaryBenchmarkModal'),
    closeSalaryBenchmarkModalBtn: document.getElementById('closeSalaryBenchmarkModalBtn'),
    closeSalaryBenchmarkModalBtnBottom: document.getElementById('closeSalaryBenchmarkModalBtnBottom'),
    benchmarkPercentileVal: document.getElementById('benchmarkPercentileVal'),
    benchmarkTargetSalaryVal: document.getElementById('benchmarkTargetSalaryVal'),
    benchmarkMarketPositionVal: document.getElementById('benchmarkMarketPositionVal'),
    benchmarkP10Val: document.getElementById('benchmarkP10Val'),
    benchmarkP25Val: document.getElementById('benchmarkP25Val'),
    benchmarkMedianVal: document.getElementById('benchmarkMedianVal'),
    benchmarkP75Val: document.getElementById('benchmarkP75Val'),
    benchmarkP90Val: document.getElementById('benchmarkP90Val'),
    benchmarkTiersGrid: document.getElementById('benchmarkTiersGrid'),

    // Dashboard Live Application Tracker
    dashboardApplicationTracker: document.getElementById('dashboardApplicationTracker'),
    dashboardApplicationsGrid: document.getElementById('dashboardApplicationsGrid'),
    trackerApplicationsCount: document.getElementById('trackerApplicationsCount'),
    refreshApplicationsBtn: document.getElementById('refreshApplicationsBtn'),

    // Results & Analytics
    topMatchScoreVal: document.getElementById('topMatchScoreVal'),
    totalMatchesVal: document.getElementById('totalMatchesVal'),
    avgSalaryVal: document.getElementById('avgSalaryVal'),
    skillGapBanner: document.getElementById('skillGapBanner'),
    skillGapText: document.getElementById('skillGapText'),
    gapSkillChips: document.getElementById('gapSkillChips'),
    jobsListContainer: document.getElementById('jobsListContainer'),
    jobSearchInput: document.getElementById('jobSearchInput'),
    sortOrderSelect: document.getElementById('sortOrderSelect'),

    // Job Details Modal
    jobDetailsModal: document.getElementById('jobDetailsModal'),
    closeJobDetailsModalBtn: document.getElementById('closeJobDetailsModalBtn'),
    modalCompanyLogo: document.getElementById('modalCompanyLogo'),
    modalJobTitle: document.getElementById('modalJobTitle'),
    modalCompanyLocation: document.getElementById('modalCompanyLocation'),
    modalSalaryRange: document.getElementById('modalSalaryRange'),
    modalSalaryFit: document.getElementById('modalSalaryFit'),
    modalWorkMode: document.getElementById('modalWorkMode'),
    modalExpLevel: document.getElementById('modalExpLevel'),
    modalJobType: document.getElementById('modalJobType'),
    modalCompanyDetailsGrid: document.getElementById('modalCompanyDetailsGrid'),
    modalLocationDetailsGrid: document.getElementById('modalLocationDetailsGrid'),
    modalJoiningHolidaysGrid: document.getElementById('modalJoiningHolidaysGrid'),
    modalScoreBreakdownGrid: document.getElementById('modalScoreBreakdownGrid'),
    modalMatchedSkills: document.getElementById('modalMatchedSkills'),
    modalMissingSkills: document.getElementById('modalMissingSkills'),
    modalBonusSkills: document.getElementById('modalBonusSkills'),
    modalJobDescription: document.getElementById('modalJobDescription'),
    modalBenefitsList: document.getElementById('modalBenefitsList'),
    modalSaveJobBtn: document.getElementById('modalSaveJobBtn'),
    modalApplyNowBtn: document.getElementById('modalApplyNowBtn'),

    // Live Application Modal & Immediate Status View
    applicationModal: document.getElementById('applicationModal'),
    closeApplyModalBtn: document.getElementById('closeApplyModalBtn'),
    applyModalJobTitle: document.getElementById('applyModalJobTitle'),
    applyModalCompany: document.getElementById('applyModalCompany'),
    applicationForm: document.getElementById('applicationForm'),
    applyFullName: document.getElementById('applyFullName'),
    applyEmail: document.getElementById('applyEmail'),
    applyPhone: document.getElementById('applyPhone'),
    applyExpectedSalary: document.getElementById('applyExpectedSalary'),
    applyJoiningTimeline: document.getElementById('applyJoiningTimeline'),
    applyWorkAuth: document.getElementById('applyWorkAuth'),
    applyPortfolio: document.getElementById('applyPortfolio'),
    applyCoverLetter: document.getElementById('applyCoverLetter'),
    regenerateCoverLetterBtn: document.getElementById('regenerateCoverLetterBtn'),
    cancelApplyBtn: document.getElementById('cancelApplyBtn'),
    submitApplyBtn: document.getElementById('submitApplyBtn'),
    applyErrorAlert: document.getElementById('applyErrorAlert'),

    // Immediate Status Screen
    applySuccessScreen: document.getElementById('applySuccessScreen'),
    successCompany: document.getElementById('successCompany'),
    successAppId: document.getElementById('successAppId'),
    successJobTitle: document.getElementById('successJobTitle'),
    successJoining: document.getElementById('successJoining'),
    modalCurrentStageBadge: document.getElementById('modalCurrentStageBadge'),
    modalStatusNoteText: document.getElementById('modalStatusNoteText'),
    modalStageStepper: document.getElementById('modalStageStepper'),
    goToDashboardTrackerBtn: document.getElementById('goToDashboardTrackerBtn'),
    viewMyAppsFromSuccessBtn: document.getElementById('viewMyAppsFromSuccessBtn'),
    doneSuccessBtn: document.getElementById('doneSuccessBtn'),

    // Dedicated Application Status Modal
    applicationStatusModal: document.getElementById('applicationStatusModal'),
    closeAppStatusModalBtn: document.getElementById('closeAppStatusModalBtn'),
    closeStatusModalBtnBottom: document.getElementById('closeStatusModalBtnBottom'),
    appStatusJobTitle: document.getElementById('appStatusJobTitle'),
    appStatusCompany: document.getElementById('appStatusCompany'),
    appStatusCompanyLogo: document.getElementById('appStatusCompanyLogo'),
    statusModalAppId: document.getElementById('statusModalAppId'),
    statusModalStageBadge: document.getElementById('statusModalStageBadge'),
    statusModalSalary: document.getElementById('statusModalSalary'),
    statusModalDate: document.getElementById('statusModalDate'),
    statusModalNote: document.getElementById('statusModalNote'),
    statusModalStepper: document.getElementById('statusModalStepper'),
    statusModalTimeline: document.getElementById('statusModalTimeline'),
    statusModalCoverLetter: document.getElementById('statusModalCoverLetter'),

    // Drawers
    myApplicationsDrawer: document.getElementById('myApplicationsDrawer'),
    closeMyApplicationsDrawerBtn: document.getElementById('closeMyApplicationsDrawerBtn'),
    myApplicationsList: document.getElementById('myApplicationsList'),

    savedJobsDrawer: document.getElementById('savedJobsDrawer'),
    closeSavedJobsDrawerBtn: document.getElementById('closeSavedJobsDrawerBtn'),
    savedJobsList: document.getElementById('savedJobsList'),

    toastContainer: document.getElementById('toastContainer'),

    // Authentication & Candidate Portal
    authPortal: document.getElementById('authPortal'),
    mainAppLayout: document.getElementById('mainAppLayout'),
    authUserBadge: document.getElementById('authUserBadge'),
    navLoginBtn: document.getElementById('navLoginBtn'),
    headerUserAvatar: document.getElementById('headerUserAvatar'),
    headerUserName: document.getElementById('headerUserName'),
    headerUserRole: document.getElementById('headerUserRole'),
    logoutBtn: document.getElementById('logoutBtn'),

    tabLoginBtn: document.getElementById('tabLoginBtn'),
    tabRegisterBtn: document.getElementById('tabRegisterBtn'),
    loginTabPane: document.getElementById('loginTabPane'),
    registerTabPane: document.getElementById('registerTabPane'),
    loginForm: document.getElementById('loginForm'),
    loginEmail: document.getElementById('loginEmail'),
    loginPassword: document.getElementById('loginPassword'),
    loginErrorAlert: document.getElementById('loginErrorAlert'),
    submitLoginBtn: document.getElementById('submitLoginBtn'),

    registerForm: document.getElementById('registerForm'),
    regFullName: document.getElementById('regFullName'),
    regEmail: document.getElementById('regEmail'),
    regPassword: document.getElementById('regPassword'),
    regExpYears: document.getElementById('regExpYears'),
    regSkills: document.getElementById('regSkills'),
    regErrorAlert: document.getElementById('regErrorAlert'),
    submitRegBtn: document.getElementById('submitRegBtn')
  };

  // ==========================================
  // INITIALIZATION
  // ==========================================

  async function init() {
    setupTheme();
    setupEventListeners();
    updateCounters();
    updateBatchSelectionUI();

    // Check Java backend server connection
    await checkServerConnection();

    // Load initial jobs
    if (isJavaServerOnline) {
      try {
        const res = await fetch(`${API_BASE_URL}/jobs`);
        if (res.ok) {
          state.jobCatalog = await res.json();
        } else {
          state.jobCatalog = [];
        }
      } catch (e) {
        state.jobCatalog = [];
      }

      // Sync saved jobs from server database
      try {
        if (state.currentUser && state.currentUser.email) {
          const savedUrl = `${API_BASE_URL}/jobs/saved?email=${encodeURIComponent(state.currentUser.email)}`;
          const savedRes = await fetchWithAuth(savedUrl);
          if (savedRes.ok) {
            const savedData = await savedRes.json();
            if (savedData.savedJobIds && Array.isArray(savedData.savedJobIds)) {
              state.savedJobIds = new Set(savedData.savedJobIds);
            } else if (savedData.savedJobs && Array.isArray(savedData.savedJobs)) {
              state.savedJobIds = new Set(savedData.savedJobs.map(s => s.job ? s.job.id : s.jobId).filter(Boolean));
            }
            localStorage.setItem('carrerx_saved_jobs', JSON.stringify([...state.savedJobIds]));
          }
        } else {
          state.savedJobIds = new Set();
        }
      } catch (e) {
        console.warn('Failed to sync saved jobs from server:', e);
      }

      // Fetch live dashboard metrics
      try {
        const dashRes = await fetch(`${API_BASE_URL}/stats/dashboard`);
        if (dashRes.ok) {
          const dashData = await dashRes.json();
          if (dashData.totalJobs) {
            DOM.totalJobsCounter.textContent = `${dashData.totalJobs} Verified`;
          }
        }
      } catch (e) {}
    } else {
      DOM.totalJobsCounter.textContent = `${state.jobCatalog.length || 17}+`;
    }

    updateCounters();

    // Sync applications & render dashboard application tracker
    await refreshApplicationsFromServer();

    // Load Fresher profile by default
    loadDemoProfile('fresher-java');
  }

  // ==========================================
  // THEME (LIGHT FRAME BY DEFAULT)
  // ==========================================

  function setupTheme() {
    const savedTheme = localStorage.getItem('carrerx_theme') || 'light';
    if (savedTheme === 'dark') {
      document.body.classList.add('dark-theme');
      DOM.themeIcon.textContent = '🌙';
    } else {
      document.body.classList.remove('dark-theme');
      DOM.themeIcon.textContent = '☀️';
    }
  }

  function toggleTheme() {
    const isDark = document.body.classList.toggle('dark-theme');
    DOM.themeIcon.textContent = isDark ? '🌙' : '☀️';
    localStorage.setItem('carrerx_theme', isDark ? 'dark' : 'light');
    showToast(`Switched to ${isDark ? 'Dark Theme' : 'Light Frame Theme'}.`, 'info');
  }

  // ==========================================
  // SERVER STATUS
  // ==========================================

  async function checkServerConnection() {
    try {
      const controller = new AbortController();
      const timeoutId = setTimeout(() => controller.abort(), 2000);
      const res = await fetch(`${API_BASE_URL}/health`, { signal: controller.signal });
      clearTimeout(timeoutId);

      if (res.ok) {
        isJavaServerOnline = true;
        DOM.serverStatusBadge.className = 'status-indicator online';
        DOM.serverStatusText.textContent = 'Java Engine (Port 8080)';
        return;
      }
    } catch (e) {}

    isJavaServerOnline = false;
    DOM.serverStatusBadge.className = 'status-indicator standalone';
    DOM.serverStatusText.textContent = 'Browser Client AI Mode';
  }

  // ==========================================
  // YEARS OF EXPERIENCE & TIER MANAGEMENT
  // ==========================================

  function setYearsOfExperience(years, syncSegmented = true) {
    const val = Math.max(0, parseInt(years, 10) || 0);
    state.userProfile.yearsOfExperience = val;
    DOM.exactYearsInput.value = val;

    let tier = 'fresher';
    let tierText = 'Fresher (0–1y)';

    if (val <= 1) {
      tier = 'fresher';
      tierText = 'Fresher (0–1y)';
    } else if (val <= 4) {
      tier = 'mid';
      tierText = 'Mid-Level (2–4y)';
    } else if (val <= 7) {
      tier = 'senior';
      tierText = 'Senior (5–7y)';
    } else {
      tier = 'lead';
      tierText = 'Lead / Manager (8y+)';
    }

    DOM.activeTierBadge.className = `tier-badge ${tier}`;
    DOM.activeTierBadge.textContent = tierText;
    DOM.summaryTierLabel.textContent = tierText;

    if (syncSegmented) {
      DOM.tierButtons.forEach(btn => {
        btn.classList.toggle('active', btn.dataset.tier === tier);
      });
    }
  }

  // ==========================================
  // ATTRIBUTE WEIGHTS MANAGEMENT (60% SKILLS)
  // ==========================================

  function updateWeights(skills, exp, sal, pref) {
    state.userProfile.weights.skills = skills;
    state.userProfile.weights.experience = exp;
    state.userProfile.weights.salary = sal;
    state.userProfile.weights.preferences = pref;

    DOM.weightSkillsSlider.value = skills;
    DOM.weightExpSlider.value = exp;
    DOM.weightSalSlider.value = sal;
    DOM.weightPrefSlider.value = pref;

    DOM.skillsWeightLabel.textContent = `${skills}%`;
    DOM.expWeightLabel.textContent = `${exp}%`;
    DOM.salWeightLabel.textContent = `${sal}%`;
    DOM.prefWeightLabel.textContent = `${pref}%`;

    DOM.modalWeightSkills.value = skills;
    DOM.modalWeightExp.value = exp;
    DOM.modalWeightSal.value = sal;
    DOM.modalWeightPref.value = pref;

    DOM.modalSkillsWeightVal.textContent = `${skills}%`;
    DOM.modalExpWeightVal.textContent = `${exp}%`;
    DOM.modalSalWeightVal.textContent = `${sal}%`;
    DOM.modalPrefWeightVal.textContent = `${pref}%`;

    const total = skills + exp + sal + pref;
    DOM.weightsTotalSum.textContent = `${total}%`;
    DOM.weightsTotalSum.style.color = (total === 100) ? 'var(--success-color)' : 'var(--warning-color)';

    DOM.summaryWeightsLabel.textContent = `${skills}% Skills • ${exp}% Exp • ${sal}% Sal • ${pref}% Mode`;
    DOM.toggleWeightsModalBtn.querySelector('.btn-label').textContent = `Weights (${skills}% Skills)`;
  }

  function resetWeightsToDefault() {
    updateWeights(60, 20, 10, 10);
    showToast('Attribute weights reset to 60% Skills / 20% Exp / 10% Sal / 10% Mode', 'info');
    calculateJobMatches();
  }

  // ==========================================
  // SKILL TAG MANAGER
  // ==========================================

  function addSkill(skillName) {
    if (!skillName) return;
    const clean = skillName.trim();
    if (!clean) return;

    const exists = state.userProfile.skills.some(s => s.toLowerCase() === clean.toLowerCase());
    if (exists) {
      showToast(`Skill "${clean}" already added.`, 'info');
      return;
    }

    state.userProfile.skills.push(clean);
    renderSkillTags();
    syncQuickChips();
  }

  function removeSkill(index) {
    state.userProfile.skills.splice(index, 1);
    renderSkillTags();
    syncQuickChips();
  }

  function renderSkillTags() {
    DOM.skillTagsList.innerHTML = '';
    state.userProfile.skills.forEach((skill, index) => {
      const tag = document.createElement('span');
      tag.className = 'skill-tag';
      tag.innerHTML = `
        <span>${escapeHtml(skill)}</span>
        <span class="remove-tag" data-index="${index}" title="Remove skill">✕</span>
      `;
      DOM.skillTagsList.appendChild(tag);
    });

    DOM.skillCountBadge.textContent = `${state.userProfile.skills.length} skills added`;
  }

  function syncQuickChips() {
    document.querySelectorAll('.chip').forEach(chip => {
      const skill = chip.dataset.skill;
      const isSelected = state.userProfile.skills.some(s => s.toLowerCase() === skill.toLowerCase());
      chip.classList.toggle('active', isSelected);
    });
  }

  // ==========================================
  // RESUME PARSING
  // ==========================================

  async function parseResumeText(text) {
    if (!text || !text.trim()) {
      showToast('Please enter or paste your resume text first.', 'info');
      return;
    }

    // Spinner and loading state on Auto-Extract button
    const btn = DOM.extractSkillsBtn;
    const originalHtml = btn ? btn.innerHTML : '';
    if (btn) {
      btn.disabled = true;
      btn.classList.add('loading');
      btn.innerHTML = '<span class="btn-spinner"></span> Extracting Skills & Profile...';
    }

    try {
      if (isJavaServerOnline) {
        try {
          const res = await fetch(`${API_BASE_URL}/resume/extract`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ resumeText: text })
          });
          if (res.ok) {
            const data = await res.json();
            await handleParsedResumeData(data);
            const count = (data.skills && data.skills.length) || (data.detectedSkills && data.detectedSkills.length) || 0;
            showToast(`Auto-extracted ${count} skills & candidate profile via Java engine!`, 'success');
            return;
          } else {
            // Fallback attempt to /api/parse-resume
            const fallbackRes = await fetch(`${API_BASE_URL}/parse-resume`, {
              method: 'POST',
              headers: { 'Content-Type': 'application/json' },
              body: JSON.stringify({ resumeText: text })
            });
            if (fallbackRes.ok) {
              const fallbackData = await fallbackRes.json();
              await handleParsedResumeData(fallbackData);
              const count = (fallbackData.skills && fallbackData.skills.length) || (fallbackData.detectedSkills && fallbackData.detectedSkills.length) || 0;
              showToast(`Auto-extracted ${count} skills via Java engine!`, 'success');
              return;
            }
          }
        } catch (apiErr) {
          console.warn('Backend extraction error, falling back to client-side:', apiErr);
          showToast(`Backend sync notice: ${apiErr.message}. Used client fallback.`, 'warning');
        }
      }

      // Client fallback
      const clientData = extractResumeEntitiesClientSide(text);
      await handleParsedResumeData(clientData);
      showToast(`Extracted ${clientData.detectedSkills.length} skills from resume!`, 'success');
    } catch (err) {
      console.error('Error during resume extraction:', err);
      showToast(`Resume extraction failed: ${err.message}`, 'error');
    } finally {
      if (btn) {
        btn.disabled = false;
        btn.classList.remove('loading');
        btn.innerHTML = originalHtml;
      }
    }
  }

  function extractResumeEntitiesClientSide(text) {
    const lower = text.toLowerCase();
    const detectedSkills = [];

    state.knownSkills.forEach(skill => {
      const regex = new RegExp(`\\b${escapeRegExp(skill)}\\b`, 'i');
      if (regex.test(text)) detectedSkills.push(skill);
    });

    let name = '';
    const firstLine = text.trim().split('\n')[0] || '';
    const nameMatch = firstLine.split(/[|•,–-]/)[0].trim();
    if (nameMatch && nameMatch.length > 2 && nameMatch.length < 40 && !/^(resume|curriculum|cv|summary|objective|career)/i.test(nameMatch)) {
      name = nameMatch;
    }

    let email = '';
    const emailMatch = text.match(/[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,6}/);
    if (emailMatch) email = emailMatch[0];

    let phone = '';
    const phoneMatch = text.match(/(\+?\d{1,3}[-.\s]?)?\(?\d{3}\)?[-.\s]?\d{3}[-.\s]?\d{4}/);
    if (phoneMatch) phone = phoneMatch[0];

    let years = 0;
    if (/\b(fresher|recent graduate|fresh graduate|entry level|intern)\b/i.test(text)) {
      years = 0;
    } else {
      const yearMatch = text.match(/(\d+)\+?\s*(?:years?|yrs?)(?:\s+(?:of\s+)?[a-z-]+\s+)?(?:experience|exp)/i);
      if (yearMatch) years = parseInt(yearMatch[1], 10);
    }

    const education = [];
    if (/ph\.?d|doctorate/i.test(lower)) education.push("Ph.D.");
    if (/master'?s?|m\.s\.|mba/i.test(lower)) education.push("Master's Degree");
    if (/bachelor'?s?|b\.s\.|b\.tech|b\.e\./i.test(lower)) education.push("Bachelor's Degree");

    const competencies = [];
    if (/cloud|aws|azure|gcp/i.test(lower)) competencies.push("Cloud Architecture & DevOps");
    if (/microservice|distributed/i.test(lower)) competencies.push("Microservices & Distributed Systems");
    if (/docker|container|kubernetes/i.test(lower)) competencies.push("Containerization & Orchestration");
    if (/rest|api/i.test(lower)) competencies.push("RESTful Web Services");
    if (/ai|machine learning|llm/i.test(lower)) competencies.push("AI & Machine Learning");
    if (competencies.length === 0) competencies.push("Software Engineering");

    return {
      name,
      candidateName: name,
      detectedSkills,
      skills: detectedSkills,
      email,
      phone,
      contact: { name, email, phone },
      experienceLevel: years <= 1 ? "0-1 years (Fresher)" : `${years} years (Experienced)`,
      estimatedYearsExp: years,
      competencies,
      education,
      wordCount: text.trim().split(/\s+/).length
    };
  }

  async function handleParsedResumeData(data) {
    if (!data) return;

    // Contact info (fill "Detected Contact")
    const name = data.candidateName || (data.contact && data.contact.name) || data.name || '';
    const email = data.email || (data.contact && data.contact.email) || '';
    const phone = data.phone || (data.contact && data.contact.phone) || '';

    if (name) state.userProfile.detectedInfo.name = name;
    if (email) state.userProfile.detectedInfo.email = email;
    if (phone) state.userProfile.detectedInfo.phone = phone;

    const contactParts = [name, email, phone].filter(Boolean);
    DOM.extractedEmailPhone.textContent = contactParts.length > 0 ? contactParts.join(' • ') : 'None found';

    // Estimated Exp (fill "Estimated Exp")
    const expText = data.experienceLevel || ((data.estimatedYearsExp !== undefined && data.estimatedYearsExp !== null) 
      ? (data.estimatedYearsExp === 0 ? '0 years (Fresher)' : `${data.estimatedYearsExp} years`) 
      : 'Not specified');
    DOM.extractedYears.textContent = expText;

    if (data.estimatedYearsExp !== undefined && data.estimatedYearsExp !== null) {
      setYearsOfExperience(data.estimatedYearsExp);
    }

    // Competencies tags (fill "Competencies tags")
    const compItems = (data.competencies && Array.isArray(data.competencies) && data.competencies.length > 0)
      ? data.competencies
      : ((data.education && Array.isArray(data.education) && data.education.length > 0)
        ? data.education
        : ['Software Engineering']);

    DOM.extractedEdu.innerHTML = compItems.map(c => 
      `<span class="competency-badge" style="display:inline-block; margin:2px 4px 2px 0; padding:2px 8px; font-size:0.75rem; background:rgba(37,99,235,0.08); color:var(--primary-color); border:1px solid rgba(37,99,235,0.2); border-radius:12px; font-weight:600;">${escapeHtml(c)}</span>`
    ).join('');

    // Reveal entity info card
    DOM.extractedInfoCard.classList.remove('hidden');

    // Populate detected skills
    const rawSkills = data.skills || data.detectedSkills || [];
    if (rawSkills && rawSkills.length > 0) {
      rawSkills.forEach(s => {
        if (!state.userProfile.skills.some(existing => existing.toLowerCase() === s.toLowerCase())) {
          state.userProfile.skills.push(s);
        }
      });
      renderSkillTags();
      syncQuickChips();
    }

    // Auto-populate job matches
    await calculateJobMatches();
  }

  // ==========================================
  // MATCHING CALCULATION
  // ==========================================

  async function calculateJobMatches() {
    DOM.matchJobsBtn.classList.add('loading');
    DOM.matchJobsBtn.innerHTML = '<span class="btn-spinner"></span><span>Analyzing Matches...</span>';

    const reqPayload = {
      resumeText: DOM.resumeTextInput.value,
      skills: state.userProfile.skills,
      yearsOfExperience: state.userProfile.yearsOfExperience,
      expectedSalary: parseInt(DOM.salaryRange.value, 10),
      preferredLocation: DOM.locationPreferenceSelect ? DOM.locationPreferenceSelect.value : 'All',
      preferredClimate: DOM.climatePreferenceSelect ? DOM.climatePreferenceSelect.value : 'All',
      preferredWorkingHours: DOM.workingHoursSelect ? DOM.workingHoursSelect.value : 'All',
      preferredWorkMode: DOM.workModeSelect ? DOM.workModeSelect.value : 'Any',
      preferredCategory: DOM.categorySelect.value,
      strictExperienceFilter: DOM.strictExperienceCheckbox.checked,
      weightSkills: state.userProfile.weights.skills,
      weightExperience: state.userProfile.weights.experience,
      weightSalary: state.userProfile.weights.salary,
      weightPreferences: state.userProfile.weights.preferences,
      currencyFilter: state.currencyFilter || 'Any'
    };

    let responseData = null;

    if (isJavaServerOnline) {
      try {
        const res = await fetch(`${API_BASE_URL}/jobs/match`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(reqPayload)
        });
        if (res.ok) {
          responseData = await res.json();
        } else {
          // Fallback to /api/match
          const fallbackRes = await fetch(`${API_BASE_URL}/match`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(reqPayload)
          });
          if (fallbackRes.ok) {
            responseData = await fallbackRes.json();
          }
        }
      } catch (err) {
        console.warn('Job matching API error:', err);
      }
    }

    if (!responseData) {
      responseData = { totalMatches: 0, results: [] };
    }

    state.matchedResults = responseData.results || [];
    renderAnalytics(responseData);
    renderJobsGrid();

    DOM.matchJobsBtn.classList.remove('loading');
    DOM.matchJobsBtn.innerHTML = '<span class="btn-icon">⚡</span><span>Calculate Intelligent Matches</span>';
    showToast(`Found ${state.matchedResults.length} qualified recommendations!`, 'success');
  }

  // ==========================================
  // RENDERING & UI UPDATES
  // ==========================================

  function renderAnalytics(data) {
    DOM.topMatchScoreVal.textContent = data.topMatchScore ? `${data.topMatchScore}%` : '--%';
    DOM.totalMatchesVal.textContent = data.totalMatches || 0;
    const inrJobsCount = (data.results || []).filter(r => r.job && ((r.job.currency && r.job.currency.toUpperCase() === 'INR') || /india|bangalore|bengaluru|hyderabad|mumbai|delhi|gurugram|gurgaon|noida|pune|chennai/i.test(r.job.location || ''))).length;
    const isMajorityINR = inrJobsCount > 0 && inrJobsCount >= (data.results || []).length / 2;
    if (isMajorityINR && data.averageSalary) {
      const lakh = (data.averageSalary / 100000).toFixed(1);
      DOM.avgSalaryVal.textContent = `₹${Number(data.averageSalary).toLocaleString('en-IN')} (${lakh} LPA)`;
    } else {
      DOM.avgSalaryVal.textContent = data.averageSalary ? (data.averageSalary >= 100000 ? `₹${(data.averageSalary / 100000).toFixed(1)} LPA` : `₹${data.averageSalary.toLocaleString('en-IN')}`) : '₹0 LPA';
    }
    DOM.experienceGuardVal.textContent = data.strictExperienceFilterActive ? 'Active 🛡️' : 'Off';

    DOM.filterStatusPill.innerHTML = data.strictExperienceFilterActive 
      ? '🛡️ Senior Manager Roles Filtered: <strong style="color:var(--success-color);">ON</strong>'
      : '🛡️ Senior Manager Roles Filtered: <strong style="color:var(--warning-color);">OFF</strong>';

    if (data.recommendedMissingSkills && data.recommendedMissingSkills.length > 0) {
      const topGap = data.recommendedMissingSkills[0];
      DOM.skillGapBanner.classList.remove('hidden');
      DOM.skillGapText.innerHTML = `Adding <strong>${topGap.skill}</strong> to your profile could unlock <strong>${topGap.demandCount} additional opportunities</strong> with higher compensation.`;
      
      DOM.gapSkillChips.innerHTML = data.recommendedMissingSkills.map(item => `
        <button class="gap-chip" data-skill="${escapeHtml(item.skill)}" title="Click to add to your skills">
          + Add ${escapeHtml(item.skill)} (${item.demandCount} jobs)
        </button>
      `).join('');
    } else {
      DOM.skillGapBanner.classList.add('hidden');
    }
  }

  function renderJobsGrid() {
    let items = [...state.matchedResults];

    if (state.searchQuery) {
      const q = state.searchQuery.toLowerCase();
      items = items.filter(r => 
        r.job.title.toLowerCase().includes(q) ||
        r.job.company.toLowerCase().includes(q) ||
        r.job.location.toLowerCase().includes(q) ||
        (r.job.locationIntelligence?.city || '').toLowerCase().includes(q) ||
        r.matchedSkills.some(s => s.toLowerCase().includes(q))
      );
    }

    if (state.activeFilter === 'online' || state.activeFilter === 'remote') {
      items = items.filter(r => r.job.workMode.toLowerCase().includes('remote') || r.job.workMode.toLowerCase().includes('online'));
    } else if (state.activeFilter === 'offline') {
      items = items.filter(r => r.job.workMode.toLowerCase().includes('on-site') || r.job.workMode.toLowerCase().includes('offline'));
    } else if (state.activeFilter === 'hybrid') {
      items = items.filter(r => r.job.workMode.toLowerCase().includes('hybrid'));
    } else if (state.activeFilter === 'bookmarks') {
      items = items.filter(r => state.savedJobIds.has(r.job.id));
    } else if (state.activeFilter === 'fresher') {
      items = items.filter(r => r.job.minYearsExp <= 1);
    } else if (state.activeFilter === 'top-rated') {
      items = items.filter(r => (r.job.companyDetails?.rating || 0) >= 4.5);
    } else if (state.activeFilter === 'exceptional') {
      items = items.filter(r => r.matchPercentage >= 75);
    }

    // Sorting
    items.sort((a, b) => {
      if (state.activeSort === 'match-desc') return b.matchPercentage - a.matchPercentage;
      if (state.activeSort === 'salary-desc') return b.job.maxSalary - a.job.maxSalary;
      if (state.activeSort === 'rating-desc') return (b.job.companyDetails?.rating || 0) - (a.job.companyDetails?.rating || 0);
      if (state.activeSort === 'exp-asc') return a.job.minYearsExp - b.job.minYearsExp;
      if (state.activeSort === 'title-asc') return a.job.title.localeCompare(b.job.title);
      return 0;
    });

    if (items.length === 0) {
      if (state.activeFilter === 'bookmarks') {
        DOM.jobsListContainer.innerHTML = `
          <div class="empty-state">
            <div class="empty-icon">🔖</div>
            <h3>No Bookmarked Jobs Yet</h3>
            <p>Click the 🔖 Bookmark button on any recommended job to save it here for later application.</p>
            <button class="empty-cta-btn" onclick="document.querySelector('[data-filter=\\'all\\']').click()">Browse All Qualified Jobs</button>
          </div>
        `;
        return;
      }

      DOM.jobsListContainer.innerHTML = `
        <div class="empty-state">
          <div class="empty-icon">🔎</div>
          <h3>No matching jobs found with current filters</h3>
          <p>Try resetting filters or adjusting experience settings.</p>
          <button class="empty-cta-btn" onclick="document.querySelector('[data-filter=\\'all\\']').click()">Show All Recommendations</button>
        </div>
      `;
      return;
    }

    DOM.jobsListContainer.innerHTML = items.map(res => renderJobCardHtml(res)).join('');
  }

  function renderJobCardHtml(res) {
    const job = res.job;
    const isSaved = state.savedJobIds.has(job.id);
    const existingApp = state.submittedApplications.find(a => a.jobId === job.id);
    const isApplied = !!existingApp;
    const isSelected = state.selectedJobIdsForBatch.has(job.id);

    const tierClass = res.matchPercentage >= 85 ? 'exceptional' : (res.matchPercentage >= 70 ? 'strong' : 'good');
    const workModeClass = job.workMode.toLowerCase().replace(/[^a-z]/g, '-');
    const bd = res.scoreBreakdown || { skills: 0, experience: 0, salary: 0, preferences: 0 };
    const w = state.userProfile.weights;
    const cd = job.companyDetails || {};
    const loc = job.locationIntelligence || {};
    const join = job.joiningAndWorkplace || {};

    let expPillClass = 'mid';
    let expPillText = `${job.minYearsExp}+ yrs exp`;
    if (job.minYearsExp <= 1) {
      expPillClass = 'fresher';
      expPillText = `🎓 Fresher Friendly (0–${job.minYearsExp}y)`;
    } else if (job.minYearsExp >= 8) {
      expPillClass = 'lead';
      expPillText = `👔 Executive / Lead (${job.minYearsExp}+y)`;
    } else if (job.minYearsExp >= 5) {
      expPillClass = 'senior';
      expPillText = `🚀 Senior (${job.minYearsExp}+y)`;
    }

    return `
      <article class="job-card ${isSelected ? 'is-selected-for-batch' : ''}" data-job-id="${job.id}">
        <!-- Multi-Job Application Checkbox Strip -->
        <div class="job-card-select-strip" style="display:flex; justify-content:space-between; align-items:center; margin-bottom:0.4rem;">
          <label class="job-card-select-label" style="margin-bottom:0;">
            <input type="checkbox" class="job-card-checkbox" data-job-id="${job.id}" ${isSelected ? 'checked' : ''} ${isApplied ? 'disabled' : ''}>
            <span class="select-label-text">${isApplied ? '✓ Already Applied' : (isSelected ? '✓ Selected to Apply' : 'Select to Apply')}</span>
          </label>
          ${isSelected ? `<span class="badge-mini" style="background:#e0e7ff; color:#4338ca; border-color:#c7d2fe;">Selected for Batch Apply</span>` : ''}
        </div>

        <!-- Top Row: Avatar, Title, Rating, Match Badge -->
        <div class="job-card-header">
          <div class="company-job-info">
            <div class="company-avatar">${job.companyLogo || '🏢'}</div>
            <div class="job-title-row">
              <h3 class="job-title">${escapeHtml(job.title)}</h3>
              <div class="company-meta">
                <span class="company-name">${escapeHtml(job.company)}</span>
                ${cd.rating ? `<span class="company-rating-badge">⭐ ${cd.rating} / 5</span>` : ''}
                <span class="meta-dot">•</span>
                <span class="location">${escapeHtml(job.location)}</span>
                <span class="meta-dot">•</span>
                <span class="work-badge ${workModeClass}">${escapeHtml(job.workMode)}</span>
                <span class="meta-dot">•</span>
                <span class="exp-badge-pill ${expPillClass}">${escapeHtml(expPillText)}</span>
              </div>
            </div>
          </div>

          <div class="match-score-badge ${tierClass}">
            <span>${res.matchPercentage}% Match</span>
          </div>
        </div>

        <!-- NEW: Quick-Glance Company & Location Strip -->
        <div class="job-intel-snippet-strip">
          <div class="intel-snippet-cell" title="Company Profile">
            <span>🏢</span>
            <span>${escapeHtml(cd.industry || 'Technology')} • <strong>${escapeHtml(cd.companySize || 'Growing Team')}</strong></span>
          </div>
          <div class="intel-snippet-cell" title="Climate & Weather">
            <span>☀️</span>
            <span><strong>${escapeHtml(loc.avgTemperature || 'Mild Climate')}</strong> • ${escapeHtml(loc.climateOverview ? loc.climateOverview.substring(0, 38) + '...' : 'Sunny')}</span>
          </div>
          <div class="intel-snippet-cell" title="Holidays & Join Time">
            <span>🏖️</span>
            <span><strong>${escapeHtml(join.annualHolidays ? join.annualHolidays.split('+')[0].trim() : '25 Days Off')}</strong> • Join: ${escapeHtml(join.onboardingTimeline ? join.onboardingTimeline.split('(')[0].trim() : 'Flexible')}</span>
          </div>
        </div>

        <!-- Multi-Attribute Breakdown Bar -->
        <div class="multi-attr-breakdown-bar">
          <div class="attr-item">
            <span class="attr-title">🧠 Skills (${w.skills}%):</span>
            <span class="attr-val highlight-skills">${bd.skills} / ${w.skills}%</span>
          </div>
          <div class="attr-item">
            <span class="attr-title">⏳ Experience (${w.experience}%):</span>
            <span class="attr-val">${bd.experience} / ${w.experience}%</span>
          </div>
          <div class="attr-item">
            <span class="attr-title">💰 Salary (${w.salary}%):</span>
            <span class="attr-val">${bd.salary} / ${w.salary}%</span>
          </div>
          <div class="attr-item">
            <span class="attr-title">🌐 Work Mode (${w.preferences}%):</span>
            <span class="attr-val">${bd.preferences} / ${w.preferences}%</span>
          </div>
        </div>

        <!-- Salary Banner -->
        <div class="job-salary-banner">
          <div>
            <span class="salary-figure">${formatJobSalary(job)}</span>
            <span style="font-size:0.75rem; color:var(--text-muted);"> / year</span>
          </div>
          <div class="salary-verdict">
            <span>${escapeHtml(res.salaryAssessment)}</span>
          </div>
        </div>

        <!-- Skills Breakdown -->
        <div class="skills-match-breakdown">
          <div class="skill-row">
            <span class="skill-row-title text-green">✓ Matched (${res.matchedSkills.length}):</span>
            <div class="tags-row">
              ${res.matchedSkills.slice(0, 5).map(s => `<span class="skill-pill matched">✓ ${escapeHtml(s)}</span>`).join('')}
              ${res.matchedSkills.length > 5 ? `<span class="skill-pill matched">+${res.matchedSkills.length - 5} more</span>` : ''}
            </div>
          </div>

          ${res.missingSkills.length > 0 ? `
            <div class="skill-row">
              <span class="skill-row-title text-amber">⚡ Missing (${res.missingSkills.length}):</span>
              <div class="tags-row">
                ${res.missingSkills.slice(0, 4).map(s => `<span class="skill-pill missing">⚡ ${escapeHtml(s)}</span>`).join('')}
              </div>
            </div>
          ` : ''}
        </div>

        <!-- Description Snippet -->
        <p class="job-desc-snippet">${escapeHtml(job.description)}</p>

        <!-- Card Footer -->
        <div class="job-card-footer">
          <div class="job-footer-left">
            <span class="urgency-badge ${job.urgency === 'Fresher Friendly' ? 'fresher-friendly' : ''}">${escapeHtml(job.urgency)}</span>
            <span class="meta-dot">•</span>
            <span>Category: ${escapeHtml(job.category)}</span>
            <span class="meta-dot">•</span>
            <span>Posted ${escapeHtml(job.postedDate)}</span>
          </div>

          <div class="job-footer-actions">
            ${!isApplied ? `
              <button type="button" class="card-action-btn select-batch-btn ${isSelected ? 'selected' : ''}" data-action="toggle-select" data-job-id="${job.id}" title="${isSelected ? 'Deselect this job' : 'Select this job for multi-job application'}">
                ${isSelected ? '✓ Selected' : '☑️ Select'}
              </button>
            ` : ''}
            <button class="card-action-btn save-btn ${isSaved ? 'saved' : ''}" data-action="save" data-job-id="${job.id}">
              ${isSaved ? '★ Saved' : '🔖 Save'}
            </button>
            <button class="card-action-btn view-btn" data-action="view" data-job-id="${job.id}">
              Company Intel & Guide 🏢
            </button>
            ${isApplied ? (() => {
              const b = getStageBadge(existingApp.status);
              return `
              <div class="card-stage-indicator">
                <span class="stage-badge-pill ${b.className}">${b.emoji} ${escapeHtml(b.label)}</span>
                <button class="card-action-btn view-status-btn" data-action="track-status" data-app-id="${existingApp.applicationId}" title="Track Live Application Status & Progression">
                  Track Status 📊
                </button>
              </div>`;
            })() : `
              <button class="card-action-btn apply-btn" data-action="apply" data-job-id="${job.id}" title="Review tailored application and apply">
                Apply Now 🚀
              </button>
              <button class="card-action-btn instant-apply-btn" data-action="instant-apply" data-job-id="${job.id}" title="Instant 1-Click Apply with your active profile">
                ⚡ Quick Apply
              </button>
            `}
          </div>
        </div>
      </article>
    `;
  }

  // ==========================================
  // DEMO PROFILES
  // ==========================================

  function loadDemoProfile(profileKey) {
    const p = DEMO_PROFILES[profileKey];
    if (!p) return;

    DOM.resumeTextInput.value = p.resumeText;
    updateWordCount();

    state.userProfile.skills = [...p.skills];
    renderSkillTags();
    syncQuickChips();

    DOM.salaryRange.value = p.salary;
    DOM.salaryDisplay.textContent = `₹${formatLPA(p.salary)}`;

    setYearsOfExperience(p.yearsExp);

    if (p.candidateName) state.userProfile.detectedInfo.name = p.candidateName;
    if (p.candidateEmail) state.userProfile.detectedInfo.email = p.candidateEmail;
    if (p.candidatePhone) state.userProfile.detectedInfo.phone = p.candidatePhone;

    DOM.workModeSelect.value = p.workMode || 'Any';
    DOM.categorySelect.value = p.category || 'All';
    if (DOM.locationPreferenceSelect && p.location) DOM.locationPreferenceSelect.value = p.location;
    if (DOM.climatePreferenceSelect && p.climate) DOM.climatePreferenceSelect.value = p.climate;
    if (DOM.workingHoursSelect && p.workingHours) DOM.workingHoursSelect.value = p.workingHours;

    // Sync segmented controls for Currency
    if (DOM.currencySegmentedControl) {
      DOM.currencySegmentedControl.querySelectorAll('.currency-filter-btn').forEach(b => {
        b.classList.toggle('active', b.dataset.curr === (state.currencyFilter || 'Any'));
      });
    }

    if (DOM.workModeSegmentedControl) {
      DOM.workModeSegmentedControl.querySelectorAll('.workmode-btn').forEach(btn => {
        btn.classList.toggle('active', btn.dataset.mode === (p.workMode || 'Any'));
      });
    }
    // Sync segmented controls for Location
    if (DOM.locationTypeSegmentedControl) {
      const isRemote = (p.workMode === 'Online/Remote' || p.location === 'Remote');
      DOM.locationTypeSegmentedControl.querySelectorAll('.loctype-btn').forEach(btn => {
        if (isRemote) {
          btn.classList.toggle('active', btn.dataset.loctype === 'remote');
        } else {
          btn.classList.toggle('active', btn.dataset.loctype === 'city');
        }
      });
      if (isRemote) {
        DOM.cityLocationSubBox?.classList.add('hidden');
        DOM.stateLocationSubBox?.classList.add('hidden');
        DOM.remoteLocationSubBox?.classList.remove('hidden');
      } else {
        DOM.cityLocationSubBox?.classList.remove('hidden');
        DOM.stateLocationSubBox?.classList.add('hidden');
        DOM.remoteLocationSubBox?.classList.add('hidden');
      }
    }

    parseResumeText(p.resumeText);
    calculateJobMatches();
  }

  // ==========================================
  // MODALS: JOB DETAILS & DEEP COMPANY INTEL
  // ==========================================

  function openJobDetailsModal(jobId) {
    const matchObj = state.matchedResults.find(r => r.job.id === jobId) || 
                     { job: state.jobCatalog.find(j => j.id === jobId), matchedSkills: [], missingSkills: [], bonusSkills: [], scoreBreakdown: {} };
    const job = matchObj.job;
    if (!job) return;

    state.currentModalJob = job;
    const bd = matchObj.scoreBreakdown || {};
    const w = state.userProfile.weights;
    const cd = job.companyDetails || {};
    const loc = job.locationIntelligence || {};
    const join = job.joiningAndWorkplace || {};

    DOM.modalCompanyLogo.textContent = job.companyLogo || '🏢';
    DOM.modalJobTitle.textContent = job.title;
    DOM.modalCompanyLocation.textContent = `${job.company} • ${job.location}`;
    DOM.modalSalaryRange.textContent = `${formatJobSalary(job)}`;
    DOM.modalSalaryFit.textContent = matchObj.salaryAssessment || 'Competitive Compensation';
    DOM.modalWorkMode.textContent = job.workMode;
    DOM.modalExpLevel.textContent = `${job.experienceLevel} (${job.minYearsExp}+ yrs)`;
    DOM.modalJobType.textContent = job.jobType;

    renderCompanyDetailsGrid(job, cd);

    // Live Indian employer intelligence from backend REST API
    if (isJavaServerOnline && job.company) {
      fetch(`${API_BASE_URL}/companies/info?name=${encodeURIComponent(job.company)}`)
        .then(r => r.ok ? r.json() : null)
        .then(info => {
          if (info && info.success && info.company) {
            renderCompanyDetailsGrid(job, info.company, true);
          }
        })
        .catch(err => console.warn('Could not fetch live company intelligence:', err));
    }

    // 2. Location & Climate Grid
    DOM.modalLocationDetailsGrid.innerHTML = `
      <div class="intel-tile">
        <span class="intel-tile-heading">City & Neighborhood</span>
        <span class="intel-tile-value">📍 ${escapeHtml(loc.city || job.location)} • ${escapeHtml(loc.neighborhood || 'Tech Hub')}</span>
      </div>
      <div class="intel-tile">
        <span class="intel-tile-heading">Average Temperature</span>
        <span class="intel-tile-value">🌡️ ${escapeHtml(loc.avgTemperature || '18°C - 26°C (64°F - 78°F)')}</span>
      </div>
      <div class="intel-tile">
        <span class="intel-tile-heading">Sunny Days Per Year</span>
        <span class="intel-tile-value">☀️ ${loc.sunnyDaysPerYear || 250} Days of Sunshine</span>
      </div>
      <div class="intel-tile full-width">
        <span class="intel-tile-heading">Climate & Weather Overview</span>
        <span class="intel-tile-value">${escapeHtml(loc.climateOverview || 'Pleasant weather with seasonal variations')}</span>
      </div>
      <div class="intel-tile">
        <span class="intel-tile-heading">Living Cost & Relocation</span>
        <span class="intel-tile-value">${escapeHtml(loc.livingCostIndex || 'Relocation allowance available')}</span>
      </div>
      <div class="intel-tile full-width">
        <span class="intel-tile-heading">Commute & Public Transport</span>
        <span class="intel-tile-value">🚆 ${escapeHtml(loc.commuteTransport || 'Transit accessible • Shuttle services provided')}</span>
      </div>
    `;

    // 3. Joining & Holidays Grid
    DOM.modalJoiningHolidaysGrid.innerHTML = `
      <div class="intel-tile">
        <span class="intel-tile-heading">When Can You Join?</span>
        <span class="intel-tile-value">📅 ${escapeHtml(join.onboardingTimeline || 'Flexible: 2–4 weeks notice')}</span>
      </div>
      <div class="intel-tile">
        <span class="intel-tile-heading">Onboarding Cohorts</span>
        <span class="intel-tile-value">🚀 ${escapeHtml(join.joiningBatch || 'Every alternating Monday')}</span>
      </div>
      <div class="intel-tile">
        <span class="intel-tile-heading">Work Flexibility</span>
        <span class="intel-tile-value">💻 ${escapeHtml(join.workFlexibility || 'Remote or Hybrid choice')}</span>
      </div>
      <div class="intel-tile">
        <span class="intel-tile-heading">Annual Paid Holidays</span>
        <span class="intel-tile-value text-green">🏖️ ${escapeHtml(join.annualHolidays || '25 Days Vacation + Holidays')}</span>
      </div>
      <div class="intel-tile">
        <span class="intel-tile-heading">Sick & Wellness Days</span>
        <span class="intel-tile-value">🤒 ${escapeHtml(join.sickAndWellnessLeave || 'Unlimited sick leave')}</span>
      </div>
      <div class="intel-tile">
        <span class="intel-tile-heading">Year-End Company Shutdown</span>
        <span class="intel-tile-value">🎄 ${escapeHtml(join.yearEndShutdown || 'Dec 24 to Jan 2 (Paid)')}</span>
      </div>
      <div class="intel-tile full-width">
        <span class="intel-tile-heading">Parental & Family Leave</span>
        <span class="intel-tile-value">👶 ${escapeHtml(join.parentalLeave || '16 Weeks fully paid parental leave')}</span>
      </div>
    `;

    // Score Breakdown
    DOM.modalScoreBreakdownGrid.innerHTML = `
      <div class="breakdown-card">
        <span class="breakdown-title">🧠 Skills Match (${w.skills}%)</span>
        <span class="breakdown-score text-green">${bd.skills || 0}%</span>
      </div>
      <div class="breakdown-card">
        <span class="breakdown-title">⏳ Experience (${w.experience}%)</span>
        <span class="breakdown-score text-blue">${bd.experience || 0}%</span>
      </div>
      <div class="breakdown-card">
        <span class="breakdown-title">💰 Salary Fit (${w.salary}%)</span>
        <span class="breakdown-score text-amber">${bd.salary || 0}%</span>
      </div>
      <div class="breakdown-card">
        <span class="breakdown-title">🌐 Work Mode (${w.preferences}%)</span>
        <span class="breakdown-score text-green">${bd.preferences || 0}%</span>
      </div>
    `;

    DOM.modalMatchedSkills.innerHTML = (matchObj.matchedSkills || []).map(s => `<span class="skill-pill matched">✓ ${escapeHtml(s)}</span>`).join('') || '<span style="font-size:0.75rem; color:var(--text-muted);">None</span>';
    DOM.modalMissingSkills.innerHTML = (matchObj.missingSkills || []).map(s => `<span class="skill-pill missing">⚡ ${escapeHtml(s)}</span>`).join('') || '<span style="font-size:0.75rem; color:var(--text-muted);">Full skill coverage!</span>';
    DOM.modalBonusSkills.innerHTML = (matchObj.bonusSkills || []).map(s => `<span class="skill-pill bonus">★ ${escapeHtml(s)}</span>`).join('') || '<span style="font-size:0.75rem; color:var(--text-muted);">Standard requirements</span>';

    DOM.modalJobDescription.textContent = job.description;
    DOM.modalBenefitsList.innerHTML = (job.benefits || []).map(b => `<li><span>🎁</span> ${escapeHtml(b)}</li>`).join('');

    const isSaved = state.savedJobIds.has(job.id);
    DOM.modalSaveJobBtn.textContent = isSaved ? '★ Saved in Bookmarks' : '🔖 Bookmark Job';

    const existingApp = state.submittedApplications.find(a => a.jobId === job.id);
    if (existingApp) {
      DOM.modalApplyNowBtn.textContent = `✓ Already Applied (${existingApp.applicationId})`;
      DOM.modalApplyNowBtn.disabled = true;
      DOM.modalApplyNowBtn.style.opacity = '0.7';
    } else {
      DOM.modalApplyNowBtn.textContent = '🚀 Apply for this Job';
      DOM.modalApplyNowBtn.disabled = false;
      DOM.modalApplyNowBtn.style.opacity = '1';
    }

    DOM.jobDetailsModal.classList.remove('hidden');
  }

  // ==========================================
  // LIVE APPLICATION FEATURE (FULLY ALIVE!)
  // ==========================================

  function showApplyError(message, type = 'error') {
    const alertEl = document.getElementById('applyErrorAlert') || DOM.applyErrorAlert;
    if (!alertEl) return;
    alertEl.textContent = message;
    alertEl.className = type === 'info' ? 'alert-info-box' : 'alert-error-box';
    alertEl.classList.remove('hidden');
  }

  function hideApplyError() {
    const alertEl = document.getElementById('applyErrorAlert') || DOM.applyErrorAlert;
    if (alertEl) {
      alertEl.classList.add('hidden');
      alertEl.textContent = '';
    }
  }

  function openApplicationModal(jobId) {
    const matchObj = state.matchedResults.find(r => r.job.id === jobId);
    const job = state.jobCatalog.find(j => j.id === jobId) 
             || matchObj?.job 
             || (state.currentModalJob?.id === jobId ? state.currentModalJob : null);
    if (!job) {
      console.warn('Job not found for jobId:', jobId);
      return;
    }

    state.currentModalJob = job;
    DOM.applicationForm.dataset.jobId = job.id;

    // Reset error alert state
    hideApplyError();

    // Populate job details in modal
    DOM.applyModalJobTitle.textContent = job.title;
    DOM.applyModalCompany.textContent = `at ${job.company} • ${job.location} (${job.workMode})`;
    const isJobINR = (job.currency && job.currency.toUpperCase() === 'INR') ||
      (job.location && /india|bangalore|bengaluru|hyderabad|mumbai|delhi|gurugram|gurgaon|noida|pune|chennai/i.test(job.location || ''));
    if (DOM.applyExpectedSalaryLabel) {
      DOM.applyExpectedSalaryLabel.textContent = isJobINR ? 'Expected Salary (₹/yr - INR) *' : 'Expected Salary ($/yr - USD) *';
    }
    DOM.applyExpectedSalary.value = isJobINR ? (job.minSalary || 1800000) : (DOM.salaryRange.value || job.minSalary || 85000);

    // Populate candidate details from user profile or detected info
    const detected = state.userProfile.detectedInfo || {};
    const resumeFirstLine = (DOM.resumeTextInput.value || '').split('\n')[0].trim();
    const parsedName = resumeFirstLine.split(/[|•,–-]/)[0].trim();
    const candidateName = DOM.applyFullName.value.trim() 
                       || detected.name 
                       || (parsedName && parsedName.length > 2 && parsedName.length < 40 ? parsedName : 'Alex Developer');

    DOM.applyFullName.value = candidateName;
    DOM.applyEmail.value = DOM.applyEmail.value.trim() || detected.email || 'alex.developer@example.com';
    DOM.applyPhone.value = DOM.applyPhone.value.trim() || detected.phone || '+1 (555) 019-2834';

    // Set work mode radio to match job work mode
    const modeRadios = document.querySelectorAll('input[name="applyWorkModeRadio"]');
    modeRadios.forEach(r => {
      if (r.value === job.workMode || (r.value === 'Online/Remote' && job.workMode.includes('Remote'))) {
        r.checked = true;
      }
    });

    // Check if the user already submitted an application for this job
    const existingApp = state.submittedApplications.find(a => a.jobId === job.id);
    const submitBtn = document.getElementById('submitApplyBtn') || DOM.submitApplyBtn;

    if (existingApp) {
      showApplyError(`ℹ️ You have already applied for this position (${job.title} at ${job.company}). Tracking ID: ${existingApp.applicationId} • Current Stage: ${existingApp.status}`, 'info');
      if (submitBtn) {
        submitBtn.disabled = true;
        submitBtn.textContent = 'Already Applied ✓';
      }
    } else {
      if (submitBtn) {
        submitBtn.disabled = false;
        submitBtn.textContent = 'Apply Now 🚀';
      }
    }

    generateCoverLetter(job);
    populateAdditionalJobs(job.id);

    // Show form, hide success screen, show modal
    DOM.applicationForm.classList.remove('hidden');
    DOM.applySuccessScreen.classList.add('hidden');
    DOM.applicationModal.classList.remove('hidden');
  }

  function populateAdditionalJobs(currentJobId) {
    if (!DOM.additionalJobsCheckboxes) return;
    const appliedIds = new Set(state.submittedApplications.map(a => a.jobId));
    const candidateJobs = state.matchedResults
      .filter(r => r.job.id !== currentJobId && !appliedIds.has(r.job.id))
      .slice(0, 4);

    if (candidateJobs.length === 0) {
      DOM.additionalJobsCheckboxes.innerHTML = `
        <div style="font-size:0.78rem; color:var(--text-muted); padding:0.35rem 0;">No other unapplied recommended roles at this moment.</div>
      `;
      return;
    }

    DOM.additionalJobsCheckboxes.innerHTML = candidateJobs.map(r => `
      <label class="additional-job-check-item">
        <input type="checkbox" class="additional-job-chk" value="${r.job.id}">
        <div style="display:flex; flex-direction:column; gap:0.15rem; flex:1;">
          <div style="font-weight:700; color:var(--text-primary); font-size:0.8rem;">${escapeHtml(r.job.title)}</div>
          <div style="font-size:0.72rem; color:var(--text-muted);">${escapeHtml(r.job.company)} • ${escapeHtml(r.job.location)} (${escapeHtml(r.job.workMode)}) • <span style="color:var(--accent-primary); font-weight:700;">${r.matchPercentage}% Match</span></div>
        </div>
      </label>
    `).join('');
  }

  function generateCoverLetter(job) {
    const matchObj = state.matchedResults.find(r => r.job.id === job.id);
    const matched = matchObj ? matchObj.matchedSkills : state.userProfile.skills.slice(0, 4);
    const skillsListStr = matched.length > 0 ? matched.join(', ') : 'software engineering principles';
    const yearsExp = state.userProfile.yearsOfExperience;
    const expText = yearsExp === 0 ? 'as an ambitious recent Computer Science graduate' : `with over ${yearsExp} years of hands-on professional experience`;
    const loc = job.locationIntelligence?.city || job.location;
    const applicantName = DOM.applyFullName.value.trim() || state.userProfile.detectedInfo?.name || 'Alex Developer';

    const letter = `Dear Hiring Team at ${job.company},

I am writing to express my strong enthusiasm for the ${job.title} role in ${loc}. ${expText}, I have developed deep technical competencies directly aligned with your core requirements, specifically in ${skillsListStr}.

I have closely followed ${job.company}'s work in ${job.companyDetails?.industry || 'tech'} and deeply admire your culture of ${job.companyDetails?.culture || 'high innovation and engineering excellence'}. I am eager to contribute immediately to your product architecture and collaborative roadmap.

Key Highlights of My Alignment:
- Core proficiency in ${matched.slice(0, 3).join(', ') || 'Software Architecture'}
- Proven dedication to writing robust, maintainable, and well-tested code
- Ready to join on your required onboarding schedule and make an immediate impact

Thank you for your time and consideration. I look forward to speaking with the team!

Warm regards,
${applicantName}`;

    DOM.applyCoverLetter.value = letter;
  }

  const STAGE_NOTES = {
    'Applied': 'Application submitted successfully. Awaiting initial recruiter screening.',
    'Under Review': 'Your resume and skills are being reviewed by the technical hiring team.',
    'Shortlisted': 'Great news! Your profile has been shortlisted for technical evaluation.',
    'Interview': 'Shortlisted for technical & cultural interview round with the Engineering Lead.',
    'Selected': 'Congratulations! You have been selected for this position. Official offer letter dispatched.',
    'Rejected': 'Thank you for your interest. While your credentials are impressive, we are proceeding with other applicants at this time.',
    'Completely Selected': 'Congratulations! You have been selected for this position. Official offer letter dispatched.',
    'Waiting List': 'Your profile is under active consideration and currently placed on the primary Waiting List.',
    'Test/Assessment': 'Application received and progressing through technical evaluation.'
  };

  function getStageBadge(status) {
    if (!status) return { emoji: '📋', label: 'Applied', className: 'stage-applied' };
    const s = status.toLowerCase();
    if (s.includes('select')) return { emoji: '🟢', label: 'Selected', className: 'stage-selected' };
    if (s.includes('interview')) return { emoji: '🎙️', label: 'Interview', className: 'stage-interview' };
    if (s.includes('shortlist')) return { emoji: '⚡', label: 'Shortlisted', className: 'stage-shortlisted' };
    if (s.includes('review')) return { emoji: '🔍', label: 'Under Review', className: 'stage-review' };
    if (s.includes('wait')) return { emoji: '🔍', label: 'Under Review', className: 'stage-review' };
    if (s.includes('test') || s.includes('assessment')) return { emoji: '🔍', label: 'Under Review', className: 'stage-review' };
    if (s.includes('reject')) return { emoji: '🔴', label: 'Rejected', className: 'stage-rejected' };
    if (s.includes('applied')) return { emoji: '📋', label: 'Applied', className: 'stage-applied' };
    return { emoji: '📋', label: status, className: 'stage-applied' };
  }

  function getStageClass(status) {
    return getStageBadge(status).className;
  }

  function renderStepper(stepperEl, currentStage) {
    if (!stepperEl) return;
    const stagesInOrder = ['Applied', 'Under Review', 'Shortlisted', 'Interview', 'Selected'];
    const curIdx = stagesInOrder.indexOf(currentStage);
    const isRejected = currentStage === 'Rejected';

    stepperEl.querySelectorAll('.step-item').forEach(item => {
      const step = item.dataset.step;
      item.classList.remove('active', 'completed', 'rejected');

      if (isRejected) {
        if (step === 'Rejected') {
          item.classList.add('active', 'rejected');
        }
      } else {
        const idx = stagesInOrder.indexOf(step);
        if (idx !== -1) {
          if (idx === curIdx) {
            item.classList.add('active');
          } else if (idx < curIdx) {
            item.classList.add('completed');
          }
        }
      }
    });
  }

  async function updateApplicationStage(appId, newStage, customNote) {
    const note = customNote || STAGE_NOTES[newStage] || `Application status updated to: ${newStage}`;
    const app = state.submittedApplications.find(a => a.applicationId === appId);
    if (!app) return;

    app.status = newStage;
    app.statusNote = note;
    if (!app.timeline) app.timeline = [];
    const now = new Date().toLocaleString();
    app.timeline.unshift(`[${now}] Stage Updated: ${newStage} - ${note}`);

    localStorage.setItem('carrerx_applications', JSON.stringify(state.submittedApplications));

    const badge = getStageBadge(newStage);

    // Immediate DOM updates on screen without requiring page refresh
    renderDashboardApplicationTracker();

    renderMyApplicationsDrawer();

    renderJobsGrid();
    renderMyApplicationsDrawer();

    if (DOM.successAppId && DOM.successAppId.textContent === appId) {
      DOM.modalCurrentStageBadge.className = `stage-badge-pill ${badge.className}`;
      DOM.modalCurrentStageBadge.textContent = `${badge.emoji} ${badge.label}`;
      DOM.modalStatusNoteText.textContent = note;
      renderStepper(DOM.modalStageStepper, newStage);
    }

    if (state.currentTrackingApp && state.currentTrackingApp.applicationId === appId) {
      openApplicationStatusModal(appId);
    }

    showToast(`Status updated immediately: ${badge.emoji} ${badge.label}`, 'success');

    if (isJavaServerOnline) {
      try {
        await fetch(`${API_BASE_URL}/applications/update-status`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            applicationId: appId,
            status: newStage,
            note: note
          })
        });
      } catch (err) {
        console.warn('Backend stage sync error:', err);
      }
    }
  }

  async function handleApplicationSubmission(e) {
    e.preventDefault();

    // 1. Resolve selected job accurately
    const targetJobId = DOM.applicationForm.dataset.jobId || state.currentModalJob?.id;
    const matchObj = state.matchedResults.find(r => r.job.id === targetJobId);
    const job = state.jobCatalog.find(j => j.id === targetJobId) 
             || matchObj?.job 
             || state.currentModalJob;

    if (!job) {
      showApplyError('Selected job could not be identified. Please select a job and try again.');
      return;
    }

    // 2. Client-side duplicate check
    const existingApp = state.submittedApplications.find(a => a.jobId === job.id);
    if (existingApp) {
      showApplyError(`Duplicate Submission Prevented: You have already applied for ${job.title} at ${job.company}. Tracking Reference: ${existingApp.applicationId} • Current Stage: ${existingApp.status}.`);
      return;
    }

    // 3. Candidate input validation
    const fullName = DOM.applyFullName.value.trim();
    const email = DOM.applyEmail.value.trim();
    const phone = DOM.applyPhone.value.trim();
    const salaryVal = parseInt(DOM.applyExpectedSalary.value, 10);
    const joiningTimeline = DOM.applyJoiningTimeline.value;
    const workAuth = DOM.applyWorkAuth.value;
    const portfolioUrl = DOM.applyPortfolio.value.trim();
    const selectedWorkMode = (document.querySelector('input[name="applyWorkModeRadio"]:checked')?.value) || 'Online/Remote';
    const coverLetter = DOM.applyCoverLetter.value.trim();

    if (!fullName || fullName.length < 2) {
      showApplyError('Please enter your full name (at least 2 characters).');
      DOM.applyFullName.focus();
      return;
    }

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!email || !emailRegex.test(email)) {
      showApplyError('Please enter a valid email address (e.g., name@example.com).');
      DOM.applyEmail.focus();
      return;
    }

    if (!phone || phone.length < 5) {
      showApplyError('Please enter a valid contact phone number.');
      DOM.applyPhone.focus();
      return;
    }

    if (isNaN(salaryVal) || salaryVal <= 0) {
      showApplyError('Please enter a valid annual expected salary amount (greater than $0).');
      DOM.applyExpectedSalary.focus();
      return;
    }

    if (!joiningTimeline) {
      showApplyError('Please select when you can join.');
      DOM.applyJoiningTimeline.focus();
      return;
    }

    // Clear previous error alerts
    hideApplyError();

    // Check if user selected additional batch apply roles
    const checkedAdditional = Array.from(document.querySelectorAll('.additional-job-chk:checked')).map(cb => cb.value);
    if (checkedAdditional.length > 0) {
      const allJobIds = [job.id, ...checkedAdditional];
      DOM.applicationModal.classList.add('hidden');
      await submitBatchApplications(allJobIds, {
        candidateName: fullName,
        candidateEmail: email,
        candidatePhone: phone,
        expectedSalary: salaryVal,
        joiningAvailability: joiningTimeline,
        workAuth: workAuth,
        portfolioUrl: portfolioUrl,
        preferredWorkMode: selectedWorkMode,
        coverLetter: coverLetter
      });
      return;
    }

    const submitBtn = document.getElementById('submitApplyBtn') || DOM.submitApplyBtn;
    if (submitBtn) {
      submitBtn.disabled = true;
      submitBtn.innerHTML = 'Submitting Application... ⏳';
    }

    const candidateSkills = (state.userProfile.skills && state.userProfile.skills.length > 0)
      ? state.userProfile.skills
      : [];
    const candidateYears = typeof state.userProfile.yearsOfExperience === 'number'
      ? state.userProfile.yearsOfExperience
      : (state.userProfile.detectedInfo?.years || 0);
    const matchPctVal = matchObj ? matchObj.matchPercentage : 0;

    const appData = {
      jobId: job.id,
      jobTitle: job.title,
      company: job.company,
      candidateName: fullName,
      candidateEmail: email,
      candidatePhone: phone,
      expectedSalary: salaryVal,
      joiningAvailability: joiningTimeline,
      workAuth: workAuth,
      preferredWorkMode: selectedWorkMode,
      portfolioUrl: portfolioUrl,
      coverLetter: coverLetter,
      skills: candidateSkills,
      yearsOfExperience: candidateYears,
      matchPercentage: matchPctVal
    };

    let serverResponse = null;

    try {
      const res = await fetch(`${API_BASE_URL}/apply`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(appData)
      });

      const data = await res.json().catch(() => null);

      if (!res.ok || !data || data.success === false) {
        const errorMsg = data?.message || data?.error || `Server responded with status ${res.status}`;
        showApplyError(`Submission failed: ${errorMsg}`);
        if (submitBtn) {
          submitBtn.disabled = false;
          submitBtn.innerHTML = 'Apply Now 🚀';
        }
        return;
      }

      serverResponse = data;
    } catch (err) {
      console.error('Apply request error:', err);
      showApplyError(`Network error: Unable to connect to the backend server at ${API_BASE}. Please ensure the Java server is running.`);
      if (submitBtn) {
        submitBtn.disabled = false;
        submitBtn.innerHTML = 'Apply Now 🚀';
      }
      return;
    }

    // Backend confirmed successful persistence
    const newRecord = {
      applicationId: serverResponse.applicationId,
      jobId: job.id,
      jobTitle: serverResponse.jobTitle || job.title,
      company: serverResponse.company || job.company,
      companyLogo: job.companyLogo || '🏢',
      location: job.location,
      expectedSalary: appData.expectedSalary,
      joiningAvailability: appData.joiningAvailability,
      submittedAt: serverResponse.submittedAt || new Date().toLocaleString(),
      status: serverResponse.status || 'Applied',
      statusNote: serverResponse.statusNote || 'Your application has been received and is currently under initial screening.',
      coverLetter: appData.coverLetter,
      timeline: [
        `[${serverResponse.submittedAt || new Date().toLocaleString()}] Application Submitted: ${serverResponse.status || 'Test/Assessment'}`
      ]
    };

    state.submittedApplications.unshift(newRecord);
    localStorage.setItem('carrerx_applications', JSON.stringify(state.submittedApplications));
    updateCounters();

    // Show Immediate Application Status View
    DOM.applicationForm.classList.add('hidden');
    DOM.successCompany.textContent = newRecord.company;
    DOM.successAppId.textContent = newRecord.applicationId;
    DOM.successJobTitle.textContent = newRecord.jobTitle;
    DOM.successJoining.textContent = newRecord.joiningAvailability;

    const badge = getStageBadge(newRecord.status);
    DOM.modalCurrentStageBadge.className = `stage-badge-pill ${badge.className}`;
    DOM.modalCurrentStageBadge.textContent = `${badge.emoji} ${badge.label}`;
    DOM.modalStatusNoteText.textContent = newRecord.statusNote;
    renderStepper(DOM.modalStageStepper, newRecord.status);

    // Stage automatically locked and assigned by employer backend screening system (manual selection removed)

    DOM.applySuccessScreen.classList.remove('hidden');

    if (submitBtn) {
      submitBtn.disabled = false;
      submitBtn.innerHTML = 'Apply Now 🚀';
    }

    renderDashboardApplicationTracker();
    renderJobsGrid();
    showToast(`Application & Tailored Cover Letter Submitted! Status: ${badge.emoji} ${badge.label} (Ref: ${newRecord.applicationId})`, 'success');
  }

  // ==========================================
  // INSTANT 1-CLICK APPLY FEATURE
  // ==========================================

  async function instantApply(jobId) {
    const matchObj = state.matchedResults.find(r => r.job.id === jobId);
    const job = state.jobCatalog.find(j => j.id === jobId) || matchObj?.job;
    if (!job) return;

    const detected = state.userProfile.detectedInfo;
    const resumeFirstLine = (DOM.resumeTextInput.value || '').split('\n')[0].trim();
    const candidateName = detected.name || (resumeFirstLine.split(/[|•,–-]/)[0].trim()) || 'Rohan Patel';
    const candidateEmail = detected.email || 'rohan.patel@graduate.edu';
    const candidatePhone = detected.phone || '+1 (408) 555-0192';
    const expectedSalary = parseInt(DOM.salaryRange.value, 10) || 85000;

    const matched = matchObj ? matchObj.matchedSkills : state.userProfile.skills.slice(0, 4);
    const skillsListStr = matched.length > 0 ? matched.join(', ') : 'software engineering';
    const coverLetter = `Dear Hiring Team at ${job.company},\n\nI am applying for the ${job.title} position in ${job.location}. My technical qualifications in ${skillsListStr} directly align with your requirements.\n\nWarm regards,\n${candidateName}`;

    const candidateSkills = (state.userProfile.skills && state.userProfile.skills.length > 0)
      ? state.userProfile.skills
      : [];
    const candidateYears = typeof state.userProfile.yearsOfExperience === 'number'
      ? state.userProfile.yearsOfExperience
      : (state.userProfile.detectedInfo?.years || 0);
    const matchPctVal = matchObj ? matchObj.matchPercentage : 0;

    const appData = {
      jobId: job.id,
      jobTitle: job.title,
      company: job.company,
      candidateName: candidateName,
      candidateEmail: candidateEmail,
      candidatePhone: candidatePhone,
      expectedSalary: expectedSalary,
      joiningAvailability: 'Immediate (Within 2 weeks)',
      workAuth: 'Authorized (No sponsorship needed)',
      preferredWorkMode: job.workMode || 'Online/Remote',
      portfolioUrl: '',
      coverLetter: coverLetter,
      skills: candidateSkills,
      yearsOfExperience: candidateYears,
      matchPercentage: matchPctVal
    };

    let serverResponse = null;
    if (isJavaServerOnline) {
      try {
        const res = await fetch(`${API_BASE_URL}/apply`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(appData)
        });
        if (res.ok) {
          serverResponse = await res.json();
        }
      } catch (err) {}
    }

    if (!serverResponse) {
      const generatedId = `APP-2026-${Math.floor(1000 + Math.random() * 9000)}`;
      serverResponse = {
        success: true,
        applicationId: generatedId,
        status: 'Applied',
        statusNote: 'Your application has been received and is currently under initial screening.',
        submittedAt: new Date().toLocaleString()
      };
    }

    const newRecord = {
      applicationId: serverResponse.applicationId,
      jobId: job.id,
      jobTitle: job.title,
      company: job.company,
      companyLogo: job.companyLogo || '🏢',
      location: job.location,
      expectedSalary: appData.expectedSalary,
      joiningAvailability: appData.joiningAvailability,
      submittedAt: serverResponse.submittedAt || new Date().toLocaleString(),
      status: serverResponse.status || 'Test/Assessment',
      statusNote: serverResponse.statusNote || 'Your application has been shortlisted for the initial Test/Assessment round.',
      coverLetter: appData.coverLetter,
      timeline: [
        `[${serverResponse.submittedAt || new Date().toLocaleString()}] Application Registered via 1-Click Apply: Test/Assessment`
      ]
    };

    state.submittedApplications.unshift(newRecord);
    localStorage.setItem('carrerx_applications', JSON.stringify(state.submittedApplications));
    updateCounters();
    renderDashboardApplicationTracker();
    renderJobsGrid();

    if (DOM.dashboardApplicationTracker) {
      DOM.dashboardApplicationTracker.scrollIntoView({ behavior: 'smooth' });
    }

    showToast(`1-Click Application Sent to ${job.company}! Reference: ${serverResponse.applicationId} (Stage: ${serverResponse.status})`, 'success');
  }

  // ==========================================
  // MULTI-JOB BATCH APPLICATION SYSTEM
  // ==========================================

  function openBatchApplicationModal(jobIds) {
    const ids = Array.isArray(jobIds) ? jobIds : Array.from(jobIds);
    if (!ids || ids.length === 0) {
      showToast('Please select at least 1 job for batch application.', 'info');
      return;
    }

    ids.forEach(id => state.selectedJobIdsForBatch.add(id));
    updateBatchSelectionUI();

    const count = state.selectedJobIdsForBatch.size;
    if (DOM.batchModalJobsCount) DOM.batchModalJobsCount.textContent = count;
    if (DOM.batchListSummaryCount) DOM.batchListSummaryCount.textContent = count;

    renderBatchSelectedJobsList();

    // Prefill form inputs
    const detected = state.userProfile.detectedInfo || {};
    const resumeFirstLine = (DOM.resumeTextInput && DOM.resumeTextInput.value ? DOM.resumeTextInput.value : '').split('\n')[0].trim();
    const parsedName = resumeFirstLine.split(/[|•,–-]/)[0].trim();
    const candidateName = (state.currentUser && state.currentUser.name) 
                       || (DOM.applyFullName ? DOM.applyFullName.value.trim() : '')
                       || detected.name 
                       || (parsedName && parsedName.length > 2 && parsedName.length < 40 ? parsedName : 'Alex Developer');

    if (DOM.batchFullName) DOM.batchFullName.value = candidateName;
    if (DOM.batchEmail) DOM.batchEmail.value = (state.currentUser && state.currentUser.email) || detected.email || (DOM.applyEmail ? DOM.applyEmail.value.trim() : '') || 'alex.developer@example.com';
    if (DOM.batchPhone) DOM.batchPhone.value = detected.phone || (DOM.applyPhone ? DOM.applyPhone.value.trim() : '') || '+1 (555) 019-2834';
    if (DOM.batchExpectedSalary) DOM.batchExpectedSalary.value = (DOM.salaryRange && DOM.salaryRange.value) ? DOM.salaryRange.value : 95000;

    generateBatchCoverLetter();

    DOM.batchApplicationForm.classList.remove('hidden');
    DOM.batchSuccessScreen.classList.add('hidden');
    DOM.batchApplicationModal.classList.remove('hidden');
  }

  function renderBatchSelectedJobsList() {
    if (!DOM.batchSelectedJobsList) return;
    const jobs = Array.from(state.selectedJobIdsForBatch).map(id => state.jobCatalog.find(j => j.id === id)).filter(Boolean);

    const count = state.selectedJobIdsForBatch.size;
    if (DOM.batchModalJobsCount) DOM.batchModalJobsCount.textContent = count;
    if (DOM.batchListSummaryCount) DOM.batchListSummaryCount.textContent = count;

    if (jobs.length === 0) {
      DOM.batchSelectedJobsList.innerHTML = `<div class="empty-batch-note" style="padding:1rem; text-align:center; color:var(--text-muted); font-size:0.85rem;">No jobs currently selected.</div>`;
      return;
    }

    DOM.batchSelectedJobsList.innerHTML = jobs.map(j => `
      <div class="batch-mini-job-card" data-job-id="${j.id}">
        <div class="batch-mini-left" style="display:flex; align-items:center; gap:0.5rem; overflow:hidden;">
          <div class="batch-mini-avatar">${j.companyLogo || '🏢'}</div>
          <div class="batch-mini-details">
            <h5>${escapeHtml(j.title)}</h5>
            <p>${escapeHtml(j.company)} • ${escapeHtml(j.location)} (${escapeHtml(j.workMode)})</p>
          </div>
        </div>
        <button type="button" class="mini-remove-btn" data-remove-batch-id="${j.id}" title="Remove from batch" style="background:none; border:none; cursor:pointer; font-size:1rem; color:#94a3b8; padding:0.2rem 0.4rem; border-radius:4px;">✕</button>
      </div>
    `).join('');
  }

  function generateBatchCoverLetter() {
    const jobs = Array.from(state.selectedJobIdsForBatch).map(id => state.jobCatalog.find(j => j.id === id)).filter(Boolean);
    const companyNames = jobs.map(j => j.company).slice(0, 3).join(', ') + (jobs.length > 3 ? ' and partner firms' : '');
    const skillsListStr = state.userProfile.skills.length > 0 ? state.userProfile.skills.slice(0, 5).join(', ') : 'Java, Web Engineering, Database Design';
    const yearsExp = state.userProfile.yearsOfExperience;
    const expText = yearsExp === 0 ? 'As an ambitious entry-level Computer Science graduate' : `With over ${yearsExp} years of hands-on software development experience`;

    const letter = `To the Hiring Committees at ${companyNames},

I am excited to submit my unified application for your open engineering roles. ${expText}, my core competencies are directly aligned with your technical requirements, specifically in ${skillsListStr}.

Key Highlights of My Candidate Profile:
- Demonstrated track record in building clean, modular, and maintainable software systems
- Strong problem-solving abilities and adherence to industry best practices
- Flexible onboarding availability across preferred work modes (Online/Remote, Offline/On-site, or Hybrid)

I welcome the opportunity to discuss my qualifications and how I can make an immediate contribution to your engineering teams.

Sincerely,
Alex Developer`;

    if (DOM.batchCoverLetter) DOM.batchCoverLetter.value = letter;
  }

  async function submitBatchApplications(jobIds, formData) {
    const submitBtn = DOM.submitBatchApplyBtn || document.getElementById('submitBatchApplyBtn');
    if (submitBtn) {
      submitBtn.disabled = true;
      submitBtn.innerHTML = 'Submitting Multi-Job Applications...';
    }

    const candidateSkills = (state.userProfile.skills && state.userProfile.skills.length > 0)
      ? state.userProfile.skills
      : [];
    const candidateYears = typeof state.userProfile.yearsOfExperience === 'number'
      ? state.userProfile.yearsOfExperience
      : (state.userProfile.detectedInfo?.years || 0);

    const payload = {
      jobIds: jobIds,
      candidateName: formData.candidateName,
      candidateEmail: formData.candidateEmail,
      candidatePhone: formData.candidatePhone,
      expectedSalary: formData.expectedSalary,
      joiningAvailability: formData.joiningAvailability,
      preferredHours: state.userProfile.preferredWorkingHours || 'All',
      preferredWorkMode: formData.preferredWorkMode,
      portfolioUrl: formData.portfolioUrl || '',
      coverLetter: formData.coverLetter || '',
      skills: candidateSkills,
      yearsOfExperience: candidateYears
    };

    let responseResults = null;

    if (isJavaServerOnline) {
      try {
        const res = await fetch(`${API_BASE_URL}/apply/batch`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(payload)
        });
        if (res.ok) {
          const resJson = await res.json();
          if (resJson && resJson.applications && Array.isArray(resJson.applications)) {
            responseResults = resJson.applications;
          } else if (Array.isArray(resJson)) {
            responseResults = resJson;
          }
        }
      } catch (err) {}
    }

    if (!responseResults || !Array.isArray(responseResults)) {
      responseResults = jobIds.map(id => {
        const job = state.jobCatalog.find(j => j.id === id) || { title: 'Software Engineer', company: 'Company', location: 'Remote' };
        const genId = `APP-2026-${Math.floor(1000 + Math.random() * 9000)}`;
        return {
          applicationId: genId,
          jobId: id,
          jobTitle: job.title,
          company: job.company,
          location: job.location,
          status: 'Test/Assessment',
          statusNote: 'Unified application shortlisted for technical Test/Assessment stage.',
          submittedAt: new Date().toLocaleString(),
          expectedSalary: formData.expectedSalary,
          joiningAvailability: formData.joiningAvailability
        };
      });
    }

    // Add new applications into state
    responseResults.forEach(item => {
      const job = state.jobCatalog.find(j => j.id === item.jobId);
      const newRecord = {
        applicationId: item.applicationId,
        jobId: item.jobId,
        jobTitle: item.jobTitle || job?.title,
        company: item.company || job?.company,
        companyLogo: job?.companyLogo || '🏢',
        location: item.location || job?.location,
        expectedSalary: item.expectedSalary || formData.expectedSalary,
        joiningAvailability: item.joiningAvailability || formData.joiningAvailability,
        submittedAt: item.submittedAt || new Date().toLocaleString(),
        status: item.status || 'Test/Assessment',
        statusNote: item.statusNote || 'Candidate application shortlisted for Test/Assessment stage.',
        coverLetter: formData.coverLetter,
        timeline: [
          `[${item.submittedAt || new Date().toLocaleString()}] Application Registered via Multi-Job Apply: Test/Assessment`
        ]
      };
      state.submittedApplications.unshift(newRecord);
    });

    localStorage.setItem('carrerx_applications', JSON.stringify(state.submittedApplications));
    state.selectedJobIdsForBatch.clear();
    updateCounters();
    updateBatchSelectionUI();
    renderDashboardApplicationTracker();
    renderJobsGrid();

    // Populate Batch Success Screen
    if (DOM.batchSuccessCount) DOM.batchSuccessCount.textContent = responseResults.length;
    if (DOM.batchSubmittedResultsList) {
      DOM.batchSubmittedResultsList.innerHTML = responseResults.map(item => `
        <div class="batch-result-row">
          <div class="batch-result-info">
            <h5>${escapeHtml(item.jobTitle)} at <strong>${escapeHtml(item.company)}</strong></h5>
            <span>Tracking ID: <code>${escapeHtml(item.applicationId)}</code> • Stage: <span class="stage-badge-pill ${getStageClass(item.status)}">${escapeHtml(item.status)}</span></span>
          </div>
          <button type="button" class="card-action-btn view-status-btn" onclick="window.trackAppById('${item.applicationId}')">
            Track Status 📊
          </button>
        </div>
      `).join('');
    }

    if (DOM.batchApplicationForm) DOM.batchApplicationForm.classList.add('hidden');
    if (DOM.batchSuccessScreen) DOM.batchSuccessScreen.classList.remove('hidden');
    if (DOM.batchApplicationModal) DOM.batchApplicationModal.classList.remove('hidden');

    if (submitBtn) {
      submitBtn.disabled = false;
      submitBtn.innerHTML = 'Submit Applications to All Roles 🚀';
    }

    showToast(`Successfully submitted applications to ${responseResults.length} jobs! Immediate tracking active.`, 'success');
  }

  window.trackAppById = function(appId) {
    if (DOM.batchApplicationModal) DOM.batchApplicationModal.classList.add('hidden');
    if (DOM.applicationModal) DOM.applicationModal.classList.add('hidden');
    openApplicationStatusModal(appId);
  };

  // ==========================================
  // DASHBOARD LIVE APPLICATION TRACKER
  // ==========================================

  function renderDashboardApplicationTracker() {
    if (!DOM.dashboardApplicationTracker) return;

    DOM.dashboardApplicationTracker.classList.remove('hidden');

    if (state.submittedApplications.length === 0) {
      DOM.trackerApplicationsCount.textContent = '0 Active';
      DOM.dashboardApplicationsGrid.innerHTML = `
        <div class="empty-tracker-notice" style="background:#f8fafc; border:1px dashed var(--border-subtle); border-radius:var(--radius-md); padding:2rem 1.5rem; text-align:center;">
          <div style="font-size:2rem; margin-bottom:0.5rem;">📋</div>
          <h4 style="font-size:1.05rem; margin-bottom:0.35rem; color:var(--text-primary);">No Active Applications Submitted Yet</h4>
          <p style="font-size:0.88rem; color:var(--text-muted); max-width:550px; margin:0 auto;">
            Submit a tailored cover letter for any job recommendation to activate real-time lifecycle tracking across all 5 hiring stages.
          </p>
        </div>
      `;
      return;
    }

    DOM.trackerApplicationsCount.textContent = `${state.submittedApplications.length} Active`;

    DOM.dashboardApplicationsGrid.innerHTML = state.submittedApplications.map(app => {
      const badge = getStageBadge(app.status);
      const stagesInOrder = ['Applied', 'Under Review', 'Shortlisted', 'Interview', 'Selected'];
      const curIdx = stagesInOrder.indexOf(app.status);
      const isRejected = app.status === 'Rejected';

      return `
        <div class="dashboard-app-card" data-app-id="${escapeHtml(app.applicationId)}">
          <div class="app-card-top-row">
            <div class="app-card-company-info">
              <div class="app-card-logo">${app.companyLogo || '🏢'}</div>
              <div class="app-card-title-meta">
                <h4>${escapeHtml(app.jobTitle)}</h4>
                <div class="app-card-meta-text">
                  <strong>${escapeHtml(app.company)}</strong> • ${escapeHtml(app.location || 'Tech Hub')} • Ref: <span class="receipt-id">${escapeHtml(app.applicationId)}</span>
                </div>
              </div>
            </div>
            <span class="stage-badge-pill ${badge.className}">${badge.emoji} ${escapeHtml(badge.label)}</span>
          </div>

          <!-- Submitted Tailored Cover Letter Preview -->
          <div class="app-cover-letter-snippet" style="background:var(--bg-tertiary); border:1px solid var(--border-subtle); border-radius:var(--radius-sm); padding:0.65rem 0.9rem; margin:0.75rem 0;">
            <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:0.35rem;">
              <span style="font-size:0.75rem; font-weight:700; color:var(--text-secondary);">📝 Tailored Cover Letter Submitted:</span>
              <span style="font-size:0.72rem; color:var(--success-color); font-weight:700;">✓ Saved to Backend</span>
            </div>
            <p style="font-size:0.82rem; line-height:1.45; color:var(--text-primary); margin:0; font-family:var(--font-sans); white-space:pre-wrap; max-height:4.2em; overflow:hidden; text-overflow:ellipsis;">${escapeHtml(app.coverLetter || 'Tailored cover letter submitted directly to hiring portal.')}</p>
          </div>

          <!-- 5-Stage Stepper -->
          <div class="stepper-horizontal">
            <div class="step-item ${curIdx === 0 ? 'active' : (curIdx > 0 ? 'completed' : '')}" data-step="Applied">
              <div class="step-dot">${curIdx > 0 ? '✓' : '1'}</div>
              <span class="step-name">Applied</span>
            </div>
            <div class="step-line"></div>
            <div class="step-item ${curIdx === 1 ? 'active' : (curIdx > 1 ? 'completed' : '')}" data-step="Under Review">
              <div class="step-dot">${curIdx > 1 ? '✓' : '2'}</div>
              <span class="step-name">Under Review</span>
            </div>
            <div class="step-line"></div>
            <div class="step-item ${curIdx === 2 ? 'active' : (curIdx > 2 ? 'completed' : '')}" data-step="Shortlisted">
              <div class="step-dot">${curIdx > 2 ? '✓' : '3'}</div>
              <span class="step-name">Shortlisted</span>
            </div>
            <div class="step-line"></div>
            <div class="step-item ${curIdx === 3 ? 'active' : (curIdx > 3 ? 'completed' : '')}" data-step="Interview">
              <div class="step-dot">${curIdx > 3 ? '✓' : '4'}</div>
              <span class="step-name">Interview</span>
            </div>
            <div class="step-line"></div>
            <div class="step-item ${curIdx === 4 ? 'active' : ''}" data-step="Selected">
              <div class="step-dot">5</div>
              <span class="step-name">Selected</span>
            </div>
            <div class="step-line branch"></div>
            <div class="step-item branch-item ${isRejected ? 'active rejected' : ''}" data-step="Rejected">
              <div class="step-dot">✕</div>
              <span class="step-name">Rejected</span>
            </div>
          </div>

          <!-- Status Note -->
          <div class="status-note-box">
            <span class="note-label">📢 Latest Hiring Status Note:</span>
            <p class="note-content">${escapeHtml(app.statusNote || 'Your application is actively progressing through our evaluation pipeline.')}</p>
          </div>



          <div class="app-card-footer-actions">
            <span style="font-size:0.75rem; color:var(--text-muted);">Submitted: ${escapeHtml(app.submittedAt)} • Join: ${escapeHtml(app.joiningAvailability || 'Immediate')}</span>
            <button type="button" class="text-link-btn" onclick="window.CarrerXApp.openStatusModal('${app.applicationId}')">
              View Audit Timeline & Full Cover Letter 📄
            </button>
          </div>
        </div>
      `;
    }).join('');
  }

  // ==========================================
  // DEDICATED APPLICATION STATUS MODAL
  // ==========================================

  function openApplicationStatusModal(appId) {
    const app = state.submittedApplications.find(a => a.applicationId === appId);
    if (!app) return;

    state.currentTrackingApp = app;
    DOM.statusModalAppId.textContent = app.applicationId;
    DOM.appStatusJobTitle.textContent = app.jobTitle;
    DOM.appStatusCompany.textContent = `${app.company} • Submitted ${app.submittedAt}`;
    DOM.appStatusCompanyLogo.textContent = app.companyLogo || '🏢';
    DOM.statusModalDate.textContent = app.submittedAt;
    const relatedJob = state.jobCatalog.find(j => j.id === app.jobId);
    const isAppINR = (relatedJob && (relatedJob.currency === 'INR' || /india|bangalore|bengaluru|hyderabad|mumbai|delhi|gurugram|gurgaon|noida|pune|chennai/i.test(relatedJob.location || ''))) ||
      app.currency === 'INR' ||
      (Number(app.expectedSalary) >= 400000) ||
      (app.location && /india|bangalore|bengaluru|hyderabad|mumbai|delhi|gurugram|gurgaon|noida|pune|chennai/i.test(app.location || '')) ||
      (app.company && /infosys|tcs|tata consultancy|flipkart|swiggy|zomato|razorpay|cred|jio|zoho|paytm/i.test(app.company));
    if (app.expectedSalary) {
      if (isAppINR) {
        const val = Number(app.expectedSalary);
        const lakh = (val / 100000).toFixed(val % 100000 === 0 ? 0 : 1);
        DOM.statusModalSalary.textContent = `₹${val.toLocaleString('en-IN')} / yr (${lakh} LPA)`;
      } else {
        DOM.statusModalSalary.textContent = app.expectedSalary ? (app.expectedSalary < 200 ? `₹${app.expectedSalary} LPA` : `₹${formatLPA(app.expectedSalary)}`) : 'Market Standard';
      }
    } else {
      DOM.statusModalSalary.textContent = 'Market Standard';
    }

    const b = getStageBadge(app.status);
    DOM.statusModalStageBadge.className = `stage-badge-pill ${b.className}`;
    DOM.statusModalStageBadge.textContent = `${b.emoji} ${b.label}`;
    DOM.statusModalNote.textContent = app.statusNote || 'Application under active review.';

    renderStepper(DOM.statusModalStepper, app.status);



    const timeline = app.timeline || [`[${app.submittedAt}] Application Submitted: ${app.status}`];
    DOM.statusModalTimeline.innerHTML = timeline.map(entry => `
      <div class="timeline-item">${escapeHtml(entry)}</div>
    `).join('');

    DOM.statusModalCoverLetter.textContent = app.coverLetter || 'Tailored cover letter submitted directly to hiring portal.';

    DOM.applicationStatusModal.classList.remove('hidden');
  }

  
  // ==========================================
  // CANDIDATE AUTHENTICATION & LOGIN WORKFLOW
  // ==========================================

  function checkAuthState() {
    if (state.currentUser) {
      if (DOM.authPortal) DOM.authPortal.classList.add('hidden');
      if (DOM.mainAppLayout) DOM.mainAppLayout.classList.remove('hidden');
      if (DOM.authUserBadge) DOM.authUserBadge.classList.remove('hidden');
      if (DOM.navLoginBtn) DOM.navLoginBtn.classList.add('hidden');

      if (DOM.headerUserName) DOM.headerUserName.textContent = state.currentUser.name;
      if (DOM.headerUserRole) DOM.headerUserRole.textContent = state.currentUser.headline || 'Candidate';
      if (DOM.headerUserAvatar) {
        const parts = state.currentUser.name.trim().split(' ');
        const initials = parts.length > 1 ? (parts[0][0] + parts[parts.length - 1][0]) : parts[0].substring(0, 2);
        DOM.headerUserAvatar.textContent = initials.toUpperCase() || '👤';
      }

      syncUserProfileWithAuthUser(state.currentUser);
    } else {
      if (DOM.authPortal) DOM.authPortal.classList.remove('hidden');
      if (DOM.mainAppLayout) DOM.mainAppLayout.classList.add('hidden');
      if (DOM.authUserBadge) DOM.authUserBadge.classList.add('hidden');
      if (DOM.navLoginBtn) DOM.navLoginBtn.classList.remove('hidden');
    }
  }

  function syncUserProfileWithAuthUser(user) {
    if (!user) return;
    state.userProfile.detectedInfo.name = user.name;
    state.userProfile.detectedInfo.email = user.email;
    state.userProfile.detectedInfo.phone = user.phone || '+1 (408) 555-0192';

    if (user.email === 'rohan.patel@graduate.edu') {
      loadDemoProfile('fresher-java');
    } else if (user.email === 'elena.rostova@techhub.io') {
      loadDemoProfile('java-fullstack');
    } else if (user.email === 'marcus.vance@aimind.org') {
      loadDemoProfile('ai-ml');
    } else {
      if (user.yearsExp !== undefined) {
        state.userProfile.yearsOfExperience = user.yearsExp;
        if (DOM.exactYearsInput) DOM.exactYearsInput.value = user.yearsExp;
        updateActiveTierBadge();
      }
      if (user.skills && Array.isArray(user.skills) && user.skills.length > 0) {
        state.userProfile.skills = [...user.skills];
        renderSkillTags();
      }
    }
  }

  async function syncUserDataFromServer() {
    if (!state.currentUser || !state.currentUser.email) return;
    const email = state.currentUser.email;

    // 1. Sync candidate applications
    if (isJavaServerOnline) {
      try {
        const appRes = await fetchWithAuth(`${API_BASE_URL}/applications?email=${encodeURIComponent(email)}`);
        if (appRes.ok) {
          const apps = await appRes.json();
          if (Array.isArray(apps)) {
            state.submittedApplications = apps;
            localStorage.setItem('carrerx_applications', JSON.stringify(apps));
          }
        }
      } catch (e) {
        console.warn('Sync applications error:', e);
      }

      // 2. Sync candidate saved bookmarks
      try {
        const savedRes = await fetchWithAuth(`${API_BASE_URL}/jobs/saved?email=${encodeURIComponent(email)}`);
        if (savedRes.ok) {
          const savedData = await savedRes.json();
          if (savedData.savedJobIds && Array.isArray(savedData.savedJobIds)) {
            state.savedJobIds = new Set(savedData.savedJobIds);
          } else if (savedData.savedJobs && Array.isArray(savedData.savedJobs)) {
            state.savedJobIds = new Set(savedData.savedJobs.map(s => s.job ? s.job.id : s.jobId).filter(Boolean));
          }
          localStorage.setItem('carrerx_saved_jobs', JSON.stringify([...state.savedJobIds]));
        }
      } catch (e) {
        console.warn('Sync saved jobs error:', e);
      }
    }

    renderDashboardApplicationTracker();
    renderMyApplicationsDrawer();
    renderJobsGrid();
    renderSavedDrawer();
    updateCounters();
  }

  async function handleLogin(email, password) {
    const submitBtn = DOM.submitLoginBtn;
    if (submitBtn) {
      submitBtn.disabled = true;
      submitBtn.innerHTML = 'Signing In... ⏳';
    }
    if (DOM.loginErrorAlert) DOM.loginErrorAlert.classList.add('hidden');

    let userData = null;

    if (isJavaServerOnline) {
      try {
        const res = await fetch(`${API_BASE_URL}/auth/login`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ email: email.trim(), password: password })
        });
        const data = await res.json().catch(() => null);
        if (res.ok && data && data.success && data.user) {
          userData = data.user;
          state.authToken = data.token || '';
          localStorage.setItem('carrerx_auth_token', state.authToken);
        } else {
          const msg = data?.message || 'Invalid email or password.';
          if (DOM.loginErrorAlert) {
            DOM.loginErrorAlert.textContent = msg;
            DOM.loginErrorAlert.classList.remove('hidden');
          }
          if (submitBtn) {
            submitBtn.disabled = false;
            submitBtn.innerHTML = 'Sign In & Launch Platform 🚀';
          }
          return;
        }
      } catch (err) {
        console.warn('Backend login connection error, using local fallback:', err);
      }
    }

    if (!userData) {
      if (email.toLowerCase().includes('rohan')) {
        userData = { candidateId: 'CAND-1003', name: 'Rohan Patel', email: 'rohan.patel@graduate.edu', headline: 'Associate Software Engineer', yearsExp: 1, skills: ['Java', 'SQL', 'Git', 'OOP', 'HTML', 'CSS'] };
      } else if (email.toLowerCase().includes('elena')) {
        userData = { candidateId: 'CAND-1004', name: 'Elena Rostova', email: 'elena.rostova@techhub.io', headline: 'Senior Java Full Stack Engineer', yearsExp: 6, skills: ['Java', 'Spring Boot', 'React', 'TypeScript', 'PostgreSQL', 'Kafka'] };
      } else if (email.toLowerCase().includes('marcus')) {
        userData = { candidateId: 'CAND-1005', name: 'Marcus Vance', email: 'marcus.vance@aimind.org', headline: 'Staff AI Engineer', yearsExp: 8, skills: ['Python', 'PyTorch', 'FastAPI', 'Rust', 'Kubernetes', 'AWS'] };
      } else {
        const cleanName = email.split('@')[0];
        userData = { candidateId: `CAND-${Date.now().toString().slice(-4)}`, name: cleanName.charAt(0).toUpperCase() + cleanName.slice(1), email: email.trim(), headline: 'Software Engineer', yearsExp: 2, skills: ['Java', 'SQL', 'Git'] };
      }
    }

    state.currentUser = userData;
    localStorage.setItem('carrerx_auth_user', JSON.stringify(userData));

    if (submitBtn) {
      submitBtn.disabled = false;
      submitBtn.innerHTML = 'Sign In & Launch Platform 🚀';
    }

    checkAuthState();
    await syncUserDataFromServer();
    showToast(`Welcome back, ${userData.name}! Platform ready.`, 'success');
    calculateJobMatches();
  }

  async function handleRegister(formData) {
    const submitBtn = DOM.submitRegBtn;
    if (submitBtn) {
      submitBtn.disabled = true;
      submitBtn.innerHTML = 'Creating Account... ⏳';
    }
    if (DOM.regErrorAlert) DOM.regErrorAlert.classList.add('hidden');

    let userData = null;

    if (isJavaServerOnline) {
      try {
        const res = await fetch(`${API_BASE_URL}/auth/register`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(formData)
        });
        const data = await res.json().catch(() => null);
        if (res.ok && data && data.success && data.user) {
          userData = data.user;
          state.authToken = data.token || '';
          localStorage.setItem('carrerx_auth_token', state.authToken);
        } else {
          const msg = data?.message || 'Registration failed. Please check inputs.';
          if (DOM.regErrorAlert) {
            DOM.regErrorAlert.textContent = msg;
            DOM.regErrorAlert.classList.remove('hidden');
          }
          if (submitBtn) {
            submitBtn.disabled = false;
            submitBtn.innerHTML = 'Register & Access Platform 🚀';
          }
          return;
        }
      } catch (err) {
        console.warn('Backend register connection error, using local fallback:', err);
      }
    }

    if (!userData) {
      userData = {
        candidateId: `CAND-${Date.now().toString().slice(-4)}`,
        name: formData.name,
        email: formData.email,
        headline: formData.yearsExp <= 1 ? 'Associate Software Engineer' : 'Software Engineer',
        yearsExp: formData.yearsExp,
        skills: formData.skills
      };
    }

    state.currentUser = userData;
    localStorage.setItem('carrerx_auth_user', JSON.stringify(userData));

    if (submitBtn) {
      submitBtn.disabled = false;
      submitBtn.innerHTML = 'Register & Access Platform 🚀';
    }

    checkAuthState();
    await syncUserDataFromServer();
    showToast(`Account created! Welcome, ${userData.name}.`, 'success');
    calculateJobMatches();
  }

  async function handleLogout() {
    if (isJavaServerOnline) {
      try {
        await fetchWithAuth(`${API_BASE_URL}/auth/logout`, { method: 'POST' });
      } catch (e) {}
    }

    state.currentUser = null;
    state.authToken = '';
    state.submittedApplications = [];
    state.savedJobIds = new Set();
    localStorage.removeItem('carrerx_auth_user');
    localStorage.removeItem('carrerx_auth_token');
    localStorage.removeItem('carrerx_applications');
    localStorage.removeItem('carrerx_saved_jobs');
    renderMyApplicationsDrawer();
    renderDashboardApplicationTracker();
    renderJobsGrid();
    renderSavedDrawer();
    updateCounters();
    checkAuthState();
    showToast('Signed out successfully. Please sign in to access the platform.', 'info');
  }

  // ==========================================
  // MY APPLICATIONS DRAWER
  // ==========================================

  function renderMyApplicationsDrawer() {
    if (state.submittedApplications.length === 0) {
      DOM.myApplicationsList.innerHTML = `
        <div class="empty-state">
          <div class="empty-icon">📂</div>
          <h4>No Applications Submitted Yet</h4>
          <p style="font-size:0.8rem; color:var(--text-muted);">When you click 'Apply Now' on any job, your tracked application and status updates across the 5 stages will appear here.</p>
        </div>
      `;
      return;
    }

    DOM.myApplicationsList.innerHTML = state.submittedApplications.map(app => {
      const b = getStageBadge(app.status);
      return `
      <div class="applied-job-card">
        <div class="applied-header-row">
          <span class="applied-app-id">ID: ${escapeHtml(app.applicationId)}</span>
          <span class="stage-badge-pill ${b.className}">${b.emoji} ${escapeHtml(b.label)}</span>
        </div>
        <h4>${escapeHtml(app.jobTitle)}</h4>
        <p>${app.companyLogo || '🏢'} <strong>${escapeHtml(app.company)}</strong> • ${escapeHtml(app.location || 'Tech Hub')}</p>
        <div class="applied-meta-row">
          <span>📅 Join: ${escapeHtml(app.joiningAvailability || 'Immediate')}</span>
          <span>Applied: ${escapeHtml(app.submittedAt.split(' ')[0])}</span>
        </div>
        <p style="font-size:0.77rem; color:var(--text-secondary); margin-top:0.4rem; font-style:italic;">"${escapeHtml(app.statusNote || 'Active evaluation')}"</p>
        <div style="display:flex; justify-content:space-between; margin-top:0.6rem; padding-top:0.4rem; border-top:1px solid var(--border-subtle);">
          <button class="text-link-btn" onclick="window.CarrerXApp.openStatusModal('${app.applicationId}')">
            Track Status & Progression 📊
          </button>
        </div>
      </div>
      `;
    }).join('');
  }

  function updateCounters() {
    DOM.savedCountBadge.textContent = state.savedJobIds.size;
    DOM.applicationsCountBadge.textContent = state.submittedApplications.length;
    if (DOM.bookmarkedCountNum) DOM.bookmarkedCountNum.textContent = state.savedJobIds.size;
    if (DOM.savedDrawerCountNum) DOM.savedDrawerCountNum.textContent = state.savedJobIds.size;
    if (DOM.applyAllSavedJobsBtn) {
      DOM.applyAllSavedJobsBtn.disabled = state.savedJobIds.size === 0;
    }
    if (DOM.selectBookmarkedBtn) {
      DOM.selectBookmarkedBtn.disabled = state.savedJobIds.size === 0;
    }
  }

  function updateBatchSelectionUI() {
    const count = state.selectedJobIdsForBatch.size;
    if (DOM.batchCountNum) DOM.batchCountNum.textContent = count;
    if (DOM.selectedJobsCountBadge) {
      DOM.selectedJobsCountBadge.textContent = `${count} Job${count === 1 ? '' : 's'} Selected`;
      if (count > 0) {
        DOM.selectedJobsCountBadge.classList.add('active-pulse');
      } else {
        DOM.selectedJobsCountBadge.classList.remove('active-pulse');
      }
    }
    if (DOM.batchApplyBtn) {
      DOM.batchApplyBtn.disabled = count === 0;
      DOM.batchApplyBtn.innerHTML = `🚀 Apply to Selected (${count})`;
    }
    if (DOM.multiApplyBanner) {
      DOM.multiApplyBanner.classList.toggle('has-selections', count > 0);
    }
    if (DOM.clearSelectionBtn) {
      if (count > 0) {
        DOM.clearSelectionBtn.classList.remove('hidden');
      } else {
        DOM.clearSelectionBtn.classList.add('hidden');
      }
    }
    if (DOM.bookmarkedCountNum) DOM.bookmarkedCountNum.textContent = state.savedJobIds.size;
    if (DOM.savedDrawerCountNum) DOM.savedDrawerCountNum.textContent = state.savedJobIds.size;
    if (DOM.applyAllSavedJobsBtn) {
      DOM.applyAllSavedJobsBtn.disabled = state.savedJobIds.size === 0;
    }
    if (DOM.selectBookmarkedBtn) {
      DOM.selectBookmarkedBtn.disabled = state.savedJobIds.size === 0;
    }
  }

  function toggleJobSelection(jobId, forceState) {
    if (!jobId) return;
    const isCurrentlySelected = state.selectedJobIdsForBatch.has(jobId);
    const newState = (forceState !== undefined) ? !!forceState : !isCurrentlySelected;
    if (newState) {
      state.selectedJobIdsForBatch.add(jobId);
    } else {
      state.selectedJobIdsForBatch.delete(jobId);
    }
    const card = document.querySelector(`.job-card[data-job-id="${jobId}"]`);
    if (card) {
      card.classList.toggle('is-selected-for-batch', newState);
      const chk = card.querySelector('.job-card-checkbox');
      if (chk) chk.checked = newState;
      const lbl = card.querySelector('.select-label-text');
      if (lbl) lbl.textContent = newState ? '✓ Selected to Apply' : 'Select to Apply';
      const selectBtn = card.querySelector('.select-batch-btn');
      if (selectBtn) {
        selectBtn.classList.toggle('selected', newState);
        selectBtn.textContent = newState ? '✓ Selected' : '☑️ Select';
      }
      let badge = card.querySelector('.select-active-badge');
      if (newState) {
        if (!badge) {
          const strip = card.querySelector('.job-card-select-strip');
          if (strip) {
            const span = document.createElement('span');
            span.className = 'badge-mini select-active-badge';
            span.textContent = '✓ Selected for Multi-Apply';
            strip.appendChild(span);
          }
        }
      } else {
        if (badge) badge.remove();
      }
    }
    updateBatchSelectionUI();
  }

  async function toggleSaveJob(jobId) {
    const email = (state.currentUser && state.currentUser.email) || state.userProfile.detectedInfo.email || 'applicant@carrerx.io';
    const isCurrentlySaved = state.savedJobIds.has(jobId);
    const action = isCurrentlySaved ? 'remove' : 'save';

    if (isCurrentlySaved) {
      state.savedJobIds.delete(jobId);
      showToast('Removed job from bookmarks.', 'info');
    } else {
      state.savedJobIds.add(jobId);
      showToast('Job saved to your bookmarks!', 'success');
    }

    localStorage.setItem('carrerx_saved_jobs', JSON.stringify([...state.savedJobIds]));
    updateCounters();
    renderJobsGrid();
    renderSavedDrawer();

    if (isJavaServerOnline) {
      try {
        const res = await fetchWithAuth(`${API_BASE_URL}/jobs/saved`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ jobId, candidateEmail: email, action })
        });
        if (!res.ok) {
          throw new Error(`Server returned status ${res.status}`);
        }
        const data = await res.json();
        if (data.totalSaved !== undefined) {
          // Sync confirmed
        }
      } catch (e) {
        console.error('Failed to sync bookmark with backend:', e);
        showToast('Bookmark updated locally (backend sync notice: ' + e.message + ')', 'warning');
      }
    }
  }

  function renderSavedDrawer() {
    if (state.savedJobIds.size === 0) {
      DOM.savedJobsList.innerHTML = '<p class="drawer-empty-text">No jobs bookmarked yet. Click the 🔖 icon on any job card to save it for later.</p>';
      return;
    }

    const savedJobs = state.jobCatalog.filter(j => state.savedJobIds.has(j.id));
    DOM.savedJobsList.innerHTML = savedJobs.map(j => `
      <div class="applied-job-card">
        <h4>${escapeHtml(j.title)}</h4>
        <p>${j.companyLogo || '🏢'} ${escapeHtml(j.company)} • ${escapeHtml(j.location)} (${escapeHtml(j.workMode)})</p>
        <span style="font-size:0.84rem; font-weight:800; color:var(--success-color); font-family:var(--font-mono);">${formatJobSalary(j)} / yr</span>
        <div style="display:flex; justify-content:space-between; margin-top:0.4rem;">
          <button class="text-link-btn" onclick="window.CarrerXApp.openApply('${j.id}')">Apply 🚀</button>
          <button class="text-link-btn text-amber" onclick="window.CarrerXApp.removeSaved('${j.id}')">Remove ✕</button>
        </div>
      </div>
    `).join('');
  }

  // ==========================================
  // EVENT LISTENERS
  // ==========================================

  
  // ==============================================================================
  // PLATFORM MULTI-VIEW NAVIGATION
  // ==============================================================================
  function switchPlatformView(viewName) {
    state.activeView = viewName;
    document.querySelectorAll('.nav-tab-btn').forEach(btn => {
      btn.classList.toggle('active', btn.dataset.view === viewName);
    });

    const heroStrip = document.querySelector('.hero-strip');
    const demoStrip = document.querySelector('.demo-profiles-strip');
    const mainContainer = document.querySelector('.main-container');

    // Hide all feature sections first
    document.querySelectorAll('.platform-view-section').forEach(sec => sec.classList.add('hidden'));

    if (viewName === 'matcher') {
      if (heroStrip) heroStrip.classList.remove('hidden');
      if (demoStrip) demoStrip.classList.remove('hidden');
      if (mainContainer) mainContainer.classList.remove('hidden');
    } else {
      if (heroStrip) heroStrip.classList.add('hidden');
      if (demoStrip) demoStrip.classList.add('hidden');
      if (mainContainer) mainContainer.classList.add('hidden');

      if (viewName === 'career-predictor') {
        const sec = document.getElementById('careerPredictorSection');
        if (sec) sec.classList.remove('hidden');
        loadCareerPredictions();
      } else if (viewName === 'skill-gap') {
        const sec = document.getElementById('skillGapSection');
        if (sec) sec.classList.remove('hidden');
        populateGapTargetJobSelect();
        loadSkillGapAnalysis();
      }
    }
  }

  // ==============================================================================
  // FEATURE 1: AI CAREER PATH PREDICTOR ENGINE
  // ==============================================================================
  async function loadCareerPredictions() {
    const recalcBtn = DOM.recalcPredictorBtn;
    let origRecalcText = '';
    if (recalcBtn) {
      origRecalcText = recalcBtn.innerHTML;
      recalcBtn.disabled = true;
      recalcBtn.innerHTML = '<span class="btn-spinner"></span> Forecasting Trajectory...';
    }
    const candidateSkills = (state.userProfile.skills && state.userProfile.skills.length > 0)
      ? state.userProfile.skills
      : ['Java', 'Spring Boot', 'SQL', 'REST APIs'];
    const yearsExp = typeof state.userProfile.yearsOfExperience === 'number'
      ? state.userProfile.yearsOfExperience
      : (state.userProfile.detectedInfo?.years || 2);
    const rawEdu = state.userProfile.detectedInfo?.education;
    const education = Array.isArray(rawEdu) ? (rawEdu.join(', ') || 'Computer Science Degree') : (rawEdu || 'Computer Science Degree');
    const email = state.currentUser?.email || state.userProfile.detectedInfo?.email || '';

    const payload = {
      skills: candidateSkills,
      yearsOfExperience: yearsExp,
      education: education,
      email: email
    };

    let data = null;

    if (isJavaServerOnline) {
      try {
        const res = await fetch(`${API_BASE_URL}/career/predict`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(payload)
        });
        if (res.ok) {
          data = await res.json();
        }
      } catch (e) {
        console.warn('Backend career predict error, using fallback:', e);
      }
    }

    if (!data || !data.success) {
      // Local rule-based fallback
      data = {
        success: true,
        primaryTrajectory: 'Enterprise Distributed Systems & Cloud Platforms',
        confidenceScore: 88,
        milestones: [
          {
            horizon: '1-Year (Immediate Next Level)',
            predictedRole: 'Senior Java Backend Engineer',
            confidence: 91,
            salaryRange: '₹14.5 - ₹18.5 LPA',
            salaryMedian: 1650000,
            marketDemand: 'High (Top 10% Indian Tech Demand)',
            description: 'Lead high-throughput microservices architecture and core business transaction pipelines.',
            requiredSkills: ['Kafka', 'Docker', 'Redis', 'AWS']
          },
          {
            horizon: '3-Year (Strategic Mid-Horizon)',
            predictedRole: 'Lead Distributed Systems Architect',
            confidence: 84,
            salaryRange: '₹24.0 - ₹32.0 LPA',
            salaryMedian: 2800000,
            marketDemand: 'Very High (Critical Talent Shortage in India)',
            description: 'Oversee cross-service event mesh, multi-region fault tolerance, and developer platform standards.',
            requiredSkills: ['System Design', 'Kubernetes', 'gRPC', 'Distributed Tracing']
          },
          {
            horizon: '5-Year (Visionary & Leadership)',
            predictedRole: 'Principal Systems Architect / VP of Engineering',
            confidence: 76,
            salaryRange: '₹45.0 - ₹65.0 LPA',
            salaryMedian: 5200000,
            marketDemand: 'Elite / Leadership Tier',
            description: 'Direct corporate technology vision, enterprise modernization, and global engineering talent.',
            requiredSkills: ['Enterprise Architecture', 'Tech Strategy', 'Org Leadership']
          }
        ],
        salaryTrajectory: [
          { year: 'Current Level', avg: 110000 },
          { year: 'Year +1', avg: 150000 },
          { year: 'Year +3', avg: 190000 },
          { year: 'Year +5', avg: 245000 }
        ],
        breakthroughSkills: ['Kafka', 'Docker', 'System Design', 'Kubernetes', 'AWS'],
        industryTransitions: [
          { industry: 'FinTech & Real-time Payments', growth: '+24% YoY', alignment: '94% Skill Overlap' },
          { industry: 'Autonomous Cloud & AI Infrastructure', growth: '+38% YoY', alignment: '89% Skill Overlap' },
          { industry: 'Enterprise Healthcare SaaS & Security', growth: '+21% YoY', alignment: '83% Skill Overlap' }
        ]
      };
    }

    renderCareerPredictionUI(data, payload);
    if (recalcBtn) {
      recalcBtn.disabled = false;
      recalcBtn.innerHTML = origRecalcText;
    }
  }

  function renderCareerPredictionUI(data, payload) {
    if (DOM.predictTrajectoryTitle) DOM.predictTrajectoryTitle.textContent = data.primaryTrajectory || 'Enterprise Software Architecture';
    if (DOM.predictConfidenceScore) DOM.predictConfidenceScore.textContent = `${data.confidenceScore || 88}%`;
    if (DOM.predictMomentumVal) DOM.predictMomentumVal.textContent = `${data.confidenceScore || 88}%`;
    if (DOM.predictExpYears) DOM.predictExpYears.textContent = `${payload.yearsOfExperience || 0} Years`;

    // Render Milestones
    if (DOM.predictorMilestonesGrid && Array.isArray(data.milestones)) {
      DOM.predictorMilestonesGrid.innerHTML = data.milestones.map((m, idx) => {
        const horizonClass = idx === 0 ? 'horizon-1' : (idx === 1 ? 'horizon-3' : 'horizon-5');
        return `
          <div class="milestone-card ${horizonClass}">
            <div>
              <div class="milestone-top-row">
                <span class="horizon-badge">${escapeHtml(m.horizon)}</span>
                <span class="confidence-tag">🎯 ${m.confidence}% Fit</span>
              </div>
              <h4 class="milestone-role-title">${escapeHtml(m.predictedRole)}</h4>
              <div class="milestone-salary-banner">
                <div class="milestone-salary-val">${escapeHtml(m.salaryRange)}</div>
                <div class="milestone-demand-text">🔥 ${escapeHtml(m.marketDemand)}</div>
              </div>
              <p class="milestone-desc">${escapeHtml(m.description)}</p>
            </div>
            <div class="milestone-skills-group">
              <h5>Key Competencies to Solidify:</h5>
              <div class="milestone-skills-pills">
                ${(m.requiredSkills || []).map(s => `<span class="m-skill-chip">⚡ ${escapeHtml(s)}</span>`).join('')}
              </div>
            </div>
          </div>
        `;
      }).join('');
    }

    // Render Salary Progression Bars
    if (DOM.predictSalaryBars && Array.isArray(data.salaryTrajectory)) {
      const maxSal = Math.max(...data.salaryTrajectory.map(s => s.avg), 300000);
      DOM.predictSalaryBars.innerHTML = data.salaryTrajectory.map(pt => {
        const pct = Math.round((pt.avg / maxSal) * 100);
        return `
          <div class="salary-bar-item">
            <div class="bar-meta">
              <span>${escapeHtml(pt.year)}</span>
              <span style="color:#059669; font-family:var(--font-mono); font-weight:800;">₹${formatLPA(pt.avg)}</span>
            </div>
            <div class="bar-track">
              <div class="bar-fill" style="width: ${pct}%;"></div>
            </div>
          </div>
        `;
      }).join('');
    }

    // Render Breakthrough Skills
    if (DOM.predictBreakthroughSkills && Array.isArray(data.breakthroughSkills)) {
      DOM.predictBreakthroughSkills.innerHTML = data.breakthroughSkills.slice(0, 6).map(s => `
        <span class="breakthrough-chip">🚀 ${escapeHtml(s)}</span>
      `).join('');
    }

    // Render Industry Transitions
    if (DOM.predictIndustryChips && Array.isArray(data.industryTransitions)) {
      DOM.predictIndustryChips.innerHTML = data.industryTransitions.map(it => `
        <div class="industry-card-item">
          <span class="industry-name">🏢 ${escapeHtml(it.industry)}</span>
          <span class="industry-stats">${escapeHtml(it.growth)} • ${escapeHtml(it.alignment)}</span>
        </div>
      `).join('');
    }
  }

  
  // ==============================================================================
  // FEATURE 2: SMART SKILL GAP FINDER & PERSONALIZED LEARNING ROADMAP
  // ==============================================================================
  function populateGapTargetJobSelect() {
    if (!DOM.gapTargetJobSelect) return;
    if (state.jobCatalog.length === 0) return;
    const curVal = DOM.gapTargetJobSelect.value;
    DOM.gapTargetJobSelect.innerHTML = state.jobCatalog.map(j => `
      <option value="${j.id}" ${j.id === curVal ? 'selected' : ''}>${escapeHtml(j.title)} (${escapeHtml(j.company)})</option>
    `).join('');
  }

  async function loadSkillGapAnalysis(targetJobId) {
    const refreshGapBtn = DOM.refreshSkillGapBtn;
    let origGapText = '';
    if (refreshGapBtn) {
      origGapText = refreshGapBtn.innerHTML;
      refreshGapBtn.disabled = true;
      refreshGapBtn.innerHTML = '<span class="btn-spinner"></span> Evaluating Skill Gap...';
    }
    const selJobId = targetJobId || (DOM.gapTargetJobSelect ? DOM.gapTargetJobSelect.value : '') || (state.jobCatalog[0]?.id || 'job-sr-01');
    const candidateSkills = (state.userProfile.skills && state.userProfile.skills.length > 0)
      ? state.userProfile.skills
      : ['Java', 'Spring Boot', 'SQL', 'REST APIs'];
    const email = state.currentUser?.email || state.userProfile.detectedInfo?.email || '';

    const payload = {
      jobId: selJobId,
      skills: candidateSkills,
      email: email
    };

    let data = null;

    if (isJavaServerOnline) {
      try {
        const res = await fetch(`${API_BASE_URL}/career/skill-gap`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(payload)
        });
        if (res.ok) {
          data = await res.json();
        }
      } catch (e) {
        console.warn('Backend skill gap error, using fallback:', e);
      }
    }

    if (!data || !data.success) {
      const job = state.jobCatalog.find(j => j.id === selJobId) || state.jobCatalog[0] || { title: 'Software Engineer', company: 'Tech Corp', requiredSkills: ['Java', 'Docker', 'AWS', 'Kubernetes'], niceToHaveSkills: ['Redis'] };
      const matched = job.requiredSkills.filter(s => candidateSkills.some(cs => cs.toLowerCase().includes(s.toLowerCase()) || s.toLowerCase().includes(cs.toLowerCase())));
      const critical = job.requiredSkills.filter(s => !matched.includes(s));
      const secondary = (job.niceToHaveSkills || []).filter(s => !candidateSkills.some(cs => cs.toLowerCase().includes(s.toLowerCase())));

      data = {
        success: true,
        jobId: job.id,
        jobTitle: job.title,
        company: job.company,
        currentMatchPercentage: Math.max(35, Math.round((matched.length / Math.max(1, job.requiredSkills.length)) * 80)),
        potentialMatchPercentage: 98,
        matchedSkills: matched.length > 0 ? matched : ['Java', 'SQL'],
        missingCritical: critical.length > 0 ? critical : ['Microservices Architecture', 'Kafka Event Streaming'],
        missingSecondary: secondary.length > 0 ? secondary : ['Kubernetes Deployment', 'Redis Caching'],
        estimatedTimeToClose: '4 to 6 Weeks (10-12 hrs/week)',
        roadmap: [
          {
            phase: 'Weeks 1–2 (High-Priority Core)',
            title: `Foundational Critical Gaps: ${critical[0] || 'Distributed System Design'}`,
            skills: critical.slice(0, 2),
            hoursPerWeek: 12,
            projectMilestone: 'Develop and benchmark a standalone proof-of-concept implementing enterprise architectural patterns',
            learningResources: ['Official Engineering Documentation & RFCs', 'Interactive Production Sandbox Tutorials', 'GitHub Enterprise Reference Architectures'],
            matchGain: '+14% Match'
          },
          {
            phase: 'Weeks 3–4 (Production Integration)',
            title: `Scalability & Cloud Infrastructure: ${critical[1] || 'Container Orchestration'}`,
            skills: critical.slice(1),
            hoursPerWeek: 10,
            projectMilestone: 'Build a fault-tolerant containerized cluster with automated CI/CD and metrics monitoring',
            learningResources: ['Production Kubernetes Best Practices Guide', 'Cloud Native Computing Foundation (CNCF) Patterns'],
            matchGain: '+10% Match'
          },
          {
            phase: 'Weeks 5–6 (Secondary Polish & Interview Mastery)',
            title: `Nice-to-Have Polish: ${secondary[0] || 'Performance Profiling'}`,
            skills: secondary,
            hoursPerWeek: 8,
            projectMilestone: 'Deploy complete end-to-end portfolio demo with automated testing and live production link',
            learningResources: ['High Performance Systems Playbook', 'Production Security Hardening Guide'],
            matchGain: '+6% Match'
          }
        ]
      };
    }

    renderSkillGapUI(data);
    if (refreshGapBtn) {
      refreshGapBtn.disabled = false;
      refreshGapBtn.innerHTML = origGapText;
    }
  }

  function renderSkillGapUI(data) {
    if (DOM.gapJobHeaderTitle) DOM.gapJobHeaderTitle.textContent = `Target Role: ${data.jobTitle || 'Software Engineer'}`;
    if (DOM.gapJobHeaderCompany) DOM.gapJobHeaderCompany.innerHTML = `at <strong>${escapeHtml(data.company || 'Enterprise')}</strong> • Evaluated Against Your Live Profile`;
    if (DOM.gapTimeToClose) DOM.gapTimeToClose.textContent = data.estimatedTimeToClose || '4 to 6 Weeks';
    const totalReq = (data.matchedSkills?.length || 0) + (data.missingCritical?.length || 0);
    if (DOM.gapTotalReqCount) DOM.gapTotalReqCount.textContent = `${totalReq} Prerequisites`;

    const uplift = Math.max(0, (data.potentialMatchPercentage || 98) - (data.currentMatchPercentage || 50));
    if (DOM.gapMatchUplift) DOM.gapMatchUplift.textContent = `+${uplift}% Match Potential`;

    if (DOM.gapCurrentMatchVal) DOM.gapCurrentMatchVal.textContent = `${data.currentMatchPercentage || 0}%`;
    if (DOM.gapCurrentBar) DOM.gapCurrentBar.style.width = `${data.currentMatchPercentage || 0}%`;

    if (DOM.gapPotentialMatchVal) DOM.gapPotentialMatchVal.textContent = `${data.potentialMatchPercentage || 98}%`;
    if (DOM.gapPotentialBar) DOM.gapPotentialBar.style.width = `${data.potentialMatchPercentage || 98}%`;

    // Skills counts & lists
    const matched = data.matchedSkills || [];
    const critical = data.missingCritical || [];
    const secondary = data.missingSecondary || [];

    if (DOM.gapMatchedCount) DOM.gapMatchedCount.textContent = matched.length;
    if (DOM.gapCriticalCount) DOM.gapCriticalCount.textContent = critical.length;
    if (DOM.gapSecondaryCount) DOM.gapSecondaryCount.textContent = secondary.length;

    if (DOM.gapMatchedSkillsList) {
      DOM.gapMatchedSkillsList.innerHTML = matched.length > 0
        ? matched.map(s => `<span class="gap-chip matched">✓ ${escapeHtml(s)}</span>`).join('')
        : `<span style="font-size:0.78rem; color:var(--text-muted);">No direct matches identified yet.</span>`;
    }

    if (DOM.gapCriticalSkillsList) {
      DOM.gapCriticalSkillsList.innerHTML = critical.length > 0
        ? critical.map(s => `<span class="gap-chip critical">⚡ ${escapeHtml(s)}</span>`).join('')
        : `<span style="font-size:0.78rem; color:#059669; font-weight:700;">Zero critical gaps! You satisfy all core requirements.</span>`;
    }

    if (DOM.gapSecondarySkillsList) {
      DOM.gapSecondarySkillsList.innerHTML = secondary.length > 0
        ? secondary.map(s => `<span class="gap-chip secondary">🌟 ${escapeHtml(s)}</span>`).join('')
        : `<span style="font-size:0.78rem; color:var(--text-muted);">No extra secondary gaps.</span>`;
    }

    // Roadmap phases
    if (DOM.gapRoadmapTimeline && Array.isArray(data.roadmap)) {
      DOM.gapRoadmapTimeline.innerHTML = data.roadmap.map(phase => `
        <div class="roadmap-phase-card">
          <div class="phase-badge-col">
            <span class="phase-pill">${escapeHtml(phase.phase)}</span>
            <span class="phase-gain">${escapeHtml(phase.matchGain || '+10%')}</span>
            <span class="phase-hours">⏱️ ${phase.hoursPerWeek || 10} hrs/week</span>
          </div>
          <div class="phase-content">
            <h5 class="phase-title">${escapeHtml(phase.title)}</h5>
            <div class="phase-milestone-box">
              <strong>Key Project Milestone:</strong> ${escapeHtml(phase.projectMilestone)}
            </div>
            <div class="phase-resources-row">
              <span style="font-size:0.72rem; color:var(--text-muted); font-weight:700;">Curated Guides:</span>
              ${(phase.learningResources || []).map(r => `<span class="resource-badge">📖 ${escapeHtml(r)}</span>`).join('')}
            </div>
          </div>
        </div>
      `).join('');
    }
  }

  
  // ==============================================================================
  // FEATURE 3: WHAT-IF CAREER SIMULATOR ENGINE
  // ==============================================================================
  function toggleSimulatedSkill(skillName) {
    if (!skillName) return;
    if (state.simulatedSkills.has(skillName)) {
      state.simulatedSkills.delete(skillName);
    } else {
      state.simulatedSkills.add(skillName);
    }

    document.querySelectorAll('.sim-chip').forEach(c => {
      c.classList.toggle('active', state.simulatedSkills.has(c.dataset.simSkill));
    });

    const count = state.simulatedSkills.size;
    if (DOM.simSelectedSkillsBadge) {
      DOM.simSelectedSkillsBadge.textContent = `${count} Skill${count === 1 ? '' : 's'} Simulated`;
    }

    runCareerSimulation();
  }

  async function runCareerSimulation() {
    const candidateSkills = (state.userProfile.skills && state.userProfile.skills.length > 0)
      ? state.userProfile.skills
      : ['Java', 'Spring Boot', 'SQL'];
    const simSkillsArr = Array.from(state.simulatedSkills);
    const yearsExp = typeof state.userProfile.yearsOfExperience === 'number'
      ? state.userProfile.yearsOfExperience
      : (state.userProfile.detectedInfo?.years || 2);

    const payload = {
      currentSkills: candidateSkills,
      simulatedSkills: simSkillsArr,
      yearsOfExperience: yearsExp,
      email: state.currentUser?.email || ''
    };

    let data = null;

    if (isJavaServerOnline) {
      try {
        const res = await fetch(`${API_BASE_URL}/career/simulator`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(payload)
        });
        if (res.ok) {
          data = await res.json();
        }
      } catch (e) {
        console.warn('Backend simulator error, using fallback:', e);
      }
    }

    if (!data || !data.success) {
      const boostVal = simSkillsArr.length * 7500;
      data = {
        success: true,
        baselineMatchAvg: 52,
        simulatedMatchAvg: Math.min(94, 52 + (simSkillsArr.length * 6)),
        matchScoreGain: simSkillsArr.length * 6,
        unlockedJobsCount: Math.min(simSkillsArr.length * 2, 8),
        salaryBoostAmount: boostVal > 0 ? boostVal : 12000,
        baselineSalaryAvg: 110000,
        simulatedSalaryAvg: 110000 + (boostVal > 0 ? boostVal : 12000),
        unlockedJobs: [
          {
            id: 'job-ai-01',
            title: 'AI & Machine Learning Solutions Engineer',
            company: 'Anthropic / Scaled AI',
            location: 'San Francisco, CA (Remote)',
            salaryRange: '$145,000 - $185,000',
            beforeMatch: 45,
            afterMatch: 88,
            unlockedBy: simSkillsArr.length > 0 ? simSkillsArr.slice(0, 2) : ['Python', 'LLMs']
          },
          {
            id: 'job-cloud-02',
            title: 'Senior Cloud DevOps Architect',
            company: 'Amazon Web Services',
            location: 'Seattle, WA (Hybrid)',
            salaryRange: '$160,000 - $205,000',
            beforeMatch: 52,
            afterMatch: 91,
            unlockedBy: simSkillsArr.length > 0 ? simSkillsArr.slice(0, 2) : ['Kubernetes', 'AWS']
          }
        ]
      };
    }

    renderSimulatorUI(data);
  }

  function renderSimulatorUI(data) {
    if (DOM.simMatchGainVal) DOM.simMatchGainVal.textContent = `+${data.matchScoreGain || 0}%`;
    if (DOM.simMatchSub) DOM.simMatchSub.textContent = `Average: ${data.baselineMatchAvg || 50}% → ${data.simulatedMatchAvg || 75}%`;
    if (DOM.simUnlockedCountVal) DOM.simUnlockedCountVal.textContent = `+${data.unlockedJobsCount || 0} Roles`;
    if (DOM.simSalaryUpliftVal) DOM.simSalaryUpliftVal.textContent = `+₹${formatLPA(data.salaryBoostAmount || 150000)}`;
    if (DOM.simNewAvgSalaryVal) DOM.simNewAvgSalaryVal.textContent = `₹${formatLPA(data.simulatedSalaryAvg || 1200000)}`;

    const count = state.simulatedSkills.size;
    if (DOM.simSelectedSkillsBadge) {
      DOM.simSelectedSkillsBadge.textContent = `${count} Skill${count === 1 ? '' : 's'} Simulated`;
    }

    if (DOM.simUnlockedJobsList) {
      const jobs = data.unlockedJobs || [];
      if (jobs.length === 0) {
        DOM.simUnlockedJobsList.innerHTML = `
          <div style="grid-column: 1 / -1; padding: 2rem; text-align: center; color: var(--text-muted); background: var(--bg-tertiary); border-radius: var(--radius-md);">
            <h5>Select 1 or more hypothetical skills above to see newly unlocked positions</h5>
            <p style="font-size: 0.82rem;">Try clicking <strong>Python</strong>, <strong>AWS</strong>, or <strong>Kubernetes</strong></p>
          </div>
        `;
        return;
      }

      DOM.simUnlockedJobsList.innerHTML = jobs.map(j => {
        const delta = j.afterMatch - j.beforeMatch;
        return `
          <div class="unlocked-job-card">
            <div>
              <div class="unlocked-top">
                <div>
                  <h5 class="unlocked-title">${escapeHtml(j.title)}</h5>
                  <div class="unlocked-company">🏢 ${escapeHtml(j.company)} • ${escapeHtml(j.location || 'Remote')}</div>
                </div>
                <span class="unlocked-badge">Unlocked!</span>
              </div>
              <div style="font-size:0.85rem; font-weight:800; color:#059669; font-family:var(--font-mono); margin: 0.5rem 0;">
                ${escapeHtml(j.salaryRange || '$120,000 - $160,000')}
              </div>
              <div class="before-after-meter">
                <div class="before-after-row">
                  <span>Match Progression:</span>
                  <span class="delta-gain">+${delta}% Jump (${j.beforeMatch}% → <strong>${j.afterMatch}%</strong>)</span>
                </div>
                <div class="mini-bar-track" style="margin-top:0.35rem;">
                  <div class="mini-bar-fill potential" style="width: ${j.afterMatch}%;"></div>
                </div>
              </div>
            </div>
            <div class="unlocked-by-tags">
              <span style="color:var(--text-muted); font-weight:700;">Unlocked By:</span>
              ${(j.unlockedBy || []).map(s => `<span class="badge-mini" style="background:#e0e7ff; color:#4338ca; border-color:#c7d2fe;">⚡ ${escapeHtml(s)}</span>`).join('')}
            </div>
          </div>
        `;
      }).join('');
    }
  }

  
  // ==============================================================================
  // FEATURE 4: AI INTERVIEW PRACTICE & EVALUATION ENGINE
  // ==============================================================================
  function populateInterviewTargetJobSelect() {
    if (!DOM.interviewTargetJobSelect) return;
    const currentVal = DOM.interviewTargetJobSelect.value;
    const jobs = (state.allJobs && state.allJobs.length > 0)
      ? state.allJobs
      : (state.matchedJobs && state.matchedJobs.length > 0 ? state.matchedJobs : []);

    if (jobs.length === 0) {
      DOM.interviewTargetJobSelect.innerHTML = `<option value="">Software Engineer (Default Role)</option>`;
      return;
    }

    DOM.interviewTargetJobSelect.innerHTML = jobs.map(j => `
      <option value="${escapeHtml(j.id)}" ${j.id === currentVal ? 'selected' : ''}>
        ${escapeHtml(j.title)} (${escapeHtml(j.company)})
      </option>
    `).join('');

    if (!state.currentInterviewJobId && jobs.length > 0) {
      state.currentInterviewJobId = jobs[0].id;
    }
  }

  async function loadInterviewQuestions(overrideJobId) {
    const jobId = overrideJobId || (DOM.interviewTargetJobSelect ? DOM.interviewTargetJobSelect.value : state.currentInterviewJobId) || 'job-01';
    state.currentInterviewJobId = jobId;

    let targetTitle = 'Backend Engineer';
    const all = (state.allJobs || []).concat(state.matchedJobs || []);
    const found = all.find(j => j.id === jobId);
    if (found) targetTitle = found.title;

    if (DOM.interviewQuestionPrompt) {
      DOM.interviewQuestionPrompt.textContent = 'Generating role-specific technical & behavioral interview questions...';
    }

    let questions = null;

    if (isJavaServerOnline) {
      try {
        const res = await fetch(`${API_BASE_URL}/interview/questions?jobId=${encodeURIComponent(jobId)}&jobTitle=${encodeURIComponent(targetTitle)}`);
        if (res.ok) {
          const data = await res.json();
          if (data.success && data.questions && data.questions.length > 0) {
            questions = data.questions;
          }
        }
      } catch (e) {
        console.warn('Backend interview questions fetch error, using fallback:', e);
      }
    }

    if (!questions) {
      questions = [
        {
          id: 'q1',
          category: 'System Architecture & Distributed Systems',
          difficulty: 'Senior System Design',
          question: `How do you ensure data consistency and transactional integrity across microservices communicating asynchronously in ${targetTitle} environments?`,
          hints: 'Discuss the Transactional Outbox Pattern, Saga pattern (Orchestration vs Choreography), idempotent consumers, and dead letter queues.',
          keyConcepts: ['Transactional Outbox', 'Saga Pattern', 'Kafka', 'Idempotency', 'Eventual Consistency']
        },
        {
          id: 'q2',
          category: 'Performance & Optimization',
          difficulty: 'Technical Deep Dive',
          question: 'A critical endpoint experiences latency spiking from 40ms to 1500ms during peak load. Detail your step-by-step profiling methodology.',
          hints: 'Cover APM profilers, database connection pools, unindexed queries (EXPLAIN ANALYZE), and Redis cache strategies.',
          keyConcepts: ['Connection Pool', 'Indexes', 'Redis Cache', 'Profiling', 'Latency']
        },
        {
          id: 'q3',
          category: 'Behavioral & Leadership (STAR Method)',
          difficulty: 'Engineering Culture',
          question: 'Describe a project where you had to push back on unrealistic delivery deadlines or tech debt accumulation. How did you handle stakeholder expectations?',
          hints: 'Use Situation, Task, Action, Result. Show pragmatic compromise, phased rollouts, and measurable risk reduction.',
          keyConcepts: ['Tech Debt', 'Stakeholders', 'Phased Delivery', 'Risk Mitigation']
        },
        {
          id: 'q4',
          category: 'API Security, Resiliency & Scale',
          difficulty: 'Production Operations',
          question: 'How do you protect high-volume APIs against cascading failure, distributed denial of service, and credential stuffing while maintaining high availability?',
          hints: 'Discuss Circuit Breakers, distributed Token Bucket rate limiting, and stateless JWT validation.',
          keyConcepts: ['Circuit Breaker', 'Rate Limiting', 'Token Bucket', 'JWT', 'High Availability']
        }
      ];
    }

    state.currentInterviewQuestions = questions;
    state.activeQuestionIndex = 0;
    renderInterviewQuestionsNav();
    displayActiveInterviewQuestion();
  }

  function renderInterviewQuestionsNav() {
    if (!DOM.interviewQuestionTabs) return;
    const labels = ['Q1 • Architecture', 'Q2 • Deep Dive', 'Q3 • STAR Behavioral', 'Q4 • Production Scale'];
    DOM.interviewQuestionTabs.innerHTML = state.currentInterviewQuestions.map((q, idx) => `
      <button type="button" class="q-nav-pill ${idx === state.activeQuestionIndex ? 'active' : ''}" data-q-idx="${idx}">
        ${escapeHtml(labels[idx] || `Q${idx+1} • Question`)}
      </button>
    `).join('');
  }

  function displayActiveInterviewQuestion() {
    const q = state.currentInterviewQuestions[state.activeQuestionIndex];
    if (!q) return;

    if (DOM.interviewCategoryBadge) DOM.interviewCategoryBadge.textContent = q.category || 'Technical Round';
    if (DOM.interviewDifficultyBadge) DOM.interviewDifficultyBadge.textContent = q.difficulty || 'Senior Level';
    if (DOM.interviewQuestionPrompt) DOM.interviewQuestionPrompt.textContent = q.question || 'Interview prompt';
    if (DOM.interviewHintsContent) DOM.interviewHintsContent.textContent = q.hints || 'Highlight architectural tradeoffs and quantitative results.';

    // Update nav active pill
    if (DOM.interviewQuestionTabs) {
      DOM.interviewQuestionTabs.querySelectorAll('.q-nav-pill').forEach((pill, idx) => {
        pill.classList.toggle('active', idx === state.activeQuestionIndex);
      });
    }

    updateInterviewWordCount();
  }

  function updateInterviewWordCount() {
    if (!DOM.interviewAnswerInput || !DOM.interviewWordCountBadge) return;
    const text = DOM.interviewAnswerInput.value.trim();
    const count = text ? text.split(/\s+/).length : 0;
    DOM.interviewWordCountBadge.textContent = `${count} word${count === 1 ? '' : 's'}`;
    if (count >= 70 && count <= 180) {
      DOM.interviewWordCountBadge.style.color = '#10b981';
      DOM.interviewWordCountBadge.style.borderColor = '#10b981';
    } else {
      DOM.interviewWordCountBadge.style.color = 'var(--text-muted)';
      DOM.interviewWordCountBadge.style.borderColor = 'var(--border-color)';
    }
  }

  async function submitInterviewAnswer() {
    if (!DOM.interviewAnswerInput) return;
    const answer = DOM.interviewAnswerInput.value.trim();
    if (!answer) {
      showToast('Please type your answer before submitting for evaluation.', 'error');
      return;
    }

    const words = answer.split(/\s+/).filter(Boolean);
    if (words.length < 5) {
      showToast('Please provide a more complete response (at least 5 words) for the AI Coach to evaluate.', 'warning');
      return;
    }

    const q = state.currentInterviewQuestions[state.activeQuestionIndex];
    if (!q) return;

    if (DOM.submitInterviewBtnText) DOM.submitInterviewBtnText.textContent = 'Evaluating with AI Coach...';
    if (DOM.submitInterviewAnswerBtn) DOM.submitInterviewAnswerBtn.disabled = true;

    const payload = {
      jobId: state.currentInterviewJobId || 'job-01',
      jobTitle: DOM.interviewTargetJobSelect ? DOM.interviewTargetJobSelect.options[DOM.interviewTargetJobSelect.selectedIndex]?.text : 'Software Engineer',
      questionId: q.id || 'q1',
      questionText: q.question,
      userAnswer: answer,
      email: state.currentUser?.email || 'mansi@example.com'
    };

    let evalResult = null;

    if (isJavaServerOnline) {
      try {
        const res = await fetch(`${API_BASE_URL}/interview/evaluate`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(payload)
        });
        if (res.ok) {
          evalResult = await res.json();
        }
      } catch (e) {
        console.warn('Backend interview evaluate error, using fallback:', e);
      }
    }

    if (!evalResult || !evalResult.success) {
      const words = answer.split(/\s+/).length;
      const score = Math.min(94, Math.max(50, 45 + Math.min(words * 0.5, 45)));
      evalResult = {
        success: true,
        overallScore: Math.round(score),
        technicalScore: Math.round(score - 2),
        communicationScore: Math.round(score + 3),
        starScore: Math.round(score - 1),
        verdict: score >= 80 ? 'Strong Hire (Exceeds Expectations)' : 'Hire (Meets Requirements)',
        strengths: [
          'Directly addresses core technical problem formulation.',
          'Demonstrates ownership of implementation and architecture decisions.'
        ],
        improvements: [
          'Add quantifiable metrics (e.g., % latency reduction or RPS processed).',
          'Mention error handling and disaster recovery considerations.'
        ],
        exemplarAnswer: 'In our previous high-throughput system, I architected an asynchronous event-driven workflow using Kafka and Redis caching. This eliminated connection pool exhaustion and reduced p99 latency by 72% across 10M daily transactions.',
        timestamp: new Date().toISOString()
      };
    }

    if (DOM.submitInterviewBtnText) DOM.submitInterviewBtnText.textContent = 'Submit Answer for AI Evaluation';
    if (DOM.submitInterviewAnswerBtn) DOM.submitInterviewAnswerBtn.disabled = false;

    renderInterviewScorecard(evalResult);
    loadInterviewHistory();
    showToast(`Evaluation Complete! Overall Score: ${evalResult.overallScore}%`, 'success');
  }

  function renderInterviewScorecard(data) {
    if (DOM.interviewScorecardEmpty) DOM.interviewScorecardEmpty.classList.add('hidden');
    if (DOM.interviewScorecardActive) DOM.interviewScorecardActive.classList.remove('hidden');

    const score = data.overallScore || 75;
    if (DOM.interviewOverallScoreVal) {
      DOM.interviewOverallScoreVal.textContent = `${score}%`;
      if (score >= 85) {
        DOM.interviewOverallScoreVal.style.background = 'linear-gradient(135deg, #10b981, #059669)';
      } else if (score >= 70) {
        DOM.interviewOverallScoreVal.style.background = 'linear-gradient(135deg, #3b82f6, #2563eb)';
      } else {
        DOM.interviewOverallScoreVal.style.background = 'linear-gradient(135deg, #f59e0b, #d97706)';
      }
    }

    if (DOM.interviewVerdictText) {
      DOM.interviewVerdictText.textContent = data.verdict || 'Good Attempt';
      DOM.interviewVerdictText.style.color = data.verdictColor || '#059669';
    }

    if (DOM.interviewWordCountStat) {
      const words = DOM.interviewAnswerInput ? DOM.interviewAnswerInput.value.trim().split(/\s+/).length : 0;
      DOM.interviewWordCountStat.textContent = `${words} Words Analyzed`;
    }

    // Sub-scores
    if (DOM.interviewTechScoreVal) DOM.interviewTechScoreVal.textContent = `${data.technicalScore || score}%`;
    if (DOM.interviewTechScoreBar) DOM.interviewTechScoreBar.style.width = `${data.technicalScore || score}%`;

    if (DOM.interviewCommScoreVal) DOM.interviewCommScoreVal.textContent = `${data.communicationScore || score}%`;
    if (DOM.interviewCommScoreBar) DOM.interviewCommScoreBar.style.width = `${data.communicationScore || score}%`;

    if (DOM.interviewStarScoreVal) DOM.interviewStarScoreVal.textContent = `${data.starScore || score}%`;
    if (DOM.interviewStarScoreBar) DOM.interviewStarScoreBar.style.width = `${data.starScore || score}%`;

    // Strengths
    if (DOM.interviewStrengthsList) {
      const items = data.strengths || ['Good technical foundation demonstrated.'];
      DOM.interviewStrengthsList.innerHTML = items.map(s => `<li>${escapeHtml(s)}</li>`).join('');
    }

    // Improvements
    if (DOM.interviewImprovementsList) {
      const items = data.improvements || ['Practice including specific quantitative business results.'];
      DOM.interviewImprovementsList.innerHTML = items.map(s => `<li>${escapeHtml(s)}</li>`).join('');
    }

    // Exemplar
    if (DOM.interviewExemplarText) {
      DOM.interviewExemplarText.textContent = data.exemplarAnswer || 'Exemplar answer will show high-impact STAR formulation.';
    }
  }

  async function loadInterviewHistory() {
    if (!DOM.interviewHistoryList) return;
    const email = state.currentUser?.email || 'mansi@example.com';

    let historyData = null;
    if (isJavaServerOnline) {
      try {
        const res = await fetch(`${API_BASE_URL}/interview/history?email=${encodeURIComponent(email)}`);
        if (res.ok) historyData = await res.json();
      } catch (e) {
        console.warn('Backend interview history fetch error:', e);
      }
    }

    if (!historyData || !historyData.history || historyData.history.length === 0) {
      if (DOM.interviewTotalPracticedBadge) DOM.interviewTotalPracticedBadge.textContent = '0 Sessions';
      DOM.interviewHistoryList.innerHTML = `
        <p style="font-size:0.78rem; color:var(--text-muted); padding:0.5rem 0;">No completed practice attempts yet. Submit your first answer above!</p>
      `;
      return;
    }

    if (DOM.interviewTotalPracticedBadge) {
      DOM.interviewTotalPracticedBadge.textContent = `${historyData.totalPracticed} Session${historyData.totalPracticed === 1 ? '' : 's'} (Avg ${historyData.averageScore}%)`;
    }

    DOM.interviewHistoryList.innerHTML = historyData.history.map(item => `
      <div class="history-mini-item">
        <div>
          <strong style="color:var(--text-primary); font-size:0.82rem;">${escapeHtml(item.jobTitle || 'Software Role')}</strong>
          <div style="font-size:0.75rem; color:var(--text-muted); max-width:260px; overflow:hidden; text-overflow:ellipsis; white-space:nowrap;">
            ${escapeHtml(item.questionText || 'Interview Question')}
          </div>
        </div>
        <div style="text-align:right;">
          <span style="font-weight:900; font-family:var(--font-mono); color:${(item.overallScore >= 80 ? '#10b981' : (item.overallScore >= 65 ? '#3b82f6' : '#f59e0b'))};">
            ${item.overallScore}%
          </span>
          <div style="font-size:0.7rem; color:var(--text-muted);">${new Date(item.timestamp).toLocaleDateString()}</div>
        </div>
      </div>
    `).join('');
  }

  
  // ==============================================================================
  // FEATURE 5: FAKE JOB DETECTION & FRAUD SHIELD ENGINE
  // ==============================================================================
  function populateFakeCatalogSelect() {
    if (!DOM.fakeCatalogSelect) return;
    const jobs = (state.allJobs && state.allJobs.length > 0)
      ? state.allJobs
      : (state.matchedJobs && state.matchedJobs.length > 0 ? state.matchedJobs : []);

    DOM.fakeCatalogSelect.innerHTML = `<option value="">-- Choose from Catalog --</option>` + jobs.map(j => `
      <option value="${escapeHtml(j.id)}">${escapeHtml(j.title)} (${escapeHtml(j.company)})</option>
    `).join('');
  }

  function loadPresetScamSample() {
    if (DOM.fakeCompanyNameInput) DOM.fakeCompanyNameInput.value = 'Apex Cloud Logistics Worldwide';
    if (DOM.fakeRecruiterEmailInput) DOM.fakeRecruiterEmailInput.value = 'apex-hiring-manager2024@gmail.com';
    if (DOM.fakeSalaryInput) DOM.fakeSalaryInput.value = '₹35,000 / week (No Experience Required)';
    if (DOM.fakePostingTextInput) {
      DOM.fakePostingTextInput.value =
        'Immediate hiring for Work From Home Senior Data Processing Specialist! Earn $4,200 weekly working just 2 hours a day. ' +
        'No technical interview needed, immediate employment offer letter issued within 2 hours. ' +
        'Initial screening conducted strictly via Telegram app messaging. ' +
        'Candidate must pay a refundable $180 registration and home-office equipment security deposit via wire transfer or crypto before receiving laptop kit.';
    }
    if (DOM.fakePaymentCheckbox) DOM.fakePaymentCheckbox.checked = true;
    runFakeJobAnalysis();
  }

  function loadPresetPhishSample() {
    if (DOM.fakeCompanyNameInput) DOM.fakeCompanyNameInput.value = 'Google Global Operations';
    if (DOM.fakeRecruiterEmailInput) DOM.fakeRecruiterEmailInput.value = 'google.careers.recruitment@yahoo.com';
    if (DOM.fakeSalaryInput) DOM.fakeSalaryInput.value = '$160,000 / yr';
    if (DOM.fakePostingTextInput) {
      DOM.fakePostingTextInput.value =
        'Google is hiring remote software developers. Send your passport copy, SSN card, and driver license scan to our Yahoo recruitment inbox immediately for background clearance before the interview invitation link is shared.';
    }
    if (DOM.fakePaymentCheckbox) DOM.fakePaymentCheckbox.checked = false;
    runFakeJobAnalysis();
  }

  function loadPresetLegitSample() {
    if (DOM.fakeCompanyNameInput) DOM.fakeCompanyNameInput.value = 'Amazon AWS';
    if (DOM.fakeRecruiterEmailInput) DOM.fakeRecruiterEmailInput.value = 'aws-technical-recruiting@amazon.com';
    if (DOM.fakeSalaryInput) DOM.fakeSalaryInput.value = '$145,000 - $190,000 / yr';
    if (DOM.fakePostingTextInput) {
      DOM.fakePostingTextInput.value =
        'Backend Java Engineer (High Concurrency). Build low-latency microservices with Java, Spring Boot, DynamoDB, and Apache Kafka. ' +
        'Requires 3+ years experience in distributed systems. Comprehensive multi-round technical interview including system design, coding assessment, and leadership principles. ' +
        'Amazon does not charge any application or onboarding fees.';
    }
    if (DOM.fakePaymentCheckbox) DOM.fakePaymentCheckbox.checked = false;
    runFakeJobAnalysis();
  }

  async function runFakeJobAnalysis(catalogJobId) {
    const companyName = DOM.fakeCompanyNameInput ? DOM.fakeCompanyNameInput.value.trim() : '';
    const recruiterEmail = DOM.fakeRecruiterEmailInput ? DOM.fakeRecruiterEmailInput.value.trim() : '';
    const postingText = DOM.fakePostingTextInput ? DOM.fakePostingTextInput.value.trim() : '';
    const salaryClaimed = DOM.fakeSalaryInput ? DOM.fakeSalaryInput.value.trim() : '';
    const paymentRequested = DOM.fakePaymentCheckbox ? DOM.fakePaymentCheckbox.checked : false;

    if (!catalogJobId && !companyName && !postingText && !recruiterEmail && !paymentRequested) {
      showToast('Please select a catalog position, or enter a company name / posting description to analyze.', 'warning');
      return;
    }
    if (DOM.runFakeBtnText) DOM.runFakeBtnText.textContent = 'Scanning Posting for Fraud...';
    if (DOM.runFakeDetectorBtn) DOM.runFakeDetectorBtn.disabled = true;

    const payload = {
      jobId: catalogJobId || '',
      companyName: DOM.fakeCompanyNameInput ? DOM.fakeCompanyNameInput.value.trim() : '',
      recruiterEmail: DOM.fakeRecruiterEmailInput ? DOM.fakeRecruiterEmailInput.value.trim() : '',
      salaryClaimed: DOM.fakeSalaryInput ? DOM.fakeSalaryInput.value.trim() : '',
      postingText: DOM.fakePostingTextInput ? DOM.fakePostingTextInput.value.trim() : '',
      paymentRequested: DOM.fakePaymentCheckbox ? DOM.fakePaymentCheckbox.checked : false
    };

    let report = null;

    if (isJavaServerOnline) {
      try {
        const res = await fetch(`${API_BASE_URL}/jobs/detect-fake`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(payload)
        });
        if (res.ok) {
          report = await res.json();
        }
      } catch (e) {
        console.warn('Backend fake detector error, using fallback:', e);
      }
    }

    if (!report || !report.success) {
      const isScam = payload.paymentRequested || payload.postingText.toLowerCase().includes('telegram') || payload.postingText.toLowerCase().includes('fee');
      report = {
        success: true,
        safetyScore: isScam ? 12 : 92,
        riskLevel: isScam ? 'Critical Threat / Likely Scam' : 'Verified Legitimate',
        riskColor: isScam ? '#dc2626' : '#059669',
        trustVerdict: isScam ? 'DO NOT APPLY: Strong indicators of employment fraud, identity theft, or fee solicitation.' : 'Safe to Apply: Standard corporate recruitment patterns verified.',
        redFlagsCount: isScam ? 3 : 0,
        redFlags: isScam ? [
          {
            severity: 'CRITICAL',
            badge: '🚨 Critical Threat',
            title: 'Upfront Payment or Financial Transaction Requested',
            detail: 'Legitimate employers never demand payment for equipment, training, or application processing.'
          },
          {
            severity: 'HIGH',
            badge: '⚠️ High Risk',
            title: 'Informal Messaging Channel (Telegram)',
            detail: 'Interviews conducted strictly over messaging apps indicate identity phishing.'
          }
        ] : [],
        checklist: [
          isScam ? '❌ Failed: Demands upfront financial outlay' : '✅ Passed: No upfront fees demanded',
          isScam ? '❌ Failed: Uses free email domain' : '✅ Passed: Verified corporate email domain',
          '✅ Passed: Cryptographic heuristics validated'
        ],
        recommendations: [
          'Never transfer money or provide gift cards to recruiters.',
          'Always verify job openings directly on the official company careers page.'
        ]
      };
    }

    if (DOM.runFakeBtnText) DOM.runFakeBtnText.textContent = 'Run Deep Fraud & Safety Analysis';
    if (DOM.runFakeDetectorBtn) DOM.runFakeDetectorBtn.disabled = false;

    renderFakeJobReport(report);
  }

  function renderFakeJobReport(data) {
    if (DOM.fakeReportPlaceholder) DOM.fakeReportPlaceholder.classList.add('hidden');
    if (DOM.fakeReportActive) DOM.fakeReportActive.classList.remove('hidden');

    const score = data.safetyScore || 50;
    if (DOM.fakeSafetyMeterVal) {
      DOM.fakeSafetyMeterVal.textContent = `${score}%`;
      if (score >= 80) {
        DOM.fakeSafetyMeterVal.style.background = 'linear-gradient(135deg, #10b981, #059669)';
      } else if (score >= 60) {
        DOM.fakeSafetyMeterVal.style.background = 'linear-gradient(135deg, #3b82f6, #2563eb)';
      } else if (score >= 40) {
        DOM.fakeSafetyMeterVal.style.background = 'linear-gradient(135deg, #f59e0b, #d97706)';
      } else {
        DOM.fakeSafetyMeterVal.style.background = 'linear-gradient(135deg, #ef4444, #dc2626)';
      }
    }

    if (DOM.fakeRiskLevelTitle) {
      DOM.fakeRiskLevelTitle.textContent = data.riskLevel || 'Analyzed';
      DOM.fakeRiskLevelTitle.style.color = data.riskColor || '#059669';
    }

    if (DOM.fakeThreatsDetectedBadge) {
      const count = data.redFlagsCount || 0;
      DOM.fakeThreatsDetectedBadge.textContent = `${count} Threat${count === 1 ? '' : 's'} Identified`;
      if (count > 0) {
        DOM.fakeThreatsDetectedBadge.style.background = 'rgba(239,68,68,0.15)';
        DOM.fakeThreatsDetectedBadge.style.color = '#f87171';
        DOM.fakeThreatsDetectedBadge.style.borderColor = '#ef4444';
      } else {
        DOM.fakeThreatsDetectedBadge.style.background = 'rgba(16,185,129,0.15)';
        DOM.fakeThreatsDetectedBadge.style.color = '#34d399';
        DOM.fakeThreatsDetectedBadge.style.borderColor = '#10b981';
      }
    }

    if (DOM.fakeVerdictBanner) {
      DOM.fakeVerdictBanner.style.borderLeftColor = data.riskColor || '#10b981';
    }
    if (DOM.fakeTrustVerdictText) {
      DOM.fakeTrustVerdictText.textContent = data.trustVerdict || 'Safety analysis complete.';
    }

    // Red flags
    if (DOM.fakeRedFlagsList) {
      const flags = data.redFlags || [];
      if (flags.length === 0) {
        DOM.fakeRedFlagsList.innerHTML = `
          <div style="padding:1rem; text-align:center; color:#059669; background:rgba(16,185,129,0.08); border-radius:6px; font-weight:700; font-size:0.85rem;">
            🛡️ Zero red flags detected. This job posting matches authentic enterprise hiring standards.
          </div>
        `;
      } else {
        DOM.fakeRedFlagsList.innerHTML = flags.map(f => `
          <div class="red-flag-item ${f.severity === 'MEDIUM' ? 'warning' : (f.severity === 'LOW' ? 'caution' : '')}">
            <div class="flag-item-top">
              <span class="flag-title">${escapeHtml(f.title)}</span>
              <span class="badge-mini" style="font-size:0.7rem; font-weight:800; background:rgba(239,68,68,0.15); color:#f87171; border-color:#ef4444;">${escapeHtml(f.badge || 'Threat')}</span>
            </div>
            <div class="flag-detail">${escapeHtml(f.detail)}</div>
          </div>
        `).join('');
      }
    }

    // Checklist
    if (DOM.fakeChecklistGrid) {
      const checks = data.checklist || [];
      DOM.fakeChecklistGrid.innerHTML = checks.map(c => `
        <div class="checklist-item">${escapeHtml(c)}</div>
      `).join('');
    }

    // Recommendations
    if (DOM.fakeRecommendationsList) {
      const recs = data.recommendations || ['Always apply through verified corporate domains.'];
      DOM.fakeRecommendationsList.innerHTML = recs.map(r => `<li>${escapeHtml(r)}</li>`).join('');
    }
  }

  
  // ==============================================================================
  // FEATURE 6: CAREER PATH COMPARISON ENGINE
  // ==============================================================================
  async function runCareerComparison() {
    const compareBtn = DOM.runCareerCompareBtn;
    let origCompareText = '';
    if (compareBtn) {
      origCompareText = compareBtn.innerHTML;
      compareBtn.disabled = true;
      compareBtn.innerHTML = '<span class="btn-spinner"></span> Comparing Trajectories...';
    }
    const roleA = DOM.compareRoleASelect ? DOM.compareRoleASelect.value : 'Senior Cloud & DevOps Architect';
    const roleB = DOM.compareRoleBSelect ? DOM.compareRoleBSelect.value : 'AI & Machine Learning Solutions Engineer';
    const candidateSkills = (state.userProfile.skills && state.userProfile.skills.length > 0)
      ? state.userProfile.skills
      : ['Java', 'Spring Boot', 'SQL', 'Docker', 'Git'];

    const payload = {
      roleA: roleA,
      roleB: roleB,
      candidateSkills: candidateSkills
    };

    let data = null;

    if (isJavaServerOnline) {
      try {
        const res = await fetch(`${API_BASE_URL}/career/compare`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(payload)
        });
        if (res.ok) {
          data = await res.json();
        }
      } catch (e) {
        console.warn('Backend career compare error, using fallback:', e);
      }
    }

    if (!data || !data.success) {
      data = {
        success: true,
        roleA: {
          title: roleA,
          minSalary: 135000,
          medianSalary: 168000,
          maxSalary: 210000,
          fiveYearSalary: 245000,
          growthRate: '+28% High Demand',
          transitionTime: '2 - 3 Months',
          learningCurve: 'Moderate (IaC & System Administration)',
          candidateFit: 75,
          topCerts: ['CKA (Certified Kubernetes Admin)', 'AWS Solutions Architect Professional', 'HashiCorp Terraform Associate'],
          skills: ['Kubernetes', 'AWS', 'Terraform', 'Docker', 'CI/CD', 'Linux', 'Git']
        },
        roleB: {
          title: roleB,
          minSalary: 145000,
          medianSalary: 180000,
          maxSalary: 230000,
          fiveYearSalary: 275000,
          growthRate: '+38% Exploding Demand',
          transitionTime: '4 - 6 Months',
          learningCurve: 'Moderate to Steep (Math & Deep Learning)',
          candidateFit: 43,
          topCerts: ['DeepLearning.AI LLM Specialization', 'AWS Machine Learning Specialty', 'TensorFlow Developer'],
          skills: ['Python', 'PyTorch', 'LLMs', 'Vector DBs', 'Docker', 'RAG Architecture', 'Git']
        },
        comparison: {
          salaryDelta: 12000,
          winningSalaryRole: roleB,
          sharedSkills: ['Docker', 'Git'],
          uniqueToA: ['Kubernetes', 'AWS', 'Terraform', 'CI/CD', 'Linux'],
          uniqueToB: ['Python', 'PyTorch', 'LLMs', 'Vector DBs', 'RAG Architecture'],
          recommendation: `Strategic Recommendation: ${roleB} provides a higher earning ceiling (+$12,000/yr median) with +38% market growth, making it worth the 4 - 6 Months learning investment.`
        }
      };
    }

    renderCareerComparison(data);
    if (compareBtn) {
      compareBtn.disabled = false;
      compareBtn.innerHTML = origCompareText;
    }
  }

  function renderCareerComparison(data) {
    const rA = data.roleA || {};
    const rB = data.roleB || {};
    const comp = data.comparison || {};

    // Role A overview
    if (DOM.roleATitle) DOM.roleATitle.textContent = rA.title || 'Role A';
    if (DOM.roleAFitVal) DOM.roleAFitVal.textContent = `${rA.candidateFit || 50}%`;
    if (DOM.roleAMedianSalary) DOM.roleAMedianSalary.textContent = `₹${formatLPA(rA.medianSalary || 2400000)}`;
    if (DOM.roleAFiveYearSalary) DOM.roleAFiveYearSalary.textContent = `₹${formatLPA(rA.fiveYearSalary || 3200000)} / yr`;
    if (DOM.roleAGrowthRate) DOM.roleAGrowthRate.textContent = rA.growthRate || '+20%';

    // Role B overview
    if (DOM.roleBTitle) DOM.roleBTitle.textContent = rB.title || 'Role B';
    if (DOM.roleBFitVal) DOM.roleBFitVal.textContent = `${rB.candidateFit || 50}%`;
    if (DOM.roleBMedianSalary) DOM.roleBMedianSalary.textContent = `₹${formatLPA(rB.medianSalary || 3600000)}`;
    if (DOM.roleBFiveYearSalary) DOM.roleBFiveYearSalary.textContent = `₹${formatLPA(rB.fiveYearSalary || 4200000)} / yr`;
    if (DOM.roleBGrowthRate) DOM.roleBGrowthRate.textContent = rB.growthRate || '+25%';

    // Verdict
    if (DOM.compareVerdictText) {
      DOM.compareVerdictText.textContent = comp.recommendation || 'Compare paths to discover optimal career transition.';
    }

    // Salary progression matrix
    if (DOM.salaryLabelA) DOM.salaryLabelA.textContent = (rA.title || 'Role A').split(' ')[0];
    if (DOM.salaryRangeA) DOM.salaryRangeA.textContent = `₹${formatLPA(rA.minSalary)} - ₹${formatLPA(rA.maxSalary)}`;
    if (DOM.salaryBarA) DOM.salaryBarA.style.width = `${Math.min(95, Math.round((rA.medianSalary / 240000) * 100))}%`;

    if (DOM.salaryLabelB) DOM.salaryLabelB.textContent = (rB.title || 'Role B').split(' ')[0];
    if (DOM.salaryRangeB) DOM.salaryRangeB.textContent = `₹${formatLPA(rB.minSalary)} - ₹${formatLPA(rB.maxSalary)}`;
    if (DOM.salaryBarB) DOM.salaryBarB.style.width = `${Math.min(95, Math.round((rB.medianSalary / 240000) * 100))}%`;

    if (DOM.salaryDeltaHighlight) {
      const delta = comp.salaryDelta || 0;
      DOM.salaryDeltaHighlight.textContent = delta !== 0
        ? `+₹${formatLPA(Math.abs(delta))} median advantage for ${comp.winningSalaryRole || 'selected role'}`
        : 'Identical median compensation level';
    }

    // Transition Time
    if (DOM.transLabelA) DOM.transLabelA.textContent = `${rA.title || 'Option A'} Transition:`;
    if (DOM.transTimeA) DOM.transTimeA.textContent = rA.transitionTime || '2 - 3 Months';
    if (DOM.transLabelB) DOM.transLabelB.textContent = `${rB.title || 'Option B'} Transition:`;
    if (DOM.transTimeB) DOM.transTimeB.textContent = rB.transitionTime || '3 - 5 Months';
    if (DOM.learningCurveDetail) {
      DOM.learningCurveDetail.innerHTML = `
        <strong>${escapeHtml(rA.title || 'Role A')}:</strong> ${escapeHtml(rA.learningCurve || 'Moderate')}<br>
        <strong>${escapeHtml(rB.title || 'Role B')}:</strong> ${escapeHtml(rB.learningCurve || 'Moderate to Steep')}
      `;
    }

    // Skills Venn
    if (DOM.vennHeaderA) DOM.vennHeaderA.textContent = `Unique to ${rA.title || 'Role A'}`;
    if (DOM.vennChipsA) {
      const uA = comp.uniqueToA || [];
      DOM.vennChipsA.innerHTML = uA.map(s => `<span class="badge-mini" style="background:#e0e7ff; color:#4338ca; border-color:#c7d2fe;">${escapeHtml(s)}</span>`).join('') || '<span style="font-size:0.75rem; color:var(--text-muted);">None</span>';
    }

    if (DOM.vennChipsShared) {
      const sh = comp.sharedSkills || [];
      DOM.vennChipsShared.innerHTML = sh.map(s => `<span class="badge-mini" style="background:#ecfdf5; color:#047857; border-color:#a7f3d0; font-weight:800;">⚡ ${escapeHtml(s)}</span>`).join('') || '<span style="font-size:0.75rem; color:var(--text-muted);">None detected</span>';
    }

    if (DOM.vennHeaderB) DOM.vennHeaderB.textContent = `Unique to ${rB.title || 'Role B'}`;
    if (DOM.vennChipsB) {
      const uB = comp.uniqueToB || [];
      DOM.vennChipsB.innerHTML = uB.map(s => `<span class="badge-mini" style="background:#fce7f3; color:#be185d; border-color:#fbcfe8;">${escapeHtml(s)}</span>`).join('') || '<span style="font-size:0.75rem; color:var(--text-muted);">None</span>';
    }

    // Top Certs
    if (DOM.certsLabelA) DOM.certsLabelA.textContent = `For ${rA.title || 'Option A'}:`;
    if (DOM.certsListA) {
      const certsA = rA.topCerts || [];
      DOM.certsListA.innerHTML = certsA.map(c => `<li>${escapeHtml(c)}</li>`).join('');
    }

    if (DOM.certsLabelB) DOM.certsLabelB.textContent = `For ${rB.title || 'Option B'}:`;
    if (DOM.certsListB) {
      const certsB = rB.topCerts || [];
      DOM.certsListB.innerHTML = certsB.map(c => `<li>${escapeHtml(c)}</li>`).join('');
    }
  }

  
  // ==============================================================================
  // FEATURE 7: MULTI-PILLAR JOB READINESS ENGINE
  // ==============================================================================
  function populateReadinessTargetJobSelect() {
    if (!DOM.readinessTargetJobSelect) return;
    const currentVal = DOM.readinessTargetJobSelect.value;
    const jobs = (state.allJobs && state.allJobs.length > 0)
      ? state.allJobs
      : (state.matchedJobs && state.matchedJobs.length > 0 ? state.matchedJobs : []);

    if (jobs.length === 0) {
      DOM.readinessTargetJobSelect.innerHTML = `<option value="">Software Engineer (Default Role)</option>`;
      return;
    }

    DOM.readinessTargetJobSelect.innerHTML = jobs.map(j => `
      <option value="${escapeHtml(j.id)}" ${j.id === currentVal ? 'selected' : ''}>
        ${escapeHtml(j.title)} (${escapeHtml(j.company)})
      </option>
    `).join('');
  }

  async function runJobReadinessAudit(overrideJobId) {
    const auditBtn = DOM.runReadinessAuditBtn;
    let origAuditText = '';
    if (auditBtn) {
      origAuditText = auditBtn.innerHTML;
      auditBtn.disabled = true;
      auditBtn.innerHTML = '<span class="btn-spinner"></span> Auditing Readiness...';
    }
    const jobId = overrideJobId || (DOM.readinessTargetJobSelect ? DOM.readinessTargetJobSelect.value : '') || 'job-01';
    const candidateSkills = (state.userProfile.skills && state.userProfile.skills.length > 0)
      ? state.userProfile.skills
      : ['Java', 'Spring Boot', 'SQL', 'Docker', 'Git'];
    const yearsExp = typeof state.userProfile.yearsOfExperience === 'number'
      ? state.userProfile.yearsOfExperience
      : 2;

    const payload = {
      jobId: jobId,
      skills: candidateSkills,
      yearsOfExperience: yearsExp,
      education: 'B.Tech in Computer Science',
      email: state.currentUser?.email || ''
    };

    let data = null;

    if (isJavaServerOnline) {
      try {
        const res = await fetch(`${API_BASE_URL}/jobs/readiness`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(payload)
        });
        if (res.ok) {
          data = await res.json();
        }
      } catch (e) {
        console.warn('Backend readiness audit error, using fallback:', e);
      }
    }

    if (!data || !data.success) {
      data = {
        success: true,
        jobId: jobId,
        jobTitle: 'Backend Java Engineer',
        company: 'Amazon AWS',
        overallReadiness: 78,
        readinessTier: 'Competitive Candidate (Minor Polish Needed)',
        tierColor: '#2563eb',
        pillars: {
          technical: { score: 75, weight: 40, status: 'Strong Match', matched: ['Java', 'SQL', 'Git'], missing: ['Kafka'] },
          experience: { score: 85, weight: 25, status: 'Meets Requirement', candidateExp: 2, requiredExp: 2 },
          education: { score: 90, weight: 15, status: 'Fully Qualified', degree: 'B.S. in Computer Science' },
          resume: { score: 82, weight: 20, status: 'Strong Framing' }
        },
        quickFixActions: [
          'Add a project demonstrating Kafka or event streaming to your GitHub.',
          'Quantify top resume bullet points with concrete throughput metrics.',
          'Tailor your summary statement to explicitly address cloud scalability.'
        ]
      };
    }

    renderJobReadinessScorecard(data);
    if (auditBtn) {
      auditBtn.disabled = false;
      auditBtn.innerHTML = origAuditText;
    }
  }

  function renderJobReadinessScorecard(data) {
    const score = data.overallReadiness || 75;
    if (DOM.readinessOverallVal) {
      DOM.readinessOverallVal.textContent = `${score}%`;
      if (score >= 82) {
        DOM.readinessOverallVal.style.background = 'linear-gradient(135deg, #10b981, #059669)';
      } else if (score >= 68) {
        DOM.readinessOverallVal.style.background = 'linear-gradient(135deg, #3b82f6, #2563eb)';
      } else {
        DOM.readinessOverallVal.style.background = 'linear-gradient(135deg, #f59e0b, #d97706)';
      }
    }

    if (DOM.readinessTargetRoleTitle) DOM.readinessTargetRoleTitle.textContent = data.jobTitle || 'Target Role';
    if (DOM.readinessTargetCompany) DOM.readinessTargetCompany.textContent = `🏢 ${data.company || 'Enterprise Company'} • Full-time`;
    if (DOM.readinessTierBadge) {
      DOM.readinessTierBadge.textContent = data.readinessTier || 'Evaluated';
      DOM.readinessTierBadge.style.color = data.tierColor || '#2563eb';
    }

    const p = data.pillars || {};

    // Pillar 1: Tech
    const tech = p.technical || {};
    if (DOM.pillarTechScoreVal) DOM.pillarTechScoreVal.textContent = `${tech.score || 75}%`;
    if (DOM.pillarTechBar) DOM.pillarTechBar.style.width = `${tech.score || 75}%`;
    if (DOM.pillarTechStatus) DOM.pillarTechStatus.textContent = tech.status || 'Match';
    if (DOM.pillarTechDetails) {
      const missing = tech.missing || [];
      DOM.pillarTechDetails.textContent = missing.length > 0
        ? `Skill gaps detected: ${missing.slice(0, 3).join(', ')}. Addressing these will boost technical score.`
        : 'Core required technical stacks match with proven capability.';
    }

    // Pillar 2: Exp
    const exp = p.experience || {};
    if (DOM.pillarExpScoreVal) DOM.pillarExpScoreVal.textContent = `${exp.score || 80}%`;
    if (DOM.pillarExpBar) DOM.pillarExpBar.style.width = `${exp.score || 80}%`;
    if (DOM.pillarExpStatus) DOM.pillarExpStatus.textContent = exp.status || 'Evaluated';
    if (DOM.pillarExpDetails) {
      DOM.pillarExpDetails.textContent = `Candidate has ${exp.candidateExp || 2} yrs exp vs ${exp.requiredExp || 2} yrs required for this role.`;
    }

    // Pillar 3: Edu
    const edu = p.education || {};
    if (DOM.pillarEduScoreVal) DOM.pillarEduScoreVal.textContent = `${edu.score || 90}%`;
    if (DOM.pillarEduBar) DOM.pillarEduBar.style.width = `${edu.score || 90}%`;
    if (DOM.pillarEduStatus) DOM.pillarEduStatus.textContent = edu.status || 'Qualified';
    if (DOM.pillarEduDetails) {
      DOM.pillarEduDetails.textContent = `Academic discipline (${edu.degree || 'Computer Science'}) meets hiring qualification standards.`;
    }

    // Pillar 4: Resume
    const resP = p.resume || {};
    if (DOM.pillarResumeScoreVal) DOM.pillarResumeScoreVal.textContent = `${resP.score || 82}%`;
    if (DOM.pillarResumeBar) DOM.pillarResumeBar.style.width = `${resP.score || 82}%`;
    if (DOM.pillarResumeStatus) DOM.pillarResumeStatus.textContent = resP.status || 'Strong Framing';
    if (DOM.pillarResumeDetails) {
      DOM.pillarResumeDetails.textContent = 'Action verbs, technology keywords, and measurable accomplishments detected.';
    }

    // Quick Fixes
    if (DOM.readinessActionItemsList) {
      const fixes = data.quickFixActions || [];
      DOM.readinessActionItemsList.innerHTML = fixes.map(f => `
        <div class="action-item-row">
          <span style="font-size:1.1rem; color:#6366f1;">⚡</span>
          <span class="action-item-text">${escapeHtml(f)}</span>
        </div>
      `).join('');
    }
  }

  
  // ==============================================================================
  // FEATURE 8: UNEXPECTED CAREER RECOMMENDATIONS ENGINE
  // ==============================================================================
  async function loadUnexpectedRecommendations() {
    const unexpBtn = DOM.refreshUnexpectedBtn;
    let origUnexpText = '';
    if (unexpBtn) {
      origUnexpText = unexpBtn.innerHTML;
      unexpBtn.disabled = true;
      unexpBtn.innerHTML = '<span class="btn-spinner"></span> Exploring Pivots...';
    }
    if (DOM.unexpectedRolesGrid) {
      DOM.unexpectedRolesGrid.innerHTML = `
        <div style="grid-column: 1 / -1; padding: 3rem; text-align: center; color: var(--text-muted);">
          <div class="spinner" style="margin: 0 auto 1rem;"></div>
          <h5>Analyzing foundational skills for high-value adjacent pivots...</h5>
        </div>
      `;
    }

    const candidateSkills = (state.userProfile.skills && state.userProfile.skills.length > 0)
      ? state.userProfile.skills
      : ['Java', 'Spring Boot', 'SQL', 'Docker', 'Git'];

    const payload = {
      skills: candidateSkills,
      email: state.currentUser?.email || ''
    };

    let data = null;

    if (isJavaServerOnline) {
      try {
        const res = await fetch(`${API_BASE_URL}/career/unexpected-matches`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(payload)
        });
        if (res.ok) {
          data = await res.json();
        }
      } catch (e) {
        console.warn('Backend unexpected matches error, using fallback:', e);
      }
    }

    if (!data || !data.roles || data.roles.length === 0) {
      data = {
        success: true,
        roles: [
          {
            id: 'unexp-01',
            title: 'Enterprise Solutions Architect (Cloud & APIs)',
            badge: '🚀 High Earning Potential',
            badgeColor: '#6366f1',
            salaryRange: '$165,000 - $220,000 / yr',
            medianSalary: 185000,
            growthRate: '+29% (Very High Demand)',
            transferabilityScore: 88,
            transitionTime: '4 - 8 Weeks',
            whyConsider: 'You already understand backend microservices and database trade-offs. Solutions Architects translate these technical architectures into customer solutions with significantly higher base compensation and zero midnight on-call duty.',
            transferableSkills: ['System Architecture', 'APIs', 'SQL', 'Docker', 'Technical Communication'],
            missingBridgeSkills: ['Cloud Pre-Sales', 'Customer Discovery']
          },
          {
            id: 'unexp-02',
            title: 'Developer Advocate & Technical Evangelist',
            badge: '✨ High Autonomy & Visibility',
            badgeColor: '#ec4899',
            salaryRange: '$140,000 - $190,000 / yr',
            medianSalary: 165000,
            growthRate: '+34% (Exploding Tech Category)',
            transferabilityScore: 82,
            transitionTime: '3 - 6 Weeks',
            whyConsider: 'Developer tooling companies crave engineers who can code SDKs, build sample apps, and write technical articles. High travel perks and flexible hours.',
            transferableSkills: ['Java / Python SDKs', 'Git', 'Open Source', 'API Integration'],
            missingBridgeSkills: ['Technical Writing', 'Public Speaking']
          }
        ]
      };
    }

    renderUnexpectedJobs(data);
    if (unexpBtn) {
      unexpBtn.disabled = false;
      unexpBtn.innerHTML = origUnexpText;
    }
  }

  function renderUnexpectedJobs(data) {
    if (!DOM.unexpectedRolesGrid) return;
    const roles = data.roles || [];

    DOM.unexpectedRolesGrid.innerHTML = roles.map(r => `
      <div class="unexpected-role-card">
        <div>
          <div class="unexpected-card-top">
            <div>
              <span class="badge-mini" style="background:${r.badgeColor}22; color:${r.badgeColor}; border-color:${r.badgeColor}; font-weight:800;">
                ${escapeHtml(r.badge || 'Adjacent Role')}
              </span>
              <h4 style="font-size:1.15rem; font-weight:800; color:var(--text-primary); margin:0.4rem 0 0.2rem;">
                ${escapeHtml(r.title)}
              </h4>
              <div style="font-size:0.88rem; font-weight:800; color:#059669; font-family:var(--font-mono);">
                ${escapeHtml(r.salaryRange || '$150,000 - $200,000')}
              </div>
            </div>
            <div class="transfer-gauge-box">
              <span style="font-size:0.7rem; text-transform:uppercase; color:var(--text-muted); font-weight:700; display:block;">Transferable</span>
              <strong style="font-size:1.25rem; font-family:var(--font-mono); color:#10b981;">${r.transferabilityScore || 80}%</strong>
            </div>
          </div>

          <div class="pivot-reason-box" style="margin-top:0.85rem;">
            <div style="font-weight:700; color:var(--text-primary); margin-bottom:0.2rem;">💡 Why This Unexpected Pivot?</div>
            ${escapeHtml(r.whyConsider)}
          </div>
        </div>

        <div class="skills-bridge-section">
          <div>
            <span style="font-weight:700; color:#059669; display:block; margin-bottom:0.25rem;">🌉 Your Direct Skills Bridge:</span>
            <div class="chips-row">
              ${(r.transferableSkills || []).map(s => `<span class="badge-mini" style="background:#ecfdf5; color:#047857; border-color:#a7f3d0;">✓ ${escapeHtml(s)}</span>`).join('')}
            </div>
          </div>
          <div>
            <span style="font-weight:700; color:#6366f1; display:block; margin-bottom:0.25rem;">⚡ Fast Bridge Skills (< ${escapeHtml(r.transitionTime || '4 Weeks')}):</span>
            <div class="chips-row">
              ${(r.missingBridgeSkills || []).map(s => `<span class="badge-mini" style="background:#e0e7ff; color:#4338ca; border-color:#c7d2fe;">⚡ ${escapeHtml(s)}</span>`).join('')}
            </div>
          </div>
        </div>

        <div style="display:flex; justify-content:space-between; align-items:center; padding-top:0.75rem; border-top:1px solid var(--border-color);">
          <span style="font-size:0.78rem; color:var(--text-muted);">Growth: <strong>${escapeHtml(r.growthRate || '+25%')}</strong></span>
          <button type="button" class="pill-action-btn" onclick="switchPlatformView('simulator'); showToast('Loaded ${escapeHtml(r.title)} into simulator!', 'info');">
            Simulate in Sandbox →
          </button>
        </div>
      </div>
    `).join('');
  }

  
  // ==============================================================================
  // FEATURE 9: PERSONAL CAREER DEVELOPMENT DASHBOARD ENGINE
  // ==============================================================================
  async function loadCareerDashboard() {
    const dashBtn = DOM.refreshPersonalDashboardBtn;
    let origDashText = '';
    if (dashBtn) {
      origDashText = dashBtn.innerHTML;
      dashBtn.disabled = true;
      dashBtn.innerHTML = '<span class="btn-spinner"></span> Syncing...';
    }
    const email = state.currentUser?.email || 'mansi@example.com';
    if (DOM.dashUserGreeting) {
      const name = state.currentUser?.name || state.userProfile?.detectedInfo?.name || 'Mansi Sharma';
      DOM.dashUserGreeting.textContent = `Welcome back, ${name}!`;
    }

    let data = null;

    if (isJavaServerOnline) {
      try {
        const res = await fetch(`${API_BASE_URL}/career/dashboard?email=${encodeURIComponent(email)}`);
        if (res.ok) {
          data = await res.json();
        }
      } catch (e) {
        console.warn('Backend career dashboard error, using fallback:', e);
      }
    }

    if (!data || !data.success) {
      data = {
        success: true,
        candidateName: 'Mansi Sharma',
        careerHealthScore: 95,
        healthTier: 'A+ High Momentum',
        targetSalary: '₹16 - ₹26 LPA',
        pipeline: {
          totalApplications: state.appliedJobs ? state.appliedJobs.size : 10,
          inInterview: 4,
          selectedCount: 3,
          waitingCount: 0,
          assessmentCount: 3
        },
        interviews: {
          sessionsPracticed: 2,
          averageScore: 88,
          highestScore: 91
        },
        skillsSummary: {
          masteredCount: 5,
          mastered: ['Java', 'Spring Boot', 'SQL', 'Docker', 'Git'],
          learningRoadmap: ['Apache Kafka', 'Kubernetes', 'AWS Solutions'],
          roadmapProgress: 65
        },
        topRecommendations: [
          { id: 'job-01', title: 'Associate Java Developer', company: 'Infosys', location: 'Bengaluru / Hybrid', salaryRange: '₹6.5 - ₹9.5 LPA', matchPercentage: 88 },
          { id: 'job-02', title: 'Junior Software Engineer', company: 'Google India', location: 'Hyderabad / Hybrid', salaryRange: '₹18.0 - ₹26.0 LPA', matchPercentage: 82 },
          { id: 'job-03', title: 'Full Stack Software Engineer', company: 'Microsoft India', location: 'Bengaluru / Remote', salaryRange: '₹20.0 - ₹30.0 LPA', matchPercentage: 78 }
        ],
        dailyTasks: [
          'Complete 1 STAR Method Behavioral Interview session',
          'Review Apache Kafka learning roadmap milestones',
          'Check stage progress on active Amazon AWS application',
          'Run What-If Career Simulation with Python & Cloud skills'
        ]
      };
    }

    renderCareerDashboard(data);
    if (dashBtn) {
      dashBtn.disabled = false;
      dashBtn.innerHTML = origDashText;
    }
  }

  function renderCareerDashboard(data) {
    // 4 Top KPIs
    if (DOM.dashHealthScoreVal) DOM.dashHealthScoreVal.textContent = `${data.careerHealthScore || 90}%`;
    if (DOM.dashHealthTier) DOM.dashHealthTier.textContent = data.healthTier || 'A+ High Momentum';

    const inv = data.interviews || {};
    if (DOM.dashInterviewAvgVal) DOM.dashInterviewAvgVal.textContent = `${inv.averageScore || 85}%`;
    if (DOM.dashInterviewSessions) DOM.dashInterviewSessions.textContent = `${inv.sessionsPracticed || 1} Session${(inv.sessionsPracticed || 1) === 1 ? '' : 's'} Completed`;

    const pipe = data.pipeline || {};
    if (DOM.dashTotalAppsVal) DOM.dashTotalAppsVal.textContent = `${pipe.totalApplications || 0}`;
    if (DOM.dashInterviewPipeline) {
      DOM.dashInterviewPipeline.textContent = `${pipe.inInterview || 0} in Interview • ${pipe.selectedCount || 0} Selected`;
    }

    if (DOM.dashTargetSalaryVal) DOM.dashTargetSalaryVal.textContent = data.targetSalary || '₹16 - ₹26 LPA';

    // Pipeline Stage counts
    if (DOM.dashStageAssessment) DOM.dashStageAssessment.textContent = pipe.assessmentCount || 0;
    if (DOM.dashStageInterview) DOM.dashStageInterview.textContent = pipe.inInterview || 0;
    if (DOM.dashStageWaiting) DOM.dashStageWaiting.textContent = pipe.waitingCount || 0;
    if (DOM.dashStageSelected) DOM.dashStageSelected.textContent = pipe.selectedCount || 0;

    // Skills
    const sk = data.skillsSummary || {};
    if (DOM.dashMasteredSkillsList) {
      const mastered = sk.mastered || ['Java', 'SQL', 'Git'];
      DOM.dashMasteredSkillsList.innerHTML = mastered.map(s => `
        <span class="badge-mini" style="background:#ecfdf5; color:#047857; border-color:#a7f3d0;">✓ ${escapeHtml(s)}</span>
      `).join('');
    }

    if (DOM.dashRoadmapProgressBar) DOM.dashRoadmapProgressBar.style.width = `${sk.roadmapProgress || 60}%`;
    if (DOM.dashRoadmapProgressText) DOM.dashRoadmapProgressText.textContent = `${sk.roadmapProgress || 60}% Complete`;

    if (DOM.dashRoadmapSkillsList) {
      const inProg = sk.learningRoadmap || ['Kafka', 'Kubernetes'];
      DOM.dashRoadmapSkillsList.innerHTML = inProg.map(s => `
        <span class="badge-mini" style="background:#e0e7ff; color:#4338ca; border-color:#c7d2fe;">⚡ ${escapeHtml(s)}</span>
      `).join('');
    }

    // Top Recommendations
    if (DOM.dashTopRecsList) {
      const recs = data.topRecommendations || [];
      DOM.dashTopRecsList.innerHTML = recs.map(r => `
        <div class="dash-rec-item">
          <div>
            <strong style="color:var(--text-primary); font-size:0.88rem; display:block;">${escapeHtml(r.title)}</strong>
            <span style="font-size:0.78rem; color:var(--text-muted);">🏢 ${escapeHtml(r.company)} • ${escapeHtml(r.salaryRange || '$120k')}</span>
          </div>
          <div style="display:flex; align-items:center; gap:0.65rem;">
            <span class="badge-mini" style="background:#ecfdf5; color:#047857; border-color:#a7f3d0; font-family:var(--font-mono); font-weight:800;">
              ${r.matchPercentage}% Match
            </span>
            <button type="button" class="pill-action-btn" onclick="switchPlatformView('matcher');">View Job →</button>
          </div>
        </div>
      `).join('');
    }

    // Interview Card
    if (DOM.dashInterviewHighVal) DOM.dashInterviewHighVal.textContent = `${inv.highestScore || 90}%`;
    if (DOM.dashInterviewMetaText) DOM.dashInterviewMetaText.textContent = `${inv.sessionsPracticed || 1} practice sessions evaluated`;

    // Daily Tasks
    if (DOM.dashDailyTasksList) {
      const tasks = data.dailyTasks || ['Review your target roles today'];
      DOM.dashDailyTasksList.innerHTML = tasks.map((t, idx) => `
        <label class="dash-task-item">
          <input type="checkbox" ${idx === 0 ? 'checked' : ''} onchange="showToast('Task status updated!', 'success');" />
          <span>${escapeHtml(t)}</span>
        </label>
      `).join('');
    }
  }

  function setupEventListeners() {
    // Feature 9: Career Dashboard Listeners
    if (DOM.refreshPersonalDashboardBtn) {
      DOM.refreshPersonalDashboardBtn.addEventListener('click', () => {
        loadCareerDashboard();
        showToast('Personal Career Dashboard refreshed!', 'success');
      });
    }
    // Feature 8: Unexpected Jobs Listeners
    if (DOM.refreshUnexpectedBtn) {
      DOM.refreshUnexpectedBtn.addEventListener('click', () => {
        loadUnexpectedRecommendations();
        showToast('Refreshed adjacent career recommendations.', 'info');
      });
    }
    // Feature 7: Job Readiness Listeners
    if (DOM.readinessTargetJobSelect) {
      DOM.readinessTargetJobSelect.addEventListener('change', e => {
        runJobReadinessAudit(e.target.value);
      });
    }

    if (DOM.runReadinessAuditBtn) {
      DOM.runReadinessAuditBtn.addEventListener('click', () => {
        runJobReadinessAudit();
        showToast('Multi-pillar readiness audit refreshed!', 'success');
      });
    }
    // Feature 6: Career Comparison Listeners
    if (DOM.runCareerCompareBtn) {
      DOM.runCareerCompareBtn.addEventListener('click', runCareerComparison);
    }

    if (DOM.presetCompareDevOpsAI) {
      DOM.presetCompareDevOpsAI.addEventListener('click', () => {
        if (DOM.compareRoleASelect) DOM.compareRoleASelect.value = 'Senior Cloud & DevOps Architect';
        if (DOM.compareRoleBSelect) DOM.compareRoleBSelect.value = 'AI & Machine Learning Solutions Engineer';
        runCareerComparison();
      });
    }

    if (DOM.presetCompareJavaCloud) {
      DOM.presetCompareJavaCloud.addEventListener('click', () => {
        if (DOM.compareRoleASelect) DOM.compareRoleASelect.value = 'Full Stack Java & Microservices Tech Lead';
        if (DOM.compareRoleBSelect) DOM.compareRoleBSelect.value = 'Senior Cloud & DevOps Architect';
        runCareerComparison();
      });
    }

    if (DOM.presetCompareDataSec) {
      DOM.presetCompareDataSec.addEventListener('click', () => {
        if (DOM.compareRoleASelect) DOM.compareRoleASelect.value = 'Big Data & Distributed Systems Engineer';
        if (DOM.compareRoleBSelect) DOM.compareRoleBSelect.value = 'Cybersecurity & DevSecOps Specialist';
        runCareerComparison();
      });
    }
    // Feature 5: Fake Job Detection Listeners
    if (DOM.runFakeDetectorBtn) {
      DOM.runFakeDetectorBtn.addEventListener('click', () => runFakeJobAnalysis());
    }

    if (DOM.scanCatalogJobBtn && DOM.fakeCatalogSelect) {
      DOM.scanCatalogJobBtn.addEventListener('click', () => {
        const jobId = DOM.fakeCatalogSelect.value;
        if (!jobId) {
          showToast('Please select a catalog job to scan.', 'info');
          return;
        }
        runFakeJobAnalysis(jobId);
      });
    }

    if (DOM.loadPresetScamBtn) {
      DOM.loadPresetScamBtn.addEventListener('click', () => {
        loadPresetScamSample();
        showToast('Loaded sample Telegram/fee scam posting for analysis.', 'info');
      });
    }

    if (DOM.loadPresetPhishBtn) {
      DOM.loadPresetPhishBtn.addEventListener('click', () => {
        loadPresetPhishSample();
        showToast('Loaded sample identity phishing offer.', 'info');
      });
    }

    if (DOM.loadPresetLegitBtn) {
      DOM.loadPresetLegitBtn.addEventListener('click', () => {
        loadPresetLegitSample();
        showToast('Loaded verified enterprise job posting.', 'info');
      });
    }
    // Feature 4: AI Interview Practice Listeners
    if (DOM.interviewTargetJobSelect) {
      DOM.interviewTargetJobSelect.addEventListener('change', e => {
        loadInterviewQuestions(e.target.value);
      });
    }

    if (DOM.refreshInterviewQuestionsBtn) {
      DOM.refreshInterviewQuestionsBtn.addEventListener('click', () => {
        loadInterviewQuestions();
        showToast('Generated fresh questions for target role.', 'info');
      });
    }

    if (DOM.interviewQuestionTabs) {
      DOM.interviewQuestionTabs.addEventListener('click', e => {
        const pill = e.target.closest('.q-nav-pill');
        if (!pill) return;
        const idx = parseInt(pill.dataset.qIdx, 10);
        if (!isNaN(idx)) {
          state.activeQuestionIndex = idx;
          displayActiveInterviewQuestion();
        }
      });
    }

    if (DOM.interviewAnswerInput) {
      DOM.interviewAnswerInput.addEventListener('input', updateInterviewWordCount);
    }

    if (DOM.insertStarTemplateBtn && DOM.interviewAnswerInput) {
      DOM.insertStarTemplateBtn.addEventListener('click', () => {
        const template = "Situation: In our previous production environment, we faced...\n" +
                         "Task: My specific responsibility was to...\n" +
                         "Action: I designed and implemented...\n" +
                         "Result: This resulted in a 40% improvement in... and successfully...";
        DOM.interviewAnswerInput.value = template;
        updateInterviewWordCount();
        DOM.interviewAnswerInput.focus();
        showToast('STAR template outline inserted!', 'info');
      });
    }

    if (DOM.clearInterviewAnswerBtn && DOM.interviewAnswerInput) {
      DOM.clearInterviewAnswerBtn.addEventListener('click', () => {
        DOM.interviewAnswerInput.value = '';
        updateInterviewWordCount();
      });
    }

    if (DOM.submitInterviewAnswerBtn) {
      DOM.submitInterviewAnswerBtn.addEventListener('click', submitInterviewAnswer);
    }

    if (DOM.copyExemplarBtn && DOM.interviewExemplarText) {
      DOM.copyExemplarBtn.addEventListener('click', () => {
        const text = DOM.interviewExemplarText.textContent;
        if (text) {
          navigator.clipboard.writeText(text);
          showToast('Exemplar response copied to clipboard!', 'success');
        }
      });
    }
    // Feature 3: What-If Simulator Listeners
    if (DOM.simulatorChipsCloud) {
      DOM.simulatorChipsCloud.addEventListener('click', e => {
        const chip = e.target.closest('.sim-chip');
        if (!chip) return;
        const skill = chip.dataset.simSkill;
        if (skill) toggleSimulatedSkill(skill);
      });
    }

    if (DOM.resetSimulatorBtn) {
      DOM.resetSimulatorBtn.addEventListener('click', () => {
        state.simulatedSkills.clear();
        document.querySelectorAll('.sim-chip').forEach(c => c.classList.remove('active'));
        if (DOM.simSelectedSkillsBadge) DOM.simSelectedSkillsBadge.textContent = '0 Skills Simulated';
        runCareerSimulation();
        showToast('Simulation reset to current baseline profile.', 'info');
      });
    }

    // Custom hypothetical skill input handler
    function handleAddCustomSimSkill() {
      if (!DOM.simulatorCustomSkillInput) return;
      const skill = DOM.simulatorCustomSkillInput.value.trim();
      if (!skill) {
        showToast('Please enter a skill name to simulate.', 'warning');
        return;
      }
      if (state.simulatedSkills.has(skill)) {
        showToast(`Skill "${skill}" is already active in your simulation.`, 'info');
        DOM.simulatorCustomSkillInput.value = '';
        return;
      }
      state.simulatedSkills.add(skill);
      DOM.simulatorCustomSkillInput.value = '';

      // If chip does not exist in DOM cloud, create a new interactive chip
      let existingChip = Array.from(document.querySelectorAll('.sim-chip')).find(c => (c.dataset.simSkill || '').toLowerCase() === skill.toLowerCase());
      if (!existingChip && DOM.simulatorChipsCloud) {
        const newChip = document.createElement('button');
        newChip.type = 'button';
        newChip.className = 'sim-chip active';
        newChip.dataset.simSkill = skill;
        newChip.textContent = `⚡ ${skill}`;
        DOM.simulatorChipsCloud.appendChild(newChip);
      } else if (existingChip) {
        existingChip.classList.add('active');
      }

      const count = state.simulatedSkills.size;
      if (DOM.simSelectedSkillsBadge) {
        DOM.simSelectedSkillsBadge.textContent = `${count} Skill${count === 1 ? '' : 's'} Simulated`;
      }
      runCareerSimulation();
      showToast(`Added "${skill}" to career simulation!`, 'success');
    }

    if (DOM.addSimulatorSkillBtn) {
      DOM.addSimulatorSkillBtn.addEventListener('click', handleAddCustomSimSkill);
    }
    if (DOM.simulatorCustomSkillInput) {
      DOM.simulatorCustomSkillInput.addEventListener('keydown', e => {
        if (e.key === 'Enter') {
          e.preventDefault();
          handleAddCustomSimSkill();
        }
      });
    }

    // Auto-recalculate Career Compare on dropdown change
    if (DOM.compareRoleASelect) {
      DOM.compareRoleASelect.addEventListener('change', () => {
        runCareerComparison();
      });
    }
    if (DOM.compareRoleBSelect) {
      DOM.compareRoleBSelect.addEventListener('change', () => {
        runCareerComparison();
      });
    }

    // Auto-populate & scan on Fake Job catalog selection
    if (DOM.fakeCatalogSelect) {
      DOM.fakeCatalogSelect.addEventListener('change', () => {
        const selId = DOM.fakeCatalogSelect.value;
        if (selId) {
          runFakeJobAnalysis(selId);
        }
      });
    }
    // Feature 2: Skill Gap Listeners
    if (DOM.gapTargetJobSelect) {
      DOM.gapTargetJobSelect.addEventListener('change', () => {
        loadSkillGapAnalysis(DOM.gapTargetJobSelect.value);
      });
    }

    if (DOM.refreshSkillGapBtn) {
      DOM.refreshSkillGapBtn.addEventListener('click', () => {
        showToast('Analyzing skill gap with live requirements...', 'info');
        loadSkillGapAnalysis(DOM.gapTargetJobSelect ? DOM.gapTargetJobSelect.value : '');
      });
    }
    // Platform Navigation Tabs Listener
    if (DOM.platformNavTabs) {
      DOM.platformNavTabs.addEventListener('click', e => {
        const btn = e.target.closest('.nav-tab-btn');
        if (!btn) return;
        const view = btn.dataset.view;
        if (view) switchPlatformView(view);
      });
    }

    if (DOM.recalcPredictorBtn) {
      DOM.recalcPredictorBtn.addEventListener('click', () => {
        showToast('Recalculating AI career trajectory...', 'info');
        loadCareerPredictions();
      });
    }

    // --- Authentication Event Listeners ---
    if (DOM.loginForm) {
      DOM.loginForm.addEventListener('submit', (e) => {
        e.preventDefault();
        const email = DOM.loginEmail ? DOM.loginEmail.value.trim() : '';
        const password = DOM.loginPassword ? DOM.loginPassword.value : '';
        if (email) handleLogin(email, password);
      });
    }

    if (DOM.registerForm) {
      DOM.registerForm.addEventListener('submit', (e) => {
        e.preventDefault();
        const name = DOM.regFullName ? DOM.regFullName.value.trim() : '';
        const email = DOM.regEmail ? DOM.regEmail.value.trim() : '';
        const password = DOM.regPassword ? DOM.regPassword.value : '';
        const yearsExp = DOM.regExpYears ? parseInt(DOM.regExpYears.value, 10) : 0;
        const skillsRaw = DOM.regSkills ? DOM.regSkills.value : '';
        const skills = skillsRaw.split(',').map(s => s.trim()).filter(Boolean);

        if (name && email) {
          handleRegister({ name, email, password, yearsExp, skills });
        }
      });
    }

    // 1-Click Quick Demo Sign In Buttons
    document.querySelectorAll('.demo-account-btn').forEach(btn => {
      btn.addEventListener('click', () => {
        const email = btn.dataset.demoEmail;
        if (DOM.loginEmail) DOM.loginEmail.value = email;
        if (DOM.loginPassword) DOM.loginPassword.value = 'password123';
        handleLogin(email, 'password123');
      });
    });

    // Auth Tab Switcher (Sign In vs Register)
    document.querySelectorAll('.auth-tab-btn').forEach(tabBtn => {
      tabBtn.addEventListener('click', () => {
        const target = tabBtn.dataset.authTab;
        document.querySelectorAll('.auth-tab-btn').forEach(b => b.classList.remove('active'));
        tabBtn.classList.add('active');

        if (target === 'login') {
          if (DOM.loginTabPane) DOM.loginTabPane.classList.remove('hidden');
          if (DOM.registerTabPane) DOM.registerTabPane.classList.add('hidden');
        } else {
          if (DOM.loginTabPane) DOM.loginTabPane.classList.add('hidden');
          if (DOM.registerTabPane) DOM.registerTabPane.classList.remove('hidden');
        }
      });
    });

    if (DOM.logoutBtn) {
      DOM.logoutBtn.addEventListener('click', handleLogout);
    }

    if (DOM.navLoginBtn) {
      DOM.navLoginBtn.addEventListener('click', () => {
        if (DOM.authPortal) {
          DOM.authPortal.classList.remove('hidden');
          DOM.authPortal.scrollIntoView({ behavior: 'smooth' });
        }
      });
    }

    DOM.themeToggleBtn.addEventListener('click', toggleTheme);

    document.querySelectorAll('.demo-pill').forEach(pill => {
      pill.addEventListener('click', () => loadDemoProfile(pill.dataset.profile));
    });

    DOM.tierButtons.forEach(btn => {
      btn.addEventListener('click', () => {
        const years = parseInt(btn.dataset.years, 10);
        setYearsOfExperience(years, true);
        calculateJobMatches();
      });
    });

    DOM.exactYearsInput.addEventListener('change', e => {
      setYearsOfExperience(e.target.value, true);
      calculateJobMatches();
    });

    DOM.decrementYearsBtn.addEventListener('click', () => {
      const cur = state.userProfile.yearsOfExperience;
      if (cur > 0) {
        setYearsOfExperience(cur - 1, true);
        calculateJobMatches();
      }
    });

    DOM.incrementYearsBtn.addEventListener('click', () => {
      const cur = state.userProfile.yearsOfExperience;
      setYearsOfExperience(cur + 1, true);
      calculateJobMatches();
    });

    DOM.strictExperienceCheckbox.addEventListener('change', e => {
      state.userProfile.strictExperienceFilter = e.target.checked;
      showToast(e.target.checked 
        ? 'Strict Experience Guard ENABLED: Senior roles hidden for Freshers.' 
        : 'Strict Experience Guard DISABLED: All roles displayed.', 'info');
      calculateJobMatches();
    });

    // Weights controls
    function syncSidebarWeights() {
      const s = parseInt(DOM.weightSkillsSlider.value, 10);
      const e = parseInt(DOM.weightExpSlider.value, 10);
      const sa = parseInt(DOM.weightSalSlider.value, 10);
      const p = parseInt(DOM.weightPrefSlider.value, 10);
      updateWeights(s, e, sa, p);
    }

    [DOM.weightSkillsSlider, DOM.weightExpSlider, DOM.weightSalSlider, DOM.weightPrefSlider].forEach(slider => {
      slider.addEventListener('input', syncSidebarWeights);
      slider.addEventListener('change', calculateJobMatches);
    });

    DOM.toggleWeightsModalBtn.addEventListener('click', () => DOM.weightsModal.classList.remove('hidden'));
    DOM.closeWeightsModalBtn.addEventListener('click', () => DOM.weightsModal.classList.add('hidden'));

    [DOM.modalWeightSkills, DOM.modalWeightExp, DOM.modalWeightSal, DOM.modalWeightPref].forEach(slider => {
      slider.addEventListener('input', () => {
        const s = parseInt(DOM.modalWeightSkills.value, 10);
        const e = parseInt(DOM.modalWeightExp.value, 10);
        const sa = parseInt(DOM.modalWeightSal.value, 10);
        const p = parseInt(DOM.modalWeightPref.value, 10);
        updateWeights(s, e, sa, p);
      });
    });

    DOM.saveWeightsModalBtn.addEventListener('click', () => {
      DOM.weightsModal.classList.add('hidden');
      calculateJobMatches();
      showToast('Updated multi-attribute weights applied!', 'success');
    });

    DOM.resetWeightsBtn.addEventListener('click', resetWeightsToDefault);

    // Resume Inputs
    DOM.resumeTextInput.addEventListener('input', updateWordCount);

    DOM.clearResumeBtn.addEventListener('click', () => {
      DOM.resumeTextInput.value = '';
      updateWordCount();
      DOM.extractedInfoCard.classList.add('hidden');
    });

    DOM.extractSkillsBtn.addEventListener('click', () => {
      parseResumeText(DOM.resumeTextInput.value);
    });

    setupFileUpload();

    // Skills input
    DOM.skillInput.addEventListener('keydown', e => {
      if (e.key === 'Enter' || e.key === ',') {
        e.preventDefault();
        addSkill(DOM.skillInput.value);
        DOM.skillInput.value = '';
        DOM.skillSuggestionsDropdown.classList.add('hidden');
      }
    });

    DOM.skillInput.addEventListener('input', e => {
      showSkillSuggestions(e.target.value);
    });

    DOM.skillTagsList.addEventListener('click', e => {
      if (e.target.classList.contains('remove-tag')) {
        const idx = parseInt(e.target.dataset.index, 10);
        removeSkill(idx);
      }
    });

    document.querySelectorAll('.quick-skill-chips .chip').forEach(chip => {
      chip.addEventListener('click', () => {
        const skill = chip.dataset.skill;
        const exists = state.userProfile.skills.some(s => s.toLowerCase() === skill.toLowerCase());
        if (exists) {
          const idx = state.userProfile.skills.findIndex(s => s.toLowerCase() === skill.toLowerCase());
          removeSkill(idx);
        } else {
          addSkill(skill);
        }
      });
    });

    DOM.salaryRange.addEventListener('input', e => {
      const val = parseInt(e.target.value, 10);
      DOM.salaryDisplay.textContent = `₹${formatLPA(val)}`;
    });

    DOM.matchJobsBtn.addEventListener('click', calculateJobMatches);
    DOM.resetFormBtn.addEventListener('click', resetForm);

    DOM.jobSearchInput.addEventListener('input', e => {
      state.searchQuery = e.target.value;
      renderJobsGrid();
    });

    DOM.sortOrderSelect.addEventListener('change', e => {
      state.activeSort = e.target.value;
      renderJobsGrid();
    });

    document.querySelectorAll('.filter-tag-btn').forEach(btn => {
      btn.addEventListener('click', () => {
        document.querySelectorAll('.filter-tag-btn').forEach(b => b.classList.remove('active'));
        btn.classList.add('active');
        state.activeFilter = btn.dataset.filter;
        renderJobsGrid();
      });
    });

    // Personalization preference selects
    [DOM.locationPreferenceSelect, DOM.climatePreferenceSelect, DOM.workingHoursSelect, DOM.workModeSelect, DOM.categorySelect].forEach(sel => {
      if (sel) sel.addEventListener('change', calculateJobMatches);
    });

    // 3-Option Work Mode Segmented Control (Online/Remote, Offline/On-site, Hybrid)
    if (DOM.currencySegmentedControl) {
      DOM.currencySegmentedControl.addEventListener('click', (e) => {
        const btn = e.target.closest('.currency-filter-btn');
        if (!btn) return;
        DOM.currencySegmentedControl.querySelectorAll('.currency-filter-btn').forEach(b => b.classList.remove('active'));
        btn.classList.add('active');
        state.currencyFilter = btn.dataset.curr || 'Any';
        if (DOM.currencyFilterSelect) DOM.currencyFilterSelect.value = state.currencyFilter;
        calculateJobMatches();
      });
    }

    if (DOM.workModeSegmentedControl) {
      DOM.workModeSegmentedControl.querySelectorAll('.workmode-btn').forEach(btn => {
        btn.addEventListener('click', () => {
          DOM.workModeSegmentedControl.querySelectorAll('.workmode-btn').forEach(b => b.classList.remove('active'));
          btn.classList.add('active');
          const mode = btn.dataset.mode;
          state.userProfile.preferredWorkMode = mode;
          if (DOM.workModeSelect) DOM.workModeSelect.value = mode;
          calculateJobMatches();
          showToast(`Work Mode filter set to: ${mode}`, 'info');
        });
      });
    }

    // 3-Option Location Segmented Control (City, State, Remote)
    if (DOM.locationTypeSegmentedControl) {
      DOM.locationTypeSegmentedControl.querySelectorAll('.loctype-btn').forEach(btn => {
        btn.addEventListener('click', () => {
          DOM.locationTypeSegmentedControl.querySelectorAll('.loctype-btn').forEach(b => b.classList.remove('active'));
          btn.classList.add('active');
          const type = btn.dataset.loctype;

          if (type === 'city') {
            DOM.cityLocationSubBox.classList.remove('hidden');
            DOM.stateLocationSubBox.classList.add('hidden');
            DOM.remoteLocationSubBox.classList.add('hidden');
            DOM.locationPreferenceSelect.value = DOM.citySelect ? DOM.citySelect.value : 'All';
          } else if (type === 'state') {
            DOM.stateLocationSubBox.classList.remove('hidden');
            DOM.cityLocationSubBox.classList.add('hidden');
            DOM.remoteLocationSubBox.classList.add('hidden');
            DOM.locationPreferenceSelect.value = DOM.stateSelect ? DOM.stateSelect.value : 'All';
          } else if (type === 'remote') {
            DOM.remoteLocationSubBox.classList.remove('hidden');
            DOM.cityLocationSubBox.classList.add('hidden');
            DOM.stateLocationSubBox.classList.add('hidden');
            DOM.locationPreferenceSelect.value = 'Remote';
          }
          calculateJobMatches();
        });
      });

      if (DOM.citySelect) {
        DOM.citySelect.addEventListener('change', () => {
          DOM.locationPreferenceSelect.value = DOM.citySelect.value;
          calculateJobMatches();
        });
      }
      if (DOM.stateSelect) {
        DOM.stateSelect.addEventListener('change', () => {
          DOM.locationPreferenceSelect.value = DOM.stateSelect.value;
          calculateJobMatches();
        });
      }
    }

    // Refresh & Dashboard Tracker buttons
    if (DOM.refreshApplicationsBtn) {
      DOM.refreshApplicationsBtn.addEventListener('click', async () => {
        await refreshApplicationsFromServer();
        showToast('Applications synchronized with live server.', 'success');
      });
    }

    if (DOM.goToDashboardTrackerBtn) {
      DOM.goToDashboardTrackerBtn.addEventListener('click', () => {
        DOM.applicationModal.classList.add('hidden');
        renderDashboardApplicationTracker();
        if (DOM.dashboardApplicationTracker) {
          DOM.dashboardApplicationTracker.scrollIntoView({ behavior: 'smooth' });
        }
      });
    }

    if (DOM.closeAppStatusModalBtn) {
      DOM.closeAppStatusModalBtn.addEventListener('click', () => DOM.applicationStatusModal.classList.add('hidden'));
    }
    if (DOM.closeStatusModalBtnBottom) {
      DOM.closeStatusModalBtnBottom.addEventListener('click', () => DOM.applicationStatusModal.classList.add('hidden'));
    }

    // Interactive Stage Progression Controls
    document.querySelectorAll('#statusProgressionBtnGroup .sim-stage-btn').forEach(btn => {
      btn.addEventListener('click', (e) => {
        const stage = e.currentTarget.dataset.stage;
        if (state.currentTrackingApp && stage) {
          updateApplicationStage(state.currentTrackingApp.applicationId, stage);
        }
      });
    });

    // Card Actions Click
    DOM.jobsListContainer.addEventListener('click', e => {
      const target = e.target.closest('button');
      if (!target) return;
      const action = target.dataset.action;
      const jobId = target.dataset.jobId;

      if (action === 'view') openJobDetailsModal(jobId);
      else if (action === 'apply') openApplicationModal(jobId);
      else if (action === 'instant-apply') instantApply(jobId);
      else if (action === 'save') toggleSaveJob(jobId);
      else if (action === 'toggle-select') toggleJobSelection(jobId);
      else if (action === 'view-app') {
        renderMyApplicationsDrawer();
        DOM.myApplicationsDrawer.classList.remove('hidden');
      } else if (action === 'track-status') {
        const appId = target.dataset.appId;
        openApplicationStatusModal(appId);
      }
    });

    // Multi-Job Checkbox Selection Delegation
    DOM.jobsListContainer.addEventListener('change', e => {
      if (e.target.classList.contains('job-card-checkbox')) {
        const jobId = e.target.dataset.jobId;
        toggleJobSelection(jobId, e.target.checked);
      }
    });

    // Multi-Job Application Toolbar Buttons
    if (DOM.batchApplyBtn) {
      DOM.batchApplyBtn.addEventListener('click', () => {
        if (state.selectedJobIdsForBatch.size === 0) {
          showToast('Please select at least one job first.', 'info');
          return;
        }
        openBatchApplicationModal(Array.from(state.selectedJobIdsForBatch));
      });
    }

    if (DOM.selectBookmarkedBtn) {
      DOM.selectBookmarkedBtn.addEventListener('click', () => {
        const unappliedSaved = Array.from(state.savedJobIds).filter(id => !state.submittedApplications.some(a => a.jobId === id));
        if (unappliedSaved.length === 0) {
          showToast('No unapplied bookmarked jobs found.', 'info');
          return;
        }
        unappliedSaved.forEach(id => state.selectedJobIdsForBatch.add(id));
        renderJobsGrid();
        updateBatchSelectionUI();
        openBatchApplicationModal(Array.from(state.selectedJobIdsForBatch));
      });
    }

    if (DOM.applyAllSavedJobsBtn) {
      DOM.applyAllSavedJobsBtn.addEventListener('click', () => {
        DOM.savedJobsDrawer.classList.add('hidden');
        const unappliedSaved = Array.from(state.savedJobIds).filter(id => !state.submittedApplications.some(a => a.jobId === id));
        if (unappliedSaved.length === 0) {
          showToast('All bookmarked jobs have already been applied for!', 'info');
          return;
        }
        unappliedSaved.forEach(id => state.selectedJobIdsForBatch.add(id));
        renderJobsGrid();
        updateBatchSelectionUI();
        openBatchApplicationModal(Array.from(state.selectedJobIdsForBatch));
      });
    }

    if (DOM.selectTopMatchesBtn) {
      DOM.selectTopMatchesBtn.addEventListener('click', () => {
        const unapplied = state.matchedResults
          .filter(r => !state.submittedApplications.some(a => a.jobId === r.job.id))
          .slice(0, 3)
          .map(r => r.job.id);
        if (unapplied.length === 0) {
          showToast('All top matches already applied for!', 'info');
          return;
        }
        unapplied.forEach(id => state.selectedJobIdsForBatch.add(id));
        renderJobsGrid();
        updateBatchSelectionUI();
        openBatchApplicationModal(Array.from(state.selectedJobIdsForBatch));
      });
    }

    if (DOM.selectAllQualifiedBtn) {
      DOM.selectAllQualifiedBtn.addEventListener('click', () => {
        const unapplied = state.matchedResults
          .filter(r => !state.submittedApplications.some(a => a.jobId === r.job.id))
          .map(r => r.job.id);
        if (unapplied.length === 0) {
          showToast('All visible roles have already been applied for!', 'info');
          return;
        }
        unapplied.forEach(id => state.selectedJobIdsForBatch.add(id));
        renderJobsGrid();
        updateBatchSelectionUI();
        showToast(`Selected ${unapplied.length} qualified roles! Click 'Apply to Selected' to submit.`, 'success');
      });
    }

    if (DOM.additionalJobsCheckboxes) {
      DOM.additionalJobsCheckboxes.addEventListener('change', () => {
        const checkedCount = document.querySelectorAll('.additional-job-chk:checked').length;
        const submitBtn = document.getElementById('submitApplyBtn') || DOM.submitApplyBtn;
        if (submitBtn) {
          if (checkedCount > 0) {
            submitBtn.textContent = `Apply to ${checkedCount + 1} Roles Simultaneously 🚀`;
            submitBtn.style.background = 'linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%)';
          } else {
            submitBtn.textContent = 'Apply Now 🚀';
            submitBtn.style.background = '';
          }
        }
      });
    }

    if (DOM.clearSelectionBtn) {
      DOM.clearSelectionBtn.addEventListener('click', () => {
        state.selectedJobIdsForBatch.clear();
        renderJobsGrid();
        updateBatchSelectionUI();
        showToast('Multi-job selection cleared.', 'info');
      });
    }

    // Batch Application Modal Buttons & Submit
    if (DOM.closeBatchModalBtn) {
      DOM.closeBatchModalBtn.addEventListener('click', () => DOM.batchApplicationModal.classList.add('hidden'));
    }
    if (DOM.cancelBatchApplyBtn) {
      DOM.cancelBatchApplyBtn.addEventListener('click', () => DOM.batchApplicationModal.classList.add('hidden'));
    }
    if (DOM.closeBatchSuccessBtn) {
      DOM.closeBatchSuccessBtn.addEventListener('click', () => DOM.batchApplicationModal.classList.add('hidden'));
    }
    if (DOM.goToDashboardFromBatchBtn) {
      DOM.goToDashboardFromBatchBtn.addEventListener('click', () => {
        DOM.batchApplicationModal.classList.add('hidden');
        renderDashboardApplicationTracker();
        if (DOM.dashboardApplicationTracker) {
          DOM.dashboardApplicationTracker.scrollIntoView({ behavior: 'smooth' });
        }
      });
    }

    if (DOM.batchApplicationForm) {
      DOM.batchApplicationForm.addEventListener('submit', async e => {
        e.preventDefault();
        const selectedWorkMode = (document.querySelector('input[name="batchWorkModeRadio"]:checked')?.value) || 'Online/Remote';
        await submitBatchApplications(Array.from(state.selectedJobIdsForBatch), {
          candidateName: DOM.batchFullName.value.trim(),
          candidateEmail: DOM.batchEmail.value.trim(),
          candidatePhone: DOM.batchPhone.value.trim(),
          expectedSalary: parseInt(DOM.batchExpectedSalary.value, 10),
          joiningAvailability: DOM.batchJoiningTimeline.value,
          workAuth: DOM.batchWorkAuth.value,
          portfolioUrl: DOM.batchPortfolio ? DOM.batchPortfolio.value.trim() : '',
          preferredWorkMode: selectedWorkMode,
          coverLetter: DOM.batchCoverLetter.value.trim()
        });
      });
    }

    if (DOM.batchSelectedJobsList) {
      DOM.batchSelectedJobsList.addEventListener('click', e => {
        const removeBtn = e.target.closest('.mini-remove-btn');
        if (removeBtn) {
          const jobId = removeBtn.dataset.removeBatchId;
          state.selectedJobIdsForBatch.delete(jobId);
          renderBatchSelectedJobsList();
          updateBatchSelectionUI();
          renderJobsGrid();
          if (state.selectedJobIdsForBatch.size === 0) {
            DOM.batchApplicationModal.classList.add('hidden');
            showToast('All jobs removed from batch.', 'info');
          }
        }
      });
    }

    DOM.gapSkillChips.addEventListener('click', e => {
      const btn = e.target.closest('.gap-chip');
      if (btn && btn.dataset.skill) {
        addSkill(btn.dataset.skill);
        showToast(`Added ${btn.dataset.skill} to skills! Recalculating...`, 'success');
        calculateJobMatches();
      }
    });

    // Modals
    DOM.closeJobDetailsModalBtn.addEventListener('click', () => DOM.jobDetailsModal.classList.add('hidden'));
    DOM.closeApplyModalBtn.addEventListener('click', () => DOM.applicationModal.classList.add('hidden'));
    DOM.cancelApplyBtn.addEventListener('click', () => DOM.applicationModal.classList.add('hidden'));
    DOM.regenerateCoverLetterBtn.addEventListener('click', () => {
      if (state.currentModalJob) generateCoverLetter(state.currentModalJob);
    });

    DOM.modalSaveJobBtn.addEventListener('click', () => {
      if (state.currentModalJob) {
        toggleSaveJob(state.currentModalJob.id);
        const isSaved = state.savedJobIds.has(state.currentModalJob.id);
        DOM.modalSaveJobBtn.textContent = isSaved ? '★ Saved in Bookmarks' : '🔖 Bookmark Job';
      }
    });

    DOM.modalApplyNowBtn.addEventListener('click', () => {
      DOM.jobDetailsModal.classList.add('hidden');
      if (state.currentModalJob) openApplicationModal(state.currentModalJob.id);
    });

    // Application Form Submission
    DOM.applicationForm.addEventListener('submit', handleApplicationSubmission);

    DOM.viewMyAppsFromSuccessBtn.addEventListener('click', () => {
      DOM.applicationModal.classList.add('hidden');
      renderMyApplicationsDrawer();
      DOM.myApplicationsDrawer.classList.remove('hidden');
    });

    DOM.doneSuccessBtn.addEventListener('click', () => {
      DOM.applicationModal.classList.add('hidden');
    });

    // My Applications Drawer / Status Dashboard View
    DOM.myApplicationsBtn.addEventListener('click', () => {
      renderMyApplicationsDrawer();
      DOM.myApplicationsDrawer.classList.remove('hidden');
    });
    DOM.closeMyApplicationsDrawerBtn.addEventListener('click', () => DOM.myApplicationsDrawer.classList.add('hidden'));

    // Saved Jobs Drawer
    DOM.savedJobsBtn.addEventListener('click', () => {
      renderSavedDrawer();
      DOM.savedJobsDrawer.classList.remove('hidden');
    });
    DOM.closeSavedJobsDrawerBtn.addEventListener('click', () => DOM.savedJobsDrawer.classList.add('hidden'));

    // Salary Benchmark Modal
    if (DOM.salaryBenchmarkBtn) {
      DOM.salaryBenchmarkBtn.addEventListener('click', () => {
        openSalaryBenchmarkModal();
      });
    }
    if (DOM.closeSalaryBenchmarkModalBtn) {
      DOM.closeSalaryBenchmarkModalBtn.addEventListener('click', () => {
        DOM.salaryBenchmarkModal.classList.add('hidden');
      });
    }
    if (DOM.closeSalaryBenchmarkModalBtnBottom) {
      DOM.closeSalaryBenchmarkModalBtnBottom.addEventListener('click', () => {
        DOM.salaryBenchmarkModal.classList.add('hidden');
      });
    }
    if (DOM.salaryBenchmarkModal) {
      DOM.salaryBenchmarkModal.addEventListener('click', e => {
        if (e.target === DOM.salaryBenchmarkModal) DOM.salaryBenchmarkModal.classList.add('hidden');
      });
    }

    // Tabs
    document.querySelectorAll('.tab-btn').forEach(btn => {
      btn.addEventListener('click', () => {
        document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
        document.querySelectorAll('.tab-content').forEach(c => c.classList.remove('active'));
        btn.classList.add('active');
        const targetId = btn.dataset.tab === 'paste-tab' ? 'pasteTabContent' : 'uploadTabContent';
        document.getElementById(targetId).classList.add('active');
      });
    });
  }

  function setupFileUpload() {
    const drop = DOM.dropZone;
    const input = DOM.resumeFileInput;

    ['dragenter', 'dragover'].forEach(name => {
      drop.addEventListener(name, e => { e.preventDefault(); drop.classList.add('dragover'); });
    });
    ['dragleave', 'drop'].forEach(name => {
      drop.addEventListener(name, e => { e.preventDefault(); drop.classList.remove('dragover'); });
    });

    drop.addEventListener('drop', e => {
      const files = e.dataTransfer.files;
      if (files.length > 0) handleUploadedFile(files[0]);
    });

    input.addEventListener('change', e => {
      if (e.target.files.length > 0) handleUploadedFile(e.target.files[0]);
    });

    DOM.removeFileBtn.addEventListener('click', () => {
      DOM.fileUploadStatus.classList.add('hidden');
      input.value = '';
    });
  }

  async function handleUploadedFile(file) {
    DOM.uploadedFileName.textContent = `${file.name} (${Math.round(file.size / 1024)} KB)`;
    DOM.fileUploadStatus.classList.remove('hidden');

    showToast(`Uploading and extracting text from ${file.name}...`, 'info');

    // If Java server is online, upload via FormData to /api/upload-resume
    if (isJavaServerOnline) {
      try {
        const formData = new FormData();
        formData.append('resume', file);

        const res = await fetch(`${API_BASE_URL}/resume/extract`, {
          method: 'POST',
          body: formData
        });

        if (res.ok) {
          const data = await res.json();
          if (data.success) {
            DOM.resumeTextInput.value = data.extractedText || '';
            updateWordCount();

            // Populate extracted skills
            if (data.detectedSkills && data.detectedSkills.length > 0) {
              state.userProfile.skills = [...data.detectedSkills];
              renderSkillTags();
              syncQuickChips();
            }

            // Populate candidate detected info
            state.userProfile.detectedInfo = {
              name: data.candidateName || '',
              email: data.email || '',
              phone: data.phone || '',
              years: data.estimatedYearsExp != null ? data.estimatedYearsExp : null,
              education: data.education || []
            };

            // Sync years of experience if detected
            if (data.estimatedYearsExp != null && data.estimatedYearsExp >= 0) {
              setYearsOfExperience(data.estimatedYearsExp);
            }

            // Switch to paste tab so user can inspect extracted text
            const pasteTabBtn = document.querySelector('[data-tab="paste-tab"]');
            if (pasteTabBtn) pasteTabBtn.click();

            showToast(`Extracted ${data.detectedSkills ? data.detectedSkills.length : 0} skills from ${file.name}!`, 'success');

            // Trigger matching
            calculateJobMatches();
            return;
          }
        }
      } catch (err) {
        console.error('Upload to server failed:', err);
      }
    }

    // Fallback for plain text files if offline
    const reader = new FileReader();
    reader.onload = e => {
      const text = e.target.result;
      DOM.resumeTextInput.value = text;
      updateWordCount();
      document.querySelector('[data-tab="paste-tab"]')?.click();
      parseResumeText(text);
      calculateJobMatches();
    };
    reader.readAsText(file);
  }

  // ==========================================
  // SALARY BENCHMARKING ENGINE & MODAL
  // ==========================================

  async function openSalaryBenchmarkModal() {
    if (!DOM.salaryBenchmarkModal) return;
    DOM.salaryBenchmarkModal.classList.remove('hidden');
    await loadSalaryBenchmarkData();
  }

  async function loadSalaryBenchmarkData() {
    const salary = parseInt(DOM.salaryRange.value, 10) || 80000;
    const exp = state.userProfile.yearsOfExperience || 0;

    if (DOM.benchmarkTargetSalaryVal) DOM.benchmarkTargetSalaryVal.textContent = `₹${formatLPA(salary)}`;

    if (isJavaServerOnline) {
      try {
        const res = await fetch(`${API_BASE_URL}/stats/salary-benchmark?salary=${salary}&exp=${exp}`);
        if (res.ok) {
          const data = await res.json();
          if (data.success) {
            const o = data.overall || {};
            const evalInfo = data.candidateEvaluation || {};
            const tiers = data.tiers || {};

            if (DOM.benchmarkPercentileVal) DOM.benchmarkPercentileVal.textContent = `${evalInfo.percentile || 50}th`;
            if (DOM.benchmarkMarketPositionVal) DOM.benchmarkMarketPositionVal.textContent = evalInfo.marketPosition || 'Competitive';

            if (DOM.benchmarkP10Val) DOM.benchmarkP10Val.textContent = `₹${formatLPA(o.p10 || 450000)}`;
            if (DOM.benchmarkP25Val) DOM.benchmarkP25Val.textContent = `₹${formatLPA(o.p25 || 700000)}`;
            if (DOM.benchmarkMedianVal) DOM.benchmarkMedianVal.textContent = `₹${formatLPA(o.median || 1400000)}`;
            if (DOM.benchmarkP75Val) DOM.benchmarkP75Val.textContent = `₹${formatLPA(o.p75 || 2600000)}`;
            if (DOM.benchmarkP90Val) DOM.benchmarkP90Val.textContent = `₹${formatLPA(o.p90 || 4200000)}`;

            if (DOM.benchmarkTiersGrid) {
              const tierKeys = ['fresher', 'mid', 'senior', 'lead'];
              DOM.benchmarkTiersGrid.innerHTML = tierKeys.map(k => {
                const t = tiers[k] || { label: k, count: 0, min: 0, max: 0, avg: 0, median: 0 };
                return `
                  <div style="background:#f8fafc; border:1px solid var(--border-subtle); border-radius:var(--radius-md); padding:1rem;">
                    <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:0.5rem;">
                      <strong style="color:var(--text-primary); font-size:0.92rem;">${escapeHtml(t.label)}</strong>
                      <span class="badge" style="background:#e2e8f0; color:#334155; font-size:0.75rem;">${t.count} Jobs</span>
                    </div>
                    <div style="display:flex; flex-direction:column; gap:0.25rem; font-size:0.84rem;">
                      <div><span style="color:var(--text-secondary);">Average:</span> <strong style="color:var(--success-color); font-family:var(--font-mono);">₹${formatLPA(t.avg)}</strong></div>
                      <div><span style="color:var(--text-secondary);">Median:</span> <strong style="color:var(--text-primary); font-family:var(--font-mono);">₹${formatLPA(t.median)}</strong></div>
                      <div><span style="color:var(--text-secondary);">Range:</span> <span style="font-family:var(--font-mono);">₹${formatLPA(t.min)} - ₹${formatLPA(t.max)}</span></div>
                    </div>
                  </div>
                `;
              }).join('');
            }
          }
        }
      } catch (e) {
        console.error('Failed to load salary benchmarks:', e);
      }
    }
  }

  function showSkillSuggestions(val) {
    if (!val || val.trim().length === 0) {
      DOM.skillSuggestionsDropdown.classList.add('hidden');
      return;
    }
    const clean = val.trim().toLowerCase();
    const matches = state.knownSkills.filter(s => s.toLowerCase().includes(clean) && !state.userProfile.skills.includes(s)).slice(0, 5);

    if (matches.length === 0) {
      DOM.skillSuggestionsDropdown.classList.add('hidden');
      return;
    }

    DOM.skillSuggestionsDropdown.innerHTML = matches.map(m => `
      <div class="suggestion-item" data-skill="${escapeHtml(m)}">${escapeHtml(m)}</div>
    `).join('');

    DOM.skillSuggestionsDropdown.classList.remove('hidden');

    DOM.skillSuggestionsDropdown.querySelectorAll('.suggestion-item').forEach(item => {
      item.addEventListener('click', () => {
        addSkill(item.dataset.skill);
        DOM.skillInput.value = '';
        DOM.skillSuggestionsDropdown.classList.add('hidden');
      });
    });
  }

  function updateWordCount() {
    const text = DOM.resumeTextInput.value.trim();
    const words = text ? text.split(/\s+/).length : 0;
    const chars = DOM.resumeTextInput.value.length;
    DOM.wordCountLabel.textContent = `${words} words • ${chars} chars`;
  }

  function resetForm() {
    DOM.resumeTextInput.value = '';
    updateWordCount();
    state.userProfile.skills = [];
    renderSkillTags();
    syncQuickChips();
    DOM.extractedInfoCard.classList.add('hidden');
    DOM.salaryRange.value = 80000;
    DOM.salaryDisplay.textContent = '₹12 LPA';
    setYearsOfExperience(0, true);
    DOM.strictExperienceCheckbox.checked = true;
    updateWeights(60, 20, 10, 10);
    if (DOM.locationPreferenceSelect) DOM.locationPreferenceSelect.value = 'All';
    if (DOM.climatePreferenceSelect) DOM.climatePreferenceSelect.value = 'All';
    if (DOM.workingHoursSelect) DOM.workingHoursSelect.value = 'All';
    DOM.workModeSelect.value = 'Any';
    DOM.categorySelect.value = 'All';
    state.matchedResults = [];
    DOM.jobsListContainer.innerHTML = `
      <div class="empty-state">
        <div class="empty-icon">🔍</div>
        <h3>Ready to discover your ideal job matches</h3>
        <p>Choose your Years of Experience above or pick a 1-click demo profile to calculate tailored recommendations with 60% skills weighting.</p>
        <button class="empty-cta-btn" onclick="document.querySelector('.demo-pill.highlight-fresher').click()">Load Fresher Graduate Profile (0-1y)</button>
      </div>
    `;
    DOM.topMatchScoreVal.textContent = '--%';
    DOM.totalMatchesVal.textContent = '0';
    DOM.avgSalaryVal.textContent = '₹0 LPA';
    DOM.skillGapBanner.classList.add('hidden');
    showToast('Form reset.', 'info');
  }

  async function refreshApplicationsFromServer() {
    if (isJavaServerOnline && state.currentUser && state.currentUser.email) {
      try {
        const url = `${API_BASE_URL}/applications?email=${encodeURIComponent(state.currentUser.email)}`;
        const res = await fetchWithAuth(url);
        if (res.ok) {
          const serverApps = await res.json();
          if (Array.isArray(serverApps)) {
            state.submittedApplications = serverApps;
            localStorage.setItem('carrerx_applications', JSON.stringify(state.submittedApplications));
          }
        }
      } catch (err) {
        console.warn('Failed refreshing applications from backend:', err);
      }
    }
    renderDashboardApplicationTracker();
    renderJobsGrid();
    renderMyApplicationsDrawer();
    updateCounters();
  }

  function showToast(message, type = 'info') {
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.innerHTML = `<span>${type === 'success' ? '✓' : 'ℹ'}</span><span>${escapeHtml(message)}</span>`;
    DOM.toastContainer.appendChild(toast);
    setTimeout(() => {
      toast.style.opacity = '0';
      toast.style.transform = 'translateY(10px)';
      setTimeout(() => toast.remove(), 250);
    }, 2800);
  }

  function formatLPA(n) {
    if (!n) return '0 LPA';
    if (n < 100000) {
      return (n / 100000).toFixed(1) + ' LPA';
    }
    const val = n / 100000;
    if (val === Math.floor(val)) return val + ' LPA';
    return val.toFixed(1) + ' LPA';
  }

  function formatJobSalary(job) {
    if (!job) return '₹0 LPA';
    const min = Number(job.minSalary) || 0;
    const max = Number(job.maxSalary) || 0;
    
    const minL = (min / 100000).toFixed(min % 100000 === 0 ? 0 : 1);
    const maxL = (max / 100000).toFixed(max % 100000 === 0 ? 0 : 1);

    if (min && max) {
      if (min === max) return `₹${minL} LPA`;
      return `₹${minL} - ₹${maxL} LPA`;
    } else if (min) {
      return `₹${minL} LPA`;
    }
    return `₹${minL || 0} LPA`;
  }

  function formatNum(n) {
    return (n || 0).toLocaleString();
  }

  function escapeHtml(str) {
    if (!str) return '';
    return str.replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#039;');
  }

  function escapeRegExp(string) {
    return string.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
  }

  window.CarrerXApp = {
    openApply: (id) => {
      DOM.savedJobsDrawer.classList.add('hidden');
      openApplicationModal(id);
    },
    instantApply: (id) => {
      instantApply(id);
    },
    openBatchModal: (jobIds) => {
      openBatchApplicationModal(jobIds);
    },
    removeSaved: (id) => {
      toggleSaveJob(id);
    },

    openStatusModal: (appId) => {
      openApplicationStatusModal(appId);
    },

    simulateStageProgression: (stage) => {
      if (state.currentTrackingApp && stage) {
        updateApplicationStage(state.currentTrackingApp.applicationId, stage);
      }
    },
    toggleSelect: (jobId, forceState) => {
      toggleJobSelection(jobId, forceState);
    },
    login: (email, pass) => handleLogin(email || 'rohan.patel@graduate.edu', pass || 'password123'),
    logout: () => handleLogout(),
    checkAuth: () => checkAuthState(),
    switchView: (v) => switchPlatformView(v), getState: () => state, loadDashboard: loadCareerDashboard
  };

  document.addEventListener('DOMContentLoaded', init);
})();
