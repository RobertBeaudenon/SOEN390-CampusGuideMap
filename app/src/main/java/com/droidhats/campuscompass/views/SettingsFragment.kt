package com.droidhats.campuscompass.views

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Switch
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.droidhats.campuscompass.R


class SettingsFragment : Fragment() {
    private var totalCheck: Int = 0
    private val preferenceOff = ArrayList<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.settings_fragment, container, false)
        val sideDrawerButton: ImageButton = root.findViewById(R.id.button_menu)
        sideDrawerButton.setOnClickListener {
            requireActivity().findViewById<DrawerLayout>(R.id.drawer_layout).openDrawer(
                GravityCompat.START
            )
        }
        totalCheck = 0

        populateSettingOffArray()

        currentSwitchStatus(root.findViewById(R.id.switch_settings_stairs), true)
        currentSwitchStatus(root.findViewById(R.id.switch_settings_escalators), true)
        currentSwitchStatus(root.findViewById(R.id.switch_settings_elevators), true)
        currentSwitchStatus(root.findViewById(R.id.switch_settings_washrooms), false)
        currentSwitchStatus(root.findViewById(R.id.switch_settings_printers), false)
        currentSwitchStatus(root.findViewById(R.id.switch_settings_fountains), false)
        currentSwitchStatus(root.findViewById(R.id.switch_settings_fireEscape), false)

        root.findViewById<Switch>(R.id.switch_settings_stairs).setOnCheckedChangeListener { _, isChecked ->
            performSwitchClick(root.findViewById(R.id.switch_settings_stairs), isChecked, true)
        }

        root.findViewById<Switch>(R.id.switch_settings_escalators).setOnCheckedChangeListener { _, isChecked ->
            performSwitchClick(root.findViewById(R.id.switch_settings_escalators), isChecked, true)
        }

        root.findViewById<Switch>(R.id.switch_settings_elevators).setOnCheckedChangeListener { _, isChecked ->
            performSwitchClick(root.findViewById(R.id.switch_settings_elevators), isChecked, true)
        }

        root.findViewById<Switch>(R.id.switch_settings_washrooms).setOnCheckedChangeListener { _, isChecked ->
            performSwitchClick(root.findViewById(R.id.switch_settings_washrooms), isChecked, false)
        }

        root.findViewById<Switch>(R.id.switch_settings_printers).setOnCheckedChangeListener { _, isChecked ->
            performSwitchClick(root.findViewById(R.id.switch_settings_printers), isChecked, false)
        }

        root.findViewById<Switch>(R.id.switch_settings_fountains).setOnCheckedChangeListener { _, isChecked ->
            performSwitchClick(root.findViewById(R.id.switch_settings_fountains), isChecked, false)
        }

        root.findViewById<Switch>(R.id.switch_settings_fireEscape).setOnCheckedChangeListener { _, isChecked ->
            performSwitchClick(root.findViewById(R.id.switch_settings_fireEscape), isChecked, false)
        }

        return root
    }

    private fun performSwitchClick(switchButton: Switch, checked: Boolean, restriction: Boolean) {
        val settingSwitchSharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE)
        val editor = settingSwitchSharedPref.edit()

        if (restriction && totalCheck == 1 && !checked) {
            switchButton.isChecked = true
            val alertDialog = AlertDialog.Builder(activity)
            alertDialog.setIcon(R.mipmap.ic_launcher_round)
            alertDialog.setTitle("Error")
            alertDialog.setMessage("Please ensure at least one of the following options is always selected: Stairs, Escalators, Elevators.")
            alertDialog.setPositiveButton("Ok") { _, _ ->
            }
            alertDialog.show()
        } else if (restriction && !checked) {
            switchButton.isChecked = false
            editor.putBoolean(switchButton.text.toString(), false).apply()
            totalCheck--
            settingOffArraySharedPref(switchButton.text.toString(), true)
        } else if (restriction && checked) {
            switchButton.isChecked = true
            editor.putBoolean(switchButton.text.toString(), true).apply()
            totalCheck++
            settingOffArraySharedPref(switchButton.text.toString(), false)
        } else if (!checked) {
            switchButton.isChecked = false
            editor.putBoolean(switchButton.text.toString(), false).apply()
            settingOffArraySharedPref(switchButton.text.toString(), true)
        } else {
            switchButton.isChecked = true
            editor.putBoolean(switchButton.text.toString(), true).apply()
            settingOffArraySharedPref(switchButton.text.toString(), false)
        }
    }

    private fun currentSwitchStatus(switchButton: Switch, restriction: Boolean) {
        val sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE)
        val default: Boolean = sharedPref.getBoolean(switchButton.text.toString(), true)
        switchButton.isChecked = default

        if(restriction && switchButton.isChecked) {
            totalCheck++
        }
    }


    private fun populateSettingOffArray() {
        val sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE)
        val set = sharedPref.getStringSet("settingOffArray", null)

        for (element in set!!) {
            preferenceOff.add(element)
        }
    }


    private fun settingOffArraySharedPref(valueToAddOrRemove: String, addingToArray: Boolean) {
        val settingOffArraySharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE)
        val editor = settingOffArraySharedPref.edit()

        if (addingToArray) {
            preferenceOff.add(valueToAddOrRemove)
        } else {
            preferenceOff.remove(valueToAddOrRemove)
        }

        val set = HashSet<String>()
        set.addAll(preferenceOff)
        editor.putStringSet("settingOffArray", set)
        editor.apply()
    }

}