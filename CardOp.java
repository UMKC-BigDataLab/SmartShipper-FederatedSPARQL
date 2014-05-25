package rdf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.OpVisitor;
import com.hp.hpl.jena.sparql.algebra.OpWalker;
import com.hp.hpl.jena.sparql.algebra.op.OpAssign;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP;
import com.hp.hpl.jena.sparql.algebra.op.OpConditional;
import com.hp.hpl.jena.sparql.algebra.op.OpDatasetNames;
import com.hp.hpl.jena.sparql.algebra.op.OpDiff;
import com.hp.hpl.jena.sparql.algebra.op.OpDisjunction;
import com.hp.hpl.jena.sparql.algebra.op.OpDistinct;
import com.hp.hpl.jena.sparql.algebra.op.OpExt;
import com.hp.hpl.jena.sparql.algebra.op.OpExtend;
import com.hp.hpl.jena.sparql.algebra.op.OpFilter;
import com.hp.hpl.jena.sparql.algebra.op.OpGraph;
import com.hp.hpl.jena.sparql.algebra.op.OpGroup;
import com.hp.hpl.jena.sparql.algebra.op.OpJoin;
import com.hp.hpl.jena.sparql.algebra.op.OpLabel;
import com.hp.hpl.jena.sparql.algebra.op.OpLeftJoin;
import com.hp.hpl.jena.sparql.algebra.op.OpList;
import com.hp.hpl.jena.sparql.algebra.op.OpMinus;
import com.hp.hpl.jena.sparql.algebra.op.OpNull;
import com.hp.hpl.jena.sparql.algebra.op.OpOrder;
import com.hp.hpl.jena.sparql.algebra.op.OpPath;
import com.hp.hpl.jena.sparql.algebra.op.OpProcedure;
import com.hp.hpl.jena.sparql.algebra.op.OpProject;
import com.hp.hpl.jena.sparql.algebra.op.OpPropFunc;
import com.hp.hpl.jena.sparql.algebra.op.OpQuad;
import com.hp.hpl.jena.sparql.algebra.op.OpQuadPattern;
import com.hp.hpl.jena.sparql.algebra.op.OpReduced;
import com.hp.hpl.jena.sparql.algebra.op.OpSequence;
import com.hp.hpl.jena.sparql.algebra.op.OpService;
import com.hp.hpl.jena.sparql.algebra.op.OpSlice;
import com.hp.hpl.jena.sparql.algebra.op.OpTable;
import com.hp.hpl.jena.sparql.algebra.op.OpTopN;
import com.hp.hpl.jena.sparql.algebra.op.OpTriple;
import com.hp.hpl.jena.sparql.algebra.op.OpUnion;

/*
 * Delicious sandboxing for cardinality optimization woohoo
 * 
 */

public class CardOp {
	
	//Here's the meat and potatoes. 
	public static Op reorder(Query orig){
		System.out.println("Original Query\n========================");
		System.out.println(orig);
		
		Op op = Algebra.compile(orig);
		System.out.println("Original Algebra\n========================");
		System.out.println(op);
		
		ArrayList<Op> indivServices = grabIndivServices(op);
		//System.out.println("Grab indiv services  \n========================");
		//System.out.println(indivServices);
		
		ArrayList<ServiceObj> cardinalities = grabEstimatedCardinalityArray(indivServices);
		System.out.println("Grab cardinalities \n========================");
		System.out.println(cardinalities);
		
		cardinalities= reorderAlgebra(cardinalities);	//do the actual reordering based on the cardinality
		System.out.println("Reorder Algebra \n========================");
		System.out.println(cardinalities);
		
		Op newOp  = makeNewAlgebra(cardinalities);	//mash them together into one algebra expression again
		System.out.println("redone algebra  \n========================");
		System.out.println(newOp);
		return newOp;
	}
	
	/*
	 * reorder the algebra using greedy algorithm
	 * returns the reordered algebra
	 * 
	 * -> looking up if hash map even preserves order in java
	 * ok. hash map doesn't preserve or guarantee order. but treemap does which implements sortedmap interface :D
	 * 
	 * wait. so treemaps are apparently a loooot slower. i don't actually even need to be sorting IN the map itself, 
	 * all that matters is the key value anyways
	 * 
	 */
	private static ArrayList<ServiceObj> reorderAlgebra(ArrayList<ServiceObj> servObjs) {
		//Reorder the service calls based on the cardinality
		Collections.sort(servObjs);	//sorts based on the compareTo method in servObjs
		return servObjs;	//just return the sorted array list for now for testing
	}
	
