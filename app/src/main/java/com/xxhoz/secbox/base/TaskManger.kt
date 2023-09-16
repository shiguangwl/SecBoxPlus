package com.xxhoz.secbox.base

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

interface TaskManger {
    companion object {
        val jobs: HashMap<String, Job> = HashMap()
        val DefaultJobName = "DefaultJob"
    }


    /**
     * 取消之前任务,提交一个新任务
     */
    fun Task(task: suspend () -> Unit) {
        Task(DefaultJobName,task)
    }

    /**
     * 取消之前任务,提交一个新任务
     */
    fun Task(taskNae: String, task: suspend () -> Unit) {
        jobs[taskNae]?.cancel()
        jobs[taskNae] = getScope().launch {
            task()
        }
    }

    fun getScope(): CoroutineScope

    fun clearTask(){
        jobs.values.forEach{
            it.cancel()
        }
        jobs.clear()
    }


    suspend fun <T> onIO(task: ()->T) : T{
        return withContext(Dispatchers.IO){
            return@withContext task()
        }
    }

    suspend fun onIO2(task: ()->Unit){
         withContext(Dispatchers.IO){
            task()
         }
    }
}
