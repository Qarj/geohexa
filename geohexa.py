#!/usr/bin/env python3
version = "0.1.0"

import sys, argparse, math


def distance(lat1, lon1, lat2, lon2):

    latDistance = math.radians(lat2 - lat1)
    lonDistance = math.radians(lon2 - lon1)

    # Haversine formula
    sin_lat = math.sin(latDistance / 2)
    sin_lon = math.sin(lonDistance / 2)
    a = sin_lat * sin_lat + math.cos(math.radians(lat1)) * math.cos(math.radians(lat2)) * sin_lon * sin_lon
    c = 2 * math.atan2(math.sqrt(a), math.sqrt(1 - a))

    earth_radius = 6371
    distance = earth_radius * c * 1000
    # convert to meters

    return distance


def lat_valid(lat):
    if lat < -90 or lat > 90:
        return False
    return True


def lon_valid(lat):
    if lat < -180 or lat > 180:
        return False
    return True


def prepare_lat(lat):
    if lat > 89.99999999:
        lat = 89.99999999
    lat = lat + 90
    return lat


def prepare_lon(lon):
    if lon == 180:
        lon = -180  # 180E is same as 180W
    lon = lon + 180
    return lon


def compress(num, cell_size):
    cell, remainder = divmod(num, cell_size)
    base36 = base36digits_output[int(cell)]

    if args.verbose:
        print(
            "base36 digit: ",
            base36,
            "(" + str(int(cell)) + ")  cell_size_degrees[",
            cell_size,
            "]   fragment[",
            num,
            "]  remainder_to_go[",
            remainder,
            "]",
            sep="",
        )

    return base36, remainder


# function only used for verbose output
def lat_bottom(all_latgeo):
    lat = 0

    if len(all_latgeo) == 0:  # worst case for longitude size is at the equator, this also fits definition of null lat
        return lat

    lat_height = 180
    for d in all_latgeo.lower():
        lat = lat + (lat_height / 36) * base36digits.index(d)
        lat_height = lat_height / 36

    return lat - 90


def latlon_to_geohexa(lat, lon, accuracy=3):
    if accuracy < 0.00000001:  # need to be sensible about the accuracy to prevent division by zero
        accuracy = 0.00000001
    geohexa = ""
    all_latgeo = ""
    lat_fragment = prepare_lat(lat)
    lon_fragment = prepare_lon(lon)
    lat_unit = 180
    lon_unit = 360

    # null is a valid geohexa - represents 0, 0
    error = distance(lat, lon, 0, 0)
    if error < accuracy:
        return ""

    lat_cell_size = distance(lat_bottom(all_latgeo), 0, lat_bottom(all_latgeo) + lat_unit, 0)

    while True:
        lon_unit = lon_unit / 36
        longeo, lon_fragment = compress(lon_fragment, lon_unit)
        geohexa = geohexa + longeo

        geolat, geolon = geohexa_to_latlon(geohexa)
        error = distance(lat, lon, geolat, geolon)

        if args.verbose:
            lon_cell_size = distance(lat_bottom(all_latgeo), 0, lat_bottom(all_latgeo), lon_unit)
            print(
                "Current lat *lon* cell size meters: ",
                lat_cell_size,
                " , ",
                lon_cell_size,
                "   error in meters[",
                error,
                "]\n...",
                sep="",
            )

        if error < accuracy:
            break

        lat_unit = lat_unit / 36
        latgeo, lat_fragment = compress(lat_fragment, lat_unit)
        geohexa = geohexa + latgeo
        all_latgeo = all_latgeo + latgeo

        geolat, geolon = geohexa_to_latlon(geohexa)
        error = distance(lat, lon, geolat, geolon)

        if args.verbose:
            lat_cell_size = distance(lat_bottom(all_latgeo), 0, lat_bottom(all_latgeo) + lat_unit, 0)
            print(
                "Current *lat* lon cell size meters: ",
                lat_cell_size,
                " , ",
                lon_cell_size,
                "   error in meters[",
                error,
                "]\n\n",
                sep="",
            )

        if error < accuracy:
            break

    return geohexa


def geohexa_to_latlon(hexa):
    lat, lon = 0, 0
    i = 0
    lat_width = 180
    lon_width = 360

    for d in hexa.lower():
        i = i + 1

        integer, remainder = divmod(i, 2)
        if remainder == 1:  # we have a lon geohexa digit
            lon = lon + (lon_width / 36) * base36digits.index(d)
            lon_width = lon_width / 36
        else:
            lat = lat + (lat_width / 36) * base36digits.index(d)
            lat_width = lat_width / 36

    # calculate mid point of cell
    lat = lat - 90 + lat_width / 2
    lon = lon - 180 + lon_width / 2
    if args.verbose:
        print(hexa, "as lat, lon", lat, lon)
    return lat, lon


def in_range(hexa):
    in_range = True
    for c in hexa.lower():
        try:
            base36digits.index(c)
        except ValueError:
            in_range = False
    return in_range


parser = argparse.ArgumentParser(description="Convert a lat and lon to a geohexa, vice versa.")
parser.add_argument("--lat", dest="lat", required=False, action="store", help="Latitude")
parser.add_argument("--lon", dest="lon", required=False, action="store", help="Longitude")
parser.add_argument(
    "--acc",
    dest="acc",
    required=False,
    action="store",
    help="Accuracy for conversion to geohexa in meters, default is 3",
    default=3,
    type=float,
)
parser.add_argument("--hexa", dest="hexa", required=False, action="store", help="geohexa", default="")
parser.add_argument("--version", action="version", version=version)
parser.add_argument(
    "--verbose", action="store_true", dest="verbose", default=False, help="Will output extra info on calculation steps"
)
parser.add_argument(
    "--null", action="store_true", dest="null", default=False, help="Will output the lat and lon of a null geohexa"
)
args = parser.parse_args()

base36digits_output = "0123456789abcdefghijkLmnopqrstuvwxyz"
base36digits = base36digits_output.lower()

if args.lat and args.lon:
    lat = float(args.lat)
    lon = float(args.lon)
    if lat_valid(lat) and lon_valid(lon):
        geohexa = latlon_to_geohexa(lat, lon, accuracy=args.acc)
        print("geohexa is", geohexa)
        check_lat, check_lon = geohexa_to_latlon(geohexa)
        print("Converted back to lat, lon: ", check_lat, ", ", check_lon, sep="")
    else:
        print("Lat must be in range -90 to 90, Lon must be -180 to 180")
    sys.exit()

if args.hexa or args.null:
    if not in_range(args.hexa):
        print("geohexa digits must only be 0-9, a-z (or A-Z). Example: Qarj")
    else:
        lat, lon = geohexa_to_latlon(args.hexa)
        print("Lat:", lat, "Lon:", lon)
