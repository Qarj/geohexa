# Geohexa Step by Step

This document describes the geohexa algorithm used by the JavaScript, Python, Java, and PHP implementations in this repository. It is intended to be the shared, language-neutral reference for discussion, reimplementation, and verification.

## 1. Idea in one paragraph

Geohexa turns a latitude and longitude into one short base-36 string by repeatedly subdividing the world into 36 parts, alternating between longitude and latitude. The first digit chooses 1 of 36 longitude bands, the second digit chooses 1 of 36 latitude bands inside that result, the third digit chooses 1 of 36 smaller longitude bands, and so on. Decoding does the reverse: it finds the final rectangular cell selected by the string and returns the midpoint of that cell.

## 2. Alphabet and digit values

Geohexa uses 36 symbols:

- `0` to `9`
- `a` to `z`

Each symbol has a digit value from 0 to 35.

- `0` to `9` mean 0 to 9.
- `a` means 10.
- `b` means 11.
- ...
- `z` means 35.

The reference implementations decode input case-insensitively. One implementation emits an uppercase `L` instead of lowercase `l` for readability, but mathematically it is still the same base-36 digit with value 21.

Digit positions alternate by axis:

- positions 1, 3, 5, ... are longitude digits
- positions 2, 4, 6, ... are latitude digits

So a geohexa of length `n` contains:

- `ceil(n / 2)` longitude digits
- `floor(n / 2)` latitude digits

Odd-length geohexas are therefore valid.

## 3. Starting cell and normalization

The algorithm starts from the whole Earth as one rectangular cell:

- longitude interval `[-180, 180)`
- latitude interval `[-90, 90)`

These intervals are treated as half-open so that each location falls into exactly one cell.

For encoding, the coordinate is first shifted into non-negative ranges:

- `X = lon + 180`, so `X` lies in `[0, 360)`
- `Y = lat + 90`, so `Y` lies in `[0, 180)`

The reference implementations also apply two boundary rules:

- longitude `180` is rewritten as `-180`, because those are the same meridian
- latitude `90` is clamped slightly downward to `89.99999999` so that it stays inside the open upper bound of the interval

## 4. Geometry of a geohexa cell

After `m` longitude digits, the remaining longitude width is:

- `W_m = 360 / 36^m` degrees

After `n` latitude digits, the remaining latitude height is:

- `H_n = 180 / 36^n` degrees

Therefore:

- a code of length `2k` has cell width `360 / 36^k` and cell height `180 / 36^k`
- a code of length `2k + 1` has cell width `360 / 36^(k + 1)` and cell height `180 / 36^k`

Every extra longitude digit divides width by 36. Every extra latitude digit divides height by 36. Every pair of digits divides both axes by 36.

Two geohexas that share a prefix of length `p` lie in the same parent cell defined by that prefix. This is why nearby places tend to share their leading characters.

## 5. Decoding: geohexa to latitude and longitude

Before validation and decoding, normalize the input geohexa as follows:

- remove all space characters
- convert the remaining characters to lower case

The algorithm below operates on that normalized string.

Let the geohexa be `g_1 g_2 ... g_L`, and let `v_i` be the base-36 value of `g_i`.

Start with:

- `x = 0`
- `y = 0`
- `w = 360`
- `h = 180`

Now read the digits from left to right.

For each odd-position digit `g_i` (longitude digit):

- set `w = w / 36`
- set `x = x + v_i * w`

For each even-position digit `g_i` (latitude digit):

- set `h = h / 36`
- set `y = y + v_i * h`

At the end, `(x, y)` is the southwest corner of the selected cell in normalized coordinates, and `(w, h)` is the size of that cell.

The decoded point is the midpoint of that cell:

- `lon = x - 180 + w / 2`
- `lat = y - 90 + h / 2`

This midpoint rule is the key point of the whole system. A geohexa does not decode to a corner or to an exact original measurement. It decodes to the center of the final cell.

### Simple decoding examples

| Geohexa      | Longitude work                                              | Latitude work                                            | Decoded midpoint |
| ------------ | ----------------------------------------------------------- | -------------------------------------------------------- | ---------------- |
| empty string | no longitude subdivision, so `w = 360` and `x = 0`          | no latitude subdivision, so `h = 180` and `y = 0`        | `(0, 0)`         |
| `0`          | first longitude cell: value `0`, so interval `[-180, -170)` | latitude still spans `[-90, 90)`                         | `(0, -175)`      |
| `00`         | same longitude interval `[-180, -170)`                      | first latitude cell: value `0`, so interval `[-90, -85)` | `(-87.5, -175)`  |
| `zz`         | longitude value `35`, so interval `[170, 180)`              | latitude value `35`, so interval `[85, 90)`              | `(87.5, 175)`    |

The empty string is therefore a valid geohexa. In this repository it is the null geohexa, and it represents the midpoint of the whole-Earth cell: `(0, 0)`.

## 6. Encoding: latitude and longitude to geohexa

Encoding is the inverse search process.

