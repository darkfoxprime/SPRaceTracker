package com.github.jearls.SPRaceTracker.data;

/**
 * @author jearls
 */
public interface DriverObserver {

    /**
     * @author jearls Used to indicate which part of a Driver has changed
     */
    public enum DriverElement {
        NAME, TAG, TEAM, RACES, XP, AGE, INJURIES, STATUS
    }

    /**
     * This method is called when some part of a Driver has changed.
     * 
     * @param driver
     *            The driver being observed.
     * @param changed
     *            What element of the driver was changed.
     */
    public void driverChanged(Driver driver, DriverElement changed);

}
