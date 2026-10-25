/* ═══════════════════════════════════════════════════════════
   EduPath Junior Portal — app.js (Integrated)
   ═══════════════════════════════════════════════════════════ */

// ───────────────────────────────────────────
// 1. PROFILE & MOCK DATA
// ───────────────────────────────────────────

let currentJuniorUser = null;
try {
  const u = localStorage.getItem("user");
  if (u) currentJuniorUser = JSON.parse(u);
} catch (_) {}

const JUNIOR_PROFILE = {
  name: (currentJuniorUser && currentJuniorUser.name) ? currentJuniorUser.name : "Vignesh K",
  initials: (currentJuniorUser && currentJuniorUser.name)
    ? currentJuniorUser.name.split(" ").map(w => w[0]).join("")
    : "VK",
  regNumber: "23CS101",
  role: "Junior Student",
  department: "Computer Science & Engineering",
  year: "3rd Year",
  email: (currentJuniorUser && currentJuniorUser.email) ? currentJuniorUser.email : "vignesh@edupath.edu"
};

window.signOut = function() {
  localStorage.removeItem("jwt");
  localStorage.removeItem("user");
  window.location.href = "/login.html";
};

// Update topbar profile elements if present
document.addEventListener("DOMContentLoaded", function () {
  const nameEl = document.getElementById("user-display-name");
  const initEl = document.getElementById("user-avatar-initials");
  if (nameEl && currentJuniorUser) {
    nameEl.textContent = `${currentJuniorUser.name} (${currentJuniorUser.role || 'Junior'})`;
  }
  if (initEl && JUNIOR_PROFILE.initials) {
    initEl.textContent = JUNIOR_PROFILE.initials;
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
    "Higher Studies": "tag--high"
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
      "Learn directly from verified seniors and alumni about semester preparation, CGPA strategy, coding interviews, and career playbooks."
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
          combinedPosts.unshift({
            id: p.postId,
            seniorName: p.authorName || "Senior Author",
            year: p.authorRole || "SENIOR",
            company: p.category || "Placement",
            jobTitle: "Senior Guide",
            title: p.title,
            summary: p.markdownContent ? p.markdownContent.slice(0, 300) + (p.markdownContent.length > 300 ? "..." : "") : "",
            tags: [p.category || "Academics"],
            journey: [
              { year: "Key Advice", desc: p.markdownContent ? p.markdownContent.slice(0, 180) + "..." : "Follow structured milestones." }
            ]
          });
        });
      }
    }
  } catch (_) {}

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
      handleGenerateRoadmap(post.id);
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

  // Heading & banner
  var head = createElement("div", "view-header");
  head.appendChild(createElement("h2", "view-heading", "Raise a Question to Seniors & Alumni"));
  head.appendChild(
    createElement(
      "p",
      "view-subtitle",
      "Have a question about academics, placements, or college life? Ask our community of verified seniors and alumni."
    )
  );

  var banner = createElement("div", "forum-banner");
  banner.style.cssText = "background:#eff6ff;border:1px solid #bfdbfe;border-radius:8px;padding:0.9rem 1.2rem;display:flex;justify-content:space-between;align-items:center;flex-wrap:wrap;gap:0.75rem;";
  banner.innerHTML = `
    <div>
      <strong style="color:#1e40af;font-size:0.92rem;">💡 Dedicated Role-Aware Q&amp;A Forum</strong>
      <p style="margin:2px 0 0;font-size:0.83rem;color:#1e3a8a">You can also view the full dual-view Senior answer queue.</p>
    </div>
    <a href="/forum.html" style="background:#2563eb;color:#fff;padding:0.45rem 1rem;border-radius:6px;font-size:0.82rem;font-weight:600;text-decoration:none;">Open Full Forum ➔</a>
  `;
  head.appendChild(banner);
  wrap.appendChild(head);

  // Two column layout: Ask Form + My Questions List
  var grid = createElement("div", "qa-grid");
  grid.style.cssText = "display:grid;grid-template-columns:minmax(0, 1fr) minmax(0, 1fr);gap:1.5rem;";
  if (window.innerWidth < 850) {
    grid.style.gridTemplateColumns = "1fr";
  }

  // Column 1: Ask Form Card
  var formCard = createElement("div", "card");
  formCard.style.cssText = "background:var(--surface);border:1px solid var(--border);border-radius:10px;padding:1.5rem;";
  formCard.innerHTML = `
    <h3 style="font-size:1.1rem;margin-bottom:0.25rem;">Ask a New Question</h3>
    <p style="color:var(--muted);font-size:0.85rem;margin-bottom:1.25rem;">Your question will be broadcast to all Seniors and Alumni mentors.</p>
    <form id="junior-qa-form">
      <div style="margin-bottom:1rem;">
        <label style="display:block;font-size:0.85rem;font-weight:600;margin-bottom:0.35rem;" for="jq-title">Question Title *</label>
        <input style="width:100%;border:1px solid var(--border);border-radius:6px;padding:0.6rem 0.75rem;font-size:0.9rem;"
               id="jq-title" required placeholder="What would you like guidance with?">
      </div>
      <div style="margin-bottom:1rem;">
        <label style="display:block;font-size:0.85rem;font-weight:600;margin-bottom:0.35rem;" for="jq-category">Topic *</label>
        <select style="width:100%;border:1px solid var(--border);border-radius:6px;padding:0.6rem 0.75rem;font-size:0.9rem;"
                id="jq-category" required>
          <option value="Academics">Academics</option>
          <option value="Internships">Internships</option>
          <option value="Higher Studies">Higher Studies</option>
          <option value="Placement">Placement</option>
        </select>
      </div>
      <div style="margin-bottom:1rem;">
        <label style="display:block;font-size:0.85rem;font-weight:600;margin-bottom:0.35rem;" for="jq-body">Question Details *</label>
        <textarea style="width:100%;min-height:100px;border:1px solid var(--border);border-radius:6px;padding:0.6rem 0.75rem;font-size:0.9rem;"
                  id="jq-body" required placeholder="Describe your question, your current semester, and what guidance you need..."></textarea>
      </div>
      <button type="submit" class="btn-primary" id="jq-submit" style="width:100%;justify-content:center;">Post Question ➔</button>
    </form>
  `;
  grid.appendChild(formCard);

  // Column 2: My Questions List Card
  var listCard = createElement("div", "card");
  listCard.style.cssText = "background:var(--surface);border:1px solid var(--border);border-radius:10px;padding:1.5rem;";
  listCard.innerHTML = `
    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:1rem;">
      <h3 style="font-size:1.1rem;margin:0;">Recent Q&amp;A Discussions</h3>
      <button type="button" id="refresh-q-list" style="font-size:0.82rem;color:var(--primary);font-weight:600;">↻ Refresh</button>
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

    // Auto-poll questions while user is on this view so senior replies appear automatically
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
    let list = [];
    const res = await fetch("/api/junior/questions");
    if (res.ok) {
      const data = await res.json();
      list = Array.isArray(data) ? data : (data.data || []);
    } else {
      const fallback = await fetch("/api/v1/questions");
      if (fallback.ok) {
        const data = await fallback.json();
        list = data.data || [];
      }
    }

    if (!list || list.length === 0) {
      feed.innerHTML = `<p style="color:var(--muted);font-size:0.85rem;">No questions posted yet. Be the first to ask!</p>`;
      return;
    }

    feed.innerHTML = list.map(q => {
      const hasAccepted = q.acceptedAnswer != null;
      const hasAnswers = (q.answers && q.answers.length > 0);
      const answer = hasAccepted ? q.acceptedAnswer : (hasAnswers ? q.answers[0] : null);
      const isAnswered = answer != null;

      return `
        <div style="border:1px solid var(--border);border-radius:8px;padding:1rem;background:var(--surface);margin-bottom:0.75rem;box-shadow:0 1px 2px rgba(0,0,0,0.03);">
          <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:0.4rem;flex-wrap:wrap;gap:0.4rem;">
            <span class="tag ${tagClass(q.category)}">${escapeHtml(q.category || "General")}</span>
            ${isAnswered
              ? '<span style="font-size:0.75rem;font-weight:700;color:#15803d;background:#dcfce7;padding:0.2rem 0.55rem;border-radius:999px;">✓ Answered by Senior</span>'
              : '<span style="font-size:0.75rem;font-weight:600;color:#b45309;background:#fef3c7;padding:0.2rem 0.55rem;border-radius:999px;">⏳ Waiting for Senior response</span>'
            }
          </div>
          <h4 style="font-size:0.95rem;margin:0 0 0.35rem;color:var(--text);font-weight:700;">${escapeHtml(q.title)}</h4>
          <p style="font-size:0.85rem;color:var(--muted);margin:0 0 0.6rem;line-height:1.45;">${escapeHtml(q.questionBody)}</p>

          ${isAnswered ? `
            <div style="margin-top:0.6rem;padding:0.75rem 1rem;background:#f0fdf4;border-left:4px solid #16a34a;border-radius:6px;font-size:0.85rem;">
              <div style="display:flex;align-items:center;gap:0.4rem;margin-bottom:0.25rem;">
                <span style="color:#15803d;font-weight:700;font-size:0.82rem;">✓ Verified Senior Guidance (${escapeHtml(answer.authorName || "Senior Mentor")}):</span>
              </div>
              <p style="margin:0;color:#1e293b;line-height:1.5;font-weight:500;">"${escapeHtml(answer.answerBody)}"</p>
            </div>
          ` : ''}
        </div>
      `;
    }).join("");
  } catch (_) {
    feed.innerHTML = `<p style="color:var(--muted);font-size:0.85rem;">Could not load recent discussions.</p>`;
  }
}

/** Build Career Roadmaps View */
function renderRoadmapsView() {
  var frag = document.createDocumentFragment();
  var wrap = createElement("div", "roadmaps-container");
  wrap.style.cssText = "max-width: 900px; margin: 0 auto;";

  wrap.innerHTML = `
    <div style="margin-bottom:1.5rem;">
      <h2 class="view-heading">AI Career &amp; Academic Roadmaps</h2>
      <p class="view-subtitle">Interactive semester-by-semester milestone graph based on EduPath's 8-table database architecture.</p>
    </div>

    <div class="card" style="background:var(--surface);border:1px solid var(--border);border-radius:10px;padding:1.5rem;margin-bottom:1.5rem;">
      <div style="display:flex;justify-content:space-between;align-items:center;flex-wrap:wrap;gap:1rem;margin-bottom:1.25rem;">
        <div>
          <span class="tag tag--high">Pre-seeded Graph</span>
          <h3 style="font-size:1.15rem;margin:0.35rem 0 0.2rem;">Full-Stack &amp; Placement Readiness Roadmap</h3>
          <p style="color:var(--muted);font-size:0.85rem;margin:0;">Goal: Comprehensive preparation for top product &amp; service company hiring drives.</p>
        </div>
        <button class="btn-primary" id="btn-re-generate" style="font-size:0.85rem;">⚡ Generate AI Roadmap</button>
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
    </div>
  `;

  setTimeout(() => {
    const btn = document.getElementById("btn-re-generate");
    if (btn) {
      btn.addEventListener("click", () => {
        showToast("AI Career Roadmap generated with 7 milestone nodes and 6 directed prerequisite edges!");
      });
    }
  }, 50);

  frag.appendChild(wrap);
  return frag;
}

/** Build My Profile view */
function renderProfile() {
  var frag = document.createDocumentFragment();

  var header = createElement("div", "profile-view__header");
  header.appendChild(createElement("h2", "view-heading", "My Student Profile"));
  header.appendChild(createElement("p", "view-subtitle", "View and manage your student profile and credentials."));
  frag.appendChild(header);

  var body = createElement("div", "profile-view__body");
  var card = createElement("div", "profile-card");
  card.appendChild(createElement("span", "avatar avatar--lg", JUNIOR_PROFILE.initials));
  card.appendChild(createElement("div", "profile-card__name", JUNIOR_PROFILE.name));
  card.appendChild(createElement("div", "profile-card__detail", "Reg No: " + JUNIOR_PROFILE.regNumber));
  card.appendChild(createElement("div", "profile-card__detail", JUNIOR_PROFILE.department + " · " + JUNIOR_PROFILE.year));
  card.appendChild(createElement("div", "profile-card__detail", "Email: " + JUNIOR_PROFILE.email));
  card.appendChild(createElement("span", "profile-card__badge", JUNIOR_PROFILE.role));

  var btn = createElement("button", "btn-primary", "Active Junior Session");
  btn.setAttribute("type", "button");
  btn.style.cssText = "margin-top:1rem;";
  btn.addEventListener("click", function () {
    showToast("Junior session active: " + JUNIOR_PROFILE.email);
  });
  card.appendChild(btn);

  body.appendChild(card);
  frag.appendChild(body);
  return frag;
}

// ───────────────────────────────────────────
// 6. VIEW SWITCHING
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

function handleGenerateRoadmap(postId) {
  showToast("Personalized AI Career Roadmap generated! Switched to AI Roadmaps view.");
  showView("career-roadmaps");
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
