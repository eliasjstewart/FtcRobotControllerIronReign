package org.firstinspires.ftc.teamcode.robots.limelight;

import com.acmerobotics.dashboard.canvas.Canvas;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.robots.deepthought.subsystem.Subsystem;

import java.util.LinkedHashMap;
import java.util.Map;

public class Robot implements Subsystem {

    public HardwareMap hardwareMap;
    Subsystem[] subsystems;
    Limelight limelight;
    public Robot(HardwareMap hardwareMap){

    }


    @Override
    public void update(Canvas fieldOverlay) {
        for (Subsystem subsystem : subsystems) {
            subsystem.update(fieldOverlay);
        }
        handleTelemetry();
    }

    public void handleTelemetry() {

    }

    @Override
    public void stop() {

    }

    @Override
    public Map<String, Object> getTelemetry(boolean debug) {
        Map<String, Object> telemetryMap = new LinkedHashMap<>();

        return telemetryMap;
    }

    @Override
    public String getTelemetryName() {
        return "Robot";
    }
}
