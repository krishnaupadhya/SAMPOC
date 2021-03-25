package com.prism.poc.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.prism.poc.GenericUtil
import com.prism.poc.R
import com.prism.poc.onboard.ui.login.LoginActivity

class ProfileFragment : Fragment() {

    private lateinit var profileViewModel: ProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        profileViewModel =
            ViewModelProvider(this).get(ProfileViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_profile, container, false)
        val tvUserName: TextView = root.findViewById(R.id.tv_user_name)
        profileViewModel.text.observe(viewLifecycleOwner, Observer {
            tvUserName.text = it
        })
        val viewLogout: CardView = root.findViewById(R.id.card_view_logout)
        viewLogout.setOnClickListener {
            showLogoutAlert()
        }
        return root
    }

    private fun showLogoutAlert() {
        if (activity == null) return
        val builder =
            AlertDialog.Builder(
                requireActivity(),
                R.style.AppCompatAlertDialogStyle
            )
        builder.setPositiveButton(
            getString(R.string.OK)
        ) { dialog, which ->
            activity?.let {
                GenericUtil.clearDataOnLogout()
                startActivity(Intent(it,LoginActivity::class.java))
                it.finishAffinity()
            }
        }
        builder.setNegativeButton(android.R.string.cancel, null)
        builder.setTitle( getString(R.string.signout_alert))
        builder.setMessage("")
        val dialog = builder.create()
        dialog.show()
    }

}