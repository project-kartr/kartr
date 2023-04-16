//Modal 
// - HTML code that gets injected into the index.html via Template-Literals
// - Listens for button-click-events via eventListener. Uses querySelector as a referenc for the buttons

// opens login popup with event listener for "login", "sign up" and "reset password"
function openLoginModal (e) {
  document.getElementById("popup-inject").innerHTML = "";
  let popupContent = document.createElement("div");
  popupContent.id = "popupContent";
  popupContent.innerHTML = loginModalTemplate();
  for (let eventtarget of popupContent.querySelectorAll("[data-eventtarget='closePopup']")) {
    eventtarget.addEventListener("click", function () {popupContent.remove()});
  }
  popupContent.querySelector("[data-eventtarget='login']").addEventListener("submit", loginFunction);
  popupContent.querySelector("[data-eventtarget='openSignup']").addEventListener("click", openSignUpModal);
  popupContent.querySelector("[data-eventtarget='openResetPassword']").addEventListener("click", openResetPassword);
  document.getElementById("popup-inject").appendChild(popupContent);
}

// opens popup with "display name" and a button to logout
function openHomeModal(e) {
  document.getElementById("popup-inject").innerHTML = "";
  let popupContent = document.createElement("div");
  popupContent.id = "popupContent";
  popupContent.innerHTML = homeModalTemplate();
  for (let eventtarget of popupContent.querySelectorAll("[data-eventtarget='closePopup']")) {
    eventtarget.addEventListener("click", function () {popupContent.remove()});
  }
  popupContent.querySelector("[data-eventtarget='logout']").addEventListener("click", logoutFunction);
  document.getElementById("popup-inject").appendChild(popupContent); 
}
//opens popup with the signup modal
function openSignUpModal(e) {
    document.getElementById("popup-inject").innerHTML = "";
    let popupContent = document.createElement("div");
    popupContent.id = "popupContent";
    popupContent.innerHTML = signupModalTemplate();
    for (let eventtarget of popupContent.querySelectorAll("[data-eventtarget='closePopup']")) {
      eventtarget.addEventListener("click", function () {popupContent.remove()});
    }
    let backBtn = popupContent.querySelector("[data-eventtarget='back']");
    backBtn.addEventListener("click", function (e) {
      openLoginModal(e); 
    })
    popupContent.querySelector("[data-eventtarget='signup']").addEventListener("submit", signupFunction);
    popupContent.querySelector("[data-eventtarget='confirmPassword']").onpaste = e => e.preventDefault(); 
    document.getElementById("popup-inject").appendChild(popupContent);
} 
// opens popup the passwort reset function
function openResetPassword(e) {
    document.getElementById("popup-inject").innerHTML = "";
    let popupContent = document.createElement("div");
    popupContent.id = "popupContent";
    popupContent.innerHTML = resetPasswordRequestTemplate();
    for (let eventtarget of popupContent.querySelectorAll("[data-eventtarget='closePopup']")) {
      eventtarget.addEventListener("click", function () {popupContent.remove()});
    }
    let backBtn = popupContent.querySelector("[data-eventtarget='back']");
    backBtn.addEventListener("click", function (e) {
      openLoginModal(e); 
    })
    popupContent.querySelector("[data-eventtarget='resetPasswordRequest']").addEventListener("submit", resetPasswordRequestFunction);
    document.getElementById("popup-inject").appendChild(popupContent); 
}


function loginFunction(e) {
  e.preventDefault();
  if (localStorage.getItem("cookie-state") != "accepted") {
    document.getElementById("responseSpan").innerHTML = "Bitte akzeptieren Sie die Cookies!";
    document.getElementById("responseSpan").classList.remove("hidden");
  }
  else {
    let form = this;
    let formData = new FormData(form);
    postAndExecute("auth/login", formData, loginCallback);
  }
}

function loginCallback(request) {
  let jsonObj = JSON.parse(request);
  if (jsonObj.status === "success") {
    let displayName = jsonObj.displayName;
    localStorage.setItem("account_displayname", jsonObj.displayName);
    document.getElementById("popupContent").remove();
    let sidebarBtn = document.getElementById("sidebar-hamburger-button");
    sidebarBtn.innerHTML=displayName.substring(0,1).toUpperCase();
    sidebarBtn.removeEventListener("click", openLoginModal);
    sidebarBtn.addEventListener("click", openHomeModal);
    document.getElementById("sidebar-add-button").classList.remove("hidden"); 
  } else {
    document.getElementById("responseSpan").innerHTML = "Email oder Passwort ist falsch!";
    document.getElementById("responseSpan").classList.remove("hidden");
  }
}

