/*
  Copyright 2012 by James McDermott
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.app.royaltree.func;
import ec.*;
import ec.app.lid.*;
import ec.gp.*;
import ec.util.*;

/*
 * RoyalTreeA.java
 *
 */

/**
 * @author James McDermott
 */

public class RoyalTreeA extends RoyalTreeNode
    {
    public int expectedChildren() { return 1; }
    public char value() { return 'A'; }
    }
