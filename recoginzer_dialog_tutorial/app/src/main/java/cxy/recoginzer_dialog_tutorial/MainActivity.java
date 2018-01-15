package cxy.recoginzer_dialog_tutorial;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;

import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private TextView voice_text;
    private ImageButton button_voice;
    private RecognizerDialog recognizerDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button_voice = (ImageButton) findViewById(R.id.button_voice);
        voice_text = (TextView) findViewById(R.id.voice_text);

        SpeechUtility.createUtility(this, SpeechConstant.APPID +"=5a3b7906");
        recognizerDialog = new RecognizerDialog(this, new InitListener() {
            @Override
            public void onInit(int code) {
                if (code != ErrorCode.SUCCESS) {
                    System.out.println("RecognizerDialog 初始化失败，错误码：" + code);
                }
            }
        });

        button_voice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!SpeechUtility.getUtility().checkServiceInstalled()){
                    String url = SpeechUtility.getUtility().getComponentUrl();
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    return;
                }

                recognizerDialog.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
                recognizerDialog.setParameter(SpeechConstant.DOMAIN, "iat");
                recognizerDialog.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
                recognizerDialog.setParameter(SpeechConstant.ACCENT, "mandarin");
                recognizerDialog.setListener(new RecognizerDialogListener() {
                    @Override
                    public void onResult(RecognizerResult recognizerResult, boolean b) {
                        String result = processJSON(recognizerResult.getResultString());
                        if (result.length() > 1) // 过滤标点符号
                            voice_text.setText(result);
                    }

                    @Override
                    public void onError(SpeechError speechError) {
                        System.out.println(speechError.toString());
                    }
                });
                recognizerDialog.show();
            }
        });
    }

    private String processJSON(String json) {
        StringBuffer result = new StringBuffer();
        try {
            JSONObject sentence = new JSONObject(json);
            JSONArray words = sentence.getJSONArray("ws");
            for (int i = 0; i < words.length(); i++) {
                JSONArray chineseWords = words.getJSONObject(i).getJSONArray("cw");
                JSONObject chineseWord = chineseWords.getJSONObject(0);
                result.append(chineseWord.getString("w"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result.toString();
    }
}
