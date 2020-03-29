package com.droidhats.campuscompass.views

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.fragment.findNavController

import com.droidhats.campuscompass.R
import kotlinx.android.synthetic.main.instructions_sheet_layout.buttonCloseInstructions
import kotlinx.android.synthetic.main.instructions_sheet_layout.instructionsStepsID

class InstructionFragment : Fragment() {
     private lateinit var root : View
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.instructions_sheet_layout, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Handle the clicking of the closure of the instructions button. Should probably move from here later
        buttonCloseInstructions.setOnClickListener {
            instructionsStepsID.text = ""
            findNavController().navigateUp()
        }
        root.findViewById<TextView>(R.id.instructionsStepsID)
        instructionsStepsID.text = MapFragment.stepInsts
    }
}