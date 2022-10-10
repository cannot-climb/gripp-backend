FROM node:16-bullseye
RUN apt-get update && apt-get install -y \
    ffmpeg
WORKDIR /video-processor
RUN ["corepack", "enable"]
COPY .env* nest-cli.json package.json tsconfig*.json yarn.lock ./
RUN ["yarn"]
COPY src/ src/
RUN ["yarn", "build"]
CMD ["yarn", "start:prod"]
