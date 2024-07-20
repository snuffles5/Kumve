package il.co.erg.mykumve.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import il.co.erg.mykumve.data.db.repository.TripInfoRepository
import il.co.erg.mykumve.data.db.repository.TripRepository
import il.co.erg.mykumve.data.db.repository.UserRepository
import il.co.erg.mykumve.data.model.Trip
import il.co.erg.mykumve.data.model.TripInfo
import il.co.erg.mykumve.util.Result
import il.co.erg.mykumve.data.model.TripInvitation
import il.co.erg.mykumve.data.data_classes.Equipment
import il.co.erg.mykumve.util.TripInvitationStatus
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TripViewModel(application: Application) : AndroidViewModel(application) {

    val TAG = TripViewModel::class.java.simpleName
    private val tripRepository: TripRepository = TripRepository(application)
    private val userRepository: UserRepository = UserRepository(application)
    private val tripInfoRepository: TripInfoRepository = TripInfoRepository(application)

    private val _trip = MutableStateFlow<Trip?>(null)
    val trip: StateFlow<Trip?> get() = _trip.asStateFlow()

    private val _tripInfo = MutableStateFlow<TripInfo?>(null)
    val tripInfo: StateFlow<TripInfo?> get() = _tripInfo.asStateFlow()

    private val _trips = MutableStateFlow<List<Trip>>(emptyList())
    private val _tripsInfo = MutableStateFlow<List<TripInfo>>(emptyList())
    val trips: StateFlow<List<Trip>> get() = _trips.asStateFlow()
    val tripsInfo: StateFlow<List<TripInfo>> get() = _tripsInfo.asStateFlow()

    private val _tripInvitations = MutableStateFlow<List<TripInvitation>>(emptyList())
    val tripInvitations: StateFlow<List<TripInvitation>> get() = _tripInvitations.asStateFlow()

    private val _tripsWithInfo = MutableStateFlow<List<TripWithInfo>>(emptyList())
    val tripsWithInfo: StateFlow<List<TripWithInfo>> get() = _tripsWithInfo.asStateFlow()

    private val _operationResult = MutableSharedFlow<Result?>()
    val operationResult: SharedFlow<Result?> get() = _operationResult

    fun fetchTripsByParticipantUserIdWithInfo(userId: Long) {
        viewModelScope.launch {
            val allTrips = tripRepository.getAllTrips()
                ?.firstOrNull() ?: emptyList()
            val tripsByParticipant = allTrips.filter { trip ->
                trip.participants?.any { it.id == userId } == true
            }

            val tripsWithInfoList = tripsByParticipant.map { trip ->
                val tripInfo =
                    trip.tripInfoId?.let { tripInfoRepository.getTripInfoById(it)
                        ?.firstOrNull() }
                TripWithInfo(trip, tripInfo)
            }
            _tripsWithInfo.emit(tripsWithInfoList)
        }
    }


//    fun fetchTripsByParticipantUserIdWithInfo(userId: Long) {
//        viewModelScope.launch {
//            val allTrips = tripRepository.getAllTrips()
//                ?.firstOrNull() ?: emptyList()
//            val tripsByParticipant = allTrips.filter { trip ->
//                trip.participants?.any { it.id == userId } == true
//            }
//
//            val tripsWithInfoList = tripsByParticipant.map { trip ->
//                val tripInfo =
//                    trip.tripInfoId?.let { tripInfoRepository.getTripInfoById(it).firstOrNull() }
//                TripWithInfo(trip, tripInfo)
//            }
//            _tripsWithInfo.emit(tripsWithInfoList)
//        }
//    }

    fun fetchTripById(id: Long) {
        viewModelScope.launch {
            tripRepository.getTripById(id)
                ?.stateIn(viewModelScope, SharingStarted.Lazily, null)
                ?.collectLatest { trip ->
                    _trip.value = trip
                }
        }
    }

/*
    fun fetchTripInfoByTripId(tripId: Long) {
        viewModelScope.launch {
            tripRepository.getTripById(tripId)
                ?.stateIn(viewModelScope, SharingStarted.Lazily, null)
                ?.collectLatest { trip ->
                    trip?.tripInfoId?.let { tripInfoId ->
                        tripInfoRepository.getTripInfoById(tripInfoId)
                            ?.stateIn(viewModelScope, SharingStarted.Lazily, null)
                            ?.collectLatest { tripInfo ->
                                _tripInfo.value = tripInfo
                            }
                    }
                }
        }
    }
*/

    fun fetchTripInfoById(id: Long) {
        viewModelScope.launch {
            tripInfoRepository.getTripInfoById(id)
                ?.stateIn(viewModelScope, SharingStarted.Lazily, null)
                ?.collectLatest { tripInfo ->
                    _tripInfo.value = tripInfo
                }
        }
    }

    fun fetchAllTrips() {
        viewModelScope.launch {
            try {
                tripRepository.getAllTrips()
                    ?.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
                    ?.collectLatest { trips ->
                        _trips.value = trips
                        _operationResult.emit(Result(true, "Fetched all trips successfully"))
                    }
            } catch (e: Exception) {
                _operationResult.emit(Result(false, "Failed to fetch trips: ${e.message}"))
            }
        }
    }

    fun fetchAllTripsInfo() {
        viewModelScope.launch {
            try {
                tripRepository.getAllTripInfo()
                    ?.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
                    ?.collectLatest { tripsInfo ->
                        _tripsInfo.value = tripsInfo
                        _operationResult.emit(Result(true, "Fetched all trips info successfully"))
                    }
            } catch (e: Exception) {
                _operationResult.emit(Result(false, "Failed to fetch trips info: ${e.message}"))
            }
        }
    }

    // Other methods remain unchanged

    fun observeTrips(lifecycleOwner: androidx.lifecycle.LifecycleOwner) {
        lifecycleOwner.lifecycleScope.launch {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                tripInfo.collectLatest { tripInfo ->
                    // Handle tripInfo updates here
                }
            }
        }
    }

    fun observeTripInfo(lifecycleOwner: androidx.lifecycle.LifecycleOwner) {
        lifecycleOwner.lifecycleScope.launch {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                trip.collectLatest { trip ->
                    // Handle trip updates here
                }
            }
        }
    }

    fun addTrip(trip: Trip) {
        viewModelScope.launch {
            tripRepository.insertTrip(trip)
        }
    }

    fun insertTripInfo(tripInfo: TripInfo) {
        viewModelScope.launch {
            tripInfoRepository.insertTripInfo(tripInfo)
        }
    }
    fun addTripWithInfo(
        trip: Trip,
        tripInfo: TripInfo,
        lifecycleOwner: LifecycleOwner
    ) {
        Log.d(TAG, "Starting addTripWithInfo")

        lifecycleOwner.lifecycleScope.launch {
            try {
                // First insert the trip to get its generated ID
                tripRepository.insertTripWithInfo(trip, tripInfo) { result ->
                    if (result.success) {
                        val insertedTripId = result.data?.get("tripId") as? Long ?: 0
                        Log.d(TAG, "insertedTripId $insertedTripId")
                        val updatedTrip = trip.copy(id = insertedTripId)

                        processAndSendUnsentInvitations(updatedTrip) { success ->
                            lifecycleOwner.lifecycleScope.launch {
                                if (success) {
                                    _operationResult.emit(result)
                                } else {
                                    _operationResult.emit(Result(false, "Failed to send some invitations."))
                                }
                            }
                        }
                    } else {
                        lifecycleOwner.lifecycleScope.launch {
                            _operationResult.emit(result)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to insert trip and trip info: ${e.message}")
                lifecycleOwner.lifecycleScope.launch {
                    _operationResult.emit(Result(false, "Failed to insert trip and trip info: ${e.message}"))
                }
            }
        }
    }

    fun updateTripWithInfo(
        trip: Trip,
        tripInfo: TripInfo,
        lifecycleOwner: LifecycleOwner
    ) {
        Log.d(TAG, "Starting updateTripWithInfo")

        lifecycleOwner.lifecycleScope.launch {
            try {
                tripRepository.updateTripWithInfo(trip, tripInfo) { result ->
                    lifecycleOwner.lifecycleScope.launch {
                        _operationResult.emit(result)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update trip and trip info: ${e.message}")
                lifecycleOwner.lifecycleScope.launch {
                    _operationResult.emit(Result(false, "Failed to update trip and trip info: ${e.message}"))
                }
            }
        }
    }

    fun updateTrip(trip: Trip) {
        viewModelScope.launch {
            tripRepository.updateTrip(trip)
        }
    }

    fun updateTripInfo(tripInfo: TripInfo) {
        viewModelScope.launch {
            val trip = tripRepository.getTripById(tripInfo.id)?.firstOrNull()
                ?: throw Exception("Trip not found")
            trip.tripInfoId = tripInfo.id
            tripRepository.updateTrip(trip)
            tripInfoRepository.updateTripInfo(tripInfo)
        }
    }

    fun deleteTrip(trip: Trip) {
        viewModelScope.launch {
            try {
                tripRepository.deleteTrip(trip)
                _operationResult.emit(Result(true, "Trip deleted successfully"))
                fetchAllTrips() // Ensure the trip list is updated after deletion
            } catch (e: Exception) {
                _operationResult.emit(Result(false, "Failed to delete trip: ${e.message}"))
            }
        }
    }


    fun deleteTripInfo(tripInfo: TripInfo) {
        viewModelScope.launch {
            tripInfoRepository.deleteTripInfo(tripInfo)
        }
    }

    fun fetchTripsByUserId(userId: Long) {
        viewModelScope.launch {
            tripRepository.getTripsByUserId(userId)?.collectLatest { trips ->
                _trips.value = trips
            }
        }
    }

//    fun fetchTripsByParticipantUserId(userId: Long) {
//        viewModelScope.launch {
//            tripRepository.getAllTrips()?.collectLatest { allTrips ->
//                _trips.value = allTrips.filter { trip -> trip.participants?.any { it.id == userId } == true }
//            }
//        }
//    }

    fun sendTripInvitation(tripId: Long, userId: Long, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            val invitation = TripInvitation(tripId = tripId, userId = userId)
            val result = tripRepository.sendTripInvitation(invitation)
            callback(result)
        }
    }

    private fun processAndSendUnsentInvitations(trip: Trip, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            var success = true
            trip.invitations.filter { it.status == TripInvitationStatus.UNSENT }.forEach { invitation ->
                val updatedInvitation = invitation.copy(tripId = trip.id)
                sendTripInvitation(updatedInvitation) { result ->
                    if (!result) {
                        success = false
                    }
                }
            }
            callback(success)
        }
    }

    private fun sendTripInvitation(invitation: TripInvitation, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            invitation.status = TripInvitationStatus.PENDING
            val result = tripRepository.sendTripInvitation(invitation)
            callback(result)
        }
    }

    fun respondToTripInvitation(invitation: TripInvitation, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val status = invitation.status
                Log.d(TAG, "Updating trip invitation with status: ${status}, TripId ${invitation.tripId}")
                tripRepository.updateTripInvitation(invitation)

                if (status == TripInvitationStatus.APPROVED) {
                    handleApprovedInvitation(invitation, callback)
                } else {
                    callback(true)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to respond to trip invitation: ${e.message}")
                callback(false)
            }
        }
    }

    private fun handleApprovedInvitation(invitation: TripInvitation, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            val trip = tripRepository.getTripById(invitation.tripId)?.firstOrNull()
            val user = userRepository.getUserById(invitation.userId)?.firstOrNull()

            if (trip != null && user != null) {
                trip.participants?.add(user)
                Log.d(TAG, "Adding user ${user.firstName} to trip participants ${trip.participants}")
                tripRepository.updateTrip(trip)
                callback(true)
            } else {
                val error = "Failed to respond to trip invitation"
                Log.e(TAG, if (trip == null) error + ", Trip is null" else error + ", user is null")
                callback(false)
            }
        }
    }

    fun fetchTripInvitationsByTripId(tripId: Long) {
        viewModelScope.launch {
            tripRepository.getTripInvitationsByTripId(tripId)?.collectLatest { invitations ->
                _tripInvitations.value = invitations
            }
        }
    }

    fun fetchTripInvitationsForUser(userId: Long) {
        viewModelScope.launch {
            tripRepository.getTripInvitationsForUser(userId)?.collectLatest { invitations ->
                _tripInvitations.emit(invitations)
            }
        }
    }

    fun deleteTripInvitation(invitation: TripInvitation) {
        viewModelScope.launch {
            tripRepository.deleteTripInvitation(invitation)
        }
    }

    fun hasPendingInvitations(userId: Long, tripId: Long, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            val invitations = tripRepository.getTripInvitationsByTripId(tripId)?.firstOrNull()
            val hasPending = invitations?.any { it.userId == userId && it.status == TripInvitationStatus.PENDING } == true
            callback(hasPending)
        }
    }

    fun addEquipment(tripId: Long, equipment: Equipment, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            val trip = tripRepository.getTripById(tripId)?.firstOrNull()
            if (trip != null) {
                trip.equipment = trip.equipment.orEmpty().toMutableList().apply { add(equipment) }
                tripRepository.updateTrip(trip)
                callback(true)
            } else {
                callback(false)
            }
        }
    }

    fun removeEquipment(tripId: Long, equipment: Equipment, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            val trip = tripRepository.getTripById(tripId)?.firstOrNull()
            if (trip != null) {
                trip.equipment = trip.equipment.orEmpty().toMutableList().apply { remove(equipment) }
                tripRepository.updateTrip(trip)
                callback(true)
            } else {
                callback(false)
            }
        }
    }

    fun updateEquipment(
        tripId: Long,
        oldEquipment: Equipment,
        newEquipment: Equipment,
        callback: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val trip = tripRepository.getTripById(tripId)?.firstOrNull()
            if (trip != null) {
                trip.equipment = trip.equipment.orEmpty().toMutableList().apply {
                    val index = indexOf(oldEquipment)
                    if (index != -1) {
                        set(index, newEquipment)
                    }
                }
                tripRepository.updateTrip(trip)
                callback(true)
            } else {
                callback(false)
            }
        }
    }
}

data class TripWithInfo(
    var trip: Trip,
    var tripInfo: TripInfo?
)