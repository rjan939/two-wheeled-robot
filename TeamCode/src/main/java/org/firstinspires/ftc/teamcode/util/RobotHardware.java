package org.firstinspires.ftc.teamcode.util;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.IMU;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;
import org.firstinspires.ftc.teamcode.util.drive.DriveType;
import org.firstinspires.ftc.teamcode.util.drive.GyroDriveLQR;
import org.firstinspires.ftc.teamcode.util.drive.GyroDrivePID;
import org.firstinspires.ftc.teamcode.util.drive.constants.BalanceConstants;
import org.firstinspires.ftc.teamcode.util.overlay.OverlayManager;

public class RobotHardware {
    public GyroDriveLQR driveLQR;
    public GyroDrivePID drivePID;

    private final IMU imu;

    private final OverlayManager overlay = new OverlayManager();

    private boolean uprighting = false;
    private final boolean LQR;

    public RobotHardware(HardwareMap hardwareMap, DriveType driveType, boolean LQR) {
        imu = hardwareMap.get(IMU.class, "imu");
        if (LQR) {
            driveLQR = new GyroDriveLQR(hardwareMap, driveType);
            driveLQR.initIMU(imu);
        } else {
            drivePID = new GyroDrivePID(hardwareMap);
            drivePID.initIMU(imu);
        }
        this.LQR = LQR;
    }

    public void update() {
        if (LQR) {
            YawPitchRollAngles angles = imu.getRobotYawPitchRollAngles();
            double pitchRate = imu.getRobotAngularVelocity(AngleUnit.DEGREES).xRotationRate;
            driveLQR.update(angles, pitchRate);
            overlay.updatePose(driveLQR.getPose());
            overlay.update();
        } else {
            YawPitchRollAngles angles = imu.getRobotYawPitchRollAngles();
            double pitchRate = imu.getRobotAngularVelocity(AngleUnit.DEGREES).xRotationRate;
            drivePID.update(angles);
            if (uprighting && angles.getPitch(AngleUnit.DEGREES) > BalanceConstants.UprightPowerMargin) {
                uprighting = false;
            }

            overlay.updatePose(drivePID.getPose());

            overlay.update();
        }
    }

    public void upright() {
        if (!uprighting) {
            uprighting = true;
        }
    }
}
