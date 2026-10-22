const registerForm = document.getElementById("register-form");
const regName = document.getElementById("reg-name");
const regEmail = document.getElementById("reg-email");
const regPhone = document.getElementById("reg-phone");
const regPassword = document.getElementById("reg-password");
const alertBox = document.getElementById("alert-message");

function setError(id, msg) { document.getElementById(id).textContent = msg; }
function clearErrors() { document.querySelectorAll(".error-text").forEach(s => s.textContent = ""); }
function showAlert(msg, type) { alertBox.className = `alert alert-${type}`; alertBox.textContent = msg; }

// Real-time character blocking
regName.addEventListener("input", (e) => {
  e.target.value = e.target.value.replace(/[^a-zA-Z\s]/g, "");
});
regPhone.addEventListener("input", (e) => {
  e.target.value = e.target.value.replace(/[^0-9]/g, "");
});

const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const PHONE_REGEX = /^[0-9]{10}$/;

registerForm.addEventListener("submit", async (e) => {
  e.preventDefault();
  clearErrors();

  let isValid = true;
  const nameVal = regName.value.trim();
  const emailVal = regEmail.value.trim();
  const phoneVal = regPhone.value.trim();
  const passwordVal = regPassword.value;
  const roleVal = document.querySelector('input[name="registerRole"]:checked')?.value || "Customer";

  if (!nameVal || !/^[a-zA-Z\s]+$/.test(nameVal)) {
    setError("reg-name-error", "Name must contain only alphabets and spaces.");
    isValid = false;
  }
  if (!emailVal || !EMAIL_REGEX.test(emailVal)) {
    setError("reg-email-error", "Valid email is required.");
    isValid = false;
  }
  if (!phoneVal || !PHONE_REGEX.test(phoneVal)) {
    setError("reg-phone-error", "Phone number must be exactly 10 digits.");
    isValid = false;
  }
  if (!passwordVal || passwordVal.length < 6) {
    setError("reg-password-error", "Password must be at least 6 characters.");
    isValid = false;
  }

  if (!isValid) return;

  // Prepare URL-encoded form parameters for RegisterServlet.java
  const params = new URLSearchParams({
    name: nameVal,
    email: emailVal,
    phone: phoneVal,
    password: passwordVal,
    role: roleVal
  });

  try {
    const res = await fetch("/Repair-Service-Management-System/api/register", {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: params
    });

    const data = await res.json();

    if (res.ok && data.success) {
      showAlert(data.message || "Registration successful! Redirecting to login...", "success");
      setTimeout(() => { window.location.href = "login.html"; }, 1500);
    } else {
      showAlert(data.message || "Registration failed.", "error");
    }
  } catch (err) {
    console.error("Registration request error:", err);
    showAlert("Server error. Please ensure Tomcat is running.", "error");
  }
});