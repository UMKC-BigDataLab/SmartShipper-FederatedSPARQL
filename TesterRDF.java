/*
 * 8/16/13 Friday
 * Goal: Get data set from file. Execute a query on dataset.
 */

package rdf;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.OpAsQuery;
import com.hp.hpl.jena.sparql.algebra.OpWalker;
import com.hp.hpl.jena.sparql.algebra.Transform;
import com.hp.hpl.jena.sparql.algebra.TransformCopy;
import com.hp.hpl.jena.sparql.algebra.Transformer;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP;
import com.hp.hpl.jena.sparql.algebra.op.OpFilter;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.expr.E_LessThan;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.mgt.Explain ;
import com.hp.hpl.jena.sparql.sse.SSE;
import com.hp.hpl.jena.sparql.util.FmtUtils;
import com.hp.hpl.jena.vocabulary.*;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.sparql.engine.ResultSetStream;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.FileHandler;

import org.apache.log4j.BasicConfigurator;

public class TesterRDF {
	public static void main(String arg[])	{
		
		
		org.apache.jena.atlas.logging.Log.setLog4j();
		BasicConfigurator.configure();
		ARQ.setExecutionLogging(Explain.InfoLevel.ALL) ;
		//FileHandler fileHandler = new FileHandler("myLogFile");
		//ARQ.getExecLogger().addHandler(fileHandler);
		//askRemote();
		//modelFromFile();
		
		//algebraStuff();
		
		//executeAlgebra();
		
		
		//transformStuff();
		
		testingWalker();
	}
	
	static void testingWalker()	{
		Var var_x = Var.alloc("x");
		Var var_z = Var.alloc("z");
		
		Op op = makePretendAlgebra(var_x, var_z);
		System.out.println(op.getClass());
		System.out.println(op);
		OpWalker.walk(op, new FakeOpVisitor());	//still not entirely sure what a visitor is supposed to do. :(
		
	}
	
	static void transformStuff()	{
		Var var_x = Var.alloc("x");
		Var var_z = Var.alloc("z");
		Op op = makePretendAlgebra(var_x, var_z);
		
		//Provided transform thing.
		Transform thing = new TransformCopy(true);	//supposed to make a deep copy of the algebra??
		op = Transformer.transform(thing, op);
		System.out.println(op);
		
		Transform myT = new FakeTransform();
		op = Transformer.transform(myT, op);
		System.out.println(op);
	}
	
	
	static Model makeModel()	{
		String BASE = "http://example/";
		Model model = ModelFactory.createDefaultModel();
		model.setNsPrefix("",BASE);	//not sure what this actually does
		
		Resource r1 = model.createResource(BASE+"r1");
		Resource r2 = model.createResource(BASE+"r2");
		Property p1 = model.createProperty(BASE+"p") ;
        Property p2 = model.createProperty(BASE+"p2") ;
        RDFNode v1 = model.createTypedLiteral("1", XSDDatatype.XSDinteger) ;
        RDFNode v2 = model.createTypedLiteral("2", XSDDatatype.XSDinteger) ;
        
        //so at this point, nothing shows up when print the model. 
        //you gotta start adding stuff to the resources 
        
        r1.addProperty(p1, v1).addProperty(p1, v2) ;
        r1.addProperty(p2, v1).addProperty(p2, v2) ;
        r2.addProperty(p1, v1).addProperty(p1, v2) ;
		//System.out.println(model);
		
		
		
		
		
		return model;
	}
	static void executeAlgebra()	{
		
		Var var_x = Var.alloc("x");
		Var var_z = Var.alloc("z");
		
		Op op = makePretendAlgebra(var_x, var_z);
		
		Model m = makeModel();
		m.write(System.out, "TTL") ;
		
		//Execute expression
		QueryIterator qIter = Algebra.exec(op, m.getGraph());
		
		
		//Look at results
		//Can read query iterator directly
		if(false){
			for( ; qIter.hasNext();)	{
				Binding b = qIter.nextBinding();
				Node n = b.get(var_x);
				System.out.println(var_x + " = " + FmtUtils.stringForNode(n));
			}
		}
		else	{
			//or make result set from it - reading iterator consumes solution
			List<String> varNames = new ArrayList<String>() ;
            varNames.add("x") ;
            varNames.add("z") ;
            ResultSet rs = new ResultSetStream(varNames, m, qIter);
            ResultSetFormatter.out(rs) ;
            qIter.close() ;
		}
		qIter.close();
		
	}
	
