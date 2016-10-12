/*******************************************************************************
 * Copyright (c) 2005, 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX - Initial API and implementation
 *     Symbian - Add some non-javadoc implementation notes
 *     Markus Schorn (Wind River Systems)
 *     IBM Corporation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.jdt.internal.core.nd.db;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;

/**
 * Database encapsulates access to a flat binary format file with a memory-manager-like API for
 * obtaining and releasing areas of storage (memory).
 */
/*
 * The file encapsulated is divided into Chunks of size CHUNK_SIZE, and a table of contents
 * mapping chunk index to chunk address is maintained. Chunk structure exists only conceptually -
 * it is not a structure that appears in the file.
 *
 * ===== The first chunk is used by Database itself for house-keeping purposes and has structure
 *
 * offset                content
 * 	                     _____________________________
 * 0                    | version number
 * INT_SIZE             | pointer to head of linked list of blocks of size MIN_BLOCK_DELTAS*BLOCK_SIZE_DELTA
 * ..                   | ...
 * INT_SIZE * (M + 1)   | pointer to head of linked list of blocks of size (M + MIN_BLOCK_DELTAS) * BLOCK_SIZE_DELTA
 * WRITE_NUMBER_OFFSET  | long integer which is incremented on every write
 * MALLOC_STATS_OFFSET  | memory usage statistics 
 * DATA_AREA            | The database singletons are stored here and use the remainder of chunk 0
 *
 * M = CHUNK_SIZE / BLOCK_SIZE_DELTA - MIN_BLOCK_DELTAS
 *
 * ===== block structure (for free/unused blocks)
 *
 * offset            content
 * 	                 _____________________________
 * 0                | size of block (positive indicates an unused block) (2 bytes)
 * PREV_OFFSET      | pointer to previous block (of same size) (only in free blocks)
 * NEXT_OFFSET      | pointer to next block (of same size) (only in free blocks)
 * ...              | unused space
 *
 *====== block structure (for allocated blocks)
 *
 * offset            content
 * 	                 _____________________________
 * 0                | size of block (negative indicates the block is in use) (2 bytes)
 * 2                | content of the struct 
 *
 */
public class Database {
	public static final int CHAR_SIZE = 2;
	public static final int BYTE_SIZE = 1;
	public static final int SHORT_SIZE = 2;
	public static final int INT_SIZE = 4;
	public static final int LONG_SIZE = 8;
	public static final int CHUNK_SIZE = 1024 * 4;
	public static final int OFFSET_IN_CHUNK_MASK= CHUNK_SIZE - 1;
	public static final int BLOCK_HEADER_SIZE = SHORT_SIZE;

	public static final int BLOCK_SIZE_DELTA_BITS = 3;
	public static final int BLOCK_SIZE_DELTA= 1 << BLOCK_SIZE_DELTA_BITS;
	
	// Fields that are only used by free blocks
	private static final int BLOCK_PREV_OFFSET = BLOCK_HEADER_SIZE;
	private static final int BLOCK_NEXT_OFFSET = BLOCK_HEADER_SIZE + INT_SIZE;
	private static final int FREE_BLOCK_HEADER_SIZE = BLOCK_NEXT_OFFSET + INT_SIZE;
	
	public static final int MIN_BLOCK_DELTAS = (FREE_BLOCK_HEADER_SIZE + BLOCK_SIZE_DELTA - 1) /
			BLOCK_SIZE_DELTA; // Must be enough multiples of BLOCK_SIZE_DELTA in order to fit the free block header
	public static final int MAX_BLOCK_DELTAS = CHUNK_SIZE / BLOCK_SIZE_DELTA;
	public static final int MAX_MALLOC_SIZE = MAX_BLOCK_DELTAS * BLOCK_SIZE_DELTA - BLOCK_HEADER_SIZE;
	public static final int PTR_SIZE = 4;  // size of a pointer in the database in bytes
	public static final int STRING_SIZE = PTR_SIZE;
	public static final int FLOAT_SIZE = INT_SIZE;
	public static final int DOUBLE_SIZE = LONG_SIZE;
	public static final long MAX_DB_SIZE= ((long) 1 << (Integer.SIZE + BLOCK_SIZE_DELTA_BITS));

