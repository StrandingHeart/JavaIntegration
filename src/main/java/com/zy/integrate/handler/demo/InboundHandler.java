package com.zy.integrate.handler.demo;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

/**
 * @author: zhangyong
 * @date 2022年11月04日
 */
public class InboundHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//        ByteBuf in = (ByteBuf) msg;
//        try {
//            // 打印输入
//            System.out.println(in.toString(io.netty.util.CharsetUtil.UTF_8));
//        } finally {
//            ReferenceCountUtil.release(msg);
//        }
        // 返回msg 这个不用 release  write的时候会释放
        //ctx.write(Object) 方法不会使消息写入到通道上，他被缓冲在了内部，你需要调用 ctx.flush() 方法来把缓冲区中数据强行输出。或者你可以用更简洁的 cxt.writeAndFlush(msg) 以达到同样的目的。
        ctx.writeAndFlush(msg);
    }

    /**
     * 方法将会在连接被建立并且准备进行通信时被调用
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        final ByteBuf time = ctx.alloc().buffer(4);
        time.writeInt((int) (System.currentTimeMillis() / 1000L + 2208988800L));
        System.out.println("enter channelActive");
        final ChannelFuture f = ctx.writeAndFlush(time);
        // 一个 ChannelFuture 代表了一个还没有发生的 I/O 操作。
        // 这意味着任何一个请求操作都不会马上被执行，因为在 Netty 里所有的操作都是异步的。
        // 举个例子下面的代码中在消息被发送之前可能会先关闭连接。
//        ctx.close()

        // 当一个写请求已经完成是如何通知到我们？这个只需要简单地在返回的 ChannelFuture 上增加一个ChannelFutureListener。
        // 这里我们构建了一个匿名的 ChannelFutureListener 类用来在操作完成时关闭 Channel。
        f.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) {
                assert f == future;
                ctx.close();
            }
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
