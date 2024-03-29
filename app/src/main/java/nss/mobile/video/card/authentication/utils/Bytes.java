package nss.mobile.video.card.authentication.utils;

public final class Bytes {
	public static int hashCode(byte value) {
		return value;
	}

	public static boolean contains(byte[] array, byte target) {
		for (byte value : array) {
			if (value == target) {
				return true;
			}
		}
		return false;
	}

	public static int indexOf(byte[] array, byte target) {
		return indexOf(array, target, 0, array.length);
	}

	private static int indexOf(byte[] array, byte target, int start, int end) {
		for (int i = start; i < end; i++) {
			if (array[i] == target) {
				return i;
			}
		}
		return -1;
	}

	public static int lastIndexOf(byte[] array, byte target) {
		return lastIndexOf(array, target, 0, array.length);
	}

	private static int lastIndexOf(byte[] array, byte target, int start, int end) {
		for (int i = end - 1; i >= start; i--) {
			if (array[i] == target) {
				return i;
			}
		}
		return -1;
	}

	public static byte[] concat(byte[][] arrays) {
		int length = 0;
		for (byte[] array : arrays) {
			length += array.length;
		}
		byte[] result = new byte[length];
		int pos = 0;
		for (byte[] array : arrays) {
			System.arraycopy(array, 0, result, pos, array.length);
			pos += array.length;
		}
		return result;
	}


	private static byte[] copyOf(byte[] original, int length) {
		byte[] copy = new byte[length];
		System.arraycopy(original, 0, copy, 0, Math.min(original.length, length));
		return copy;
	}
}