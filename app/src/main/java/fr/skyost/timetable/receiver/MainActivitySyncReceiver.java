package fr.skyost.timetable.receiver;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;

import de.mateware.snacky.Snacky;
import fr.skyost.timetable.R;
import fr.skyost.timetable.activity.IntroActivity;
import fr.skyost.timetable.activity.MainActivity;
import fr.skyost.timetable.sync.authentication.AuthenticationTask;
import fr.skyost.timetable.utils.Utils;

/**
 * The MainActivity synchronization receiver.
 */

public class MainActivitySyncReceiver extends BroadcastReceiver {

	/**
	 * The action.
	 */

	public static final String INTENT_ACTION = "sync-finished";

	/**
	 * The response intent key.
	 */

	public static final String INTENT_RESPONSE = "sync-response";

	/**
	 * The activity instance.
	 */

	private final MainActivity activity;

	/**
	 * Creates a new MainActivity synchronization receiver instance.
	 *
	 * @param activity The MainActivity instance.
	 */

	public MainActivitySyncReceiver(final MainActivity activity) {
		this.activity = activity;
	}

	@Override
	public void onReceive(final Context context, final Intent intent) {
		if(!intent.getBooleanExtra(ContentResolver.SYNC_EXTRAS_MANUAL, false)) {
			return;
		}
		intent.removeExtra(ContentResolver.SYNC_EXTRAS_MANUAL);
		switch(intent.getIntExtra(INTENT_RESPONSE, AuthenticationTask.ERROR)) {
		case AuthenticationTask.SUCCESS:
			// If success, nothing particular, we just open a SnackBar message.
			Snacky.builder().setView(activity.findViewById(R.id.main_fab)).setText(R.string.main_snackbar_success).success().show();
			break;
		case AuthenticationTask.NOT_FOUND:
			// If not found, we notify the user.
			if(activity.isFinishing()) {
				break;
			}
			new AlertDialog.Builder(activity)
					.setTitle(R.string.dialog_error_notfound_title)
					.setMessage(R.string.dialog_error_notfound_message)
					.setPositiveButton(R.string.dialog_generic_button_positive, null)
					.show();
			break;
		case AuthenticationTask.UNAUTHORIZED: {
			// If unauthorized, it means that the user has changed its credentials so we redirect him to the IntroActivity.
			final Snackbar snackbar = Snacky.builder().setView(activity.findViewById(R.id.main_fab)).setText(R.string.main_snackbar_error_credentials).warning();
			Utils.setSnackBarCallback(snackbar, new Snackbar.Callback() {

				@Override
				public void onDismissed(final Snackbar snackbar, final int event) {
					super.onDismissed(snackbar, event);
					// We go to the account slide.
					final Intent intent = new Intent(activity, IntroActivity.class);
					intent.putExtra(IntroActivity.INTENT_GOTO, IntroActivity.SLIDE_ACCOUNT);
					activity.startActivityForResult(intent, MainActivity.INTRO_ACTIVITY_RESULT);
				}

			});
			snackbar.show();
			break;
		}
		case AuthenticationTask.ERROR:
			// If error, we only have to notify the user.
			Snacky.builder().setView(activity.findViewById(R.id.main_fab)).setText(R.string.main_snackbar_error_network).error().show();
			break;
		}
	}

	/**
	 * Returns the activity instance.
	 *
	 * @return The activity instance.
	 */

	public MainActivity getActivity() {
		return activity;
	}

}
