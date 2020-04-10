package com.droidhats.campuscompass.views

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Switch
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.droidhats.campuscompass.R

class SettingsFragment : Fragment() {
    private var totalCheck: Int = 3

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.settings_preferences, container, false)
        val sideDrawerButton: ImageButton = root.findViewById(R.id.button_menu)
        sideDrawerButton.setOnClickListener {
            requireActivity().findViewById<DrawerLayout>(R.id.drawer_layout).openDrawer(
                GravityCompat.START
            )
        }

        val stairsSwitch: Switch = root.findViewById(R.id.switch_settings_stairs)
        stairsSwitch.setOnCheckedChangeListener { _, isChecked ->
            switchOnCheck(stairsSwitch, isChecked)
        }

        val escalatorsSwitch: Switch = root.findViewById(R.id.switch_settings_escalators)
        escalatorsSwitch.setOnCheckedChangeListener { _, isChecked ->
            switchOnCheck(escalatorsSwitch, isChecked)
        }
        val elevatorsSwitch: Switch = root.findViewById(R.id.switch_settings_elevators)
        elevatorsSwitch.setOnCheckedChangeListener { _, isChecked ->
            switchOnCheck(elevatorsSwitch, isChecked)
        }

        return root
    }

    private fun switchOnCheck(switchButton: Switch, checked: Boolean) {
        if (totalCheck == 1 && !checked) {
            switchButton.isChecked = true
            val alertDialog = AlertDialog.Builder(activity)
            alertDialog.setIcon(R.mipmap.ic_launcher_round)
            alertDialog.setTitle("Error")
            alertDialog.setMessage("Please ensure at least one option is selected at all time")
            alertDialog.setPositiveButton("Ok") { _, _ ->
            }
            alertDialog.show()
        } else if (!checked) {
            switchButton.isChecked = false
            Toast.makeText(context, "The " + switchButton.text + " is OFF", Toast.LENGTH_LONG).show()
            totalCheck--
        } else {
            switchButton.isChecked = true
            Toast.makeText(context, "The " + switchButton.text + " is ON", Toast.LENGTH_LONG).show()
            totalCheck++
        }
    }
}