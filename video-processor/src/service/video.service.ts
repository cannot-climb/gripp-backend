import { Injectable } from '@nestjs/common';
import AWS from 'aws-sdk';
import axios from 'axios';
import execa from 'execa';
import fs from 'fs';
import mime from 'mime-types';
import path from 'path';
import urlJoin from 'url-join';
import { MakeStreamResponse } from './dto/make-stream.dto';

@Injectable()
export class VideoService {
  private readonly HIGH_RES = 720;
  private readonly MEDIUM_RES = 480;
  private readonly HLS_MASTER_FILE_NAME = 'master.m3u8';
  private readonly HLS_THUMBNAIL_FILE_NAME = 'thumbnail.png';
  private readonly GRIPP_DOWNLOAD_API = urlJoin(
    String(process.env.GRIPP_MAIN_API_ENDPOINT),
    'download',
  );
  private readonly GRIPP_PREDICT_API = urlJoin(
    String(process.env.GRIPP_DEEP_API_ENDPOINT),
    'kilterboard/upload',
  );
  private readonly GRIPP_PREDICT_PING_TIMEOUT_MS = 5000;
  private readonly GRIPP_PREDICT_PROCESS_TIMEOUT_MS = 1800000;
  private readonly GRIPP_PREDICT_FALLBACK_REASONABLE_DURATION_MIN = 30;
  private readonly GRIPP_PREDICT_FALLBACK_REASONABLE_DURATION_MAX = 90;
  private readonly s3 = new AWS.S3({
    credentials: {
      accessKeyId: `${process.env.GRIPP_AWS_ACCESS_KEY}`,
      secretAccessKey: `${process.env.GRIPP_AWS_SECRET_KEY}`,
    },
    endpoint: `${process.env.GRIPP_AWS_S3_ENDPOINT}`,
    region: `${process.env.GRIPP_AWS_REGION}`,
    s3ForcePathStyle: true,
    signatureVersion: 'v4',
  });

  public async makeStream(
    uuid: string,
    fileName: string,
    useFallback: boolean,
  ): Promise<MakeStreamResponse> {
    try {
      const opt = { shell: 'bash' };

      if (!fs.existsSync(`videos/${uuid}`)) {
        fs.mkdirSync(`videos/${uuid}`, { recursive: true });
      }

      const wgetCommand = `wget --no-verbose \\
      --user=${process.env.GRIPP_ADMIN_NAME} \\
      --password=${process.env.GRIPP_ADMIN_PASSWORD_RAW} \\
      -O videos/${fileName} \\
      ${this.GRIPP_DOWNLOAD_API}/${fileName}`;

      const wget = execa.command(wgetCommand, opt);
      wget.stdout?.pipe(process.stdout);
      wget.stderr?.pipe(process.stderr);
      await wget;

      const prediction = await this.predictVideo(fileName, useFallback);

      const hlsCommand = `ffmpeg -hide_banner -nostdin -y \\
      -ss ${prediction.startTime} -to ${prediction.endTime} \\
      -i videos/${fileName} \\
      -filter_complex "[v]split=2[vt1][vt2];[vt1]scale=${this.HIGH_RES}:-2,format=yuv420p[vo1];[vt2]scale=${this.MEDIUM_RES}:-2,format=yuv420p[vo2]" \\
      -preset veryfast -crf 20 -sc_threshold 0 \\
      -force_key_frames "expr:gte(t,n_forced*2)" \\
      -map [vo1] -codec:v:0 h264 -profile:v:0 main -b:v:0 3000K -maxrate:v:0 3500K -bufsize:v:0 4500K \\
      -map [vo2] -codec:v:1 h264 -profile:v:1 main -b:v:1 1500K -maxrate:v:1 1750K -bufsize:v:1 2250K \\
      -map a -codec:a:0 aac -b:a:0 128K \\
      -map a -codec:a:1 aac -b:a:1 128K \\
      -var_stream_map "v:0,a:0,name:${this.HIGH_RES} v:1,a:1,name:${this.MEDIUM_RES}" \\
      -f hls -hls_time 6 -hls_list_size 0 -hls_segment_type mpegts -hls_playlist_type vod \\
      -master_pl_name ${this.HLS_MASTER_FILE_NAME} \\
      -hls_segment_filename videos/${uuid}/stream_%v_%03d.ts \\
      videos/${uuid}/stream_%v.m3u8`;
      console.log(hlsCommand);

      const hls = execa.command(hlsCommand, opt);
      hls.stdout?.pipe(process.stdout);
      hls.stderr?.pipe(process.stderr);
      await hls;

      const ffprobeCommand = `ffprobe -v quiet -of json \\
      -select_streams v \\
      -show_format -show_streams \\
      videos/${uuid}/stream_${this.HIGH_RES}.m3u8`;

      const ffprobe = execa.command(ffprobeCommand, opt);
      ffprobe.stdout?.pipe(process.stdout);
      ffprobe.stderr?.pipe(process.stderr);
      const ffprobeResult = JSON.parse((await ffprobe).stdout || '{}');

      const thumbnailCommand = `ffmpeg -hide_banner -nostdin -y \\
      -ss ${(Number(ffprobeResult?.format?.duration || 0) / 2).toFixed(6)} \\
      -i videos/${uuid}/stream_${this.MEDIUM_RES}.m3u8 \\
      -frames:v 1 \\
      -f image2 -update 1 \\
      videos/${uuid}/${this.HLS_THUMBNAIL_FILE_NAME}`;
      console.log(thumbnailCommand);

      const thumbnail = execa.command(thumbnailCommand, opt);
      thumbnail.stdout?.pipe(process.stdout);
      thumbnail.stderr?.pipe(process.stderr);
      await thumbnail;

      console.log(`영상 인코딩 완료 - videos/${uuid}`);

      const uploadedUrls = await this.uploadFolder(`videos/${uuid}`);

      return {
        streamingUrl:
          uploadedUrls.find((url) => url.endsWith(this.HLS_MASTER_FILE_NAME)) ||
          '',
        streamingLength: Math.ceil(Number(ffprobeResult?.format?.duration)),
        streamingAspectRatio:
          ffprobeResult?.streams[0]?.height / ffprobeResult?.streams[0]?.width,
        thumbnailUrl:
          uploadedUrls.find((url) =>
            url.endsWith(this.HLS_THUMBNAIL_FILE_NAME),
          ) || '',
        certified: prediction.success,
      };
    } catch (e) {
      throw e;
    } finally {
      fs.unlink(`videos/${fileName}`, (err) => {
        if (err) {
          console.error(err);
        }
      });

      fs.rm(
        `videos/${uuid}`,
        {
          force: true,
          recursive: true,
          maxRetries: 10,
        },
        (err) => {
          if (err) {
            console.error(err);
          }
        },
      );
    }
  }

