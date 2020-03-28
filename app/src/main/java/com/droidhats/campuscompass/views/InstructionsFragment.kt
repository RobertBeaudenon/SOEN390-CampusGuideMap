package com.droidhats.campuscompass.views

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import com.droidhats.campuscompass.R
import kotlinx.android.synthetic.main.instructions_sheet_layout.*

class InstructionFragment : Fragment() {
    private lateinit var root : View
    private val myArrayHolder = arrayListOf("One", "Two Is The Longest Text That Is Possible At The Very Moment", "Three", "Four", "Five", "Six")
    private var tracker: Int = 0

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
        //TODO: To delete as well as from the layout
        //instructionsStepsID.text = MapFragment.stepInsts
        stepByStepInstructions()
    }

    private fun stepByStepInstructions() {
        arrayInstruction.text = myArrayHolder[0]
        prevArrow.visibility = View.INVISIBLE

        nextArrow.setOnClickListener {
            tracker++
            prevArrow.visibility = View.VISIBLE
            if(tracker < myArrayHolder.size) {
                arrayInstruction.text = myArrayHolder[tracker]
            }
            if (tracker == myArrayHolder.size-1) {
                nextArrow.visibility = View.INVISIBLE
            }
        }
        prevArrow.setOnClickListener {
            tracker--
            nextArrow.visibility = View.VISIBLE
            if(tracker < myArrayHolder.size) {
                arrayInstruction.text = myArrayHolder[tracker]
            }
            if (tracker == 0) {
                prevArrow.visibility = View.INVISIBLE
            }
        }
    }
}