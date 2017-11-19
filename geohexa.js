var BASE_36_DIGITS_OUTPUT = "0123456789abcdefghijkLmnopqrstuvwxyz";
var BASE_36_DIGITS = BASE_36_DIGITS_OUTPUT.toLowerCase();

Math.toRadians = function(degrees) {
  return degrees * Math.PI / 180;
};

function distance (lat1, lon1, lat2, lon2) {

    var latDistance = Math.toRadians(lat2 - lat1);
    var lonDistance = Math.toRadians(lon2 - lon1);

    // Haversine formula
    var sinLat = Math.sin(latDistance / 2);
    var sinLon = Math.sin(lonDistance / 2);
    var a = sinLat * sinLat + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * sinLon * sinLon;
    var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

    var earthRadius = 6371;
    var distance = earthRadius * c * 1000; // convert to meters

    return distance;
}

function prepareLat(lat) {
    if (lat > 89.99999999) {
        lat = 89.99999999;
    }
    return lat + 90;
}

function prepareLon(lon) {
    if (lon == 180) {
        lon = -180; // 180E is same as 180W
    }
    return lon + 180;
}

function compress(num, cellSize) {
    var cell = Math.floor(num / cellSize);
    var remainder = num - ( cell * cellSize);
    var base36 = BASE_36_DIGITS_OUTPUT.charAt(cell);

    //if (VERBOSE) {
    //    System.out.println ("base36 digit: " + base36 + "(" + cell + ")  cell_size_degrees[" + cell_size + "]   fragment[" + num + "]  remainder_to_go[" + remainder + "]");
    //}

    return {base36: base36, remainder: remainder};
}

function latLonToGeohexa(lat, lon, accuracy) {
    if (accuracy === void 0 || accuracy === null) {
        //alert("defaulting accuracy to 3 for lat, lon " + lat + "," + lon)
        accuracy = 3;
    }
    lat = parseFloat(lat);
    lon = parseFloat(lon);
    accuracy = parseFloat(accuracy);

    if (accuracy < 0.00000001) { // need to be sensible about the accuracy to prevent division by zero
        accuracy = 0.00000001;
    }
    var geohexa = "";
    var allLatgeo = "";
    var latFragment = prepareLat(lat);
    var lonFragment = prepareLon(lon);
    var latUnit = 180;
    var lonUnit = 360;
    
    //alert ("Preparing lat " + prepareLat(lat));
    
    //alert ("latFragment, lonFragment: ", latFragment + ", " + lonFragment);

    // null is a valid geohexa - represents 0, 0
    var error = distance(lat, lon, 0, 0);
    if (error < accuracy) {
        return "";
    }

    var latCellSize = 0;
    var lonCellSize = 0;
    //if (VERBOSE) {
    //    latCellSize = distance(latBottom(allLatgeo), 0, latBottom(allLatgeo)+latUnit, 0);
    //}

    var count = 0;
    while (true) {
        count = count + 1;
        lonUnit = lonUnit / 36;
        var cLon = compress(lonFragment, lonUnit);
        lonFragment = cLon.remainder;
        geohexa = geohexa + cLon.base36;

        var geoLatLon = geohexaToLatLon(geohexa);
        error = distance(lat, lon, geoLatLon.lat, geoLatLon.lon);

        //if (VERBOSE) {
        //    lonCellSize = distance(latBottom(allLatgeo), 0, latBottom(allLatgeo), lonUnit);
        //    System.out.println("Current lat *lon* cell size meters: " + latCellSize + " , " + lonCellSize + "   error in meters[" + error + "]\n...");
        //}

        if (error < accuracy) {
            break;
        }

        latUnit = latUnit / 36;
        var cLat = compress(latFragment, latUnit);
        latFragment = cLat.remainder;
        geohexa = geohexa + cLat.base36;
        allLatgeo = allLatgeo + cLat.base36;

        geoLatLon = geohexaToLatLon(geohexa);
        error = distance(lat, lon, geoLatLon.lat, geoLatLon.lon);

        //if (VERBOSE) {
        //    latCellSize = distance(latBottom(allLatgeo), 0, latBottom(allLatgeo)+latUnit, 0);
        //    System.out.println("Current *lat* lon cell size meters: " + latCellSize + " , " + lonCellSize + "   error in meters[" + error + "]\n\n");
        //}

        if (error < accuracy) {
            break;
        }
        
        if (count > 15) {
            break;
        }
        
    }

    return geohexa;
}

