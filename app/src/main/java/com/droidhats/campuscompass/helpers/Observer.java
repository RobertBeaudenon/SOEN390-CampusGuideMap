package com.droidhats.campuscompass.helpers;

/**
 * Observer interface used to implement the update method that will update the map based on a zoom level
 *
 * Please see the other class {@link com.droidhats.campuscompass.helpers.Subject reference.
 */
public interface Observer {
    /**
     * Update method to be implemented by concrete observer
     * @param mapZoomLevel The map's zoom level
     */
    void update (float mapZoomLevel);
}
