"use strict";

const elements = {
  previewPanel: document.querySelector("#preview-panel"),
  previewError: document.querySelector("#preview-error"),
  forumWorkspace: document.querySelector("#forum-workspace"),
  juniorWorkspace: document.querySelector("#junior-workspace"),
  seniorWorkspace: document.querySelector("#senior-workspace"),
  pageTitle: document.querySelector("#page-title"),
  pageDescription: document.querySelector("#page-description"),
  roleBadge: document.querySelector("#role-badge"),
  roleLabel: document.querySelector("#role-label"),
  questionForm: document.querySelector("#question-form"),
  questionTitle: document.querySelector("#question-title"),
  questionBody: document.querySelector("#question-body"),
  questionCategory: document.querySelector("#question-category"),
  bodyCount: document.querySelector("#body-count"),
  juniorQuestionList: document.querySelector("#junior-question-list"),
  seniorQuestionList: document.querySelector("#senior-question-list"),
  refreshButton: document.querySelector("#refresh-button"),
  pageError: document.querySelector("#page-error"),
  pageSuccess: document.querySelector("#page-success"),
  previewFooter: document.querySelector("#preview-footer"),
  switchRoleButton: document.querySelector("#switch-preview-role"),
  connectionMessage: document.querySelector("#connection-message"),
};

const state = {
  csrfToken: "",
  role: "",
  previewMode: false,
  questionsLoading: false,
};

const categoryNames = {
  ACADEMICS: "Academics",
  Academics: "Academics",
  INTERNSHIPS: "Internships",
  Internships: "Internships",
  HIGHER_STUDIES: "Higher Studies",
  "Higher Studies": "Higher Studies",
  PLACEMENT: "Placement",
  Placement: "Placement",
};

function escapeHtml(value) {
  return String(value ?? "").replace(/[&<>"']/g, (character) => ({
    "&": "&amp;",
    "<": "&lt;",
    ">": "&gt;",
    '"': "&quot;",
    "'": "&#39;",
  })[character]);
}

function formatDate(value) {
  if (!value) return "";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return "";
  return new Intl.DateTimeFormat(undefined, { dateStyle: "medium", timeStyle: "short" }).format(date);
}

function categoryClass(category) {
  const name = categoryNames[category] ?? category;
  if (name === "Internships") return "category-internships";
  if (name === "Higher Studies") return "category-higher-studies";
  if (name === "Placement") return "category-placement";
  return "category-academics";
}

function showError(message) {
  elements.pageError.textContent = message;
  elements.pageError.hidden = !message;
  if (message) elements.pageSuccess.hidden = true;
}

function showSuccess(message) {
  elements.pageSuccess.textContent = message;
  elements.pageSuccess.hidden = !message;
  if (message) elements.pageError.hidden = true;
}

function renderEmpty(title, description) {
  return `<div class="empty-state">
    <span class="empty-icon" aria-hidden="true">✦</span>
    <h3>${escapeHtml(title)}</h3>
    <p>${escapeHtml(description)}</p>
  </div>`;
}

async function apiRequest(url, options = {}) {
  const headers = new Headers(options.headers ?? {});
  headers.set("Accept", "application/json");
  if (options.body) headers.set("Content-Type", "application/json");
  if (options.method && options.method !== "GET" && state.csrfToken) {
    headers.set("X-CSRF-TOKEN", state.csrfToken);
  }

  const response = await fetch(url, {
    ...options,
    headers,
    credentials: "same-origin",
  });

  if (response.status === 401) {
    throw new Error("Your EduPath session has expired. Open the forum again from your portal.");
  }
  if (response.status === 403) {
    throw new Error("Your current EduPath account does not have permission for this action.");
  }
  if (response.status === 204) return null;

  const result = await response.json().catch(() => null);
  if (!response.ok) {
    const fieldErrors = result?.fields ? Object.values(result.fields).join(" ") : "";
    throw new Error(fieldErrors || result?.message || "Something went wrong. Please try again.");
  }
  return result;
}

async function readResponse(response) {
  const result = await response.json().catch(() => null);
  if (!response.ok) {
    const fieldErrors = result?.fields ? Object.values(result.fields).join(" ") : "";
    throw new Error(fieldErrors || result?.message || "Something went wrong. Please try again.");
  }
  return result;
}

function renderRole(user) {
  state.role = user.role;
  elements.previewPanel.hidden = true;
  elements.connectionMessage.hidden = true;
  elements.forumWorkspace.hidden = false;
  elements.previewFooter.hidden = !state.previewMode;
  elements.roleBadge.textContent = state.previewMode
    ? `${user.role === "JUNIOR" ? "Junior" : "Senior"} preview`
    : user.role === "JUNIOR" ? "Junior Portal" : "Senior Portal";
  elements.roleLabel.textContent = state.previewMode ? "LOCAL FORUM PREVIEW" : "EDUPATH COMMUNITY";

  if (user.role === "JUNIOR") {
    elements.juniorWorkspace.hidden = false;
    elements.seniorWorkspace.hidden = true;
    elements.pageTitle.textContent = "Ask a question";
    elements.pageDescription.textContent = "Share your question with all EduPath Seniors.";
  } else if (user.role === "SENIOR") {
    elements.juniorWorkspace.hidden = true;
    elements.seniorWorkspace.hidden = false;
    elements.pageTitle.textContent = "Answer a question";
    elements.pageDescription.textContent = "See open questions from Juniors across EduPath.";
  } else {
    throw new Error("Open this forum using a Junior or Senior EduPath account.");
  }
}

