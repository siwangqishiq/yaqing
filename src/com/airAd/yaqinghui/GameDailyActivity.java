package com.airAd.yaqinghui;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airAd.yaqinghui.business.AlarmService;
import com.airAd.yaqinghui.business.GameService;
import com.airAd.yaqinghui.business.model.GameInfo;
import com.airAd.yaqinghui.common.Common;
import com.airAd.yaqinghui.common.StringUtil;
import com.airAd.yaqinghui.ui.BackBaseActivity;
import com.airAd.yaqinghui.ui.CanCloseListView;
import com.airAd.yaqinghui.ui.PushClose;
import com.airAd.yaqinghui.ui.PushClose.OnDateClickListener;

/**
 * @author Panyi
 */
public class GameDailyActivity extends BackBaseActivity {

	private PushClose mPushClose;
	private CanCloseListView listView;
	private ProgressBar progressbar;
	private GameService gameService;
	private List<GameInfo> gameInfoList = new ArrayList<GameInfo>();
	private DailyAdapter dailyAdapter;
	// private OnClickListener addScheduleListener;
	private OnCheckedChangeListener scheduleChangeListener;
	private List<String> storedInfoIdList;// 已经持久化的gameInfolist

	private GameDailyTask task;
	private String gameId;
	private String gamePicUrl;
	private int checkboxWidth, checkboxHeight;
	private TextView bannerText;
	
	public static final String GAME_ID = "game_id";
	public static final String GAME_PIC_URL = "game_pic_url";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.schedule_daily);
		gameId = getIntent().getStringExtra(GAME_ID);
		gamePicUrl = getIntent().getStringExtra(GAME_PIC_URL);
		Drawable checkboxDrawable = getResources().getDrawable(R.drawable.game_daily_delete);
		checkboxWidth = checkboxDrawable.getIntrinsicWidth();
		checkboxHeight = checkboxDrawable.getIntrinsicHeight();
		init();
	}

	public void init() {
		gameService = new GameService();
		setPushClose();
		dailyAdapter = new DailyAdapter();
		listView.setAdapter(dailyAdapter);
		doDailyTask(mPushClose.getFirstDate());
		storedInfoIdList = gameService.queryScheduleIds();
		Log.i("storedInfoIdList", storedInfoIdList.toString());
		scheduleChangeListener = new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				int pos = (Integer) buttonView.getTag();
				GameInfo gameInfo = gameInfoList.get(pos);
				if (isChecked) {
					storedInfoIdList.add(gameInfo.getId());
					int cid = gameService.addtoSchedule(gameInfoList.get(pos), gamePicUrl);
					AlarmService.getInstance().addAlarm(cid, gameInfo.getStartTime(), gameInfo.getTitle());
				} else {
					storedInfoIdList.remove(gameInfoList.get(pos).getId());
					gameService.deleteFromSchedule(gameInfoList.get(pos)
							.getId());
					AlarmService.getInstance().removeAlarm(Integer.parseInt(gameInfo.getId()));
				}
				
				
			}
		};
		mPushClose.setOnDateClickListener(new OnDateClickListener() {

			@Override
			public void onDateClick(Calendar calendar) {
				doDailyTask(calendar);
				bannerText.setText(Common.genBannerText(calendar.get(Calendar.DAY_OF_MONTH)));
			}
		});
	}

	/**
	 * 设置时间数据
	 */
	private void setPushClose() {
		mPushClose = (PushClose) this.findViewById(R.id.pushClose);
		View bottomView = LayoutInflater.from(this)
				.inflate(R.layout.date, null);
		View topView = LayoutInflater.from(this).inflate(R.layout.dialy, null);
		bannerText= (TextView) topView.findViewById(R.id.date_banner);
		progressbar = (ProgressBar) topView.findViewById(R.id.progressBar);
		listView = (CanCloseListView) topView.findViewById(R.id.date_list);
		mPushClose.setContent(topView, bottomView);
		final Calendar calendar = Calendar.getInstance();
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int weekday = calendar.get(Calendar.DAY_OF_WEEK) - 1;
		TextView bannerText = (TextView) bottomView
				.findViewById(R.id.home_date_banner);
		TextView dateText = (TextView) topView.findViewById(R.id.date_banner);
		dateText.setText(day + " " + StringUtil.retWeekName(weekday));
	}

	private class DailyAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return gameInfoList.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = getLayoutInflater().inflate(
						R.layout.game_daily_item, null);
			}
			if (convertView.getTag() == null) {
				ViewHolder viewHolder = new ViewHolder();
				viewHolder.titleView = (TextView) convertView
						.findViewById(R.id.game_title);
				viewHolder.locView = (TextView) convertView
						.findViewById(R.id.game_loc);
				viewHolder.dateView = (TextView) convertView
						.findViewById(R.id.date);
				viewHolder.addCheckBox = (CheckBox) convertView
						.findViewById(R.id.game_add_btn);
				viewHolder.itemView = convertView.findViewById(R.id.daily_item);
				convertView.setTag(viewHolder);
			}
			ViewHolder viewHolder = (ViewHolder) convertView.getTag();
			if (gameInfoList.get(position).isGame()) {
				viewHolder.itemView
						.setBackgroundResource(R.drawable.game_daily_match_bg);
			} else {
				viewHolder.itemView
						.setBackgroundResource(R.drawable.game_daily_tran_bg);
			}
			viewHolder.titleView.setText(gameInfoList.get(position).getTitle());
			viewHolder.locView.setText(gameInfoList.get(position).getPlace());
			viewHolder.dateView.setText(formatTime(gameInfoList.get(position)
					.getTime()));
			RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams)viewHolder.addCheckBox.getLayoutParams();
			rlp.width = checkboxWidth;
			rlp.height = checkboxHeight;
			viewHolder.addCheckBox.setLayoutParams(rlp);
			viewHolder.addCheckBox.setTag(position);
			viewHolder.addCheckBox.setOnCheckedChangeListener(null);
			if (storedInfoIdList.contains(gameInfoList.get(position).getId())) {
				viewHolder.addCheckBox.setChecked(true);
			} else {
				viewHolder.addCheckBox.setChecked(false);
			}
			viewHolder.addCheckBox
					.setOnCheckedChangeListener(scheduleChangeListener);
			return convertView;
		}
	}

	public void doDailyTask(Calendar calendar) {
		gameInfoList.clear();
		dailyAdapter.notifyDataSetChanged();
		if (task != null && !task.isCancelled()) {
			task.cancel(true);
		}
		task = new GameDailyTask();
		task.execute(calendar);
	}

	public String formatTime(String time) {
		return time.substring(time.indexOf(" ") + 1);
	}

	private static class ViewHolder {
		public TextView titleView;
		public TextView locView;
		public TextView dateView;
		public CheckBox addCheckBox;
		public View itemView;
	}

	private class GameDailyTask extends
			AsyncTask<Calendar, Void, List<GameInfo>> {
		@Override
		protected void onPreExecute() {
			progressbar.setVisibility(View.VISIBLE);
			listView.setVisibility(View.GONE);
		}

		@Override
		protected List<GameInfo> doInBackground(Calendar... params) {
			Calendar c = params[0];
			return gameService.getGameInfo(gameId, c.getTime());
		}

		@Override
		protected void onPostExecute(List<GameInfo> list) {
			if (list != null) {
				gameInfoList = list;
			}
			progressbar.setVisibility(View.GONE);
			listView.setVisibility(View.VISIBLE);
			dailyAdapter.notifyDataSetChanged();
		}
	}
}
