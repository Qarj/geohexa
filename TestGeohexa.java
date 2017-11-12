package geohexa;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.Random;

public class TestGeohexa {

    private final static String BASE_36_DIGITS = "0123456789abcdefghijklmnopqrstuvwxyz";

    double MM = 0.001;
    double CM = 0.01;
    double METER = 1;
    double TEN_METER = 10;
    double HUNDRED_METER = 100;
    double KM = 1_000;
    double TEN_KM = 10_000;
    double THOUSAND_KM = 1_000_000;

    double ACCURACY = 3;
    double MANY=10_000;

    @Test
    public void testMaxLatLon() {
        Geohexa subject = new Geohexa();
        String hexa = subject.latlon_to_geohexa(90,180);
        assertTrue(hexa.length() >= 2);
    }

    @Test
    public void testMinLatLon() {
        Geohexa subject = new Geohexa();
        String hexa = subject.latlon_to_geohexa(-90,-180);
        assertTrue(hexa.length() >= 2);
    }

    @Test
    public void testGeohexaToLatLonWithinSpecification() {
        Geohexa subject = new Geohexa();
        Geohexa.Coordinate latLon = subject.geohexa_to_latlon("zz");
        assertTrue(latLon.getLat() >= -90);
        assertTrue(latLon.getLat() <= 90);
        assertTrue(latLon.getLon() >= -180);
        assertTrue(latLon.getLon() <= 180);
    }

    @Test
    public void testDistanceBetweenTwoLatLons() {
        Geohexa subject = new Geohexa();
        assertEquals(199.48427672D, subject.distance(51.487141, -0.125873, 51.488178, -0.128224), 0.00000001D);
    }
    
    @Test
    public void testLatLontoGeohexaAndBackAccuracyForTenKM() {
        Geohexa subject = new Geohexa();
        double testLat = 1.285864;
        double testLon = 103.851831; // Singapore Boat Quay
        String hexa = subject.latlon_to_geohexa(testLat, testLon);
        Geohexa.Coordinate latLon = subject.geohexa_to_latlon(hexa);
        double error = subject.distance(testLat, testLon, latLon.getLat(), latLon.getLon());
        assertTrue (error < TEN_KM);
    }
    
    @Test
    public void testLatLontoGeohexaAndBackAccuracyForMeter() {
        Geohexa subject = new Geohexa();
        double testLat = 64.123565;
        double testLon = -21.805507; // Reykjavik Brewery
        String hexa = subject.latlon_to_geohexa(testLat, testLon, METER);
        Geohexa.Coordinate latLon = subject.geohexa_to_latlon(hexa);
        double error = subject.distance(testLat, testLon, latLon.getLat(), latLon.getLon());
        assertTrue (error < METER);
    }
    
    @Test
    public void testLatLontoGeohexaAndBackAccuracyForMillimeter() {
        Geohexa subject = new Geohexa();
        double testLat = -46.896522;
        double testLon = 168.130336; // NZ Oyster Bar
        String hexa = subject.latlon_to_geohexa(testLat, testLon, MM);
        Geohexa.Coordinate latLon = subject.geohexa_to_latlon(hexa);
        double error = subject.distance(testLat, testLon, latLon.getLat(), latLon.getLon());
        assertTrue (error < MM);
    }

    @Test
    public void testManyRandomLatLons() {
        Geohexa subject = new Geohexa();
        Random rand = new Random(42);
        double totalLength = 0;
        for (double i=1; i<MANY+1; i++) {
            double testLat = rand.nextDouble()*180 - 90;
            double testLon = rand.nextDouble()*360 - 180;
            //System.out.println("my randoms: " + testLat + ", " + testLon );
            String hexa = subject.latlon_to_geohexa(testLat, testLon, ACCURACY);
            Geohexa.Coordinate latLon = subject.geohexa_to_latlon(hexa);
            double error = subject.distance(testLat, testLon, latLon.getLat(), latLon.getLon());
            assertTrue (error < ACCURACY);
            totalLength = totalLength + hexa.length();
        }
        System.out.println("\nTotal length of geohexa for " + MANY + " random co-ordinates at " +  ACCURACY + " meter accuracy: " + totalLength);
        System.out.println("Average length of geohexa: " + totalLength/MANY + "\n");
    }

