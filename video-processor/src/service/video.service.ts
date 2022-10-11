import { Injectable } from '@nestjs/common';
import axios from 'axios';
import execa from 'execa';
import fs from 'fs';

@Injectable()
export class VideoService {
  private readonly GRIPP_DOWNLOAD_API = 'https://gripp.dev.njw.kr/download';

  public async makeStream(uuid: string, fileName: string) {
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

      const { data: deepNetworkResult } = await axios.post(
        'http://gripp-deep.njw.kr/kilterboard/upload',
        {
          videoUrl: `${this.GRIPP_DOWNLOAD_API}/${fileName}`,
        },
        {
          headers: {
            Authorization: `Bearer ${process.env.GRIPP_DEEP_TOKEN}`,
          },
          timeout: 300000,
        },
      );

      const start = deepNetworkResult?.startTime || '00:00:00';
      const end = deepNetworkResult?.endTime || '00:60:00';
      const hlsCommand = `ffmpeg -hide_banner -nostdin -y \\
      -ss ${start} -to ${end} \\
      -i videos/${fileName} \\
      -filter_complex "[v]split=2[vt1][vt2];[vt1]scale=720:-2,format=yuv420p[vo1];[vt2]scale=480:-2,format=yuv420p[vo2]" \\
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
      videos/${uuid}/stream_%v.m3u8`;

      const hls = execa.command(hlsCommand, opt);
      hls.stdout?.pipe(process.stdout);
      hls.stderr?.pipe(process.stderr);
      await hls;

      const ffprobeCommand = `ffprobe -v quiet -of json \\
      -select_streams v \\
      -show_format -show_streams \\
      videos/${uuid}/stream_720.m3u8`;

      const ffprobe = execa.command(ffprobeCommand, opt);
      ffprobe.stdout?.pipe(process.stdout);
      ffprobe.stderr?.pipe(process.stderr);
      const ffprobeResult = JSON.parse((await ffprobe).stdout || '{}');

      const thumbnailCommand = `ffmpeg -hide_banner -nostdin -y \\
      -ss ${(Number(ffprobeResult?.format?.duration || 0) / 2).toFixed(6)} \\
      -i videos/${uuid}/stream_480.m3u8 \\
      -frames:v 1 \\
      -f image2 -update 1 \\
      videos/${uuid}/thumbnail.png`;

      const thumbnail = execa.command(thumbnailCommand, opt);
      thumbnail.stdout?.pipe(process.stdout);
      thumbnail.stderr?.pipe(process.stderr);
      await thumbnail;

      console.log(`영상 인코딩 완료 - ${uuid}/master.m3u8`);
    } finally {
      fs.unlink(`videos/${fileName}`, (err) => {
        if (err) {
          console.error(err);
        }
      });
    }
  }
}
