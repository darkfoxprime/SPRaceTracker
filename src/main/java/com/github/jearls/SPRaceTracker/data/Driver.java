package com.github.jearls.SPRaceTracker.data;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import com.github.jearls.SPRaceTracker.data.DriverObserver.DriverElement;

/**
 * @author jearls
 */
@Entity
public class Driver {

    public enum DriverStatus {
        ACTIVE, RETIRED;
        public String toString() {
            String allcaps = super.toString();
            return allcaps.substring(0, 1) + allcaps.substring(1).toLowerCase();
        }
    }

    public static final long serialVersionUID = 1L;

    @Id
    UUID                     id;

    private String           name;
    private String           tag;
    private int              XP;
    private int              age;
    private int              injuries;
    private DriverStatus     status;
    @ManyToOne(cascade=CascadeType.ALL)
    private Team             team;

    // public List<RaceResults> results;

    
    // The observer handling code

    /**
     * The list of observers for this Driver.
     */
    @Transient
    Set<DriverObserver> observers = new HashSet<DriverObserver>();
    
    /**
     * Adds a new observer.
     * @param observer The object to be notified when this Driver changes.
     */
    public void addObserver(DriverObserver observer) {
        observers.add(observer);
    }
    
    /**
     * Removes an observer.
     * @param observer The object that should no longer be notified when this Driver changes.
     */
    public void removeObserver(DriverObserver observer) {
        observers.remove(observer);
    }
    
    /**
     * Notifies observers that this Driver has changed.
     * @param whatChanged What element in the driver has changed.
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
        this.team = team;
        this.notify(DriverElement.TEAM);
    }

}