function logoutFunction() {
  localStorage.removeItem("account_displayname");
  postAndExecute("api/logout", new FormData(), logoutCallback);
}

function logoutCallback(request){
  let jsonObj = JSON.parse(request);
  if (jsonObj.status === "success") {
    document.getElementById("popupContent").remove();
    let sidebarBtn = document.getElementById("sidebar-hamburger-button");
    sidebarBtn.innerHTML=`
      <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="icon">
        <polyline points="5,5 19,5"/>
        <polyline points="5,12, 19,12"/>
        <polyline points="5,19 19,19"/> 
      </svg>
    `;
    sidebarBtn.removeEventListener("click", openHomeModal);
    sidebarBtn.addEventListener("click", openLoginModal);
    document.getElementById("sidebar-add-button").classList.add("hidden");
  } 
}

function signupFunction(e) {
  e.preventDefault();
  let form = this;
  let formData = new FormData(form);

  if(formData.get("confirmPassword") !== formData.get("password")) {
    document.getElementById("responseSpan").innerHTML = "Passwörter stimmen nicht überein!";
    document.getElementById("responseSpan").classList.remove("hidden");
  } else {
    postAndExecute("public/register", formData, signUpCallback);
  }
}

function signUpCallback(request){
  const response = JSON.parse(request);
  if (response.status === "success") {
    document.getElementById("signUp-form").remove();
    // TODO: change color to green
    document.getElementById("responseSpan").innerHTML = "Danke für Ihre Registrierung!.\n Wir haben Ihnen eine E-Mail zum Bestätigen des Accounts geschickt.";
    document.getElementById("responseSpan").classList.remove("hidden");
  }
  else if(response.description.includes("valid email")){
    //TODO: Handle Error & display description!
    document.getElementById("responseSpan").innerHTML = "Die angegebene E-Mail-Adresse ist ungültig!";
    document.getElementById("responseSpan").classList.remove("hidden");
  }
  else {
    document.getElementById("signupspan").innerHTML = "Die Registrierung ist fehlgeschlagen,\n bitte versuchen Sie es erneut.";
    document.getElementById("responseSpan").classList.remove("hidden");
  }
}

function resetPasswordRequestFunction(e){
  e.preventDefault();
  let form = this;
  let formData = new FormData(form);
  postAndExecute("public/reset-password-request", formData, resetPasswordRequestCallback);
}

function resetPasswordRequestCallback(request){
  const response = JSON.parse(request);
  if (response.status === "success") {
    document.getElementById("resetPasswordRequest-form").remove();
    // TODO: change color to green
    document.getElementById("responseSpan").innerHTML = "Wir haben dir eine E-Mail geschickt!";
    document.getElementById("responseSpan").classList.remove("hidden");
  } else {
    document.getElementById("responseSpan").innerHTML = "Error!";
    document.getElementById("responseSpan").classList.remove("hidden");
  }
}

function handleValidateSessionResponse(resp) {
  let jsonObj = JSON.parse(resp);
  let sidebarBtn = document.getElementById("sidebar-hamburger-button");
  let displayName = jsonObj.displayName;
  if (jsonObj.status === "failed") {
    sidebarBtn.innerHTML=`
      <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="icon">
        <polyline points="5,5 19,5"/>
        <polyline points="5,12, 19,12"/>
        <polyline points="5,19 19,19"/> 
      </svg>
    `;
    sidebarBtn.addEventListener("click", openLoginModal);
    document.getElementById("sidebar-add-button").classList.add("hidden");
  } else if (jsonObj.status === "success" ){
    sidebarBtn.innerHTML=displayName.substring(0,1).toUpperCase();
    sidebarBtn.addEventListener("click", openHomeModal);
    document.getElementById("sidebar-add-button").classList.remove("hidden");
  }
}


function loginModalTemplate(options) {
  return `
    <div class="popup-background full-size">
      <div class="popup">
        <div class="popup-container full-size">
          <header class="popup-header">
            <div class="layout-space-between full-size">
              <button data-eventtarget="closePopup" class="button-transparent">
                <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="icon">
                  <polyline points="12.5,19 5,12.5 12.5,5"/>
                </svg>
              </button>
              <h2 class="text-margin-0">Konto</h2>
              <button data-eventtarget="closePopup" class="button-transparent">
                <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="icon">
                  <polyline points="5,5 19,19"/>
                  <polyline points="19,5 5,19"/> 
                </svg>
              </button>
            </div>
          </header>
          <main class="popup-main">
            <form id="login-form" data-eventtarget="login" class="popup-container">
              <div class="layout-inline">
                <p class="label">Noch kein Konto?</p>
                <a class="link" data-eventtarget="openSignup">Konto erstellen</a>
              </div>
              <p class="warning-card hidden" id="responseSpan"></p>   
              <label for="email" class="label">E-Mail</label>
              <input name="email" type="text" placeholder="E-Mail" class="input">
              <label for="password" class="label">Passwort</label>
              <input name="password" type="password" placeholder="Passwort" class="input">
              <a class="link" data-eventtarget="openResetPassword">Passwort vergessen</a>
              <input type="submit" name="submit" value="Anmelden" class="button">
            </form>
          </main>
        </div>
      </div>
    </div>`;
}


