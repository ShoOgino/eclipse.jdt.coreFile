package org.eclipse.jdt.internal.core.nd.java;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.internal.core.nd.Nd;
import org.eclipse.jdt.internal.core.nd.db.Database;
import org.eclipse.jdt.internal.core.nd.db.IndexException;

/**
 * @since 3.12
 */
public abstract class TagTreeReader {
	public static final int[] UNUSED_RESULT = new int[1];

	public static abstract class TagHandler<T> {
		abstract public T read(Nd pdom, long address, TagTreeReader reader, int[] bytesRead);
		abstract public void write(Nd pdom, long address, TagTreeReader reader, T toWrite, int[] bytesWritten);
		abstract public int getSize(Nd pdom, T object, TagTreeReader reader);
		public void destruct(Nd pdom, long address, TagTreeReader reader) {
			// Nothing to do by default
		}
	}

	public static abstract class FixedSizeTagHandler<T> extends TagHandler<T> {
		protected abstract T read(Nd pdom, long address);
		protected abstract void write(Nd pdom, long address, T value);
		protected abstract int getSize();
		protected void destruct(Nd pdom, long address) {
			// Nothing to do by default
		}

		public final T read(Nd pdom, long address, TagTreeReader reader, int[] bytesRead) {
			bytesRead[0] = getSize();
			return read(pdom, address);
		}

		@Override
		public final void write(Nd pdom, long address, TagTreeReader reader, T value, int[] bytesWritten) {
			bytesWritten[0] = getSize();
			write(pdom, address, value);
		}

		@Override
		public final int getSize(Nd pdom, T object, TagTreeReader reader) {
			return getSize();
		}

		@Override
		public final void destruct(Nd pdom, long address, TagTreeReader reader) {
			destruct(pdom, address);
		}
	}

	private TagHandler<?> readers[] = new TagHandler[256];
	private Map<TagHandler<?>, Integer> values = new HashMap<>();

	public final void add(byte key, TagHandler<?> reader) {
		this.readers[key] = reader;
		this.values.put(reader, (int) key);
	}

	public final Object read(Nd pdom, long address) {
		return read(pdom, address, UNUSED_RESULT);
	}

	public final Object read(Nd pdom, long address, int[] bytesRead) {
		long readAddress = address;
		Database db = pdom.getDB();
		byte nextByte = db.getByte(address);
		readAddress += Database.BYTE_SIZE;
		TagHandler<?> reader = this.readers[nextByte];
		if (reader == null) {
			throw new IndexException("Found unknown tag with value " + nextByte + " at address " + address); //$NON-NLS-1$//$NON-NLS-2$
		}

		return reader.read(pdom, readAddress, this, bytesRead);
	}

	protected abstract byte getKeyFor(Object toWrite);

	public final void write(Nd pdom, long address, Object toWrite) {
		write(pdom, address, toWrite, UNUSED_RESULT);
	}

	@SuppressWarnings("unchecked")
	public final void write(Nd pdom, long address, Object toWrite, int[] bytesWritten) {
		byte key = getKeyFor(toWrite);

		@SuppressWarnings("rawtypes")
		TagHandler handler = this.readers[key];

		if (handler == null) {
			throw new IndexException("Invalid key " + key + " returned from getKeyFor(...)"); //$NON-NLS-1$//$NON-NLS-2$
		}

		handler.write(pdom, address, this, toWrite, bytesWritten);
	}

	public final void destruct(Nd pdom, long address) {
		Database db = pdom.getDB();
		long readAddress = address;
		byte nextByte = db.getByte(readAddress);
		readAddress += Database.BYTE_SIZE;

		TagHandler<?> handler = this.readers[nextByte];
		if (handler == null) {
			throw new IndexException("Found unknown tag with value " + nextByte + " at address " + address); //$NON-NLS-1$//$NON-NLS-2$
		}

		handler.destruct(pdom, readAddress, this);
	}

	@SuppressWarnings("unchecked")
	public final int getSize(Nd pdom, Object toMeasure) {
		byte key = getKeyFor(toMeasure);

		@SuppressWarnings("rawtypes")
		TagHandler handler = this.readers[key];
		if (handler == null) {
			throw new IndexException("Attempted to get size of object " + toMeasure.toString() + " with unknown key " //$NON-NLS-1$//$NON-NLS-2$
					+ key);
		}

		return handler.getSize(pdom, toMeasure, this);
	}
}
