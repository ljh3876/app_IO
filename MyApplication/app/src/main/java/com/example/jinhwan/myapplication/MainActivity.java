package com.example.jinhwan.myapplication;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    EditText memo;
    TextView viewCount;
    ListView list;
    Button btnSave;
    ArrayList<String> listName = new ArrayList<>();
    ArrayAdapter<String> adapter;
    DatePicker datePicker;
    LinearLayout linear1, linear2;
    int count=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        showList();
        listListener();
    }

    void init() {
        memo = (EditText) findViewById(R.id.memo);
        list = (ListView) findViewById(R.id.listview);
        datePicker = (DatePicker) findViewById(R.id.date);
        viewCount = (TextView) findViewById(R.id.tvCount);
        linear1 = (LinearLayout) findViewById(R.id.linear1);
        linear2 = (LinearLayout) findViewById(R.id.linear2);
        btnSave = (Button) findViewById(R.id.btnsave);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listName);
        list.setAdapter(adapter);

        String path = getExternalPath();
        File file = new File(path + "diary");
        file.mkdir();
    }

    void showList() {
        String path = getExternalPath();
        count =0;
        //int count = 0;
        listName.clear();

        File[] files = new File(path + "diary").listFiles();
        for (int i = 0; i < files.length ; i++) {
            listName.add(files[i].getName().substring(0, 13));
            count++;
        }
        Collections.sort(listName, compare);
        adapter.notifyDataSetChanged();

        viewCount.setText("등록된 메모 개수 : " + String.valueOf(count));
    }
    Comparator<String> compare = new Comparator<String>() {
        @Override
        public int compare(String o1, String o2) {
            return o1.compareTo(o2);
        }
    };

    String temp;
    public void onClick(View v) {
        if (v.getId() == R.id.btn1) {
            memo.setText("");
            linear1.setVisibility(View.INVISIBLE);
            linear2.setVisibility(View.VISIBLE);
        }

        if (v.getId() == R.id.btnsave) {
            linear1.setVisibility(View.VISIBLE);
            linear2.setVisibility(View.INVISIBLE);
            Date date = new Date(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
            SimpleDateFormat format = new SimpleDateFormat("yy-mm-dd");
            final String filename = format.format(date)+".memo";
            final String path = getExternalPath();

           if (btnSave.getText().toString().equals("저장")) {
                count = 0;
                if (listName.contains(filename)) {
                    AlertDialog.Builder dlg = new AlertDialog.Builder(MainActivity.this);
                    dlg.setTitle("경고")
                            .setMessage("저장된 파일이 있습니다. 수정모드로 들어갑니다.")
                            .setPositiveButton("예", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    readFile(path + "diary/" +filename+ ".txt");
                                    btnSave.setText("수정");
                                }
                            }).show();
                    linear1.setVisibility(View.INVISIBLE);
                    linear2.setVisibility(View.VISIBLE);
                    temp = filename;
                    count++;
                    return;
                }
                writeFile(path + "diary/" +filename+ ".txt");
                Toast.makeText(this, "저장완료", Toast.LENGTH_SHORT).show();

            } else {
                if(count !=0) {
                    deleteExternalFile(path + "diary/" +temp+ ".txt");
                    count = 0;
                }
                writeFile(path + "diary/" +filename+ ".txt");
                Toast.makeText(this, "수정완료", Toast.LENGTH_SHORT).show();
                btnSave.setText("저장");

            }
            showList();
        }
        if (v.getId() == R.id.btncancel) {
            linear1.setVisibility(View.VISIBLE);
            linear2.setVisibility(View.INVISIBLE);
            btnSave.setText("저장");
        }
    }

    void listListener() {
        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder dlg = new AlertDialog.Builder(MainActivity.this);
                dlg.setTitle("경고")
                        .setMessage("삭제하시겠습니까?")
                        .setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String path = getExternalPath();
                                String item = listName.get(position);
                                deleteExternalFile(path + "diary/" +item+ ".txt");
                                showList();
                            }
                        })
                        .setNegativeButton("취소", null)
                        .show();
                return true;
            }
        });

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String path = getExternalPath();
                readFile(path + "diary/"+listName.get(position)+".txt");
                String[] date = listName.get(position).substring(0, 8).split("-");
                if (Integer.parseInt(date[0]) <= 20)
                    date[0] = "20" + date[0];
                else
                    date[0] = "19" + date[0];

                datePicker.init(Integer.parseInt(date[0]), Integer.parseInt(date[1])-1, Integer.parseInt(date[2]), null);
                btnSave.setText("수정");
                linear1.setVisibility(View.INVISIBLE);
                linear2.setVisibility(View.VISIBLE);
            }
        });
    }

    void readFile(String path) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            String readStr = "";
            String str;
            while ((str = br.readLine()) != null) {
                readStr += str;
            }
            br.close();

            memo.setText(readStr);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void writeFile(String path) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(path, false));
            bw.write(memo.getText().toString());
            bw.newLine();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    void deleteExternalFile(String path) {
        File file = new File(path);
        file.delete();

    }

    public String getExternalPath() {
        String sdPath ;
        String ext = Environment.getExternalStorageState();
        if (ext.equals(Environment.MEDIA_MOUNTED)) {
            sdPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
        } else
            sdPath = ""+getFilesDir();
        return sdPath;
    }
}