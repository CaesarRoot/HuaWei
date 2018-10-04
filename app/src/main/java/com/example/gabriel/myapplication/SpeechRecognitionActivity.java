package com.example.gabriel.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.huawei.hiai.asr.AsrConstants;
import com.huawei.hiai.asr.AsrError;
import com.huawei.hiai.asr.AsrEvent;
import com.huawei.hiai.asr.AsrListener;
import com.huawei.hiai.asr.AsrRecognizer;
//import com.huawei.hiai.testme.hiaiapp.activities.MainActivity;
//import com.huawei.hiai.testme.hiaiapp.data.ResultInfo;
//import com.huawei.hiai.testme.hiaiapp.data.Utils;
//import com.huawei.hiai.vision.hiaiapp.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SpeechRecognitionActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "ASRActivity";
    public static final String AUDIO_SOURCE_TYPE_RECORDER = "recorder";
    public static final String AUDIO_SOURCE_TYPE_USER = "user";
    public static final String AUDIO_SOURCE_TYPE_FILE = "file";
    private static final String DEBUG_FILE_PATH = "dbgFilePath";

    private static final int END_AUTO_TEST = 0;
    private final static int INIT_ENGINE = 1;
    private static final int NEXT_FILE_TEST = 2;
    private static final int WRITE_RESULT_SD = 3;
    private static final int DELAYED_SATRT_RECORD = 4;
    private static final int DELAYED_STOP_RECORD = 5;
    private static final int DELAYED_WRITE_PCM = 6;

    private long startTime;
    private long endTime;
    private long waitTime;

    private AsrRecognizer mAsrRecognizer;
    private TextView showResult;
    private EditText input_result;
    private CheckBox right;
    private CheckBox wrong;
    private Button autoTest;
    private Button stopListeningBtn;
    private Button writePcmBtn;
    private Button cancelListeningBtn;
    private Button startRecord;

    private String mResult;
    private String writeResult;
    private boolean isAutoTest = true;
    private boolean isAutoTestEnd = false;
    private boolean isWritePcm = false;
    private int fileTotalCount;
    private int count = 0;

    private List<String> resultList = new ArrayList<>();
    private MyAsrListener mMyAsrListener = new MyAsrListener();
    private List<String> pathList = new ArrayList<>();
    private List<String> writePcmList = new ArrayList<>();

    private String TEST_HIAI_PATH = "/storage/emulated/0/Android/data/com.huawei.hiai";
    private String TEST_FILES_PATH = "/storage/emulated/0/Android/data/com.huawei.hiai/files";
    private String TEST_FILE_PATH = "/storage/emulated/0/Android/data/com.huawei.hiai/files/test";
    private String TEST_RESULT_FILE_PATH = "/storage/emulated/0/Android/data/com.huawei.hiai/files/result";
    private String TEST_PCM_PATH = "/storage/emulated/0/Android/data/com.huawei.hiai/files/pcm";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_recognizer);
        Utils.requestMicPermission(this);
        Utils.requestStoragePermission(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        makeResDir();
        initView();

        if (isSupportAsr()) {
            initEngine(AsrConstants.ASR_SRC_TYPE_RECORD);
        } else {
            Log.e(TAG, "not support asr!");
        }
    }

    private boolean isSupportAsr() {
        PackageManager packageManager = getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo("com.huawei.hiai", 0);
            Log.d(TAG, "Engine versionName: " + packageInfo.versionName + " ,versionCode: " + packageInfo.versionCode);
            if (packageInfo.versionCode <= 801000300) {
                return false;
            }
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
        return true;
    }

    private void makeResDir() {
        File root_test = new File(TEST_HIAI_PATH);
        File files_test = new File(TEST_FILES_PATH);
        File test = new File(TEST_FILE_PATH);
        File result = new File(TEST_RESULT_FILE_PATH);
        File pcm = new File(TEST_PCM_PATH);
        if (!root_test.exists()) {
            root_test.mkdir();
        }
        if (!files_test.exists()) {
            files_test.mkdir();
        }
        if (!test.exists()) {
            test.mkdir();
        }
        if (!result.exists()) {
            result.mkdir();
        }
        if (!pcm.exists()) {
            pcm.mkdir();
        }

        Log.d(TAG, "onCreate: " + TEST_FILE_PATH + "==" + TEST_RESULT_FILE_PATH + "==" + TEST_PCM_PATH);

    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause() ");
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop() ");
        super.onStop();
        reset();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroyEngine();
        mAsrRecognizer = null;
    }


    private void initView() {
        Log.d(TAG, "initView() ");

        writePcmBtn = (Button) findViewById(R.id.button_writePcm);
        writePcmBtn.setOnClickListener(this);

        stopListeningBtn = (Button) findViewById(R.id.button_stopListening);
        stopListeningBtn.setOnClickListener(this);

        cancelListeningBtn = (Button) findViewById(R.id.button_cacelListening);
        cancelListeningBtn.setOnClickListener(this);

        startRecord = (Button) findViewById(R.id.start_record);
        startRecord.setOnClickListener(this);

        autoTest = (Button) findViewById(R.id.auto_test);
        autoTest.setOnClickListener(this);

        Button commit = (Button) findViewById(R.id.submit);
        commit.setOnClickListener(this);

        showResult = (TextView) findViewById(R.id.start_record_show);
        input_result = (EditText) findViewById(R.id.input_result);

        right = (CheckBox) findViewById(R.id.result_right);
        wrong = (CheckBox) findViewById(R.id.result_wrong);

        right.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    wrong.setChecked(false);
                }
            }
        });
        wrong.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    right.setChecked(false);
                }
            }
        });

    }


    private void destroyEngine() {
        Log.d(TAG, "destroyEngine() ");
        if (mAsrRecognizer != null) {
            mAsrRecognizer.destroy();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_stopListening:
                stopListening();
                break;
            case R.id.button_writePcm:
                writePcm();
                break;
            case R.id.button_cacelListening:
                cancelListening();
                break;
            case R.id.start_record:
                startRecord();
                break;
            case R.id.auto_test:
                autoTest();
                break;
            case R.id.submit:
//                submit();
                break;
        }
    }


    private void submit() {
        boolean isRight = right.isChecked();
        boolean isWrong = wrong.isChecked();
        if (TextUtils.isEmpty(mResult) && (!isRight && !isWrong)) {
            Toast.makeText(this, "请单次说话测试并标注是否正确后点击确认！", Toast.LENGTH_SHORT).show();
            return;
        }
        String audioPath = getWavName();
        ResultInfo resultInfo = new ResultInfo();
        resultInfo.setWavName(audioPath);
        Log.d(TAG, "submit: ");

        if (isRight) {
            Log.d(TAG, "right.isChecked() " + right.isChecked());
            writeResult = mResult;
            resultInfo.setLable(writeResult);
            resultInfo.setCorrect("TRUE");
        }
        if (isWrong) {
            Log.d(TAG, "wrong.isChecked() " + wrong.isChecked());
            writeResult = mResult;
            resultInfo.setLable(input_result.getText().toString().trim());
            resultInfo.setCorrect("FALSE");
        }

        resultInfo.setRec(writeResult);
        Utils.writeNewName(resultInfo, TEST_RESULT_FILE_PATH, "/result.txt");
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.d(TAG, "handleMessage: " + msg.what);
            switch (msg.what) {
                case END_AUTO_TEST:
                    initEngine(AsrConstants.ASR_SRC_TYPE_RECORD);
                    startListening(-1, null);
                    isAutoTestEnd = false;
                    isWritePcm = false;
                    break;
                case INIT_ENGINE:
                    handleInitEngine();
                    break;
                case NEXT_FILE_TEST:
                    handleNextFileTest();
                    break;
                case WRITE_RESULT_SD:
                    showResult.setText("批量测试结束");
                    isAutoTestEnd = true;
                    setBtEnabled(true);
                    resultList.clear();
                    break;
                case DELAYED_SATRT_RECORD:
                    if (isAutoTestEnd || isWritePcm) {
                        if (mAsrRecognizer != null) {
                            mAsrRecognizer.destroy();
                        }
                        mHandler.sendEmptyMessageDelayed(END_AUTO_TEST, 300);
                    } else {
                        startListening(-1, null);
                    }
                    break;
                case DELAYED_STOP_RECORD:

                    break;
                case DELAYED_WRITE_PCM:
                    handleWritePcm();
                    break;
                default:
                    break;
            }
        }
    };

    private void autoTest() {
        Log.d(TAG, "autoTest() ");
        pathList.clear();
        fileTotalCount = getFilePath(TEST_FILE_PATH);
        Log.d(TAG, "fileTotalCount: " + pathList.toString());
        if (pathList.size() <= 0) {
            Toast.makeText(this, "请放入需要测试语音文件！", Toast.LENGTH_SHORT).show();
            return;
        }
        isAutoTest = true;
        if (mAsrRecognizer != null) {
            mAsrRecognizer.destroy();
        }
        mHandler.sendEmptyMessageDelayed(INIT_ENGINE, 1000);
    }

    private void handleNextFileTest() {
        if (isAutoTest) {
            if (count < fileTotalCount) {
                Log.d(TAG, "handleMessage: " + count + " path :" + pathList.get(count));
                startListening(AsrConstants.ASR_SRC_TYPE_FILE, pathList.get(count));
            }
        }
    }

    private void handleWriteResultSD() {
        Log.d(TAG, "writeAllResultInSD: count:" + count + "filePath :" + pathList.toString() + "waitTime :" + waitTime);
        String time = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        Utils.writeNewName(resultList, TEST_RESULT_FILE_PATH, time + "autoTestResult.txt");
        showResult.setText("批量测试结束");
        isAutoTestEnd = true;
        setBtEnabled(true);
        resultList.clear();
    }

    public void setBtEnabled(boolean isEnabled) {
        autoTest.setEnabled(isEnabled);
        stopListeningBtn.setEnabled(isEnabled);
        cancelListeningBtn.setEnabled(isEnabled);
        startRecord.setEnabled(isEnabled);
        writePcmBtn.setEnabled(isEnabled);
    }


    private void startRecord() {
        Log.d(TAG, "startRecord() ");
        isAutoTest = false;
        startRecord.setEnabled(false);
        showResult.setText("识别中：");
        mHandler.sendEmptyMessage(DELAYED_SATRT_RECORD);
    }

    private void initEngine(int srcType) {
        Log.d(TAG, "initEngine() ");
        mAsrRecognizer = AsrRecognizer.createAsrRecognizer(this);
        Intent initIntent = new Intent();
        initIntent.putExtra(AsrConstants.ASR_AUDIO_SRC_TYPE, srcType);

        if (mAsrRecognizer != null) {
            mAsrRecognizer.init(initIntent, mMyAsrListener);
        }

        mAsrRecognizer.startPermissionRequestForEngine();
    }

    private void handleInitEngine() {
        if (isAutoTest) {
            initEngine(AsrConstants.ASR_SRC_TYPE_FILE);
            setBtEnabled(false);
            Log.d(TAG, "handleMessage: " + count + " path :" + pathList.get(count));
            startListening(AsrConstants.ASR_SRC_TYPE_FILE, pathList.get(count));
        }
    }

    private void startListening(int srcType, String filePath) {
        Log.d(TAG, "startListening() " + "src_type:" + srcType);
        if (count == 0) {
            startTime = getTimeMillis();
        }
        Intent intent = new Intent();
        intent.putExtra(AsrConstants.ASR_VAD_FRONT_WAIT_MS, 4000);
        intent.putExtra(AsrConstants.ASR_VAD_END_WAIT_MS, 5000);
        intent.putExtra(AsrConstants.ASR_TIMEOUT_THRESHOLD_MS, 20000);
        if (srcType == AsrConstants.ASR_SRC_TYPE_FILE) {
            Log.d(TAG, "startListening() :filePath= " + filePath);
            intent.putExtra(AsrConstants.ASR_SRC_FILE, filePath);
        }
        if (mAsrRecognizer != null) {
            mAsrRecognizer.startListening(intent);
        }
    }

    private void stopListening() {
        Log.d(TAG, "stopListening() ");
        if (mAsrRecognizer != null) {
            mAsrRecognizer.stopListening();
        }
    }

    private void cancelListening() {
        Log.d(TAG, "cancelListening() ");
        startRecord.setEnabled(true);
        if (mAsrRecognizer != null) {
            mAsrRecognizer.cancel();
        }
    }

    private void writePcm() {
        Log.d(TAG, "writePcm() ");
        writePcmList.clear();
        getFilePath(TEST_PCM_PATH);
        if (writePcmList.size() == 0) {
            Toast.makeText(this, "请放入PCM文件", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!isWritePcm) {
            if (mAsrRecognizer != null) {
                mAsrRecognizer.destroy();
            }
        }
        mHandler.sendEmptyMessageDelayed(DELAYED_WRITE_PCM, 1000);
    }

    private void handleWritePcm() {
        Log.d(TAG, "handleWritePcm() ");
        if (!isWritePcm) {
            initEngine(AsrConstants.ASR_SRC_TYPE_PCM);
        }
        isWritePcm = true;
        startListening(AsrConstants.ASR_SRC_TYPE_PCM, null);

        ByteArrayOutputStream bos = null;
        BufferedInputStream in = null;
        try {
            File file = new File(writePcmList.get(0));
            if (!file.exists()) {
                throw new FileNotFoundException("file not exists");
            }
            bos = new ByteArrayOutputStream((int) file.length());
            in = new BufferedInputStream(new FileInputStream(file));
            int buf_size = 1280;
            byte[] buffer = new byte[buf_size];
            int len = 0;
            while (-1 != (len = in.read(buffer, 0, buf_size))) {
                bos.reset();
                bos.write(buffer, 0, len);
                mAsrRecognizer.writePcm(bos.toByteArray(), bos.toByteArray().length);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (bos != null) {
                    bos.close();
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void updateLexicon() {
        Log.d(TAG, "updateLexicon() ");
        Intent intent = new Intent();
        intent.putExtra("key", "updateLexicon");
        if (mAsrRecognizer != null) {
            mAsrRecognizer.updateLexicon(intent);
        }
    }

    private class MyAsrListener implements AsrListener {
        @Override
        public void onInit(Bundle params) {
            Log.d(TAG, "onInit() called with: params = [" + params + "]");
        }

        @Override
        public void onBeginningOfSpeech() {
            Log.d(TAG, "onBeginningOfSpeech() called");
        }

        @Override
        public void onRmsChanged(float rmsdB) {
            Log.d(TAG, "onRmsChanged() called with: rmsdB = [" + rmsdB + "]");
        }

        @Override
        public void onBufferReceived(byte[] buffer) {
            Log.d(TAG, "onBufferReceived() called with: buffer = [" + buffer + "]");
        }

        @Override
        public void onEndOfSpeech() {
            Log.d(TAG, "onEndOfSpeech: ");


        }

        @Override
        public void onError(int error) {
            Log.d(TAG, "onError() called with: error = [" + error + "]");
            if (error == AsrError.SUCCESS) {
                return;
            }

            if (error == AsrError.ERROR_CLIENT_INSUFFICIENT_PERMISSIONS) {
                Toast.makeText(getApplicationContext(), "请在设置中打开麦克风权限!", Toast.LENGTH_LONG).show();
            }

            setBtEnabled(true);
        }


        @Override
        public void onResults(Bundle results) {
            Log.d(TAG, "onResults() called with: results = [" + results + "]");
            endTime = getTimeMillis();
            waitTime = endTime - startTime;
            mResult = getOnResult(results, AsrConstants.RESULTS_RECOGNITION);

            stopListening();
            if (isAutoTest) {
                resultList.add(pathList.get(count) + "\t" + mResult);
                Log.d(TAG, "isAutoTest: " + waitTime + "count :" + count);
                if (count == fileTotalCount - 1) {
                    resultList.add("\n\nwaittime:\t" + waitTime + "ms");
                    mHandler.sendEmptyMessage(WRITE_RESULT_SD);
                    Log.d(TAG, "waitTime: " + waitTime + "count :" + count);
                    count = 0;
                } else {
                    Log.d(TAG, "isAutoTest: else" + waitTime + "count :" + count);
                    count += 1;
                    mHandler.sendEmptyMessageDelayed(NEXT_FILE_TEST, 1000);
                }
            } else {
                startRecord.setEnabled(true);
            }

        }

        @Override
        public void onPartialResults(Bundle partialResults) {
            Log.d(TAG, "onPartialResults() called with: partialResults = [" + partialResults + "]");
            getOnResult(partialResults, AsrConstants.RESULTS_PARTIAL);
        }

        @Override
        public void onEnd() {

        }

        private String getOnResult(Bundle partialResults, String key) {
            Log.d(TAG, "getOnResult() called with: getOnResult = [" + partialResults + "]");
            String json = partialResults.getString(key);
            final StringBuilder sb = new StringBuilder();
            try {
                JSONObject result = new JSONObject(json);
                JSONArray items = result.getJSONArray("result");
                for (int i = 0; i < items.length(); i++) {
                    String word = items.getJSONObject(i).getString("word");
                    double confidences = items.getJSONObject(i).getDouble("confidence");
                    sb.append(word);
                    Log.d(TAG, "asr_engine: result str " + word);
                    Log.d(TAG, "asr_engine: confidence " + String.valueOf(confidences));
                }
                Log.d(TAG, "getOnResult: " + sb.toString());
                showResult.setText(sb.toString());
            } catch (JSONException exp) {
                Log.w(TAG, "JSONException: " + exp.toString());
            }
            return sb.toString();
        }

        @Override
        public void onEvent(int eventType, Bundle params) {
            Log.d(TAG, "onEvent() called with: eventType = [" + eventType + "], params = [" + params + "]");
//            switch (eventType) {
//                case AsrEvent.EVENT_PERMISSION_RESULT:
//                    int result = params.getInt(AsrEvent.EVENT_KEY_PERMISSION_RESULT, PackageManager.PERMISSION_DENIED);
//                    if (result != PackageManager.PERMISSION_GRANTED) {
//                        reset();
//                    }
//                default:
//                    return;
//            }
        }
    }


    /**
     * 通过递归得到当前文件夹里所有的文件数量和路径
     *
     * @param path
     * @return
     */
    public int getFilePath(String path) {
        int sum = 0;
        Log.i(TAG, "getFilePath()" + path);
        try {
            File file = new File(path);
            File[] list = file.listFiles();
            if (list == null) {
                Log.d(TAG, "getFilePath: fileList is null!");
                return 0;
            }
            for (int i = 0; i < list.length; i++) {
                if (list[i].isFile()) {
                    String[] splitPATH = list[i].toString().split("\\.");
                    if (splitPATH[splitPATH.length - 1].equals("pcm") ||
                            splitPATH[splitPATH.length - 1].equals("wav")) {
                        sum++;
                        pathList.add(list[i].toString());
                        writePcmList.add(list[i].toString());
                    }
                } else {
                    sum += getFilePath(list[i].getPath());
                }
            }
        } catch (NullPointerException ne) {
            Toast.makeText(this, "找不到指定路径！", Toast.LENGTH_SHORT).show();
        }
        return sum;
    }

    public String getWavName() {
        File file = new File(TEST_PCM_PATH);
        File[] files = file.listFiles();
        if (files == null) {
            Toast.makeText(this, "PCM文件或文件夹不存在！", Toast.LENGTH_SHORT).show();
            return null;
        }
        if (files.length == 0) {
            Log.i(TAG, "getWavName() : file not found!");
            return null;
        }
        return files[files.length - 1].toString();
    }

    public long getTimeMillis() {
        long time = System.currentTimeMillis();
        return time;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.i(TAG, "onBackPressed() : finish()");
        finish();
    }

    private void reset() {
        cancelListening();
        setBtEnabled(true);
        showResult.setText("");
        input_result.setText("");
    }
}
