package geohexa;

public class Geohexa {
    
    private final static String BASE_36_DIGITS_OUTPUT = "0123456789abcdefghijkLmnopqrstuvwxyz";
    private final static String BASE_36_DIGITS = BASE_36_DIGITS_OUTPUT.toLowerCase();

    private boolean VERBOSE; // set to true to get detailed information on calculation steps

    void setVERBOSE () {
        VERBOSE = true;
    }

    public void cli(String[] args) {
        
        double lat = 0;
        double lon = 0;
        double acc = 3;
        String hexa = "";

        if (args.length == 2 || args.length == 3) {
            //System.out.println("Converting a lat and lon to a geohexa");
            
            try {
                lat = Double.parseDouble(args[0]);
                lon = Double.parseDouble(args[1]);
            } catch (NumberFormatException e) {
                System.err.println("\nLat and Lon must be decimal numbers e.g. 51.2 -0.125");
                System.exit(1);
            }
            
            if (lat < -90 || lat > 90 ) {
                System.err.println("\nLatitude must be in range -90 to 90");
                System.exit(1);
            }

            if (lon < -180 || lon > 180 ) {
                System.err.println("\nLongitude must be in range -180 to 180");
                System.exit(1);
            }
            
            if (args.length == 3) {
                try {
                    acc = Double.parseDouble(args[2]);
                } catch (NumberFormatException e) {
                    System.err.println("\nAccuracy must be a decimal number e.g. 1.5");
                    System.exit(1);
                }
            }

            hexa = latLonToGeohexa(lat, lon, acc);
            System.out.println("geohexa is " + hexa);
            Coordinate checkLatLon = geohexaToLatLon(hexa);
            System.out.println("Converted back to lat, lon: " + checkLatLon.getLat() + ", " + checkLatLon.getLon());
                
            return;
        }    

        if (args.length == 1) {
    
            //System.out.println("Converting a geohexa to a lat and lon");
            hexa = args[0].toLowerCase();
    
            if (! inRange(hexa)) {
                System.err.println("\nGeohexa digits must only be 0-9, a-z (or A-Z). Example: Qarj");
                System.exit(1);
            }
    
            Coordinate latLon = geohexaToLatLon(hexa);
            System.out.println("Lat: " + latLon.getLat() + " Lon: " + latLon.getLon());

            return;
        }

        System.out.println("Enter two or three arguments - a lat followed by a lon and optional accuracy");
        System.out.println("\nor\n\nEnter one argument - a geohexa");

    }
    
    public static boolean inRange(String hexa) {
        boolean inRange = true;
        for (char c : hexa.toCharArray()) {
            if (BASE_36_DIGITS.indexOf(c) == -1) {
                inRange = false;
            }
        }
        return inRange;
    }

    double distance(double lat1, double lon1, double lat2, double lon2) {

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        // Haversine formula
        double sinLat = Math.sin(latDistance / 2);
        double sinLon = Math.sin(lonDistance / 2);
        double a = sinLat * sinLat + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * sinLon * sinLon;
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        final int earthRadius = 6371;
        double distance = earthRadius * c * 1000; // convert to meters

        return distance;
    }

    private double prepareLat(double lat) {
        if (lat > 89.99999999) {
            lat = 89.99999999;
        }
        return lat + 90;
    }

    private double prepareLon(double lon) {
        if (lon == 180) {
            lon = -180; // 180E is same as 180W
        }
        return lon + 180;
    }

    // function only used for verbose output
    private double latBottom(String allLatgeo) {
        double lat = 0;

        if (allLatgeo.length() == 0) { // worst case for longitude size is at the equator, this also fits definition of null lat
            return lat;
        }

        double latHeight = 180;
        for (char d : allLatgeo.toLowerCase().toCharArray()) {
            lat = lat + (latHeight / 36) * BASE_36_DIGITS.indexOf(d);
            latHeight = latHeight / 36;
        }

        return lat - 90;
    }

    private Base36Remainder compress(double num, double cellSize) {
        int cell = (int) (num / cellSize);
        double remainder = num - ( (double) cell * cellSize);
        String base36 = Character.toString(BASE_36_DIGITS_OUTPUT.charAt(cell));

        if (VERBOSE) {
            System.out.println ("base36 digit: " + base36 + "(" + cell + ")  cell_size_degrees[" + cellSize + "]   fragment[" + num + "]  remainder_to_go[" + remainder + "]");
        }

        return new Base36Remainder(base36, remainder);
    }

