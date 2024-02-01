package de.symeda.sormas.api.messaging;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FCMResponseDto implements Serializable{

	@JsonProperty("message_id")
	private long messageId;

	public long getMessageId() {
		return messageId;
	}

	public void setMessageId(long messageId) {
		this.messageId = messageId;
	}
}
