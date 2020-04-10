package com.droidhats.campuscompass.views

import android.os.Bundle
import android.widget.Toast
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.droidhats.campuscompass.R


class SettingsFragment: PreferenceFragmentCompat() {
    /*
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.settings_fragment, container, false)
        val sideDrawerButton: ImageButton = root.findViewById(R.id.button_menu)
        sideDrawerButton.setOnClickListener {
            requireActivity().findViewById<DrawerLayout>(R.id.drawer_layout).openDrawer(
                GravityCompat.START
            )
        }

     return root
    }
*/
    //loads view from res/xml/settings_preference.xml
    //want to try to integrate the Preference view into the settings_fragment layouyt
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_preferences, rootKey)

        val stairsPreference: SwitchPreferenceCompat? = findPreference("stairs")
        val escalatorsPreference: SwitchPreferenceCompat? = findPreference("escalators")
        val elevatorsPreference: SwitchPreferenceCompat? = findPreference("elevators")

        stairsPreference?.setOnPreferenceChangeListener(object : Preference.OnPreferenceChangeListener {
            override fun onPreferenceChange(
                preference: Preference?,
                newValue: Any
            ): Boolean {
                if (newValue as Boolean) {
                    //needed to update the switch UI
                    stairsPreference.setChecked(true)
                    Toast.makeText(context, "Stairs are on", Toast.LENGTH_LONG).show()
                } else {
                    stairsPreference.setChecked(false)
                    Toast.makeText(context, "Stairs are off", Toast.LENGTH_LONG).show()
                }
                return true
            }
        })
    }
}