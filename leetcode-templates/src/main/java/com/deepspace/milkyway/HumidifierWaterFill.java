package com.deepspace.milkyway;

public class HumidifierWaterFill {

    public int doSomething(String s) {
        String[] lines = s.split("\n");
        int n = Integer.parseInt(lines[0]);

        int water = 0;
        int t = 0;
        for (int i = 1; i < n; i++) {
            String[] vals = lines[i].split(" ");
            int tCurr = Integer.parseInt(vals[0]);
            int vCurr = Integer.parseInt(vals[1]);

            water = water - (tCurr - t) + vCurr;

            t = tCurr;
        }

        return water;
    }


}
