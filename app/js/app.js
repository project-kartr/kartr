//INIT 
// - used to load the map, markers and the Service-Worker
//Modal 
// - HTML code that gets injected into the index.html via Template-Literals
// - Listens for button-click-events via eventListener. Uses querySelector as a referenc for the buttons
//POI / Story-Upload
// - State-Patern
//  - calls the API sequential during Story/POI-Upload
//  - Only when all files are uploaded does the state get changed
//  - end point dependecies can be worked on in sequential order
window.onload = init;


let map;
let markerGroup;
let isMarkerMode;

class PopupCreateState {
  state;
  formData;
  poiId;
  fileId = [];
  static States = {
    INIT: "INIT",
    UPLOADPOI: "UPLOADPOI",
    UPLOADFILES: "UPLOADFILES",
    UPLOADSTORY: "UPLOADSTORY",
    DONE: "DONE",
    FAILED: "FAILED",
  };
  onDone;

  constructor(formData, onDone) {
    this.state = PopupCreateState.States.INIT;
    this.formData = formData;
    this.poiId = formData.get("poi_id");
    this.onDone = onDone;
  }

  uploadPoi() {
    this.state = PopupCreateState.States.UPLOADPOI;
    const poiData = new FormData();
    poiData.append("longitude", this.formData.get("longitude"));
    poiData.append("latitude", this.formData.get("latitude"));
    poiData.append("displayname", this.formData.get("displayname"));
    postAndExecute("api/poi-upload", poiData, (response) => {
      let responseJson = JSON.parse(response);
      if (responseJson.status === "success") {
        this.poiId = responseJson.poi_id;
        this.uploadFile();
      } else {
        snackbarMessage("POI konnte nicht hochgeladen werden", 1);
        this.fail();
      }
    });
  }

  uploadFile() {
    this.state = PopupCreateState.States.UPLOADFILES;
    this.fileId = this.formData.get("files")? this.formData.get("files").split(","): [];
    let files = this.formData.getAll("images");
    let uploadedFiles = 0;
    for (let i = 0; i < files.length; ++i) {
      if (files[i].name === '') {
        this.deleteFiles();
        break;
      }
      let fileData = new FormData();
      fileData.append("thefile", files[i]);
      postAndExecute("api/file-upload", fileData, (response) => {
        let responseJson = JSON.parse(response);
        if (responseJson.status === "success") {
          this.fileId[this.fileId.length] = responseJson.file_id;
          if (++uploadedFiles === files.length) {
            this.deleteFiles();
          }
        } else {
          snackbarMessage("Bilder konnten nicht hochgeladen werden", 1);
          this.fail();
        }
      });
    }
  }

  deleteFiles() {
    let filesToDelete = this.formData.get("files-to-delete");
    if (filesToDelete) {
      filesToDelete = filesToDelete.split(", ");
      let deletedFiles = 0;
      for (let i = 0; i < filesToDelete.length; ++i) {
        if (filesToDelete[i].name === '') {
          this.uploadStory();
          break;
        }
        const fileIndex = this.fileId.indexOf(filesToDelete[i]);
        if (fileIndex > -1) {
          this.fileId.splice(fileIndex, 1);
        }
        let fileData = new FormData();
        fileData.append("filename", filesToDelete[i]);
        postAndExecute("api/file-delete", fileData, (response) => {
          let responseJson = JSON.parse(response);
          if (responseJson.status === "success") {
            if (++deletedFiles === filesToDelete.length) {
              this.uploadStory();
            }
          } else {
            snackbarMessage("Bilder konnten nicht gelöscht werden", 1);
            this.fail();
          }
        });
      }
    }
    else {
      this.uploadStory();
    }
  }

