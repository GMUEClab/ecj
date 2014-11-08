/*
  Copyright 2014 by Xiaomeng Ye
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.gp.lambda.interpreter;

import java.io.Serializable;
import java.util.ArrayList;

import ec.gp.GPNode;
import ec.gp.GPNodeGatherer;
import ec.gp.GPNodeParent;
import ec.gp.lambda.IndexNode;
import ec.gp.lambda.LambdaNode;
import ec.gp.lambda.LambdaTree;
/**
 * A fitness function developed by Kazuto Tominaga, Tomoya Suzuki, and Kazuhiro Oka that measures the syntactical differences between two tree structure.
 * @author Ye Xiaomeng
 *
 */
public class Tomi implements Serializable{
	private static final long serialVersionUID = 1;
	static GPNodeGatherer g = new GPNodeGatherer() { public boolean test(GPNode node) { return true; } };

//	static GPNodeGatherer getNodeGatherer(){
//		if(g==null){
//			g = new GPNodeGatherer_NoRestriction();
//		}
//		return g;
//	}
	
	int levelWeight;
	int boundFreeWeight;

	/**
	 * @param levelWeight the levelWeight to set
	 */
	public void setLevelWeight(int levelWeight) {
		this.levelWeight = levelWeight;
	}
	/**
	 * @param boundFreeWeight the boundFreeWeight to set
	 */
	public void setBoundFreeWeight(int boundFreeWeight) {
		this.boundFreeWeight = boundFreeWeight;
	}

	
	/**
	 * @param tree1
	 * @param tree2
	 * @return the syntactical difference score between tree1 and tree2
	 */
	public float diff(LambdaTree tree1, LambdaTree tree2) {

		return dist((LambdaNode) tree1.child, (LambdaNode) tree2.child);
	}
	/**
	 * 
	 * @param node1
	 * @param node2
	 * @return the syntactical difference score between the subtrees under node1 and node2 (including node1 and node2)
	 */
	public float dist(LambdaNode node1, LambdaNode node2){
		if(node1 instanceof IndexNode && node2 instanceof IndexNode){
			//if one bound the other not
			if((indexIsBound((IndexNode) node1) && ! indexIsBound((IndexNode) node2))||
					(!indexIsBound((IndexNode) node1) && indexIsBound((IndexNode) node2))){
				return (boundFreeWeight+ ((float)(abstlev(node1)+abstlev(node2)))/2);
			}else{//both bound or unbound
				return Math.abs(((IndexNode)node1).getIndex() - ((IndexNode)node2).getIndex());
			}
		}else if(node1.toString().equals("L") && node2.toString().equals("L")){
			return dist((LambdaNode)node1.children[0], (LambdaNode)node2.children[0]);
		}else if(node1.toString().equals("P") && node2.toString().equals("P")){
			return dist((LambdaNode)node1.children[0], (LambdaNode)node2.children[0])+
			dist((LambdaNode)node1.children[1], (LambdaNode)node2.children[1]);
		}else if(node1 instanceof IndexNode && !(node2 instanceof IndexNode)){
			return numNodes(node2);
		}else if(node2 instanceof IndexNode && !(node1 instanceof IndexNode)){
			return numNodes(node1);
		}else if((node1.toString().equals("L") && node2.toString().equals("P"))
				||(node1.toString().equals("P") && node2.toString().equals("L"))){
			return diffnodes(node1,node2);
		}
		System.err.println("Should never reach here");
		return 0;
	}

	private boolean indexIsBound(IndexNode theNode) {
		int bindingInt = theNode.getIndex();
		LambdaNode iter = theNode;
		while (iter.parent instanceof LambdaNode && bindingInt != 0) {
			iter = (LambdaNode) iter.parent;
			if (iter.toString().equals("L")) {
				bindingInt--;
			}
		}
		return (bindingInt == 0);
	}

	private int abstlev(GPNode theNode) {
		GPNode iter = theNode;
		int level = 0;
		while (iter.parent instanceof GPNode) {
			iter = (GPNode) iter.parent;
			if (iter.toString().equals("L")) {
				level++;
			}
		}
		return level;
	}

	private int numNodes(GPNode theNode) {
		return theNode.numNodes(g);
	}

	private int diffnodes(GPNode node1, GPNode node2) {
		int count = 0;
		int m = Math.max(node1.depth(), node2.depth());
		// this level's nodes
		ArrayList<GPNode> list1 = new ArrayList<GPNode>();
		ArrayList<GPNode> list2 = new ArrayList<GPNode>();
		// next level
		ArrayList<GPNode> list1next = new ArrayList<GPNode>();
		ArrayList<GPNode> list2next = new ArrayList<GPNode>();

		list1.add(node1);
		list2.add(node2);
		for (int i = 0; i < m - 1; i++) {
			int count1 = 0;
			int count2 = 0;
			for (GPNode curNode : list1) {
				count1++;
				for (GPNode child : curNode.children) {
					list1next.add(child);
				}
			}
			for (GPNode curNode : list2) {
				count2++;
				for (GPNode child : curNode.children) {
					list1next.add(child);
				}
			}
			count += (levelWeight + Math.abs(count1 - count2));
			list1 = list1next;
			list2 = list2next;
			list1next = new ArrayList<GPNode>();
			list2next = new ArrayList<GPNode>();
		}
		return count;
	}

	// not needed, already implemented in diffnodes()
	// private static int numNodesAtLevel(int l, GPNode theNode){
	// int curLevel = 0;
	// while(curLevel)
	// }
}
