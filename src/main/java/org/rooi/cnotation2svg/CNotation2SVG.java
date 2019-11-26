package org.rooi.cnotation2svg;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Graphics;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jfree.graphics2d.svg.SVGGraphics2D;


public class CNotation2SVG
{
   static public abstract class SElement
   {
      abstract public void draw(Graphics g);
   }

   static public class Note extends SElement
   {
      @Override
      public void draw(Graphics g)
      {
         g.drawString("c", 10, 30);
      }
   }
   
   static public class Score
   {
      private SElement root;
      
      public Score(SElement rt)
      {
         root = rt;
      }
      
      public void draw(Graphics g)
      {
         if (root != null)
         {
            root.draw(g);
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
    		g.drawLine(10, 10, 200, 300);
    		score.draw(g);
    	}
   }

   JFrame frame;
   
   public void swingdemo()
   {
    	JFrame frame= new JFrame("Welcome to CNotation2SVG");
    	Score sc = new Score(new Note());
    	CNotationPanel panel = new CNotationPanel(sc);
    	frame.getContentPane().add(panel);
    	frame.setSize(600, 400);
    	frame.setVisible(true);
    	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	frame.setResizable(false);		
   }   
   
   public static void svgdemo()
   {
      SVGGraphics2D g2 = new SVGGraphics2D(300, 200);
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
