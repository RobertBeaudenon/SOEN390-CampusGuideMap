package com.droidhats.campuscompass.views

import android.app.AlertDialog
import android.content.Context
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
    private var totalCheck: Int = 0

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
        switchChecked(root.findViewById(R.id.switch_settings_stairs))
        switchChecked(root.findViewById(R.id.switch_settings_escalators))
        switchChecked(root.findViewById(R.id.switch_settings_elevators))
        switchChecked(root.findViewById(R.id.switch_settings_washrooms))
        switchChecked(root.findViewById(R.id.switch_settings_printers))
        switchChecked(root.findViewById(R.id.switch_settings_fountains))
        switchChecked(root.findViewById(R.id.switch_settings_fireEscape))

        root.findViewById<Switch>(R.id.switch_settings_stairs).setOnCheckedChangeListener { _, isChecked ->
            switchToggle(root.findViewById(R.id.switch_settings_stairs), isChecked)
        }

        root.findViewById<Switch>(R.id.switch_settings_escalators).setOnCheckedChangeListener { _, isChecked ->
            switchToggle(root.findViewById(R.id.switch_settings_escalators), isChecked)
        }

        root.findViewById<Switch>(R.id.switch_settings_elevators).setOnCheckedChangeListener { _, isChecked ->
            switchToggle(root.findViewById(R.id.switch_settings_elevators), isChecked)
        }

        root.findViewById<Switch>(R.id.switch_settings_washrooms).setOnCheckedChangeListener { _, isChecked ->
            switchToggle(root.findViewById(R.id.switch_settings_washrooms), isChecked)
        }

        root.findViewById<Switch>(R.id.switch_settings_printers).setOnCheckedChangeListener { _, isChecked ->
            switchToggle(root.findViewById(R.id.switch_settings_printers), isChecked)
        }

        root.findViewById<Switch>(R.id.switch_settings_fountains).setOnCheckedChangeListener { _, isChecked ->
            switchToggle(root.findViewById(R.id.switch_settings_fountains), isChecked)
        }

        root.findViewById<Switch>(R.id.switch_settings_fireEscape).setOnCheckedChangeListener { _, isChecked ->
            switchToggle(root.findViewById(R.id.switch_settings_fireEscape), isChecked)
        }

        return root
    }

    private fun switchToggle(switchButton: Switch, checked: Boolean) {
        val sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE)
        val editor = sharedPref.edit()

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
            editor.putBoolean(switchButton.text.toString(), false).apply()
            Toast.makeText(context, "The " + switchButton.text + " is OFF", Toast.LENGTH_LONG).show()
            totalCheck--
        } else {
            switchButton.isChecked = true
            editor.putBoolean(switchButton.text.toString(), true).apply()
            Toast.makeText(context, "The " + switchButton.text + " is ON", Toast.LENGTH_LONG).show()
            totalCheck++
        }
    }

    private fun switchChecked(switchButton: Switch) {
        val sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE)
        val default: Boolean = sharedPref.getBoolean(switchButton.text.toString(), true)
        switchButton.isChecked = default

        if(switchButton.isChecked) {
            totalCheck++
        }
    }
}