/* ═══════════════════════════════════════════════════════════
   EduPath Junior Portal — app.js
   ═══════════════════════════════════════════════════════════
   Responsibilities:
     • Sidebar navigation (SPA-style view switching)
     • Rendering Senior Posts, Raise-a-Question, and My Profile
     • Toast notifications
     • Mobile sidebar toggle
   ═══════════════════════════════════════════════════════════ */

// ───────────────────────────────────────────
// 1. MOCK DATA
//    Keep all sample data in one place so it
//    can be swapped with real API responses.
// ───────────────────────────────────────────

/**
 * Placeholder junior profile.
 * Replace this object with data from the backend after profile integration.
 */
const JUNIOR_PROFILE = {
  name: "Arun Kumar",
  initials: "AK",
  regNumber: "21BCE7291",
  role: "Junior",
  department: "Computer Science & Engineering",
  year: "3rd Year",
};

/**
 * Sample senior experience posts.
 * Each post supports optional fields — the renderer gracefully
 * skips any field that is absent or empty.
 */
const MOCK_POSTS = [
  {
    id: 1,
    seniorName: "Priya Sharma",
    year: "2024 Batch",
    company: "Google",
    jobTitle: "SDE-1",
    title: "How I Prepared for Google — From Zero DSA to an Offer",
    summary:
      "I started college with almost no coding background. By the end of final year I had an offer from Google. Here's the year-wise approach that worked for me, including the resources, mistakes, and mindset shifts that made the difference.",
    tags: ["Academics", "Placement"],
    journey: [
      { year: "1st Year", desc: "Built a strong foundation in C and Python. Focused on understanding logic rather than memorising syntax." },
      { year: "2nd Year", desc: "Completed 300+ LeetCode problems. Studied OS, DBMS, and CN thoroughly for interview fundamentals." },
      { year: "3rd Year", desc: "Did a summer internship at a mid-size startup. Practised system design basics and mock interviews." },
      { year: "4th Year", desc: "Applied to Google through campus placement. Cleared 3 coding rounds and 1 system-design round." },
    ],
  },
  {
    id: 2,
    seniorName: "Rohan Mehta",
    year: "2023 Batch",
    company: "Amazon",
    jobTitle: "SDE Intern → Full-time",
    title: "My Internship-to-PPO Journey at Amazon",
    summary:
      "I converted my summer internship at Amazon into a full-time pre-placement offer. This post covers what I did during the internship and how I balanced academic coursework with placement preparation.",
    tags: ["Internship", "Placement"],
    journey: [
      { year: "1st Year", desc: "Explored web development and built small projects to understand full-stack basics." },
      { year: "2nd Year", desc: "Focused on data structures and started competitive programming on Codeforces." },
      { year: "3rd Year", desc: "Secured an Amazon internship. Worked on an internal tool used by the operations team." },
      { year: "4th Year", desc: "Received a PPO and continued refining system-design skills before joining." },
    ],
  },
  {
    id: 3,
    seniorName: "Anjali Verma",
    year: "2024 Batch",
    company: "",
    jobTitle: "",
    title: "Balancing CGPA, Clubs, and Placement Prep — What Actually Matters",
    summary:
      "Not everyone gets placed at a product giant, and that's okay. I want to share how I managed a strong CGPA while staying active in college communities, and what I learned about realistic career planning.",
    tags: ["Academics"],
    journey: [
      { year: "1st Year", desc: "Joined the coding club and the college magazine. Kept grades above 8.5 CGPA." },
      { year: "2nd Year", desc: "Took on a leadership role in the tech club. Started learning DSA through peer study groups." },
      { year: "3rd Year", desc: "Attended multiple hackathons. Realised that consistent fundamentals matter more than cramming." },
    ],
  },
  {
    id: 4,
    seniorName: "Karthik Iyer",
    year: "2023 Batch",
    company: "Microsoft",
    jobTitle: "Software Engineer",
    title: "Cracking Microsoft — Lessons from Two Failed Attempts",
    summary:
      "I failed the Microsoft interview twice before finally making it through in my final year. Here's an honest account of what went wrong, what I changed, and what advice I'd give to juniors preparing right now.",
    tags: ["Placement", "Highlight"],
    journey: [
      { year: "2nd Year", desc: "First attempt — struggled with graph problems and got eliminated in round 2." },
      { year: "3rd Year", desc: "Second attempt during internship drive — made it to the final round but didn't convert." },
      { year: "4th Year", desc: "Strengthened weak areas (DP and graphs), practised under timed conditions, and cleared all rounds." },
    ],
  },
];

