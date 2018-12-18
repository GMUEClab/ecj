/*
  Copyright 2013 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package ec.app.mona; 

import java.awt.*;
import java.awt.image.*;
import javax.imageio.*;
import javax.swing.*;
import java.io.*;

/** Picture contains two images: an ORIGINAL image which is loaded from a file, and
    writable IMAGE, which you scribble on and try to make as similar to the ORIGINAL.
*/

public class Picture implements Cloneable, Serializable
    {
    public BufferedImage original;
    public BufferedImage image;
    public Graphics graphics;

    int[] xpoints = new int[0];
    int[] ypoints = new int[0];
        
    public Picture(final boolean headless)
        {
        if (!headless)
            f = new JFrame();
        }
    // This allows genes from 0...1 to go to -0.025 ... +1.025.
    // which in turn makes it easy for polygons to have points off-screen
    double extend(double value)
        {
        return (value * 1.05) - 0.025;
        }

    // this is small enough to be inlined
    int discretize(double value, int max)
        {
        // This weird bit of magic uniformly spreads doubles over the 0...max space properly
        int v = (int)(value * (max + 1));
        if (v > max) v = max;
        return v;
        }

    public void disposeGraphics()
        {
        if (graphics!=null) graphics.dispose();
        graphics = null;
        }

    /** Adds a polygon with the given colors. ALL double values passed in must be 0.0 ... 1.0.
        The values are taken starting at vals[offset].  The first four values are colors and alpha.
        The remaining values are the x and y values of the polygon vertices. 
        You must call graphics.dispose() after you're done with all your polygon-drawing.  */
    public void addPolygon(double[] vals, int offset, int numVertices)
        {

        // RGB
        double c1 = (vals[offset]);
        double c2 = (vals[offset+1]);
        double c3 = (vals[offset+2]);
        double c4 = (vals[offset+3]);
        int r = discretize(c1, 255);
        int b = discretize(c2, 255);
        int g = discretize(c3, 255);
        int a = discretize(c4, 255);
        Color color = new Color(r,b,g,a);

        /*
        // HSB (or HSV)
        double c1 = (vals[offset]);
        double c2 = (vals[offset+1]);
        double c3 = (vals[offset+2]);
        double c4 = (vals[offset+3]);
        int r = discretize(c1, 255);
        int b = discretize(c2, 255);
        int g = discretize(c3, 255);
        int a = discretize(c4, 255);
        int rgb = Color.HSBtoRGB((float)c1, (float)c2, (float)c3);
        Color color = new Color((rgb) & 0xFF, (rgb >> 8) & 0xFF, (rgb >> 16) & 0xFF, a);
        */
        
        if (graphics == null) graphics =  image.getGraphics();

        graphics.setColor(color);

        if (xpoints.length != numVertices)
            {
            xpoints = new int[numVertices];
            ypoints = new int[numVertices];
            }

        int[] xpoints = this.xpoints;
        int[] ypoints = this.ypoints;
        int width = image.getWidth(null);
        int height = image.getHeight(null);
        for(int i=0;i<numVertices;i++)
            {
            xpoints[i] = discretize(extend(vals[offset+i*2 + 4]), width-1);
            ypoints[i] = discretize(extend(vals[offset+i*2 + 5]), height-1);
            }
        graphics.fillPolygon(xpoints, ypoints, numVertices);
        }

    /** Erases the image. */
    public void clear()
        {
        // this is faster than drawing a big white rect
        int width = image.getWidth(null);
        int height = image.getHeight(null);
        int[] data = new int[width * height];
        int len = data.length;
        for(int i = 0; i < len; i++)
            // you could do opaque white also:        0xFFFFFFFF; 
            data[i] = 0xFF000000;  // totally opaque, but black
        image.getRaster().setDataElements(0,0,width,height, data);  // clears it out
        }

    /** The maximum possible error between the two images.  Will be a value >= 0.
        By the way, the min error -- what you're shooting for -- is 0 */
    double maxError()
        {
        // we disregard alpha
        // width x height pixels, each with 3 color channels (rgb), each with an error of up to 255
        return Math.sqrt(image.getWidth(null) * image.getHeight(null) * (255.0*255.0) * 3);
        }

    /** Computes the sum squared error between the image and the original.  This is defined as the sum, for all pixels,
        and for all three colors in the pixel, of the squared difference between the images with regard
        to that color on that pixel.  Error goes from 0 to 1.*/
    public double error()
        {
        int width = image.getWidth(null);
        int height = image.getHeight(null);
        int[] originalData = (int[])(original.getRaster().getDataElements(0,0,width,height,null));
        int[] imageData = (int[])(image.getRaster().getDataElements(0,0,width,height,null));
        // since it's *ARGB*, the alpha is in the high byte, we ignore that.
        int len = originalData.length;
        double error = 0;
        for(int i = 0; i < len; i++)  // go through every pixel (which is stored as an int)
            {
            int a = originalData[i];
            int b = imageData[i];

            int error1 = ((a & 0xff) - (b & 0xff));
            int error2 = (((a >> 8) & 0xff) - (((b >> 8) & 0xff)));
            int error3 = (((a >> 16) & 0xff) - (((b >> 16) & 0xff)));

            // do sum squared of color errors
            error += error1*error1 + error2*error2 + error3*error3;
            }
        return Math.sqrt(error) / maxError();
        }

    /** Loads the original and creates a new blank image to scribble on, and a new graphics object. */
    public void load(File file)
        {
        BufferedImage i = null;
        try
            {
            i = ImageIO.read(file);
            }
        catch(Exception e)
            {
            throw new RuntimeException("Cannot load image file " + file + " because of error:\n\n" + e);
            }
        int width = i.getWidth(null);
        int height = i.getHeight(null);
        int type = BufferedImage.TYPE_INT_ARGB;  // is this the fastest choice?
        original = new BufferedImage(width, height, type);
        image = new BufferedImage(width, height, type);
        original.flush();
        image.flush();
        clear();

        // now copy the loaded image into the buffer.  This is ugly but gets the job done.
        // Rasters are a pain so I'm just using graphics2d here.
        Graphics2D g = (Graphics2D)(original.getGraphics());
        g.drawImage(i, 0, 0, null);
        g.dispose();
        }

    static JFrame f;
    static  boolean first = true;
    static JLabel left = new JLabel();
    static JLabel right = new JLabel();

    int count = 0;
        
    /** For debugging only.  */
    public void display(String title)
        {           
        if (f == null)
            return;
        left.setIcon(new ImageIcon(copyImage(original)));
        right.setIcon(new ImageIcon(copyImage(image)));
        if (first)
            {
            first = false;
            f.getContentPane().setLayout(new GridLayout(1,2));
            f.getContentPane().add(left);
            f.getContentPane().add(right);
            f.pack();
            f.setVisible(true);
            }
        f.setTitle(title);
        f.repaint();
        }


    /** Saves the image (not the original) out to a PNG file so you can compare. */
    public void save(File file)
        {
        try
            {
            ImageIO.write(image, "png", file);
            }
        catch(Exception e)
            {
            }
        }

    // for serialization, 'cause (grrr) BufferedImage isn't serializable for some reason
    private void writeObject(java.io.ObjectOutputStream out) throws IOException
        {
        int width = image.getWidth(null);
        int height = image.getHeight(null);
        int type = image.getType();
        out.writeInt(type);
        out.writeInt(width);
        out.writeInt(height);
        out.writeObject(image.getRGB(0,0,width,height,null,0,width));
        out.writeObject(original.getRGB(0,0,width,height,null,0,width));
        }

    // for serialization, 'cause (grrr) BufferedImage isn't serializable for some reason
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
        {
        int type = in.readInt();
        int width = in.readInt();
        int height = in.readInt();
        original = new BufferedImage(width, height, type);
        image = new BufferedImage(width, height, type);
        image.getRGB(0,0,width,height,(int[])(in.readObject()),0,width);
        original.getRGB(0,0,width,height,(int[])(in.readObject()),0,width);
        xpoints = new int[0];
        ypoints = new int[0];
        }

    // for cloneable, 'cause (grrr) BufferedImage isn't cloneable for some reason
    public Object clone()
        {
        Picture p = null;
        try { p = (Picture)(super.clone()); } catch (CloneNotSupportedException e) { } 
        p.original = copyImage(original);
        p.image = copyImage(image);
        p.xpoints = new int[0];
        p.ypoints = new int[0];
        return p;
        }

    public BufferedImage copyImage(BufferedImage image)
        {
        int width = image.getWidth(null);
        int height = image.getHeight(null);
        BufferedImage i = new BufferedImage(width, height,
            image.getType());
        i.setRGB(0,0,width,height,image.getRGB(0,0,width,height,null,0,width),0,width);  // Always assume alpha is opaque
        return i;
        }           
    }