function geohexaToLatLon(hexa) {
    var lat = 0;
    var lon = 0;

    var latHeight = 180;
    var lonWidth = 360;

    var digitNum = 0;
    var len = 0;
    for (var i = 0, len = hexa.length; i < len; i++) {
        var d = hexa[i].toLowerCase();
        digitNum = digitNum + 1;
        var remainder = digitNum % 2;
        if (remainder == 1 ) { // we have a lon geohexa digit
            lon = lon + (lonWidth / 36) * BASE_36_DIGITS.indexOf(d);
            lonWidth = lonWidth / 36;
        } else {
            lat = lat + (latHeight / 36) * BASE_36_DIGITS.indexOf(d);
            latHeight = latHeight / 36;
        }
    }

    // calculate mid point of cell
    lat = lat - 90  + latHeight / 2;
    lon = lon - 180 + lonWidth / 2;

    //if (VERBOSE) {
    //    System.out.println(hexa + " as lat, lon " + lat + " " + lon);
    //}
    
    return {lat: lat, lon: lon};
}

function inRange(hexa) {
    var len = 0;
    for (var i = 0, len = hexa.length; i < len; i++) {
        if (BASE_36_DIGITS.indexOf(hexa[i].toLowerCase()) == -1) {
            return false;
        }
    }
    return true;
}

function updateLatLon() {
    var lat = updateLat();
    var lon = updateLon();
    
    var eOGeohexa = document.getElementById("oGeohexa");
    if (! (lat.ok && lon.ok) ) {
        eOGeohexa.textContent = "";
        //alert("lat or lon is no ok");
        return;
    }
    
    var eAccuracy  = document.getElementById("accuracy");
    var accuracy = parseFloat(eAccuracy.value);

    var eOAccuracy = document.getElementById("oAccuracy");
    if ( isNaN(accuracy) ) {
        eOAccuracy.textContent = "default is 3";
        accuracy = 3;
    } else {
        eOAccuracy.textContent = "";
    }

    //alert("calculating geohexa for lat " + lat.value + " and lon " + lon.value );
    var geohexa = latLonToGeohexa(lat.value, lon.value, accuracy);

    if (geohexa == "") {
        geohexa = "[null geohexa]";
    }
    eOGeohexa.textContent = geohexa;
}

function updateLat() {
    var eLat = document.getElementById("lat");
    var lat = parseFloat(eLat.value);

    if ( isNaN(lat) ) {
        return {ok: false};
    }
    
    var eLatError = document.getElementById("latError");

    if ( lat >= -90 && lat <= 90 ) {
        eLat.style.background = "";
        eLatError.textContent = "";
        //alert("lat is ok");
    } else {
        eLat.style.background = "yellow";
        eLatError.textContent = "The latitude must be from -90 to 90";
        //alert("lat is not ok");
        return {ok: false};
    }
    
    return {ok: true, value: lat};
}

function updateLon() {
    var eLon = document.getElementById("lon");
    var lon = parseFloat(eLon.value);

    if ( isNaN(lon) ) {
        return {ok: false};
    }

    var eLonError = document.getElementById("lonError");

    if (lon >= -180 && lon <= 180) {
        eLon.style.background = "";
        eLonError.textContent = "";
        //alert("lon is ok - value [" + lon + "]");
    } else {
        eLon.style.background = "yellow";
        eLonError.textContent = "The longitude must be from -180 to 180";
        //alert("lon is not ok");
        return {ok: false};
    }

    return {ok: true, value: lon};
}

function updateGeohexa() {
    var eHexaError = document.getElementById("hexaError");

    var eHexa = document.getElementById("geohexa");
    var hexa = eHexa.value.toLowerCase();

    var eOLat = document.getElementById("oLat");
    var eOLon = document.getElementById("oLon");

    if (inRange(hexa)) {
        eHexa.style.background = "";
        eHexaError.textContent = "";
    } else {
        eHexa.style.background = "yellow";
        eOLat.textContent = "";
        eOLon.textContent = "";
        eHexaError.textContent = "The geohexa must only use the digits 0-9 and a-z";
        return;
    }
    
    var coord = geohexaToLatLon(hexa);
    eOLat.textContent = coord.lat;
    eOLon.textContent = coord.lon;
}
