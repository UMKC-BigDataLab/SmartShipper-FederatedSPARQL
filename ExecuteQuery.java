package rdf;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.BasicConfigurator;

import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.optimize.Optimize;
import com.hp.hpl.jena.sparql.mgt.Explain;

public class ExecuteQuery {
	public static void main(String args[]){
		//org.apache.jena.atlas.logging.Log.setLog4j();
		//BasicConfigurator.configure();
		//ARQ.setExecutionLogging(Explain.InfoLevel.ALL) ;
		
		//Optimize.noOptimizer();
		
		
		Query query = QueryFactory.read("file:/Users/vptarmigan/anapsid_old/federatedActor_sparql11.query");
		//Query query = QueryFactory.read("file:/Users/vptarmigan/dropbox/rdf_queries/2_db_imdb_schwarz.query");
		//System.out.println(query);
		CardOp.reorder(query);
		
		
		
	}
	
	/*
	public static void splitAlgebra()	{
		//find birth dates of actors in Star Trek -> subquery to dbpedia relies on subquery to linked mdb :(
		//Query query = QueryFactory.read("file:/Users/vptarmigan/ANAPSID/federatedBirthDate_sparql11.query");
		
		
		
		
		
		//convert query -> algebra	
		Op op = Algebra.compile(query);
		System.out.println(op.getClass());
		System.out.println(op);
		
		//executeThing(op, Arrays.asList("actor_name", "birth_date")); for birthdate query
		//executeThing(op, Arrays.asList( "film", "director","genre", "x")); //for years query
		Model model = ModelFactory.createDefaultModel();
		QueryExecution qexec = QueryExecutionFactory.create(query, model);
		
		System.out.println("QueryExecutionFactory create - > \n" + qexec.toString());
		
		ResultSet results = qexec.execSelect();
		System.out.println(ResultSetFormatter.asText(results));
		
	}
	*/
	
}
