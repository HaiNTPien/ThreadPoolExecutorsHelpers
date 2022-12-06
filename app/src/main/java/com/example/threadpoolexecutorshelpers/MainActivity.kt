package com.example.threadpoolexecutorshelpers

import android.annotation.SuppressLint
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatTextView
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
            when (i) {
                0 -> {
                    i = 1
                    executor.putTask(taskRunUpTo(binding.tv1, 300), false)
//                    executor.putTask(taskRunUpTo(binding.tv2, 300), false
//                    executor.putTask(taskRunUpTo(binding.tv3, 300), false)
//                    executor.putTask(taskRunUpTo(binding.tv3, 150), false)
//                    executor.putTask(taskRunUpTo(binding.tv3, 150), false)
                }
                1 -> {
                    i = 2
                    executor.putTask(taskRunUpTo(binding.tv2, 300), false)
                }
                else -> {
                    i = 0
                    executor.putTask(taskRunUpTo(binding.tv3, 300), false)
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

    private fun taskRunUpTo(view: AppCompatTextView, to: Int): Runnable {
        return Runnable {
            var isRunning = true
            while (isRunning) {
                val number = (Integer.parseInt(view.text.toString()) + 1)
                if (number < to) {
                    view.post {
                        view.text = number.toString()
                    }
                } else {
                    view.post {
                        view.text = "0"
                    }
                    isRunning = false
                }
                Thread.sleep(20)
            }
        }
    }
}