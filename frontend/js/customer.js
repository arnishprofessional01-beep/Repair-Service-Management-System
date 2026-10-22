document.addEventListener("DOMContentLoaded", () => {
  // 1. Show user email in navigation
  const userDisplay = document.getElementById("user-display");
  const userEmail = sessionStorage.getItem("userEmail") || "customer@example.com";
  if (userDisplay) {
    userDisplay.textContent = userEmail;
  }

  // 2. Logout functionality
  const logoutBtn = document.getElementById("logout-btn");
  if (logoutBtn) {
    logoutBtn.addEventListener("click", () => {
      sessionStorage.clear();
      window.location.href = "../login.html";
    });
  }

  // 3. Initialize pages with live database calls
  initDashboardStats();
  initCreateRequestForm();
  initMyRequestsTable();
});

// --- HELPER: Fetch Requests from MySQL Backend ---
async function fetchUserRequests() {
  try {
    const userEmail = sessionStorage.getItem("userEmail") || "";
    const res = await fetch(`/Repair-Service-Management-System/api/requests?email=${encodeURIComponent(userEmail)}`);
    if (res.ok) {
      return await res.json();
    }
    return [];
  } catch (err) {
    console.error("Error fetching requests:", err);
    return [];
  }
}

// --- DASHBOARD METRICS ---
async function initDashboardStats() {
  const statTotal = document.getElementById("stat-total");
  if (!statTotal) return;

  const requests = await fetchUserRequests();
  const total = requests.length;
  const pending = requests.filter(r => r.status === "Pending").length;
  const progress = requests.filter(r => r.status === "In Progress" || r.status === "Assigned").length;
  const completed = requests.filter(r => r.status === "Completed").length;

  document.getElementById("stat-total").textContent = total;
  document.getElementById("stat-pending").textContent = pending;
  document.getElementById("stat-progress").textContent = progress;
  document.getElementById("stat-completed").textContent = completed;
}

// --- CREATE REQUEST FORM ---
function initCreateRequestForm() {
  const requestForm = document.getElementById("request-form");
  if (!requestForm) return;

  const dateInput = document.getElementById("preferred-date");
  // Restrict date picker to today or future dates
  const today = new Date().toISOString().split("T")[0];
  dateInput.setAttribute("min", today);

  requestForm.addEventListener("submit", async (e) => {
    e.preventDefault();
    clearErrors();

    const serviceType = document.getElementById("service-type").value;
    const deviceName = document.getElementById("device-name").value.trim();
    const problem = document.getElementById("problem-summary").value.trim();
    const description = document.getElementById("description").value.trim();
    let preferredDate = dateInput.value;
    const userEmail = sessionStorage.getItem("userEmail") || "roger@gmail.com";

    let isValid = true;
    if (!serviceType) {
      setError("service-type-error", "Please select a service type.");
      isValid = false;
    }
    if (!deviceName) {
      setError("device-name-error", "Device name is required.");
      isValid = false;
    }
    if (!problem) {
      setError("problem-summary-error", "Problem summary is required.");
      isValid = false;
    }
    if (!description) {
      setError("description-error", "Please provide a detailed description.");
      isValid = false;
    }
    if (!preferredDate) {
      setError("preferred-date-error", "Preferred date is required.");
      isValid = false;
    }

    if (!isValid) return;

    // Convert DD-MM-YYYY to standard SQL YYYY-MM-DD
    if (preferredDate.includes("-")) {
      const parts = preferredDate.split("-");
      if (parts[0].length === 2 && parts[2].length === 4) {
        preferredDate = `${parts[2]}-${parts[1]}-${parts[0]}`;
      }
    }

    // Send URL-encoded parameters to RepairRequestServlet.java including customerEmail
    const params = new URLSearchParams({
      serviceType: serviceType,
      deviceName: deviceName,
      problem: problem,
      description: description,
      preferredDate: preferredDate,
      customerEmail: userEmail
    });

    try {
      const res = await fetch("/Repair-Service-Management-System/api/requests", {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: params
      });

      const data = await res.json();
      if (res.ok && data.success) {
        showSuccess(data.requestId);
      } else {
        alert(data.message || "Failed to submit request.");
      }
    } catch (err) {
      console.error("Submission error:", err);
      alert("Server error. Check if Tomcat is running.");
    }
  });
}

function showSuccess(requestId) {
  const alertBox = document.getElementById("request-alert");
  if (alertBox) {
    alertBox.className = "alert alert-success";
    alertBox.textContent = `Request ${requestId} submitted successfully! Redirecting to My Requests...`;
    alertBox.classList.remove("hidden");
  }
  setTimeout(() => {
    window.location.href = "my-requests.html";
  }, 1500);
}

// --- MY REQUESTS TABLE ---
async function initMyRequestsTable() {
  const tbody = document.getElementById("requests-table-body");
  if (!tbody) return;

  const requests = await fetchUserRequests();
  const emptyMsg = document.getElementById("no-requests-msg");

  if (requests.length === 0) {
    if (emptyMsg) emptyMsg.classList.remove("hidden");
    tbody.innerHTML = "";
    return;
  }

  if (emptyMsg) emptyMsg.classList.add("hidden");
  tbody.innerHTML = "";

  requests.forEach((req) => {
    const tr = document.createElement("tr");

    // Map status to CSS badge class
    let badgeClass = "badge-pending";
    if (req.status === "Assigned") badgeClass = "badge-assigned";
    else if (req.status === "In Progress") badgeClass = "badge-in-progress";
    else if (req.status === "Completed") badgeClass = "badge-completed";

    tr.innerHTML = `
      <td><strong>${req.requestId}</strong></td>
      <td>${req.serviceType}</td>
      <td>${req.deviceName}</td>
      <td>${req.problem}</td>
      <td>${req.preferredDate}</td>
      <td>${req.technician || "Unassigned"}</td>
      <td><span class="badge ${badgeClass}">${req.status}</span></td>
      <td>
        <button type="button" style="background-color: #dc2626; color: white; border: none; border-radius: 4px; padding: 5px 12px; font-size: 12px; cursor: pointer;" onclick="deleteCustomerRequest('${req.requestId}')">Delete</button>
      </td>
    `;
    tbody.appendChild(tr);
  });
}

// --- CUSTOMER DELETE REQUEST HANDLER ---
window.deleteCustomerRequest = async function(requestId) {
  if (!confirm(`Are you sure you want to cancel/delete request ${requestId}?`)) return;

  try {
    const res = await fetch(`/Repair-Service-Management-System/api/requests?requestId=${encodeURIComponent(requestId)}`, {
      method: "DELETE"
    });
    const data = await res.json();
    if (res.ok && data.success) {
      alert(`Request ${requestId} deleted successfully!`);
      await initMyRequestsTable();
      await initDashboardStats();
    } else {
      alert("Failed to delete request from database.");
    }
  } catch (err) {
    console.error("Delete request error:", err);
    alert("Server error while deleting request.");
  }
};

// --- HELPER FUNCTIONS ---
function setError(id, msg) {
  const el = document.getElementById(id);
  if (el) el.textContent = msg;
}

function clearErrors() {
  document.querySelectorAll(".error-text").forEach(span => span.textContent = "");
}