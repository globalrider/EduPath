/* ═══════════════════════════════════════════════════════════
   EduPath Junior Portal — app.js (Integrated)
   ═══════════════════════════════════════════════════════════ */

// ───────────────────────────────────────────
// 1. PROFILE & DYNAMIC STUDENT STATE
// ───────────────────────────────────────────

let currentJuniorUser = null;
try {
  const u = localStorage.getItem("user");
  if (u) currentJuniorUser = JSON.parse(u);
} catch (_) {}

function getStudentAcademicProfile() {
  const u = currentJuniorUser || {};
  let custom = {};
  try {
    const key = "edupath_student_acad_" + (u.regNo || u.email || "default");
    const stored = localStorage.getItem(key);
    if (stored) custom = JSON.parse(stored);
  } catch (_) {}

  // Infer semester from regNo if available (e.g. 24 -> Sem 2, 23 -> Sem 4, 22 -> Sem 6)
  let inferredSem = 3;
  if (u.regNo && u.regNo.length >= 2) {
    const yrPrefix = u.regNo.substring(0, 2);
    if (yrPrefix === "24" || yrPrefix === "25") inferredSem = 2;
    else if (yrPrefix === "23") inferredSem = 4;
    else if (yrPrefix === "22") inferredSem = 6;
  }

  const name = u.name || "Junior Student";
  const initials = name.split(" ").filter(Boolean).map(w => w[0]).join("") || "JS";

  return {
    name: name,
    initials: initials,
    regNumber: u.regNo || "24CS101",
    role: u.role || "Junior Student",
    department: "Computer Science & Engineering",
    year: (inferredSem <= 2 ? "1st" : inferredSem <= 4 ? "2nd" : "3rd") + " Year",
    email: u.email || "student@edupath.edu",
    semester: Number(custom.semester || u.semester || inferredSem),
    cgpa: Number(custom.cgpa || u.cgpa || 8.6),
    careerGoal: custom.careerGoal || u.careerGoal || "Full-Stack Software Development Engineer"
  };
}

function saveStudentAcademicProfile(semester, cgpa, careerGoal) {
  const u = currentJuniorUser || {};
  const key = "edupath_student_acad_" + (u.regNo || u.email || "default");
  const data = {
    semester: Number(semester),
    cgpa: Number(cgpa),
    careerGoal: careerGoal
  };
  localStorage.setItem(key, JSON.stringify(data));
}

const JUNIOR_PROFILE = getStudentAcademicProfile();

window.signOut = function() {
  localStorage.removeItem("jwt");
  localStorage.removeItem("user");
  window.location.href = "/login.html";
};

// Update topbar profile elements if present
document.addEventListener("DOMContentLoaded", function () {
  const nameEl = document.getElementById("user-display-name");
  const initEl = document.getElementById("user-avatar-initials");
  const profile = getStudentAcademicProfile();
  if (nameEl) {
    nameEl.textContent = `${profile.name} (${profile.role}) · ${profile.regNumber}`;
  }
  if (initEl && profile.initials) {
    initEl.textContent = profile.initials;
  }
});


const MOCK_POSTS = [
  {
    id: 101,
    seniorName: "Aravind R",
    year: "2024 Batch",
    company: "Zoho",
    jobTitle: "SDE-1",
    title: "Semester 1 & 2 Blueprint: Computer Science Fundamentals",
    summary:
      "Don't ignore Engineering Mathematics: it directly impacts 3rd Sem algorithmic efficiency. Focus heavily on C basics, pointers, dynamic memory allocation (`malloc`/`free`), and memory layout. Start solving basic logic puzzles early to build strong fundamentals for Data Structures.",
    tags: ["Academics", "Placement"],
    journey: [
      { year: "1st Year", desc: "Mastered C language basics, pointers, and memory layout. Kept CGPA at 8.9." },
      { year: "2nd Year", desc: "Studied OOP concepts in Java and solved 200+ basic array and string problems." },
      { year: "3rd Year", desc: "Built full-stack Spring Boot web apps and prepared for campus placement rounds." },
      { year: "4th Year", desc: "Cracked Zoho placement drive with 3 coding rounds and technical interviews." }
    ]
  },
  {
    id: 102,
    seniorName: "Deepa M",
    year: "Alumni · 2022 Batch",
    company: "Amazon",
    jobTitle: "Senior SDE",
    title: "Mastering Placement Coding Rounds & System Design at Amazon",
    summary:
      "Amazon and top tech interviews focus on DSA rigor (Trees, Graphs, Sliding Window) and Clean Code. Emphasize modular object-oriented design and demonstrate solid database indexing and concurrency knowledge.",
    tags: ["Placement", "Highlight"],
    journey: [
      { year: "1st Year", desc: "Discovered competitive programming and learned discrete mathematics." },
      { year: "2nd Year", desc: "Solved 400+ LeetCode problems across Trees, Graphs, and Dynamic Programming." },
      { year: "3rd Year", desc: "Summer SDE intern at Amazon; worked on microservices and AWS infrastructure." },
      { year: "4th Year", desc: "Converted internship into full-time offer; now mentoring juniors at EduPath." }
    ]
  },
  {
    id: 103,
    seniorName: "Karthik S",
    year: "2023 Batch",
    company: "Microsoft",
    jobTitle: "Software Engineer",
    title: "Off-Campus Internship Strategy & Resume Building That Gets Shortlisted",
    summary:
      "Build 2 deep full-stack projects rather than 10 tutorial clones. Ensure your resume has quantifiable impact (e.g. 'Optimized database queries decreasing latency by 45%'). Start cold reaching out on LinkedIn early.",
    tags: ["Internships", "Academics"],
    journey: [
      { year: "1st Year", desc: "Explored web technologies and built portfolio projects." },
      { year: "2nd Year", desc: "Focused on core DBMS, SQL optimization, and computer networks." },
      { year: "3rd Year", desc: "Applied off-campus to 80+ companies; cleared Microsoft interview." }
    ]
  }
];

