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

import java.io.*;

/* 
 * GEPSymbol.java
 * 
 * Created: Nov. 10, 2006
 * By: Bob Orchard
 */

/**
 * The abstract class form which GEPTerminalSymbol, GEPFunctionSymbol 
 * and GEPConstantTerminalSymbol are derived. They encode terminals 
 * (varibles), functions and constants in the models produced.
 */


public abstract class GEPSymbol implements Serializable
{
	/** 
	 * The weight accorded to the symbol. Used to determine how the symbols are to be
	 * allocated proportionally in genes
	 */
	public int weight = 1; // default weight is 1
	
	/** 
	 * The arity of the symbol (0 for Terminal symbols and 1 or more for Function symbols)
	 */
	public int arity;
	
	/** The id associated with this symbol in the symbol set it is associated with */
	public int id;
	
	/** 
	 * The symbol assocaited with the function or terminal. For functions we will
	 * see '+', '-', '/', 'sqrt', etc. and for terminals we will see 'x', 'y', etc.
	 */
	public String symbol;
	
    public String toString() 
    { return symbol; }
    

}
