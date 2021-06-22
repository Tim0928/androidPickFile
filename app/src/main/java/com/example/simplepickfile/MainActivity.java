package com.example.simplepickfile;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainActivity extends AppCompatActivity {
    String rootPath = "";  //外置 SD 卡路径
    String packageName = "com.example.simplepickfile";
    private EditText editTextPath;
    private static final int BROWSE_PERMISSION_CODE = 1000;
//    private static final int MY_REQUEST_CODE_ANY_PERMISSION = 1001;
//    private static final int MY_REQUEST_CODE_WRITE_SETTING_PERMISSION = 1002;
//    private static final int MY_REQUEST_CODE_MOUNT_PERMISSION = 1003;
//    private static final int MY_REQUEST_CODE_MANAGE_EXTERNAL_STORAGE_PERMISSION = 1004;
//    private static final int MY_REQUEST_CODE_MANAGE_DOCUMENTS_PERMISSION = 1005;
    private static final int FILECHOOSER_CODE = 2000;
    private static final int STORAGE_PERMISSION_CODE = 100;
    private String TAG = "MainActivity";


    private List<String> unPermissionList = new ArrayList<String>(); //申请未得到授权的权限列表
    private String[] permissionList = new String[]{    //申请的权限列表
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editTextPath = (EditText) findViewById(R.id.editText_path);

        checkPermission();


//        rootPath = getStoragePath(getApplicationContext(),true);  //获取 可以插拔的 sd 卡  路径
//        Log.i(TAG,"lum rootPath： " + rootPath);
//        if (DocumentsUtils.checkWritableRootPath(getApplicationContext(), rootPath)) {   //检查sd卡路径是否有 权限 没有显示dialog
//            Log.i(TAG,"lum rootPath： Permission no"  );
//            Toast.makeText(getApplicationContext(), "please pick folder get permission", Toast.LENGTH_LONG).show();
//            showOpenDocumentTree(rootPath);
//        }else{
//            Log.i(TAG,"lum rootPath： Permission yes"  );
//        }
//

    }

    public void pickonClick(View view){
        askPermissionAndBrowseFile();
    }



    private void askPermissionAndBrowseFile()  {
        // With Android Level >= 23, you have to ask the user
        // for permission to access External Storage.
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) { // Level 23

            // Check if we have Call permission  取得權限
            int permisson = ActivityCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE);

            if (permisson != PackageManager.PERMISSION_GRANTED) {
                // If don't have permission so prompt the user.
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, BROWSE_PERMISSION_CODE);
                return;
            }
        }
        doBrowseFile();
    }

    //open folder
    private void doBrowseFile(){
        Intent chooseFileIntent = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFileIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        chooseFileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        chooseFileIntent.setType("*/*");
        // Only return URIs that can be opened with ContentResolver
        chooseFileIntent.addCategory(Intent.CATEGORY_OPENABLE);

        chooseFileIntent = Intent.createChooser(chooseFileIntent, "Choose a file");

        startActivityForResult(chooseFileIntent, FILECHOOSER_CODE);
    }

    // When you have the request results
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case BROWSE_PERMISSION_CODE:{
                // Note: If request is cancelled, the result arrays are empty.
                // Permissions granted (CALL_PHONE).
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG,"Permission granted!");
                    Toast.makeText(getApplicationContext(), "Permission granted!", Toast.LENGTH_SHORT).show();

                    doBrowseFile();
                }else {
                    Log.i(TAG,"Permission denied!");
                    Toast.makeText(getApplicationContext(), "Permission denied!", Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case STORAGE_PERMISSION_CODE:{

            }

            default: {
                break;
            }
        }
    }
