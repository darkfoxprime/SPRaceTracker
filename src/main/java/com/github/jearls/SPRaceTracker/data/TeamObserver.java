package com.github.jearls.SPRaceTracker.data;

/**
 * @author jearls
 */
public interface TeamObserver {

    /**
     * @author jearls Used to indicate which part of a Team has changed
     */
    public enum TeamElement {
        NAME, TAG, DRIVERS, SEASONS
    }

    /**
     * This method is called when some part of a Team has changed.
     * 
     * @param team
     *            The team being observed.
     * @param changed
     *            What element of the team was changed.
     */
    public void teamChanged(Team team, TeamElement changed);

}