  uploadStory() {
    this.state = PopupCreateState.States.UPLOADSTORY;
    let storyData = new FormData();
    storyData.append("poi_id", (this.formData.has("poi_id")? this.formData.get("poi_id"): this.poiId));
    if (this.formData.has("story_id")) {
      storyData.append("story_id", this.formData.get("story_id"));
    }
    storyData.append("headline", this.formData.get("headline"));
    storyData.append("content", this.formData.get("content"));
    if (this.fileId.length > 0) {
      storyData.append("files", this.fileId);
    }
    postAndExecute("api/story-upload", storyData, (response) => {
      let responseJson = JSON.parse(response);
      if (responseJson.status === "success") {
        let story_id = responseJson.story_id;
        getAndExecute("public/get-story-by-id?story_id=" + story_id, (response) => {
          let story = JSON.parse(response);
          if(story.status === "success") {
            let stories = (this.formData.get("stories") === null? []: this.formData.get("stories").split(","));
            let isEdited = false;
            for (let i = 0; i < stories.length; ++i) {
              if (stories[i] == story_id) {
                isEdited = true;
              }
            }
            if(!isEdited) {
              stories[stories.length] = story_id;
            }
            let poi = {
              poi_id: this.poiId,
              displayname: this.formData.get("displayname"),
              stories: stories
            }
            openStoryViewModal(story, poi);
          } else {
            snackbarMessage("Story konnte nicht hochgeladen werden", 1);
          }
	})
        this.done();
      } else {
        snackbarMessage("Story konnte nicht hochgeladen werden", 1);
        this.fail();
      }
    });
  } 

  done() {
    this.state = PopupCreateState.States.DONE;
    this.onDone(); 
  }

  fail() {
    this.state = PopupCreateState.States.FAILED;
  }
}

function snackbarMessage(msg, status) {
  hideLoader();
  let snkbar = document.getElementById("snackbar");
  snkbar.innerHTML = msg;
  if (status === 1) {
    snkbar.style.backgroundColor = "#9C241F";
  } else {
    snkbar.style.backgroundColor = "#184F04";
  }
  snkbar.className = "show";
  setTimeout(function(){ 
    snkbar.className = snkbar.className.replace("show", ""); 
  }, 3000);
}

function init() {
  map = L.map("map").setView({ lon: 8.5835, lat: 53.54 }, 17);
  L.tileLayer("https://example.org/map/{z}/{x}/{y}.png", {
    detectRetina: true,
    maxZoom: 19,
    attribution:
      '&copy; <a href="https://openstreetmap.org/copyright">OpenStreetMap contributors</a>',
  }).addTo(map);

  L.control.scale({ imperial: true, metric: true }).addTo(map);

  map.on("click", openPoiCreateModal);

  markerGroup = L.layerGroup().addTo(map);

  getAndExecute("public/get-list-of-pois", injectMarker);
  registerServiceWorker();
  document.getElementById("sidebar-add-button").addEventListener("click", toggleMarkerMode);
  document.getElementById("sidebar-help-button").addEventListener("click", openHelpModal);
  postAndExecute("auth/status", new FormData(), handleValidateSessionResponse);
  
  if (localStorage.getItem("cookie-state") != "accepted") {
    showCookieBanner();
  }
}

function reloadMarker() {
  //Löschen der alten Marker
  markerGroup.clearLayers(); 
  //Laden der Marker
  getAndExecute("public/get-list-of-pois", injectMarker);
}

function injectMarker(response) {
  const responseJSON = JSON.parse(response);
  if (responseJSON.status == "success"){
    const pois = responseJSON.pois;
    for (let i = 0; i < pois.length; ++i) {
      createPoiMarker(pois[i]);
    }
  }
}

function createPoiMarker(poi) {
  let marker = new L.Marker([poi.latitude, poi.longitude])
    .on("click", function () {
      openPoiViewModal(poi);
    })
    .addTo(markerGroup);
}

function markerCreate(event) {
  event.preventDefault();
  showLoader();
  for(let eventtarget of document.querySelectorAll("[data-eventtarget='save']")) {
    eventtarget.disabled = true;
  }
  for(let eventtarget of document.querySelectorAll("[data-eventtarget='back']")) {
    eventtarget.disabled = true;
  }
  for(let eventtarget of document.querySelectorAll("[data-eventtarget='closePopup']")) {
    eventtarget.disabled = true;
  }
  let form = event.target;
  const formData = new FormData(form);
  let popupCreateState = new PopupCreateState(formData, function() {
    snackbarMessage("POI und Story wurden erstellt", 0);
  });
  popupCreateState.uploadPoi();
}

