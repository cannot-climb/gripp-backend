import { AmqpConnection, RabbitSubscribe } from '@golevelup/nestjs-rabbitmq';
import { Injectable } from '@nestjs/common';
import { ConsumeMessage } from 'amqplib';
import { VideoService } from './video.service';

@Injectable()
export class MessagingService {
  private static readonly MAX_ATTEMPTS = 5;
  private static readonly MAX_DEATH = MessagingService.MAX_ATTEMPTS - 1;

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
      if (
        Number(msg.properties.headers['x-death']?.at(0)?.count) >=
        MessagingService.MAX_DEATH
      ) {
        channel.ack(msg);
        return;
      }

      channel.nack(msg, false, false);
    },
  })
  public async processVideo(
    request: Record<string, unknown>,
    amqpMsg: ConsumeMessage,
  ) {
    console.log(request);

    if (!request.uuid || !request.fileName) {
      throw new Error('invalid request');
    }

    const useFallback =
      Number(amqpMsg.properties.headers['x-death']?.at(0)?.count) >=
      MessagingService.MAX_DEATH;
    const result = await this.videoService.makeStream(
      String(request.uuid).replace(/\//g, ''),
      String(request.fileName).replace(/\//g, ''),
      useFallback,
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