// In-memory cache of all loaded posts (both pre-seeded and dynamically published)
let allLoadedPosts = [...MOCK_POSTS];

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

function showToast(message) {
  const el = document.createElement("div");
  el.className = "toast";
  el.setAttribute("role", "status");
  el.textContent = message;
  toastContainer.appendChild(el);
  setTimeout(function () {
    if (el.parentNode) el.parentNode.removeChild(el);
  }, 3500);
}

// ───────────────────────────────────────────
// 4. SAFE DOM HELPER
// ───────────────────────────────────────────

function createElement(tag, className, text) {
  var el = document.createElement(tag);
  if (className) el.className = className;
  if (text !== undefined && text !== null) el.textContent = text;
  return el;
}

function tagClass(label) {
  var map = {
    Academics: "tag--acad",
    Placement: "tag--intern",
    Internships: "tag--intern",
    Internship: "tag--intern",
    Highlight: "tag--high",
    "Higher Studies": "tag--high",
    "Senior Achievement": "tag--high"
  };
  return map[label] || "tag--acad";
}

// ───────────────────────────────────────────
// 5. VIEW RENDERERS
// ───────────────────────────────────────────

/** Build Senior Posts Feed (combining backend + mock posts) */
function renderSeniorPosts(activeCategory) {
  var frag = document.createDocumentFragment();
  var feed = createElement("div", "senior-posts-feed");

  // Header & introduction
  feed.appendChild(createElement("h2", "view-heading", "Senior Experiences & Guidance Feed"));
  feed.appendChild(
    createElement(
      "p",
      "view-subtitle",
      "Learn directly from verified seniors and alumni about semester preparation, CGPA strategy, coding interviews, and verified career playbooks."
    )
  );

  // Filter pills
  var filterWrap = createElement("div", "filter-bar", "");
  filterWrap.style.cssText = "display:flex;gap:8px;margin-bottom:1.5rem;flex-wrap:wrap;";
  var categories = ["All", "Academics", "Internships", "Placement", "Higher Studies"];
  categories.forEach(function (cat) {
    var btn = createElement("button", "btn-filter", cat);
    btn.style.cssText = "padding:0.4rem 0.9rem;border-radius:999px;font-size:0.82rem;font-weight:600;border:1px solid var(--border);background:" +
      (((!activeCategory && cat === "All") || activeCategory === cat) ? "var(--primary);color:#fff;" : "var(--surface);color:var(--text);");
    btn.addEventListener("click", function () {
      mainContent.textContent = "";
      mainContent.appendChild(renderSeniorPosts(cat === "All" ? null : cat));
    });
    filterWrap.appendChild(btn);
  });
  feed.appendChild(filterWrap);

  var postsContainer = createElement("div", "posts-list");
  feed.appendChild(postsContainer);

  // Render mock and live posts
  renderPostsList(postsContainer, activeCategory);

  frag.appendChild(feed);
  return frag;
}

async function renderPostsList(container, categoryFilter) {
  let combinedPosts = [...MOCK_POSTS];

  try {
    const res = await fetch("/api/v1/playbooks");
    if (res.ok) {
      const data = await res.json();
      if (data.data && Array.isArray(data.data)) {
        data.data.forEach(p => {
          let company = p.category || "Placement";
          if (p.title && p.title.includes("—")) {
            const parts = p.title.split("—");
            if (parts.length > 1) company = parts[1].trim();
          }

          combinedPosts.unshift({
            id: p.postId,
            seniorName: p.authorName || "Senior Mentor",
            year: p.authorRole || "SENIOR",
            company: company,
            jobTitle: "Verified Senior Guide",
            title: p.title,
            summary: p.markdownContent ? p.markdownContent.slice(0, 350) + (p.markdownContent.length > 350 ? "..." : "") : "",
            fullContent: p.markdownContent || "",
            tags: [p.category || "Academics", "Senior Achievement"],
            journey: [
              { year: "Achievement & Guide", desc: p.markdownContent ? p.markdownContent.slice(0, 220) + (p.markdownContent.length > 220 ? "..." : "") : "Follow structured milestones." }
            ]
          });
        });
      }
    }
  } catch (_) {}

  // Update global registry so all newly loaded posts can generate roadmaps
  allLoadedPosts = combinedPosts;

  // Filter if needed
  if (categoryFilter) {
    combinedPosts = combinedPosts.filter(p =>
      p.tags && p.tags.some(t => t.toLowerCase() === categoryFilter.toLowerCase())
    );
  }

  container.textContent = "";

  if (combinedPosts.length === 0) {
    container.innerHTML = `<div style="text-align:center;padding:3rem 1rem;color:var(--muted)">
      <p style="font-size:1.1rem;font-weight:600">No guidance playbooks found for this topic.</p>
    </div>`;
    return;
  }

  combinedPosts.forEach(function (post) {
    var card = createElement("article", "post-card");

    // Header
    var header = createElement("div", "post-card__header");
    var initials = (post.seniorName || "SM")
      .split(" ")
      .filter(Boolean)
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
    }
    if (roleParts.length) {
      meta.appendChild(createElement("span", "post-card__role", roleParts.join(" — ")));
    }
    header.appendChild(meta);
    card.appendChild(header);

    // Tags
    if (post.tags && post.tags.length) {
      var tagsWrap = createElement("div", "post-card__tags");
      post.tags.forEach(function (t) {
        tagsWrap.appendChild(createElement("span", "tag " + tagClass(t), t));
      });
      card.appendChild(tagsWrap);
    }

    // Title & summary
    card.appendChild(createElement("h3", "post-card__title", post.title));
    if (post.summary) {
      card.appendChild(createElement("p", "post-card__summary", post.summary));
    }

    // Timeline steps
    if (post.journey && post.journey.length) {
      card.appendChild(createElement("div", "post-card__journey-heading", "Year-wise Learning Journey"));
      var timeline = createElement("div", "timeline");
      post.journey.forEach(function (step) {
        var stepEl = createElement("div", "timeline__step");
        stepEl.appendChild(createElement("span", "timeline__year", step.year));
        stepEl.appendChild(createElement("p", "timeline__desc", step.desc));
        timeline.appendChild(stepEl);
      });
      card.appendChild(timeline);
    }

    // Generate Roadmap button
    var btn = createElement("button", "btn-primary", "");
    btn.innerHTML = `<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="margin-right:6px">
      <path d="M12 2v4M12 18v4M4.93 4.93l2.83 2.83M16.24 16.24l2.83 2.83M2 12h4M18 12h4M4.93 19.07l2.83-2.83M16.24 7.76l2.83-2.83"/>
    </svg> Generate Personalized Roadmap`;
    btn.setAttribute("type", "button");
    btn.addEventListener("click", function () {
      // Pass the actual post object so newly created achievements work seamlessly
      handleGenerateRoadmap(post, btn);
    });

    card.appendChild(btn);
    container.appendChild(card);
  });
}

