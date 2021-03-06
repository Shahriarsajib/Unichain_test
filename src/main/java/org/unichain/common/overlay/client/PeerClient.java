package org.unichain.common.overlay.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.unichain.common.overlay.discover.node.Node;
import org.unichain.common.overlay.discover.node.NodeHandler;
import org.unichain.common.overlay.server.UnichainChannelInitializer;
import org.unichain.core.config.args.Args;
import org.unichain.protos.Protocol.ReasonCode;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j(topic = "net")
@Component
public class PeerClient {

  @Autowired
  private ApplicationContext ctx;

  private EventLoopGroup workerGroup;

  public PeerClient() {
    workerGroup = new NioEventLoopGroup(0, new ThreadFactory() {
      private AtomicInteger cnt = new AtomicInteger(0);

      @Override
      public Thread newThread(Runnable r) {
        return new Thread(r, "UnichainJClientWorker-" + cnt.getAndIncrement());
      }
    });
  }

  public void connect(String host, int port, String remoteId) {
    try {
      ChannelFuture f = connectAsync(host, port, remoteId, false);
      f.sync().channel().closeFuture().sync();
    } catch (Exception e) {
      logger
          .info("PeerClient: Can't connect to " + host + ":" + port + " (" + e.getMessage() + ")");
    }
  }

  public ChannelFuture connectAsync(NodeHandler nodeHandler, boolean discoveryMode) {
    Node node = nodeHandler.getNode();
    return connectAsync(node.getHost(), node.getPort(), node.getHexId(), discoveryMode)
        .addListener((ChannelFutureListener) future -> {
          if (!future.isSuccess()) {
            logger.warn("Connect to {}:{} fail,cause:{}", node.getHost(), node.getPort(),
                future.cause().getMessage());
            nodeHandler.getNodeStatistics().nodeDisconnectedLocal(ReasonCode.CONNECT_FAIL);
            nodeHandler.getNodeStatistics().notifyDisconnect();
            future.channel().close();
          }
        });
  }

  private ChannelFuture connectAsync(String host, int port, String remoteId,
      boolean discoveryMode) {

    logger.info("Connect peer {} {} {}", host, port, remoteId);

    UnichainChannelInitializer unichainChannelInitializer = ctx
        .getBean(UnichainChannelInitializer.class, remoteId);
    unichainChannelInitializer.setPeerDiscoveryMode(discoveryMode);

    Bootstrap b = new Bootstrap();
    b.group(workerGroup);
    b.channel(NioSocketChannel.class);

    b.option(ChannelOption.SO_KEEPALIVE, true);
    b.option(ChannelOption.MESSAGE_SIZE_ESTIMATOR, DefaultMessageSizeEstimator.DEFAULT);
    b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, Args.getInstance().getNodeConnectionTimeout());
    b.remoteAddress(host, port);

    b.handler(unichainChannelInitializer);

    // Start the client.
    return b.connect();
  }

  public void close() {
    workerGroup.shutdownGracefully();
    workerGroup.terminationFuture().syncUninterruptibly();
  }
}
