package com.shark.netty;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.*;

public class ServerHandler extends ChannelInboundHandlerAdapter {
    
	@Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }
	
	@Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws InterruptedException, URISyntaxException {
        if (msg instanceof HttpRequest) {
            HttpRequest req = (HttpRequest) msg;
            DBConnection con = new DBConnection();
            String ip = ((InetSocketAddress)ctx.channel().remoteAddress()).getAddress().getHostAddress();
            
            if (HttpHeaders.is100ContinueExpected(req)) {
                ctx.writeAndFlush(new DefaultFullHttpResponse(HTTP_1_1, CONTINUE));
                con.close();
            }
            
            //boolean keepAlive = HttpHeaders.isKeepAlive(req);
            if (req.getUri().equalsIgnoreCase("/hello")) {
            	Thread.sleep(10000);
            	FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, 
            			Unpooled.copiedBuffer("Hello world!".toCharArray(), Charset.forName("UTF-8")));
                response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");
                response.headers().set(CONTENT_LENGTH, response.content().readableBytes());
                con.insertData(ip, req.getUri());
                con.close();
                ctx.writeAndFlush(response);
            	ctx.close();
            } else if ((req.getUri().length() > 13) && (req.getUri().substring(0, 14).equalsIgnoreCase("/redirect?url="))) {
            	System.out.println("mama");
            	con.insertData(ip, req.getUri(), req.getUri().substring(14));
            	con.close();
            	FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, MOVED_PERMANENTLY);
            	response.headers().set(LOCATION, new URI(req.getUri().substring(14)));
                ctx.writeAndFlush(response);
            	ctx.close();
            } else if (req.getUri().equalsIgnoreCase("/status")) {
            	String status = con.getStatusOutput();
            	FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, 
            			Unpooled.copiedBuffer(status.toCharArray(), Charset.forName("UTF-8")));
                response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");
                response.headers().set(CONTENT_LENGTH, response.content().readableBytes());
                con.insertData(ip, req.getUri());
                con.close();
                ctx.writeAndFlush(response);
            	ctx.close();
            } else {
            	FullHttpResponse response = new DefaultFullHttpResponse(
						HTTP_1_1, BAD_REQUEST);
            	con.insertData(ip, req.getUri());
            	con.close();
            	ctx.writeAndFlush(response);
            	ctx.close();
            }
        }
    }
	
	@Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
