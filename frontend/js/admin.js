document.addEventListener("DOMContentLoaded", () => {
  // Display Admin Identifier
  const userDisplay = document.getElementById("admin-user-display");
  const userEmail = sessionStorage.getItem("userEmail") || "admin@repair.com";
  if (userDisplay) {
    userDisplay.textContent = userEmail;
  }

  // Logout Handler
  const logoutBtn = document.getElementById("admin-logout-btn");
  if (logoutBtn) {
    logoutBtn.addEventListener("click", () => {
      sessionStorage.clear();
      window.location.href = "../login.html";
    });
  }

  // Page Initializers
  initAdminDashboard();
  initAdminRequestsPage();
  initTechniciansPage();
});

// ==========================================
// API DATA HELPERS (MySQL Backend Integration)
// ==========================================
async function fetchAllRequests() {
  try {
    const res = await fetch("/Repair-Service-Management-System/api/requests");
    if (res.ok) {
      return await res.json();
    }
    return [];
  } catch (err) {
    console.error("Error fetching requests:", err);
    return [];
  }
}

async function fetchAllTechnicians() {
  try {
    const res = await fetch("/Repair-Service-Management-System/api/technicians");
    if (res.ok) {
      return await res.json();
    }
    return [];
  } catch (err) {
    console.error("Error fetching technicians:", err);
    return [];
  }
}

// ==========================================
// 1. ADMIN DASHBOARD METRICS
// ==========================================
async function initAdminDashboard() {
  const totalElem = document.getElementById("admin-stat-total");
  if (!totalElem) return;

  const [requests, techs] = await Promise.all([fetchAllRequests(), fetchAllTechnicians()]);

  const total = requests.length;
  const pending = requests.filter(r => r.status === "Pending").length;
  const progress = requests.filter(r => r.status === "In Progress" || r.status === "Assigned").length;
  const completed = requests.filter(r => r.status === "Completed").length;

  document.getElementById("admin-stat-total").textContent = total;
  document.getElementById("admin-stat-pending").textContent = pending;
  document.getElementById("admin-stat-progress").textContent = progress;
  document.getElementById("admin-stat-completed").textContent = completed;
  document.getElementById("admin-stat-techs").textContent = techs.length;
}

// ==========================================
// 2. ADMIN REQUESTS MANAGEMENT (With Delete)
// ==========================================
let globalRequests = [];
let globalTechnicians = [];

async function initAdminRequestsPage() {
  const tbody = document.getElementById("admin-requests-tbody");
  if (!tbody) return;

  await renderAdminRequests();
}

async function renderAdminRequests() {
  const tbody = document.getElementById("admin-requests-tbody");
  const emptyMsg = document.getElementById("no-admin-requests");
  if (!tbody) return;

  const [requests, technicians] = await Promise.all([fetchAllRequests(), fetchAllTechnicians()]);
  globalRequests = requests;
  globalTechnicians = technicians;

  if (requests.length === 0) {
    tbody.innerHTML = "";
    if (emptyMsg) emptyMsg.classList.remove("hidden");
    return;
  }

  if (emptyMsg) emptyMsg.classList.add("hidden");
  tbody.innerHTML = "";

  requests.forEach((req, index) => {
    const tr = document.createElement("tr");

    // Technician options dropdown
    let techOptions = `<option value="">-- Unassigned --</option>`;
    technicians.forEach(t => {
      const selected = req.technicianId == t.technicianId ? "selected" : "";
      techOptions += `<option value="${t.technicianId}" ${selected}>${t.name} (${t.specialization})</option>`;
    });

    // Status options dropdown
    const statuses = ["Pending", "Assigned", "In Progress", "Completed"];
    let statusOptions = "";
    statuses.forEach(st => {
      const selected = req.status === st ? "selected" : "";
      statusOptions += `<option value="${st}" ${selected}>${st}</option>`;
    });

    tr.innerHTML = `
      <td><strong>${req.requestId}</strong></td>
      <td>${req.userEmail || "Customer"}</td>
      <td>${req.serviceType}</td>
      <td>${req.deviceName}</td>
      <td>${req.problem}</td>
      <td>${req.preferredDate}</td>
      <td>
        <select class="admin-select" id="tech-select-${index}">
          ${techOptions}
        </select>
      </td>
      <td>
        <select class="admin-select" id="status-select-${index}">
          ${statusOptions}
        </select>
      </td>
      <td style="display: flex; gap: 8px;">
        <button class="btn-primary" style="padding: 6px 12px; font-size: 12px;" onclick="saveRequestRow(${index})">Save</button>
        <button class="btn-secondary" style="color: #dc2626; border-color: #fca5a5; padding: 6px 10px; font-size: 12px;" onclick="deleteRequest('${req.requestId}')">Delete</button>
      </td>
    `;
    tbody.appendChild(tr);
  });
}

window.saveRequestRow = async function(index) {
  const targetReq = globalRequests[index];
  const selectedTechId = document.getElementById(`tech-select-${index}`).value;
  let selectedStatus = document.getElementById(`status-select-${index}`).value;

  if (selectedTechId && selectedStatus === "Pending") {
    selectedStatus = "Assigned";
  }

  const params = new URLSearchParams({
    requestId: targetReq.requestId,
    technicianId: selectedTechId,
    status: selectedStatus
  });

  try {
    const res = await fetch("/Repair-Service-Management-System/api/status", {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: params
    });

    const data = await res.json();
    if (res.ok && data.success) {
      showAdminAlert(`Updated ticket ${targetReq.requestId} successfully!`, "success");
      await renderAdminRequests();
    } else {
      showAdminAlert("Failed to update status in database.", "error");
    }
  } catch (err) {
    console.error("Status update error:", err);
    showAdminAlert("Server error while updating request.", "error");
  }
};

