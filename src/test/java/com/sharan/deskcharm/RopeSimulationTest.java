package com.sharan.deskcharm;

import com.sharan.deskcharm.physics.RopeConfiguration;
import com.sharan.deskcharm.physics.RopeNode;
import com.sharan.deskcharm.physics.RopeSimulation;
import com.sharan.deskcharm.physics.Vector2;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RopeSimulationTest {

    private RopeSimulation newSimulation() {
        RopeConfiguration config = new RopeConfiguration(
                20, 9.0, new Vector2(0, 620), 0.985, 8, 1.12, 4.0, 20
        );
        return new RopeSimulation(config, new Vector2(130, 20));
    }

    @Test
    void topNodeStaysPinnedToAnchor() {
        RopeSimulation sim = newSimulation();
        sim.update(1.0);
        RopeNode top = sim.getNodes().get(0);
        assertEquals(130.0, top.getPosition().getX(), 1e-6);
        assertEquals(20.0, top.getPosition().getY(), 1e-6);
    }

    @Test
    void ropeSettlesBelowAnchorUnderGravity() {
        RopeSimulation sim = newSimulation();
        for (int i = 0; i < 600; i++) {
            sim.update(1.0 / 60.0);
        }
        Vector2 charmPosition = sim.getCharmPosition();
        assertTrue(charmPosition.getY() > 20.0, "charm should hang below the anchor");
    }

    @Test
    void segmentsDoNotExceedMaxStretch() {
        RopeSimulation sim = newSimulation();
        sim.beginDrag(new Vector2(500, 20));
        sim.updateDrag(new Vector2(900, 20));
        for (int i = 0; i < 30; i++) {
            sim.update(1.0 / 60.0);
        }
        List<RopeNode> nodes = sim.getNodes();
        double maxAllowed = sim.getConfiguration().getSegmentLength() * sim.getConfiguration().getMaxStretchFactor();
        for (int i = 0; i < nodes.size() - 1; i++) {
            double dist = nodes.get(i).getPosition().distanceTo(nodes.get(i + 1).getPosition());
            assertTrue(dist <= maxAllowed + 1e-6, "segment " + i + " stretched to " + dist);
        }
    }

    @Test
    void ropeEventuallySleepsWhenUndisturbed() {
        RopeSimulation sim = newSimulation();
        for (int i = 0; i < 3000; i++) {
            sim.update(1.0 / 60.0);
        }
        assertTrue(sim.isSleeping());
    }

    @Test
    void wakeUpClearsSleepState() {
        RopeSimulation sim = newSimulation();
        for (int i = 0; i < 3000; i++) {
            sim.update(1.0 / 60.0);
        }
        assertTrue(sim.isSleeping());
        sim.wakeUp();
        assertFalse(sim.isSleeping());
    }

    @Test
    void endDragImpartsThrowVelocity() {
        RopeSimulation sim = newSimulation();
        sim.beginDrag(new Vector2(130, 200));
        sim.updateDrag(new Vector2(130, 200));
        sim.updateDrag(new Vector2(230, 200));
        sim.endDrag();
        RopeNode bottom = sim.getNodes().get(sim.getNodes().size() - 1);
        assertTrue(bottom.getImplicitVelocity().getX() > 0, "throw should carry rightward momentum");
    }
}
