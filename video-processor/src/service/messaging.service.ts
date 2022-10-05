import { AmqpConnection, RabbitSubscribe } from '@golevelup/nestjs-rabbitmq';
import { Injectable } from '@nestjs/common';

@Injectable()
export class MessagingService {
  constructor(private readonly amqpConnection: AmqpConnection) {}

  @RabbitSubscribe({
    exchange: 'video-processor.dlx',
    routingKey: 'video-processor',
    queue: 'video-processor',
    queueOptions: {
      deadLetterExchange: 'video-processor.dlx',
    },
    errorHandler: (channel, msg) => {
      if (msg.properties.headers['x-death']?.length > 0) {
        if (msg.properties.headers['x-death'][0]?.count >= 5) {
          channel.ack(msg);
          return;
        }
      }

      channel.nack(msg, false, false);
    },
  })
  public async processVideo(request?: Record<string, unknown>) {
    const response = {
      request,
      result: true,
      url: 'test',
      certified: true,
    };
    this.amqpConnection.publish('', 'video-processor-return', response);
    console.log(response);
  }
}
