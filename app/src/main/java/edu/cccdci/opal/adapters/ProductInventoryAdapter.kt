package edu.cccdci.opal.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import edu.cccdci.opal.R
import edu.cccdci.opal.dataclasses.Product
import edu.cccdci.opal.firestore.FirestoreClass
import edu.cccdci.opal.ui.activities.ProductEditorActivity
import edu.cccdci.opal.ui.fragments.ProductUnlistedFragment
import edu.cccdci.opal.ui.fragments.ProductViolationFragment
import edu.cccdci.opal.utils.Constants
import edu.cccdci.opal.utils.DialogClass
import edu.cccdci.opal.utils.GlideLoader
import edu.cccdci.opal.utils.UtilityClass

class ProductInventoryAdapter(
    private val fragment: Fragment,
    private val context: Context,
    private val activity: Activity,
    options: FirestoreRecyclerOptions<Product>
) : FirestoreRecyclerAdapter<Product,
        ProductInventoryAdapter.ProductInventoryViewHolder>(options) {

    // Nested Class to hold views from the target layout
    inner class ProductInventoryViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {
        // Get all the ids of views from product inventory item layout
        private val pdInvImage: ImageView = itemView
            .findViewById(R.id.iv_pd_inv_image)
        private val pdInvName: TextView = itemView
            .findViewById(R.id.tv_pd_inv_name)
        private val pdInvPriceUnit: TextView = itemView
            .findViewById(R.id.tv_pd_inv_price_unit)
        private val pdInvStock: TextView = itemView
            .findViewById(R.id.tv_pd_inv_stock)
        private val pdInvSales: TextView = itemView
            .findViewById(R.id.tv_pd_inv_sales)
        private val pdInvViews: TextView = itemView
            .findViewById(R.id.tv_pd_inv_views)
        private val pdInvMoreBtn: ImageView = itemView
            .findViewById(R.id.iv_more_menu_btn)

        // Function to set values to the specified views from product inventory item
        internal fun setProductInvData(product: Product) {
            // Store the product inventory object values in the respective views
            pdInvName.text = product.name
            pdInvPriceUnit.text = context.getString(
                R.string.product_price, product.price, product.unit
            )
            pdInvStock.text = context.getString(
                R.string.pd_inv_stock, product.stock
            )
            pdInvSales.text = context.getString(
                R.string.pd_inv_sales, product.sales
            )
            pdInvViews.text = context.getString(
                R.string.pd_inv_views, product.views
            )

            // Load the product image
            GlideLoader(context).loadImage(product.image, pdInvImage)

            // Actions when the more button is clicked
            pdInvMoreBtn.setOnClickListener { view ->
                showProductInvMenu(view, product)  // Show the popup menu
            }
        }  // end of setInvProductData method

        // Function to show the popup menu when the more button is clicked
        private fun showProductInvMenu(view: View, product: Product) {
            // Prepare the popup menu object
            val popup = PopupMenu(context, view)
            popup.menuInflater.inflate(R.menu.product_inv_menu, popup.menu)

            /* If the selected tab is Unlisted, show relist in the more menu
             * instead of unlist.
             */
            if (fragment is ProductUnlistedFragment) {
                popup.menu.findItem(R.id.popup_pd_relist).isVisible = true
                popup.menu.findItem(R.id.popup_pd_unlist).isVisible = false
            } else if (fragment is ProductViolationFragment) {
                popup.menu.findItem(R.id.popup_pd_unlist).isVisible = false
            }

            // Actions when the popup menu item is selected
            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    // Set the current product as unlisted
                    R.id.popup_pd_unlist -> {
                        /* Display an alert dialog with two action buttons
                         * (Unlist & Cancel)
                         */
                        DialogClass(
                            context, fragment, product, this@ProductInventoryAdapter
                        ).alertDialog(
                            context.getString(R.string.dialog_unlist_product_title),
                            context.getString(R.string.dialog_unlist_product_message),
                            context.getString(R.string.dialog_btn_unlist),
                            context.getString(R.string.dialog_btn_cancel),
                            Constants.UNLIST_PRODUCT_ACTION
                        )
                    }

                    // Set the current product as in stock
                    R.id.popup_pd_relist -> {
                        /* Display an alert dialog with two action buttons
                         * (Relist & Cancel)
                         */
                        DialogClass(
                            context, fragment, product, this@ProductInventoryAdapter
                        ).alertDialog(
                            context.getString(R.string.dialog_relist_product_title),
                            context.getString(R.string.dialog_relist_product_message),
                            context.getString(R.string.dialog_btn_relist),
                            context.getString(R.string.dialog_btn_cancel),
                            Constants.RELIST_PRODUCT_ACTION
                        )
                    }

                    // Go to the Product Editor with the product data
                    R.id.popup_pd_edit -> {
                        // Create an Intent to launch ProductEditorActivity
                        Intent(context, ProductEditorActivity::class.java).run {
                            // Add product information to intent
                            putExtra(Constants.PRODUCT_DESCRIPTION, product)

                            // Opens the product editor
                            context.startActivity(this)
                        }
                    }

                    // Delete the current product
                    R.id.popup_pd_delete -> {
                        /* Display an alert dialog with two action buttons
                         * (Delete & Cancel)
                         */
                        DialogClass(
                            context, fragment, product, this@ProductInventoryAdapter
                        ).alertDialog(
                            context.getString(R.string.dialog_delete_product_title),
                            context.getString(R.string.dialog_delete_product_message),
                            context.getString(R.string.dialog_btn_delete),
                            context.getString(R.string.dialog_btn_cancel),
                            Constants.DELETE_PRODUCT_ACTION
                        )
                    }
                }  // end of when

                true
            }

            popup.show()  // Display the popup menu
        }  // end of showProductInvMenu method

    }  // end of ProductInventoryViewHolder class

    // Function to inflate the layout in the RecyclerView
    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): ProductInventoryViewHolder {
        return ProductInventoryViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_product_inventory, parent, false
            )
        )
    }  // end of onCreateViewHolder method

    // Function to implement the codes for each item in the RecyclerView
    override fun onBindViewHolder(
        holder: ProductInventoryViewHolder, position: Int, product: Product
    ) {
        // Sets the values of product inventory data to the current view
        holder.setProductInvData(product)
    }  // end of onBindViewHolder method

    // Function to update the product's status
    internal fun updateProductStatus(product: Product, statusCode: Int) {
        // Create a utility object for progress dialog and others
        UtilityClass().run {
            // Display a loading message
            showProgressDialog(
                context, activity, context.getString(R.string.msg_please_wait)
            )

            // Update the status code, whether unlist or relist
            FirestoreClass().updateProduct(
                context, product.id, hashMapOf(Constants.STATUS to statusCode),
                this, fragment
            )
        }
    }  // end of updateProductStatus method

    // Function to delete the selected product
    internal fun deleteProductInfo(product: Product) {
        // Create a utility object for progress dialog and others
        UtilityClass().run {
            // Display the loading message
            showProgressDialog(
                context, activity, context.getString(R.string.msg_please_wait)
            )

            // Deletes the product data in the Firestore Database
            FirestoreClass().deleteProduct(context, product.id, this)
        }
    }  // end of deleteProductInfo method

}  // end of ProductInventoryAdapter class
