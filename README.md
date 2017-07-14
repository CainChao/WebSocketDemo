# WebSocket简单使用
&nbsp;&nbsp;&nbsp;&nbsp;网上有使用Okhttp进行封装，在封装的，还有使用了java-websocket这个包的。而我选择的是后者。因为这个包有maven包，好像还是麻省理工实验室出的，使用起来也算比较简单。</br>
**第一步：**去gitHub上导包gitHub地址<a href="https://github.com/TooTallNate/Java-WebSocket">Java-WebSocket。</a></br>
**第二步：**
```Java
  	try {
         WebSocketClient   client = new WebSocketClient(new URI(webSocketUrl), new Draft_17()) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                       LogUtil.e(TAG + "lal-open", "已经连接到服务器【" + getURI() + "】");
                   }

                   @Override
                   public void onMessage(String message) {
                       LogUtil.e(TAG + "lal-message", "获取到服务器信息【" + message + "】");

                   }

                   @Override
                   public void onClose(int code, String reason, boolean remote) {
                       LogUtil.e(TAG + "lal-close", "断开服务器连接【" + getURI() + "，状态码： " + code + "，断开原因：" + reason + "】times:" + closeNum);
                   }

                   @Override
                   public void onError(Exception ex) {
                       LogUtil.e(TAG + "lal-error", "连接发生了异常【异常原因：" + ex + "】Times:" + errNum);
                   }
               };
           } catch (URISyntaxException e) {
               LogUtil.e("--TAG--", e.toString());
               e.printStackTrace();
           }
```
webSocketUrl是后台的URL包括参数。</br>
**第三步：**
```Java
	client.connect();
```
&nbsp;&nbsp;&nbsp;&nbsp;好了，经过这三步成功的使用了WebSocket连上后台,在onDestroy()方法中记得调用client.close()这个方法进行断开链接。是不是很简单。然而并不是这么简单，如果由于网络的原因或者后台发生故障了，断开了怎么办呢？你肯定会想，这还不好办，这个包肯定有断开重连的方法吧！你写个广播监听网络状态。如果网络异常了就去重连。呵呵，这个包是没有重连的方法的，在github上还有人提过这个Bug，但是博主并没有解决，目前还是open状态bug号#392。这个问题让后思索了很久，后来我我就想，既然断开了，我就不如重新开给连接，重新走上面的方法。然而，在测试的时候，我自己将网络断开，发现无限的走上面的方法，因为我在onClose和onError是有重新走上面的方法，最后抛异常了，异常是OutOfMemoryException没错就是我上面所指的内存溢出。这很明显吧，断开或者异常断开就走上面的方法，就会去重新创建一个WebSocketClient对象，而原来的也没有回收掉，还无限的去创建新对象，不抛异常才怪。既然问题找到了，那就找解决办法呗。我的解决方案是这样的，在调用这个方法之前，我判断这个对象是否为空，如果不为空，我会想调用client.close()这个方法。为什么要调用这个方法呢？其实很简单，因为这个方法会将里面的一些变量变为空。然后将client变为空。经过改良后是这样的
```Java
public class WebSocketUtil {
    private static final String TAG = WebSocketUtil.class.getSimpleName();

    private WebSocketClient client;// 连接客户端

    private IWebSocketCallBack callBack;


   private   WebSocketUtil(){
    }

    private static class SingletonHolder{
        public static WebSocketUtil instance = new WebSocketUtil();
    }

    public static WebSocketUtil newInstance(){
        return SingletonHolder.instance;
    }

    public void setWebSocketCallBack(IWebSocketCallBack callBack){
        this.callBack = callBack;
    }

   public void requestNetWork(String mSn){
      String  webSocketUrl = ServiceGenerator.WEBSOCKET_BASE_URL+mSn;
       if(client != null){
           client.close();
           client = null;
       }

       if(client == null) {
           try {
               client = new WebSocketClient(new URI(webSocketUrl), new Draft_17()) {
                   @Override
                   public void onOpen(ServerHandshake handshakedata) {
                       LogUtil.e(TAG + "lal-open", "已经连接到服务器【" + getURI() + "】");
                       callBack.onOpen(handshakedata);
                   }

                   @Override
                   public void onMessage(String message) {
                       LogUtil.e(TAG + "lal-message", "获取到服务器信息【" + message + "】");
                       callBack.onMessage(message);

                   }

                   @Override
                   public void onClose(int code, String reason, boolean remote) {
                       LogUtil.e(TAG + "lal-close", "断开服务器连接【" + getURI() + "，状态码： " + code + "，断开原因：" + reason + "】");
                   }

                   @Override
                   public void onError(Exception ex) {
                       LogUtil.e(TAG + "lal-error", "连接发生了异常【异常原因：" + ex + "】");
                   }
               };
           } catch (URISyntaxException e) {
               callBack.failure();
               LogUtil.e("--TAG--", e.toString());
               e.printStackTrace();
           }
       }
       client.connect();
   }
   public void close(){
       if(null != client){
           client.close();
       }
   }
   public void sendMessage(String msg)  throws WebsocketNotConnectedException {
       if(null != client){
           client.send(msg);
       }
   }
}
```
&nbsp;&nbsp;&nbsp;&nbsp;这样Android移动端出现问题，自己可以检测到，而且可以重连的问题解决了，但是如果是后台发生问题呢怎么办呢。最好的办法就是进行心跳检测。那什么是心跳检测呢？知道的可以跳过。心跳检测是这样的，客户端："你还活着吗？",后台服务:"嗯，还活着"，然后过一段时间客户端又问一句，后台回复一句，如果超过一定时间没有回答，就视为客户端和后台断开了，然后进行重连。这样包括客户端自己出问题了重连，和后台出问题了重连，可以保证客户端一直连着的状态了。</br>
&nbsp;&nbsp;&nbsp;&nbsp;最后完整代码放在<a href="https://github.com/CainChao/WebSockeDemo">github</a>上。代码上还有点问题，比如重连的次数，我的重连的次数是在onCreate方法中进行请求，其实大可以通过心跳进行获取，而后台可以将这个值放到缓存中，比如放到redis中，不需要每次去数据库中获取。而Android客户端不需要重启应用进行获取。这点不足是我的好朋友看了代码给的提醒。