    @Test
    public void testManyRandomLatLonsNearEquator() {
        Geohexa subject = new Geohexa();
        Random rand = new Random(42);
        double totalLength = 0;
        for (double i=1; i<MANY+1; i++) {
            double testLat = rand.nextDouble()*10 - 5;
            double testLon = rand.nextDouble()*360 - 180;
            String hexa = subject.latlon_to_geohexa(testLat, testLon, ACCURACY);
            Geohexa.Coordinate latLon = subject.geohexa_to_latlon(hexa);
            double error = subject.distance(testLat, testLon, latLon.getLat(), latLon.getLon());
            assertTrue (error < ACCURACY);
            totalLength = totalLength + hexa.length();
        }
        System.out.println("\nTotal length of geohexa for " + MANY + " random co-ordinates near the Equator at " +  ACCURACY + " meter accuracy: " + totalLength);
        System.out.println("Average length of geohexa: " + totalLength/MANY + "\n");
    }

    @Test
    public void testManyRandomLatLonsNearNorthPole() {
        Geohexa subject = new Geohexa();
        Random rand = new Random(42);
        double totalLength = 0;
        for (double i=1; i<MANY+1; i++) {
            double testLat = rand.nextDouble()*10 + 80;
            double testLon = rand.nextDouble()*360 - 180;
            String hexa = subject.latlon_to_geohexa(testLat, testLon, ACCURACY);
            Geohexa.Coordinate latLon = subject.geohexa_to_latlon(hexa);
            double error = subject.distance(testLat, testLon, latLon.getLat(), latLon.getLon());
            assertTrue (error < ACCURACY);
            totalLength = totalLength + hexa.length();
        }
        System.out.println("\nTotal length of geohexa for " + MANY + " random co-ordinates near the North Pole at " +  ACCURACY + " meter accuracy: " + totalLength);
        System.out.println("Average length of geohexa: " + totalLength/MANY + "\n");
    }

    @Test
    public void testManyRandomLatLonsNearLat42To52() {
        Geohexa subject = new Geohexa();
        Random rand = new Random(42);
        double totalLength = 0;
        for (double i=1; i<MANY+1; i++) {
            double testLat = rand.nextDouble()*10 + 42;
            double testLon = rand.nextDouble()*360 - 180;
            String hexa = subject.latlon_to_geohexa(testLat, testLon, ACCURACY);
            Geohexa.Coordinate latLon = subject.geohexa_to_latlon(hexa);
            double error = subject.distance(testLat, testLon, latLon.getLat(), latLon.getLon());
            assertTrue (error < ACCURACY);
            totalLength = totalLength + hexa.length();
        }
        System.out.println("\nTotal length of geohexa for " + MANY + " random co-ordinates near lat 42 to 52 at " +  ACCURACY + " meter accuracy: " + totalLength);
        System.out.println("Average length of geohexa: " + totalLength/MANY + "\n");
    }
    
    @Test
    public void testCaseInsensitiveInputOk() {
        Geohexa subject = new Geohexa();
        Geohexa.Coordinate latLon1 = subject.geohexa_to_latlon("iilloo");
        Geohexa.Coordinate latLon2 = subject.geohexa_to_latlon("IILLOO");
        assertTrue ( latLon1.getLat() == latLon2.getLat());
        assertTrue ( latLon1.getLon() == latLon2.getLon());
    }

    @Test
    public void testSingleNullGeohexa() {
        Geohexa subject = new Geohexa();
        Geohexa.Coordinate latLon = subject.geohexa_to_latlon("");
        assertTrue ( latLon.getLat() == 0 );
        assertTrue ( latLon.getLon() == 0 );
    }

    @Test
    public void testThreeDigitGeohexa() {
        Geohexa subject = new Geohexa();
        Geohexa.Coordinate latLon = subject.geohexa_to_latlon("Tim");
        String hexa = subject.latlon_to_geohexa(latLon.getLat(), latLon.getLon());
        assertEquals ("tim", hexa );
    }

    @Test
    public void testFourDigitGeohexa() {
        Geohexa subject = new Geohexa();
        Geohexa.Coordinate latLon = subject.geohexa_to_latlon("Qarj");
        String hexa = subject.latlon_to_geohexa(latLon.getLat(), latLon.getLon());
        assertEquals ("qarj", hexa );
    }

    @Test
    public void testAllSingleDigitGeohexa() {
        Geohexa subject = new Geohexa();
        for (char c : BASE_36_DIGITS.toCharArray()) {
            String d1 = Character.toString(c); 
            Geohexa.Coordinate latLon = subject.geohexa_to_latlon(d1);
            String hexa = subject.latlon_to_geohexa(latLon.getLat(), latLon.getLon()).toLowerCase();
            assertEquals (d1, hexa );
        }
    }

