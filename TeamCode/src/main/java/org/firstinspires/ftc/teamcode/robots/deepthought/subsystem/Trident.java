package org.firstinspires.ftc.teamcode.robots.deepthought.subsystem;

import static org.firstinspires.ftc.teamcode.util.utilMethods.futureTime;
import static org.firstinspires.ftc.teamcode.util.utilMethods.isPast;
import static org.firstinspires.ftc.teamcode.util.utilMethods.withinError;

import android.graphics.Color;

import com.acmerobotics.dashboard.canvas.Canvas;
import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.hardware.CRServoImplEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.NormalizedColorSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;
import org.firstinspires.ftc.teamcode.robots.csbot.subsystem.Intake;
import org.firstinspires.ftc.teamcode.robots.deepthought.util.Constants;
import org.firstinspires.ftc.teamcode.robots.deepthought.util.DcMotorExResetable;
import org.firstinspires.ftc.teamcode.robots.deepthought.util.Joint;
import org.firstinspires.ftc.teamcode.robots.deepthought.util.Utils;

import java.util.*;


@Config(value = "00_ITD_TRIDENT")
public class Trident implements Subsystem {
    HardwareMap hardwareMap;
    Robot robot;

    public static boolean enforceSlideLimits;
    public DcMotorExResetable slide = null;
    public DcMotorExResetable crane = null;
    public Joint elbow;

    public CRServoImplEx beater = null;
    public Servo pincer;
    public static boolean tuckFromHighOuttake = false;

    public NormalizedColorSensor colorSensor = null;
    public static boolean colorSensorEnabled = false;

    public static int colorSensorGain = 12;

    public boolean calibrated = false;

    public void intakeSlideLimits() {
        if (slideTargetPosition > slidePositionMax) slideTargetPosition = slidePositionMax;
        if (slideTargetPosition < slidePositionMin) slideTargetPosition = slidePositionMin;
    }


    public enum Sample {
        RED, BLUE, NEUTRAL, NO_SAMPLE

    }

    public Sample currentSample = Sample.NO_SAMPLE;
    public List<Sample> targetSamples = new ArrayList<>();

    public static boolean preferHighOuttake = true;
    //SLIDE
    public static int slideTargetPosition = 0;
    public static int slidePositionMax = 3800;
    public static int slidePositionMin = 0;
    private static final int SLIDE_INTAKE_MIN_POSITION = 200;
    private static final int SLIDE_PREINTAKE_POSITION = 900;
    public static int SLIDE_LOWOUTTAKE_POSITION = 320;
    public static int SLIDE_HIGHOUTTAKE_POSITION = 2880;
    public static int slideSpeed = 80;
    public static double SLIDE_SPEED = 2000;


    //CRANE
    public static double CRANE_STALL_THRESHOLD = 4.5;
    public static int CRANE_CALIBRATE_ENCODER = Integer.MIN_VALUE;
    public int craneTargetPosition = 0;
    public static int craneSpeed = 45;
    public static int CRANE_HOME_POSITION = 250;
    public static int CRANE_INTAKE_POSITION = 125;
    public static int CRANE_LOWOUTTAKE_POSITION = 2105;
    public static int CRANE_HIGHOUTTAKE_POSITION = 1925;
    public int cranePositionMax = 850;

    //ELBOW JOINT VARIABLES
    public static double ELBOW_START_ANGLE = 145;
    public static int ELBOW_HOME_POSITION = 2050;
    public static double ELBOW_PWM_PER_DEGREE = -5.672222222222222;
    public static double ELBOW_JOINT_SPEED = 120;
    public static double ELBOW_MIN_ANGLE = -15;
    public static double ELBOW_MAX_ANGLE = 220;
    public static int ELBOW_ADJUST_ANGLE = 5;
    public static double ELBOW_PREINTAKE_ANGLE = 20;
    public static double ELBOW_LOWOUTTAKE_ANGLE = 102;
    public static double ELBOW_HIGHOUTTAKE_ANGLE = 70;

    //BEATER
    public double beaterPower;

    //PINCER
    public static double PINCER_CLOSE = 1750;
    public static double PINCER_OPEN = 2150;
    public boolean pincerEnabled = true;


    public enum Articulation {
        MANUAL, //does nothing - used for transition tracking
        TUCK, //does nothing - used for transition tracking
        INTAKE, OUTTAKE
    }

    //LIVE STATES
    public Articulation articulation;
    private Articulation prevArticulation;

    public Articulation articulate(Articulation articulation) {
        this.articulation = articulation;
        return articulation;
    }