/** Build Raise a Question view (Integrated with QA Forum backend) */
function renderRaiseQuestionView() {
  var frag = document.createDocumentFragment();
  var wrap = createElement("div", "raise-question-container");
  wrap.style.cssText = "max-width: 900px; margin: 0 auto; display: grid; gap: 1.5rem;";

  var head = createElement("div", "view-header");
  head.appendChild(createElement("h2", "view-heading", "Raise a Question to Seniors & Alumni"));
  head.appendChild(
    createElement(
      "p",
      "view-subtitle",
      "Have a question about academics, placements, or college life? Ask our community of verified seniors and alumni."
    )
  );
  wrap.appendChild(head);

  var grid = createElement("div", "qa-layout-grid");
  grid.style.cssText = "display:grid;grid-template-columns: repeat(auto-fit, minmax(360px, 1fr));gap:1.5rem;";

  var formCard = createElement("div", "card");
  formCard.style.cssText = "background:var(--surface);border:1px solid var(--border);border-radius:10px;padding:1.5rem;";
  formCard.innerHTML = `
    <h3 style="font-size:1.15rem;margin-bottom:0.75rem;font-weight:700;">Ask Your Question</h3>
    <form id="junior-qa-form" style="display:grid;gap:1rem;">
      <div>
        <label style="display:block;font-size:0.85rem;font-weight:600;margin-bottom:0.35rem;">Question Title *</label>
        <input type="text" id="jq-title" required placeholder="e.g. How to prepare for campus placement coding rounds in Sem 5?" 
          style="width:100%;padding:0.65rem;border-radius:6px;border:1px solid var(--border);background:var(--bg);color:var(--text);font-size:0.9rem;">
      </div>
      <div>
        <label style="display:block;font-size:0.85rem;font-weight:600;margin-bottom:0.35rem;">Category</label>
        <select id="jq-category" style="width:100%;padding:0.65rem;border-radius:6px;border:1px solid var(--border);background:var(--bg);color:var(--text);font-size:0.9rem;">
          <option value="Placement">Placement Preparation</option>
          <option value="Academics">Academic Strategy &amp; CGPA</option>
          <option value="Internships">Internships &amp; Resume</option>
          <option value="Higher Studies">Higher Studies (GATE/MS)</option>
        </select>
      </div>
      <div>
        <label style="display:block;font-size:0.85rem;font-weight:600;margin-bottom:0.35rem;">Detailed Context / Query *</label>
        <textarea id="jq-body" rows="4" required placeholder="Provide details about your current semester, skills, or obstacles..."
          style="width:100%;padding:0.65rem;border-radius:6px;border:1px solid var(--border);background:var(--bg);color:var(--text);font-size:0.9rem;resize:vertical;"></textarea>
      </div>
      <button type="submit" class="btn-primary" id="jq-submit" style="padding:0.75rem;font-weight:700;">Post Question ➔</button>
    </form>
  `;
  grid.appendChild(formCard);

  var listCard = createElement("div", "card");
  listCard.style.cssText = "background:var(--surface);border:1px solid var(--border);border-radius:10px;padding:1.5rem;";
  listCard.innerHTML = `
    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:1rem;">
      <h3 style="font-size:1.15rem;font-weight:700;margin:0;">Recent Questions &amp; Senior Answers</h3>
      <button id="refresh-q-list" style="background:none;border:none;color:var(--primary);cursor:pointer;font-weight:600;font-size:0.85rem;">🔄 Refresh</button>
    </div>
    <div id="questions-feed" style="display:grid;gap:1rem;max-height:450px;overflow-y:auto;padding-right:4px;">
      <p style="color:var(--muted);font-size:0.85rem;">Loading discussions...</p>
    </div>
  `;
  grid.appendChild(listCard);
  wrap.appendChild(grid);

  // Handle Form Submission
  setTimeout(() => {
    const form = document.getElementById("junior-qa-form");
    if (form) {
      form.addEventListener("submit", async function (e) {
        e.preventDefault();
        const title = document.getElementById("jq-title").value.trim();
        const category = document.getElementById("jq-category").value;
        const questionBody = document.getElementById("jq-body").value.trim();
        const submitBtn = document.getElementById("jq-submit");

        submitBtn.disabled = true;
        submitBtn.textContent = "Posting…";

        try {
          const token = localStorage.getItem("jwt");
          const headers = { "Content-Type": "application/json" };
          if (token) headers["Authorization"] = "Bearer " + token;

          const res = await fetch("/api/junior/questions", {
            method: "POST",
            headers,
            body: JSON.stringify({ title, category, questionBody })
          });

          if (res.ok) {
            showToast("Question posted successfully! Seniors will be notified.");
            form.reset();
            loadJuniorQuestionsList();
          } else {
            showToast("Failed to post question. Please try again.");
          }
        } catch (err) {
          showToast("Network error submitting question.");
        } finally {
          submitBtn.disabled = false;
          submitBtn.textContent = "Post Question ➔";
        }
      });
    }

    const refreshBtn = document.getElementById("refresh-q-list");
    if (refreshBtn) refreshBtn.addEventListener("click", loadJuniorQuestionsList);
    loadJuniorQuestionsList();

    if (window._qaPollInterval) clearInterval(window._qaPollInterval);
    window._qaPollInterval = setInterval(() => {
      if (document.getElementById("questions-feed")) {
        loadJuniorQuestionsList();
      } else {
        clearInterval(window._qaPollInterval);
      }
    }, 5000);
  }, 50);

  frag.appendChild(wrap);
  return frag;
}

