package com.munin.mhrxjavademo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.Flowable;
import io.reactivex.Notification;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.internal.operators.single.SingleToObservable;
import io.reactivex.observables.ConnectableObservable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.subjects.PublishSubject;


/**
 * Created by munin on 2017/12/29.
 * <p>
 * Schedulers.io()  io线程
 * Schedulers.newThread()  新建一个线程，如果出现异常會傳遞到綫程調度（uncaughtexceptionhandler）
 * Schedulers.shutdown()   關閉綫程調度
 * Schedulers.computation() 創建一個用於計算的綫程調度，在其不能使用io綫程，要使用就是用io()代替,如果出现异常會傳遞到綫程調度（uncaughtexceptionhandler）(处理器调度)
 * (先来先服务，最短作业优先，响应比最高优先，优先级调度，时间片轮转调度)
 * Schedulers.single()     返回一个单线程的调度实例，支持schedulers.from()来构建延迟调度，并且避免循环计算的攻击
 * Schedulers.from(*)      可以构建很多线程调度 newCachedThreadPool newFixedThreadPool newSingleThreadExecutor newScheduleThreadPool newWorkStealingPool(可以并行进行,空闲可以去执行) newSingleThreadScheduledExecutor
 * Schedulers.start()      开启线程调度
 * Schedulers.trampoline() 创建一个当前线程的调度
 * AndroidSchedulers.mainThread()  创建一个android主线程线程调度
 * <p>
 * <p>
 * http://blog.csdn.net/firelion0725/article/details/51093512
 * <p>
 * http://reactivex.io/RxJava/javadoc/rx/Observable.html
 * http://reactivex.io/documentation/operators
 * </p>
 * <p>
 * <p>
 * <p>
 * Single
 * <p>
 * RxJava（以及它派生出来的RxGroovy和RxScala）中有一个名为Single的Observable变种。
 * <p>
 * Single类似于Observable，不同的是，它总是只发射一个值，或者一个错误通知，而不是发射一系列的值。
 * <p>
 * 因此，不同于Observable需要三个方法onNext, onError, onCompleted，订阅Single只需要两个方法：
 * <p>
 * onSuccess - Single发射单个的值到这个方法
 * onError - 如果无法发射需要的值，Single发射一个Throwable对象到这个方法
 * Single只会调用这两个方法中的一个，而且只会调用一次，调用了任何一个方法之后，订阅关系终止。
 * <p>
 * <p>
 * <p>
 * Subject Subject可以看成是一个桥梁或者代理，在某些ReactiveX实现中（如RxJava），它同时充当了Observer和Observable的角色。
 * 因为它是一个Observer，它可以订阅一个或多个Observable；又因为它是一个Observable，它可以转发它收到(Observe)的数据，也可以发射新的数据。
 * 由于一个Subject订阅一个Observable，它可以触发这个Observable开始发射数据（如果那个Observable是”冷”的–就是说，它等待有订阅才开始发射数据）。
 * 因此有这样的效果，Subject可以把原来那个”冷”的Observable变成”热”的。
 * <p>
 * 不支持背压
 * AsyncSubject 无论输入多少参数，永远只输出最后一个参数。
 * BehaviorSubject 会发送离订阅最近的上一个值，没有上一个值的时候会发送默认值
 * PublishSubject 可以说是最正常的Subject，从那里订阅就从那里开始发送数据
 * ReplaySubject   无论何时订阅，都会将所有历史订阅内容全部发出
 * UnicastSubject  单播 点对点，订阅者只要有点订阅就会发送给它，整个生命周期都会存在知道终止
 * <p>
 * 支持背压
 * AsyncProcessor
 * BehaviorProcessor
 * PublishProcessor
 * ReplayProcessor
 * UnicastProcessor
 * </p>
 *
 * 按照其生命周期来，该取消订阅取消，否则后果自负
 */

public class RxJavaDemo {
    //    线程调度不做说明,自己体会
    static List<Integer> list = new ArrayList<>();
    static List<Integer> mList = new ArrayList<>();

    static {
        list.add(0);
        list.add(1);
        list.add(3);
        list.add(4);
        list.add(5);
        mList.add(10);
        mList.add(11);
        mList.add(13);
        mList.add(15);
    }

