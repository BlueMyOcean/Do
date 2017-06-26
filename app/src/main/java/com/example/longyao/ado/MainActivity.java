package com.example.longyao.ado;

import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback,
        Camera.PreviewCallback {

    private SurfaceView mSurfaceview = null; // SurfaceView对象：(视图组件)视频显示
    private SurfaceHolder mSurfaceHolder = null; // SurfaceHolder对象：(抽象接口)SurfaceView支持类
    private Camera mCamera = null; // Camera对象，相机预览

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initCamera();
        mSurfaceview = (SurfaceView) findViewById(R.id.surfaceview);
        mSurfaceHolder = mSurfaceview.getHolder(); // 绑定SurfaceView，取得SurfaceHolder对象
        mSurfaceHolder.addCallback(this); // SurfaceHolder加入回调接口
        // mSurfaceHolder.setFixedSize(176, 144); // 预览大小設置
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);// 設置顯示器類型，setType必须设置
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{android.Manifest.permission.CAMERA},200);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults) {
        switch (requestCode){
            case 200:
                if (grantResults.length>0&&grantResults[0]== PackageManager.PERMISSION_GRANTED){
                    //这里已经获取到了摄像头的权限，想干嘛干嘛了可以

                }else {
                    //这里是拒绝给APP摄像头权限，给个提示什么的说明一下都可以。
                    Toast.makeText(MainActivity.this,"请手动打开相机权限",Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mCamera==null) {
            initCamera();
        }
    }

    private void initCamera()
    {
        mCamera = Camera.open();
        mCamera.setDisplayOrientation(90);//摄像头进行旋转90°
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setPreviewFpsRange(4, 10);
        //设置图片格式
        parameters.setPictureFormat(ImageFormat.JPEG);
        //设置图片的质量
        parameters.set("jpeg-quality", 90);
        //设置照片的大小
//        parameters.setPictureSize(viewWidth, viewHeight);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {

        File sdDir=null;
        boolean sdCardExist = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        if(sdCardExist)
        {
            sdDir = Environment.getExternalStorageDirectory();//获取根目录
        }else {
            Toast.makeText(this,"未发现存储卡",Toast.LENGTH_SHORT).show();
            return;
        }

        // 刚刚拍照的文件名
        String fileName = "IMG_"
                + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date())
                .toString() + ".jpg";
//        File sdRoot = Environment.getExternalStorageDirectory();
//        String dir = "/Camera/";
//        File mkDir = new File(sdRoot, dir);
//        if (!mkDir.exists())
//            mkDir.mkdirs();
        File pictureFile = new File(sdDir,"/"+fileName);
        if (!pictureFile.exists()) {
            try {
                pictureFile.createNewFile();
                Camera.Parameters parameters = mCamera.getParameters();
                Camera.Size size = parameters.getPreviewSize();
                YuvImage image = new YuvImage(bytes,
                        parameters.getPreviewFormat(), size.width, size.height,
                        null);
                FileOutputStream filecon = new FileOutputStream(pictureFile);

                image.compressToJpeg(
                        new Rect(0, 0, image.getWidth(), image.getHeight()),
                        70, filecon);
                Toast.makeText(this,"保存成功！",Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Toast.makeText(this,"保存失败！",Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }

    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

        try {
            if (mCamera != null) {
                mCamera.setPreviewDisplay(mSurfaceHolder);
                mCamera.startPreview();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

        if (mCamera == null) {
            return;
        }
        mCamera.stopPreview();
        mCamera.setPreviewCallback(this);
        mCamera.startPreview();

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {



    }
}
