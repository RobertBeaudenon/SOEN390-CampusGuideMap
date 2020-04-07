package com.droidhats.campuscompass

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.novoda.merlin.*

/**
 * This class has the objective of outlining the required methods for using the Merlin library
 * The library is used to observe the network connection status while using the application
 */
abstract class MerlinActivity : AppCompatActivity() {

    protected var merlin: Merlin? = null

    /**
     * Overrides the activity's OnCreate method to attach the Merlin object
     *
     * @param Bundle: the saved state of the application to pass between default Android methods
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        merlin = createMerlin()
    }

    /**
     * Abstract class signature to initialize the Merlin builder
     *
     * @returns the builder based on the context of the activity
     */
    protected abstract fun createMerlin(): Merlin?

    /**
     * Registers the Merlin object with a Connectable object.
     * Allows extending classes to simply override OnConnect without explicitly passing Connectable
     *
     * @param connectable: Connectable object to be registered with the Merlin object
     */
    protected open fun registerConnectable(connectable: Connectable?) {
        merlin!!.registerConnectable(connectable)
    }

    /**
     * Registers the Merlin object with a Disonnectable object.
     * Allows extending classes to simply override OnDisconnect without explicitly passing Disconnectable
     *
     * @param disconnectable: Disonnectable object to be registered with the Merlin object
     */
    protected open fun registerDisconnectable(disconnectable: Disconnectable?) {
        merlin!!.registerDisconnectable(disconnectable)
    }

    /**
     * Binding annotation that bings the Merlin object with Connectable or Disconnectable
     *
     * @param bindable: Bindable object to be registered with the Merlin object
     */
    protected open fun registerBindable(bindable: Bindable?) {
        merlin!!.registerBindable(bindable)
    }

    /**
     *  Overrides an activity method to bind the Merlin object once the activity has begun
     */
    override fun onStart() {
        super.onStart()
        merlin!!.bind()
    }

    /**
     *  Overrides an activity method to unbind the Merlin object once the activity is not in view
     */
    override fun onStop() {
        super.onStop()
        merlin!!.unbind()
    }
}