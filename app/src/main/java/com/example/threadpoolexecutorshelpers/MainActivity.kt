package com.example.threadpoolexecutorshelpers

import android.annotation.SuppressLint
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import com.example.threadpoolexecutorshelpers.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    var i = 0
    private val executor = ExecutorHelpers(3, 5000)

    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.btnStart.setOnClickListener {
            val view = when (i) {
                0 -> {
                    i = 1
                    binding.tv1
                }
                1 -> {
                    i = 2
                    binding.tv2
                }
                else -> {
                    i = 0
                    binding.tv3
                }
            }
            executor.putTask {
                var isRunning = true
                while (isRunning) {
                    val number = (Integer.parseInt(view.text.toString()) + 1)
                    if(number < 500) {
                        view.post {
                            view.text = number.toString()
                        }
                    }else {
                        view.post {
                            view.text = "0"
                        }
                        isRunning = false
                    }
                    Thread.sleep(20)
                }
            }
        }
        executor.totalThread.observe(this) {
            binding.tvTotalThread.text = "Total Thread: $it"
        }
        executor.numberPendingTask.observe(this) {
            binding.tvPendingTask.text = "Pending tasks: $it"
        }
    }

}