package org.firstinspires.ftc.teamcode.opmodes;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.util.RobotHardware;
import org.firstinspires.ftc.teamcode.util.drive.DriveType;
import org.firstinspires.ftc.teamcode.util.lib.FtcDashboardManager;
import org.firstinspires.ftc.teamcode.util.lib.GamepadButton;
import org.firstinspires.ftc.teamcode.util.lib.StatefulGamepad;

@Config
@TeleOp(name="EasyDriveLQR")
public class EasyDriveLQR extends LinearOpMode {

    private RobotHardware robot;
    protected DriveType getDriveType() {
        return DriveType.SMOOTH;
    }

    @Override
    public void runOpMode() throws InterruptedException {
        robot = new RobotHardware(hardwareMap, getDriveType(), true);

        StatefulGamepad gamepad1Buttons = new StatefulGamepad(gamepad1);
        StatefulGamepad gamepad2Buttons = new StatefulGamepad(gamepad2);

        waitForStart();

        double lastTime = System.currentTimeMillis();

        while (opModeIsActive() && !isStopRequested()) {
            double currentTime = System.currentTimeMillis();
            FtcDashboardManager.addData("Loop Time", currentTime - lastTime);
            lastTime = currentTime;

            gamepad1Buttons.update();
            gamepad2Buttons.update();

            if (!gamepad1Buttons.getButton(GamepadButton.RIGHT_BUMPER)) {
                robot.driveLQR.drive(Math.max(Math.min(-gamepad1.left_stick_y + -gamepad2.left_stick_y, 1), -1), Math.max(Math.min(gamepad1.left_stick_x + gamepad2.left_stick_x, 1), -1), gamepad2Buttons.getButton(GamepadButton.LEFT_BUMPER));
            } else {
                robot.driveLQR.drive(-gamepad2.left_stick_y, gamepad2.left_stick_x, gamepad1Buttons.getButton(GamepadButton.LEFT_BUMPER));
            }

            if (gamepad2Buttons.getButton(GamepadButton.RIGHT_BUMPER)) {
                robot.driveLQR.brake();
            }

            FtcDashboardManager.addData("Drive Type", robot.driveLQR.getDriveType());

            robot.update();
            FtcDashboardManager.update();
        }
    }
}