    public Articulation articulate() {
        switch (articulation) {
            case MANUAL:
                break;
            case TUCK:
                if (tuck()) {
                    articulation = Articulation.MANUAL;
                }
                break;
            //SHOULD ONLY BE ACCESSED BY SAMPLE()
            case INTAKE:
                if (intake()) {
                    articulation = Articulation.MANUAL;
                }
                break;

            case OUTTAKE:
                if (outtake()) articulation = Articulation.MANUAL;
                break;
            default:
                throw new RuntimeException("how the ^%*( did you get here?");
        }
        prevArticulation = articulation;
        return articulation;
    }

    public long tuckTimer = 0;
    public static int tuckIndex = 0;

    public boolean tuck() {
        switch (tuckIndex) {
            case 0:
                tuckTimer = futureTime(.7);
                elbow.setTargetAngle(ELBOW_START_ANGLE);
                beaterPower = 0;
                tuckIndex++;
                break;
            case 1:
                if (isPast(tuckTimer)) {
                    slideTargetPosition = 0;
                    tuckIndex++;
                }
                break;
            case 2:
                if (slide.getCurrentPosition() < 150) {
                    craneTargetPosition = CRANE_HOME_POSITION;
                    return true;
                }
        }
        return false;
    }

    public static int intakeIndex;
    public long intakeTimer;

    public boolean intake() {
        switch (intakeIndex) {
            case 0:
                elbow.setTargetAngle(ELBOW_PREINTAKE_ANGLE);
                intakeTimer = futureTime(1);
                intakeIndex++;
                break;
            case 1:
                if (isPast(intakeTimer)) {
                    craneTargetPosition = CRANE_INTAKE_POSITION;
                    slideTargetPosition = SLIDE_PREINTAKE_POSITION;
                    intakeIndex++;
                }
                break;
            case 2:
                if (withinError(crane.getCurrentPosition(), CRANE_INTAKE_POSITION, 5) && withinError(slide.getCurrentPosition(), SLIDE_PREINTAKE_POSITION, 5)) {
                    intakeIndex++;
                    colorSensorEnabled = true;
                }
                break;
            case 3:
//                if (slideTargetPosition > SLIDE_INTAKE_MIN_POSITION) {
//                    slideTargetPosition -= 10;
//                }
                if (stopOnSample()) {
                    intakeIndex = 0;
                    robot.articulation = Robot.Articulation.MANUAL;
                    return true;
                }
                break;
        }
        return false;
    }

    public static int outtakeIndex;
    public long outtakeTimer;

    public boolean outtake() {
        switch (outtakeIndex) {
            case 0:
                outtakeTimer = futureTime(0);
//                elbow.setTargetAngle(preferHighOuttake ? ELBOW_HIGHOUTTAKE_ANGLE : ELBOW_LOWOUTTAKE_ANGLE);
                outtakeIndex++;
                break;
            case 1:
                if (isPast(outtakeTimer)) {
                    slideTargetPosition = preferHighOuttake ? SLIDE_HIGHOUTTAKE_POSITION : SLIDE_LOWOUTTAKE_POSITION;
                    craneTargetPosition = preferHighOuttake ? (int)(CRANE_HIGHOUTTAKE_POSITION * .75) : CRANE_LOWOUTTAKE_POSITION;
                    outtakeIndex++;
                }
                break;
            case 2:
                if (withinError(slide.getCurrentPosition(), preferHighOuttake ? SLIDE_HIGHOUTTAKE_POSITION : SLIDE_LOWOUTTAKE_POSITION, 5)) {
                    elbow.setTargetAngle(preferHighOuttake ? ELBOW_HIGHOUTTAKE_ANGLE : ELBOW_LOWOUTTAKE_ANGLE);
                    outtakeTimer = futureTime(.3);
                    outtakeIndex++;
                }
                break;
            case 3:
                if(isPast(outtakeTimer)) {
                    craneTargetPosition = preferHighOuttake ? CRANE_HIGHOUTTAKE_POSITION : CRANE_LOWOUTTAKE_POSITION;
                    outtakeIndex ++;
                }
                break;
            case 4:
                if (!sampleDetected()) {
                    return true;
                }
                break;
        }
        return false;
    }

    public void sample(List<Sample> samples) {
        articulation = Articulation.INTAKE;
        this.targetSamples = samples;
    }

    public void sample(Constants.Alliance alliance) {
        List<Sample> samples = new ArrayList<>();
        if (alliance.isRed()) samples.add(Sample.RED);
        else samples.add(Sample.BLUE);
        samples.add(Sample.NEUTRAL);
        articulation = Articulation.INTAKE;
        this.targetSamples = samples;
    }

    public boolean sampleDetected() {
        return targetSamples.contains(currentSample);
    }

    public boolean stopOnSample() {
        colorSensorEnabled = true;
        beaterPower = 1;
        if (targetSamples.contains(currentSample)) {
            beaterPower = 0;
            colorSensorEnabled = false;
            return true;
        }
        return false;
    }