	static Op makePretendAlgebra(Var var_x, Var var_z)	{
		
		String BASE = "http://example/";
		BasicPattern bp = new BasicPattern();
		
		
		//build some lovely expressions
		bp.add(new Triple(var_x, NodeFactory.createURI(BASE + "p"), var_z));
		bp.add(new Triple(var_x, NodeFactory.createURI(BASE + "q"), var_z));
		System.out.println("basic pattern: " + bp);
		
		//convert basicpattern -> algebra expression
		Op op = new OpBGP(bp);		
		System.out.println("Algebra expression: " + op);
		
		Expr expr = new E_LessThan(new ExprVar(var_z), NodeValue.makeNodeInteger(2));
		System.out.println("making a filter: " + expr);
		
		op = OpFilter.filter(expr, op);
		System.out.println("after applying filter: \n" + op);
		
		System.out.println("End makePretendAlgebra()");
		return op;
	}
	static void algebraStuff()	{
		
		//Query query = QueryFactory.read("file:/Users/vptarmigan/ANAPSID/federatedYears_sparql11.query");
		Query query = QueryFactory.read("file:/Users/vptarmigan/ANAPSID/singleService.query");
		
		//convert query -> algebra
		Op op = Algebra.compile(query);
		System.out.println(op.getClass());
		System.out.println(op);
		
		
		//convert algebra -> back to qu ery
		//interesing -> 
		Query query1 = OpAsQuery.asQuery(op);	
		System.out.println(query1);
		
		//let's look at dealing with SSE itself
		Op op2 = SSE.parseOp("(bgp (?s ?p ?o))");
		//System.out.println(op2);
		
		
	}

	static void askRemote()	{
	
		//how about... getting dataset from somewhere else?
		//I want to know how many birds there are that eat meat.
		//Or how about just mammals.  - How to ask a query to a sparql endpoint?
		String birdsQuery = "PREFIX dbpedia: <http://dbpedia.org/resource/> PREFIX dbpedia-owl: <http://dbpedia.org/ontology/> select distinct ?thing where {"
			+ "	SERVICE <http://dbpedia.org/sparql> {"
			+ "		?thing dbpedia-owl:class dbpedia:Bird "
			+ "		} ";
		
		String selectQuerySimple = 
				"PREFIX imdb: <http://data.linkedmdb.org/resource/movie/> " + 
				        "PREFIX dbpedia: <http://dbpedia.org/ontology/> " +
				        "PREFIX dcterms: <http://purl.org/dc/terms/> " + 
				        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " + 
				        "SELECT * " +  
				        //"   from <http://xmlns.com/foaf/0.1/> " +
				        "   { " +
				        "       SERVICE <http://data.linkedmdb.org/sparql> " + 
				        "       { " +  
				        "          ?actor1 imdb:actor_name \"Tom Hanks\". " + 
				        "          ?movie imdb:actor ?actor1 ; " +
				        "                 dcterms:title ?movieTitle . " + 
				        "       } " +
				        "       SERVICE <http://dbpedia.org/sparql> " +
				        "       { " + 
				        "           ?actor rdfs:label \"Tom Hanks\"@en ; " + 
				        "                  dbpedia:birthDate ?birth_date . " +
				        "        } " +  
				        "   } ";
		String selectQuery = 
				"PREFIX imdb: <http://data.linkedmdb.org/resource/movie/> " + 
				        "PREFIX dbpedia: <http://dbpedia.org/ontology/> " +
				        "PREFIX dcterms: <http://purl.org/dc/terms/> " + 
				        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " + 
				        "SELECT * " +  
				       // "   from <http://xmlns.com/foaf/0.1/> " +
				        "   { " +
				        "       SERVICE <http://data.linkedmdb.org/sparql> " + 
				        "       { " +  
				        "          ?actor1 imdb:actor_name ?name. " + 
				        "          ?movie imdb:actor ?actor1 ; " +
				        "                 dcterms:title ?movieTitle . " + 
				        "       } " +
				        "       SERVICE <http://dbpedia.org/sparql> " +
				        "       { " + 
				        "           ?actor rdfs:label ?name ; " + 
				        "                  dbpedia:birthDate ?birth_date . " +
				        "        } " +  
				        "   } ";
		Model model = ModelFactory.createDefaultModel();
		try	{
			//Query query = QueryFactory.create(birdsQuery, Syntax.syntaxSPARQL_11);
			//Query query = QueryFactory.read("file:/Users/vptarmigan/ANAPSID/federatedActor_sparql11.query");
			//Query query = QueryFactory.read("file:/Users/vptarmigan/ANAPSID/federatedBirthDate_sparql11.query");
			Query query = QueryFactory.read("file:/Users/vptarmigan/ANAPSID/federatedYears_sparql11.query");
			//QueryExecution qexec = QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", query);
			
			
			QueryExecution qexec = QueryExecutionFactory.create(query, model);
			System.out.println("QueryExecutionFactory create - > \n" + qexec.toString());
			
			ResultSet results = qexec.execSelect();
			System.out.println(ResultSetFormatter.asText(results));
		}
		catch (JenaException e)	{
			System.out.println("Invalid syntax");
			e.printStackTrace();
		}
	
	}

	static void modelFromFile()	{
		//just from file
		String fileName = "src/rdf/data/thirteenDrugs.nt";
		String selectQuery = "select * where {?s ?p ?o}";

		File file = new File(fileName);
		if(file.exists())	{
			Model model = null;
			try	{
				model = FileManager.get().loadModel(file.getCanonicalPath());
				Query query = QueryFactory.create(selectQuery, Syntax.syntaxSPARQL_11);
				QueryExecution qexec = QueryExecutionFactory.create(query, model);
				ResultSet results = qexec.execSelect();
				System.out.println(ResultSetFormatter.asText(results));
			}
			catch (JenaException e)	{
				System.out.println("Invalid syntax");
			}
			catch (IOException e)	{
				e.printStackTrace();
			}
		}
		else	{
			System.out.println(fileName + " does not exist.");	
		}
	}
}
