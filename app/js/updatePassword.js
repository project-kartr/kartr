window.onload = init;

/*
  Handling user request and form-data about password reset and returning response to user.
*/

function init() {
  // Verifying user token from URL
  const urlParams = new URLSearchParams(window.location.search);
  const token = urlParams.get("token");
  // TODO: token validation
  const tokenArr = token.split(":");
  if (tokenArr.length<3){
    console.error("token is not valid");
  }

  let form = document.getElementById("updatePasswordForm");
  form.addEventListener("submit", function(e) {
    e.preventDefault();
    updatePassword(e, token);
  });
  
}

function updatePassword(e, token) {
  // Collecting form-data and token and making Post-Request to update the password
  let form = e.currentTarget;
  let formData = new FormData(form);
  formData.append("token", token)
  postAndExecute("public/update-password", formData, updatePasswordCallback);
}

function updatePasswordCallback(request) {
  const response = JSON.parse(request);
  if (response.status === "success") {
    document.getElementById("updatePasswordForm").remove();
    document.getElementById("responseSpan").innerHTML =
      "Dein Passwort wurde erfolgreich geändert!";
  } else {
    console.error(response);
    document.getElementById("responseSpan").innerHTML = "Error!";
  }
}
