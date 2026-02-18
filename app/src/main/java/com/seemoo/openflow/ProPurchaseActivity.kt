package com.seemoo.openflow

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

/**
 * ProPurchaseActivity - Deprecated
 * This activity is no longer needed as the app is now completely free.
 * Kept for compatibility but displays a message that the app is free.
 */
class ProPurchaseActivity : BaseNavigationActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pro_purchase)
        
        findViewById<View>(R.id.back_button)?.setOnClickListener {
            finish()
        }
        
        Toast.makeText(this, "OpenFlow-AI is now completely free!", Toast.LENGTH_LONG).show()
        // Auto finish after showing message
        finish()
    }
    
    override fun getContentLayoutId(): Int {
        return R.layout.activity_pro_purchase
    }

    override fun getCurrentNavItem(): NavItem {
        return BaseNavigationActivity.NavItem.HOME
    }
}