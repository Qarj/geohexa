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

    public function testLatlonToGeohexa()
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

}
