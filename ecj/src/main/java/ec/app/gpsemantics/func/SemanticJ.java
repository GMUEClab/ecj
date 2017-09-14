/*
  Copyright 2012 by James McDermott
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package ec.app.gpsemantics.func;
import ec.*;
import ec.gp.*;
import ec.util.*;

/*
 * SemanticJ.java
 *
 */

/**
 * @author James McDermott
 */

public class SemanticJ extends SemanticNode
    {
    public String toString() { return "J"; }
    public int expectedChildren() { return 2; }

    public int index() { return -1; }
    public char value() { return 'J'; }
    }
