package com.seemoo.openflow.utilities

import android.content.Context
import androidx.core.content.ContextCompat
import com.seemoo.openflow.R

/**
 * Utility class for mapping OpenFlowState values to their corresponding colors
 * and providing state-related information for the delta symbol.
 */
object DeltaStateColorMapper {

    /**
     * Data class representing the visual state of the delta symbol
     */
    data class DeltaVisualState(
        val state: OpenFlowState,
        val color: Int,
        val statusText: String,
        val colorHex: String
    )

    /**
     * Get the color resource ID for a given OpenFlowState
     */
    fun getColorResourceId(state: OpenFlowState): Int {
        return when (state) {
            OpenFlowState.IDLE -> R.color.delta_idle
            OpenFlowState.LISTENING -> R.color.delta_listening
            OpenFlowState.PROCESSING -> R.color.delta_processing
            OpenFlowState.SPEAKING -> R.color.delta_speaking
            OpenFlowState.ERROR -> R.color.delta_error
        }
    }

    /**
     * Get the resolved color value for a given OpenFlowState
     */
    fun getColor(context: Context, state: OpenFlowState): Int {
        val colorResId = getColorResourceId(state)
        return ContextCompat.getColor(context, colorResId)
    }

    /**
     * Get the status text for a given OpenFlowState
     */
    fun getStatusText(state: OpenFlowState): String {
        return when (state) {
            OpenFlowState.IDLE -> "Ready, tap delta to wake me up!"
            OpenFlowState.LISTENING -> "Listening..."
            OpenFlowState.PROCESSING -> "Processing..."
            OpenFlowState.SPEAKING -> "Speaking..."
            OpenFlowState.ERROR -> "Error"
        }
    }

    /**
     * Get the hex color string for a given OpenFlowState (for debugging/logging)
     */
    fun getColorHex(context: Context, state: OpenFlowState): String {
        val color = getColor(context, state)
        return String.format("#%08X", color)
    }

    /**
     * Get complete visual state information for a given OpenFlowState
     */
    fun getDeltaVisualState(context: Context, state: OpenFlowState): DeltaVisualState {
        return DeltaVisualState(
            state = state,
            color = getColor(context, state),
            statusText = getStatusText(state),
            colorHex = getColorHex(context, state)
        )
    }

    /**
     * Get all available states with their visual information
     */
    fun getAllStates(context: Context): List<DeltaVisualState> {
        return OpenFlowState.values().map { state ->
            getDeltaVisualState(context, state)
        }
    }

    /**
     * Check if a state represents an active operation (not idle or error)
     */
    fun isActiveState(state: OpenFlowState): Boolean {
        return when (state) {
            OpenFlowState.LISTENING, OpenFlowState.PROCESSING, OpenFlowState.SPEAKING -> true
            OpenFlowState.IDLE, OpenFlowState.ERROR -> false
        }
    }

    /**
     * Check if a state represents an error condition
     */
    fun isErrorState(state: OpenFlowState): Boolean {
        return state == OpenFlowState.ERROR
    }

    /**
     * Get the priority of a state for determining which state to display
     * when multiple conditions might be true. Higher numbers = higher priority.
     */
    fun getStatePriority(state: OpenFlowState): Int {
        return when (state) {
            OpenFlowState.ERROR -> 5      // Highest priority
            OpenFlowState.SPEAKING -> 4   // High priority
            OpenFlowState.LISTENING -> 3  // Medium-high priority
            OpenFlowState.PROCESSING -> 2 // Medium priority
            OpenFlowState.IDLE -> 1       // Lowest priority
        }
    }
}