	/*
	 * makeNewAlgebra
	 * given an array list of already sorted service calls, mash them together into one algebra expression again
	 */
	
	private static Op makeNewAlgebra(ArrayList<ServiceObj> servObjs)	{
		//erm let's just assume we have two service calls ok
		Op op = OpSequence.create(servObjs.get(0).getOp(), servObjs.get(1).getOp());
		return op;
	}
	
	
	
	/*
	 * Returns array list of individual service calls
	 */
	private static ArrayList<Op> grabIndivServices(Op op)	{
		ArrayList<Op> stuff = new ArrayList<Op>();
		OpWalker.walk(op, new ServiceGrabber(stuff));
		return stuff;
	}
	/*
	 * matches incoming service calls with their cardinalities, and returns it all in a hash map. 
	 * not going to use this, since sorting a hash map/treempa is a pain
	 */
	
	/*
	private static HashMap<Op,Integer> grabEstimatedCardinality(ArrayList<Op> services)	{
		//Based on the service ops in this array list, will lookup their cardinality
		//we assume we already know the cardinality estimates and store them in a table somewhere
		
		//for now I'll just hardcode this thing, but really we should look these up
		//... it seems that opwalker may use go from bottom up so the order will be a bit messed up TODO
		ArrayList<Integer> fakeCardinality = new ArrayList<Integer>();
		fakeCardinality.add(3);
		fakeCardinality.add(25);
		
		HashMap<Op,Integer> meat = new HashMap<Op,Integer>();
		
		for(int i =0; i < services.size(); i++)	{
			meat.put(services.get(i), fakeCardinality.get(i));		//matching the service calls to the fake cardinalities
		}
		return meat;
	}
	*/
	
	/*
	 * Given a bunch of service calls, will look up their respective estimated cardinalities
	 * 
	 */
	private static ArrayList<ServiceObj>  grabEstimatedCardinalityArray(ArrayList<Op> services){
		
		ArrayList<ServiceObj> list = new ArrayList<ServiceObj>();
		int cardinality = 10;
		for(Op op: services){
			//int cardinality = (int) (Math.random() * 30);	//for now randomly generate cardinalites from 0 - 30
			list.add(new ServiceObj(op, cardinality));
			cardinality -=1;
		}
		return list;
	}
	

	
	public static class ServiceGrabber  implements OpVisitor {
	
		
		//private HashMap<Op, Integer> map;
		private ArrayList<Op> map;
			
		public ServiceGrabber(ArrayList<Op> m)	{
			map = m;
		}
	
		@Override
		public void visit(OpService arg0) {
			// TODO Auto-generated method stub
			//YAAAY!... opefully this does what i want it to do T.T
			System.out.println("VISITING OPSERVICE");
			map.add(arg0);
		}
		
		@Override
		public void visit(OpBGP arg0) {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void visit(OpQuadPattern arg0) {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void visit(OpTriple arg0) {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void visit(OpQuad arg0) {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void visit(OpPath arg0) {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void visit(OpTable arg0) {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void visit(OpNull arg0) {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void visit(OpProcedure arg0) {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void visit(OpPropFunc arg0) {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void visit(OpFilter arg0) {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void visit(OpGraph arg0) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void visit(OpDatasetNames arg0) {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void visit(OpLabel arg0) {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void visit(OpAssign arg0) {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void visit(OpExtend arg0) {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void visit(OpJoin arg0) {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void visit(OpLeftJoin arg0) {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void visit(OpUnion arg0) {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void visit(OpDiff arg0) {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void visit(OpMinus arg0) {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void visit(OpConditional arg0) {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void visit(OpSequence arg0) {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void visit(OpDisjunction arg0) {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void visit(OpExt arg0) {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void visit(OpList arg0) {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void visit(OpOrder arg0) {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void visit(OpProject arg0) {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void visit(OpReduced arg0) {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void visit(OpDistinct arg0) {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void visit(OpSlice arg0) {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void visit(OpGroup arg0) {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void visit(OpTopN arg0) {
			// TODO Auto-generated method stub
			
		}
	}

}
