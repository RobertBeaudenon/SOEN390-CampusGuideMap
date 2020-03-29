package com.droidhats.campuscompass.views

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.Observer
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.droidhats.campuscompass.R
import com.droidhats.campuscompass.viewmodels.ShuttleViewModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.shuttle_campus_tab.*
import kotlinx.android.synthetic.main.shuttle_fragment.*

class ShuttleFragment : Fragment() {

    private lateinit var shuttleAdapter: ShuttleAdapter
    private lateinit var viewPager: ViewPager2

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.shuttle_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        shuttleAdapter = ShuttleAdapter(this)
        viewPager = view.findViewById(R.id.pager)
        viewPager.adapter = shuttleAdapter

        val tabLayout: TabLayout = view.findViewById(R.id.tab_layout)
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            if (position == 0)
                tab.text = "SGW TO LOY"
            else
                tab.text = "LOY TO SGW"
        }.attach()
    }
}

class ShuttleAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 2 // 1.SGW TO LOY, 2.LOY TO SGW

    override fun createFragment(position: Int): Fragment {
        val fragment = CampusTabFragment()
        fragment.arguments = Bundle().apply {
            putInt(ARG_OBJECT, position)
        }
        return fragment
    }
}

private const val ARG_OBJECT = "object"

class CampusTabFragment : Fragment() {

    private lateinit var viewModel: ShuttleViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.shuttle_campus_tab, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = ViewModelProviders.of(this).get(ShuttleViewModel::class.java)

        arguments?.takeIf { it.containsKey(ARG_OBJECT) }?.apply {
        if (getInt(ARG_OBJECT) == 0)
          observeSGWShuttle()
          else
            observeLOYShuttle()
        }
    }


    private fun observeSGWShuttle(){
        viewModel.getSGWShuttleTime().observe(viewLifecycleOwner , Observer {
            var times = ""
            for (i in it)
                times += i

            sgwPlaceholderTimes.text = times
        })
    }

    private fun observeLOYShuttle(){
        viewModel.getLoyolaShuttleTime().observe(viewLifecycleOwner , Observer {
            var times = ""
            for (i in it)
                times += i

            loyolaPlaceholderTimes.text = times
        })
    }

}

