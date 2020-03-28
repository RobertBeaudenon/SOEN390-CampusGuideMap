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

    private var startAndEnd: MutableLiveData<Pair<Float, Float>?> = MutableLiveData(null)

    fun setStartAndEnd(startAndEnd: Pair<Float, Float>) {
        this.startAndEnd.value = startAndEnd
    }

    fun getStartAndEnd(): Pair<Float, Float>? {
        val startAndEnd = this.startAndEnd.value
        this.startAndEnd.value = null
        return startAndEnd
    }

}