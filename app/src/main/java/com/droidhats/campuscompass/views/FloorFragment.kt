package com.droidhats.campuscompass.views

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.VectorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
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
        var root = inflater.inflate(R.layout.floor_fragment, container, false)

        var randomPathBtn: Button = root.findViewById(R.id.randomPathBtn)
        val inputStream: InputStream = requireContext().assets.open("hall8.svg")
        randomPathBtn.setOnClickListener{it ->
            val svg: SVG = SVG.getFromInputStream(inputStream)
            var imageView: ImageView = root.findViewById(R.id.floormap)
            svg.setDocumentWidth(imageView.width.toFloat())
            val bitmap = Bitmap.createBitmap(imageView.width, imageView.height, Bitmap.Config.ARGB_8888)
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
