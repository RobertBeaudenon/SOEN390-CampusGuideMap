package com.droidhats.campuscompass.repositories

import androidx.lifecycle.MutableLiveData

class IndoorNavigationRepository private constructor() {

    companion object {
        // Singleton instantiation
        private var instance: IndoorNavigationRepository? = null

        fun getInstance() =
                instance
                        ?: synchronized(this) {
                            instance
                                    ?: IndoorNavigationRepository().also { instance = it }
                        }
    }

    private var startAndEnd: MutableLiveData<Pair<String, String>?> = MutableLiveData(null)

    fun setStartAndEnd(startAndEnd: Pair<String, String>) {
        this.startAndEnd.value = startAndEnd
    }

    fun getStartAndEnd(): Pair<String, String>? {
        val startAndEnd = this.startAndEnd.value
        this.startAndEnd.value = null
        return startAndEnd
    }

}