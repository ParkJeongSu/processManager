package kr.co.aim.common.format.request;

import lombok.Data;

@Data
public class BaseMessage <T>{
	private String messageName;
	private String transactionId;
	private String messageFrom;
	private String messageOwner;
	private String messageTo;
	private String eventTime;
	private String resultCode;
	private String resultMessage;
	private T body;
}