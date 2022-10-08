import { Injectable } from '@nestjs/common';
import execa from 'execa';
import fs from 'fs';

@Injectable()
export class VideoService {
  private readonly GRIPP_DOWNLOAD_API = 'https://gripp.dev.njw.kr/download';

  public async makeStream(
    uuid: string,
    fileName: string,
    start: number,
    end: number,
  ) {
    const thumbnailSS = Math.trunc((end - start) / 2);
    const wgetCommand = `wget --no-verbose \\
      --user=${process.env.GRIPP_ADMIN_NAME} \\
      --password=${process.env.GRIPP_ADMIN_PASSWORD_RAW} \\
      -O videos/${fileName} \\
      ${this.GRIPP_DOWNLOAD_API}/${fileName}`;
    const ffmpegCommand = `ffmpeg -hide_banner -nostdin -y -loglevel warning \\
      -ss ${start} -to ${end} \\
      -i videos/${fileName} \\
      -filter_complex "[v]split=2[vt1][vt2];[vt1]scale=720:-2:flags=lanczos,format=yuv420p[vo1];[vt2]scale=480:-2:flags=lanczos,format=yuv420p[vo2]" \\
      -preset veryfast -crf 20 -sc_threshold 0 \\
      -force_key_frames "expr:gte(t,n_forced*2)" \\
      -map [vo1] -codec:v:0 h264 -profile:v:0 main -b:v:0 3000K -maxrate:v:0 3500K -bufsize:v:0 4500K \\
      -map [vo2] -codec:v:1 h264 -profile:v:1 main -b:v:1 1500K -maxrate:v:1 1750K -bufsize:v:1 2250K \\
      -map a -codec:a:0 aac -b:a:0 128K \\
      -map a -codec:a:1 aac -b:a:1 128K \\
      -var_stream_map "v:0,a:0,name:720 v:1,a:1,name:480" \\
      -f hls -hls_time 6 -hls_list_size 0 -hls_segment_type mpegts -hls_playlist_type vod \\
      -master_pl_name master.m3u8 \\
      -hls_segment_filename videos/${uuid}/stream_%v_%03d.ts \\
      videos/${uuid}/stream_%v.m3u8 \\
      -ss ${thumbnailSS} -frames:v 1 -filter:v "scale=480:-2:flags=lanczos" \\
      videos/${uuid}/thumbnail.png`;
    const opt = { shell: 'bash' };

    if (!fs.existsSync(`videos/${uuid}`)) {
      fs.mkdirSync(`videos/${uuid}`, { recursive: true });
    }

    try {
      const wget = execa.command(wgetCommand, opt);
      wget.stdout?.pipe(process.stdout);
      wget.stderr?.pipe(process.stderr);

      await wget;

      const ffmpeg = execa.command(ffmpegCommand, opt);
      ffmpeg.stdout?.pipe(process.stdout);
      ffmpeg.stderr?.pipe(process.stderr);

      await ffmpeg;
    } finally {
      fs.unlink(`videos/${fileName}`, (err) => {
        if (err) {
          console.error(err);
        }
      });
    }
  }
}
