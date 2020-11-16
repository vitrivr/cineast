package org.vitrivr.cineast.standalone;

import org.vitrivr.cineast.standalone.importer.lsc2020.LSCUtilities;

public class Sandbox {

    public static void main(String[] args) {
        /*
        // TODO move to unit test
        // From lsc metadata except weekday, that's from wolframalpha
        // UTC_2015-03-20_07:30,2015-03-20_15:30,Asia/Shanghai FRI
        // UTC_2016-08-23_16:38,2016-08-23_17:38,Europe/Dublin TUE
        // UTC_2018-05-10_22:36,2018-05-10_23:36,Europe/Dublin THUR
        String date1 = "UTC_2015-03-20_07:30";
        String local1 = "2015-03-20_15:30";
        String zone1 = "Asia/Shanghai";
        LocalDateTime d1 = LocalDateTime.of(2015, 3, 20, 7, 30);
        ZonedDateTime z1 = ZonedDateTime.of(2015, 3, 20, 15, 30, 0, 0, ZoneId.of(zone1));
        LocalDateTime p1 = convertUtc(date1);
        ZonedDateTime c1 = convertLocal(local1, zone1);

        String date2 = "UTC_2016-08-23_16:38";
        String local2 = "2016-08-23_17:38";
        String zone2 = "Europe/Dublin";
        LocalDateTime d2 = LocalDateTime.of(2016, 8, 23, 16, 38);
        ZonedDateTime z2 = ZonedDateTime.of(2016, 8, 23, 17, 38, 0, 0, ZoneId.of(zone2));
        LocalDateTime p2 = convertUtc(date2);
        ZonedDateTime c2 = convertLocal(local2, zone2);

        if (!d1.equals(p1)) {
            System.out.println("D1: not equal");
        }
        if (!z1.equals(c1)) {
            System.out.println("Z1: not equal");
        }

        if (!d2.equals(p2)) {
            System.out.println("D2: not equal");
        }
        if (!z2.equals(c2)) {
            System.out.println("Z2: not equal");
        }


        System.out.println("---");
        System.out.println("d1: " + d1 + " " + d1.getDayOfWeek());
        System.out.println("p1: " + p1);
        System.out.println("z1: " + d1);
        System.out.println("c1: " + p1);
        System.out.println("---");

        System.out.println("---");
        System.out.println("d2: " + d2 + " " + d2.getDayOfWeek());
        System.out.println("p2: " + p2);
        System.out.println("z2: " + d2);
        System.out.println("c2: " + p2);
        System.out.println("---");
        */

        // LSC Cleaner output report-valid.txt extract:
        /*
        String[] items = new String[]{
                "20160914_145746_000.jpg              ",
                "20160914_145803_000.jpg              ",
                "20160914_145820_000.jpg              ",
                "20160914_145837_000.jpg              ",
                "20160914_145854_000.jpg              ",
                "20160914_145911_000.jpg              ",
                "20160914_145928_000.jpg              ",
                "20160914_145958_000.jpg              ",
                "B00011200_21I6X0_20180507_212447E.JPG",
                "B00011201_21I6X0_20180507_212559E.JPG",
                "B00011202_21I6X0_20180507_212707E.JPG",
                "B00011203_21I6X0_20180507_212819E.JPG",
                "B00011204_21I6X0_20180507_212928E.JPG",
                "B00011205_21I6X0_20180507_213036E.JPG",
                "B00011206_21I6X0_20180507_213147E.JPG",
                "B00011207_21I6X0_20180507_213256E.JPG",
                "B00011208_21I6X0_20180507_213407E.JPG",
                "B00011209_21I6X0_20180507_213736E.JPG"
        };
        String[] expected = new String[]{
                "20160914_1457",
                "20160914_1458",
                "20160914_1458",
                "20160914_1458",
                "20160914_1458",
                "20160914_1459",
                "20160914_1459",
                "20160914_1459",
                "20180507_2124",
                "20180507_2125",
                "20180507_2127",
                "20180507_2128",
                "20180507_2129",
                "20180507_2130",
                "20180507_2131",
                "20180507_2132",
                "20180507_2134",
                "20180507_2137"
        };

        for(int i=0; i<items.length; i++){
            String minuteId = LSCUtilities.filenameToMinuteId(items[i]).get();
            System.out.println("Given: "+items[i]+", Result: "+minuteId+" = "+minuteId.equalsIgnoreCase(expected[i])+" ("+expected[i]+")");
            System.out.println("  Time: "+LSCUtilities.fromMinuteId(minuteId));
        }
        */
        // Testing the path remover
        /*
        String[] items = new String[]{
                "lsc2020-images/2018-05-07/B00011202_21I6X0_20180507_212707E.JPG",
                "lsc2020-images/2018-05-07/B00011203_21I6X0_20180507_212819E.JPG",
                "lsc2020-images/2018-05-07/B00011204_21I6X0_20180507_212928E.JPG",
                "lsc2020-images/2018-05-07/B00011205_21I6X0_20180507_213036E.JPG",
                "lsc2020-images/2018-05-07/B00011206_21I6X0_20180507_213147E.JPG",
                "lsc2020-images/2018-05-07/B00011207_21I6X0_20180507_213256E.JPG",
                "lsc2020-images/2018-05-07/B00011208_21I6X0_20180507_213407E.JPG",
                "lsc2020-images/2018-05-07/B00011209_21I6X0_20180507_213736E.JPG"
        };
        for(String s : items){
            System.out.println(s+" => "+sanitizeFilename(s));
        }*/
    }
}
