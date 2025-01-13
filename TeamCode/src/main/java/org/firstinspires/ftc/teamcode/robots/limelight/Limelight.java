package org.firstinspires.ftc.teamcode.robots.limelight;

import com.acmerobotics.dashboard.canvas.Canvas;
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.robots.deepthought.subsystem.Subsystem;

import java.util.LinkedHashMap;
import java.util.Map;

public class Limelight implements Subsystem {
    private Limelight3A limelight;


    Limelight (HardwareMap hardwareMap, Robot lime){
    limelight = hardwareMap.get(Limelight3A.class, "limelight");

    limelight.setPollRateHz(100);
    limelight.start();

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
        LLResult result = limelight.getLatestResult();
    if(result != null && result.isValid()){
        double tx = result.getTx();
        double ty = result.getTy();
        double ta = result.getTa();
        telemetryMap.put("Target X", tx);
        telemetryMap.put("Target Y", ty);
        telemetryMap.put("Target Area", ta);
    }
    else{ telemetryMap.put("Limelight", "No Targets");}
        return telemetryMap;
    }

    @Override
    public String getTelemetryName() {
        return "Robot";
    }
}
