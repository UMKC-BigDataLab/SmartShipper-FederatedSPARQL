/*
 * 8/16/13 Friday
 * Goal: Get data set from file. Execute a query on dataset.
 */

package rdf;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.*;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;

import java.io.*;

public class TesterRDF {
	public static void main(String arg[])	{
		
		askRemote();
		//modelFromFile();
		
	}

	static void askRemote()	{
	
		//how about... getting dataset from somewhere else?
		//I want to know how many birds there are that eat meat.
		//Or how about just mammals.  - How to ask a query to a sparql endpoint?
		String selectQuery = 
			"PREFIX dbpedia: <http://dbpedia.org/resource/>"
			+ "PREFIX dbpedia-owl: <http://dbpedia.org/ontology/>"
			+ "select distinct ?thing where {?thing dbpedia-owl:class dbpedia:Bird } LIMIT 100";
		Model model = null;
		try	{
			Query query = QueryFactory.create(selectQuery, Syntax.syntaxSPARQL_11);
			QueryExecution qexec = QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", query);
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
