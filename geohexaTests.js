//start karma start
//karma run

BASE_36_DIGITS = '0123456789abcdefghijklmnopqrstuvwxyz';

MM = 0.001;
CM = 0.01;
METER = 1;
TEN_METER = 10;
HUNDRED_METER = 100;
KM = 1000;
TEN_KM = 10000;
THOUSAND_KM = 1000000;

ACCURACY = 3;
MANY = 10000;

// Custom assertion for comparing floating point numbers
QUnit.assert.near = function (actual, expected, message, error) {
    if (error === void 0 || error === null) {
        error = 0.00000001;
    }

    var result = false;
    if (actual <= expected + error && actual >= expected - error) {
        result = true;
    }

    this.pushResult({
        result: result,
        actual: actual,
        expected: expected,
        message: message,
    });
};

QUnit.test('near', function (assert) {
    assert.expect(2);

    assert.near(6, 5, '6 is near enough to 5 (error up to 2)', 2);
    assert.near(1.00000001, 1, '1.00000001 is near enough to 1 (default error)');
});

QUnit.test('Check that Unit Test Framework is up and running', function (assert) {
    // Setup the various states of the code you want to test and assert conditions.
    assert.equal(1, 1, '1 === 1'); // actual, expected, message
    assert.ok(true, 'true is truthy');
    assert.ok(1, '1 is also truthy');
    assert.ok([], 'so is an empty array or object');
});

QUnit.test('Test in range', function (assert) {
    assert.ok(inRange('abc'), 'abc is a valid geohexa');
    assert.ok(inRange(''), 'null is a valid geohexa');

    assert.ok(inRange('a_c') == false, 'a_c is not a valid geohexa');
});

QUnit.test('Test specific value for geohexa to lat lon', function (assert) {
    assert.near(geohexaToLatLon('shazolent').lat, -0.05535193758573396, 'Latitude conversion failed');
    assert.near(geohexaToLatLon('shazolent').lon, 102.9661392842173, 'Longitude conversion failed');
});

QUnit.test('Test specific value for lat lon to geohexa', function (assert) {
    assert.equal(latLonToGeohexa(51.481874, -0.112564), 'hszaLoe3t', 'Oval Tube should be hszaLoe3t');
});

QUnit.test('Test max lat lon', function (assert) {
    var hexa = latLonToGeohexa(90, 180);
    assert.equal(hexa.length, 10, 'max lat lon length is 10 at default accuracy');
});

QUnit.test('Test max min lon', function (assert) {
    var hexa = latLonToGeohexa(-90, -180);
    assert.equal(hexa.length, 10, 'min lat lon length is 10 at default accuracy');
});

QUnit.test('Test geohexa to lat lon is within specification', function (assert) {
    var coord = geohexaToLatLon('zz');
    assert.ok(coord.lat >= -90);
    assert.ok(coord.lat <= 90);
    assert.ok(coord.lon >= -180);
    assert.ok(coord.lon <= 180);
});

QUnit.test('Test distance between two lat lons', function (assert) {
    assert.near(
        distance(51.487141, -0.125873, 51.488178, -0.128224),
        199.48427672,
        'Distance from one side of Vauxhall bridge to the other is about 200 meters',
    );
});

QUnit.test('Test lat lon to geohexa and back ten km accuracy', function (assert) {
    var lat = 1.285864;
    var lon = 103.851831; // Singapore Boat Quay
    var hexa = latLonToGeohexa(lat, lon, TEN_KM);
    var coord = geohexaToLatLon(hexa);
    var error = distance(lat, lon, coord.lat, coord.lon);
    assert.ok(error < TEN_KM, 'error should be less than 10 km');
});

QUnit.test('Test lat lon to geohexa and back meter accuracy', function (assert) {
    var lat = 64.123565;
    var lon = -21.805507; // Reykjavik Brewery
    var hexa = latLonToGeohexa(lat, lon, METER);
    var coord = geohexaToLatLon(hexa);
    var error = distance(lat, lon, coord.lat, coord.lon);
    assert.ok(error < METER, 'error should be less than one meter');
});

QUnit.test('Test lat lon to geohexa and back mm accuracy', function (assert) {
    var lat = -46.896522;
    var lon = 168.130336; // NZ Oyster Bar
    var hexa = latLonToGeohexa(lat, lon, MM);
    var coord = geohexaToLatLon(hexa);
    var error = distance(lat, lon, coord.lat, coord.lon);
    assert.ok(error < MM, 'error should be less than one millimeter');
});

var seed = 42;
function random() {
    var x = Math.sin(seed++) * 10000;
    return x - Math.floor(x);
}

