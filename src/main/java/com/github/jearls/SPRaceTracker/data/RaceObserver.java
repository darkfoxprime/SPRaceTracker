package com.github.jearls.SPRaceTracker.data;

/**
 * @author jearls
 */
public interface RaceObserver {

    /**
     * @author jearls Used to indicate which part of a Team has changed
     */
    public enum RaceElement {
        SEASON, RACE_NUMBER, COURSE_NAME, VALUE_MULTIPLIER, BY_WEEKS, FINISHERS
    }

    /**
     * This method is called when some part of a Team has changed.
     * 
     * @param team
     *            The team being observed.
     * @param changed
     *            What element of the team was changed.
     */
    public void raceChanged(Race race, RaceElement changed);

}
