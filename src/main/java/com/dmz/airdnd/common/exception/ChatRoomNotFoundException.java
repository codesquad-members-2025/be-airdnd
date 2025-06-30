package com.dmz.airdnd.common.exception;

public class ChatRoomNotFoundException extends BaseException {
	public ChatRoomNotFoundException(ErrorCode errorCode) {
		super(errorCode);
	}
}