function homeModalTemplate(options) {
  return `
    <div class="popup-background full-size">
      <div class="popup">
        <div class="popup-container full-size">
          <header class="popup-header">
            <div class="layout-space-between full-size">
              <button data-eventtarget="closePopup" class="button-transparent">
                <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="icon">
                  <polyline points="12.5,19 5,12.5 12.5,5"/>
                </svg>
              </button>
              <h2 class="text-margin-0">Konto</h2>
              <button data-eventtarget="closePopup" class="button-transparent">
                <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="icon">
                  <polyline points="5,5 19,19"/>
                  <polyline points="19,5 5,19"/> 
                </svg>
              </button>
            </div>
          </header>
          <main class="popup-main">
            <p class="warning-card hidden" id="responseSpan"></p>
            <div class="w-100 layout-inline">
              <p class="label">Sie sind angemeldet als: </p>
              <p class="label-normal text-cut">${localStorage.getItem("account_displayname")}</p>
            </div>
            <button id="logout" data-eventtarget="logout" class="button">Abmelden</button>
          </main>
        </div>
      </div>
    </div>`;
}


function signupModalTemplate(options) {
return `
  <div class="popup-background full-size">
    <div class="popup">
      <div class="popup-container full-size">
        <header class="popup-header">
          <div class="layout-space-between full-size">
            <button data-eventtarget="back" class="button-transparent">
              <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="icon">
                <polyline points="12.5,19 5,12.5 12.5,5"/>
              </svg>
            </button>
            <h2 class="text-margin-0">Konto erstellen</h2>
            <button data-eventtarget="closePopup" class="button-transparent">
              <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="icon">
                <polyline points="5,5 19,19"/>
                <polyline points="19,5 5,19"/> 
              </svg>
            </button>
          </div>
        </header>
        <main class="popup-main">
          <p class="warning-card hidden" id="responseSpan"></p>
          <form id="signUp-form" data-eventtarget="signup" class="popup-container">
            <label for="displayname" class="label">Anzeigename</label>
            <input name="displayname" type="text" required placeholder="Anzeigename" class="input" />
            <label for="email" class="label">E-Mail</label>
            <input name="email" type="text" placeholder="E-Mail" required autocomplete="off" class="input" />
            <label for="password" class="label">Passwort</label>
            <input name="password" type="password" required placeholder="Passwort" autocomplete="off" class="input" />
            <label for="confirmPassword" class="label">Passwort wiederholen</label>
            <input data-eventtarget="confirmPassword" name="confirmPassword" type="password" required placeholder="Password-Wiederholung" autocomplete="off" class="input" />
            <input type="submit" name="submit" value="Konto erstellen" class="button">
          </form>
        </main>
      </div>
    </div>
  </div>`;
}

function resetPasswordRequestTemplate(options) {
return `
  <div class="popup-background full-size">
    <div class="popup">
      <div class="popup-container full-size">
        <header class="popup-header">
          <div class="layout-space-between full-size">
            <button data-eventtarget="back" class="button-transparent">
              <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="icon">
                <polyline points="12.5,19 5,12.5 12.5,5"/>
              </svg>
            </button>
            <h2 class="text-margin-0">Passwort zurücksetzen</h2>
            <button data-eventtarget="closePopup" class="button-transparent">
              <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="icon">
                <polyline points="5,5 19,19"/>
                <polyline points="19,5 5,19"/> 
              </svg>
            </button>
          </div>
        </header>
        <main class="popup-main">
          <p class="warning-card hidden" id="responseSpan"></p>
          <form id="resetPasswordRequest-form" data-eventtarget="resetPasswordRequest" class="popup-container">
            <label for="email" class="label">E-Mail</label>
            <input name="email" type="text" placeholder="E-Mail" required autocomplete="off" class="input" />
            <input type="submit" name="submit" value="Passwort zurücksetzen" class="button">
          </form>
        </main>
      </div>
    </div>
  </div>`;
 }