QUnit.test('Test many random lat lons', function (assert) {
    seed = 42;
    var totalLength = 0;
    for (var i = 1; i < MANY + 1; i++) {
        var testLat = random() * 180 - 90;
        var testLon = random() * 360 - 180;
        //alert("my randoms: " + testLat + ", " + testLon );
        var hexa = latLonToGeohexa(testLat, testLon, ACCURACY);
        var latLon = geohexaToLatLon(hexa);
        var error = distance(testLat, testLon, latLon.lat, latLon.lon);
        assert.ok(error < ACCURACY);
        totalLength = totalLength + hexa.length;
    }
    alert(
        '\nTotal length of geohexa for ' +
            MANY +
            ' random co-ordinates at ' +
            ACCURACY +
            ' meter accuracy: ' +
            totalLength +
            '\nAverage length of geohexa: ' +
            totalLength / MANY +
            '\n',
    );
});

QUnit.test('Test many random lat lons near equator', function (assert) {
    seed = 42;
    var totalLength = 0;
    for (var i = 1; i < MANY + 1; i++) {
        var testLat = random() * 10 - 5;
        var testLon = random() * 360 - 180;
        //alert("my randoms: " + testLat + ", " + testLon );
        var hexa = latLonToGeohexa(testLat, testLon, ACCURACY);
        var latLon = geohexaToLatLon(hexa);
        var error = distance(testLat, testLon, latLon.lat, latLon.lon);
        assert.ok(error < ACCURACY);
        totalLength = totalLength + hexa.length;
    }
    alert(
        '\nTotal length of geohexa for ' +
            MANY +
            ' random co-ordinates near the Equator at ' +
            ACCURACY +
            ' meter accuracy: ' +
            totalLength +
            '\nAverage length of geohexa: ' +
            totalLength / MANY +
            '\n',
    );
});

QUnit.test('Test many random lat lons near north pole', function (assert) {
    seed = 42;
    var totalLength = 0;
    for (var i = 1; i < MANY + 1; i++) {
        var testLat = random() * 10 + 80;
        var testLon = random() * 360 - 180;
        //alert("my randoms: " + testLat + ", " + testLon );
        var hexa = latLonToGeohexa(testLat, testLon, ACCURACY);
        var latLon = geohexaToLatLon(hexa);
        var error = distance(testLat, testLon, latLon.lat, latLon.lon);
        assert.ok(error < ACCURACY);
        totalLength = totalLength + hexa.length;
    }
    alert(
        '\nTotal length of geohexa for ' +
            MANY +
            ' random co-ordinates near the North Pole at ' +
            ACCURACY +
            ' meter accuracy: ' +
            totalLength +
            '\nAverage length of geohexa: ' +
            totalLength / MANY +
            '\n',
    );
});

QUnit.test('Test many random lat lons near lat 42 to 52', function (assert) {
    seed = 42;
    var totalLength = 0;
    for (var i = 1; i < MANY + 1; i++) {
        var testLat = random() * 10 - 42;
        var testLon = random() * 360 - 180;
        //alert("my randoms: " + testLat + ", " + testLon );
        var hexa = latLonToGeohexa(testLat, testLon, ACCURACY);
        var latLon = geohexaToLatLon(hexa);
        var error = distance(testLat, testLon, latLon.lat, latLon.lon);
        assert.ok(error < ACCURACY);
        totalLength = totalLength + hexa.length;
    }
    alert(
        '\nTotal length of geohexa for ' +
            MANY +
            ' random co-ordinates near lat 42 to 52 at ' +
            ACCURACY +
            ' meter accuracy: ' +
            totalLength +
            '\nAverage length of geohexa: ' +
            totalLength / MANY +
            '\n',
    );
});

QUnit.test('Test case insensitive input', function (assert) {
    var latLon1 = geohexaToLatLon('iilloo');
    var latLon2 = geohexaToLatLon('IILLOO');
    assert.ok(latLon1.lat === latLon2.lat);
    assert.ok(latLon1.lon === latLon2.lon);
});

QUnit.test('Test single null geohexa', function (assert) {
    var latLon = geohexaToLatLon('');
    assert.ok(latLon.lat === 0);
    assert.ok(latLon.lon === 0);
});

QUnit.test('Test three digit geohexa', function (assert) {
    var latLon = geohexaToLatLon('Tim');
    var hexa = latLonToGeohexa(latLon.lat, latLon.lon);
    assert.ok('tim', hexa);
});

QUnit.test('Test four digit geohexa', function (assert) {
    var latLon = geohexaToLatLon('Qarj');
    var hexa = latLonToGeohexa(latLon.lat, latLon.lon);
    assert.ok('qarj', hexa);
});

