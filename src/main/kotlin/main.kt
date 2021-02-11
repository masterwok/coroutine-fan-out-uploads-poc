import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce

@ExperimentalCoroutinesApi
private fun CoroutineScope.produceItems() = produce {
    for(item in (0..4)) {
        println("Sending: $item")
        send(item)
    }

    println("Closing producer...")

    close()
}

fun CoroutineScope.launchProcessor(id: Int, channel: ReceiveChannel<Int>) = launch {
    for (msg in channel) {
        delay(4000)
        println("Processor #$id processed $msg")
    }
}

@ExperimentalCoroutinesApi
suspend fun main(args: Array<String>) = withContext(Dispatchers.Default) {
    val receiver = produceItems()

    repeat(2) { launchProcessor(it, receiver)}
}