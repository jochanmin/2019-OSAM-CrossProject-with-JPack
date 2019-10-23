package com.sonagi.android.myapplication;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.CalendarMode;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.sonagi.android.myapplication.decorators.OneDayDecorator;
import com.sonagi.android.myapplication.decorators.SaturdayDecorator;
import com.sonagi.android.myapplication.decorators.SundayDecorator;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.Date;


/**
 * A simple {@link Fragment} subclass.
 */
public class FirstFragment extends Fragment{

    String time,kcal,menu;
    private final OneDayDecorator oneDayDecorator = new OneDayDecorator();
    Cursor cursor;
    MaterialCalendarView datePicker;

    TextView viewDatePick;  //  viewDatePick - 선택한 날짜를 보여주는 textView
    EditText edtDiary;   //  edtDiary - 선택한 날짜의 일기를 쓰거나 기존에 저장된 일기가 있다면 보여주고 수정하는 영역
    Button btnSave;   //  btnSave - 선택한 날짜의 일기 저장 및 수정(덮어쓰기) 버튼
    String fileName;   //  fileName - 돌고 도는 선택된 날짜의 파일 이름
    String [] permission_list={Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.READ_CALENDAR}; //파일입출력을 위한 권한 체크


    public FirstFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        checkPermission(); // 권한 확인
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_first, container, false);
        datePicker = (MaterialCalendarView)view.findViewById(R.id.datePicker);
        viewDatePick = (TextView) view.findViewById(R.id.viewDatePick);
        edtDiary = (EditText) view.findViewById(R.id.edtDiary);
        btnSave = (Button) view.findViewById(R.id.btnSave);

        //오늘날 체크가 자동으로 되게 하는 코드
        datePicker.setCurrentDate(new Date(System.currentTimeMillis()));
        datePicker.setDateSelected(new Date(System.currentTimeMillis()), true);
        datePicker.setSelectedDate(new Date(System.currentTimeMillis()));

        //달력 셋팅
        datePicker.state().edit()
                .setFirstDayOfWeek(Calendar.SUNDAY)
                .setMinimumDate(CalendarDay.from(2017, 0, 1)) // 달력의 시작
                .setMaximumDate(CalendarDay.from(2030, 11, 31)) // 달력의 끝
                .setCalendarDisplayMode(CalendarMode.MONTHS)
                .commit();
        //주말 데코레이터해주기
        datePicker.addDecorators(
                new SundayDecorator(),
                new SaturdayDecorator(),
                oneDayDecorator);

        // 초록색 추가
        //datePicker.addDecorator(new SaveDayDecorator(year, month+1, day));




        // 오늘 날짜를 받게해주는 Calender 친구들
        Calendar c = Calendar.getInstance();

        int cYear = c.get(Calendar.YEAR);
        int cMonth = c.get(Calendar.MONTH);cMonth++;
        int cDay = c.get(Calendar.DAY_OF_MONTH);

        // 첫 시작 시에는 오늘 날짜 일기 읽어주기
        checkedDay(cYear, cMonth, cDay);
        DateListener listener=new DateListener();
        datePicker.setOnDateChangedListener(listener);


        // 저장/수정 버튼 누르면 실행되는 리스너
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // fileName을 넣고 저장 시키는 메소드를 호출
                saveDiary(fileName);
            }
        });
        return view;
    }


    //데이터피커 리스너
    class DateListener implements OnDateSelectedListener{
        @Override
        public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                // 이미 선택한 날짜에 일기가 있는지 없는지 체크해야할 시간이다
                int yearc=date.getYear();
                int days=date.getMonth();days++;
                int dayq=date.getDay();
                checkedDay(yearc, days, dayq);
        }
    }

    private void checkedDay(int year, int monthOfYear, int dayOfMonth) {


        // 받은 날짜로 날짜 보여주는
        viewDatePick.setText("일기 작성일 : "+year + " - " + monthOfYear + " - " + dayOfMonth);

        // 파일 이름을 만들어준다. 파일 이름은 "20170318.txt" 이런식으로 나옴
        fileName = year + "" + monthOfYear + "" + dayOfMonth + ".txt";

        // 읽어봐서 읽어지면 일기 가져오고
        // 없으면 catch 그냥 살아? 아주 위험한 생각같다..
        FileInputStream fis = null;
        try {
            fis = getActivity().openFileInput(fileName);
            DataInputStream dis = new DataInputStream(fis);
            //byte[] fileData = new byte[fis.available()];
            //fis.read(fileData);
            //fis.close();

            //String str = new String(fileData, "UTF-8");
            // 읽어서 토스트 메시지로 보여줌
            String str = dis.readUTF();
            dis.close();

            //Toast.makeText(getActivity().getApplicationContext(), "일기 써둔 날", Toast.LENGTH_SHORT).show();
            edtDiary.setText(str);
            btnSave.setText("수정하기");
        } catch (Exception e) { // UnsupportedEncodingException , FileNotFoundException , IOException
            // 없어서 오류가 나면 일기가 없는 것 -> 일기를 쓰게 한다.
            //Toast.makeText(getActivity().getApplicationContext(), "일기 없는 날", Toast.LENGTH_SHORT).show();
            edtDiary.setText("");
            btnSave.setText("새 일기 저장");
            e.printStackTrace();
        }

    }

    // 일기 저장하는 메소드
    private void saveDiary(String readDay) {
        String rt=readDay.substring(0,8);

        FileOutputStream fos = null;

        try {
            fos = getActivity().openFileOutput(readDay, Context.MODE_PRIVATE); //MODE_WORLD_WRITEABLE

            String content = edtDiary.getText().toString();

            DataOutputStream dos = new DataOutputStream(fos);

            dos.writeUTF(content);
            dos.flush();
            dos.close();

            // getApplicationContext() = 현재 클래스.this ?
            Toast.makeText(getActivity().getApplicationContext(), "성공", Toast.LENGTH_SHORT).show();

        } catch (Exception e) { // Exception - 에러 종류 제일 상위 // FileNotFoundException , IOException
            e.printStackTrace();
            Toast.makeText(getActivity().getApplicationContext(), "오류오류", Toast.LENGTH_SHORT).show();
        }
    }
    public void checkPermission(){
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
            return;
        }
        // 각 권한의 허용 여부를 확인한다.
        for(String permission : permission_list){
            // 권한 허용 여부를 확인한다.
            int chk = getActivity().checkCallingOrSelfPermission(permission);
            // 거부 상태라고 한다면..
            if(chk == PackageManager.PERMISSION_DENIED){
                // 사용자에게 권한 허용여부를 확인하는 창을 띄운다.
                requestPermissions(permission_list, 0);
            }
        }
    }

}
