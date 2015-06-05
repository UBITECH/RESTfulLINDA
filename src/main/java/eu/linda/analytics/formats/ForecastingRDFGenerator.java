/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.linda.analytics.formats;

/**
 *
 * @author eleni
 */
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Seq;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.XSD;
import eu.linda.analytics.config.Configuration;
import eu.linda.analytics.model.Analytics;
import eu.linda.analytics.weka.utils.Util;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.AbstractList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.rosuda.JRI.REXP;
import org.rosuda.JRI.RVector;
import org.rosuda.JRI.Rengine;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;
import weka.core.Instances;

/**
 *
 * @author eleni
 */
public class ForecastingRDFGenerator extends RDFGenerator {

    Util helpfulFunctions;

    public ForecastingRDFGenerator() {
        helpfulFunctions = Util.getInstance();
    }

    //Generate General RDF Model
    @Override
    public Model generateRDFModel(Analytics analytics, AbstractList dataToExport) {

        helpfulFunctions.nicePrintMessage("Generate Forecasting RDFModel for weka algorithms ");

        Date date = new Date();
        DateFormat formatter = new SimpleDateFormat("ddMMyyyy");
        String today = formatter.format(date);
        String base = Configuration.lindaworkbenchURI + "openrdf-sesame/repositories/myRepository/statements?context=:_";
        String datasetContextToString = "analytics" + analytics.getId() + "V" + (analytics.getVersion() + 1) + "Date" + today;

        Instances triplets = (Instances) dataToExport;
        int tripletsAttibutesNum = triplets.numAttributes();

        // Create the model and define some prefixes (for nice serialization in RDF/XML and TTL)
        Model model = ModelFactory.createDefaultModel();
        //openrdf + analytic_process ID_version_date
        String NS = base + datasetContextToString + "#";

        String analytics_base = Configuration.lindaworkbenchURI + "openrdf-sesame/repositories/linda/rdf-graphs/analyticsontology";
        String analytics_NS = analytics_base + "#";

        model.setNsPrefix("ds", NS);
        model.setNsPrefix("rdf", RDF.getURI());
        model.setNsPrefix("xsd", XSD.getURI());
        model.setNsPrefix("foaf", FOAF.getURI());
        model.setNsPrefix("rdfs", RDFS.getURI());
        model.setNsPrefix("prov", "http://www.w3.org/ns/prov#");
        model.setNsPrefix("sio", "http://semanticscience.org/ontology/sio#");
        model.setNsPrefix("an", Configuration.lindaworkbenchURI + "openrdf-sesame/repositories/linda/rdf-graphs/analyticsontology#");

        // Define local properties
        Property analyzedField = model.createProperty(NS + "#analyzedField");
        Property predictedValue = model.createProperty(NS + "#predictedValue");
        Property wasDerivedFrom = model.createProperty("http://www.w3.org/ns/prov#wasDerivedFrom");
        Property wasGeneratedBy = model.createProperty("http://www.w3.org/ns/prov#wasGeneratedBy");
        Property actedOnBehalfOf = model.createProperty("http://www.w3.org/ns/prov#actedOnBehalfOf");
        Property wasAssociatedWith = model.createProperty("http://www.w3.org/ns/prov#wasAssociatedWith");
        Property hasTrainDataset = model.createProperty(NS + "hasTrainDataset");
        Property hasEvaluationDataset = model.createProperty(NS + "hasEvaluationDataset");
        Property algorithmProperty = model.createProperty(NS + "algorithm");

        Resource entity = model.createResource("http://www.w3.org/ns/prov#Entity");
        Resource activity = model.createResource("http://www.w3.org/ns/prov#Activity");
        Resource agent = model.createResource("http://www.w3.org/ns/prov#Agent");
        Resource onlineAccount = model.createResource(FOAF.OnlineAccount);

        Resource software_statement = model.createResource(analytics_NS + "Software/LinDa_analytics_software");
        Resource software = model.createResource(analytics_NS + "Software");
        Resource linda_user = model.createResource(analytics_NS + "User");

        Resource analytic_process = model.createResource(analytics_NS + "analytic_process");
        Resource analytic_process_statement = model.createResource(analytics_NS + "analytic_process/" + analytics.getId() + "/" + (analytics.getVersion() + 1));
        analytic_process_statement.addProperty(RDF.type, analytic_process);
        analytic_process_statement.addProperty(OWL.versionInfo, "1.0.0");
        analytic_process_statement.addLiteral(analyzedField, triplets.attribute(tripletsAttibutesNum - 1).name());
        analytic_process_statement.addProperty(RDFS.subClassOf, activity);
        analytic_process_statement.addProperty(wasAssociatedWith, software_statement);
        analytic_process_statement.addProperty(RDFS.label, "linda analytic process");
        analytic_process_statement.addProperty(RDFS.comment, analytics.getDescription());
        analytic_process_statement.addProperty(algorithmProperty, analytics.getAlgorithm_name());

        if (helpfulFunctions.isRDFInputFormat(analytics.getTrainQuery_id())) {

            Resource analytic_train_dataset_statement = model.createResource(Configuration.lindaworkbenchURI + "sparql/?q_id=" + analytics.getTrainQuery_id());
            analytic_process_statement.addProperty(hasTrainDataset, analytic_train_dataset_statement);

        }

        if (helpfulFunctions.isRDFInputFormat(analytics.getEvaluationQuery_id())) {

            Resource analytic_evaluation_dataset_statement = model.createResource(Configuration.lindaworkbenchURI + "sparql/?q_id=" + analytics.getEvaluationQuery_id());
            analytic_process_statement.addProperty(hasEvaluationDataset, analytic_evaluation_dataset_statement);

        }

        Resource linda_user_statement = model.createResource(analytics_NS + "User/" + analytics.getUser_name());
        linda_user_statement.addProperty(RDF.type, linda_user);
        linda_user_statement.addProperty(RDFS.subClassOf, agent);
        linda_user_statement.addProperty(RDFS.label, "linda user");

        software_statement.addProperty(RDF.type, software);
        software_statement.addProperty(RDFS.subClassOf, agent);
        software_statement.addProperty(RDFS.label, "analytics software");
        software_statement.addProperty(actedOnBehalfOf, linda_user_statement);

        linda_user_statement.addProperty(OWL.equivalentClass, FOAF.Person);

        linda_user_statement.addProperty(FOAF.holdsAccount, onlineAccount);

        linda_user_statement.addProperty(FOAF.accountName, analytics.getUser_name());
        onlineAccount.addProperty(FOAF.homepage, Configuration.lindaworkbenchURI);

        Resource analytic_result_node = model.createResource(analytics_NS + "analytics_result_node");
        Resource analytic_input_node = model.createResource(analytics_NS + "analytic_input_node");

        // For each triplet, create a resource representing the sentence, as well as the subject, 
        // predicate, and object, and then add the triples to the model.
        for (int i = 1; i < triplets.size(); i++) {

            Resource analytic_result_node_statement = model.createResource(NS + "/" + i);

            Resource analytic_input_node_statement = model.createResource(triplets.get(i).toString(1));
            analytic_input_node_statement.addProperty(RDF.type, analytic_input_node);

            analytic_result_node_statement.addProperty(RDF.type, analytic_result_node);
            analytic_result_node_statement.addProperty(RDFS.subClassOf, entity);
            analytic_result_node_statement.addProperty(wasDerivedFrom, analytic_input_node_statement);
            analytic_result_node_statement.addProperty(wasGeneratedBy, analytic_process_statement);
            analytic_result_node_statement.addProperty(predictedValue, triplets.get(i).toString(tripletsAttibutesNum - 1));
        }
        return model;

    }

