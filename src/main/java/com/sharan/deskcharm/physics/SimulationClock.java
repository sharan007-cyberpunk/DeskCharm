package com.sharan.deskcharm.physics;

/**
 * Fixed-timestep accumulator that decouples the physics step rate from the
 * JavaFX rendering frame rate. Callers feed in the wall-clock delta observed
 * by the render loop via {@link #accumulate(double)}, then repeatedly call
 * {@link #consumeStep()} until it returns false, running exactly one physics
 * step per successful consumption.
 *
 * This keeps the rope simulation numerically stable even if the screen's
 * refresh rate varies or a frame is delayed, since {@link RopeSimulation}
 * always advances in constant-size {@link #FIXED_TIMESTEP} increments.
 */
public final class SimulationClock {

    /** ~1/240 second fixed physics step, as required by the rope specification. */
    public static final double FIXED_TIMESTEP = 1.0 / 240.0;

    /**
     * Upper bound on how much wall-clock time can be accumulated in one go.
     * Prevents a "spiral of death" (e.g. after the app was minimized or the
     * debugger paused execution) where an enormous backlog of steps would
     * otherwise need to run synchronously before the loop catches up.
     */
    private static final double MAX_ACCUMULATED_SECONDS = 0.25;

    private double accumulator = 0.0;

    /**
     * Adds observed wall-clock time (in seconds) to the accumulator, clamped
     * so a single very long frame cannot force an unbounded number of steps.
     */
    public void accumulate(double frameDeltaSeconds) {
        if (frameDeltaSeconds < 0) {
            return;
        }
        accumulator = Math.min(accumulator + frameDeltaSeconds, MAX_ACCUMULATED_SECONDS);
    }

    /**
     * If enough time has accumulated for one more fixed step, consumes it and
     * returns true. Callers should loop on this method until it returns false.
     */
    public boolean consumeStep() {
        if (accumulator >= FIXED_TIMESTEP) {
            accumulator -= FIXED_TIMESTEP;
            return true;
        }
        return false;
    }

    /** Fraction (0..1) of a fixed step remaining in the accumulator, useful for render interpolation. */
    public double getInterpolationAlpha() {
        return accumulator / FIXED_TIMESTEP;
    }

    public void reset() {
        accumulator = 0.0;
    }
}
