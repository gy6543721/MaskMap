package com.levilin.maskmap

import android.R
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.levilin.maskmap.data.Feature
import com.levilin.maskmap.data.MaskInfo
import com.levilin.maskmap.databinding.ActivityMainBinding
import com.levilin.maskmap.utility.OkHttpUtility
import com.levilin.maskmap.utility.OkHttpUtility.Companion.mOkHttpUtility
import com.levilin.maskmap.utility.SpinnerUtility
import okhttp3.*
import okio.IOException


class MainActivity : AppCompatActivity(), MainAdapter.ItemClickListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var viewAdapter: MainAdapter

    private var currentCounty: String = "臺北市"
    private var currentTown: String = "信義區"
    private var maskInfo: MaskInfo? = null

    override fun onCreate(savedInstanceState: Bundle?) {

//      New setup with binding:
        super.onCreate(savedInstanceState)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
        getMaskData()
    }

    private fun initView() {
        // SpinnerView
        val spinnerCountyAdapter = ArrayAdapter(
            this,
            R.layout.simple_spinner_dropdown_item,
            SpinnerUtility.getAllCountiesName()
        )
        binding.spinnerCounty.adapter = spinnerCountyAdapter
        binding.spinnerCounty.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                currentCounty = binding.spinnerCounty.selectedItem.toString()
                setSpinnerTown()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        binding.spinnerTown.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                currentTown = binding.spinnerTown.selectedItem.toString()
                updateRecyclerView()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        setDefaultCountyWithTown()

        // RecycleView, default is vertical layout
        viewManager = LinearLayoutManager(this)
        viewAdapter = MainAdapter(this)

        binding.recyclerView.apply {
            layoutManager = viewManager
            adapter = viewAdapter
        }
    }

    private fun setDefaultCountyWithTown() {
        binding.spinnerCounty.setSelection(SpinnerUtility.getCountyIdByName(currentCounty),true)
        setSpinnerTown()
    }

    private fun setSpinnerTown() {
        val spinnerTownAdapter = ArrayAdapter(
            this,
            R.layout.simple_spinner_dropdown_item,
            SpinnerUtility.getTownsByCountyName(currentCounty)
        )
        binding.spinnerTown.adapter = spinnerTownAdapter
        binding.spinnerTown.setSelection(SpinnerUtility.getTownIdByName(currentCounty,currentTown),true)
    }

    private fun getMaskData() {
        binding.progressBar.visibility = View.VISIBLE
        mOkHttpUtility.getAsync(MASK_DATA_URL, object: OkHttpUtility.AsyncCallback {
            override fun onFailure(e: IOException) {
                runOnUiThread {
                    binding.progressBar.visibility = View.GONE
                    val alertDialog: AlertDialog.Builder = AlertDialog.Builder(this@MainActivity)
                    alertDialog.setTitle(R.string.dialog_alert_title).setMessage("call failed: $e").show()
                }
            }
            override fun onResponse(response: Response) {
                maskInfo = Gson().fromJson(
                    response.body?.string(),
                    MaskInfo::class.java
                )

                runOnUiThread {
                    updateRecyclerView()
                    binding.progressBar.visibility = View.GONE
                }
            }
        })
    }

    private fun updateRecyclerView() {
        if (maskInfo == null) {
            getMaskData()
        } else {
            val filterData: List<Feature> = maskInfo!!.features.filter {
                it.properties.county == currentCounty && it.properties.town == currentTown
            }
            viewAdapter.maskDataList = filterData
        }
    }

    override fun onItemClickListener(data: Feature) {
        val intent = Intent(this, CardInfoActivity::class.java)
        intent.putExtra("data", data)
        startActivity(intent)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return true
    }
}