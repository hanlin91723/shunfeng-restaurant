package com.google.zxing.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.giants.imagepicker.ImageDataSource;
import com.giants.imagepicker.bean.ImageFolder;
import com.giants.imagepicker.bean.ImageItem;
import com.giants.imagepicker.view.FolderPopUpWindow;
import com.ninecm.np.R;

import java.util.List;


/**
 * Created by Administrator on 2018/1/10.
 */

public class ImageGridActivity extends AppCompatActivity implements ImageDataSource.OnImagesLoadedListener,View.OnClickListener,ImageRecyclerAdapter.OnImageItemClickListener{
    public static final int REQUEST_PERMISSION_STORAGE = 0x01;
    private ImageRecyclerAdapter adapter;//图片文件展示
    private GridView mGridView;  //图片展示控件
    private View mFooterBar;     //底部栏
    private RecyclerView mRecyclerView;//所有图片文件管理
    private ImageView ivBack;//返回按钮
    private Button mBtnDir;      //文件夹切换按钮
    private FolderPopUpWindow mFolderPopupWindow;  //ImageSet的PopupWindow
    private ImageFolderAdapter mImageFolderAdapter;
    private List<ImageFolder> mImageFolders;   //所有的图片文件夹

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_grid);
        mRecyclerView =(RecyclerView) findViewById(R.id.recycler);
        mGridView =(GridView) findViewById(R.id.gridview);
        mBtnDir =(Button) findViewById(R.id.btn_dir);
        mBtnDir.setOnClickListener(this);
        ivBack = (ImageView) findViewById(R.id.btn_back);
        ivBack.setOnClickListener(this);
        mFooterBar = findViewById(R.id.footer_bar);
        findViewById(R.id.btn_preview).setVisibility(View.GONE);
        findViewById(R.id.line_view).setVisibility(View.GONE);
        findViewById(R.id.btn_ok).setVisibility(View.GONE);
        adapter=new ImageRecyclerAdapter(this,null);
        mImageFolderAdapter = new ImageFolderAdapter(this, null);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
            if (checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                new ImageDataSource(this, null, this);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_STORAGE);
            }
        }else{
            new ImageDataSource(this, null, this);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                new ImageDataSource(this, null, this);
            } else {
                showToast("权限被禁止，无法选择本地图片");
            }
        }
    }
    @Override
    public void onImagesLoaded(List<ImageFolder> imageFolders) {
        this.mImageFolders = imageFolders;
        if (imageFolders.size() == 0) {
//            mImageGridAdapter.refreshData(null);
            adapter.refreshData(null);
        }
        else{
//            mImageGridAdapter.refreshData(imageFolders.get(0).images);
            adapter.refreshData(imageFolders.get(0).images);
        }
//        mImageGridAdapter.setOnImageItemClickListener(this);
//        mGridView.setAdapter(mImageGridAdapter);
        adapter.setOnImageItemClickListener(this);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this,3));
        mRecyclerView.setAdapter(adapter);
        mImageFolderAdapter.refreshData(imageFolders);
    }
    public boolean checkPermission(@NonNull String permission) {
        return ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
    }
    /** 创建弹出的ListView */
    private void createPopupFolderList() {
        mFolderPopupWindow = new FolderPopUpWindow(this, mImageFolderAdapter);
        mFolderPopupWindow.setOnItemClickListener(new FolderPopUpWindow.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                mImageFolderAdapter.setSelectIndex(position);
                mFolderPopupWindow.dismiss();
                ImageFolder imageFolder = (ImageFolder) adapterView.getAdapter().getItem(position);
                if (null != imageFolder) {
//                    mImageGridAdapter.refreshData(imageFolder.images);
                    adapter.refreshData(imageFolder.images);
                    mBtnDir.setText(imageFolder.name);
                }
                mGridView.smoothScrollToPosition(0);//滑动到顶部
            }
        });
        mFolderPopupWindow.setMargin(mFooterBar.getHeight());
    }
    @Override
    public void onClick(View view) {
        int id=view.getId();
        if (id==R.id.btn_back){
            finish();
        }else{
            if (mImageFolders == null) {
                Log.i("ImageGridActivity", "您的手机没有图片");
                return;
            }
            //点击文件夹按钮
            createPopupFolderList();
            mImageFolderAdapter.refreshData(mImageFolders);  //刷新数据
            if (mFolderPopupWindow.isShowing()) {
                mFolderPopupWindow.dismiss();
            } else {
                mFolderPopupWindow.showAtLocation(mFooterBar, Gravity.NO_GRAVITY, 0, 0);
                //默认选择当前选择的上一个，当目录很多时，直接定位到已选中的条目
                int index = mImageFolderAdapter.getSelectIndex();
                index = index == 0 ? index : index - 1;
                mFolderPopupWindow.setSelection(index);
            }
        }

    }
    public void showToast(String toastText) {
        Toast.makeText(getApplicationContext(), toastText, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onImageItemClick(View view, ImageItem imageItem, int position) {
        Intent intent=new Intent();
        intent.putExtra("imagePath",imageItem.path);
        setResult(RESULT_OK,intent);
        finish();
    }
}
