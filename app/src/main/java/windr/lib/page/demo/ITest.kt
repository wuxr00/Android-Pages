package windr.lib.page.demo

import kotlinx.coroutines.flow.flow

interface ITest {


    fun holdPageExitAnimation() = flow<Any> {  }
}