  private async predictVideo(fileName: string, predictFallback: boolean) {
    if (predictFallback) {
      return this.predictVideoFallback(fileName);
    }

    try {
      await axios.options(this.GRIPP_PREDICT_API, {
        validateStatus: null,
        timeout: this.GRIPP_PREDICT_PING_TIMEOUT_MS,
      });
    } catch (e) {
      console.log(e);
      return this.predictVideoFallback(fileName);
    }

    const deepNetworkResponse = await axios.post(
      this.GRIPP_PREDICT_API,
      {
        videoUrl: `${this.GRIPP_DOWNLOAD_API}/${fileName}`,
      },
      {
        headers: {
          Authorization: `Bearer ${process.env.GRIPP_DEEP_TOKEN}`,
        },
        timeout: this.GRIPP_PREDICT_PROCESS_TIMEOUT_MS,
      },
    );

    console.log({
      status: deepNetworkResponse.status,
      statusText: deepNetworkResponse.statusText,
      headers: deepNetworkResponse.headers,
      config: deepNetworkResponse.config,
      data: deepNetworkResponse.data,
    });

    let startTime = String(deepNetworkResponse.data?.startTime || '00:00:00');
    let endTime = String(deepNetworkResponse.data?.endTime || '00:60:00');

    if (startTime > endTime) {
      [startTime, endTime] = [endTime, startTime];
    }

    return {
      success: Boolean(deepNetworkResponse.data?.success),
      startTime,
      endTime,
    };
  }

  private async predictVideoFallback(fileName: string) {
    const opt = { shell: 'bash' };
    const ffprobeCommand = `ffprobe -v quiet -of json \\
    -select_streams v \\
    -show_format -show_streams \\
    videos/${fileName}`;

    const ffprobe = execa.command(ffprobeCommand, opt);
    ffprobe.stdout?.pipe(process.stdout);
    ffprobe.stderr?.pipe(process.stderr);
    const ffprobeResult = JSON.parse((await ffprobe).stdout || '{}');
    const duration = Number(ffprobeResult?.format?.duration || 0);

    return {
      success:
        duration >= this.GRIPP_PREDICT_FALLBACK_REASONABLE_DURATION_MIN &&
        duration <= this.GRIPP_PREDICT_FALLBACK_REASONABLE_DURATION_MAX,
      startTime: '00:00:00',
      endTime: '00:60:00',
    };
  }

  private async uploadFolder(folder: string) {
    const results: string[] = [];
    const files = fs.readdirSync(folder);

    if (!files || files.length === 0) {
      return [];
    }

    for (const fileName of files) {
      const filePath = path.join(folder, fileName);

      if (fs.lstatSync(filePath).isDirectory()) {
        continue;
      }

      const data = fs.readFileSync(filePath);
      const url = await (() => {
        return new Promise<string>((resolve, reject) => {
          this.s3.upload(
            {
              Bucket: `${process.env.GRIPP_AWS_S3_BUCKET}`,
              Key: filePath,
              Body: data,
              ContentType: mime.lookup(filePath) || undefined,
            },
            (err, data) => {
              if (err) {
                reject(err);
                return;
              }

              const url = this.hackS3UrlForOracleCloud(data.Location);
              resolve(url);
              console.log(`업로드 완료 - ${url}`);
            },
          );
        });
      })();

      results.push(url);
    }

    return results;
  }

  private hackS3UrlForOracleCloud(fileUrl: string) {
    const found = fileUrl.match(
      /^.*:\/\/([^.]+)\.compat\.objectstorage\.([^.]+)\.oraclecloud\.com\/([^/]+)\/(.+)$/i,
    );

    if (!found) {
      return fileUrl;
    }

    return `https://objectstorage.${found[2]}.oraclecloud.com/n/${found[1]}/b/${found[3]}/o/${found[4]}`;
  }
}
