package eu.linda.analytics.db;


import eu.linda.analytics.model.Analytics;
import eu.linda.analytics.model.Classification;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Panagiotis Gouvas
 */
public class DBSynchronizer {

    Connection connection;

    public DBSynchronizer() {
        connection = ConnectionFactory.getInstance();
    }//Constructor

    /*
     * Fetch analytics_analytics by id
     */
    public Analytics getlindaAnalytics_analytics(int id) {
        Analytics analytics = null;
        try {

            String query = "SELECT  * FROM analytics_analytics  AS analytics, analytics_algorithm AS alg WHERE analytics.id =? AND analytics.algorithm_id = alg.id";
            //String query = "SELECT  * FROM "+analytics_table+"  AS analytics, analytics_algorithm AS alg WHERE analytics.id =? AND analytics.algorithm_id = alg.id";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, id);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                analytics = new Analytics(
                        rs.getInt("id"),
                        rs.getInt("category_id"),
                        rs.getInt("algorithm_id"),
                        rs.getString("document"),
                        rs.getString("testdocument"),
                        rs.getString("model"),
                        rs.getString("modelReadable"),
                        rs.getString("processinfo"),
                        rs.getString("resultdocument"),
                        rs.getString("exportFormat")
                );
                
                analytics.setAlgorithm_name(rs.getString("name"));
                
                break;
            }
        } catch (SQLException ex) {
            Logger.getLogger(DBSynchronizer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return analytics;

    }//EoM   
    
        /*
     * Fetch analytics_analytics by id
     */
    public Classification getlindaAnalytics_classification(int id) {
        Classification classification = null;
        try {

            String query = "SELECT  * FROM  analytics_clasification  AS analytics, analytics_algorithm AS alg WHERE analytics.id =? AND analytics.algorithm_id = alg.id";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, id);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                classification = new Classification(
                        rs.getInt("id"),
                        rs.getInt("category_id"),
                        rs.getInt("algorithm_id"),
                        rs.getString("traindocument"),
                        rs.getString("model"),
                        rs.getString("resultdocument"),
                        rs.getString("exportFormat")
                );
                
                classification.setAlgorithm_name(rs.getString("name"));
                
                break;
            }
        } catch (SQLException ex) {
            Logger.getLogger(DBSynchronizer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return classification;

    }//EoM  
    
    
    /*
     * Update LINDA Analytics with result file
    */
    
        /*
     * Updates an Item
     */
    public void updateLindaAnalytics(String resultPath,String column, int analytics_id) {
        try {
            String query = "update analytics_analytics set "+column+"=? where id=?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, resultPath);
            preparedStatement.setInt(2, analytics_id);
            preparedStatement.executeUpdate();

        } catch (SQLException ex) {
            Logger.getLogger(DBSynchronizer.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("ERROR SEVERE"+ ex);
        }
    }//EoM updateParent   
    
            /*
     * Updates an Item
     */
    public void updateLindaAnalyticsModel(String resultPath, int analytics_id) {
        try {
            String query = "update analytics_analytics set model=? where id=?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, resultPath);
            preparedStatement.setInt(2, analytics_id);
            preparedStatement.executeUpdate();

        } catch (SQLException ex) {
            Logger.getLogger(DBSynchronizer.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("ERROR SEVERE"+ ex);
        }
    }//EoM updateParent   
    
                /*
     * Updates an Item
     */
    public void updateLindaAnalyticsModelReadable(String resultPath, int analytics_id) {
        try {
            String query = "update analytics_analytics set modelReadable=? where id=?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, resultPath);
            preparedStatement.setInt(2, analytics_id);
            preparedStatement.executeUpdate();

        } catch (SQLException ex) {
            Logger.getLogger(DBSynchronizer.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("ERROR SEVERE"+ ex);
        }
    }//EoM updateParent   
   
}//EoC