function storyEdit(event){
  event.preventDefault();
  showLoader();
  for(let eventtarget of document.querySelectorAll("[data-eventtarget='save']")) {
    eventtarget.disabled = true;
  }
  for(let eventtarget of document.querySelectorAll("[data-eventtarget='back']")) {
    eventtarget.disabled = true;
  }
  for(let eventtarget of document.querySelectorAll("[data-eventtarget='closePopup']")) {
    eventtarget.disabled = true;
  }
  for(let eventtarget of document.querySelectorAll("[data-eventtarget='deleteStory']")) {
    eventtarget.disabled = true;
  }
  let form = event.target;
  const formData = new FormData(form);
  let popupCreateState = new PopupCreateState(formData, function() {
    snackbarMessage("Story wurde bearbeitet", 0);
  });
  popupCreateState.uploadFile();
}

function storyDelete(event, poi, story) {
  event.preventDefault();
  showLoader();
  const formData = new FormData();
  formData.append("story_id", story.story_id);
  postAndExecute("api/story-delete", formData, function (response) {
    let responseJson = JSON.parse(response)
    if (responseJson.status === "success") {
      let stories = []
      for(let i = 0; i < poi.stories.length; ++i) {
        if(poi.stories[i] != story.story_id) {
          stories[stories.length] = poi.stories[i];
        }
      }
      poi.stories = stories;
      snackbarMessage("Story wurde gelöscht", 0)
      openPoiViewModal(poi);
    } else {
      snackbarMessage("Story konnte nicht gelöscht werden", 1)
    }
  });
}

function storyUpload(event) {
  event.preventDefault();
  showLoader();
  for(let eventtarget of document.querySelectorAll("[data-eventtarget='save']")) {
    eventtarget.disabled = true;
  }
  for(let eventtarget of document.querySelectorAll("[data-eventtarget='back']")) {
    eventtarget.disabled = true;
  }
  for(let eventtarget of document.querySelectorAll("[data-eventtarget='closePopup']")) {
    eventtarget.disabled = true;
  }
  let form = event.target;
  const formData = new FormData(form);
  let popupCreateState = new PopupCreateState(formData, function() {
    snackbarMessage("Story wurde erstellt", 0);
  });
  popupCreateState.uploadFile();
}

function removeFileFromStory(event) {
  let img = event.target.parentElement;
  let storyFileId = img.id.replace("story-file", "");
  
  let files = document.getElementById("files-to-delete");
  if(files.value) {
    files.value = files.value + ", " + storyFileId;
  }
  else {
    files.value = storyFileId;
  }

  img.remove();
}

function openPoiCreateModal(e) {
  if (isMarkerMode) {
    isMarkerMode = false;
    document.getElementById("sidebar-add-button").classList.remove("active");
    let popupContent = document.createElement("div");
    popupContent.id = "popup";
    popupContent.innerHTML = markerCreateTemplate({latitude: e.latlng.lat, longitude: e.latlng.lng});
    for (let eventtarget of popupContent.querySelectorAll(
      "[data-eventtarget='closePopup']"
    )) {
      eventtarget.addEventListener("click", function () {
        reloadMarker();
        popupContent.remove();
      });
    }
    popupContent
      .querySelector("[data-eventtarget='markerCreate']")
      .addEventListener("submit", markerCreate);  

    document.getElementById("popup-inject").innerHTML = "";
    document.getElementById("popup-inject").appendChild(popupContent);
  }
}

function showCookieBanner() {
    let cookieBanner = document.createElement("div");
    cookieBanner.innerHTML = cookieBannerTemplate();
    for (let eventtarget of cookieBanner.querySelectorAll(
      "[data-eventtarget='acceptCookies']"
    )) {
      eventtarget.addEventListener("click", function () {
        localStorage.setItem("cookie-state", "accepted");
        cookieBanner.remove();
      });
    }
    for (let eventtarget of cookieBanner.querySelectorAll(
      "[data-eventtarget='declineCookies']"
    )) {
      eventtarget.addEventListener("click", function () {
        cookieBanner.remove();
      });
    }
    for (let eventtarget of cookieBanner.querySelectorAll(
      "[data-eventtarget='openCookieModal']"
    )) {
      eventtarget.addEventListener("click", function () {
        openCookieModal();
      });
    }
    document.getElementById("cookie-inject").innerHTML = "";
    document.getElementById("cookie-inject").appendChild(cookieBanner);
}

