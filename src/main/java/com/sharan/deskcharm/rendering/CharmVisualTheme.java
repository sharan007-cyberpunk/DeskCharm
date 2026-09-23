package com.sharan.deskcharm.rendering;

import com.sharan.deskcharm.charm.CharmType;
import javafx.scene.paint.Color;

/** Material palette shared by each charm and its matching cord/beads. Picked to echo the
 *  charm's own real materials (antique bronze, red-black enamel, cobalt glass, braided cord). */
public record CharmVisualTheme(Color ropeBase, Color ropeHighlight, Color ropeShadow,
                               Color accent, Color glow, Color metal, Color metalHighlight) {
    public static CharmVisualTheme forType(CharmType type) {
        return switch (type) {
            case MURUGAN -> t("#5C4420","#E8C87A","#1E1608","#C9962E","#D9A84B","#8A6A2E","#F0D998");
            case DRISHTI_MASK -> t("#4A0A06","#FF8A65","#1A0503","#D0392B","#E85B4A","#C9962E","#FFF3C4");
            case EVIL_EYE -> t("#183B67","#77D7FF","#07182D","#28A9E8","#2AA9FF","#C7A25B","#FFF1B7");
            case CHILI_LEMON -> t("#1E1E1E","#8FBF3F","#0A0A0A","#E0B400","#6FBF3F","#3A3A3A","#D8D8D8");
            case DANGLE_NAME -> t("#3A2A08","#F7DD8A","#140E03","#D4AF37","#F0C14B","#C9962E","#FFF3C4");
            case CUSTOM -> t("#28533A","#A5E8BA","#0A2316","#6BD28B","#66D48C","#C7B275","#FFF2B9");
        };
    }
    private static CharmVisualTheme t(String base,String hi,String shadow,String accent,String glow,String metal,String metalHi) {
        return new CharmVisualTheme(Color.web(base),Color.web(hi),Color.web(shadow),Color.web(accent),Color.web(glow),Color.web(metal),Color.web(metalHi));
    }
}
