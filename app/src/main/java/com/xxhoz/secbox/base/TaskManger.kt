package com.xxhoz.secbox.base

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

interface TaskManger {
    companion object {
        val jobs: HashMap<String, Job> = HashMap()
        val DefaultJobName = "DefaultJob"
    }


    /**
     * 取消之前任务,提交一个新任务
     */
    fun SingleTask(task: Job): Job {
        return SingleTask(DefaultJobName, task)
    }

    /**
     * 取消之前任务,提交一个新任务
     */
    fun SingleTask(taskNae: String, task: Job): Job {
        jobs[taskNae]?.cancel()
        jobs[taskNae] = task
        return task
    }


    fun clearTask(){
        jobs.forEach {
            if (it.value.isActive) {
                it.value.cancel()
            }
        }
        jobs.clear()
    }
    fun getScope(): CoroutineScope

}