function openCookieModal() {
  let popupContent = document.createElement("div");
  popupContent.innerHTML = cookieModalTemplate(); 
  for (let eventtarget of popupContent.querySelectorAll(
    "[data-eventtarget='closePopup']"
  )) {
    eventtarget.addEventListener("click", function () {
      reloadMarker();
      popupContent.remove();
    });
  }
   
  document.getElementById("popup-inject").innerHTML = "";
  document.getElementById("popup-inject").appendChild(popupContent);
}

function openPoiViewModal(poi) {
  let popupContent = document.createElement("div");
  popupContent.innerHTML = markerViewTemplate({ displayname: poi.displayname });
  let stories = popupContent.querySelector("#stories-inject");
  for (let i = 0; i < poi.stories.length; ++i) {
    let div = document.createElement("div");
    div.id = "poi-story-" + poi.stories[i];
    div.classList.add("storyPreview");
    stories.appendChild(div);
    
    getAndExecute("public/get-story-by-id?story_id="+poi.stories[i], function (response) {
      let story = JSON.parse(response);
      let html = `
      <h3 class="m-0 text-cut">${story.headline}</h3>
      <p class="m-0 text-cut">${story.content}</p>`;
      let storyDiv = document.getElementById("poi-story-"+story.story_id);
      storyDiv.innerHTML=html;
      storyDiv.addEventListener("click", function (e) {
        e.preventDefault();
        openStoryViewModal(story, poi);
      });

    });

  }
  for (let eventtarget of popupContent.querySelectorAll(
    "[data-eventtarget='closePopup']"
  )) {
    eventtarget.addEventListener("click", function () {
      reloadMarker();
      popupContent.remove();
    });
  }
  if (localStorage.getItem("account_displayname") != null) {
    let addBtn = popupContent.querySelector("[data-eventtarget='addStory']");
    addBtn.addEventListener("click", function (e) {
      openStoryUploadModal(poi);
    });
    addBtn.classList.remove("hide");
  } 
  document.getElementById("popup-inject").innerHTML = "";
  document.getElementById("popup-inject").appendChild(popupContent);
}

function openHelpModal() {
  let popupContent = document.createElement("div");
  popupContent.innerHTML = helpTemplate();
   for (let eventtarget of popupContent.querySelectorAll(
    "[data-eventtarget='closePopup']"
  )) {
    eventtarget.addEventListener("click", function () {
      reloadMarker();
      popupContent.remove();
    });
  }
   
  document.getElementById("popup-inject").innerHTML = "";
  document.getElementById("popup-inject").appendChild(popupContent);
}

function openStoryViewModal(story, poi) {
  let popupContent = document.createElement("div");
  popupContent.id = "popupContent";
  popupContent.innerHTML = storyViewTemplate(story, poi.displayname);
  //story-files
  let files = popupContent.querySelector("#story-files");
  for (let i = 0; i < story.files.length; ++i) {
    let div = document.createElement("div");
    div.innerHTML =  `
      <img src="public/get-file-by-id?file_id=${story.files[i]}" />`;
    div.classList.add("file");
    div.addEventListener("click", function(e) {
      window.open("public/get-file-by-id?file_id=" + story.files[i],"Image");
    });
    files.appendChild(div);
  }
  for (let eventtarget of popupContent.querySelectorAll(
    "[data-eventtarget='closePopup']"
  )) {
    eventtarget.addEventListener("click", function () {
      reloadMarker();
      popupContent.remove();
    });
  }
  let backBtn = popupContent.querySelector("[data-eventtarget='back']");
  backBtn.addEventListener("click", function (e) {
    openPoiViewModal(poi); 
  })

  let editBtn = popupContent.querySelector("[data-eventtarget='edit']");
  if (story.is_owner) {
    editBtn.addEventListener("click", function (e) {
      openStoryEditModal(story, poi); 
    })
    editBtn.classList.remove("hide"); 
  }
  document.getElementById("popup-inject").innerHTML = "";
  document.getElementById("popup-inject").appendChild(popupContent);
}

