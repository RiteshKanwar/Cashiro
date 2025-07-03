package com.ritesh.cashiro.services
import android.app.PendingIntent
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import androidx.annotation.RequiresApi
import com.ritesh.cashiro.MainActivity
import com.ritesh.cashiro.R

class AddTransactionTileService : TileService() {

    companion object {
        private const val TAG = "AddTransactionTile"
        private const val EXTRA_NAVIGATE_TO = "navigate_to"
        private const val EXTRA_IS_UPDATE = "is_update_transaction"
        private const val EXTRA_DEFAULT_TAB = "defaultTab"
        private const val ADD_TRANSACTION_VALUE = "ADD_TRANSACTION"
    }

    override fun onStartListening() {
        super.onStartListening()
        Log.d(TAG, "Tile started listening")
        updateTile()
    }

    override fun onStopListening() {
        super.onStopListening()
        Log.d(TAG, "Tile stopped listening")
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onClick() {
        super.onClick()
        Log.d(TAG, "Tile clicked")

        try {
            // Create intent to launch MainActivity with ADD_TRANSACTION navigation
            val intent = Intent(this, MainActivity::class.java).apply {
                // Use your existing navigation system
                putExtra(EXTRA_NAVIGATE_TO, ADD_TRANSACTION_VALUE)
                putExtra(EXTRA_IS_UPDATE, false)

                // Optional: Set default tab to "Expense" for quick expense entry
                // You can change this to "Income" or "Transfer" based on your preference
                putExtra(EXTRA_DEFAULT_TAB, "Expense")

                // These flags ensure the app opens properly from quick settings
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
            }

            // Create PendingIntent from the Intent
            val pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Provide visual feedback by briefly changing tile state
            updateTileState(Tile.STATE_ACTIVE)

            // Launch the activity and collapse quick settings using PendingIntent
            startActivityAndCollapse(pendingIntent)

            Log.d(TAG, "Activity launched successfully")

        } catch (e: Exception) {
            Log.e(TAG, "Error launching activity", e)
            // Reset tile state on error
            updateTileState(Tile.STATE_UNAVAILABLE)
        }
    }

    /**
     * Updates the tile appearance and state
     */
    private fun updateTile() {
        qsTile?.let { tile ->
            tile.apply {
                // Set tile label
                label = getString(R.string.tile_add_transaction_label)

                // Set tile icon
                icon = Icon.createWithResource(
                    this@AddTransactionTileService,
                    R.drawable.wallet_bulk
                )

                // Set tile state to inactive by default
                state = Tile.STATE_INACTIVE

                // Add subtitle for better UX
                subtitle = getString(R.string.tile_add_transaction_subtitle)

                // Set content description for accessibility
                contentDescription = getString(R.string.tile_add_transaction_content_description)
            }

            // Update the tile
            tile.updateTile()
        }
    }

    /**
     * Updates only the tile state (for providing visual feedback)
     */
    private fun updateTileState(newState: Int) {
        qsTile?.let { tile ->
            tile.state = newState
            tile.updateTile()

            // Reset to inactive state after a short delay
            if (newState == Tile.STATE_ACTIVE) {
                android.os.Handler(mainLooper).postDelayed({
                    qsTile?.let {
                        it.state = Tile.STATE_INACTIVE
                        it.updateTile()
                    }
                }, 1000) // Reset after 1 second
            }
        }
    }

    override fun onTileAdded() {
        super.onTileAdded()
        Log.d(TAG, "Tile added to Quick Settings")
        updateTile()
    }

    override fun onTileRemoved() {
        super.onTileRemoved()
        Log.d(TAG, "Tile removed from Quick Settings")
        // Cleanup if needed
    }
}