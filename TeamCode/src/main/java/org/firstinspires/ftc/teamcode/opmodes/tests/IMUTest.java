package org.firstinspires.ftc.teamcode.opmodes.tests;

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.IMU;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AngularVelocity;
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;
import org.firstinspires.ftc.teamcode.util.lib.FtcDashboardManager;

@TeleOp(name = "IMUTest", group = "tests")
public class IMUTest extends LinearOpMode {
    @Override
    public void runOpMode() {
        IMU imu = hardwareMap.get(IMU.class, "imu");
        imu.initialize(
                new IMU.Parameters(
                        new RevHubOrientationOnRobot(
                                RevHubOrientationOnRobot.LogoFacingDirection.BACKWARD,
                                RevHubOrientationOnRobot.UsbFacingDirection.RIGHT
                        )
                )
        );

        waitForStart();

        while (opModeIsActive() && !isStopRequested()) {
            YawPitchRollAngles angles = imu.getRobotYawPitchRollAngles();
            FtcDashboardManager.addData("Pitch", angles.getPitch(AngleUnit.DEGREES));
            telemetry.addData("Pitch", angles.getPitch(AngleUnit.DEGREES));
            telemetry.addData("Roll", angles.getRoll(AngleUnit.DEGREES));
            telemetry.addLine();

            AngularVelocity velocity = imu.getRobotAngularVelocity(AngleUnit.DEGREES);
            telemetry.addLine("IMU Angular Velocity");
            telemetry.addData("X", velocity.xRotationRate);
            telemetry.addData("Y", velocity.yRotationRate);
            telemetry.addData("Z", velocity.zRotationRate);

            telemetry.update();
            FtcDashboardManager.update();
        }
    }
}