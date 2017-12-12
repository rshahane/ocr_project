package com.paragyte.ocrproject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.paragyte.ocrproject.classes.DateWithFormat;
import com.paragyte.ocrproject.classes.ListWithFlags;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.regex.Pattern;
import butterknife.BindView;
import butterknife.ButterKnife;
import static com.paragyte.ocrproject.Constants.AMOUNT;
import static com.paragyte.ocrproject.Constants.DATE;
import static com.paragyte.ocrproject.Constants.FILE_URI;
import static com.paragyte.ocrproject.Constants.HOTEL_NAME;

public class ResultActivity extends AppCompatActivity {
    @BindView(R.id.activity_result_image_view)
    ImageView mImgResult;
//    @BindView(R.id.activity_result_image_view2)
//    ImageView getmImgResult2;
//    @BindView(R.id.activity_result_image_view3)
//    ImageView getmImgResult3;
//    @BindView(R.id.activity_result_image_view4)
//    ImageView getmImgResult4;

    @BindView(R.id.txt_decoded_text)
    TextView mTxtResult;
    @BindView(R.id.amount)
    TextView mTxtAmount;
    @BindView(R.id.date)
    TextView mTxtDate;
    private Uri fileUri;
    private TextRecognizer detector;

    String [] totalAmountPossibleTexts = {
            "TOTAL",
            "GROSS AMOUNT",
            "NET AMOUNT",
            "CASH",
            "DUE",
            "TO PAY",
            "AMOUNT INCL OF ALL TAXES",
            "GRAND TOTAL"
    };

    String [] dateFormatArray = {
            "dd/MM/YY",
            "dd/MM/YYYY",
            "dd-MM-YY",
            "dd-MM-YYYY",
            "dd-MMM-YYYY",
            "dd-MMM-YY",
            "ddMMM'yy",
            "MMMdd'YY"
    };

    String [] excludeFromDate = {
            "DATE:-","DT:-","DT:","DATE:","DATE",":-",":"
    };


    String [] datePossibleText = { "DATE", "DATE:"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        ButterKnife.bind(this);
        fileUri = getIntent().getParcelableExtra(FILE_URI);
        detector = new TextRecognizer.Builder(getApplicationContext()).build();
        //previewCapturedImage();
        scanSecondHalfForAmount();
        scanTopHalfForDate();
//        Bitmap bmp =  previewCapturedImage();
//        if(bmp != null){
//            Log.d(Constants.TAG, "onCreate: BITMAP NOT NULL");
//            createSections(bmp);
//        }else {
//            Toast.makeText(this, "IMAGE IS NULL", Toast.LENGTH_SHORT).show();
//        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        File file = new File(fileUri.getPath());
        file.delete();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(FILE_URI, fileUri);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        fileUri = savedInstanceState.getParcelable(FILE_URI);
    }

