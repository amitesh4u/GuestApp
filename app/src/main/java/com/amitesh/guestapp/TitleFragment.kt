package com.amitesh.guestapp


import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.amitesh.guestapp.databinding.FragmentTitleBinding
import com.amitesh.guestapp.model.TitleViewModel


/**
 * A simple [Fragment] subclass.
 *
 */
class TitleFragment : Fragment() {

    /**
     * Lazily initialize our [TitleViewModel].
     */
    private val titleViewModel: TitleViewModel by lazy {
        ViewModelProviders.of(this).get(TitleViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //Inflate using Data binding
        val binding = DataBindingUtil.inflate<FragmentTitleBinding>(
            inflater, R.layout.fragment_title, container, false
        )

        // Allows Data Binding to Observe LiveData with the lifecycle of this Fragment
        binding.lifecycleOwner = this

        // Giving the binding access to the OverviewViewModel
        binding.titleViewModel = titleViewModel

        //Tell Android that our Fragment has a menu
        setHasOptionsMenu(true)

        /* The complete onClickListener with Navigation using createNavigateOnClickListener.
        Navigate through NavDirections i.e. SafeARgs
        */
        binding.smartKeyButton.setOnClickListener(
            Navigation.createNavigateOnClickListener(
                TitleFragmentDirections.actionTitleFragmentToSmartKeyFragment()
            )
        )

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
