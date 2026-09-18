package com.sharan.deskcharm;

import com.sharan.deskcharm.physics.Vector2;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Vector2Test {

    @Test
    void addComputesComponentWiseSum() {
        Vector2 a = new Vector2(1.0, 2.0);
        Vector2 b = new Vector2(3.0, -1.0);
        Vector2 result = a.add(b);
        assertEquals(4.0, result.getX(), 1e-9);
        assertEquals(1.0, result.getY(), 1e-9);
    }

    @Test
    void lengthMatchesPythagorean() {
        Vector2 v = new Vector2(3.0, 4.0);
        assertEquals(5.0, v.length(), 1e-9);
    }

    @Test
    void distanceToIsSymmetric() {
        Vector2 a = new Vector2(0.0, 0.0);
        Vector2 b = new Vector2(6.0, 8.0);
        assertEquals(10.0, a.distanceTo(b), 1e-9);
        assertEquals(a.distanceTo(b), b.distanceTo(a), 1e-9);
    }

    @Test
    void normalizeOfZeroVectorIsZero() {
        Vector2 zero = new Vector2(0.0, 0.0);
        assertEquals(Vector2.ZERO, zero.normalize());
    }
}