    @Test
    public void testAllTwoDigitGeohexa() {
        Geohexa subject = new Geohexa();
        for (char c : BASE_36_DIGITS.toCharArray()) {
            String d1 = Character.toString(c); 
            for (char d : BASE_36_DIGITS.toCharArray()) {
                String d2 = Character.toString(d); 
                Geohexa.Coordinate latLon = subject.geohexa_to_latlon(d1+d2);
                String hexa = subject.latlon_to_geohexa(latLon.getLat(), latLon.getLon()).toLowerCase();
                assertEquals (d1+d2, hexa );
            }
        }
    }

    @Test
    public void testAllThirdFourthDigitGeohexa() {
        Geohexa subject = new Geohexa();
        for (char c : BASE_36_DIGITS.toCharArray()) {
            String d1 = Character.toString(c); 
            for (char d : BASE_36_DIGITS.toCharArray()) {
                String d2 = Character.toString(d); 
                Geohexa.Coordinate latLon = subject.geohexa_to_latlon("00"+d1+d2);
                String hexa = subject.latlon_to_geohexa(latLon.getLat(), latLon.getLon()).toLowerCase();
                assertEquals ("00"+d1+d2, hexa );
            }
        }
    }

    @Test
    public void testShouldOuputNullDigitGeohexa() {
        Geohexa subject = new Geohexa();
        String hexa = subject.latlon_to_geohexa(0, 0, 0);
        assertEquals (0, hexa.length() );
    }
    
    @Test
    public void testUpperDangerBoundary() {
        Geohexa subject = new Geohexa();
        String hexa = subject.latlon_to_geohexa(64, 0, 0);
        // testing that there is no division by 0 exception - this test applies to an alternate more optimal (and complicated) algorithm - not relevant for the published version
    }

    @Test
    public void testLowerDangerBoundary() {
        Geohexa subject = new Geohexa();
        String hexa = subject.latlon_to_geohexa(-46, 0, 0);
        // same comment as testUpperDangerBoundary
    }

    @Test
    public void testAllLatInPointOneIncrements() {
        Geohexa subject = new Geohexa();
        double acc = 3;
        Random rand = new Random(180);
        double totalLength = 0;
        double count = 0;
        double max = 0;
        double min = 999999;
        for (double i=-900; i<900+1; i++) {
            count = count + 1;
            double testLat = i / 10;
            double testLon = rand.nextDouble()*360 - 180;
            String hexa = subject.latlon_to_geohexa(testLat, testLon, acc);
            if (hexa.length() > max) {
                max = hexa.length();
            }
            if (hexa.length() < min) {
                min = hexa.length();
            }
            Geohexa.Coordinate latLon = subject.geohexa_to_latlon(hexa);
            double error = subject.distance(testLat, testLon, latLon.getLat(), latLon.getLon());
            assertTrue (error < acc);
            totalLength = totalLength + hexa.length();
        }
        assertEquals (10, max, 0.1);
        System.out.println("\nTotal length of geohexa for all lat in 0.1 increments at " + acc + " meter accuracy: " + totalLength);
        System.out.println("Average length of geohexa: " + totalLength/count);
        System.out.println("Min length of geohexa: " + min);
        System.out.println("Max length of geohexa: " + max + "\n");
    }

    @Test
    public void testAllLonInPointOneIncrements() {
        Geohexa subject = new Geohexa();
        double acc = 3;
        Random rand = new Random(180);
        double totalLength = 0;
        double count = 0;
        double max = 0;
        double min = 999999;
        for (double i=-1800; i<1800+1; i++) {
            count = count + 1;
            double testLat = rand.nextDouble()*180 - 90;
            double testLon = i / 10;
            String hexa = subject.latlon_to_geohexa(testLat, testLon, acc);
            if (hexa.length() > max) {
                max = hexa.length();
            }
            if (hexa.length() < min) {
                min = hexa.length();
            }
            Geohexa.Coordinate latLon = subject.geohexa_to_latlon(hexa);
            double error = subject.distance(testLat, testLon, latLon.getLat(), latLon.getLon());
            assertTrue (error < acc);
            totalLength = totalLength + hexa.length();
        }
        assertEquals (10, max, 0.1);
        System.out.println("\nTotal length of geohexa for all lon in 0.1 increments at " + acc + " meter accuracy: " + totalLength);
        System.out.println("Average length of geohexa: " + totalLength/count);
        System.out.println("Min length of geohexa: " + min);
        System.out.println("Max length of geohexa: " + max + "\n");
    }

}

// Setup JUnit Environment: https://www.tutorialspoint.com/junit/junit_environment_setup.htm

// How to know which test failed - it tells you on fail
// How to know the failed output?