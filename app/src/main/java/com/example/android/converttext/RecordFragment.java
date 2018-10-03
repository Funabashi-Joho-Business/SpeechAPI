package com.example.android.converttext;

import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

class FilesAdapter extends RecyclerView.Adapter implements View.OnClickListener {
    public interface OnFileListener{
        public void onFile(File file);
    }
    private File[] mFiles;
    private OnFileListener mListener;

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_filelist, viewGroup, false);
        view.setOnClickListener(this);
        return new RecyclerView.ViewHolder(view){};
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        if(mFiles==null || i>=mFiles.length)
            return;
        viewHolder.itemView.setTag(i);
        TextView textName = viewHolder.itemView.findViewById(R.id.filename);
        TextView textDate = viewHolder.itemView.findViewById(R.id.date);
        TextView textSize = viewHolder.itemView.findViewById(R.id.size);

        Date date = new Date(mFiles[i].lastModified());

        textName.setText(mFiles[i].getName());
        textDate.setText(date.toString());
        textSize.setText(""+mFiles[i].length());
    }

    @Override
    public int getItemCount() {
        return mFiles==null?0:mFiles.length;
    }

    public void setFiles(File[] files){
        mFiles = files;
    }
    public void setOnFileListener(OnFileListener listener){
        mListener = listener;
    }
    @Override
    public void onClick(View v) {
        int index = (int)v.getTag();
        if(mListener != null)
            mListener.onFile(mFiles[index]);

    }
}

public class RecordFragment extends Fragment implements View.OnClickListener, MediaRecorder.OnInfoListener {

    private Timer mTimer;

    private MediaRecorder mRecorder;
    private RecyclerView mFileList;
    private FilesAdapter mFilesAdapter;

    private long mStartTime;
    private Handler mHandler = new Handler();
    private TimerTask mRecordTask = new TimerTask() {
        @Override
        public void run() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    long t = System.currentTimeMillis()-mStartTime;
                    TextView textView = getView().findViewById(R.id.recordInfo);
                    textView.setText(String.valueOf(t/1000)+"秒");
                }
            });
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_record, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.start).setOnClickListener(this);
        view.findViewById(R.id.stop).setOnClickListener(this);
        view.findViewById(R.id.reload).setOnClickListener(this);


        //データ表示用のビューを作成
        mFilesAdapter = new FilesAdapter();
        mFileList = view.findViewById(R.id.filelist);
        mFileList.setLayoutManager(new LinearLayoutManager(getContext()));     //アイテムを縦に並べる
        mFileList.setAdapter(mFilesAdapter);
        mFilesAdapter.setOnFileListener(new FilesAdapter.OnFileListener() {
            @Override
            public void onFile(File file) {
                Bundle bundle = new Bundle();
                bundle.putString("path",file.getPath());
                ((MainActivity)getActivity()).changeFragment(TextFragment.class,bundle);
            }
        });

        drawFiles();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.start:
                startRecord();
                break;
            case R.id.stop:
                stopRecord();
                drawFiles();
                break;
            case R.id.reload:
                drawFiles();
                break;
        }
    }

    void startRecord(){
        stopRecord();
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_WB);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);
        mRecorder.setOnInfoListener(this);
        //保存先
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String filePath = getContext().getExternalFilesDir("sound") + "/record"+sdf.format(new Date())+".awb";
        System.err.println(filePath);
        mRecorder.setOutputFile(filePath);
        try {
            mRecorder.prepare();
            mRecorder.start();
            mStartTime = System.currentTimeMillis();
            mTimer = new Timer();
            mTimer.schedule(mRecordTask,0,100);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    void stopRecord() {
        if(mRecorder != null) {
            mRecorder.stop();
            mRecorder.reset();
            mRecorder.release();
            mRecorder = null;
            mTimer.cancel();
            mTimer.purge();
            mTimer = null;
        }
    }

    @Override
    public void onInfo(MediaRecorder mr, int what, int extra) {
        System.out.println("OUTPUT:"+what);
//        switch (what){
//            MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED
//        }
    }

    public void drawFiles() {
        File path = getContext().getExternalFilesDir("sound");
        File[] list = path.listFiles();
        mFilesAdapter.setFiles(list);
        mFilesAdapter.notifyDataSetChanged();   //データ再表示要求

    }
}
