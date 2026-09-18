package com.sharan.deskcharm;

import com.sharan.deskcharm.physics.Vector2;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class Vector2Test {
    @Test void arithmeticWorks() {
        Vector2 a = new Vector2(3, 4);
        assertEquals(5, a.length(), 1e-9);
        assertEquals(new Vector2(4, 6), a.add(new Vector2(1, 2)));
        assertEquals(new Vector2(2, 2), a.subtract(new Vector2(1, 2)));
    }
}