function openStoryEditModal(story, poi) {
  let popupContent = document.createElement("div");
  popupContent.id = "popupContent";
  popupContent.innerHTML = storyEditTemplate({
    poiId: poi.poi_id,
    displayname: poi.displayname,
    stories: poi.stories,
    storyId: story.story_id,
    headline: story.headline,
    content: story.content,
    files: story.files,
    account_displayname: story.account_displayname,
    is_owner: story.is_owner
  });

  //story-files
  let files = popupContent.querySelector("#story-files");
  for (let i = 0; i < story.files.length; ++i) {
    let div = document.createElement("div");
    div.id = "story-file" + story.files[i];
    div.classList.add("file");
    div.innerHTML =  `
      <img src="public/get-file-by-id?file_id=${story.files[i]}" />`;
    div.classList.add("file");
    let storyFileRemoveDiv = document.createElement("div");
    storyFileRemoveDiv.classList.add("story-file-remove");
    storyFileRemoveDiv.addEventListener("click", (event) => {
      removeFileFromStory(event);
    });
    div.appendChild(storyFileRemoveDiv);
    files.appendChild(div);
  }

  for (let eventtarget of popupContent.querySelectorAll(
    "[data-eventtarget='closePopup']"
  )) {
    eventtarget.addEventListener("click", function () {
      reloadMarker();
      popupContent.remove();
    });
  }
  popupContent
  .querySelector("[data-eventtarget='storyEdit']")
  .addEventListener("submit", storyEdit);


  let backBtn = popupContent.querySelector("[data-eventtarget='back']");
  backBtn.addEventListener("click", function (e) {
    openStoryViewModal(story, poi); 
  }) 
  let delBtn = popupContent.querySelector("[data-eventtarget='deleteStory']");
  let account_displayname = localStorage.getItem("account_displayname");
  if (account_displayname == story.account_displayname) {
    delBtn.addEventListener("click", function (e) {
      storyDelete(e, poi, story);
    });
    delBtn.classList.remove("hide");
  }
  document.getElementById("popup-inject").innerHTML = "";
  document.getElementById("popup-inject").appendChild(popupContent);
}

function openStoryUploadModal(poi) {
  let popupContent = document.createElement("div");
  popupContent.id = "popupContent";
  popupContent.innerHTML = storyUploadTemplate({
    poiId: poi.poi_id,
    displayname: poi.displayname,
    stories: poi.stories
  });
  for (let eventtarget of popupContent.querySelectorAll(
    "[data-eventtarget='closePopup']"
  )) {
    eventtarget.addEventListener("click", function () {
      reloadMarker();
      popupContent.remove();
    });
  }
  popupContent
  .querySelector("[data-eventtarget='storyUpload']")
  .addEventListener("submit", storyUpload);


  let backBtn = popupContent.querySelector("[data-eventtarget='back']");
  backBtn.addEventListener("click", function (e) {
    openPoiViewModal(poi);
  })

  document.getElementById("popup-inject").innerHTML = "";
  document.getElementById("popup-inject").appendChild(popupContent);
}

function markerCreateTemplate(options) {
  return `
  <div class="popup-background full-size text">
    <div class="popup">
      <div class="full-size">
        <div class="layout-space-between-column full-size">
          <header class="popup-header">
            <div class="layout-space-between full-size">
              <button data-eventtarget="closePopup" class="button-transparent">
                <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="icon">
                  <polyline points="12.5,19 5,12.5 12.5,5"/>
                </svg> 
              </button>
              <h2 class="text-margin-0 popup-title">Neue Story erstellen</h2>
              <button data-eventtarget="save" type="submit" form="markerCreate" class="button-transparent">
                <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="icon">
                  <polyline points="5,12.5 11,19 19,5"/>
                </svg>
              </button>
            </div>
          </header>
          <div class="popup-main">
            <form id="markerCreate" data-eventtarget="markerCreate" class="popup-container">
              <input type="hidden" name="latitude" id="latitude" value="${options.latitude}" />
              <input type="hidden" name="longitude" id="longitude" value="${options.longitude}" />
              <label for="displayname" class="label">Orts-Titel</label>
              <input class="input" type="text" name="displayname" required/>
	      <h2>Story</h2>
	      <label for="headline" class="label">Überschrift</label>
              <input class="input" type="text" name="headline" required/>
	      <label for="content" class="label">Inhalt</label>
              <textarea rows="10" name="content" class="input" required></textarea>
              <label for="images" class="label">Bilder</label>
              <input data-eventtarget="imageUpload" class="input-files" type="file" name="images" id="image-upload" multiple accept="image/png, image/jpeg"/>
	    </form>
          </div>
          <footer class="button-group">
            <button data-eventtarget="closePopup" class="button">Abbrechen</button>
            <button data-eventtarget="save" type="submit" form="markerCreate" class="button">Speichern</button>
          </footer>
        </div>
      </div>
    </div>
  </div>
`;
}