function renderJuniorQuestions(questions) {
  if (questions.length === 0) {
    elements.juniorQuestionList.innerHTML = renderEmpty(
      "Your questions will appear here",
      "Post a question to get advice from EduPath Seniors."
    );
    return;
  }

  elements.juniorQuestionList.innerHTML = questions.map((question) => {
    const category = categoryNames[question.category] ?? question.category;
    const answered = question.status === "ANSWERED";
    const answer = question.answer
      ? `<div class="answer-panel">
          <h3>Your answer</h3>
          <p>${escapeHtml(question.answer.body)}</p>
          <span class="answer-byline">From ${escapeHtml(question.answer.authorName)}${question.answer.createdAt ? ` · ${escapeHtml(formatDate(question.answer.createdAt))}` : ""}</span>
        </div>`
      : "";

    return `<article class="question-card">
      <div class="question-card-heading">
        <h3>${escapeHtml(question.title)}</h3>
        <span class="status-badge ${answered ? "status-answered" : "status-open"}">${answered ? "Answered" : "Waiting for an answer"}</span>
      </div>
      <p class="question-body">${escapeHtml(question.body)}</p>
      <div class="question-meta">
        <span class="category-tag ${categoryClass(category)}">${escapeHtml(category)}</span>
        <span>${escapeHtml(formatDate(question.createdAt))}</span>
      </div>
      ${answer}
    </article>`;
  }).join("");
}

function renderSeniorQuestions(questions) {
  if (questions.length === 0) {
    elements.seniorQuestionList.innerHTML = renderEmpty(
      "No open questions right now",
      "New Junior questions will appear here automatically."
    );
    return;
  }

  const openForm = elements.seniorQuestionList.querySelector(".answer-form:not([hidden])");
  const answerHadFocus = openForm?.elements.answerBody === document.activeElement;
  const draft = openForm
    ? { questionId: openForm.dataset.questionId, answerBody: openForm.elements.answerBody.value }
    : null;

  elements.seniorQuestionList.innerHTML = questions.map((question) => {
    const category = categoryNames[question.category] ?? question.category;
    return `<article class="question-card senior-question-card" data-question-id="${question.id}">
      <div class="question-meta">
        <span class="category-tag ${categoryClass(category)}">${escapeHtml(category)}</span>
        <span>Asked by ${escapeHtml(question.authorName)}</span>
        <span>${escapeHtml(formatDate(question.createdAt))}</span>
      </div>
      <h3 class="senior-question-title">${escapeHtml(question.title)}</h3>
      <p class="question-body">${escapeHtml(question.body)}</p>
      <button class="button button-primary answer-toggle" type="button" data-action="toggle-answer">
        Write an answer
      </button>
      <form class="answer-form" data-question-id="${question.id}" hidden>
        <label class="field-label" for="answer-${question.id}">Your answer</label>
        <textarea class="text-input textarea-input" id="answer-${question.id}" name="answerBody"
                  maxlength="10000" rows="4" placeholder="Write a helpful answer..." required></textarea>
        <div class="answer-form-actions">
          <span>The first Senior answer completes this question.</span>
          <button class="button button-secondary" type="button" data-action="cancel-answer">Cancel</button>
          <button class="button button-primary" type="submit">
            <span class="button-label">Send answer</span><span aria-hidden="true">→</span>
          </button>
        </div>
      </form>
    </article>`;
  }).join("");

  if (draft) {
    const restoredForm = elements.seniorQuestionList.querySelector(
      `.answer-form[data-question-id="${CSS.escape(draft.questionId)}"]`
    );
    if (restoredForm) {
      restoredForm.hidden = false;
      restoredForm.elements.answerBody.value = draft.answerBody;
      const toggle = restoredForm.parentElement.querySelector('[data-action="toggle-answer"]');
      toggle.hidden = true;
      if (answerHadFocus) {
        restoredForm.elements.answerBody.focus({ preventScroll: true });
        restoredForm.elements.answerBody.setSelectionRange(
          draft.answerBody.length,
          draft.answerBody.length
        );
      }
    }
  }
}

async function loadQuestions() {
  if (!state.role || state.questionsLoading || document.hidden) return;
  state.questionsLoading = true;
  try {
    if (state.role === "JUNIOR") {
      const questions = await apiRequest("/api/junior/questions");
      renderJuniorQuestions(questions);
    } else {
      const questions = await apiRequest("/api/senior/questions");
      renderSeniorQuestions(questions);
    }
    showError("");
  } catch (error) {
    showError(error.message);
  } finally {
    state.questionsLoading = false;
  }
}

