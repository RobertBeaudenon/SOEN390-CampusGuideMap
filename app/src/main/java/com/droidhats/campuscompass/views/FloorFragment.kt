package com.droidhats.campuscompass.views

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
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
import java.io.InputStream


class FloorFragment : Fragment() {

    private lateinit var viewModel: FloorViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var root: View = inflater.inflate(R.layout.floor_fragment, container, false)

        val inputStream: InputStream = requireContext().assets.open("hall8.svg")
        val svg: SVG = SVG.getFromInputStream(inputStream)
        var imageView: ImageView = root.findViewById(R.id.floormap)
        svg.setDocumentWidth(imageView.layoutParams.width.toFloat())
        val bitmap = Bitmap.createBitmap(imageView.layoutParams.width, imageView.layoutParams.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawARGB(0, 255, 255, 255)
        svg.renderToCanvas(canvas)
        imageView.setImageDrawable(BitmapDrawable(getResources(), bitmap))

        var randomPathBtn: Button = root.findViewById(R.id.randomPathBtn)

        randomPathBtn.setOnClickListener{it ->
            val inputStream: InputStream = requireContext().assets.open("hall8.svg")
            val file: String = inputStream.bufferedReader().use { it.readText() }
            val mapProcessor: ProcessMap = ProcessMap()
            mapProcessor.readSVGFromString(file)
            val svg: SVG = SVG.getFromString(mapProcessor.getSVGString())
            var imageView: ImageView = root.findViewById(R.id.floormap)
            svg.setDocumentWidth(imageView.layoutParams.width.toFloat())
            val bitmap = Bitmap.createBitmap(imageView.layoutParams.width, imageView.layoutParams.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawARGB(0, 255, 255, 255)
            svg.renderToCanvas(canvas)
            imageView.setImageDrawable(BitmapDrawable(getResources(), bitmap))
        }

        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(FloorViewModel::class.java)
    }

}