function helpTemplate() {
  return `
    <div class="popup-background full-size text">
      <div class="popup">
        <div class="full-size">
          <div class="layout-space-between-column full-size">
            <header class="popup-header">
              <div class="layout-space-between full-size">
                <button data-eventtarget="closePopup" class="button-transparent">
                  <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="icon">
                    <polyline points="12.5,19 5,12.5 12.5,5"/>
                  </svg> 
                </button>
                <h2 class="text-margin-0 popup-title text-cut">Hilfe</h2>
                <button data-eventtarget="closePopup" class="button-transparent">
                  <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="icon">
                    <polyline points="5,5 19,19"/>
                    <polyline points="19,5 5,19"
                  </svg> 
                </button>
              </div>
            </header>
	    <div class="popup-main">
	      <p><h2>Allgemeines:</h2>

        <p>Bei dieser Website handelt es sich um eine interaktive Kartenanwendung, die dazu dient auf der Karte zu interessanten und geschichtsträchtigen Standorten (POIs) Bilder und Storys zu erstellen. Dabei können zu einem POI verschiedene Storys von unterschiedlichen Usern erstellt werden.
        <p>Diese können von allen Besuchern der Seite gelesen werden. Nach der Anmeldung bietet sich die Möglichkeit, neue Storys zu erstellen und eigene Storys zu bearbeiten/ zu löschen.
        <p>
        <p><h2>Einen POI anlegen:</h2>
        <p>
        <p>Um einen POI anzulegen, muss man sich zuerst anmelden. Nach der Anmeldung bietet sich die Möglichkeit, im unteren Bereich auf das Plus zu klicken. Anschließend wählt man auf der Karte einen Punkt aus, an dem man einen POI anlegen will. Anschließend müssen verschiedene Felder befüllt werden:
        <p><h3>Orts-Titel:</h3> Hier wird die Überschrift für den POI festgelegt.
        <p><h3>Überschrift:</h3> Hier wird die Überschrift für die User-Story festgelegt
        <p><h3>Inhalt:</h3> Hier kann die entsprechende Geschichte der Story erzählt werden
        <p><h3>Bilder:</h3> Hier können verschiedene Bilder hochgeladen werden. Sowohl alte Bilder aus der Galerie, als auch welche, die in dem Moment aufgenommen werden.
        <p>Mit einem Klick auf Speichern wird der POI mit der neuen Story gespeichert.
        <p>
        <p><h2>Eine Story anlegen:</h2>
        <p>
        <p>Um eine Story zu einem bestehenden POI hinzuzufügen, klickt man als angemeldeter Nutzer auf einen POI. Hier kann am Ende der Liste mit den bestehenden Storys mit Klick auf „Story hinzufügen“ eine neue erstellt werden. Der Prozess ähnelt dem der POI-Erstellung. Lediglich die POI-Überschrift muss nicht erneut gepflegt werden.
        <p>
        <p><h2>Eine Story bearbeiten / löschen:</h2>
        <p>
        <p>Zum Bearbeiten und Löschen einer Story wird eine eigens erstellte Story ausgewählt. Beim Klick auf das Bearbeiten-Symbol in der rechten oberen Ecke öffnet sich die Bearbeitungsansicht. Hier können bestehende Texte geändert werden. Anschließend kann die Änderung gespeichert werden. Darüber hinaus lässt sich die Story in dieser Ansicht löschen.
	    </div>
            <footer class="popup-footer">
              <a href="" class="Impressum">Impressum</a>
              <button data-eventtarget="closePopup" class="button">Schließen</button>
            </footer>
          </div>
        </div>
      </div>
    </div>`;
}

function markerViewTemplate(options) {
  return `
    <div class="popup-background full-size text">
      <div class="popup">
        <div class="full-size">
          <div class="layout-space-between-column full-size">
            <header class="popup-header">
              <div class="layout-space-between full-size">
                <button data-eventtarget="closePopup" class="button-transparent">
                  <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="icon">
                    <polyline points="12.5,19 5,12.5 12.5,5"/>
                  </svg> 
                </button>
                <h2 class="text-margin-0 popup-title text-cut">${options.displayname}</h2>
                <button data-eventtarget="closePopup" class="button-transparent">
                  <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="icon">
                    <polyline points="5,5 19,19"/>
                    <polyline points="19,5 5,19"
                  </svg> 
                </button>
              </div>
            </header>
	    <div class="popup-main">
	      <div id="stories-inject" class="stories-inject"></div>
	      <button data-eventtarget="addStory" class="button hide">Story hinzufügen</button>
	    </div>
            <footer class="popup-footer">
              <button data-eventtarget="closePopup" class="button">Schließen</button>
            </footer>
          </div>
        </div>
      </div>
    </div>`;
}