window.deleteRequest = async function(requestId) {
  if (!confirm(`Are you sure you want to delete request ${requestId}?`)) return;

  try {
    const res = await fetch(`/Repair-Service-Management-System/api/requests?requestId=${encodeURIComponent(requestId)}`, {
      method: "DELETE"
    });
    const data = await res.json();
    if (res.ok && data.success) {
      showAdminAlert(`Request ${requestId} deleted successfully!`, "success");
      await renderAdminRequests();
    } else {
      showAdminAlert("Failed to delete request.", "error");
    }
  } catch (err) {
    console.error("Delete request error:", err);
    showAdminAlert("Server error while deleting request.", "error");
  }
};

function showAdminAlert(msg, type) {
  const alertBox = document.getElementById("admin-alert");
  if (!alertBox) return;
  alertBox.className = `alert alert-${type}`;
  alertBox.textContent = msg;
  alertBox.classList.remove("hidden");
  setTimeout(() => alertBox.classList.add("hidden"), 3000);
}

// ==========================================
// 3. TECHNICIAN MANAGEMENT (With Delete)
// ==========================================
function initTechniciansPage() {
  const techTableBody = document.getElementById("tech-table-tbody");
  const form = document.getElementById("add-tech-form");
  if (!techTableBody) return;

  renderTechniciansTable();

  if (form) {
    form.addEventListener("submit", async (e) => {
      e.preventDefault();
      clearTechErrors();

      const nameInput = document.getElementById("tech-name");
      const phoneInput = document.getElementById("tech-phone");
      const specInput = document.getElementById("tech-specialization");

      const nameVal = nameInput.value.trim();
      const phoneVal = phoneInput.value.trim();
      const specVal = specInput.value;

      const NAME_REGEX = /^[A-Za-z\s]+$/;
      const PHONE_REGEX = /^[0-9]{10}$/;

      let isValid = true;

      if (!nameVal) {
        setTechError("tech-name-error", "Technician name is required.");
        isValid = false;
      } else if (!NAME_REGEX.test(nameVal)) {
        setTechError("tech-name-error", "Name must contain only alphabets.");
        isValid = false;
      }

      if (!phoneVal) {
        setTechError("tech-phone-error", "Phone number is required.");
        isValid = false;
      } else if (!PHONE_REGEX.test(phoneVal)) {
        setTechError("tech-phone-error", "Phone must be exactly 10 digits.");
        isValid = false;
      }

      if (!specVal) {
        setTechError("tech-spec-error", "Please select a specialization.");
        isValid = false;
      }

      if (!isValid) return;

      const params = new URLSearchParams({
        name: nameVal,
        phone: phoneVal,
        specialization: specVal
      });

      try {
        const res = await fetch("/Repair-Service-Management-System/api/technicians", {
          method: "POST",
          headers: { "Content-Type": "application/x-www-form-urlencoded" },
          body: params
        });

        const data = await res.json();
        if (res.ok && data.success) {
          form.reset();
          await renderTechniciansTable();

          const alertBox = document.getElementById("tech-alert");
          if (alertBox) {
            alertBox.className = "alert alert-success";
            alertBox.textContent = `Technician ${nameVal} registered successfully!`;
            alertBox.classList.remove("hidden");
            setTimeout(() => alertBox.classList.add("hidden"), 3000);
          }
        } else {
          setTechError("tech-name-error", "Failed to add technician to database.");
        }
      } catch (err) {
        console.error("Add technician error:", err);
      }
    });
  }
}

async function renderTechniciansTable() {
  const tbody = document.getElementById("tech-table-tbody");
  const emptyMsg = document.getElementById("no-techs-msg");
  if (!tbody) return;

  const techs = await fetchAllTechnicians();

  if (techs.length === 0) {
    tbody.innerHTML = "";
    if (emptyMsg) emptyMsg.classList.remove("hidden");
    return;
  }

  if (emptyMsg) emptyMsg.classList.add("hidden");
  tbody.innerHTML = "";

  techs.forEach((t) => {
    const tr = document.createElement("tr");
    tr.innerHTML = `
      <td><strong>T${t.technicianId}</strong></td>
      <td>${t.name}</td>
      <td>${t.phone}</td>
      <td>${t.specialization}</td>
      <td>
        <button class="btn-secondary" style="color: #dc2626; border-color: #fca5a5; padding: 4px 10px; font-size: 12px;" onclick="deleteTechnician(${t.technicianId}, '${t.name}')">Delete</button>
      </td>
    `;
    tbody.appendChild(tr);
  });
}

window.deleteTechnician = async function(techId, techName) {
  if (!confirm(`Are you sure you want to delete technician ${techName}?`)) return;

  try {
    const res = await fetch(`/Repair-Service-Management-System/api/technicians?technicianId=${techId}`, {
      method: "DELETE"
    });
    const data = await res.json();
    if (res.ok && data.success) {
      await renderTechniciansTable();
      const alertBox = document.getElementById("tech-alert");
      if (alertBox) {
        alertBox.className = "alert alert-error";
        alertBox.textContent = `Technician ${techName} removed successfully!`;
        alertBox.classList.remove("hidden");
        setTimeout(() => alertBox.classList.add("hidden"), 3000);
      }
    } else {
      alert("Failed to delete technician from database.");
    }
  } catch (err) {
    console.error("Delete technician error:", err);
  }
};

function setTechError(id, msg) {
  const elem = document.getElementById(id);
  if (elem) elem.textContent = msg;
}

function clearTechErrors() {
  document.querySelectorAll(".error-text").forEach(s => s.textContent = "");
}