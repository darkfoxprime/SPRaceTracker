package com.github.jearls.SPRaceTracker.data;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import com.github.jearls.SPRaceTracker.data.TeamObserver.TeamElement;

/**
 * @author jearls
 */
@Entity
public class Team {
    // When adding new fields or changing fields, make sure to update
    // serialVersionUID.
    public static final long serialVersionUID = 1L;

    @Id
    private UUID             id;

    // Semantics: must be unique!
    private String           name;
    private String           tag;
    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL)
    private List<Driver>     drivers;

    @ManyToMany(cascade = CascadeType.ALL)
    private List<Season>     seasons;

    // The observer handling code

    /**
     * The list of observers for this Team.
     */
    @Transient
    Set<TeamObserver>        observers        = new HashSet<TeamObserver>();

    /**
     * Adds a new observer.
     * 
     * @param observer
     *            The object to be notified when this team changes.
     */
    public void addObserver(TeamObserver observer) {
        observers.add(observer);
    }

    /**
     * Removes an observer.
     * 
     * @param observer
     *            The object that should no longer be notified when this team
     *            changes.
     */
    public void removeObserver(TeamObserver observer) {
        observers.remove(observer);
    }

    /**
     * Notifies observers that this team has changed.
     * 
     * @param whatChanged
     *            What element in the team has changed.
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

    /**
     * Adds a driver to the team, if the driver is not already in the team. Also
     * tells the driver to set the team, if the driver's team is not already set
     * to this team.
     * 
     * @param driver
     *            The driver to add.
     */
    public void addDriver(Driver driver) {
        if (!this.drivers.contains(driver)) {
            this.drivers.add(driver);
            this.notify(TeamElement.DRIVERS);
            if (driver.getTeam() == null || !driver.getTeam().equals(this)) {
                driver.setTeam(this);
            }
        }
    }

    /**
     * Removes a driver from the team, if the driver is in the team. Also tells
     * the driver to unset the team, if the driver's team is equal to this team.
     * 
     * @param driver
     *            The driver to remove.
     */
    public void removeDriver(Driver driver) {
        if (this.drivers.contains(driver)) {
            this.drivers.remove(driver);
            this.notify(TeamElement.DRIVERS);
            if (driver.getTeam() != null && driver.getTeam().equals(this)) {
                driver.setTeam(null);
            }
        }
    }

    /**
     * @return the seasons
     */
    public List<Season> getSeasons() {
        return seasons;
    }

    /**
     * @param seasons
     *            the seasons to set
     */
    public void setSeasons(List<Season> seasons) {
        this.seasons = seasons;
        this.notify(TeamElement.SEASONS);
    }

    /**
     * Adds a season to the team, if the season is not already in the team. Also
     * tells the season to add the team, if the season does not already contain
     * this team.
     * 
     * @param season
     *            The season to add.
     */
    public void addSeason(Season season) {
        if (!this.seasons.contains(season)) {
            this.seasons.add(season);
            this.notify(TeamElement.SEASONS);
            if (season.getTeams() == null || !season.getTeams().contains(this)) {
                season.addTeam(this);
            }
        }
    }

    /**
     * Removes a season from the team, if the season is in the team. Also tells
     * the season to remove the team, if the team is in the season.
     * 
     * @param season
     *            The season to remove.
     */
    public void removeSeason(Season season) {
        if (this.seasons.contains(season)) {
            this.seasons.remove(season);
            this.notify(TeamElement.SEASONS);
            if (season.getTeams() != null && season.getTeams().contains(this)) {
                season.removeTeam(this);
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
     * Returns true if "other" is a Team and both Teams have the same ID.
     * 
     * @param other
     *            The object to compare against
     * @return true if both Teams have the same ID.
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object other) {
        if (other instanceof Team) {
            return this.getId().equals(((Team) other).getId());
        } else
            return false;
    }
}
