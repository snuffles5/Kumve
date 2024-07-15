package com.example.mykumve.ui.trip

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.mykumve.R
import com.example.mykumve.data.data_classes.Equipment
import com.example.mykumve.data.model.Trip
import com.example.mykumve.data.model.TripInvitation
import com.example.mykumve.data.model.User
import com.example.mykumve.databinding.TravelManagerViewBinding
import com.example.mykumve.ui.viewmodel.SharedTripViewModel
import com.example.mykumve.ui.viewmodel.TripViewModel
import com.example.mykumve.util.ImagePickerUtil
import com.example.mykumve.util.ShareLevel
import com.example.mykumve.util.UserManager
import com.example.mykumve.util.Utility.timestampToString
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class TripManager : Fragment() {
    val TAG = TripManager::class.java.simpleName
    private var _binding: TravelManagerViewBinding? = null
    private val binding get() = _binding!!
    private val tripViewModel: TripViewModel by activityViewModels()
    private val sharedViewModel: SharedTripViewModel by activityViewModels()
    private var currentUser: User? = null

    private var startDate: Long? = null
    private var endDate: Long? = null
    private lateinit var imagePickerUtil: ImagePickerUtil


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "On view created")
        // Logic to determine if it's a new trip creation

        // Restore data if available
        sharedViewModel.trip.value?.let { trip ->
            binding.tripImage.setImageURI(trip.image?.toUri())
            binding.nameTrip.setText(trip.title)
            binding.description.setText(trip.description.toString())
            binding.dateStartPick.text = timestampToString(trip.gatherTime)
            binding.dateEndPick.text = timestampToString(trip.endDate)
        }

        sharedViewModel.isEditingExistingTrip = true

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            sharedViewModel.isEditingExistingTrip = false
            findNavController().navigate(R.id.action_travelManager_to_mainScreenManager)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = TravelManagerViewBinding.inflate(inflater, container, false)
        Log.d(TAG, "On create view")

        if (UserManager.isLoggedIn()) {
            currentUser = UserManager.getUser()
        } else {
            // Handle the case where the user is not logged in
            Toast.makeText(requireContext(), R.string.please_log_in, Toast.LENGTH_SHORT).show()
        }

        imagePickerUtil = ImagePickerUtil(this) { uri ->
            binding.tripImage.setImageURI(uri)
        }

        binding.dateStartBtn.setOnClickListener {
            showDateTimePicker(true)
        }

        binding.dateEndBtn.setOnClickListener {
            showDateTimePicker(false)
        }

        //equipment list:
        binding.equipmentListBtn.setOnClickListener {
            cacheTrip()
            findNavController().navigate(R.id.action_travelManager_to_equipmentFragment)
        }

        //Partner list
        binding.PartnersBtn.setOnClickListener {
            cacheTrip()
            findNavController().navigate(R.id.action_travelManager_to_partnerListFragment)
        }

        binding.NextBtn.setOnClickListener {
            currentUser?.let { user ->
                if (verifyTripForm()) {
                    cacheTrip()
                    viewLifecycleOwner.lifecycleScope.launch {
                        repeatOnLifecycle(Lifecycle.State.STARTED) {
                            sharedViewModel.trip.collect { trip ->
                                if (trip != null) {
                                    findNavController().navigate(R.id.action_travelManager_to_routeManager)
                                    return@collect
                                }
                            }
                        }
                    }
                }
            } ?: run {
                Toast.makeText(requireContext(), R.string.please_log_in, Toast.LENGTH_SHORT).show()
            }
        }

        binding.tripImage.setOnClickListener {
            imagePickerUtil.pickImage()
        }
        return binding.root
    }

    private fun verifyTripForm(): Boolean {
        if (binding.nameTrip.text.toString().isBlank()) {
            Toast.makeText(requireContext(), R.string.title_is_required, Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun cacheTrip() {
        currentUser?.let { user ->
            Log.d(TAG, "Caching trip." + if (sharedViewModel.isCreatingTripMode) " Creating trip mode" else " Selecting existing trip")
            val tempTrip = formToTripObject(user)
            if (sharedViewModel.isCreatingTripMode){
                sharedViewModel.setPartialTrip(tempTrip)
            } else {
                sharedViewModel.selectExistingTrip(tempTrip)
            }
        }
    }

    private fun showDateTimePicker(isStartDate: Boolean, eventLength: Int = 4 * 60 * 60 * 1000) {
        val c = Calendar.getInstance()

        // Set date picker to allow only future dates
        val dateListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            val timeListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                val calendar = Calendar.getInstance()
                calendar.set(year, month, dayOfMonth, hourOfDay, minute)

                if (isStartDate) {
                    if (calendar.timeInMillis < System.currentTimeMillis()) {
                        Toast.makeText(
                            requireContext(),
                            "Start date cannot be before current date",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@OnTimeSetListener
                    }
                    startDate = calendar.timeInMillis
                    binding.StartView.text = SimpleDateFormat(
                        "dd/MM/yyyy HH:mm",
                        Locale.getDefault()
                    ).format(calendar.time)

                    // Automatically set end date to 4 hours later
                    endDate = startDate!! + eventLength
                    binding.EndView.text = SimpleDateFormat(
                        "dd/MM/yyyy HH:mm",
                        Locale.getDefault()
                    ).format(Date(endDate!!))
                } else {
                    if (startDate == null) {
                        Toast.makeText(
                            requireContext(),
                            "Please select a start date first",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@OnTimeSetListener
                    }
                    if (calendar.timeInMillis < startDate!!) {
                        Toast.makeText(
                            requireContext(),
                            "End date cannot be before start date",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@OnTimeSetListener
                    }
                    val diff = calendar.timeInMillis - startDate!!
                    if (diff < 3600000) {
                        Toast.makeText(
                            requireContext(),
                            "End date must be at least 1 hour after start date",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@OnTimeSetListener
                    }
                    endDate = calendar.timeInMillis
                    binding.EndView.text = SimpleDateFormat(
                        "dd/MM/yyyy HH:mm",
                        Locale.getDefault()
                    ).format(calendar.time)
                }
            }
            TimePickerDialog(
                requireContext(),
                timeListener,
                c.get(Calendar.HOUR_OF_DAY),
                c.get(Calendar.MINUTE),
                true
            ).show()
        }

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            dateListener,
            c.get(Calendar.YEAR),
            c.get(Calendar.MONTH),
            c.get(Calendar.DAY_OF_MONTH)
        )

        // Limit date picker to future dates only
        datePickerDialog.datePicker.minDate = System.currentTimeMillis()

        if (!isStartDate && startDate != null) {
            // Limit end date picker to dates after the start date
            datePickerDialog.datePicker.minDate = startDate!!
        }

        datePickerDialog.show()
    }

    private fun formToTripObject(
        user: User,
        equipmentList: List<Equipment>? = null,
        participantList: List<User>? = null,
        invitationList: List<TripInvitation>? = null,

        ): Trip {
        val title = binding.nameTrip.text.toString()
        val description = binding.description.text.toString()
        val gatherTime = startDate ?: sharedViewModel.trip.value?.gatherTime
        val endTime = endDate ?: sharedViewModel.trip.value?.endDate
        val equipments = equipmentList?.takeIf { it.isNotEmpty() }?.toMutableList()
            ?: sharedViewModel.trip.value?.equipment?.toMutableList()

        val participants =
            participantList?.takeIf { it.isNotEmpty() }?.toMutableList() ?: mutableListOf(user)

        val invitations = invitationList?.takeIf { it.isNotEmpty() }?.toMutableList()
            ?: sharedViewModel.trip.value?.invitations?.takeIf { it.isNotEmpty() }?.toMutableList()
            ?: mutableListOf()

        val photo = imagePickerUtil.getImageUri().toString().takeIf { it != "null" } ?: sharedViewModel.trip.value?.image
        val notes = null

        // Create a new Trip object with the provided details
        val trip = Trip(
            title = title,
            gatherTime = gatherTime,
            endDate = endTime,
            description = description,
            notes = notes,
            participants = participants,
            invitations = invitations,
            equipment = equipments,
            userId = user.id,
            image = photo,
            tripInfoId = null,
            shareLevel = ShareLevel.PUBLIC,
        )
        return trip
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
