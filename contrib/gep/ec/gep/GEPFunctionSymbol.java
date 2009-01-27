/*
 * Copyright (c) 2006 by National Research Council of Canada.
 *
 * This software is the confidential and proprietary information of
 * the National Research Council of Canada ("Confidential Information").
 * You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into
 * with the National Research Council of Canada.
 *
 * THE NATIONAL RESEARCH COUNCIL OF CANADA MAKES NO REPRESENTATIONS OR
 * WARRANTIES ABOUT THE SUITABILITY OF THE SOFTWARE, EITHER EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, OR NON-INFRINGEMENT.
 * THE NATIONAL RESEARCH COUNCIL OF CANADA SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
 *
 *
 */

package ec.gep;

/* 
 * GEPTerminalSymbol.java
 * 
 * Created: Nov. 10, 2006
 * By: Bob Orchard
 *
 */

 /**
  * Supports encoding the information about a function: its arity (number
  * of arguments) and its name (symbol).
  */


public abstract class GEPFunctionSymbol extends GEPSymbol 
{	
	public GEPFunctionSymbol( String symbolName, int numArgs) 
	{
		arity = numArgs; // must be > 0
			
		symbol = symbolName;
	}

	/** 
	 * Each defined function symbol must provide the appropriate method for 
	 * calculating its value given the set of paramters.
	 *  
	 * @param parameters values used to calculate the value of the function
	 * @return the calculated value
	 */
    public abstract double eval(double parameters[]);
    
    public abstract boolean isLogicalFunction();
    
    public abstract String getMathExpressionAsString( String params[] );

}