function storyViewTemplate(story, displayname) {
  return `
    <div class="popup-background full-size text">
      <div class="popup">
        <div class="full-size">
          <div class="layout-space-between-column full-size">
            <header class="popup-header">
              <div class="layout-space-between full-size">
                <button data-eventtarget="back" class="button-transparent">
                  <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="icon">
                    <polyline points="12.5,19 5,12.5 12.5,5"/>
                  </svg> 
                </button>
                <h2 class="text-margin-0 popup-title text-cut">${displayname}</h2>
                <button data-eventtarget="edit" class="button-transparent hide">
                  <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="icon">
                    <polygon points="5,19 6,15 16,5 19,8 9,18"/>
                    <polyline points="14,19 19,19"/>
                  </svg>
                </button>
              </div>
            </header>
            <div class="popup-main">
              <h2 class="text-wrap">${story.headline}</h2>
              <p class="text-wrap">${story.content}</p>
              <div class="files" id="story-files"></div>
              <p class="story-author">Erstellt von: ${story.account_displayname}</p>
            </div>
            <footer class="popup-footer">
              <button data-eventtarget="closePopup" class="button">Schließen</button>
            </footer>
          </div>
        </div>
      </div>
    </div>`;
}

function storyEditTemplate(options) {
  return `
    <div class="popup-background full-size text">
      <div class="popup">
        <div class="full-size">
          <div class="layout-space-between-column-story-edit full-size">
            <header class="popup-header">
              <div class="layout-space-between full-size">
                <button data-eventtarget="back" class="button-transparent">
                  <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="icon">
                    <polyline points="12.5,19 5,12.5 12.5,5"/>
                  </svg> 
                </button>
                <h2 class="text-margin-0 popup-title text-cut">${options.displayname}</h2>
                <button data-eventtarget="save" type="submit" form="storyEdit" class="button-transparent">
                  <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="icon">
                    <polyline points="5,12.5 11,19 19,5"/>
                  </svg>
                </button>
              </div>
            </header>
            <div class="popup-main">
              <form id="storyEdit" data-eventtarget="storyEdit" class="popup-container">
                <input type="hidden" name="poi_id" id="poi_id" value="${options.poiId}" />
                <input type="hidden" name="story_id" id="story_id" value="${options.storyId}" />
                <input type="hidden" name="stories" id="stories" value="${options.stories}" />
                <input type="hidden" name="displayname" id="displayname" value="${options.displayname}" />
                <input type="hidden" name="files" id="files" value="${options.files}" />
                <input type="hidden" name="files-to-delete" id="files-to-delete" value="" />
                <label for="headline" class="label">Überschrift</label>
                <input class="input" type="text" name="headline" value="${options.headline}" required/>
                <label for="content" class="label">Inhalt</label>
                <textarea rows="10" name="content" class="input" required>${options.content}</textarea>
                <label for="images" class="label">Bilder</label>
                <input data-eventtarget="imageUpload" class="input-files" type="file" name="images" id="image-upload" multiple accept="image/png, image/jpeg"/>
                <div class="files" id="story-files"></div>
                <button data-eventtarget="deleteStory" class="button delete hide">Story löschen</button>
              </form>  
            </div>
            <footer class="button-group">
              <button data-eventtarget="closePopup" class="button">Abbrechen</button>
              <button data-eventtarget="save" type="submit" form="storyEdit" class="button">Speichern</button>
            </footer>
          </div>
        </div>
      </div>
    </div>`;
}

