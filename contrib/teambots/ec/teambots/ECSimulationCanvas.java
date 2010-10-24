/*
  Copyright 1997 by Tucker Balch and Georgia Tech Research Corporation
  Copyright 1998 by Tucker Balch and Carnegie Mellon University
*/


/* ECSimulationCanvas
 * By Tucker Balch
 * Modified by Liviu Panait
 */
 
/**
 * This is a file which does roughly the same thing that Teambots' TBSim.SimulationCanvas
 * does.  It is essentially the SimulationCanvas object, only with special modifications
 * so that it resets all its variables when we start a new evaluation of the robots.
 *
 * <p>The approach we have taken to hooking Teambots up with ECJ is to hack Teambots
 * so that it can be used as a class library accessed by ECJ.  ECSimulationCanvas is
 * our top-level access object.
 * 
 * <P>The original TBSim.SimulationCanvas is Copyright
 * <br>(c)1997 Tucker Balch and Georgia Tech Research Corporation
 * <br>(c)1998 Tucker Balch and Carnegie Mellon University
 *
 * @author Tucker Balch
 * @author Liviu Panait
 * @version $Revision: 1.2 $
 */

package ec.teambots;
import java.io.*;
import java.awt.*;
import java.lang.System;
import java.lang.Class;
import EDU.gatech.cc.is.abstractrobot.*;
import EDU.gatech.cc.is.simulation.*;
import EDU.gatech.cc.is.util.*;
import EDU.cmu.cs.coral.util.*;
import EDU.cmu.cs.coral.simulation.*;



