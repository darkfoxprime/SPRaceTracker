/**
 * 
 */
package com.github.jearls.SPRaceTracker;

import java.io.File;
import java.io.IOException;
import java.util.prefs.Preferences;

import com.github.jearls.SPRaceTracker.data.DataStore;
import com.github.jearls.SPRaceTracker.data.DataStoreException;
import com.github.jearls.SPRaceTracker.data.Driver;
import com.github.jearls.SPRaceTracker.data.DriverObserver;
import com.github.jearls.SPRaceTracker.data.EBeanDataStore;
import com.github.jearls.SPRaceTracker.data.Finish;
import com.github.jearls.SPRaceTracker.data.Team;
import com.github.jearls.SPRaceTracker.data.TeamObserver;
import com.github.jearls.SPRaceTracker.data.importexport.CSVExporter;
import com.github.jearls.SPRaceTracker.data.importexport.ImporterExporterException;
import com.github.jearls.SPRaceTracker.data.importexport.XMLExporter;
import com.github.jearls.SPRaceTracker.ui.DatabaseDirectoryChooser;

/**
 * This is the main driver class for the SPRaceTracker application. It
 * initializes the data system and creates the UI views and controllers that
 * drive the application logic.
 * 
 * @author jearls
 *
 */
public class SPRaceTracker implements TeamObserver, DriverObserver {
    public static final String APP_NAME = "SPRaceTracker";

    public void teamChanged(Team team, TeamElement changed) {
        System.err.println("Team changed: " + changed);
    }

    public void driverChanged(Driver driver, DriverElement changed) {
        System.err.println("Driver changed: " + changed);
    }

    public static void main(String[] args) throws Exception {
        Preferences prefNode =
                Preferences.userNodeForPackage(SPRaceTracker.class);
        File dbPath = null;
        String dbPathName = prefNode.get("DatabasePath", null);
        if (dbPathName != null) {
            dbPath = new File(dbPathName);
        }
        if (dbPath == null || !dbPath.isDirectory()) {
            dbPath = DatabaseDirectoryChooser.chooseDatabaseDirectory(APP_NAME);
        }
        try {
            dbPathName = dbPath.getCanonicalPath();
        } catch (NullPointerException e) {
            System.err.println("User cancelled; exiting");
            System.exit(0);
        } catch (IOException e) {
            System.err.println("Error accessing database path: " + e);
            System.exit(1);
        }

        DataStore dataStore = null;
        try {
            dataStore = new EBeanDataStore(dbPath);

            // new XMLImporter(dataStore, new
            // File(System.getProperty("user.home", ".")
            // + File.separator + APP_NAME + ".xml"), APP_NAME).importData();
            // System.exit(0);
            try {
                new CSVExporter(new File(System.getProperty("user.home", ".")
                        + File.separator + APP_NAME + ".zip")).exportData(
                        dataStore, Finish.class);
                new XMLExporter(new File(System.getProperty("user.home", ".")
                        + File.separator + APP_NAME + ".xml"), APP_NAME)
                        .exportData(dataStore, Finish.class);
            } catch (ImporterExporterException e) {
                System.err.println("Error exporting data store: " + e);
            }
        } catch (DataStoreException e) {
            System.err.println("Error initializing data store: " + e);
            System.exit(1);
        }
    }
}
