package com.bornfire.AccountStatement.services;

import java.util.Date;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;



@Service
@Component
public class EmailServices {

	@Autowired
	Environment env;
	
	private static final Logger logger = LoggerFactory.getLogger(EmailServices.class);

	public void sendEmail(String filename, byte[] fileBytes, String fileType) {

	    logger.info("EMAIL STARTS");

	    String host = env.getProperty("mail.host");
	    String user = env.getProperty("mail.username");
	    String password = env.getProperty("mail.password");
	    String port = env.getProperty("mail.port");

	    Properties props = new Properties();
	    props.put("mail.smtp.auth", "true");
	    props.put("mail.smtp.host", host);
	    props.put("mail.smtp.port", port);

	    Session session = Session.getInstance(props, new javax.mail.Authenticator() {
	        protected PasswordAuthentication getPasswordAuthentication() {
	            return new PasswordAuthentication(user, password);
	        }
	    });

	    try {
	        MimeMessage msg = new MimeMessage(session);
	        msg.setFrom(new InternetAddress(user));
	        msg.addRecipient(Message.RecipientType.TO,
	                new InternetAddress("vinothkumarall4@gmail.com"));

	        msg.setSubject("Account Statement");
	        msg.setSentDate(new Date());

	        // Body
	        BodyPart messageBodyPart = new MimeBodyPart();
	        messageBodyPart.setText("Please find the attached file.");

	        // Attachment (FROM MEMORY)
	        MimeBodyPart attachmentPart = new MimeBodyPart();

	        ByteArrayDataSource dataSource =
	                new ByteArrayDataSource(fileBytes, fileType);

	        attachmentPart.setDataHandler(new DataHandler(dataSource));
	        attachmentPart.setFileName(filename);

	        Multipart multipart = new MimeMultipart();
	        multipart.addBodyPart(messageBodyPart);
	        multipart.addBodyPart(attachmentPart);

	        msg.setContent(multipart);

	        Transport.send(msg);

	        logger.info("MAIL SENT SUCCESS");

	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}

}


