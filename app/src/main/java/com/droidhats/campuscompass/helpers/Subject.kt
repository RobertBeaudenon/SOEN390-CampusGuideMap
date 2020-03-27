package com.droidhats.campuscompass.helpers

/**
 * Subject interface used implement attach, detach methods for observers and notifying observers
 *
 * Please see the other class {@link com.droidhats.campuscompass.helpers.Observer for reference.
 */
interface Subject {

    fun attach(observer: Observer?)

    fun detach(observer: Observer?)

    fun notifyObservers()
}