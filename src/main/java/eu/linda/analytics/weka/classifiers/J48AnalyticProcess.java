/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.linda.analytics.weka.classifiers;

import eu.linda.analytic.controller.AnalyticProcess;
import eu.linda.analytics.config.Configuration;
import eu.linda.analytics.model.Analytics;
import eu.linda.analytics.weka.utils.HelpfulFuncions;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import weka.classifiers.Classifier;

/**
 *
 * @author eleni
 */
public class J48AnalyticProcess extends AnalyticProcess {

    J48Output j48Output;
    HelpfulFuncions helpfulFuncions;

    public J48AnalyticProcess() {
        System.out.println("-------------------------------------------------------");
        System.out.println("------------Create analytic process for J48------------");
        System.out.println("-------------------------------------------------------");

        j48Output = new J48Output();
        helpfulFuncions = new HelpfulFuncions();
    }

    @Override
    public void train(Analytics analytics) {
        System.out.println("-------------------------------------------------------");
        System.out.println("--------------------Train J48--------------------------");
        System.out.println("-------------------------------------------------------");

        Classifier j48ClassifierModel;
        try {
            j48ClassifierModel = j48Output.getJ48TreeModel(Configuration.docroot + analytics.getDocument());
            analytics = helpfulFuncions.saveModel(j48ClassifierModel, analytics);
        } catch (Exception ex) {
            Logger.getLogger(J48AnalyticProcess.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Override
    public String eval(Analytics analytics) {
        System.out.println("-------------------------------------------------------");
        System.out.println("--------------------Eval J48---------------------------");
        System.out.println("-------------------------------------------------------");

        JSONArray jsonresult = null;
        try {
            jsonresult = j48Output.getJ48TreeResultDataset(analytics);

            helpfulFuncions.writeToFile(jsonresult.getString(0), "processinfo", analytics);

        } catch (Exception ex) {
            Logger.getLogger(J48AnalyticProcess.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jsonresult.getString(1);
        
//helpfulFuncions.writeToFile(jsonresult.getString(1), "resultdocument", analytics);
        //result = jsonresult.toString();
    }

}
