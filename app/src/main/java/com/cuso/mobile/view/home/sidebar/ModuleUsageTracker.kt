package com.cuso.mobile.view.home.sidebar

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList

/**
 * Tracks MOST RECENTLY USED modules (Sales, Finance, Inventory, etc.) — not frequency.
 * Every time a module is opened, it jumps to the front of the list. Only the most
 * recent [MAX_TRACKED] modules are kept, so an older module can be pushed out entirely
 * once enough newer ones have been clicked.
 *
 * Backed by a Compose SnapshotStateList so any composable reading getRecentlyUsed()
 * (via derivedStateOf) recomposes live, the instant a new click reorders things —
 * no need to close/reopen the panel.
 *
 * Persisted to SharedPreferences (as an ordered, comma-joined string) so the order
 * survives an app restart.
 */
object ModuleUsageTracker {
    private const val PREFS_NAME = "module_usage_prefs"
    private const val KEY_RECENT_ORDER = "recent_module_order"
    private const val MAX_TRACKED = 3

    // Most-recent-first list of module labels. Index 0 = most recently used.
    private val recentOrder: SnapshotStateList<String> = mutableStateListOf()
    private var isLoaded = false

    private fun ensureLoaded(context: Context) {
        if (isLoaded) return
        val p = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = p.getString(KEY_RECENT_ORDER, null)
        if (!saved.isNullOrBlank()) {
            recentOrder.addAll(saved.split(",").filter { it.isNotBlank() })
        }
        isLoaded = true
    }

    private fun persist(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_RECENT_ORDER, recentOrder.joinToString(","))
            .apply()
    }

    /**
     * Call this whenever a user opens a module. Moves it to the front, removing any
     * older occurrence, then trims the list down to [MAX_TRACKED] entries — so the
     * oldest module disappears once a 4th distinct one is clicked.
     */
    fun recordUsage(context: Context, moduleLabel: String) {
        ensureLoaded(context)

        recentOrder.remove(moduleLabel)      // drop any existing occurrence
        recentOrder.add(0, moduleLabel)       // push to the front (most recent)

        while (recentOrder.size > MAX_TRACKED) {
            recentOrder.removeAt(recentOrder.lastIndex)  // drop the oldest
        }

        persist(context)
    }

    /**
     * Returns module labels in most-recently-used order, filtered to only those still
     * present in [candidateLabels] (e.g. in case a module was removed from the menu).
     * Reading `recentOrder` here is what makes callers reactive — call this from a
     * `remember { derivedStateOf { ... } }` and it recomputes on every recordUsage().
     */
    fun getRecentlyUsed(context: Context, candidateLabels: List<String>, limit: Int = MAX_TRACKED): List<String> {
        ensureLoaded(context)
        return recentOrder
            .filter { it in candidateLabels }
            .take(limit)
    }
}