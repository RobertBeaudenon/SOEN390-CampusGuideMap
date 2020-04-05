package com.droidhats.campuscompass.NavHandler

import android.app.Application
import androidx.lifecycle.AndroidViewModel

abstract class NavHandler {
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
