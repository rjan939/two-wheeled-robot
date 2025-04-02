package org.firstinspires.ftc.teamcode.util.lib;

public class MapPoint {
    public double x;
    public double y;

    public MapPoint(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void add(MapPoint point) {
        x += point.x;
        y += point.y;
    }
}