    public Trident(HardwareMap hardwareMap, Robot robot) {
        this.hardwareMap = hardwareMap;
        this.robot = robot;
        elbow = new Joint(hardwareMap, "elbow", false, ELBOW_HOME_POSITION, ELBOW_PWM_PER_DEGREE, ELBOW_MIN_ANGLE, ELBOW_MAX_ANGLE, ELBOW_START_ANGLE, ELBOW_JOINT_SPEED);
        DcMotorEx bruh = this.hardwareMap.get(DcMotorEx.class, "slide");
        DcMotorEx bruhx2 = this.hardwareMap.get(DcMotorEx.class, "crane");
        slide = new DcMotorExResetable(bruh);
        crane = new DcMotorExResetable(bruhx2);
        colorSensor = this.hardwareMap.get(NormalizedColorSensor.class, "intakeSensor");
        beater = this.hardwareMap.get(CRServoImplEx.class, "beater");
        pincer = this.hardwareMap.get(Servo.class, "pincer");
        slide.setMotorEnable();
        slide.setPower(1);
        slide.setDirection(DcMotorSimple.Direction.REVERSE);
        slide.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        slide.setTargetPosition(0);
        slide.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        slide.setVelocity(SLIDE_SPEED);

        crane.setMotorEnable();
        crane.setPower(1);
        crane.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        crane.setTargetPosition(0);
        crane.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        crane.setVelocity(1500);
        articulation = Articulation.MANUAL;
        CRANE_CALIBRATE_ENCODER = Integer.MIN_VALUE;
    }

    public void adjustElbow(double angle) {
        elbow.setTargetAngle(elbow.getCurrentAngle() + angle);
    }


    @Override
    public void update(Canvas fieldOverlay) {
        //compute the current articulation/behavior
        articulate();
        //allow real-time flipper speed changes
        elbow.setSpeed(ELBOW_JOINT_SPEED);

        if (colorSensorEnabled) {
            updateColorSensor();
            colorSensor.setGain(colorSensorGain);
        }


        //actually instruct actuators to go to desired targets
        elbow.update();
        if (enforceSlideLimits) intakeSlideLimits();
        slide.setTargetPosition(slideTargetPosition);
        if (calibrated) {
            crane.setTargetPosition(craneTargetPosition);
        }
        beater.setPower(-beaterPower);

//        if(pincerEnabled) {
//            pincer.setPosition(Utils.servoNormalize(PINCER_OPEN));
//        }
//        else {
//            pincer.setPosition(Utils.servoNormalize(PINCER_CLOSE));
//        }

    }

    public String updateColorSensor() {
        double hue = getHSV()[0];
        if (hue < 45 && hue > 35) {
            currentSample = Sample.NEUTRAL;
            return "NEUTRAL";
        } else if (hue < 15 || hue > 350) {
            currentSample = Sample.RED;
            return "RED";
        } else if (hue < 225 && hue > 200) {
            currentSample = Sample.BLUE;
            return "BLUE";
        } else {
            currentSample = Sample.NO_SAMPLE;
            return "NO SAMPLE";
        }
    }

    @Override
    public void stop() {
        slide.setMotorDisable();
        beater.setPower(Utils.servoNormalize(1500));
    }

    public String HSVasString() {
        float[] hsv = getHSV();
        return hsv[0] + " " + hsv[1] + " " + hsv[2];
    }

    public float[] getHSV() {
        float[] hsv = new float[3];
        Color.colorToHSV(colorSensor.getNormalizedColors().toColor(), hsv);
        return hsv;
    }

    @Override
    public Map<String, Object> getTelemetry(boolean debug) {
        Map<String, Object> telemetryMap = new LinkedHashMap<>();
        telemetryMap.put("articulation", articulation.name());
        telemetryMap.put("preferHighOuttake", preferHighOuttake);
        telemetryMap.put("intake index", intakeIndex);
        telemetryMap.put("outtake index", outtakeIndex);
        telemetryMap.put("crane current", crane.getCurrent(CurrentUnit.AMPS));
        telemetryMap.put("crane target : real", "" + craneTargetPosition + " : " + crane.getCurrentPosition());
        telemetryMap.put("slide target : real", slideTargetPosition + " : " + slide.getCurrentPosition());
        telemetryMap.put("current sample", currentSample.name());
        telemetryMap.put("colorsensor", colorSensorEnabled);
        if (colorSensorEnabled) {
            telemetryMap.put("colorsensor hsv", "" + HSVasString());
            telemetryMap.put("colorsensor rgb", colorSensor.getNormalizedColors().red + " " + colorSensor.getNormalizedColors().green + " " + colorSensor.getNormalizedColors().blue);
        }
//        0 40 214
        telemetryMap.put("beater speed", beater.getPower());
        telemetryMap.put("elbow angle target : real", elbow.getTargetAngle() + " : " + elbow.getCurrentAngle());
        return telemetryMap;
    }

    @Override
    public String getTelemetryName() {
        return "TRIDENT";
    }
}
