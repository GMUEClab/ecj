/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec;

/* 
 * DefaultsForm.java
 * 
 * Created: Fri Jan 21 15:14:01 2000
 * By: Sean Luke
 */

/**
 * DefaultsForm is the interface which describes how Defaults objects
 * should work.  In general there is one Defaults class for each
 * package (there doesn't have to be, but it would be nice).  This
 * class should be relatively uniquely named (the defaults class in
 * the GP package is called GPDefaults for example).
 * DefaultsForm objects should implement a single static final method:

 <p><tt>public final Parameter base();</tt>

 <p>...which returns the default parameter base for the package.  This
 method cannot be declared in this interface, however, because it is
 static.  :-)  So this interface isn't much use, except to describe how
 defaults objects should generally work.

 <p> A parameter base is a secondary "default" place for the parameters database to look
 for a parameter value if the primary value was not defined.

 *
 * @author Sean Luke
 * @version 1.0 
 */

public interface DefaultsForm 
    {
    }
