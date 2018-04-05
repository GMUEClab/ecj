/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.neat;

import java.text.*;
import ec.*;
import ec.util.*;
import ec.vector.*;

/**
 * NEATGene is the combination of class Gene and class Link in original code. It
 * is used to represent a single connection between two nodes (NEATNode) of a
 * neural network, and extends the abstract Gene class to make use of its
 * read/write utilities.
 * 
 * @author Ermo Wei and David Freelan
 *
 */
public class NEATGene extends Gene
    {
    public final static String P_GENE = "gene";

    /** The weight of link this gene is represent. */
    public double weight;

    /** The actual in node this gene connect to. */
    public NEATNode inNode;

    /** The actual out node this gene connect to. */
    public NEATNode outNode;

    /**
     * The id of the in node, this is useful in reading a gene from file, we
     * will use this id to find the actual node after we finish reading the
     * genome file.
     */
    public int inNodeId;

    /**
     * The id of the in node, this is useful in reading a gene from file, we
     * will use this id to find the actual node after we finish reading the
     * genome file.
     */
    public int outNodeId;

    /** Is the link this gene represent a recurrent link. */
    public boolean isRecurrent;

    /** Time delay of the link, used in network activation. */
    public boolean timeDelay;

    /** The innovation number of this link. */
    public int innovationNumber;

    /**
     * The mutation number of this gene, Used to see how much mutation has
     * changed.
     */
    public double mutationNumber;

    /** Is the link this gene represent is enable in network activation. */
    public boolean enable;

    /**
     * Is this gene frozen, a frozen gene's weight cannot get mutated in
     * breeding procedure.
     */
    public boolean frozen;

    /**
     * The setup method initializes a "meaningless" gene that does not specify
     * any connection.
     */
    public void setup(EvolutionState state, Parameter base)
        {
        weight = 0.0;
        // node id 1-indexed
        inNodeId = 0;
        outNodeId = 0;
        inNode = null;
        outNode = null;
        isRecurrent = false;
        innovationNumber = 0;
        mutationNumber = 0.0;
        timeDelay = false;
        enable = true;
        frozen = false;
        }

    public Parameter defaultBase()
        {
        return NEATDefaults.base().push(P_GENE);
        }

    @Override
    public void reset(EvolutionState state, int thread)
        {
        // frozen and timeDelay are not read from template genome, we set it
        // here
        frozen = false;
        timeDelay = false;
        }

    /** Reset the gene with given parameters. */
    public void reset(double w, int iNodeId, int oNodeId, boolean recur, int innov, double mutNum)
        {
        // Gene::Gene(double w, NNode *inode, NNode *onode, bool recur, double
        // innov, double mnum)
        weight = w;
        inNodeId = iNodeId;
        outNodeId = oNodeId;
        inNode = null;
        outNode = null;
        isRecurrent = recur;
        innovationNumber = innov;
        mutationNumber = mutNum;
        timeDelay = false;
        enable = true;
        frozen = false;
        }

    @Override
    public Object clone()
        {
        // Gene::Gene(Gene *g,Trait *tp,NNode *inode,NNode *onode)
        // we do not clone the inNode and outNode instance
        NEATGene myobj = (NEATGene) (super.clone());
        myobj.weight = weight;
        myobj.isRecurrent = isRecurrent;
        myobj.inNodeId = inNodeId;
        myobj.outNodeId = outNodeId;
        myobj.innovationNumber = innovationNumber;
        myobj.mutationNumber = mutationNumber;
        myobj.enable = enable;
        myobj.frozen = frozen;
        myobj.timeDelay = timeDelay;

        return myobj;
        }

    public String printGeneToStringForHumans()
        {
        return printGeneToString();
        }

    /**
     * This method convert the gene in to human readable format. It can be
     * useful in debugging.
     */
    public String toString()
        {
        StringBuffer stringBuffer = new StringBuffer();
        String mask03 = " 0.00000000000000000;-0.00000000000000000";
        DecimalFormat fmt03 = new DecimalFormat(mask03);

        String mask5 = " 0000";
        DecimalFormat fmt5 = new DecimalFormat(mask5);

        stringBuffer.append("\n [Link (" + fmt5.format(inNode.nodeId));
        stringBuffer.append("," + fmt5.format(outNode.nodeId));
        stringBuffer.append("]  innov (" + fmt5.format(innovationNumber));

        stringBuffer.append(", mut=" + fmt03.format(mutationNumber) + ")");
        stringBuffer.append(" Weight " + fmt03.format(weight));

       

        if (!enable)
            stringBuffer.append(" -DISABLED-");

        if (isRecurrent)
            stringBuffer.append(" -RECUR-");

        return stringBuffer.toString();
        }

    /**
     * This method is used to output a gene that is same as the format in start
     * genome file.
     */
    public String printGeneToString()
        {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(Code.encode(inNode.nodeId));
        stringBuilder.append(Code.encode(outNode.nodeId));
        stringBuilder.append(Code.encode(weight));
        stringBuilder.append(Code.encode(isRecurrent));
        stringBuilder.append(Code.encode(innovationNumber));
        stringBuilder.append(Code.encode(mutationNumber));
        stringBuilder.append(Code.encode(enable));

        return stringBuilder.toString();
        }

    /**
     * This method is used to read a gene in start genome from file. Example :
     * i1|i4|d0|0.0|Fi1|d0|0.0|T have these parts : i1 i4 d0|0.0 F i1 d0|0.0 T
     * which are: inNode outNode weight isRecurrent innovationNumber
     * mutationNumber enable
     */
    public void readGeneFromString(String string, EvolutionState state)
        {
        // Gene::Gene(const char *argline, std::vector<Trait*> &traits,
        // std::vector<NNode*> &nodes)
        DecodeReturn dr = new DecodeReturn(string);
        Code.decode(dr);
        inNodeId = (int) dr.l;
        Code.decode(dr);
        outNodeId = (int) dr.l;
        Code.decode(dr);
        weight = dr.d;
        Code.decode(dr);
        isRecurrent = (dr.l == (long) 1);
        Code.decode(dr);
        innovationNumber = (int) dr.l;
        Code.decode(dr);
        mutationNumber = dr.d;
        Code.decode(dr);
        enable = (dr.l == (long) 1);
        }

    /**
     * "Placeholder" method for generating a hashcode. The algorithm is stolen
     * from GPIndividual and modified a bit to use NEATGene's variables. It is
     * by no means "good" and is subject for improvement.
     */
    public int hashCode()
        {
        int hash = innovationNumber;
        hash = (hash * 31 + 17 + inNodeId);
        hash = (hash * 31 + 17 + outNodeId);
        hash = (hash * 31 + 17 + Float.floatToIntBits((float)weight));
        hash = (hash * 31 + 17 + Float.floatToIntBits((float)mutationNumber));
        if (enable) hash = (hash * 31 + 17);
        if (isRecurrent) hash = (hash * 31 + 13);  // different value
        return hash;
        }

    @Override
    public boolean equals(Object o)
        {
        NEATGene g = (NEATGene) o;
        if (inNodeId != g.inNodeId)
            return false;
        if (outNodeId != g.outNodeId)
            return false;
        if (weight != g.weight)
            return false;
        if (isRecurrent != g.isRecurrent)
            return false;
        if (innovationNumber != g.innovationNumber)
            return false;
        if (mutationNumber != g.mutationNumber)
            return false;
        return enable == g.enable;

        }

    }
