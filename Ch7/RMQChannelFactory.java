/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rmqpool;

/**
 *
 * @author igortn
 */


import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import java.io.IOException;

public class RMQChannelFactory extends BasePooledObjectFactory<Channel> {

  private Connection connection;

  public RMQChannelFactory(Connection connection) {
    this.connection = connection;
  }

  @Override
  public Channel create() throws IOException {
    return connection.createChannel();
  }


  @Override
  public PooledObject<Channel> wrap(Channel channel) {
    return new DefaultPooledObject<Channel>(channel);
  }


  // for all other methods, the no-op implementation
  // in BasePooledObjectFactory will suffice
}
