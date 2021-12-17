package edu.cccdci.opal.layoutwrapper

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class WrapAddressLinearLayoutManager(
    context: Context, orientation: Int, reverseLayout: Boolean
): LinearLayoutManager(context, orientation, reverseLayout) {

    override fun onLayoutChildren(
        recycler: RecyclerView.Recycler?, state: RecyclerView.State?
    ) {
        try {
            super.onLayoutChildren(recycler, state)
        } catch (e: IndexOutOfBoundsException) {
            e.printStackTrace()
        }
    }  // end of onLayoutChildren method

}  // end of WrapAddressLinearLayoutManager class