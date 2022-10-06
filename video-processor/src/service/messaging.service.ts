import { AmqpConnection, RabbitSubscribe } from '@golevelup/nestjs-rabbitmq';
import { Injectable } from '@nestjs/common';
import { VideoService } from './video.service';

@Injectable()
export class MessagingService {
  constructor(
    private readonly amqpConnection: AmqpConnection,
    private readonly videoService: VideoService,
  ) {}

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
  public async processVideo(request: Record<string, unknown>) {
    console.log(request);

    if (String(request.uuid).length < 36 || !request.file) {
      throw new Error('invalid request');
    }

    await this.videoService.makeStream(
      String(request.uuid),
      String(request.file),
      10,
      40,
    );

    const response = {
      request,
      result: true,
      url: 'test',
      thumbnail: '',
      certified: true,
    };

    this.amqpConnection.publish('', 'video-processor-return', response);
    console.log(response);
  }
}
