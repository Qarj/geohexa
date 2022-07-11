#!/usr/bin/env python3
version="0.1.0"

import unittest, random
from geohexa import latlon_to_geohexa, geohexa_to_latlon, distance

MM = 0.001
CM = 0.01
METER = 1
TEN_METER = 10
HUNDRED_METER = 100
KM = 1_000
TEN_KM = 10_000
THOUSAND_KM = 1_000_000

ACCURACY = 3
MANY=10_000

base36digits ='0123456789abcdefghijklmnopqrstuvwxyz'


class Testgeohexa(unittest.TestCase):
    
    def setUp(self):
        pass
        
    def test_oval_tube_specific_value(self):
        hexa = latlon_to_geohexa(51.481874, -0.112564)
        self.assertEqual( hexa, "hszaLoe3t")

    def test_max_lat_lon(self):
        self.assertTrue( len(latlon_to_geohexa(90,180) ) >= 2)

    def test_min_lat_lon(self):
        self.assertTrue( len(latlon_to_geohexa(-90,-180) ) >= 2)

    def test_geohexa_to_latlon_within_specification(self):
        lat, lon = geohexa_to_latlon('zz')
        self.assertTrue( isinstance(lat, float) )
        self.assertTrue( isinstance(lon, float) )
        self.assertTrue( lat >= -90)
        self.assertTrue( lat <= 90)
        self.assertTrue( lon >= -180)
        self.assertTrue( lon <= 180)

    def test_distance_between_two_lat_lons(self):
        # from south to north of Vauxhall Bridge - approx 200 meters
        self.assertEqual( round(distance(51.487141, -0.125873, 51.488178, -0.128224), 8), 199.48427672)

    def test_latlon_to_geohexa_and_back_accuracy_for_ten_km(self):
        test_lat, test_lon = 1.285864, 103.851831 # Singapore Boat Quay
        geohexa = latlon_to_geohexa(test_lat, test_lon, accuracy=TEN_KM)
        lat, lon = geohexa_to_latlon(geohexa)
        error = distance(test_lat, test_lon, lat, lon)
        self.assertTrue (error < TEN_KM)

    def test_latlon_to_geohexa_and_back_accuracy_for_meter(self):
        test_lat, test_lon = 64.123565, -21.805507 # Reykjavik Brewery
        geohexa = latlon_to_geohexa(test_lat, test_lon, accuracy=METER)
        lat, lon = geohexa_to_latlon(geohexa)
        error = distance(test_lat, test_lon, lat, lon)
        self.assertTrue (error < METER)

    def test_latlon_to_geohexa_and_back_accuracy_for_millimeter(self):
        test_lat, test_lon = -46.896522, 168.130336 # NZ Oyster Bar
        geohexa = latlon_to_geohexa(test_lat, test_lon, accuracy=MM)
        lat, lon = geohexa_to_latlon(geohexa)
        error = distance(test_lat, test_lon, lat, lon)
        self.assertTrue (error < MM)

    def test_many_random_lat_lons(self):
        random.seed(42)
        total_length = 0
        for i in range (0,MANY):
            test_lat = (random.random() * 180) - 90
            test_lon = (random.random() * 360) - 180
            geohexa = latlon_to_geohexa(test_lat, test_lon, accuracy=ACCURACY)
            lat, lon = geohexa_to_latlon(geohexa)
            error = distance(test_lat, test_lon, lat, lon)
            self.assertTrue (error < ACCURACY)
            total_length = total_length + len(geohexa)
        print('\nTotal length of geohexa for', MANY, 'random co-ordinates at', ACCURACY, 'meter accuracy:',total_length)
        print('Average length of geohexa:', total_length/MANY, '\n')

    def test_many_random_lat_lons_near_equator(self):
        random.seed(42)
        total_length = 0
        for i in range (0,MANY):
            test_lat = (random.random() * 10) - 5
            test_lon = (random.random() * 360) - 180
            geohexa = latlon_to_geohexa(test_lat, test_lon, accuracy=ACCURACY)
            lat, lon = geohexa_to_latlon(geohexa)
            error = distance(test_lat, test_lon, lat, lon)
            self.assertTrue (error < ACCURACY)
            total_length = total_length + len(geohexa)
        print('\nTotal length of geohexa for', MANY, 'random co-ordinates near the Equator at', ACCURACY, 'meter accuracy:',total_length)
        print('Average length of geohexa:', total_length/MANY, '\n')

    def test_many_random_lat_lons_near_north_pole(self):
        random.seed(42)
        total_length = 0
        for i in range (0,MANY):
            test_lat = (random.random() * 10) + 80
            test_lon = (random.random() * 360) - 180
            geohexa = latlon_to_geohexa(test_lat, test_lon, accuracy=ACCURACY)
            lat, lon = geohexa_to_latlon(geohexa)
            error = distance(test_lat, test_lon, lat, lon)
            self.assertTrue (error < ACCURACY)
            total_length = total_length + len(geohexa)
        print('\nTotal length of geohexa for', MANY, 'random co-ordinates near the North Pole at', ACCURACY, 'meter accuracy:',total_length)
        print('Average length of geohexa:', total_length/MANY, '\n')
        
    def test_many_random_lat_lons_near_lat_42_to_52(self):
        random.seed(42)
        total_length = 0
        for i in range (0,MANY):
            test_lat = (random.random() * 10) + 42
            test_lon = (random.random() * 360) - 180
            geohexa = latlon_to_geohexa(test_lat, test_lon, accuracy=ACCURACY)
            lat, lon = geohexa_to_latlon(geohexa)
            error = distance(test_lat, test_lon, lat, lon)
            self.assertTrue (error < ACCURACY)
            total_length = total_length + len(geohexa)
        print('\nTotal length of geohexa for', MANY, 'random co-ordinates near lat 42 to 52 at', ACCURACY, 'meter accuracy:',total_length)
        print('Average length of geohexa:', total_length/MANY, '\n')
        
    def test_case_insensitive_input_ok(self):
        lat1, lon1 = geohexa_to_latlon('iilloo')
        lat2, lon2 = geohexa_to_latlon('IILLOO')
        self.assertTrue (lat1 == lat2)
        self.assertTrue (lon1 == lon2)
        
    def test_single_null_geohexa(self):
        lat, lon = geohexa_to_latlon('')
        self.assertTrue (lat == 0)
        self.assertTrue (lon == 0)

    def test_three_digit_geohexa(self):
        lat, lon = geohexa_to_latlon('Tim')
        self.assertTrue( isinstance(lat, float) )
        self.assertTrue( isinstance(lon, float) )

    def test_four_digit_geohexa(self):
        lat, lon = geohexa_to_latlon('Qarj')
        self.assertTrue( isinstance(lat, float) )
        self.assertTrue( isinstance(lon, float) )
        self.assertTrue (lon != 0)
        self.assertTrue (lat != 0)
        geohexa = latlon_to_geohexa(lat,lon, 0)
        self.assertTrue (geohexa.lower() == 'Qarj'.lower())
        
    def test_all_single_digit_geohexa(self):
        for c in base36digits:
            lat, lon = geohexa_to_latlon(c)
            geohexa = latlon_to_geohexa(lat, lon)
            self.assertEqual(geohexa.lower(), c)

    def test_all_two_digit_geohexa(self):
        for c in base36digits:
            for d in base36digits:
                lat, lon = geohexa_to_latlon(c+d)
                geohexa = latlon_to_geohexa(lat, lon)
                self.assertEqual(geohexa.lower(), c+d)

    def test_all_third_and_fourth_digit_geohexa(self):
        for c in base36digits:
            for d in base36digits:
                lat, lon = geohexa_to_latlon('00'+c+d)
                geohexa = latlon_to_geohexa(lat, lon)
                self.assertEqual(geohexa.lower(), '00'+c+d)

    def test_should_output_null_digit_geohexa(self):
        geohexa = latlon_to_geohexa(0, 0, accuracy=0)
        self.assertEqual (len(geohexa), 0)
        
    def test_upper_danger_boundary(self):
        geohexa = latlon_to_geohexa(64, 0, accuracy=0)

    def test_lower_danger_boundary(self):
        geohexa = latlon_to_geohexa(-46, 0, accuracy=0)

    def test_all_lat_in_point_one_increments(self):
        acc = 3
        random.seed(180)
        total_length = 0
        count = 0
        max = 0
        min = 999999
        for i in range (-900,900):
            count = count + 1
            test_lat = i / 10
            test_lon = (random.random() * 360) - 180
            geohexa = latlon_to_geohexa(test_lat, test_lon, accuracy=acc)
            if (len(geohexa) > max):
                max = len(geohexa)
            if (len(geohexa) < min):
                min = len(geohexa)
            lat, lon = geohexa_to_latlon(geohexa)
            error = distance(test_lat, test_lon, lat, lon)
            self.assertTrue (error < acc)
            total_length = total_length + len(geohexa)
        self.assertEqual (max, 10)
        print('\nTotal length of geohexa for all lat in 0.1 increments at', acc, 'meter accuracy:',total_length)
        print('Average length of geohexa:', total_length/count)
        print('Min length of geohexa:', min)
        print('Max length of geohexa:', max, '\n')

    def test_all_lon_in_point_one_increments(self):
        acc = 3
        random.seed(360)
        total_length = 0
        count = 0
        max = 0
        min = 999999
        for i in range (-1800,1800):
            count = count + 1
            test_lat = (random.random() * 180) - 90
            test_lon = i / 10
            geohexa = latlon_to_geohexa(test_lat, test_lon, accuracy=acc)
            if (len(geohexa) > max):
                max = len(geohexa)
            if (len(geohexa) < min):
                min = len(geohexa)
            lat, lon = geohexa_to_latlon(geohexa)
            error = distance(test_lat, test_lon, lat, lon)
            self.assertTrue (error < acc)
            total_length = total_length + len(geohexa)
        self.assertEqual (max, 10)
        print('\nTotal length of geohexa for all lon in 0.1 increments at', acc, 'meter accuracy:',total_length)
        print('Average length of geohexa:', total_length/count)
        print('Min length of geohexa:', min)
        print('Max length of geohexa:', max, '\n')


if __name__ == '__main__':
    unittest.main()
