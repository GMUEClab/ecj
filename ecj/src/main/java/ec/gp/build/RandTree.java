/*
  Copyright 2006 by Alexander Chircop
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


/*
 *  RandTree as described by Hitoshi Iba
 *  Author: Alexander Chircop
 *  Date:   28th Nov 2000
 */
 
package ec.gp.build;
import ec.gp.*;
import ec.*;
import ec.util.*;
import java.util.*;

public class RandTree extends GPNodeBuilder
    {
    public static final String P_RANDOMBRANCH = "randtree";
    int[] arities;
    boolean aritySetupDone=false;

    LinkedList permutations;

    public static class ArityObject extends Object
        {
        public int arity;
        public ArityObject(int a) { arity=a; }
        }

    public Parameter defaultBase()
        {
        return GPBuildDefaults.base().push(P_RANDOMBRANCH);
        }

    public void setup(final EvolutionState state, final Parameter base)
        {
        super.setup(state,base);

        // we use size distributions -- did the user specify any?
        if (!canPick())
            state.output.fatal("RandTree requires some kind of size distribution set, either with " + P_MINSIZE + "/" + P_MAXSIZE + ", or with " + P_NUMSIZES + ".",
                base, defaultBase());
        }

    // Added method to enhance linked list functionality with ArityObject
    boolean contains(LinkedList initArities,int a)
        {
        boolean truth=false;
        int counter=0;
        ArityObject b;

        if (initArities.size()!=0)
            while ((counter<initArities.size()) && (!truth))
                {
                b=(ArityObject)initArities.get(counter);
                if (b.arity==a) {truth=true;}
                counter++;
                }
        return truth;
        }

    void remove(LinkedList initArities,int a)
        {
        int counter=0;
        boolean removed=false;
        while((counter<initArities.size()) && (!removed))
            {
            ArityObject b=(ArityObject)initArities.get(counter);
            if (b.arity==a)
                {
                initArities.remove(counter);
                removed=true;
                }
            counter++;
            }
        }

    public void setupArities(final EvolutionState state,final GPFunctionSet set)
        {
        int noOfArities=0,current=0,marker=0,counter=0,a;
        LinkedList initArities=new LinkedList();
        GPInitializer initializer = ((GPInitializer)state.initializer);
        // count available arities and place on linked list
        while(current<set.nodes[0].length)
            {
                {
                a=set.nodes[0][current].constraints(initializer).childtypes.length;
                if((!contains(initArities,a)) && (a!=0))
                    {
                    initArities.add(new ArityObject(a));
                    noOfArities++;
                    }
                }
            current++;
            }

        if (initArities.size()==0) {state.output.fatal("Arity count failed... counted 0.");}

        // identify different available arities and place on array
        arities=new int[noOfArities];
        current=0;

        while(current<noOfArities)
            {
            // finds maximum arity on the list
            marker=0;
            for (counter=0;counter<initArities.size();counter++)
                {
                ArityObject max=(ArityObject) initArities.get(counter);
                if (max.arity > marker)
                    {
                    marker=max.arity;
                    }
                }

            // Place maximum found on the array
            arities[current]=marker;
            remove(initArities,marker);
            current++;
            }

        aritySetupDone=true;
        }

    long fact(long num)
        {
        if (num==0) { return 1; }
        else { return num*fact(num-1); }
        }

    int[] select(LinkedList permutations,int size)
        {
        int counter1,counter2,total=0;
        long residue,denominator=1;
        int selection;
        int[] current;
        int[] quantity=new int[permutations.size()];

        for (counter1=0;counter1<permutations.size();counter1++)
            {
            current=(int[])permutations.get(counter1);
            residue=size;
            // Quick internal calculations
            for (counter2=0;counter2<current.length;counter2++)
                {
                residue -= current[counter2];
                denominator *= fact(current[counter2]);
                }
            quantity[counter1] = (int) (fact(size-1)/(denominator * fact(residue)));
            total += quantity[counter1];
            }

        double[] prob=new double[quantity.length];
        // quantities found... now build array for probabilities
        for (counter1=0;counter1<quantity.length;counter1++)
            {
            prob[counter1] = (double)quantity[counter1]/(double)total;
            // I don't think we need to check for negative values here -- Sean
            }
        RandomChoice.organizeDistribution(prob);
        double s=0.0;
        selection = RandomChoice.pickFromDistribution(prob,s);

        return (int[])permutations.get(selection);
        }

    public GPNode newRootedTree(final EvolutionState state,
        final GPType type,
        final int thread,
        final GPNodeParent parent,
        final GPFunctionSet set,
        final int argposition,
        final int requestedSize)
        {
        int treeSize;
        boolean valid=false;
        String word=new String();

        treeSize=pickSize(state,thread);

        if (!aritySetupDone) { setupArities(state,set); }

        int[] temp=new int[arities.length];
        permutations=new LinkedList();
        Permute(0,temp,treeSize-1);
        if (permutations.size()==0) { state.output.fatal("Not able to build combination of nodes."); }
        int[] scheme=select(permutations,treeSize);
        word=buildDyckWord(treeSize,arities,scheme,state,thread);
        int cycle=0;
        while(!valid)
            {
            valid=checkDyckWord(word);
            if (!valid)
                {
                word=word.substring(word.length()-1,word.length()).concat(word.substring(0,word.length()-1));
                cycle++;
                if (cycle>=(treeSize*2)-1) {state.output.fatal("Not able to find valid permutation for generated Dyck word: "+word);}
                }
            }
        return buildTree(state,thread,parent,argposition,set,word);
        }

    // recursive function to work out all combinations and push them onto ArrayList
    void Permute(int current,int[] sol,int size)
        {
        int counter=0,result=0;
        // base case
        if (current==arities.length-1) /* set last one to maximum allowable */
            {
            while(result<=size)
                {
                counter++;
                result=result+arities[current];
                }
            result=result-arities[current];
            counter--;
            if (result<0)
                {
                result=0;
                counter=0;
                }
            sol[current]=counter;

            //Adding this solution to the list.
            permutations.add(sol);
            }
        // end of base case
        else
            {
            while(result<=size)
                {
                if (result<=size)
                    {
                    sol[current]=counter;
                    Permute(current+1,sol,size-result);
                    }
                result=result+arities[current];
                counter++;
                }
            }
        }

    public String buildDyckWord(int requestedSize,int[] arities,int[] s,EvolutionState state,int thread)
        {
        int counter,choices,choice,pos,arity=0,checksum=0,size=0;
        int[] scheme=new int[s.length];

        String dyck="";
        String addStr="";

        scheme=s;
        for(counter=0;counter<scheme.length;counter++)
            {
            checksum += scheme[counter]*arities[counter];
            }

        size=checksum+1;
        if (size!=requestedSize) { state.output.message("A tree of the requested size could not be built.  Using smaller size.");}
        choices=size;

        for(counter=0;counter<size;counter++)
            {
            dyck=dyck.concat("x*");
            }

        // Find a non-0 arity to insert
        counter=0;
        while((arity==0) && (counter<scheme.length))
            {
            if (scheme[counter]>0)
                {
                arity=arities[counter];
                scheme[counter]--;
                }
            counter++;
            }

        while(arity!=0)
            {
            choice=state.random[thread].nextInt(choices--)+1;
            pos=-1;
            counter=0;
            // find insertion position within the string
            while(counter!=choice)
                {
                pos++;
                if (dyck.charAt(pos)=='*') { counter++; }
                }
            // building no of y's in string
            addStr="";
            while (addStr.length()<arity) { addStr=addStr.concat("y"); }

            // finally put the string together again
            dyck=dyck.substring(0,pos).concat(addStr).concat(dyck.substring(pos+1,dyck.length()));

            // Find another non-0 arity to insert
            counter=0;
            arity=0;
            while((arity==0) && (counter<scheme.length))
                {
                if (scheme[counter]>0)
                    {
                    arity=arities[counter];
                    scheme[counter]--;
                    }
                counter++;
                }
            }
        //Clean up leftover *'s
        for (counter=0;counter<dyck.length();counter++)
            {
            if(dyck.charAt(counter)=='*')
                {
                dyck=dyck.substring(0,counter).concat(dyck.substring(counter+1,dyck.length()));
                }
            }
        return dyck;
        }

    // function to check validity of Dyck word
    public boolean checkDyckWord(String dyck)
        {
        int counter=0;
        boolean underflow=false;
        String stack="";
        while ((counter<dyck.length()) && (!underflow))
            {
            switch (dyck.charAt(counter))
                {
                case 'x':
                    stack=stack.concat("x");
                    break;
                case 'y':
                    if (stack.length()<=1)
                        {
                        underflow=true;
                        stack="";
                        }
                    else
                        {
                        stack=stack.substring(0,stack.length()-1);
                        }
                    break;
                default:  // cannot happen
                    throw new RuntimeException("default case should never be able to occur");
                }
            counter++;
            }
        if (stack.length()!=1)
            {
            return false;
            }
        else
            {
            return true;
            }
        }

    // This function parses the dyck word and puts random nodes into their slots.
    GPNode buildTree(final EvolutionState state,
        final int thread,
        final GPNodeParent parent,
        final int argposition,
        final GPFunctionSet set,
        final String dyckWord) 
        {
        int counter=0;
        Stack s=new Stack();
        char nextChar;

        // Parsing dyck word from left to right and building tree
        for (counter=0;counter<dyckWord.length();counter++)
            {
            if (counter<dyckWord.length()-1) { nextChar=dyckWord.charAt(counter+1);} else { nextChar='*'; }
            if ((nextChar=='x') || (nextChar=='*')) /* terminal node */
                {
                GPNode[] nn = set.terminals[0];
                GPNode n = (GPNode)(nn[state.random[thread].nextInt(nn.length)].lightClone());
                n.resetNode(state,thread);  // give ERCs a chance to randomize
                s.push(n);
                }
            else if (nextChar=='y') /* non-terminal */
                {
                // finding arity of connection
                int Ycount=0; /* arity */
                boolean nextCharY;
                nextCharY=(nextChar=='y');
                counter++;
                while ((counter<dyckWord.length()) && (nextCharY))
                    {
                    if (dyckWord.charAt(counter)=='y') { Ycount++; }
                    if (counter<dyckWord.length()-1) { nextCharY=(dyckWord.charAt(counter+1)=='y'); }
                    counter++;
                    }

                //Arity found.  Now just choose non terminal at random.
                GPNode[] nonTerms=set.nodesByArity[0][Ycount];
                GPNode nT=(GPNode) (nonTerms[state.random[thread].nextInt(nonTerms.length)].lightClone());
                // Non terminal chosen, now attaching children
                int childcount=Ycount;
                while (childcount>0)
                    {
                    childcount--;
                    if (s.size()==0) { state.output.fatal("Stack underflow when building tree."); }
                    GPNode child=(GPNode) s.pop();
                    child.parent=nT;
                    child.argposition=(byte)childcount;
                    nT.children[childcount]=child;
                    }
                nT.argposition=0;
                nT.parent=null;
                s.push(nT);
                if (counter!=dyckWord.length()) counter--;
                }
            }
        return (GPNode) s.pop();
        }
    }