async function loadJuniorQuestionsList() {
  const feed = document.getElementById("questions-feed");
  if (!feed) return;

  try {
    const token = localStorage.getItem("jwt");
    const headers = {};
    if (token) headers["Authorization"] = "Bearer " + token;

    const res = await fetch("/api/junior/questions", { headers });
    if (!res.ok) throw new Error();
    const questions = await res.json();

    if (!questions || questions.length === 0) {
      feed.innerHTML = `<p style="color:var(--muted);font-size:0.85rem;text-align:center;padding:1rem;">No questions posted yet. Be the first to ask!</p>`;
      return;
    }

    feed.innerHTML = questions.map(q => `
      <div style="padding:0.85rem;background:var(--bg);border-radius:8px;border:1px solid var(--border);">
        <div style="display:flex;justify-content:space-between;align-items:flex-start;gap:0.5rem;margin-bottom:0.35rem;">
          <span style="font-weight:700;font-size:0.9rem;color:var(--text);">${escapeHtml(q.title)}</span>
          <span class="tag ${q.status === 'ANSWERED' ? 'tag--intern' : 'tag--acad'}" style="font-size:0.75rem;">${q.status || 'OPEN'}</span>
        </div>
        <p style="color:var(--muted);font-size:0.82rem;margin-bottom:0.5rem;">${escapeHtml(q.questionBody || q.body || '')}</p>
        <div style="display:flex;justify-content:space-between;align-items:center;font-size:0.76rem;color:var(--muted);">
          <span>By: ${escapeHtml(q.studentName || 'Student')}</span>
          <span>${(q.answers || []).length} Verified Answers</span>
        </div>
      </div>
    `).join("");
  } catch (_) {
    feed.innerHTML = `<p style="color:var(--muted);font-size:0.85rem;">Could not load recent discussions.</p>`;
  }
}