// ───────────────────────────────────────────
// 2. DOM REFERENCES
// ───────────────────────────────────────────

const mainContent = document.getElementById("main-content");
const toastContainer = document.getElementById("toast-container");
const sidebarButtons = document.querySelectorAll(".sidebar__item");
const sidebar = document.querySelector(".sidebar");

// ───────────────────────────────────────────
// 3. TOAST UTILITY
// ───────────────────────────────────────────

/**
 * Show a small notification toast at the bottom-right corner.
 * It auto-dismisses after ~3.3 s (CSS animation handles fade-out).
 * @param {string} message — plain text message
 */
function showToast(message) {
  const el = document.createElement("div");
  el.className = "toast";
  el.setAttribute("role", "status");
  el.textContent = message;
  toastContainer.appendChild(el);
  // Remove from DOM after the CSS animation ends
  setTimeout(function () {
    if (el.parentNode) el.parentNode.removeChild(el);
  }, 3500);
}

// ───────────────────────────────────────────
// 4. SAFE TEXT HELPER
//    All mock-data text is treated as data,
//    never injected through innerHTML.
// ───────────────────────────────────────────

/**
 * Create a DOM element with optional className and text content.
 * @param {string}  tag       — HTML tag name
 * @param {string}  className — CSS class(es)
 * @param {string}  [text]    — safe text content
 * @returns {HTMLElement}
 */
function createElement(tag, className, text) {
  var el = document.createElement(tag);
  if (className) el.className = className;
  if (text !== undefined && text !== null) el.textContent = text;
  return el;
}

// ───────────────────────────────────────────
// 5. TAG MAPPING
// ───────────────────────────────────────────

function tagClass(label) {
  var map = {
    Academics: "tag--acad",
    Placement: "tag--intern",
    Internship: "tag--intern",
    Highlight: "tag--high",
  };
  return map[label] || "tag--acad";
}

// ───────────────────────────────────────────
// 6. VIEW RENDERERS
// ───────────────────────────────────────────

