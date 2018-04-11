# ExEventBus
ExEventBus实现了进程间内与进程间发布订阅的功能

它可以向其他进程发布一个订阅事件，就像在一个进程里一样轻松.

ExEventBus可以与xposed结合起来使用，帮助调试一个被xposed注入的应用

## 使用方法

初始化ExeventBus

```
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ExEventBus.create(this);
    }
}
```
订阅事件
```
@Subscriber(tag = "remoteReceiveMethod",remote = true,model = ThreadModel.MAIN,type = Type.DEFAULT)
public void remoteReceiveMethod(Event event) {
    String msg = event.getMsg();
    int pid = event.getPid();
    String text = "pid : " + pid + "\nmsg : " + msg;
    mTextView.setText(text);
 }
```
远程发布
```
Event event = new Event(msg, Process.myPid());
try {
    ExEventBus.getDefault().remotePublish(event,"remoteReceiveMethod",2000);
} catch (TimeoutException e) {
    Log.e("main","",e);
}
```