    static Consumer<List<Integer>> consumer = new Consumer<List<Integer>>() {
        @Override
        public void accept(List<Integer> integers) throws Exception {
            System.out.println("consumer   :" + integers);
        }
    };
    static Consumer<Integer> consumer1 = new Consumer<Integer>() {
        @Override
        public void accept(Integer integer) throws Exception {
            System.out.println("consumer1   :" + integer);
        }
    };

    //    observe observable 使用 debounce(类似拦截，过滤，在规定的时间内) last(去最后的值)   (灵异事件，具体为什么可能跟系统环境有问题,debounce不按照要求输出。。。。)
//  throttleFirst(类似过滤，在规定内操作，场景 点击事件在1ms内只能触发一次之类的取的是第一个，throttleLast 相反取后面)
    public static void demo() {
        Observable<String> ob = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {
                Thread.sleep(100);//debounce 有它跟无它是有区别的（没有则mh value不会输出，有则mh value 输出）
                e.onNext("mh value");
                Thread.sleep(10);
                e.onNext("what1");
                Thread.sleep(100);
                e.onNext("what2");
                Thread.sleep(10);
                e.onNext("what3");
                Thread.sleep(100);
                e.onComplete();
            }
        });
        Observer<String> observer = new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            //观察者接收到通知,进行相关操作
            public void onNext(String aLong) {
                System.out.println("我接收到数据了" + aLong);
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        };

//        ob.subscribe(observer);
        ob.debounce(10, TimeUnit.MILLISECONDS).subscribe(observer);
