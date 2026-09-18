package com.sharan.deskcharm.interaction;

/**
 * Small state machine describing what the user is currently doing
 * with the charm. Kept separate from JavaFX event handling so it
 * can be reasoned about (and tested) independently.
 */
public enum InteractionState {
    IDLE,
    HOVERING,
    GRABBED
}
