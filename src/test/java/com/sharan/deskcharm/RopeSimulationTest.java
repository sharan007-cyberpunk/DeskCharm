package com.sharan.deskcharm;

import com.sharan.deskcharm.physics.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RopeSimulationTest {
    @Test void createsExpectedNodes() {
        RopeSimulation sim = new RopeSimulation(new RopeConfiguration(20, 15, 900, .995, 12, 1.025));
        assertEquals(21, sim.getNodes().size());
    }

    @Test void segmentsDoNotExceedMaxStretch() {
        double segmentLength = 15.0;
        double maxStretch = 1.025;
        RopeSimulation sim = new RopeSimulation(
                new RopeConfiguration(20, segmentLength, 900, .995, 12, maxStretch));
        sim.beginDrag(new Vector2(1000, 1000));
        for (int i = 0; i < 240; i++) sim.step(1.0 / 240.0);

        double max = segmentLength * maxStretch;
        for (int i = 0; i < sim.getNodes().size() - 1; i++) {
            double distance = sim.getNodes().get(i).position()
                    .distance(sim.getNodes().get(i + 1).position());
            assertTrue(distance <= max + 1e-9,
                    "segment " + i + " stretched to " + distance);
        }
    }

    @Test void anchorRemainsFixed() {
        RopeSimulation sim = new RopeSimulation(new RopeConfiguration(20, 15, 900, .995, 12, 1.025));
        sim.setAnchor(new Vector2(100, 50));
        for (int i = 0; i < 100; i++) sim.step(1.0/240.0);
        assertEquals(100, sim.getNodes().get(0).position().x(), 1e-9);
        assertEquals(50, sim.getNodes().get(0).position().y(), 1e-9);
    }
}
