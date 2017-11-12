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

            hexa = latlon_to_geohexa(lat, lon, acc);
            System.out.println("geohexa is " + hexa);
            Coordinate checkLatLon = geohexa_to_latlon(hexa);
            System.out.println("Converted back to lat, lon: " + checkLatLon.getLat() + ", " + checkLatLon.getLon());
                
            return;
        }    

        if (args.length == 1) {
    
            //System.out.println("Converting a geohexa to a lat and lon");
            hexa = args[0].toLowerCase();
    
            if (! in_range(hexa)) {
                System.err.println("\nGeohexa digits must only be 0-9, a-z (or A-Z). Example: Qarj");
                System.exit(1);
            }
    
            Coordinate latLon = geohexa_to_latlon(hexa);
            System.out.println("Lat: " + latLon.getLat() + " Lon: " + latLon.getLon());

            return;
        }

        System.out.println("Enter two or three arguments - a lat followed by a lon and optional accuracy");
        System.out.println("\nor\n\nEnter one argument - a geohexa");

    }
    
    public static boolean in_range(String hexa) {
        boolean in_range = true;
        for (char c : hexa.toCharArray()) {
            if (BASE_36_DIGITS.indexOf(c) == -1) {
                in_range = false;
            }
        }
        return in_range;
    }

    double distance(double lat1, double lon1, double lat2, double lon2) {

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        // Haversine formula
        double sin_lat = Math.sin(latDistance / 2);
        double sin_lon = Math.sin(lonDistance / 2);
        double a = sin_lat * sin_lat + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * sin_lon * sin_lon;
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        final int earth_radius = 6371;
        double distance = earth_radius * c * 1000; // convert to meters

        return distance;
    }

    private double prepare_lat(double lat) {
        if (lat > 89.99999999) {
            lat = 89.99999999;
        }
        return lat + 90;
    }

    private double prepare_lon(double lon) {
        if (lon == 180) {
            lon = -180; // 180E is same as 180W
        }
        return lon + 180;
    }

    // function only used for verbose output
    private double lat_bottom(String all_latgeo) {
        double lat = 0;

        if (all_latgeo.length() == 0) { // worst case for longitude size is at the equator, this also fits definition of null lat
            return lat;
        }

        double lat_height = 180;
        for (char d : all_latgeo.toLowerCase().toCharArray()) {
            lat = lat + (lat_height / 36) * BASE_36_DIGITS.indexOf(d);
            lat_height = lat_height / 36;
        }

        return lat - 90;
    }

    private Base36Remainder compress(double num, double cell_size) {
        int cell = (int) (num / cell_size);
        double remainder = num - ( (double) cell * cell_size);
        String base36 = Character.toString(BASE_36_DIGITS_OUTPUT.charAt(cell));

        if (VERBOSE) {
            System.out.println ("base36 digit: " + base36 + "(" + cell + ")  cell_size_degrees[" + cell_size + "]   fragment[" + num + "]  remainder_to_go[" + remainder + "]");
        }

        return new Base36Remainder(base36, remainder);
    }

    public String latlon_to_geohexa(double lat, double lon, double... acc) {
        double accuracy = acc.length > 0 ? acc[0] : 3;
        if (accuracy < 0.00000001) { // need to be sensible about the accuracy to prevent division by zero
            accuracy = 0.00000001;
        }
        String geohexa = "";
        String all_latgeo = "";
        double lat_fragment = prepare_lat(lat);
        double lon_fragment = prepare_lon(lon);
        double lat_unit = 180;
        double lon_unit = 360;

        // null is a valid geohexa - represents 0, 0
        double error = distance(lat, lon, 0, 0);
        if (error < accuracy) {
            return "";
        }

        double lat_cell_size = 0;
        double lon_cell_size = 0;
        if (VERBOSE) {
            lat_cell_size = distance(lat_bottom(all_latgeo), 0, lat_bottom(all_latgeo)+lat_unit, 0);
        }

        Coordinate geoLatLon = new Coordinate();
        while (true) {
            lon_unit = lon_unit / 36;
            Base36Remainder lon36Remainder = compress(lon_fragment, lon_unit);
            lon_fragment = lon36Remainder.getRemainder();
            geohexa = geohexa + lon36Remainder.getBase36();

            geoLatLon = geohexa_to_latlon(geohexa);
            error = distance(lat, lon, geoLatLon.getLat(), geoLatLon.getLon());

            if (VERBOSE) {
                lon_cell_size = distance(lat_bottom(all_latgeo), 0, lat_bottom(all_latgeo), lon_unit);
                System.out.println("Current lat *lon* cell size meters: " + lat_cell_size + " , " + lon_cell_size + "   error in meters[" + error + "]\n...");
            }

            if (error < accuracy) {
                break;
            }

            lat_unit = lat_unit / 36;
            Base36Remainder lat36Remainder = compress(lat_fragment, lat_unit);
            lat_fragment = lat36Remainder.getRemainder();
            geohexa = geohexa + lat36Remainder.getBase36();
            all_latgeo = all_latgeo + lat36Remainder.getBase36();

            geoLatLon = geohexa_to_latlon(geohexa);
            error = distance(lat, lon, geoLatLon.getLat(), geoLatLon.getLon());

            if (VERBOSE) {
                lat_cell_size = distance(lat_bottom(all_latgeo), 0, lat_bottom(all_latgeo)+lat_unit, 0);
                System.out.println("Current *lat* lon cell size meters: " + lat_cell_size + " , " + lon_cell_size + "   error in meters[" + error + "]\n\n");
            }

            if (error < accuracy) {
                break;
            }
        }

        return geohexa;
    }

    public Coordinate geohexa_to_latlon(String hexa) {
        double lat = 0;
        double lon = 0;

        int i = 0;
        
        double lat_width = 180;
        double lon_width = 360;

        for (char d : hexa.toLowerCase().toCharArray()) {
            i = i + 1;

            int remainder = Math.floorMod(i, 2);
            if (remainder == 1 ) { // we have a lon geohexa digit
                lon = lon + (lon_width / 36) * BASE_36_DIGITS.indexOf(d);
                lon_width = lon_width / 36;
            } else {
                lat = lat + (lat_width / 36) * BASE_36_DIGITS.indexOf(d);
                lat_width = lat_width / 36;
            }
        }

        // calculate mid point of cell
        lat = lat - 90  + lat_width / 2;
        lon = lon - 180 + lon_width / 2;

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