	public static final int VERSION_OFFSET = 0;
	private static final int MALLOC_TABLE_OFFSET = VERSION_OFFSET + INT_SIZE;
	public static final int WRITE_NUMBER_OFFSET = MALLOC_TABLE_OFFSET
			+ (CHUNK_SIZE / BLOCK_SIZE_DELTA - MIN_BLOCK_DELTAS + 1) * INT_SIZE;
	public static final int MALLOC_STATS_OFFSET = WRITE_NUMBER_OFFSET + LONG_SIZE;
	public static final int DATA_AREA_OFFSET = MALLOC_STATS_OFFSET + MemoryStats.SIZE;

	// Malloc pool IDs (used for classifying memory allocations and recording statistics about them)
	/** Misc pool -- may be used for any purpose that doesn't fit the IDs below. */
	public static final short POOL_MISC 			= 0x0000;
	public static final short POOL_BTREE 			= 0x0001;
	public static final short POOL_DB_PROPERTIES 	= 0x0002;
	public static final short POOL_STRING_LONG 		= 0x0003;
	public static final short POOL_STRING_SHORT		= 0x0004;
	public static final short POOL_LINKED_LIST		= 0x0005;
	public static final short POOL_STRING_SET 		= 0x0006;
	public static final short POOL_GROWABLE_ARRAY	= 0x0007;
	/** Id for the first node type. All node types will record their stats in a pool whose ID is POOL_FIRST_NODE_TYPE + node_id*/
	public static final short POOL_FIRST_NODE_TYPE	= 0x0100;

	private final File fLocation;
	private final boolean fReadOnly;
	private RandomAccessFile fFile;
	private boolean fExclusiveLock;	 // Necessary for any write operation.
	private boolean fLocked;		 // Necessary for any operation.
	private boolean fIsMarkedIncomplete;

	private int fVersion;
	private final Chunk fHeaderChunk;
	private Chunk[] fChunks;
	private int fChunksUsed;
	private int fChunksAllocated;
	private ChunkCache fCache;

	private long malloced;
	private long freed;
	private long cacheHits;
	private long cacheMisses;

	private MemoryStats memoryUsage;

	/**
	 * Construct a new Database object, creating a backing file if necessary.
	 * @param location the local file path for the database
	 * @param cache the cache to be used optimization
	 * @param version the version number to store in the database (only applicable for new databases)
	 * @param openReadOnly whether this Database object will ever need writing to
	 * @throws IndexException
	 */
	public Database(File location, ChunkCache cache, int version, boolean openReadOnly) throws IndexException {
		try {
			this.fLocation = location;
			this.fReadOnly= openReadOnly;
			this.fCache= cache;
			openFile();

			int nChunksOnDisk = (int) (this.fFile.length() / CHUNK_SIZE);
			this.fHeaderChunk= new Chunk(this, 0);
			this.fHeaderChunk.fLocked= true;		// Never makes it into the cache, needed to satisfy assertions.
			if (nChunksOnDisk <= 0) {
				this.fVersion= version;
				this.fChunks= new Chunk[1];
				this.fChunksUsed = this.fChunksAllocated = this.fChunks.length;
			} else {
				this.fHeaderChunk.read();
				this.fVersion= this.fHeaderChunk.getInt(VERSION_OFFSET);
				this.fChunks = new Chunk[nChunksOnDisk];	// chunk[0] is unused.
				this.fChunksUsed = this.fChunksAllocated = nChunksOnDisk;
			}
		} catch (IOException e) {
			throw new IndexException(new DBStatus(e));
		}
		this.memoryUsage = new MemoryStats(this.fHeaderChunk, MALLOC_STATS_OFFSET);
	}

	private static int divideRoundingUp(int num, int den) {
		return (num + den - 1) / den;
	}

