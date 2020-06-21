package com.udacity.project4.locationreminders.savereminder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.navArgs
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

class SaveReminderFragment : BaseFragment() {

    // Get the view model this time as a single to be shared with the other fragment.
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding

    private val args by navArgs<SaveReminderFragmentArgs>()
    private lateinit var reminderData: ReminderDataItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_save_reminder,
            container,
            false
        )
        binding.lifecycleOwner = this
        binding.viewModel = _viewModel

        setDisplayHomeAsUpEnabled(true)

        initListeners()

        return binding.root
    }

    private fun initView() {
        args.reminderData?.let {
            reminderData = it
            _viewModel.apply {
                reminderTitle.postValue(it.title)
                reminderDescription.postValue(it.description)
                reminderSelectedLocationStr.postValue(it.location)
                latitude.postValue(it.latitude)
                longitude.postValue(it.longitude)
            }
        }
    }

    private fun initListeners() {
        binding.selectedLocation.setOnClickListener {
            // Navigate to the other fragment to get the user location.
            _viewModel.navigationCommand.value = NavigationCommand.To(
                SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment()
            )
        }

        binding.saveReminder.setOnClickListener {
            // TODO: Use the user entered reminder details to add a geofencing request.
            if (!::reminderData.isInitialized) {
                reminderData = ReminderDataItem(
                    _viewModel.reminderTitle.value,
                    _viewModel.reminderDescription.value,
                    _viewModel.reminderSelectedLocationStr.value,
                    _viewModel.latitude.value,
                    _viewModel.longitude.value
                )
            } else {
                reminderData.apply {
                    title = _viewModel.reminderTitle.value
                    description = _viewModel.reminderDescription.value
                    location = _viewModel.reminderSelectedLocationStr.value
                    latitude = _viewModel.latitude.value
                    longitude = _viewModel.longitude.value
                }
            }
            _viewModel.validateAndSaveReminder(reminderData)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }
}