public class ECSimulationCanvas extends Canvas implements Runnable
    {
    private String bufferedInputString; // contains all the input file buffered in a string (faster for multiple reads)

    private Graphics g;
    private Frame parent;
    private     int height, width;
    private boolean preserveSize = false;
    private     Color bgcolor = new Color(0xFFFFFF);
    private     Image bgimage;
    private     Image buffer;
    private     Graphics bufferg;
    private     boolean read_once = false; //indicates if we've read a dsc file
    private     boolean pause = true;
    private     boolean graphics_on = true;
    private SimulatedObject simulated_objects[] = new SimulatedObject[0];
    private ControlSystemS control_systems[];// = new ControlSystemS[0];
    private     double top, bottom, left, right;
    private     double time_compression=1;
    private     long current_time = 0;
    private long sim_time = 0;
    private     long timestep=100;
    private     long timeout=-1;
    public      long seed=-1;
    private     int trials=-1;
    private     Thread run_sim_thread;
    private String descriptionfile;
    private     int     idcounter = 0;
    private     boolean to_draw = false;

    /*make these package scope so TBSim can access for updating menu on startup*/
    boolean     draw_ids = false; //don't draw robot ids
    boolean     draw_icons = false; //don't draw robot icons
    boolean     draw_robot_state = false; //don't draw robot state
    boolean     draw_object_state = false; //don't draw robot state
    boolean     draw_trails = false; //don't draw object trails
    /*end package scope*/
    private double visionNoiseMean;
    private double visionNoiseStddev; //the standard deviation for vision noise
    private long visionNoiseSeed; //the seed value

    private long        startrun = 0;
    private long        frames = 0;

    /**
     * The maximum number of objects in a simulation.
     */
    public      static final int MAX_SIM_OBJS = 1000;

    /**
     * Read the description of the world from a file.
     */
    private     void loadEnvironmentDescription() throws IOException
        {

        // set up the buffered string input
        if( bufferedInputString == null )
            {
            BufferedReader buf;
            bufferedInputString = "";
            try
                {
                buf = new BufferedReader( new FileReader( descriptionfile ) );
                }
            catch( IOException e )
                {
                System.out.println( "Error reading the input file " + descriptionfile );
                throw(e);
                }
            // read till exception
            try
                {
                String temp = "";
                while( temp != null )
                    {
                    temp = buf.readLine();
                    if( temp != null )
                        bufferedInputString += temp + "\n";
                    }
                }
            catch( IOException e )
                {
                }
            }

        StringReader stringReader = new StringReader( bufferedInputString );
        Reader raw_in = new PreProcessor(stringReader);
        StreamTokenizer in = new StreamTokenizer(raw_in);
        String token;
        SimulatedObject[] temp_objs = new SimulatedObject[MAX_SIM_OBJS];
        int temp_objs_count = 0;
        ControlSystemS[] temp_css = new ControlSystemS[MAX_SIM_OBJS];
        int temp_css_count = 0;
        double  x, y, t, r;
        double  x1, y1, x2, y2;
        int     color1, color2;
        int     vc;
        idcounter = 0;
        String  string1, string2;
        TBDictionary bboard = new TBDictionary();
        /*--- assume success. reset later if failure ---*/
        boolean dfl = true; //description_file_loaded;

        /*--- set default bounds before reading ---*/
        top = 5;
        bottom = -5;
        left = -5;
        right = 5;

        /*--- set up tokenizer ---*/
        in.wordChars('A','_'); // let _ be a word character
        in.quoteChar('"');     // " is the quote char

        /*--- tokenize the file ---*/
        token = "beginning of file";
        try
            {
            while (in.nextToken() != StreamTokenizer.TT_EOF)
                {
                if (in.ttype == StreamTokenizer.TT_WORD)
                    {
                    token = in.sval;
                    if (false) System.out.println(token);
                                
                    /*--- check for "dictionary" statements ---*/
                    //FORMAT: dictionary KEY "some string"
                    if (token.equalsIgnoreCase("dictionary")) {
                        String key, obj;
                        if (in.nextToken() ==
                            StreamTokenizer.TT_WORD) {
                            key = in.sval;
                                   
                            } else {
                                token = in.sval;
                                throw new IOException();
                                }

                        in.nextToken();
                        obj = in.sval;
                                
                        bboard.put(key, obj);
                        }
                        
                    /*--- this affects the vision sensor noise ---*/
                    //FORMAT: vision_noise MEAN STDDEV SEED
                    if (token.equalsIgnoreCase("vision_noise")) {
                        //the next token is the value for the mean
                        //and should be a double
                                  
                        if (in.nextToken() == StreamTokenizer.TT_NUMBER) {
                            visionNoiseMean = (double) in.nval;   
                            }else {
                                //we are looking for number, not string
                                token = in.sval;
                                throw new IOException();
                                }

                        //this is the stddev
                        if (in.nextToken() == StreamTokenizer.TT_NUMBER) {
                            visionNoiseStddev = (double) in.nval;
                            } else {
                                token = in.sval;
                                throw new IOException();
                                }
                                  
                        //the next one is a long for the seed value
                        if (in.nextToken() == StreamTokenizer.TT_NUMBER) {
                            visionNoiseSeed = (long) in.nval;
                            } else {
                                //not what we wanted!
                                token = in.sval;
                                throw new IOException();
                                }
                                  
                        }  

                    /*--- it is to turn trails on/off ---*/
                    //FORMAT: view_robot_trails on
                    if (token.equalsIgnoreCase("view_robot_trails")) {
                                   
                        if (in.nextToken()==
                            StreamTokenizer.TT_NUMBER)
                            {
                            token = in.sval; // for error report
                            throw new IOException();
                            }
                        else
                            {
                            if (in.sval.equalsIgnoreCase("on"))
                                draw_trails=true;
                            }
                        }

                    /*--- it is to turn IDs on/off ---*/
                    //FORMAT: view_robot_IDs on
                    if (token.equalsIgnoreCase("view_robot_IDs")) {
                                   
                        if (in.nextToken()==
                            StreamTokenizer.TT_NUMBER)
                            {
                            token = in.sval; // for error report
                            throw new IOException();
                            }
                        else
                            {
                            if (in.sval.equalsIgnoreCase("on"))
                                draw_ids=true;
                            }
                        }
                                
                    /*--- it is to turn robot state on/off ---*/
                    //FORMAT: view_robot_state on
                    if (token.equalsIgnoreCase("view_robot_state")) {
                                   
                        if (in.nextToken()==
                            StreamTokenizer.TT_NUMBER)
                            {
                            token = in.sval; // for error report
                            throw new IOException();
                            }
                        else
                            {
                            if (in.sval.equalsIgnoreCase("on"))
                                draw_robot_state=true;
                            }
                        }


                    /*--- it is to turn objec info IDs on/off ---*/
                    //FORMAT: view_object_into on
                    if (token.equalsIgnoreCase("view_object_info")) {
                                   
                        if (in.nextToken()==
                            StreamTokenizer.TT_NUMBER)
                            {
                            token = in.sval; // for error report
                            throw new IOException();
                            }
                        else
                            {
                            if (in.sval.equalsIgnoreCase("on"))
                                draw_object_state=true;
                            }
                        }
                                
                    /*--- it is to turn icons on/off ---*/
                    //FORMAT: view_icons on
                    if (token.equalsIgnoreCase("view_icons")) {
                                   
                        if (in.nextToken()==
                            StreamTokenizer.TT_NUMBER)
                            {
                            token = in.sval; // for error report
                            throw new IOException();
                            }
                        else
                            {
                            if (in.sval.equalsIgnoreCase("on"))
                                draw_icons=true;
                            }
                        }


                    /*--- it is a background_image statement ---*/
                    //FORMAT: background_image filename
                    if (token.equalsIgnoreCase("background_image"))
                        {
                        in.nextToken(); // get the filename
                        String img_filename = in.sval;
                        System.out.println("loading "
                                           + "background image file "+
                                           img_filename);
                        Toolkit tk = Toolkit.getDefaultToolkit();
                        bgimage = tk.getImage(img_filename);
                        tk.prepareImage(bgimage, -1, -1, this);
                                        
                        }

                    /*--- it is a background statement ---*/
                    //FORMAT: background color
                    if (token.equalsIgnoreCase("background"))
                        {
                        if (in.nextToken()==
                            StreamTokenizer.TT_WORD)
                            {
                            String tmp = in.sval;
                            tmp = tmp.replace('x','0');
                            tmp = tmp.replace('X','0');
                            bgcolor = new Color(
                                Integer.parseInt(tmp,16));
                            }
                        else
                            {
                            bgcolor = new Color((int)in.nval);
                            }
                        }

                    /*--- it is a time statement ---*/
                    //FORMAT: time accel_rate
                    if (token.equalsIgnoreCase("time"))
                        {
                        if (in.nextToken()==
                            StreamTokenizer.TT_NUMBER)
                            {
                            time_compression = in.nval;
                            }
                        else
                            {
                            token = in.sval; // for error report
                            throw new IOException();
                            }
                        }

                    /*--- it is a timeout statement ---*/
                    //FORMAT: timeout time
                    if (token.equalsIgnoreCase("timeout"))
                        {
                        if (in.nextToken()==
                            StreamTokenizer.TT_NUMBER)
                            {
                            timeout = (long) in.nval;
                            }
                        else
                            {
                            token = in.sval; // for error report
                            throw new IOException();
                            }
                        }

                    /*--- it is a seed statement ---*/
                    //FORMAT: seed seed_val
                    if (token.equalsIgnoreCase("seed"))
                        {
                        if (in.nextToken()==
                            StreamTokenizer.TT_NUMBER)
                            {
                            // skip for subsequent trials
                            if (!read_once)
                                seed = (long) in.nval;
                            }
                        else
                            {
                            token = in.sval; // for error report
                            throw new IOException();
                            }
                        }

                    /*--- it is a graphics statement ---*/
                    //FORMAT: graphics on/off
                    if (token.equalsIgnoreCase("graphics"))
                        {
                        if (in.nextToken()==
                            StreamTokenizer.TT_NUMBER)
                            {
                            token = in.sval; // for error report
                            throw new IOException();
                            }
                        else
                            {
                            if (in.sval.equalsIgnoreCase("off"))
                                graphics_on = false;
                                                
                            }
                        }

                    /*--- it is a trials statement ---*/
                    //FORMAT: trials num_trials
                    if (token.equalsIgnoreCase("trials"))
                        {
                        if (in.nextToken()==
                            StreamTokenizer.TT_NUMBER)
                            {
                            if (trials == -1)
                                trials = (int) in.nval;
                            if (trials<0)
                                throw new IOException();
                            }
                        else
                            {
                            token = in.sval; // for error report
                            throw new IOException();
                            }
                        }

                    /*--- it is a maxtimestep statement ---*/
                    //FORMAT: maxtimestep milliseconds
                    //DEPRECATED!
                    if (token.equalsIgnoreCase("maxtimestep"))
                        {
                        if (in.nextToken()==
                            StreamTokenizer.TT_NUMBER)
                            {
                            timestep = (long) in.nval;
                            System.out.println("maxtimestep statement read, treated as timestep");
                            }
                        else
                            {
                            token = in.sval; // for error report
                            throw new IOException();
                            }
                        }

                    /*--- it is a timestep statement ---*/
                    //FORMAT: timestep milliseconds
                    //DEPRECATED!
                    if (token.equalsIgnoreCase("timestep"))
                        {
                        if (in.nextToken()==
                            StreamTokenizer.TT_NUMBER)
                            {
                            timestep = (long) in.nval;
                            }
                        else
                            {
                            token = in.sval; // for error report
                            throw new IOException();
                            }
                        }

                    /*--- it is a bounds statement ---*/
                    //FORMAT: bounds left right bottom top
                    if (token.equalsIgnoreCase("bounds"))
                        {
                        if (in.nextToken()==StreamTokenizer.TT_NUMBER)
                            left = in.nval;
                        else
                            {
                            token = in.sval; // for error report
                            throw new IOException();
                            }
                        if (in.nextToken()==StreamTokenizer.TT_NUMBER)
                            right = in.nval;
                        else
                            {
                            token = in.sval; // for error report
                            throw new IOException();
                            }
                        if (in.nextToken()==StreamTokenizer.TT_NUMBER)
                            bottom = in.nval;
                        else
                            {
                            token = in.sval; // for error report
                            throw new IOException();
                            }
                        if (in.nextToken()==StreamTokenizer.TT_NUMBER)
                            top = in.nval;
                        else
                            {
                            token = in.sval; // for error report
                            throw new IOException();
                            }
                        }

                    /*--- it is a windowsize statement ---*/
                    //FORMAT: windowsize width height
                    if (token.equalsIgnoreCase("windowsize"))
                        {
                        int localWidth = width;
                        int localHeight = height;
                        if (in.nextToken()==StreamTokenizer.TT_NUMBER)
                            localWidth = (int)in.nval;
                        else
                            {
                            token = in.sval; // for error report
                            throw new IOException();
                            }
                        if (in.nextToken()==StreamTokenizer.TT_NUMBER)
                            localHeight = (int)in.nval;
                        else
                            {
                            token = in.sval; // for error report
                            throw new IOException();
                            }
                        if (!preserveSize) {
                            setSize(localWidth, localHeight);
                            reSizeWindow();
                            }
                        }

                    /*--- it is an object statement ---*/
                    //FORMAT object objectclass 
                    //  x y t r color1 color2 visionclass
                    if (token.equalsIgnoreCase("object"))
                        {
                        if (in.nextToken()==StreamTokenizer.TT_WORD)
                            string1 = in.sval;
                        else
                            {
                            token = in.sval; // for error report
                            throw new IOException();
                            }
                        if (in.nextToken()==StreamTokenizer.TT_NUMBER)
                            x = in.nval;
                        else
                            {
                            token = in.sval; // for error report
                            throw new IOException();
                            }
                        if (in.nextToken()==StreamTokenizer.TT_NUMBER)
                            y = in.nval;
                        else
                            {
                            token = in.sval; // for error report
                            throw new IOException();
                            }
                        if (in.nextToken()==StreamTokenizer.TT_NUMBER)
                            t = in.nval;
                        else
                            {
                            token = in.sval; // for error report
                            throw new IOException();
                            }
                        if (in.nextToken()==StreamTokenizer.TT_NUMBER)
                            r = in.nval;
                        else
                            {
                            token = in.sval; // for error report
                            throw new IOException();
                            }
                        if (in.nextToken()==
                            StreamTokenizer.TT_WORD)
                            {
                            String tmp = in.sval;
                            tmp = tmp.replace('x','0');
                            tmp = tmp.replace('X','0');
                            color1 = Integer.parseInt(tmp,16);
                            }
                        else
                            {
                            color1 = (int)in.nval;
                            }
                        if (in.nextToken()==
                            StreamTokenizer.TT_WORD)
                            {
                            String tmp = in.sval;
                            tmp = tmp.replace('x','0');
                            tmp = tmp.replace('X','0');
                            color2 = Integer.parseInt(tmp,16);
                            }
                        else
                            {
                            color2 = (int)in.nval;
                            }

                        if (in.nextToken()==StreamTokenizer.TT_NUMBER)
                            vc = (int)in.nval;
                        else
                            {
                            token = in.sval; // for error report
                            throw new IOException();
                            }
                        /*--- instantiate the obj ---*/
                        token = string1; // in case of error
                        Class rclass = Class.forName(string1);
                        SimulatedObject
                            obj = (SimulatedObject)rclass.newInstance();
                        obj.init(x, y, t, r, new Color(color1),
                                 new Color(color2),vc,
                                 idcounter++,seed++);
                        temp_objs[temp_objs_count++] = obj;
                        }


                    /*--- it is a linearobject statement ---*/
                    //FORMAT linearobject objectclass 
                    //  x1 y1 x2 y2 r color1 color2 visionclass
                    if (token.equalsIgnoreCase("linearobject"))
                        {
                        if (in.nextToken()==StreamTokenizer.TT_WORD)
                            string1 = in.sval;
                        else
                            {
                            token = in.sval; // for error report
                            throw new IOException();
                            }
                        if (in.nextToken()==StreamTokenizer.TT_NUMBER)
                            x1 = in.nval;
                        else
                            {
                            token = in.sval; // for error report
                            throw new IOException();
                            }
                        if (in.nextToken()==StreamTokenizer.TT_NUMBER)
                            y1 = in.nval;
                        else
                            {
                            token = in.sval; // for error report
                            throw new IOException();
                            }
                        if (in.nextToken()==StreamTokenizer.TT_NUMBER)
                            x2 = in.nval;
                        else
                            {
                            token = in.sval; // for error report
                            throw new IOException();
                            }
                        if (in.nextToken()==StreamTokenizer.TT_NUMBER)
                            y2 = in.nval;
                        else
                            {
                            token = in.sval; // for error report
                            throw new IOException();
                            }
                        if (in.nextToken()==StreamTokenizer.TT_NUMBER)
                            r = in.nval;
                        else
                            {
                            token = in.sval; // for error report
                            throw new IOException();
                            }
                        if (in.nextToken()==
                            StreamTokenizer.TT_WORD)
                            {
                            String tmp = in.sval;
                            tmp = tmp.replace('x','0');
                            tmp = tmp.replace('X','0');
                            color1 = Integer.parseInt(tmp,16);
                            }
                        else
                            {
                            color1 = (int)in.nval;
                            }
                        if (in.nextToken()==
                            StreamTokenizer.TT_WORD)
                            {
                            String tmp = in.sval;
                            tmp = tmp.replace('x','0');
                            tmp = tmp.replace('X','0');
                            color2 = Integer.parseInt(tmp,16);
                            }
                        else
                            {
                            color2 = (int)in.nval;
                            }

                        if (in.nextToken()==StreamTokenizer.TT_NUMBER)
                            vc = (int)in.nval;
                        else
                            {
                            token = in.sval; // for error report
                            throw new IOException();
                            }
                        /*--- instantiate the obj ---*/
                        token = string1; // in case of error
                        System.out.println(string1);
                        Class rclass = Class.forName(string1);
                        SimulatedLinearObject
                            obj = (SimulatedLinearObject)rclass.newInstance();
                        obj.init(x1, y1, x2, y2, r, new Color(color1),
                                 new Color(color2),vc,
                                 idcounter++,seed++);
                        temp_objs[temp_objs_count++] = obj;
                        }


                    /*--- it is a robot statement ---*/
                    //FORMAT robot robotclass controlsystemclass 
                    //  x y t color1 color2 visionclass
                    if (token.equalsIgnoreCase("robot"))
                        {
                        if (in.nextToken()==StreamTokenizer.TT_WORD)
                            string1 = in.sval;
                        else
                            {
                            token = in.sval; // for error report
                            throw new IOException();
                            }
                        if (in.nextToken()==StreamTokenizer.TT_WORD)
                            string2 = in.sval;
                        else
                            {
                            token = in.sval; // for error report
                            throw new IOException();
                            }
                        if (in.nextToken()==StreamTokenizer.TT_NUMBER)
                            x = in.nval;
                        else
                            {
                            token = in.sval; // for error report
                            throw new IOException();
                            }
                        if (in.nextToken()==StreamTokenizer.TT_NUMBER)
                            y = in.nval;
                        else
                            {
                            token = in.sval; // for error report
                            throw new IOException();
                            }
                        if (in.nextToken()==StreamTokenizer.TT_NUMBER)
                            t = in.nval;
                        else
                            {
                            token = in.sval; // for error report
                            throw new IOException();
                            }
                        if (in.nextToken()==
                            StreamTokenizer.TT_WORD)
                            {
                            String tmp = in.sval;
                            tmp = tmp.replace('x','0');
                            tmp = tmp.replace('X','0');
                            color1 = Integer.parseInt(tmp,16);
                            }
                        else
                            {
                            color1 = (int)in.nval;
                            }
                        if (in.nextToken()==
                            StreamTokenizer.TT_WORD)
                            {
                            String tmp = in.sval;
                            tmp = tmp.replace('x','0');
                            tmp = tmp.replace('X','0');
                            color2 = Integer.parseInt(tmp,16);
                            }
                        else
                            {
                            color2 = (int)in.nval;
                            }

                        if (in.nextToken()==StreamTokenizer.TT_NUMBER)
                            vc = (int)in.nval;
                        else
                            {
                            token = in.sval; // for error report
                            throw new IOException();
                            }

                        /*--- the robot ---*/
                        token = string1; // in case of error
                        Class rclass = Class.forName(string1);
                        SimulatedObject
                            obj = (SimulatedObject)rclass.newInstance();
                        obj.init(x, y, t, 0, new Color(color1),
                                 new Color(color2),vc,
                                 idcounter++,seed++);
                        temp_objs[temp_objs_count++] = obj;
                                        
                        /*--- set the dictionary ---*/
                        ((Simple)obj).setDictionary(bboard);

                        /*--- the control system ---*/
                        token = string2; // in case of error
                        Class csclass = Class.forName(string2);
                        ControlSystemS css = 
                            (ControlSystemS)
                            csclass.newInstance();
                        css.init((Simple)obj,seed++);
                        //css.Configure();//save for later
                        temp_css[temp_css_count++] = 
                            (ControlSystemS) css;
                        }
                    }
                else
                    {
                    throw new IOException();
                    }
                stringReader.close();
                raw_in.close();
                }

            /*--- catch any exceptions thrown in the parsing ---*/
            }
        catch (IOException e)
            {
            dfl = false;
            simulated_objects = new SimulatedObject[0];
            String msg =
                "bad format"+
                " at line "+ in.lineno() +
                " in " + descriptionfile +
                " near "+ 
                "'"+token+"'";
            Dialog tmp;
            if (graphics_on) tmp = new DialogMessage(parent,
                                                     "TBSim Error",msg);
            else
                System.out.println(msg);
            descriptionfile = null;
            }
        catch (ClassNotFoundException e )
            {
            dfl = false;
            simulated_objects = new SimulatedObject[0];
            String msg =
                "unable to find class "+
                "'"+token+"'" +
                " at line "+ in.lineno() +
                " in " + descriptionfile +".\n"+
                "You may need to check your CLASSPATH.";
            Dialog tmp;
            if (graphics_on) tmp = new DialogMessage(parent,
                                                     "TBSim Error",msg);
            else
                System.out.println(msg);
            descriptionfile = null;
            }
        catch (IllegalAccessException e)
            {
            dfl = false;
            simulated_objects = new SimulatedObject[0];
            String msg = 
                "illegal to access class "+
                "'"+token+"'" +
                " at line "+ in.lineno() +
                " in " + descriptionfile;
            Dialog tmp;
            if (graphics_on) tmp = new DialogMessage(parent,
                                                     "TBSim Error", msg);
            else
                System.out.println(msg);
            descriptionfile = null;
            }
        catch (InstantiationException e)
            {
            dfl = false;
            simulated_objects = new SimulatedObject[0];
            String msg =
                "instantiation error for "+
                "'"+token+"'" +
                " at line "+ in.lineno() +
                " in " + descriptionfile ;
            Dialog tmp;
            if (graphics_on) tmp = new DialogMessage(parent,
                                                     "TBSim Error", msg);
            else
                System.out.println(msg);
            descriptionfile = null;
            }
        catch (ClassCastException e)
            {
            dfl = false;
            simulated_objects = new SimulatedObject[0];
            String msg =
                "class conflict for "+
                "'"+token+"'" +
                " at line "+ in.lineno() +
                " in " + descriptionfile +"."+
                " It could be that the control system was not "+
                " written for the type of robot you " +
                " specified.";
            Dialog tmp;
            if (graphics_on) tmp = new DialogMessage(parent,
                                                     "TBSim Error",msg);
            else
                System.out.println(msg);
            descriptionfile = null;
            }

                

        /*--- set up global arrays of objs and cont systems ---*/
        simulated_objects = new SimulatedObject[temp_objs_count];
        for(int i=0; i<temp_objs_count; i++)
            simulated_objects[i] = temp_objs[i];
        for(int i=0; i<temp_objs_count; i++)
            {   
            // let everyone take a step to update their pointers
            simulated_objects[i].takeStep(0,simulated_objects);
            if (simulated_objects[i] instanceof VisualObjectSensor) 
                {
                //we need to tell it the noise parameters...
                //we do it here so that it doesnt matter where in dsc they
                //declare visionnoise
                
                ((VisualObjectSensor)simulated_objects[i]).setVisionNoise(visionNoiseMean, 
                                                                          visionNoiseStddev,
                                                                          visionNoiseSeed);
                }
            }
        control_systems = new ControlSystemS[temp_css_count];
        for(int i=0; i<temp_css_count; i++)
            {
            control_systems[i] = temp_css[i];
            control_systems[i].configure();
            }

        description_file_loaded = dfl;
        read_once = true;
                
        }

    /**
       Modifies a control system
    */
    public void setControlSystem( final int index, final ControlSystemS controlSystem )
        {
        controlSystem.abstract_robot = control_systems[index].abstract_robot;
        controlSystem.seed = control_systems[index].seed;
        control_systems[index] = controlSystem;
        }

    /**
       Returns a robot atached to a control system (for logging and evaluation purposes)
    */
    public Simple getRobot( final int index )
        {
        return control_systems[index].abstract_robot;
        }

    /**
       Get the thread (for joining)
    */
    public Thread getThread()
        {
        return run_sim_thread;
        }

    public ECSimulationCanvas(Frame p, int w, int h,
                              String dscfile) {
        this(p, w, h, dscfile, true);

        visionNoiseStddev = 0.0; //default is no noise
        visionNoiseSeed = 31337; //default noise seed
    }
          
    /**
       Set up the SimulationCanvas.
    */
    public ECSimulationCanvas(Frame p, int w, int h,
                              String dscfile, boolean preserveSize)
        {
        if (p == null) 
            {
            graphics_on = false;
            pause = false;
            }
        else
            {
            graphics_on = true;
            pause = true;
            }
        parent = p;
        simulated_objects = new SimulatedObject[0];
        control_systems = new ControlSystemS[0];
        this.preserveSize = preserveSize;
        
        descriptionfile = dscfile;

        if (graphics_on)
            {
            setSize(w,h);
            setBackground(Color.white);
            }

        }


    private boolean description_file_loaded = false;
    /**
     * Provide info about whether we have successufully
     * loaded the file.
     * @return true if a file is loaded, false otherwise.
     */
    public boolean descriptionLoaded()
        {
        return (description_file_loaded);
        }


    private boolean keep_running = true;
    /**
     * Run the simulation.
     */
    public void run()
        {
        //pause = true;
        long start_time = System.currentTimeMillis();
        long sim_timestep = 0;
        boolean robots_done = false;
        while (keep_running)
            {
            while(pause||(description_file_loaded==false))
                {
                if (graphics_on) this.repaint();
                try {Thread.sleep(200);}
                catch(InterruptedException e){}
                }

            current_time = System.currentTimeMillis();
            sim_timestep = timestep;
                        
            //--- deprecated
            //sim_timestep = (long)(
            //(double)(current_time - last_time)*
            //time_compression);
            //if (sim_timestep>maxtimestep)
            //sim_timestep = maxtimestep;

            /*--- run control systems and check for done ---*/
            robots_done = true;
            for(int i=0; i<control_systems.length; i++)
                {
                int stat = control_systems[i].takeStep();
                if (stat!=ControlSystemS.CSSTAT_DONE)
                    robots_done = false;
                }

            /*--- run the physics ---*/
            for(int i=0; i<simulated_objects.length; i++)
                simulated_objects[i].takeStep(
                    sim_timestep, 
                    simulated_objects);

            /*--- draw everything ---*/
            to_draw = true;
            if (graphics_on) this.repaint();
            if (to_draw&&graphics_on)
                {
                try {Thread.sleep(10);}
                catch(InterruptedException e){}
                }

            /*--- garbage collect every time ---*/
            // this is to make cycle times more homogeneous
            //System.gc();  // too slow!

            /*--- count frames ---*/
            frames++;// for statistics gathering

            /*--- check for timeout or done ---*/
            if (((timeout>0)&&(sim_time>=timeout))
                || robots_done)
                {
                if (trials <= 1)
                    {
                    for (int i = 0; 
                         i<control_systems.length; i++)
                        {
                        control_systems[i].trialEnd();
                        control_systems[i].quit();
                        }
                    keep_running=false;
                    if (graphics_on == false)
                        showRuntimeStats();
                    // A System.exit was replaced
                    // with a return to make possible that
                    // the run of the simulator in an
                    // evolutionary loop
                    return;
                    }
                else
                    {
                    for (int i = 0; 
                         i<control_systems.length; i++)
                        {
                        control_systems[i].trialEnd();
                        }
                    trials--;
                    sim_time = 0;
                    reset();
                    start();
                    }
                }

            /*--- increment simulation time ---*/
            sim_time += sim_timestep;
            }
        }


    /**
     * Handle a drawing request.
     */
    public synchronized void update(Graphics g)
        {
        if((bufferg!=null)&&(graphics_on))
            {
            /*--- if no bgimage, draw bgcolor ---*/
            if (bgimage == null)
                {
                bufferg.setColor(bgcolor);
                bufferg.fillRect(0,0,width,height);
                }

            /*--- draw the background image first ---*/
            if (bgimage != null)
                bufferg.drawImage(bgimage, 0, 0, this);

            /*--- draw robot trails first ---*/
            for(int i= 0; i<simulated_objects.length; i++)
                {
                // if robot
                if (simulated_objects[i] instanceof Simple)
                    {
                    // draw trail
                    if (draw_trails)
                        {
                        simulated_objects[i].drawTrail(bufferg,
                                                       width,height,
                                                       top, bottom,left,right);
                        }
                    }
                }

            /*--- draw IDs and state ---*/
            for(int i= 0; i<simulated_objects.length; i++)
                {
                // if robot
                if (simulated_objects[i] instanceof Simple)
                    {
                    if (draw_ids)
                        {
                        simulated_objects[i].drawID(bufferg,
                                                    width,height,
                                                    top, bottom,left,right);
                        }
                    if (draw_robot_state)
                        {
                        simulated_objects[i].drawState(bufferg,
                                                       width,height,
                                                       top, bottom,left,right);
                        }
                    }

                /*--- draw the object ---*/
                if (draw_icons)
                    simulated_objects[i].drawIcon(
                        bufferg,width,height,
                        top, bottom,left,right);
                else
                    simulated_objects[i].draw(
                        bufferg,width,height,
                        top, bottom,left,right);

                /*--- draw object state ---*/
                // if not a robot
                if (!(simulated_objects[i] instanceof Simple))
                    {
                    if (draw_object_state)
                        {
                        simulated_objects[i].drawState(bufferg,
                                                       width,height,
                                                       top, bottom,left,right);
                        }
                    }
                }
            g.drawImage(buffer, 0, 0, this);
            }
        to_draw = false;
        }
          
    /**
       Resize the SimulationCanvas.
    */
    public void setSize(int w, int h)
        {
        width = w;
        height = h;
        super.setSize(width,height);
        }

    public Dimension getPreferredSize() {
        return new Dimension(width, height);
    }

    public void reSizeWindow() {
        invalidate();
        Container parent = getParent();
        while (parent.getParent() != null) {
            parent = parent.getParent();
            }
        parent.setSize(parent.getPreferredSize());
        parent.validate();
    }
          

    /**
     * Handle a quit event.
     */
    public void quit()
        {
        //call all the control system .quit methods
        for (int i = 0; i<control_systems.length; i++)
            {
            System.out.println(control_systems.length);
            control_systems[i].trialEnd();
            control_systems[i].quit();
            }
        }

    /**
     * Handle a reset event.
     */
    public void reset()
        {

        //
        // verify whether the thread needs to be shut down
        //
        // shut down the thread (if any)
        if( run_sim_thread != null )
            {
            keep_running = false;
            try
                {
                run_sim_thread.join();
                }
            catch( InterruptedException e )
                {
                }
            }

        sim_time = 0;
        keep_running = true;
        pause = true;

// DO NOT START THE SIMULATION EVEN IF YOU HAVE NO GRAPHICS!!!!
// THAT IS BECAUSE ECJ NEEDS TO RESET THE SIMULATOR WITH NO GRAPHICS, BUT MAY NOT WANT TO HAVE IT RESTART
/*
  if (graphics_on == true) 
  pause = true;
  else
  pause = false;
*/

        for (int i = 0; 
             i<control_systems.length; i++)
            {
            control_systems[i].trialEnd();
            control_systems[i].quit();
            }

        if (descriptionfile!=null)
            {
            try
                {
                loadEnvironmentDescription();
                }
            catch (FileNotFoundException e)
                {
                Dialog tmp;
                description_file_loaded = false;
                simulated_objects = new SimulatedObject[0];
                String msg = "file not found: "
                    + descriptionfile;
                if (graphics_on)
                    tmp = new DialogMessage(parent,
                                            "TBSim Error", msg);
                else
                    System.out.println(msg);
                descriptionfile = null;
                }
            catch(IOException e)
                {
                Dialog tmp;
                description_file_loaded = false;
                simulated_objects = new SimulatedObject[0];
                String msg = "error trying to load "+
                    descriptionfile;
                if (graphics_on)
                    tmp = new DialogMessage(parent,
                                            "TBSim Error", msg);
                else
                    System.out.println(msg);
                descriptionfile = null;
                }
            if (graphics_on)
                {
                buffer = createImage(width,height);
                bufferg = buffer.getGraphics();
                bufferg.setColor(Color.white);
                bufferg.fillRect(0,0,width,height);
                this.repaint();
                pause = true;
                }
            }
        else
            {
            Dialog tmp;
            String msg = "Error: no description file";
            if (graphics_on)
                tmp = new DialogMessage(parent,
                                        "TBSim Error",
                                        "You must choose description file first.\n"+
                                        "Use the `load' option under the `file' menu.");
            }

        /*--- instantitate thread ---*/
        run_sim_thread = new Thread(this);
        run_sim_thread.start();
        }


    /**
     * Handle a start/resume event.
     */
    public void start()
        {
        if (description_file_loaded)
            {
            pause = false;
            if (graphics_on) this.repaint();
            // tell the control systems the trial is beginning
            for (int i = 0; i<control_systems.length; i++)
                {
                control_systems[i].trialInit();
                }
            startrun = System.currentTimeMillis();
            frames = 0;
            }
        else
            {
            Dialog tmp;
            if (graphics_on) 
                tmp = new DialogMessage(parent,
                                        "TBSim Error",
                                        "You must load a description file first.\n"+
                                        "Use the `load' option under the `file' menu.");
            }
        }


    /**
     * Handle a Runtime Stats event
     */
    public void showRuntimeStats()
        {
        long    f = frames;
        long    t = System.currentTimeMillis() - startrun;

        Runtime r = Runtime.getRuntime();

        String this_sim = 
            " trial number      : "+trials+" (counts down)\n"+
            " sim time          : "+sim_time+" milliseconds\n"+
            " timestep          : "+timestep+" milliseconds\n"+
            " timeout           : "+timeout+" milliseconds\n";

        if (pause)
            {
            this_sim = this_sim +
                " frames/second : N/A while paused\n"
                + " free memory         : "+r.freeMemory()+"\n"
                + " total memory        : "+r.totalMemory()+"\n"
                + " os.name             : "+System.getProperty("os.name")+"\n"
                + " os.version          : "+System.getProperty("os.version")+"\n"
                + " os.arch             : "+System.getProperty("os.arch")+"\n"
                + " java.version        : "+System.getProperty("java.version")+"\n";
            }
        else
            {
            double rate = 1000*(double)frames/(double)t;
            this_sim = this_sim +
                " frames/second : "+rate+ "\n"
                + " free memory : "+r.freeMemory()+"\n"
                + " total memory        : "+r.totalMemory()+"\n"
                + " os.name     : "+System.getProperty("os.name")+"\n"
                + " os.version  : "+System.getProperty("os.version")+"\n"
                + " os.arch     : "+System.getProperty("os.arch")+"\n"
                + " java.version        : "+System.getProperty("java.version")+"\n";
            }
        Dialog tmp;
        if (graphics_on)
            tmp = new DialogMessage(parent, "Runtime Stats",
                                    this_sim);
        else
            System.out.println(this_sim);
        }


    /**
     * Handle a pause event.
     */
    public void pause()
        {
        pause = true;
        }


    /**
     * Handle setDrawIDs
     */
    public void setDrawIDs(boolean v)
        {
        draw_ids = v;
        }


    /**
     * Handle setDrawIcons
     */
    public void setDrawIcons(boolean v)
        {
        draw_icons = v;
        }


    /**
     * Handle setGraphics
     */
    public void setGraphics(boolean v)
        {
        graphics_on = v;
        }


    /**
     * Handle setDrawRobotState
     */
    public void setDrawRobotState(boolean v)
        {
        draw_robot_state = v;
        }


    /**
     * Handle setDrawObjectState
     */
    public void setDrawObjectState(boolean v)
        {
        draw_object_state = v;
        }


    /**
     * Handle setDrawTrails
     */
    public void setDrawTrails(boolean v)
        {
        draw_trails = v;
        }


    /**
       Handle a load request.
    */
    public void load(String df)
        {
        pause();
        descriptionfile = df;
        reset();
        }

    }