//        ob.debounce(100, TimeUnit.MILLISECONDS).last("what1").subscribe(new Consumer<String>() {
//            @Override
//            public void accept(String s) throws Exception {
//                System.out.println("last:     " + s);
//            }
//        });
//        ob.throttleFirst(120, TimeUnit.MILLISECONDS).subscribe(observer);
    }

    //    just doOnNext doOnSubscribe doOnComplete
    public static void demo1() {
        Observable.just(list).doOnNext(new Consumer<List<Integer>>() {
            @Override
            public void accept(List<Integer> integers) throws Exception {
                for (Integer i : integers)
                    i *= 10;
                System.out.println("不会影响得");
            }
        }).doOnSubscribe(new Consumer<Disposable>() {
            @Override
            public void accept(Disposable disposable) throws Exception {
                System.out.println("what?");
            }
        }).doOnComplete(new Action() {
            @Override
            public void run() throws Exception {
                System.out.println("doOnComplete");
            }
        }).subscribe(consumer);
    }

    //    fromIterable take(取内容) skip(跳过前面1个)
    static void demo2() {
        Observable.fromIterable(list).take(2).skip(1).subscribe(consumer1);
    }

    //    flatMap map(做数据操作处理) concatmap(发送的数据是有序的一般用这个就行了) takelast(针对发射源来所的，不是内容的限制，而是发射的源发射的observable个数取最新的)
    static void demo3() {
        Observable.just(list).flatMap(new Function<List<Integer>, ObservableSource<Integer>>() {
            @Override
            public ObservableSource<Integer> apply(List<Integer> integers) throws Exception {
                return Observable.fromIterable(list);
            }
        }).takeLast(3).map(new Function<Integer, Integer>() {
            @Override
            public Integer apply(Integer integer) throws Exception {
                return integer + 10;
            }
        }).concatMap(new Function<Integer, ObservableSource<Integer>>() {
            @Override
            public ObservableSource<Integer> apply(Integer integer) throws Exception {
                return Observable.just(integer + 20);
            }
        }).subscribe(consumer1);
    }

    //    fromArray filter（过滤，过滤需要的数据）
    void demo4() {
        Observable.fromArray(0, 1, 2, 3)
                .filter(new Predicate<Integer>() {
                    @Override
                    public boolean test(Integer integer) throws Exception {
                        return integer > 2;
                    }
                }).subscribe(consumer1);
    }

    //defer (订阅才创建) range(从某个到某个 只有 long int之类的) delay （延时）
    static void demo5() {
        Observable.defer(new Callable<ObservableSource<Integer>>() {
            @Override
            public ObservableSource<Integer> call() throws Exception {
                return Observable.just(1);
            }
        }).subscribe(consumer1);
        Observable.range(1, 10).delay(1, TimeUnit.SECONDS)
                .subscribe(consumer1);
    }

    //    interval(定时任务，心跳之类的) timer(延时发送某个不是定时的)
    static void demo6() {
        final Disposable f = Flowable.interval(100, TimeUnit.MILLISECONDS)
                .doOnNext(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        System.out.println("ni shi sha");
                    }
                }).subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        System.out.println("sha " + aLong);
                    }
                });
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        f.dispose();
        Flowable.timer(1, TimeUnit.SECONDS).subscribe(new Consumer<Long>() {
            @Override
            public void accept(Long aLong) throws Exception {
                System.out.println("sha 11111" + aLong);
            }
        });
    }

    //    concat （先读缓存后读网络的策略） map
    static void demo7() {
        Observable cache = Observable.just(list);
        Observable net = Observable.just(mList);
        Observable.concat(cache, net).map(new Function<List<Integer>, List<Integer>>() {
            @Override
            public List<Integer> apply(List<Integer> o) throws Exception {
                if (o.size() <= 3)
                    return o;
                else {
                    List<Integer> data = new ArrayList<>();
                    for (int i = 0; i < o.size(); i++) {
                        if (i < 3)
                            data.add(o.get(i));
                        else
                            break;
                    }
                    return data;
                }
            }
        }).subscribe(consumer);
    }

    //     zip (多个请求联合完成某个目的)
    static void demo8() {
        Observable cache = Observable.just(list);
        Observable net = Observable.just(mList);
        Observable.zip(cache, net, new BiFunction<List<Integer>, List<Integer>, List<Integer>>() {
            @Override
            public List<Integer> apply(List<Integer> o, List<Integer> o2) throws Exception {
                List<Integer> data = new ArrayList<>();
                for (Integer i : o)
                    data.add(i);
                for (Integer i : o2)
                    data.add(i);
                return data;
            }
        }).subscribe(consumer);
    }

    // empty(作为线程调度测试使用) never(创建一个不发射数据也不终止) error(创建一个不发射数据以一个错误终止的Observable)
    static void demo9() {
        Observable.empty()
                .doOnComplete(new Action() {
                    @Override
                    public void run() throws Exception {
                        System.out.println("empty");
                    }
                })
                .subscribe();
        Observable.never().doOnComplete(new Action() {
            @Override
            public void run() throws Exception {
                System.out.println("never");
            }
        }).subscribe();
        Observable.error(new NullPointerException())
                .subscribe();

    }

    // combineLatest 结合 取最新的结合到一起组成结果(2个接口请求n次取它们最新的结果)
    static void demo10() {
        Observable.combineLatest(Observable.fromArray(list), Observable.fromArray(mList), new BiFunction<List<Integer>, List<Integer>, Integer>() {
            @Override
            public Integer apply(List<Integer> integers, List<Integer> integers2) throws Exception {
                return integers.size() + integers2.size();
            }
        }).subscribe(consumer1);
    }

    //  merge 合并，是的其看似是一个但有先后顺序
    static void demo11() {
        Observable.merge(Observable.fromArray(list), Observable.fromArray(mList))
                .subscribe(consumer);
    }

    //  这种一般很少有使用场景，（先订阅的能获取所有的，但调用了complete，后订阅的会接收因为replay的特性，但由于被限制2所以只会发最后2个（重发上次只针对————重发限制））
    static void demo12() {
        PublishSubject<Integer> source = PublishSubject.create();
        ConnectableObservable connectableObservable = source.replay(2);
        connectableObservable.connect();
        connectableObservable.subscribe(consumer1);
        source.onNext(1);
        source.onNext(2);
        source.onNext(3);
//        source.onComplete();
        System.out.println("1111111");
        connectableObservable.subscribe(consumer1);
        source.onNext(11);
        source.onNext(21);
        source.onNext(31);
        System.out.println("1111112222222222221");
        connectableObservable.subscribe(consumer1);
    }

    //      repeat （重复对象几次） distinct(出去重复对象) publish(把其变成可连接的，必须订阅后通过connect来能consumer接收数据)
    static void demo13() {
        ConnectableObservable o = Observable.just(list)
                .repeat(3)
                .distinct()
                .publish();
        o.subscribe(consumer);
        o.connect();
    }

    // share （个人理解是发数据进行唯一，发送给订阅者时候都是相同的，在并发的时候是可行的（个人觉得））
    // retry(在接收到error会触发从新走一边，场景是数据拿不到想拿到就再次做操作拿到数据知道拿到数据为止)
    static void demo14() {
        Observable o = Observable.just(list)
                .share();
        o.retry();
        o.subscribe(consumer);
    }

    // switchmap(这个用法跟flatmap map相似，区别在于订阅中有新的，会把旧的取消掉，只订阅新的)
    static void demo15() {
        Observable.just(list).switchMap(new Function<List<Integer>, ObservableSource<Integer>>() {
            @Override
            public ObservableSource<Integer> apply(List<Integer> integers) throws Exception {
                return Observable.fromIterable(integers);
            }
        }).subscribe(consumer1);
    }

    // wrap(正如其名，包裹) amb(发送第一个，后面都会被抛弃掉)
    static void demo16() {
        ConcurrentHashMap<Integer, ObservableSource> map = new ConcurrentHashMap<>();
        map.put(0, Observable.just(mList).wrap(new ObservableSource<List<Integer>>() {

            @Override
            public void subscribe(Observer<? super List<Integer>> observer) {
                observer.onNext(list);
            }
        }));
        map.put(1, Observable.just(mList).delay(1, TimeUnit.SECONDS));

        Observable.ambArray(map.get(0), map.get(1)).subscribe(consumer);
    }

    //     reduce(把第一个跟后面一个做计算然后用到后面的值)
    static void demo17() {
        Observable.fromIterable(list)
                .reduce(new BiFunction<Integer, Integer, Integer>() {
                    @Override
                    public Integer apply(Integer integer, Integer integer2) throws Exception {
                        System.out.println(integer + "   " + integer2);
                        return integer - integer2 * integer2;
                    }
                }).subscribe(consumer1);
    }

    // buffer（把数据一个个组合，count 3个一起组合， 间隔2，继续组合）
    static void demo18() {
        Observable.fromIterable(list).buffer(3, 2)
                .subscribe(consumer);
    }

    // scan 开始把2个数据合并发送，然后合并的再次合并下一个原来的数结合在发送，继续第二步到最后
    static void demo19() {
        Observable.fromIterable(list).scan(new BiFunction<Integer, Integer, Integer>() {
            @Override
            public Integer apply(Integer integer, Integer integer2) throws Exception {
                return integer + integer2;
            }
        }).subscribe(consumer1);
    }

    // window 类似skip但区别在于是发送observable
    static void demo20() {
        Observable.fromIterable(list).window(3, 2)
                .subscribe(new Consumer<Observable<Integer>>() {
                    @Override
                    public void accept(Observable<Integer> integerObservable) throws Exception {
                        integerObservable.subscribe(consumer1);
                    }
                });
    }

    //   materialize 把Observable变成notification(没啥意思) 对上面的反操作（dematerialize）
    static void demo21() {
        Observable.just(list).materialize().subscribe(new Consumer<Notification<List<Integer>>>() {
            @Override
            public void accept(Notification<List<Integer>> listNotification) throws Exception {
                System.out.println("11111111 :        " + listNotification.getValue());
            }
        });

        Observable.just(list).materialize().dematerialize().subscribe(new Consumer<Object>() {
            @Override
            public void accept(Object o) throws Exception {
                System.out.println("22222222:   " + o);
            }
        });

    }

    // CompositeDisposable 类似管理类，专门管理disposable,统一使用delete和clear 和dispose 来关闭

    static void demo22() {
        CompositeDisposable d = new CompositeDisposable();
        d.add(Observable.fromIterable(list).subscribeWith(new DisposableObserver<Object>() {
            @Override
            public void onNext(Object o) {
                System.out.println("22222:  " + o);
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        }));
    }

    // single completable 没什么好玩的（自己体会把）
    static void demo24() {
        Single.just(list).subscribe(consumer);
        Completable.timer(1, TimeUnit.SECONDS).subscribe(new CompletableObserver() {
            @Override
            public void onSubscribe(Disposable d) {
                System.out.println("sub");
            }

            @Override
            public void onComplete() {
                System.out.println("com");
            }

            @Override
            public void onError(Throwable e) {

            }
        });
    }
}
