package org.firstinspires.ftc.teamcode.robots.limelight;

import com.acmerobotics.dashboard.canvas.Canvas;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.robots.deepthought.subsystem.Subsystem;

import java.util.LinkedHashMap;
import java.util.Map;

public class Limelight implements Subsystem {
    private Limelight3A limelight;

    Limelight (HardwareMap hardwareMap, Robot lime){
    limelight = hardwareMap.get(Limelight3A.class, "limelight");
    }
    @Override
    public void update(Canvas fieldOverlay) {
        
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