function storyUploadTemplate(options) {
  return `
    <div class="popup-background full-size text">
      <div class="popup">
        <div class="full-size">
          <div class="layout-space-between-column full-size">
            <header class="popup-header">
              <div class="layout-space-between full-size">
                <button data-eventtarget="back" class="button-transparent">
                  <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="icon">
                    <polyline points="12.5,19 5,12.5 12.5,5"/>
                  </svg> 
                </button>
                <h2 class="text-margin-0 popup-title text-cut">${options.displayname}</h2>
                <button data-eventtarget="save" type="submit" form="storyUpload" id="saveButton" class="button-transparent">
                  <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="icon">
                    <polyline points="5,12.5 11,19 19,5"/>
                  </svg>
                </button>
              </div>
            </header>
            <div class="popup-main">
              <form id="storyUpload" data-eventtarget="storyUpload" class="popup-container">
                <input type="hidden" name="poi_id" id="poi_id" value="${options.poiId}" />
                <input type="hidden" name="stories" id="stories" value="${options.stories}" />
                <input type="hidden" name="displayname" id="displayname" value="${options.displayname}" />
                <label for="headline" class="label">Überschrift</label>
                <input class="input" type="text" name="headline" value="" required />
                <label for="content" class="label">Inhalt</label>
                <textarea class="input" rows="10" name="content" required></textarea>
                <label for="images" class="label">Bilder</label>
                <input data-eventtarget="imageUpload" class="input-files" type="file" name="images" id="image-upload" multiple accept="image/png, image/jpeg"/> 
              </form>
            </div>
            <footer class="button-group">
              <button data-eventtarget="closePopup" class="button">Abbrechen</button>
              <button data-eventtarget="save" type="submit" form="storyUpload" id="saveButton" class="button">Speichern</button>
            </footer>
          </div>
        </div>
      </div>
    </div>`;
}

function cookieBannerTemplate() {
  return `
    <div class="cookie-banner">
      <header>
        <h2 class="cookie-banner-headline">Wir benutzen Cookies</h2>
      </header>
      <main class="cookie-banner-main">
        <a class="link" data-eventtarget="openCookieModal">Mehr Informationen</a>
      </main>
      <footer class="cookie-banner-footer">
        <button class="button" data-eventtarget="declineCookies">Ablehnen</button>
        <button class="button" data-eventtarget="acceptCookies">Akzeptieren</button>
      </footer>
    </div>
  `
}

function cookieModalTemplate() {
  return `
    <div class="popup-background full-size text">
      <div class="popup">
        <div class="full-size">
          <div class="layout-space-between-column full-size">
            <header class="popup-header">
              <div class="layout-space-between full-size">
                <button data-eventtarget="closePopup" class="button-transparent">
                  <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="icon">
                    <polyline points="12.5,19 5,12.5 12.5,5"/>
                  </svg> 
                </button>
                <h2 class="text-margin-0 popup-title text-cut">Cookies</h2>
                <button data-eventtarget="closePopup" class="button-transparent">
                  <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="icon">
                    <polyline points="5,5 19,19"/>
                    <polyline points="19,5 5,19"
                  </svg> 
                </button>
              </div>
            </header>
	    <div class="popup-main">
	      <p>Hier stehen die Cookie Hinweise.</p>
	    </div>
            <footer class="popup-footer">
              <button data-eventtarget="closePopup" class="button">Schließen</button>
            </footer>
          </div>
        </div>
      </div>
    </div>`;
}

function toggleMarkerMode() {
  isMarkerMode = !isMarkerMode;
  if(isMarkerMode) {
    document.getElementById("sidebar-add-button").classList.add("active");
  } else {
    document.getElementById("sidebar-add-button").classList.remove("active");
  }
}

function registerServiceWorker() {
  if ("serviceWorker" in navigator) {
    fetch("manifest.json")
      .then((resp) => resp.json())
      .then((manifest) => {
        return navigator.serviceWorker
          .register("serviceworker.js", {
            scope: manifest.scope,
          })
      })
      .catch(console.error);
  }
}

function onEachFeature(feature, layer) {
  if (feature.properties && feature.properties.description) {
    layer.bindPopup(feature.properties.description);
  }
}

function injectGeoJSON(geoJSON) {
  L.geoJSON(JSON.parse(geoJSON), { onEachFeature: onEachFeature }).addTo(map);
}

function showLoader() {
  document.getElementById("blur").style.display = "block";
  document.getElementById("loader").style.display = "block";
}

function hideLoader() {
  document.getElementById("blur").style.display = "none";
  document.getElementById("loader").style.display = "none";
}
