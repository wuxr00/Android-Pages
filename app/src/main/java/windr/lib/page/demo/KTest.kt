package windr.lib.page.demo

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.animation.addListener
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import windr.lib.page.IPage
import java.lang.NullPointerException
import kotlin.coroutines.CoroutineContext

fun main() {

//   println( 3 fixAdd 3 4)
//   Test{
//      println("in test scope")
//      1
//   }.test()

    runBlocking {

//        val test1 = CoTestScope {
//
//        launch {
//            while (true) {
//                delay(1000)
//                println("looping 1...${Thread.currentThread()}")
//            }
//        }
//
//
////            launch(SupervisorJob() + CoroutineExceptionHandler { coroutineContext, throwable ->
////                println("子协异常处理器" + throwable.localizedMessage)
////            }) {
////                throw NullPointerException("null pointer a")
////            }
////            launch {
////                while (true) {
////                    delay(1000)
////                    println("looping 2...")
////                }
////            }
//            println("end scope ")
//        }
//
//       val scope2 = CoTestScope1(newCoroutineContext(Dispatchers.Default)){
//
//          launch {
//             while (true) {
//                delay(1000)
//                println("looping 2...${Thread.currentThread()}")
//             }
//          }
//          println("end scope 2")
//
//       }
//
//        delay(10000)
//        test1.cancel()
//
//       delay(1000)
//       println("end runblocking ")
//        val job = launch {
//
////            launch {
////                while (isActive) {
////                    delay(1000)
////                    println("looping 1...${Thread.currentThread()}")
////                }
////            }
//////            launch {
////                flow<Unit> {
////                    println("flow in...${Thread.currentThread()}")
////                    delay(3000)
////                    emit(Unit)
////                }.collect {
////                    print("collect flow->$it")
////                }
//////            }
//////
////            launch {
////                while (true) {
////                    delay(1000)
////                    println("looping 2...${Thread.currentThread()}")
////                }
////            }
//            println("end scope...${Thread.currentThread()}")
//        }
//
//        delay(2000)
//        job.cancel()
//        println("end run...${Thread.currentThread()}")

//        testFlow().collect { value -> println(value) }

//        val scope = CoTestScope {
//
//
//        }
//
//        delay(5000)
//        scope.cancel()

//        launch(SupervisorJob() + CoroutineExceptionHandler { coroutineContext, throwable ->
//                println("子协异常处理器" + throwable.localizedMessage)
//            }) {
//            println("channel - ${scope.channel.isClosedForSend} - ${scope.channel.isClosedForReceive}")
//            scope.channel.send("cancel send")
//        }
//        testSuspendFlowFun()

        var result = ""
        testFlow().collect {
            println(it)
            delay(1000)
            result = it
        }
        println("end run block $result ")
    }

    val test1 = Test1()
    val test2 = Test2()

    println("end main ${test1 as? Test2}")

    object : IPage {
        override fun createPage(context: Context, parent: ViewGroup, args: Bundle?) {

            TODO("Not yet implemented")
        }

        override fun getView(): View {
            TODO("Not yet implemented")
        }

    }

}


suspend fun CoTestScope.testChannel() {
    val channel = Channel<Unit>()
    testThread {
        println("end thread ${Thread.currentThread()}")
        launch {
            println("send ${Thread.currentThread()}")
            channel.send(Unit)
        }
    }
    channel.receive()
}

fun testThread(callback: () -> Unit) {
    object : Thread() {
        override fun run() {
            super.run()
            println("start thread ${Thread.currentThread()}")

            Thread.sleep(2000)
            callback.invoke()
        }
    }.start()
}

suspend fun CoroutineScope.testSuspendFlowFun() {
    val channel = Channel<Unit>()
    println("test testFlow")
    testFlow2().collect { value ->
        println("get flow2- $value")
        launch {
            channel.send(Unit)
        }
    }

    channel.receive()
}

fun testFlow() = flow<String> {
    println("test testFlow")
    testFlow2().collect { value -> emit("get->$value") }
}

fun testFlow2() = flow<Int> {
    println("test testFlow2")
    delay(2000)
//    testSuspendFlow()
    emit(2)
}


suspend fun testSuspendFlow() {
    println("test suspend flow")
    delay(3000)
    println("test suspend flow end")
}



class Test1 {

}

class Test2 {

}

class CoTestScope(init: CoTestScope.() -> Unit) : CoroutineScope {
    private val job = Job()
    val channel = Channel<String>()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + job

    init {
        init.invoke(this)
        launch {
            while (isActive) {
                delay(1000)
                println("loop in scope...")
            }
        }
        launch {
            while (true) {
                channel.send("test")
                println("send...")
                delay(500)
            }
        }

        launch {
            while (true) {
                println(channel.receive())
            }
        }
    }

    fun cancel() {
//        job.cancel()
        channel.close()
        cancel("test")
        println("cancel-> $isActive - ${job.isActive}")
    }

}

class CoTestScope1(coroutineContext: CoroutineContext, init: CoTestScope1.() -> Unit) :
    CoroutineScope by CoroutineScope(coroutineContext) {


    init {
        init.invoke(this)
    }

}

infix fun Int.fixAdd(a: Int) = this + a

class Test(var init: Test.() -> Int) {
    init {
        println("in test init ${init.invoke(this)}")
    }


    fun test() {
        println("in test test ${init.invoke(this)}")

    }
}