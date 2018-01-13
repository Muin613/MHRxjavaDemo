package com.munin.mhrxjavademo.rxbus;

import com.jakewharton.rxrelay2.PublishRelay;
import com.jakewharton.rxrelay2.Relay;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;

/**
 * Created by munin on 2017/12/29.
 * 上面的两种 RxBus 在订阅者处理事件出现异常后，订阅者无法再收到事件，
 * 这是 RxJava 当初本身的设计原则，
 * 但是在事件总线中这反而是个问题，
 * 不过 JakeWharton 大神写了即使出现异常也不会终止订阅关系的 RxRelay ，
 * 所以基于 RxRelay 就能写出有异常处理能力的 Rxbus
 */

public class RxBus2RelayB {

    private final Relay<Object> mBus;

    private RxBus2RelayB() {
        // toSerialized method made bus thread safe
        mBus = PublishRelay.create().toSerialized();
    }

    public static RxBus2RelayB get() {
        return Holder.BUS;
    }

    public void post(Object obj) {
        mBus.accept(obj);
    }


    public Flowable register(Class eventType) {
        return mBus.toFlowable(BackpressureStrategy.LATEST).ofType(eventType);
    }

    public Flowable<Object> asFlowable() {
        return mBus.toFlowable(BackpressureStrategy.LATEST);
    }

    public <T> Flowable<T> registerClass(Class<T> tClass) {
        return mBus.toFlowable(BackpressureStrategy.LATEST).ofType(tClass);
    }

    public boolean hasObservers() {
        return mBus.hasObservers();
    }

    private static class Holder {
        private static final RxBus2RelayB BUS = new RxBus2RelayB();
    }
}
