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
import javax.persistence.OrderBy;
import javax.persistence.SequenceGenerator;
import javax.persistence.Transient;

import com.github.jearls.SPRaceTracker.data.SeasonObserver.SeasonElement;

/**
 * @author jearls
 */
@Entity
@IdentifiedBy("name")
public class Season {
    // When adding new fields or changing fields, make sure to update
    // serialVersionUID.
    public static final long serialVersionUID = 1L;

    @Id
    public UUID              id;

    public String            name;

    @ManyToMany(cascade = CascadeType.ALL)
    public List<Team>        teams;

    @OneToMany(mappedBy = "season", cascade = CascadeType.ALL)
    public List<Race>        races;

    @OrderBy
    @SequenceGenerator(name = "SeasonOrder", allocationSize = 1,
            initialValue = 1)
    public int               seasonOrder;

    // The observer handling code

    /**
     * The list of observers for this Team.
     */
    @Transient
    Set<SeasonObserver>      observers        = new HashSet<SeasonObserver>();

    /**
     * Adds a new observer.
     * 
     * @param observer
     *            The object to be notified when this season changes.
     */
    public void addObserver(SeasonObserver observer) {
        observers.add(observer);
    }

    /**
     * Removes an observer.
     * 
     * @param observer
     *            The object that should no longer be notified when this season
     *            changes.
     */
    public void removeObserver(SeasonObserver observer) {
        observers.remove(observer);
    }

    /**
     * Notifies observers that this season has changed.
     * 
     * @param whatChanged
     *            What element in the season has changed.
     */
    public void notify(SeasonElement whatChanged) {
        for (SeasonObserver observer : observers) {
            observer.seasonChanged(this, whatChanged);
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
        this.notify(SeasonElement.NAME);
    }

    /**
     * @return the teams
     */
    public List<Team> getTeams() {
        return teams;
    }

    /**
     * @param teams
     *            the teams to set
     */
    public void setTeams(List<Team> teams) {
        this.teams = teams;
        this.notify(SeasonElement.TEAMS);
    }

    /**
     * Adds a team to the season, if the team is not already in the season. Also
     * tells the team to set the season, if the team's season is not already set
     * to this season.
     * 
     * @param team
     *            The team to add.
     */
    public void addTeams(Team team) {
        if (!this.teams.contains(team)) {
            this.teams.add(team);
            this.notify(SeasonElement.TEAMS);
            if (team.getSeasons() == null || !team.getSeasons().contains(this)) {
                team.addSeasons(this);
            }
        }
    }

    /**
     * Removes a team from the season, if the team is in the season. Also tells
     * the team to remove the season, if the team includes the season.
     * 
     * @param team
     *            The team to remove.
     */
    public void removeTeams(Team team) {
        if (this.teams.contains(team)) {
            this.teams.remove(team);
            this.notify(SeasonElement.TEAMS);
            if (team.getSeasons() != null && team.getSeasons().contains(this)) {
                team.removeSeasons(this);
            }
        }
    }

    /**
     * @return the races
     */
    public List<Race> getRaces() {
        return races;
    }

    /**
     * @param races
     *            the races to set
     */
    public void setRaces(List<Race> races) {
        this.races = races;
        this.notify(SeasonElement.RACES);
    }

    /**
     * Adds a race to the season, if the race is not already in the season. Also
     * tells the race to set the season, if the race's season is not already set
     * to this season.
     * 
     * @param race
     *            The race to add.
     */
    public void addRace(Race race) {
        if (!this.races.contains(race)) {
            this.races.add(race);
            this.notify(SeasonElement.TEAMS);
            if (race.getSeason() == null || !race.getSeason().equals(this)) {
                race.setSeason(this);
            }
        }
    }

    /**
     * Removes a race from the season, if the race is in the season. Also tells
     * the race to remove the season, if the race includes the season.
     * 
     * @param race
     *            The race to remove.
     */
    public void removeRace(Race race) {
        if (this.races.contains(race)) {
            this.races.remove(race);
            this.notify(SeasonElement.TEAMS);
            if (race.getSeason() != null && race.getSeason().equals(this)) {
                race.setSeason(null);
            }
        }
    }

    /**
     * @return the seasonOrder
     */
    public int getSeasonOrder() {
        return seasonOrder;
    }

    /**
     * @param seasonOrder
     *            the seasonOrder to set
     */
    public void setSeasonOrder(int seasonOrder) {
        this.seasonOrder = seasonOrder;
        this.notify(SeasonElement.SEASON_ORDER);
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
     * Returns true if "other" is a Season and both Seasons have the same ID.
     * 
     * @param other
     *            The object to compare against
     * @return true if both Seasons have the same ID.
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object other) {
        if (other instanceof Season) {
            return this.getId().equals(((Season) other).getId());
        } else
            return false;
    }
}
