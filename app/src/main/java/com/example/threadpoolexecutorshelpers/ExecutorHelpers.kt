package com.example.threadpoolexecutorshelpers

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import java.util.*

class ExecutorHelpers(private var maxSize: Int = 10, private var aliveTime: Long = 5000L) {
    private val availableThreads = mutableListOf<String>()
    private val pendingTasksQueue: ArrayDeque<Runnable> = ArrayDeque()
    private var lock = Object()
    var totalThread = MutableLiveData(0)
    var numberPendingTask = MutableLiveData(0)
    private var countForName = 0


    @RequiresApi(Build.VERSION_CODES.N)
    fun putTask(r: Runnable, priority: Boolean = false) {
        synchronized(lock = lock) {
            if(priority) { pendingTasksQueue.push(r) } else { pendingTasksQueue.add(r) }
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
        var timeLastTaskDone = 0L
        val newThread = Thread({
            while (true) {
                if (pendingTasksQueue.peek() != null) {
                    availableThreads.remove(name)
                    runFirstPendingTask()
                    timeLastTaskDone = System.currentTimeMillis()
                } else {
                    if (System.currentTimeMillis() - timeLastTaskDone < 5000) {
                        synchronized(lock) {
                            availableThreads.add(name)
                            lock.wait(aliveTime)
                        }
                    } else {
                        availableThreads.remove(name)
                        totalThread.postValue(totalThread.value?.minus(1) ?: 0)
                        break
                    }
                }
            }
        }, name)
        totalThread.postValue(totalThread.value?.plus(1) ?: 0)
        countForName++
        newThread.start()
    }

    private fun runFirstPendingTask() {
        var task: Runnable?
        synchronized(lock) {
            task = pendingTasksQueue.poll()
        }
        numberPendingTask.postValue(pendingTasksQueue.size)
        task?.run()
    }
}
