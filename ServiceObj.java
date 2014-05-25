package rdf;

import com.hp.hpl.jena.sparql.algebra.Op;

public class ServiceObj implements Comparable<ServiceObj>{
	//custom class to store both service call algebra and cardinality.
	//used for easier sorting. 
		private Op serviceCall;
		private int cardinality;
		
		public ServiceObj(Op op, int i)	{
			serviceCall = op;
			cardinality = i;
		}
		
		Op getOp()	{ return serviceCall; }
		int getCard()	{ return cardinality; }
		
		@Override
		public int compareTo(ServiceObj other)	{
			if (this.cardinality == other.cardinality) return 0;
			return (this.cardinality - other.cardinality); 	//NEED TO TEST THIS
		}
		
		@Override
		public String toString()	{
			return cardinality + " : " + serviceCall;
		
		}
}