QUnit.test('Test all single digit geohexa', function (assert) {
    var len = 0;
    for (var i = 0, len = BASE_36_DIGITS.length; i < len; i++) {
        var d1 = BASE_36_DIGITS[i];
        var latLon = geohexaToLatLon(d1);
        var hexa = latLonToGeohexa(latLon.lat, latLon.lon).toLowerCase();
        assert.equal(d1, hexa);
    }
});

QUnit.test('Test all two digit geohexa', function (assert) {
    var len1 = 0;
    for (var i = 0, len1 = BASE_36_DIGITS.length; i < len1; i++) {
        var d1 = BASE_36_DIGITS[i];
        var len2 = 0;
        for (var j = 0, len2 = BASE_36_DIGITS.length; j < len2; j++) {
            var d2 = BASE_36_DIGITS[j];
            var latLon = geohexaToLatLon(d1 + d2);
            var hexa = latLonToGeohexa(latLon.lat, latLon.lon).toLowerCase();
            assert.equal(d1 + d2, hexa);
        }
    }
});

QUnit.test('Test allthird fourth digit geohexa', function (assert) {
    var len1 = 0;
    for (var i = 0, len1 = BASE_36_DIGITS.length; i < len1; i++) {
        var d1 = BASE_36_DIGITS[i];
        var len2 = 0;
        for (var j = 0, len2 = BASE_36_DIGITS.length; j < len2; j++) {
            var d2 = BASE_36_DIGITS[j];
            var latLon = geohexaToLatLon('00' + d1 + d2);
            var hexa = latLonToGeohexa(latLon.lat, latLon.lon).toLowerCase();
            assert.equal('00' + d1 + d2, hexa);
        }
    }
});

QUnit.test('Test can output null digit geohexa', function (assert) {
    assert.equal(latLonToGeohexa(0, 0, 0), '', '0, 0 is a null geohexa');
});

QUnit.test('Test upper danger boundary', function (assert) {
    assert.ok(latLonToGeohexa(64, 0, 0));
    // testing that there is no division by 0 exception - this test applies to an alternate more optimal (and complicated) algorithm - not relevant for the published version
});

QUnit.test('Test lower danger boundary', function (assert) {
    assert.ok(latLonToGeohexa(-46, 0, 0));
    // same comment as testUpperDangerBoundary
});

QUnit.test('Test all lat in point one increments', function (assert) {
    var acc = 3;
    seed = 180;
    var totalLength = 0;
    var count = 0;
    var max = 0;
    var min = 999999;
    for (var i = -900; i < 900 + 1; i++) {
        count = count + 1;
        var testLat = i / 10;
        var testLon = random() * 360 - 180;
        var hexa = latLonToGeohexa(testLat, testLon, acc);
        if (hexa.length > max) {
            max = hexa.length;
        }
        if (hexa.length < min) {
            min = hexa.length;
        }
        var latLon = geohexaToLatLon(hexa);
        var error = distance(testLat, testLon, latLon.lat, latLon.lon);
        assert.ok(error < acc);
        totalLength = totalLength + hexa.length;
    }
    assert.equal(10, max);
    var log = '\nTotal length of geohexa for all lat in 0.1 increments at ' + acc + ' meter accuracy: ' + totalLength;
    log += '\nAverage length of geohexa: ' + totalLength / count;
    log += '\nMin length of geohexa: ' + min;
    log += '\nMax length of geohexa: ' + max + '\n';
    console.log(log);
});

QUnit.test('Test all lon in point one increments', function (assert) {
    var acc = 3;
    seed = 180;
    var totalLength = 0;
    var count = 0;
    var max = 0;
    var min = 999999;
    for (var i = -1800; i < 1800 + 1; i++) {
        count = count + 1;
        var testLat = random() * 180 - 90;
        var testLon = i / 10;
        var hexa = latLonToGeohexa(testLat, testLon, acc);
        if (hexa.length > max) {
            max = hexa.length;
        }
        if (hexa.length < min) {
            min = hexa.length;
        }
        var latLon = geohexaToLatLon(hexa);
        var error = distance(testLat, testLon, latLon.lat, latLon.lon);
        assert.ok(error < acc);
        totalLength = totalLength + hexa.length;
    }
    assert.equal(10, max);
    var log = '\nTotal length of geohexa for all lon in 0.1 increments at ' + acc + ' meter accuracy: ' + totalLength;
    log += '\nAverage length of geohexa: ' + totalLength / count;
    log += '\nMin length of geohexa: ' + min;
    log += '\nMax length of geohexa: ' + max + '\n';
    console.log(log);
});
