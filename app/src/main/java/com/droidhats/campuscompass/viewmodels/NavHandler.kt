package com.droidhats.campuscompass.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel

abstract class NavHandler(application: Application) : AndroidViewModel(application) {
    private var next: NavHandler? = null
    private var prev: NavHandler? = null

    fun setNext(navHandler: NavHandler): NavHandler {
        next = navHandler
        navHandler.prev = this
        return next!!
    }

    fun executeNextNav() {
        displayNav()
        next?.executeNextNav()
    }

    abstract fun displayNav()
}
