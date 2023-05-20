# geohexa 0.1.0

![Python](https://github.com/Qarj/geohexa/workflows/Python/badge.svg)
![JavaScript](https://github.com/Qarj/geohexa/actions/workflows/JavaScript.yml/badge.svg)
![PHP](https://github.com/Qarj/geohexa/actions/workflows/PHP.yml/badge.svg)

Latitude and Longitude combined into one small string

Try it here: https://qarj.github.io/geohexa/

## Principles

-   Compress co-ordinate size by using alphabet as well as digits
-   Case insensitive
-   No decimal point to worry about, or negative signs
-   No need to worry about which is the latitude and which is the longitude
-   More digits = more precision
-   A geohexa with similar (higher significant digit) numbers are near each other

## Why

-   Separate Latitude and Longitude has multiple representations making it particularly confusing
-   Easy to say and type into a mobile device - don't have to worry about case or changing keyboard modes for special characters
-   No need for separate Lat and Lon headings / fields

## How

-   Uses 0-9 plus a-z giving 36 possible numbers in a single digit - a hexatrigesimal
-   First digit is a longitude - compressing all possible longitudes into 36 digits
-   second digit is a latitude
-   Now the you have compressed the world into a 36 x 36 grid, your location is the midpoint of the 'rectangle'
-   For more precision, keep adding digits (lon lat lon lat lon ...)
-   can end of a odd number of digits

That's it!

## Example geohexas

| Geohexa  | Lat , Lon            | Notes                    |
| :------- | :------------------- | :----------------------- |
| `null`   | 0 , 0                | Null geohexa             |
| 0        | 0 , -175             | Single character geohexa |
| 00       | -87.5 , -175         | Two character geohexa    |
| zz       | 87.5 , 175           |
| hsxatoom | 51.483892, -0.604316 | Windsor Castle           |
| hszaotyy | 51.504468, -0.085198 | The Shard, London        |
| hszaounu | 51.507898, -0.087555 | London Bridge            |
| hszaqu88 | 51.505540, -0.075338 | Tower Bridge             |
| pqe5cdjz | 40.748403, 73.985661 | Empire State Building    |

## Example implementations

The Android app, Pointy Arrow, implements the java version of geohexa. See it here:

https://play.google.com/store/apps/details?id=qarj.pointyarrow

### Python 3

#### Example - lat lon to geohexa

`geohexa --lat 51.481874 --lon -0.112564`

Produces output:

```txt
geohexa is hszaLoe3t
Converted back to lat, lon: 51.48185656721534, -0.11256441948635994
```

By default, the accuracy is at least within 3 meters.

#### Example - geohexa to lat lon

`geohexa --hexa aqL5k4f8my`

Produces output:

```txt
Lat: 40.71083658407638 Lon: -74.00899669924554
```

#### Example - accurate to at least 0.1 of a meter

`geohexa --lat -46.896522 --lon 168.130336 --acc 0.1`

Produces output:

```txt
geohexa is y8tm9cofw68
Converted back to lat, lon: -46.8965212881992, 168.1303359584042
```

#### Further notes

`--verbose` flag produces a lot of info on how the geohexa is calculated

`--null` flag outputs the lat lon of a null geohexa

`--help` flag outputs help

`test_geohexa.py` runs the unit tests

### Java

First compile the Java:

```sh
javac -d . Geohexa.java
javac -d . Run.java
```

#### Example - lat lon to geohexa

`java geohexa.Run 51.481874 -0.112564`

Produces output:

```txt
geohexa is hszaLoe3t
Converted back to lat, lon: 51.48185656721534, -0.11256441948635994
```

By default, the accuracy is at least within 3 meters.

#### Example - geohexa to lat lon

`java geohexa.Run aqL5k4f8my`

Produces output:

```txt
Lat: 40.71083658407638 Lon: -74.00899669924554
```

#### Example - accurate to at least 0.1 of a meter

`java geohexa.Run -46.896522 168.130336 0.1`

Produces output:

```txt
geohexa is y8tm9cofw68
Converted back to lat, lon: -46.8965212881992, 168.1303359584042
```

#### Further notes

To run the unit tests, ensure the JUnit environment is set up correctly. Refer to
https://www.tutorialspoint.com/junit/junit_environment_setup.htm for full details.

Then run `testJava.bat` on Windows machines to compile and execute the tests.

### JavaScript

After cloning the repo, double click on `geohexa.html`.

Or just head over to: https://qarj.github.io/geohexa/

#### Further notes

To run the unit tests, first do one time setup:

-   Install nodejs: https://nodejs.org/en/
-   Install karma `npm install -g karma`
-   Install qunit-qunit `npm install -g karma-qunit`
-   Install qunit `npm install -g qunitjs`

Then run the tests as follows:

-   `start karma start` (on Windows)
-   `karma run`

You can create your own JavaScript unit test config by changing directory to your project, then:

-   `karma init`

Which will create `karma.conf.js` (after you answer questions about your desired setup).

In an organisation with SSL interception, you may need to:

-   `npm config set strict-ssl false`
-   `npm cache verify`

### PHP

Check the [README.md](/php/README.md) in the php directory.