/** Build Career Roadmaps View — Integrated for any logged-in student & custom goal */
function renderRoadmapsView() {
  const student = getStudentAcademicProfile();
  var frag = document.createDocumentFragment();
  var wrap = createElement("div", "roadmaps-container");
  wrap.style.cssText = "max-width: 900px; margin: 0 auto; display: grid; gap: 1.5rem;";

  // Top header with student greeting
  var headerDiv = createElement("div", "");
  headerDiv.innerHTML = `
    <h2 class="view-heading">AI Career &amp; Academic Roadmaps</h2>
    <p class="view-subtitle">Generate customized semester-wise career milestones tailored to your dream company, current semester, and academic status.</p>
  `;
  wrap.appendChild(headerDiv);

  // Dynamic Custom Generator Card
  var genCard = createElement("div", "card");
  genCard.style.cssText = "background: var(--surface); border: 2px solid var(--primary); border-radius: 12px; padding: 1.75rem; box-shadow: 0 4px 12px rgba(37,99,235,0.08);";
  genCard.innerHTML = `
    <div style="display:flex;justify-content:space-between;align-items:center;flex-wrap:wrap;gap:0.75rem;margin-bottom:1.25rem;">
      <div>
        <span class="tag tag--intern" style="font-weight:700;">✨ Interactive AI Roadmap Generator</span>
        <h3 style="font-size:1.2rem;margin:0.4rem 0 0.15rem;font-weight:800;">Generate Roadmap for ${student.name}</h3>
        <p style="color:var(--muted);font-size:0.85rem;margin:0;">Reg No: <strong>${student.regNumber}</strong> · Student Profile: <strong>${student.email}</strong></p>
      </div>
    </div>

    <form id="custom-ai-roadmap-form" style="display:grid;gap:1.1rem;">
      <div>
        <label style="display:block;font-size:0.86rem;font-weight:700;margin-bottom:0.4rem;">
          Target Career Goal / Dream Company Role *
        </label>
        <input type="text" id="custom-target-goal" class="form-input" required
          value="${escapeHtml(student.careerGoal)}"
          placeholder="e.g. Full-Stack SDE at Google, DevOps &amp; Cloud at AWS, AI Engineer, Zoho PPO"
          style="width:100%;padding:0.7rem 0.85rem;border-radius:8px;border:1px solid var(--border);background:var(--bg);color:var(--text);font-size:0.92rem;">
        
        <!-- Quick Goal Presets -->
        <div style="display:flex;gap:6px;flex-wrap:wrap;margin-top:0.5rem;align-items:center;">
          <span style="font-size:0.76rem;color:var(--muted);font-weight:600;">Quick Presets:</span>
          <button type="button" class="preset-chip" data-goal="Full-Stack SDE at Product Companies (Google/Amazon)" style="font-size:0.75rem;padding:3px 9px;border-radius:12px;border:1px solid var(--border);background:var(--surface);cursor:pointer;color:var(--text);">🚀 Product SDE</button>
          <button type="button" class="preset-chip" data-goal="Cloud &amp; DevOps Engineer (AWS, Docker, Kubernetes)" style="font-size:0.75rem;padding:3px 9px;border-radius:12px;border:1px solid var(--border);background:var(--surface);cursor:pointer;color:var(--text);">☁️ Cloud &amp; DevOps</button>
          <button type="button" class="preset-chip" data-goal="AI &amp; Machine Learning Engineer (PyTorch, LLMs, RAG)" style="font-size:0.75rem;padding:3px 9px;border-radius:12px;border:1px solid var(--border);background:var(--surface);cursor:pointer;color:var(--text);">🤖 AI &amp; ML</button>
          <button type="button" class="preset-chip" data-goal="Backend Microservices Engineer (Java Spring Boot, PostgreSQL)" style="font-size:0.75rem;padding:3px 9px;border-radius:12px;border:1px solid var(--border);background:var(--surface);cursor:pointer;color:var(--text);">☕ Spring Boot SDE</button>
        </div>
      </div>

      <div style="display:grid;grid-template-columns:1fr 1fr;gap:1rem;">
        <div>
          <label style="display:block;font-size:0.86rem;font-weight:700;margin-bottom:0.4rem;">
            Current Semester (1 to 8)
          </label>
          <select id="custom-semester" style="width:100%;padding:0.7rem 0.85rem;border-radius:8px;border:1px solid var(--border);background:var(--bg);color:var(--text);font-size:0.92rem;">
            ${[1, 2, 3, 4, 5, 6, 7, 8].map(s => `<option value="${s}" ${s === student.semester ? 'selected' : ''}>Semester ${s}</option>`).join("")}
          </select>
        </div>
        <div>
          <label style="display:block;font-size:0.86rem;font-weight:700;margin-bottom:0.4rem;">
            Current CGPA (0.0 to 10.0)
          </label>
          <input type="number" id="custom-cgpa" step="0.1" min="0" max="10" value="${student.cgpa}"
            style="width:100%;padding:0.7rem 0.85rem;border-radius:8px;border:1px solid var(--border);background:var(--bg);color:var(--text);font-size:0.92rem;">
        </div>
      </div>

      <button type="submit" id="btn-custom-generate" class="btn-primary" style="padding:0.85rem 1.25rem;font-size:0.95rem;font-weight:700;justify-content:center;">
        ⚡ Generate AI Career Roadmap with Gemini ➔
      </button>
    </form>
  `;
  wrap.appendChild(genCard);

  // Pre-seeded Reference Timeline Card
  var refCard = createElement("div", "card");
  refCard.style.cssText = "background:var(--surface);border:1px solid var(--border);border-radius:10px;padding:1.5rem;";
  refCard.innerHTML = `
    <div style="display:flex;justify-content:space-between;align-items:center;flex-wrap:wrap;gap:1rem;margin-bottom:1.25rem;">
      <div>
        <span class="tag tag--acad">Curriculum Benchmark</span>
        <h3 style="font-size:1.15rem;margin:0.35rem 0 0.2rem;font-weight:700;">Department Full-Stack &amp; Placement Readiness Blueprint</h3>
        <p style="color:var(--muted);font-size:0.85rem;margin:0;">Standard semester milestone sequence recommended by CS department mentors.</p>
      </div>
    </div>

    <div style="display:grid;gap:0.75rem;">
      <div style="display:flex;gap:1rem;align-items:center;padding:0.75rem 1rem;background:var(--bg);border-radius:8px;border-left:4px solid var(--primary);">
        <div style="font-weight:700;font-size:0.85rem;min-width:65px;color:var(--primary);">Sem 1</div>
        <div>
          <div style="font-weight:600;font-size:0.9rem;">C &amp; Discrete Mathematics Fundamentals</div>
          <div style="color:var(--muted);font-size:0.82rem;">Variables, memory allocation, pointers, Boolean algebra, logic gates.</div>
        </div>
      </div>

      <div style="display:flex;gap:1rem;align-items:center;padding:0.75rem 1rem;background:var(--bg);border-radius:8px;border-left:4px solid var(--primary);">
        <div style="font-weight:700;font-size:0.85rem;min-width:65px;color:var(--primary);">Sem 2</div>
        <div>
          <div style="font-weight:600;font-size:0.9rem;">Object-Oriented Programming (Java)</div>
          <div style="color:var(--muted);font-size:0.82rem;">Inheritance, Polymorphism, Collections Framework, Exception Handling.</div>
        </div>
      </div>

      <div style="display:flex;gap:1rem;align-items:center;padding:0.75rem 1rem;background:var(--bg);border-radius:8px;border-left:4px solid var(--primary);">
        <div style="font-weight:700;font-size:0.85rem;min-width:65px;color:var(--primary);">Sem 3</div>
        <div>
          <div style="font-weight:600;font-size:0.9rem;">Data Structures &amp; Algorithms Rigor</div>
          <div style="color:var(--muted);font-size:0.82rem;">Arrays, Linked Lists, Stacks, Queues, Binary Trees, Time Complexity.</div>
        </div>
      </div>

      <div style="display:flex;gap:1rem;align-items:center;padding:0.75rem 1rem;background:var(--bg);border-radius:8px;border-left:4px solid var(--primary);">
        <div style="font-weight:700;font-size:0.85rem;min-width:65px;color:var(--primary);">Sem 4</div>
        <div>
          <div style="font-weight:600;font-size:0.9rem;">Database Systems &amp; SQL Optimization</div>
          <div style="color:var(--muted);font-size:0.82rem;">ACID properties, Normalization (1NF to BCNF), indexing, complex JOIN queries.</div>
        </div>
      </div>

      <div style="display:flex;gap:1rem;align-items:center;padding:0.75rem 1rem;background:var(--bg);border-radius:8px;border-left:4px solid var(--primary);">
        <div style="font-weight:700;font-size:0.85rem;min-width:65px;color:var(--primary);">Sem 5</div>
        <div>
          <div style="font-weight:600;font-size:0.9rem;">Web Architecture &amp; REST APIs (Spring Boot)</div>
          <div style="color:var(--muted);font-size:0.82rem;">4-tier MVC architecture, Spring Security, JWT authentication, Hibernate JPA.</div>
        </div>
      </div>

      <div style="display:flex;gap:1rem;align-items:center;padding:0.75rem 1rem;background:var(--bg);border-radius:8px;border-left:4px solid var(--primary);">
        <div style="font-weight:700;font-size:0.85rem;min-width:65px;color:var(--primary);">Sem 6</div>
        <div>
          <div style="font-weight:600;font-size:0.9rem;">Operating Systems &amp; Computer Networks</div>
          <div style="color:var(--muted);font-size:0.82rem;">Process synchronization, virtual memory, TCP/IP handshake, HTTP/HTTPS protocols.</div>
        </div>
      </div>

      <div style="display:flex;gap:1rem;align-items:center;padding:0.75rem 1rem;background:var(--bg);border-radius:8px;border-left:4px solid #16a34a;">
        <div style="font-weight:700;font-size:0.85rem;min-width:65px;color:#16a34a;">Sem 7–8</div>
        <div>
          <div style="font-weight:600;font-size:0.9rem;">System Design &amp; Mock Placement Drives 🎯</div>
          <div style="color:var(--muted);font-size:0.82rem;">Scalability, caching, microservices, behavioral rounds, campus recruitment.</div>
        </div>
      </div>
    </div>
  `;
  wrap.appendChild(refCard);

  // Attach event listeners for dynamic custom form & quick preset chips
  setTimeout(() => {
    document.querySelectorAll(".preset-chip").forEach(chip => {
      chip.addEventListener("click", () => {
        const goalInput = document.getElementById("custom-target-goal");
        if (goalInput) goalInput.value = chip.getAttribute("data-goal");
      });
    });

    const form = document.getElementById("custom-ai-roadmap-form");
    if (form) {
      form.addEventListener("submit", (e) => {
        e.preventDefault();
        const goal = document.getElementById("custom-target-goal").value.trim();
        const semester = Number(document.getElementById("custom-semester").value);
        const cgpa = Number(document.getElementById("custom-cgpa").value);
        const btn = document.getElementById("btn-custom-generate");

        // Save preferences for this student
        saveStudentAcademicProfile(semester, cgpa, goal);

        // Call the AI roadmap generator with the custom parameters
        handleGenerateRoadmap(null, btn, goal, semester, cgpa);
      });
    }
  }, 50);

  frag.appendChild(wrap);
  return frag;
}

