package com.sharan.deskcharm.rendering;

import com.sharan.deskcharm.charm.Charm;
import com.sharan.deskcharm.charm.CharmType;
import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.paint.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Charm renderer. MURUGAN / DRISHTI_MASK / EVIL_EYE / CHILI_LEMON and CUSTOM charms are drawn from a
 *  real bundled or user-imported photo (see Charm.imagePath()); DANGLE_NAME charms are a procedurally
 *  drawn engraved plaque built from whatever text the person typed. A charm photo that fails to load
 *  falls back to a plain polished locket so the app never renders nothing. */
public final class CharmRenderer {
    private final Map<String, Image> imageCache = new ConcurrentHashMap<>();

    /** Loads (and caches) the real photo for a charm, if it has one. Returns null for text-only charms
     *  or if the image fails to load, so callers can fall back to procedural drawing. */
    public Image loadCharmImage(Charm charm, double size) {
        if (charm.imagePath() == null) return null;
        return imageCache.computeIfAbsent(charm.id() + "@" + (int) size, key -> {
            try {
                // Classpath resource (bundled charm photo) vs. filesystem path (user-imported custom charm).
                var stream = getClass().getResourceAsStream(charm.imagePath());
                Image img = stream != null
                        ? new Image(stream, size, size, true, true)
                        : new Image(java.nio.file.Path.of(charm.imagePath()).toUri().toString(), size, size, true, true);
                return img.isError() ? null : img;
            } catch (Exception e) {
                return null;
            }
        });
    }

    public void draw(GraphicsContext g, Charm charm, double x, double y, Image ignored, boolean shadow) {
        double r = charm.radius();
        CharmVisualTheme t = CharmVisualTheme.forType(charm.type());
        drawAura(g,t,x,y,r);
        if (shadow) drawContactShadow(g,x,y,r);

        Image photo = loadCharmImage(charm, r * 2.6);
        if (photo != null) {
            g.save();
            g.setEffect(new DropShadow(7,0,3,Color.rgb(0,0,0,.35)));
            // Anchor near the TOP of the photo (where its own ring/bail sits) rather than centering,
            // so the pendant visually hangs from the rope's end point instead of straddling it.
            double topOffset = r * 0.30;
            g.drawImage(photo, x - photo.getWidth()/2, y - topOffset);
            g.restore();
            return;
        }

        g.save();
        g.setEffect(new DropShadow(6.5,0,2.8,Color.rgb(0,0,0,.28)));
        switch (charm.type()) {
            case DANGLE_NAME -> nameDangle(g,x,y,r,t,charm.customText());
            case MURUGAN, DRISHTI_MASK, EVIL_EYE, CHILI_LEMON, CUSTOM -> photoFallback(g,x,y,r,t);
        }
        g.restore();
    }

    private void drawAura(GraphicsContext g,CharmVisualTheme t,double x,double y,double r){
        g.save();
        g.setFill(new RadialGradient(0,0,x-r*.25,y-r*.3,r*1.7,false,CycleMethod.NO_CYCLE,
                new Stop(0,Color.color(t.glow().getRed(),t.glow().getGreen(),t.glow().getBlue(),.12)),
                new Stop(.55,Color.color(t.glow().getRed(),t.glow().getGreen(),t.glow().getBlue(),.025)),
                new Stop(1,Color.TRANSPARENT)));
        g.fillOval(x-r*1.7,y-r*1.7,r*3.4,r*3.4); g.restore();
    }
    private void drawContactShadow(GraphicsContext g,double x,double y,double r){
        g.save();g.setEffect(new DropShadow(9,0,4,Color.rgb(0,0,0,.34)));g.setFill(Color.rgb(0,0,0,.16));
        g.fillOval(x-r*.64,y+r*.72,r*1.28,r*.18);g.restore();
    }
    private Paint gold(CharmVisualTheme t){return new LinearGradient(0,0,1,1,true,CycleMethod.NO_CYCLE,
            new Stop(0,t.metalHighlight()),new Stop(.24,t.metal()),new Stop(.58,Color.web("#9B6D24")),new Stop(.82,t.metal()),new Stop(1,Color.web("#6F4A17")));}
    private Paint silver(CharmVisualTheme t){return new LinearGradient(0,0,1,1,true,CycleMethod.NO_CYCLE,
            new Stop(0,Color.WHITE),new Stop(.25,t.metalHighlight()),new Stop(.58,t.metal()),new Stop(1,Color.web("#718396")));}

    /** Safe fallback for photo-based charms if the image ever fails to load: a plain polished locket
     *  rather than nothing, so a missing/corrupt file never breaks the overlay. */
    private void photoFallback(GraphicsContext g,double x,double y,double r,CharmVisualTheme t){
        g.setFill(gold(t));g.fillOval(x-r,y-r,r*2,r*2);
        g.setStroke(Color.rgb(255,246,200,.6));g.setLineWidth(1.4);g.strokeOval(x-r,y-r,r*2,r*2);
        g.setFill(Color.rgb(0,0,0,.22));g.fillOval(x-r*.5,y-r*.5,r,r);
    }

    /** Engraved gold plaque carrying whatever name/text the person typed into the Name Dangle field.
     *  Font size auto-shrinks to keep long names on a single line. */
    private void nameDangle(GraphicsContext g,double x,double y,double r,CharmVisualTheme t,String text){
        String label = (text == null || text.isBlank()) ? "Name" : text.trim();
        double plateW = Math.max(r*2.6, Math.min(r*5.2, r*1.15 + label.length()*r*.32));
        double plateH = r*1.55;

        g.setFill(gold(t));
        g.fillRoundRect(x-plateW/2, y-plateH/2, plateW, plateH, plateH*.55, plateH*.55);
        g.setStroke(Color.rgb(255,246,200,.65));g.setLineWidth(1.6);
        g.strokeRoundRect(x-plateW/2, y-plateH/2, plateW, plateH, plateH*.55, plateH*.55);

        double innerW = plateW*.90, innerH = plateH*.64;
        g.setFill(Color.rgb(255,255,255,.12));
        g.fillRoundRect(x-innerW/2, y-innerH/2, innerW, innerH, innerH*.5, innerH*.5);
        g.setStroke(Color.rgb(0,0,0,.18));g.setLineWidth(1);
        g.strokeRoundRect(x-innerW/2, y-innerH/2, innerW, innerH, innerH*.5, innerH*.5);

        double fontSize = plateH*.60;
        Text measure = new Text();
        Font font;
        do {
            font = Font.font("Segoe Script", fontSize);
            measure.setFont(font);
            measure.setText(label);
            fontSize -= 1;
        } while (measure.getLayoutBounds().getWidth() > innerW*.90 && fontSize > 8);

        g.setFont(font);
        g.setTextAlign(TextAlignment.CENTER);
        g.setTextBaseline(VPos.CENTER);
        g.setFill(Color.rgb(255,255,255,.35));
        g.fillText(label, x+0.8, y+plateH*0.03+0.8);
        g.setFill(Color.web("#3A2A08"));
        g.fillText(label, x, y+plateH*0.03);

        g.setFill(Color.web("#C0392B"));g.fillOval(x-plateW/2+plateH*.20-4, y-4, 8,8);
        g.setFill(Color.web("#1E824C"));g.fillOval(x+plateW/2-plateH*.20-4, y-4, 8,8);
    }
}
