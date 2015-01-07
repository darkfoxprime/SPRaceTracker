package com.github.jearls.SPRaceTracker.data;

/**
 * @author jearls
 */
public interface FinishObserver {

    /**
     *  Used to indicate which part of a Finish has changed
     * @author jearls
     */
    public enum FinishElement {
        RACE, PLACE, DRIVER, FINISHED, INJURED, RACES_MISSED
    }

    /**
     * This method is called when some part of a Finish has changed.
     * 
     * @param finish
     *            The results being observed.
     * @param changed
     *            What element of the results was changed.
     */
    public void finishChanged(Finish finish, FinishElement changed);

}
