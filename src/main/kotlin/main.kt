import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce

private val mapping = mutableMapOf<Int, Job>()

@ExperimentalCoroutinesApi
private fun CoroutineScope.produceItems() = produce {
    for(item in (0..10)) {
        println("Sending: $item")
        send(item)
    }

    println("Closing producer...")

    close()
}

private fun CoroutineScope.launchProcessor(processorId: Int, channel: ReceiveChannel<Int>) = launch {
    for (item in channel) {
        println("Processor #$processorId received $item")

        val job = launch { uploadItem(item) }

        mapping[item] = job

        job.join()

        if(!job.isCancelled) {
            println("Processor #$processorId processed $item")
        }
    }
}

private suspend fun uploadItem(item: Int) {
    delay(4000)
    println(">>> Done uploading: $item ✅")
}

private suspend fun cancelItem(item: Int) {
    println(">>> cancelling $item ❌")

    mapping[item]?.cancelAndJoin()
    mapping.remove(item)
}

@ExperimentalCoroutinesApi
suspend fun main(args: Array<String>): Unit = withContext(Dispatchers.Default) {
    val receiver = produceItems()

    repeat(3) { launchProcessor(it, receiver)}

    launch {
        delay(1000)
        cancelItem(0)
    }
}