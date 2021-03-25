package com.prism.poc.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.prism.poc.HistoryAdapter
import com.prism.poc.R
import com.prism.poc.events.LocationHistoryEvent
import com.prism.poc.events.LocationHistoryUpdated
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class HistoryFragment : Fragment() {

    private var historyViewModel: HistoryViewModel? = null
    private var recyclerView: RecyclerView? = null
    private var noHistoryLyt: ConstraintLayout? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        historyViewModel =
            ViewModelProvider(this).get(HistoryViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_history, container, false)
        recyclerView = root.findViewById(R.id.recyclerView)
        noHistoryLyt = root.findViewById(R.id.no_history_lyt)
        return root
    }

    override fun onStart() {
        super.onStart()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    override fun onStop() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
        super.onStop()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        historyViewModel = ViewModelProvider(this).get(HistoryViewModel::class.java)
        historyViewModel?.locations?.observe(viewLifecycleOwner, Observer { list ->
            val filteredList = list.filter { it.address!=null && it.address.isNotEmpty() }.distinctBy { it.address }
            if (filteredList.isNotEmpty()) {
                val historyAdapter = HistoryAdapter(filteredList)
                recyclerView?.let {
                    val layoutManager = LinearLayoutManager(context)
                    it.layoutManager = layoutManager
                    it.itemAnimator = DefaultItemAnimator()
                    it.adapter = historyAdapter
                }
                updateViewVisibility(true)
            } else {
                updateViewVisibility(false)
            }
        })
        historyViewModel?.getHistoryLocationsList()
    }

    private fun updateViewVisibility(isListAavilable: Boolean) {
        noHistoryLyt?.visibility = if (isListAavilable) View.GONE else View.VISIBLE
        recyclerView?.visibility = if (isListAavilable) View.VISIBLE else View.GONE
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onLocationFetched(locations: LocationHistoryEvent) {
        // Add a marker in Sydney and move the camera
        historyViewModel?.setLocationHistory(locations.locationHistoryList)
        EventBus.getDefault().removeStickyEvent(locations)
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onLocationHisrotyUpdated(locations: LocationHistoryUpdated) {
        historyViewModel?.getHistoryLocationsList()
        EventBus.getDefault().removeStickyEvent(locations)
    }
}