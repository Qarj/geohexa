<?php

class Geohexa
{
    private $base36digits = "0123456789abcdefghijkLmnopqrstuvwxyz";

    public function latlonToGeohexa($lat, $lon, $accuracy = 3)
    {
        if ($accuracy < 0.00000001) {
            $accuracy = 0.00000001;
        }
        $geohexa = "";
        $all_latgeo = "";
        $lat_fragment = $this->prepareLat($lat);
        $lon_fragment = $this->prepareLon($lon);
        $lat_unit = 180;
        $lon_unit = 360;

        $error = $this->distance($lat, $lon, 0, 0);
        if ($error < $accuracy) {
            return "";
        }

        while (true) {
            $lon_unit = $lon_unit / 36;
            list($longeo, $lon_fragment) = $this->compress($lon_fragment, $lon_unit);
            $geohexa = $geohexa . $longeo;

            list($geolat, $geolon) = $this->geohexaToLatlon($geohexa);
            $error = $this->distance($lat, $lon, $geolat, $geolon);

            if ($error < $accuracy) {
                break;
            }

            $lat_unit = $lat_unit / 36;
            list($latgeo, $lat_fragment) = $this->compress($lat_fragment, $lat_unit);
            $geohexa = $geohexa . $latgeo;
            $all_latgeo = $all_latgeo . $latgeo;

            list($geolat, $geolon) = $this->geohexaToLatlon($geohexa);
            $error = $this->distance($lat, $lon, $geolat, $geolon);

            if ($error < $accuracy) {
                break;
            }
        }

        return $geohexa;
    }

    public function geohexaToLatlon($hexa)
    {
        $lat = 0;
        $lon = 0;
        $i = 0;
        $lat_width = 180;
        $lon_width = 360;

        for ($j = 0; $j < strlen($hexa); $j++) {
            $d = strtolower($hexa[$j]);
            $i = $i + 1;

            $integer = intval($i / 2);
            $remainder = $i % 2;
            if ($remainder == 1) {
                $lon = $lon + ($lon_width / 36) * strpos(strtolower($this->base36digits), $d);
                $lon_width = $lon_width / 36;
            } else {
                $lat = $lat + ($lat_width / 36) * strpos(strtolower($this->base36digits), $d);
                $lat_width = $lat_width / 36;
            }
        }

        $lat = $lat - 90 + $lat_width / 2;
        $lon = $lon - 180 + $lon_width / 2;

        return array($lat, $lon);
    }

    private function distance($lat1, $lon1, $lat2, $lon2)
    {
        $latDistance = deg2rad($lat2 - $lat1);
        $lonDistance = deg2rad($lon2 - $lon1);

        $sin_lat = sin($latDistance / 2);
        $sin_lon = sin($lonDistance / 2);
        $a = $sin_lat * $sin_lat + cos(deg2rad($lat1)) * cos(deg2rad($lat2)) * $sin_lon * $sin_lon;
        $c = 2 * atan2(sqrt($a), sqrt(1 - $a));

        $earth_radius = 6371;
        $distance = $earth_radius * $c * 1000;

        return $distance;
    }

    private function prepareLat($lat)
    {
        if ($lat > 89.99999999) {
            $lat = 89.99999999;
        }
        $lat = $lat + 90;
        return $lat;
    }

    private function prepareLon($lon)
    {
        if ($lon == 180) {
            $lon = -180;
        }
        $lon = $lon + 180;
        return $lon;
    }

    private function compress($num, $cell_size)
    {
        if ($cell_size == 0) {
            throw new InvalidArgumentException("Cell size cannot be zero.");
        }

        fwrite(STDERR, "num: $num\n");
        fwrite(STDERR, "cell_size: $cell_size\n");

        $cell = intval($num / $cell_size);
        fwrite(STDERR, "cell: $cell\n");
        $remainder = fmod($num, $cell_size);
        $base36 = $this->base36digits[$cell];

        return array($base36, $remainder);
    }
}
