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
 * SemanticNode.java
 *
 */

/**
 * @author James McDermott
 */

public class SemanticExtra extends SemanticNode
    {
    char value;
    int index;
    public SemanticExtra(char v, int i) { value = v; index = i; }
    public char value() { return value; }
    public int index() { return index; }
    }