async function choosePreviewRole(role) {
  const buttons = document.querySelectorAll("[data-preview-role]");
  buttons.forEach((button) => {
    button.disabled = true;
  });
  elements.previewError.hidden = true;

  try {
    const response = await fetch("/api/dev/preview-role", {
      method: "POST",
      headers: {
        Accept: "application/json",
        "Content-Type": "application/json",
        "X-CSRF-TOKEN": state.csrfToken,
      },
      credentials: "same-origin",
      body: JSON.stringify({ role }),
    });
    const user = await readResponse(response);
    state.previewMode = true;
    renderRole(user);
    await loadQuestions();
  } catch (error) {
    elements.previewError.textContent = error.message;
    elements.previewError.hidden = false;
  } finally {
    buttons.forEach((button) => {
      button.disabled = false;
    });
  }
}

elements.questionBody.addEventListener("input", () => {
  elements.bodyCount.textContent = `${elements.questionBody.value.length} / 10000`;
});

elements.questionForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  showError("");
  showSuccess("");
  const button = elements.questionForm.querySelector('button[type="submit"]');
  const label = button.querySelector(".button-label");
  button.disabled = true;
  label.textContent = "Posting…";

  try {
    await apiRequest("/api/junior/questions", {
      method: "POST",
      body: JSON.stringify({
        title: elements.questionTitle.value,
        questionBody: elements.questionBody.value,
        category: elements.questionCategory.value,
      }),
    });
    elements.questionForm.reset();
    elements.bodyCount.textContent = "0 / 10000";
    await loadQuestions();
    showSuccess("Your question is now visible in the Senior answer queue.");
  } catch (error) {
    showError(error.message);
  } finally {
    button.disabled = false;
    label.textContent = "Post question";
  }
});

elements.seniorQuestionList.addEventListener("click", (event) => {
  const cancelButton = event.target.closest('[data-action="cancel-answer"]');
  if (cancelButton) {
    const form = cancelButton.closest(".answer-form");
    form.reset();
    form.hidden = true;
    form.parentElement.querySelector('[data-action="toggle-answer"]').hidden = false;
    return;
  }

  const button = event.target.closest('[data-action="toggle-answer"]');
  if (!button) return;
  const form = button.closest(".senior-question-card").querySelector(".answer-form");
  form.hidden = !form.hidden;
  button.hidden = !form.hidden;
  if (!form.hidden) form.querySelector("textarea").focus();
});

elements.seniorQuestionList.addEventListener("submit", async (event) => {
  const form = event.target.closest(".answer-form");
  if (!form) return;
  event.preventDefault();
  showError("");
  showSuccess("");
  const button = form.querySelector('button[type="submit"]');
  const label = button.querySelector(".button-label");
  button.disabled = true;
  label.textContent = "Sending…";

  try {
    await apiRequest(`/api/senior/questions/${form.dataset.questionId}/answers`, {
      method: "POST",
      body: JSON.stringify({ answerBody: form.elements.answerBody.value }),
    });
    await loadQuestions();
    showSuccess("Your answer was sent. This question is complete.");
  } catch (error) {
    showError(error.message);
    if (error.message.toLowerCase().includes("already answered")) await loadQuestions();
  } finally {
    if (form.isConnected) {
      button.disabled = false;
      label.textContent = "Send answer";
    }
  }
});

elements.refreshButton.addEventListener("click", loadQuestions);
elements.switchRoleButton.addEventListener("click", () => {
  state.role = "";
  state.previewMode = false;
  elements.forumWorkspace.hidden = true;
  elements.previewPanel.hidden = false;
  elements.pageError.hidden = true;
  elements.pageSuccess.hidden = true;
});

document.querySelectorAll("[data-preview-role]").forEach((button) => {
  button.addEventListener("click", () => choosePreviewRole(button.dataset.previewRole));
});

async function startForum() {
  try {
    const csrfResponse = await fetch("/api/csrf", { headers: { Accept: "application/json" } });
    const csrf = await readResponse(csrfResponse);
    state.csrfToken = csrf.token;

    const previewResponse = await fetch("/api/dev/preview-role", {
      headers: { Accept: "application/json" },
      credentials: "same-origin",
    });
    if (previewResponse.ok) {
      const preview = await previewResponse.json();
      if (!preview.role) {
        state.previewMode = true;
        elements.previewPanel.hidden = false;
        elements.refreshButton.hidden = true;
        return;
      }
      const user = await apiRequest("/api/me");
      state.previewMode = true;
      renderRole(user);
      await loadQuestions();
      return;
    }

    const user = await apiRequest("/api/me");
    state.previewMode = false;
    renderRole(user);
    await loadQuestions();
  } catch {
    elements.connectionMessage.hidden = false;
    elements.forumWorkspace.hidden = true;
    elements.previewPanel.hidden = true;
    elements.refreshButton.hidden = true;
  }
}

document.addEventListener("visibilitychange", () => {
  if (!document.hidden) loadQuestions();
});

window.setInterval(loadQuestions, 5000);
startForum();
