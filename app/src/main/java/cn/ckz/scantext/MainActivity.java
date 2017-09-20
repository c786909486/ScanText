package cn.ckz.scantext;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import cn.ckz.scantext.zxing.app.CaptureActivity;
import cn.ckz.scantext.zxing.util.ORCUtils;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private android.widget.Button qrcode;
    private android.widget.Button orccode;
    private android.widget.TextView showtext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.showtext = (TextView) findViewById(R.id.show_text);
        this.orccode = (Button) findViewById(R.id.orc_code);
        this.qrcode = (Button) findViewById(R.id.qr_code);
        qrcode.setOnClickListener(this);
        orccode.setOnClickListener(this);
        MainActivityPermissionsDispatcher.needWithCheck(this);
        new Thread(new Runnable() {
            @Override
            public void run() {
                ORCUtils.copyToSD(MainActivity.this);
            }
        }).start();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.qr_code:
                startActivity(new Intent(this, CaptureActivity.class));
                break;
            case R.id.orc_code:
                Intent intent = new Intent(this,CaptureActivity.class);
                intent.putExtra("ScanType",CaptureActivity.SCAN_TYPE_BANK_CARD);
                startActivity(intent);
                break;
        }
    }


    @NeedsPermission({Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void need() {
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }
}