    //Generate Forecasting RDF Model
    @Override
    public Model generateRDFModel(Analytics analytics, Rengine re) {

        helpfulFunctions.nicePrintMessage("Generate Forecasting RDFModel for R algorithms ");

        RVector dataToExportasVector = re.eval("df_to_export").asVector();
        Vector colnames = dataToExportasVector.getNames();
        String[] colnamesArray = new String[colnames.size()];
        colnames.copyInto(colnamesArray);

        String analyzedFieldValue = colnamesArray[colnames.size() - 1];

        //create input bag
        //re.eval("data_matrix[,c('uri')]");
        REXP datesAsCharacter = re.eval("as.character(df_to_export$" + colnamesArray[0] + ")");

        String[] datesAsStringArray = datesAsCharacter.asStringArray();
//        for (String string : datesAsStringArray) {
//            System.out.println("datesAsStringArray" + string);
//        }

        System.out.println("colname of predicted value" + colnamesArray[colnames.size() - 1]);
        REXP predictedValues = re.eval("df_to_export$" + colnamesArray[colnames.size() - 1]);

        double[] predictedValuesAsDoubleArray = predictedValues.asDoubleArray();

//        for (double d : predictedValuesAsDoubleArray) {
//            System.out.println("predictedValues:" + d);
//        }
        REXP uriAsCharacter = re.eval("as.character(loaded_data$uri)");
        String[] urisAsStringArray = uriAsCharacter.asStringArray();

//        for (String string : urisAsStringArray) {
//            System.out.println("urisAsStringArray" + string);
//        }
        Date date = new Date();
        DateFormat formatter = new SimpleDateFormat("ddMMyyyy");
        String today = formatter.format(date);
        String base = Configuration.lindaworkbenchURI + "openrdf-sesame/repositories/linda/statements?context=:_";
        String datasetContextToString = "analytics" + analytics.getId() + "V" + (analytics.getVersion() + 1) + "Date" + today;

        // Create the model and define some prefixes (for nice serialization in RDF/XML and TTL)
        Model model = ModelFactory.createDefaultModel();
        //openrdf + analytic_process ID_version_date
        String ds = base + datasetContextToString + "#";

        String analytics_base = Configuration.lindaworkbenchURI + "openrdf-sesame/repositories/linda/rdf-graphs/analyticsontology";
        String analytics_NS = analytics_base + "#";

        model.setNsPrefix("ds", ds);
        model.setNsPrefix("rdf", RDF.getURI());
        model.setNsPrefix("xsd", XSD.getURI());
        model.setNsPrefix("foaf", FOAF.getURI());
        model.setNsPrefix("rdfs", RDFS.getURI());
        model.setNsPrefix("prov", "http://www.w3.org/ns/prov#");
        model.setNsPrefix("sio", "http://semanticscience.org/ontology/sio#");
        model.setNsPrefix("an", Configuration.lindaworkbenchURI + "openrdf-sesame/repositories/linda/rdf-graphs/analyticsontology#");

        // Define local properties
        Property analyzedField = model.createProperty(ds + "analyzedField");
        Property predictedValue = model.createProperty(ds + "predictedValue");
        Property forecastingDate = model.createProperty(ds + "forecastingDate");
        Property wasDerivedFrom = model.createProperty("http://www.w3.org/ns/prov#wasDerivedFrom");
        Property wasGeneratedBy = model.createProperty("http://www.w3.org/ns/prov#wasGeneratedBy");
        Property actedOnBehalfOf = model.createProperty("http://www.w3.org/ns/prov#actedOnBehalfOf");
        Property wasAssociatedWith = model.createProperty("http://www.w3.org/ns/prov#wasAssociatedWith");
        Property hasTrainDataset = model.createProperty(ds + "hasTrainDataset");
        Property hasEvaluationDataset = model.createProperty(ds + "hasEvaluationDataset");
        Property algorithmProperty = model.createProperty(ds + "algorithm");
        Property dataSizeOfAnalyzedDataProperty = model.createProperty(ds + "dataSizeOfAnalyzedDatainBytes");
        Property timeToGetDataProperty = model.createProperty(ds + "timeToGetDataInSecs");
        Property timeToRunAnalyticsProcessProperty = model.createProperty(ds + "timeToRunAnalyticsProcessInSecs");
        Property timeToCreateRDFOutPutProperty = model.createProperty(ds + "timeToCreateRDFOutPutInSecs");
        Property performanceProperty = model.createProperty(ds + "performance");
        Property atTime = model.createProperty("http://www.w3.org/ns/prov#atTime");

        Resource entity = model.createResource("http://www.w3.org/ns/prov#Entity");
        Resource activity = model.createResource("http://www.w3.org/ns/prov#Activity");
        Resource agent = model.createResource("http://www.w3.org/ns/prov#Agent");
        Resource onlineAccount = model.createResource(FOAF.OnlineAccount);

        Resource software_statement = model.createResource(analytics_NS + "Software/LinDa_analytics_software");
        Resource software = model.createResource(analytics_NS + "Software");
        Resource linda_user = model.createResource(analytics_NS + "User");
        Resource performance = model.createResource(analytics_NS + "performance");
        Resource performance_statement = model.createResource(analytics_NS + "performance/" + analytics.getId() + "/" + analytics.getVersion());

        Resource analytic_process = model.createResource(analytics_NS + "analytic_process");
        Resource analytic_process_statement = model.createResource(analytics_NS + "analytic_process/" + analytics.getId() + "/" + analytics.getVersion());
        analytic_process_statement.addProperty(RDF.type, analytic_process);
        analytic_process_statement.addProperty(OWL.versionInfo, "1.0.0");
        analytic_process_statement.addLiteral(analyzedField, analyzedFieldValue);
        analytic_process_statement.addProperty(RDFS.subClassOf, activity);
        analytic_process_statement.addProperty(wasAssociatedWith, software_statement);
        analytic_process_statement.addProperty(RDFS.label, "linda analytic process");
        analytic_process_statement.addProperty(RDFS.comment, analytics.getDescription());
        analytic_process_statement.addProperty(algorithmProperty, analytics.getAlgorithm_name());

        Calendar cal = GregorianCalendar.getInstance();
        Literal value = model.createTypedLiteral(cal);
        analytic_process_statement.addProperty(atTime, value);

        performance_statement.addProperty(RDF.type, performance);
        performance_statement.addProperty(dataSizeOfAnalyzedDataProperty, Float.toString(analytics.getData_size()));
        performance_statement.addProperty(timeToGetDataProperty, Float.toString(analytics.getTimeToGet_data()));
        performance_statement.addProperty(timeToRunAnalyticsProcessProperty, Float.toString(analytics.getTimeToRun_analytics()));
        performance_statement.addProperty(timeToCreateRDFOutPutProperty, Float.toString(analytics.getTimeToCreate_RDF()));
        analytic_process_statement.addProperty(performanceProperty, performance_statement);

        if (helpfulFunctions.isRDFInputFormat(analytics.getTrainQuery_id())) {

            Resource analytic_train_dataset_statement = model.createResource(Configuration.lindaworkbenchURI + "sparql/?q_id=" + analytics.getTrainQuery_id());
            analytic_process_statement.addProperty(hasTrainDataset, analytic_train_dataset_statement);

        }

        if (helpfulFunctions.isRDFInputFormat(analytics.getEvaluationQuery_id())) {

            Resource analytic_evaluation_dataset_statement = model.createResource(Configuration.lindaworkbenchURI + "sparql/?q_id=" + analytics.getEvaluationQuery_id());
            analytic_process_statement.addProperty(hasEvaluationDataset, analytic_evaluation_dataset_statement);

        }

        Resource linda_user_statement = model.createResource(analytics_NS + "User/" + analytics.getUser_name());
        linda_user_statement.addProperty(RDF.type, linda_user);
        linda_user_statement.addProperty(RDFS.subClassOf, agent);
        linda_user_statement.addProperty(RDFS.label, "linda user");

        software_statement.addProperty(RDF.type, software);
        software_statement.addProperty(RDFS.subClassOf, agent);
        software_statement.addProperty(RDFS.label, "analytics software");
        software_statement.addProperty(actedOnBehalfOf, linda_user_statement);

        linda_user_statement.addProperty(OWL.equivalentClass, FOAF.Person);
        linda_user_statement.addProperty(FOAF.holdsAccount, onlineAccount);
        linda_user_statement.addProperty(FOAF.accountName, analytics.getUser_name());
        onlineAccount.addProperty(FOAF.homepage, Configuration.lindaworkbenchURI);

        Resource analytic_result_node = model.createResource(analytics_NS + "analytics_result_node");
        Resource analytic_input_node = model.createResource(analytics_NS + "analytic_input_node");

        Seq analytic_input_nodes = model.createSeq(analytics_NS + "analytic_input_collection/" + analytics.getId() + "/" + analytics.getVersion());

        // fill analytic input collection
        for (int i = 0; i < urisAsStringArray.length; i++) {

            Resource analytic_input_node_statement = model.createResource(urisAsStringArray[i]);
            analytic_input_node_statement.addProperty(RDF.type, analytic_input_node);
            analytic_input_nodes.add(analytic_input_node_statement);
        }

        // For each triplet, create a resource representing the sentence, as well as the subject, 
        // predicate, and object, and then add the triples to the model.
        for (int i = 0; i < predictedValuesAsDoubleArray.length; i++) {
            Resource analytic_result_node_statement = model.createResource(ds + "/" + i);
            analytic_result_node_statement.addProperty(RDF.type, analytic_result_node);
            analytic_result_node_statement.addProperty(RDFS.subClassOf, entity);
            analytic_result_node_statement.addProperty(wasDerivedFrom, analytic_input_nodes);
            analytic_result_node_statement.addProperty(wasGeneratedBy, analytic_process_statement);
            analytic_result_node_statement.addProperty(forecastingDate, datesAsStringArray[i]);
            analytic_result_node_statement.addProperty(predictedValue, String.valueOf(predictedValuesAsDoubleArray[i]));

        }

        re.eval("rm(list=ls());");

        return model;
    }

