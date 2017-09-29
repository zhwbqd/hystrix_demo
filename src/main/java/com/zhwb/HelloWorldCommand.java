package com.zhwb;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import rx.Observable;
import rx.Observer;
import rx.functions.Action1;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class HelloWorldCommand extends HystrixCommand<String> {
    private final String name;

    public HelloWorldCommand(String name) {
        //最少配置:指定命令组名(CommandGroup)  
        super(HystrixCommandGroupKey.Factory.asKey("ExampleGroup"));
        this.name = name;
    }

    /**
     * 依赖逻辑封装在run()方法中
     */
    @Override
    protected String run() {
        return "Hello " + name + " thread:" + Thread.currentThread().getName();
    }

}

class Test {

    //调用实例, 每个Command只能被调用一次
    public static void main(String[] args) throws Exception {

        //case1: 同步调用
        HelloWorldCommand helloWorldCommand = new HelloWorldCommand("World");
        String result = helloWorldCommand.execute();
        System.out.println(result);

        //case2: 异步调用,可自由控制获取结果时机及超时
        helloWorldCommand = new HelloWorldCommand("A-World");
        Future<String> future = helloWorldCommand.queue();
        result = future.get(2000, TimeUnit.MILLISECONDS);
        System.out.println(result);

        //case3: 异步事件回调执行
        //注册观察者事件拦截
        Observable<String> fs = new HelloWorldCommand("Ob-World").observe();
        fs.subscribe(new Action1<String>() {
            @Override
            public void call(String result) {
                //执行结果处理,result 为HelloWorldCommand返回的结果
                //用户对结果做二次处理.
                System.out.println("handle result:" + result);
            }
        });
        fs.subscribe(new Observer<String>() {
            @Override
            public void onCompleted() {
                // onNext/onError完成之后最后回调
                System.out.println("execute onCompleted");
            }

            @Override
            public void onError(Throwable e) {
                // 当产生异常时回调
                System.out.println("onError " + e.getMessage());
                e.printStackTrace();
            }

            @Override
            public void onNext(String v) {
                // 获取结果后回调
                System.out.println("onNext: " + v);
            }
        });
    }

}  