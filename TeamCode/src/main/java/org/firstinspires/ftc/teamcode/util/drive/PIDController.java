package org.firstinspires.ftc.teamcode.util.drive;

import org.firstinspires.ftc.teamcode.util.lib.PIDParams;

public class PIDController {
    private PIDParams constants;
    private double i;
    private double lastError;
    private double lastTime;

    public PIDController(PIDParams constants) {
        this.constants = constants;
        i = 0;
        lastError = 0;
        lastTime = 0;
    }

    public double update(double error) {
        double dt = System.currentTimeMillis() - lastTime;
        double p = constants.Kp * error;
        i += constants.Ki * error * dt;
        i = Math.min(constants.MaxI, Math.max(-constants.MaxI, i));
        double d = constants.Kd * (error - lastError) / dt;
        lastError = error;
        lastTime = System.currentTimeMillis();
        return p + i + d;
    }

    public void setConstants(PIDParams constants) {
        this.constants = constants;
    }

    public PIDParams getConstants() {
        return constants;
    }

    public void reset() {
        i = 0;
        lastError = 0;
        lastTime = 0;
    }

    public void resetI() {
        i = 0;
    }
}