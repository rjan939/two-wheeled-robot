package org.firstinspires.ftc.teamcode.opmodes;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.util.RobotHardware;
import org.firstinspires.ftc.teamcode.util.drive.DriveState;
import org.firstinspires.ftc.teamcode.util.drive.DriveType;
import org.firstinspires.ftc.teamcode.util.drive.constants.BalanceConstants;
import org.firstinspires.ftc.teamcode.util.lib.FtcDashboardManager;
import org.firstinspires.ftc.teamcode.util.lib.GamepadButton;
import org.firstinspires.ftc.teamcode.util.lib.StatefulGamepad;


@Config
@TeleOp(name = "EasyDrivePID")
public class EasyDrivePID extends LinearOpMode {
    @Override
    public void runOpMode() {
        RobotHardware robot = new RobotHardware(hardwareMap, DriveType.SMOOTH, false);

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

            robot.drivePID.drive(-gamepad1.left_stick_y, gamepad1.left_stick_x, gamepad1Buttons.getButton(GamepadButton.LEFT_BUMPER));

            if (gamepad1Buttons.getButton(GamepadButton.RIGHT_BUMPER)) {
                robot.drivePID.resetTarget();
            }

            if (gamepad1Buttons.getButton(GamepadButton.RIGHT_STICK_BUTTON)) {
                robot.drivePID.emergencyStop();
            }

            if (gamepad1Buttons.wasJustPressed(GamepadButton.LEFT_STICK_BUTTON) && robot.drivePID.getState() == DriveState.STOPPED) {
                robot.upright();
            }

            if (gamepad1Buttons.wasJustPressed(GamepadButton.RIGHT_STICK_BUTTON)) {
                robot.drivePID.setTargetAngle(BalanceConstants.TargetAngle);
            }

            robot.update();

            FtcDashboardManager.update();
        }
    }
}