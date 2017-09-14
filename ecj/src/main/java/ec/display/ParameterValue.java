/*
  Copyright 2006 by Sean Paus
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


/*
 * Created on Apr 19, 2005 10:30:19 PM
 * 
 * By: spaus
 */
package ec.display;

/**
 * @author spaus
 */
public class ParameterValue 
    {
    
    final String value;
    
    public ParameterValue(String value) 
        {
        this.value = value;
        }
    
    public String getValue() 
        {
        return value;
        }
    
    public String toString() 
        {
        return "*"+value+"*";
        }
    }
