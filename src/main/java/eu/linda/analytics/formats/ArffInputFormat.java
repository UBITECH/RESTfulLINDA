/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.linda.analytics.formats;

import eu.linda.analytics.weka.utils.HelpfulFunctions;
import java.util.AbstractList;
import java.util.logging.Level;
import java.util.logging.Logger;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

/**
 *
 * @author eleni
 */
public class ArffInputFormat extends InputFormat {
    
    HelpfulFunctions helpfulFuncions;

    public ArffInputFormat() {
        helpfulFuncions = new HelpfulFunctions();
    }


    @Override
    public AbstractList importData(String pathToFile, boolean isForRDFOutput) {

    helpfulFuncions.nicePrintMessage("import Arff file "+pathToFile);
    

    Instances data = null;
    //Instances newData = null;
        try {
            
            data = ConverterUtils.DataSource.read(pathToFile);
           
            //NominalToString filter1 = new NominalToString();
            //filter1.setInputFormat(data);
            //data = Filter.useFilter(data, filter1);

            /*/first 2 colums are metadata info used for rdf output
            if (excludeMetadataInfo) {
                String[] options = new String[2];
                options[0] = "-R";                                    // "range"
                options[1] = "1,2";                                     // first attribute
                Remove remove = new Remove();                         // new instance of filter
                remove.setOptions(options);                           // set options
                remove.setInputFormat(data);                          // inform filter about dataset **AFTER** setting options
                newData = Filter.useFilter(data, remove);   // apply filter                
                newData.setClassIndex(newData.numAttributes() - 1);
                return newData;
            }*/
             data.setClassIndex(data.numAttributes() - 1);
            
        } catch (Exception ex) {
            Logger.getLogger(ArffInputFormat.class.getName()).log(Level.SEVERE, null, ex);
        }
     return data;
    
    }
    
}