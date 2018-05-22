package com.handlers;



import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.github.messenger4j.MessengerPlatform;
import com.github.messenger4j.exceptions.MessengerApiException;
import com.github.messenger4j.exceptions.MessengerIOException;
import com.github.messenger4j.exceptions.MessengerVerificationException;
import com.github.messenger4j.receive.MessengerReceiveClient;
import com.github.messenger4j.receive.handlers.QuickReplyMessageEventHandler;
import com.github.messenger4j.receive.handlers.TextMessageEventHandler;
import com.github.messenger4j.send.MessengerSendClient;
import com.github.messenger4j.send.NotificationType;
import com.github.messenger4j.send.QuickReply;
import com.github.messenger4j.send.Recipient;
import com.github.messenger4j.send.buttons.Button;
import com.github.messenger4j.send.buttons.ShareButton;
import com.github.messenger4j.send.buttons.UrlButton;
import com.github.messenger4j.send.buttons.UrlButton.Builder;
import com.github.messenger4j.send.templates.GenericTemplate;
import com.google.gson.JsonObject;

@RestController
@RequestMapping("/reCall")
public class CallbackHandler {

	private static final Logger logger = LoggerFactory.getLogger(CallbackHandler.class);
	public static final String GOOD_ACTION = "DEVELOPER_DEFINED_PAYLOAD_FOR_GOOD_ACTION";
    public static final String NOT_GOOD_ACTION = "DEVELOPER_DEFINED_PAYLOAD_FOR_NOT_GOOD_ACTION";

	private final MessengerReceiveClient receiveClient;
	private final MessengerSendClient sendClient;

	@Autowired
	public CallbackHandler(@Value("${messenger4j.appSecret}") final String appSecret,
			@Value("${messenger4j.verifyToken}") final String verifyToken, final MessengerSendClient sendClient) {

		logger.debug("Initializing MessengerReceiveClient - appSecret: {} | verifyToken: {}", appSecret, verifyToken);
		this.receiveClient = MessengerPlatform.newReceiveClientBuilder(appSecret, verifyToken)
				.onTextMessageEvent(newTextMessageEventHandler())
				.onQuickReplyMessageEvent(newQuickReplyMessageEventHandler()).build();

		this.sendClient = sendClient;
	}

	

	@RequestMapping(method = RequestMethod.GET)
	public ResponseEntity<String> verifyWebhook(@RequestParam("hub.mode") final String mode,
			@RequestParam("hub.verify_token") final String verifyToken,
			@RequestParam("hub.challenge") final String challenge) {

		logger.debug("Received Webhook verification request - mode: {} | verifyToken: {} | challenge: {}", mode,
				verifyToken, challenge);
		try {
			return ResponseEntity.ok(this.receiveClient.verifyWebhook(mode, verifyToken, challenge));
		} catch (MessengerVerificationException e) {
			logger.warn("Webhook verification failed: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
		}
	}

	@RequestMapping(method = RequestMethod.POST)
	public ResponseEntity<Void> handleCallback(@RequestBody final String payload,
			@RequestHeader("X-Hub-Signature") final String signature, String recpientId) {

		logger.debug("Received Messenger Platform callback - payload: {} | signature: {}", payload, signature);
		try {
		   System.out.println("payload:"+payload);
			System.out.println("INSIDE TRY:"+signature);
			this.receiveClient.processCallbackPayload(payload, signature);
			logger.debug("Processed callback payload successfully");
			return ResponseEntity.status(HttpStatus.OK).build();
		} catch (MessengerVerificationException e) {
			System.out.println("INSIDE CATCH");
			logger.warn("Processing of callback payload failed: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.OK).build();
		}
	}

	private TextMessageEventHandler newTextMessageEventHandler() {

		return event -> {
			logger.debug("Received TextMessageEvent: {}", event);

			final String messageId = event.getMid();
			final String messageText = event.getText();
			final String senderId = event.getSender().getId();
			final Date timestamp = event.getTimestamp();

			logger.info("Received message '{}' with text '{}' from user '{}' at '{}'", messageId, messageText, senderId,
					timestamp);

			switch (messageText.toLowerCase()) {

			case "hello":
				sendTextMessage(senderId, "Hello, What I can do for you ? Type the word you're looking for");
				break;

			case "user":
			sendTextMessage(senderId, "Okay, Make appropriate selection Below.....");
			
				try {
					sendQuickReply(senderId);
					break;
				} catch (MessengerApiException | MessengerIOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			

			default:
				sendTextMessage(senderId, "I do not know what are you talking about");
			}

		};
	}
	
		

		private void sendTextMessage(String recipientId, String text) {

		final Recipient recipient = Recipient.newBuilder().recipientId(recipientId).build();
		final NotificationType notificationType = NotificationType.REGULAR;
		final String metadata = "DEVELOPER_DEFINED_METADATA";

		try {
		  if(recipientId.equals("206469290133580")) {
		        final MessengerSendClient sendClientSecond = MessengerPlatform.newSendClientBuilder("EAAaISLtfMnUBABFUpJWbTqNvVnP7A6ZAVJtZCxtWIkvbLoIsNmmxrvGONYZB2cqQTm18dzAlYZBb9INcrFAO5tpo7aqA5kuPcolP2vInLtgdU5w7CsrDBNps3o585exZBhvptzHWMmK3R81q0qmM1lwvQRhISwpLBFZBCF5UyANAZDZD").build();
		  
			sendClientSecond.sendTextMessage(recipient, notificationType, text, metadata);
		  }else {
		    this.sendClient.sendTextMessage(recipient, notificationType, text, metadata);
		  }
		} catch (MessengerApiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MessengerIOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
		
		private void sendQuickReply(String recipientId) throws MessengerApiException, MessengerIOException {
			final List<QuickReply> quickReplies = QuickReply.newListBuilder()
	                .addTextQuickReply("USER INFO",GOOD_ACTION).toList()
	                .addTextQuickReply("APPOINTMENT INFO",NOT_GOOD_ACTION).toList()
	                .build();
			this.sendClient.sendTextMessage(recipientId, "Was this helpful?!", quickReplies);
		}
		
		private QuickReplyMessageEventHandler newQuickReplyMessageEventHandler() {
			return event -> {
	            logger.debug("Received QuickReplyMessageEvent: {}", event);

	            final String senderId = event.getSender().getId();
	            final String messageId = event.getMid();
	            final String quickReplyPayload = event.getQuickReply().getPayload();

	            logger.info("Received quick reply for message '{}' with payload '{}'", messageId, quickReplyPayload);


	                    if(quickReplyPayload.equals(GOOD_ACTION)) {
	                    String name;
                       	sendTextMessage(senderId,"USER NAME IS :- " +"AMIT BILLORE");
                       	System.out.println("NAME ON CONSOLE :- "+"AMIT BILLORE");
	                    	}
	                    else {
	                       try {
							sendGifMessage(senderId,"https://media.giphy.com/media/26ybx7nkZXtBkEYko/giphy.gif");
	                    	  //com.github.messenger4j.send.buttons.ShareButton.Builder button = ShareButton.newListBuilder().addShareButton(); 
	                    	  
						} catch (MessengerApiException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (MessengerIOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
	                    }
	                    sendTextMessage(senderId, "THANKS FOR SELECTING");

			};	
		}
		 private void sendGifMessage(String recipientId, String gif) throws MessengerApiException, MessengerIOException {
		        this.sendClient.sendImageAttachment(recipientId, gif);
		    }
}