private File file;
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case FILECHOOSER_CODE:
                if (resultCode == Activity.RESULT_OK ) {
                    if(data != null){
                        Uri fileUri = data.getData();
                        Log.i(TAG, "Uri: " + fileUri);
                        String filePath = null;
                        try {
                            filePath = FileUtils.getPath(getApplicationContext(),fileUri);
                        } catch (Exception e) {
                            Log.e(TAG,"Error: " + e);
                            Toast.makeText(getApplicationContext(), "Error: " + e, Toast.LENGTH_SHORT).show();
                        }
                        editTextPath.setText(filePath);
                        try {

                            MetaData metaData = dumpImageMetaData(fileUri);
                            String str = readTextFromUri(fileUri);
                            Log.i(TAG, "onActivityResult: "+str);
                            byte[] bytes = str.getBytes();

                            String pathOnlyStr = filePath.replace(metaData.getDisplayName(),"");

                            //-----------------------------------------tim test
//                            byte[] bytes1k = new byte[65536];
//                            Arrays.fill( bytes, (byte) 1 );
//                            writeBinaryFile(bytes1k, pathOnlyStr+"64k.bin");
                            //---------------------------------------------------------

                            String displaybinname = metaData.getDisplayName().replace("hex","bin");

                            String fileIn = filePath;//"Application.hex";
                            String fileOut = pathOnlyStr+displaybinname;
                            String dataFrom = "0X0000";//default = "min"
                            String dataTo = "0X10000";//default = "max"
                            boolean minimize = false;

//                            securityManager.c(fileOut);
//                            securityManager.checkPermission();
                            try (FileInputStream is = new FileInputStream(fileIn)) {


                                file = new File(pathOnlyStr,displaybinname);
                                if (DocumentsUtils.checkWritableRootPath(getApplicationContext(),pathOnlyStr)) {   //检查sd卡路径是否有 权限 没有显示dialog
                                    Toast.makeText(getApplicationContext(), "please pick folder get permission", Toast.LENGTH_LONG).show();
                                    Log.e(TAG, "onActivityResult: fail" );
                                    return;
                                }else{
                                    Log.i(TAG, "onActivityResult: good0");
                                }

                                OutputStream os = DocumentsUtils.getOutputStream(getApplicationContext(),file);


                                // init parser
                                Parser parser = new Parser(is);

                                // 1st iteration - calculate maximum output range
                                RangeDetector rangeDetector = new RangeDetector();
                                parser.setDataListener(rangeDetector);
                                parser.parse();
                                is.getChannel().position(0);
                                Region outputRegion = rangeDetector.getFullRangeRegion();

                                // if address parameter is "max", calculate maximum memory region
                                if (!("min".equals(dataFrom))) {
                                    outputRegion.setAddressStart(Long.parseLong(dataFrom.substring(2), 16));
                                }
                                if (!("max".equals(dataTo))) {
                                    outputRegion.setAddressEnd(Long.parseLong(dataTo.substring(2), 16));
                                }

                                // 2nd iteration - actual write of the output
                                BinWriter writer = new BinWriter(outputRegion, os, minimize);
                                parser.setDataListener(writer);
                                parser.parse();

                                // print statistics
                                System.out.printf("Program start address 0x%08X\r\n", parser.getStartAddress());
                                System.out.println("Memory regions: ");
                                System.out.println(rangeDetector.getMemoryRegions());

                                System.out.print("Written output: ");
                                System.out.println(outputRegion);

                            } catch (IntelHexException | IOException ex) {
                                Logger.getLogger(TAG).log(Level.SEVERE, null, ex);
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }


                        //-------------------------------
                    }
                }
                break;


            case DocumentsUtils.OPEN_DOCUMENT_TREE_CODE:
                if (data != null && data.getData() != null) {
                    Uri uri = data.getData();
                    DocumentsUtils.saveTreeUri(getApplicationContext(), rootPath, uri);
                    Log.i(TAG,"OPEN_DOCUMENT_TREE_CODE lum_uri ： "  + uri);

                }else{
                    if(data == null) {
                        Log.i(TAG, "OPEN_DOCUMENT_TREE_CODE onActivityResult: data null");
                    }else {
                        if (data.getData() == null)
                            Log.i(TAG, "OPEN_DOCUMENT_TREE_CODE onActivityResult: data.getData() null");
                    }
                }
                break;
            default:
                break;
        }
    }

    /**
     * 在外置TF卡根目录下创建目录
     */
    public void writeBinaryFile(byte[] bytes, String uristr) {
//        File file = new File(Environment.getExternalStorageDirectory() + "/" + File.separator + "test.txt");
        File file = new File(uristr);


        OutputStream os = DocumentsUtils.getOutputStream(getApplicationContext(),file);
        try {
            os.write(bytes);
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }



    }


