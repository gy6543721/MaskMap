package com.levilin.maskmap

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.levilin.maskmap.data.Feature
import com.levilin.maskmap.databinding.ActivityCardInfoBinding


class CardInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCardInfoBinding

    private val data by lazy { if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("data", Feature::class.java)!!
        } else {
            intent.getSerializableExtra("data") as Feature
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""

        binding = ActivityCardInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            textStoreName.text = data.properties.name
            textAdultMaskQuantity.text = data.properties.mask_adult.toString()
            textChildMaskQuantity.text = data.properties.mask_child.toString()
            textPhoneNumber.text = data.properties.phone
            textAddress.text = data.properties.address
        }

        Log.d("TAG", data.properties.name)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return true
    }

//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        return true
//    }

}