/** Build My Profile view with real student editing */
function renderProfile() {
  const profile = getStudentAcademicProfile();
  var frag = document.createDocumentFragment();

  var header = createElement("div", "profile-view__header");
  header.appendChild(createElement("h2", "view-heading", "My Student Profile"));
  header.appendChild(createElement("p", "view-subtitle", "View and update your academic details used for personalized AI roadmaps."));
  frag.appendChild(header);

  var body = createElement("div", "profile-view__body");
  var card = createElement("div", "profile-card");
  card.style.cssText = "max-width:650px;margin:0 auto;background:var(--surface);border:1px solid var(--border);border-radius:12px;padding:2rem;";

  card.innerHTML = `
    <div style="display:flex;align-items:center;gap:1.25rem;margin-bottom:1.5rem;">
      <span class="avatar avatar--lg" style="font-size:1.4rem;">${profile.initials}</span>
      <div>
        <div style="font-size:1.35rem;font-weight:800;color:var(--text);">${escapeHtml(profile.name)}</div>
        <div style="color:var(--primary);font-weight:700;font-size:0.9rem;">Register No: ${escapeHtml(profile.regNumber)}</div>
        <div style="color:var(--muted);font-size:0.85rem;">${escapeHtml(profile.email)} · ${profile.role}</div>
      </div>
    </div>

    <form id="profile-acad-form" style="display:grid;gap:1.2rem;border-top:1px solid var(--border);padding-top:1.5rem;">
      <h3 style="font-size:1.05rem;font-weight:700;">Academic Settings for AI Roadmap</h3>
      <div>
        <label style="display:block;font-size:0.86rem;font-weight:700;margin-bottom:0.35rem;">Current Semester (1 to 8)</label>
        <select id="profile-sem" style="width:100%;padding:0.65rem;border-radius:6px;border:1px solid var(--border);background:var(--bg);color:var(--text);font-size:0.9rem;">
          ${[1, 2, 3, 4, 5, 6, 7, 8].map(s => `<option value="${s}" ${s === profile.semester ? 'selected' : ''}>Semester ${s}</option>`).join("")}
        </select>
      </div>

      <div>
        <label style="display:block;font-size:0.86rem;font-weight:700;margin-bottom:0.35rem;">Current CGPA (0.0 to 10.0)</label>
        <input type="number" id="profile-cgpa" step="0.1" min="0" max="10" value="${profile.cgpa}"
          style="width:100%;padding:0.65rem;border-radius:6px;border:1px solid var(--border);background:var(--bg);color:var(--text);font-size:0.9rem;">
      </div>

      <div>
        <label style="display:block;font-size:0.86rem;font-weight:700;margin-bottom:0.35rem;">Target Career Goal</label>
        <input type="text" id="profile-goal" value="${escapeHtml(profile.careerGoal)}"
          style="width:100%;padding:0.65rem;border-radius:6px;border:1px solid var(--border);background:var(--bg);color:var(--text);font-size:0.9rem;">
      </div>

      <button type="submit" class="btn-primary" style="justify-content:center;padding:0.75rem;font-weight:700;">
        Save Academic Profile
      </button>
    </form>
  `;

  setTimeout(() => {
    const pForm = document.getElementById("profile-acad-form");
    if (pForm) {
      pForm.addEventListener("submit", (e) => {
        e.preventDefault();
        const sem = Number(document.getElementById("profile-sem").value);
        const cgpa = Number(document.getElementById("profile-cgpa").value);
        const goal = document.getElementById("profile-goal").value.trim();
        saveStudentAcademicProfile(sem, cgpa, goal);
        showToast("Academic profile saved! AI Roadmaps will now use Semester " + sem + " & CGPA " + cgpa + ".");
      });
    }
  }, 50);

  body.appendChild(card);
  frag.appendChild(body);
  return frag;
}

