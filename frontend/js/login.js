const loginForm = document.getElementById("login-form");
const loginEmail = document.getElementById("login-email");
const loginPassword = document.getElementById("login-password");
const alertBox = document.getElementById("alert-message");

function setError(id, msg) { 
  const el = document.getElementById(id);
  if (el) el.textContent = msg; 
}

function clearErrors() { 
  document.querySelectorAll(".error-text").forEach(s => s.textContent = ""); 
}

function showAlert(msg, type) { 
  if (alertBox) {
    alertBox.className = `alert alert-${type}`; 
    alertBox.textContent = msg;
    alertBox.classList.remove("hidden");
  } else {
    alert(msg);
  }
}

const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

loginForm.addEventListener("submit", async (e) => {
  e.preventDefault();
  clearErrors();

  let isValid = true;
  const emailVal = loginEmail.value.trim();
  const passwordVal = loginPassword.value;
  const selectedRole = document.querySelector('input[name="loginRole"]:checked')?.value || "customer";

  // Validate Email
  if (!emailVal) {
    setError("login-email-error", "Email is required.");
    isValid = false;
  } else if (!EMAIL_REGEX.test(emailVal)) {
    setError("login-email-error", "Invalid email format.");
    isValid = false;
  }

  // Validate Password
  if (!passwordVal) {
    setError("login-password-error", "Password is required.");
    isValid = false;
  } else if (passwordVal.length < 6) {
    setError("login-password-error", "Password must be at least 6 characters.");
    isValid = false;
  }

  if (!isValid) return;

  // Send credentials to LoginServlet
  const params = new URLSearchParams({
    email: emailVal,
    password: passwordVal
  });

  try {
    const res = await fetch("/Repair-Service-Management-System/api/login", {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: params
    });

    const data = await res.json();

    if (res.ok && data.success) {
      // Normalize role to lowercase for auth guards across all pages
      const activeRole = (data.role || selectedRole).toLowerCase();

      // Verify that selected login role matches database account role
      if (activeRole !== selectedRole.toLowerCase()) {
        showAlert(`Unauthorized. This account is registered as ${data.role}.`, "error");
        return;
      }

      sessionStorage.setItem("userRole", activeRole);
      sessionStorage.setItem("userEmail", emailVal);

      showAlert("Login successful! Redirecting...", "success");

      // Redirect relative to current frontend directory
      setTimeout(() => {
        if (activeRole === "admin") {
          window.location.assign("admin/dashboard.html");
        } else {
          window.location.assign("customer/dashboard.html");
        }
      }, 800);
    } else {
      showAlert(data.message || "Invalid credentials.", "error");
    }
  } catch (err) {
    console.error("Login request error:", err);
    showAlert("Server connection failed. Ensure Apache Tomcat is running.", "error");
  }
});