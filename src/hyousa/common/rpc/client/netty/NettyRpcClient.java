package hyousa.common.rpc.client.netty;

import hyousa.common.conf.Configuration;
import hyousa.common.rpc.Invocation;
import hyousa.common.rpc.Result;
import hyousa.common.rpc.client.RpcClient;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

/**
 * Created by yousa on 2017/12/4.
 */
public class NettyRpcClient extends RpcClient {
    private final static String RPC_SERIALIZE_MAXSIZE = "rpc.netty.serialize.maxsize";

    private Invocation ivc;
    private Result result;

    public NettyRpcClient(String host, int port, Configuration conf) {
        super(host, port, conf);
    }

    /**
     * connect server and send Invocation, then return the received Result
     * invoke always close relevant resource before return Result
     * @param ivc Invocation: rpc request abstraction
     * @return Result: rpc response abstraction
     */
    public Result invoke(String host, int port, Invocation ivc) {
        this.ivc = ivc;
        EventLoopGroup group = new NioEventLoopGroup();
        ChannelHandler handler = new ResultChannelHandler();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group).remoteAddress(host, port).channel(NioSocketChannel.class)
            .handler(new ChannelInitializer<SocketChannel>() {
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new ObjectEncoder())
                    .addLast(new ObjectDecoder(conf.getInt(RPC_SERIALIZE_MAXSIZE), ClassResolvers.cacheDisabled(null)))
                    .addLast(handler);
                }
            });
            ChannelFuture future = bootstrap.connect().sync();
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            logger.log(1, "["+host+":"+port+"] Method "+ivc.getMethod()+" in NettyRpcClient interrupted");
        } finally {
            group.shutdownGracefully();
        }
        synchronized (this) {
            while (result == null) {
                try { wait(100); } catch (InterruptedException e) {}
            }
        }
        return result;
    }

    private class ResultChannelHandler extends SimpleChannelInboundHandler<Object> {
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            ctx.writeAndFlush(ivc);
        }

        public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
            result = (Result) msg;
            ctx.close();
            synchronized (NettyRpcClient.this) {
                NettyRpcClient.this.notify();
            }
        }

        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            logger.error(cause);
            ctx.close();
        }
    }
}
