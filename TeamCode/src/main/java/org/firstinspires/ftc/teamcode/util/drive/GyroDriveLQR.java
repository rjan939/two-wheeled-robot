package org.firstinspires.ftc.teamcode.util.drive;

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;
import org.firstinspires.ftc.teamcode.util.drive.constants.BalanceConstants;
import org.firstinspires.ftc.teamcode.util.drive.constants.LQRConstants;
import org.firstinspires.ftc.teamcode.util.drive.constants.SpeedConstants;
import org.firstinspires.ftc.teamcode.util.lib.FtcDashboardManager;
import org.hipparchus.linear.MatrixUtils;
import org.hipparchus.linear.RealMatrix;

public class GyroDriveLQR {
    private final DcMotor leftMotor;
    private final DcMotor rightMotor;

    private DriveType driveType;

    private DriveState lastState = DriveState.STOPPED;
    private DriveState state = DriveState.STOPPED;

    private YawPitchRollAngles angles;

    private final LQRController controller;

    private final PIDController angleController = new PIDController(BalanceConstants.AnglePID);

    private final Pose pose = new Pose();

    private double targetAngle = BalanceConstants.TargetAngle;

    private boolean lastBalanced = false;
    private ElapsedTime enablingTimer = null;

    private double lastTargetVel = 0;
    private double targetVel = 0;
    private double currentVel = 0;

    private double rotationVel = 0;

    private double targetPos = 0;
    private double lastPos = 0;
    private double currentPos = 0;

    private double lastTime = 0;

    private boolean emergencyStop = false;

    private boolean stopping = false;
    private ElapsedTime stoppingTimer = null;

    private boolean position = false;

    public GyroDriveLQR(HardwareMap hardwareMap, DriveType driveType) {
        leftMotor = hardwareMap.get(DcMotor.class, "right");
        rightMotor = hardwareMap.get(DcMotor.class, "left");
        leftMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rightMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rightMotor.setDirection(DcMotorSimple.Direction.REVERSE);
        leftMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        rightMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        this.driveType = driveType;

        leftMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        updateCurrentVelocity();
        updateCurrentVelocity();

        controller = new LQRController();
    }

    public void initIMU(IMU imu) {
        imu.initialize(
                new IMU.Parameters(
                        new RevHubOrientationOnRobot(
                                RevHubOrientationOnRobot.LogoFacingDirection.BACKWARD,
                                RevHubOrientationOnRobot.UsbFacingDirection.RIGHT
                        )
                )
        );
    }

    public void setDriveType(DriveType driveType) {
        this.driveType = driveType;
    }

    public DriveType getDriveType() {
        return driveType;
    }

    public Pose getPose() {
        return pose;
    }

    public void emergencyStop() {
        emergencyStop = true;
        leftMotor.setPower(0);
        rightMotor.setPower(0);
    }

    public void drive(double drivePower, double turnPower, boolean fast) {
        targetVel = drivePower * (driveType == DriveType.SMOOTH ? (fast ? SpeedConstants.FastDrive : SpeedConstants.Drive) : (fast ? SpeedConstants.FastOffroadDrive : SpeedConstants.OffroadDrive));
        if (!position && isBalanced() && targetVel != 0) position = true;
        if (!position) {
            targetPos = currentPos;
            setTargetAngle(BalanceConstants.TargetAngle);
        }
        if (lastTargetVel != 0 && targetVel != 0) {
            targetPos = currentPos;
        }
        if (targetVel == 0 && lastTargetVel != 0) {
            stopping = true;
        }
        rotationVel = turnPower * (fast ? SpeedConstants.FastTurn : SpeedConstants.Turn);
        lastTargetVel = targetVel;
    }

    public void setTargetPos() {
        targetPos = currentPos;
    }

    public void setTargetAngle(double target) {
        targetAngle = target;
    }

    public void brake() {
        setTargetPos();
        setTargetAngle(BalanceConstants.TargetAngle);
        if (!position && isPlaceable()) {
            position = true;
        }
    }