//    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
//        Cursor cursor = null;
////        String[] projection = { MediaStore.Images.Media.DATA };
//        String[] projection = {  };
//        try {
//            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
//            if (cursor != null && cursor.moveToFirst()) {
//                int index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
//                return cursor.getString(index);
//            }
//        } finally {
//            if (cursor != null)
//                cursor.close();
//        }
//        return null;
//    }


    private String readTextFromUri(Uri uri) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        try (InputStream inputStream =
                     getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(Objects.requireNonNull(inputStream)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
        }
        return stringBuilder.toString();
    }


    public MetaData dumpImageMetaData(Uri uri) {
        String token = uri.getLastPathSegment();//getPath();
        Log.i(TAG, "getLastPathSegment path: " + token);
        MetaData metaData = new MetaData();

        String token2 = uri.getPath();//getPath();
        Log.i(TAG, "getPath path: " + token2);

        String token3 = uri.getEncodedPath();
        Log.i(TAG, "getEncodedPath path: " + token3);

        // The query, because it only applies to a single document, returns only
        // one row. There's no need to filter, sort, or select fields,
        // because we want all fields for one document.
        Cursor cursor = getApplicationContext().getContentResolver()
                .query(uri, null, null, null, null, null);

        try {
            // moveToFirst() returns false if the cursor has 0 rows. Very handy for
            // "if there's anything to look at, look at it" conditionals.
            if (cursor != null && cursor.moveToFirst()) {

                // Note it's called "Display Name". This is
                // provider-specific, and might not necessarily be the file name.
                String displayName = cursor.getString(
                        cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                Log.i(TAG, "Display Name: " + displayName);
//                token-displayName;

                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                // If the size is unknown, the value stored is null. But because an
                // int can't be null, the behavior is implementation-specific,
                // and unpredictable. So as
                // a rule, check if it's null before assigning to an int. This will
                // happen often: The storage API allows for remote files, whose
                // size might not be locally known.
                metaData.setDisplayName(displayName);
                String size = null;
                if (!cursor.isNull(sizeIndex)) {
                    // Technically the column stores an int, but cursor.getString()
                    // will do the conversion automatically.
                    size = cursor.getString(sizeIndex);
                    metaData.setSize(size);
                } else {
                    size = "Unknown";
                }
                Log.i(TAG, "Size: " + size);
            }
        } finally {
            cursor.close();
            return metaData;
        }
    }

    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }


    private void showOpenDocumentTree(String path) {
        Intent intent = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && Build.VERSION.SDK_INT <=Build.VERSION_CODES.P) {//sdk > 26    sdk < 28  剛好中間才走
            StorageManager sm = getSystemService(StorageManager.class);

            StorageVolume volume = sm.getStorageVolume(new File(path));
            Log.i(TAG, "showOpenDocumentTree: good0");
            if (volume != null) {
                Log.i(TAG, "showOpenDocumentTree: good0.5");
                intent = volume.createAccessIntent(null);
//                volume.createOpenDocumentTreeIntent();
            }
        }

        if (intent == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP || Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {// sdk > 21
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ) {
                intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);

                Log.d(TAG, "showOpenDocumentTree: sdk > 21");
            }else{
                Log.d("TAG","android<=4.4不需要权限，TF卡操作自由");
            }
        }
        Log.i(TAG, "showOpenDocumentTree: good1");
        startActivityForResult(intent, DocumentsUtils.OPEN_DOCUMENT_TREE_CODE);
        Log.i(TAG, "showOpenDocumentTree: good2");
    }



    /**
     * 通过反射调用获取内置存储和外置sd卡根路径(通用)
     *
     * @param mContext    上下文
     * @param is_removale 是否可移除，false返回内部存储路径，true返回外置SD卡路径
     * @return
     */
    private static String getStoragePath(Context mContext, boolean is_removale) {
        String path = "";
        //使用getSystemService(String)检索一个StorageManager用于访问系统存储功能。
        StorageManager mStorageManager = (StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);
        Class<?> storageVolumeClazz = null;
        try {
            storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
            Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
            Method getPath = storageVolumeClazz.getMethod("getPath");
            Method isRemovable = storageVolumeClazz.getMethod("isRemovable");
            Object result = getVolumeList.invoke(mStorageManager);

            for (int i = 0; i < Array.getLength(result); i++) {
                Object storageVolumeElement = Array.get(result, i);
                path = (String) getPath.invoke(storageVolumeElement);
                boolean removable = (Boolean) isRemovable.invoke(storageVolumeElement);
                if (is_removale == removable) {
                    return path;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return path;
    }
    public  void checkSDcardWritePermission(){
                rootPath = getStoragePath(getApplicationContext(),true);  //获取 可以插拔的 sd 卡  路径
        Log.i(TAG,"lum rootPath： " + rootPath);
        if (DocumentsUtils.checkWritableRootPath(getApplicationContext(), rootPath)) {   //检查sd卡路径是否有 权限 没有显示dialog
            Log.i(TAG,"lum rootPath： Permission no"  );
            Toast.makeText(getApplicationContext(), "please pick folder get permission", Toast.LENGTH_LONG).show();
            showOpenDocumentTree(rootPath);
        }else{
            Log.i(TAG,"lum rootPath： Permission yes"  );
        }
    }

    //权限判断和申请
    public void checkPermission() {
        unPermissionList.clear();//清空申请的没有通过的权限
        //逐个判断是否还有未通过的权限
        for (int i = 0; i < permissionList.length; i++) {
            if (ContextCompat.checkSelfPermission(this, permissionList[i]) !=
                    PackageManager.PERMISSION_GRANTED) {
                unPermissionList.add(permissionList[i]);//添加还未授予的权限到unPermissionList中
            }
        }

        //有权限没有通过，需要申请
        if (unPermissionList.size() > 0) {
            ActivityCompat.requestPermissions( this,permissionList, STORAGE_PERMISSION_CODE);
            Log.i(TAG, "check 有权限未通过");
        } else {
            //权限已经都通过了，可以将程序继续打开了
            Log.i(TAG, "check 权限都已经申请通过");
            checkSDcardWritePermission();


        }
    }


    public  void writeDataToSD() throws IOException {
        //写文件的例子 文件不存在,会创建
        String  str = "just a test\n";
        String strRead = "";

        String  sdkOut = getStoragePath(getApplicationContext(),true);  //获取 可以插拔的 sd 卡  路径

        String  filePath = sdkOut + "/Download";
        Log.i(TAG,"lum_ sdkOut: " + filePath);
        File file = new File(filePath);
        if (!file.exists()){
            file.mkdirs();
            Log.i(TAG,"创建文件夹：" + filePath);
        }

        String  fileWritePath = filePath + "/text.bin";
        File fileWrite = new File(fileWritePath);


        Log.i(TAG,"lum  准备写入" );
        try {
            OutputStream outputStream = DocumentsUtils.getOutputStream(getApplicationContext(),fileWrite);  //获取输出流
//              OutputStream outputStream = new FileOutputStream(fileWrite);
            outputStream.write(str.getBytes());
//            outputStream.write(63);
            outputStream.close();
            Log.i(TAG,"lum  写入成功" );
            Toast.makeText(this,"路径：" + fileWritePath + "成功",Toast.LENGTH_SHORT ).show();
        } catch (IOException e) {
            e.printStackTrace();
            Log.i(TAG,"lum  写入失败" );
            Toast.makeText(this,"路径：" + fileWritePath + "失败",Toast.LENGTH_SHORT ).show();
        }

        /*

        //读取文件 的例子
        try {
            InputStream is = DocumentsUtils.getInputStream(this,fileWritePath);
            InputStreamReader input = new InputStreamReader(is, "UTF-8");
            BufferedReader reader = new BufferedReader(input);
            while ((str = reader.readLine()) != null) {
                strRead  +=  str;
            }
            Log.i(TAG,"lum:读取的文件是 " +  strRead);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
*/


    }

}