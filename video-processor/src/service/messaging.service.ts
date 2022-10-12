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

    if (!request.uuid || !request.fileName) {
      throw new Error('invalid request');
    }

    const result = await this.videoService.makeStream(
      String(request.uuid).replace(/\//g, ''),
      String(request.fileName).replace(/\//g, ''),
    );

    const response = {
      request,
      streamingUrl: result.streamingUrl,
      streamingLength: result.streamingLength,
      streamingAspectRatio: result.streamingAspectRatio,
      thumbnailUrl: result.thumbnailUrl,
      certified: result.certified,
    };
    console.log(response);

    if (
      !response.streamingUrl ||
      !response.streamingLength ||
      !response.streamingAspectRatio ||
      !response.thumbnailUrl
    ) {
      throw new Error('invalid response');
    }

    this.amqpConnection.publish('', 'video-processor-return', response);
  }
}