/** Build and return the Senior Posts view as a DocumentFragment. */
function renderSeniorPosts() {
  var frag = document.createDocumentFragment();

  // Wrapper to constrain feed width (max-width lives in CSS)
  var feed = createElement("div", "senior-posts-feed");

  feed.appendChild(
    createElement("h2", "view-heading", "Senior Experiences & Guidance")
  );
  feed.appendChild(
    createElement(
      "p",
      "view-subtitle",
      "Learn from seniors and alumni about their academic journey, placement preparation, interviews, and career decisions."
    )
  );

  MOCK_POSTS.forEach(function (post) {
    var card = createElement("article", "post-card");

    // ── Header: avatar + meta ──
    var header = createElement("div", "post-card__header");
    var initials = post.seniorName
      .split(" ")
      .map(function (w) { return w[0]; })
      .join("");
    header.appendChild(createElement("span", "avatar avatar--md", initials));

    var meta = createElement("div", "post-card__meta");
    meta.appendChild(createElement("span", "post-card__name", post.seniorName));

    var roleParts = [];
    if (post.year) roleParts.push(post.year);
    if (post.company && post.jobTitle) {
      roleParts.push(post.company + " · " + post.jobTitle);
    } else if (post.company) {
      roleParts.push(post.company);
    } else if (post.jobTitle) {
      roleParts.push(post.jobTitle);
    }
    if (roleParts.length) {
      meta.appendChild(
        createElement("span", "post-card__role", roleParts.join(" — "))
      );
    }
    header.appendChild(meta);
    card.appendChild(header);

    // ── Tags ──
    if (post.tags && post.tags.length) {
      var tagsWrap = createElement("div", "post-card__tags");
      post.tags.forEach(function (t) {
        tagsWrap.appendChild(createElement("span", "tag " + tagClass(t), t));
      });
      card.appendChild(tagsWrap);
    }

    // ── Title & summary ──
    card.appendChild(createElement("h3", "post-card__title", post.title));
    if (post.summary) {
      card.appendChild(createElement("p", "post-card__summary", post.summary));
    }

    // ── Year-wise journey timeline ──
    if (post.journey && post.journey.length) {
      card.appendChild(
        createElement("div", "post-card__journey-heading", "Year-wise Learning Journey")
      );
      var timeline = createElement("div", "timeline");
      post.journey.forEach(function (step) {
        var stepEl = createElement("div", "timeline__step");
        stepEl.appendChild(createElement("span", "timeline__year", step.year));
        stepEl.appendChild(createElement("p", "timeline__desc", step.desc));
        timeline.appendChild(stepEl);
      });
      card.appendChild(timeline);
    }

    // ── Generate Roadmap button ──
    var btn = createElement("button", "btn-primary", "");
    // small sparkle icon
    var svg = document.createElementNS("http://www.w3.org/2000/svg", "svg");
    svg.setAttribute("width", "16");
    svg.setAttribute("height", "16");
    svg.setAttribute("viewBox", "0 0 24 24");
    svg.setAttribute("fill", "none");
    svg.setAttribute("stroke", "currentColor");
    svg.setAttribute("stroke-width", "2");
    svg.setAttribute("stroke-linecap", "round");
    svg.setAttribute("stroke-linejoin", "round");
    svg.setAttribute("aria-hidden", "true");
    var path1 = document.createElementNS("http://www.w3.org/2000/svg", "path");
    path1.setAttribute("d", "M12 2v4M12 18v4M4.93 4.93l2.83 2.83M16.24 16.24l2.83 2.83M2 12h4M18 12h4M4.93 19.07l2.83-2.83M16.24 7.76l2.83-2.83");
    svg.appendChild(path1);
    btn.appendChild(svg);
    btn.appendChild(document.createTextNode("Generate Roadmap"));
    btn.setAttribute("type", "button");
    btn.setAttribute("aria-label", "Generate personalised roadmap from " + post.seniorName + "'s experience");

    // Attach click handler (isolated for future backend integration)
    btn.addEventListener("click", function () {
      handleGenerateRoadmap(post.id);
    });

    card.appendChild(btn);
    feed.appendChild(card);
  });

  frag.appendChild(feed);
  return frag;
}

