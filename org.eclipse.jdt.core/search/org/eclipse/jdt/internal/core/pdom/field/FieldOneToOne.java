package org.eclipse.jdt.internal.core.pdom.field;

import org.eclipse.jdt.internal.core.pdom.Nd;
import org.eclipse.jdt.internal.core.pdom.NdNode;
import org.eclipse.jdt.internal.core.pdom.db.Database;

/**
 * Represents a 1-to-0..1 relationship in a PDOM database.
 * @since 3.12
 */
public class FieldOneToOne<T extends NdNode> implements IField, IDestructableField, IRefCountedField {
	private int offset;
	public final Class<T> nodeType; 
	FieldOneToOne<?> backPointer;
	private boolean pointsToOwner;

	/**
	 * @param nodeType
	 * @param backPointer
	 */
	private FieldOneToOne(Class<T> nodeType, FieldOneToOne<?> backPointer, boolean pointsToOwner) {
		this.nodeType = nodeType;

		if (backPointer != null) {
			if (backPointer.backPointer != null && backPointer.backPointer != this) {
				throw new IllegalArgumentException(
					"Attempted to construct a FieldOneToOne referring to a backpointer list that is already in use" //$NON-NLS-1$
						+ " by another field"); //$NON-NLS-1$
			}
			backPointer.backPointer = this;
		}
		this.backPointer = backPointer;
		this.pointsToOwner = pointsToOwner;
	}

	public static <T extends NdNode, B extends NdNode> FieldOneToOne<T> create(StructDef<B> builder,
			Class<T> nodeType, FieldOneToOne<B> forwardPointer) {

		FieldOneToOne<T> result = new FieldOneToOne<T>(nodeType, forwardPointer, false);
		builder.add(result);
		builder.addDestructableField(result);
		return result;
	}

	public static <T extends NdNode, B extends NdNode> FieldOneToOne<T> createOwner(StructDef<B> builder,
			Class<T> nodeType, FieldOneToOne<B> forwardPointer) {

		FieldOneToOne<T> result = new FieldOneToOne<T>(nodeType, forwardPointer, true);
		builder.add(result);
		builder.addDestructableField(result);
		builder.addOwnerField(result);
		return result;
	}

	public T get(Nd pdom, long address) {
		long ptr = pdom.getDB().getRecPtr(address + this.offset);
		return NdNode.load(pdom, ptr, this.nodeType);
	}

	public void put(Nd pdom, long address, T target) {
		cleanup(pdom, address);
		pdom.getDB().putRecPtr(address + this.offset, target == null ? 0 : target.address);
		if (target == null && this.pointsToOwner) {
			pdom.scheduleDeletion(address);
		}
	}

	@Override
	public void destruct(Nd pdom, long address) {
		cleanup(pdom, address);
	}

	private void cleanup(Nd pdom, long address) {
		Database db = pdom.getDB();
		long ptr = db.getRecPtr(address + this.offset);
		if (ptr != 0) {
			db.putRecPtr(ptr + this.backPointer.offset, 0);
			// If we own our target, delete it
			if (this.backPointer.pointsToOwner) {
				pdom.scheduleDeletion(ptr);
			}
		}
	}

	@Override
	public void setOffset(int offset) {
		this.offset = offset;
	}

	@Override
	public int getRecordSize() {
		return Database.PTR_SIZE;
	}

	@Override
	public boolean hasReferences(Nd pdom, long address) {
		if (this.pointsToOwner) {
			long ptr = pdom.getDB().getRecPtr(address + this.offset);
			return ptr != 0;
		}
		return false;
	}
}
