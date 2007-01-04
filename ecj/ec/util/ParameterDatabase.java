/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

/* 

* ParameterDatabase.java
* Created: Sat Aug  7 12:09:19 1999
*/

/**
 * 
 * <p>
 * This extension of the Properties class allows you to set, get, and delete
 * Parameters in a hierarchical tree-like database. The database consists of a
 * list of Parameters, plus an array of "parent databases" which it falls back
 * on when it can't find the Parameter you're looking for. Parents may also have
 * arrays of parents, and so on..
 * 
 * <p>
 * The parameters are loaded from a Java property-list file, which is basically
 * a collection of parameter=value pairs, one per line. Empty lines and lines
 * beginning with # are ignored. These parameters and their values are
 * <b>case-sensitive </b>, and whitespace is trimmed I believe.
 * 
 * <p>
 * An optional set of parameters, "parent. <i>n </i>", where <i>n </i> are
 * consecutive integers starting at 0, define the filenames of the database's
 * parents.
 * 
 * <p>
 * An optional set of parameters, "print-params", specifies whether or not
 * parameters should be printed as they are used (through one of the get(...)
 * methods). If print-params is unset, or set to false or FALSE, nothing is
 * printed. If set to non-false, then the parameters are printed prepended with a "P:"
 * when their values are requested,  "E:" when their existence is tested.  Prior to the
 * "P:" or "E:" you may see a "!" (meaning that the parameter isn't in the database),
 * or a "&lt;" (meaning that the parameter was a default parameter which was never
 * looked up because the primary parameter contained the value).
 * 
 * <p>
 * <p>
 * When you create a ParameterDatabase using new ParameterDatabase(), it is
 * created thus:
 * 
 * <p>
 * <table border=0 cellpadding=0 cellspacing=0>
 * <tr>
 * <td><tt>DATABASE:</tt></td>
 * <td><tt>&nbsp;database</tt></td>
 * </tr>
 * <tr>
 * <td><tt>FROM:</tt></td>
 * <td><tt>&nbsp;(empty)</tt></td>
 * </tr>
 * </table>
 * 
 * 
 * <p>
 * When you create a ParameterDatabase using new ParameterDatabase( <i>file
 * </i>), it is created by loading the database file, and its parent file tree,
 * thus:
 * 
 * <p>
 * <table border=0 cellpadding=0 cellspacing=0>
 * <tr>
 * <td><tt>DATABASE:</tt></td>
 * <td><tt>&nbsp;database</tt></td>
 * <td><tt>&nbsp;-&gt;</tt></td>
 * <td><tt>&nbsp;parent0</tt></td>
 * <td><tt>&nbsp;+-&gt;</tt></td>
 * <td><tt>&nbsp;parent0</tt></td>
 * <td><tt>&nbsp;+-&gt;</tt></td>
 * <td><tt>&nbsp;parent0</tt></td>
 * <td><tt>&nbsp;+-&gt;</tt></td>
 * <td><tt>&nbsp;....</tt></td>
 * </tr>
 * <tr>
 * <td><tt>FROM:</tt></td>
 * <td><tt>&nbsp;(empty)</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;(file)</tt></td>
 * <td><tt>&nbsp;|</tt></td>
 * <td><tt>&nbsp;(parent.0)</tt></td>
 * <td><tt>&nbsp;|</tt></td>
 * <td><tt>&nbsp;(parent.0)</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;....</tt></td>
 * </tr>
 * <tr>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;|</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;+-&gt;</tt></td>
 * <td><tt>&nbsp;parent1</tt></td>
 * <td><tt>&nbsp;+-&gt;</tt></td>
 * <td><tt>&nbsp;....</tt></td>
 * </tr>
 * <tr>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;|</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;|</tt></td>
 * <td><tt>&nbsp;(parent.1)</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * </tr>
 * <tr>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;|</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;....</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * </tr>
 * <tr>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;|</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * </tr>
 * <tr>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;+-&gt;</tt></td>
 * <td><tt>&nbsp;parent1</tt></td>
 * <td><tt>&nbsp;+-&gt;</tt></td>
 * <td><tt>&nbsp;....</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * </tr>
 * <tr>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;|</tt></td>
 * <td><tt>&nbsp;(parent.1)</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * </tr>
 * <tr>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;....</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * </tr>
 * </table>
 * 
 * 
 * <p>
 * When you create a ParameterDatabase using new ParameterDatabase( <i>file,argv
 * </i>), the preferred way, it is created thus:
 * 
 * 
 * <p>
 * <table border=0 cellpadding=0 cellspacing=0>
 * <tr>
 * <td><tt>DATABASE:</tt></td>
 * <td><tt>&nbsp;database</tt></td>
 * <td><tt>&nbsp;-&gt;</tt></td>
 * <td><tt>&nbsp;parent0</tt></td>
 * <td><tt>&nbsp;+-&gt;</tt></td>
 * <td><tt>&nbsp;parent0</tt></td>
 * <td><tt>&nbsp;+-&gt;</tt></td>
 * <td><tt>&nbsp;parent0</tt></td>
 * <td><tt>&nbsp;+-&gt;</tt></td>
 * <td><tt>&nbsp;parent0</tt></td>
 * <td><tt>&nbsp;+-&gt;</tt></td>
 * <td><tt>&nbsp;....</tt></td>
 * </tr>
 * <tr>
 * <td><tt>FROM:</tt></td>
 * <td><tt>&nbsp;(empty)</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>(argv)</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;(file)</tt></td>
 * <td><tt>&nbsp;|</tt></td>
 * <td><tt>&nbsp;(parent.0)</tt></td>
 * <td><tt>&nbsp;|</tt></td>
 * <td><tt>&nbsp;(parent.0)</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;....</tt></td>
 * </tr>
 * <tr>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;|</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;+-&gt;</tt></td>
 * <td><tt>&nbsp;parent1</tt></td>
 * <td><tt>&nbsp;+-&gt;</tt></td>
 * <td><tt>&nbsp;....</tt></td>
 * </tr>
 * <tr>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;|</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;|</tt></td>
 * <td><tt>&nbsp;(parent.1)</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * </tr>
 * <tr>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;|</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;....</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * </tr>
 * <tr>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;|</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * </tr>
 * <tr>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;+-&gt;</tt></td>
 * <td><tt>&nbsp;parent1</tt></td>
 * <td><tt>&nbsp;+-&gt;</tt></td>
 * <td><tt>&nbsp;....</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * </tr>
 * <tr>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;|</tt></td>
 * <td><tt>&nbsp;(parent.1)</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * </tr>
 * <tr>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;....</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * <td><tt>&nbsp;</tt></td>
 * </tr>
 * </table>
 * 
 * 
 * <p>
 * ...that is, the actual top database is empty, and stores parameters added
 * programmatically; its parent is a database formed from arguments passed in on
 * the command line; <i>its </i> parent is the parameter database which actually
 * loads from foo. This allows you to programmatically add parameters which
 * override those in foo, then delete them, thus bringing foo's parameters back
 * in view.
 * 
 * <p>
 * Once a parameter database is loaded, you query it with the <tt>get</tt>
 * methods. The database, then its parents, are searched until a match is found
 * for your parameter. The search rules are thus: (1) the root database is
 * searched first. (2) If a database being searched doesn't contain the data, it
 * searches its parents recursively, starting with parent 0, then moving up,
 * until all searches are exhausted or something was found. (3) No database is
 * searched twice.
 *
 * <p>The various <tt>get</tt> methods all take two parameters.  The first
 * parameter is fetched and retrieved first.  If that fails, the second one
 * (known as the <i>default parameter</i>) is fetched and retrieved.  You
 * can pass in <tt>null</tt> for the default parameter if you don't have one.
 *
 * <p>You can test a parameter for existence with the <tt>exists</tt> methods.
 * 
 * <p>
 * You can set a parameter (in the topmost database <i>only </i> with the
 * <tt>set</tt> command. The <tt>remove</tt> command removes a parameter
 * from the topmost database only. The <tt>removeDeeply</tt> command removes
 * that parameter from every database.
 * 
 * <p>
 * The values stored in a parameter database must not contain "#", "=",
 * non-ascii values, or whitespace.
 * 
 * <p>
 * <b>Note for JDK 1.1 </b>. Finally recovering from stupendous idiocy, JDK 1.2
 * included parseDouble() and parseFloat() commands; now you can READ A FLOAT
 * FROM A STRING without having to create a Float object first! Anyway, you will
 * need to modify the getFloat() method below if you're running on JDK 1.1, but
 * understand that large numbers of calls to the method may be inefficient.
 * Sample JDK 1.1 code is given with those methods, but is commented out.
 * 
 * 
 * @author Sean Luke
 * @version 1.0
 */

