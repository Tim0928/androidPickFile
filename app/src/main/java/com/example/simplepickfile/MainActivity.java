package com.example.simplepickfile;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainActivity extends AppCompatActivity {
    private EditText editTextPath;
    private static final int MY_REQUEST_CODE_PERMISSION = 1000;
    private static final int MY_REQUEST_CODE_WRITE_PERMISSION = 1001;
    private static final int MY_RESULT_CODE_FILECHOOSER = 2000;
    private String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editTextPath = (EditText) findViewById(R.id.editText_path);
        askWritePermission();
    }

    public void pickonClick(View view){
        askPermissionAndBrowseFile();
    }




    private void askWritePermission()  {
        // With Android Level >= 23, you have to ask the user
        // for permission to access External Storage.
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) { // Level 23


            int permisson = ActivityCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (permisson != PackageManager.PERMISSION_GRANTED) {
                // If don't have permission so prompt the user.
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_REQUEST_CODE_WRITE_PERMISSION);
                return;
            }
        }
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
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_REQUEST_CODE_PERMISSION);
                return;
            }
        }
        doBrowseFile();
    }

    //open folder
    private void doBrowseFile(){
        Intent chooseFileIntent = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFileIntent.setType("*/*");
        // Only return URIs that can be opened with ContentResolver
        chooseFileIntent.addCategory(Intent.CATEGORY_OPENABLE);

        chooseFileIntent = Intent.createChooser(chooseFileIntent, "Choose a file");
        startActivityForResult(chooseFileIntent, MY_RESULT_CODE_FILECHOOSER);
    }

    // When you have the request results
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_REQUEST_CODE_PERMISSION:{
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

            case MY_REQUEST_CODE_WRITE_PERMISSION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG,"Permission granted!");
                    Toast.makeText(getApplicationContext(), "Permission granted!", Toast.LENGTH_SHORT).show();
                }else {
                    Log.i(TAG,"Permission denied!");
                    Toast.makeText(getApplicationContext(), "Permission denied!", Toast.LENGTH_SHORT).show();
                }
                break;
            }
            default: {
                break;
            }
        }
    }
private File file;
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case MY_RESULT_CODE_FILECHOOSER:
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
//                        filePath
//                        String myUrl = "http://stackoverflow.com";
                        try {

//                            File file = new File(fileUri.getPath());//create path from uri
                            MetaData metaData = dumpImageMetaData(fileUri);
                            String str = readTextFromUri(fileUri);
                            Log.i(TAG, "onActivityResult: "+str);

                            byte[] bytes = str.getBytes();
//                            writeBinaryFile(bytes, Environment.getExternalStorageDirectory().getPath()+File.separator+"good.bin");
                            String pathOnlyStr = filePath.replace(metaData.getDisplayName(),"");
//                            writeBinaryFile(bytes, pathOnlyStr+"good.bin");
//-------------------------------------------------------------------





//                            // create input stream of some IntelHex data
//                            InputStream is = new FileInputStream("Application.hex");
//
//                            // create IntelHexParserObject
//                            IntelHexParser ihp = new IntelHexParser(is);
//
//                            // register parser listener
//                            ihp.setDataListener(new IntelHexDataListener() {
//                                @Override
//                                public void data(long address, byte[] data) {
//                                    // process data
//                                }
//
//                                @Override
//                                public void eof() {
//                                    // do some action
//                                }
//                            });
//                            ihp.parse();
//------------------------------------------------------------
                            String fileIn = filePath;//"Application.hex";
                            String fileOut = pathOnlyStr+"Application.bin";
                            String dataFrom = "min";
                            String dataTo = "max";
                            boolean minimize = false;

//                            if (args.length == 0) {
//                                System.out.println("usage:");
//                                System.out.println("    hex2bin <hex> <bin> <start address> <end address> [minimize]");
//                                System.out.println();
//                                System.out.println("    full address range of app.hex");
//                                System.out.println("        hex2bin app.hex app.bin");
//                                System.out.println();
//                                System.out.println("    limited exact address range of app.hex, undefined data are 0xff");
//                                System.out.println("        hex2bin app.hex app.bin 0x0000 0x1fff");
//                                System.out.println();
//                                System.out.println("    limited minimal address range of app.hex, start at 0x0000,");
//                                System.out.println("    max address is 0x1fff, but can be lower");
//                                System.out.println("        hex2bin app.hex app.bin 0x0000 0x1fff minimize");
//                                return;
//                            }
//
//                            if (args.length >= 1) {
//                                fileIn = args[0];
//                            }
//
//                            if (args.length >= 2) {
//                                fileOut = args[1];
//                            }
//
//                            if (args.length >= 3) {
//                                dataFrom = args[2];
//                            }
//
//                            if (args.length >= 4) {
//                                dataTo = args[3];
//                            }
//
//                            if (args.length >= 5) {
//                                if (args[4].equals("minimize")) {
//                                    minimize = true;
//                                }
//                            }

                            try (FileInputStream is = new FileInputStream(fileIn)) {
                                OutputStream os = new FileOutputStream(fileOut);
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


                            //---------------------








                            //------------------------
                        } catch (Exception e) {
                            e.printStackTrace();
                        }


                        //-------------------------------
                    }
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    void writeBinaryFile(byte[] aBytes, String aFileName) throws IOException {
        Path path = Paths.get(aFileName);
        Files.write(path, aBytes); //creates, overwrites


//        InputStream fis = getContentResolver().openInputStream(Uri.parse(aFileName));
//        fis.read(aBytes);
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
//
//
//    public static String getMimeType(String url) {
//        String type = null;
//        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
//        if (extension != null) {
//            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
//        }
//        return type;
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
//        String replacestr = null;
        String token = uri.getLastPathSegment();//getPath();
        Log.i(TAG, "path: " + token);
        MetaData metaData = new MetaData();
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

}