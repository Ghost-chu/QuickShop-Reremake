package org.maxgamer.quickshop.wrapper;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;

public class WrapperPlayClientUpdateSign extends AbstractPacket {
	public static final PacketType TYPE = PacketType.Play.Client.UPDATE_SIGN;

	public WrapperPlayClientUpdateSign() {
		super(new PacketContainer(TYPE), TYPE);
		handle.getModifier().writeDefaults();
	}

	public WrapperPlayClientUpdateSign(PacketContainer packet) {
		super(packet, TYPE);
	}

	/**
	 * Retrieve Location.
	 * <p>
	 * Notes: block Coordinates
	 * 
	 * @return The current Location
	 */
	public BlockPosition getLocation() {
		return handle.getBlockPositionModifier().read(0);
	}

	/**
	 * Set Location.
	 * 
	 * @param value - new value.
	 */
	public void setLocation(BlockPosition value) {
		handle.getBlockPositionModifier().write(0, value);
	}

	/**
	 * Retrieve this sign's lines of text.
	 * 
	 * @return The current lines
	 */
	public String[] getLines() {
		return handle.getStringArrays().read(0);
	}

	/**
	 * Set this sign's lines of text.
	 * 
	 * @param value - Lines, must be 4 elements long
	 */
	public void setLines(String[] value) {
		if (value == null)
			throw new IllegalArgumentException("value cannot be null!");
		if (value.length != 4)
			throw new IllegalArgumentException("value must have 4 elements!");

		handle.getStringArrays().write(0, value);
	}
}