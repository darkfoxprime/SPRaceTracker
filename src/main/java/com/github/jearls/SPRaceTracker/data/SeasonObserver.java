package com.github.jearls.SPRaceTracker.data;

/**
 * @author jearls
 */
public interface SeasonObserver {

    /**
     * @author jearls Used to indicate which part of a Team has changed
     */
    public enum SeasonElement {
        NAME, TEAMS, RACES, SEASON_ORDER
    }

    /**
     * This method is called when some part of a Team has changed.
     * 
     * @param team
     *            The team being observed.
     * @param changed
     *            What element of the team was changed.
     */
    public void seasonChanged(Season season, SeasonElement changed);

}
