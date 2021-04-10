package com.prism.poc.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.prism.poc.HistoryAdapter
import com.prism.poc.PrismTrackerApplication
import com.prism.poc.R
import com.prism.poc.events.LocationHistoryEvent
import com.prism.poc.events.LocationHistoryUpdated
import com.prism.poc.ui.HistoryViewModelFactory
import kotlinx.coroutines.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class HistoryFragment : Fragment() {

    private val historyViewModel: HistoryViewModel by viewModels {
        HistoryViewModelFactory((PrismTrackerApplication.instance as PrismTrackerApplication).repository)
    }

    private var recyclerView: RecyclerView? = null
    private var swipeRefreshLayout: SwipeRefreshLayout? = null
    private var noHistoryLyt: ConstraintLayout? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {



        val root = inflater.inflate(R.layout.fragment_history, container, false)
        recyclerView = root.findViewById(R.id.recyclerView)
        noHistoryLyt = root.findViewById(R.id.no_history_lyt)
        noHistoryLyt?.setOnClickListener {
            refreshLocations()
        }
        swipeRefreshLayout = root.findViewById(R.id.swipe_container)
        swipeRefreshLayout?.setOnRefreshListener(OnRefreshListener { // Your code to refresh the list here.
            // Make sure you call swipeContainer.setRefreshing(false)
            // once the network request has completed successfully.
            refreshLocations()
        })

        // Configure the refreshing colors
        // Configure the refreshing colors
        swipeRefreshLayout?.setColorSchemeResources(
            android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_red_light
        )
        return root
    }

    private fun refreshLocations() {
        GlobalScope.launch {
            historyViewModel.getHistoryLocationsList()
            delay(2000)
            withContext(Dispatchers.Main){
                swipeRefreshLayout?.isRefreshing=false
            }
        }
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
        historyViewModel.locations?.observe(viewLifecycleOwner, Observer { list ->
            if (list!=null && list.isNotEmpty()) {
                val historyAdapter = HistoryAdapter(list.filter { it.address.isNotEmpty() })
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
        historyViewModel.getHistoryLocationsList()
    }

    private fun updateViewVisibility(isListAavilable: Boolean) {
        noHistoryLyt?.visibility = if (isListAavilable) View.GONE else View.VISIBLE
        recyclerView?.visibility = if (isListAavilable) View.VISIBLE else View.GONE
        swipeRefreshLayout?.visibility = if (isListAavilable) View.VISIBLE else View.GONE
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onLocationFetched(locations: LocationHistoryEvent) {
        // Add a marker in Sydney and move the camera
        historyViewModel.getHistoryLocationsList()
        EventBus.getDefault().removeStickyEvent(locations)
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onLocationHisrotyUpdated(locations: LocationHistoryUpdated) {
        historyViewModel.getHistoryLocationsList()
        EventBus.getDefault().removeStickyEvent(locations)
    }
}