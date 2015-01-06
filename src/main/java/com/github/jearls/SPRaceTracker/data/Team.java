package com.github.jearls.SPRaceTracker.data;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import com.github.jearls.SPRaceTracker.data.TeamObserver.TeamElement;

/**
 * @author jearls
 */
@Entity
public class Team {
    public static final long serialVersionUID = 1L;

    @Id
    UUID                     id;

    private String           name;
    private String           tag;
    @OneToMany(mappedBy = "team")
    private List<Driver>     drivers;

    // public List<Season> seasons;

    // The observer handling code

    /**
     * The list of observers for this Team.
     */
    @Transient
    Set<TeamObserver> observers = null;
    
    /**
     * Adds a new observer.
     * @param observer The object to be notified when this team changes.
     */
    public void addObserver(TeamObserver observer) {
        if (observers == null) {
            observers = new HashSet<TeamObserver>();
        }
        observers.add(observer);
    }
    
    /**
     * Removes an observer.
     * @param observer The object that should no longer be notified when this team changes.
     */
    public void removeObserver(TeamObserver observer) {
        observers.remove(observer);
    }
    
    /**
     * Notifies observers that this team has changed.
     * @param whatChanged What element in the team has changed.
     */
    public void notify(TeamElement whatChanged) {
        for (TeamObserver observer : observers) {
            observer.teamChanged(this, whatChanged);
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
        this.notify(TeamElement.NAME);
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
        this.notify(TeamElement.TAG);
    }

    /**
     * @return the drivers
     */
    public List<Driver> getDrivers() {
        return drivers;
    }

    /**
     * @param drivers
     *            the drivers to set
     */
    public void setDrivers(List<Driver> drivers) {
        this.drivers = drivers;
        this.notify(TeamElement.DRIVERS);
    }
    
    public void addDriver(Driver driver) {
        driver.setTeam(this);
    }
}
