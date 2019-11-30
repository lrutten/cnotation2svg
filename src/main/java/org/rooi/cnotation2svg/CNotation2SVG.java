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
      static public final boolean withborder = false; 
      static public final int     ptsize     = 40;  // size of font in pt
      static public final float   linestroke = 0.05f * ptsize;
      
      // horizontal line
      static public final int     lineheight = (int)(0.25 * ptsize);
      static public final int     linemarge  = (int)(0.12 * ptsize);
      
      // barline
      static public final int     barlinewidth = (int)(0.25 * ptsize);
   }

   static public class Indent
   {
      public static void indent(int d)
      {
         for (int i=0; i < d; i++)
         {
            System.out.print("   ");
         }
      }
   }

   static public abstract class SElement
   {
      static private int nrctr = 0;
      private int     nr;
      private int     x;
      private int     y;
      private int     w;
      private int     h;
      private boolean hasbaseline;
      private int     baseline;
      private boolean border;
      private boolean lok;  // is layout ok
      private boolean whok; // is wh ok
      
      public SElement()
      {
         nr     = nrctr++;
         border = Config.withborder;
         lok    = false;
         whok   = false;
         x = 0;
         y = 0;
         w = 0;
         h = 0;
         baseline    = 0;
         hasbaseline = true;
      }

      public SElement(int xx, int yy)
      {
         nr     = nrctr++;
         border = Config.withborder;
         lok    = false;
         whok   = false;
         x = xx;
         y = yy;
         w = 0;
         h = 0;
         baseline = 0;
      }

      public int getNr()
      {
         return nr;
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

      public boolean withBaseline()
      {
         return hasbaseline;
      }

      public int getBaseline()
      {
         return baseline;
      }

      public void setBaseline(int bsl)
      {
         baseline = bsl;
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

      public boolean isWHok()
      {
         return whok;
      }

      public void setWHok(boolean whk)
      {
         whok = whk;
      }

      public void clearLok()
      {
         border = Config.withborder;
         lok    = false;
         whok   = false;
         x = 0;
         y = 0;
         w = 0;
         h = 0;
         baseline = 0;
      }

      abstract protected void calcWH(int d);
      abstract public void calcLayout(int d, Graphics g);
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
      private char  c;
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
         System.out.println("Note #" + getNr() + " " + c + " " + getX() + " " + getY() + " " + getBaseline());
      }

      @Override
      protected void calcWH(int d)
      {
         if (isWHok())
         {
            return;
         }
         setWHok(true);
         
         Indent.indent(d);
         System.out.println("Note.calcWH #" + getNr());
      }
      
      @Override 
      public void calcLayout(int d, Graphics g)
      {
         if (isLok())
         {
            return;
         }
         
         Graphics2D g2 = (Graphics2D) g;

         Indent.indent(d);
         System.out.println("Note.calcLayout() #" + getNr());
         
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
         
         setBaseline((int) ascent);
         
         //System.out.println("ascent  " + ascent);
         //System.out.println("descent " + descent);
         //System.out.println("width   " + width);
         setLok(true);
      }

      @Override
      public void draw(int dx, int dy, Graphics g)
      {
         Graphics2D g2 = (Graphics2D) g;

         Font font = new Font("Serif", Font.PLAIN, Config.ptsize);
         g2.setFont(font);
         
         super.draw(dx, dy, g2);
         String s = String.format("%c", c);
         //System.out.println("Note.draw() " + s + " " + (dx + getX()) + " " + (dy + getY()));
         g2.drawString(s, dx + getX(), dy + getY() + ascent);
         if (hasBorder())
         {
            g2.drawLine(dx + getX(), (int)(dy + getY() + ascent), (int)(dx + getX() + width), (int)(dy + getY() + ascent));
         }
      }
   }
   
   static public class Barline extends SElement
   {
      public Barline()
      {
      }

      @Override
      public void show(int d)
      {
         Indent.indent(d);
         System.out.println("Barline #" + getNr() + getX() + " " + getY() + " " + getBaseline());
      }

      @Override
      public boolean withBaseline()
      {
         return false;
      }

      @Override
      protected void calcWH(int d)
      {
         if (isWHok())
         {
            return;
         }
         setWHok(true);
         
         Indent.indent(d);
         System.out.println("Barline.calcWH #" + getNr());
      }
      
      @Override 
      public void calcLayout(int d, Graphics g)
      {
         if (isLok())
         {
            return;
         }
         
         Graphics2D g2 = (Graphics2D) g;

         Indent.indent(d);
         System.out.println("Barline.calcLayout() #" + getNr());
         
         setW(Config.barlinewidth);
         //setH( ... );
         
         setLok(true);
      }

      @Override
      public void draw(int dx, int dy, Graphics g)
      {
         Graphics2D g2 = (Graphics2D) g;
         
         //System.out.println("Barline.draw() " + (dx + getX()) + " " + (dy + getY()));

         super.draw(dx, dy, g2);

         Stroke bstr = g2.getStroke();
         Stroke str = new BasicStroke(Config.linestroke);
         g2.setStroke(str);
         
         //System.out.println("   line fr " + (dx + getX()) + " " + (dy + getY() + hline/2) );
         //System.out.println("   line to " + (dx + getX() + getW()) + " " + (dy + getY() + hline/2) );
         
         // draw the vertical line
         g2.drawLine(dx + getX() + getW()/2, dy + getY(), 
                     dx + getX() + getW()/2, dy + getY() + getH());
         g2.setStroke(bstr);
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
         System.out.println("Group #" + getNr() + " " + getX() + " " + getY() + " " + getBaseline());
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
      protected void calcWH(int d)
      {
         if (isWHok())
         {
            return;
         }
         setWHok(true);

         Indent.indent(d);
         System.out.println("Group.calcHW() #" + getNr());
         
         for (SElement el: list)
         {
            el.calcWH(d + 1);
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
            if (el.withBaseline())
            {
               if (el.getH() > hh)
               {
                  hh = el.getH();
               }
            }
         }
         
         // hh is now the calculated heigth
         setH(hh);

         // search the highest baseline
         int bsl = 0;
         for (SElement el: list)
         {
            if (el.withBaseline())
            {
               if (el.getBaseline() > bsl)
               {
                  bsl = el.getBaseline();
               }
            }
         }
         
         // bsl is now the calculated baseline
         setBaseline(bsl);
         
         
         // align all child elements at the calculated baseline
         for (SElement el: list)
         {
            if (el.withBaseline())
            {
               if (el.getBaseline() < getBaseline())
               {
                  int dy = getBaseline() - el.getBaseline();
                  Indent.indent(d+1);
                  System.out.println("dy " + dy);
                  Indent.indent(d+1);
                  System.out.println("y oud   " + el.getY());
                  el.setY(el.getY() + dy);
                  Indent.indent(d+1);
                  System.out.println("y nieuw " + el.getY());
               }
            }
            else
            {
               // this element has no baseline
               el.setH(hh);
            }
         }
      }

      @Override 
      public void calcLayout(int d, Graphics g)
      {
         if (isLok())
         {
            return;
         }

         Graphics2D g2 = (Graphics2D) g;
         
         Indent.indent(d);
         System.out.println("Group.calcLayout() #" + getNr());

         for (SElement el: list)
         {
            el.calcLayout(d + 1, g);
         }

         calcWH(d + 1);

         // position all the child elements
         int xx = 0;
         int yy = 0;
         for (SElement el: list)
         {
            Indent.indent(d);
            System.out.println("Group.calcLayout() xx " + xx);
            el.setX(xx);
            xx += el.getW();
         }

         setLok(true);
      }

      @Override
      public void draw(int dx, int dy, Graphics g)
      {
         Graphics2D g2 = (Graphics2D) g;
         
         //System.out.println("Group.draw() " + (dx + getX()) + " " + (dy + getY()));

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
      protected void calcWH(int d)
      {
         if (isWHok())
         {
            return;
         }
         setWHok(true);

         root.calcWH(d + 1);

         setW(root.getW());
         setH(root.getH());
      }

      @Override 
      public void calcLayout(int d, Graphics g)
      {
         if (isLok())
         {
            return;
         }

         Indent.indent(d);
         System.out.println("Container.calcLayout() #"  + getNr());

         root.calcLayout(d + 1, g);
         
         calcWH(d);
         
         setLok(true);
      }

      @Override
      public void draw(int dx, int dy, Graphics g)
      {
         Graphics2D g2 = (Graphics2D) g;
         
         //System.out.println("Container.draw() " + (dx + getX()) + " " + (dy + getY()));

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
         System.out.println("Line #" + getNr() + " " +getX() + " " + getY() + " " + getBaseline());
         root.show(d + 1);
      }

      @Override
      protected void calcWH(int d)
      {
         if (isWHok())
         {
            return;
         }
         setWHok(true);

         Indent.indent(d);
         System.out.println("Line.calcWH() #" + getNr());
         root.calcWH(d + 1);

         setW(root.getW());
         setH(hline + root.getH());
      }

      @Override 
      public void calcLayout(int d, Graphics g)
      {
         if (isLok())
         {
            return;
         }

         Indent.indent(d);
         System.out.println("Line.calcLayout() #"  + getNr());

         root.calcLayout(d + 1, g);
         root.setY(hline);
         setBaseline(hline + root.getBaseline());

         setLok(true);
      }

      @Override
      public void draw(int dx, int dy, Graphics g)
      {
         Graphics2D g2 = (Graphics2D) g;
         
         //System.out.println("Line.draw() " + (dx + getX()) + " " + (dy + getY()));

         super.draw(dx, dy, g2);

         Stroke bstr = g2.getStroke();
         Stroke str = new BasicStroke(Config.linestroke);
         g2.setStroke(str);
         
         //System.out.println("   line fr " + (dx + getX()) + " " + (dy + getY() + hline/2) );
         //System.out.println("   line to " + (dx + getX() + getW()) + " " + (dy + getY() + hline/2) );
         g2.drawLine(dx + getX() + Config.linemarge, dy + getY() + hline/2, 
                     dx + getX() + getW() - Config.linemarge, dy + getY() + hline/2);
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
         System.out.println("Bar #" + getNr() + " " + getX() + " " + getY() + " " + getBaseline() + " " + getW() + "x" + getH());
         root.show(d + 1);
      }
   }

   static public class Voice extends Container
   {
      public Voice()
      {
      }

      @Override 
      public void calcLayout(int d, Graphics g)
      {
         if (isLok())
         {
            return;
         }
         setLok(true);

         Indent.indent(d);
         System.out.println("Voice.calcLayout() #"  + getNr());

         root.calcLayout(d + 1, g);
      }


      @Override
      public void show(int d)
      {
         Indent.indent(d);
         System.out.println("Voice #" + getNr() + " " + getX() + " " + getY() + " " + getBaseline() + " " + getW() + "x" + getH());
         root.show(d + 1);
      }
   }

   static public class Score extends Container
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

      @Override 
      public void calcLayout(int d, Graphics g)
      {
         if (isLok())
         {
            return;
         }
         setLok(true);

         Indent.indent(d);
         System.out.println("Score.calcLayout() #"  + getNr());

         root.calcLayout(d + 1, g);
      }


      synchronized public void draw(Graphics g)
      {
         if (root != null)
         {
            System.out.println("----- Score.calcLayout() ----------");
            //root.calcLayout(0, g);
            calcLayout(0, g);
            System.out.println("----- Score.calcLayout() end ----------");
            root.draw(0, 0, g);
            show(0);
         }
      }
   }

   
//  ----------------- Swing -------------------   
   
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
   

// ----------------- parse -----------------------   
   
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
                     parse_bar2(co, text);
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

      public SElement parse_voice2(Container co, ParseText text) throws ParseException
      {
         System.out.println("Parse.parse_voice2() " + text);
         if (!text.empty())
         {
            if (text.get() == '|')
            {
               Barline bline = new Barline();
               co.add(bline);
               text.next();
               parse_voice2(co, text);
            }
            else
            {
               SElement el = parse_bar(text);
               co.add(el);
               parse_voice2(co, text);
            }
         }
         return co;
      }
      
      public SElement parse_voice(ParseText text) throws ParseException
      {
         System.out.println("Parse.parse_voice() " + text);
         Voice voice = new Voice();
         parse_voice2(voice, text);
         return voice;
      }
      
      public Score parse(String text) throws ParseException
      {
         System.out.println("Parse.parse() " + text);
         return new Score(parse_voice(new ParseText(text)));
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
         //sc.show(0);
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
    	String cccc = "|((cc)(cc))|cccc|";
    	Score sc = makeScore(cccc);
    	
    	CNotationPanel panel = new CNotationPanel(sc);
    	frame.getContentPane().add(panel);
    	frame.setSize(800, 600);
    	frame.setVisible(true);
    	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	frame.setResizable(false);		

    	/*
      // export to SVG
    	Score sc2 = makeScore(cccc);
      SVGGraphics2D g2 = new SVGGraphics2D(600, 400);
    	sc2.clearLok();
      sc2.draw(g2);
      
      // only now w and h are set
      SVGGraphics2D gg2 = new SVGGraphics2D(sc2.getW(), sc2.getH());
    	sc2.clearLok();
      sc2.draw(gg2);
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
       */
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
