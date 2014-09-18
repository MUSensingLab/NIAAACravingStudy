package edu.missouri.niaaa.craving.logger;

import java.io.Serializable;

import android.util.Log;

@SuppressWarnings("serial")
public class Logger implements Serializable {

	static String tagSuffix;

    private Logger(final String tagSuffix) {
        if (null == tagSuffix) {
            throw new IllegalArgumentException("The tag suffix cannot be null");
		} else if (tagSuffix.length() > 23) {
			Logger.tagSuffix = tagSuffix.substring(0, 22);
		} else {
			Logger.tagSuffix = tagSuffix;
		}
    }

    public static Logger getLogger(final String tagSuffix) {
        return new Logger(tagSuffix);
    }

    public static Logger getLogger(final Class<?> clazz) {
        return new Logger(clazz.getSimpleName());
    }

	public static void d(String msg) {
		Log.d(tagSuffix, msg);
    }

	public static void e(String msg) {
		Log.e(tagSuffix, msg);
	}

	public static void i(String msg) {
		Log.i(tagSuffix, msg);
	}

	public static void v(String msg) {
		Log.v(tagSuffix, msg);
	}

	public static void w(String msg) {
		Log.w(tagSuffix, msg);
	}
}

