package com.amitesh.guestapp.fragment


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.amitesh.guestapp.R

/**
 * A simple [Fragment] subclass.
 *
 */
class HelpAndSupportFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_help_and_support, container, false)
    }


}