/** Build and return the Raise a Question placeholder view. */
function renderQuestionPlaceholder() {
  var frag = document.createDocumentFragment();
  var wrap = createElement("div", "placeholder-view");

  // Inner container keeps all children as one centered group
  var inner = createElement("div", "placeholder-view__inner");

  // Icon
  var iconWrap = createElement("div", "placeholder-view__icon");
  var svg = document.createElementNS("http://www.w3.org/2000/svg", "svg");
  svg.setAttribute("width", "48");
  svg.setAttribute("height", "48");
  svg.setAttribute("viewBox", "0 0 24 24");
  svg.setAttribute("fill", "none");
  svg.setAttribute("stroke", "currentColor");
  svg.setAttribute("stroke-width", "1.5");
  svg.setAttribute("stroke-linecap", "round");
  svg.setAttribute("stroke-linejoin", "round");
  svg.setAttribute("aria-hidden", "true");
  var c = document.createElementNS("http://www.w3.org/2000/svg", "circle");
  c.setAttribute("cx", "12");
  c.setAttribute("cy", "12");
  c.setAttribute("r", "10");
  var p1 = document.createElementNS("http://www.w3.org/2000/svg", "path");
  p1.setAttribute("d", "M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3");
  var p2 = document.createElementNS("http://www.w3.org/2000/svg", "line");
  p2.setAttribute("x1", "12");
  p2.setAttribute("y1", "17");
  p2.setAttribute("x2", "12.01");
  p2.setAttribute("y2", "17");
  svg.appendChild(c);
  svg.appendChild(p1);
  svg.appendChild(p2);
  iconWrap.appendChild(svg);
  inner.appendChild(iconWrap);

  inner.appendChild(createElement("h2", "placeholder-view__heading", "Raise a Question"));
  inner.appendChild(
    createElement(
      "p",
      "placeholder-view__text",
      "Have a question about academics, placements, or college life? This feature lets you ask seniors and alumni directly for guidance."
    )
  );
  inner.appendChild(
    createElement(
      "p",
      "placeholder-view__text",
      "The question-and-answer module is being built by a teammate and will be connected here once the integration is ready."
    )
  );
  wrap.appendChild(inner);
  frag.appendChild(wrap);
  return frag;
}

/** Build and return the My Profile view. */
function renderProfile() {
  var frag = document.createDocumentFragment();

  // Header: heading + subtitle sit at top-left of the content area
  var header = createElement("div", "profile-view__header");
  header.appendChild(createElement("h2", "view-heading", "My Profile"));
  header.appendChild(
    createElement("p", "view-subtitle", "View and manage your student profile.")
  );
  frag.appendChild(header);

  // Body: flex container that centers the card horizontally
  var body = createElement("div", "profile-view__body");

  var card = createElement("div", "profile-card");
  card.appendChild(
    createElement("span", "avatar avatar--lg", JUNIOR_PROFILE.initials)
  );
  card.appendChild(createElement("div", "profile-card__name", JUNIOR_PROFILE.name));
  card.appendChild(
    createElement("div", "profile-card__detail", JUNIOR_PROFILE.regNumber)
  );
  card.appendChild(
    createElement("div", "profile-card__detail", JUNIOR_PROFILE.department + " · " + JUNIOR_PROFILE.year)
  );
  card.appendChild(createElement("span", "profile-card__badge", JUNIOR_PROFILE.role));

  // Edit Profile button
  var btn = createElement("button", "btn-primary", "Edit Profile");
  btn.setAttribute("type", "button");
  btn.addEventListener("click", function () {
    showToast("Profile editing will be available after profile integration.");
  });
  card.appendChild(btn);

  body.appendChild(card);
  frag.appendChild(body);
  return frag;
}

// ───────────────────────────────────────────
// 7. VIEW SWITCHING
// ───────────────────────────────────────────

var currentView = "senior-posts";

/**
 * Replace the main content area with the requested view.
 * @param {"senior-posts"|"raise-question"|"my-profile"} viewName
 */
function showView(viewName) {
  // Guard: do nothing if already active
  if (viewName === currentView) return;
  currentView = viewName;

  // Clear previous content
  mainContent.textContent = "";

  // Render the chosen view
  switch (viewName) {
    case "senior-posts":
      mainContent.appendChild(renderSeniorPosts());
      break;
    case "raise-question":
      mainContent.appendChild(renderQuestionPlaceholder());
      break;
    case "my-profile":
      mainContent.appendChild(renderProfile());
      break;
    default:
      mainContent.appendChild(renderSeniorPosts());
  }

  // Update sidebar active state
  sidebarButtons.forEach(function (btn) {
    var isActive = btn.getAttribute("data-view") === viewName;
    btn.classList.toggle("sidebar__item--active", isActive);
    if (isActive) {
      btn.setAttribute("aria-current", "page");
    } else {
      btn.removeAttribute("aria-current");
    }
  });

  // Close sidebar on mobile after navigation
  closeMobileSidebar();
}