    public String latLonToGeohexa(double lat, double lon, double... acc) {
        double accuracy = acc.length > 0 ? acc[0] : 3;
        if (accuracy < 0.00000001) { // need to be sensible about the accuracy to prevent division by zero
            accuracy = 0.00000001;
        }
        String geohexa = "";
        String allLatgeo = "";
        double latFragment = prepareLat(lat);
        double lonFragment = prepareLon(lon);
        double latUnit = 180;
        double lonUnit = 360;

        // null is a valid geohexa - represents 0, 0
        double error = distance(lat, lon, 0, 0);
        if (error < accuracy) {
            return "";
        }

        double latCellSize = 0;
        double lonCellSize = 0;
        if (VERBOSE) {
            latCellSize = distance(latBottom(allLatgeo), 0, latBottom(allLatgeo)+latUnit, 0);
        }

        Coordinate geoLatLon = new Coordinate();
        while (true) {
            lonUnit = lonUnit / 36;
            Base36Remainder lon36Remainder = compress(lonFragment, lonUnit);
            lonFragment = lon36Remainder.getRemainder();
            geohexa = geohexa + lon36Remainder.getBase36();

            geoLatLon = geohexaToLatLon(geohexa);
            error = distance(lat, lon, geoLatLon.getLat(), geoLatLon.getLon());

            if (VERBOSE) {
                lonCellSize = distance(latBottom(allLatgeo), 0, latBottom(allLatgeo), lonUnit);
                System.out.println("Current lat *lon* cell size meters: " + latCellSize + " , " + lonCellSize + "   error in meters[" + error + "]\n...");
            }

            if (error < accuracy) {
                break;
            }

            latUnit = latUnit / 36;
            Base36Remainder lat36Remainder = compress(latFragment, latUnit);
            latFragment = lat36Remainder.getRemainder();
            geohexa = geohexa + lat36Remainder.getBase36();
            allLatgeo = allLatgeo + lat36Remainder.getBase36();

            geoLatLon = geohexaToLatLon(geohexa);
            error = distance(lat, lon, geoLatLon.getLat(), geoLatLon.getLon());

            if (VERBOSE) {
                latCellSize = distance(latBottom(allLatgeo), 0, latBottom(allLatgeo)+latUnit, 0);
                System.out.println("Current *lat* lon cell size meters: " + latCellSize + " , " + lonCellSize + "   error in meters[" + error + "]\n\n");
            }

            if (error < accuracy) {
                break;
            }
        }

        return geohexa;
    }

    public Coordinate geohexaToLatLon(String hexa) {
        double lat = 0;
        double lon = 0;

        int i = 0;
        
        double latHeight = 180;
        double lonWidth = 360;

        for (char d : hexa.toLowerCase().toCharArray()) {
            i = i + 1;

            int remainder = Math.floorMod(i, 2);
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

        if (VERBOSE) {
            System.out.println(hexa + " as lat, lon " + lat + " " + lon);
        }

        return new Coordinate(lat, lon);
    }

    public class Coordinate {
        private double mLat;
        private double mLon;
        public Coordinate () {
        }
        public Coordinate (double lat, double lon) {
            mLat = lat;
            mLon = lon;
        }
        public double getLat() {
            return mLat;
        }
        public double getLon() {
            return mLon;
        }
        public void setLat(double lat) {
            mLat = lat;
        }
        public void setLon(double lon) {
            mLon = lon;
        }
    }

    public final class Base36Remainder {
        private final String mBase36;
        private final double mRemainder;
        public Base36Remainder (String base36, double remainder) {
            mBase36 = base36;
            mRemainder = remainder;
        }
        public String getBase36() {
            return mBase36;
        }
        public double getRemainder() {
            return mRemainder;
        }
    }

}


/*


*/

/*
jrun geohexa.Run 51.481874 -0.112564
jrun geohexa.Run abcdefgh
ToDo:
 - Java variable naming conventions
 - unit tests for system out
 - lat_height instead of width
*/

