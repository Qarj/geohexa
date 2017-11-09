# geohexa 0.1.0
Latitude and Longitude combined into one small string

## Principles
* Compress co-ordinate size by using alphabet as well as digits
* Case insensitive
* No decimal point to worry about, or negative signs
* No need to worry about which is the latitude and which is the longitude
* More digits = more precision
* A geohexa with similar (higher significant digit) numbers are near each other

## Why
* Separate Latitude and Longitude has multiple representations making it particularly confusing
* Easy to say and type into a mobile device - don't have to worry about case or changing keyboard modes for special characters
* No need for separate Lat and Lon headings / fields

## How

* Uses 0 to 9 + a-z giving 36 digits - hexatrigesimal
* First digit is a longitude - compressing all possible longitude into 36 digits
* second digit is a latitude
* Now the you have compressed the world into a 36 x 36 grid, you location is the midpoint of the 'rectangle'
* For more precision, keep adding digits (lon lat lon lat lon ...)
* can be an odd number of digits

That's it!

## Example geohexas

Geohexa | Lat , Lon | Notes
:--------------- | :---------- | :----------
`null` | 0 , 0 | Null geohexa
0 | 0 , -175 | Single character geohexa
00 | -87.5 , -175 | Two character geohexa
zz | 87.5 , 175 |
hsxatoom | 51.483892, -0.604316 | Windsor Castle
hszaotyy | 51.504468, -0.085198 | The Shard, London
hszaounu | 51.507898, -0.087555 | London Bridge
hszaqu88 | 51.505540, -0.075338 | Tower Bridge
pqe5cdjz | 40.748403, 73.985661 | Empire State Building

## Example implementations

### Python 3

#### Example - lat lon to geohexa
`geohexa --lat 51.481874 --lon -0.112564`

Produces output:
```
geohexa is hszaLoe3t
Converted back to lat, lon: 51.48185656721534, -0.11256441948635994
```

By default, the accuracy is at least within 3 meters.

#### Example - geohexa to lat lon

`geohexa --hexa aqL5k4f8my`

Produces output:
```
Lat: 40.71083658407638 Lon: -74.00899669924554
```

#### Example - accurate to at least 0.1 of a meter

`geohexa --lat -46.896522 --lon 168.130336 --acc 0.1`

Produces output:
```
geohexa is y8tm9cofw68
Converted back to lat, lon: -46.8965212881992, 168.1303359584042
```

#### Further notes

`--verbose` flag produces a lot of info on how the geohexa is calculated

`--null` flag outputs the lat lon of a null geohexa

`--help` flag outputs help

`test_geohexa.py` runs the unit tests