package org.firstinspires.ftc.teamcode.util.drive.constants;

import com.acmerobotics.dashboard.config.Config;

@Config
public class LQRConstants {
    private final static double GRAVITY = 9.81; // m/s^2
    public static double LENGTH = 0.04408; // length to center of mass (m) 0.04708

    // TODO: Tune
    public static double MASS = 2.71125168088; // kg (5.98 lbs)


    public static double WHEEL_RADIUS = 0.045; // m
    public static double TICKS_PER_REVOLUTE = 384.5 * 26 / 20;

    public static double M_PER_TICKS = (WHEEL_RADIUS * 2 * Math.PI) / TICKS_PER_REVOLUTE;

    public static double[][] getA() {
        return new double[][]{
                {0, 1, 0, 0},
                {GRAVITY / LENGTH, 0, 0, 0},
                {0, 0, 0, 1},
                {-GRAVITY / MASS, 0, 0, 0}
        };
    }

    public static double[][] getB() {
        return new double[][]{
                {0},
                {-1 / (MASS * (LENGTH * LENGTH))},
                {0},
                {1 / MASS}
        };
    }


    // TODO:
    public static double AnglePenalty = 1.0;
    public static double AngularVelocityPenalty = 0;
    public static double PositionPenalty = 3.0;
    public static double VelocityPenalty = 0.5;

    public static double[] getQ() {
        return new double[]{AnglePenalty, AngularVelocityPenalty, PositionPenalty, VelocityPenalty}; // state penalty, x, x dot, theta, theta dot
    }

    public static double R = 0.5;

    public static double VelocityModifier = 10000;
    public static double PositionModifier = 1;

    public static double StoppingAmount = 0;
    public static double StoppedMargin = 0.2;
    public static double StoppedTime = 2;

    public static boolean UpdateLQRGains = true;
}