public class ParameterDatabase extends Properties implements Serializable 
    {
    public static final String C_HERE = "$";
    public static final String UNKNOWN_VALUE = "";
    public static final String PRINT_PARAMS = "print-params";
    public static final int PS_UNKNOWN = -1;
    public static final int PS_NONE = 0;
    public static final int PS_PRINT_PARAMS = 1;
    public int printState = PS_UNKNOWN;
    Vector parents;
    File directory;
    String filename;
    boolean checked;
    Hashtable gotten;
    Hashtable accessed;
    Vector listeners;

    /**
     * Searches down through databases to find a given parameter, whose value
     * must be a full Class name, and the class must be a descendent of but not
     * equal to <i>mustCastTosuperclass </i>. Loads the class and returns an
     * instance (constructed with the default constructor), or throws a
     * ParamClassLoadException if there is no such Class. If the parameter is
     * not found, the defaultParameter is used. The parameter chosen is marked
     * "used".
     */
    public Object getInstanceForParameter(Parameter parameter, Parameter defaultParameter, Class mustCastTosuperclass) throws ParamClassLoadException 
        {
        printGotten(parameter, defaultParameter, false);
        Parameter p;
        if (_exists(parameter))
            p = parameter;
        else if (_exists(defaultParameter))
            p = defaultParameter;
        else
            throw new ParamClassLoadException(
                "No class name provided.\nPARAMETER: "
                + parameter
                + (defaultParameter == null ? "" : "\n     ALSO: "
                   + defaultParameter));
        try 
            {
            Class c = Class.forName(get(p));
            if (!mustCastTosuperclass.isAssignableFrom(c))
                throw new ParamClassLoadException("The class "
                                                  + c.getName()
                                                  + "\ndoes not cast into the superclass "
                                                  + mustCastTosuperclass.getName()
                                                  + "\nPARAMETER: "
                                                  + parameter
                                                  + (defaultParameter == null ? "" : "\n     ALSO: "
                                                     + defaultParameter));
            if (mustCastTosuperclass == c)
                throw new ParamClassLoadException("The class "
                                                  + c.getName()
                                                  + "\nmust not be the same as the required superclass "
                                                  + mustCastTosuperclass.getName()
                                                  + "\nPARAMETER: "
                                                  + parameter
                                                  + (defaultParameter == null ? "" : "\n     ALSO: "
                                                     + defaultParameter));
            return c.newInstance();
            } 
        catch (ClassNotFoundException e) 
            {
            throw new ParamClassLoadException("Class not found: "
                                              + get(p)
                                              + "\nPARAMETER: "
                                              + parameter
                                              + (defaultParameter == null ? "" : "\n     ALSO: "
                                                 + defaultParameter) + "\nEXCEPTION: \n\n" + e);
            } 
        catch (IllegalArgumentException e) 
            {
            throw new ParamClassLoadException("Could not load class: "
                                              + get(p)
                                              + "\nPARAMETER: "
                                              + parameter
                                              + (defaultParameter == null ? "" : "\n     ALSO: "
                                                 + defaultParameter) + "\nEXCEPTION: \n\n" + e);
            } 
        catch (InstantiationException e) 
            {
            throw new ParamClassLoadException(
                "The requested class is an interface or an abstract class: "
                + get(p)
                + "\nPARAMETER: "
                + parameter
                + (defaultParameter == null ? "" : "\n     ALSO: "
                   + defaultParameter) + "\nEXCEPTION: \n\n"
                + e);
            } 
        catch (IllegalAccessException e) 
            {
            throw new ParamClassLoadException(
                "The requested class cannot be initialized with the default initializer: "
                + get(p)
                + "\nPARAMETER: "
                + parameter
                + (defaultParameter == null ? "" : "\n     ALSO: "
                   + defaultParameter) + "\nEXCEPTION: \n\n"
                + e);
            }
        }

    /**
     * Searches down through databases to find a given parameter, whose value
     * must be a full Class name, and the class must be a descendent, or equal
     * to, <i>mustCastTosuperclass </i>. Loads the class and returns an instance
     * (constructed with the default constructor), or throws a
     * ParamClassLoadException if there is no such Class. The parameter chosen
     * is marked "used".
     */
    public Object getInstanceForParameterEq(Parameter parameter,
                                            Parameter defaultParameter, Class mustCastTosuperclass)
        throws ParamClassLoadException 
        {
        printGotten(parameter, defaultParameter, false);
        Parameter p;
        if (_exists(parameter))
            p = parameter;
        else if (_exists(defaultParameter))
            p = defaultParameter;
        else
            throw new ParamClassLoadException(
                "No class name provided.\nPARAMETER: "
                + parameter
                + "\n     ALSO: "
                + (defaultParameter == null ? "" : "\n     ALSO: "
                   + defaultParameter));
        try
            {
            Class c = Class.forName(get(p));
            if (!mustCastTosuperclass.isAssignableFrom(c))
                throw new ParamClassLoadException("The class "
                                                  + c.getName()
                                                  + "\ndoes not cast into the superclass "
                                                  + mustCastTosuperclass.getName()
                                                  + "\nPARAMETER: "
                                                  + parameter
                                                  + "\n     ALSO: "
                                                  + (defaultParameter == null ? "" : "\n     ALSO: "
                                                     + defaultParameter));
            return c.newInstance();
            } 
        catch (ClassNotFoundException e) 
            {
            throw new ParamClassLoadException("Class not found: "
                                              + get(p)
                                              + "\nPARAMETER: "
                                              + parameter
                                              + "\n     ALSO: "
                                              + (defaultParameter == null ? "" : "\n     ALSO: "
                                                 + defaultParameter) + "\nEXCEPTION: \n\n" + e);
            } 
        catch (IllegalArgumentException e) 
            {
            throw new ParamClassLoadException("Could not load class: "
                                              + get(p)
                                              + "\nPARAMETER: "
                                              + parameter
                                              + "\n     ALSO: "
                                              + (defaultParameter == null ? "" : "\n     ALSO: "
                                                 + defaultParameter) + "\nEXCEPTION: \n\n" + e);
            } 
        catch (InstantiationException e) 
            {
            throw new ParamClassLoadException(
                "The requested class is an interface or an abstract class: "
                + get(p)
                + "\nPARAMETER: "
                + parameter
                + "\n     ALSO: "
                + (defaultParameter == null ? "" : "\n     ALSO: "
                   + defaultParameter) + "\nEXCEPTION: \n\n"
                + e);
            } 
        catch (IllegalAccessException e) 
            {
            throw new ParamClassLoadException(
                "The requested class cannot be initialized with the default initializer: "
                + get(p)
                + "\nPARAMETER: "
                + parameter
                + "\n     ALSO: "
                + (defaultParameter == null ? "" : "\n     ALSO: "
                   + defaultParameter) + "\nEXCEPTION: \n\n"
                + e);
            }
        }

    /**
     * Searches down through databases to find a given parameter. The value
     * associated with this parameter must be a full Class name, and the class
     * must be a descendent of but not equal to <i>mustCastTosuperclass </i>.
     * Loads and returns the associated Class, or throws a
     * ParamClassLoadException if there is no such Class. If the parameter is
     * not found, the defaultParameter is used. The parameter chosen is marked
     * "used".
     */
    public Object getClassForParameter(Parameter parameter,
                                       Parameter defaultParameter, Class mustCastTosuperclass)
        throws ParamClassLoadException 
        {
        printGotten(parameter, defaultParameter, false);
        Parameter p;
        if (_exists(parameter))
            p = parameter;
        else if (_exists(defaultParameter))
            p = defaultParameter;
        else
            throw new ParamClassLoadException(
                "No class name provided.\nPARAMETER: "
                + parameter
                + "\n     ALSO: "
                + (defaultParameter == null ? "" : "\n     ALSO: "
                   + defaultParameter));
        try
            {
            Class c = Class.forName(get(p));
            if (!mustCastTosuperclass.isAssignableFrom(c))
                throw new ParamClassLoadException("The class "
                                                  + c.getName()
                                                  + "\ndoes not cast into the superclass "
                                                  + mustCastTosuperclass.getName()
                                                  + "\nPARAMETER: "
                                                  + parameter
                                                  + "\n     ALSO: "
                                                  + (defaultParameter == null ? "" : "\n     ALSO: "
                                                     + defaultParameter));
            return c;
            } 
        catch (ClassNotFoundException e) 
            {
            throw new ParamClassLoadException("Class not found: "
                                              + get(p)
                                              + "\nPARAMETER: "
                                              + parameter
                                              + "\n     ALSO: "
                                              + (defaultParameter == null ? "" : "\n     ALSO: "
                                                 + defaultParameter) + "\nEXCEPTION: \n\n" + e);
            } 
        catch (IllegalArgumentException e) 
            {
            throw new ParamClassLoadException("Could not load class: "
                                              + get(p)
                                              + "\nPARAMETER: "
                                              + parameter
                                              + "\n     ALSO: "
                                              + (defaultParameter == null ? "" : "\n     ALSO: "
                                                 + defaultParameter) + "\nEXCEPTION: \n\n" + e);
            }
        }

    /**
     * Searches down through databases to find a given parameter; If the
     * parameter does not exist, defaultValue is returned. If the parameter
     * exists, and it is set to "false" (case insensitive), false is returned.
     * Else true is returned. The parameter chosen is marked "used" if it
     * exists.
     */
    public boolean getBoolean(Parameter parameter,
                              Parameter defaultParameter, boolean defaultValue) 
        {
        printGotten(parameter, defaultParameter, false);
        if (_exists(parameter))
            return getBoolean(parameter, defaultValue);
        else
            return getBoolean(defaultParameter, defaultValue);
        }

    /**
     * Searches down through databases to find a given parameter; If the
     * parameter does not exist, defaultValue is returned. If the parameter
     * exists, and it is set to "false" (case insensitive), false is returned.
     * Else true is returned. The parameter chosen is marked "used" if it
     * exists.
     */
    boolean getBoolean(Parameter parameter, boolean defaultValue) 
        {
        if (!_exists(parameter))
            return defaultValue;
        return (!get(parameter).equalsIgnoreCase("false"));
        }

    /**
     * Parses an integer from a string, either in decimal or (if starting with
     * an x) in hex
     */
    // we assume that the string has been trimmed already
    /*protected*/ int parseInt(String string)
        throws NumberFormatException 
        {
        char c;
        if (string != null && string.length() > 0
            && ((string.charAt(0) == (c = 'x')) || c == 'X')) 
            {
            // it's a hex int, load it as hex
            return Integer.parseInt(string.substring(1), 16);
            } 
        else
            // it's decimal
            return Integer.parseInt(string);
        }

    /**
     * Parses a long from a string, either in decimal or (if starting with an x)
     * in hex
     */
    // we assume that the string has been trimmed already
    /*protected*/ long parseLong(String string)
        throws NumberFormatException 
        {
        char c;
        if (string != null && string.length() > 0
            && ((string.charAt(0) == (c = 'x')) || c == 'X')) 
            {
            // it's a hex int, load it as hex
            return Long.parseLong(string.substring(1), 16);
            } 
        else
            // it's decimal
            return Long.parseLong(string);
        }

    /**
     * Searches down through databases to find a given parameter, whose value
     * must be an integer. It returns the value, else throws a
     * NumberFormatException exception if there is an error in parsing the
     * parameter. The parameter chosen is marked "used" if it exists. Integers
     * may be in decimal or (if preceded with an X or x) in hexadecimal.
     */
    /*protected*/ int getInt(Parameter parameter)
        throws NumberFormatException 
        {
        if (_exists(parameter)) 
            {
            try
                {
                return parseInt(get(parameter));
                } 
            catch (NumberFormatException e) 
                {
                throw new NumberFormatException("Bad integer ("
                                                + get(parameter) + " ) for parameter " + parameter);
                }
            } 
        else
            throw new NumberFormatException(
                "Integer does not exist for parameter " + parameter);
        }

    /**
     * Searches down through databases to find a given parameter, whose value
     * must be an integer. It returns the value, else throws a
     * NumberFormatException exception if there is an error in parsing the
     * parameter. The parameter chosen is marked "used" if it exists. Integers
     * may be in decimal or (if preceded with an X or x) in hexadecimal.
     */
    public int getInt(Parameter parameter, Parameter defaultParameter)
        throws NumberFormatException 
        {
        printGotten(parameter, defaultParameter, false);
        if (_exists(parameter))
            return getInt(parameter);
        else if (_exists(defaultParameter))
            return getInt(defaultParameter);
        else
            throw new NumberFormatException(
                "Integer does not exist for either parameter " + parameter
                + "\nor\n" + defaultParameter);
        }

    /**
     * Searches down through databases to find a given parameter, whose value
     * must be an integer >= minValue. It returns the value, or minValue-1 if
     * the value is out of range or if there is an error in parsing the
     * parameter. The parameter chosen is marked "used" if it exists. Integers
     * may be in decimal or (if preceded with an X or x) in hexadecimal.
     */
    public int getInt(Parameter parameter, Parameter defaultParameter,
                      int minValue) 
        {
        printGotten(parameter, defaultParameter, false);
        if (_exists(parameter))
            return getInt(parameter, minValue);
        else
            return getInt(defaultParameter, minValue);
        }

    /**
     * Searches down through databases to find a given parameter, whose value
     * must be an integer >= minValue. It returns the value, or minValue-1 if
     * the value is out of range or if there is an error in parsing the
     * parameter. The parameter chosen is marked "used" if it exists. Integers
     * may be in decimal or (if preceded with an X or x) in hexadecimal.
     */
    /*protected*/ int getInt(Parameter parameter, int minValue) 
        {
        if (_exists(parameter)) 
            {
            try
                {
                int i = parseInt(get(parameter));
                if (i < minValue)
                    return minValue - 1;
                return i;
                } 
            catch (NumberFormatException e) 
                {
                return minValue - 1;
                }
            } 
        else
            return minValue - 1;
        }

    /**
     * Searches down through databases to find a given parameter, which must be
     * an integer. If there is an error in parsing the parameter, then default
     * is returned. The parameter chosen is marked "used" if it exists. Integers
     * may be in decimal or (if preceded with an X or x) in hexadecimal.
     */
    public int getIntWithDefault(Parameter parameter,
                                 Parameter defaultParameter, int defaultValue) 
        {
        printGotten(parameter, defaultParameter, false);
        if (_exists(parameter))
            return getIntWithDefault(parameter, defaultValue);
        else
            return getIntWithDefault(defaultParameter, defaultValue);
        }

    /**
     * Searches down through databases to find a given parameter, which must be
     * an integer. If there is an error in parsing the parameter, then default
     * is returned. The parameter chosen is marked "used" if it exists. Integers
     * may be in decimal or (if preceded with an X or x) in hexadecimal.
     */
    int getIntWithDefault(Parameter parameter, int defaultValue) 
        {
        if (_exists(parameter)) 
            {
            try
                {
                return parseInt(get(parameter));
                } 
            catch (NumberFormatException e) 
                {
                return defaultValue;
                }
            } 
        else
            return defaultValue;
        }

    /**
     * Searches down through databases to find a given parameter, whose value
     * must be an integer >= minValue and <= maxValue. It returns the value, or
     * minValue-1 if the value is out of range or if there is an error in
     * parsing the parameter. The parameter chosen is marked "used" if it
     * exists. Integers may be in decimal or (if preceded with an X or x) in
     * hexadecimal.
     */
    public int getIntWithMax(Parameter parameter,
                             Parameter defaultParameter, int minValue, int maxValue) 
        {
        printGotten(parameter, defaultParameter, false);
        if (_exists(parameter))
            return getIntWithMax(parameter, minValue, maxValue);
        else
            return getIntWithMax(defaultParameter, minValue, maxValue);
        }

    /**
     * Searches down through databases to find a given parameter, whose value
     * must be an integer >= minValue and <= maxValue. It returns the value, or
     * minValue-1 if the value is out of range or if there is an error in
     * parsing the parameter. The parameter chosen is marked "used" if it
     * exists. Integers may be in decimal or (if preceded with an X or x) in
     * hexadecimal.
     */
    int getIntWithMax(Parameter parameter, int minValue, int maxValue) 
        {
        if (_exists(parameter)) 
            {
            try
                {
                int i = parseInt(get(parameter));
                if (i < minValue)
                    return minValue - 1;
                if (i > maxValue)
                    return minValue - 1;
                return i;
                } 
            catch (NumberFormatException e) 
                {
                return minValue - 1;
                }
            } 
        else
            return minValue - 1;
        }

    /**
     * Searches down through databases to find a given parameter, whose value
     * must be a float >= minValue. If not, this method returns minvalue-1, else
     * it returns the parameter value. The parameter chosen is marked "used" if
     * it exists.
     */

    public float getFloat(Parameter parameter,
                          Parameter defaultParameter, double minValue) 
        {
        printGotten(parameter, defaultParameter, false);
        if (_exists(parameter))
            return getFloat(parameter, minValue);
        else
            return getFloat(defaultParameter, minValue);
        }

    /**
     * Searches down through databases to find a given parameter, whose value
     * must be a float >= minValue. If not, this method returns minvalue-1, else
     * it returns the parameter value. The parameter chosen is marked "used" if
     * it exists.
     */

    float getFloat(Parameter parameter, double minValue) 
        {
        if (_exists(parameter)) 
            {
            try
                {
                float i = Float.valueOf(get(parameter)).floatValue(); // what
                                                                      // stupidity...

                // For JDK 1.2 and later, this is more efficient...
                // float i = Float.parseFloat(get(parameter));
                // ...but we can't use it and still be compatible with JDK 1.1

                if (i < minValue)
                    return (float) (minValue - 1);
                return i;
                } 
            catch (NumberFormatException e) 
                {
                return (float) (minValue - 1);
                }
            } 
        else
            return (float) (minValue - 1);
        }

    /**
     * Searches down through databases to find a given parameter, which must be
     * a float. If there is an error in parsing the parameter, then default is
     * returned. The parameter chosen is marked "used" if it exists.
     */
    public float getFloatWithDefault(Parameter parameter,
                                     Parameter defaultParameter, double defaultValue) 
        {
        printGotten(parameter, defaultParameter, false);
        if (_exists(parameter))
            return getFloatWithDefault(parameter, defaultValue);
        else
            return getFloatWithDefault(defaultParameter, defaultValue);
        }

    /**
     * Searches down through databases to find a given parameter, which must be
     * a float. If there is an error in parsing the parameter, then default is
     * returned. The parameter chosen is marked "used" if it exists.
     */
    float getFloatWithDefault(Parameter parameter, double defaultValue) 
        {
        if (_exists(parameter)) 
            {
            try
                {
                // For JDK 1.2 and later, this is more efficient...
                // return Float.parseFloat(get(parameter));
                // ...but we can't use it and still be compatible with JDK 1.1
                return Float.valueOf(get(parameter)).floatValue(); // what
                                                                   // stupidity...
                } 
            catch (NumberFormatException e) 
                {
                return (float) (defaultValue);
                }
            } 
        else
            return (float) (defaultValue);
        }

    /**
     * Searches down through databases to find a given parameter, whose value
     * must be a float >= minValue and <= maxValue. If not, this method returns
     * minvalue-1, else it returns the parameter value. The parameter chosen is
     * marked "used" if it exists.
     */

    public float getFloat(Parameter parameter,
                          Parameter defaultParameter, double minValue, double maxValue) 
        {
        printGotten(parameter, defaultParameter, false);
        if (_exists(parameter))
            return getFloat(parameter, minValue, maxValue);
        else
            return getFloat(defaultParameter, minValue, maxValue);
        }

    /**
     * Searches down through databases to find a given parameter, whose value
     * must be a float >= minValue and <= maxValue. If not, this method returns
     * minvalue-1, else it returns the parameter value. The parameter chosen is
     * marked "used" if it exists.
     */

    float getFloat(Parameter parameter, double minValue, double maxValue) 
        {
        if (_exists(parameter)) 
            {
            try
                {
                float i = Float.valueOf(get(parameter)).floatValue(); // what
                                                                      // stupidity...

                // For JDK 1.2 and later, this is more efficient...
                // float i = Float.parseFloat(get(parameter));
                // ...but we can't use it and still be compatible with JDK 1.1

                if (i < minValue)
                    return (float) (minValue - 1);
                if (i > maxValue)
                    return (float) (minValue - 1);
                return i;
                } 
            catch (NumberFormatException e) 
                {
                return (float) (minValue - 1);
                }
            } 
        else
            return (float) (minValue - 1);
        }

    /**
     * Searches down through databases to find a given parameter, whose value
     * must be a double >= minValue. If not, this method returns minvalue-1,
     * else it returns the parameter value. The parameter chosen is marked
     * "used" if it exists.
     */

    public double getDouble(Parameter parameter,
                            Parameter defaultParameter, double minValue) 
        {
        printGotten(parameter, defaultParameter, false);
        if (_exists(parameter))
            return getDouble(parameter, minValue);
        else
            return getDouble(defaultParameter, minValue);
        }

    /**
     * Searches down through databases to find a given parameter, whose value
     * must be a double >= minValue. If not, this method returns minvalue-1,
     * else it returns the parameter value. The parameter chosen is marked
     * "used" if it exists.
     */

    double getDouble(Parameter parameter, double minValue) 
        {
        if (_exists(parameter)) 
            {
            try
                {
                double i = Double.valueOf(get(parameter)).doubleValue(); // what
                                                                         // stupidity...

                // For JDK 1.2 and later, this is more efficient...
                // double i = Double.parseDouble(get(parameter));
                // ...but we can't use it and still be compatible with JDK 1.1

                if (i < minValue)
                    return (double) (minValue - 1);
                return i;
                } 
            catch (NumberFormatException e) 
                {
                return (double) (minValue - 1);
                }
            } 
        else
            return (double) (minValue - 1);
        }

    /**
     * Searches down through databases to find a given parameter, whose value
     * must be a double >= minValue and <= maxValue. If not, this method returns
     * minvalue-1, else it returns the parameter value. The parameter chosen is
     * marked "used" if it exists.
     */

    public double getDouble(Parameter parameter,
                            Parameter defaultParameter, double minValue, double maxValue) 
        {
        printGotten(parameter, defaultParameter, false);
        if (_exists(parameter))
            return getDouble(parameter, minValue, maxValue);
        else
            return getDouble(defaultParameter, minValue, maxValue);
        }

    /**
     * Searches down through databases to find a given parameter, whose value
     * must be a double >= minValue and <= maxValue. If not, this method returns
     * minvalue-1, else it returns the parameter value. The parameter chosen is
     * marked "used" if it exists.
     */

    double getDouble(Parameter parameter, double minValue, double maxValue) 
        {
        if (_exists(parameter)) 
            {
            try
                {
                double i = Double.valueOf(get(parameter)).doubleValue(); // what
                                                                         // stupidity...

                // For JDK 1.2 and later, this is more efficient...
                // double i = Double.parseDouble(get(parameter));
                // ...but we can't use it and still be compatible with JDK 1.1

                if (i < minValue)
                    return (double) (minValue - 1);
                if (i > maxValue)
                    return (double) (minValue - 1);
                return i;
                } 
            catch (NumberFormatException e) 
                {
                return (double) (minValue - 1);
                }
            } 
        else
            return (double) (minValue - 1);
        }

    /**
     * Searches down through databases to find a given parameter, which must be
     * a float. If there is an error in parsing the parameter, then default is
     * returned. The parameter chosen is marked "used" if it exists.
     */
    public double getDoubleWithDefault(Parameter parameter,
                                       Parameter defaultParameter, double defaultValue) 
        {
        printGotten(parameter, defaultParameter, false);
        if (_exists(parameter))
            return getDoubleWithDefault(parameter, defaultValue);
        else
            return getDoubleWithDefault(defaultParameter, defaultValue);
        }

    /**
     * Searches down through databases to find a given parameter, which must be
     * a float. If there is an error in parsing the parameter, then default is
     * returned. The parameter chosen is marked "used" if it exists.
     */
    double getDoubleWithDefault(Parameter parameter, double defaultValue) 
        {
        if (_exists(parameter)) 
            {
            try
                {
                // For JDK 1.2 and later, this is more efficient...
                // return Double.parseDouble(get(parameter));
                // ...but we can't use it and still be compatible with JDK 1.1
                return Double.valueOf(get(parameter)).doubleValue(); // what
                                                                     // stupidity...
                } 
            catch (NumberFormatException e) 
                {
                return defaultValue;
                }
            } 
        else
            return defaultValue;
        }

    /**
     * Searches down through databases to find a given parameter, whose value
     * must be a long. It returns the value, else throws a NumberFormatException
     * exception if there is an error in parsing the parameter. The parameter
     * chosen is marked "used" if it exists. Longs may be in decimal or (if
     * preceded with an X or x) in hexadecimal.
     */
    /*protected*/ long getLong(Parameter parameter)
        throws NumberFormatException 
        {
        if (_exists(parameter)) 
            {
            try
                {
                return parseLong(get(parameter));
                } 
            catch (NumberFormatException e) 
                {
                throw new NumberFormatException("Bad long (" + get(parameter)
                                                + " ) for parameter " + parameter);
                }
            } 
        else
            throw new NumberFormatException(
                "Long does not exist for parameter " + parameter);
        }

    /**
     * Searches down through databases to find a given parameter, whose value
     * must be a long. It returns the value, else throws a NumberFormatException
     * exception if there is an error in parsing the parameter. The parameter
     * chosen is marked "used" if it exists. Longs may be in decimal or (if
     * preceded with an X or x) in hexadecimal.
     */
    public long getLong(Parameter parameter, Parameter defaultParameter)
        throws NumberFormatException 
        {
        printGotten(parameter, defaultParameter, false);
        if (_exists(parameter))
            return getLong(parameter);
        else if (_exists(defaultParameter))
            return getLong(defaultParameter);
        else
            throw new NumberFormatException(
                "Long does not exist for either parameter " + parameter
                + "\nor\n" + defaultParameter);
        }

    /**
     * Searches down through databases to find a given parameter, whose value
     * must be a long >= minValue. If not, this method returns errValue, else it
     * returns the parameter value. The parameter chosen is marked "used" if it
     * exists. Longs may be in decimal or (if preceded with an X or x) in
     * hexadecimal.
     */

    public long getLong(Parameter parameter, Parameter defaultParameter,
                        long minValue) 
        {
        printGotten(parameter, defaultParameter, false);
        if (_exists(parameter))
            return getLong(parameter, minValue);
        else
            return getLong(defaultParameter, minValue);
        }

    /**
     * Searches down through databases to find a given parameter, whose value
     * must be a long >= minValue. If not, this method returns errValue, else it
     * returns the parameter value. The parameter chosen is marked "used" if it
     * exists. Longs may be in decimal or (if preceded with an X or x) in
     * hexadecimal.
     */
    long getLong(Parameter parameter, long minValue) 
        {
        if (_exists(parameter)) 
            {
            try
                {
                long i = parseLong(get(parameter));
                if (i < minValue)
                    return minValue - 1;
                return i;
                } 
            catch (NumberFormatException e) 
                {
                return minValue - 1;
                }
            } 
        else
            return (minValue - 1);
        }

    /**
     * Searches down through databases to find a given parameter, which must be
     * a long. If there is an error in parsing the parameter, then default is
     * returned. The parameter chosen is marked "used" if it exists. Longs may
     * be in decimal or (if preceded with an X or x) in hexadecimal.
     */
    public long getLongWithDefault(Parameter parameter,
                                   Parameter defaultParameter, long defaultValue) 
        {
        printGotten(parameter, defaultParameter, false);
        if (_exists(parameter))
            return getLongWithDefault(parameter, defaultValue);
        else
            return getLongWithDefault(defaultParameter, defaultValue);
        }

    /**
     * Searches down through databases to find a given parameter, which must be
     * a long. If there is an error in parsing the parameter, then default is
     * returned. The parameter chosen is marked "used" if it exists. Longs may
     * be in decimal or (if preceded with an X or x) in hexadecimal.
     */
    long getLongWithDefault(Parameter parameter, long defaultValue) 
        {
        if (_exists(parameter)) 
            {
            try
                {
                return parseLong(get(parameter));
                } 
            catch (NumberFormatException e) 
                {
                return defaultValue;
                }
            } 
        else
            return defaultValue;
        }

    /**
     * Searches down through databases to find a given parameter, whose value
     * must be a long >= minValue and = < maxValue. If not, this method returns
     * errValue, else it returns the parameter value. The parameter chosen is
     * marked "used" if it exists. Longs may be in decimal or (if preceded with
     * an X or x) in hexadecimal.
     */
    public long getLongWithMax(Parameter parameter,
                               Parameter defaultParameter, long minValue, long maxValue) 
        {
        printGotten(parameter, defaultParameter, false);
        if (_exists(parameter))
            return getLong(parameter, minValue, maxValue);
        else
            return getLong(defaultParameter, minValue, maxValue);
        }

    /**
     * Use getLongWithMax(...) instead. Searches down through databases to find
     * a given parameter, whose value must be a long >= minValue and = <
     * maxValue. If not, this method returns errValue, else it returns the
     * parameter value. The parameter chosen is marked "used" if it exists.
     * Longs may be in decimal or (if preceded with an X or x) in hexadecimal.
     */
    long getLongWithMax(Parameter parameter, long minValue, long maxValue) 
        {
        if (_exists(parameter)) 
            {
            try
                {
                long i = parseLong(get(parameter));
                if (i < minValue)
                    return minValue - 1;
                if (i > maxValue)
                    return minValue - 1;
                return i;
                } 
            catch (NumberFormatException e) 
                {
                return minValue - 1;
                }
            } 
        else
            return (minValue - 1);
        }

    /**
     * Use getLongWithMax(...) instead. Searches down through databases to find
     * a given parameter, whose value must be a long >= minValue and = <
     * maxValue. If not, this method returns errValue, else it returns the
     * parameter value. The parameter chosen is marked "used" if it exists.
     * Longs may be in decimal or (if preceded with an X or x) in hexadecimal.
     * 
     * @deprecated
     */
    public long getLong(Parameter parameter, Parameter defaultParameter,
                        long minValue, long maxValue) 
        {
        printGotten(parameter, defaultParameter, false);
        return getLongWithMax(parameter, defaultParameter, minValue, maxValue);
        }

    /**
     * Use getLongWithMax(...) instead. Searches down through databases to find
     * a given parameter, whose value must be a long >= minValue and = <
     * maxValue. If not, this method returns errValue, else it returns the
     * parameter value. The parameter chosen is marked "used" if it exists.
     * 
     * @deprecated
     */
    long getLong(Parameter parameter, long minValue, long maxValue) 
        {
        return getLongWithMax(parameter, minValue, maxValue);
        }

    /**
     * Searches down through the databases to find a given parameter, whose
     * value must be an absolute or relative path name. If it is absolute, a
     * File is made based on the path name. If it is relative, a file is made by
     * resolving the path name with respect to the directory in which the file
     * was which defined this ParameterDatabase in the ParameterDatabase
     * hierarchy. If the parameter is not found, this returns null. The File is
     * not checked for validity. The parameter chosen is marked "used" if it
     * exists.
     */

    public File getFile(Parameter parameter, Parameter defaultParameter) 
        {
        printGotten(parameter, defaultParameter, false);
        if (_exists(parameter))
            return getFile(parameter);
        else
            return getFile(defaultParameter);
        }

    /**
     * Searches down through the databases to find a given parameter, whose
     * value must be an absolute or relative path name. If the parameter begins
     * with a "$", a file is made based on the relative path name and returned
     * directly. Otherwise, if it is absolute, a File is made based on the path
     * name, or if it is relative, a file is made by resolving the path name
     * with respect to the directory in which the file was which defined this
     * ParameterDatabase in the ParameterDatabase hierarchy. If the parameter is
     * not found, this returns null. The File is not checked for validity. The
     * parameter chosen is marked "used" if it exists.
     */

    File getFile(Parameter parameter) 
        {
        if (_exists(parameter)) 
            {
            String p = get(parameter);
            if (p == null)
                return null;
            if (p.startsWith(C_HERE))
                return new File(p.substring(C_HERE.length()));
            else {
                File f = new File(p);
                if (f.isAbsolute())
                    return f;
                else
                    return new File(directoryFor(parameter), p);
                }
            } 
        else
            return null;
        }

    /**
     * Searches down through databases to find a given parameter. Returns the
     * parameter's value (trimmed) or null if not found or if the trimmed result
     * is empty. The parameter chosen is marked "used" if it exists.
     */

    public synchronized String getString(Parameter parameter,
                                         Parameter defaultParameter) 
        {
        printGotten(parameter, defaultParameter, false);
        if (_exists(parameter))
            return getString(parameter);
        else
            return getString(defaultParameter);
        }

    /**
     * Searches down through databases to find a given parameter. Returns the
     * parameter's value (trimmed) or null if not found or if the trimmed result
     * is empty. The parameter chosen is marked "used" if it exists.
     */

    /*protected*/ synchronized String getString(Parameter parameter) 
        {
        if (_exists(parameter))
            return get(parameter);
        else
            return null;
        }

    /**
     * Searches down through databases to find a given parameter. Returns the
     * parameter's value trimmed of whitespace, or defaultValue.trim() if the
     * result is not found or the trimmed result is empty.
     */
    public String getStringWithDefault(Parameter parameter,
                                       Parameter defaultParameter, String defaultValue) 
        {
        printGotten(parameter, defaultParameter, false);
        if (_exists(parameter))
            return getStringWithDefault(parameter, defaultValue);
        else
            return getStringWithDefault(defaultParameter, defaultValue);
        }

    /**
     * Searches down through databases to find a given parameter. Returns the
     * parameter's value trimmed of whitespace, or defaultValue.trim() if the
     * result is not found or the trimmed result is empty.
     */
    /*protected*/ String getStringWithDefault(Parameter parameter,
                                              String defaultValue) 
        {
        if (_exists(parameter)) 
            {
            String result = get(parameter);
            if (result == null) 
                {
                if (defaultValue == null)
                    return null;
                else
                    result = defaultValue.trim();
                } 
            else {
                result = result.trim();
                if (result.length() == 0) 
                    {
                    if (defaultValue == null)
                        return null;
                    else
                        result = defaultValue.trim();
                    }
                }
            return result;
            } 
        else {
            if (defaultValue == null)
                return null;
            else
                return defaultValue.trim();
            }
        }

    /** Clears the checked flag */
    /*protected*/ synchronized void uncheck() 
        {
        if (!checked)
            return; // we already unchecked this path -- this is dangerous if
                    // parents are used without children
        checked = false;
        int size = parents.size();
        for (int x = 0; x < size; x++)
            ((ParameterDatabase) (parents.elementAt(x))).uncheck();
        }
    
    /**
     * @param l
     */
    public synchronized void addListener(ParameterDatabaseListener l) 
        {
        listeners.add(l);
        }
    
    /**
     * @param l
     */
    public synchronized void removeListener(ParameterDatabaseListener l) 
        {
        listeners.remove(l);
        }
    
    /**
     * Fires a parameter set event.
     * 
     * @param parameter
     * @param value
     */
    public synchronized void fireParameterSet(
        Parameter parameter, String value) 
        {
        Iterator it = listeners.iterator();
        
        while (it.hasNext()) 
            {
            ParameterDatabaseListener l = (ParameterDatabaseListener)it.next();
            l.parameterSet(new ParameterDatabaseEvent(this,parameter,value, ParameterDatabaseEvent.SET));
            }
        }

    /**
     * Fires a parameter accessed event.
     * 
     * @param parameter
     * @param value
     */
    public synchronized void fireParameterAccessed(
        Parameter parameter, String value) 
        {
        Iterator it = listeners.iterator();
        
        while (it.hasNext()) 
            {
            ParameterDatabaseListener l = (ParameterDatabaseListener)it.next();
            l.parameterSet(new ParameterDatabaseEvent(this,parameter,value, ParameterDatabaseEvent.ACCESSED));
            }
        }

    /**
     * Sets a parameter in the topmost database to a given value, trimmed of
     * whitespace.
     */
    public synchronized void set(Parameter parameter, String value) 
        {
        String tmp = value.trim();
        put(parameter.param, tmp);
        fireParameterSet(parameter, tmp);
        }

    /**
     * Prints out all the parameters marked as used, plus their values. If a
     * parameter was listed as "used" but not's actually in the database, the
     * value printed is UNKNOWN_VALUE (set to "?????")
     */

    public synchronized void listGotten(PrintWriter p) 
        {
        Vector vec = new Vector();
        Enumeration e = gotten.keys();
        while (e.hasMoreElements())
            vec.addElement(e.nextElement());

        // sort the keys
        Object[] array = new Object[vec.size()];
        vec.copyInto(array);

        java.util.Collections.sort(vec);

        // Uncheck and print each item
        for (int x = 0; x < array.length; x++) 
            {
            String s = (String) (array[x]);
            String v = null;
            if (s != null) 
                {
                v = (String) (_get(s));
                uncheck();
                }
            if (v == null)
                v = UNKNOWN_VALUE;
            p.println(s + " = " + v);
            }
        p.flush();
        }

    /** Prints out all the parameters NOT marked as used, plus their values. */

    public synchronized void listNotGotten(PrintWriter p) 
        {
        Vector vec = new Vector();

        Hashtable all = new Hashtable();
        _list(null, false, null, all); // grab all the nonshadowed keys
        Enumeration e = gotten.keys();
        while (e.hasMoreElements())
            all.remove(e.nextElement());
        e = all.keys();
        while (e.hasMoreElements())
            vec.addElement(e.nextElement());

        // sort the keys
        Object[] array = new Object[vec.size()];
        vec.copyInto(array);

        java.util.Collections.sort(vec);

        // Uncheck and print each item
        for (int x = 0; x < array.length; x++) 
            {
            String s = (String) (array[x]);
            String v = null;
            if (s != null) 
                {
                v = (String) (_get(s));
                uncheck();
                }
            if (v == null)
                v = UNKNOWN_VALUE;
            p.println(s + " = " + v);
            }
        p.flush();
        }

    /** Prints out all the parameters NOT marked as used, plus their values. */

    public synchronized void listNotAccessed(PrintWriter p) 
        {
        Vector vec = new Vector();

        Hashtable all = new Hashtable();
        _list(null, false, null, all); // grab all the nonshadowed keys
        Enumeration e = accessed.keys();
        while (e.hasMoreElements())
            all.remove(e.nextElement());
        e = all.keys();
        while (e.hasMoreElements())
            vec.addElement(e.nextElement());

        // sort the keys
        Object[] array = new Object[vec.size()];
        vec.copyInto(array);

        java.util.Collections.sort(vec);

        // Uncheck and print each item
        for (int x = 0; x < array.length; x++) 
            {
            String s = (String) (array[x]);
            String v = null;
            if (s != null) 
                {
                v = (String) (_get(s));
                uncheck();
                }
            if (v == null)
                v = UNKNOWN_VALUE;
            p.println(s + " = " + v);
            }
        p.flush();
        }

    /**
     * Prints out all the parameters marked as accessed ("gotten" by some
     * getFoo(...) method), plus their values. If this method ever prints
     * UNKNOWN_VALUE ("?????"), that's a bug.
     */

    public synchronized void listAccessed(PrintWriter p) 
        {
        Vector vec = new Vector();
        Enumeration e = accessed.keys();
        while (e.hasMoreElements())
            vec.addElement(e.nextElement());

        // sort the keys
        Object[] array = new Object[vec.size()];
        vec.copyInto(array);

        java.util.Collections.sort(vec);

        // Uncheck and print each item
        for (int x = 0; x < array.length; x++) 
            {
            String s = (String) (array[x]);
            String v = null;
            if (s != null) 
                {
                v = (String) (_get(s));
                uncheck();
                }
            if (v == null)
                v = UNKNOWN_VALUE;
            p.println(s + " = " + v);
            }
        p.flush();
        }

    /** Returns true if parameter exist in the database */
    public synchronized boolean exists(Parameter parameter) 
        {
        printGotten(parameter, null, true);
        return _exists(parameter);
        }


    /*protected*/ synchronized boolean _exists(Parameter parameter) 
        {
        if (parameter == null) return false;
        String result = _get(parameter.param);
        uncheck();
        
        accessed.put(parameter.param, Boolean.TRUE);
        return (result != null);
        }

    /**
     * Returns true if either parameter or defaultParameter exists in the
     * database
     */
    public synchronized boolean exists(Parameter parameter,
                                       Parameter defaultParameter) 
        {
        printGotten(parameter, defaultParameter, true);
        if (exists(parameter))
            return true;
        if (exists(defaultParameter))
            return true;
        return false;
        }


/*
  P: Successfully retrieved parameter
  !P: Unsuccessfully retrieved parameter
  <P: Would have retrieved parameter

  E: Successfully tested for existence of parameter
  !E: Unsuccessfully tested for existence of parameter
  <E: Would have tested for exidstence of parameter
*/

    /*protected*/ void printGotten(Parameter parameter, Parameter defaultParameter, boolean exists)
        {
        if (printState == PS_UNKNOWN)
            {
            Parameter p = new Parameter(PRINT_PARAMS);
            String jp = get(p);
            if (jp == null || jp.equalsIgnoreCase("false"))
                printState = PS_NONE;
            else
                printState = PS_PRINT_PARAMS;
            uncheck();
            printGotten(p,null,false);
            }

        if (printState == PS_PRINT_PARAMS)
            {
            String p = "P: ";
            if (exists) p = "E: ";
            
            if (parameter==null && defaultParameter == null) 
                return;
                
            else if (parameter == null)
                {
                String result = _get(defaultParameter.param);
                uncheck();
                if (result == null)
                    // null parameter, didn't find defaultParameter
                    System.err.println("\t!" + p +defaultParameter.param);
                else 
                    // null parameter, found defaultParameter
                    System.err.println("\t " + p +defaultParameter.param + " = " + result);
                }
            
            else if (defaultParameter == null)
                {
                String result = _get(parameter.param);
                uncheck();
                if (result == null)
                    // null defaultParameter, didn't find parameter
                    System.err.println("\t!" + p +parameter.param);
                else 
                    // null defaultParameter, found parameter
                    System.err.println("\t " + p +parameter.param+ " = " + result);
                }
            
            else
                {
                String result = _get(parameter.param);
                uncheck();
                if (result == null)
                    {
                    // didn't find parameter
                    System.err.println("\t!" + p +parameter.param);
                    result = _get(defaultParameter.param);
                    uncheck();
                    if (result == null)
                        // didn't find defaultParameter
                        System.err.println("\t!" + p +defaultParameter.param);
                    else 
                        // found defaultParameter
                        System.err.println("\t " + p +defaultParameter.param+ " = " + result);
                    }
                else 
                    {
                    // found parameter
                    System.err.println("\t " + p +parameter.param+ " = " + result);
                    System.err.println("\t<" + p +defaultParameter.param);
                    }
                }
            }
        }

    /*protected*/ synchronized String get(Parameter parameter) 
        {
        String result = _get(parameter.param);
        uncheck();

        // set hashtable appropriately
        if (parameter != null)
            accessed.put(parameter.param, Boolean.TRUE);
        if (parameter != null)
            gotten.put(parameter.param, Boolean.TRUE);
        return result;
        }

    /** Private helper function */
    synchronized String _get(String parameter) 
        {
        if (parameter == null)
            {
            return null;
            }
        if (checked)
            return null; // we already searched this path
        checked = true;
        String result = getProperty(parameter);
        if (result == null) 
            {
            int size = parents.size();
            for (int x = 0; x < size; x++) 
                {
                result = ((ParameterDatabase) (parents.elementAt(x)))._get(parameter);
                if (result != null)
                    {
                    return result;
                    }
                }
            } 
        else  // preprocess
            {
            result = result.trim();
            if (result.length() == 0)
                result = null;
            }
        return result;
        }

    /*protected*/ Set _getShadowedValues(Parameter parameter, Set vals) 
        {
        if (parameter == null) 
            {
            return vals;
            }
        
        if (checked) 
            {
            return vals;
            }
        
        checked = true;
        String result = getProperty(parameter.param);
        if (result != null) 
            {
            result = result.trim();
            if (result.length() != 0)
                vals.add(result);
            }
        
        int size = parents.size();
        for (int i = 0; i < size; ++i) 
            {
            ((ParameterDatabase)parents.elementAt(i))._getShadowedValues(parameter,vals);
            }

        return vals;
        }

    public Set getShadowedValues(Parameter parameter) 
        {
        Set vals = new LinkedHashSet();
        vals = _getShadowedValues(parameter, vals);
        uncheck();
        return vals;
        }
    
    /**
     * Searches down through databases to find the directory for the database
     * which holds a given parameter. Returns the directory name or null if not
     * found.
     */

    public File directoryFor(Parameter parameter) 
        {
        File result = _directoryFor(parameter);
        uncheck();
        return result;
        }
    
    /** Private helper function */
    synchronized File _directoryFor(Parameter parameter) 
        {
        if (checked)
            return null; // we already searched this path
        checked = true;
        File result = null;
        String p = getProperty(parameter.param);
        if (p == null) 
            {
            int size = parents.size();
            for (int x = 0; x < size; x++) 
                {
                result = ((ParameterDatabase) (parents.elementAt(x)))
                    ._directoryFor(parameter);
                if (result != null)
                    return result;
                }
            return result;
            } 
        else
            return directory;
        }
    
    /**
     * Searches down through databases to find the parameter file 
     * which holds a given parameter. Returns the filename or null if not
     * found.
     */

    public File fileFor(Parameter parameter) 
        {
        File result = _fileFor(parameter);
        uncheck();
        return result;
        }

    synchronized File _fileFor(Parameter parameter) 
        {
        if (checked)
            return null;
        
        checked = true;
        File result = null;
        String p = getProperty(parameter.param);
        if (p==null) 
            {
            int size = parents.size();
            for(int i = 0; i < size; ++i) 
                {
                result = ((ParameterDatabase)parents.elementAt(i))._fileFor(parameter);
                if (result != null)
                    return result;
                }
            return result;
            }
        else
            return new File(directory,filename);
        }

    /** Removes a parameter from the topmost database. */
    public synchronized void remove(Parameter parameter) 
        {
        if (parameter.param.equals(PRINT_PARAMS)) printState = PS_UNKNOWN;
        remove(parameter.param);
        }

    /** Removes a parameter from the database and all its parent databases. */
    public synchronized void removeDeeply(Parameter parameter) 
        {
        _removeDeeply(parameter);
        uncheck();
        }

    /** Private helper function */
    synchronized void _removeDeeply(Parameter parameter) 
        {
        if (checked)
            return; // already removed from this path
        checked = true;
        remove(parameter);
        int size = parents.size();
        for (int x = 0; x < size; x++)
            ((ParameterDatabase) (parents.elementAt(x)))
                .removeDeeply(parameter);
        }

    /** Creates an empty parameter database. */
    public ParameterDatabase() 
        {
        super();
        accessed = new Hashtable();
        gotten = new Hashtable();
        directory = new File(new File("").getAbsolutePath()); // uses the user
                                                              // path
        filename = "";
        parents = new Vector();
        checked = false; // unnecessary
        listeners = new Vector();
        }
    
    /** Creates a new parameter database from the given Dictionary.  
        Both the keys and values will be run through toString() before adding to the dataase.   
        Keys are parameters.  Values are the values of the parameters.  
        Beware that a ParameterDatabase is itself a Dictionary; but if you pass one in here you 
        will only get the lowest-level elements.  If parent.n are defined, parents will 
        be attempted to be loaded -- that's the reason for the FileNotFoundException and IOException.  */
    public ParameterDatabase(java.util.Dictionary map) throws FileNotFoundException, IOException 
        {
        this();
        java.util.Enumeration keys = map.keys();
        while(keys.hasMoreElements())
            {
            Object obj = keys.nextElement();
            set(new Parameter(""+obj),""+map.get(obj));
            }

        // load parents
        for (int x = 0;; x++) 
            {
            String s = getProperty("parent." + x);
            if (s == null)
                return; // we're done

            if (new File(s).isAbsolute()) // it's an absolute file definition
                parents.addElement(new ParameterDatabase(new File(s)));
            else throw new FileNotFoundException("Attempt to load a relative file, but there's no parent file: " + s);
            }
        }

    /** Creates a new parameter database loaded from the given string describing a file in a jar,
        in the context of a resource location (a class).
        This approach uses resourceLocation.getResourceAsStream() to load the parameter file.
        If parent.n are defined, parents will be attempted to be loaded -- that's 
        the reason for the FileNotFoundException and IOException. */

    public ParameterDatabase(String pathNameInJar, Class jarResourceLocation) throws FileNotFoundException, IOException 
        {
        this();
        load(jarResourceLocation.getResourceAsStream(pathNameInJar));

        listeners = new Vector();

        // load parents
        for (int x = 0;; x++) 
            {
            String s = getProperty("parent." + x);
            if (s == null)
                return; // we're done

            String path = new File(new File(pathNameInJar).getParent(), s).toString();

            parents.addElement(new ParameterDatabase(path, jarResourceLocation));
            }
        }


    /** Creates a new parameter database loaded from the given stream.  Non-relative parents are not permitted.
        If parent.n are defined, parents will be attempted to be loaded -- that's 
        the reason for the FileNotFoundException and IOException. */

    public ParameterDatabase(java.io.InputStream stream) throws FileNotFoundException, IOException 
        {
        this();
        load(stream);

        listeners = new Vector();

        // load parents
        for (int x = 0;; x++) 
            {
            String s = getProperty("parent." + x);
            if (s == null)
                return; // we're done

            if (new File(s).isAbsolute()) // it's an absolute file definition
                parents.addElement(new ParameterDatabase(new File(s)));
            else throw new FileNotFoundException("Attempt to load a relative file, but there's no parent file: " + s);
            }
        }


    /**
     * Creates a new parameter database tree from a given database file and its
     * parent files.
     */
    public ParameterDatabase(File filename)
        throws FileNotFoundException, IOException 
        {
        this();
        this.filename = filename.getName();
        directory = new File(filename.getParent()); // get the directory
                                                    // filename is in
        load(new FileInputStream(filename));

        listeners = new Vector();

        // load parents
        for (int x = 0;; x++) 
            {
            String s = getProperty("parent." + x);
            if (s == null)
                return; // we're done

            if (new File(s).isAbsolute()) // it's an absolute file definition
                parents.addElement(new ParameterDatabase(new File(s)));
            else
                // it's relative to my path
                parents.addElement(new ParameterDatabase(new File(filename
                                                                  .getParent(), s)));
            }
        }

    /**
     * Creates a new parameter database from a given database file and argv
     * list. The top-level database is completely empty, pointing to a second
     * database which contains the parameter entries stored in args, which
     * points to a tree of databases constructed using
     * ParameterDatabase(filename).
     */

    public ParameterDatabase(File filename, String[] args)
        throws FileNotFoundException, IOException 
        {
        this();
        this.filename = filename.getName();
        directory = new File(filename.getParent()); // get the directory
                                                    // filename is in

        // Create the Parameter Database tree for the files
        ParameterDatabase files = new ParameterDatabase(filename);

        // Create the Parameter Database for the arguments
        ParameterDatabase a = new ParameterDatabase();
        a.parents.addElement(files);
        for (int x = 0; x < args.length - 1; x++) 
            {
            if (args[x].equals("-p"))
                a.parseParameter(args[x + 1]);
            }

        // Set me up
        parents.addElement(a);
        listeners = new Vector();
        }

    /**
     * Parses and adds s to the database. Returns true if there was actually
     * something to parse.
     */
    boolean parseParameter(String s) 
        {
        s = s.trim();
        if (s.length() == 0)
            return false;
        if (s.charAt(0) == '#')
            return false;
        int eq = s.indexOf('=');
        if (eq < 0)
            return false;
        put(s.substring(0, eq), s.substring(eq + 1));
        return true;
        }

    /**
     * Prints out all the parameters in the database. Useful for debugging. If
     * listShadowed is true, each parameter is printed with the parameter
     * database it's located in. If listShadowed is false, only active
     * parameters are listed, and they're all given in one big chunk.
     */
    public void list(PrintStream p, boolean listShadowed) 
        {
        list(new PrintWriter(p), listShadowed);
        }

    /**
     * Prints out all the parameters in the database, but not shadowed
     * parameters.
     */
    public void list(PrintStream p) 
        {
        list(new PrintWriter(p), false);
        }

    /**
     * Prints out all the parameters in the database, but not shadowed
     * parameters.
     */
    public void list(PrintWriter p) 
        {
        list(p, false);
        }

    /**
     * Prints out all the parameters in the database. Useful for debugging. If
     * listShadowed is true, each parameter is printed with the parameter
     * database it's located in. If listShadowed is false, only active
     * parameters are listed, and they're all given in one big chunk.
     */
    public void list(PrintWriter p, boolean listShadowed) 
        {
        if (listShadowed)
            _list(p, listShadowed, "root", null);
        else {
            Hashtable gather = new Hashtable();
            _list(p, listShadowed, "root", gather);

            Vector vec = new Vector();
            Enumeration e = gather.keys();
            while (e.hasMoreElements())
                vec.addElement(e.nextElement());

            // sort the keys
            Object[] array = new Object[vec.size()];
            vec.copyInto(array);

            java.util.Collections.sort(vec);

            // Uncheck and print each item
            for (int x = 0; x < array.length; x++) 
                {
                String s = (String) (array[x]);
                String v = null;
                if (s != null)
                    v = (String) gather.get(s);
                if (v == null)
                    v = UNKNOWN_VALUE;
                p.println(s + " = " + v);
                }
            }
        p.flush();
        }

    /** Private helper function. */
    void _list(PrintWriter p, boolean listShadowed,
               String prefix, Hashtable gather) 
        {
        if (listShadowed) 
            {
            // Print out my header
            p.println("\n########" + prefix);
            super.list(p);
            int size = parents.size();
            for (int x = 0; x < size; x++)
                ((ParameterDatabase) (parents.elementAt(x)))._list(p,
                                                                   listShadowed, prefix + "." + x, gather);
            } 
        else {
            // load in reverse order so things get properly overwritten
            int size = parents.size();
            for (int x = size - 1; x >= 0; x--)
                ((ParameterDatabase) (parents.elementAt(x)))._list(p,
                                                                   listShadowed, prefix, gather);
            Enumeration e = keys();
            while (e.hasMoreElements()) 
                {
                String key = (String) (e.nextElement());
                gather.put(key, get(key));
                }
            }
        p.flush();
        }

    public String toString() 
        {
        String s = super.toString();
        if (parents.size() > 0) 
            {
            s += " : (";
            for (int x = 0; x < parents.size(); x++) 
                {
                if (x > 0)
                    s += ", ";
                s += parents.elementAt(x);
                }
            s += ")";
            }
        return s;
        }

    /**
     * Builds a TreeModel from the available property keys.   
     * @return
     */
    public TreeModel buildTreeModel() 
        {
        String sep = System.getProperty("file.separator");
        ParameterDatabaseTreeNode root = new ParameterDatabaseTreeNode(
            this.directory.getAbsolutePath() + sep + this.filename);
        ParameterDatabaseTreeModel model = new ParameterDatabaseTreeModel(root);

        _buildTreeModel(model, root);

        model.sort(root, new Comparator() 
            {
            public int compare(Object o1, Object o2) 
                {
                ParameterDatabaseTreeNode t1 = (ParameterDatabaseTreeNode)o1;
                ParameterDatabaseTreeNode t2 = (ParameterDatabaseTreeNode)o2;
                
                return ((Comparable)t1.getUserObject()).compareTo(t2.getUserObject());
                }
            });

        // In order to add elements to the tree model, the leaves need to be
        // visible. This is because some properties have values *and* sub-
        // properties. Otherwise, if the nodes representing these properties did
        // not yet have children, then they would be invisible and the tree model
        // would be unable to add child nodes to them.
        model.setVisibleLeaves(false);
        
//        addListener(new ParameterDatabaseAdapter() {
//           public void parameterSet(ParameterDatabaseEvent evt) {
//               model.setVisibleLeaves(true);
//               _addNodeForParameter(model, root, evt.getParameter().param);
//               model.setVisibleLeaves(false);
//           }
//        });

        return model;
        }

    void _buildTreeModel(DefaultTreeModel model,
                         DefaultMutableTreeNode root) 
        {
        Enumeration e = keys();
        while (e.hasMoreElements()) 
            {
            _addNodeForParameter(model, root, (String)e.nextElement());
            }

        int size = parents.size();
        for (int i = 0; i < size; ++i) 
            {
            ParameterDatabase parentDB = (ParameterDatabase) parents
                .elementAt(i);
            parentDB._buildTreeModel(model, root);
            }
        }

    /**
     * @param model
     * @param root
     * @param e
     */
    void _addNodeForParameter(DefaultTreeModel model, DefaultMutableTreeNode root, String key) 
        {
        if (key.indexOf("parent.") == -1) 
            {
            /* 
             * TODO split is new to 1.4.  To maintain 1.2 compatability we need
             * to use a different approach.  Just use a string tokenizer.
             */ 
            StringTokenizer tok = new StringTokenizer(key,".");
            String[] path = new String[tok.countTokens()];
            int t = 0;
            while(tok.hasMoreTokens()) 
                {
                path[t++] = tok.nextToken();
                }
            DefaultMutableTreeNode parent = root;

            for (int i = 0; i < path.length; ++i) 
                {
                int children = model.getChildCount(parent);
                if (children > 0) 
                    {
                    int c = 0;
                    for (; c < children; ++c) 
                        {
                        DefaultMutableTreeNode child = 
                            (DefaultMutableTreeNode) parent.getChildAt(c);
                        if (child.getUserObject().equals(path[i])) 
                            {
                            parent = child;
                            break;
                            }
                        }

                    if (c == children) 
                        {
                        DefaultMutableTreeNode child = 
                            new ParameterDatabaseTreeNode(path[i]);
                        model.insertNodeInto(child, parent, 
                                             parent.getChildCount());
                        parent = child;
                        }
                    }
                // If the parent has no children, just add the node.
                else {
                    DefaultMutableTreeNode child = 
                        new ParameterDatabaseTreeNode(path[i]);
                    model.insertNodeInto(child, parent, 0);
                    parent = child;
                    }
                }
            }
        }
    
    /** Test the ParameterDatabase */
    public static void main(String[] args)
        throws FileNotFoundException, IOException 
        {
        ParameterDatabase pd = new ParameterDatabase(new File(args[0]), args);
        pd.set(new Parameter("Hi there"), "Whatever");
        pd.set(new Parameter(new String[] { "1", "2", "3" }), " Whatever ");
        pd.set(new Parameter(new String[] { "a", "b", "c" }).pop().push("d"),
               "Whatever");

        System.err.println("\n\n PRINTING ALL PARAMETERS \n\n");
        pd.list(new PrintWriter(System.err, true), true);
        System.err.println("\n\n PRINTING ONLY VALID PARAMETERS \n\n");
        pd.list(new PrintWriter(System.err, true), false);
        }

    }