Start with the normalized coordinate `(X, Y)` and with:

- `x_fragment = X`
- `y_fragment = Y`
- `w = 360`
- `h = 180`
- `geohexa = ""`

Then alternate longitude and latitude steps.

### Longitude step

1. Replace `w` with `w / 36`.
2. Compute `q = floor(x_fragment / w)`.
3. Append the base-36 digit for `q`.
4. Replace `x_fragment` with `x_fragment - q * w`.

This selects the correct longitude subcell and keeps only the remainder that still needs to be located inside that subcell.

### Latitude step

1. Replace `h` with `h / 36`.
2. Compute `q = floor(y_fragment / h)`.
3. Append the base-36 digit for `q`.
4. Replace `y_fragment` with `y_fragment - q * h`.

This does the same thing for latitude.

### Stopping rule

After each appended digit, decode the current partial geohexa back to a midpoint and measure the geographic distance from that midpoint to the original coordinate.

If that error is smaller than the requested accuracy, stop and return the current geohexa.

The reference implementations use the haversine formula with Earth radius `6371 km` to compute this distance in meters.

Because the error is checked after every digit, the algorithm may stop immediately after a longitude digit. That is why odd-length results are valid.

The empty string is also a valid encoded result: if the original point is already within the requested accuracy of `(0, 0)`, then the null geohexa is accurate enough and no digits are needed.

## 7. Worked example

Encode the point:

- latitude `51.481874`
- longitude `-0.112564`

Normalize it:

- `X = -0.112564 + 180 = 179.887436`
- `Y = 51.481874 + 90 = 141.481874`

Now alternate longitude and latitude.

| Step | Axis      | Cell size                   | Digit value                           | Digit | Selected interval                |
| ---- | --------- | --------------------------- | ------------------------------------- | ----- | -------------------------------- |
| 1    | longitude | `360 / 36 = 10`             | `floor(179.887436 / 10) = 17`         | `h`   | `[-10, 0)`                       |
| 2    | latitude  | `180 / 36 = 5`              | `floor(141.481874 / 5) = 28`          | `s`   | `[50, 55)`                       |
| 3    | longitude | `360 / 36^2 = 0.2777777778` | `floor(9.887436 / 0.2777777778) = 35` | `z`   | `[-0.2777777778, 0)`             |
| 4    | latitude  | `180 / 36^2 = 0.1388888889` | `floor(1.481874 / 0.1388888889) = 10` | `a`   | `[51.3888888889, 51.5277777778)` |

After four digits the prefix is `hsza`.

Its decoded midpoint is:

- longitude `-0.1388888889`
- latitude `51.4583333333`

That is already in the right part of London, but it is not yet within the default 3 meter target. Continuing the same process yields:

- `hszaLoe3t`

When decoded, that midpoint is within the requested accuracy of the original coordinate.

## 8. Accuracy and interpretation

The main guarantee provided by the algorithm is:

- decoding the encoded result returns a point within the requested distance threshold of the original input point

More precisely, if `encode(lat, lon, accuracy)` returns `g`, then `decode(g)` is the midpoint of a cell whose center is less than `accuracy` meters from `(lat, lon)` according to the chosen distance formula.

There is also an exact midpoint property:

- if a finite geohexa `g` is decoded to its midpoint and that midpoint is encoded again with a sufficiently small positive tolerance, the result is `g` again

This follows from the fact that the midpoint lies inside that exact cell and reaches zero decoding error as soon as the same sequence of digits has been reconstructed.

Note that geohexa precision is not spatially uniform in meters. A degree of longitude covers less physical distance near the poles than near the equator, so the same angular cell width corresponds to different east-west distances at different latitudes.

## 9. Boundary and validation rules

The repository implementations agree on the following practical rules:

- decode removes space characters before processing
- decode accepts upper or lower case input
- decode rejects any character outside base 36
- encode expects latitude in `[-90, 90]`
- encode expects longitude in `[-180, 180]`
- the empty string is valid and decodes to `(0, 0)`
- odd-length strings are valid because the alternation may end after a longitude digit

One more implementation detail matters in practice: a request for exact zero-meter tolerance does not generally make sense for arbitrary measured coordinates, because most points are not exact midpoints of finite cells. The implementations therefore protect themselves with a very small positive minimum tolerance when needed.

## 10. Why this representation is useful

Geohexa has a few structural advantages:

- one string replaces a latitude and longitude pair
- the string is case-insensitive on input
- spaces can be inserted when sharing or reading a geohexa and are ignored on decode
- there are no minus signs or decimal points
- longer strings mean higher precision
- shared prefixes represent shared geographic parent cells

This makes the format compact, easy to say, and easy to compare visually.

## 11. Reference background

The following references are useful background for understanding the accuracy discussion used by the implementations:

- OpenStreetMap Wiki, Precision of coordinates: https://wiki.openstreetmap.org/wiki/Precision_of_coordinates
- Wikipedia, Haversine formula: https://en.wikipedia.org/wiki/Haversine_formula
