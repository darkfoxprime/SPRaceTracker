package com.github.jearls.SPRaceTracker.data;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import com.github.jearls.SPRaceTracker.data.FinishObserver.FinishElement;

/**
 * @author jearls
 */
@Entity
public class Finish {

    // When adding new fields or changing fields, make sure to update
    // serialVersionUID.
    public static final long serialVersionUID = 1L;

    @Id
    private UUID             id;

    @OneToMany(cascade = CascadeType.ALL)
    private Race             race;
    private int              place;
    @OneToMany(cascade = CascadeType.ALL)
    private Driver           driver;
    private boolean          finished;
    private boolean          injured;
    private int              weeksMissed;

    // The observer handling code

    /**
     * The list of observers for this Finish.
     */
    @Transient
    Set<FinishObserver>      observers        = new HashSet<FinishObserver>();

    /**
     * Adds a new observer.
     * 
     * @param observer
     *            The object to be notified when this Finish changes.
     */
    public void addObserver(FinishObserver observer) {
        observers.add(observer);
    }

    /**
     * Removes an observer.
     * 
     * @param observer
     *            The object that should no longer be notified when this Finish
     *            changes.
     */
    public void removeObserver(FinishObserver observer) {
        observers.remove(observer);
    }

    /**
     * Notifies observers that this Finish has changed.
     * 
     * @param whatChanged
     *            What element in the driver has changed.
     */
    public void notify(FinishElement whatChanged) {
        for (FinishObserver observer : observers) {
            observer.finishChanged(this, whatChanged);
        }
    }

    /**
     * @return the race
     */
    public Race getRace() {
        return race;
    }

    /**
     * @param race
     *            the race to set
     */
    public void setRace(Race race) {
        Race oldRace = this.race;
        this.race = race;
        this.notify(FinishElement.RACE);
        if (oldRace != null) {
            oldRace.removeFinish(this);
        }
        if (race != null) {
            race.addFinish(this);
        }
    }

    /**
     * @return the place
     */
    public int getPlace() {
        return place;
    }

    /**
     * @param place
     *            the place to set
     */
    public void setPlace(int place) {
        this.place = place;
        this.notify(FinishElement.PLACE);
    }

    /**
     * @return the driver
     */
    public Driver getDriver() {
        return driver;
    }

    /**
     * @param driver
     *            the driver to set
     */
    public void setDriver(Driver driver) {
        Driver oldDriver = this.driver;
        this.driver = driver;
        this.notify(FinishElement.DRIVER);
        if (oldDriver != null) {
            oldDriver.removeFinish(this);
        }
        if (driver != null) {
            driver.addFinish(this);
        }
    }

    /**
     * @return the finished
     */
    public boolean isFinished() {
        return finished;
    }

    /**
     * @param finished
     *            the finished to set
     */
    public void setFinished(boolean finished) {
        this.finished = finished;
        this.notify(FinishElement.FINISHED);
    }

    /**
     * @return the injured
     */
    public boolean isInjured() {
        return injured;
    }

    /**
     * @param injured
     *            the injured to set
     */
    public void setInjured(boolean injured) {
        this.injured = injured;
        this.notify(FinishElement.INJURED);
    }

    /**
     * @return the weeksMissed
     */
    public int getWeeksMissed() {
        return weeksMissed;
    }

    /**
     * @param weeksMissed
     *            the weeksMissed to set
     */
    public void setWeeksMissed(int weeksMissed) {
        this.weeksMissed = weeksMissed;
        this.notify(FinishElement.WEEKS_MISSED);
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
     * Returns true if "other" is a Finish and both RaceResultss have the same
     * ID.
     * 
     * @param other
     *            The object to compare against
     * @return true if both RaceResultss have the same ID.
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object other) {
        if (other instanceof Finish) {
            return this.getId().equals(((Finish) other).getId());
        } else
            return false;
    }

}
