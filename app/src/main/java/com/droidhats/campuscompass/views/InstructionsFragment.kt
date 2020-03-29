package com.droidhats.campuscompass.views

import android.os.Bundle
import android.text.Html
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.droidhats.campuscompass.R
import com.droidhats.campuscompass.views.MapFragment.Companion.myArrayHolder
import kotlinx.android.synthetic.main.instructions_sheet_layout.nextArrow
import kotlinx.android.synthetic.main.instructions_sheet_layout.prevArrow
import kotlinx.android.synthetic.main.instructions_sheet_layout.arrayInstruction
import kotlinx.android.synthetic.main.instructions_sheet_layout.buttonCloseInstructions

class InstructionFragment : Fragment() {
    private lateinit var root : View
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

            //Todo: clear the map path + set the cardview to invisible
            myArrayHolder.clear() // Array is cleared
            findNavController().navigateUp()
        }
        stepByStepInstructions()
    }

    private fun stepByStepInstructions() {

        arrayInstruction.text = Html.fromHtml(myArrayHolder[0]).toString()
        prevArrow.visibility = View.INVISIBLE

        nextArrow.setOnClickListener {
            tracker++
            prevArrow.visibility = View.VISIBLE
            if(tracker < myArrayHolder.size) {
                arrayInstruction.text = Html.fromHtml(myArrayHolder[tracker]).toString()
            }
            if (tracker == myArrayHolder.size-1) {
                nextArrow.visibility = View.INVISIBLE
            }
        }
        prevArrow.setOnClickListener {
            tracker--
            nextArrow.visibility = View.VISIBLE
            if(tracker < myArrayHolder.size) {
                arrayInstruction.text = Html.fromHtml(myArrayHolder[tracker]).toString()
            }
            if (tracker == 0) {
                prevArrow.visibility = View.INVISIBLE
            }
        }
    }
}