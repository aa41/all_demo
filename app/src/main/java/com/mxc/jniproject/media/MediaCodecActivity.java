package com.mxc.jniproject.media;

import android.annotation.SuppressLint;
import android.media.MediaCodec;
import android.media.MediaCodecList;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.mxc.jniproject.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class MediaCodecActivity extends AppCompatActivity {
    private String filePath = "sdcard/test.mp4";
    private static final long TIMEOUT_US = 0;


    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_codec);

        File outputVideoFile = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "test1.mp4");
        File outputAudioFile = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "test1.aac");
        File outputMuxerFile = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "testMux.mp4");
        if (outputVideoFile.exists()) {
            outputVideoFile.delete();
        }
        if (outputAudioFile.exists()) {
            outputAudioFile.delete();
        }


        findViewById(R.id.select_video).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                extraSource(filePath, "video/", outputVideoFile.getAbsolutePath());
            }
        });

        findViewById(R.id.select_audio).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                extraSource(filePath, "audio/", outputAudioFile.getAbsolutePath());

            }
        });

        findViewById(R.id.muxer_audio).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        muxerVideo(outputAudioFile.getAbsolutePath(), outputVideoFile.getAbsolutePath(), outputMuxerFile.getAbsolutePath());
                    }
                }.start();
            }
        });


        findViewById(R.id.sync_codec).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                syncAacToPcm(outputAudioFile.getAbsolutePath());
            }
        });

        findViewById(R.id.async_codec).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                asyncAacToPcm(outputAudioFile.getAbsolutePath());
            }
        });

    }


    @SuppressLint("WrongConstant")
    private boolean muxerVideo(String inputAudio, String inputVideo, String outputVideo) {
        File outputVideoFile = new File(outputVideo);
        if (outputVideoFile.exists()) {
            outputVideoFile.delete();
        }
        File videoFile = new File(inputVideo);
        File audioFile = new File(inputAudio);
        if (!videoFile.exists() || !audioFile.exists()) {
            return false;
        }
        MediaExtractor videoExtractor = new MediaExtractor();
        MediaExtractor audioExtractor = new MediaExtractor();
        boolean isSuccess = false;

        try {
            MediaMuxer muxer = new MediaMuxer(outputVideo, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            int videoTrackIndex = 0;
            int audioTrackIndex = 0;
            videoExtractor.setDataSource(inputVideo);
            int count = videoExtractor.getTrackCount();
            for (int i = 0; i < count; i++) {
                MediaFormat trackFormat = videoExtractor.getTrackFormat(i);
                String mine = trackFormat.getString(MediaFormat.KEY_MIME);
                if (TextUtils.isEmpty(mine)) {
                    continue;
                }
                if (mine.startsWith("video/")) {
                    videoExtractor.selectTrack(i);
                    videoTrackIndex = muxer.addTrack(trackFormat);
                    break;
                }
            }

            audioExtractor.setDataSource(inputAudio);
            int audioExtractorTrackCount = audioExtractor.getTrackCount();
            for (int i = 0; i < audioExtractorTrackCount; i++) {
                MediaFormat trackFormat = audioExtractor.getTrackFormat(i);
                String mine = trackFormat.getString(MediaFormat.KEY_MIME);
                if (TextUtils.isEmpty(mine)) {
                    continue;
                }

                if (mine.startsWith("audio/")) {
                    audioExtractor.selectTrack(i);
                    audioTrackIndex = muxer.addTrack(trackFormat);
                    break;
                }
            }

            muxer.start();

            ByteBuffer buffer = ByteBuffer.allocate(500 * 1024);


            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            int sampleSize = 0;

            while ((sampleSize = videoExtractor.readSampleData(buffer, 0)) > 0) {
                bufferInfo.flags = videoExtractor.getSampleFlags();
                bufferInfo.size = sampleSize;
                bufferInfo.offset = 0;
                bufferInfo.presentationTimeUs = videoExtractor.getSampleTime();
                Log.e("111111", "bufferInfo: flags:" + bufferInfo.flags + "  size:" + bufferInfo.size + " offset:" + bufferInfo.offset + " time:" + bufferInfo.presentationTimeUs);
                muxer.writeSampleData(videoTrackIndex, buffer, bufferInfo);
                videoExtractor.advance();
            }

            int audioSampleSize = 0;
            MediaCodec.BufferInfo audioBufferInfo = new MediaCodec.BufferInfo();
            while ((audioSampleSize = audioExtractor.readSampleData(buffer, 0)) > 0) {
                audioBufferInfo.flags = audioExtractor.getSampleFlags();
                audioBufferInfo.offset = 0;
                audioBufferInfo.size = audioSampleSize;
                audioBufferInfo.presentationTimeUs = audioExtractor.getSampleTime();
                muxer.writeSampleData(audioTrackIndex, buffer, audioBufferInfo);
                Log.e("111111", "audioBufferInfo: flags:" + audioBufferInfo.flags + "  size:" + audioBufferInfo.size + " offset:" + audioBufferInfo.offset + " time:" + audioBufferInfo.presentationTimeUs);

                audioExtractor.advance();
            }
            muxer.stop();
            muxer.release();
            isSuccess = true;
        } catch (IOException e) {
            e.printStackTrace();
            isSuccess = false;
        } finally {
            videoExtractor.release();
            audioExtractor.release();
        }

        return isSuccess;
    }


    @SuppressLint("WrongConstant")
    private void extraSource(String inputVideo, String keyMine, String outputFilePath) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    MediaExtractor mediaExtractor = new MediaExtractor();
                    mediaExtractor.setDataSource(inputVideo);
                    int trackCount = mediaExtractor.getTrackCount();
                    for (int i = 0; i < trackCount; i++) {
                        MediaFormat trackFormat = mediaExtractor.getTrackFormat(i);
                        String mine = trackFormat.getString(MediaFormat.KEY_MIME);
                        if (TextUtils.isEmpty(mine)) {
                            continue;
                        }
                        if (mine.startsWith(keyMine)) {
                            mediaExtractor.selectTrack(i);
                            MediaMuxer muxer = new MediaMuxer(outputFilePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
                            muxer.addTrack(trackFormat);
                            muxer.start();
                            ByteBuffer buffer = ByteBuffer.allocate(500 * 1024);
                            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                            int sampleSize = 0;
                            while ((sampleSize = mediaExtractor.readSampleData(buffer, 0)) > 0) {
                                bufferInfo.flags = mediaExtractor.getSampleFlags();
                                bufferInfo.size = sampleSize;
                                bufferInfo.offset = 0;
                                bufferInfo.presentationTimeUs = mediaExtractor.getSampleTime();
                                muxer.writeSampleData(0, buffer, bufferInfo);
                                mediaExtractor.advance();
                            }
                            mediaExtractor.unselectTrack(i);
                            muxer.stop();
                            muxer.release();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }


    private MediaFormat chooseVideoTrack(MediaExtractor extractor,String mimeType) {
        int trackCount = extractor.getTrackCount();
        for (int i = 0; i < trackCount; i++) {
            MediaFormat trackFormat = extractor.getTrackFormat(i);
            String mime = trackFormat.getString(MediaFormat.KEY_MIME);
            if (!TextUtils.isEmpty(mime) && mime.startsWith(mimeType)) {
                extractor.selectTrack(i);
                return trackFormat;
            }
        }
        return null;
    }


    private MediaCodec createMediaCodec(MediaFormat format, Surface surface) throws IOException {
        if (format == null) return null;
        MediaCodecList codecList = new MediaCodecList(MediaCodecList.REGULAR_CODECS);
        MediaCodec codec = MediaCodec.createByCodecName(codecList.findDecoderForFormat(format));
        codec.configure(format, surface, null, 0);
        return codec;
    }


    private void asyncAacToPcm(String aacFilePath){
        new Thread(){
            @Override
            public void run() {
                super.run();
                File outputFile = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "aacToPcmAsync.pcm");
                if (outputFile.exists()) {
                    outputFile.delete();
                }
                MediaExtractor mediaExtractor = new MediaExtractor();
                FileOutputStream fileOutputStream = null;
                MediaCodec codec = null;
                try {
                    mediaExtractor.setDataSource(filePath);
                    fileOutputStream = new FileOutputStream(outputFile);
                    MediaFormat mediaFormat = chooseVideoTrack(mediaExtractor, "audio/");
                    codec = createMediaCodec(mediaFormat,null);

                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                        FileOutputStream finalFileOutputStream = fileOutputStream;
                        codec.setCallback(new MediaCodec.Callback() {
                            @Override
                            public void onInputBufferAvailable(@NonNull MediaCodec codec, int index) {
                                if(index >= 0){
                                    ByteBuffer inputBuffer = codec.getInputBuffer(index);
                                    if(inputBuffer != null){
                                        inputBuffer.clear();
                                    }

                                    int sampleSize = mediaExtractor.readSampleData(inputBuffer, 0);
                                    if(sampleSize < 0){
                                        codec.queueInputBuffer(index,0,0,0,MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                                    }else{
                                        codec.queueInputBuffer(index,0,sampleSize,mediaExtractor.getSampleTime(),0);
                                        mediaExtractor.advance();
                                    }
                                }
                            }

                            @Override
                            public void onOutputBufferAvailable(@NonNull MediaCodec codec, int index, @NonNull MediaCodec.BufferInfo info) {
                                if(index >= 0){
                                    ByteBuffer outputBuffer = codec.getOutputBuffer(index);
                                    byte[] bytes = new byte[info.size];
                                    outputBuffer.get(bytes);
                                    try {
                                        finalFileOutputStream.write(bytes);
                                        finalFileOutputStream.flush();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                    codec.releaseOutputBuffer(index,false);
                                    if((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0){
                                        try {
                                            finalFileOutputStream.close();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }

                                        mediaExtractor.release();
                                        codec.stop();
                                        codec.release();
                                    }
                                }
                            }

                            @Override
                            public void onError(@NonNull MediaCodec codec, @NonNull MediaCodec.CodecException e) {

                            }

                            @Override
                            public void onOutputFormatChanged(@NonNull MediaCodec codec, @NonNull MediaFormat format) {

                            }
                        });

                    }

                    if(codec != null){
                        codec.start();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }


    private void syncAacToPcm(String aacFilePath) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                File outputFile = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "aacToPcmSync.pcm");
                if (outputFile.exists()) {
                    outputFile.delete();
                }
                MediaExtractor mediaExtractor = new MediaExtractor();
                FileOutputStream fileOutputStream = null;
                MediaCodec codec = null;
                ByteBuffer[] inputBuffers = null;
                ByteBuffer[] outputBuffers = null;
                try {
                    fileOutputStream = new FileOutputStream(outputFile);

                    mediaExtractor.setDataSource(aacFilePath);
                    MediaFormat mediaFormat = chooseVideoTrack(mediaExtractor,"audio/");
                    codec = createMediaCodec(mediaFormat, null);
                    if (codec != null) {
                        codec.start();
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                            inputBuffers = codec.getInputBuffers();
                            outputBuffers = codec.getOutputBuffers();
                        }
                        boolean isInputBufferEOS = false;
                        boolean isOutputBufferEOS = false;
                        while (!isOutputBufferEOS) {
                            if (!isInputBufferEOS) {
                                int index = codec.dequeueInputBuffer(TIMEOUT_US);
                                if (index >= 0) {
                                    ByteBuffer inputBuffer;
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                        inputBuffer = codec.getInputBuffer(index);
                                    } else {
                                        inputBuffer = inputBuffers[index];
                                    }
                                    if (inputBuffer != null) {
                                        inputBuffer.clear();
                                    }

                                    int sampleSize = mediaExtractor.readSampleData(inputBuffer, 0);
                                    if (sampleSize < 0) {
                                        isInputBufferEOS = true;
                                        codec.queueInputBuffer(index, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                                    } else {
                                        codec.queueInputBuffer(index, 0, sampleSize, mediaExtractor.getSampleTime(), 0);
                                        mediaExtractor.advance();
                                    }
                                }
                            }

                            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                            int index = codec.dequeueOutputBuffer(bufferInfo, TIMEOUT_US);
                            if (index < 0) {
                                continue;
                            }
                            ByteBuffer outputBuffer;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                outputBuffer = codec.getOutputBuffer(index);
                            } else {
                                outputBuffer = outputBuffers[index];
                            }
                            byte[] bytes = new byte[bufferInfo.size];
                            outputBuffer.get(bytes);

                            fileOutputStream.write(bytes);
                            fileOutputStream.flush();
                            codec.releaseOutputBuffer(index, false);
                            if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                                isOutputBufferEOS = true;
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    mediaExtractor.release();
                    if (codec != null) {
                        codec.stop();
                        codec.release();
                    }

                }
            }
        }.start();


    }


}
