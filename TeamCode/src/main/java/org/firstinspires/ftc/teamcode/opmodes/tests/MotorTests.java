package org.firstinspires.ftc.teamcode.opmodes.tests;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

@TeleOp(name="MotorTests")
public class MotorTests extends LinearOpMode {
    /**
     * Override this method and place your code here.
     * <p>
     * Please do not catch {@link InterruptedException}s that are thrown in your OpMode
     * unless you are doing it to perform some brief cleanup, in which case you must exit
     * immediately afterward. Once the OpMode has been told to stop, your ability to
     * control hardware will be limited.
     *
     * @throws InterruptedException When the OpMode is stopped while calling a method
     *                              that can throw {@link InterruptedException}
     */
    @Override
    public void runOpMode() throws InterruptedException {
        DcMotor leftMotor = hardwareMap.get(DcMotor.class, "left");
        DcMotor rightMotor = hardwareMap.get(DcMotor.class, "right");

        waitForStart();

        while (opModeIsActive()) {
            if (gamepad1.a) {
                leftMotor.setPower(0.5);
            }
            if (gamepad1.b) {
                rightMotor.setPower(0.5);
            }
            leftMotor.setPower(0);
            rightMotor.setPower(0);
        }
    }
}
