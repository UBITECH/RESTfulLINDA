/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.linda.analytics.weka.generic;

import eu.linda.analytic.controller.AnalyticProcess;
import eu.linda.analytic.formats.InputFormat;
import eu.linda.analytics.config.Configuration;
import eu.linda.analytics.model.Analytics;
import eu.linda.analytics.weka.utils.HelpfulFunctions;
import java.util.AbstractList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import weka.classifiers.functions.LinearRegression;
import weka.core.Instance;
import weka.core.Instances;

/**
 *
 * @author eleni
 */
public class LinearRegressionAnalyticProcess extends AnalyticProcess {

    _LinearRegressionOutput linearRegressionOutput;
    HelpfulFunctions helpfulFunctions;
    InputFormat input;

    public LinearRegressionAnalyticProcess(InputFormat input) {

        helpfulFunctions = new HelpfulFunctions();
        helpfulFunctions.nicePrintMessage("Create analytic process for LinearRegression");
        //linearRegressionOutput = new LinearRegressionOutput(in);  
        this.input = input;

    }

    //Get LinearRegression Estimations
    @Override
    public void train(Analytics analytics) {
        helpfulFunctions.nicePrintMessage("Train LinearRegression");

        try {

            AbstractList<Instance> data1 = input.importData(Configuration.docroot + analytics.getDocument());
            Instances data = (Instances) data1;

            // remove dataset metadata (first two columns)    
            if (helpfulFunctions.isRDFExportFormat(analytics.getExportFormat()) ) {
                HashMap<String, Instances> separatedData = helpfulFunctions.separateDataFromMetadataInfo(data);
                data = separatedData.get("newData");
            }

            data.setClassIndex(data.numAttributes() - 1);
            //build model
            LinearRegression linearRegressionmodel = new LinearRegression();
            linearRegressionmodel.buildClassifier(data); //the last instance with missing    class is   not used

            helpfulFunctions.nicePrintMessage(linearRegressionmodel.toString());

            //classify the last instance
            Instance instancesToPredict = data.lastInstance();
            double price = linearRegressionmodel.classifyInstance(instancesToPredict);

            helpfulFunctions.saveModel(linearRegressionmodel, analytics);

        } catch (Exception ex) {
            Logger.getLogger(LinearRegressionAnalyticProcess.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    //Get LinearRegression Results
    @Override
    public AbstractList eval(Analytics analytics) {

        helpfulFunctions.nicePrintMessage("Eval Linear Regresion");

        AbstractList dataToReturn = null;
        HashMap<String, Instances> separatedData = null;
        try {
            //load data
            AbstractList<Instance> data1 = input.importData(Configuration.docroot + analytics.getTestdocument());
            Instances data = (Instances) data1;

            if (helpfulFunctions.isRDFExportFormat(analytics.getExportFormat()) ) {
                separatedData = helpfulFunctions.separateDataFromMetadataInfo(data);
                data = separatedData.get("newData");
            }
            
            
            //load model
            LinearRegression model = (LinearRegression) weka.core.SerializationHelper.read(Configuration.docroot + analytics.getModel());

            Instances unlabeled = data;

            // set class attribute
            unlabeled.setClassIndex(unlabeled.numAttributes() - 1);

            // create copy
            Instances labeled = new Instances(unlabeled);

            // label instances
            for (int i = 0; i < unlabeled.numInstances(); i++) {
                double clsLabel = model.classifyInstance(unlabeled.instance(i));
                labeled.instance(i).setClassValue(clsLabel);
            }

           //<--in linear regression there is no process info text-->
            

            if (helpfulFunctions.isRDFExportFormat(analytics.getExportFormat()) ) {
                Instances mergedData = helpfulFunctions.mergeDataAndMetadataInfo(labeled, separatedData.get("metaData"));
                dataToReturn= mergedData;

            } else {
                dataToReturn= labeled;
            }
            
        } catch (Exception ex) {
            Logger.getLogger(LinearRegressionAnalyticProcess.class.getName()).log(Level.SEVERE, null, ex);
        }

        //return resultjson.getString(1);
        return dataToReturn;
    }

}
