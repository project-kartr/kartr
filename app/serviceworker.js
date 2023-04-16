const staticCache = "my-static-cache";
// const assets = [
//   "index.html",
//   "css/app.css",
//   "js/app.js"
// ];

const assets = [
  "./index.html"
]

self.addEventListener("install", installEvent => {
  installEvent.waitUntil(
    caches.open(staticCache)
    .then(cache => {
      cache.addAll(assets);
    }).catch(e => {
      console.error(e);
    })
  );
});

self.addEventListener("fetch", fetchEvent => {
  fetchEvent.respondWith(
    caches.match(fetchEvent.request).then(res => {
      return res || fetch(fetchEvent.request);
    })
  );
});
