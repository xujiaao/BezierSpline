package com.xujiaao.android.bezier.spline.sample

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.main_act.*
import kotlinx.android.synthetic.main.main_sample_item.view.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.main_act)
        initComponents()
    }

    private fun initComponents() {
        recyclerView.run {
            this.layoutManager = LinearLayoutManager(this@MainActivity)
            this.adapter = SamplesAdapter(this@MainActivity, getSamples()) {
                startActivity(Intent().apply {
                    component = ComponentName(packageName, it.activity)
                })
            }
        }
    }

    private fun getSamples(): List<Sample> {
        val intent = Intent(Intent.ACTION_VIEW)
            .setPackage(packageName)
            .addCategory(Intent.CATEGORY_SAMPLE_CODE)

        return packageManager.queryIntentActivities(intent, 0).map {
            Sample(
                it.activityInfo.loadLabel(packageManager).toString(),
                it.activityInfo.name
            )
        }
    }

    private class SamplesAdapter(
        context: Context,
        private val samples: List<Sample>,
        private val onSampleClick: (Sample) -> Unit
    ) : RecyclerView.Adapter<SamplesHolder>() {

        private val inflater = LayoutInflater.from(context)

        override fun getItemCount(): Int = samples.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SamplesHolder =
            SamplesHolder(inflater.inflate(R.layout.main_sample_item, parent, false), onSampleClick)

        override fun onBindViewHolder(holder: SamplesHolder, position: Int) {
            holder.sample = samples[position]
        }
    }

    private class SamplesHolder(itemView: View, private val onSampleClick: (Sample) -> Unit) :
        RecyclerView.ViewHolder(itemView) {

        private val label = itemView.label

        var sample: Sample? = null
            set(sample) {
                field = sample
                label.text = sample?.label
            }

        init {
            itemView.setOnClickListener {
                sample?.let { sample ->
                    onSampleClick(sample)
                }
            }
        }
    }

    private data class Sample(val label: String, val activity: String)
}