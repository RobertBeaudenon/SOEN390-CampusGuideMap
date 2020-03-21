package com.droidhats.campuscompass.helpers;

/**
 *Subject interface used implement attach, detach methods for observers and notifying observers
 *
 * Please see the other class {@link com.droidhats.campuscompass.helpers.Observer for reference.
 */
public interface Subject {

    void attach(Observer observer);

    void detach(Observer observer);

    void notifyObservers();
}
