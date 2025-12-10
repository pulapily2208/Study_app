package com.example.study_app.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.RemoteViews;

import com.example.study_app.R;
import com.example.study_app.data.DatabaseHelper;
import com.example.study_app.data.TimetableDao;
import com.example.study_app.ui.Subject.Model.Subject;
import com.example.study_app.ui.Subject.SubjectDetailActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class TodayWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        for (int id : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.today_widget);

            List<Subject> todayList = getTodaySubjects(context);

            if (todayList.isEmpty()) {
                views.setTextViewText(R.id.txtTodayLabel, "Hôm nay không có môn học");
                hideItem(views, R.id.item1);
                hideItem(views, R.id.item2);
                hideItem(views, R.id.item3);
                views.setViewVisibility(R.id.txtMore, View.GONE);
            } else {
                views.setTextViewText(R.id.txtTodayLabel,
                        "Hôm nay: " + todayList.size() + " môn");

                renderItem(context, views, 1, todayList, 0);
                renderItem(context, views, 2, todayList, 1);
                renderItem(context, views, 3, todayList, 2);

                if (todayList.size() > 3) {
                    int more = todayList.size() - 3;
                    views.setTextViewText(R.id.txtMore, "+" + more + " môn nữa…");
                    views.setViewVisibility(R.id.txtMore, View.VISIBLE);
                } else {
                    views.setViewVisibility(R.id.txtMore, View.GONE);
                }
            }

            appWidgetManager.updateAppWidget(id, views);
        }
    }

    private void hideItem(RemoteViews v, int layoutId) {
        v.setViewVisibility(layoutId, View.GONE);
    }

    private void renderItem(Context ctx, RemoteViews v, int index, List<Subject> list, int pos) {
        int layoutId, titleId, timeId, roomId;

        if (pos >= list.size()) {
            if (index == 1) hideItem(v, R.id.item1);
            if (index == 2) hideItem(v, R.id.item2);
            if (index == 3) hideItem(v, R.id.item3);
            return;
        }

        Subject s = list.get(pos);

        switch (index) {
            case 1:
                layoutId = R.id.item1;
                titleId = R.id.item1_title;
                timeId = R.id.item1_time;
                roomId = R.id.item1_room;
                break;
            case 2:
                layoutId = R.id.item2;
                titleId = R.id.item2_title;
                timeId = R.id.item2_time;
                roomId = R.id.item2_room;
                break;
            default:
                layoutId = R.id.item3;
                titleId = R.id.item3_title;
                timeId = R.id.item3_time;
                roomId = R.id.item3_room;
        }

        v.setViewVisibility(layoutId, View.VISIBLE);
        v.setTextViewText(titleId, s.tenHp);

        SimpleDateFormat f = new SimpleDateFormat("HH:mm", Locale.getDefault());
        v.setTextViewText(timeId, f.format(s.gioBatDau) + " - " + f.format(s.gioKetThuc));
        v.setTextViewText(roomId, "Phòng: " + s.phongHoc);

        // enable click: open SubjectDetailActivity
        Intent it = new Intent(ctx, SubjectDetailActivity.class);
        it.putExtra("SUBJECT_ID", s.maHp);

        PendingIntent pi = PendingIntent.getActivity(
                ctx, pos + 100,
                it,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        v.setOnClickPendingIntent(layoutId, pi);
    }

    /** Lọc các môn học của hôm nay dựa theo weekday của ngày bắt đầu */
    private List<Subject> getTodaySubjects(Context context) {
        DatabaseHelper db = new DatabaseHelper(context);
        TimetableDao dao = new TimetableDao(db);

        List<Subject> all = dao.getAllSubjects();
        List<Subject> today = new ArrayList<>();

        Calendar now = Calendar.getInstance();
        int todayW = now.get(Calendar.DAY_OF_WEEK);

        for (Subject s : all) {
            if (s.ngayBatDau == null) continue;

            Calendar c = Calendar.getInstance();
            c.setTime(s.ngayBatDau);
            int weekday = c.get(Calendar.DAY_OF_WEEK);

            if (weekday == todayW) {
                today.add(s);
            }
        }
        return today;
    }

    /** Gọi để refresh widget */
    public static void refresh(Context ctx) {
        AppWidgetManager mgr = AppWidgetManager.getInstance(ctx);
        ComponentName me = new ComponentName(ctx, TodayWidget.class);
        int[] ids = mgr.getAppWidgetIds(me);

        Intent i = new Intent(ctx, TodayWidget.class);
        i.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        i.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        ctx.sendBroadcast(i);
    }
}
