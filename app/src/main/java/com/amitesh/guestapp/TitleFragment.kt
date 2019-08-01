package com.amitesh.guestapp


import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.amitesh.guestapp.databinding.FragmentTitleBinding


/**
 * A simple [Fragment] subclass.
 *
 */
class TitleFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //Inflate using Data binding
        val binding = DataBindingUtil.inflate<FragmentTitleBinding>(
            inflater, R.layout.fragment_title, container, false
        )

        //Tell Android that our Fragment has a menu
        setHasOptionsMenu(true)

        return binding.root
    }


    /*
        Override onCreateOptionsMenu and inflate our new menu resource
        using the provided menu inflater and menu structure.
     */
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.overflow_menu, menu)
    }

    /*
        Override onOptionsItemSelected to connect it to our NavigationUI.
        Add a backup option to get it handled by Android in case custom handler fails
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //!! asserts that an expression is non-null
        return NavigationUI.onNavDestinationSelected(item, view!!.findNavController())
                || super.onOptionsItemSelected(item)
    }
}
