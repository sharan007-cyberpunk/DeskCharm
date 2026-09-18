package com.sharan.deskcharm.rendering;

import com.sharan.deskcharm.charm.CharmType;
import javafx.scene.paint.Color;

/** Material palette shared by each charm and its matching cord. */
public record CharmVisualTheme(Color ropeBase, Color ropeHighlight, Color ropeShadow,
                               Color accent, Color glow, Color metal, Color metalHighlight) {
    public static CharmVisualTheme forType(CharmType type) {
        return switch (type) {
            case EVIL_EYE -> t("#183B67","#77D7FF","#07182D","#28A9E8","#2AA9FF","#C7A25B","#FFF1B7");
            case CROSS -> t("#6B4A18","#FFE4A0","#211405","#C99838","#E5B85A","#C89B42","#FFF0B0");
            case INITIAL_S -> t("#6C4A1C","#FFF0B0","#241605","#D3A548","#F2C968","#D2A64D","#FFF4C2");
            case HAMSA -> t("#234B66","#8EE6FF","#071D2D","#4AC9EE","#48C9F2","#C5A45B","#FFF0B0");
            case CLOVER -> t("#154A32","#83E9B1","#061F15","#43C980","#45D28A","#CBA65B","#FFF1B0");
            case CROWN -> t("#72551C","#FFE6A0","#241702","#E3B54D","#F1C765","#D5AA4D","#FFF2B8");
            case INFINITY -> t("#303A49","#DCEBFF","#10151E","#B9CBE2","#9EC7F2","#B8C4D1","#FFFFFF");
            case COMPASS -> t("#21384C","#8BC5E8","#071722","#5EACD6","#5CAFE0","#BDA568","#FFF0B5");
            case LOTUS -> t("#54264E","#F1B8D9","#1E0A1A","#D77CB8","#D77CB8","#CDAA66","#FFF1BC");
            case FEATHER -> t("#39414D","#E7EDF5","#11151B","#B9C4D4","#AABFD9","#B7A678","#FFF3C0");
            case HEART -> t("#672A31","#FF9FAE","#220B10","#E85B72","#EA617A","#D0A65A","#FFF1B5");
            case LIGHTNING -> t("#4D4316","#FFF4A8","#191602","#F0D34D","#E8CB4E","#D0A94C","#FFF4BD");
            case SUN -> t("#76531A","#FFE7A0","#251803","#E6B94F","#EBC25C","#D3A64B","#FFF3BA");
            case MANEKI_NEKO -> t("#805019","#FFE19A","#2A1706","#E9B64B","#E5AD45","#D7AA55","#FFF0B3");
            case GANESHA -> t("#71321E","#FFB17A","#2A0D06","#E68045","#E47D45","#D0A15A","#FFEFB5");
            case STAR -> t("#574411","#FFF0A4","#211804","#E1B64A","#E5BE58","#D0A64B","#FFF0B0");
            case MOON -> t("#314A6C","#D7EAFF","#0B1728","#9BC7EE","#8DBDE9","#B7C6D6","#FFFFFF");
            case PLANET -> t("#39256D","#C8B4FF","#130B2C","#9876F0","#9C7BEF","#B8A9D7","#F3ECFF");
            case DIAMOND -> t("#184A61","#C4F5FF","#061D28","#74DDF3","#70D9F1","#BFD8DF","#FFFFFF");
            case LEAF, CUSTOM -> t("#28533A","#A5E8BA","#0A2316","#6BD28B","#66D48C","#C7B275","#FFF2B9");
        };
    }
    private static CharmVisualTheme t(String base,String hi,String shadow,String accent,String glow,String metal,String metalHi) {
        return new CharmVisualTheme(Color.web(base),Color.web(hi),Color.web(shadow),Color.web(accent),Color.web(glow),Color.web(metal),Color.web(metalHi));
    }
}