// ───────────────────────────────────────────
// 8. GENERATE ROADMAP HANDLER
// ───────────────────────────────────────────

/**
 * Handle Generate Roadmap button click.
 * Isolated so the responsible teammate can replace the body
 * with an actual API call to the Spring Boot backend.
 * @param {number} postId — the ID of the senior post
 */
      
async function handleGenerateRoadmap(postId) {
  const button = document.querySelector(
    ".post-card button[aria-label]"
  );

  let token = null;

  try {
    token = localStorage.getItem("jwt");
  } catch (_) {
    showToast("Unable to access your login session.");
    return;
  }

  if (!token) {
    showToast("Please log in first to generate your roadmap.");
    return;
  }

  let post = null;
  if (typeof postId === "object" && postId !== null) {
    post = postId;
  } else if (postId) {
    post = (window.allLoadedPosts || []).find(item => item.id === postId)
        || MOCK_POSTS.find(item => item.id === postId);
  }

  let studentUser = null;
  try {
    const raw = localStorage.getItem("user");
    if (raw) studentUser = JSON.parse(raw);
  } catch (_) {}

  const sem = (studentUser && studentUser.semester) ? Number(studentUser.semester) : 3;
  const cgpa = (studentUser && studentUser.cgpa) ? Number(studentUser.cgpa) : 8.8;

  const goal = post
    ? "Prepare for career based on senior playbook & achievements: " + post.title + (post.summary ? " — " + post.summary : "")
    : "Comprehensive full-stack and algorithmic preparation for top product company recruitment";

  const originalText = button ? button.textContent : "";

  if (button) {
    button.disabled = true;
    button.textContent = "Generating roadmap...";
  }

  showToast("Generating your personalised roadmap...");

  try {
    const response = await fetch(
      "http://localhost:8080/api/v1/ai-assistant/generate-path",
      {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "Authorization": "Bearer " + token
        },
        body: JSON.stringify({
          goal: goal,
          currentSemester: 3,
          currentCgpa: 9.13
        })
      }
    );

    const result = await response.json();

    if (!response.ok || !result.success) {
      if (response.status === 401) {
        throw new Error("Your session has expired. Please log in again.");
      }

      if (response.status === 403) {
        throw new Error("You don't have permission to generate this roadmap.");
      }

      throw new Error(
        result.message || "Roadmap generation failed. Please try again."
      );
    }

    renderGeneratedRoadmap(result.data);

  } catch (error) {
    console.error("Roadmap generation error:", error);

    showToast(
      error.message.includes("Failed to fetch")
        ? "Cannot connect to the backend. Check that Spring Boot is running."
        : error.message || "Something went wrong while generating the roadmap."
    );

  } finally {
    if (button) {
      button.disabled = false;
      button.textContent = originalText;
    }
  }
}

function renderGeneratedRoadmap(data) {
  mainContent.textContent = "";

  const wrap = createElement("div", "senior-posts-feed");

  const backButton = createElement(
    "button",
    "btn-primary",
    "← Back to Senior Experiences"
  );

  backButton.type = "button";
  backButton.addEventListener("click", function () {
    showView("my-profile");
    showView("senior-posts");
  });

  wrap.appendChild(backButton);

  wrap.appendChild(
    createElement("h2", "view-heading", "Your Personalised Roadmap")
  );

  wrap.appendChild(
    createElement("p", "view-subtitle", "Goal: " + data.goal)
  );

  wrap.appendChild(
    createElement("h3", "post-card__journey-heading", "Semester-wise Milestones")
  );

  const timeline = createElement("div", "timeline");

  (data.adaptiveMilestones || []).forEach(function (milestone) {
    const step = createElement("div", "timeline__step");
    step.appendChild(createElement("p", "timeline__desc", milestone));
    timeline.appendChild(step);
  });

  wrap.appendChild(timeline);

  wrap.appendChild(
    createElement("h3", "post-card__journey-heading", "Recommended Skills")
  );

  const skills = createElement("div", "post-card__tags");

  (data.recommendedRoadmapSkills || []).forEach(function (skill) {
    skills.appendChild(createElement("span", "tag tag--acad", skill));
  });

  wrap.appendChild(skills);

  const adviceCard = createElement("article", "post-card");

  adviceCard.appendChild(
    createElement("h3", "post-card__title", "Senior Mentor Advice")
  );

  adviceCard.appendChild(
    createElement("p", "post-card__summary", data.seniorMentorAdvice || "No additional advice available.")
  );

  wrap.appendChild(adviceCard);

  if (data.generatedAt) {
    wrap.appendChild(
      createElement(
        "p",
        "view-subtitle",
        "Generated at: " + new Date(data.generatedAt).toLocaleString()
      )
    );
  }

  mainContent.appendChild(wrap);
}


