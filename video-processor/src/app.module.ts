import {
  MessageHandlerErrorBehavior,
  RabbitMQModule,
} from '@golevelup/nestjs-rabbitmq';
import { Module } from '@nestjs/common';
import { ConfigModule } from '@nestjs/config';
import { MessagingService } from './service/messaging.service';
import { VideoService } from './service/video.service';

@Module({
  imports: [
    ConfigModule.forRoot({
      validate: (options) => {
        if (!options.GRIPP_RABBITMQ_ADDRESS) {
          throw new Error('no env');
        }

        return options;
      },
    }),
    RabbitMQModule.forRoot(RabbitMQModule, {
      uri: `${process.env.GRIPP_RABBITMQ_ADDRESS}`,
      exchanges: [
        {
          name: 'video-processor.dlx',
          type: 'direct',
        },
      ],
      prefetchCount: 1,
      connectionInitOptions: { wait: false },
      defaultSubscribeErrorBehavior: MessageHandlerErrorBehavior.NACK,
    }),
  ],
  controllers: [],
  providers: [MessagingService, VideoService],
})
export class AppModule {}
