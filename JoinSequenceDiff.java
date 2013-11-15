package rdf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.BasicConfigurator;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.op.OpJoin;
import com.hp.hpl.jena.sparql.algebra.op.OpSequence;
import com.hp.hpl.jena.sparql.algebra.op.OpService;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.ResultSetStream;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.mgt.Explain;
import com.hp.hpl.jena.sparql.sse.SSE;
import com.hp.hpl.jena.sparql.util.FmtUtils;

public class JoinSequenceDiff {
	public static void main(String args[])	{
		
		
		org.apache.jena.atlas.logging.Log.setLog4j();
		BasicConfigurator.configure();
		ARQ.setExecutionLogging(Explain.InfoLevel.ALL) ;
		
		//<http://dbpedia.org/resource/Tom_Hanks>
		
		Op triple1 = SSE.parseOp("(bgp (triple ?x <http://example/p> ?z))");
		Op triple2 = SSE.parseOp("(bgp (triple ?x <http://example/q> ?z))");
		Op service1 = SSE.parseOp("(service <http://dbpedia.org/sparql>" + 
				"(bgp (triple ?actor_name <http://dbpedia.org/ontology/birthDate> ?birth_date)))");
		
		Op service2 = SSE.parseOp("(service <http://data.linkedmdb.org/sparql>" + 
      "(bgp" +
        "(triple ?actor <http://data.linkedmdb.org/resource/movie/actor_name> ?actor_name)" + 
        "(triple ?movie <http://data.linkedmdb.org/resource/movie/actor> ?actor)" + 
        "(triple ?movie <http://purl.org/dc/terms/title> \"The Shining\")"+ 
      "))");
		
		Op service3 = SSE.parseOp("(slice _ 100" +
  "(distinct" + 
    "(filter (> ?birth_date \"1999-10-01\"^^<http://www.w3.org/2001/XMLSchema#date>)" +
      "(bgp (triple ?actor <http://dbpedia.org/ontology/birthDate> ?birth_date)))))");
		
		
		//JOIN TIME
		Op joined = OpJoin.create(service1, service2);		
		System.out.println(joined);
		
		//SEQUENCE TIME
		Op sequenced = OpSequence.create(service2, service1);
		System.out.println(sequenced);
		
		//Op meat = makeOp();
		
		//want to try to execute service1
		service3 = new OpService(NodeFactory.createURI("http://dbpedia.org/sparql"), service3, false);
		//executeThing(service3);
		System.out.println("JOINED ");
		//executeThing(joined);
		System.out.println("SEQUENCED");
		executeThing(sequenced, Arrays.asList("actor_name", "movie", "name", "birth_date"));
	}
	
	public static void executeThing(Op op, List<String> meat)	{

		Model m = ModelFactory.createDefaultModel();
		m.write(System.out, "TTL") ;
		
		//Execute expression
		QueryIterator qIter = Algebra.exec(op, m.getGraph());
		
		
		//Look at results
		//Can read query iterator directly
		List<String> varNames = new ArrayList<String>() ;
        for (String var : meat)	{
        	varNames.add(var);
        }

        ResultSet rs = new ResultSetStream(varNames, m, qIter);
        ResultSetFormatter.out(rs) ;
        qIter.close() ;
	}
	
	public static void makeOp()	{
		Var var_date = Var.alloc("birth_date");
		Var var_person = Var.alloc("person");
			
		BasicPattern bp = new BasicPattern();
		bp.add(new Triple(var_person, NodeFactory.createURI("http://dbpedia.org/ontology/birthDate"), var_date));
		
		//Expr expr = new E_LessThan(new ExprVar("birth_date"), NodeValue.makeNodeInteger(2));
	}
		
}
