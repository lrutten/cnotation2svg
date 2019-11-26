package org.rooi.cnotation2svg;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jfree.graphics2d.svg.SVGGraphics2D;


public class CNotation2SVG
{
   static public class Config
   {
      static public final boolean withborder = false; 
   }

   static public abstract class SElement
   {
      private int     x;
      private int     y;
      private int     w;
      private int     h;
      private boolean border;

      public SElement()
      {
         border = Config.withborder;
         x = 0;
         y = 0;
         w = 0;
         h = 0;
      }

      public SElement(int xx, int yy)
      {
         border = Config.withborder;
         x = xx;
         y = yy;
         w = 0;
         h = 0;
      }

      public int getX()
      {
         return x;
      }

      public int getY()
      {
         return y;
      }

      public int getW()
      {
         return w;
      }

      public int getH()
      {
         return h;
      }

      public void setX(int xx)
      {
         x = xx;
      }

      public void setY(int yy)
      {
         y = yy;
      }

      public void setW(int ww)
      {
         w = ww;
      }

      public void setH(int hh)
      {
         h = hh;
      }

      public boolean hasBorder()
      {
         return border;
      }

      abstract protected void calcWH();
      abstract public void calcLayout(Graphics g);
      
      public void draw(int dx, int dy, Graphics g)
      {
         if (border)
         {
            g.drawLine(dx + x,     dy + y,     dx + x + w, dy + y);
            g.drawLine(dx + x + w, dy + y,     dx + x + w, dy + y + h);
            g.drawLine(dx + x + w, dy + y + h, dx + x,     dy + y + h);
            g.drawLine(dx + x,     dy + y + h, dx + x,     dy + y);
         }
      }
   }

   static public class Note extends SElement
   {
      private char c;
      private float ascent;
      private float descent;
      private float width;
      
      public Note(char cc)
      {
         c = cc;
      }

      public Note(int xx, int yy, char cc)
      {
         super(xx, yy);
         c = cc;
      }

      @Override
      protected void calcWH()
      {
      }
      
      @Override 
      public void calcLayout(Graphics g)
      {
         Graphics2D g2 = (Graphics2D) g;
         
         Font font = new Font("Serif", Font.PLAIN, 100);
         g2.setFont(font);
         
         FontRenderContext frc = g2.getFontRenderContext();
         String s = String.format("%c", c);
         LineMetrics lm = font.getLineMetrics(s, frc);
         ascent  = lm.getAscent();
         descent = lm.getDescent();
         width   = (float) font.getStringBounds(s, frc).getWidth();
         
         setW((int) width);
         setH((int) (descent + ascent));
         
         System.out.println("ascent  " + ascent);
         System.out.println("descent " + descent);
         System.out.println("width   " + width);
      }

      @Override
      public void draw(int dx, int dy, Graphics g)
      {
         Graphics2D g2 = (Graphics2D) g;
         
         super.draw(dx, dy, g2);
         String s = String.format("%c", c);
         g2.drawString(s, dx + getX(), dy + getY() + ascent);
         if (hasBorder())
         {
            g2.drawLine(dx + getX(), (int)(dy + getY() + ascent), (int)(dx + getX() + width), (int)(dy + getY() + ascent));
         }
      }
   }
   
   static public class Group extends SElement
   {
      protected ArrayList<SElement> list;
      
      public Group(int xx, int yy)
      {
         super(xx, yy);
         list = new ArrayList<SElement>();
      }

      public void add(SElement el)
      {
         list.add(el);
      }

      @Override
      protected void calcWH()
      {
         for (SElement el: list)
         {
            el.calcWH();
         }

         // sum all the widths
         int ww = 0;
         for (SElement el: list)
         {
            ww += el.getW();
         }
         setW(ww);

         int hh = 0;
         for (SElement el: list)
         {
            if (el.getH() > hh)
            {
               hh = el.getH();
            }
         }
         setH(hh);
      }

      @Override 
      public void calcLayout(Graphics g)
      {
         Graphics2D g2 = (Graphics2D) g;
         
         for (SElement el: list)
         {
            el.calcLayout(g);
         }

         // position all the child elements
         int xx = 0;
         int yy = 0;
         for (SElement el: list)
         {
            el.setX(xx);
            xx += el.getW();
         }
         
         calcWH();
      }

      @Override
      public void draw(int dx, int dy, Graphics g)
      {
         Graphics2D g2 = (Graphics2D) g;
         
         super.draw(dx, dy, g2);
         for (SElement el: list)
         {
            el.draw(dx, dy, g2);
         }
      }
   }

   static public class Line extends Group
   {
      static final int hline = 50; // height of the line section

      public Line(int xx, int yy)
      {
         super(xx, yy);
      }

      @Override 
      public void calcLayout(Graphics g)
      {
         Graphics2D g2 = (Graphics2D) g;
         
         for (SElement el: list)
         {
            el.calcLayout(g);
         }
         
         // position all the child elements
         int xx = 0;
         int yy = hline;
         for (SElement el: list)
         {
            el.setX(xx);
            xx += el.getW();
            el.setY(yy);
         }
         
         calcWH();
         setH(hline + getH());
      }

      @Override
      public void draw(int dx, int dy, Graphics g)
      {
         Graphics2D g2 = (Graphics2D) g;
         
         super.draw(dx, dy, g2);
         g2.drawLine(dx + getX(), dy + getY() + hline/2, dx + getX() + getW(), dy + getY() + hline/2);
      }
   }
   
   static public class Score
   {
      private SElement root;
      
      public Score(SElement rt)
      {
         root = rt;
      }
      
      public int getW()
      {
         return root.getW();
      }

      public int getH()
      {
         return root.getH();
      }

      public void draw(Graphics g)
      {
         if (root != null)
         {
            root.calcLayout(g);
            root.draw(0, 0, g);
         }
      }
   }

   public class CNotationPanel extends JPanel
   {
      private Score score;
      
      CNotationPanel(Score sc)
      {
         score = sc;
      }

    	public void paint(Graphics g)
    	{
    		score.draw(g);
    	}
   }

   JFrame frame;
   
   public Score makeDemo()
   {
    	Note no1 = new Note('c');
    	Note no2 = new Note('c');
      Line gr = new Line(0, 0);
      gr.add(no1);
      gr.add(no2);
    	Score sc = new Score(gr);
    	return sc;
   }

   public void swingdemo()
   {
    	JFrame frame= new JFrame("Welcome to CNotation2SVG");

    	Score sc = makeDemo();
    	
    	CNotationPanel panel = new CNotationPanel(sc);
    	frame.getContentPane().add(panel);
    	frame.setSize(800, 600);
    	frame.setVisible(true);
    	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	frame.setResizable(false);		

    	// export to SVG
      SVGGraphics2D g2 = new SVGGraphics2D(600, 400);
      sc.draw(g2);
      
      // only now w and h are set
      SVGGraphics2D gg2 = new SVGGraphics2D(sc.getW(), sc.getH());
      sc.draw(gg2);
      String svgElement = gg2.getSVGElement();
      System.out.println(svgElement);
   }   
   
   public static void svgdemo()
   {
      SVGGraphics2D g2 = new SVGGraphics2D(600, 400);
      g2.setPaint(Color.RED);
      g2.draw(new Rectangle(10, 10, 280, 180));
      String svgElement = g2.getSVGElement();
      System.out.println(svgElement);
   }
   
   
   public static void main(String[] args)
   {
      CNotation2SVG cnotation = new CNotation2SVG();
      
      cnotation.swingdemo();
   }
}
