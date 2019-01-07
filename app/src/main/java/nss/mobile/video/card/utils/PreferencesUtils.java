package nss.mobile.video.card.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import static android.provider.ContactsContract.Intents.Insert.NAME;

/**
 * @description Preferences管理工具类
 * @author liwei
 * @date 2016年10月11日
 * @version icon_1.0
 */
public class PreferencesUtils {

	public static final String FILE_NAME = "share_data";// 保存在手机里面的文件名

	public PreferencesUtils() {
		throw new UnsupportedOperationException("cannot be instantiated");
	}

	/**
	 * 保存数据的方法，我们需要拿到保存数据的具体类型
	 * 然后根据类型调用不同的保存方法
	 * @param key
	 * @param object
	 */
	public static void put(Context context, String key, Object object) {
		SharedPreferences sp = context.getSharedPreferences(FILE_NAME,
				Context.MODE_PRIVATE);
		Editor editor = sp.edit();
		if (object instanceof String) {
			editor.putString(key, (String) object);
		} else if (object instanceof Integer) {
			editor.putInt(key, (Integer) object);
		} else if (object instanceof Boolean) {
			editor.putBoolean(key, (Boolean) object);
		} else if (object instanceof Float) {
			editor.putFloat(key, (Float) object);
		} else if (object instanceof Long) {
			editor.putLong(key, (Long) object);
		} else {
			editor.putString(key, object.toString());
		}
		SharedPreferencesCompat.apply(editor);
	}

	/**
	 * 得到保存数据的方法，我们根据默认值得到保存的数据的具体类型
	 * 然后调用相对于的方法获取值
	 * @param key
	 * @param defaultObject
	 * @return
	 */
	public static Object get(Context context, String key, Object defaultObject) {
		SharedPreferences sp = context.getSharedPreferences(FILE_NAME,
				Context.MODE_PRIVATE);
		if (defaultObject instanceof String) {
			return sp.getString(key, (String) defaultObject);
		} else if (defaultObject instanceof Integer) {
			return sp.getInt(key, (Integer) defaultObject);
		} else if (defaultObject instanceof Boolean) {
			return sp.getBoolean(key, (Boolean) defaultObject);
		} else if (defaultObject instanceof Float) {
			return sp.getFloat(key, (Float) defaultObject);
		} else if (defaultObject instanceof Long) {
			return sp.getLong(key, (Long) defaultObject);
		}
		return null;
	}

	/**
	 * 移除某个key值已经对应的值
	 * @param key
	 */
	public static void remove(Context context, String key) {
		SharedPreferences sp = context.getSharedPreferences(FILE_NAME,
				Context.MODE_PRIVATE);
		Editor editor = sp.edit();
		editor.remove(key);
		SharedPreferencesCompat.apply(editor);
	}

	/**
	 * 清除所有数据
	 */
	public static void clear(Context context) {
		SharedPreferences sp = context.getSharedPreferences(FILE_NAME,
				Context.MODE_PRIVATE);
		Editor editor = sp.edit();
		editor.clear();
		SharedPreferencesCompat.apply(editor);
	}

	/**
	 * 返回所有的键值对
	 * @return
	 */
	public static Map<String, ?> getAll(Context context) {
		SharedPreferences sp = context.getSharedPreferences(FILE_NAME,
				Context.MODE_PRIVATE);
		return sp.getAll();
	}

	public static String getLastLoginToken(Context context){
		return PreferencesUtils.get(context,"token","")+"";
	}

	public static void saveLastLoginToken(Context context, String token) {
		LogUtil.i("utils的token:"+token);
			PreferencesUtils.put(context,"token",token);
	}

	public static SharedPreferences getSharedPreferences(Context context) {
		return context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
	}

	public static void putBean(Context context, String key, Object obj) {
		if (obj instanceof Serializable) {
			try {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ObjectOutputStream oos = new ObjectOutputStream(baos);
				oos.writeObject(obj);
				String string64 = new String(Base64.encode(baos.toByteArray(),
						0));
				Editor editor = getSharedPreferences(context).edit();
				editor.putString(key, string64);
				editor.commit();
			} catch (IOException e) {
				e.printStackTrace();
			}

		} else {
			throw new IllegalArgumentException(
					"the obj must implement Serializble");
		}

	}

	public static Object getBean(Context context, String key, String defVaule) {
		Object obj = null;
		try {
			String base64 = getSharedPreferences(context).getString(key, defVaule);
			if (base64.equals("")) {
				return null;
			}
			byte[] base64Bytes = Base64.decode(base64.getBytes(), 1);
			ByteArrayInputStream bais = new ByteArrayInputStream(base64Bytes);
			ObjectInputStream ois = new ObjectInputStream(bais);
			obj = ois.readObject();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return obj;
	}

	/**
	 * 创建一个解决SharedPreferencesCompat.apply方法的一个兼容类
	 * @author liwei
	 */
	private static class SharedPreferencesCompat {
		private static final Method sApplyMethod = findApplyMethod();
		/**
		 * 反射查找apply的方法
		 *
		 * @return
		 */
		@SuppressWarnings({ "unchecked", "rawtypes" })
		private static Method findApplyMethod() {
			try {
				Class clz = Editor.class;
				return clz.getMethod("apply");
			} catch (NoSuchMethodException e) {
			}
			return null;
		}

		/**
		 * 如果找到则使用apply执行，否则使用commit
		 * @param editor
		 */
		public static void apply(Editor editor) {
			try {
				if (sApplyMethod != null) {
					sApplyMethod.invoke(editor);
					return;
				}
			} catch (IllegalArgumentException e) {
			} catch (IllegalAccessException e) {
			} catch (InvocationTargetException e) {
			}
			editor.commit();
		}
	}
}
