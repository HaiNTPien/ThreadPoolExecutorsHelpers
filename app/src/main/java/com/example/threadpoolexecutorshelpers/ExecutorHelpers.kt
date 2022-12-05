package com.example.threadpoolexecutorshelpers

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import java.util.*

class ExecutorHelpers(private var maxSize: Int = 10, var aliveTime: Long = 5000L) {
    private val availableThreads = mutableListOf<String>()
    private val pendingTasksQueue: Queue<Runnable> = LinkedList()
    private var lock = Object()
    var totalThread = MutableLiveData(0)
    var numberPendingTask = MutableLiveData(0)
    var countForName = 0


    @RequiresApi(Build.VERSION_CODES.N)
    fun putTask(r: Runnable) {
        synchronized(lock = lock) {
            pendingTasksQueue.add(r)
            numberPendingTask.postValue(pendingTasksQueue.size)
            if (availableThreads.isNotEmpty()) {
                lock.notifyAll()
            }
        }
        if (maxSize != totalThread.value && (availableThreads.isEmpty() || totalThread.value == 0)) {
             createNewThread()
        }
    }


    @RequiresApi(Build.VERSION_CODES.N)
    private fun createNewThread() {
        val name = "Thread${countForName}"
        var isRunning = true
        var timeLastTaskDone = 0L
        val newThread = Thread({
            while (isRunning) {
                if (pendingTasksQueue.peek() != null) {
                    availableThreads.remove(name)
                    var task: Runnable?
                    synchronized(lock) {
                        task = pendingTasksQueue.poll()
                    }
                    numberPendingTask.postValue(pendingTasksQueue.size)
                    task?.run()
                    timeLastTaskDone = System.currentTimeMillis()
                } else {
                    if (System.currentTimeMillis() - timeLastTaskDone < 5000) {
                        synchronized(lock) {
                            availableThreads.add(name)
                            lock.wait(aliveTime)
                        }
                    } else {
                        availableThreads.remove(name)
                        isRunning = false
                        totalThread.postValue(totalThread.value?.minus(1) ?: 0)
                    }
                }
            }
        }, name)
        totalThread.postValue(totalThread.value?.plus(1) ?: 0)
        countForName++
        newThread.start()
    }

}
