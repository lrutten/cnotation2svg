package org.rooi.cnotation2svg;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.BasicStroke;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jfree.graphics2d.svg.SVGGraphics2D;


public class CNotation2SVG
{
   static public class Config
   {
      static public final boolean withborder = true; 
      static public final int     ptsize     = 100;  // size of font in pt
      static public final float   linestroke = 0.05f * ptsize;
      static public final int     lineheight = (int)(0.25 * ptsize);
   }

   static public class Indent
   {
      public static void indent(int d)
      {
         for (int i=0; i < d; i++)
         {
            System.out.print("  ");
         }
      }
   }

   static public abstract class SElement
   {
      private int     x;
      private int     y;
      private int     w;
      private int     h;
      private boolean border;
      private boolean lok;

      public SElement()
      {
         border = Config.withborder;
         lok    = false;
         x = 0;
         y = 0;
         w = 0;
         h = 0;
      }

      public SElement(int xx, int yy)
      {
         border = Config.withborder;
         lok    = false;
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

      public boolean isLok()
      {
         return lok;
      }

      public void setLok(boolean lk)
      {
         lok = lk;
      }

      public void clearLok()
      {
         border = Config.withborder;
         lok    = false;
         x = 0;
         y = 0;
         w = 0;
         h = 0;
      }

      abstract protected void calcWH();
      abstract public void calcLayout(Graphics g);
      abstract public void show(int d);
      
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
      public void show(int d)
      {
         Indent.indent(d);
         System.out.println("Note " + c);
      }

      @Override
      protected void calcWH()
      {
      }
      
      @Override 
      public void calcLayout(Graphics g)
      {
         if (isLok())
         {
            return;
         }
         setLok(true);
         
         Graphics2D g2 = (Graphics2D) g;

         System.out.println("Note.calcLayout()");
         
         Font font = new Font("Serif", Font.PLAIN, Config.ptsize);
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

         Font font = new Font("Serif", Font.PLAIN, Config.ptsize);
         g2.setFont(font);
         
         super.draw(dx, dy, g2);
         String s = String.format("%c", c);
         System.out.println("Note.draw() " + s + " " + (dx + getX()) + " " + (dy + getY()));
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
      
      public Group()
      {
         list = new ArrayList<SElement>();
      }

      public Group(int xx, int yy)
      {
         super(xx, yy);
         list = new ArrayList<SElement>();
      }

      @Override
      public void show(int d)
      {
         Indent.indent(d);
         System.out.println("Group");
         for (SElement el: list)
         {
            el.show(d + 1);
         }
      }

      @Override
      public void clearLok()
      {
         super.clearLok();
         for (SElement el: list)
         {
            el.clearLok();
         }
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
         // and this value as the global width
         int ww = 0;
         for (SElement el: list)
         {
            ww += el.getW();
         }
         setW(ww);

         // search the highest child element and
         // take it's height as the global height
         int hh = 0;
         for (SElement el: list)
         {
            if (el.getH() > hh)
            {
               hh = el.getH();
            }
         }
         setH(hh);
         
         // align all child elements at the lower boundary
         for (SElement el: list)
         {
            if (el.getH() < hh)
            {
               int dy = hh - el.getH();
               System.out.println("dy " + dy);
               System.out.println("y oud   " + el.getY());
               el.setY(el.getY() + dy);
               System.out.println("y nieuw " + el.getY());
            }
         }
      }

      @Override 
      public void calcLayout(Graphics g)
      {
         if (isLok())
         {
            return;
         }
         setLok(true);

         Graphics2D g2 = (Graphics2D) g;
         
         System.out.println("Group.calcLayout()");

         for (SElement el: list)
         {
            el.calcLayout(g);
         }

         calcWH();

         // position all the child elements
         int xx = 0;
         int yy = 0;
         for (SElement el: list)
         {
            System.out.println("Group.calcLayout() xx " + xx);
            el.setX(xx);
            xx += el.getW();
         }
      }

      @Override
      public void draw(int dx, int dy, Graphics g)
      {
         Graphics2D g2 = (Graphics2D) g;
         
         System.out.println("Group.draw() " + (dx + getX()) + " " + (dy + getY()));

         super.draw(dx, dy, g2);
         for (SElement el: list)
         {
            el.draw(dx + getX(), dy + getY(), g2);
         }
      }
   }
   
   static public abstract class Container extends SElement
   {
      protected Group    root;

      public Container()
      {
         root = new Group();
      }

      public Container(int xx, int yy)
      {
         super(xx, yy);
         
         root = new Group();
      }

      @Override
      public void clearLok()
      {
         super.clearLok();
         root.clearLok();
      }

      public void add(SElement el)
      {
         root.add(el);
      }

      @Override
      protected void calcWH()
      {
         root.calcWH();

         setW(root.getW());
         setH(root.getH());
      }

      @Override 
      public void calcLayout(Graphics g)
      {
         if (isLok())
         {
            return;
         }
         setLok(true);

         root.calcLayout(g);
      }

      @Override
      public void draw(int dx, int dy, Graphics g)
      {
         Graphics2D g2 = (Graphics2D) g;
         
         System.out.println("Container.draw() " + (dx + getX()) + " " + (dy + getY()));

         super.draw(dx, dy, g2);
         root.draw(dx + getX(), dy + getY(), g);
      }
   }
   
   static public class Line extends Container
   {
      static final int hline = Config.lineheight; // height of the line section

      public Line()
      {
      }

      public Line(int xx, int yy)
      {
         super(xx, yy);
      }

      @Override
      public void show(int d)
      {
         Indent.indent(d);
         System.out.println("Line");
         root.show(d + 1);
      }

      @Override
      protected void calcWH()
      {
         root.calcWH();

         setW(root.getW());
         setH(hline + root.getH());
      }

      @Override 
      public void calcLayout(Graphics g)
      {
         if (isLok())
         {
            return;
         }
         setLok(true);

         System.out.println("Line.calcLayout()");

         root.calcLayout(g);
         root.setY(hline);
      }

      @Override
      public void draw(int dx, int dy, Graphics g)
      {
         Graphics2D g2 = (Graphics2D) g;
         
         System.out.println("Line.draw() " + (dx + getX()) + " " + (dy + getY()));

         super.draw(dx, dy, g2);

         Stroke bstr = g2.getStroke();
         Stroke str = new BasicStroke(Config.linestroke);
         g2.setStroke(str);
         
         System.out.println("   line fr " + (dx + getX()) + " " + (dy + getY() + hline/2) );
         System.out.println("   line to " + (dx + getX() + getW()) + " " + (dy + getY() + hline/2) );
         g2.drawLine(dx + getX(), dy + getY() + hline/2, dx + getX() + getW(), dy + getY() + hline/2);
         g2.setStroke(bstr);
         
         root.draw(dx + getX(), dy + getY(), g);
      }
   }
   
   static public class Bar extends Container
   {
      public Bar()
      {
      }

      @Override
      public void show(int d)
      {
         Indent.indent(d);
         System.out.println("Bar");
         root.show(d + 1);
      }
   }

   static public class Score
   {
      private SElement root;
      
      public Score(SElement rt)
      {
         root = rt;
      }
      
      public void show(int d)
      {
         Indent.indent(d);
         System.out.println("Root");
         root.show(d + 1);
      }

      public int getW()
      {
         return root.getW();
      }

      public int getH()
      {
         return root.getH();
      }

      public void clearLok()
      {
         if (root != null)
         {
            root.clearLok();
         }
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
   
   
   static public class ParseException extends Exception
   {
      private String text;
      
      public ParseException()
      {
         text = "";
      }

      public ParseException(String te)
      {
         text = te;
      }
      
      public String toString()
      {
         return "ParseException " + text;
      }
   }
   
   static public class ParseText
   {
      private String text;
      
      public ParseText(String te)
      {
         text = te;
      }
      
      public boolean empty()
      {
         return text.length() == 0;
      }

      public char get() throws ParseException
      {
         if (text.length() > 0)
         {
            return text.charAt(0);
         }
         else
         {
            throw new ParseException("at get");
         }
      }
      
      public void next() throws ParseException
      {
         if (text.length() > 0)
         {
            text = text.substring(1);
         }
         else
         {
            throw new ParseException("at next");
         }
      }
      
      public String toString()
      {
         return text;
      }
   }
   
   static public class Parser
   {
      public SElement parse_bar2(Container co, ParseText text) throws ParseException
      {
         System.out.println("Parse.parse_bar2() " + text);
         if (!text.empty())
         {
            if (text.get() == '(')
            {
               Line line = new Line();
               co.add(line);
               text.next();
               parse_bar2(line, text);
               if (!text.empty())
               {
                  if (text.get() == ')')
                  {
                     text.next();
                  }
               }
               else
               {
                  throw new ParseException("at ) expected");
               }
            }
            else
            if (text.get() == 'c')
            {
               while (!text.empty() && text.get() == 'c')
               {
                  Note nt = new Note(text.get());
                  co.add(nt);
                  text.next();
                  parse_bar2(co, text);
               }
            }
         }
         else
         {
            //throw new ParseException();
         }
         return co;
      }

      public SElement parse_bar(ParseText text) throws ParseException
      {
         System.out.println("Parse.parse_bar() " + text);
         Bar bar = new Bar();
         parse_bar2(bar, text);
         return bar;
      }

      public Score parse(String text) throws ParseException
      {
         System.out.println("Parse.parse() " + text);
         return new Score(parse_bar(new ParseText(text)));
      }
   }

   JFrame frame;
   
   public Score makeDemo()
   {
      Group gr = new Group();
      gr.add(new Note('c'));

      Line li = new Line();
      li.add(new Note('a'));
      li.add(new Note('b'));
      gr.add(li);

      gr.add(new Note('d'));

      Line li2 = new Line();
      li2.add(new Note('e'));
      li2.add(new Note('f'));
      gr.add(li2);

    	Score sc = new Score(gr);
    	return sc;
   }

   public Score makeScore(String te)
   {
      Parser parser = new Parser();
      try
      {
         Score sc = parser.parse(te);
         System.out.println("parse result " + sc);
         sc.show(0);
         return sc;
      }
      catch (ParseException e)
      {
         System.out.println("exception " + e);
         return null;
      }
   }

   public void swingdemo()
   {
    	JFrame frame= new JFrame("Welcome to CNotation2SVG");

    	//Score sc = makeDemo();
    	Score sc = makeScore("c((c)c)c");
    	
    	CNotationPanel panel = new CNotationPanel(sc);
    	frame.getContentPane().add(panel);
    	frame.setSize(800, 600);
    	frame.setVisible(true);
    	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	frame.setResizable(false);		

       // export to SVG
      SVGGraphics2D g2 = new SVGGraphics2D(600, 400);
    	sc.clearLok();
      sc.draw(g2);
      
      // only now w and h are set
      SVGGraphics2D gg2 = new SVGGraphics2D(sc.getW(), sc.getH());
    	sc.clearLok();
      sc.draw(gg2);
      String svgElement = gg2.getSVGElement();
      //System.out.println(svgElement);

      try
      {
         BufferedWriter writer = new BufferedWriter(new FileWriter("test.svg"));
         writer.write(svgElement);
         writer.close();
      }
      catch (IOException e)
      {
      }
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
