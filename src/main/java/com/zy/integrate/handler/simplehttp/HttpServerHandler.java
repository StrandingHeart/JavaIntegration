package com.zy.integrate.handler.simplehttp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;

import java.nio.charset.Charset;

/**
 * @author: zhangyong
 * @date 2022年11月10日
 */
public class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
        System.out.println("访问了" + msg.method().name() + msg.uri() + " 方法");
        String content = msg.content().toString(Charset.defaultCharset());
        System.out.println("参数体:" + content);
        String sendMsg = "qwertyuiop";

        //封装返回对象
        ByteBuf byteBuf = Unpooled.copiedBuffer(sendMsg, Charset.defaultCharset());
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, byteBuf);
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, sendMsg.length());
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
    }
}
