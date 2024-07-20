package il.co.erg.mykumve.ui.notifications

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import il.co.erg.mykumve.data.model.TripInvitation
import il.co.erg.mykumve.databinding.ItemNotificationBinding
import il.co.erg.mykumve.ui.viewmodel.TripViewModel
import il.co.erg.mykumve.util.TripInvitationStatus
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class TripInvitationAdapter(
    private var invitations: List<TripInvitation>,
    var tripViewModel: TripViewModel,
    private val lifecycleOwner: LifecycleOwner
) : RecyclerView.Adapter<TripInvitationAdapter.TripInvitationViewHolder>() {

    val TAG = TripInvitationAdapter::class.java.simpleName

    inner class TripInvitationViewHolder(private val binding: ItemNotificationBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(invitation: TripInvitation) {
            tripViewModel.fetchTripById(invitation.tripId) // Ensure this is called to fetch data

            lifecycleOwner.lifecycleScope.launch {
                lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    tripViewModel.trip.collectLatest { trip ->
                        if (trip != null) {
                            Log.d(TAG, "Binding trip invitation, tripId ${invitation.tripId}")
                            binding.invitationTitle.text = trip.title
                            binding.invitationStatus.text = "Status: ${invitation.status}"

                            if (invitation.status in setOf(
                                    TripInvitationStatus.APPROVED,
                                    TripInvitationStatus.REJECTED
                                )
                            ) {
                                binding.acceptButton.isEnabled = false
                                binding.rejectButton.isEnabled = false
                            } else {
                                binding.acceptButton.setOnClickListener {
                                    handleAccept(invitation)
                                }
                                binding.rejectButton.setOnClickListener {
                                    handleReject(invitation)
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripInvitationViewHolder {
        val binding =
            ItemNotificationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TripInvitationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TripInvitationViewHolder, position: Int) {
        holder.bind(invitations[position])
    }

    override fun getItemCount(): Int {
        return invitations.size
    }

    fun updateInvitations(newInvitations: List<TripInvitation>) {
        invitations = newInvitations
        notifyDataSetChanged()
    }

    private fun handleAccept(invitation: TripInvitation) {
        Log.d(TAG, "ACCEPT selected - Responding to trip invitation")
        invitation.status = TripInvitationStatus.APPROVED
        respondToTripInvitation(invitation)
    }


    private fun handleReject(invitation: TripInvitation) {
        Log.d(TAG, "REJECT selected - Responding to trip invitation")
        invitation.status = TripInvitationStatus.REJECTED
        respondToTripInvitation(invitation)
    }

    private fun respondToTripInvitation(invitation: TripInvitation) {
        tripViewModel.respondToTripInvitation(invitation) { result ->
            Log.d(TAG, if (result) "TripInvitation Accepted" else "TripInvitation Rejected")
        }
    }

}