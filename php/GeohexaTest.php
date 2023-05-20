<?php

use PHPUnit\Framework\TestCase;

require_once 'vendor/autoload.php';

class GeohexaTest extends TestCase
{
    private $geohexa;

    protected function setUp(): void
    {
        $this->geohexa = new Geohexa();
    }

    public function testOvalTubeSpecificValue()
    {
        $hexa = $this->geohexa->latlonToGeohexa(51.481874, -0.112564);
        $this->assertEquals('hszaLoe3t', $hexa);
    }

    public function testGeohexaToLatlon()
    {
        list($lat, $lon) = $this->geohexa->geohexaToLatlon('hszaLoe3t');
        $this->assertEquals(51.48185656721534, $lat, '', 0.0001);
        $this->assertEquals(-0.11256441948635994, $lon, '', 0.0001);
    }

    public function testMaxLatLon()
    {
        $this->assertTrue(strlen($this->geohexa->latlonToGeohexa(90, 180)) >= 2);
    }

    public function testMinLatLon()
    {
        $this->assertTrue(strlen($this->geohexa->latlonToGeohexa(-90, -180)) >= 2);
    }

    public function testGeohexaToLatlonWithinSpecification()
    {
        list($lat, $lon) = $this->geohexa->geohexaToLatlon('zz');
        $this->assertIsNumeric($lat);
        $this->assertIsNumeric($lon);
        $this->assertTrue($lat >= -90);
        $this->assertTrue($lat <= 90);
        $this->assertTrue($lon >= -180);
        $this->assertTrue($lon <= 180);
    }

    public function testDistanceBetweenTwoLatLons()
    {
        // from south to north of Vauxhall Bridge - approx 200 meters
        $this->assertEquals(round($this->geohexa->distance(51.487141, -0.125873, 51.488178, -0.128224), 8), 199.48427672);
    }

    public function testLatlonToGeohexaAndBackAccuracyForTenKm()
    {
        $test_lat = 1.285864;
        $test_lon = 103.851831; // Singapore Boat Quay
        $geohexa = $this->geohexa->latlonToGeohexa($test_lat, $test_lon, 10000);
        list($lat, $lon) = $this->geohexa->geohexaToLatlon($geohexa);
        $error = $this->geohexa->distance($test_lat, $test_lon, $lat, $lon);
        $this->assertTrue($error < 10000);
    }

    public function testLatlonToGeohexaAndBackAccuracyForMeter()
    {
        $test_lat = 64.123565;
        $test_lon = -21.805507; // Reykjavik Brewery
        $geohexa = $this->geohexa->latlonToGeohexa($test_lat, $test_lon, 1);
        list($lat, $lon) = $this->geohexa->geohexaToLatlon($geohexa);
        $error = $this->geohexa->distance($test_lat, $test_lon, $lat, $lon);
        $this->assertTrue($error < 1);
    }

    public function testLatlonToGeohexaAndBackAccuracyForMillimeter()
    {
        $test_lat = -46.896522;
        $test_lon = 168.130336; // NZ Oyster Bar
        $geohexa = $this->geohexa->latlonToGeohexa($test_lat, $test_lon, 0.001);
        list($lat, $lon) = $this->geohexa->geohexaToLatlon($geohexa);
        $error = $this->geohexa->distance($test_lat, $test_lon, $lat, $lon);
        $this->assertTrue($error < 0.001);
    }

    public function testManyRandomLatLons()
    {
        srand(42);
        $total_length = 0;
        $many = 10000;
        $accuracy = 3;

        for ($i = 0; $i < $many; $i++) {
            $test_lat = (rand() / getrandmax() * 180) - 90;
            $test_lon = (rand() / getrandmax() * 360) - 180;
            $geohexa = $this->geohexa->latlonToGeohexa($test_lat, $test_lon, $accuracy);
            list($lat, $lon) = $this->geohexa->geohexaToLatlon($geohexa);
            $error = $this->geohexa->distance($test_lat, $test_lon, $lat, $lon);
            $this->assertTrue($error < $accuracy);
            $total_length = $total_length + strlen($geohexa);
        }

        echo "\nTotal length of geohexa for " . $many . " random co-ordinates at " . $accuracy . " meter accuracy: " . $total_length;
        echo "\nAverage length of geohexa: " . ($total_length / $many) . "\n";
    }

}