// ───────────────────────────────────────────
// 6. VIEW SWITCHING & ROADMAP GENERATION
// ───────────────────────────────────────────

var currentView = "senior-posts";

function showView(viewName) {
  currentView = viewName;
  mainContent.textContent = "";

  switch (viewName) {
    case "senior-posts":
      mainContent.appendChild(renderSeniorPosts());
      break;
    case "raise-question":
      mainContent.appendChild(renderRaiseQuestionView());
      break;
    case "career-roadmaps":
      mainContent.appendChild(renderRoadmapsView());
      break;
    case "my-profile":
      mainContent.appendChild(renderProfile());
      break;
    default:
      mainContent.appendChild(renderSeniorPosts());
  }

  sidebarButtons.forEach(function (btn) {
    var isActive = btn.getAttribute("data-view") === viewName;
    btn.classList.toggle("sidebar__item--active", isActive);
    if (isActive) {
      btn.setAttribute("aria-current", "page");
    } else {
      btn.removeAttribute("aria-current");
    }
  });

  closeMobileSidebar();
}

/**
 * Handle AI Roadmap generation for ANY post (newly published achievements or pre-seeded)
 * and for ANY logged-in student (using their actual semester, CGPA, and name).
 */
async function handleGenerateRoadmap(target, clickedButton, customGoal, customSemester, customCgpa) {
  const button = clickedButton || document.querySelector(".post-card button.btn-primary");
  let token = null;

  try {
    token = localStorage.getItem("jwt");
  } catch (_) {}

  // Resolve target post whether passed as object or ID
  let post = null;
  if (typeof target === "object" && target !== null) {
    post = target;
  } else if (target) {
    post = (allLoadedPosts || []).find(item => item.id === target)
        || MOCK_POSTS.find(item => item.id === target);
  }

  const student = getStudentAcademicProfile();
  const semester = Number(customSemester || student.semester || 3);
  const cgpa = Number(customCgpa || student.cgpa || 8.6);

  let goal;
  if (customGoal && customGoal.trim()) {
    goal = customGoal.trim();
  } else if (post) {
    // Include the actual senior's name, company, and achievement details in the goal prompt
    goal = `Personalized career roadmap for ${student.name} (Semester ${semester}, CGPA ${cgpa}) inspired by senior playbook & achievement: "${post.title}". Senior Guide: ${post.seniorName} (${post.company || 'Industry Mentor'}). Guidance Details: ${post.summary || post.title}`;
  } else {
    goal = student.careerGoal || "Comprehensive full-stack and algorithmic preparation for top product company recruitment";
  }

  const originalContent = button ? button.innerHTML : "";
  if (button) {
    button.disabled = true;
    button.innerHTML = `⏳ Generating AI Roadmap...`;
  }

  showToast("Generating personalized roadmap for " + student.name + " (Sem " + semester + ")...");

  try {
    const headers = { "Content-Type": "application/json" };
    if (token) {
      headers["Authorization"] = "Bearer " + token;
    }

    const response = await fetch("/api/v1/ai-assistant/generate-path", {
      method: "POST",
      headers: headers,
      body: JSON.stringify({
        goal: goal,
        currentSemester: semester,
        currentCgpa: cgpa
      })
    });

    const result = await response.json();

    if (!response.ok || !result.success) {
      if (response.status === 401) {
        throw new Error("Your session has expired. Please log in again.");
      }
      if (response.status === 403) {
        throw new Error("You don't have permission to generate this roadmap.");
      }
      throw new Error(result.message || "Roadmap generation failed. Please try again.");
    }

    renderGeneratedRoadmap(result.data, student, post);
    showToast("Personalized AI Career Roadmap generated successfully!");
  } catch (error) {
    console.error("Roadmap generation error:", error);
    showToast(
      error.message && error.message.includes("Failed to fetch")
        ? "Cannot connect to backend server. Ensure Spring Boot is running."
        : error.message || "Failed to generate roadmap."
    );
  } finally {
    if (button) {
      button.disabled = false;
      button.innerHTML = originalContent;
    }
  }
}

