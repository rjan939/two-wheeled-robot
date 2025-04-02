package org.firstinspires.ftc.teamcode.util;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.IMU;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;
import org.firstinspires.ftc.teamcode.util.drive.DriveType;
import org.firstinspires.ftc.teamcode.util.drive.GyroDrive;
import org.firstinspires.ftc.teamcode.util.overlay.OverlayManager;

public class RobotHardware {
    public final GyroDrive drive;

    private final IMU imu;

    private final OverlayManager overlay = new OverlayManager();

    private boolean uprighting = false;

    public RobotHardware(HardwareMap hardwareMap, DriveType driveType) {
        drive = new GyroDrive(hardwareMap, driveType);
        imu = hardwareMap.get(IMU.class, "imu");
        drive.initIMU(imu);
    }

    public void update() {
        YawPitchRollAngles angles = imu.getRobotYawPitchRollAngles();
        double pitchRate = imu.getRobotAngularVelocity(AngleUnit.DEGREES).xRotationRate;
        drive.update(angles, pitchRate);
        overlay.updatePose(drive.getPose());
        overlay.update();
    }
}
