package com.sharan.deskcharm.rendering;

import com.sharan.deskcharm.charm.Charm;
import com.sharan.deskcharm.charm.CharmType;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.paint.*;
import javafx.scene.shape.ArcType;

/** Premium jewelry-style charm renderer: polished metal, enamel, gemstones and restrained contact shadows. */
public final class CharmRenderer {
    public Image loadCharmImage(Charm charm, double size) { return null; }

    public void draw(GraphicsContext g, Charm charm, double x, double y, Image ignored, boolean shadow) {
        double r = charm.radius();
        CharmVisualTheme t = CharmVisualTheme.forType(charm.type());
        drawAura(g,t,x,y,r);
        if (shadow) drawContactShadow(g,x,y,r);
        g.save();
        g.setEffect(new DropShadow(6.5,0,2.8,Color.rgb(0,0,0,.28)));
        switch (charm.type()) {
            case EVIL_EYE -> evilEye(g,x,y,r,t);
            case CROSS -> cross(g,x,y,r,t);
            case INITIAL_S -> initialS(g,x,y,r,t);
            case HAMSA -> hamsa(g,x,y,r,t);
            case CLOVER -> clover(g,x,y,r,t);
            case CROWN -> crown(g,x,y,r,t);
            case INFINITY -> infinity(g,x,y,r,t);
            case COMPASS -> compass(g,x,y,r,t);
            case LOTUS -> lotus(g,x,y,r,t);
            case FEATHER -> feather(g,x,y,r,t);
            case HEART -> heart(g,x,y,r,t);
            case LIGHTNING -> lightning(g,x,y,r,t);
            case SUN -> sun(g,x,y,r,t);
            case MANEKI_NEKO -> maneki(g,x,y,r,t);
            case GANESHA -> ganesha(g,x,y,r,t);
            case STAR -> star(g,x,y,r,t);
            case MOON -> moon(g,x,y,r,t);
            case PLANET -> planet(g,x,y,r,t);
            case DIAMOND -> diamond(g,x,y,r,t);
            case LEAF, CUSTOM -> leaf(g,x,y,r,t);
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
    private void outline(GraphicsContext g,double x,double y,double r){g.setStroke(Color.rgb(255,248,220,.62));g.setLineWidth(1.0);}

    private void evilEye(GraphicsContext g,double x,double y,double r,CharmVisualTheme t){
        // New design: a slim marquise jewel, gold bezel, navy enamel, sapphire iris and micro-gem highlights.
        g.beginPath();
        g.moveTo(x-r*1.05,y); g.bezierCurveTo(x-r*.58,y-r*.72,x+r*.58,y-r*.72,x+r*1.05,y);
        g.bezierCurveTo(x+r*.58,y+r*.72,x-r*.58,y+r*.72,x-r*1.05,y); g.closePath();
        g.setFill(gold(t)); g.fill();
        g.beginPath();
        g.moveTo(x-r*.88,y); g.bezierCurveTo(x-r*.48,y-r*.48,x+r*.48,y-r*.48,x+r*.88,y);
        g.bezierCurveTo(x+r*.48,y+r*.48,x-r*.48,y+r*.48,x-r*.88,y); g.closePath();
        g.setFill(Color.web("#071C38")); g.fill();
        g.setFill(new RadialGradient(0,.15,x-r*.12,y-r*.15,r*.34,false,CycleMethod.NO_CYCLE,new Stop(0,Color.web("#C7F8FF")),new Stop(.28,Color.web("#36C9F3")),new Stop(.72,Color.web("#0967B6")),new Stop(1,Color.web("#063B79"))));
        g.fillOval(x-r*.31,y-r*.31,r*.62,r*.62);
        g.setFill(Color.web("#F7FBFF"));g.fillOval(x-r*.18,y-r*.18,r*.36,r*.36);
        g.setFill(Color.web("#071321"));g.fillOval(x-r*.105,y-r*.105,r*.21,r*.21);
        g.setFill(Color.WHITE);g.fillOval(x-r*.075,y-r*.095,r*.055,r*.055);
        // tiny bezel stones
        g.setFill(Color.rgb(245,246,239,.88));
        for(int i=0;i<7;i++){double a=Math.PI*(i/6.0);double sx=x+Math.cos(a)*r*.68, sy=y-Math.sin(a)*r*.40;g.fillOval(sx-1.35,sy-1.35,2.7,2.7);}
        outline(g,x,y,r); g.beginPath(); g.moveTo(x-r*1.05,y); g.bezierCurveTo(x-r*.58,y-r*.72,x+r*.58,y-r*.72,x+r*1.05,y); g.bezierCurveTo(x+r*.58,y+r*.72,x-r*.58,y+r*.72,x-r*1.05,y); g.closePath(); g.stroke();
    }
    private void cross(GraphicsContext g,double x,double y,double r,CharmVisualTheme t){
        g.setFill(gold(t));g.fillRoundRect(x-r*.22,y-r*1.0,r*.44,r*1.68,r*.12,r*.12);g.fillRoundRect(x-r*.72,y-r*.28,r*1.44,r*.45,r*.13,r*.13);
        g.setFill(new LinearGradient(0,0,1,1,true,CycleMethod.NO_CYCLE,new Stop(0,Color.web("#FFF7D0")),new Stop(.42,t.accent()),new Stop(1,Color.web("#8B5D19"))));
        g.fillRoundRect(x-r*.13,y-r*.82,r*.26,r*1.48,r*.08,r*.08);g.fillRoundRect(x-r*.57,y-r*.18,r*1.14,r*.25,r*.08,r*.08);
        g.setStroke(Color.rgb(255,247,207,.55));g.setLineWidth(1);g.strokeRoundRect(x-r*.22,y-r*1.0,r*.44,r*1.68,r*.12,r*.12);
    }
    private void initialS(GraphicsContext g,double x,double y,double r,CharmVisualTheme t){
        g.setStroke(gold(t));g.setLineWidth(r*.23);g.setLineCap(javafx.scene.shape.StrokeLineCap.ROUND);g.setLineJoin(javafx.scene.shape.StrokeLineJoin.ROUND);
        g.beginPath();g.moveTo(x+r*.42,y-r*.70);g.bezierCurveTo(x-r*.58,y-r*.98,x-r*.65,y-r*.22,x+r*.20,y-r*.12);g.bezierCurveTo(x+r*.88,y-r*.02,x+r*.58,y+r*.82,x-r*.40,y+r*.64);g.stroke();
        g.setStroke(Color.rgb(255,245,198,.62));g.setLineWidth(r*.045);g.beginPath();g.moveTo(x+r*.40,y-r*.70);g.bezierCurveTo(x-r*.42,y-r*.90,x-r*.50,y-r*.25,x+r*.18,y-r*.10);g.bezierCurveTo(x+r*.70,y+r*.02,x+r*.42,y+r*.65,x-r*.35,y+r*.58);g.stroke();
    }
    private void hamsa(GraphicsContext g,double x,double y,double r,CharmVisualTheme t){
        g.setFill(gold(t));g.fillOval(x-r*.47,y-r*.76,r*.94,r*1.52);g.fillRoundRect(x-r*.72,y-r*.25,r*.40,r*.82,r*.18,r*.18);g.fillRoundRect(x+r*.32,y-r*.25,r*.40,r*.82,r*.18,r*.18);
        for(int i=-2;i<=2;i++){double px=x+i*r*.19;double top=y-r*(.70-Math.abs(i)*.05);g.fillRoundRect(px-r*.075,top,r*.15,r*.43,r*.07,r*.07);}
        g.setFill(Color.web("#0B314B"));g.fillOval(x-r*.19,y-r*.12,r*.38,r*.38);g.setFill(Color.web("#43C7EF"));g.fillOval(x-r*.11,y-r*.04,r*.22,r*.22);g.setFill(Color.web("#07131E"));g.fillOval(x-r*.055,y+r*.01,r*.11,r*.11);
    }
    private void clover(GraphicsContext g,double x,double y,double r,CharmVisualTheme t){
        g.setFill(new RadialGradient(0,.1,x-r*.2,y-r*.25,r,false,CycleMethod.NO_CYCLE,new Stop(0,Color.web("#D5FFE6")),new Stop(.3,Color.web("#51D08B")),new Stop(.78,Color.web("#117A46")),new Stop(1,Color.web("#06321E"))));
        double q=r*.48;g.fillOval(x-q*1.25,y-r*.62,q*1.7,q*1.7);g.fillOval(x-q*.45,y-r*.62,q*1.7,q*1.7);g.fillOval(x-q*1.25,y-r*.02,q*1.7,q*1.7);g.fillOval(x-q*.45,y-r*.02,q*1.7,q*1.7);
        g.setFill(gold(t));g.fillRoundRect(x-r*.07,y+r*.35,r*.14,r*.65,r*.06,r*.06);
    }
    private void crown(GraphicsContext g,double x,double y,double r,CharmVisualTheme t){
        g.setFill(gold(t));double[] xs={x-r*.75,x-r*.50,x-r*.18,x,x+r*.18,x+r*.50,x+r*.75,x+r*.55,x-r*.55};double[] ys={y+r*.45,y-r*.55,y-r*.10,y-r*.72,y-r*.10,y-r*.55,y+r*.45,y+r*.72,y+r*.72};g.fillPolygon(xs,ys,xs.length);g.setFill(Color.web("#DCEBFF"));for(int i=-1;i<=1;i++)g.fillOval(x+i*r*.22-2.5,y-r*.22,5,5);g.setStroke(Color.rgb(255,246,200,.6));g.setLineWidth(1);g.strokePolygon(xs,ys,xs.length);
    }
    private void infinity(GraphicsContext g,double x,double y,double r,CharmVisualTheme t){
        g.setStroke(silver(t));g.setLineWidth(r*.22);g.setLineCap(javafx.scene.shape.StrokeLineCap.ROUND);g.beginPath();g.moveTo(x-r*.68,y);g.bezierCurveTo(x-r*.35,y-r*.70,x-r*.02,y-r*.70,x+r*.03,y);g.bezierCurveTo(x+r*.08,y+r*.70,x+r*.40,y+r*.70,x+r*.70,y);g.bezierCurveTo(x+r*.40,y-r*.70,x+r*.08,y-r*.70,x-r*.03,y);g.bezierCurveTo(x-r*.08,y+r*.70,x-r*.40,y+r*.70,x-r*.70,y);g.stroke();
    }
    private void compass(GraphicsContext g,double x,double y,double r,CharmVisualTheme t){g.setFill(gold(t));g.fillOval(x-r*.85,y-r*.85,r*1.7,r*1.7);g.setFill(Color.web("#102B40"));g.fillOval(x-r*.70,y-r*.70,r*1.4,r*1.4);g.setFill(Color.web("#DCEBFF"));double[] xs={x,x+r*.18,x,x-r*.18},ys={y-r*.55,y,x+r*.55,y};g.fillPolygon(xs,ys,4);g.setFill(Color.web("#D9A84B"));g.fillPolygon(new double[]{x,x+r*.18,x,x-r*.18},new double[]{y+r*.55,y,x-r*.55,y},4);g.setFill(gold(t));g.fillOval(x-4,y-4,8,8);
    }
    private void lotus(GraphicsContext g,double x,double y,double r,CharmVisualTheme t){g.setFill(new LinearGradient(0,0,0,1,true,CycleMethod.NO_CYCLE,new Stop(0,Color.web("#FFE3F3")),new Stop(.5,t.accent()),new Stop(1,Color.web("#6A2459"))));for(int i=-2;i<=2;i++){double px=x+i*r*.20;double h=r*(.65+(.5-Math.abs(i)*.12));g.fillOval(px-r*.18,y-h*.55,r*.36,h);}g.setFill(gold(t));g.fillOval(x-r*.10,y+r*.24,r*.20,r*.22);
    }
    private void feather(GraphicsContext g,double x,double y,double r,CharmVisualTheme t){g.save();g.translate(x,y);g.rotate(-25);g.setFill(silver(t));g.fillOval(-r*.27,-r,r*.54,r*1.8);g.setStroke(Color.rgb(255,255,255,.55));g.setLineWidth(1);g.strokeOval(-r*.27,-r,r*.54,r*1.8);g.setStroke(gold(t));g.setLineWidth(2);g.strokeLine(0,r*.80,0,-r*.72);for(int i=0;i<6;i++){double yy=-r*.58+i*r*.20;g.strokeLine(0,yy,-r*.20,yy-r*.10);g.strokeLine(0,yy+r*.05,r*.20,yy-r*.10);}g.restore();}
    private void heart(GraphicsContext g,double x,double y,double r,CharmVisualTheme t){g.setFill(new RadialGradient(0,.1,x-r*.2,y-r*.25,r,false,CycleMethod.NO_CYCLE,new Stop(0,Color.web("#FFD3DA")),new Stop(.4,t.accent()),new Stop(1,Color.web("#8A2438"))));g.beginPath();g.moveTo(x,y+r*.82);g.bezierCurveTo(x-r*1.05,y+r*.18,x-r*.72,y-r*.70,x-r*.20,y-r*.42);g.bezierCurveTo(x,y-r*.28,x,y-r*.28,x,y-r*.12);g.bezierCurveTo(x,y-r*.28,x,y-r*.28,x+r*.20,y-r*.42);g.bezierCurveTo(x+r*.72,y-r*.70,x+r*1.05,y+r*.18,x,y+r*.82);g.closePath();g.fill();g.setStroke(Color.rgb(255,220,225,.55));g.setLineWidth(1);g.stroke();}
    private void lightning(GraphicsContext g,double x,double y,double r,CharmVisualTheme t){g.setFill(gold(t));double[] xs={x+r*.08,y*0+x+r*.05,x-r*.55,x-r*.02,x-r*.10,x+r*.58};double[] ys={y-r*.95,y-r*.30,y+r*.02,y+r*.02,y+r*.88,y-r*.02};g.fillPolygon(xs,ys,6);g.setFill(Color.web("#FFF3A7"));g.fillPolygon(new double[]{x+r*.02,x-r*.28,x-r*.03,x+r*.32},new double[]{y-r*.62,y-r*.02,y-r*.02,y-r*.02},4);}
    private void sun(GraphicsContext g,double x,double y,double r,CharmVisualTheme t){g.setStroke(gold(t));g.setLineWidth(3);for(int i=0;i<12;i++){double a=i*Math.PI/6;g.strokeLine(x+Math.cos(a)*r*.78,y+Math.sin(a)*r*.78,x+Math.cos(a)*r*1.02,y+Math.sin(a)*r*1.02);}g.setFill(gold(t));g.fillOval(x-r*.62,y-r*.62,r*1.24,r*1.24);g.setFill(Color.web("#FFF2B4"));g.fillOval(x-r*.38,y-r*.38,r*.76,r*.76);g.setFill(Color.web("#D79B2E"));g.fillOval(x-r*.09,y-r*.09,r*.18,r*.18);}
    private void maneki(GraphicsContext g,double x,double y,double r,CharmVisualTheme t){g.setFill(new LinearGradient(0,0,1,1,true,CycleMethod.NO_CYCLE,new Stop(0,Color.web("#FFF8E1")),new Stop(.5,Color.web("#EFCB75")),new Stop(1,Color.web("#9E651F"))));g.fillOval(x-r*.55,y-r*.55,r*1.1,r*1.38);g.setFill(gold(t));g.fillPolygon(new double[]{x-r*.55,x-r*.40,x-r*.08},new double[]{y-r*.38,y-r*.98,y-r*.65},3);g.fillPolygon(new double[]{x+r*.55,x+r*.40,x+r*.08},new double[]{y-r*.38,y-r*.98,y-r*.65},3);g.setFill(Color.web("#25242A"));g.fillOval(x-r*.25,y-r*.12,r*.09,r*.11);g.fillOval(x+r*.16,y-r*.12,r*.09,r*.11);g.setFill(Color.web("#C52E3A"));g.fillOval(x-r*.08,y+r*.02,r*.16,r*.10);g.setStroke(gold(t));g.setLineWidth(3);g.strokeLine(x+r*.35,y-r*.10,x+r*.67,y-r*.70);}
    private void ganesha(GraphicsContext g,double x,double y,double r,CharmVisualTheme t){g.setFill(new LinearGradient(0,0,1,1,true,CycleMethod.NO_CYCLE,new Stop(0,Color.web("#FFD4A7")),new Stop(.45,Color.web("#E28B4B")),new Stop(1,Color.web("#8E3B1C"))));g.fillOval(x-r*.52,y-r*.68,r*1.04,r*1.15);g.fillOval(x-r*.76,y-r*.10,r*.45,r*.76);g.fillOval(x+r*.31,y-r*.10,r*.45,r*.76);g.setFill(gold(t));g.fillPolygon(new double[]{x,y-r*.85,x-r*.10,y-r*.45,x+r*.10,y-r*.45},new double[]{y-r*.85,y-r*.45,y-r*.28,y-r*.28,y-r*.28},3);g.setFill(Color.web("#412017"));g.fillOval(x-r*.24,y-r*.22,r*.09,r*.11);g.fillOval(x+r*.15,y-r*.22,r*.09,r*.11);g.setStroke(Color.web("#7A2D17"));g.setLineWidth(2.5);g.strokeArc(x-r*.16,y-r*.04,r*.32,r*.24,180,180,ArcType.OPEN);}
    private void star(GraphicsContext g,double x,double y,double r,CharmVisualTheme t){poly(g,x,y,r,5,gold(t),.42);g.setStroke(Color.rgb(255,245,200,.65));g.setLineWidth(1);}
    private void moon(GraphicsContext g,double x,double y,double r,CharmVisualTheme t){g.setFill(silver(t));g.fillOval(x-r,y-r,r*2,r*2);g.setFill(Color.rgb(8,17,31,.98));g.fillOval(x-r*.43,y-r*1.02,r*2,r*2);g.setStroke(Color.rgb(245,250,255,.5));g.setLineWidth(1);g.strokeOval(x-r,y-r,r*2,r*2);}
    private void planet(GraphicsContext g,double x,double y,double r,CharmVisualTheme t){g.setStroke(Color.rgb(220,210,255,.8));g.setLineWidth(4);g.strokeOval(x-r*1.16,y-r*.40,r*2.32,r*.80);g.setFill(new RadialGradient(0,.1,x-r*.22,y-r*.30,r,false,CycleMethod.NO_CYCLE,new Stop(0,Color.web("#E1D8FF")),new Stop(.45,Color.web("#7657C8")),new Stop(1,Color.web("#2B1A58"))));g.fillOval(x-r*.68,y-r*.68,r*1.36,r*1.36);}
    private void diamond(GraphicsContext g,double x,double y,double r,CharmVisualTheme t){double[] xs={x,x+r*.78,x,x-r*.78},ys={y-r,y,y+r,y};g.setFill(new LinearGradient(0,0,1,1,true,CycleMethod.NO_CYCLE,new Stop(0,Color.WHITE),new Stop(.35,Color.web("#C8F7FF")),new Stop(.68,t.accent()),new Stop(1,Color.web("#3C7D91"))));g.fillPolygon(xs,ys,4);g.setStroke(Color.WHITE);g.setLineWidth(1);g.strokePolygon(xs,ys,4);}
    private void leaf(GraphicsContext g,double x,double y,double r,CharmVisualTheme t){g.save();g.translate(x,y);g.rotate(-30);g.setFill(new LinearGradient(0,0,1,1,true,CycleMethod.NO_CYCLE,new Stop(0,Color.web("#D6FFE3")),new Stop(.45,Color.web("#4DBB77")),new Stop(1,Color.web("#155B35"))));g.fillOval(-r*.52,-r,r*1.04,r*2);g.setStroke(gold(t));g.setLineWidth(1.7);g.strokeLine(-r*.35,r*.65,r*.38,-r*.65);g.restore();}
    private void poly(GraphicsContext g,double x,double y,double r,int points,Paint fill,double inner){double[] xs=new double[points*2],ys=new double[points*2];for(int i=0;i<xs.length;i++){double a=-Math.PI/2+i*Math.PI/points;double rr=i%2==0?r:r*inner;xs[i]=x+Math.cos(a)*rr;ys[i]=y+Math.sin(a)*rr;}g.setFill(fill);g.fillPolygon(xs,ys,xs.length);}

}
