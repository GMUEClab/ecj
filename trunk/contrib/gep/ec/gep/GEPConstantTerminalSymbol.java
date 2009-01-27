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
 * GEPConstantTerminalSymbol.java
 * 
 * Created: Nov. 10, 2006
 * By: Bob Orchard
 */


public class GEPConstantTerminalSymbol extends GEPTerminalSymbol
{
	/** The Constant Teriminal Symbol is special ... no values to set and has the label "?" 
	 *  It gets its constant value from the Dc part of the gene when the expression is parsed
	 *  and this value is stored in a special constant expression tree node. So this Symbol
	 *  is a placeholder for constants in the gene (Karva) expressions.
	 *  The constant values are assigned to the constant tree nodes from the Dc portion of the gene
	 *  in the order they are encountered in the Karva expression.
	 */
	
	
	public GEPConstantTerminalSymbol( )
	{
		super( null );  // sets arity to 0;
		symbol = "?";
	}
	
}
