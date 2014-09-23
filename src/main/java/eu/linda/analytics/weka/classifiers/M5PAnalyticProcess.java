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
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;

/**
 *
 * @author eleni
 */
public class M5PAnalyticProcess extends AnalyticProcess {

    M5POutput m5pOutput;
    HelpfulFuncions helpfulFuncions;

    public M5PAnalyticProcess() {
        System.out.println("-------------------------------------------------------");
        System.out.println("------------Create analytic process for M5P------------");
        System.out.println("-------------------------------------------------------");

        m5pOutput = new M5POutput();
        helpfulFuncions = new HelpfulFuncions();
    }

    @Override
    public void train(Analytics analytics) {
        System.out.println("-------------------------------------------------------");
        System.out.println("--------------------Train  M5P--------------------------");
        System.out.println("-------------------------------------------------------");

        Vector M5Pmodel;
        try {
            M5Pmodel = m5pOutput.trainModelM5P(Configuration.docroot + analytics.getDocument());
            helpfulFuncions.saveModelasVector(M5Pmodel, analytics);

        } catch (Exception ex) {
            Logger.getLogger(M5PAnalyticProcess.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public String eval(Analytics analytics) {
        System.out.println("-------------------------------------------------------");
        System.out.println("--------------------Eval M5P---------------------------");
        System.out.println("-------------------------------------------------------");

        JSONArray jsonresult = null;
        try {
            jsonresult = m5pOutput.predictM5P(analytics);
            helpfulFuncions.writeToFile(jsonresult.getString(0), "processinfo", analytics);

        } catch (Exception ex) {
            Logger.getLogger(M5PAnalyticProcess.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jsonresult.getString(1);

    }

}