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
import ec.util.Parameter;
import ec.*;

/* 
 * GEPDefaults.java
 * 
 * Created: Mon Nov 6, 2006
 * By: Bob Orchard
 */

/**
 * A static class that returns the base for "default values" which GEP-style
 * operators use, rather than making the user specify them all on a per-
 * species basis.
 *
 * @author Bob Orchard
 * @version 1.0 
 */

public final class GEPDefaults implements DefaultsForm
{
    public static final String P_GEP = "gep";

    /** Returns the default base. */
    public static final Parameter base()
    {
        return new Parameter(P_GEP);
    }
}
