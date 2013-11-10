package rdf;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.TransformBase;
import com.hp.hpl.jena.sparql.algebra.Transformer;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP;
import com.hp.hpl.jena.sparql.algebra.op.OpFilter;
import com.hp.hpl.jena.sparql.algebra.op.OpJoin;
import com.hp.hpl.jena.sparql.sse.SSE;

public class FakeTransformTester {
	
	public static void main(String args[])	{
		String thingy = "(join (join " + 
           "(filter (< ?o0 20) (bgp (triple ?s <urn:ex:prop0> ?o0)))" +
           "(filter (< ?o1 20) (bgp (triple ?s <urn:ex:prop1> ?o1)))) "+
       "(filter (< ?o2 20) (bgp (triple ?s <urn:ex:prop2> ?o2))))";
		Op op = SSE.parseOp(thingy) ;	
		
		System.out.println("Before:" + op);
		op = Transformer.transform(new QueryCleaner(), op);
		System.out.println("After:" + op);
	}
	
}

//copied from jena documentation - manipulating sparql using arq
class QueryCleaner extends TransformBase
{
    @Override
    public Op transform(OpJoin join, Op left, Op right) {
        // Bail if not of the right form
        if (!(left instanceof OpFilter && right instanceof OpFilter)) return join;
        OpFilter leftF = (OpFilter) left;
        OpFilter rightF = (OpFilter) right;

        // Add all of the triple matches to the LHS BGP
        ((OpBGP) leftF.getSubOp()).getPattern().addAll(((OpBGP) rightF.getSubOp()).getPattern());
        // Add the RHS filter to the LHS
        leftF.getExprs().addAll(rightF.getExprs());
        return leftF;
    }
}