    public void update(YawPitchRollAngles angles, double pitchRate) {
        this.angles = angles;

        if (!lastBalanced) {
            if (isPlaceable()) {
                if (enablingTimer == null) enablingTimer = new ElapsedTime();
                else if (enablingTimer.seconds() > BalanceConstants.PlaceDelay) {
                    lastBalanced = true;
                    targetPos = currentPos;
                }
                state = DriveState.PLACING;
            } else {
                enablingTimer = null;
                state = DriveState.STOPPED;
            }
            return;
        }

        if (!isBalanced() || emergencyStop) {
            if (!isBalanced() && emergencyStop) emergencyStop = false;
            stopMotors();
            state = DriveState.STOPPED;
            lastBalanced = false;
            return;
        } else if (!lastBalanced) {
            lastBalanced = true;
        }

        if (LQRConstants.UpdateLQRGains) {
            controller.updateK();
            LQRConstants.UpdateLQRGains = false;
        }

        updateCurrentVelocity();

        /*if (Math.abs(targetVel) < 0.05 && isBalanced() && !position) {
            targetPos = currentPos;
            position = true;
        }

         if (Math.abs(targetVel) >= 0.05) {
             position = false;
         }*/

        /*if (Math.abs(targetVel) < 0.05) {
            if (Math.abs(currentVel) > 0.1) {
                targetVel *= 0.9;
            } else {
                targetVel = 0;
            }
        }*/

        if (Math.abs(targetVel) < 0.05 && Math.abs(currentVel) < 0.1) {
            //targetAngle *= 0.9;
        }

        double currentAngle = angles.getPitch(AngleUnit.DEGREES);

        RealMatrix currentState = getCurrentState(currentAngle, pitchRate, currentVel);

        RealMatrix targetState = getTargetState();

        double[] output = controller.calculateOutputPowers(currentState, targetState);

        if (driveType == DriveType.NONE) {
            setPower(0, 0);
        } else {
            double left = output[0] + rotationVel;
            double right = output[1] - rotationVel;

            left = Math.max(Math.min(left, 1.0), -1.0);
            right = Math.max(Math.min(right, 1.0), -1.0);

            left *= 0.7;
            right *= 0.7;
            setPower(left, right);
        }

        if (targetVel == 0 || BalanceConstants.AngleAssistedDriving) {
            updateAngle();
        }

        updateState();

        FtcDashboardManager.addData("Angle", currentAngle);
        FtcDashboardManager.addData("AngleError", currentAngle-targetAngle);
        FtcDashboardManager.addData("PitchRate", pitchRate);
        FtcDashboardManager.addData("CurrentVelocity", currentVel);
        FtcDashboardManager.addData("TargetVelocity", targetVel);
        FtcDashboardManager.addData("Turn", rotationVel);
        FtcDashboardManager.addData("CurrentPos", currentPos);
        FtcDashboardManager.addData("TargetPos", targetPos);
        FtcDashboardManager.addData("Output", output[0] + " | " + output[1]);
        FtcDashboardManager.addData("K", controller.getK());
        overlayRobot();
        lastTime = System.currentTimeMillis();
    }

    public void updateAngle() {
        if (!BalanceConstants.UpdateAngle) return;

        double error = targetVel - currentVel;
        targetAngle -= angleController.update(error);
        targetAngle = Math.min(BalanceConstants.MaxTargetAngle, Math.max(-BalanceConstants.MaxTargetAngle, targetAngle));

        FtcDashboardManager.addData("TargetAngle", targetAngle);
    }

    private void updateCurrentVelocity() {
        currentPos = (double) (leftMotor.getCurrentPosition() + rightMotor.getCurrentPosition()) / 2 * LQRConstants.M_PER_TICKS * LQRConstants.PositionModifier;

        currentVel = (currentPos - lastPos) / (System.currentTimeMillis() - lastTime) / LQRConstants.PositionModifier * LQRConstants.VelocityModifier;

        lastPos = currentPos;
    }

    public RealMatrix getCurrentState(double angle, double pitchRate, double currentVelocity) {
        return MatrixUtils.createRealMatrix(new double[][]{
                {angle},
                {pitchRate},
                {currentPos},
                {currentVelocity}
        });
    }

    private RealMatrix getTargetState() {
        double target = targetAngle;

        double targetVel = this.targetVel;

        /*if (targetVel == 0 && stopping) {
            targetVel = (-currentVel / Math.abs(currentVel)) * Math.min(Math.abs(currentVel) * LQRConstants.StoppingAmount, SpeedConstants.Stopping);
        } else if (targetVel != 0 && stopping) {
            stopping = false;
        }

        if (stopping && currentVel < LQRConstants.StoppedMargin) {
            targetVel = 0;
            stoppingTimer = new ElapsedTime();
            if (stoppingTimer.milliseconds() < LQRConstants.StoppedTime * 1000) {
                stopping = false;
                stoppingTimer = null;
            }
        }*/

        double adjustedTargetVel = targetVel;
        if (Math.abs(targetVel) < 0.05 && Math.abs(currentVel) > 0.05) {
            adjustedTargetVel = -currentVel * 0.3; // tune braking coefficient, 0.2-0.5
        }

        return MatrixUtils.createRealMatrix(new double[][]{
                {target}, // pitch angle
                {0}, // pitch rate
                {targetPos}, // position
                {adjustedTargetVel} // velocity
        });
    }

    private void updateState() {
        lastState = state;
        if (isBalanced()) {
            if (targetVel == 0) {
                state = DriveState.IDLE;
            } else {
                state = DriveState.DRIVING;
            }
        } else {
            state = DriveState.STOPPED;
            position = false;
        }
    }

    private void setPower(double leftPower, double rightPower) {
        leftMotor.setPower(leftPower);
        rightMotor.setPower(rightPower);
    }

    public boolean isBalanced() {
        if (angles == null) return false;
        return Math.abs(angles.getPitch(AngleUnit.DEGREES) - BalanceConstants.TargetAngle) < BalanceConstants.MaxAngle;
    }

    public boolean isPlaceable() {
        return Math.abs(angles.getPitch(AngleUnit.DEGREES) - BalanceConstants.TargetAngle) < BalanceConstants.MaxPlaceAngle;
    }

    public double getTurnVelocity() {
        return rotationVel;
    }

    public void stopMotors() {
        leftMotor.setPower(0);
        rightMotor.setPower(0);
    }

    public DriveState getLastState() {
        return lastState;
    }

    public DriveState getState() {
        return state;
    }

    private void overlayRobot() {
        FtcDashboardManager.getPacket().fieldOverlay()
                .setFill("white")
                .setStroke("green")
                .fillCircle(pose.x, pose.y, 5)
                .strokeLine(pose.x, pose.y, Math.cos(Math.toRadians(pose.heading)) * 4, Math.sin(Math.toRadians(pose.heading)) * 4);
    }
}