function renderGeneratedRoadmap(data, student, sourcePost) {
  mainContent.textContent = "";

  const wrap = createElement("div", "senior-posts-feed");
  wrap.style.cssText = "max-width: 900px; margin: 0 auto; display: grid; gap: 1.5rem;";

  const topBar = createElement("div", "");
  topBar.style.cssText = "display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 0.75rem;";

  const backButton = createElement("button", "btn-primary", "← Back to Guidance Feed");
  backButton.type = "button";
  backButton.addEventListener("click", function () {
    showView("senior-posts");
  });
  topBar.appendChild(backButton);

  const badge = createElement("span", "tag tag--high", "✨ Gemini AI Generated Roadmap");
  topBar.appendChild(badge);
  wrap.appendChild(topBar);

  const headerCard = createElement("div", "card");
  headerCard.style.cssText = "background: var(--surface); border: 2px solid var(--primary); border-radius: 12px; padding: 1.75rem;";
  
  let seniorCredit = "";
  if (sourcePost) {
    seniorCredit = `<div style="margin-top:0.5rem;padding:0.5rem 0.75rem;background:var(--bg);border-radius:6px;font-size:0.83rem;color:var(--text);border-left:3px solid var(--primary);">
      💡 Inspired by Senior Guide: <strong>${escapeHtml(sourcePost.seniorName)}</strong> (${escapeHtml(sourcePost.company || 'Industry')}) — <em>${escapeHtml(sourcePost.title)}</em>
    </div>`;
  }

  headerCard.innerHTML = `
    <h2 class="view-heading" style="margin-bottom: 0.5rem;">Personalized AI Career Roadmap</h2>
    <p class="view-subtitle" style="margin-bottom: 0.75rem;"><strong>Roadmap Target:</strong> ${escapeHtml(data.goal)}</p>
    ${seniorCredit}
    <div style="display: flex; gap: 1.25rem; font-size: 0.85rem; color: var(--muted); flex-wrap: wrap; margin-top:0.75rem;">
      <span><strong>Student:</strong> ${escapeHtml(student ? student.name : 'Junior Student')} (${escapeHtml(student ? student.regNumber : '')})</span>
      <span>•</span>
      <span><strong>Starting Semester:</strong> Semester ${data.currentSemester || (student ? student.semester : 3)}</span>
      <span>•</span>
      <span><strong>Milestones:</strong> ${(data.adaptiveMilestones || []).length} sequential steps</span>
      <span>•</span>
      <span><strong>Skills Identified:</strong> ${(data.recommendedRoadmapSkills || []).length} core technologies</span>
    </div>
  `;
  wrap.appendChild(headerCard);

  // Milestones
  const milestonesCard = createElement("div", "card");
  milestonesCard.style.cssText = "background: var(--surface); border: 1px solid var(--border); border-radius: 12px; padding: 1.5rem;";
  milestonesCard.appendChild(createElement("h3", "post-card__journey-heading", "Semester-Wise Adaptive Milestones"));

  const timeline = createElement("div", "timeline");
  (data.adaptiveMilestones || []).forEach(function (milestone, idx) {
    const step = createElement("div", "timeline__step");
    const yearEl = createElement("span", "timeline__year", "Step " + (idx + 1));
    const descEl = createElement("p", "timeline__desc", milestone);
    step.appendChild(yearEl);
    step.appendChild(descEl);
    timeline.appendChild(step);
  });
  milestonesCard.appendChild(timeline);
  wrap.appendChild(milestonesCard);

  // Recommended Skills
  const skillsCard = createElement("div", "card");
  skillsCard.style.cssText = "background: var(--surface); border: 1px solid var(--border); border-radius: 12px; padding: 1.5rem;";
  skillsCard.appendChild(createElement("h3", "post-card__journey-heading", "Recommended Technical Skills"));

  const skillsWrap = createElement("div", "post-card__tags");
  skillsWrap.style.cssText = "display: flex; flex-wrap: wrap; gap: 0.5rem; margin-top: 0.75rem;";
  (data.recommendedRoadmapSkills || []).forEach(function (skill) {
    skillsWrap.appendChild(createElement("span", "tag tag--acad", skill));
  });
  skillsCard.appendChild(skillsWrap);
  wrap.appendChild(skillsCard);

  // Senior Mentor Advice
  const adviceCard = createElement("article", "post-card");
  adviceCard.style.cssText = "background: var(--surface); border: 1px solid var(--border); border-left: 4px solid var(--primary); border-radius: 12px; padding: 1.5rem;";
  adviceCard.appendChild(createElement("h3", "post-card__title", "💡 Senior Mentor Advice"));
  adviceCard.appendChild(
    createElement(
      "p",
      "post-card__summary",
      data.seniorMentorAdvice || "Focus consistently on coding problem solving and build projects that demonstrate clean system architecture."
    )
  );
  wrap.appendChild(adviceCard);

  mainContent.appendChild(wrap);
}

function injectMobileSidebarControls() {
  var toggleBtn = createElement("button", "sidebar-toggle");
  toggleBtn.setAttribute("type", "button");
  toggleBtn.setAttribute("aria-label", "Open navigation menu");
  var svg = document.createElementNS("http://www.w3.org/2000/svg", "svg");
  svg.setAttribute("width", "22");
  svg.setAttribute("height", "22");
  svg.setAttribute("viewBox", "0 0 24 24");
  svg.setAttribute("fill", "none");
  svg.setAttribute("stroke", "currentColor");
  svg.setAttribute("stroke-width", "2");
  svg.setAttribute("stroke-linecap", "round");
  svg.setAttribute("stroke-linejoin", "round");
  ["4", "12", "20"].forEach(function (y) {
    var line = document.createElementNS("http://www.w3.org/2000/svg", "line");
    line.setAttribute("x1", "3");
    line.setAttribute("y1", y);
    line.setAttribute("x2", "21");
    line.setAttribute("y2", y);
    svg.appendChild(line);
  });
  toggleBtn.appendChild(svg);

  var brand = document.querySelector(".header__brand");
  if (brand) brand.insertBefore(toggleBtn, brand.firstChild);

  var overlay = createElement("div", "sidebar-overlay");
  overlay.id = "sidebar-overlay";
  document.body.appendChild(overlay);

  toggleBtn.addEventListener("click", function () {
    var isOpen = sidebar.classList.toggle("sidebar--open");
    overlay.classList.toggle("sidebar-overlay--visible", isOpen);
  });

  overlay.addEventListener("click", closeMobileSidebar);
}

function closeMobileSidebar() {
  if (sidebar) sidebar.classList.remove("sidebar--open");
  var overlay = document.getElementById("sidebar-overlay");
  if (overlay) overlay.classList.remove("sidebar-overlay--visible");
}

function escapeHtml(value) {
  return String(value ?? "").replace(/[&<>"']/g, (c) => ({
    "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;"
  })[c]);
}

// ───────────────────────────────────────────
// 7. INITIALISE
// ───────────────────────────────────────────

(function init() {
  mainContent.appendChild(renderSeniorPosts());

  sidebarButtons.forEach(function (btn) {
    btn.addEventListener("click", function () {
      var view = btn.getAttribute("data-view");
      showView(view);
    });
  });

  var headerProfileBtn = document.getElementById("header-profile-btn");
  if (headerProfileBtn) {
    headerProfileBtn.addEventListener("click", function () {
      showView("my-profile");
    });
  }

  injectMobileSidebarControls();
})();
