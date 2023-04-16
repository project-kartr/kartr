function postAndExecute(path, body, callback) {
    let xmlHttpReq = new XMLHttpRequest();
    xmlHttpReq.onreadystatechange = function () {
      if (xmlHttpReq.readyState === 4 && xmlHttpReq.status === 200) {
        callback(xmlHttpReq.responseText);
      }
    };
    xmlHttpReq.open("POST", path);
    xmlHttpReq.send(body);
  }
  
function getAndExecute(path, callback) {
  let xmlHttpReq = new XMLHttpRequest();
  xmlHttpReq.onreadystatechange = function () {
    if (xmlHttpReq.readyState === 4 && xmlHttpReq.status === 200) {
      callback(xmlHttpReq.responseText);
    }
  };
  xmlHttpReq.open("GET", path);
  xmlHttpReq.send();
}

function getImageAndExecute(path, callback) {
  let xmlHttpReq = new XMLHttpRequest();
  xmlHttpReq.responseType = "blob"
  xmlHttpReq.onreadystatechange = function () {
    if (xmlHttpReq.readyState === 4 && xmlHttpReq.status === 200) {
      callback(xmlHttpReq.response);
    }
  };
  xmlHttpReq.open("GET", path);
  xmlHttpReq.send();
}

function removeValueFromString(list, value) {
  return list.replace(new RegExp(",?" + value + ",?"), function(match) {
      var first_comma = match.charAt(0) === ',',
          second_comma;

      if (first_comma &&
          (second_comma = match.charAt(match.length - 1) === ',')) {
        return ',';
      }
      return '';
    });
};
