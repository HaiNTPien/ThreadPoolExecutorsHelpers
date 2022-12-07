package com.example.threadpoolexecutorshelpers

import android.util.Log
import androidx.lifecycle.MutableLiveData
import java.util.*

class ExecutorHelpers(private var maxSize: Int = 10, private var aliveTime: Long = 5000L) {
    private val pendingTasksQueue: ArrayDeque<Runnable> = ArrayDeque()
    private var lock = Object()
    var totalThread = MutableLiveData(0)
    var numberPendingTask = MutableLiveData(0)
    private var countForName = 0
    private var countThread = 0
    private var countAvailableThread = 0

    fun putTask(r: Runnable, priority: Boolean = false) {
        var needCreateThread = false
        synchronized(lock = lock) {
            if(priority) { pendingTasksQueue.push(r) } else { pendingTasksQueue.add(r) }
            updateTaskMessage()
            if (countAvailableThread > 0) {
                countAvailableThread = 0
                lock.notifyAll()
            } else {
                if (countThread < maxSize) {
                    needCreateThread = true
                    countThread ++
                }
            }
        }
        if(needCreateThread) {
            createNewThread()
            updateThreadMessage()
        }
    }

    private fun updateThreadMessage() {
        totalThread.postValue(countThread)
    }

    private fun updateTaskMessage() {
        numberPendingTask.postValue(pendingTasksQueue.size)
    }

    private fun createNewThread() {
        val name = "Thread${countForName}"
        countForName++
        var timeLastTaskDone = 0L
        val newThread = Thread({
            while (true) {
                val task = getFirstPendingTask()
                if (task != null) {
                    task.run()
                    timeLastTaskDone = System.currentTimeMillis()
                } else {
                    if (System.currentTimeMillis() - timeLastTaskDone < aliveTime) {
                        synchronized(lock) {
                            countAvailableThread++
                            lock.wait(aliveTime)
                        }
                    } else {
                        countAvailableThread -= 1
                        countThread -= 1
                        updateThreadMessage()
                        break
                    }
                }
            }
        }, name)
        newThread.start()
    }

    private fun getFirstPendingTask(): Runnable? {
        var task: Runnable?
        synchronized(lock) {
            task = pendingTasksQueue.poll()
        }
        return if (task == null) null
        else {
            updateTaskMessage()
            task
        }
    }
}
