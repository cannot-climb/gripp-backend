FROM node:16-bullseye
WORKDIR /ffmpeg
RUN wget https://johnvansickle.com/ffmpeg/releases/ffmpeg-release-$(dpkg --print-architecture)-static.tar.xz -O ffmpeg.tar.xz
RUN ["tar", "Jxvf", "ffmpeg.tar.xz", "--strip-components", "1"]
RUN ["rm", "ffmpeg.tar.xz"]
RUN ["ln", "-s", "/ffmpeg/ffmpeg", "/usr/local/bin/ffmpeg"]
RUN ["ln", "-s", "/ffmpeg/ffprobe", "/usr/local/bin/ffprobe"]

WORKDIR /video-processor
RUN ["corepack", "enable"]
COPY .env* nest-cli.json package.json tsconfig*.json yarn.lock ./
RUN ["yarn"]
COPY src/ src/
RUN ["yarn", "build"]
CMD ["yarn", "start:prod"]
