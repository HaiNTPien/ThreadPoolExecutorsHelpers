package com.example.threadpoolexecutorshelpers

import android.util.Log
import androidx.lifecycle.MutableLiveData
import java.util.*

class ExecutorHelpers(private var maxSize: Int = 10, private var aliveTime: Long = 5000L) {
    private val availableThreads = mutableListOf<String>()
    private val pendingTasksQueue: ArrayDeque<Runnable> = ArrayDeque()
    private var lock = Object()
    var totalThread = MutableLiveData(0)
    var numberPendingTask = MutableLiveData(0)
    private var countForName = 0
    private var countThread = 0

    fun putTask(r: Runnable, priority: Boolean = false) {
        synchronized(lock = lock) {
            if(priority) { pendingTasksQueue.push(r) } else { pendingTasksQueue.add(r) }
            updateTaskMessage()
            if (availableThreads.isNotEmpty()) {
                lock.notifyAll()
            } else {
                if(countThread < maxSize) {
                    createNewThread()
                    updateThreadMessage()
                }
            }
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
        var timeLastTaskDone = 0L
        val newThread = Thread({
            while (true) {
                val task = getFirstPendingTask()
                if (task != null) {
                    synchronized(lock) {
                        availableThreads.remove(name)
                    }
                    task.run()
                    timeLastTaskDone = System.currentTimeMillis()
                } else {
                    if (System.currentTimeMillis() - timeLastTaskDone < 5000) {
                        availableThreads.add(name)
                        synchronized(lock) {
                            lock.wait(aliveTime)
                        }
                    } else {
                        synchronized(lock) {
                            availableThreads.remove(name)
                        }
                        countThread --
                        updateThreadMessage()
                        break
                    }
                }
            }
        }, name)
        synchronized(lock) {
            countThread++
            countForName++
            newThread.start()
        }
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