	private void openFile() throws FileNotFoundException {
		this.fFile = new RandomAccessFile(this.fLocation, this.fReadOnly ? "r" : "rw"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	void read(ByteBuffer buf, long position) throws IOException {
		int retries= 0;
		do {
			try {
				this.fFile.getChannel().read(buf, position);
				return;
			} catch (ClosedChannelException e) {
				// Always reopen the file if possible or subsequent reads will fail.
				openFile();

				// This is the most common type of interruption. If another thread called Thread.interrupt,
				// throw an OperationCanceledException.
				if (e instanceof ClosedByInterruptException) {
					throw new OperationCanceledException();
				}

				// If we've retried too many times, just rethrow the exception.
				if (++retries >= 20) {
					throw e;
				}

				// Otherwise, retry
			}
		} while (true);
	}

	/**
	 * Attempts to write to the given position in the file. Will retry if interrupted by Thread.interrupt() until,
	 * the write succeeds. It will return true if any call to Thread.interrupt() was detected.
	 *
	 * @return true iff a call to Thread.interrupt() was detected at any point during the operation.
	 * @throws IOException
	 */
	boolean write(ByteBuffer buf, long position) throws IOException {
		return performUninterruptableWrite(() -> {this.fFile.getChannel().write(buf, position);});
	}

	private static interface IORunnable {
		void run() throws IOException;
	}

	/**
	 * Attempts to perform an uninterruptable write operation on the database. Returns true if an attempt was made
	 * to interrupt it. 
	 * 
	 * @throws IOException
	 */
	private boolean performUninterruptableWrite(IORunnable runnable) throws IOException {
		boolean interrupted = false;
		int retries= 0;
		while (true) {
			try {
				runnable.run();
				return interrupted;
			} catch (ClosedChannelException e) {
				openFile();

				if (e instanceof ClosedByInterruptException) {
					// Retry forever if necessary as long as another thread is calling Thread.interrupt
					interrupted = true;
				} else {
					if (++retries > 20) {
						throw e;
					}
				}
			}
		}
	}
	
	public void transferTo(FileChannel target) throws IOException {
		assert this.fLocked;
        final FileChannel from= this.fFile.getChannel();
        long nRead = 0;
        long position = 0;
        long size = from.size();
        while (position < size) {
        	nRead = from.transferTo(position, 4096 * 16, target);
        	if (nRead == 0) {
        		break;		// Should not happen.
        	} else {
        		position+= nRead;
        	}
        }
	}

	public int getVersion() {
		return this.fVersion;
	}

	public void setVersion(int version) throws IndexException {
		assert this.fExclusiveLock;
		this.fHeaderChunk.putInt(VERSION_OFFSET, version);
		this.fVersion= version;
	}

	/**
	 * Empty the contents of the Database, make it ready to start again. Interrupting the thread with
	 * {@link Thread#interrupt()} won't interrupt the write. Returns true iff the thread was interrupted
	 * with {@link Thread#interrupt()}.
	 * 
	 * @throws IndexException
	 */
	public boolean clear(int version) throws IndexException {
		assert this.fExclusiveLock;
		boolean wasCanceled = false;
		removeChunksFromCache();

		this.fVersion= version;
		// Clear the first chunk.
		this.fHeaderChunk.clear(0, CHUNK_SIZE);
		// Chunks have been removed from the cache, so we may just reset the array of chunks.
		this.fChunks = new Chunk[] {null};
		this.fChunksUsed = this.fChunksAllocated = this.fChunks.length;
		try {
			wasCanceled = this.fHeaderChunk.flush() || wasCanceled; // Zero out header chunk.
			wasCanceled = performUninterruptableWrite(() -> {
				this.fFile.getChannel().truncate(CHUNK_SIZE);
			}) || wasCanceled;
		} catch (IOException e) {
			Package.log(e);
		}
		this.malloced = this.freed = 0;
		/*
		 * This is for debugging purposes in order to simulate having a very large Nd database.
		 * This will set aside the specified number of chunks.
		 * Nothing uses these chunks so subsequent allocations come after these fillers.
		 * The special function createNewChunks allocates all of these chunks at once.
		 * 524288 for a file starting at 2G
		 * 8388608 for a file starting at 32G
		 *
		 */
		long setasideChunks = Long.getLong("org.eclipse.jdt.core.parser.nd.chunks", 0); //$NON-NLS-1$
		if (setasideChunks != 0) {
			setVersion(getVersion());
			createNewChunks((int) setasideChunks);
			wasCanceled = flush() || wasCanceled;
		}
		this.memoryUsage.refresh();
		return wasCanceled;
	}

	private void removeChunksFromCache() {
		synchronized (this.fCache) {
			for (int i= 1; i < this.fChunks.length; i++) {
				Chunk chunk= this.fChunks[i];
				if (chunk != null) {
					this.fCache.remove(chunk);
					this.fChunks[i]= null;
				}
			}
		}
	}

	/**
	 * Return the Chunk that contains the given offset.
	 * @throws IndexException
	 */
	public Chunk getChunk(long offset) throws IndexException {
		assertLocked();
		if (offset < CHUNK_SIZE) {
			return this.fHeaderChunk;
		}
		long long_index = offset / CHUNK_SIZE;
		assert long_index < Integer.MAX_VALUE;

		synchronized (this.fCache) {
			assert this.fLocked;
			final int index = (int) long_index;
			if (index < 0 || index >= this.fChunks.length) {
				databaseCorruptionDetected();
			}
			Chunk chunk= this.fChunks[index];
			if (chunk == null) {
				this.cacheMisses++;
				chunk = new Chunk(this, index);
				chunk.read();
				this.fChunks[index] = chunk;
			} else {
				this.cacheHits++;
			}
			this.fCache.add(chunk, this.fExclusiveLock);
			return chunk;
		}
	}

	public void assertLocked() {
		if (!this.fLocked) {
			throw new IllegalStateException("Database not locked!"); //$NON-NLS-1$
		}
	}

	private void databaseCorruptionDetected() throws IndexException {
		String msg = "Corrupted database: " + this.fLocation.getName(); //$NON-NLS-1$
		throw new IndexException(new DBStatus(msg));
	}

	/**
	 * Copies numBytes from source to destination
	 */
	public void memcpy(long dest, long source, int numBytes) {
		assert numBytes >= 0;
		assert numBytes <= MAX_MALLOC_SIZE;
		// TODO: make use of lower-level System.arrayCopy
		for (int count = 0; count < numBytes; count++) {
			putByte(dest + count, getByte(source + count));
		}
	}

	/**
	 * Allocate a block out of the database.
	 */
	public long malloc(final int datasize, final short poolId) throws IndexException {
		assert this.fExclusiveLock;
		assert datasize >= 0;
		assert datasize <= MAX_MALLOC_SIZE;

		int needDeltas= divideRoundingUp(datasize + BLOCK_HEADER_SIZE, BLOCK_SIZE_DELTA);
		if (needDeltas < MIN_BLOCK_DELTAS) {
			needDeltas= MIN_BLOCK_DELTAS;
		}

		// Which block size.
		long freeblock = 0;
		int useDeltas;
		for (useDeltas= needDeltas; useDeltas <= MAX_BLOCK_DELTAS; useDeltas++) {
			freeblock = getFirstBlock(useDeltas * BLOCK_SIZE_DELTA);
			if (freeblock != 0)
				break;
		}

		// Get the block.
		Chunk chunk;
		if (freeblock == 0) {
			// Allocate a new chunk.
			freeblock= createNewChunk();
			useDeltas = MAX_BLOCK_DELTAS;
			chunk = getChunk(freeblock);
		} else {
			chunk = getChunk(freeblock);
			removeBlock(chunk, useDeltas * BLOCK_SIZE_DELTA, freeblock);
		}

		final int unusedDeltas = useDeltas - needDeltas;
		if (unusedDeltas >= MIN_BLOCK_DELTAS) {
			// Add in the unused part of our block.
			addBlock(chunk, unusedDeltas * BLOCK_SIZE_DELTA, freeblock + needDeltas * BLOCK_SIZE_DELTA);
			useDeltas= needDeltas;
		}

		// Make our size negative to show in use.
		final int usedSize= useDeltas * BLOCK_SIZE_DELTA;
		chunk.putShort(freeblock, (short) -usedSize);

		// Clear out the block, lots of people are expecting this.
		chunk.clear(freeblock + BLOCK_HEADER_SIZE, usedSize - BLOCK_HEADER_SIZE);

		this.malloced += usedSize;
		long result = freeblock + BLOCK_HEADER_SIZE;
		this.memoryUsage.recordMalloc(poolId, usedSize);
		return result;
	}

	private long createNewChunk() throws IndexException {
		assert this.fExclusiveLock;
		synchronized (this.fCache) {
			final int newChunkIndex = this.fChunksUsed; // fChunks.length;

			final Chunk chunk = new Chunk(this, newChunkIndex);
			chunk.fDirty = true;

			if (newChunkIndex >= this.fChunksAllocated) {
				int increment = Math.max(1024, this.fChunksAllocated / 20);
				Chunk[] newchunks = new Chunk[this.fChunksAllocated + increment];
				System.arraycopy(this.fChunks, 0, newchunks, 0, this.fChunksAllocated);

				this.fChunks = newchunks;
				this.fChunksAllocated += increment;
			}
			this.fChunksUsed += 1;
			this.fChunks[newChunkIndex] = chunk;

			this.fCache.add(chunk, true);
			long address = (long) newChunkIndex * CHUNK_SIZE;

			/*
			 * Non-dense pointers are at most 31 bits dense pointers are at most 35 bits Check the sizes here and throw
			 * an exception if the address is too large. By throwing the IndexException with the special status, the
			 * indexing operation should be stopped. This is desired since generally, once the max size is exceeded,
			 * there are lots of errors.
			 */
			if (address >= MAX_DB_SIZE) {
				Object bindings[] = { this.getLocation().getAbsolutePath(), MAX_DB_SIZE };
				throw new IndexException(new Status(IStatus.ERROR, Package.PLUGIN_ID, Package.STATUS_DATABASE_TOO_LARGE,
						NLS.bind("Database too large! Address = " + address + ", max size = " + MAX_DB_SIZE, bindings), //$NON-NLS-1$ //$NON-NLS-2$
						null));
			}
			return address;
		}
	}

	/**
	 * For testing purposes, only.
	 */
	private long createNewChunks(int numChunks) throws IndexException {
		assert this.fExclusiveLock;
		synchronized (this.fCache) {
			final int oldLen= this.fChunks.length;
			Chunk[] newchunks = new Chunk[oldLen + numChunks];
			System.arraycopy(this.fChunks, 0, newchunks, 0, oldLen);
			final Chunk chunk= new Chunk(this, oldLen + numChunks - 1);
			chunk.fDirty= true;
			newchunks[ oldLen + numChunks - 1 ] = chunk;
			this.fChunks= newchunks;
			this.fCache.add(chunk, true);
			this.fChunksAllocated=oldLen + numChunks;
			this.fChunksUsed=oldLen + numChunks;
			return (long) (oldLen + numChunks - 1) * CHUNK_SIZE;
		}
	}

	/**
	 * @param blocksize (must be a multiple of BLOCK_SIZE_DELTA)
	 */
	private long getFirstBlock(int blocksize) throws IndexException {
		assert this.fLocked;
		return this.fHeaderChunk.getFreeRecPtr(MALLOC_TABLE_OFFSET + (blocksize / BLOCK_SIZE_DELTA - MIN_BLOCK_DELTAS) * INT_SIZE);
	}

	private void setFirstBlock(int blocksize, long block) throws IndexException {
		assert this.fExclusiveLock;
		this.fHeaderChunk.putFreeRecPtr(MALLOC_TABLE_OFFSET + (blocksize / BLOCK_SIZE_DELTA - MIN_BLOCK_DELTAS) * INT_SIZE, block);
	}

	private void removeBlock(Chunk chunk, int blocksize, long block) throws IndexException {
		assert this.fExclusiveLock;
		long prevblock = chunk.getFreeRecPtr(block + BLOCK_PREV_OFFSET);
		long nextblock = chunk.getFreeRecPtr(block + BLOCK_NEXT_OFFSET);
		if (prevblock != 0) {
			putFreeRecPtr(prevblock + BLOCK_NEXT_OFFSET, nextblock);
		} else { // We were the head.
			setFirstBlock(blocksize, nextblock);
		}

		if (nextblock != 0)
			putFreeRecPtr(nextblock + BLOCK_PREV_OFFSET, prevblock);
	}

	private void addBlock(Chunk chunk, int blocksize, long block) throws IndexException {
		assert this.fExclusiveLock;
		// Mark our size
		chunk.putShort(block, (short) blocksize);

		// Add us to the head of the list.
		long prevfirst = getFirstBlock(blocksize);
		chunk.putFreeRecPtr(block + BLOCK_PREV_OFFSET, 0);
		chunk.putFreeRecPtr(block + BLOCK_NEXT_OFFSET, prevfirst);
		if (prevfirst != 0)
			putFreeRecPtr(prevfirst + BLOCK_PREV_OFFSET, block);
		setFirstBlock(blocksize, block);
	}

	/**
	 * Free an allocated block.
	 *
	 * @param address memory address to be freed
	 * @param poolId the same ID that was previously passed into malloc when allocating this memory address
	 */
	public void free(long address, short poolId) throws IndexException {
		assert this.fExclusiveLock;
		if (address == 0) {
			return;
		}
		// TODO Look for opportunities to merge blocks
		long block = address - BLOCK_HEADER_SIZE;
		Chunk chunk = getChunk(block);
		int blocksize = - chunk.getShort(block);
		if (blocksize < 0) {
			// Already freed.
			throw new IndexException(new Status(IStatus.ERROR, Package.PLUGIN_ID, 0,
					"Already freed record " + address, new Exception())); //$NON-NLS-1$
		}
		addBlock(chunk, blocksize, block);
		this.freed += blocksize;
		this.memoryUsage.recordFree(poolId, blocksize);
	}

	public void putByte(long offset, byte value) throws IndexException {
		getChunk(offset).putByte(offset, value);
	}

	public byte getByte(long offset) throws IndexException {
		return getChunk(offset).getByte(offset);
	}

	public void putInt(long offset, int value) throws IndexException {
		getChunk(offset).putInt(offset, value);
	}

	public int getInt(long offset) throws IndexException {
		return getChunk(offset).getInt(offset);
	}

	public void putRecPtr(long offset, long value) throws IndexException {
		getChunk(offset).putRecPtr(offset, value);
	}

	public long getRecPtr(long offset) throws IndexException {
		return getChunk(offset).getRecPtr(offset);
	}

	private void putFreeRecPtr(long offset, long value) throws IndexException {
		getChunk(offset).putFreeRecPtr(offset, value);
	}

	private long getFreeRecPtr(long offset) throws IndexException {
		return getChunk(offset).getFreeRecPtr(offset);
	}

	public void put3ByteUnsignedInt(long offset, int value) throws IndexException {
		getChunk(offset).put3ByteUnsignedInt(offset, value);
	}

	public int get3ByteUnsignedInt(long offset) throws IndexException {
		return getChunk(offset).get3ByteUnsignedInt(offset);
	}

	public void putShort(long offset, short value) throws IndexException {
		getChunk(offset).putShort(offset, value);
	}

	public short getShort(long offset) throws IndexException {
		return getChunk(offset).getShort(offset);
	}

	public void putLong(long offset, long value) throws IndexException {
		getChunk(offset).putLong(offset, value);
	}

	public void putDouble(long offset, double value) throws IndexException {
		getChunk(offset).putDouble(offset, value);
	}

	public void putFloat(long offset, float value) throws IndexException {
		getChunk(offset).putFloat(offset, value);
	}

	public long getLong(long offset) throws IndexException {
		return getChunk(offset).getLong(offset);
	}

	public double getDouble(long offset) throws IndexException {
		return getChunk(offset).getDouble(offset);
	}

	public float getFloat(long offset) throws IndexException {
		return getChunk(offset).getFloat(offset);
	}

	public void putChar(long offset, char value) throws IndexException {
		getChunk(offset).putChar(offset, value);
	}

	public char getChar(long offset) throws IndexException {
		return getChunk(offset).getChar(offset);
	}

	public void clearBytes(long offset, int byteCount) throws IndexException {
		getChunk(offset).clear(offset, byteCount);
	}

	public void putBytes(long offset, byte[] data, int len) throws IndexException {
		getChunk(offset).put(offset, data, len);
	}

	public void putBytes(long offset, byte[] data, int dataPos, int len) throws IndexException {
		getChunk(offset).put(offset, data, dataPos, len);
	}

	public void getBytes(long offset, byte[] data) throws IndexException {
		getChunk(offset).get(offset, data);
	}

	public void getBytes(long offset, byte[] data, int dataPos, int len) throws IndexException {
		getChunk(offset).get(offset, data, dataPos, len);
	}

	public IString newString(String string) throws IndexException {
		return newString(string.toCharArray());
	}

	public IString newString(char[] chars) throws IndexException {
		int len= chars.length;
		int bytelen;
		final boolean useBytes = useBytes(chars);
		if (useBytes) {
			bytelen= len;
		} else {
			bytelen= 2 * len;
		}

		if (bytelen > ShortString.MAX_BYTE_LENGTH) {
			return new LongString(this, chars, useBytes);
		} else {
			return new ShortString(this, chars, useBytes);
		}
	}

	private boolean useBytes(char[] chars) {
		for (char c : chars) {
			if ((c & 0xff00) != 0)
				return false;
		}
		return true;
	}

	public IString getString(long offset) throws IndexException {
		final int l = getInt(offset);
		int bytelen= l < 0 ? -l : 2 * l;
		if (bytelen > ShortString.MAX_BYTE_LENGTH) {
			return new LongString(this, offset);
		}
		return new ShortString(this, offset);
	}

	public long getDatabaseSize() {
		return this.fChunksUsed * CHUNK_SIZE;
	}

	/**
	 * For debugging purposes, only.
	 */
	public void reportFreeBlocks() throws IndexException {
		System.out.println("Allocated size: " + getDatabaseSize() + " bytes"); //$NON-NLS-1$ //$NON-NLS-2$
		System.out.println("malloc'ed: " + this.malloced); //$NON-NLS-1$
		System.out.println("free'd: " + this.freed); //$NON-NLS-1$
		System.out.println("wasted: " + (getDatabaseSize() - (this.malloced - this.freed))); //$NON-NLS-1$
		System.out.println("Free blocks"); //$NON-NLS-1$
		for (int bs = MIN_BLOCK_DELTAS*BLOCK_SIZE_DELTA; bs <= CHUNK_SIZE; bs += BLOCK_SIZE_DELTA) {
			int count = 0;
			long block = getFirstBlock(bs);
			while (block != 0) {
				++count;
				block = getFreeRecPtr(block + BLOCK_NEXT_OFFSET);
			}
			if (count != 0)
				System.out.println("Block size: " + bs + "=" + count); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/**
	 * Closes the database.
	 * <p>
	 * The behavior of any further calls to the Database is undefined
	 * @throws IndexException
	 */
	public void close() throws IndexException {
		assert this.fExclusiveLock;
		flush();
		removeChunksFromCache();

		// Chunks have been removed from the cache, so we are fine.
		this.fHeaderChunk.clear(0, CHUNK_SIZE);
		this.memoryUsage.refresh();
		this.fHeaderChunk.fDirty= false;
		this.fChunks= new Chunk[] { null };
		this.fChunksUsed = this.fChunksAllocated = this.fChunks.length;
		try {
			this.fFile.close();
		} catch (IOException e) {
			throw new IndexException(new DBStatus(e));
		}
	}

	/**
     * This method is public for testing purposes only.
     */
	public File getLocation() {
		return this.fLocation;
	}

	/**
	 * Called from any thread via the cache, protected by {@link #fCache}.
	 */
	void releaseChunk(final Chunk chunk) {
		if (!chunk.fLocked) {
			this.fChunks[chunk.fSequenceNumber]= null;
		}
	}

	/**
	 * Returns the cache used for this database.
	 * @since 4.0
	 */
	public ChunkCache getChunkCache() {
		return this.fCache;
	}

	/**
	 * Asserts that database is used by one thread exclusively. This is necessary when doing
	 * write operations.
	 */
	public void setExclusiveLock() {
		this.fExclusiveLock= true;
		this.fLocked= true;
	}

	public void setLocked(boolean val) {
		this.fLocked= val;
	}

	public boolean giveUpExclusiveLock(final boolean flush) throws IndexException {
		boolean wasInterrupted = false;
		if (this.fExclusiveLock) {
			try {
				ArrayList<Chunk> dirtyChunks= new ArrayList<>();
				synchronized (this.fCache) {
					for (int i= 1; i < this.fChunksUsed; i++) {
						Chunk chunk= this.fChunks[i];
						if (chunk != null) {
							if (chunk.fCacheIndex < 0) {
								// Locked chunk that has been removed from cache.
								if (chunk.fDirty) {
									dirtyChunks.add(chunk); // Keep in fChunks until it is flushed.
								} else {
									chunk.fLocked= false;
									this.fChunks[i]= null;
								}
							} else if (chunk.fLocked) {
								// Locked chunk, still in cache.
								if (chunk.fDirty) {
									if (flush) {
										dirtyChunks.add(chunk);
									}
								} else {
									chunk.fLocked= false;
								}
							} else {
								assert !chunk.fDirty; // Dirty chunks must be locked.
							}
						}
					}
				}
				// Also handles header chunk.
				wasInterrupted = flushAndUnlockChunks(dirtyChunks, flush) || wasInterrupted;
			} finally {
				this.fExclusiveLock= false;
			}
		}
		return wasInterrupted;
	}

	public boolean flush() throws IndexException {
		boolean wasInterrupted = false;
		assert this.fLocked;
		if (this.fExclusiveLock) {
			try {
				wasInterrupted = giveUpExclusiveLock(true) || wasInterrupted;
			} finally {
				setExclusiveLock();
			}
			return wasInterrupted;
		}

		// Be careful as other readers may access chunks concurrently.
		ArrayList<Chunk> dirtyChunks= new ArrayList<>();
		synchronized (this.fCache) {
			for (int i= 1; i < this.fChunksUsed ; i++) {
				Chunk chunk= this.fChunks[i];
				if (chunk != null && chunk.fDirty) {
					dirtyChunks.add(chunk);
				}
			}
		}

		// Also handles header chunk.
		return flushAndUnlockChunks(dirtyChunks, true) || wasInterrupted;
	}

	/**
	 * Interrupting the thread with {@link Thread#interrupt()} won't interrupt the write. Returns true iff an attempt
	 * was made to interrupt the thread with {@link Thread#interrupt()}.
	 * 
	 * @throws IndexException
	 */
	private boolean flushAndUnlockChunks(final ArrayList<Chunk> dirtyChunks, boolean isComplete) throws IndexException {
		boolean wasInterrupted = false;
		assert !Thread.holdsLock(this.fCache);
		synchronized (this.fHeaderChunk) {
			final boolean haveDirtyChunks = !dirtyChunks.isEmpty();
			if (haveDirtyChunks || this.fHeaderChunk.fDirty) {
				wasInterrupted = markFileIncomplete() || wasInterrupted;
			}
			if (haveDirtyChunks) {
				for (Chunk chunk : dirtyChunks) {
					if (chunk.fDirty) {
						wasInterrupted = chunk.flush() || wasInterrupted;
					}
				}

				// Only after the chunks are flushed we may unlock and release them.
				synchronized (this.fCache) {
					for (Chunk chunk : dirtyChunks) {
						chunk.fLocked= false;
						if (chunk.fCacheIndex < 0) {
							this.fChunks[chunk.fSequenceNumber]= null;
						}
					}
				}
			}

			if (isComplete) {
				if (this.fHeaderChunk.fDirty || this.fIsMarkedIncomplete) {
					this.fHeaderChunk.putInt(VERSION_OFFSET, this.fVersion);
					wasInterrupted = this.fHeaderChunk.flush() || wasInterrupted;
					this.fIsMarkedIncomplete= false;
				}
			}
		}
		return wasInterrupted;
	}

	private boolean markFileIncomplete() throws IndexException {
		boolean wasInterrupted = false;
		if (!this.fIsMarkedIncomplete) {
			this.fIsMarkedIncomplete= true;
			try {
				final ByteBuffer buf= ByteBuffer.wrap(new byte[4]);
				wasInterrupted = performUninterruptableWrite(() -> this.fFile.getChannel().write(buf, 0));
			} catch (IOException e) {
				throw new IndexException(new DBStatus(e));
			}
		}
		return wasInterrupted;
	}

	public void resetCacheCounters() {
		this.cacheHits= this.cacheMisses= 0;
	}

	public long getCacheHits() {
		return this.cacheHits;
	}

	public long getCacheMisses() {
		return this.cacheMisses;
	}

	public long getSizeBytes() throws IOException {
		return this.fFile.length();
	}

	/**
	 * A Record Pointer is a pointer as returned by Database.malloc().
	 * This is a pointer to a block + BLOCK_HEADER_SIZE.
	 */
	public static void putRecPtr(final long value, byte[] buffer, int idx) {
		final int denseValue = value == 0 ? 0 : Chunk.compressFreeRecPtr(value - BLOCK_HEADER_SIZE);
		Chunk.putInt(denseValue, buffer, idx);
	}

	/**
	 * A Record Pointer is a pointer as returned by Database.malloc().
	 * This is a pointer to a block + BLOCK_HEADER_SIZE.
	 */
	public static long getRecPtr(byte[] buffer, final int idx) {
		int value = Chunk.getInt(buffer, idx);
		long address = Chunk.expandToFreeRecPtr(value);
		return address != 0 ? (address + BLOCK_HEADER_SIZE) : address;
	}

	public MemoryStats getMemoryStats() {
		return this.memoryUsage;
	}
}
