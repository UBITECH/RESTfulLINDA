/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.linda.analytics.controller;

import eu.linda.analytics.formats.OutputFormat;
import eu.linda.analytics.model.Analytics;

/**
 *
 * @author eleni
 */

abstract public class AnalyticProcess {


    abstract public void train(Analytics a);

    abstract public void eval(Analytics a,OutputFormat out);
    
}
