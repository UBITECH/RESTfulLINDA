/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.linda.analytic.controller;

import eu.linda.analytics.model.Analytics;
import org.json.JSONArray;

/**
 *
 * @author eleni
 */
abstract public class OutputFormat {


    abstract public void exportData(Analytics analytics,String dataToExport);

}