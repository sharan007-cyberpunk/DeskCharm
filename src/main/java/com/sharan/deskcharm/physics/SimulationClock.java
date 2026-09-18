package com.sharan.deskcharm.physics;

public final class SimulationClock {
    private static final double FIXED_DT = 1.0 / 240.0;
    private double accumulator;

    public int consume(double frameDelta, RopeSimulation simulation) {
        accumulator += Math.min(frameDelta, 0.1);
        int steps = 0;
        while (accumulator >= FIXED_DT && steps < 12) {
            simulation.step(FIXED_DT);
            accumulator -= FIXED_DT;
            steps++;
        }
        return steps;
    }
}
