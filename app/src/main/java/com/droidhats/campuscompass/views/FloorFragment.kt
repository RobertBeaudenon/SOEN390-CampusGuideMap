package com.droidhats.campuscompass.views

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.DroidHats.ProcessMap
import com.caverock.androidsvg.SVG
import com.droidhats.campuscompass.R
import com.droidhats.campuscompass.viewmodels.FloorViewModel
import com.otaliastudios.zoom.ZoomImageView
import java.io.InputStream


class FloorFragment : Fragment() {

    private lateinit var viewModel: FloorViewModel
    private lateinit var root: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.floor_fragment, container, false)

        val inputStream: InputStream = requireContext().assets.open("hall8.svg")
        val svg: SVG = SVG.getFromInputStream(inputStream)
        setImage(svg)

        var randomPathBtn: Button = root.findViewById(R.id.randomPathBtn)

        randomPathBtn.setOnClickListener{it ->
            val inputStream: InputStream = requireContext().assets.open("hall8.svg")
            val file: String = inputStream.bufferedReader().use { it.readText() }
            val mapProcessor: ProcessMap = ProcessMap()
            mapProcessor.readSVGFromString(file)
            val svg: SVG = SVG.getFromString(mapProcessor.getSVGString())
            setImage(svg)
        }

        return root
    }

    fun setImage(svg: SVG) {
        val imageView: ZoomImageView = root.findViewById(R.id.floormap)
        val displayMetrics = DisplayMetrics()
        (context as Activity?)!!.windowManager
            .defaultDisplay
            .getMetrics(displayMetrics)

        svg.setDocumentWidth(displayMetrics.widthPixels.toFloat())
        val bitmap = Bitmap.createBitmap(displayMetrics.widthPixels, displayMetrics.widthPixels, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawARGB(0, 255, 255, 255)
        svg.renderToCanvas(canvas)
        imageView.setImageDrawable(BitmapDrawable(getResources(), bitmap))
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(FloorViewModel::class.java)
    }

}