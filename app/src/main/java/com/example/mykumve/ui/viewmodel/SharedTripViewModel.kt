package com.example.mykumve.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewModelScope
import com.example.mykumve.data.data_classes.Equipment
import com.example.mykumve.data.model.Trip
import com.example.mykumve.data.model.TripInfo
import com.example.mykumve.data.model.TripInvitation
import com.example.mykumve.util.Result
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SharedTripViewModel : ViewModel() {
    val TAG = SharedTripViewModel::class.java.simpleName

    private val _selectedExistingTripWithInfo = MutableStateFlow<TripWithInfo?>(null)
    private lateinit var tripViewModel: TripViewModel
    var isCreatingTripMode: Boolean = false
    var isEditingExistingTrip: Boolean = false
    var isNavigatedFromTripList: Boolean = false

    private val _partialTrip = MutableStateFlow<Trip?>(null)
    private val _partialTripInfo = MutableStateFlow<TripInfo?>(null)

    private val _operationResult = MutableSharedFlow<Result>()
    val operationResult: SharedFlow<Result> = _operationResult
    val trip: StateFlow<Trip?>
        get() = if (isCreatingTripMode) _partialTrip.asStateFlow() else _selectedExistingTripWithInfo.value?.trip?.let {
            MutableStateFlow(
                it
            ).asStateFlow()
        } ?: MutableStateFlow(null).asStateFlow()
    val tripInfo: StateFlow<TripInfo?>
        get() = if (isCreatingTripMode) _partialTripInfo.asStateFlow() else _selectedExistingTripWithInfo.value?.tripInfo?.let {
            MutableStateFlow(
                it
            ).asStateFlow()
        } ?: MutableStateFlow(null).asStateFlow()

    fun setPartialTrip(trip: Trip) {
        if (_partialTrip.value != trip) {
            Log.v(TAG, "Setting partial trip ${trip.title} ${trip.id}")
            _partialTrip.value = trip
        } else {
            Log.v(TAG, "_partialTrip and trip is the same ${trip.title} ${trip.id}")
        }
    }

    fun setPartialTripInfo(tripInfo: TripInfo) {
        if (_partialTripInfo.value != tripInfo) {
            Log.v(TAG, "Setting partial trip info ${tripInfo.title} ${tripInfo.id}")
            _partialTripInfo.value = tripInfo
        } else {
            Log.v(
                TAG,
                "_partialTripInfo and trip info is the same ${tripInfo.title} ${tripInfo.id}"
            )
        }
    }


    fun addInvitation(invitation: TripInvitation) {
        _partialTrip.value?.invitations?.add(invitation)
        _partialTrip.value = _partialTrip.value // Notify observers of the change
    }

    fun initTripViewModel(viewModelStoreOwner: ViewModelStoreOwner) {
        tripViewModel = ViewModelProvider(viewModelStoreOwner).get(TripViewModel::class.java)
    }

    fun selectExistingTripWithInfo(tripWithInfo: TripWithInfo) {
        isEditingExistingTrip = true
        if (_selectedExistingTripWithInfo.value != tripWithInfo) {
            Log.d(
                TAG,
                "Selecting existing trip with info. title: ${tripWithInfo.trip.title}, id: ${tripWithInfo.trip.id}"
            )
            _selectedExistingTripWithInfo.value = tripWithInfo
        } else {
            Log.v(
                TAG,
                "Not selecting same existing trip with info. title: ${tripWithInfo.trip.title}, id: ${tripWithInfo.trip.id}"
            )
        }
    }

    fun updateEquipment(equipment: List<Equipment>?) {
        viewModelScope.launch {
            try {
                val currentTrip = trip.value?.copy(equipment = equipment?.toMutableList())
                if (currentTrip != null && !isCreatingTripMode) {
                    tripViewModel.updateTrip(currentTrip)
                    _operationResult.emit(Result(true, "Equipment updated successfully"))
                } else {
                    _operationResult.emit(Result(false, "Trip is null or in creating mode"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error with update equipment \n${e.message}")
                _operationResult.emit(Result(false, "Error with update equipment: ${e.message}"))
            }
        }

    }
    fun resetNewTripState() {
        if (!isEditingExistingTrip) {
            Log.d(
                TAG,
                "Resetting new trip state: ${_selectedExistingTripWithInfo.value?.trip?.title}"
            )
            _selectedExistingTripWithInfo.value = null
            _partialTrip.value = null
            _partialTripInfo.value = null
            isCreatingTripMode = true
        }
    }
}
