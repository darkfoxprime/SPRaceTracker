package com.github.jearls.SPRaceTracker.data;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import com.github.jearls.SPRaceTracker.data.RaceObserver.RaceElement;

/**
 * @author jearls
 */
@Entity
public class Race {
    // When adding new fields or changing fields, make sure to update
    // serialVersionUID. Also make sure to update or implement the
    // "updateDataStore(DataStore, long)" class method!
    public static final long serialVersionUID = 1L;

    @Id
    private UUID             id;

    @ManyToOne(cascade = CascadeType.ALL)
    private Season           season;
    private int              raceNumber;
    private String           courseName;
    private int              valueMultiplier;
    private int              byWeeks;
    @OneToMany(mappedBy = "race", cascade = CascadeType.ALL)
    private List<Finish>     finishes;

    // The observer handling code

    /**
     * The list of observers for this Race.
     */
    @Transient
    Set<RaceObserver>        observers        = new HashSet<RaceObserver>();

    /**
     * Adds a new observer.
     * 
     * @param observer
     *            The object to be notified when this race changes.
     */
    public void addObserver(RaceObserver observer) {
        observers.add(observer);
    }

    /**
     * Removes an observer.
     * 
     * @param observer
     *            The object that should no longer be notified when this race
     *            changes.
     */
    public void removeObserver(RaceObserver observer) {
        observers.remove(observer);
    }

    /**
     * Notifies observers that this race has changed.
     * 
     * @param whatChanged
     *            What element in the race has changed.
     */
    public void notify(RaceElement whatChanged) {
        for (RaceObserver observer : observers) {
            observer.raceChanged(this, whatChanged);
        }
    }

    /**
     * @return the season
     */
    public Season getSeason() {
        return season;
    }

    /**
     * @param season
     *            the season to set
     */
    public void setSeason(Season season) {
        Season oldSeason = this.season;
        this.season = season;
        this.notify(RaceElement.SEASON);
        if (oldSeason != null) {
            oldSeason.removeRace(this);
        }
        if (season != null) {
            season.addRace(this);
        }
    }

    /**
     * @return the raceNumber
     */
    public int getRaceNumber() {
        return raceNumber;
    }

    /**
     * @param raceNumber
     *            the raceNumber to set
     */
    public void setRaceNumber(int raceNumber) {
        this.raceNumber = raceNumber;
        this.notify(RaceElement.RACE_NUMBER);
    }

    /**
     * @return the courseName
     */
    public String getCourseName() {
        return courseName;
    }

    /**
     * @param courseName
     *            the courseName to set
     */
    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    /**
     * @return the valueMultiplier
     */
    public int getValueMultiplier() {
        return valueMultiplier;
    }

    /**
     * @param valueMultiplier
     *            the valueMultiplier to set
     */
    public void setValueMultiplier(int valueMultiplier) {
        this.valueMultiplier = valueMultiplier;
    }

    /**
     * @return the byWeeks
     */
    public int getByWeeks() {
        return byWeeks;
    }

    /**
     * @param byWeeks
     *            the byWeeks to set
     */
    public void setByWeeks(int byWeeks) {
        this.byWeeks = byWeeks;
    }

    /**
     * @return the finishes
     */
    public List<Finish> getFinishes() {
        return finishes;
    }

    /**
     * @param finishes
     *            the finishes to set
     */
    public void setFinishes(List<Finish> finishes) {
        this.finishes = finishes;
    }

    /**
     * Adds a finish to the race, if the finish is not already in the race. Also
     * tells the finish to set the race, if the finish's race is not already set
     * to this race.
     * 
     * @param finish
     *            The finish to add.
     */
    public void addFinish(Finish finish) {
        if (!this.finishes.contains(finish)) {
            this.finishes.add(finish);
            this.notify(RaceElement.FINISHERS);
            if (finish.getRace() == null || !finish.getRace().equals(this)) {
                finish.setRace(this);
            }
        }
    }

    /**
     * Removes a finish from the race, if the finish is in the race. Also tells
     * the finish to unset the race, if the finish's race is equal to this race.
     * 
     * @param finish
     *            The finish to remove.
     */
    public void removeFinish(Finish finish) {
        if (this.finishes.contains(finish)) {
            this.finishes.remove(finish);
            this.notify(RaceElement.FINISHERS);
            if (finish.getRace() != null && finish.getRace().equals(this)) {
                finish.setRace(null);
            }
        }
    }

    /**
     * @return the id
     */
    public UUID getId() {
        return id;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(UUID id) {
        this.id = id;
    }

    /**
     * Returns true if "other" is a Race and both Races have the same ID.
     * 
     * @param other
     *            The object to compare against
     * @return true if both Races have the same ID.
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object other) {
        if (other instanceof Race) {
            return this.getId().equals(((Race) other).getId());
        } else
            return false;
    }

    /**
     * Updates the data store if a previous version was identified by the
     * ObjectVersion mapper.
     * 
     * @param store
     *            The DataStore to update.
     * @param previousVersion
     *            The previous version found.
     */
    public static void updateDataStore(DataStore store, long previousVersion) {
        // nothing to update ... yet!
    }

}
