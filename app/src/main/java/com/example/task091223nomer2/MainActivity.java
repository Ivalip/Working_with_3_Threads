package com.example.task091223nomer2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextDirectionHeuristics;
import android.util.Log;
import android.view.View;

import com.example.task091223nomer2.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    static int turn = 0;

    static boolean[] flags = new boolean[]{false, false, false};

    public static Object lock = new Object();

    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        handler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                char chars = (char) msg.obj;
                String str = String.valueOf(chars);
                binding.ETM.append(str);
            }
        };

        binding.But.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.ETM.setText("");
                flags = new boolean[] {false, false, false};
                turn = 0;

                String str1 = binding.ET1.getText().toString();
                String str2 = binding.ET2.getText().toString();
                String str3 = binding.ET3.getText().toString();

                MyThread Thread1 = new MyThread(str1, 0);
                MyThread Thread2 = new MyThread(str2, 1);
                MyThread Thread3 = new MyThread(str3, 2);

                Thread1.start();
                Thread2.start();
                Thread3.start();
            }
        });
    }

    class MyThread extends Thread{
        private char[] textToView;
        private String text;
        private int num;
        public MyThread(String text, int num) {
            this.num = num;
            this.text = text;
            this.textToView = new char[text.length()];
        }

        @Override
        public void run() {
            super.run();
            char[] textchars = text.toCharArray();

            synchronized (lock) {
                for (int i = 0; i < textchars.length; i++) {
                    while (turn != num) {
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    char ch = textchars[i];
                    Message msg = new Message();
                    msg.obj = ch;
                    handler.sendMessage(msg);

                    try {
                        if (ch == ' ') {
                            do {
                                turn = (turn + 1) % 3;
                            } while (flags[turn]);
                            Log.d("turn", "turn = " + turn);
                            Log.d("msg", "ch = " + ch);
                            lock.notify();
                            lock.notify();
                        } else {
                            Thread.sleep(50);
                        }
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                Message msg = new Message();
                msg.obj = ' ';
                handler.sendMessage(msg);

                flags[num] = true;
                do {
                    turn = (turn + 1) % 3;
                } while (flags[turn]);
                lock.notify();
                lock.notify();
                Log.d("turn", "flags [" + flags[0] + " " + flags[1] + " "+ flags[2] + "]");
            }
        }
    }
}