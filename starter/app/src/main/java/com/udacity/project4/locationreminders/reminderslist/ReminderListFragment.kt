package com.udacity.project4.locationreminders.reminderslist

import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import com.firebase.ui.auth.AuthUI
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentRemindersBinding
import com.udacity.project4.utils.Navigator
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import com.udacity.project4.utils.setTitle
import com.udacity.project4.utils.setup
import org.koin.androidx.viewmodel.ext.android.viewModel

class ReminderListFragment : BaseFragment() {

    // Use Koin to retrieve the ViewModel instance.
    override val _viewModel: RemindersListViewModel by viewModel()
    private lateinit var binding: FragmentRemindersBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_reminders,
            container,
            false
        )
        binding.lifecycleOwner = this
        binding.viewModel = _viewModel

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(false)
        setTitle(getString(R.string.app_name))

        setupRecyclerView()
        initListeners()

        return binding.root
    }

    private fun setupRecyclerView() {
        val adapter = RemindersListAdapter {
            navigateToAddReminder(it)
        }

        // Setup the recycler view using the extension function.
        binding.remindersRecyclerView.setup(adapter)
    }

    private fun initListeners() {
        binding.addReminderFAB.setOnClickListener {
            navigateToAddReminder()
        }
    }

    private fun navigateToAddReminder(reminderData: ReminderDataItem? = null) {
        // Use the navigationCommand live data to navigate between the fragments.
        _viewModel.navigationCommand.postValue(
            NavigationCommand.To(
                ReminderListFragmentDirections.toSaveReminder(reminderData)
            )
        )
    }

    override fun onResume() {
        super.onResume()
        // Load the reminders list on the UI.
        _viewModel.loadReminders()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        // Display logout as menu item.
        inflater.inflate(R.menu.main_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout -> {
                AuthUI.getInstance()
                    .signOut(requireContext())
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            Navigator.navigateToAuthenticationActivity(requireActivity())
                        } else {
                            showSnackbar(getString(R.string.error_failed_to_log_out))
                        }
                    }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