/*
  $Log: not supported by cvs2svn $
  Revision 1.2  2010/10/24 19:40:46  feijai
  yo

  Revision 1.1  2006/06/01 00:41:33  feijai
  Moved Teambots
  Issue number:
  Obtained from:
  Submitted by:
  Reviewed by:

  Revision 1.1.1.1  2006/05/31 22:27:56  feijai
  Original Import

  Revision 1.2  2006/02/12 20:07:02  feijai
  Updated License

  Revision 1.1.1.1  2006/01/13 21:12:09  lpanait
  Imported current source files of ECJ from the GMU repository as of 1/13/2006, 16:10PM

  Revision 1.1.1.1  2005/12/05 20:59:38  sean
  YoCVS: ----------------------------------------------------------------------

  Revision 1.4  2005/02/17 04:45:49  spaus
  Deglobalization changes.
  Master/Slave problem feature.

  Revision 1.3  2004/05/26 03:02:35  sean
  Yo

  Revision 1.2  2004/03/09 21:48:11  sean
  Yo

  Revision 1.1  2002/03/07 01:26:31  sean
  Yeah

  Revision 1.1  2002/03/06 22:08:44  lpanait
  *** empty log message ***

  Revision 1.11  2000/03/13 00:08:14  trb
  *** empty log message ***

  # Revision 1.10  2000/03/08  00:59:18  jds
  # fixed small visionNoise related bug
  # 
  # Revision 1.9  2000/03/07  23:37:19  jds
  # this refines support for "vision_noise"  by allowing a mean to be
  # specified.  an improvement for this command in general is that it would
  # be nice to specify noise parameters for individual sensors...
  # 
  # Revision 1.8  2000/03/07  15:31:08  jds
  # added support for a "vision_noise" command which will give the std dev
  # for a normal dist of noise on all sensors of type VisualObjectSensor.  it
  # also lets you give it a seed value for repeatable noise.
  # this noise feature should be expanded to allow different noise values
  # per robot, but that is for later....
  # 
  # Revision 1.7  2000/03/07  00:06:20  jds
  # the commands used in the dsc file are no longer case sensitive
  # 
  # Revision 1.6  99/12/06  16:37:30  trb
  # *** empty log message ***
  # 
  # Revision 1.5  99/11/23  23:55:23  jds
  # *** empty log message ***
  # 
  # Revision 1.4  99/11/23  23:21:06  jds
  # added Will's changes :
  #  - added the windowsize keyword to the description file format
  #  - fixed the implementation of setSize()
  #   - added a getPreferredSize() method
  #   - added a reSizeWindow() window method (this walks the component tree to
  # get our window then resizes it to fit our new size.  Call after setSize().)
  #  - added a new constructor with a boolean argument that indicates whether
  # to ignore any windowsize keywords you read
  # 
  # Revision 1.3  99/11/18  19:59:19  jds
  # now looks for dictionary keyword and adds the key string pair to the dictionary
  # then it sets the robot's dictionary reference
  # 
  # Revision 1.2  99/11/16  18:24:59  jds
  # Added dsc file support for view_robot_IDs, view_robot_trails, view_robot_state, view_object_info, view_icons.  They default to false, but read "on" as true.  made the vars package scope so TBSim could use them to update the menus
  # 
  # Revision 1.1  99/11/05  15:53:52  trb
  # Initial revision
  # 
  # Revision 1.2  99/03/09  13:17:20  slenser
  # Made the default to draw icons.
  # 
  # Revision 1.1  99/03/07  14:54:21  trb
  # Changed name from JavaBotSim to TBSim
  # 
  # Revision 1.1.1.1  98/07/25  18:44:38  slenser
  # Initial version.
  # 
  Revision 1.8  1998/07/25 22:42:24  tucker
  *** empty log message ***

  # Revision 1.7  1998/06/02  20:52:41  tucker
  # *** empty log message ***
  #
  Revision 1.6  1998/04/06 21:13:32  tucker
  *** empty log message ***

  Revision 1.5  1998/02/23 22:57:31  tucker
  faster

  Revision 1.4  1998/02/04 17:48:57  tucker
  *** empty log message ***

  Revision 1.3  1998/02/02 04:52:23  tucker
  fixed class naming bug

  # Revision 1.2  1998/01/30  22:56:18  tucker
  # *** empty log message ***
  #
  Revision 1.1  1998/01/29 02:49:03  tucker
  Initial revision

  # Revision 1.6  1997/10/28  01:19:27  tucker
  # added maxtimestep
  #
  Revision 1.5  1997/10/14 00:43:44  tucker
  added capability to simulate Pebbles robot.

  Revision 1.4  1997/09/09 19:14:50  tucker
  *** empty log message ***

  Revision 1.3  1997/08/17 17:00:06  tucker
  *** empty log message ***

  # Revision 1.2  1997/08/07  15:13:27  tucker
  # *** empty log message ***
  #
  Revision 1.1  1997/08/05 22:54:55  tucker
  Initial revision

*/
