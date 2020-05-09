package windr.lib.page.demo;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlinx.coroutines.flow.Flow;
import kotlinx.coroutines.flow.FlowCollector;

public class JTest {
    public static void main(String[] args) {
        ITest page = new ITest() {


            @NotNull
            @Override
            public Flow<Object> holdPageExitAnimation() {
                return new Flow<Object>() {

                    @Nullable
                    @Override
                    public Object collect(@NotNull FlowCollector<? super Object> flowCollector, @NotNull Continuation<? super Unit> continuation) {
                        flowCollector.emit(null, continuation);
                        return null;
                    }
                };
            }
        };

        page.holdPageExitAnimation().collect(new FlowCollector<Object>() {

            @Nullable
            @Override
            public Object emit(Object o, @NotNull Continuation<? super Unit> continuation) {
                continuation.resumeWith(o);
                return null;
            }
        }, new Continuation<Object>() {
            @NotNull
            @Override
            public CoroutineContext getContext() {
                return null;
            }

            @Override
            public void resumeWith(@NotNull Object o) {
                System.out.println("get -> " + o);
            }
        });

    }
}