    private Bitmap previewCapturedImage() {
        try {
            // bimatp factory
            BitmapFactory.Options options = new BitmapFactory.Options();
            // downsizing image as it throws OutOfMemory Exception for larger
            // images
            options.inSampleSize = 2;
            Bitmap bitmap = BitmapFactory.decodeFile(fileUri.getPath(), options);
            mImgResult.setImageBitmap(bitmap);
            //detectText(bitmap, Constants.AMOUNT);
            return bitmap;
        } catch (NullPointerException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void scanSecondHalfForAmount(){
        try{
            Bitmap bitmap = BitmapFactory.decodeFile(fileUri.getPath());
            Log.d(Constants.TAG, "scanSecondHalfForAmount: bitmap.getWidth() "+bitmap.getWidth());
            Log.d(Constants.TAG, "scanSecondHalfForAmount: bitmap.getHeight() "+bitmap.getHeight());
            if(bitmap.getWidth() > bitmap.getHeight()){
                bitmap = rotateBitmap(bitmap, 90);
                Log.d(Constants.TAG, "scanSecondHalfForAmount: rotatedbitmap.getWidth() "+bitmap.getWidth());
                Log.d(Constants.TAG, "scanSecondHalfForAmount: rotatedbitmap.getHeight() "+bitmap.getHeight());
            }
            mImgResult.setImageBitmap(bitmap);
            //Rect rect = new Rect(bitmap.getWidth()/2, bitmap.getHeight()/2, bitmap.getWidth(), bitmap.getHeight());
            Log.d(Constants.TAG, "Image height: "+bitmap.getHeight());
            Rect rect = new Rect(0, bitmap.getHeight()/2, bitmap.getWidth(), bitmap.getHeight());
            BitmapRegionDecoder decoder= BitmapRegionDecoder.newInstance(fileUri.getPath(), true);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 4;
            Bitmap croppedBitmap= decoder.decodeRegion(rect, null);
            detectText(croppedBitmap, Constants.AMOUNT);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private Bitmap rotateBitmap(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    private void scanTopHalfForDate(){
        try{
            final Bitmap bitmap = BitmapFactory.decodeFile(fileUri.getPath());
            mImgResult.setImageBitmap(bitmap);
            //Rect rect = new Rect(bitmap.getWidth()/2, bitmap.getHeight()/2, bitmap.getWidth(), bitmap.getHeight());
            Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()/2);
            BitmapRegionDecoder decoder= BitmapRegionDecoder.newInstance(fileUri.getPath(), true);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 4;
            Bitmap croppedBitmap= decoder.decodeRegion(rect, null);
            detectText(croppedBitmap, Constants.DATE);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

//    private void detectText(Bitmap bitmap) {
//        try {
//            launchMediaScanIntent();
//            if (detector.isOperational() && bitmap != null) {
//                Frame frame = new Frame.Builder().setBitmap(bitmap).build();
//                SparseArray<TextBlock> textBlocks = detector.detect(frame);
//                ArrayList<String> linesList = new ArrayList<>();
//                ArrayList<String> wordsList= new ArrayList<>();
//                String blocks = "";
//                String lines = "";
//                String words = "";
//                for (int index = 0; index < textBlocks.size(); index++) {
//                    //extract scanned text blocks here
//                    TextBlock tBlock = textBlocks.valueAt(index);
//                    blocks = blocks + tBlock.getValue() + "\n" + "\n";
//                    for (Text line : tBlock.getComponents()) {
//                        //extract scanned text lines here
//                        linesList.add(line.getValue());
//                        lines = lines + line.getValue() + "\n";
//                        for (Text element : line.getComponents()) {
//                            //extract scanned text words here
//                            words = words + element.getValue() + ", ";
//                        }
//                    }
//                }
//                if (textBlocks.size() == 0) {
//                    mTxtResult.setText("Scan Failed: Found nothing to scan");
//                } else {
//                    mTxtResult.setText(mTxtResult.getText() + "Blocks: " + "\n");
//                    mTxtResult.setText(mTxtResult.getText() + blocks + "\n");
//                    mTxtResult.setText(mTxtResult.getText() + "---------" + "\n");
//                    mTxtResult.setText(mTxtResult.getText() + "Lines: " + "\n");
//                    mTxtResult.setText(mTxtResult.getText() + lines + "\n");
//                    mTxtResult.setText(mTxtResult.getText() + "---------" + "\n");
//                    mTxtResult.setText(mTxtResult.getText() + "Words: " + "\n");
//                    mTxtResult.setText(mTxtResult.getText() + words + "\n");
//                    mTxtResult.setText(mTxtResult.getText() + "---------" + "\n");
//                }
//                String amt = getAmount(linesList);
//                mTxtAmount.setText(amt);
//            } else {
//                Log.d(Constants.TAG, "Could not set up the detector! " + detector.isOperational());
//                Log.d(Constants.TAG, "Could not set up the detector! " + (bitmap != null));
//                mTxtResult.setText("Could not set up the detector! ");
//
//            }
//        } catch (Exception e) {
//            Toast.makeText(this, "Failed to load Image", Toast.LENGTH_SHORT)
//                    .show();
//            Log.e(Constants.TAG, e.toString());
//        }
//    }


    private void detectText(Bitmap bitmap, String type) {
        try {
            launchMediaScanIntent();
            if (detector.isOperational() && bitmap != null) {
                Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                SparseArray<TextBlock> textBlocks = detector.detect(frame);
                ArrayList<String> linesList = new ArrayList<>();
                ArrayList<String> wordlist = new ArrayList<>();
                String result = "";
                for(int index = 0; index < textBlocks.size(); index++){
                    Text tBlock = textBlocks.valueAt(index);
                    result = result +"\n\n"+"########"+"\n\n";
                    for(Text line : tBlock.getComponents()){
                        result = result + line.getValue() + "\n";
                        linesList.add(line.getValue());
                        for(Text word : line.getComponents()){
                            wordlist.add(word.getValue());
                        }
                    }
                }
                Log.d(Constants.TAG, "RESULTS "+result);
                mTxtResult.setText(result);
                if(type.equals(AMOUNT)){
                    String amt = getAmount2(wordlist);
                    mTxtAmount.setText("AMOUNT : "+ amt);
                }else if(type.equals(DATE)){
                    String date = getDate(wordlist);
                    mTxtDate.setText("DATE : "+date);
                }else if(type.equals(HOTEL_NAME)){

                }
                //mAcTextView.setText(amt);
            } else {
                Log.d(Constants.TAG, "Could not set up the detector! " + detector.isOperational());
                Log.d(Constants.TAG, "Could not set up the detector! " + (bitmap != null));
                mTxtResult.setText("Could not set up the detector! ");
            }
        } catch (Exception e) {
            Toast.makeText(this, "Failed to load Image", Toast.LENGTH_SHORT)
                    .show();
            Log.e(Constants.TAG, e.toString());
        }
    }



    private ArrayList<String> getWordsList(Bitmap bitmap){
        ArrayList<String> wordlist = new ArrayList<>();
        try {
            launchMediaScanIntent();
            if (detector.isOperational() && bitmap != null) {
                Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                SparseArray<TextBlock> textBlocks = detector.detect(frame);
                String result = "";
                for(int index = 0; index < textBlocks.size(); index++){
                    Text tBlock = textBlocks.valueAt(index);
                    result = result +"\n\n"+"########"+"\n\n";
                    for(Text line : tBlock.getComponents()){
                        result = result + line.getValue() + "\n";
                        for(Text word : line.getComponents()){
                            wordlist.add(word.getValue());
                        }
                    }
                }
            } else {
                Log.d(Constants.TAG, "Could not set up the detector! " + detector.isOperational());
                Log.d(Constants.TAG, "Could not set up the detector! " + (bitmap != null));
                mTxtResult.setText("Could not set up the detector! ");
            }
        } catch (Exception e) {
            Toast.makeText(this, "Failed to load Image", Toast.LENGTH_SHORT)
                    .show();
            Log.e(Constants.TAG, e.toString());
        }
        return wordlist;
    }

    private void launchMediaScanIntent() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(fileUri);
        this.sendBroadcast(mediaScanIntent);
    }

    private String getAmount(ArrayList<String> lines) {
        for (String line : lines) {
            if (line.toUpperCase().contains("GRAND TOTAL")
                    || line.toUpperCase().contains("TOTAL")
                    || line.toUpperCase().contains("TOTAL AMOUNT")
                    || line.toUpperCase().contains("TOTAL AMOUNT TO PAY")
                    || line.toUpperCase().contains("TOTAL AMOUNT")
                    || line.toUpperCase().contains("GROSS AMOUNT")
                    || line.toUpperCase().contains("NET AMOUNT")
                    || line.toUpperCase().contains("CASH")
                    || line.toUpperCase().contains("BALANCE DUE")
                    || line.toUpperCase().contains("DUE")
                    || line.toUpperCase().contains("TOTAL TO PAY")
                    || line.toUpperCase().contains("AMOUNT INCL OF ALL TAXES")
                    || line.toUpperCase().contains("GRAND TOTAL")){
                String [] words = line.split(" ");
                for(String word : words){
                    // regex for amount
                    if(Pattern.matches("[\\$0-9\\.\\,]+", word)){
                        return word;
                    }
                }
            }else{
                return "";
            }
        }
        return "";
    }

    private boolean hasDiscount(ArrayList<String> words){
        for(String word : words){
            if(word.toUpperCase().contains("DISCOUNT")){
                return true;
            }
        }
        return false;
    }

    private String getAmount2(ArrayList<String> words){
        ArrayList<String> possibleResults = new ArrayList<>();
        HashMap<String, Integer> resultsMap = new HashMap<>();
        for(int i = 0; i< words.size(); i++){
            String word = words.get(i);
            // regex for amount [\$0-9\.\,]+
            // regex 2 : [0-9\,]+\.?[0-9]+
            //  \$?[0-9\,]+\.?[0-9]+
            if(Pattern.matches("\\$?[0-9\\,]+\\.?[0-9]+", word)){
                if(word.contains("$")){
                    Log.d(Constants.TAG, "contains $");
                    word = word.replace("$","");
                    Log.d(Constants.TAG, "word "+word);
                }
                possibleResults.add(word);
                resultsMap.put(word, i);
            }
        }
        printPossibleResults(possibleResults);
//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_singlechoice, possibleResults);
//        mAcTextView.setThreshold(2);
//        mAcTextView.setAdapter(adapter);
        if(possibleResults.isEmpty()){
            return "";
        }else if(possibleResults.size() == 1){
            return possibleResults.get(0);
        }else{
            return Float.toString(getBiggestNumber(possibleResults, hasDiscount(words)));
        }
    }

//    private String getDate(ArrayList<String> words){
//        ArrayList<DateWithFormat> possibleDates = new ArrayList<>();
//        for(String frmt : dateFormatArray){
//            SimpleDateFormat format = new SimpleDateFormat(frmt);
//            for(String word : words){
//                for(String temp : excludeFromDate){
//                    word = word.replace(temp,"");
//                }
//                try {
//                    format.parse(word);
//                    possibleDates.add(new DateWithFormat(word, frmt));
//                }catch (Exception e){
//                    continue;
//                }
//            }
//        }
//        Log.d(Constants.TAG, "possibleDates.size() "+possibleDates.size());
//        if(possibleDates.size() == 1){
//            return possibleDates.get(0).getDate();
//        }else{
//            for(DateWithFormat dwf : possibleDates){
//                if(isValidDate(dwf)){
//                    return dwf.getDate();
//                }
//            }
//
//        }
//        return "NO_DATE";
//    }

    private String getDate(ArrayList<String> words){
        for(String frmt : dateFormatArray){
            SimpleDateFormat format = new SimpleDateFormat(frmt);
            for(String word : words){
                for(String temp : excludeFromDate){
                    word = word.replace(temp,"");
                }
                try {
                    format.parse(word);
                    return word;
                }catch (Exception e){
                    continue;
                }
            }
        }
        return "NO_DATE";
    }


    public boolean isValidDate(DateWithFormat dwf){
        try{
            Log.d(Constants.TAG, "DATE "+ dwf.getDate()+" FORMAT "+dwf.getDateFormat());
            SimpleDateFormat format = new SimpleDateFormat(dwf.getDateFormat());
            Date date = format.parse(dwf.getDate());
            Date current = new Date();
            if(date.getYear() > current.getYear()
                    || date.getMonth() > 12
                    || (date.getYear() - current.getYear()) > 2){
                Log.d(Constants.TAG, "INVALID DATE "+ dwf.getDate());
                return false;
            }else{
                return true;
            }
        }catch (Exception e){
            return true;
        }
    }

    private void printPossibleResults(ArrayList<String> possibleResults){
        for(String s : possibleResults){
            Log.d(Constants.TAG, "printPossibleResults: "+s);
        }
    }

    private float getBiggestNumber(ArrayList<String> possibleResults, boolean hasDiscount){
        float max = Float.parseFloat(possibleResults.get(0).replace(",",""));
        float secondMax = max;
        for(String res : possibleResults){
            float f = Float.parseFloat(res.replace(",",""));
            if(f > max){
                secondMax = max;
                max = f;
            }else if(f > secondMax && f < max){
                secondMax = f;
            }
        }
        return hasDiscount? secondMax : max;
    }

    //possible conditions
    // discount which may be 0
    // Remove Numbers which are not related to amount
    // Round figure
    // Bold characters for bills


    private void createSections(Bitmap wholeImage){
        Bitmap section1;
        Bitmap section2;
        Bitmap section3;
        Bitmap section4;
        Rect rect1 = new Rect(0, 0, wholeImage.getWidth(), wholeImage.getHeight()/4);
        Rect rect2 = new Rect(0, wholeImage.getHeight()/4, wholeImage.getWidth(), wholeImage.getHeight()/2);
        Rect rect3 = new Rect(0, wholeImage.getHeight()/2, wholeImage.getWidth(), wholeImage.getHeight()*3/4);
        Rect rect4 = new Rect(0, wholeImage.getHeight()*3/4, wholeImage.getWidth(), wholeImage.getHeight());
        try{
            BitmapRegionDecoder decoder= BitmapRegionDecoder.newInstance(fileUri.getPath(), true);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 4;

            section1 = decoder.decodeRegion(rect1, options);
            section2 = decoder.decodeRegion(rect2, options);
            section3 = decoder.decodeRegion(rect3, options);
            section4 = decoder.decodeRegion(rect4, options);

            mImgResult.setImageBitmap(section1);
            mImgResult.setImageBitmap(section2);
            mImgResult.setImageBitmap(section3);
            mImgResult.setImageBitmap(section4);

            ArrayList<String> list1 = getWordsList(section1);
            ArrayList<String> list2 = getWordsList(section2);
            ArrayList<String> list3 = getWordsList(section3);
            ArrayList<String> list4 = getWordsList(section4);

            ListWithFlags l1f = new ListWithFlags();
            ListWithFlags l2f = new ListWithFlags();
            ListWithFlags l3f = new ListWithFlags();
            ListWithFlags l4f = new ListWithFlags();

            l1f.setList(list1);
            l2f.setList(list2);
            l3f.setList(list3);
            l4f.setList(list4);

            TreeMap<Integer, ArrayList<String>> listMap = new TreeMap<>();
            listMap.put(1, list1);
            listMap.put(2, list2);
            listMap.put(3, list3);
            listMap.put(4, list4);

            displaySections(listMap);


            TreeMap<Integer, ArrayList<String>> totalTextListMap = new TreeMap<>();
            for(int i : listMap.keySet()){
                if(containsTotalText(listMap.get(i))){
                    totalTextListMap.put(i, listMap.get(i));
                }
            }

            TreeMap<Integer, ArrayList<String>> dateTextHashMap = new TreeMap<>();
            for(int i : listMap.keySet()){
                if(containsDateText(listMap.get(i))){
                    dateTextHashMap.put(i, listMap.get(i));
                }
            }

            if(totalTextListMap.size() != 0){
                getAmount2(totalTextListMap.get(totalTextListMap.size()-1));
            }else{
                Toast.makeText(this, "NO TOTAL TEXT FOUND", Toast.LENGTH_SHORT).show();
            }

        }catch (Exception e){

        }
    }

    private boolean containsTotalText(ArrayList<String> words){
        for(String word : words){
            for(String possibleText : totalAmountPossibleTexts){
                if(possibleText.toUpperCase().equals(word)){
                    return true;
                }
            }
        }
        return false;
    }

    private boolean containsDateText(ArrayList<String> words){
        for(String word : words){
            for(String possibleText : datePossibleText){
                if(possibleText.toUpperCase().equals(word)){
                    return true;
                }
            }
        }
        return false;
    }

    public void displaySections(TreeMap<Integer, ArrayList<String>> tm){
        String result = "";
        for(Integer i : tm.keySet()){
            for(String s : tm.get(i)){
                result = result + s + "\n";
            }
            result = result + "##########"+"\n";
        }

        mTxtResult.setText(result);
    }





}