// ───────────────────────────────────────────
// 9. MOBILE SIDEBAR TOGGLE
// ───────────────────────────────────────────

function injectMobileSidebarControls() {
  // Hamburger button inside header (before profile chip)
  var toggleBtn = createElement("button", "sidebar-toggle");
  toggleBtn.setAttribute("type", "button");
  toggleBtn.setAttribute("aria-label", "Open navigation menu");
  // hamburger icon (three lines)
  var svg = document.createElementNS("http://www.w3.org/2000/svg", "svg");
  svg.setAttribute("width", "22");
  svg.setAttribute("height", "22");
  svg.setAttribute("viewBox", "0 0 24 24");
  svg.setAttribute("fill", "none");
  svg.setAttribute("stroke", "currentColor");
  svg.setAttribute("stroke-width", "2");
  svg.setAttribute("stroke-linecap", "round");
  svg.setAttribute("stroke-linejoin", "round");
  svg.setAttribute("aria-hidden", "true");
  ["4", "12", "20"].forEach(function (y) {
    var line = document.createElementNS("http://www.w3.org/2000/svg", "line");
    line.setAttribute("x1", "3");
    line.setAttribute("y1", y);
    line.setAttribute("x2", "21");
    line.setAttribute("y2", y);
    svg.appendChild(line);
  });
  toggleBtn.appendChild(svg);

  // Insert toggle button as first child of header brand area
  var brand = document.querySelector(".header__brand");
  brand.insertBefore(toggleBtn, brand.firstChild);

  // Overlay
  var overlay = createElement("div", "sidebar-overlay");
  overlay.id = "sidebar-overlay";
  document.body.appendChild(overlay);

  // Events
  toggleBtn.addEventListener("click", function () {
    var isOpen = sidebar.classList.toggle("sidebar--open");
    overlay.classList.toggle("sidebar-overlay--visible", isOpen);
    toggleBtn.setAttribute("aria-label", isOpen ? "Close navigation menu" : "Open navigation menu");
  });

  overlay.addEventListener("click", closeMobileSidebar);
}

function closeMobileSidebar() {
  sidebar.classList.remove("sidebar--open");
  var overlay = document.getElementById("sidebar-overlay");
  if (overlay) overlay.classList.remove("sidebar-overlay--visible");
}

// ───────────────────────────────────────────
// 10. INITIALISE
// ───────────────────────────────────────────

(function init() {
  // Render default view immediately
  mainContent.appendChild(renderSeniorPosts());

  // Sidebar navigation
  sidebarButtons.forEach(function (btn) {
    btn.addEventListener("click", function () {
      var view = btn.getAttribute("data-view");
      showView(view);
    });
  });

  // Header profile chip → open My Profile view
  var headerProfileBtn = document.getElementById("header-profile-btn");
  if (headerProfileBtn) {
    headerProfileBtn.addEventListener("click", function () {
      showView("my-profile");
    });
  }

  // Mobile sidebar controls
  injectMobileSidebarControls();
})();
