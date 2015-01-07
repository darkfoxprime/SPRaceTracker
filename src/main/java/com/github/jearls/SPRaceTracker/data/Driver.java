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

import com.github.jearls.SPRaceTracker.data.DriverObserver.DriverElement;

/**
 * @author jearls
 */
@Entity
public class Driver {

    public enum DriverStatus {
        ACTIVE, RETIRED;
        @Override
        public String toString() {
            String allcaps = super.toString();
            return allcaps.substring(0, 1) + allcaps.substring(1).toLowerCase();
        }
    }

    // When adding new fields or changing fields, make sure to update
    // serialVersionUID. Also make sure to update or implement the
    // "updateDataStore(DataStore, long)" class method!
    public static final long serialVersionUID = 1L;

    @Id
    private UUID             id;

    private String           name;
    private String           tag;
    private int              XP;
    private int              age;
    private int              injuries;
    private DriverStatus     status;
    @ManyToOne(cascade = CascadeType.ALL)
    private Team             team;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "driver")
    private List<Finish>     finishes;

    // public List<Finish> results;

    // The observer handling code

    /**
     * The list of observers for this Driver.
     */
    @Transient
    Set<DriverObserver>      observers        = new HashSet<DriverObserver>();

    /**
     * Adds a new observer.
     * 
     * @param observer
     *            The object to be notified when this Driver changes.
     */
    public void addObserver(DriverObserver observer) {
        observers.add(observer);
    }

    /**
     * Removes an observer.
     * 
     * @param observer
     *            The object that should no longer be notified when this Driver
     *            changes.
     */
    public void removeObserver(DriverObserver observer) {
        observers.remove(observer);
    }

    /**
     * Notifies observers that this Driver has changed.
     * 
     * @param whatChanged
     *            What element in the driver has changed.
     */
    public void notify(DriverElement whatChanged) {
        for (DriverObserver observer : observers) {
            observer.driverChanged(this, whatChanged);
        }
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
        this.notify(DriverElement.NAME);
    }

    /**
     * @return the tag
     */
    public String getTag() {
        return tag;
    }

    /**
     * @param tag
     *            the tag to set
     */
    public void setTag(String tag) {
        this.tag = tag;
        this.notify(DriverElement.TAG);
    }

    /**
     * @return the XP
     */
    public int getXP() {
        return XP;
    }

    /**
     * @param XP
     *            the XP to set
     */
    public void setXP(int XP) {
        this.XP = XP;
        this.notify(DriverElement.XP);
    }

    /**
     * @return the age
     */
    public int getAge() {
        return age;
    }

    /**
     * @param age
     *            the age to set
     */
    public void setAge(int age) {
        this.age = age;
        this.notify(DriverElement.AGE);
    }

    /**
     * @return the injuries
     */
    public int getInjuries() {
        return injuries;
    }

    /**
     * @param injuries
     *            the injuries to set
     */
    public void setInjuries(int injuries) {
        this.injuries = injuries;
        this.notify(DriverElement.INJURIES);
    }

    /**
     * @return the status
     */
    public DriverStatus getStatus() {
        return status;
    }

    /**
     * @param status
     *            the status to set
     */
    public void setStatus(DriverStatus status) {
        this.status = status;
        this.notify(DriverElement.STATUS);
    }

    /**
     * @return the team
     */
    public Team getTeam() {
        return team;
    }

    /**
     * @param team
     *            the team to set
     */
    public void setTeam(Team team) {
        Team oldTeam = this.team;
        this.team = team;
        this.notify(DriverElement.TEAM);
        if (oldTeam != null) {
            oldTeam.removeDriver(this);
        }
        if (team != null) {
            team.addDriver(this);
        }
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
        this.notify(DriverElement.FINISHES);
    }

    /**
     * Adds a finish to the driver, if the finish is not already in the driver.
     * Also tells the finish to add the driver, if the finish does not already
     * contain this driver.
     * 
     * @param finish
     *            The finish to add.
     */
    public void addFinish(Finish finish) {
        if (!this.finishes.contains(finish)) {
            this.finishes.add(finish);
            this.notify(DriverElement.FINISHES);
            if (finish.getDriver() == null || !finish.getDriver().equals(this)) {
                finish.setDriver(this);
            }
        }
    }

    /**
     * Removes a finish from the driver, if the finish is in the driver. Also
     * tells the finish to remove the driver, if the driver is in the finish.
     * 
     * @param finish
     *            The finish to remove.
     */
    public void removeFinish(Finish finish) {
        if (this.finishes.contains(finish)) {
            this.finishes.remove(finish);
            this.notify(DriverElement.FINISHES);
            if (finish.getDriver() != null && finish.getDriver().equals(this)) {
                finish.setDriver(null);
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
     * Returns true if "other" is a Driver and both Drivers have the same ID.
     * 
     * @param other
     *            The object to compare against
     * @return true if both Drivers have the same ID.
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object other) {
        if (other instanceof Driver) {
            return this.getId().equals(((Driver) other).getId());
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
