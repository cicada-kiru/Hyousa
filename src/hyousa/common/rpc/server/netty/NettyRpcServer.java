package hyousa.common.rpc.server.netty;

import hyousa.common.conf.Configuration;
import hyousa.common.log.Logger;
import hyousa.common.log.LoggerFactory;
import hyousa.common.rpc.Invocation;
import hyousa.common.rpc.Result;
import hyousa.common.rpc.server.RpcServer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by yousa on 2017/12/1.
 */
public class NettyRpcServer extends RpcServer {
    private static final String RPC_SERIALIZE_MAXSIZE = "rpc.netty.serialize.maxsize";

    private Logger logger = LoggerFactory.getLogger(NettyRpcServer.class);

    public NettyRpcServer(Object delegate) {
        super(delegate);
    }

    public void start(int port, Configuration conf) {
        InvocationChannelHandler handler = new InvocationChannelHandler();
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(group)
            .channel(NioServerSocketChannel.class)
            .localAddress(port)
            .childHandler(new ChannelInitializer<SocketChannel>() {
        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline()
                    .addLast(new ObjectEncoder())
                    .addLast(new ObjectDecoder(conf.getInt(RPC_SERIALIZE_MAXSIZE), ClassResolvers.cacheDisabled(this.getClass().getClassLoader())))
                    .addLast(handler);
                }
            });
            ChannelFuture future = bootstrap.bind().sync();
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            logger.log(1, "[localhost:"+port+"] NettyRpcServer.start() interrupted");
        } finally {
            group.shutdownGracefully();
        }
    }

    private Result invoke(Invocation ivc) {
        try {
            Method method = delegate.getClass().getDeclaredMethod(ivc.getMethod(), ivc.getTypes());
            boolean isVoid = method.getReturnType() == void.class;
            Object result = null;
            boolean hasError = false;
            Throwable cause = null;
            try {
                result = method.invoke(delegate, ivc.getParams());
            } catch (InvocationTargetException e) {
                hasError = true;
                cause = e.getCause();
//                logger.error(e.getCause()); //debug
            } catch (IllegalAccessException | IllegalArgumentException e) {
                //impossible
                logger.error(e);
                System.exit(1);
            }
            return new SerializableResult(isVoid, hasError, (Serializable)result, cause);
        } catch (NoSuchMethodException e) {
            //impossible
            logger.error(e);
            System.exit(1);
            return null;
        }
    }

    /**
     * Created by yousa on 2017/12/2.
     */
    @ChannelHandler.Sharable
    public class InvocationChannelHandler extends ChannelInboundHandlerAdapter {
        public void channelRead(ChannelHandlerContext ctx, Object obj) throws Exception {
            ctx.writeAndFlush(invoke((Invocation) obj));
        }

        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            ctx.close();
        }

        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            logger.error(cause);
            ctx.close();
        }
    }
}
