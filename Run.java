package geohexa;

public class Run {
    
    public static void main(String[] args) {
        try {
            Geohexa geohexa = new Geohexa ();
            //geohexa.setVERBOSE();
            geohexa.cli (args);
        }
        catch (Exception e) {
            e.printStackTrace ();
        }
    }

}