
import interfaces.*;
import utils.*;
import javax.swing.JFrame;
import java.awt.Graphics;
import java.awt.Color;
import java.util.List;

public class Viewer extends JFrame {
    private RoadMap m;
    private List<Car> c;
    private List<TrafficLight> l;
    private int init = 0;

    private String[] intensity;
    
    private static final int u = 10;
    private static final int w = 3; // we require 2*w < u
    private static final int n = 60;
    

    private static final Color backgroundColor = new Color(0x7e7e7e);
    private static final Color roadColor = new Color(0x212323);
    private static final Color intersectBackground = roadColor;
    private static final Color error = Color.magenta;
    private static final int darkish = 0x3f;
    
    public Viewer() {
        super();
        this.setSize(u*n, u*n);
        this.setVisible(true);
    }

    public void view(RoadMap m, List<Car> c, List<TrafficLight> l, String[] in) {
        this.m = m;
        this.c = c;
        this.l = l;
        this.intensity = in; 
        if (init == 0) {
            init = 1;
        }
        this.repaint(0);
    }
    
    private void stillLearning(Graphics g) {
        g.drawString("Still learning...", u*n/3, u*n/3);
    }
    
    private void firstPicture(Graphics g) {
        g.drawString("Learned! Enjoy the show!", u*n/3, u*n/3);
        g.setColor(backgroundColor);
        g.fillRect(0, 0, u*n, u*n);
    }

    private void displayMap(Graphics g) {
        g.setColor(roadColor);
   
        g.drawString("Queens Road/Minsk Sq Circle ", 0, 50); 
        g.drawString("("+intensity[0]+")", 0, 65);   
        g.drawString("Cubbon Road - Central St ", 230, 50); 
        g.drawString("("+intensity[1]+")", 230, 65);   
        g.drawString("Cubbon Road ", 480, 165);   
        g.drawString("("+intensity[2]+")", 480, 180);   
        g.drawString("MG Road ", 490, 365);
        g.drawString("("+intensity[3]+")", 490, 380);   
        g.drawString("St Marks Road ", 220, 575); 
        g.drawString("("+intensity[4]+")", 220, 590);   
        g.drawString("Kasturba Road-Queens Road ", 420, 575);  
        g.drawString("("+intensity[5]+")", 420, 590);   
        g.drawString("Kasturba Road - MG Road ", 0, 430);  
        g.drawString("("+intensity[6]+")", 0, 445);   
        g.drawString("Cubbon Road/Minsk Sq Circle ", 0, 235);        
        g.drawString("("+intensity[7]+")", 0, 250);   

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; ++j) {
                Coords k = new Coords(i, j);
                if (m.roadAt(k)) {
                    g.fillRect(u*i, u*j, u, u);
                }
            }
        }

        g.setColor(intersectBackground);
        for (TrafficLight i : l) {
            int x = i.getCoords().getX(), y = i.getCoords().getY();
            g.fillRect(u*x, u*y, u, u);
        }
        
        for (Car i : c) {
            int x = i.getCoords().getX(), y = i.getCoords().getY();
            int dx = i.getDirection().getXSpeed();
            int dy = i.getDirection().getYSpeed();
            g.setColor(
                dy == 0 ? Color.green :
                dx == 0 ? new Color(0x1793D1) :
                    error
            );
            int p = dx == 0 ? w : 1, q = dy == 0 ? w : 1;
            g.fillRect(u*x+p, u*y+q, u-2*p, u-2*q);
        }

        for (TrafficLight i : l) {
            int x = i.getCoords().getX(), y = i.getCoords().getY();
            g.setColor(
                !i.horizontalGreen() && i.getDelay() > 0 ? Color.orange:
                !i.horizontalGreen() ? Color.green :
                    Color.red
            );
            g.fillRect(u*x+w, u*y, u-2*w, w);
            g.fillRect(u*x+w, u*(y+1)-w, u-2*w, w);
            g.setColor(
                i.horizontalGreen() && i.getDelay() > 0 ? Color.orange:
                i.horizontalGreen() ? Color.green :
                    Color.red
            );
            g.fillRect(u*x, u*y+w, w, u-2*w);
            g.fillRect(u*(x+1)-w, u*y+w, w, u-2*w);
        }
    }

    public void paint(Graphics g) {
        if (init == 0) {
            stillLearning(g);
        } else if (init == 1) {
            firstPicture(g);
            init = 2;
        } else if (init == 2) {
            displayMap(g);
        } else {
            g.setColor(error);
            g.fillRect(0, 0, u*n, u*n);
        }
    }
}