    @Override
    public Model generateRDFModel(Analytics analytics, RConnection re) {
        // Create the model and define some prefixes (for nice serialization in RDF/XML and TTL)
        Model model = ModelFactory.createDefaultModel();

        try {
            helpfulFunctions.nicePrintMessage("Generate Forecasting RDFModel for R algorithms ");

            org.rosuda.REngine.REXP column_to_predict = re.eval("column_to_predict");
            String analyzedFieldValue = column_to_predict.asString();


            org.rosuda.REngine.REXP datesAsCharacter = re.eval("as.character(df_to_export$Date)");

            String[] datesAsStringArray = datesAsCharacter.asStrings();
//        for (String string : datesAsStringArray) {
//            System.out.println("datesAsStringArray" + string);
//        }

            System.out.println("colname of predicted value" + analyzedFieldValue);
            org.rosuda.REngine.REXP predictedValues = re.eval("df_to_export$" + analyzedFieldValue);

            double[] predictedValuesAsDoubleArray = predictedValues.asDoubles();

//        for (double d : predictedValuesAsDoubleArray) {
//            System.out.println("predictedValues:" + d);
//        }
            org.rosuda.REngine.REXP uriAsCharacter = re.eval("as.character(loaded_data$uri)");
            String[] urisAsStringArray = uriAsCharacter.asStrings();

//        for (String string : urisAsStringArray) {
//            System.out.println("urisAsStringArray" + string);
//        }
            Date date = new Date();
            DateFormat formatter = new SimpleDateFormat("ddMMyyyy");
            String today = formatter.format(date);
            String base = Configuration.lindaworkbenchURI + "openrdf-sesame/repositories/linda/statements?context=:_";
            String datasetContextToString = "analytics" + analytics.getId() + "V" + (analytics.getVersion() + 1) + "Date" + today;

            //openrdf + analytic_process ID_version_date
            String ds = base + datasetContextToString + "#";

            String analytics_base = Configuration.lindaworkbenchURI + "openrdf-sesame/repositories/linda/rdf-graphs/analyticsontology";
            String analytics_NS = analytics_base + "#";

            model.setNsPrefix("ds", ds);
            model.setNsPrefix("rdf", RDF.getURI());
            model.setNsPrefix("xsd", XSD.getURI());
            model.setNsPrefix("foaf", FOAF.getURI());
            model.setNsPrefix("rdfs", RDFS.getURI());
            model.setNsPrefix("prov", "http://www.w3.org/ns/prov#");
            model.setNsPrefix("sio", "http://semanticscience.org/ontology/sio#");
            model.setNsPrefix("an", Configuration.lindaworkbenchURI + "openrdf-sesame/repositories/linda/rdf-graphs/analyticsontology#");

            // Define local properties
            Property analyzedField = model.createProperty(ds + "analyzedField");
            Property predictedValue = model.createProperty(ds + "predictedValue");
            Property forecastingDate = model.createProperty(ds + "forecastingDate");
            Property wasDerivedFrom = model.createProperty("http://www.w3.org/ns/prov#wasDerivedFrom");
            Property wasGeneratedBy = model.createProperty("http://www.w3.org/ns/prov#wasGeneratedBy");
            Property actedOnBehalfOf = model.createProperty("http://www.w3.org/ns/prov#actedOnBehalfOf");
            Property wasAssociatedWith = model.createProperty("http://www.w3.org/ns/prov#wasAssociatedWith");
            Property hasTrainDataset = model.createProperty(ds + "hasTrainDataset");
            Property hasEvaluationDataset = model.createProperty(ds + "hasEvaluationDataset");
            Property algorithmProperty = model.createProperty(ds + "algorithm");
            Property dataSizeOfAnalyzedDataProperty = model.createProperty(ds + "dataSizeOfAnalyzedDatainBytes");
            Property timeToGetDataProperty = model.createProperty(ds + "timeToGetDataInSecs");
            Property timeToRunAnalyticsProcessProperty = model.createProperty(ds + "timeToRunAnalyticsProcessInSecs");
            Property timeToCreateRDFOutPutProperty = model.createProperty(ds + "timeToCreateRDFOutPutInSecs");
            Property performanceProperty = model.createProperty(ds + "performance");
            Property atTime = model.createProperty("http://www.w3.org/ns/prov#atTime");

            Resource entity = model.createResource("http://www.w3.org/ns/prov#Entity");
            Resource activity = model.createResource("http://www.w3.org/ns/prov#Activity");
            Resource agent = model.createResource("http://www.w3.org/ns/prov#Agent");
            Resource onlineAccount = model.createResource(FOAF.OnlineAccount);

            Resource software_statement = model.createResource(analytics_NS + "Software/LinDa_analytics_software");
            Resource software = model.createResource(analytics_NS + "Software");
            Resource linda_user = model.createResource(analytics_NS + "User");
            Resource performance = model.createResource(analytics_NS + "performance");
            Resource performance_statement = model.createResource(analytics_NS + "performance/" + analytics.getId() + "/" + analytics.getVersion());

            Resource analytic_process = model.createResource(analytics_NS + "analytic_process");
            Resource analytic_process_statement = model.createResource(analytics_NS + "analytic_process/" + analytics.getId() + "/" + analytics.getVersion());
            analytic_process_statement.addProperty(RDF.type, analytic_process);
            analytic_process_statement.addProperty(OWL.versionInfo, "1.0.0");
            analytic_process_statement.addLiteral(analyzedField, analyzedFieldValue);
            analytic_process_statement.addProperty(RDFS.subClassOf, activity);
            analytic_process_statement.addProperty(wasAssociatedWith, software_statement);
            analytic_process_statement.addProperty(RDFS.label, "linda analytic process");
            analytic_process_statement.addProperty(RDFS.comment, analytics.getDescription());
            analytic_process_statement.addProperty(algorithmProperty, analytics.getAlgorithm_name());

            Calendar cal = GregorianCalendar.getInstance();
            Literal value = model.createTypedLiteral(cal);
            analytic_process_statement.addProperty(atTime, value);

            performance_statement.addProperty(RDF.type, performance);
            performance_statement.addProperty(dataSizeOfAnalyzedDataProperty, Float.toString(analytics.getData_size()));
            performance_statement.addProperty(timeToGetDataProperty, Float.toString(analytics.getTimeToGet_data()));
            performance_statement.addProperty(timeToRunAnalyticsProcessProperty, Float.toString(analytics.getTimeToRun_analytics()));
            performance_statement.addProperty(timeToCreateRDFOutPutProperty, Float.toString(analytics.getTimeToCreate_RDF()));
            analytic_process_statement.addProperty(performanceProperty, performance_statement);

            if (helpfulFunctions.isRDFInputFormat(analytics.getTrainQuery_id())) {

                Resource analytic_train_dataset_statement = model.createResource(Configuration.lindaworkbenchURI + "sparql/?q_id=" + analytics.getTrainQuery_id());
                analytic_process_statement.addProperty(hasTrainDataset, analytic_train_dataset_statement);

            }

            if (helpfulFunctions.isRDFInputFormat(analytics.getEvaluationQuery_id())) {

                Resource analytic_evaluation_dataset_statement = model.createResource(Configuration.lindaworkbenchURI + "sparql/?q_id=" + analytics.getEvaluationQuery_id());
                analytic_process_statement.addProperty(hasEvaluationDataset, analytic_evaluation_dataset_statement);

            }

            Resource linda_user_statement = model.createResource(analytics_NS + "User/" + analytics.getUser_name());
            linda_user_statement.addProperty(RDF.type, linda_user);
            linda_user_statement.addProperty(RDFS.subClassOf, agent);
            linda_user_statement.addProperty(RDFS.label, "linda user");

            software_statement.addProperty(RDF.type, software);
            software_statement.addProperty(RDFS.subClassOf, agent);
            software_statement.addProperty(RDFS.label, "analytics software");
            software_statement.addProperty(actedOnBehalfOf, linda_user_statement);

            linda_user_statement.addProperty(OWL.equivalentClass, FOAF.Person);
            linda_user_statement.addProperty(FOAF.holdsAccount, onlineAccount);
            linda_user_statement.addProperty(FOAF.accountName, analytics.getUser_name());
            onlineAccount.addProperty(FOAF.homepage, Configuration.lindaworkbenchURI);

            Resource analytic_result_node = model.createResource(analytics_NS + "analytics_result_node");
            Resource analytic_input_node = model.createResource(analytics_NS + "analytic_input_node");

            Seq analytic_input_nodes = model.createSeq(analytics_NS + "analytic_input_collection/" + analytics.getId() + "/" + analytics.getVersion());

            // fill analytic input collection
            for (int i = 0; i < urisAsStringArray.length; i++) {

                Resource analytic_input_node_statement = model.createResource(urisAsStringArray[i]);
                analytic_input_node_statement.addProperty(RDF.type, analytic_input_node);
                analytic_input_nodes.add(analytic_input_node_statement);
            }

            // For each triplet, create a resource representing the sentence, as well as the subject,
            // predicate, and object, and then add the triples to the model.
            for (int i = 0; i < predictedValuesAsDoubleArray.length; i++) {
                Resource analytic_result_node_statement = model.createResource(ds + "/" + i);
                analytic_result_node_statement.addProperty(RDF.type, analytic_result_node);
                analytic_result_node_statement.addProperty(RDFS.subClassOf, entity);
                analytic_result_node_statement.addProperty(wasDerivedFrom, analytic_input_nodes);
                analytic_result_node_statement.addProperty(wasGeneratedBy, analytic_process_statement);
                analytic_result_node_statement.addProperty(forecastingDate, datesAsStringArray[i]);
                analytic_result_node_statement.addProperty(predictedValue, String.valueOf(predictedValuesAsDoubleArray[i]));

            }

            re.eval("rm(list=ls());");

        } catch (RserveException ex) {
            Logger.getLogger(ForecastingRDFGenerator.class.getName()).log(Level.SEVERE, null, ex);
        } catch (REXPMismatchException ex) {
            Logger.getLogger(ForecastingRDFGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }
        return model;
    }
}
